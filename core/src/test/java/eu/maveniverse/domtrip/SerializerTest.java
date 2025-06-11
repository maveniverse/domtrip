package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test cases for Serializer functionality and edge cases.
 */
public class SerializerTest {

    private Editor editor;
    private Serializer serializer;

    @BeforeEach
    void setUp() {
        editor = new Editor(Document.of());
        serializer = new Serializer();
    }

    @Test
    void testSerializeNullDocument() {
        String result = serializer.serialize(null);
        assertEquals("", result);
    }

    @Test
    void testSerializeEmptyDocument() {
        Document doc = new Document();
        String result = serializer.serialize(doc);
        assertNotNull(result);
    }

    @Test
    void testSerializeDocumentWithOnlyXmlDeclaration() {
        Document doc = new Document();
        doc.xmlDeclaration("<?xml version=\"1.0\"?>");

        String result = serializer.serialize(doc);
        assertEquals("<?xml version=\"1.0\"?>", result);
    }

    @Test
    void testSerializeDocumentWithDoctype() {
        Document doc = new Document();
        doc.xmlDeclaration("<?xml version=\"1.0\"?>");
        doc.doctype("<!DOCTYPE root SYSTEM \"root.dtd\">");
        Element root = new Element("root");
        doc.root(root);

        String result = serializer.serialize(doc);
        assertTrue(result.contains("<?xml version=\"1.0\"?>"));
        assertTrue(result.contains("<!DOCTYPE root SYSTEM \"root.dtd\">"));
        assertTrue(result.contains("<root"));
    }

    @Test
    void testSerializeElementWithNoChildren() {
        Element element = new Element("empty");

        String result = element.toXml();
        assertTrue(result.contains("<empty"));
        assertTrue(result.contains("</empty>") || result.contains("/>"));
    }

    @Test
    void testSerializeElementWithAttributes() {
        Element element = new Element("test");
        element.attribute("attr1", "value1");
        element.attribute("attr2", "value2");

        String result = element.toXml();
        assertTrue(result.contains("attr1=\"value1\""));
        assertTrue(result.contains("attr2=\"value2\""));
    }

    @Test
    void testSerializeElementWithSpecialCharactersInAttributes() {
        Element element = new Element("test");
        element.attribute("attr", "value with <tags> & \"quotes\"");

        String result = element.toXml();
        assertTrue(result.contains("&lt;tags&gt;"));
        assertTrue(result.contains("&amp;"));
        assertTrue(result.contains("&quot;quotes&quot;"));
    }

    @Test
    void testSerializeElementWithMixedQuotes() {
        Element element = new Element("test");
        element.attribute("single", "value", QuoteStyle.SINGLE);
        element.attribute("double", "value", QuoteStyle.DOUBLE);

        String result = element.toXml();
        assertTrue(result.contains("single='value'"));
        assertTrue(result.contains("double=\"value\""));
    }

    @Test
    void testSerializeTextWithSpecialCharacters() {
        Text text = new Text("Content with <tags> & symbols > here");

        String result = text.toXml();
        assertTrue(result.contains("&lt;tags&gt;"));
        assertTrue(result.contains("&amp;"));
        assertTrue(result.contains("&gt;"));
    }

    @Test
    void testSerializeCDataSection() {
        Text cdata = new Text("function() { return x < y && z > 0; }", true);

        String result = cdata.toXml();
        assertEquals("<![CDATA[function() { return x < y && z > 0; }]]>", result);
    }

    @Test
    void testSerializeComment() {
        Comment comment = new Comment("This is a comment");

        String result = comment.toXml();
        assertEquals("<!--This is a comment-->", result);
    }

    @Test
    void testSerializeCommentWithSpecialCharacters() {
        Comment comment = new Comment("Comment with <tags> & symbols");

        String result = comment.toXml();
        // Comments should preserve content exactly
        assertEquals("<!--Comment with <tags> & symbols-->", result);
    }

    @Test
    void testSerializeProcessingInstruction() {
        ProcessingInstruction pi = new ProcessingInstruction("<?target data?>");

        String result = pi.toXml();
        assertEquals("<?target data?>", result);
    }

