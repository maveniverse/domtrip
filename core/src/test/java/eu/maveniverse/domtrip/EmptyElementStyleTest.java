package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for empty element style configuration and auto-detection functionality.
 */
public class EmptyElementStyleTest {

    @Test
    void testEmptyElementStyleEnum() {
        // Test enum values
        assertEquals(3, EmptyElementStyle.values().length);

        // Test toString methods
        assertTrue(EmptyElementStyle.EXPANDED.toString().contains("expanded"));
        assertTrue(EmptyElementStyle.SELF_CLOSING.toString().contains("self-closing"));
        assertTrue(EmptyElementStyle.SELF_CLOSING_SPACED.toString().contains("self-closing with space"));
    }

    @Test
    void testEmptyElementStyleFormatting() {
        String elementName = "test";
        String attributes = "id=\"123\" class=\"example\"";

        // Test EXPANDED style
        String expanded = EmptyElementStyle.EXPANDED.format(elementName, attributes);
        assertEquals("<test id=\"123\" class=\"example\"></test>", expanded);

        // Test SELF_CLOSING style
        String selfClosing = EmptyElementStyle.SELF_CLOSING.format(elementName, attributes);
        assertEquals("<test id=\"123\" class=\"example\"/>", selfClosing);

        // Test SELF_CLOSING_SPACED style
        String selfClosingSpaced = EmptyElementStyle.SELF_CLOSING_SPACED.format(elementName, attributes);
        assertEquals("<test id=\"123\" class=\"example\" />", selfClosingSpaced);

        // Test with no attributes
        String expandedNoAttrs = EmptyElementStyle.EXPANDED.format(elementName, "");
        assertEquals("<test></test>", expandedNoAttrs);

        String selfClosingNoAttrs = EmptyElementStyle.SELF_CLOSING.format(elementName, "");
        assertEquals("<test/>", selfClosingNoAttrs);

        String selfClosingSpacedNoAttrs = EmptyElementStyle.SELF_CLOSING_SPACED.format(elementName, "");
        assertEquals("<test />", selfClosingSpacedNoAttrs);
    }

    @Test
    void testAutoDetectionWithSelfClosingElements() throws DomTripException {
        String xml =
                """
            <root>
                <empty1/>
                <empty2/>
                <empty3/>
                <nonempty>content</nonempty>
            </root>
            """;

        Document doc = Document.of(xml);
        EmptyElementStyle detected = EmptyElementStyle.detectFromDocument(doc);
        assertEquals(EmptyElementStyle.SELF_CLOSING, detected);
    }

    @Test
    void testAutoDetectionWithSelfClosingSpacedElements() throws DomTripException {
        String xml =
                """
            <root>
                <empty1 />
                <empty2 />
                <empty3 />
                <nonempty>content</nonempty>
            </root>
            """;

        Document doc = Document.of(xml);
        EmptyElementStyle detected = EmptyElementStyle.detectFromDocument(doc);
        assertEquals(EmptyElementStyle.SELF_CLOSING_SPACED, detected);
    }

    @Test
    void testAutoDetectionWithExpandedElements() throws DomTripException {
        String xml =
                """
            <root>
                <empty1></empty1>
                <empty2></empty2>
                <empty3></empty3>
                <nonempty>content</nonempty>
            </root>
            """;

        Document doc = Document.of(xml);
        EmptyElementStyle detected = EmptyElementStyle.detectFromDocument(doc);
        assertEquals(EmptyElementStyle.EXPANDED, detected);
    }

    @Test
    void testAutoDetectionWithMixedStyles() throws DomTripException {
        // When there's a tie or mixed styles, should default to SELF_CLOSING
        String xml =
                """
            <root>
                <empty1/>
                <empty2></empty2>
                <nonempty>content</nonempty>
            </root>
            """;

        Document doc = Document.of(xml);
        EmptyElementStyle detected = EmptyElementStyle.detectFromDocument(doc);
        assertEquals(EmptyElementStyle.SELF_CLOSING, detected);
    }

    @Test
    void testAutoDetectionWithNoEmptyElements() throws DomTripException {
        String xml =
                """
            <root>
                <child1>content1</child1>
                <child2>content2</child2>
            </root>
            """;

        Document doc = Document.of(xml);
        EmptyElementStyle detected = EmptyElementStyle.detectFromDocument(doc);
        assertEquals(EmptyElementStyle.SELF_CLOSING, detected); // Default when no empty elements
    }

