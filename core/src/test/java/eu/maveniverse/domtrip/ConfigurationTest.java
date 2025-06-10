package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Test cases for configuration and serialization options.
 */
public class ConfigurationTest {

    @Test
    void testDomTripConfigDefaults() {
        DomTripConfig config = DomTripConfig.defaults();

        assertTrue(config.isPreserveWhitespace());
        assertTrue(config.isPreserveComments());
        assertTrue(config.isPreserveEntities());
        assertTrue(config.isPreserveProcessingInstructions());
        assertTrue(config.isPreserveCData());
        assertEquals("UTF-8", config.defaultEncoding());
        assertEquals(QuoteStyle.DOUBLE, config.defaultQuoteStyle());
        assertTrue(config.isValidateXmlNames());
        assertFalse(config.isStrictParsing());
        assertFalse(config.isPrettyPrint());
        assertEquals("    ", config.indentString());
    }

    @Test
    void testDomTripConfigStrict() {
        DomTripConfig config = DomTripConfig.strict();

        assertTrue(config.isStrictParsing());
        assertTrue(config.isValidateXmlNames());
        // Other defaults should remain
        assertTrue(config.isPreserveWhitespace());
        assertTrue(config.isPreserveComments());
    }

    @Test
    void testDomTripConfigLenient() {
        DomTripConfig config = DomTripConfig.lenient();

        assertFalse(config.isStrictParsing());
        assertFalse(config.isValidateXmlNames());
        // Other defaults should remain
        assertTrue(config.isPreserveWhitespace());
        assertTrue(config.isPreserveComments());
    }

    @Test
    void testDomTripConfigPrettyPrint() {
        DomTripConfig config = DomTripConfig.prettyPrint();

        assertTrue(config.isPrettyPrint());
        assertFalse(config.isPreserveWhitespace());
        // Other defaults should remain
        assertTrue(config.isPreserveComments());
        assertTrue(config.isPreserveEntities());
    }

    @Test
    void testDomTripConfigFluentApi() {
        DomTripConfig config = DomTripConfig.defaults()
                .withWhitespacePreservation(false)
                .withCommentPreservation(false)
                .withEntityPreservation(false)
                .withProcessingInstructionPreservation(false)
                .withCDataPreservation(false)
                .withDefaultEncoding("ISO-8859-1")
                .withDefaultQuoteStyle(QuoteStyle.SINGLE)
                .withXmlNameValidation(false)
                .withStrictParsing(true)
                .withPrettyPrint(true)
                .withIndentString("\t");

        assertFalse(config.isPreserveWhitespace());
        assertFalse(config.isPreserveComments());
        assertFalse(config.isPreserveEntities());
        assertFalse(config.isPreserveProcessingInstructions());
        assertFalse(config.isPreserveCData());
        assertEquals("ISO-8859-1", config.defaultEncoding());
        assertEquals(QuoteStyle.SINGLE, config.defaultQuoteStyle());
        assertFalse(config.isValidateXmlNames());
        assertTrue(config.isStrictParsing());
        assertTrue(config.isPrettyPrint());
        assertEquals("\t", config.indentString());
    }

    @Test
    void testEditorWithConfiguration() throws ParseException {
        DomTripConfig config =
                DomTripConfig.strict().withDefaultEncoding("ISO-8859-1").withDefaultQuoteStyle(QuoteStyle.SINGLE);

        Editor editor = new Editor(config);
        assertEquals(config, editor.config());

        // Test with XML
        String xml = "<root attr=\"value\">content</root>";
        Editor editorWithXml = new Editor(xml, config);
        assertEquals(config, editorWithXml.config());
        assertNotNull(editorWithXml.document());
    }

    @Test
    void testSerializerWithConfig() throws ParseException {
        String xml = "<?xml version=\"1.0\"?>\n<!-- Comment -->\n<root attr=\"value\">content</root>";

        // Test with different configurations
        Serializer defaultSerializer = new Serializer();
        Serializer prettySerializer = new Serializer(DomTripConfig.prettyPrint());
        Serializer minimalSerializer = new Serializer(DomTripConfig.minimal());

        Editor editor = new Editor(xml);
        Document doc = editor.document();

        String defaultResult = defaultSerializer.serialize(doc);
        String prettyResult = prettySerializer.serialize(doc);
        String minimalResult = minimalSerializer.serialize(doc);

        // All should contain the basic content
        assertTrue(defaultResult.contains("<root"));
        assertTrue(prettyResult.contains("<root"));
        assertTrue(minimalResult.contains("<root"));

        assertTrue(defaultResult.contains("content"));
        assertTrue(prettyResult.contains("content"));
        assertTrue(minimalResult.contains("content"));
    }

