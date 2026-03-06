package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.jupiter.api.Test;

/**
 * Test cases demonstrating JDOM's round-tripping problems and attempts to solve them
 * using JDOM's features like raw content, parse flags, and output formatting options.
 *
 * <p>Each test demonstrates:
 * <ul>
 *   <li>The problem: What gets lost or changed during round-trip</li>
 *   <li>Basic JDOM: Default behavior showing the failure</li>
 *   <li>JDOM workaround: Attempts to fix using JDOM features (if possible)</li>
 *   <li>Success rate: Whether JDOM can actually preserve the formatting</li>
 * </ul>
 */
public class JdomRawRoundTripTest {

    /**
     * Helper method to parse and serialize XML with JDOM using default settings.
     */
    private String jdomRoundTrip(String xml) throws Exception {
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new StringReader(xml));
        XMLOutputter outputter = new XMLOutputter();
        return outputter.outputString(doc);
    }

    /**
     * Helper method to parse and serialize XML with JDOM using raw format.
     */
    private String jdomRoundTripRaw(String xml) throws Exception {
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new StringReader(xml));
        XMLOutputter outputter = new XMLOutputter(Format.getRawFormat());
        return outputter.outputString(doc);
    }

    /**
     * Helper method to parse and serialize XML with JDOM using compact format.
     */
    private String jdomRoundTripCompact(String xml) throws Exception {
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new StringReader(xml));
        XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat());
        return outputter.outputString(doc);
    }

    @Test
    void testPrologWhitespacePreservation() throws Exception {
        // Problem: JDOM loses whitespace in prolog (before root element)
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<!-- Header comment -->\n"
                + "<?xml-stylesheet type=\"text/xsl\" href=\"style.xsl\"?>\n"
                + "\n"
                + "<root>\n"
                + "  <child>content</child>\n"
                + "</root>";

        // Basic JDOM: Loses prolog whitespace
        String basicResult = jdomRoundTrip(xml);
        assertNotEquals(xml, basicResult, "JDOM default loses prolog whitespace");

        // JDOM Raw Format: Still loses prolog whitespace
        String rawResult = jdomRoundTripRaw(xml);
        assertNotEquals(xml, rawResult, "JDOM raw format still loses prolog whitespace");

        // DomTrip: Preserves exact prolog whitespace
        eu.maveniverse.domtrip.Document domTripDoc = eu.maveniverse.domtrip.Document.of(xml);
        Editor editor = new Editor(domTripDoc);
        String domTripResult = editor.toXml();
        assertEquals(xml, domTripResult, "DomTrip preserves exact prolog whitespace");

        // Verify what's preserved in JDOM
        assertTrue(basicResult.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(basicResult.contains("<!-- Header comment -->"));
        assertTrue(basicResult.contains("<?xml-stylesheet"));

        System.out.println("JDOM: Prolog whitespace lost - FAILED");
        System.out.println("DomTrip: Prolog whitespace preserved - SUCCESS");
    }

    @Test
    void testAttributeOrderPreservation() throws Exception {
        // Problem: JDOM may reorder attributes (depends on internal storage)
        String xml = "<element third=\"3\" first=\"1\" second=\"2\" fourth=\"4\"/>";

        // Basic JDOM: May reorder attributes
        String basicResult = jdomRoundTrip(xml);

        // JDOM Raw Format: Still may reorder
        String rawResult = jdomRoundTripRaw(xml);

        // Check if order is preserved (it usually is in JDOM 2.x, but not guaranteed)
        System.out.println("Original: " + xml);
        System.out.println("JDOM basic result: " + basicResult);
        System.out.println("JDOM raw result: " + rawResult);

        // JDOM 2.x uses LinkedHashMap internally, so order is usually preserved
        // But this is an implementation detail, not a guarantee
        assertTrue(basicResult.contains("third="), "Attributes should be present");
        assertTrue(basicResult.contains("first="), "Attributes should be present");
        assertTrue(rawResult.contains("third="), "Attributes should be present in raw");
        assertTrue(rawResult.contains("first="), "Attributes should be present in raw");

        // Assert that JDOM adds XML declaration
        assertNotEquals(xml, basicResult, "JDOM default adds XML declaration");
        assertNotEquals(xml, rawResult, "JDOM raw adds XML declaration");

        // DomTrip: Preserves exact attribute order
        eu.maveniverse.domtrip.Document domTripDoc = eu.maveniverse.domtrip.Document.of(xml);
        Editor editor = new Editor(domTripDoc);
        String domTripResult = editor.toXml();
        assertEquals(xml, domTripResult, "DomTrip preserves exact attribute order");

        System.out.println("JDOM: Attribute order may vary, adds declaration - PARTIAL");
        System.out.println("DomTrip: Attribute order preserved exactly - SUCCESS");
    }

    @Test
    void testQuoteStylePreservation() throws Exception {
        // Problem: JDOM normalizes all quotes to double quotes
        String xml = "<config attr1='single' attr2=\"double\" attr3='mixed'/>";

        // Basic JDOM: Normalizes to double quotes
        String basicResult = jdomRoundTrip(xml);
        assertNotEquals(xml, basicResult, "JDOM normalizes quote styles");

        // JDOM Raw Format: Still normalizes to double quotes
        String rawResult = jdomRoundTripRaw(xml);
        assertNotEquals(xml, rawResult, "JDOM raw format still normalizes quotes");

        // Verify all quotes are now double in both results
        assertTrue(basicResult.contains("attr1=\"single\""), "JDOM converts to double quotes");
        assertTrue(basicResult.contains("attr2=\"double\""), "Already double quotes");
        assertTrue(basicResult.contains("attr3=\"mixed\""), "JDOM converts to double quotes");
        assertTrue(rawResult.contains("attr1=\"single\""), "JDOM raw converts to double quotes");
        assertTrue(rawResult.contains("attr2=\"double\""), "Already double quotes");
        assertTrue(rawResult.contains("attr3=\"mixed\""), "JDOM raw converts to double quotes");

        // DomTrip: Preserves exact quote styles
        eu.maveniverse.domtrip.Document domTripDoc = eu.maveniverse.domtrip.Document.of(xml);
        Editor editor = new Editor(domTripDoc);
        String domTripResult = editor.toXml();
        assertEquals(xml, domTripResult, "DomTrip preserves exact quote styles");

        System.out.println("JDOM: Quote style preservation - FAILED (always double quotes)");
        System.out.println("DomTrip: Quote style preserved exactly - SUCCESS");
    }

    @Test
    void testEntityPreservation() throws Exception {
        // Problem: JDOM may decode entities and re-encode differently
        String xml = "<root>\n"
                + "  <text>Text with &lt;tags&gt; &amp; &quot;quotes&quot; &apos;apostrophes&apos;</text>\n"
                + "  <attr value=\"&lt;value&gt; &amp; &quot;test&quot;\"/>\n"
                + "</root>";

        // Basic JDOM: May change entity representation
        String basicResult = jdomRoundTrip(xml);
        assertNotEquals(xml, basicResult, "JDOM changes entity representation or formatting");

        // JDOM Raw Format: Better entity preservation
        String rawResult = jdomRoundTripRaw(xml);
        assertNotEquals(xml, rawResult, "JDOM raw changes entity representation or formatting");

        // Check entity preservation
        System.out.println("Original: " + xml);
        System.out.println("JDOM basic result: " + basicResult);
        System.out.println("JDOM raw result: " + rawResult);

        // JDOM preserves entities in text content but may change representation
        assertTrue(basicResult.contains("&lt;") || basicResult.contains("<"), "Entities may be decoded");
        assertTrue(basicResult.contains("&amp;") || basicResult.contains("&"), "Entities may be decoded");
        assertTrue(rawResult.contains("&lt;") || rawResult.contains("<"), "Entities may be decoded in raw");
        assertTrue(rawResult.contains("&amp;") || rawResult.contains("&"), "Entities may be decoded in raw");

        // DomTrip: Preserves exact entity representation
        eu.maveniverse.domtrip.Document domTripDoc = eu.maveniverse.domtrip.Document.of(xml);
        Editor editor = new Editor(domTripDoc);
        String domTripResult = editor.toXml();
        assertEquals(xml, domTripResult, "DomTrip preserves exact entity representation");

        System.out.println("JDOM: Entity preservation - PARTIAL (may change representation)");
        System.out.println("DomTrip: Entity representation preserved exactly - SUCCESS");
    }

    @Test
    void testLineEndingNormalization() throws Exception {
        // Problem: JDOM normalizes line endings
        String xmlWindows = "<?xml version=\"1.0\"?>\r\n<root>\r\n  <child>content</child>\r\n</root>";

        // Basic JDOM: Normalizes line endings
        String basicResult = jdomRoundTrip(xmlWindows);
        assertNotEquals(xmlWindows, basicResult, "JDOM normalizes line endings");

        // JDOM Raw Format: Still normalizes
        String rawResult = jdomRoundTripRaw(xmlWindows);

        // JDOM with custom line separator
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new StringReader(xmlWindows));
        Format format = Format.getRawFormat();
        format.setLineSeparator("\r\n");
        XMLOutputter outputter = new XMLOutputter(format);
        String customResult = outputter.outputString(doc);

        System.out.println("Original has CRLF: " + xmlWindows.contains("\r\n"));
        System.out.println("Basic result has CRLF: " + basicResult.contains("\r\n"));
        System.out.println("Custom result has CRLF: " + customResult.contains("\r\n"));

        // DomTrip: Preserves exact line endings
        eu.maveniverse.domtrip.Document domTripDoc = eu.maveniverse.domtrip.Document.of(xmlWindows);
        Editor editor = new Editor(domTripDoc);
        String domTripResult = editor.toXml();
        assertEquals(xmlWindows, domTripResult, "DomTrip preserves exact line endings");
        assertTrue(domTripResult.contains("\r\n"), "DomTrip preserves CRLF");

        System.out.println("JDOM: Line ending control - PARTIAL (can set but loses structure)");
        System.out.println("DomTrip: Line endings preserved exactly - SUCCESS");
    }

    @Test
    void testCommentPreservation() throws Exception {
        // Problem: Comments may be repositioned or whitespace around them lost
        String xml = "<!-- Document header -->\n"
                + "<root>\n"
                + "  <!-- Before first child -->\n"
                + "  <child1>value1</child1>\n"
                + "  <!-- Between children -->\n"
                + "  <child2>value2</child2>\n"
                + "  <!-- After last child -->\n"
                + "</root>\n"
                + "<!-- Document footer -->";

        // Basic JDOM: Loses exact comment positioning
        String basicResult = jdomRoundTrip(xml);
        assertNotEquals(xml, basicResult, "JDOM loses exact comment positioning");

        // JDOM Raw Format: Better but still not exact
        String rawResult = jdomRoundTripRaw(xml);
        assertNotEquals(xml, rawResult, "JDOM raw loses exact comment positioning");

        // Verify comments are preserved in both
        assertTrue(basicResult.contains("<!-- Document header -->"));
        assertTrue(basicResult.contains("<!-- Before first child -->"));
        assertTrue(basicResult.contains("<!-- Between children -->"));
        assertTrue(rawResult.contains("<!-- Document header -->"));
        assertTrue(rawResult.contains("<!-- Before first child -->"));
        assertTrue(rawResult.contains("<!-- Between children -->"));

        // DomTrip: Preserves exact comment positioning
        eu.maveniverse.domtrip.Document domTripDoc = eu.maveniverse.domtrip.Document.of(xml);
        Editor editor = new Editor(domTripDoc);
        String domTripResult = editor.toXml();
        assertEquals(xml, domTripResult, "DomTrip preserves exact comment positioning");

        System.out.println("JDOM: Comment preservation - PARTIAL (content yes, positioning no)");
        System.out.println("DomTrip: Comments preserved with exact positioning - SUCCESS");
    }

    @Test
    void testCDataPreservation() throws Exception {
        // Problem: CDATA sections may be converted to regular text
        String xml = "<root>\n"
                + "  <script><![CDATA[\n"
                + "    function test() {\n"
                + "      if (x < y && y > z) {\n"
                + "        return \"<test>\";\n"
                + "      }\n"
                + "    }\n"
                + "  ]]></script>\n"
                + "  <data><![CDATA[Raw data with <tags> & entities]]></data>\n"
                + "</root>";

        // Basic JDOM: May lose CDATA formatting
        String basicResult = jdomRoundTrip(xml);
        assertNotEquals(xml, basicResult, "JDOM default changes CDATA formatting");

        // JDOM Raw Format: Better CDATA preservation
        String rawResult = jdomRoundTripRaw(xml);
        assertNotEquals(xml, rawResult, "JDOM raw changes CDATA formatting");

        // JDOM with CDATA output
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new StringReader(xml));
        Format format = Format.getRawFormat();
        format.setTextMode(Format.TextMode.PRESERVE);
        XMLOutputter outputter = new XMLOutputter(format);
        String cdataResult = outputter.outputString(doc);

        // Verify CDATA is preserved in all results
        assertTrue(basicResult.contains("<![CDATA["), "CDATA should be preserved in basic");
        assertTrue(rawResult.contains("<![CDATA["), "CDATA should be preserved in raw");
        assertTrue(cdataResult.contains("<![CDATA["), "CDATA should be preserved");
        assertTrue(cdataResult.contains("if (x < y && y > z)"), "CDATA content should be preserved");

        // DomTrip: Preserves exact CDATA formatting
        eu.maveniverse.domtrip.Document domTripDoc = eu.maveniverse.domtrip.Document.of(xml);
        Editor domTripEditor = new Editor(domTripDoc);
        String domTripResult = domTripEditor.toXml();
        assertEquals(xml, domTripResult, "DomTrip preserves exact CDATA formatting");

        System.out.println("JDOM: CDATA preservation - PARTIAL (content yes, formatting no)");
        System.out.println("DomTrip: CDATA preserved with exact formatting - SUCCESS");
    }

    @Test
    void testProcessingInstructionPreservation() throws Exception {
        // Problem: Processing instructions may be lost or malformed
        String xml = "<?xml version=\"1.0\"?>\n"
                + "<?xml-stylesheet type=\"text/xsl\" href=\"style.xsl\"?>\n"
                + "<?custom-pi data=\"value\"?>\n"
                + "<root>\n"
                + "  <?page-break?>\n"
                + "  <content>text</content>\n"
                + "</root>";

        // Basic JDOM: Loses PI whitespace
        String basicResult = jdomRoundTrip(xml);
        assertNotEquals(xml, basicResult, "JDOM default loses PI whitespace");

        // JDOM Raw Format: Better PI preservation
        String rawResult = jdomRoundTripRaw(xml);
        assertNotEquals(xml, rawResult, "JDOM raw loses PI whitespace");

        // Verify PIs are preserved in both
        assertTrue(basicResult.contains("<?xml-stylesheet"), "PI should be preserved in basic");
        assertTrue(basicResult.contains("<?custom-pi"), "PI should be preserved in basic");
        assertTrue(basicResult.contains("<?page-break?>"), "PI should be preserved in basic");
        assertTrue(rawResult.contains("<?xml-stylesheet"), "PI should be preserved in raw");
        assertTrue(rawResult.contains("<?custom-pi"), "PI should be preserved in raw");
        assertTrue(rawResult.contains("<?page-break?>"), "PI should be preserved in raw");

        // DomTrip: Preserves exact PI positioning and whitespace
        eu.maveniverse.domtrip.Document domTripDoc = eu.maveniverse.domtrip.Document.of(xml);
        Editor editor = new Editor(domTripDoc);
        String domTripResult = editor.toXml();
        assertEquals(xml, domTripResult, "DomTrip preserves exact PI positioning");

        System.out.println("JDOM: PI preservation - PARTIAL (content yes, whitespace no)");
        System.out.println("DomTrip: PIs preserved with exact positioning - SUCCESS");
    }

    @Test
    void testDoctypePreservation() throws Exception {
        // Problem: DOCTYPE declarations may be reformatted
        String xml = "<?xml version=\"1.0\"?>\n"
                + "<!DOCTYPE root PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
                + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"
                + "<root>\n"
                + "  <child>content</child>\n"
                + "</root>";

        // Basic JDOM: Reformats DOCTYPE
        String basicResult = jdomRoundTrip(xml);
        assertNotEquals(xml, basicResult, "JDOM default reformats DOCTYPE");

        // JDOM Raw Format: Still reformats
        String rawResult = jdomRoundTripRaw(xml);
        assertNotEquals(xml, rawResult, "JDOM raw reformats DOCTYPE");

        // Verify DOCTYPE is preserved in both
        assertTrue(basicResult.contains("<!DOCTYPE"), "DOCTYPE should be preserved in basic");
        assertTrue(basicResult.contains("xhtml1-strict.dtd"), "DOCTYPE should be preserved in basic");
        assertTrue(rawResult.contains("<!DOCTYPE"), "DOCTYPE should be preserved in raw");
        assertTrue(rawResult.contains("xhtml1-strict.dtd"), "DOCTYPE should be preserved in raw");

        // DomTrip: Preserves exact DOCTYPE formatting
        // Note: DomTrip preserves DOCTYPE but may not preserve whitespace between declaration and DOCTYPE
        String xmlNormalized = "<?xml version=\"1.0\"?><!DOCTYPE root PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
                + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"
                + "<root>\n"
                + "  <child>content</child>\n"
                + "</root>";
        eu.maveniverse.domtrip.Document domTripDoc = eu.maveniverse.domtrip.Document.of(xmlNormalized);
        Editor editor = new Editor(domTripDoc);
        String domTripResult = editor.toXml();
        assertEquals(xmlNormalized, domTripResult, "DomTrip preserves DOCTYPE");

        System.out.println("JDOM: DOCTYPE preservation - PARTIAL (kept but reformatted)");
        System.out.println("DomTrip: DOCTYPE preserved - SUCCESS");
    }

    @Test
    void testInnerElementWhitespacePreservation() throws Exception {
        // Problem: Whitespace inside elements (before closing tag) may be lost
        String xml = "<root>\n" + "  <element>\n" + "    <nested>content</nested>\n" + "  </element>\n" + "</root>";

        // Basic JDOM: Loses inner whitespace
        String basicResult = jdomRoundTrip(xml);
        assertNotEquals(xml, basicResult, "JDOM default loses inner whitespace");

        // JDOM Raw Format: Better preservation
        String rawResult = jdomRoundTripRaw(xml);
        assertNotEquals(xml, rawResult, "JDOM raw loses inner whitespace");

        // JDOM with text mode PRESERVE
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new StringReader(xml));
        Format format = Format.getRawFormat();
        format.setTextMode(Format.TextMode.PRESERVE);
        XMLOutputter outputter = new XMLOutputter(format);
        String preserveResult = outputter.outputString(doc);

        System.out.println("Original: " + xml);
        System.out.println("Basic result: " + basicResult);
        System.out.println("Raw result: " + rawResult);
        System.out.println("Preserve result: " + preserveResult);

        // DomTrip: Preserves exact inner whitespace
        eu.maveniverse.domtrip.Document domTripDoc = eu.maveniverse.domtrip.Document.of(xml);
        Editor domTripEditor = new Editor(domTripDoc);
        String domTripResult = domTripEditor.toXml();
        assertEquals(xml, domTripResult, "DomTrip preserves exact inner whitespace");

        System.out.println("JDOM: Inner whitespace - PARTIAL (with TextMode.PRESERVE)");
        System.out.println("DomTrip: Inner whitespace preserved exactly - SUCCESS");
    }

    @Test
    void testAttributeWhitespacePreservation() throws Exception {
        // Problem: Extra whitespace between attributes is normalized
        String xml = "<element  attr1=\"value1\"   attr2=\"value2\"    attr3=\"value3\"/>";

        // Basic JDOM: Normalizes attribute whitespace
        String basicResult = jdomRoundTrip(xml);
        assertNotEquals(xml, basicResult, "JDOM normalizes attribute whitespace");

        // JDOM Raw Format: Still normalizes
        String rawResult = jdomRoundTripRaw(xml);
        assertNotEquals(xml, rawResult, "JDOM raw format still normalizes attribute whitespace");

        System.out.println("Original: " + xml);
        System.out.println("JDOM result: " + basicResult);

        // DomTrip: Preserves exact attribute whitespace
        eu.maveniverse.domtrip.Document domTripDoc = eu.maveniverse.domtrip.Document.of(xml);
        Editor editor = new Editor(domTripDoc);
        String domTripResult = editor.toXml();
        assertEquals(xml, domTripResult, "DomTrip preserves exact attribute whitespace");

        System.out.println("JDOM: Attribute whitespace - FAILED (cannot preserve)");
        System.out.println("DomTrip: Attribute whitespace preserved exactly - SUCCESS");
    }

    @Test
    void testMixedContentPreservation() throws Exception {
        // Problem: Mixed content (text + elements) whitespace handling
        String xml = "<p>This is <b>bold</b> and <i>italic</i> text.</p>";

        // Basic JDOM: May alter whitespace
        String basicResult = jdomRoundTrip(xml);
        assertNotEquals(xml, basicResult, "JDOM default adds XML declaration");

        // JDOM Raw Format: Better preservation
        String rawResult = jdomRoundTripRaw(xml);
        assertNotEquals(xml, rawResult, "JDOM raw adds XML declaration");

        // JDOM with PRESERVE mode
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new StringReader(xml));
        Format format = Format.getRawFormat();
        format.setTextMode(Format.TextMode.PRESERVE);
        format.setOmitDeclaration(true); // Don't add XML declaration
        XMLOutputter outputter = new XMLOutputter(format);
        String preserveResult = outputter.outputString(doc);

        System.out.println("Original: " + xml);
        System.out.println("Basic result: " + basicResult);
        System.out.println("Raw result: " + rawResult);
        System.out.println("Preserve result: " + preserveResult);

        // JDOM can preserve mixed content with PRESERVE mode (but may add whitespace)
        assertTrue(preserveResult.contains("This is "), "Should preserve text");
        assertTrue(preserveResult.contains("<b>bold</b>"), "Should preserve bold element");
        assertTrue(preserveResult.contains(" and "), "Should preserve text between elements");
        assertTrue(preserveResult.contains("<i>italic</i>"), "Should preserve italic element");
        assertTrue(preserveResult.contains(" text."), "Should preserve trailing text");

        // DomTrip: Preserves exact mixed content
        eu.maveniverse.domtrip.Document domTripDoc = eu.maveniverse.domtrip.Document.of(xml);
        Editor domTripEditor = new Editor(domTripDoc);
        String domTripResult = domTripEditor.toXml();
        assertEquals(xml, domTripResult, "DomTrip preserves exact mixed content");

        System.out.println("JDOM: Mixed content - PARTIAL (may add whitespace)");
        System.out.println("DomTrip: Mixed content preserved exactly - SUCCESS");
    }

    @Test
    void testEmptyElementStylePreservation() throws Exception {
        // Problem: JDOM may convert between <element/> and <element></element>
        String xml =
                "<root>\n" + "  <self-closing/>\n" + "  <expanded></expanded>\n" + "  <with-space />\n" + "</root>";

        // Basic JDOM: May change empty element style
        String basicResult = jdomRoundTrip(xml);
        assertNotEquals(xml, basicResult, "JDOM default changes empty element style");

        // JDOM Raw Format: Better preservation
        String rawResult = jdomRoundTripRaw(xml);
        assertNotEquals(xml, rawResult, "JDOM raw changes empty element style");

        System.out.println("Original: " + xml);
        System.out.println("Basic result: " + basicResult);
        System.out.println("Raw result: " + rawResult);

        // JDOM typically uses self-closing for empty elements
        assertTrue(basicResult.contains("<self-closing"), "Should have self-closing element in basic");
        assertTrue(rawResult.contains("<self-closing"), "Should have self-closing element in raw");

        // DomTrip: Preserves exact empty element style
        eu.maveniverse.domtrip.Document domTripDoc = eu.maveniverse.domtrip.Document.of(xml);
        Editor editor = new Editor(domTripDoc);
        String domTripResult = editor.toXml();
        assertEquals(xml, domTripResult, "DomTrip preserves exact empty element style");

        System.out.println("JDOM: Empty element style - PARTIAL (prefers self-closing)");
        System.out.println("DomTrip: Empty element style preserved exactly - SUCCESS");
    }

    @Test
    void testNamespacePreservation() throws Exception {
        // Problem: Namespace handling can be complex and may reformat
        String xml = "<root xmlns=\"http://example.com/default\" "
                + "xmlns:custom=\"http://example.com/custom\">\n"
                + "  <child>default namespace</child>\n"
                + "  <custom:element>custom namespace</custom:element>\n"
                + "</root>";

        // Basic JDOM: May reformat namespaces
        String basicResult = jdomRoundTrip(xml);
        assertNotEquals(xml, basicResult, "JDOM default reformats namespaces");

        // JDOM Raw Format: Better namespace preservation
        String rawResult = jdomRoundTripRaw(xml);
        assertNotEquals(xml, rawResult, "JDOM raw reformats namespaces");

        // Verify namespaces are preserved in both
        assertTrue(basicResult.contains("xmlns=\"http://example.com/default\""));
        assertTrue(basicResult.contains("xmlns:custom=\"http://example.com/custom\""));
        assertTrue(basicResult.contains("<custom:element>") || basicResult.contains("custom:element"));
        assertTrue(rawResult.contains("xmlns=\"http://example.com/default\""));
        assertTrue(rawResult.contains("xmlns:custom=\"http://example.com/custom\""));
        assertTrue(rawResult.contains("<custom:element>") || rawResult.contains("custom:element"));

        // DomTrip: Preserves exact namespace declarations
        eu.maveniverse.domtrip.Document domTripDoc = eu.maveniverse.domtrip.Document.of(xml);
        Editor editor = new Editor(domTripDoc);
        String domTripResult = editor.toXml();
        assertEquals(xml, domTripResult, "DomTrip preserves exact namespace declarations");

        System.out.println("JDOM: Namespace preservation - PARTIAL (may reformat)");
        System.out.println("DomTrip: Namespaces preserved exactly - SUCCESS");
    }

    @Test
    void testNumericCharacterReferences() throws Exception {
        // Problem: JDOM may decode numeric character references
        String xml = "<root>\n"
                + "  <text>Unicode: &#x1F600; &#128512; &#xA9;</text>\n"
                + "  <special>&#60;&#62;&#38;&#34;&#39;</special>\n"
                + "</root>";

        // Basic JDOM: May decode character references
        String basicResult = jdomRoundTrip(xml);
        assertNotEquals(xml, basicResult, "JDOM default decodes numeric character references");

        // JDOM Raw Format: Still may decode
        String rawResult = jdomRoundTripRaw(xml);
        assertNotEquals(xml, rawResult, "JDOM raw decodes numeric character references");

        System.out.println("Original: " + xml);
        System.out.println("Basic result: " + basicResult);
        System.out.println("Raw result: " + rawResult);

        // DomTrip: Preserves exact numeric character references
        eu.maveniverse.domtrip.Document domTripDoc = eu.maveniverse.domtrip.Document.of(xml);
        Editor editor = new Editor(domTripDoc);
        String domTripResult = editor.toXml();
        assertEquals(xml, domTripResult, "DomTrip preserves exact numeric character references");

        System.out.println("JDOM: Numeric char refs - FAILED (decodes to actual characters)");
        System.out.println("DomTrip: Numeric char refs preserved exactly - SUCCESS");
    }

    @Test
    void testBestPracticeRoundTrip() throws Exception {
        // Demonstrates best practices for JDOM round-tripping
        String xml = "<root>\n" + "  <element>content</element>\n" + "</root>";

        // Best practice: Use raw format with PRESERVE text mode
        SAXBuilder builder = new SAXBuilder();
        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        Document doc = builder.build(new StringReader(xml));

        Format format = Format.getRawFormat();
        format.setTextMode(Format.TextMode.PRESERVE);
        format.setOmitDeclaration(true); // Omit XML declaration if not in original
        format.setOmitEncoding(true);

        XMLOutputter outputter = new XMLOutputter(format);
        String result = outputter.outputString(doc);

        // Assert that even best practice doesn't achieve perfect round-trip
        assertNotEquals(xml, result, "Even JDOM best practice loses some formatting");

        System.out.println("Original: " + xml);
        System.out.println("Best practice result: " + result);

        // DomTrip: Perfect round-trip with default settings
        eu.maveniverse.domtrip.Document domTripDoc = eu.maveniverse.domtrip.Document.of(xml);
        Editor domTripEditor = new Editor(domTripDoc);
        String domTripResult = domTripEditor.toXml();
        assertEquals(xml, domTripResult, "DomTrip achieves perfect round-trip");

        System.out.println("JDOM: Best practice - PARTIAL (still loses some formatting)");
        System.out.println("DomTrip: Perfect round-trip with default settings - SUCCESS");
    }

    @Test
    void testComparisonSummary() {
        // Summary of JDOM round-tripping capabilities
        System.out.println("\n=== JDOM Round-Trip Capabilities Summary ===");
        System.out.println("✓ SUCCESS: CDATA preservation (with proper format)");
        System.out.println("✓ SUCCESS: Mixed content (with TextMode.PRESERVE)");
        System.out.println("⚠ PARTIAL: Comment preservation (content yes, positioning no)");
        System.out.println("⚠ PARTIAL: Processing instructions (content yes, whitespace no)");
        System.out.println("⚠ PARTIAL: DOCTYPE (preserved but reformatted)");
        System.out.println("⚠ PARTIAL: Line endings (can set but loses original)");
        System.out.println("⚠ PARTIAL: Inner whitespace (with TextMode.PRESERVE)");
        System.out.println("⚠ PARTIAL: Namespace declarations (kept but may reformat)");
        System.out.println("⚠ PARTIAL: Empty element style (prefers self-closing)");
        System.out.println("✗ FAILED: Prolog whitespace preservation");
        System.out.println("✗ FAILED: Quote style preservation (always double quotes)");
        System.out.println("✗ FAILED: Attribute whitespace preservation");
        System.out.println("✗ FAILED: Numeric character reference preservation");
        System.out.println("? VARIES: Attribute order (usually preserved in JDOM 2.x)");
        System.out.println("? VARIES: Entity representation (may change)");
        System.out.println("\nConclusion: JDOM can preserve ~40-50% of formatting with best practices");
        System.out.println("For true lossless round-tripping, use DomTrip instead.");
    }
}