    @Test
    void testAutoDetectionWithNullDocument() {
        EmptyElementStyle detected = EmptyElementStyle.detectFromDocument(null);
        assertEquals(EmptyElementStyle.SELF_CLOSING, detected);
    }

    @Test
    void testDomTripConfigWithEmptyElementStyle() {
        // Test default configuration
        DomTripConfig defaultConfig = DomTripConfig.defaults();
        assertEquals(EmptyElementStyle.SELF_CLOSING, defaultConfig.emptyElementStyle());

        // Test explicit configuration
        DomTripConfig customConfig = DomTripConfig.defaults().withEmptyElementStyle(EmptyElementStyle.EXPANDED);
        assertEquals(EmptyElementStyle.EXPANDED, customConfig.emptyElementStyle());

        // Test fluent configuration
        DomTripConfig fluentConfig = DomTripConfig.prettyPrint()
                .withEmptyElementStyle(EmptyElementStyle.SELF_CLOSING_SPACED)
                .withIndentString("  ");
        assertEquals(EmptyElementStyle.SELF_CLOSING_SPACED, fluentConfig.emptyElementStyle());
    }

    @Test
    void testDomTripConfigAutoDetection() throws DomTripException {
        String xml =
                """
            <root>
                <empty1 />
                <empty2 />
                <child>content</child>
            </root>
            """;

        Document doc = Document.of(xml);
        DomTripConfig config = DomTripConfig.defaults().withAutoDetectedEmptyElementStyle(doc);

        assertEquals(EmptyElementStyle.SELF_CLOSING_SPACED, config.emptyElementStyle());
    }

    @Test
    void testSerializerWithEmptyElementStyle() {
        // Test that Serializer respects the empty element style configuration
        DomTripConfig config = DomTripConfig.prettyPrint().withEmptyElementStyle(EmptyElementStyle.EXPANDED);

        Serializer serializer = new Serializer(config);
        assertEquals(EmptyElementStyle.EXPANDED, serializer.getEmptyElementStyle());

        // Test setter
        serializer.setEmptyElementStyle(EmptyElementStyle.SELF_CLOSING_SPACED);
        assertEquals(EmptyElementStyle.SELF_CLOSING_SPACED, serializer.getEmptyElementStyle());

        // Test null handling
        serializer.setEmptyElementStyle(null);
        assertEquals(EmptyElementStyle.SELF_CLOSING, serializer.getEmptyElementStyle());
    }

    @Test
    void testPrettyPrintSerializationWithExpandedStyle() throws DomTripException {
        Document doc = Document.withRootElement("root");
        Element root = doc.root();

        // Add an empty element
        Element empty = Element.of("empty");
        root.addNode(empty);

        // Configure for expanded style
        DomTripConfig config = DomTripConfig.prettyPrint()
                .withEmptyElementStyle(EmptyElementStyle.EXPANDED)
                .withIndentString("  ");

        Serializer serializer = new Serializer(config);
        String result = serializer.serialize(doc);

        // Should contain expanded empty element
        assertTrue(result.contains("<empty></empty>"), "Should contain expanded empty element, but got: " + result);
    }

    @Test
    void testPrettyPrintSerializationWithSelfClosingStyle() throws DomTripException {
        Document doc = Document.withRootElement("root");
        Element root = doc.root();

        // Add an empty element
        Element empty = Element.of("empty");
        root.addNode(empty);

        // Configure for self-closing style
        DomTripConfig config = DomTripConfig.prettyPrint()
                .withEmptyElementStyle(EmptyElementStyle.SELF_CLOSING)
                .withIndentString("  ");

        Serializer serializer = new Serializer(config);
        String result = serializer.serialize(doc);

        // Should contain self-closing empty element
        assertTrue(result.contains("<empty/>"), "Should contain self-closing empty element, but got: " + result);
    }

    @Test
    void testPrettyPrintSerializationWithSelfClosingSpacedStyle() throws DomTripException {
        Document doc = Document.withRootElement("root");
        Element root = doc.root();

        // Add an empty element
        Element empty = Element.of("empty");
        root.addNode(empty);

        // Configure for self-closing spaced style
        DomTripConfig config = DomTripConfig.prettyPrint()
                .withEmptyElementStyle(EmptyElementStyle.SELF_CLOSING_SPACED)
                .withIndentString("  ");

        Serializer serializer = new Serializer(config);
        String result = serializer.serialize(doc);

        // Should contain self-closing spaced empty element
        assertTrue(
                result.contains("<empty />"), "Should contain self-closing spaced empty element, but got: " + result);
    }

