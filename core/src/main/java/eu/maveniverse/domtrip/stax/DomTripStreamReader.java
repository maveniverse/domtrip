/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.stax;

import eu.maveniverse.domtrip.Comment;
import eu.maveniverse.domtrip.ContainerNode;
import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.Element;
import eu.maveniverse.domtrip.NamespaceResolver;
import eu.maveniverse.domtrip.Node;
import eu.maveniverse.domtrip.ProcessingInstruction;
import eu.maveniverse.domtrip.Text;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Pull-based {@link XMLStreamReader} implementation backed by a domtrip {@link Document}.
 *
 * <p>This enables domtrip to participate in StAX-based XML processing pipelines.
 * Rather than serializing to a string and re-parsing, the reader walks the domtrip tree
 * directly and exposes it as a sequence of StAX events via the cursor API.</p>
 *
 * <p><strong>Note:</strong> Formatting preservation is intentionally lost at this boundary
 * since StAX events do not carry formatting metadata. The value is interoperability,
 * not round-tripping through StAX.</p>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * Document doc = Document.of(xml);
 *
 * // Create a StAX reader
 * DomTripStreamReader reader = new DomTripStreamReader(doc);
 * while (reader.hasNext()) {
 *     int event = reader.next();
 *     // process events...
 * }
 *
 * // Use with JAXP via StAXSource
 * StAXSource source = DomTripStAXSource.of(doc);
 * transformer.transform(source, result);
 * }</pre>
 *
 * @see DomTripStAXSource
 * @since 1.3.0
 */
public class DomTripStreamReader implements XMLStreamReader {

    private static final String XMLNS = "xmlns";
    private static final String XMLNS_PREFIX = "xmlns:";
    private static final String CDATA_TYPE = "CDATA";

    private final Document document;
    private int eventType;
    private Node currentNode;
    private Element currentElement;
    private boolean closed;

    private final Deque<Frame> frameStack = new ArrayDeque<>();

    // Cached element info — rebuilt at each START_ELEMENT/END_ELEMENT
    private List<String[]> namespaceDecls;
    private List<String[]> attributeList;

    private static class Frame {
        final ContainerNode container;
        final List<Node> children;
        int childIndex;

        Frame(ContainerNode container) {
            this.container = container;
            this.children = collectChildren(container);
            this.childIndex = 0;
        }

        private static List<Node> collectChildren(ContainerNode container) {
            List<Node> result = new ArrayList<>();
            container.children().forEach(result::add);
            if (container instanceof Document) {
                Element root = ((Document) container).root();
                if (root != null && !result.contains(root)) {
                    result.add(root);
                }
            }
            return result;
        }
    }

