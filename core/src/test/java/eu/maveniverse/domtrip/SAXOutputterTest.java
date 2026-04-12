/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.junit.jupiter.api.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Tests for {@link SAXOutputter}, {@link DomTripXMLReader}, and {@link DomTripSAXSource}.
 */
class SAXOutputterTest {

    @Test
    void simpleDocumentEmitsCorrectEventSequence() throws Exception {
        Document doc = Document.of("<root><child>text</child></root>");

        EventRecorder recorder = new EventRecorder();
        SAXOutputter outputter = new SAXOutputter();
        outputter.output(doc, recorder);

        List<String> events = recorder.events;
        assertTrue(events.contains("startDocument"), "Should emit startDocument");
        assertTrue(events.contains("startElement: :root:root"), "Should emit startElement for root");
        assertTrue(events.contains("startElement: :child:child"), "Should emit startElement for child");
        assertTrue(events.contains("characters: text"), "Should emit characters");
        assertTrue(events.contains("endElement: :child:child"), "Should emit endElement for child");
        assertTrue(events.contains("endElement: :root:root"), "Should emit endElement for root");
        assertTrue(events.contains("endDocument"), "Should emit endDocument");

        // Verify order: startDocument is first, endDocument is last
        assertEquals("startDocument", events.get(0));
        assertEquals("endDocument", events.get(events.size() - 1));
    }

    @Test
    void namespaceDeclarationsEmitPrefixMappings() throws Exception {
        Document doc = Document.of(
                "<root xmlns=\"http://example.com\" xmlns:p=\"http://prefix.com\">" + "<p:child>text</p:child></root>");

        EventRecorder recorder = new EventRecorder();
        SAXOutputter outputter = new SAXOutputter();
        outputter.output(doc, recorder);

        assertTrue(
                recorder.events.contains("startPrefixMapping: =http://example.com"),
                "Should emit default namespace mapping");
        assertTrue(
                recorder.events.contains("startPrefixMapping: p=http://prefix.com"),
                "Should emit prefixed namespace mapping");
        assertTrue(recorder.events.contains("endPrefixMapping: "), "Should emit endPrefixMapping for default");
        assertTrue(recorder.events.contains("endPrefixMapping: p"), "Should emit endPrefixMapping for p");

        // Verify namespace URI in startElement
        assertTrue(recorder.events.contains("startElement: http://example.com:root:root"));
        assertTrue(recorder.events.contains("startElement: http://prefix.com:child:p:child"));
    }

    @Test
    void attributesReportedCorrectly() throws Exception {
        Document doc = Document.of("<root attr1=\"value1\" attr2=\"value2\"/>");

        EventRecorder recorder = new EventRecorder();
        SAXOutputter outputter = new SAXOutputter();
        outputter.output(doc, recorder);

        // Find the startElement event to check attributes
        assertTrue(recorder.attributeRecords.stream()
                .anyMatch(a -> a.localName.equals("attr1") && a.value.equals("value1")));
        assertTrue(recorder.attributeRecords.stream()
                .anyMatch(a -> a.localName.equals("attr2") && a.value.equals("value2")));
    }

    @Test
    void namespacedAttributesResolvedCorrectly() throws Exception {
        Document doc =
                Document.of("<root xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"myType\"/>");

        EventRecorder recorder = new EventRecorder();
        SAXOutputter outputter = new SAXOutputter();
        outputter.output(doc, recorder);

        assertTrue(recorder.attributeRecords.stream()
                .anyMatch(a -> a.uri.equals("http://www.w3.org/2001/XMLSchema-instance")
                        && a.localName.equals("type")
                        && a.value.equals("myType")));
    }

    @Test
    void cdataSectionsEmitLexicalEvents() throws Exception {
        Document doc = Document.of("<root><![CDATA[some <data>]]></root>");

        EventRecorder recorder = new EventRecorder();
        LexicalRecorder lexRecorder = new LexicalRecorder();
        SAXOutputter outputter = new SAXOutputter();
        outputter.output(doc, recorder, lexRecorder);

        assertTrue(lexRecorder.events.contains("startCDATA"), "Should emit startCDATA");
        assertTrue(lexRecorder.events.contains("endCDATA"), "Should emit endCDATA");
        assertTrue(recorder.events.contains("characters: some <data>"), "Should emit CDATA content as characters");
    }

    @Test
    void cdataWithoutLexicalHandlerStillEmitsCharacters() throws Exception {
        Document doc = Document.of("<root><![CDATA[data]]></root>");

        EventRecorder recorder = new EventRecorder();
        SAXOutputter outputter = new SAXOutputter();
        outputter.output(doc, recorder);

        assertTrue(recorder.events.contains("characters: data"));
    }

