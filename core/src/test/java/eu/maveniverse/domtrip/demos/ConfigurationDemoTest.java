package eu.maveniverse.domtrip.demos;

import static org.junit.jupiter.api.Assertions.*;

import eu.maveniverse.domtrip.Attribute;
import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripConfig;
import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import eu.maveniverse.domtrip.QuoteStyle;
import org.junit.jupiter.api.Test;

/**
 * Demonstrates configuration options and serialization features.
 */
class ConfigurationDemoTest {

    @Test
    void demonstrateConfigurationAndSerialization() throws DomTripException {
        String sampleXml = createSampleXml();

        verifyConfigurations(sampleXml);
        verifySerializationOptions(sampleXml);
        verifyQuoteStylePreservation();
        verifyWhitespaceManagement();
    }

    private static String createSampleXml() {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <!-- Sample configuration file -->
            <?xml-stylesheet type="text/xsl" href="config.xsl"?>
            <configuration xmlns="http://example.com/config">
                <database type="postgresql" host='localhost' port="5432">
                    <connection-pool>
                        <min-size>5</min-size>
                        <max-size>20</max-size>
                        <timeout>30</timeout>
                    </connection-pool>
                    <credentials>
                        <username>admin</username>
                        <password><![CDATA[p@ssw0rd!]]></password>
                    </credentials>
                </database>

                <logging level="INFO">
                    <appenders>
                        <console enabled="true"/>
                        <file path="/var/log/app.log" max-size="10MB"/>
                    </appenders>
                </logging>

                <features>
                    <feature name="caching" enabled="true"/>
                    <feature name="metrics" enabled="false"/>
                </features>
            </configuration>
            """;
    }

    private static void verifyConfigurations(String xml) throws DomTripException {
        // Default configuration
        Editor defaultEditor = new Editor(Document.of(xml), DomTripConfig.defaults());
        assertTrue(defaultEditor.config().isPreserveComments());
        assertTrue(defaultEditor.config().isPreserveProcessingInstructions());
        assertNotNull(defaultEditor.config().defaultQuoteStyle());

        // Custom configuration
        DomTripConfig customConfig = DomTripConfig.defaults()
                .withDefaultQuoteStyle(QuoteStyle.SINGLE)
                .withIndentString("\t")
                .withPrettyPrint(true);

        Editor customEditor = new Editor(Document.of(xml), customConfig);
        assertEquals(QuoteStyle.SINGLE, customEditor.config().defaultQuoteStyle());
        assertEquals("\t", customEditor.config().indentString());
        assertTrue(customEditor.config().isPrettyPrint());
    }

    private static void verifySerializationOptions(String xml) throws DomTripException {
        Editor editor = new Editor(Document.of(xml));

        // Default serialization
        String defaultOutput = editor.toXml(DomTripConfig.defaults());
        assertTrue(defaultOutput.contains("<?xml"));
        assertTrue(defaultOutput.contains("<!--"));
        assertTrue(defaultOutput.contains("<?xml-stylesheet"));
        assertTrue(defaultOutput.contains("<![CDATA["));

        // Pretty print serialization
        String prettyOutput = editor.toXml(DomTripConfig.prettyPrint());
        assertNotNull(prettyOutput);
        assertFalse(prettyOutput.isEmpty());

        // Minimal serialization
        String minimalOutput = editor.toXml(DomTripConfig.minimal());
        assertFalse(minimalOutput.contains("<!--"));

        // Custom serialization options
        DomTripConfig customConfig = DomTripConfig.defaults()
                .withPrettyPrint(true)
                .withIndentString("\t")
                .withCommentPreservation(false)
                .withLineEnding("\r\n");

        String customOutput = editor.toXml(customConfig);
        assertFalse(customOutput.contains("<!--"));
        assertTrue(customOutput.contains("\t"));
    }

    private static void verifyQuoteStylePreservation() throws DomTripException {
        // Create XML with mixed quote styles
        String mixedQuotesXml = "<root attr1=\"double\" attr2='single' attr3=\"mixed'quotes\"/>";
        Editor editor = new Editor(Document.of(mixedQuotesXml));

        assertEquals(mixedQuotesXml, editor.toXml(), "Quotes should be preserved during round-trip");

        // Demonstrate attribute builder with different quote styles
        Element root = editor.root();

        Attribute doubleQuoted = Attribute.of("new-double", "value with 'single' quotes", QuoteStyle.DOUBLE);
        Attribute singleQuoted = Attribute.of("new-single", "value with \"double\" quotes", QuoteStyle.SINGLE);

        root.attributeObject("new-double", doubleQuoted);
        root.attributeObject("new-single", singleQuoted);

        String result = editor.toXml();
        assertNotNull(result);
        assertTrue(result.contains("new-double"));
        assertTrue(result.contains("new-single"));

        // Demonstrate immutable quote style changes
        Attribute original = root.attributeObject("attr1");
        Attribute modified = original.withQuoteStyle(QuoteStyle.SINGLE);

        assertEquals(QuoteStyle.DOUBLE, original.quoteStyle());
        assertEquals(QuoteStyle.SINGLE, modified.quoteStyle());
    }

    private static void verifyWhitespaceManagement() {
        String xml = "<root>\n  <child>content</child>\n</root>";

        // Test with default configuration
        DomTripConfig preserveConfig = DomTripConfig.defaults();
        Editor preserveEditor = new Editor(Document.of(xml), preserveConfig);
        assertEquals(xml, preserveEditor.toXml(), "Default configuration should preserve whitespace");

        // Test with different indentation
        DomTripConfig tabConfig = DomTripConfig.defaults().withIndentString("\t");
        Editor tabEditor = new Editor(Document.of(xml), tabConfig);
        assertNotNull(tabEditor.toXml());

        // Test pretty printing
        DomTripConfig prettyConfig = DomTripConfig.prettyPrint().withIndentString("  ");
        Editor prettyEditor = new Editor(Document.of(xml), prettyConfig);
        assertNotNull(prettyEditor.toXml());

        // Demonstrate adding elements with proper indentation
        Element root = preserveEditor.root();
        preserveEditor.addElement(root, "newChild", "new content");
        String result = preserveEditor.toXml();
        assertTrue(result.contains("newChild"));
        assertTrue(result.contains("new content"));
    }
}
