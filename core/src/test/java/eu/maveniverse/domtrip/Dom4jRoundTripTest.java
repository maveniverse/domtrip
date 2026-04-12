package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.junit.jupiter.api.Test;

/**
 * Comparison tests demonstrating DOM4J 2.1.4's round-tripping limitations
 * versus DomTrip's lossless round-trip preservation.
 *
 * <p>Each test gives DOM4J its best chance by using a raw {@link OutputFormat}
 * with no trimming/indenting. Declaration suppression is conditional: only applied
 * when the input has no XML declaration, to prevent DOM4J from adding one.</p>
 */
@SuppressWarnings("java:S5976") // Tests are intentionally separate - each demonstrates a different XML feature
class Dom4jRoundTripTest {

    /**
     * Creates a SAXReader with external DTD/entity loading disabled
     * to prevent network I/O during tests.
     */
    private SAXReader safeSaxReader() throws Exception {
        SAXReader reader = new SAXReader();
        reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
        reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        return reader;
    }

    /**
     * Round-trips XML through DOM4J using best-effort settings:
     * no newlines, no trimming. Suppresses declaration only when
     * input has none, to avoid DOM4J adding one.
     */
    private String dom4jBestEffort(String xml) throws Exception {
        SAXReader reader = safeSaxReader();
        Document doc = reader.read(new StringReader(xml));
        OutputFormat format = OutputFormat.createCompactFormat();
        if (!xml.startsWith("<?xml")) {
            format.setSuppressDeclaration(true);
        }
        format.setNewlines(false);
        format.setIndent(false);
        format.setTrimText(false);
        format.setPadText(false);
        StringWriter writer = new StringWriter();
        XMLWriter xmlWriter = new XMLWriter(writer, format);
        xmlWriter.write(doc);
        return writer.toString();
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

        String dom4jResult = dom4jBestEffort(xml);
        assertNotEquals(xml, dom4jResult, "DOM4J loses prolog whitespace");

        // Verify DOM4J at least preserves the prolog content
        assertTrue(dom4jResult.contains("<!-- Header comment -->"), "DOM4J preserves comment content");
        assertTrue(dom4jResult.contains("<?xml-stylesheet"), "DOM4J preserves PI content");

        // DomTrip preserves exact prolog whitespace
        assertEquals(xml, domTripRoundTrip(xml), "DomTrip preserves exact prolog whitespace");
    }

    @Test
    void testAttributeOrderPreservation() throws Exception {
        // DOM4J stores attributes in a List, preserving SAX document order
        String xml = """
                <element third="3" first="1" second="2" fourth="4"/>""";

        String dom4jResult = dom4jBestEffort(xml);
        assertTrue(
                dom4jResult.indexOf("third=") < dom4jResult.indexOf("first=")
                        && dom4jResult.indexOf("first=") < dom4jResult.indexOf("second=")
                        && dom4jResult.indexOf("second=") < dom4jResult.indexOf("fourth="),
                "DOM4J preserves attribute order");

        assertEquals(xml, domTripRoundTrip(xml), "DomTrip preserves attribute order");
    }

    @Test
    void testQuoteStylePreservation() throws Exception {
        // DOM4J always normalizes to double quotes - fundamental SAX parser limitation
        String xml = """
                <config attr1='single' attr2="double" attr3='mixed'/>""";

        String dom4jResult = dom4jBestEffort(xml);
        assertTrue(dom4jResult.contains("attr1=\"single\""), "DOM4J converts single to double quotes");
        assertTrue(dom4jResult.contains("attr3=\"mixed\""), "DOM4J converts single to double quotes");
        assertNotEquals(xml, dom4jResult, "DOM4J cannot preserve quote styles");

        assertEquals(xml, domTripRoundTrip(xml), "DomTrip preserves exact quote styles");
    }

    @Test
    void testEntityPreservation() throws Exception {
        // DOM4J decodes entities through SAX and re-encodes on output
        String xml = """
                <root>
                  <text>Text with &lt;tags&gt; &amp; &quot;quotes&quot; &apos;apostrophes&apos;</text>
                  <attr value="&lt;value&gt; &amp; &quot;test&quot;"/>
                </root>""";

        String dom4jResult = dom4jBestEffort(xml);
        assertNotEquals(xml, dom4jResult, "DOM4J changes entity representation");

        assertEquals(xml, domTripRoundTrip(xml), "DomTrip preserves exact entity representation");
    }

