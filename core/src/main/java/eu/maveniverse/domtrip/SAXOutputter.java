/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Emits SAX {@link ContentHandler} events from a domtrip {@link Document} tree.
 *
 * <p>This enables domtrip to participate in SAX-based XML processing pipelines,
 * bridging the gap with dom4j's {@code SAXWriter} and JDOM2's {@code SAXOutputter}.
 * The outputter walks the domtrip tree depth-first and emits the corresponding
 * SAX events for each node.</p>
 *
 * <p>Note: formatting preservation is intentionally lost at the SAX boundary &mdash;
 * SAX events don't carry formatting metadata. The value is interop, not round-tripping
 * through SAX.</p>
 *
 * <h3>Basic Usage:</h3>
 * <pre>{@code
 * Document doc = Document.of(xml);
 *
 * SAXOutputter outputter = new SAXOutputter();
 * outputter.output(doc, contentHandler);
 * }</pre>
 *
 * <h3>With LexicalHandler:</h3>
 * <pre>{@code
 * SAXOutputter outputter = new SAXOutputter();
 * outputter.output(doc, contentHandler, lexicalHandler);
 * }</pre>
 *
 * @see DomTripSAXSource
 * @see DomTripXMLReader
 * @since 1.3.0
 */
public class SAXOutputter {

    private boolean reportNamespaceDeclarations;

    /**
     * Creates a new SAXOutputter with default settings.
     */
    public SAXOutputter() {
        this.reportNamespaceDeclarations = false;
    }

    /**
     * Returns whether namespace declarations ({@code xmlns} attributes) are also
     * reported as regular attributes in {@code startElement}.
     *
     * @return true if namespace declarations are reported as attributes
     */
    public boolean isReportNamespaceDeclarations() {
        return reportNamespaceDeclarations;
    }

    /**
     * Sets whether namespace declarations ({@code xmlns} attributes) should also
     * be reported as regular attributes in {@code startElement}.
     *
     * <p>By default this is {@code false}, matching the SAX2 default where namespace
     * declarations are reported only via {@code startPrefixMapping} /
     * {@code endPrefixMapping}. Set to {@code true} to also include them in the
     * {@code Attributes} parameter of {@code startElement}.</p>
     *
     * @param reportNamespaceDeclarations true to report namespace declarations as attributes
     */
    public void setReportNamespaceDeclarations(boolean reportNamespaceDeclarations) {
        this.reportNamespaceDeclarations = reportNamespaceDeclarations;
    }

    /**
     * Outputs SAX events for the given document to the specified content handler.
     *
     * @param document the domtrip document to output
     * @param handler the SAX content handler to receive events
     * @throws SAXException if the content handler reports an error
     * @throws IllegalArgumentException if document or handler is null
     */
    public void output(Document document, ContentHandler handler) throws SAXException {
        output(document, handler, null);
    }

    /**
     * Outputs SAX events for the given document to the specified handlers.
     *
     * @param document the domtrip document to output
     * @param handler the SAX content handler to receive events
     * @param lexicalHandler optional lexical handler for comments and CDATA sections (may be null)
     * @throws SAXException if a handler reports an error
     * @throws IllegalArgumentException if document or handler is null
     */
    public void output(Document document, ContentHandler handler, LexicalHandler lexicalHandler) throws SAXException {
        if (document == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }
        if (handler == null) {
            throw new IllegalArgumentException("ContentHandler cannot be null");
        }

        handler.startDocument();

        // Walk document children (includes root element and any PIs/comments)
        for (Node child : new ArrayList<>(document.children)) {
            outputNode(child, handler, lexicalHandler);
        }
        // Visit root element if set and not already in children
        if (document.root() != null && !document.children.contains(document.root())) {
            outputNode(document.root(), handler, lexicalHandler);
        }

        handler.endDocument();
    }

    private void outputNode(Node node, ContentHandler handler, LexicalHandler lexicalHandler) throws SAXException {
        switch (node.type()) {
            case ELEMENT:
                outputElement((Element) node, handler, lexicalHandler);
                break;
            case TEXT:
                outputText((Text) node, handler, lexicalHandler);
                break;
            case COMMENT:
                outputComment((Comment) node, lexicalHandler);
                break;
            case PROCESSING_INSTRUCTION:
                outputProcessingInstruction((ProcessingInstruction) node, handler);
                break;
            default:
                break;
        }
    }