    @Test
    void testSerializeComplexDocument() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<!-- Document comment -->\n"
                + "<?xml-stylesheet type=\"text/xsl\" href=\"style.xsl\"?>\n"
                + "<root xmlns=\"http://example.com\">\n"
                + "  <element attr='value'>text content</element>\n"
                + "  <cdata><![CDATA[raw content]]></cdata>\n"
                + "  <!-- inline comment -->\n"
                + "  <empty/>\n"
                + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        // XML declaration may not be preserved exactly
        assertTrue(result.contains("<!-- Document comment -->"));
        assertTrue(result.contains("<?xml-stylesheet"));
        assertTrue(result.contains("<root xmlns=\"http://example.com\">"));
        assertTrue(result.contains("<![CDATA[raw content]]>"));
    }

    @Test
    void testSerializeWithWhitespacePreservation() {
        String xml = "<root>\n" + "    <element   attr=\"value\"   >\n"
                + "        content with spaces\n"
                + "    </element>\n"
                + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        assertEquals(xml, result);
    }

    @Test
    void testSerializeModifiedElement() {
        String xml = "<root><element attr=\"original\">original content</element></root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element root = editor.root();
        Element element = (Element) root.getNode(0);

        // Modify the element
        element.attribute("attr", "modified");
        element.textContent("modified content");

        String result = editor.toXml();
        assertTrue(result.contains("attr=\"modified\""));
        assertTrue(result.contains("modified content"));
        assertFalse(result.contains("original"));
    }

    @Test
    void testSerializeWithEntityPreservation() {
        String xml = "<root>\n" + "  <content>Text with &lt;entities&gt; &amp; symbols</content>\n"
                + "  <attr value=\"&quot;quoted&quot; &amp; escaped\"/>\n"
                + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        // Entities should be preserved in output
        assertTrue(result.contains("&lt;entities&gt;"));
        assertTrue(result.contains("&amp; symbols"));
        assertTrue(result.contains("&quot;quoted&quot;"));
        assertEquals(xml, result);
    }

    @Test
    void testSerializeLargeDocument() {
        // Create a large document
        Document doc = new Document();
        doc.xmlDeclaration("<?xml version=\"1.0\"?>");
        Element root = new Element("root");
        doc.root(root);

        // Add many child elements
        for (int i = 0; i < 1000; i++) {
            Element child = new Element("element" + i);
            child.attribute("id", String.valueOf(i));
            child.textContent("Content " + i);
            root.addNode(child);
        }

        String result = serializer.serialize(doc);

        assertNotNull(result);
        assertTrue(result.contains("<?xml version=\"1.0\"?>"));
        assertTrue(result.contains("<root"));
        assertTrue(result.contains("element0"));
        assertTrue(result.contains("element999"));
        assertTrue(result.contains("Content 0"));
        assertTrue(result.contains("Content 999"));
    }

    @Test
    void testSerializeDeeplyNestedDocument() {
        // Create deeply nested structure
        Document doc = new Document();
        Element root = new Element("root");
        doc.root(root);

        Element current = root;
        for (int i = 0; i < 100; i++) {
            Element child = new Element("level" + i);
            current.addNode(child);
            current = child;
        }
        current.textContent("deep content");

        String result = serializer.serialize(doc);

        assertNotNull(result);
        assertTrue(result.contains("<root"));
        assertTrue(result.contains("level0"));
        assertTrue(result.contains("level99"));
        assertTrue(result.contains("deep content"));
    }

    @Test
    void testSerializeWithNamespaces() {
        String xml = "<root xmlns=\"http://default.ns\" xmlns:ns=\"http://custom.ns\">\n"
                + "  <element>default namespace</element>\n"
                + "  <ns:element>custom namespace</ns:element>\n"
                + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        assertEquals(xml, result);
    }

    @Test
    void testSerializeStringBuilderMethod() {
        String xml = "<root><child>content</child></root>";

        Document doc = Document.of(xml);

        StringBuilder sb = new StringBuilder();
        doc.toXml(sb);

        String result = sb.toString();
        assertEquals(xml, result);
    }

    @Test
    void testSerializeEmptyElements() {
        String xml = "<root>\n" + "  <self-closing/>\n"
                + "  <empty></empty>\n"
                + "  <with-content>content</with-content>\n"
                + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        // Should preserve original formatting
        assertTrue(result.contains("<self-closing/>"));
        assertTrue(result.contains("<empty></empty>"));
        assertTrue(result.contains("<with-content>content</with-content>"));
        assertEquals(xml, result);
    }

    @Test
    void testSerializeWithMultipleTextNodes() {
        String xml = "<root>Text 1<element/>Text 2<another/>Text 3</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        assertEquals(xml, result);
    }

    @Test
    void testSerializePerformance() {
        // Create a moderately complex document
        Document doc = new Document();
        doc.xmlDeclaration("<?xml version=\"1.0\"?>");
        Element root = new Element("root");
        doc.root(root);

        for (int i = 0; i < 100; i++) {
            Element child = new Element("element");
            child.attribute("id", String.valueOf(i));
            child.attribute("name", "element" + i);
            child.textContent("Content for element " + i);
            root.addNode(child);
        }

        // Measure serialization time
        long start = System.nanoTime();
        String result = serializer.serialize(doc);
        long duration = System.nanoTime() - start;

        assertNotNull(result);
        assertTrue(result.length() > 0);
        // Should complete in reasonable time (less than 10ms)
        assertTrue(duration < 10_000_000, "Serialization took too long: " + duration + "ns");
    }
}
