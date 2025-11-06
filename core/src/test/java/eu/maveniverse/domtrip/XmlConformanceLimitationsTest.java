package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Documents actual XML conformance limitations vs round-tripping capabilities.
 * This test class categorizes issues into:
 * 1. Round-tripping issues (data loss) - Should be fixed
 * 2. Minor formatting differences (acceptable)
 * 3. XML spec conformance issues (document as limitations)
 */
public class XmlConformanceLimitationsTest {

    // ========== ROUND-TRIPPING ISSUES (DATA LOSS) ==========

    @Test
    void testNumericCharacterReferencesInAttributesNotPreserved() {
        // ISSUE: Numeric character references in attributes are double-escaped
        String xml = "<root attr=\"line1&#10;line2\"/>";
        Document doc = Document.of(xml);
        String result = doc.toXml();

        // The &#10; becomes &amp;#10; which is wrong
        assertFalse(result.contains("&#10;"), "Numeric char refs are double-escaped");
        assertTrue(result.contains("&amp;#10;"), "Shows the double-escaping issue");

        // This is a DATA LOSS issue - the newline character is lost
        Element root = doc.root();
        String attrValue = root.attribute("attr");
        assertFalse(attrValue.contains("\n"), "Newline character is lost");
    }

    @Test
    void testXmlDeclarationAttributesNotParsed() {
        // ISSUE: XML declaration attributes (version, standalone) are not parsed from string
        String xml = "<?xml version=\"1.1\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<root/>";
        Document doc = Document.of(xml);

        // These should be parsed but aren't
        assertEquals("1.0", doc.version(), "Version not parsed from declaration");
        assertFalse(doc.isStandalone(), "Standalone not parsed from declaration");

        // However, the declaration IS preserved as-is
        String result = doc.toXml();
        assertTrue(result.contains("version=\"1.1\""), "Declaration preserved as string");
        assertTrue(result.contains("standalone=\"yes\""), "Declaration preserved as string");
    }

    // ========== MINOR FORMATTING DIFFERENCES (ACCEPTABLE) ==========

    @Test
    void testDoctypeAddsExtraNewline() {
        // MINOR: Extra newline added after DOCTYPE
        String xml = "<?xml version=\"1.0\"?>\n<!DOCTYPE root SYSTEM \"example.dtd\">\n<root/>";
        Document doc = Document.of(xml);
        String result = doc.toXml();

        // DOCTYPE is preserved but with extra newline
        assertTrue(result.contains("<!DOCTYPE root SYSTEM \"example.dtd\">"), "DOCTYPE preserved");
        assertTrue(result.contains(">\n\n<root"), "Extra newline added");

        // This is acceptable - no data loss, just formatting
    }

    @Test
    void testAttributeQuoteNormalization() {
        // MINOR: &quot; in single-quoted attributes becomes literal "
        String xml = "<root attr='value with &quot;quotes&quot;'/>";
        Document doc = Document.of(xml);
        String result = doc.toXml();

        // The &quot; becomes " because it's in single quotes
        assertTrue(result.contains("'value with \"quotes\"'"), "Quote normalized");

        // This is acceptable - semantically equivalent
        Element root = doc.root();
        assertEquals("value with \"quotes\"", root.attribute("attr"), "Value is correct");
    }

    @Test
    void testProcessingInstructionsPreserved() {
        // XML declaration and other PIs are preserved
        String xml = "<?xml version=\"1.0\"?>\n<?xml-stylesheet type=\"text/xsl\" href=\"style.xsl\"?>\n<root/>";
        Document doc = Document.of(xml);
        String result = doc.toXml();

        // Both PIs are preserved (though xml declaration might not have a newline)
        assertTrue(result.contains("<?xml-stylesheet"), "Other PI preserved");
        // The exact formatting might differ slightly but content is preserved
    }

    // ========== WHAT WORKS PERFECTLY ==========

    @Test
    void testStandardEntitiesPreserved() {
        String xml = "<root>&lt;&gt;&amp;&quot;&apos;</root>";
        Document doc = Document.of(xml);
        String result = doc.toXml();

        assertEquals(xml, result, "Standard entities round-trip perfectly");
    }

    @Test
    void testCDataPreserved() {
        String xml = "<root><![CDATA[<tag> & special]]></root>";
        Document doc = Document.of(xml);
        String result = doc.toXml();

        assertEquals(xml, result, "CDATA round-trips perfectly");
    }

    @Test
    void testCommentsPreserved() {
        String xml = "<!-- comment -->\n<root/>";
        Document doc = Document.of(xml);
        String result = doc.toXml();

        assertEquals(xml, result, "Comments round-trip perfectly");
    }

    @Test
    void testWhitespacePreserved() {
        String xml = "<root>  text with   spaces  </root>";
        Document doc = Document.of(xml);
        String result = doc.toXml();

        assertEquals(xml, result, "Whitespace round-trips perfectly");
    }

    @Test
    void testNamespacesPreserved() {
        String xml =
                "<root xmlns=\"http://example.com\" xmlns:ns=\"http://ns.com\">\n" + "  <ns:element/>\n" + "</root>";
        Document doc = Document.of(xml);
        String result = doc.toXml();

        assertEquals(xml, result, "Namespaces round-trip perfectly");
    }

    @Test
    void testAttributeOrderPreserved() {
        String xml = "<root z=\"3\" a=\"1\" m=\"2\"/>";
        Document doc = Document.of(xml);
        String result = doc.toXml();

        assertEquals(xml, result, "Attribute order round-trips perfectly");
    }

    @Test
    void testAttributeQuoteStylePreserved() {
        String xml = "<root attr1='single' attr2=\"double\"/>";
        Document doc = Document.of(xml);
        String result = doc.toXml();

        assertEquals(xml, result, "Quote styles round-trip perfectly");
    }

    @Test
    void testEmptyAttributesPreserved() {
        String xml = "<root attr=\"\"/>";
        Document doc = Document.of(xml);
        String result = doc.toXml();

        assertEquals(xml, result, "Empty attributes round-trip perfectly");
    }
}
