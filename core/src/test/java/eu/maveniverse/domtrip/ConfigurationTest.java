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

        assertTrue(config.isPreserveComments());
        assertTrue(config.isPreserveProcessingInstructions());
        assertEquals(QuoteStyle.DOUBLE, config.defaultQuoteStyle());
        assertFalse(config.isPrettyPrint());
        assertEquals("    ", config.indentString());
    }

    @Test
    void testDomTripConfigPrettyPrint() {
        DomTripConfig config = DomTripConfig.prettyPrint();

        assertTrue(config.isPrettyPrint());
        // Other defaults should remain
        assertTrue(config.isPreserveComments());
    }

    @Test
    void testDomTripConfigFluentApi() {
        DomTripConfig config = DomTripConfig.defaults()
                .withCommentPreservation(false)
                .withProcessingInstructionPreservation(false)
                .withDefaultQuoteStyle(QuoteStyle.SINGLE)
                .withPrettyPrint(true)
                .withIndentString("\t");

        assertFalse(config.isPreserveComments());
        assertFalse(config.isPreserveProcessingInstructions());
        assertEquals(QuoteStyle.SINGLE, config.defaultQuoteStyle());
        assertTrue(config.isPrettyPrint());
        assertEquals("\t", config.indentString());
    }

    @Test
    void testEditorWithConfiguration() throws DomTripException {
        DomTripConfig config = DomTripConfig.defaults().withDefaultQuoteStyle(QuoteStyle.SINGLE);

        Editor editor = new Editor(config);
        assertEquals(config, editor.config());

        // Test with XML
        String xml = "<root attr=\"value\">content</root>";
        Editor editorWithXml = new Editor(Document.of(xml), config);
        assertEquals(config, editorWithXml.config());
        assertNotNull(editorWithXml.document());
    }

    @Test
    void testSerializerWithConfig() throws DomTripException {
        String xml = "<?xml version=\"1.0\"?>\n<!-- Comment -->\n<root attr=\"value\">content</root>";

        // Test with different configurations
        Serializer defaultSerializer = new Serializer();
        Serializer prettySerializer = new Serializer(DomTripConfig.prettyPrint());
        Serializer minimalSerializer = new Serializer(DomTripConfig.minimal());

        Editor editor = new Editor(Document.of(xml));
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
    void testQuoteStyleEnum() throws DomTripException {
        assertEquals('"', QuoteStyle.DOUBLE.getCharacter());
        assertEquals('\'', QuoteStyle.SINGLE.getCharacter());

        assertEquals(QuoteStyle.DOUBLE, QuoteStyle.fromChar('"'));
        assertEquals(QuoteStyle.SINGLE, QuoteStyle.fromChar('\''));

        assertThrows(DomTripException.class, () -> {
            QuoteStyle.fromChar('x');
        });

        assertEquals("\"", QuoteStyle.DOUBLE.toString());
        assertEquals("'", QuoteStyle.SINGLE.toString());
    }

    @Test
    void testWhitespace() {
        DomTripConfig config = DomTripConfig.defaults().withIndentString("  ");
        Editor editor = new Editor(config);

        // Test whitespace detection
        assertTrue(editor.isWhitespaceOnly("   "));
        assertTrue(editor.isWhitespaceOnly("\n\t  "));
        assertTrue(editor.isWhitespaceOnly(""));
        assertTrue(editor.isWhitespaceOnly(null));
        assertFalse(editor.isWhitespaceOnly("content"));
        assertFalse(editor.isWhitespaceOnly("  content  "));
    }
}
