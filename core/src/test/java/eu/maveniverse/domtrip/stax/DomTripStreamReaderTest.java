/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.stax;

import static org.junit.jupiter.api.Assertions.*;

import eu.maveniverse.domtrip.Document;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import org.junit.jupiter.api.Test;
import org.w3c.dom.NodeList;

class DomTripStreamReaderTest {

    @Test
    void testSimpleElement() throws Exception {
        Document doc = Document.of("<root/>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        assertEquals(XMLStreamConstants.START_DOCUMENT, reader.getEventType());
        assertEquals(XMLStreamConstants.START_ELEMENT, reader.next());
        assertEquals("root", reader.getLocalName());
        assertEquals(XMLStreamConstants.END_ELEMENT, reader.next());
        assertEquals("root", reader.getLocalName());
        assertEquals(XMLStreamConstants.END_DOCUMENT, reader.next());
        assertFalse(reader.hasNext());
    }

    @Test
    void testElementWithText() throws Exception {
        Document doc = Document.of("<root>hello</root>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        assertEquals(XMLStreamConstants.START_DOCUMENT, reader.getEventType());
        assertEquals(XMLStreamConstants.START_ELEMENT, reader.next());
        assertEquals("root", reader.getLocalName());
        assertEquals(XMLStreamConstants.CHARACTERS, reader.next());
        assertEquals("hello", reader.getText());
        assertTrue(reader.hasText());
        assertEquals(XMLStreamConstants.END_ELEMENT, reader.next());
        assertEquals(XMLStreamConstants.END_DOCUMENT, reader.next());
    }

    @Test
    void testNestedElements() throws Exception {
        Document doc = Document.of("<root><child>text</child></root>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        List<String> events = collectEvents(reader);
        assertTrue(events.contains("START_ELEMENT:root"));
        assertTrue(events.contains("START_ELEMENT:child"));
        assertTrue(events.contains("CHARACTERS:text"));
        assertTrue(events.contains("END_ELEMENT:child"));
        assertTrue(events.contains("END_ELEMENT:root"));
    }

    @Test
    void testAttributes() throws Exception {
        Document doc = Document.of("<root id=\"1\" class=\"main\"/>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.next(); // START_ELEMENT
        assertEquals(2, reader.getAttributeCount());
        assertEquals("id", reader.getAttributeLocalName(0));
        assertEquals("1", reader.getAttributeValue(0));
        assertEquals("class", reader.getAttributeLocalName(1));
        assertEquals("main", reader.getAttributeValue(1));
        assertEquals("CDATA", reader.getAttributeType(0));
        assertTrue(reader.isAttributeSpecified(0));
    }

    @Test
    void testAttributeByNamespaceAndLocalName() throws Exception {
        Document doc = Document.of("<root id=\"1\" class=\"main\"/>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.next(); // START_ELEMENT
        assertEquals("1", reader.getAttributeValue(null, "id"));
        assertEquals("main", reader.getAttributeValue("", "class"));
        assertNull(reader.getAttributeValue(null, "nonexistent"));
    }

    @Test
    void testAttributeName() throws Exception {
        Document doc = Document.of("<root id=\"1\"/>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.next(); // START_ELEMENT
        javax.xml.namespace.QName attrName = reader.getAttributeName(0);
        assertEquals("id", attrName.getLocalPart());
        assertEquals("", attrName.getNamespaceURI());
        assertEquals("", attrName.getPrefix());
    }

    @Test
    void testNamespacedAttribute() throws Exception {
        Document doc =
                Document.of("<root xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"myType\"/>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.next(); // START_ELEMENT
        assertEquals(1, reader.getAttributeCount());
        assertEquals("type", reader.getAttributeLocalName(0));
        assertEquals("xsi", reader.getAttributePrefix(0));
        assertEquals("http://www.w3.org/2001/XMLSchema-instance", reader.getAttributeNamespace(0));
        assertEquals("myType", reader.getAttributeValue(0));
    }

    @Test
    void testDefaultNamespace() throws Exception {
        Document doc = Document.of("<root xmlns=\"http://example.com\"/>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.next(); // START_ELEMENT
        assertEquals("root", reader.getLocalName());
        assertEquals("http://example.com", reader.getNamespaceURI());
        assertNull(reader.getPrefix());
        assertEquals(1, reader.getNamespaceCount());
        assertNull(reader.getNamespacePrefix(0));
        assertEquals("http://example.com", reader.getNamespaceURI(0));
    }

    @Test
    void testPrefixedNamespace() throws Exception {
        Document doc = Document.of("<ns:root xmlns:ns=\"http://example.com\"/>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.next(); // START_ELEMENT
        assertEquals("root", reader.getLocalName());
        assertEquals("ns", reader.getPrefix());
        assertEquals("http://example.com", reader.getNamespaceURI());
        assertEquals(1, reader.getNamespaceCount());
        assertEquals("ns", reader.getNamespacePrefix(0));
        assertEquals("http://example.com", reader.getNamespaceURI(0));
    }

    @Test
    void testMultipleNamespaces() throws Exception {
        Document doc = Document.of("<root xmlns=\"http://default.com\" xmlns:ns=\"http://ns.com\"><ns:child/></root>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.next(); // START_ELEMENT root
        assertEquals(2, reader.getNamespaceCount());
        assertEquals("http://default.com", reader.getNamespaceURI());

        reader.next(); // START_ELEMENT ns:child
        assertEquals("child", reader.getLocalName());
        assertEquals("ns", reader.getPrefix());
        assertEquals("http://ns.com", reader.getNamespaceURI());
        assertEquals(0, reader.getNamespaceCount()); // no new declarations on child
    }

    @Test
    void testNamespaceInheritance() throws Exception {
        Document doc = Document.of("<root xmlns:ns=\"http://ns.com\"><ns:child><ns:grandchild/></ns:child></root>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.next(); // START_ELEMENT root
        reader.next(); // START_ELEMENT ns:child
        assertEquals("http://ns.com", reader.getNamespaceURI());
        reader.next(); // START_ELEMENT ns:grandchild
        assertEquals("http://ns.com", reader.getNamespaceURI());
    }

    @Test
    void testNamespaceURIResolution() throws Exception {
        Document doc = Document.of("<root xmlns=\"http://default.com\" xmlns:ns=\"http://ns.com\"/>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.next(); // START_ELEMENT
        assertEquals("http://default.com", reader.getNamespaceURI(""));
        assertEquals("http://ns.com", reader.getNamespaceURI("ns"));
        assertNull(reader.getNamespaceURI("undefined"));
    }

    @Test
    void testGetNamespaceURINullPrefixThrows() throws Exception {
        Document doc = Document.of("<root/>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.next(); // START_ELEMENT
        assertThrows(IllegalArgumentException.class, () -> reader.getNamespaceURI((String) null));
    }

    @Test
    void testNamespaceContext() throws Exception {
        Document doc = Document.of("<root xmlns=\"http://default.com\" xmlns:ns=\"http://ns.com\"/>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.next(); // START_ELEMENT
        javax.xml.namespace.NamespaceContext ctx = reader.getNamespaceContext();
        assertEquals("http://default.com", ctx.getNamespaceURI(""));
        assertEquals("http://ns.com", ctx.getNamespaceURI("ns"));
        assertEquals("http://www.w3.org/XML/1998/namespace", ctx.getNamespaceURI("xml"));
        assertEquals("http://www.w3.org/2000/xmlns/", ctx.getNamespaceURI("xmlns"));
        assertEquals("ns", ctx.getPrefix("http://ns.com"));
        assertEquals("xml", ctx.getPrefix("http://www.w3.org/XML/1998/namespace"));
        assertNotNull(ctx.getPrefixes("http://ns.com"));
        assertTrue(ctx.getPrefixes("http://ns.com").hasNext());
        assertFalse(ctx.getPrefixes("http://unknown.com").hasNext());
    }

    @Test
    void testNamespaceContextNullChecks() throws Exception {
        Document doc = Document.of("<root/>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);
        reader.next();

        javax.xml.namespace.NamespaceContext ctx = reader.getNamespaceContext();
        assertThrows(IllegalArgumentException.class, () -> ctx.getNamespaceURI(null));
        assertThrows(IllegalArgumentException.class, () -> ctx.getPrefix(null));
        assertThrows(IllegalArgumentException.class, () -> ctx.getPrefixes(null));
    }

    @Test
    void testGetPrefixesMultiplePrefixesSameURI() throws Exception {
        Document doc = Document.of("<root xmlns:a=\"http://example.com\" xmlns:b=\"http://example.com\"/>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.next(); // START_ELEMENT
        javax.xml.namespace.NamespaceContext ctx = reader.getNamespaceContext();
        Iterator<String> prefixes = ctx.getPrefixes("http://example.com");
        Set<String> prefixSet = new HashSet<>();
        while (prefixes.hasNext()) {
            prefixSet.add(prefixes.next());
        }
        assertEquals(2, prefixSet.size());
        assertTrue(prefixSet.contains("a"));
        assertTrue(prefixSet.contains("b"));
    }

    @Test
    void testCDATA() throws Exception {
        Document doc = Document.of("<root><![CDATA[<data>]]></root>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.next(); // START_ELEMENT root
        int event = reader.next();
        assertEquals(XMLStreamConstants.CDATA, event);
        assertEquals("<data>", reader.getText());
        assertTrue(reader.hasText());
    }

    @Test
    void testComment() throws Exception {
        Document doc = Document.of("<root><!-- hello --></root>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.next(); // START_ELEMENT root
        int event = reader.next();
        assertEquals(XMLStreamConstants.COMMENT, event);
        assertEquals(" hello ", reader.getText());
        assertTrue(reader.hasText());
    }

    @Test
    void testProcessingInstruction() throws Exception {
        Document doc = Document.of("<root><?target data?></root>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.next(); // START_ELEMENT root
        int event = reader.next();
        assertEquals(XMLStreamConstants.PROCESSING_INSTRUCTION, event);
        assertEquals("target", reader.getPITarget());
        assertEquals("data", reader.getPIData());
        assertFalse(reader.hasText()); // PI does not count as text
    }

    @Test
    void testMixedContent() throws Exception {
        Document doc = Document.of("<root>text1<child/>text2<!-- comment --></root>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        List<String> events = collectEvents(reader);
        assertEquals("START_ELEMENT:root", events.get(0));
        assertEquals("CHARACTERS:text1", events.get(1));
        assertEquals("START_ELEMENT:child", events.get(2));
        assertEquals("END_ELEMENT:child", events.get(3));
        assertEquals("CHARACTERS:text2", events.get(4));
        assertEquals("COMMENT: comment ", events.get(5));
        assertEquals("END_ELEMENT:root", events.get(6));
    }

    @Test
    void testDeepNesting() throws Exception {
        Document doc = Document.of("<a><b><c><d>deep</d></c></b></a>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        List<String> events = collectEvents(reader);
        assertTrue(events.contains("START_ELEMENT:a"));
        assertTrue(events.contains("START_ELEMENT:b"));
        assertTrue(events.contains("START_ELEMENT:c"));
        assertTrue(events.contains("START_ELEMENT:d"));
        assertTrue(events.contains("CHARACTERS:deep"));
        assertTrue(events.contains("END_ELEMENT:d"));
        assertTrue(events.contains("END_ELEMENT:c"));
        assertTrue(events.contains("END_ELEMENT:b"));
        assertTrue(events.contains("END_ELEMENT:a"));
    }

    @Test
    void testSelfClosingElement() throws Exception {
        Document doc = Document.of("<root><empty/></root>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.next(); // START_ELEMENT root
        assertEquals(XMLStreamConstants.START_ELEMENT, reader.next()); // empty
        assertEquals("empty", reader.getLocalName());
        assertEquals(XMLStreamConstants.END_ELEMENT, reader.next()); // end empty
        assertEquals("empty", reader.getLocalName());
    }

    @Test
    void testElementName() throws Exception {
        Document doc = Document.of("<ns:root xmlns:ns=\"http://example.com\"/>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.next(); // START_ELEMENT
        javax.xml.namespace.QName name = reader.getName();
        assertEquals("root", name.getLocalPart());
        assertEquals("ns", name.getPrefix());
        assertEquals("http://example.com", name.getNamespaceURI());
        assertTrue(reader.hasName());
    }

    @Test
    void testDocumentProperties() throws Exception {
        Document doc = Document.of("<?xml version=\"1.1\" encoding=\"UTF-8\" standalone=\"yes\"?><root/>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        assertEquals("1.1", reader.getVersion());
        assertEquals("UTF-8", reader.getEncoding());
        assertEquals("UTF-8", reader.getCharacterEncodingScheme());
        assertTrue(reader.isStandalone());
        assertTrue(reader.standaloneSet());
    }

    @Test
    void testStandaloneNotSet() throws Exception {
        Document doc = Document.of("<?xml version=\"1.0\" encoding=\"UTF-8\"?><root/>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        assertFalse(reader.isStandalone());
        assertFalse(reader.standaloneSet());
    }

    @Test
    void testGetElementText() throws Exception {
        Document doc = Document.of("<root><child>hello world</child></root>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.next(); // START_ELEMENT root
        reader.next(); // START_ELEMENT child
        assertEquals("hello world", reader.getElementText());
        // After getElementText, should be at END_ELEMENT
        assertEquals(XMLStreamConstants.END_ELEMENT, reader.getEventType());
    }

    @Test
    void testGetElementTextWithNestedElementThrows() throws Exception {
        Document doc = Document.of("<root><child><nested/></child></root>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.next(); // START_ELEMENT root
        reader.next(); // START_ELEMENT child
        assertThrows(XMLStreamException.class, reader::getElementText);
    }

    @Test
    void testGetElementTextNotAtStartElement() throws Exception {
        Document doc = Document.of("<root/>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        // At START_DOCUMENT
        assertThrows(IllegalStateException.class, reader::getElementText);
    }

    @Test
    void testNextTag() throws Exception {
        Document doc = Document.of("<root>  <child/>  </root>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.next(); // START_ELEMENT root
        int event = reader.nextTag();
        assertEquals(XMLStreamConstants.START_ELEMENT, event);
        assertEquals("child", reader.getLocalName());
    }

    @Test
    void testNextTagSkipsComments() throws Exception {
        Document doc = Document.of("<root><!-- comment --><child/></root>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.next(); // START_ELEMENT root
        int event = reader.nextTag();
        assertEquals(XMLStreamConstants.START_ELEMENT, event);
        assertEquals("child", reader.getLocalName());
    }

    @Test
    void testRequireSuccess() throws Exception {
        Document doc = Document.of("<root/>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.next(); // START_ELEMENT
        assertDoesNotThrow(() -> reader.require(XMLStreamConstants.START_ELEMENT, null, null));
        assertDoesNotThrow(() -> reader.require(XMLStreamConstants.START_ELEMENT, null, "root"));
        assertDoesNotThrow(() -> reader.require(XMLStreamConstants.START_ELEMENT, "", "root"));
    }

    @Test
    void testRequireWrongType() throws Exception {
        Document doc = Document.of("<root/>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.next(); // START_ELEMENT
        assertThrows(XMLStreamException.class, () -> reader.require(XMLStreamConstants.END_ELEMENT, null, null));
    }

    @Test
    void testRequireWrongLocalName() throws Exception {
        Document doc = Document.of("<root/>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.next(); // START_ELEMENT
        assertThrows(XMLStreamException.class, () -> reader.require(XMLStreamConstants.START_ELEMENT, null, "other"));
    }

    @Test
    void testTextCharacters() throws Exception {
        Document doc = Document.of("<root>hello</root>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.next(); // START_ELEMENT
        reader.next(); // CHARACTERS
        char[] chars = reader.getTextCharacters();
        assertEquals("hello", new String(chars));
        assertEquals(0, reader.getTextStart());
        assertEquals(5, reader.getTextLength());
    }

    @Test
    void testTextCharactersBulkRead() throws Exception {
        Document doc = Document.of("<root>hello</root>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.next(); // START_ELEMENT
        reader.next(); // CHARACTERS
        char[] target = new char[10];
        int count = reader.getTextCharacters(0, target, 0, 10);
        assertEquals(5, count);
        assertEquals("hello", new String(target, 0, count));
    }

    @Test
    void testGetTextCharactersValidation() throws Exception {
        Document doc = Document.of("<root>hello</root>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.next(); // START_ELEMENT
        reader.next(); // CHARACTERS

        // null target
        assertThrows(NullPointerException.class, () -> reader.getTextCharacters(0, null, 0, 5));

        // negative parameters
        char[] target = new char[10];
        assertThrows(IndexOutOfBoundsException.class, () -> reader.getTextCharacters(-1, target, 0, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> reader.getTextCharacters(0, target, -1, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> reader.getTextCharacters(0, target, 0, -1));

        // sourceStart beyond source length
        assertThrows(IndexOutOfBoundsException.class, () -> reader.getTextCharacters(10, target, 0, 5));

        // target too small
        char[] smallTarget = new char[2];
        assertThrows(IndexOutOfBoundsException.class, () -> reader.getTextCharacters(0, smallTarget, 0, 5));
    }

    @Test
    void testIsWhiteSpace() throws Exception {
        // Use CDATA to ensure whitespace content is stored as a Text node
        // (plain whitespace-only content is absorbed into element formatting metadata)
        Document doc = Document.of("<root><![CDATA[   ]]></root>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.next(); // START_ELEMENT
        int event = reader.next();
        assertEquals(XMLStreamConstants.CDATA, event);
        assertTrue(reader.isWhiteSpace());
    }

    @Test
    void testIsNotWhiteSpace() throws Exception {
        Document doc = Document.of("<root>hello</root>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.next(); // START_ELEMENT
        reader.next(); // CHARACTERS
        assertFalse(reader.isWhiteSpace());
    }

    @Test
    void testConvenienceQueries() throws Exception {
        Document doc = Document.of("<root>text</root>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.next(); // START_ELEMENT
        assertTrue(reader.isStartElement());
        assertFalse(reader.isEndElement());
        assertFalse(reader.isCharacters());
        assertTrue(reader.hasName());

        reader.next(); // CHARACTERS
        assertFalse(reader.isStartElement());
        assertTrue(reader.isCharacters());
        assertFalse(reader.hasName());
        assertTrue(reader.hasText());

        reader.next(); // END_ELEMENT
        assertTrue(reader.isEndElement());
        assertTrue(reader.hasName());
    }

    @Test
    void testLocation() throws Exception {
        Document doc = Document.of("<root/>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        javax.xml.stream.Location loc = reader.getLocation();
        assertNotNull(loc);
        assertEquals(-1, loc.getLineNumber());
        assertEquals(-1, loc.getColumnNumber());
        assertEquals(-1, loc.getCharacterOffset());
        assertNull(loc.getPublicId());
        assertNull(loc.getSystemId());
    }

    @Test
    void testProperties() throws Exception {
        Document doc = Document.of("<root/>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        assertEquals(Boolean.FALSE, reader.getProperty("javax.xml.stream.isValidating"));
        assertEquals(Boolean.TRUE, reader.getProperty("javax.xml.stream.isNamespaceAware"));
        assertNull(reader.getProperty("unknown.property"));
    }

    @Test
    void testPropertyNullName() throws Exception {
        Document doc = Document.of("<root/>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        assertThrows(IllegalArgumentException.class, () -> reader.getProperty(null));
    }

    @Test
    void testCloseAndSubsequentCalls() throws Exception {
        Document doc = Document.of("<root/>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.close();
        assertThrows(XMLStreamException.class, reader::hasNext);
        assertThrows(XMLStreamException.class, reader::next);
    }

    @Test
    void testNullDocument() {
        assertThrows(IllegalArgumentException.class, () -> new DomTripStreamReader(null));
    }

    @Test
    void testIllegalStateForAttributes() throws Exception {
        Document doc = Document.of("<root/>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        // At START_DOCUMENT — attribute access should throw
        assertThrows(IllegalStateException.class, reader::getAttributeCount);
        assertThrows(IllegalStateException.class, reader::getLocalName);
    }

    @Test
    void testIllegalStateForText() throws Exception {
        Document doc = Document.of("<root/>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.next(); // START_ELEMENT — text access should throw
        assertThrows(IllegalStateException.class, reader::getText);
    }

    @Test
    void testIllegalStateForPI() throws Exception {
        Document doc = Document.of("<root/>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.next(); // START_ELEMENT — PI access should throw
        assertThrows(IllegalStateException.class, reader::getPITarget);
        assertThrows(IllegalStateException.class, reader::getPIData);
    }

    @Test
    void testEndDocumentNoMoreEvents() throws Exception {
        Document doc = Document.of("<root/>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        // Consume all events
        while (reader.hasNext()) {
            reader.next();
        }
        assertThrows(Exception.class, reader::next);
    }

    @Test
    void testNamespaceCountAtEndElement() throws Exception {
        Document doc = Document.of("<root xmlns:ns=\"http://example.com\"><ns:child/></root>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        reader.next(); // START_ELEMENT root
        assertEquals(1, reader.getNamespaceCount()); // xmlns:ns declared here

        reader.next(); // START_ELEMENT ns:child
        assertEquals(0, reader.getNamespaceCount()); // no new declarations

        reader.next(); // END_ELEMENT ns:child
        assertEquals(0, reader.getNamespaceCount());

        reader.next(); // END_ELEMENT root
        assertEquals(1, reader.getNamespaceCount()); // declarations still available
    }

    @Test
    void testRoundTripViaDOMResult() throws Exception {
        String xml = "<root><child id=\"1\">text</child><empty/></root>";
        Document doc = Document.of(xml);
        StAXSource source = DomTripStAXSource.of(doc);

        DOMResult result = new DOMResult();
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(source, result);

        org.w3c.dom.Document domDoc = (org.w3c.dom.Document) result.getNode();
        assertNotNull(domDoc);
        assertEquals("root", domDoc.getDocumentElement().getNodeName());

        NodeList children = domDoc.getDocumentElement().getChildNodes();
        assertTrue(children.getLength() >= 2);
    }

    @Test
    void testXSLTTransformation() throws Exception {
        String xml = "<root><item>Hello</item></root>";
        String xslt = "<?xml version=\"1.0\"?>"
                + "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
                + "  <xsl:template match=\"/root\">"
                + "    <result><xsl:value-of select=\"item\"/></result>"
                + "  </xsl:template>"
                + "</xsl:stylesheet>";

        Document doc = Document.of(xml);
        StAXSource source = DomTripStAXSource.of(doc);

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer =
                tf.newTransformer(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(xslt)));

        StringWriter writer = new StringWriter();
        transformer.transform(source, new StreamResult(writer));
        String result = writer.toString();
        assertTrue(result.contains("Hello"));
        assertTrue(result.contains("result"));
    }

    @Test
    void testStAXSourceFactory() throws Exception {
        Document doc = Document.of("<root/>");
        DomTripStAXSource source = DomTripStAXSource.of(doc);

        assertNotNull(source);
        XMLStreamReader reader = source.getXMLStreamReader();
        assertNotNull(reader);
        assertTrue(reader instanceof DomTripStreamReader);
    }

    @Test
    void testStAXSourceNullDocument() {
        assertThrows(IllegalArgumentException.class, () -> DomTripStAXSource.of(null));
    }

    @Test
    void testDocumentWithCommentBeforeRoot() throws Exception {
        Document doc = Document.of("<!-- top comment --><root/>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        List<String> events = collectEvents(reader);
        // Should have comment before the root element
        assertTrue(events.stream().anyMatch(e -> e.startsWith("COMMENT:")));
        assertTrue(events.contains("START_ELEMENT:root"));
    }

    @Test
    void testDocumentWithPIBeforeRoot() throws Exception {
        Document doc = Document.of("<?pi data?><root/>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        List<String> events = collectEvents(reader);
        assertTrue(events.stream().anyMatch(e -> e.startsWith("PI:")));
        assertTrue(events.contains("START_ELEMENT:root"));
    }

    @Test
    void testMultipleChildElements() throws Exception {
        Document doc = Document.of("<root><a/><b/><c/></root>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        List<String> events = collectEvents(reader);
        assertTrue(events.contains("START_ELEMENT:a"));
        assertTrue(events.contains("END_ELEMENT:a"));
        assertTrue(events.contains("START_ELEMENT:b"));
        assertTrue(events.contains("END_ELEMENT:b"));
        assertTrue(events.contains("START_ELEMENT:c"));
        assertTrue(events.contains("END_ELEMENT:c"));
    }

    @Test
    void testEmptyDocument() throws Exception {
        Document doc = Document.of("<root/>");
        DomTripStreamReader reader = new DomTripStreamReader(doc);

        assertTrue(reader.hasNext());
        reader.next(); // START_ELEMENT
        assertTrue(reader.hasNext());
        reader.next(); // END_ELEMENT
        assertTrue(reader.hasNext());
        reader.next(); // END_DOCUMENT
        assertFalse(reader.hasNext());
    }

    // ── Helper methods ──────────────────────────────────────────────────

    private List<String> collectEvents(DomTripStreamReader reader) throws XMLStreamException {
        List<String> events = new ArrayList<>();
        while (reader.hasNext()) {
            int event = reader.next();
            events.add(formatEvent(reader, event));
        }
        return events;
    }

    private String formatEvent(DomTripStreamReader reader, int event) {
        switch (event) {
            case XMLStreamConstants.START_ELEMENT:
                return "START_ELEMENT:" + reader.getLocalName();
            case XMLStreamConstants.END_ELEMENT:
                return "END_ELEMENT:" + reader.getLocalName();
            case XMLStreamConstants.CHARACTERS:
                return "CHARACTERS:" + reader.getText();
            case XMLStreamConstants.CDATA:
                return "CDATA:" + reader.getText();
            case XMLStreamConstants.COMMENT:
                return "COMMENT:" + reader.getText();
            case XMLStreamConstants.PROCESSING_INSTRUCTION:
                return "PI:" + reader.getPITarget();
            case XMLStreamConstants.START_DOCUMENT:
                return "START_DOCUMENT";
            case XMLStreamConstants.END_DOCUMENT:
                return "END_DOCUMENT";
            default:
                return "EVENT:" + event;
        }
    }
}
