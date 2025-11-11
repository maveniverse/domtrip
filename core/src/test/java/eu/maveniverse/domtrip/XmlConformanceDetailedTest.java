package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * Detailed tests to identify actual XML conformance and round-tripping limitations.
 * This test class helps distinguish between:
 * 1. Round-tripping issues (data loss) - MUST be fixed
 * 2. XML conformance issues (spec compliance) - Document as limitations
 *
 * NOTE: Some tests in this class are expected to fail and document known limitations.
 * See XmlConformanceLimitationsTest for tests that verify the documented behavior.
 */
public class XmlConformanceDetailedTest {

    @Test
    void testDoctypeRoundTrip() throws DomTripException {
        String xml = "<?xml version=\"1.0\"?>\n" + "<!DOCTYPE root SYSTEM \"example.dtd\">\n" + "<root/>";

        Document doc = Document.of(xml);
        String result = doc.toXml();

        // Check if DOCTYPE is preserved
        assertNotNull(doc.doctype(), "DOCTYPE should be captured");
        assertTrue(result.contains("<!DOCTYPE"), "DOCTYPE should be in output: " + result);
        assertEquals(xml, result, "DOCTYPE should round-trip exactly");
    }

    @Test
    void testDoctypeWithInternalSubsetRoundTrip() throws DomTripException {
        String xml = "<?xml version=\"1.0\"?>\n" + "<!DOCTYPE root [\n"
                + "  <!ELEMENT root (child*)>\n"
                + "  <!ELEMENT child (#PCDATA)>\n"
                + "]>\n"
                + "<root><child>text</child></root>";

        Document doc = Document.of(xml);
        String result = doc.toXml();

        // Check if DOCTYPE with internal subset is preserved
        assertNotNull(doc.doctype(), "DOCTYPE should be captured");
        assertTrue(result.contains("<!DOCTYPE"), "DOCTYPE should be in output");
        assertTrue(result.contains("<!ELEMENT"), "Internal subset should be preserved");
        assertEquals(xml, result, "DOCTYPE with internal subset should round-trip exactly");
    }

    @Test
    void testDoctypeWithPublicIdRoundTrip() throws DomTripException {
        String xml = "<?xml version=\"1.0\"?>\n"
                + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"
                + "<html/>";

        Document doc = Document.of(xml);
        String result = doc.toXml();

        assertNotNull(doc.doctype(), "DOCTYPE should be captured");
        assertTrue(result.contains("PUBLIC"), "PUBLIC identifier should be preserved");
        assertEquals(xml, result, "DOCTYPE with PUBLIC should round-trip exactly");
    }

    @Test
    void testCustomEntityDefinitionRoundTrip() throws DomTripException {
        String xml = "<?xml version=\"1.0\"?>\n" + "<!DOCTYPE root [\n"
                + "  <!ENTITY custom \"Custom Value\">\n"
                + "]>\n"
                + "<root>&custom;</root>";

        Document doc = Document.of(xml);
        String result = doc.toXml();

        // Check if entity definition is preserved
        assertTrue(result.contains("<!ENTITY"), "Entity definition should be preserved");
        assertTrue(result.contains("&custom;"), "Entity reference should be preserved");
        assertEquals(xml, result, "Custom entity should round-trip exactly");
    }

    @Test
    void testNumericCharacterReferencesRoundTrip() throws DomTripException {
        String xml = "<root>\n" + "  <hex>&#x3C;&#x3E;&#x26;</hex>\n"
                + "  <decimal>&#60;&#62;&#38;</decimal>\n"
                + "  <unicode>&#x1F600;&#x2764;</unicode>\n"
                + "</root>";

        Document doc = Document.of(xml);
        String result = doc.toXml();

        // Numeric character references should be preserved exactly
        assertTrue(
                result.contains("&#x3C;") || result.contains("&lt;"),
                "Hex references should be preserved or normalized");
        assertTrue(
                result.contains("&#60;") || result.contains("&lt;"),
                "Decimal references should be preserved or normalized");
        assertTrue(result.contains("&#x1F600;") || result.contains("😀"), "Unicode should be preserved or decoded");

        // Check for exact round-trip
        assertEquals(xml, result, "Numeric character references should round-trip exactly");
    }

    @Test
    void testEncodingDeclarationRoundTrip() throws DomTripException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root>Test</root>";