    @Test
    void commentsEmitLexicalEvents() throws Exception {
        Document doc = Document.of("<root><!-- a comment --></root>");

        EventRecorder recorder = new EventRecorder();
        LexicalRecorder lexRecorder = new LexicalRecorder();
        SAXOutputter outputter = new SAXOutputter();
        outputter.output(doc, recorder, lexRecorder);

        assertTrue(lexRecorder.events.contains("comment:  a comment "), "Should emit comment");
    }

    @Test
    void commentsWithoutLexicalHandlerAreSkipped() throws Exception {
        Document doc = Document.of("<root><!-- a comment --><child/></root>");

        EventRecorder recorder = new EventRecorder();
        SAXOutputter outputter = new SAXOutputter();
        outputter.output(doc, recorder);

        // Should not throw, and should still process other nodes
        assertTrue(recorder.events.contains("startElement: :child:child"));
    }

    @Test
    void processingInstructionsEmitEvents() throws Exception {
        Document doc = Document.of("<root><?target data?></root>");

        EventRecorder recorder = new EventRecorder();
        SAXOutputter outputter = new SAXOutputter();
        outputter.output(doc, recorder);

        assertTrue(recorder.events.contains("processingInstruction: target data"), "Should emit processingInstruction");
    }

    @Test
    void mixedContent() throws Exception {
        Document doc = Document.of("<root>text1<child/>text2</root>");

        EventRecorder recorder = new EventRecorder();
        SAXOutputter outputter = new SAXOutputter();
        outputter.output(doc, recorder);

        // Verify interleaved text and elements
        int text1Idx = recorder.events.indexOf("characters: text1");
        int childStart = recorder.events.indexOf("startElement: :child:child");
        int text2Idx = recorder.events.indexOf("characters: text2");

        assertTrue(text1Idx < childStart, "text1 before child");
        assertTrue(childStart < text2Idx, "child before text2");
    }

    @Test
    void emptyElementsBothStyles() throws Exception {
        // Self-closing
        Document doc1 = Document.of("<root><empty/></root>");
        EventRecorder rec1 = new EventRecorder();
        new SAXOutputter().output(doc1, rec1);

        assertTrue(rec1.events.contains("startElement: :empty:empty"));
        assertTrue(rec1.events.contains("endElement: :empty:empty"));

        // Expanded empty
        Document doc2 = Document.of("<root><empty></empty></root>");
        EventRecorder rec2 = new EventRecorder();
        new SAXOutputter().output(doc2, rec2);

        assertTrue(rec2.events.contains("startElement: :empty:empty"));
        assertTrue(rec2.events.contains("endElement: :empty:empty"));
    }

    @Test
    void deeplyNestedStructure() throws Exception {
        Document doc = Document.of("<a><b><c><d><e>deep</e></d></c></b></a>");

        EventRecorder recorder = new EventRecorder();
        new SAXOutputter().output(doc, recorder);

        assertTrue(recorder.events.contains("startElement: :e:e"));
        assertTrue(recorder.events.contains("characters: deep"));

        // Verify proper nesting: all opens come before their closes
        int aStart = recorder.events.indexOf("startElement: :a:a");
        int eEnd = recorder.events.indexOf("endElement: :e:e");
        int aEnd = recorder.events.indexOf("endElement: :a:a");
        assertTrue(aStart < eEnd && eEnd < aEnd);
    }

    @Test
    void reportNamespaceDeclarationsAsAttributes() throws Exception {
        Document doc = Document.of("<root xmlns=\"http://example.com\" xmlns:p=\"http://prefix.com\"/>");

        EventRecorder recorder = new EventRecorder();
        SAXOutputter outputter = new SAXOutputter();
        outputter.setReportNamespaceDeclarations(true);
        outputter.output(doc, recorder);

        assertTrue(recorder.attributeRecords.stream()
                .anyMatch(a -> a.qName.equals("xmlns") && a.value.equals("http://example.com")));
        assertTrue(recorder.attributeRecords.stream()
                .anyMatch(a -> a.qName.equals("xmlns:p") && a.value.equals("http://prefix.com")));
    }

    @Test
    void namespaceDeclarationsExcludedByDefault() throws Exception {
        Document doc = Document.of("<root xmlns=\"http://example.com\" attr=\"val\"/>");

        EventRecorder recorder = new EventRecorder();
        SAXOutputter outputter = new SAXOutputter();
        outputter.setReportNamespaceDeclarations(false);
        outputter.output(doc, recorder);

        assertEquals(1, recorder.attributeRecords.size(), "Only non-namespace attributes");
        assertEquals("attr", recorder.attributeRecords.get(0).localName);
    }

