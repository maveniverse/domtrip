package eu.maveniverse.domtrip.demos;

import eu.maveniverse.domtrip.Attribute;
import eu.maveniverse.domtrip.DomTripConfig;
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import eu.maveniverse.domtrip.QuoteStyle;

/**
 * Demonstrates configuration options and serialization features.
 */
public class ConfigurationDemo {

    public static void main(String[] args) {
        System.out.println("=== Configuration and Serialization Demo ===\n");

        String sampleXml = createSampleXml();

        demonstrateConfigurations(sampleXml);
        demonstrateSerializationOptions(sampleXml);
        demonstrateQuoteStylePreservation();
        demonstrateWhitespaceManagement();

        System.out.println("\n=== Demo Complete ===");
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

    private static void demonstrateConfigurations(String xml) {
        System.out.println("1. Configuration Options Demo:");

        // Default configuration
        Editor defaultEditor = new Editor(xml, DomTripConfig.defaults());
        System.out.println("Default configuration preserves everything:");
        System.out.println("- Preserves whitespace: " + defaultEditor.config().isPreserveWhitespace());
        System.out.println("- Preserves comments: " + defaultEditor.config().isPreserveComments());
        System.out.println("- Preserves entities: " + defaultEditor.config().isPreserveEntities());
        System.out.println("- Default quote style: " + defaultEditor.config().defaultQuoteStyle());

        // Strict configuration
        Editor strictEditor = new Editor(xml, DomTripConfig.strict());
        System.out.println("\nStrict configuration enables validation:");
        System.out.println("- Strict parsing: " + strictEditor.config().isStrictParsing());
        System.out.println("- Validate XML names: " + strictEditor.config().isValidateXmlNames());

        // Lenient configuration
        Editor lenientEditor = new Editor(xml, DomTripConfig.lenient());
        System.out.println("\nLenient configuration disables validation:");
        System.out.println("- Strict parsing: " + lenientEditor.config().isStrictParsing());
        System.out.println("- Validate XML names: " + lenientEditor.config().isValidateXmlNames());

        // Custom configuration
        DomTripConfig customConfig = DomTripConfig.defaults()
                .withDefaultQuoteStyle(QuoteStyle.SINGLE)
                .withDefaultEncoding("ISO-8859-1")
                .withIndentString("\t")
                .withPrettyPrint(true);

        Editor customEditor = new Editor(xml, customConfig);
        System.out.println("\nCustom configuration:");
        System.out.println("- Default quote style: " + customEditor.config().defaultQuoteStyle());
        System.out.println("- Default encoding: " + customEditor.config().defaultEncoding());
        System.out.println(
                "- Indent string: '" + customEditor.config().indentString().replace("\t", "\\t") + "'");
        System.out.println("- Pretty print: " + customEditor.config().isPrettyPrint());

        System.out.println();
    }

    private static void demonstrateSerializationOptions(String xml) {
        System.out.println("2. Serialization Options Demo:");

        Editor editor = new Editor(xml);

        System.out.println("Original XML (first 200 chars):");
        System.out.println(xml.substring(0, Math.min(200, xml.length())) + "...\n");

        // Default serialization
        System.out.println("Default serialization (preserves all):");
        String defaultOutput = editor.toXml(DomTripConfig.defaults());
        System.out.println("Length: " + defaultOutput.length() + " characters");
        System.out.println("Contains XML declaration: " + defaultOutput.contains("<?xml"));
        System.out.println("Contains comments: " + defaultOutput.contains("<!--"));
        System.out.println("Contains processing instructions: " + defaultOutput.contains("<?xml-stylesheet"));
        System.out.println("Contains CDATA: " + defaultOutput.contains("<![CDATA["));

        // Pretty print serialization
        System.out.println("\nPretty print serialization:");
        String prettyOutput = editor.toXml(DomTripConfig.prettyPrint());
        System.out.println("Length: " + prettyOutput.length() + " characters");
        System.out.println("First few lines:");
        String[] lines = prettyOutput.split("\n");
        for (int i = 0; i < Math.min(5, lines.length); i++) {
            System.out.println("  " + lines[i]);
        }

        // Minimal serialization
        System.out.println("\nMinimal serialization:");
        String minimalOutput = editor.toXml(DomTripConfig.minimal());
        System.out.println("Length: " + minimalOutput.length() + " characters");
        System.out.println("Contains XML declaration: " + minimalOutput.contains("<?xml"));
        System.out.println("Contains comments: " + minimalOutput.contains("<!--"));
        System.out.println("Output: " + minimalOutput.substring(0, Math.min(150, minimalOutput.length())) + "...");

        // Custom serialization options
        System.out.println("\nCustom serialization (tab indents, no comments):");
        DomTripConfig customConfig = DomTripConfig.defaults()
                .withPrettyPrint(true)
                .withIndentString("\t")
                .withCommentPreservation(false)
                .withLineEnding("\r\n");

        String customOutput = editor.toXml(customConfig);
        System.out.println("Length: " + customOutput.length() + " characters");
        System.out.println("Contains comments: " + customOutput.contains("<!--"));
        System.out.println("Uses tab indents: " + customOutput.contains("\t"));

        System.out.println();
    }

    private static void demonstrateQuoteStylePreservation() {
        System.out.println("3. Quote Style Preservation Demo:");

        // Create XML with mixed quote styles
        String mixedQuotesXml = "<root attr1=\"double\" attr2='single' attr3=\"mixed'quotes\"/>";
        Editor editor = new Editor(mixedQuotesXml);

        System.out.println("Original: " + mixedQuotesXml);
        System.out.println("Round-trip: " + editor.toXml());
        System.out.println("Quotes preserved: " + mixedQuotesXml.equals(editor.toXml()));

        // Demonstrate attribute builder with different quote styles
        Element root = editor.documentElement().orElseThrow();

        Attribute doubleQuoted = Attribute.builder()
                .name("new-double")
                .value("value with 'single' quotes")
                .quoteStyle(QuoteStyle.DOUBLE)
                .build();

        Attribute singleQuoted = Attribute.builder()
                .name("new-single")
                .value("value with \"double\" quotes")
                .quoteStyle(QuoteStyle.SINGLE)
                .build();

        root.attributeObject("new-double", doubleQuoted);
        root.attributeObject("new-single", singleQuoted);

        System.out.println("After adding new attributes: " + editor.toXml());

        // Demonstrate immutable quote style changes
        Attribute original = root.attributeObject("attr1");
        Attribute modified = original.withQuoteStyle(QuoteStyle.SINGLE);

        System.out.println("Original attribute: " + original);
        System.out.println("Modified attribute: " + modified);
        System.out.println("Original unchanged: " + (original.quoteStyle() == QuoteStyle.DOUBLE));

        System.out.println();
    }

    private static void demonstrateWhitespaceManagement() {
        System.out.println("4. Whitespace Management Demo:");

        // Note: WhitespaceManager is now package-protected and used internally by Editor
        // This demo shows the effects of whitespace configuration through Editor

        // Demonstrate whitespace handling through Editor configuration
        String xml = "<root>\n  <child>content</child>\n</root>";

        System.out.println("Original XML:");
        System.out.println(xml);

        try {
            // Test with whitespace preservation
            DomTripConfig preserveConfig = DomTripConfig.defaults().withWhitespacePreservation(true);
            Editor preserveEditor = new Editor(xml, preserveConfig);
            System.out.println("\nWith whitespace preservation:");
            System.out.println(preserveEditor.toXml());

            // Test with different indentation
            DomTripConfig tabConfig = DomTripConfig.defaults().withIndentString("\t");
            Editor tabEditor = new Editor(xml, tabConfig);
            System.out.println("\nWith tab indentation config:");
            System.out.println(tabEditor.toXml());

            // Test pretty printing
            DomTripConfig prettyConfig = DomTripConfig.prettyPrint().withIndentString("  ");
            Editor prettyEditor = new Editor(xml, prettyConfig);
            System.out.println("\nWith pretty printing:");
            System.out.println(prettyEditor.toXml());

            // Demonstrate adding elements with proper indentation
            Element root = preserveEditor.documentElement().orElseThrow();
            preserveEditor.addElement(root, "newChild", "new content");
            System.out.println("\nAfter adding element (indentation preserved):");
            System.out.println(preserveEditor.toXml());
        } catch (Exception e) {
            System.out.println("Error in whitespace demo: " + e.getMessage());
        }

        System.out.println();
    }
}
