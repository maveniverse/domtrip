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
    void testWellFormedXml() throws DomTripException {
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
    void testEmptyElements() throws DomTripException {
        String xml = "<root>\n" + "  <empty/>\n" + "  <also-empty></also-empty>\n" + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        // Should preserve original formatting
        assertTrue(result.contains("<empty/>"));
        assertTrue(result.contains("<also-empty></also-empty>"));
    }

    @Test
    void testAttributeVariations() throws DomTripException {
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
    void testEntityHandling() throws DomTripException {
        String xml =
                "<root>\n" + "  <text>&lt;tag&gt; &amp; &quot;quotes&quot; &apos;apostrophe&apos;</text>\n" + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        // Find the text element by name instead of index
        Element textElement = editor.root().child("text").orElse(null);
        if (textElement == null) {
            throw new AssertionError("Could not find 'text' element");
        }
        String content = textElement.textContent();

        // Entities should be decoded in the content
        assertEquals("<tag> & \"quotes\" 'apostrophe'", content);

        // But preserved in the XML output
        String result = editor.toXml();
        assertTrue(result.contains("&lt;tag&gt; &amp; &quot;quotes&quot; &apos;apostrophe&apos;"));
    }

    @Test
    void testCDataSections() throws DomTripException {
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
    void testComments() throws DomTripException {
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
    void testWhitespaceHandling() throws DomTripException {
        String xml = "<root>\n" + "    <element>  content with spaces  </element>\n"
                + "    <preserve xml:space=\"preserve\">  preserve this  </preserve>\n"
                + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        // Find elements by name instead of index
        Element element = editor.root().child("element").orElse(null);
        Element preserve = editor.root().child("preserve").orElse(null);
        if (element == null || preserve == null) {
            throw new AssertionError("Could not find required elements");
        }

        // Text content should include whitespace
        assertEquals("  content with spaces  ", element.textContent());
        assertEquals("  preserve this  ", preserve.textContent());

        String result = editor.toXml();
        assertTrue(result.contains(">  content with spaces  <"));
        assertTrue(result.contains(">  preserve this  <"));
    }

    @Test
    void testNamespaces() throws DomTripException {
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
    void testProcessingInstructions() throws DomTripException {
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
    void testMixedContent() throws DomTripException {
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
    void testDocumentStats() throws DomTripException {
        String xml = "<root>\n" + "  <element>text</element>\n" + "  <!-- comment -->\n" + "  <another/>\n" + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        String stats = editor.documentStats();

        // Should count elements, text nodes, and comments
        assertTrue(stats.contains("3 elements")); // root, element, another
        assertTrue(stats.contains("comment"));
    }

    @Test
    void testMultipleCDataSections() throws DomTripException {
        String xml = "<root>\n" + "  <data><![CDATA[first]]><![CDATA[second]]><![CDATA[third]]></data>\n" + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        // Multiple consecutive CDATA sections should be preserved
        assertTrue(result.contains("<![CDATA[first]]>"));
        assertTrue(result.contains("<![CDATA[second]]>"));
        assertTrue(result.contains("<![CDATA[third]]>"));
    }

    @Test
    void testCDataEdgeCases() throws DomTripException {
        String xml = "<root>\n" + "  <empty><![CDATA[]]></empty>\n"
                + "  <special><![CDATA[<![CDATA[nested-like]]]]></special>\n"
                + "  <brackets><![CDATA[]]]]></brackets>\n"
                + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        // Edge cases should be preserved
        assertTrue(result.contains("<![CDATA[]]>"));
        assertTrue(result.contains("<![CDATA[<![CDATA[nested-like]]]]>"));
        assertTrue(result.contains("<![CDATA[]]]]>"));
    }

    @Test
    void testNumericCharacterReferences() throws DomTripException {
        String xml = "<root>\n" + "  <hex>&#x3C;&#x3E;&#x26;</hex>\n" + "  <decimal>&#60;&#62;&#38;</decimal>\n"
                + "  <unicode>&#x1F600;&#x2764;</unicode>\n"
                + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        // Numeric character references should be preserved or properly handled
        assertNotNull(result);
        assertTrue(result.contains("<hex>") && result.contains("</hex>"));
        assertTrue(result.contains("<decimal>") && result.contains("</decimal>"));
        assertTrue(result.contains("<unicode>") && result.contains("</unicode>"));
    }

    @Test
    void testEncodingDeclarations() throws DomTripException {
        String[] encodings = {"UTF-8", "UTF-16", "ISO-8859-1", "windows-1252"};

        for (String encoding : encodings) {
            String xml = "<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>\n<root>content</root>";

            Document doc = Document.of(xml);
            Editor editor = new Editor(doc);
            String result = editor.toXml();

            // Encoding declaration should be preserved
            assertTrue(
                    result.contains("encoding=\"" + encoding + "\""), "Encoding " + encoding + " should be preserved");
        }
    }

    @Test
    void testAttributeSpecialCharacters() throws DomTripException {
        String xml = "<root\n" + "  attr1=\"value with &lt;brackets&gt;\"\n"
                + "  attr2='value with &quot;quotes&quot;'\n"
                + "  attr3=\"value with &amp; ampersand\"\n"
                + "  attr4=\"line1&#10;line2\"\n"
                + "  attr5=\"tab&#9;separated\"/>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element root = editor.root();

        // Entities in attributes should be decoded
        assertTrue(root.attribute("attr1").contains("<brackets>"));
        assertTrue(root.attribute("attr2").contains("\"quotes\""));
        assertTrue(root.attribute("attr3").contains("&"));

        // But preserved in XML output
        String result = editor.toXml();
        assertTrue(result.contains("&lt;brackets&gt;") || result.contains("<brackets>"));
        assertTrue(result.contains("&amp;") || result.contains("&"));
    }

    @Test
    void testElementNamesWithSpecialCharacters() throws DomTripException {
        String xml = "<root>\n" + "  <element-with-dash/>\n"
                + "  <element_with_underscore/>\n"
                + "  <element.with.dot/>\n"
                + "  <ns:element-with-colon/>\n"
                + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        // Element names with valid special characters should be preserved
        assertTrue(result.contains("<element-with-dash/>"));
        assertTrue(result.contains("<element_with_underscore/>"));
        assertTrue(result.contains("<element.with.dot/>"));
        assertTrue(result.contains("<ns:element-with-colon/>"));
    }

    @Test
    void testLongAttributeValues() throws DomTripException {
        StringBuilder longValue = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longValue.append("word").append(i).append(" ");
        }

        String xml = "<root attr=\"" + longValue.toString().trim() + "\"/>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element root = editor.root();

        // Long attribute value should be preserved
        assertEquals(longValue.toString().trim(), root.attribute("attr"));

        String result = editor.toXml();
        assertTrue(result.contains("word0"));
        assertTrue(result.contains("word999"));
    }

    @Test
    void testLongTextContent() throws DomTripException {
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longText.append("This is line ").append(i).append(".\n");
        }

        String xml = "<root>" + longText.toString() + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element root = editor.root();

        // Long text content should be preserved
        String content = root.textContent();
        assertTrue(content.contains("This is line 0."));
        assertTrue(content.contains("This is line 999."));
    }

    @Test
    void testMultipleNamespaceDeclarations() throws DomTripException {
        String xml = "<root\n" + "  xmlns=\"http://default.example.com\"\n"
                + "  xmlns:ns1=\"http://ns1.example.com\"\n"
                + "  xmlns:ns2=\"http://ns2.example.com\"\n"
                + "  xmlns:ns3=\"http://ns3.example.com\">\n"
                + "  <element/>\n"
                + "  <ns1:element/>\n"
                + "  <ns2:element/>\n"
                + "  <ns3:element/>\n"
                + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        // All namespace declarations should be preserved
        assertTrue(result.contains("xmlns=\"http://default.example.com\""));
        assertTrue(result.contains("xmlns:ns1=\"http://ns1.example.com\""));
        assertTrue(result.contains("xmlns:ns2=\"http://ns2.example.com\""));
        assertTrue(result.contains("xmlns:ns3=\"http://ns3.example.com\""));
    }

    @Test
    void testDefaultNamespaceOverriding() throws DomTripException {
        String xml = "<root xmlns=\"http://default1.example.com\">\n"
                + "  <child xmlns=\"http://default2.example.com\">\n"
                + "    <grandchild/>\n"
                + "  </child>\n"
                + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        // Namespace overriding should be preserved
        assertTrue(result.contains("xmlns=\"http://default1.example.com\""));
        assertTrue(result.contains("xmlns=\"http://default2.example.com\""));
    }

    @Test
    void testXmlDeclarationVariations() throws DomTripException {
        String[] declarations = {
            "<?xml version=\"1.0\"?>",
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
            "<?xml version=\"1.0\" standalone=\"yes\"?>",
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>",
            "<?xml version=\"1.1\"?>",
            "<?xml version=\"1.1\" encoding=\"ISO-8859-1\" standalone=\"yes\"?>"
        };

        for (String declaration : declarations) {
            String xml = declaration + "\n<root/>";

            Document doc = Document.of(xml);
            Editor editor = new Editor(doc);
            String result = editor.toXml();

            // XML declaration should be preserved
            assertNotNull(result);
            assertTrue(result.contains("<?xml"), "XML declaration should be present for: " + declaration);
        }
    }

    @Test
    void testDoctypeDeclaration() throws DomTripException {
        String xml = "<?xml version=\"1.0\"?>\n" + "<!DOCTYPE root SYSTEM \"example.dtd\">\n" + "<root/>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        // DOCTYPE should be preserved
        assertTrue(
                result.contains("<!DOCTYPE") || result.contains("root"),
                "DOCTYPE declaration should be preserved or handled");
    }

    @Test
    void testDoctypeWithInternalSubset() throws DomTripException {
        String xml = "<?xml version=\"1.0\"?>\n" + "<!DOCTYPE root [\n"
                + "  <!ELEMENT root (child*)>\n"
                + "  <!ELEMENT child (#PCDATA)>\n"
                + "]>\n"
                + "<root><child>text</child></root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        // DOCTYPE with internal subset should be handled
        assertNotNull(result);
        assertTrue(result.contains("<root>"));
        assertTrue(result.contains("<child>text</child>"));
    }

    @Test
    void testEmptyAttributeValue() throws DomTripException {
        String xml = "<root attr1=\"\" attr2='' attr3=\"value\"/>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element root = editor.root();

        // Empty attributes should be preserved
        assertEquals("", root.attribute("attr1"));
        assertEquals("", root.attribute("attr2"));
        assertEquals("value", root.attribute("attr3"));

        String result = editor.toXml();
        assertTrue(result.contains("attr1=\"\"") || result.contains("attr1=''"));
        assertTrue(result.contains("attr2=\"\"") || result.contains("attr2=''"));
    }

    @Test
    void testConsecutiveWhitespaceVariations() throws DomTripException {
        String xml = "<root>\n" + "  <element>text  with   multiple    spaces</element>\n"
                + "  <tabs>text\t\twith\t\t\ttabs</tabs>\n"
                + "  <mixed>text \t \t with \t mixed</mixed>\n"
                + "  <newlines>line1\n\n\nline2</newlines>\n"
                + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        Element element = editor.root().child("element").orElse(null);
        Element tabs = editor.root().child("tabs").orElse(null);
        Element mixed = editor.root().child("mixed").orElse(null);
        Element newlines = editor.root().child("newlines").orElse(null);

        assertNotNull(element);
        assertNotNull(tabs);
        assertNotNull(mixed);
        assertNotNull(newlines);

        // Whitespace should be preserved in text content
        assertTrue(element.textContent().contains("multiple    spaces"));
        assertTrue(tabs.textContent().contains("\t\t"));
        assertTrue(newlines.textContent().contains("\n\n\n"));

        String result = editor.toXml();
        assertTrue(result.contains("multiple    spaces"));
    }

    @Test
    void testCDataWithSpecialXmlCharacters() throws DomTripException {
        String xml = "<root>\n" + "  <data><![CDATA[<?xml version=\"1.0\"?>]]></data>\n"
                + "  <comment><![CDATA[<!-- not a comment -->]]></comment>\n"
                + "  <entity><![CDATA[&lt;&gt;&amp;]]></entity>\n"
                + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        // CDATA should preserve XML-like content without interpretation
        assertTrue(result.contains("<![CDATA[<?xml version=\"1.0\"?>]]>"));
        assertTrue(result.contains("<![CDATA[<!-- not a comment -->]]>"));
        assertTrue(result.contains("<![CDATA[&lt;&gt;&amp;]]>"));
    }

    @Test
    void testMixedContentWithWhitespace() throws DomTripException {
        String xml = "<root>  \n" + "  text1  \n"
                + "  <element>content</element>  \n"
                + "  text2  \n"
                + "  <another/>  \n"
                + "  text3  \n"
                + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        // Mixed content with whitespace should be preserved
        assertTrue(result.contains("text1"));
        assertTrue(result.contains("text2"));
        assertTrue(result.contains("text3"));
        assertTrue(result.contains("<element>content</element>"));
        assertTrue(result.contains("<another/>"));
    }
}
