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

    // ========== ROUND-TRIPPING ISSUES (DATA LOSS) - NOW FIXED! ==========

    @Test
    void testNumericCharacterReferencesInAttributesNowFixed() {
        // FIXED: Numeric character references in attributes are now properly decoded AND preserved
        String xml = "<root attr=\"line1&#10;line2\"/>";
        Document doc = Document.of(xml);
        String result = doc.toXml();

        // The newline character should be properly decoded
        Element root = doc.root();
        String attrValue = root.attribute("attr");
        assertTrue(attrValue.contains("\n"), "Newline character is properly decoded");
        assertEquals("line1\nline2", attrValue, "Attribute value should contain actual newline");

        // The output should preserve the exact format (&#10;) for perfect round-tripping
        assertTrue(result.contains("&#10;"), "Numeric char ref should be preserved in output");
        assertEquals(xml, result, "Should round-trip exactly");
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
    void testDoctypeNowPreservedPerfectly() {
        // FIXED: DOCTYPE now round-trips perfectly without extra newline
        String xml = "<?xml version=\"1.0\"?>\n<!DOCTYPE root SYSTEM \"example.dtd\">\n<root/>";
        Document doc = Document.of(xml);
        String result = doc.toXml();

        // DOCTYPE is preserved perfectly
        assertTrue(result.contains("<!DOCTYPE root SYSTEM \"example.dtd\">"), "DOCTYPE preserved");
        assertEquals(xml, result, "Should round-trip exactly");
    }

    @Test
    void testAttributeQuotePreservationNowFixed() {
        // FIXED: &quot; in single-quoted attributes is now preserved
        String xml = "<root attr='value with &quot;quotes&quot;'/>";
        Document doc = Document.of(xml);
        String result = doc.toXml();

        // The &quot; is now preserved for perfect round-tripping
        assertTrue(result.contains("&quot;"), "Quote entity preserved");
        assertEquals(xml, result, "Should round-trip exactly");

        // The decoded value is still correct
        Element root = doc.root();
        assertEquals("value with \"quotes\"", root.attribute("attr"), "Value is correctly decoded");
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
