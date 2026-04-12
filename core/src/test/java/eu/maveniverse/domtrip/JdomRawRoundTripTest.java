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
 * Comparison tests demonstrating JDOM 2.0.6.1's round-tripping limitations
 * versus DomTrip's lossless round-trip preservation.
 *
 * <p>Each test gives JDOM its best chance by using {@code Format.getRawFormat()}
 * with {@code TextMode.PRESERVE}. Declaration omission is conditional: only applied
 * when the input has no XML declaration, to prevent JDOM from adding one.</p>
 *
 * <p>JDOM 2.0.6.1 is the latest and final release (Dec 2021). The project is
 * effectively in maintenance mode with no commits since Oct 2021.</p>
 */
@SuppressWarnings({"java:S5976", "java:S125"
}) // S5976: Tests are intentionally separate - each demonstrates a different XML feature; S125: XML entity content is
// not commented-out code
class JdomRawRoundTripTest {

    /**
     * Creates a SAXBuilder with external DTD/entity loading disabled
     * to prevent network I/O during tests.
     */
    private SAXBuilder safeSaxBuilder() {
        SAXBuilder builder = new SAXBuilder();
        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
        builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        return builder;
    }

    /**
     * Round-trips XML through JDOM using best-effort settings:
     * raw format + PRESERVE text mode. Omits declaration only when
     * input has none, to avoid JDOM adding one.
     */
    private String jdomBestEffort(String xml) throws Exception {
        SAXBuilder builder = safeSaxBuilder();
        Document doc = builder.build(new StringReader(xml));
        Format format = Format.getRawFormat();
        if (!xml.startsWith("<?xml")) {
            format.setOmitDeclaration(true);
        }
        format.setTextMode(Format.TextMode.PRESERVE);
        XMLOutputter outputter = new XMLOutputter(format);
        return outputter.outputString(doc);
    }

    /**
     * Round-trips XML through DomTrip.
     */
    private String domTripRoundTrip(String xml) {
        eu.maveniverse.domtrip.Document doc = eu.maveniverse.domtrip.Document.of(xml);
        Editor editor = new Editor(doc);
        return editor.toXml();
    }