        Document doc = Document.of(xml);
        String result = doc.toXml();

        assertEquals("UTF-8", doc.encoding(), "Encoding should be captured");
        assertTrue(result.contains("encoding=\"UTF-8\""), "Encoding should be in output");
        assertEquals(xml, result, "Encoding declaration should round-trip exactly");
    }

    @Test
    void testEncodingWithSpecialCharactersRoundTrip() throws DomTripException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root>Spëcîål çhàrś: àáâãäå</root>";

        Document doc = Document.of(xml);
        String result = doc.toXml();

        assertTrue(result.contains("Spëcîål çhàrś"), "Special characters should be preserved");
        assertEquals(xml, result, "Special characters should round-trip exactly");
    }

    @Test
    void testEncodingWithByteStreamRoundTrip() throws DomTripException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root>Test: àáâãäå</root>";

        // Convert to bytes and parse from InputStream
        ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        Parser parser = new Parser();
        Document doc = parser.parse(input);

        // Serialize back to bytes
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        doc.toXml(output);
        String result = output.toString(StandardCharsets.UTF_8);

        assertEquals("UTF-8", doc.encoding(), "Encoding should be detected");
        assertEquals(xml, result, "Byte stream should round-trip exactly");
    }

    @Test
    void testXmlDeclarationStandaloneRoundTrip() throws DomTripException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<root/>";

        Document doc = Document.of(xml);
        String result = doc.toXml();

        assertTrue(doc.isStandalone(), "Standalone should be captured");
        assertTrue(result.contains("standalone=\"yes\""), "Standalone should be in output");
        assertEquals(xml, result, "Standalone declaration should round-trip exactly");
    }

    @Test
    void testXmlVersion11RoundTrip() throws DomTripException {
        String xml = "<?xml version=\"1.1\"?>\n<root/>";

        Document doc = Document.of(xml);
        String result = doc.toXml();

        assertEquals("1.1", doc.version(), "Version should be captured");
        assertTrue(result.contains("version=\"1.1\""), "Version 1.1 should be in output");
        assertEquals(xml, result, "XML 1.1 declaration should round-trip exactly");
    }

    @Test
    void testProcessingInstructionRoundTrip() throws DomTripException {
        String xml = "<?xml version=\"1.0\"?>\n" + "<?xml-stylesheet type=\"text/xsl\" href=\"style.xsl\"?>\n"
                + "<root>\n"
                + "  <?target instruction data?>\n"
                + "</root>";

        Document doc = Document.of(xml);
        String result = doc.toXml();

        assertTrue(result.contains("<?xml-stylesheet"), "PI should be preserved");
        assertTrue(result.contains("<?target"), "PI should be preserved");
        assertEquals(xml, result, "Processing instructions should round-trip exactly");
    }

    @Test
    void testCDataWithSpecialContentRoundTrip() throws DomTripException {
        String xml = "<root>\n" + "  <data><![CDATA[<?xml version=\"1.0\"?>]]></data>\n"
                + "  <comment><![CDATA[<!-- not a comment -->]]></comment>\n"
                + "  <entity><![CDATA[&lt;&gt;&amp;]]></entity>\n"
                + "  <brackets><![CDATA[]]]]></brackets>\n"
                + "</root>";

        Document doc = Document.of(xml);
        String result = doc.toXml();

        assertTrue(
                result.contains("<![CDATA[<?xml version=\"1.0\"?>]]>"),
                "CDATA with PI-like content should be preserved");
        assertTrue(
                result.contains("<![CDATA[<!-- not a comment -->]]>"),
                "CDATA with comment-like content should be preserved");
        assertTrue(result.contains("<![CDATA[&lt;&gt;&amp;]]>"), "CDATA with entities should be preserved");
        assertTrue(result.contains("<![CDATA[]]]]>"), "CDATA with ]] should be preserved");
        assertEquals(xml, result, "CDATA with special content should round-trip exactly");
    }

    @Test
    void testAttributeWithSpecialCharactersRoundTrip() throws DomTripException {
        String xml = "<root\n" + "  attr1=\"value with &lt;brackets&gt;\"\n"
                + "  attr2='value with &quot;quotes&quot;'\n"
                + "  attr3=\"value with &amp; ampersand\"\n"
                + "  attr4=\"line1&#10;line2\"\n"
                + "  attr5=\"tab&#9;separated\"/>";

        Document doc = Document.of(xml);
        String result = doc.toXml();

        // Check that entities are preserved in attributes
        assertTrue(result.contains("&lt;") || result.contains("<"), "Entities in attributes should be preserved");
        assertTrue(result.contains("&amp;") || result.contains("&"), "Ampersand entity should be preserved");
        assertEquals(xml, result, "Attributes with special characters should round-trip exactly");
    }

    @Test
    void testEmptyAttributeRoundTrip() throws DomTripException {
        String xml = "<root attr1=\"\" attr2='' attr3=\"value\"/>";

        Document doc = Document.of(xml);
        String result = doc.toXml();

        Element root = doc.root();
        assertEquals("", root.attribute("attr1"), "Empty attribute should be preserved");
        assertEquals("", root.attribute("attr2"), "Empty attribute should be preserved");
        assertEquals("value", root.attribute("attr3"), "Non-empty attribute should be preserved");
        assertEquals(xml, result, "Empty attributes should round-trip exactly");
    }

    @Test
    void testNamespaceDeclarationRoundTrip() throws DomTripException {
        String xml = "<root xmlns=\"http://example.com/default\" xmlns:ns=\"http://example.com/ns\">\n"
                + "  <element>default namespace</element>\n"
                + "  <ns:element>namespaced</ns:element>\n"
                + "</root>";

        Document doc = Document.of(xml);
        String result = doc.toXml();

        assertTrue(result.contains("xmlns=\"http://example.com/default\""), "Default namespace should be preserved");
        assertTrue(result.contains("xmlns:ns=\"http://example.com/ns\""), "Namespace prefix should be preserved");
        assertTrue(result.contains("<ns:element>"), "Namespaced element should be preserved");
        assertEquals(xml, result, "Namespace declarations should round-trip exactly");
    }

    @Test
    void testDefaultNamespaceOverridingRoundTrip() throws DomTripException {
        String xml = "<root xmlns=\"http://default1.example.com\">\n"
                + "  <child xmlns=\"http://default2.example.com\">\n"
                + "    <grandchild/>\n"
                + "  </child>\n"
                + "</root>";

        Document doc = Document.of(xml);
        String result = doc.toXml();

        assertTrue(
                result.contains("xmlns=\"http://default1.example.com\""),
                "First default namespace should be preserved");
        assertTrue(
                result.contains("xmlns=\"http://default2.example.com\""),
                "Overriding default namespace should be preserved");
        assertEquals(xml, result, "Default namespace overriding should round-trip exactly");
    }

    @Test
    void testWhitespaceInTextContentRoundTrip() throws DomTripException {
        String xml = "<root>\n" + "  <element>  content with spaces  </element>\n"
                + "  <tabs>text\t\twith\t\t\ttabs</tabs>\n"
                + "  <newlines>line1\n\n\nline2</newlines>\n"
                + "</root>";

        Document doc = Document.of(xml);
        String result = doc.toXml();

        Element element = doc.root().child("element").orElseThrow();
        assertEquals("  content with spaces  ", element.textContent(), "Spaces should be preserved");

        Element tabs = doc.root().child("tabs").orElseThrow();
        assertTrue(tabs.textContent().contains("\t\t"), "Tabs should be preserved");

        Element newlines = doc.root().child("newlines").orElseThrow();
        assertTrue(newlines.textContent().contains("\n\n\n"), "Newlines should be preserved");

        assertEquals(xml, result, "Whitespace in text content should round-trip exactly");
    }

    @Test
    void testCommentRoundTrip() throws DomTripException {
        String xml = "<!-- Document comment -->\n" + "<root>\n"
                + "  <!-- Element comment -->\n"
                + "  <element>content</element>\n"
                + "  <!-- Multi-line\n"
                + "       comment -->\n"
                + "</root>";

        Document doc = Document.of(xml);
        String result = doc.toXml();

        assertTrue(result.contains("<!-- Document comment -->"), "Document comment should be preserved");
        assertTrue(result.contains("<!-- Element comment -->"), "Element comment should be preserved");
        assertTrue(result.contains("<!-- Multi-line\n       comment -->"), "Multi-line comment should be preserved");
        assertEquals(xml, result, "Comments should round-trip exactly");
    }
}
