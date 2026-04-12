/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.sax;

import static org.junit.jupiter.api.Assertions.*;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.Element;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.junit.jupiter.api.Test;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.ext.LexicalHandler;

/**
 * Tests for {@link SAXOutputter}, {@link DomTripXMLReader}, and {@link DomTripSAXSource}.
 */
class SAXOutputterTest {

    @Test
    void simpleDocumentEmitsCorrectEvents() throws Exception {
        Document doc = Document.of("<root><child>text</child></root>");
        RecordingHandler handler = new RecordingHandler();

        new SAXOutputter().output(doc, handler);

        assertContainsInOrder(
                handler.events,
                "startDocument",
                "startElement[,root,root]",
                "startElement[,child,child]",
                "characters[text]",
                "endElement[,child,child]",
                "endElement[,root,root]",
                "endDocument");
    }

    @Test
    void namespacesEmitPrefixMappings() throws Exception {
        Document doc = Document.of(
                "<root xmlns=\"http://example.com\" xmlns:ns=\"http://ns.example.com\">" + "<ns:child/></root>");
        RecordingHandler handler = new RecordingHandler();

        new SAXOutputter().output(doc, handler);

        assertContainsInOrder(
                handler.events,
                "startPrefixMapping[,http://example.com]",
                "startPrefixMapping[ns,http://ns.example.com]",
                "startElement[http://example.com,root,root]",
                "startElement[http://ns.example.com,child,ns:child]",
                "endElement[http://ns.example.com,child,ns:child]",
                "endElement[http://example.com,root,root]",
                "endPrefixMapping[ns]",
                "endPrefixMapping[]");
    }

    @Test
    void attributesReportedCorrectly() throws Exception {
        Document doc = Document.of("<root attr1=\"value1\" attr2=\"value2\"/>");
        RecordingHandler handler = new RecordingHandler();

        new SAXOutputter().output(doc, handler);

        // Find the startElement event and verify attributes
        String startEvent = handler.events.stream()
                .filter(e -> e.startsWith("startElement"))
                .findFirst()
                .orElseThrow();
        assertTrue(startEvent.contains("attr1=value1"));
        assertTrue(startEvent.contains("attr2=value2"));
    }

    @Test
    void namespacedAttributesResolveUri() throws Exception {
        Document doc =
                Document.of("<root xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + " xsi:type=\"myType\"/>");
        RecordingHandler handler = new RecordingHandler();

        new SAXOutputter().output(doc, handler);

        String startEvent = handler.events.stream()
                .filter(e -> e.startsWith("startElement"))
                .findFirst()
                .orElseThrow();
        assertTrue(startEvent.contains("http://www.w3.org/2001/XMLSchema-instance"));
        assertTrue(startEvent.contains("type"));
    }

    @Test
    void cdataSectionsEmitLexicalEvents() throws Exception {
        Document doc = Document.of("<root><![CDATA[cdata content]]></root>");
        RecordingHandler handler = new RecordingHandler();
        RecordingLexicalHandler lexHandler = new RecordingLexicalHandler();

        new SAXOutputter().output(doc, handler, lexHandler);

        assertContainsInOrder(handler.events, "characters[cdata content]");
        assertContainsInOrder(lexHandler.events, "startCDATA", "endCDATA");
    }

    @Test
    void cdataWithoutLexicalHandlerEmitsCharacters() throws Exception {
        Document doc = Document.of("<root><![CDATA[cdata content]]></root>");
        RecordingHandler handler = new RecordingHandler();

        new SAXOutputter().output(doc, handler);

        assertContainsInOrder(handler.events, "characters[cdata content]");
    }

    @Test
    void commentsEmitLexicalEvents() throws Exception {
        Document doc = Document.of("<root><!-- a comment --></root>");
        RecordingHandler handler = new RecordingHandler();
        RecordingLexicalHandler lexHandler = new RecordingLexicalHandler();

        new SAXOutputter().output(doc, handler, lexHandler);

        assertContainsInOrder(lexHandler.events, "comment[ a comment ]");
    }

    @Test
    void commentsWithoutLexicalHandlerAreSkipped() throws Exception {
        Document doc = Document.of("<root><!-- a comment --></root>");
        RecordingHandler handler = new RecordingHandler();

        // Should not throw
        new SAXOutputter().output(doc, handler);

        assertTrue(handler.events.stream().noneMatch(e -> e.contains("comment")));
    }