    @Test
    void testPrologWhitespacePreservation() throws Exception {
        // Whitespace between prolog nodes (declaration, comments, PIs) and root element
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <!-- Header comment -->
                <?xml-stylesheet type="text/xsl" href="style.xsl"?>

                <root>
                  <child>content</child>
                </root>""";

        // JDOM loses whitespace between prolog nodes
        String jdomResult = jdomBestEffort(xml);
        assertNotEquals(xml, jdomResult, "JDOM loses prolog whitespace even with best-effort settings");

        // Verify JDOM at least preserves the prolog content
        assertTrue(jdomResult.contains("<!-- Header comment -->"), "JDOM preserves comment content");
        assertTrue(jdomResult.contains("<?xml-stylesheet"), "JDOM preserves PI content");

        // DomTrip preserves exact prolog whitespace
        assertEquals(xml, domTripRoundTrip(xml), "DomTrip preserves exact prolog whitespace");
    }

    @Test
    void testAttributeOrderPreservation() throws Exception {
        // The XML spec says attribute order is not significant, but in practice
        // both the SAX parser (Xerces) and JDOM's AttributeList preserve source order.
        // Both JDOM and DomTrip preserve attribute order for this case.
        String xml = """
                <element third="3" first="1" second="2" fourth="4"/>""";

        // JDOM preserves attribute order (but adds space before />)
        String jdomResult = jdomBestEffort(xml);
        assertTrue(
                jdomResult.indexOf("third=") < jdomResult.indexOf("first=")
                        && jdomResult.indexOf("first=") < jdomResult.indexOf("second=")
                        && jdomResult.indexOf("second=") < jdomResult.indexOf("fourth="),
                "JDOM preserves attribute order");

        // DomTrip preserves attribute order and exact self-closing style
        assertEquals(xml, domTripRoundTrip(xml), "DomTrip preserves attribute order");
    }

    @Test
    void testQuoteStylePreservation() throws Exception {
        // JDOM always normalizes to double quotes - fundamental SAX parser limitation
        String xml = """
                <config attr1='single' attr2="double" attr3='mixed'/>""";

        String jdomResult = jdomBestEffort(xml);
        // JDOM forces double quotes - no API to change this
        assertTrue(jdomResult.contains("attr1=\"single\""), "JDOM converts single to double quotes");
        assertTrue(jdomResult.contains("attr3=\"mixed\""), "JDOM converts single to double quotes");
        assertNotEquals(xml, jdomResult, "JDOM cannot preserve quote styles");

        // DomTrip preserves original quote styles
        assertEquals(xml, domTripRoundTrip(xml), "DomTrip preserves exact quote styles");
    }

    @Test
    void testEntityPreservation() throws Exception {
        // JDOM decodes entities through SAX and re-encodes on output,
        // potentially changing the representation (e.g., &apos; may become &#39;)
        String xml = """
                <root>
                  <text>Text with &lt;tags&gt; &amp; &quot;quotes&quot; &apos;apostrophes&apos;</text>
                  <attr value="&lt;value&gt; &amp; &quot;test&quot;"/>
                </root>""";

        String jdomResult = jdomBestEffort(xml);
        assertNotEquals(xml, jdomResult, "JDOM changes entity representation");

        // DomTrip preserves exact entity representation
        assertEquals(xml, domTripRoundTrip(xml), "DomTrip preserves exact entity representation");
    }

    @Test
    void testLineEndingPreservation() throws Exception {
        // XML spec requires parsers to normalize \r\n to \n (XML 1.0 Section 2.11)
        // This means CRLF line endings are irrecoverably lost during SAX parsing
        // Note: text blocks normalize line endings so we must use concatenation here
        String xml = "<?xml version=\"1.0\"?>\r\n<root>\r\n  <child>content</child>\r\n</root>";

        // JDOM can set output line separator but can't detect the original
        String jdomResult = jdomBestEffort(xml);
        assertNotEquals(xml, jdomResult, "JDOM normalizes CRLF to LF per XML spec");

        // Even with explicit CRLF line separator, JDOM can't match because
        // it also changes other formatting
        SAXBuilder builder = safeSaxBuilder();
        Document doc = builder.build(new StringReader(xml));
        Format format = Format.getRawFormat();
        format.setLineSeparator("\r\n");
        XMLOutputter outputter = new XMLOutputter(format);
        String crlfResult = outputter.outputString(doc);
        assertNotEquals(xml, crlfResult, "JDOM with CRLF separator still can't match original");

        // DomTrip preserves exact line endings
        String domTripResult = domTripRoundTrip(xml);
        assertEquals(xml, domTripResult, "DomTrip preserves exact line endings");
        assertTrue(domTripResult.contains("\r\n"), "DomTrip preserves CRLF");
    }

    @Test
    void testCommentPositioningPreservation() throws Exception {
        // JDOM preserves comment content but loses exact whitespace positioning
        String xml = """
                <!-- Document header -->
                <root>
                  <!-- Before first child -->
                  <child1>value1</child1>
                  <!-- Between children -->
                  <child2>value2</child2>
                  <!-- After last child -->
                </root>
                <!-- Document footer -->""";

        String jdomResult = jdomBestEffort(xml);

        // JDOM preserves comment content
        assertTrue(jdomResult.contains("<!-- Document header -->"), "Comment content preserved");
        assertTrue(jdomResult.contains("<!-- Between children -->"), "Comment content preserved");
        assertTrue(jdomResult.contains("<!-- Document footer -->"), "Comment content preserved");

        // But exact positioning/whitespace differs
        assertNotEquals(xml, jdomResult, "JDOM loses exact comment positioning");

        // DomTrip preserves exact comment positioning
        assertEquals(xml, domTripRoundTrip(xml), "DomTrip preserves exact comment positioning");
    }

    @Test
    void testCDataPreservation() throws Exception {
        // JDOM preserves CDATA sections but may alter surrounding whitespace
        String xml = """
                <root>
                  <script><![CDATA[
                    function test() {
                      if (x < y && y > z) {
                        return "<test>";
                      }
                    }
                  ]]></script>
                  <data><![CDATA[Raw data with <tags> & entities]]></data>
                </root>""";

        String jdomResult = jdomBestEffort(xml);

        // JDOM preserves CDATA markers and content
        assertTrue(jdomResult.contains("<![CDATA["), "CDATA markers preserved");
        assertTrue(jdomResult.contains("if (x < y && y > z)"), "CDATA content preserved");

        // But overall formatting differs
        assertNotEquals(xml, jdomResult, "JDOM changes CDATA formatting");

        // DomTrip preserves exact CDATA formatting
        assertEquals(xml, domTripRoundTrip(xml), "DomTrip preserves exact CDATA formatting");
    }

    @Test
    void testProcessingInstructionPreservation() throws Exception {
        // JDOM preserves PI content but loses exact whitespace
        String xml = """
                <?xml version="1.0"?>
                <?xml-stylesheet type="text/xsl" href="style.xsl"?>
                <?custom-pi data="value"?>
                <root>
                  <?page-break?>
                  <content>text</content>
                </root>""";

        String jdomResult = jdomBestEffort(xml);

        // JDOM preserves PI content
        assertTrue(jdomResult.contains("<?xml-stylesheet"), "PI preserved");
        assertTrue(jdomResult.contains("<?custom-pi"), "PI preserved");
        assertTrue(jdomResult.contains("<?page-break?>"), "PI preserved");

        // But exact positioning/whitespace differs
        assertNotEquals(xml, jdomResult, "JDOM loses PI whitespace positioning");

        // DomTrip preserves exact PI positioning
        assertEquals(xml, domTripRoundTrip(xml), "DomTrip preserves exact PI positioning");
    }

    @Test
    void testDoctypePreservation() throws Exception {
        // JDOM preserves DOCTYPE but reformats it
        String xml = """
                <?xml version="1.0"?>
                <!DOCTYPE root PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" \
                "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
                <root>
                  <child>content</child>
                </root>""";

        String jdomResult = jdomBestEffort(xml);

        // JDOM preserves DOCTYPE content
        assertTrue(jdomResult.contains("<!DOCTYPE"), "DOCTYPE preserved");
        assertTrue(jdomResult.contains("xhtml1-strict.dtd"), "DOCTYPE system ID preserved");

        // But reformats it
        assertNotEquals(xml, jdomResult, "JDOM reformats DOCTYPE");

        // DomTrip preserves exact DOCTYPE formatting
        assertEquals(xml, domTripRoundTrip(xml), "DomTrip preserves exact DOCTYPE formatting");
    }

    @Test
    void testInnerElementWhitespacePreservation() throws Exception {
        // Whitespace between child elements and before closing tags
        String xml = """
                <root>
                  <element>
                    <nested>content</nested>
                  </element>
                </root>""";

        String jdomResult = jdomBestEffort(xml);

        // JDOM with PRESERVE mode keeps text nodes but still may differ
        assertNotEquals(xml, jdomResult, "JDOM loses inner element whitespace");

        // DomTrip preserves exact inner whitespace
        assertEquals(xml, domTripRoundTrip(xml), "DomTrip preserves exact inner whitespace");
    }

    @Test
    void testAttributeWhitespacePreservation() throws Exception {
        // Extra whitespace between attributes - lost during SAX parsing
        // JDOM GitHub issue #153 (closed as won't fix)
        String xml = """
                <element  attr1="value1"   attr2="value2"    attr3="value3"/>""";

        String jdomResult = jdomBestEffort(xml);
        assertNotEquals(xml, jdomResult, "JDOM normalizes inter-attribute whitespace");

        // DomTrip preserves exact attribute whitespace
        assertEquals(xml, domTripRoundTrip(xml), "DomTrip preserves exact attribute whitespace");
    }

    @Test
    void testMixedContentPreservation() throws Exception {
        // Mixed content: text interleaved with elements
        // JDOM preserves the structure but always appends a trailing newline
        String xml = """
                <p>This is <b>bold</b> and <i>italic</i> text.</p>""";

        String jdomResult = jdomBestEffort(xml);
        assertEquals(xml, jdomResult.stripTrailing(), "JDOM preserves mixed content structure");
        assertNotEquals(xml, jdomResult, "JDOM adds trailing newline");

        assertEquals(xml, domTripRoundTrip(xml), "DomTrip preserves exact mixed content");
    }

    @Test
    void testEmptyElementStylePreservation() throws Exception {
        // JDOM has setExpandEmptyElements but cannot distinguish per-element
        // and cannot preserve the space before /> in <with-space />
        String xml = """
                <root>
                  <self-closing/>
                  <expanded></expanded>
                  <with-space />
                </root>""";

        // Default: JDOM prefers self-closing
        String jdomResult = jdomBestEffort(xml);
        assertNotEquals(xml, jdomResult, "JDOM default converts expanded to self-closing");

        // With expandEmptyElements: converts all to expanded form
        SAXBuilder builder = safeSaxBuilder();
        Document doc = builder.build(new StringReader(xml));
        Format format = Format.getRawFormat();
        format.setOmitDeclaration(true);
        format.setExpandEmptyElements(true);
        XMLOutputter outputter = new XMLOutputter(format);
        String expandedResult = outputter.outputString(doc);
        // Still can't match because it's all-or-nothing and loses <with-space />
        assertNotEquals(xml, expandedResult, "JDOM expandEmptyElements is all-or-nothing");

        // DomTrip preserves exact empty element style per-element
        assertEquals(xml, domTripRoundTrip(xml), "DomTrip preserves exact empty element style");
    }

    @Test
    void testNamespaceDeclarationPreservation() throws Exception {
        // Namespace declarations are attributes to the parser but JDOM handles them specially
        String xml = """
                <root xmlns="http://example.com/default" \
                xmlns:custom="http://example.com/custom">
                  <child>default namespace</child>
                  <custom:element>custom namespace</custom:element>
                </root>""";

        String jdomResult = jdomBestEffort(xml);

        // JDOM preserves namespace URIs and prefixes
        assertTrue(jdomResult.contains("xmlns=\"http://example.com/default\""), "Default namespace preserved");
        assertTrue(jdomResult.contains("xmlns:custom=\"http://example.com/custom\""), "Prefixed namespace preserved");

        // But may reformat declaration positioning
        assertNotEquals(xml, jdomResult, "JDOM reformats namespace declarations");

        // DomTrip preserves exact namespace declarations
        assertEquals(xml, domTripRoundTrip(xml), "DomTrip preserves exact namespace declarations");
    }

    @Test
    void testNumericCharacterReferencePreservation() throws Exception {
        // SAX parsers decode numeric character references to actual characters;
        // the original reference form (hex vs decimal) is irrecoverably lost
        String xml = """
                <root>
                  <text>Unicode: &#x1F600; &#128512; &#xA9;</text>
                  <special>&#60;&#62;&#38;&#34;&#39;</special>
                </root>""";

        String jdomResult = jdomBestEffort(xml);
        assertNotEquals(xml, jdomResult, "JDOM decodes numeric character references");

        // DomTrip preserves exact numeric character references
        assertEquals(xml, domTripRoundTrip(xml), "DomTrip preserves exact numeric character references");
    }

    @Test
    void testBestPracticeRoundTrip() throws Exception {
        // Even with every JDOM optimization enabled, complex XML cannot round-trip perfectly
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <!-- Configuration file -->
                <project xmlns='http://maven.apache.org/POM/4.0.0'
                         xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>
                  <modelVersion>4.0.0</modelVersion>
                  <!-- GAV coordinates -->
                  <groupId>com.example</groupId>
                  <artifactId>demo</artifactId>
                  <version>1.0-SNAPSHOT</version>
                  <properties>
                    <java.version>17</java.version>
                  </properties>
                </project>
                """;

        // JDOM with all best-effort settings
        SAXBuilder builder = safeSaxBuilder();
        Document doc = builder.build(new StringReader(xml));

        Format format = Format.getRawFormat();
        format.setTextMode(Format.TextMode.PRESERVE);
        format.setOmitEncoding(true);
        XMLOutputter outputter = new XMLOutputter(format);
        String jdomResult = outputter.outputString(doc);

        // JDOM fails on: single quotes, attribute whitespace/newlines, namespace formatting
        assertNotEquals(xml, jdomResult, "JDOM best practice still loses formatting");

        // DomTrip achieves perfect round-trip
        assertEquals(xml, domTripRoundTrip(xml), "DomTrip achieves perfect round-trip");
    }
}