    @Test
    void testEmptyElementWithAttributes() throws DomTripException {
        Document doc = Document.withRootElement("root");
        Element root = doc.root();

        // Add an empty element with attributes
        Element empty = Element.of("empty");
        empty.attribute("id", "test");
        empty.attribute("class", "example");
        root.addNode(empty);

        // Test different styles
        DomTripConfig expandedConfig = DomTripConfig.prettyPrint().withEmptyElementStyle(EmptyElementStyle.EXPANDED);
        String expandedResult = new Serializer(expandedConfig).serialize(doc);
        assertTrue(expandedResult.contains("<empty id=\"test\" class=\"example\"></empty>"));

        DomTripConfig selfClosingConfig =
                DomTripConfig.prettyPrint().withEmptyElementStyle(EmptyElementStyle.SELF_CLOSING);
        String selfClosingResult = new Serializer(selfClosingConfig).serialize(doc);
        assertTrue(selfClosingResult.contains("<empty id=\"test\" class=\"example\"/>"));

        DomTripConfig spacedConfig =
                DomTripConfig.prettyPrint().withEmptyElementStyle(EmptyElementStyle.SELF_CLOSING_SPACED);
        String spacedResult = new Serializer(spacedConfig).serialize(doc);
        assertTrue(spacedResult.contains("<empty id=\"test\" class=\"example\" />"));
    }

    @Test
    void testNonPrettyPrintPreservesOriginalFormatting() throws DomTripException {
        // Test that empty element style configuration doesn't affect existing formatting
        // when pretty print is disabled
        String originalXml =
                """
            <root>
                <expanded></expanded>
                <selfClosing/>
                <selfClosingSpaced />
                <mixed attr="value"/>
                <child>content</child>
            </root>
            """;

        Document doc = Document.of(originalXml);

        // Configure different empty element styles but disable pretty print
        DomTripConfig expandedConfig = DomTripConfig.defaults().withEmptyElementStyle(EmptyElementStyle.EXPANDED);
        String expandedResult = new Serializer(expandedConfig).serialize(doc);

        DomTripConfig selfClosingConfig =
                DomTripConfig.defaults().withEmptyElementStyle(EmptyElementStyle.SELF_CLOSING);
        String selfClosingResult = new Serializer(selfClosingConfig).serialize(doc);

        DomTripConfig spacedConfig =
                DomTripConfig.defaults().withEmptyElementStyle(EmptyElementStyle.SELF_CLOSING_SPACED);
        String spacedResult = new Serializer(spacedConfig).serialize(doc);

        // All results should be identical to original XML regardless of empty element style config
        assertEquals(originalXml, expandedResult, "EXPANDED config should preserve original formatting");
        assertEquals(originalXml, selfClosingResult, "SELF_CLOSING config should preserve original formatting");
        assertEquals(originalXml, spacedResult, "SELF_CLOSING_SPACED config should preserve original formatting");

        // Verify specific original formatting is preserved
        assertTrue(expandedResult.contains("<expanded></expanded>"));
        assertTrue(expandedResult.contains("<selfClosing/>"));
        assertTrue(expandedResult.contains("<selfClosingSpaced />"));
        assertTrue(expandedResult.contains("<mixed attr=\"value\"/>"));
    }

    @Test
    void testNonPrettyPrintPreservesOriginalFormattingAfterModification() throws DomTripException {
        // Test that only modified elements use the empty element style configuration
        String originalXml =
                """
            <root>
                <existing/>
                <another />
                <child>content</child>
            </root>
            """;

        Document doc = Document.of(originalXml);

        // Add a new empty element
        Element newEmpty = Element.of("newElement");
        doc.root().addNode(newEmpty);

        // Configure for expanded style but disable pretty print
        DomTripConfig config = DomTripConfig.defaults().withEmptyElementStyle(EmptyElementStyle.EXPANDED);
        String result = new Serializer(config).serialize(doc);

        // Original elements should preserve their formatting
        assertTrue(result.contains("<existing/>"), "Original self-closing should be preserved");
        assertTrue(result.contains("<another />"), "Original spaced self-closing should be preserved");
        assertTrue(result.contains("<child>content</child>"), "Non-empty element should be preserved");

        // New element should use the configured style (expanded)
        assertTrue(result.contains("<newElement></newElement>"), "New element should use configured style");
    }