    @Test
    void testLineEndingPreservation() throws Exception {
        // XML spec requires parsers to normalize \r\n to \n (XML 1.0 Section 2.11)
        // Note: text blocks normalize line endings so we must use concatenation here
        String xml = "<?xml version=\"1.0\"?>\r\n<root>\r\n  <child>content</child>\r\n</root>";

        String dom4jResult = dom4jBestEffort(xml);
        assertNotEquals(xml, dom4jResult, "DOM4J normalizes CRLF to LF per XML spec");

        String domTripResult = domTripRoundTrip(xml);
        assertEquals(xml, domTripResult, "DomTrip preserves exact line endings");
        assertTrue(domTripResult.contains("\r\n"), "DomTrip preserves CRLF");
    }

    @Test
    void testCommentPositioningPreservation() throws Exception {
        // DOM4J preserves comment content but loses exact whitespace positioning
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

        String dom4jResult = dom4jBestEffort(xml);

        assertTrue(dom4jResult.contains("<!-- Document header -->"), "Comment content preserved");
        assertTrue(dom4jResult.contains("<!-- Between children -->"), "Comment content preserved");
        assertTrue(dom4jResult.contains("<!-- Document footer -->"), "Comment content preserved");

        assertNotEquals(xml, dom4jResult, "DOM4J loses exact comment positioning");

        assertEquals(xml, domTripRoundTrip(xml), "DomTrip preserves exact comment positioning");
    }

    @Test
    void testCDataPreservation() throws Exception {
        // DOM4J preserves CDATA sections but may alter surrounding whitespace
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

        String dom4jResult = dom4jBestEffort(xml);

        // DOM4J preserves CDATA sections with best-effort settings
        assertTrue(dom4jResult.contains("<![CDATA["), "CDATA markers preserved");
        assertTrue(dom4jResult.contains("if (x < y && y > z)"), "CDATA content preserved");

        assertEquals(xml, domTripRoundTrip(xml), "DomTrip preserves exact CDATA formatting");
    }

    @Test
    void testProcessingInstructionPreservation() throws Exception {
        // DOM4J preserves PI content but loses exact whitespace
        String xml = """
                <?xml version="1.0"?>
                <?xml-stylesheet type="text/xsl" href="style.xsl"?>
                <?custom-pi data="value"?>
                <root>
                  <?page-break?>
                  <content>text</content>
                </root>""";

        String dom4jResult = dom4jBestEffort(xml);

        assertTrue(dom4jResult.contains("<?xml-stylesheet"), "PI preserved");
        assertTrue(dom4jResult.contains("<?custom-pi"), "PI preserved");
        // DOM4J adds a space before ?> in PIs without data: <?page-break?>  becomes <?page-break ?>
        assertTrue(dom4jResult.contains("<?page-break"), "PI preserved");

        assertNotEquals(xml, dom4jResult, "DOM4J loses PI whitespace positioning");

        assertEquals(xml, domTripRoundTrip(xml), "DomTrip preserves exact PI positioning");
    }

    @Test
    void testDoctypePreservation() throws Exception {
        // DOM4J preserves DOCTYPE but reformats it
        String xml = """
                <?xml version="1.0"?>
                <!DOCTYPE root PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" \
                "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
                <root>
                  <child>content</child>
                </root>""";

        String dom4jResult = dom4jBestEffort(xml);

        assertTrue(dom4jResult.contains("<!DOCTYPE"), "DOCTYPE preserved");
        assertTrue(dom4jResult.contains("xhtml1-strict.dtd"), "DOCTYPE system ID preserved");

        assertNotEquals(xml, dom4jResult, "DOM4J reformats DOCTYPE");

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

        // DOM4J preserves inner element whitespace with best-effort settings
        String dom4jResult = dom4jBestEffort(xml);
        assertEquals(xml, dom4jResult.stripTrailing(), "DOM4J preserves inner element whitespace");

        assertEquals(xml, domTripRoundTrip(xml), "DomTrip preserves exact inner whitespace");
    }