    @Test
    void processingInstructionsEmitEvents() throws Exception {
        Document doc = Document.of("<root><?target data?></root>");
        RecordingHandler handler = new RecordingHandler();

        new SAXOutputter().output(doc, handler);

        assertContainsInOrder(handler.events, "processingInstruction[target,data]");
    }

    @Test
    void mixedContent() throws Exception {
        Document doc = Document.of("<root>text1<child/>text2</root>");
        RecordingHandler handler = new RecordingHandler();

        new SAXOutputter().output(doc, handler);

        assertContainsInOrder(
                handler.events,
                "startElement[,root,root]",
                "characters[text1]",
                "startElement[,child,child]",
                "endElement[,child,child]",
                "characters[text2]",
                "endElement[,root,root]");
    }

    @Test
    void emptyElementsSelfClosing() throws Exception {
        Document doc = Document.of("<root><empty/></root>");
        RecordingHandler handler = new RecordingHandler();

        new SAXOutputter().output(doc, handler);

        assertContainsInOrder(handler.events, "startElement[,empty,empty]", "endElement[,empty,empty]");
    }

    @Test
    void emptyElementsExpanded() throws Exception {
        Document doc = Document.of("<root><empty></empty></root>");
        RecordingHandler handler = new RecordingHandler();

        new SAXOutputter().output(doc, handler);

        assertContainsInOrder(handler.events, "startElement[,empty,empty]", "endElement[,empty,empty]");
    }

    @Test
    void deeplyNestedStructure() throws Exception {
        Document doc = Document.of("<a><b><c><d>deep</d></c></b></a>");
        RecordingHandler handler = new RecordingHandler();

        new SAXOutputter().output(doc, handler);

        assertContainsInOrder(
                handler.events,
                "startElement[,a,a]",
                "startElement[,b,b]",
                "startElement[,c,c]",
                "startElement[,d,d]",
                "characters[deep]",
                "endElement[,d,d]",
                "endElement[,c,c]",
                "endElement[,b,b]",
                "endElement[,a,a]");
    }

    @Test
    void reportNamespaceDeclarationsAsAttributes() throws Exception {
        Document doc = Document.of("<root xmlns=\"http://example.com\"/>");
        RecordingHandler handler = new RecordingHandler();

        SAXOutputter outputter = new SAXOutputter();
        outputter.setReportNamespaceDeclarations(true);
        outputter.output(doc, handler);

        String startEvent = handler.events.stream()
                .filter(e -> e.startsWith("startElement"))
                .findFirst()
                .orElseThrow();
        assertTrue(startEvent.contains("xmlns=http://example.com"));
    }

    @Test
    void namespaceDeclarationsNotInAttributesByDefault() throws Exception {
        Document doc = Document.of("<root xmlns=\"http://example.com\"/>");
        RecordingHandler handler = new RecordingHandler();

        new SAXOutputter().output(doc, handler);

        String startEvent = handler.events.stream()
                .filter(e -> e.startsWith("startElement"))
                .findFirst()
                .orElseThrow();
        assertFalse(startEvent.contains("xmlns="));
    }

    @Test
    void elementOutputDoesNotEmitDocumentEvents() throws Exception {
        Element element = Element.of("root");
        RecordingHandler handler = new RecordingHandler();

        new SAXOutputter().output(element, handler);

        assertTrue(handler.events.stream().noneMatch(e -> e.equals("startDocument")));
        assertTrue(handler.events.stream().noneMatch(e -> e.equals("endDocument")));
        assertContainsInOrder(handler.events, "startElement[,root,root]", "endElement[,root,root]");
    }

    @Test
    void roundTripViaDomResult() throws Exception {
        String xml = "<root xmlns=\"http://example.com\"><child attr=\"val\">text</child></root>";
        Document doc = Document.of(xml);

        SAXSource source = DomTripSAXSource.of(doc);
        DOMResult result = new DOMResult();
        TransformerFactory.newInstance().newTransformer().transform(source, result);

        org.w3c.dom.Document domDoc = (org.w3c.dom.Document) result.getNode();
        org.w3c.dom.Element domRoot = domDoc.getDocumentElement();
        assertEquals("root", domRoot.getLocalName());
        assertEquals("http://example.com", domRoot.getNamespaceURI());

        org.w3c.dom.NodeList children = domRoot.getElementsByTagNameNS("http://example.com", "child");
        assertEquals(1, children.getLength());
        assertEquals("text", children.item(0).getTextContent());
        assertEquals("val", ((org.w3c.dom.Element) children.item(0)).getAttribute("attr"));
    }