    private void outputElement(Element element, ContentHandler handler, LexicalHandler lexicalHandler)
            throws SAXException {
        // Collect namespace declarations from this element's attributes
        List<String[]> namespaceMappings = collectNamespaceDeclarations(element);

        // Emit startPrefixMapping for each namespace declaration
        for (String[] mapping : namespaceMappings) {
            handler.startPrefixMapping(mapping[0], mapping[1]);
        }

        // Build SAX attributes (excluding namespace declarations unless configured)
        AttributesImpl attrs = buildAttributes(element);

        // Resolve element namespace
        String[] parts = NamespaceResolver.splitQualifiedName(element.name());
        String prefix = parts[0];
        String localName = parts[1];
        String namespaceURI = "";
        if (prefix != null) {
            String resolved = NamespaceResolver.resolveNamespaceURI(element, prefix);
            if (resolved != null) {
                namespaceURI = resolved;
            }
        } else {
            // Check default namespace
            String defaultNs = NamespaceResolver.resolveNamespaceURI(element, null);
            if (defaultNs != null) {
                namespaceURI = defaultNs;
            }
        }

        handler.startElement(namespaceURI, localName, element.name(), attrs);

        // Walk children
        for (Node child : new ArrayList<>(element.children)) {
            outputNode(child, handler, lexicalHandler);
        }

        handler.endElement(namespaceURI, localName, element.name());

        // Emit endPrefixMapping in reverse order
        for (int i = namespaceMappings.size() - 1; i >= 0; i--) {
            handler.endPrefixMapping(namespaceMappings.get(i)[0]);
        }
    }

    private void outputText(Text text, ContentHandler handler, LexicalHandler lexicalHandler) throws SAXException {
        String content = text.content();
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

    private void outputComment(Comment comment, LexicalHandler lexicalHandler) throws SAXException {
        if (lexicalHandler != null) {
            char[] chars = comment.content().toCharArray();
            lexicalHandler.comment(chars, 0, chars.length);
        }
    }

    private void outputProcessingInstruction(ProcessingInstruction pi, ContentHandler handler) throws SAXException {
        handler.processingInstruction(pi.target(), pi.data());
    }

    /**
     * Collects namespace declarations from an element's attributes.
     *
     * @return list of {prefix, uri} pairs; prefix is "" for default namespace
     */
    private List<String[]> collectNamespaceDeclarations(Element element) {
        List<String[]> mappings = new ArrayList<>();
        for (Map.Entry<String, String> entry : element.attributes().entrySet()) {
            String attrName = entry.getKey();
            String attrValue = entry.getValue();

            if (Element.XMLNS.equals(attrName)) {
                // Default namespace declaration
                mappings.add(new String[] {"", attrValue});
            } else if (attrName.startsWith(Element.XMLNS_PREFIX)) {
                // Prefixed namespace declaration
                String prefix = attrName.substring(Element.XMLNS_PREFIX.length());
                mappings.add(new String[] {prefix, attrValue});
            }
        }
        return mappings;
    }

    /**
     * Builds SAX Attributes from element attributes, optionally excluding namespace declarations.
     */
    private AttributesImpl buildAttributes(Element element) {
        AttributesImpl attrs = new AttributesImpl();

        for (Map.Entry<String, String> entry : element.attributes().entrySet()) {
            String attrName = entry.getKey();
            String attrValue = entry.getValue();

            // Skip namespace declarations unless reporting them
            if (isNamespaceDeclaration(attrName)) {
                if (reportNamespaceDeclarations) {
                    String nsUri = NamespaceResolver.XMLNS_NAMESPACE_URI;
                    String localName;
                    if (Element.XMLNS.equals(attrName)) {
                        localName = Element.XMLNS;
                    } else {
                        localName = attrName.substring(Element.XMLNS_PREFIX.length());
                    }
                    attrs.addAttribute(nsUri, localName, attrName, "CDATA", attrValue);
                }
                continue;
            }

            // Regular attribute
            String[] parts = NamespaceResolver.splitQualifiedName(attrName);
            String prefix = parts[0];
            String localName = parts[1];
            String namespaceURI = "";
            if (prefix != null) {
                String resolved = NamespaceResolver.resolveNamespaceURI(element, prefix);
                if (resolved != null) {
                    namespaceURI = resolved;
                }
            }

            attrs.addAttribute(namespaceURI, localName, attrName, "CDATA", attrValue);
        }

        return attrs;
    }

    private static boolean isNamespaceDeclaration(String attrName) {
        return Element.XMLNS.equals(attrName) || attrName.startsWith(Element.XMLNS_PREFIX);
    }
}