    @Test
    void roundTripViaDOMResult() throws Exception {
        String xml = "<root xmlns=\"http://example.com\"><child attr=\"value\">text</child></root>";
        Document doc = Document.of(xml);

        SAXSource source = DomTripSAXSource.of(doc);
        DOMResult result = new DOMResult();

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(source, result);

        org.w3c.dom.Document domDoc = (org.w3c.dom.Document) result.getNode();
        assertNotNull(domDoc.getDocumentElement());
        assertEquals("root", domDoc.getDocumentElement().getLocalName());
        assertEquals("http://example.com", domDoc.getDocumentElement().getNamespaceURI());

        org.w3c.dom.NodeList children = domDoc.getDocumentElement().getChildNodes();
        boolean foundChild = false;
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child instanceof org.w3c.dom.Element) {
                org.w3c.dom.Element childElem = (org.w3c.dom.Element) child;
                assertEquals("child", childElem.getLocalName());
                assertEquals("value", childElem.getAttribute("attr"));
                assertEquals("text", childElem.getTextContent());
                foundChild = true;
            }
        }
        assertTrue(foundChild, "Should find child element in DOM result");
    }

    @Test
    void xsltTransformation() throws Exception {
        String xml = "<items><item>one</item><item>two</item></items>";
        String xslt = "<?xml version=\"1.0\"?>"
                + "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
                + "<xsl:output method=\"text\"/>"
                + "<xsl:template match=\"/\">"
                + "<xsl:for-each select=\"items/item\">"
                + "<xsl:value-of select=\".\"/><xsl:text> </xsl:text>"
                + "</xsl:for-each>"
                + "</xsl:template>"
                + "</xsl:stylesheet>";

        Document doc = Document.of(xml);
        SAXSource source = DomTripSAXSource.of(doc);

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer(new StreamSource(new StringReader(xslt)));

        StringWriter writer = new StringWriter();
        transformer.transform(source, new StreamResult(writer));

        assertEquals("one two ", writer.toString());
    }

    @Test
    void nullDocumentThrows() {
        SAXOutputter outputter = new SAXOutputter();
        DefaultHandler handler = new DefaultHandler();
        assertThrows(IllegalArgumentException.class, () -> outputter.output(null, handler));
    }

    @Test
    void nullHandlerThrows() {
        Document doc = Document.of("<root/>");
        SAXOutputter outputter = new SAXOutputter();
        assertThrows(IllegalArgumentException.class, () -> outputter.output(doc, null));
    }

    @Test
    void domTripXMLReaderNullDocumentThrows() {
        assertThrows(IllegalArgumentException.class, () -> new DomTripXMLReader(null));
    }

    @Test
    void domTripXMLReaderFeaturesAndProperties() throws Exception {
        Document doc = Document.of("<root/>");
        DomTripXMLReader reader = new DomTripXMLReader(doc);

        // Namespaces always true
        assertTrue(reader.getFeature("http://xml.org/sax/features/namespaces"));
        assertThrows(
                org.xml.sax.SAXNotSupportedException.class,
                () -> reader.setFeature("http://xml.org/sax/features/namespaces", false));

        // Namespace prefixes configurable
        assertFalse(reader.getFeature("http://xml.org/sax/features/namespace-prefixes"));
        reader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        assertTrue(reader.getFeature("http://xml.org/sax/features/namespace-prefixes"));

        // Unknown feature throws
        assertThrows(org.xml.sax.SAXNotRecognizedException.class, () -> reader.getFeature("http://unknown"));
        assertThrows(org.xml.sax.SAXNotRecognizedException.class, () -> reader.setFeature("http://unknown", true));

        // Lexical handler property
        assertNull(reader.getProperty("http://xml.org/sax/properties/lexical-handler"));
        LexicalRecorder lex = new LexicalRecorder();
        reader.setProperty("http://xml.org/sax/properties/lexical-handler", lex);
        assertSame(lex, reader.getProperty("http://xml.org/sax/properties/lexical-handler"));

        // Unknown property throws
        assertThrows(org.xml.sax.SAXNotRecognizedException.class, () -> reader.getProperty("http://unknown"));
        assertThrows(org.xml.sax.SAXNotRecognizedException.class, () -> reader.setProperty("http://unknown", "value"));
    }

    @Test
    void domTripXMLReaderParseWithoutContentHandlerThrows() {
        Document doc = Document.of("<root/>");
        DomTripXMLReader reader = new DomTripXMLReader(doc);
        assertThrows(SAXException.class, () -> reader.parse("systemId"));
    }

    @Test
    void domTripXMLReaderHandlerAccessors() {
        Document doc = Document.of("<root/>");
        DomTripXMLReader reader = new DomTripXMLReader(doc);

        DefaultHandler handler = new DefaultHandler();
        reader.setContentHandler(handler);
        assertSame(handler, reader.getContentHandler());

        reader.setDTDHandler(handler);
        assertSame(handler, reader.getDTDHandler());

        reader.setEntityResolver(handler);
        assertSame(handler, reader.getEntityResolver());

        reader.setErrorHandler(handler);
        assertSame(handler, reader.getErrorHandler());
    }

    @Test
    void domTripSAXSourceFactoryMethod() {
        Document doc = Document.of("<root/>");
        DomTripSAXSource source = DomTripSAXSource.of(doc);
        assertNotNull(source);
        assertNotNull(source.getXMLReader());
        assertNotNull(source.getInputSource());
    }

    @Test
    void domTripSAXSourceNullDocumentThrows() {
        assertThrows(IllegalArgumentException.class, () -> DomTripSAXSource.of(null));
    }

    @Test
    void documentLevelProcessingInstruction() throws Exception {
        Document doc = Document.of("<?xml version=\"1.0\"?><?style type=\"text/css\"?><root/>");

        EventRecorder recorder = new EventRecorder();
        new SAXOutputter().output(doc, recorder);

        assertTrue(recorder.events.contains("processingInstruction: style type=\"text/css\""));
    }

    @Test
    void documentLevelComment() throws Exception {
        Document doc = Document.of("<!-- doc comment --><root/>");

        LexicalRecorder lexRecorder = new LexicalRecorder();
        new SAXOutputter().output(doc, new DefaultHandler(), lexRecorder);

        assertTrue(lexRecorder.events.contains("comment:  doc comment "));
    }

    @Test
    void multipleNamespacesOnSameElement() throws Exception {
        Document doc =
                Document.of("<root xmlns:a=\"http://a.com\" xmlns:b=\"http://b.com\">" + "<a:child/><b:other/></root>");

        EventRecorder recorder = new EventRecorder();
        new SAXOutputter().output(doc, recorder);

        assertTrue(recorder.events.contains("startPrefixMapping: a=http://a.com"));
        assertTrue(recorder.events.contains("startPrefixMapping: b=http://b.com"));
        assertTrue(recorder.events.contains("startElement: http://a.com:child:a:child"));
        assertTrue(recorder.events.contains("startElement: http://b.com:other:b:other"));
    }

    @Test
    void defaultNamespaceInherited() throws Exception {
        Document doc = Document.of("<root xmlns=\"http://example.com\"><child/></root>");

        EventRecorder recorder = new EventRecorder();
        new SAXOutputter().output(doc, recorder);

        // child should inherit the default namespace
        assertTrue(recorder.events.contains("startElement: http://example.com:child:child"));
    }

    @Test
    void isReportNamespaceDeclarationsDefault() {
        SAXOutputter outputter = new SAXOutputter();
        assertFalse(outputter.isReportNamespaceDeclarations());
    }

    // -- Helper classes --

    /** Records SAX ContentHandler events. */
    private static class EventRecorder extends DefaultHandler {
        final List<String> events = new ArrayList<>();
        final List<AttributeRecord> attributeRecords = new ArrayList<>();

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
            events.add("startPrefixMapping: " + prefix + "=" + uri);
        }

        @Override
        public void endPrefixMapping(String prefix) {
            events.add("endPrefixMapping: " + prefix);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) {
            events.add("startElement: " + uri + ":" + localName + ":" + qName);
            for (int i = 0; i < atts.getLength(); i++) {
                attributeRecords.add(
                        new AttributeRecord(atts.getURI(i), atts.getLocalName(i), atts.getQName(i), atts.getValue(i)));
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            events.add("endElement: " + uri + ":" + localName + ":" + qName);
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            events.add("characters: " + new String(ch, start, length));
        }

        @Override
        public void processingInstruction(String target, String data) {
            events.add("processingInstruction: " + target + " " + data);
        }
    }

    /** Records SAX LexicalHandler events. */
    private static class LexicalRecorder implements LexicalHandler {
        final List<String> events = new ArrayList<>();

        @Override
        public void startDTD(String name, String publicId, String systemId) {
            events.add("startDTD: " + name);
        }

        @Override
        public void endDTD() {
            events.add("endDTD");
        }

        @Override
        public void startEntity(String name) {
            events.add("startEntity: " + name);
        }

        @Override
        public void endEntity(String name) {
            events.add("endEntity: " + name);
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
            events.add("comment: " + new String(ch, start, length));
        }
    }

    /** Records attribute details from startElement calls. */
    private static class AttributeRecord {
        final String uri;
        final String localName;
        final String qName;
        final String value;

        AttributeRecord(String uri, String localName, String qName, String value) {
            this.uri = uri;
            this.localName = localName;
            this.qName = qName;
            this.value = value;
        }
    }
}