    @Test
    void xsltTransformation() throws Exception {
        String xml = "<data><item>Hello</item></data>";
        String xslt = "<?xml version=\"1.0\"?>"
                + "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
                + "<xsl:template match=\"/data\">"
                + "<result><xsl:value-of select=\"item\"/></result>"
                + "</xsl:template>"
                + "</xsl:stylesheet>";

        Document doc = Document.of(xml);
        SAXSource source = DomTripSAXSource.of(doc);

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer(new StreamSource(new StringReader(xslt)));

        StringWriter writer = new StringWriter();
        transformer.transform(source, new StreamResult(writer));

        String output = writer.toString();
        assertTrue(output.contains("<result>Hello</result>"), "Expected transformed output, got: " + output);
    }

    @Test
    void schemaValidation() throws Exception {
        String xsd = "<?xml version=\"1.0\"?>"
                + "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">"
                + "<xs:element name=\"root\">"
                + "<xs:complexType><xs:sequence>"
                + "<xs:element name=\"child\" type=\"xs:string\"/>"
                + "</xs:sequence></xs:complexType>"
                + "</xs:element></xs:schema>";

        String xml = "<root><child>value</child></root>";
        Document doc = Document.of(xml);

        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = sf.newSchema(new StreamSource(new StringReader(xsd)));
        Validator validator = schema.newValidator();

        SAXSource source = DomTripSAXSource.of(doc);
        assertDoesNotThrow(() -> validator.validate(source));
    }

    @Test
    void nullDocumentThrows() {
        SAXOutputter outputter = new SAXOutputter();
        RecordingHandler handler = new RecordingHandler();

        assertThrows(IllegalArgumentException.class, () -> outputter.output((Document) null, handler));
    }

    @Test
    void nullHandlerThrows() {
        Document doc = Document.of("<root/>");
        SAXOutputter outputter = new SAXOutputter();

        assertThrows(IllegalArgumentException.class, () -> outputter.output(doc, (ContentHandler) null));
    }

    @Test
    void nullElementThrows() {
        SAXOutputter outputter = new SAXOutputter();
        RecordingHandler handler = new RecordingHandler();

        assertThrows(IllegalArgumentException.class, () -> outputter.output((Element) null, handler));
    }

    @Test
    void xmlReaderFeatures() throws Exception {
        Document doc = Document.of("<root/>");
        DomTripXMLReader reader = new DomTripXMLReader(doc);

        assertTrue(reader.getFeature("http://xml.org/sax/features/namespaces"));
        assertFalse(reader.getFeature("http://xml.org/sax/features/namespace-prefixes"));

        reader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        assertTrue(reader.getFeature("http://xml.org/sax/features/namespace-prefixes"));
    }

    @Test
    void xmlReaderLexicalHandlerProperty() throws Exception {
        Document doc = Document.of("<root/>");
        DomTripXMLReader reader = new DomTripXMLReader(doc);

        assertNull(reader.getProperty("http://xml.org/sax/properties/lexical-handler"));

        RecordingLexicalHandler lexHandler = new RecordingLexicalHandler();
        reader.setProperty("http://xml.org/sax/properties/lexical-handler", lexHandler);
        assertSame(lexHandler, reader.getProperty("http://xml.org/sax/properties/lexical-handler"));
    }

    @Test
    void xmlReaderRequiresContentHandler() {
        Document doc = Document.of("<root/>");
        DomTripXMLReader reader = new DomTripXMLReader(doc);

        org.xml.sax.InputSource input = new org.xml.sax.InputSource();
        assertThrows(IllegalStateException.class, () -> reader.parse(input));
    }

    @Test
    void documentLevelProcessingInstructions() throws Exception {
        Document doc = Document.of("<?pi-before data?><root/>");
        RecordingHandler handler = new RecordingHandler();

        new SAXOutputter().output(doc, handler);

        assertContainsInOrder(
                handler.events,
                "startDocument",
                "processingInstruction[pi-before,data]",
                "startElement[,root,root]",
                "endElement[,root,root]",
                "endDocument");
    }