    /**
     * Creates a new StAX reader for the given document.
     *
     * <p>The reader starts at {@link XMLStreamConstants#START_DOCUMENT}.</p>
     *
     * @param document the document to read
     * @throws IllegalArgumentException if document is null
     */
    public DomTripStreamReader(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }
        this.document = document;
        this.eventType = XMLStreamConstants.START_DOCUMENT;
        this.currentNode = document;
        this.closed = false;
        this.namespaceDecls = Collections.emptyList();
        this.attributeList = Collections.emptyList();
    }

    // ── Core traversal ──────────────────────────────────────────────────

    @Override
    public int next() throws XMLStreamException {
        checkClosed();
        return advance();
    }

    private int advance() {
        switch (eventType) {
            case XMLStreamConstants.START_DOCUMENT:
                frameStack.push(new Frame(document));
                return advanceToNextOrEnd();
            case XMLStreamConstants.START_ELEMENT:
                frameStack.push(new Frame(currentElement));
                return advanceToNextOrEnd();
            case XMLStreamConstants.END_ELEMENT:
                frameStack.pop();
                return advanceToNextOrEnd();
            case XMLStreamConstants.END_DOCUMENT:
                throw new NoSuchElementException("No more events after END_DOCUMENT");
            default:
                // CHARACTERS, CDATA, COMMENT, PI → advance to next sibling
                return advanceToNextOrEnd();
        }
    }

    private int advanceToNextOrEnd() {
        Frame frame = frameStack.peek();
        if (frame != null && frame.childIndex < frame.children.size()) {
            return processChild(frame.children.get(frame.childIndex++));
        }
        return endContainer(frame);
    }

    private int processChild(Node child) {
        currentNode = child;
        if (child instanceof Element) {
            currentElement = (Element) child;
            cacheElementInfo(currentElement);
            eventType = XMLStreamConstants.START_ELEMENT;
        } else {
            // For non-element events, update currentElement to the enclosing element
            // so namespace resolution (getNamespaceURI, getNamespaceContext) resolves
            // against the correct container, not a previously visited sibling element.
            Frame frame = frameStack.peek();
            if (frame != null && frame.container instanceof Element) {
                currentElement = (Element) frame.container;
            } else {
                currentElement = null;
            }
            if (child instanceof Text) {
                eventType = ((Text) child).cdata() ? XMLStreamConstants.CDATA : XMLStreamConstants.CHARACTERS;
            } else if (child instanceof Comment) {
                eventType = XMLStreamConstants.COMMENT;
            } else if (child instanceof ProcessingInstruction) {
                eventType = XMLStreamConstants.PROCESSING_INSTRUCTION;
            } else {
                eventType = XMLStreamConstants.CHARACTERS;
            }
        }
        return eventType;
    }

    private int endContainer(Frame frame) {
        if (frame != null && frame.container instanceof Element) {
            currentElement = (Element) frame.container;
            cacheElementInfo(currentElement);
            eventType = XMLStreamConstants.END_ELEMENT;
        } else {
            currentElement = null;
            eventType = XMLStreamConstants.END_DOCUMENT;
        }
        return eventType;
    }

    @Override
    public boolean hasNext() throws XMLStreamException {
        checkClosed();
        return eventType != XMLStreamConstants.END_DOCUMENT;
    }

    @Override
    public int getEventType() {
        return eventType;
    }

    // ── Element name info ───────────────────────────────────────────────

    @Override
    public QName getName() {
        requireElementState();
        String uri = resolveElementNamespace(currentElement);
        return new QName(uri, currentElement.localName(), prefixOrEmpty(currentElement));
    }

    @Override
    public String getLocalName() {
        requireElementState();
        return currentElement.localName();
    }

    @Override
    public String getPrefix() {
        requireElementState();
        return currentElement.prefix();
    }

    @Override
    public String getNamespaceURI() {
        requireElementState();
        return NamespaceResolver.resolveNamespaceURI(currentElement, currentElement.prefix());
    }

    // ── Attribute access (START_ELEMENT only) ───────────────────────────

    @Override
    public int getAttributeCount() {
        requireStartElementState();
        return attributeList.size();
    }

    @Override
    public QName getAttributeName(int index) {
        requireStartElementState();
        String[] attr = attributeList.get(index);
        return new QName(attr[0], attr[1], attr[2]);
    }

    @Override
    public String getAttributeLocalName(int index) {
        requireStartElementState();
        return attributeList.get(index)[1];
    }

    @Override
    public String getAttributePrefix(int index) {
        requireStartElementState();
        return attributeList.get(index)[2];
    }

    @Override
    public String getAttributeNamespace(int index) {
        requireStartElementState();
        return attributeList.get(index)[0];
    }

    @Override
    public String getAttributeType(int index) {
        requireStartElementState();
        checkAttributeIndex(index);
        return CDATA_TYPE;
    }

    @Override
    public String getAttributeValue(int index) {
        requireStartElementState();
        return attributeList.get(index)[4];
    }

    @Override
    public String getAttributeValue(String namespaceURI, String localName) {
        requireStartElementState();
        for (String[] attr : attributeList) {
            if (matchesAttribute(attr, namespaceURI, localName)) {
                return attr[4];
            }
        }
        return null;
    }

    @Override
    public boolean isAttributeSpecified(int index) {
        requireStartElementState();
        checkAttributeIndex(index);
        return true;
    }

    // ── Namespace access ────────────────────────────────────────────────

    @Override
    public int getNamespaceCount() {
        requireElementState();
        return namespaceDecls.size();
    }

    @Override
    public String getNamespacePrefix(int index) {
        requireElementState();
        return namespaceDecls.get(index)[0];
    }

    @Override
    public String getNamespaceURI(int index) {
        requireElementState();
        return namespaceDecls.get(index)[1];
    }

    @Override
    public String getNamespaceURI(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("Prefix cannot be null");
        }
        String effectivePrefix = prefix.isEmpty() ? null : prefix;
        return NamespaceResolver.resolveNamespaceURI(currentElement, effectivePrefix);
    }

    @Override
    public javax.xml.namespace.NamespaceContext getNamespaceContext() {
        return new StAXNamespaceContext(currentElement);
    }

    // ── Text access ─────────────────────────────────────────────────────

    @Override
    public String getText() {
        requireTextState();
        return getTextContent();
    }

    @Override
    public char[] getTextCharacters() {
        requireTextState();
        return getTextContent().toCharArray();
    }

    @Override
    public int getTextStart() {
        requireTextState();
        return 0;
    }

    @Override
    public int getTextLength() {
        requireTextState();
        return getTextContent().length();
    }

    @Override
    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length)
            throws XMLStreamException {
        requireTextState();
        if (target == null) {
            throw new NullPointerException("target cannot be null");
        }
        if (sourceStart < 0 || targetStart < 0 || length < 0) {
            throw new IndexOutOfBoundsException("sourceStart, targetStart, and length must be non-negative");
        }
        char[] chars = getTextContent().toCharArray();
        if (sourceStart > chars.length) {
            throw new IndexOutOfBoundsException(
                    "sourceStart (" + sourceStart + ") > source length (" + chars.length + ")");
        }
        int count = Math.min(length, chars.length - sourceStart);
        if (targetStart + count > target.length) {
            throw new IndexOutOfBoundsException("target too small for requested copy");
        }
        if (count > 0) {
            System.arraycopy(chars, sourceStart, target, targetStart, count);
        }
        return count;
    }

    // ── Processing instruction access ───────────────────────────────────

    @Override
    public String getPITarget() {
        requireState(XMLStreamConstants.PROCESSING_INSTRUCTION);
        return ((ProcessingInstruction) currentNode).target();
    }

    @Override
    public String getPIData() {
        requireState(XMLStreamConstants.PROCESSING_INSTRUCTION);
        String data = ((ProcessingInstruction) currentNode).data();
        return data != null ? data : "";
    }

    // ── Document properties ─────────────────────────────────────────────

    @Override
    public String getEncoding() {
        return document.encoding();
    }

    @Override
    public String getVersion() {
        return document.version();
    }

    @Override
    public String getCharacterEncodingScheme() {
        return document.encoding();
    }

    @Override
    public boolean isStandalone() {
        return document.isStandalone();
    }

    @Override
    public boolean standaloneSet() {
        String xmlDecl = document.xmlDeclaration();
        return xmlDecl != null && xmlDecl.contains("standalone");
    }

    // ── Convenience queries ─────────────────────────────────────────────

    @Override
    public boolean isStartElement() {
        return eventType == XMLStreamConstants.START_ELEMENT;
    }

    @Override
    public boolean isEndElement() {
        return eventType == XMLStreamConstants.END_ELEMENT;
    }

    @Override
    public boolean isCharacters() {
        return eventType == XMLStreamConstants.CHARACTERS;
    }

    @Override
    public boolean isWhiteSpace() {
        if (eventType == XMLStreamConstants.CHARACTERS || eventType == XMLStreamConstants.CDATA) {
            return getText().trim().isEmpty();
        }
        return false;
    }

    @Override
    public boolean hasName() {
        return eventType == XMLStreamConstants.START_ELEMENT || eventType == XMLStreamConstants.END_ELEMENT;
    }

    @Override
    public boolean hasText() {
        return eventType == XMLStreamConstants.CHARACTERS
                || eventType == XMLStreamConstants.CDATA
                || eventType == XMLStreamConstants.COMMENT;
    }

    // ── Complex operations ──────────────────────────────────────────────

    @Override
    public void require(int type, String namespaceURI, String localName) throws XMLStreamException {
        if (eventType != type) {
            throw new XMLStreamException("Expected event type " + type + " but got " + eventType);
        }
        if (hasName()) {
            requireMatchingNamespace(namespaceURI);
            requireMatchingLocalName(localName);
        }
    }

    private void requireMatchingNamespace(String expected) throws XMLStreamException {
        if (expected != null) {
            String actual = getNamespaceURI();
            // Normalize: both "" and null represent "no namespace"
            if (actual == null) {
                actual = "";
            }
            if (!expected.equals(actual)) {
                throw new XMLStreamException("Expected namespace URI '" + expected + "'");
            }
        }
    }

    private void requireMatchingLocalName(String expected) throws XMLStreamException {
        if (expected != null && !expected.equals(getLocalName())) {
            throw new XMLStreamException("Expected local name '" + expected + "'");
        }
    }

    @Override
    public String getElementText() throws XMLStreamException {
        requireStartElementState();
        StringBuilder sb = new StringBuilder();
        int event = next();
        while (event != XMLStreamConstants.END_ELEMENT) {
            if (event == XMLStreamConstants.START_ELEMENT) {
                throw new XMLStreamException("Unexpected nested element in getElementText()");
            }
            if (isTextEvent(event)) {
                sb.append(getText());
            }
            event = next();
        }
        return sb.toString();
    }

    @Override
    public int nextTag() throws XMLStreamException {
        int event = next();
        while (isSkippableForNextTag(event)) {
            if (isNonWhitespaceText(event)) {
                throw new XMLStreamException("Non-whitespace text encountered in nextTag()");
            }
            event = next();
        }
        if (event != XMLStreamConstants.START_ELEMENT && event != XMLStreamConstants.END_ELEMENT) {
            throw new XMLStreamException("Expected START_ELEMENT or END_ELEMENT, got " + event);
        }
        return event;
    }

    // ── Location ────────────────────────────────────────────────────────

    @Override
    public Location getLocation() {
        return UNKNOWN_LOCATION;
    }

    // ── Property ────────────────────────────────────────────────────────

    @Override
    public Object getProperty(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Property name cannot be null");
        }
        if ("javax.xml.stream.isValidating".equals(name)) {
            return Boolean.FALSE;
        }
        if ("javax.xml.stream.isNamespaceAware".equals(name)) {
            return Boolean.TRUE;
        }
        return null;
    }

    // ── Lifecycle ───────────────────────────────────────────────────────

    @Override
    public void close() {
        closed = true;
    }

    // ── Private helpers ─────────────────────────────────────────────────

    private void checkClosed() throws XMLStreamException {
        if (closed) {
            throw new XMLStreamException("Reader has been closed");
        }
    }

    private void requireElementState() {
        if (eventType != XMLStreamConstants.START_ELEMENT && eventType != XMLStreamConstants.END_ELEMENT) {
            throw new IllegalStateException("Not at START_ELEMENT or END_ELEMENT (current: " + eventType + ")");
        }
    }

    private void requireStartElementState() {
        if (eventType != XMLStreamConstants.START_ELEMENT) {
            throw new IllegalStateException("Not at START_ELEMENT (current: " + eventType + ")");
        }
    }

    private void requireTextState() {
        if (eventType != XMLStreamConstants.CHARACTERS
                && eventType != XMLStreamConstants.CDATA
                && eventType != XMLStreamConstants.COMMENT) {
            throw new IllegalStateException("Not at a text event (current: " + eventType + ")");
        }
    }

    private void requireState(int expected) {
        if (eventType != expected) {
            throw new IllegalStateException("Expected event " + expected + " but at " + eventType);
        }
    }

    private void checkAttributeIndex(int index) {
        if (index < 0 || index >= attributeList.size()) {
            throw new IndexOutOfBoundsException("Attribute index: " + index);
        }
    }

    private void cacheElementInfo(Element element) {
        namespaceDecls = new ArrayList<>();
        attributeList = new ArrayList<>();
        for (Map.Entry<String, String> entry : element.attributes().entrySet()) {
            classifyAttribute(element, entry.getKey(), entry.getValue());
        }
    }

    private void classifyAttribute(Element element, String name, String value) {
        if (XMLNS.equals(name)) {
            namespaceDecls.add(new String[] {null, value});
        } else if (name.startsWith(XMLNS_PREFIX)) {
            namespaceDecls.add(new String[] {name.substring(XMLNS_PREFIX.length()), value});
        } else {
            addRegularAttribute(element, name, value);
        }
    }

    private void addRegularAttribute(Element element, String name, String value) {
        String attrUri = "";
        String attrLocalName = name;
        String attrPrefix = "";
        int colonIdx = name.indexOf(':');
        if (colonIdx > 0) {
            attrPrefix = name.substring(0, colonIdx);
            attrLocalName = name.substring(colonIdx + 1);
            String resolved = NamespaceResolver.resolveNamespaceURI(element, attrPrefix);
            if (resolved != null) {
                attrUri = resolved;
            }
        }
        attributeList.add(new String[] {attrUri, attrLocalName, attrPrefix, name, value});
    }

    private String getTextContent() {
        if (currentNode instanceof Text) {
            String content = ((Text) currentNode).content();
            return content != null ? content : "";
        }
        if (currentNode instanceof Comment) {
            String content = ((Comment) currentNode).content();
            return content != null ? content : "";
        }
        return "";
    }

    private static String resolveElementNamespace(Element element) {
        String prefix = element.prefix();
        String uri = NamespaceResolver.resolveNamespaceURI(element, prefix);
        return uri != null ? uri : "";
    }

    private static String prefixOrEmpty(Element element) {
        String prefix = element.prefix();
        return prefix != null ? prefix : "";
    }

    private static boolean matchesAttribute(String[] attr, String namespaceURI, String localName) {
        boolean uriMatch = namespaceURI == null || namespaceURI.equals(attr[0]);
        return uriMatch && localName.equals(attr[1]);
    }

    private static boolean isTextEvent(int event) {
        return event == XMLStreamConstants.CHARACTERS
                || event == XMLStreamConstants.CDATA
                || event == XMLStreamConstants.SPACE;
    }

    private static boolean isSkippableForNextTag(int event) {
        return event == XMLStreamConstants.CHARACTERS
                || event == XMLStreamConstants.CDATA
                || event == XMLStreamConstants.SPACE
                || event == XMLStreamConstants.COMMENT
                || event == XMLStreamConstants.PROCESSING_INSTRUCTION;
    }

    private boolean isNonWhitespaceText(int event) {
        return (event == XMLStreamConstants.CHARACTERS || event == XMLStreamConstants.CDATA) && !isWhiteSpace();
    }

    // ── Inner classes ───────────────────────────────────────────────────

    private static final Location UNKNOWN_LOCATION = new Location() {
        @Override
        public int getLineNumber() {
            return -1;
        }

        @Override
        public int getColumnNumber() {
            return -1;
        }

        @Override
        public int getCharacterOffset() {
            return -1;
        }

        @Override
        public String getPublicId() {
            return null;
        }

        @Override
        public String getSystemId() {
            return null;
        }
    };

    /**
     * Adapts a domtrip element's in-scope namespace declarations to the
     * {@link javax.xml.namespace.NamespaceContext} interface.
     */
    private static class StAXNamespaceContext implements javax.xml.namespace.NamespaceContext {
        private final Element element;

        StAXNamespaceContext(Element element) {
            this.element = element;
        }

        @Override
        public String getNamespaceURI(String prefix) {
            if (prefix == null) {
                throw new IllegalArgumentException("Prefix cannot be null");
            }
            if (XMLConstants.XML_NS_PREFIX.equals(prefix)) {
                return XMLConstants.XML_NS_URI;
            }
            if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
                return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
            }
            if (element == null) {
                return XMLConstants.NULL_NS_URI;
            }
            String effectivePrefix = prefix.isEmpty() ? null : prefix;
            String uri = NamespaceResolver.resolveNamespaceURI(element, effectivePrefix);
            return uri != null ? uri : XMLConstants.NULL_NS_URI;
        }

        @Override
        public String getPrefix(String namespaceURI) {
            if (namespaceURI == null) {
                throw new IllegalArgumentException("Namespace URI cannot be null");
            }
            if (XMLConstants.XML_NS_URI.equals(namespaceURI)) {
                return XMLConstants.XML_NS_PREFIX;
            }
            if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespaceURI)) {
                return XMLConstants.XMLNS_ATTRIBUTE;
            }
            if (element == null) {
                return null;
            }
            String prefix = NamespaceResolver.resolvePrefix(element, namespaceURI);
            if (prefix != null) {
                return prefix;
            }
            // The resolver does not return a result when the default namespace matches,
            // so we fall back to checking the default namespace binding explicitly.
            String defaultUri = NamespaceResolver.resolveNamespaceURI(element, null);
            if (namespaceURI.equals(defaultUri)) {
                return XMLConstants.DEFAULT_NS_PREFIX;
            }
            return null;
        }

        @Override
        public Iterator<String> getPrefixes(String namespaceURI) {
            if (namespaceURI == null) {
                throw new IllegalArgumentException("Namespace URI cannot be null");
            }
            if (XMLConstants.XML_NS_URI.equals(namespaceURI)) {
                return Collections.singleton(XMLConstants.XML_NS_PREFIX).iterator();
            }
            if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespaceURI)) {
                return Collections.singleton(XMLConstants.XMLNS_ATTRIBUTE).iterator();
            }
            if (element == null) {
                return Collections.emptyIterator();
            }
            List<String> matching = collectMatchingPrefixes(namespaceURI);
            return Collections.unmodifiableList(matching).iterator();
        }

        private List<String> collectMatchingPrefixes(String namespaceURI) {
            Set<String> seen = new HashSet<>();
            List<String> matching = new ArrayList<>();
            Element current = element;
            while (current != null) {
                collectPrefixesFromElement(current, namespaceURI, seen, matching);
                Node parent = current.parent();
                current = (parent instanceof Element) ? (Element) parent : null;
            }
            return matching;
        }

        private void collectPrefixesFromElement(
                Element el, String namespaceURI, Set<String> seen, List<String> matching) {
            for (Map.Entry<String, String> entry : el.attributes().entrySet()) {
                String attrName = entry.getKey();
                if (XMLNS.equals(attrName)) {
                    if (seen.add("") && namespaceURI.equals(entry.getValue())) {
                        matching.add(XMLConstants.DEFAULT_NS_PREFIX);
                    }
                } else if (attrName.startsWith(XMLNS_PREFIX)) {
                    String p = attrName.substring(XMLNS_PREFIX.length());
                    if (seen.add(p) && namespaceURI.equals(entry.getValue())) {
                        matching.add(p);
                    }
                }
            }
        }
    }
}
