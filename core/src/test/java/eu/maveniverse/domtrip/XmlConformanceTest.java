package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test cases for XML parser conformance and edge cases.
 */
public class XmlConformanceTest {

    private Editor editor;

    @BeforeEach
    void setUp() {
        editor = new Editor(Document.of());
    }

    @Test
    void testWellFormedXml() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<root>\n"
                + "  <element attr=\"value\">content</element>\n"
                + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        assertTrue(editor.isWellFormed());

        String result = editor.toXml();
        assertNotNull(result);
        assertTrue(result.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(result.contains("<root>"));
        assertTrue(result.contains("<element attr=\"value\">content</element>"));
    }

    @Test
    void testEmptyElements() {
        String xml = "<root>\n" + "  <empty/>\n" + "  <also-empty></also-empty>\n" + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        // Should preserve original formatting
        assertTrue(result.contains("<empty/>"));
        assertTrue(result.contains("<also-empty></also-empty>"));
    }

    @Test
    void testAttributeVariations() {
        String xml = "<root\n" + "  attr1='single'\n"
                + "  attr2=\"double\"\n"
                + "  attr3=  'spaced'\n"
                + "  attr4=\"no-space\"/>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element root = editor.root();

        assertEquals("single", root.attribute("attr1"));
        assertEquals("double", root.attribute("attr2"));
        assertEquals("spaced", root.attribute("attr3"));
        assertEquals("no-space", root.attribute("attr4"));

        // Quote styles should be preserved
        assertEquals(QuoteStyle.SINGLE, root.attributeQuote("attr1"));
        assertEquals(QuoteStyle.DOUBLE, root.attributeQuote("attr2"));
        assertEquals(QuoteStyle.SINGLE, root.attributeQuote("attr3"));
        assertEquals(QuoteStyle.DOUBLE, root.attributeQuote("attr4"));
    }

    @Test
    void testEntityHandling() {
        String xml =
                "<root>\n" + "  <text>&lt;tag&gt; &amp; &quot;quotes&quot; &apos;apostrophe&apos;</text>\n" + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element textElement = (Element) editor.root().getNode(1);
        String content = textElement.textContent();

        // Entities should be decoded in the content
        assertEquals("<tag> & \"quotes\" 'apostrophe'", content);

        // But preserved in the XML output
        String result = editor.toXml();
        assertTrue(result.contains("&lt;tag&gt; &amp; &quot;quotes&quot; &apos;apostrophe&apos;"));
    }

    @Test
    void testCDataSections() {
        String xml = "<root>\n" + "  <script><![CDATA[function() { return x < y && z > 0; }]]></script>\n"
                + "  <mixed>Text <![CDATA[and CDATA]]> together</mixed>\n"
                + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        // CDATA should be preserved exactly
        assertTrue(result.contains("<![CDATA[function() { return x < y && z > 0; }]]>"));
        assertTrue(result.contains("<![CDATA[and CDATA]]>"));
    }

    @Test
    void testComments() {
        String xml = "<!-- Document comment -->\n" + "<root>\n"
                + "  <!-- Element comment -->\n"
                + "  <element>content</element>\n"
                + "  <!-- Multi-line\n"
                + "       comment -->\n"
                + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        // Comments should be preserved
        assertTrue(result.contains("<!-- Document comment -->"));
        assertTrue(result.contains("<!-- Element comment -->"));
        assertTrue(result.contains("<!-- Multi-line\n       comment -->"));
    }

    @Test
    void testWhitespaceHandling() {
        String xml = "<root>\n" + "    <element>  content with spaces  </element>\n"
                + "    <preserve xml:space=\"preserve\">  preserve this  </preserve>\n"
                + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element element = (Element) editor.root().getNode(1);
        Element preserve = (Element) editor.root().getNode(3);

        // Text content should include whitespace
        assertEquals("  content with spaces  ", element.textContent());
        assertEquals("  preserve this  ", preserve.textContent());

        String result = editor.toXml();
        assertTrue(result.contains(">  content with spaces  <"));
        assertTrue(result.contains(">  preserve this  <"));
    }

    @Test
    void testNamespaces() {
        String xml = "<root xmlns=\"http://example.com/default\" xmlns:ns=\"http://example.com/ns\">\n"
                + "  <element>default namespace</element>\n"
                + "  <ns:element>namespaced</ns:element>\n"
                + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        // Namespace declarations should be preserved
        assertTrue(result.contains("xmlns=\"http://example.com/default\""));
        assertTrue(result.contains("xmlns:ns=\"http://example.com/ns\""));
        assertTrue(result.contains("<ns:element>"));
    }

    @Test
    void testProcessingInstructions() {
        String xml = "<?xml version=\"1.0\"?>\n" + "<?xml-stylesheet type=\"text/xsl\" href=\"style.xsl\"?>\n"
                + "<root>\n"
                + "  <?target instruction data?>\n"
                + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        // Processing instructions should be preserved
        assertTrue(result.contains("<?xml-stylesheet type=\"text/xsl\" href=\"style.xsl\"?>"));
        assertTrue(result.contains("<?target instruction data?>"));
    }

    @Test
    void testMixedContent() {
        String xml = "<root>\n" + "  Text before <element>element content</element> text after\n"
                + "  <another>more content</another> and more text\n"
                + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element root = editor.root();

        // Should have multiple children: text, element, text, element, text
        assertTrue(root.nodeCount() >= 5);

        String result = editor.toXml();
        assertTrue(result.contains("Text before <element>"));
        assertTrue(result.contains("</element> text after"));
        assertTrue(result.contains("</another> and more text"));
    }

    @Test
    void testDocumentStats() {
        String xml = "<root>\n" + "  <element>text</element>\n" + "  <!-- comment -->\n" + "  <another/>\n" + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        String stats = editor.documentStats();

        // Should count elements, text nodes, and comments
        assertTrue(stats.contains("3 elements")); // root, element, another
        assertTrue(stats.contains("comment"));
    }
}