    @Test
    void testAttributeWhitespacePreservation() throws Exception {
        // Extra whitespace between attributes - lost during SAX parsing
        String xml = """
                <element  attr1="value1"   attr2="value2"    attr3="value3"/>""";

        String dom4jResult = dom4jBestEffort(xml);
        assertNotEquals(xml, dom4jResult, "DOM4J normalizes inter-attribute whitespace");

        assertEquals(xml, domTripRoundTrip(xml), "DomTrip preserves exact attribute whitespace");
    }

    @Test
    void testMixedContentPreservation() throws Exception {
        // Mixed content: text interleaved with elements
        String xml = """
                <p>This is <b>bold</b> and <i>italic</i> text.</p>""";

        String dom4jResult = dom4jBestEffort(xml);
        assertEquals(xml, dom4jResult.stripTrailing(), "DOM4J preserves mixed content structure");

        assertEquals(xml, domTripRoundTrip(xml), "DomTrip preserves exact mixed content");
    }

    @Test
    void testEmptyElementStylePreservation() throws Exception {
        // DOM4J has setExpandEmptyElements but it's all-or-nothing
        String xml = """
                <root>
                  <self-closing/>
                  <expanded></expanded>
                  <with-space />
                </root>""";

        String dom4jResult = dom4jBestEffort(xml);
        assertNotEquals(xml, dom4jResult, "DOM4J changes empty element style");

        assertEquals(xml, domTripRoundTrip(xml), "DomTrip preserves exact empty element style");
    }

    @Test
    void testNamespaceDeclarationPreservation() throws Exception {
        // Namespace declarations are handled specially by DOM4J
        String xml = """
                <root xmlns="http://example.com/default" \
                xmlns:custom="http://example.com/custom">
                  <child>default namespace</child>
                  <custom:element>custom namespace</custom:element>
                </root>""";

        String dom4jResult = dom4jBestEffort(xml);

        // DOM4J preserves namespace declarations with best-effort settings
        assertTrue(dom4jResult.contains("xmlns=\"http://example.com/default\""), "Default namespace preserved");
        assertTrue(dom4jResult.contains("xmlns:custom=\"http://example.com/custom\""), "Prefixed namespace preserved");
        assertEquals(xml, dom4jResult.stripTrailing(), "DOM4J preserves namespace declarations");

        assertEquals(xml, domTripRoundTrip(xml), "DomTrip preserves exact namespace declarations");
    }

    @Test
    void testNumericCharacterReferencePreservation() throws Exception {
        // SAX parsers decode numeric character references to actual characters
        String xml = """
                <root>
                  <text>Unicode: &#x1F600; &#128512; &#xA9;</text>
                  <special>&#60;&#62;&#38;&#34;&#39;</special>
                </root>""";

        String dom4jResult = dom4jBestEffort(xml);
        assertNotEquals(xml, dom4jResult, "DOM4J decodes numeric character references");

        assertEquals(xml, domTripRoundTrip(xml), "DomTrip preserves exact numeric character references");
    }

    @Test
    void testBestPracticeRoundTrip() throws Exception {
        // Even with every DOM4J optimization, complex XML cannot round-trip perfectly
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

        // DOM4J with best-effort settings
        SAXReader reader = safeSaxReader();
        Document doc = reader.read(new StringReader(xml));
        OutputFormat format = OutputFormat.createCompactFormat();
        format.setNewlines(false);
        format.setIndent(false);
        format.setTrimText(false);
        format.setPadText(false);
        StringWriter writer = new StringWriter();
        XMLWriter xmlWriter = new XMLWriter(writer, format);
        xmlWriter.write(doc);
        String dom4jResult = writer.toString();

        // DOM4J fails on: single quotes, attribute whitespace/newlines, namespace formatting
        assertNotEquals(xml, dom4jResult, "DOM4J best practice still loses formatting");

        assertEquals(xml, domTripRoundTrip(xml), "DomTrip achieves perfect round-trip");
    }
}