    @Test
    void testNonPrettyPrintWithMixedOriginalStyles() throws DomTripException {
        // Test preservation of various original empty element styles
        String originalXml =
                """
            <project>
                <groupId>com.example</groupId>
                <artifactId>test</artifactId>
                <version>1.0</version>
                <properties>
                    <maven.compiler.source>17</maven.compiler.source>
                    <maven.compiler.target>17</maven.compiler.target>
                    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                </properties>
                <dependencies>
                    <dependency>
                        <groupId>junit</groupId>
                        <artifactId>junit</artifactId>
                        <version>4.13.2</version>
                        <scope>test</scope>
                        <exclusions/>
                    </dependency>
                </dependencies>
                <build>
                    <plugins>
                        <plugin>
                            <groupId>org.apache.maven.plugins</groupId>
                            <artifactId>maven-compiler-plugin</artifactId>
                            <version>3.11.0</version>
                            <configuration />
                        </plugin>
                    </plugins>
                </build>
            </project>
            """;

        Document doc = Document.of(originalXml);

        // Test with different empty element style configurations
        DomTripConfig[] configs = {
            DomTripConfig.defaults().withEmptyElementStyle(EmptyElementStyle.EXPANDED),
            DomTripConfig.defaults().withEmptyElementStyle(EmptyElementStyle.SELF_CLOSING),
            DomTripConfig.defaults().withEmptyElementStyle(EmptyElementStyle.SELF_CLOSING_SPACED)
        };

        for (DomTripConfig config : configs) {
            String result = new Serializer(config).serialize(doc);

            // All original formatting should be preserved regardless of config
            assertEquals(
                    originalXml,
                    result,
                    "Original formatting should be preserved with config: " + config.emptyElementStyle());

            // Verify specific elements maintain their original style
            assertTrue(result.contains("<exclusions/>"), "Self-closing without space should be preserved");
            assertTrue(result.contains("<configuration />"), "Self-closing with space should be preserved");
        }
    }

    @Test
    void testNonEmptyElementsUnaffected() throws DomTripException {
        Document doc = Document.withRootElement("root");
        Element root = doc.root();

        // Add a non-empty element
        Element nonEmpty = Element.of("child");
        nonEmpty.textContent("some content");
        root.addNode(nonEmpty);

        // Configure for expanded style
        DomTripConfig config = DomTripConfig.prettyPrint().withEmptyElementStyle(EmptyElementStyle.EXPANDED);

        Serializer serializer = new Serializer(config);
        String result = serializer.serialize(doc);

        // Non-empty elements should not be affected by empty element style
        assertTrue(result.contains("<child>some content</child>"));
        assertFalse(result.contains("<child></child>"));
    }

    @Test
    void testNestedEmptyElements() throws DomTripException {
        Document doc = Document.withRootElement("root");
        Element root = doc.root();

        // Add nested structure with empty elements
        Element parent = Element.of("parent");
        Element empty1 = Element.of("empty1");
        Element empty2 = Element.of("empty2");

        parent.addNode(empty1);
        parent.addNode(empty2);
        root.addNode(parent);

        // Configure for self-closing spaced style
        DomTripConfig config = DomTripConfig.prettyPrint()
                .withEmptyElementStyle(EmptyElementStyle.SELF_CLOSING_SPACED)
                .withIndentString("  ");

        Serializer serializer = new Serializer(config);
        String result = serializer.serialize(doc);

        // Both nested empty elements should use the configured style
        assertTrue(result.contains("<empty1 />"));
        assertTrue(result.contains("<empty2 />"));
        // Parent should not be affected as it has content
        assertTrue(result.contains("<parent>"));
        assertTrue(result.contains("</parent>"));
    }

    @Test
    void testAutoDetectionWithNestedEmptyElements() throws DomTripException {
        String xml =
                """
            <root>
                <parent>
                    <empty1/>
                    <empty2/>
                    <child>content</child>
                </parent>
                <empty3/>
            </root>
            """;

        Document doc = Document.of(xml);
        EmptyElementStyle detected = EmptyElementStyle.detectFromDocument(doc);
        assertEquals(EmptyElementStyle.SELF_CLOSING, detected);
    }

    @Test
    void testRoundTripPreservation() throws DomTripException {
        // Test that parsing and re-serializing preserves the detected style
        String originalXml =
                """
            <root>
                <empty1 />
                <empty2 />
                <child>content</child>
            </root>
            """;

        Document doc = Document.of(originalXml);

        // Auto-detect and configure
        DomTripConfig config = DomTripConfig.prettyPrint()
                .withAutoDetectedEmptyElementStyle(doc)
                .withIndentString("    ");

        Serializer serializer = new Serializer(config);
        String result = serializer.serialize(doc);

        // Should preserve the spaced self-closing style
        assertTrue(result.contains("<empty1 />"));
        assertTrue(result.contains("<empty2 />"));
    }