    @Test
    void defaultNamespaceInherited() throws Exception {
        Document doc = Document.of("<root xmlns=\"http://example.com\"><child/></root>");
        RecordingHandler handler = new RecordingHandler();

        new SAXOutputter().output(doc, handler);

        // child should inherit the default namespace from root
        assertContainsInOrder(
                handler.events,
                "startElement[http://example.com,root,root]",
                "startElement[http://example.com,child,child]",
                "endElement[http://example.com,child,child]",
                "endElement[http://example.com,root,root]");
    }

    @Test
    void multipleNamespacePrefixes() throws Exception {
        Document doc = Document.of("<root xmlns:a=\"http://a.example.com\" xmlns:b=\"http://b.example.com\">"
                + "<a:child/><b:child/></root>");
        RecordingHandler handler = new RecordingHandler();

        new SAXOutputter().output(doc, handler);

        assertContainsInOrder(
                handler.events,
                "startPrefixMapping[a,http://a.example.com]",
                "startPrefixMapping[b,http://b.example.com]",
                "startElement[,root,root]",
                "startElement[http://a.example.com,child,a:child]",
                "endElement[http://a.example.com,child,a:child]",
                "startElement[http://b.example.com,child,b:child]",
                "endElement[http://b.example.com,child,b:child]",
                "endElement[,root,root]",
                "endPrefixMapping[b]",
                "endPrefixMapping[a]");
    }

    @Test
    void saxSourceOfNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> DomTripSAXSource.of(null));
    }

    // --- Helpers ---

    private static void assertContainsInOrder(List<String> events, String... expected) {
        int searchFrom = 0;
        for (String exp : expected) {
            boolean found = false;
            for (int i = searchFrom; i < events.size(); i++) {
                if (events.get(i).equals(exp)) {
                    searchFrom = i + 1;
                    found = true;
                    break;
                }
            }
            if (!found) {
                fail("Expected event '" + exp + "' not found in order. Events: " + events);
            }
        }
    }

    /**
     * Records SAX ContentHandler events as strings for assertion.
     */
    static class RecordingHandler implements ContentHandler {
        final List<String> events = new ArrayList<>();

        @Override
        public void setDocumentLocator(Locator locator) {
            // Not needed for event recording
        }

        @Override
        public void startDocument() {
            events.add("startDocument");
        }

        @Override
        public void endDocument() {
            events.add("endDocument");
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) {
            events.add("startPrefixMapping[" + prefix + "," + uri + "]");
        }

        @Override
        public void endPrefixMapping(String prefix) {
            events.add("endPrefixMapping[" + prefix + "]");
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) {
            StringBuilder sb = new StringBuilder();
            sb.append("startElement[")
                    .append(uri)
                    .append(",")
                    .append(localName)
                    .append(",")
                    .append(qName)
                    .append("]");
            if (atts.getLength() > 0) {
                sb.append("{");
                for (int i = 0; i < atts.getLength(); i++) {
                    if (i > 0) sb.append(",");
                    String attrUri = atts.getURI(i);
                    if (attrUri != null && !attrUri.isEmpty()) {
                        sb.append(attrUri).append("|");
                    }
                    sb.append(atts.getQName(i)).append("=").append(atts.getValue(i));
                }
                sb.append("}");
            }
            events.add(sb.toString());
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            events.add("endElement[" + uri + "," + localName + "," + qName + "]");
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            events.add("characters[" + new String(ch, start, length) + "]");
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) {
            events.add("ignorableWhitespace[" + new String(ch, start, length) + "]");
        }

        @Override
        public void processingInstruction(String target, String data) {
            events.add("processingInstruction[" + target + "," + data + "]");
        }

        @Override
        public void skippedEntity(String name) {
            events.add("skippedEntity[" + name + "]");
        }
    }

    /**
     * Records SAX LexicalHandler events as strings for assertion.
     */
    static class RecordingLexicalHandler implements LexicalHandler {
        final List<String> events = new ArrayList<>();

        @Override
        public void startDTD(String name, String publicId, String systemId) {
            events.add("startDTD[" + name + "]");
        }

        @Override
        public void endDTD() {
            events.add("endDTD");
        }

        @Override
        public void startEntity(String name) {
            events.add("startEntity[" + name + "]");
        }

        @Override
        public void endEntity(String name) {
            events.add("endEntity[" + name + "]");
        }

        @Override
        public void startCDATA() {
            events.add("startCDATA");
        }

        @Override
        public void endCDATA() {
            events.add("endCDATA");
        }

        @Override
        public void comment(char[] ch, int start, int length) {
            events.add("comment[" + new String(ch, start, length) + "]");
        }
    }
}
