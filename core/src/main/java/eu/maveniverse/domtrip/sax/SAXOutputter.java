/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.sax;

import eu.maveniverse.domtrip.Comment;
import eu.maveniverse.domtrip.ContainerNode;
import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.Element;
import eu.maveniverse.domtrip.NamespaceResolver;
import eu.maveniverse.domtrip.Node;
import eu.maveniverse.domtrip.ProcessingInstruction;
import eu.maveniverse.domtrip.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.XMLConstants;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Emits SAX {@link ContentHandler} events from a domtrip {@link Document} or {@link Element} tree.
 *
 * <p>This enables domtrip to participate in SAX-based XML processing pipelines such as
 * XSLT processors, XML validators, serializers, and content pipelines. Rather than
 * serializing to a string and re-parsing, the SAXOutputter walks the domtrip tree
 * directly and emits the corresponding SAX events.</p>
 *
 * <p><strong>Note:</strong> Formatting preservation is intentionally lost at this boundary
 * since SAX events do not carry formatting metadata. The value is interoperability,
 * not round-tripping through SAX.</p>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * Document doc = Document.of(xml);
 *
 * // Feed into a ContentHandler
 * SAXOutputter outputter = new SAXOutputter();
 * outputter.output(doc, contentHandler);
 *
 * // With a LexicalHandler for comments and CDATA
 * outputter.output(doc, contentHandler, lexicalHandler);
 *
 * // Output a single element subtree
 * outputter.output(element, contentHandler);
 * }</pre>
 *
 * <h3>Namespace Handling:</h3>
 * <p>The outputter emits {@code startPrefixMapping}/{@code endPrefixMapping} events
 * for namespace declarations on each element. By default, namespace declaration
 * attributes ({@code xmlns}, {@code xmlns:prefix}) are <em>not</em> included in the
 * {@code Attributes} parameter of {@code startElement}. Set
 * {@link #setReportNamespaceDeclarations(boolean)} to {@code true} to include them,
 * matching the SAX {@code namespace-prefixes} feature behavior.</p>
 *
 * @implNote This class is not thread-safe. External synchronization is required for concurrent access.
 *
 * @see DomTripSAXSource
 * @see DomTripXMLReader
 * @since 1.3.0
 */
public class SAXOutputter {

    private static final String XMLNS = "xmlns";
    private static final String XMLNS_PREFIX = "xmlns:";
    private static final String CDATA_TYPE = "CDATA";

    private boolean reportNamespaceDeclarations;

    /**
     * Creates a new SAXOutputter with default settings.
     */
    public SAXOutputter() {
        this.reportNamespaceDeclarations = false;
    }

    /**
     * Returns whether namespace declarations are reported as attributes.
     *
     * @return {@code true} if namespace declarations are included in {@code startElement} attributes
     */
    public boolean isReportNamespaceDeclarations() {
        return reportNamespaceDeclarations;
    }

    /**
     * Sets whether namespace declarations should be reported as attributes
     * in {@code startElement} calls.
     *
     * <p>When {@code true}, {@code xmlns} and {@code xmlns:prefix} attributes are
     * included in the {@code Attributes} parameter, matching the SAX
     * {@code namespace-prefixes} feature.</p>
     *
     * @param reportNamespaceDeclarations {@code true} to include namespace declaration attributes
     */
    public void setReportNamespaceDeclarations(boolean reportNamespaceDeclarations) {
        this.reportNamespaceDeclarations = reportNamespaceDeclarations;
    }

    /**
     * Emits SAX events for the given document to the specified content handler.
     *
     * @param doc the document to output
     * @param handler the content handler to receive events
     * @throws SAXException if the content handler reports an error
     * @throws IllegalArgumentException if doc or handler is null
     */
    public void output(Document doc, ContentHandler handler) throws SAXException {
        output(doc, handler, null);
    }

    /**
     * Emits SAX events for the given document to the specified content and lexical handlers.
     *
     * <p>The lexical handler receives events for comments and CDATA sections.
     * If {@code null}, comments and CDATA boundaries are silently skipped
     * (CDATA text content is still emitted as regular characters).</p>
     *
     * @param doc the document to output
     * @param handler the content handler to receive events
     * @param lexicalHandler the lexical handler for comments and CDATA, or null
     * @throws SAXException if a handler reports an error
     * @throws IllegalArgumentException if doc or handler is null
     */
    public void output(Document doc, ContentHandler handler, LexicalHandler lexicalHandler) throws SAXException {
        if (doc == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }
        if (handler == null) {
            throw new IllegalArgumentException("ContentHandler cannot be null");
        }

        handler.startDocument();

        // Process all children in document order (snapshot for consistent root check)
        List<Node> snapshot = doc.children().collect(Collectors.toList());
        for (Node child : snapshot) {
            processNode(child, handler, lexicalHandler);
        }

        // Process root element if not already in children list
        Element root = doc.root();
        if (root != null && !snapshot.contains(root)) {
            processNode(root, handler, lexicalHandler);
        }

        handler.endDocument();
    }

    /**
     * Emits SAX events for the given element subtree to the specified content handler.
     *
     * <p>Unlike {@link #output(Document, ContentHandler)}, this method does not
     * emit {@code startDocument}/{@code endDocument} events.</p>
     *
     * @param element the element to output
     * @param handler the content handler to receive events
     * @throws SAXException if the content handler reports an error
     * @throws IllegalArgumentException if element or handler is null
     */
    public void output(Element element, ContentHandler handler) throws SAXException {
        output(element, handler, null);
    }

    /**
     * Emits SAX events for the given element subtree to the specified handlers.
     *
     * <p>Unlike {@link #output(Document, ContentHandler, LexicalHandler)}, this
     * method does not emit {@code startDocument}/{@code endDocument} events.</p>
     *
     * @param element the element to output
     * @param handler the content handler to receive events
     * @param lexicalHandler the lexical handler for comments and CDATA, or null
     * @throws SAXException if a handler reports an error
     * @throws IllegalArgumentException if element or handler is null
     */
    public void output(Element element, ContentHandler handler, LexicalHandler lexicalHandler) throws SAXException {
        if (element == null) {
            throw new IllegalArgumentException("Element cannot be null");
        }
        if (handler == null) {
            throw new IllegalArgumentException("ContentHandler cannot be null");
        }

        processElement(element, handler, lexicalHandler);
    }

    private void processNode(Node node, ContentHandler handler, LexicalHandler lexicalHandler) throws SAXException {
        if (node instanceof Element) {
            processElement((Element) node, handler, lexicalHandler);
        } else if (node instanceof Text) {
            processText((Text) node, handler, lexicalHandler);
        } else if (node instanceof Comment) {
            processComment((Comment) node, lexicalHandler);
        } else if (node instanceof ProcessingInstruction) {
            processPI((ProcessingInstruction) node, handler);
        }
    }

    private void processElement(Element element, ContentHandler handler, LexicalHandler lexicalHandler)
            throws SAXException {
        // Separate namespace declarations from regular attributes
        List<String[]> nsDecls = new ArrayList<>();
        AttributesImpl attrs = buildAttributes(element, nsDecls);

        // Emit startPrefixMapping for each namespace declaration
        for (String[] nsDecl : nsDecls) {
            handler.startPrefixMapping(nsDecl[0], nsDecl[1]);
        }

        // Resolve element namespace URI
        String localName = element.localName();
        String prefix = element.prefix();
        String qName = element.name();
        String uri = resolveElementNamespace(element, prefix);

        handler.startElement(uri, localName, qName, attrs);

        // Process children
        for (Node child : collectChildren(element)) {
            processNode(child, handler, lexicalHandler);
        }

        handler.endElement(uri, localName, qName);

        // Emit endPrefixMapping in reverse order
        for (int i = nsDecls.size() - 1; i >= 0; i--) {
            handler.endPrefixMapping(nsDecls.get(i)[0]);
        }
    }

    private AttributesImpl buildAttributes(Element element, List<String[]> nsDecls) {
        AttributesImpl attrs = new AttributesImpl();

        for (Map.Entry<String, String> entry : element.attributes().entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();

            if (XMLNS.equals(name)) {
                nsDecls.add(new String[] {"", value});
                if (reportNamespaceDeclarations) {
                    attrs.addAttribute(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, XMLNS, XMLNS, CDATA_TYPE, value);
                }
            } else if (name.startsWith(XMLNS_PREFIX)) {
                String prefix = name.substring(XMLNS_PREFIX.length());
                nsDecls.add(new String[] {prefix, value});
                if (reportNamespaceDeclarations) {
                    attrs.addAttribute(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, prefix, name, CDATA_TYPE, value);
                }
            } else {
                addRegularAttribute(element, attrs, name, value);
            }
        }

        return attrs;
    }

    private static void addRegularAttribute(Element element, AttributesImpl attrs, String name, String value) {
        String attrUri = "";
        String attrLocalName = name;
        int colonIdx = name.indexOf(':');
        if (colonIdx > 0) {
            String attrPrefix = name.substring(0, colonIdx);
            attrLocalName = name.substring(colonIdx + 1);
            String resolved = NamespaceResolver.resolveNamespaceURI(element, attrPrefix);
            if (resolved != null) {
                attrUri = resolved;
            }
        }
        attrs.addAttribute(attrUri, attrLocalName, name, CDATA_TYPE, value);
    }

    private void processText(Text text, ContentHandler handler, LexicalHandler lexicalHandler) throws SAXException {
        String content = text.content();
        if (content == null || content.isEmpty()) {
            return;
        }

        char[] chars = content.toCharArray();

        if (text.cdata()) {
            if (lexicalHandler != null) {
                lexicalHandler.startCDATA();
            }
            handler.characters(chars, 0, chars.length);
            if (lexicalHandler != null) {
                lexicalHandler.endCDATA();
            }
        } else {
            handler.characters(chars, 0, chars.length);
        }
    }

    private void processComment(Comment comment, LexicalHandler lexicalHandler) throws SAXException {
        if (lexicalHandler == null) {
            return;
        }
        String content = comment.content();
        if (content != null) {
            char[] chars = content.toCharArray();
            lexicalHandler.comment(chars, 0, chars.length);
        }
    }

    private void processPI(ProcessingInstruction pi, ContentHandler handler) throws SAXException {
        String target = pi.target();
        String data = pi.data();
        handler.processingInstruction(target, data != null ? data : "");
    }

    private static String resolveElementNamespace(Element element, String prefix) {
        String uri = NamespaceResolver.resolveNamespaceURI(element, prefix);
        return uri != null ? uri : "";
    }

    private static List<Node> collectChildren(ContainerNode container) {
        return container.children().collect(Collectors.toList());
    }
}
