package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test cases for different serialization modes: preserve formatting, pretty print, and raw mode.
 */
public class SerializationModesTest {

    private Document document;
    private String originalXml;

    @BeforeEach
    void setUp() {
        originalXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<!-- Document comment -->\n"
                + "<root xmlns=\"http://example.com\">\n"
                + "  <element attr=\"value\">text content</element>\n"
                + "  <nested>\n"
                + "    <child>nested content</child>\n"
                + "  </nested>\n"
                + "  <!-- inline comment -->\n"
                + "  <empty/>\n"
                + "</root>";
        document = Document.of(originalXml);
    }

    @Test
    void testPreserveFormattingMode() {
        // Default serializer should preserve formatting when prettyPrint = false
        Serializer serializer = new Serializer();
        assertFalse(serializer.isPrettyPrint());

        String result = serializer.serialize(document);

        // Should preserve original formatting exactly
        assertEquals(originalXml, result);
    }

    @Test
    void testPrettyPrintMode() {
        // Pretty print mode should apply consistent formatting
        Serializer serializer = new Serializer();
        serializer.setPrettyPrint(true);
        serializer.setIndentString("  "); // 2 spaces

        String result = serializer.serialize(document);

        // Should have consistent indentation
        assertTrue(result.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(result.contains("<root xmlns=\"http://example.com\">"));

        // Check that elements are properly indented
        String[] lines = result.split("\n");
        boolean foundElementLine = false;
        boolean foundNestedLine = false;
        boolean foundChildLine = false;

        for (String line : lines) {
            if (line.contains("<element attr=\"value\">")) {
                assertTrue(line.startsWith("  "), "element should be indented with 2 spaces");
                foundElementLine = true;
            }
            if (line.contains("<nested>")) {
                assertTrue(line.startsWith("  "), "nested should be indented with 2 spaces");
                foundNestedLine = true;
            }
            if (line.contains("<child>")) {
                assertTrue(line.startsWith("    "), "child should be indented with 4 spaces");
                foundChildLine = true;
            }
        }

        assertTrue(foundElementLine, "Should find element line");
        assertTrue(foundNestedLine, "Should find nested line");
        assertTrue(foundChildLine, "Should find child line");
    }

    @Test
    void testRawMode() {
        // Create a document without whitespace content for raw mode testing
        String compactXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<!-- Document comment -->"
                + "<root xmlns=\"http://example.com\">"
                + "<element attr=\"value\">text content</element>"
                + "<nested><child>nested content</child></nested>"
                + "<!-- inline comment -->"
                + "<empty/>"
                + "</root>";
        Document compactDoc = Document.of(compactXml);

        // Raw mode: no line endings, no indentation
        Serializer serializer = new Serializer();
        serializer.setPrettyPrint(true);
        serializer.setIndentString(""); // No indentation
        serializer.setLineEnding(""); // No line endings

        String result = serializer.serialize(compactDoc);

        // Should have no line breaks or indentation added by serializer
        assertFalse(result.contains("\n"), "Raw mode should not contain line breaks");

        // Should still contain all the content
        assertTrue(result.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(result.contains("<root xmlns=\"http://example.com\">"));
        assertTrue(result.contains("<element attr=\"value\">text content</element>"));
        assertTrue(result.contains("<nested><child>nested content</child></nested>"));
        assertTrue(result.contains("<!-- Document comment -->"));
        assertTrue(result.contains("<!-- inline comment -->"));
        assertTrue(result.contains("<empty/>"));
        assertTrue(result.contains("</root>"));

        // The result should be exactly the same as input since no formatting is applied
        assertEquals(compactXml, result);
    }

    @Test
    void testRawModeWithConfig() {
        // Create a document without whitespace content for raw mode testing
        String compactXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<root xmlns=\"http://example.com\">"
                + "<element attr=\"value\">text content</element>"
                + "</root>";
        Document compactDoc = Document.of(compactXml);

        // Test raw mode using DomTripConfig
        DomTripConfig rawConfig =
                DomTripConfig.prettyPrint().withIndentString("").withLineEnding("");

        Serializer serializer = new Serializer(rawConfig);
        String result = serializer.serialize(compactDoc);

        // Should have no line breaks or indentation
        assertFalse(result.contains("\n"), "Raw mode should not contain line breaks");

        // Should still contain all the content
        assertTrue(result.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(result.contains("<root xmlns=\"http://example.com\">"));
        assertTrue(result.contains("<element attr=\"value\">text content</element>"));

        // The result should be exactly the same as input since no formatting is applied
        assertEquals(compactXml, result);
    }

    @Test
    void testModifiedDocumentBehavior() {
        // When document is modified, formatting behavior should change
        Editor editor = new Editor(document);
        Element root = editor.root();
        editor.addElement(root, "newElement", "new content");

        // Preserve formatting mode (prettyPrint = false)
        Serializer preserveSerializer = new Serializer();
        assertFalse(preserveSerializer.isPrettyPrint());
        String preserveResult = preserveSerializer.serialize(document);

        // Should still use Element.toXml() for modified elements
        assertTrue(preserveResult.contains("<newElement>new content</newElement>"));

        // Pretty print mode
        Serializer prettySerializer = new Serializer();
        prettySerializer.setPrettyPrint(true);
        String prettyResult = prettySerializer.serialize(document);

        // Should apply pretty printing formatting
        assertTrue(prettyResult.contains("\n"));
        assertTrue(prettyResult.contains("  <newElement>new content</newElement>"));
    }

    @Test
    void testDifferentIndentationStyles() {
        Serializer serializer = new Serializer();
        serializer.setPrettyPrint(true);

        // Test with tabs
        serializer.setIndentString("\t");
        String tabResult = serializer.serialize(document);
        assertTrue(tabResult.contains("\t<element"));

        // Test with 4 spaces
        serializer.setIndentString("    ");
        String fourSpaceResult = serializer.serialize(document);
        assertTrue(fourSpaceResult.contains("    <element"));

        // Test with custom indentation
        serializer.setIndentString("--");
        String customResult = serializer.serialize(document);
        assertTrue(customResult.contains("--<element"));
    }

    @Test
    void testDifferentLineEndings() {
        Serializer serializer = new Serializer();
        serializer.setPrettyPrint(true);

        // Test with Windows line endings
        serializer.setLineEnding("\r\n");
        String windowsResult = serializer.serialize(document);
        assertTrue(windowsResult.contains("\r\n"));

        // Test with Unix line endings
        serializer.setLineEnding("\n");
        String unixResult = serializer.serialize(document);
        assertTrue(unixResult.contains("\n"));
        assertFalse(unixResult.contains("\r\n"));

        // Test with custom line ending
        serializer.setLineEnding(" | ");
        String customResult = serializer.serialize(document);
        assertTrue(customResult.contains(" | "));
    }

    @Test
    void testSingleElementRawMode() {
        Element element = new Element("test");
        element.attribute("attr", "value");
        element.textContent("content");

        Serializer serializer = new Serializer();
        serializer.setPrettyPrint(true);
        serializer.setIndentString("");
        serializer.setLineEnding("");

        String result = serializer.serialize(element);

        assertEquals("<test attr=\"value\">content</test>", result);
        assertFalse(result.contains("\n"));
        // Should not contain indentation spaces (but attribute spaces are OK)
        assertFalse(result.contains("  "), "Should not contain indentation spaces");
    }

    @Test
    void testEmptyIndentAndLineEndingPreservesContent() {
        // Ensure that empty indent and line ending don't affect content
        String xmlWithSpaces = "<root><element>  content with spaces  </element></root>";
        Document doc = Document.of(xmlWithSpaces);

        Serializer serializer = new Serializer();
        serializer.setPrettyPrint(true);
        serializer.setIndentString("");
        serializer.setLineEnding("");

        String result = serializer.serialize(doc);

        // Content spaces should be preserved
        assertTrue(result.contains("  content with spaces  "));
        // But no formatting spaces should be added
        assertEquals("<root><element>  content with spaces  </element></root>", result);
    }

    @Test
    void testRawConfigMethod() {
        // Test the convenience raw() method in DomTripConfig
        String compactXml = "<?xml version=\"1.0\"?><root><child>content</child></root>";
        Document doc = Document.of(compactXml);

        Serializer serializer = new Serializer(DomTripConfig.raw());
        String result = serializer.serialize(doc);

        // Should produce completely unformatted output
        assertFalse(result.contains("\n"));
        assertEquals(compactXml, result);
    }

    @Test
    void testRawModeVsPrettyPrintComparison() {
        // Create a document and compare different serialization modes
        String xml = "<root><child attr=\"value\">content</child><another>text</another></root>";
        Document doc = Document.of(xml);

        // Raw mode
        Serializer rawSerializer = new Serializer(DomTripConfig.raw());
        String rawResult = rawSerializer.serialize(doc);

        // Pretty print mode
        Serializer prettySerializer = new Serializer(DomTripConfig.prettyPrint());
        String prettyResult = prettySerializer.serialize(doc);

        // Preserve formatting mode (default)
        Serializer preserveSerializer = new Serializer();
        String preserveResult = preserveSerializer.serialize(doc);

        // Raw should have no line breaks
        assertFalse(rawResult.contains("\n"));
        assertEquals(xml, rawResult);

        // Pretty print should have line breaks and indentation
        assertTrue(prettyResult.contains("\n"));
        assertTrue(prettyResult.contains("  <child"));

        // Preserve should maintain original formatting (which has no line breaks in this case)
        assertEquals(xml, preserveResult);
    }

    @Test
    void testRawFormattingDetectionAndPreservation() {
        // Test the key question: when parsing raw XML and adding nodes,
        // does the system detect and preserve the raw formatting?
        String rawXml = "<root><existing>content</existing></root>";
        Document doc = Document.of(rawXml);
        Editor editor = new Editor(doc);

        // Add a new element
        Element root = editor.root();
        editor.addElement(root, "newElement", "new content");

        String result = editor.toXml();

        // Now the system should detect raw formatting and preserve it
        assertFalse(result.contains("\n"), "Should preserve raw formatting when original has no line breaks");
        assertTrue(result.contains("<newElement>new content</newElement>"), "Should contain the new element");

        // The result should be in raw format
        String expectedPattern = "<root><existing>content</existing><newElement>new content</newElement></root>";
        assertEquals(expectedPattern, result);
    }

    @Test
    void testEmptyDocumentUsesConfigDefault() {
        // Test that empty documents use config default, not raw formatting
        Document emptyDoc = new Document();
        Editor editor = new Editor(
                emptyDoc, DomTripConfig.prettyPrint().withLineEnding("\n").withIndentString("  "));

        // Create root element and add content
        editor.createDocument("root");
        Element root = editor.root();
        editor.addElement(root, "child", "content");

        String result = editor.toXml();

        // Should use config defaults (pretty print with line endings), not raw formatting
        assertTrue(result.contains("\n"), "Empty document should use config default line endings");
        assertTrue(result.contains("  <child"), "Should have proper indentation");
    }

    @Test
    void testFormattingDetectionScenarios() {
        // Test various scenarios to ensure correct formatting detection

        // Scenario 1: Raw XML (no formatting)
        String rawXml = "<root><child>content</child></root>";
        Document rawDoc = Document.of(rawXml);
        Editor rawEditor = new Editor(rawDoc);
        rawEditor.addElement(rawEditor.root(), "new", "element");
        String rawResult = rawEditor.toXml();
        assertFalse(rawResult.contains("\n"), "Raw XML should preserve raw formatting");

        // Scenario 2: Pretty printed XML
        String prettyXml = "<root>\n  <child>content</child>\n</root>";
        Document prettyDoc = Document.of(prettyXml);
        Editor prettyEditor = new Editor(prettyDoc);
        prettyEditor.addElement(prettyEditor.root(), "new", "element");
        String prettyResult = prettyEditor.toXml();
        assertTrue(prettyResult.contains("\n"), "Pretty XML should preserve pretty formatting");
        assertTrue(prettyResult.contains("  <new>element</new>"), "Should maintain indentation");

        // Scenario 3: Empty document with default config
        Document emptyDoc = new Document();
        Editor emptyEditor = new Editor(emptyDoc); // Uses default config
        emptyEditor.createDocument("root");
        emptyEditor.addElement(emptyEditor.root(), "child", "content");
        String emptyResult = emptyEditor.toXml();
        // Default config has prettyPrint = false, so it should preserve formatting
        // But since there's no existing formatting, it uses minimal formatting

        // Scenario 4: Empty document with pretty print config
        Document emptyPrettyDoc = new Document();
        Editor emptyPrettyEditor = new Editor(emptyPrettyDoc, DomTripConfig.prettyPrint());
        emptyPrettyEditor.createDocument("root");
        emptyPrettyEditor.addElement(emptyPrettyEditor.root(), "child", "content");
        String emptyPrettyResult = emptyPrettyEditor.toXml();
        assertTrue(emptyPrettyResult.contains("\n"), "Empty doc with pretty config should use pretty formatting");
    }
}