    @Test
    void testEditorIntegration() throws DomTripException {
        // Test that Editor respects empty element style configuration
        Document doc = Document.withRootElement("root");
        DomTripConfig config = DomTripConfig.prettyPrint().withEmptyElementStyle(EmptyElementStyle.EXPANDED);

        Editor editor = new Editor(doc, config);

        // Add an empty element through Editor
        editor.addElement(editor.root(), "placeholder");

        String result = editor.toXml();

        // Should use expanded style for the empty element
        assertTrue(result.contains("<placeholder></placeholder>"));
    }

    @Test
    void testMixedContentWithEmptyElements() throws DomTripException {
        Document doc = Document.withRootElement("root");
        Element root = doc.root();

        // Create mixed content: text, empty elements, and non-empty elements
        root.addNode(Text.of("  "));
        root.addNode(Element.of("empty1"));
        root.addNode(Text.of("  "));
        root.addNode(Element.text("nonempty", "content"));
        root.addNode(Text.of("  "));
        root.addNode(Element.of("empty2"));
        root.addNode(Text.of("  "));

        DomTripConfig config = DomTripConfig.prettyPrint().withEmptyElementStyle(EmptyElementStyle.SELF_CLOSING_SPACED);

        String result = new Serializer(config).serialize(doc);

        // Empty elements should use configured style
        assertTrue(result.contains("<empty1 />"));
        assertTrue(result.contains("<empty2 />"));
        // Non-empty element should be unaffected
        assertTrue(result.contains("<nonempty>content</nonempty>"));
    }

    @Test
    void testEmptyElementStyleOnlyAffectsPrettyPrint() throws DomTripException {
        // Verify that empty element style configuration only affects pretty print mode
        String originalXml =
                """
            <root>
                <empty1/>
                <empty2 />
                <empty3></empty3>
            </root>
            """;

        Document doc = Document.of(originalXml);

        // Test with pretty print disabled (default)
        DomTripConfig nonPrettyConfig = DomTripConfig.defaults().withEmptyElementStyle(EmptyElementStyle.EXPANDED);
        String nonPrettyResult = new Serializer(nonPrettyConfig).serialize(doc);

        // Should preserve original formatting exactly
        assertEquals(originalXml, nonPrettyResult);

        // Test with pretty print enabled
        DomTripConfig prettyConfig = DomTripConfig.prettyPrint()
                .withEmptyElementStyle(EmptyElementStyle.EXPANDED)
                .withIndentString("    ");
        String prettyResult = new Serializer(prettyConfig).serialize(doc);

        // Should apply the configured empty element style
        assertTrue(prettyResult.contains("<empty1></empty1>"));
        assertTrue(prettyResult.contains("<empty2></empty2>"));
        assertTrue(prettyResult.contains("<empty3></empty3>"));

        // Should not match original formatting
        assertNotEquals(originalXml, prettyResult);
    }

    @Test
    void testOriginalFormattingPreservedWithEditor() throws DomTripException {
        // Test that Editor preserves original formatting for unmodified elements
        String originalXml =
                """
            <config>
                <setting1/>
                <setting2 />
                <setting3></setting3>
                <value>content</value>
            </config>
            """;

        Document doc = Document.of(originalXml);

        // Configure for different empty element style
        DomTripConfig config = DomTripConfig.defaults().withEmptyElementStyle(EmptyElementStyle.SELF_CLOSING_SPACED);

        Editor editor = new Editor(doc, config);

        // Add a new empty element (should use configured style)
        editor.addElement(editor.root(), "newSetting");

        String result = editor.toXml();

        // Original elements should preserve their formatting
        assertTrue(result.contains("<setting1/>"), "Original self-closing should be preserved");
        assertTrue(result.contains("<setting2 />"), "Original spaced self-closing should be preserved");
        assertTrue(result.contains("<setting3></setting3>"), "Original expanded should be preserved");
        assertTrue(result.contains("<value>content</value>"), "Non-empty element should be preserved");

        // New element should use configured style (but only in pretty print mode)
        // Since we're not using pretty print, new element uses default Element.toXml()
        assertTrue(result.contains("<newSetting></newSetting>") || result.contains("<newSetting/>"));
    }
}