    @Test
    void testQuoteStyleEnum() {
        assertEquals('"', QuoteStyle.DOUBLE.getCharacter());
        assertEquals('\'', QuoteStyle.SINGLE.getCharacter());

        assertEquals(QuoteStyle.DOUBLE, QuoteStyle.fromChar('"'));
        assertEquals(QuoteStyle.SINGLE, QuoteStyle.fromChar('\''));

        assertThrows(IllegalArgumentException.class, () -> {
            QuoteStyle.fromChar('x');
        });

        assertEquals("\"", QuoteStyle.DOUBLE.toString());
        assertEquals("'", QuoteStyle.SINGLE.toString());
    }

    @Test
    void testWhitespaceStyleEnum() {
        assertEquals(" ", WhitespaceStyle.SINGLE_SPACE.getValue());
        assertEquals("  ", WhitespaceStyle.DOUBLE_SPACE.getValue());
        assertEquals("\t", WhitespaceStyle.TAB.getValue());
        assertEquals("\n", WhitespaceStyle.NEWLINE.getValue());
        assertEquals("\n    ", WhitespaceStyle.NEWLINE_WITH_INDENT.getValue());
        assertEquals("", WhitespaceStyle.EMPTY.getValue());

        assertEquals(WhitespaceStyle.SINGLE_SPACE, WhitespaceStyle.fromString(" "));
        assertEquals(WhitespaceStyle.DOUBLE_SPACE, WhitespaceStyle.fromString("  "));
        assertEquals(WhitespaceStyle.TAB, WhitespaceStyle.fromString("\t"));

        // Unknown whitespace should return SINGLE_SPACE
        assertEquals(WhitespaceStyle.SINGLE_SPACE, WhitespaceStyle.fromString("unknown"));
        assertEquals(WhitespaceStyle.SINGLE_SPACE, WhitespaceStyle.fromString(null));

        assertEquals(" ", WhitespaceStyle.SINGLE_SPACE.toString());
        assertEquals("\t", WhitespaceStyle.TAB.toString());
    }

    @Test
    void testWhitespaceManager() {
        DomTripConfig config = DomTripConfig.defaults().withIndentString("  ");
        WhitespaceManager wm = new WhitespaceManager(config);

        // Test whitespace detection
        assertTrue(wm.isWhitespaceOnly("   "));
        assertTrue(wm.isWhitespaceOnly("\n\t  "));
        assertTrue(wm.isWhitespaceOnly(""));
        assertTrue(wm.isWhitespaceOnly(null));
        assertFalse(wm.isWhitespaceOnly("content"));
        assertFalse(wm.isWhitespaceOnly("  content  "));

        // Test indentation creation
        assertEquals("", wm.createIndentation(0));
        assertEquals("  ", wm.createIndentation(1));
        assertEquals("    ", wm.createIndentation(2));
        assertEquals("      ", wm.createIndentation(3));

        // Test depth calculation
        assertEquals(0, wm.calculateDepth(""));
        assertEquals(0, wm.calculateDepth("no newline"));
        assertEquals(1, wm.calculateDepth("\n  "));
        assertEquals(2, wm.calculateDepth("\n    "));
        assertEquals(0, wm.calculateDepth("\n"));

        // Test normalization (with preserve whitespace enabled, content is returned as-is)
        assertEquals("", wm.normalizeWhitespace(""));
        assertEquals("content", wm.normalizeWhitespace("content"));
        assertEquals("  content  ", wm.normalizeWhitespace("  content  "));
        assertEquals("a   b\n\tc", wm.normalizeWhitespace("a   b\n\tc"));

        // Test with preserve whitespace disabled
        DomTripConfig noWhitespaceConfig =
                DomTripConfig.defaults().withWhitespacePreservation(false).withIndentString("  ");
        WhitespaceManager noWsManager = new WhitespaceManager(noWhitespaceConfig);

        assertEquals("content", noWsManager.normalizeWhitespace("  content  "));
        assertEquals("a b c", noWsManager.normalizeWhitespace("a   b\n\tc"));
    }
}
