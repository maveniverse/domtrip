package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test cases for error handling and edge cases.
 */
public class ErrorHandlingTest {

    private Editor editor;
    private Parser parser;

    @BeforeEach
    void setUp() {
        editor = new Editor();
        parser = new Parser();
    }

    @Test
    void testParseNullXml() {
        assertThrows(ParseException.class, () -> {
            parser.parse(null);
        });
    }

    @Test
    void testParseEmptyXml() {
        assertThrows(ParseException.class, () -> {
            parser.parse("");
        });
    }

    @Test
    void testParseWhitespaceOnlyXml() {
        assertThrows(ParseException.class, () -> {
            parser.parse("   \n\t  ");
        });
    }

    @Test
    void testParseMalformedXml() {
        // Test unclosed tag - parser may handle this gracefully
        assertDoesNotThrow(() -> {
            Document doc = parser.parse("<root><unclosed>");
            assertNotNull(doc);
        });
    }

    @Test
    void testParseInvalidCharacters() {
        // Test with invalid XML characters
        String invalidXml = "<root>Invalid \u0000 character</root>";
        // Should not throw - parser should handle gracefully
        Document doc = parser.parse(invalidXml);
        assertNotNull(doc);
    }

    @Test
    void testAddElementWithNullParent() {
        assertThrows(InvalidXmlException.class, () -> {
            editor.addElement(null, "test");
        });
    }

    @Test
    void testAddElementWithNullName() throws ParseException {
        String xml = "<root/>";
        editor.loadXml(xml);
        Element root = editor.documentElement().orElseThrow();

        assertThrows(InvalidXmlException.class, () -> {
            editor.addElement(root, (String) null);
        });
    }

    @Test
    void testAddElementWithEmptyName() throws ParseException {
        String xml = "<root/>";
        editor.loadXml(xml);
        Element root = editor.documentElement().orElseThrow();

        assertThrows(InvalidXmlException.class, () -> {
            editor.addElement(root, "");
        });
    }

    @Test
    void testAddElementWithWhitespaceOnlyName() throws ParseException {
        String xml = "<root/>";
        editor.loadXml(xml);
        Element root = editor.documentElement().orElseThrow();

        assertThrows(InvalidXmlException.class, () -> {
            editor.addElement(root, "   ");
        });
    }

    @Test
    void testAddCommentWithNullParent() {
        assertThrows(InvalidXmlException.class, () -> {
            editor.addComment(null, "test comment");
        });
    }

    @Test
    void testAddCommentWithNullContent() throws ParseException {
        String xml = "<root/>";
        editor.loadXml(xml);
        Element root = editor.documentElement().orElseThrow();

        // Should not throw - null content should be handled gracefully
        Comment comment = editor.addComment(root, null);
        assertNotNull(comment);
        assertEquals("", comment.content());
    }

    @Test
    void testSetAttributeWithNullName() throws ParseException {
        String xml = "<root/>";
        editor.loadXml(xml);
        Element root = editor.documentElement().orElseThrow();

        // Implementation may handle null name gracefully
        assertDoesNotThrow(() -> {
            root.setAttribute(null, "value");
        });
    }

    @Test
    void testSetAttributeWithNullValue() throws ParseException {
        String xml = "<root/>";
        editor.loadXml(xml);
        Element root = editor.documentElement().orElseThrow();

        // Should handle null value gracefully
        root.setAttribute("test", null);
        assertNull(root.attribute("test"));
    }

    @Test
    void testRemoveNonExistentElement() throws ParseException {
        String xml = "<root><child/></root>";
        editor.loadXml(xml);
        Element root = editor.documentElement().orElseThrow();
        Element child = (Element) root.getChild(0);

        // Remove the child
        editor.removeElement(child);

        // Try to remove it again - should handle gracefully
        assertDoesNotThrow(() -> {
            editor.removeElement(child);
        });
    }

    @Test
    void testFindElementInEmptyDocument() {
        // Don't load any XML
        Optional<Element> result = editor.element("nonexistent");
        assertFalse(result.isPresent());
    }

    @Test
    void testFindElementWithNullName() throws ParseException {
        String xml = "<root><child/></root>";
        editor.loadXml(xml);

        // This should return empty Optional for null name
        Optional<Element> result = editor.element((String) null);
        assertFalse(result.isPresent());
    }

    @Test
    void testToXmlWithEmptyDocument() {
        // Don't load any XML
        String result = editor.toXml();
        assertEquals("", result);
    }

    @Test
    void testCreateDocumentWithNullRootName() {
        assertThrows(InvalidXmlException.class, () -> {
            editor.createDocument(null);
        });
    }

    @Test
    void testCreateDocumentWithEmptyRootName() {
        assertThrows(InvalidXmlException.class, () -> {
            editor.createDocument("");
        });
    }

    @Test
    void testLargeXmlDocument() throws ParseException {
        // Test with a large XML document to ensure no memory issues
        StringBuilder largeXml = new StringBuilder("<root>");
        for (int i = 0; i < 1000; i++) {
            largeXml.append("<element")
                    .append(i)
                    .append(">content")
                    .append(i)
                    .append("</element")
                    .append(i)
                    .append(">");
        }
        largeXml.append("</root>");

        editor.loadXml(largeXml.toString());
        String result = editor.toXml();
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    @Test
    void testDeeplyNestedXml() throws ParseException {
        // Test with deeply nested XML
        StringBuilder nestedXml = new StringBuilder();
        int depth = 100;

        for (int i = 0; i < depth; i++) {
            nestedXml.append("<level").append(i).append(">");
        }
        nestedXml.append("content");
        for (int i = depth - 1; i >= 0; i--) {
            nestedXml.append("</level").append(i).append(">");
        }

        editor.loadXml(nestedXml.toString());
        String result = editor.toXml();
        assertNotNull(result);
        assertTrue(result.contains("content"));
    }

    @Test
    void testXmlWithManyAttributes() throws ParseException {
        // Test element with many attributes
        StringBuilder xmlWithManyAttrs = new StringBuilder("<root");
        for (int i = 0; i < 50; i++) {
            xmlWithManyAttrs
                    .append(" attr")
                    .append(i)
                    .append("=\"value")
                    .append(i)
                    .append("\"");
        }
        xmlWithManyAttrs.append("/>");

        editor.loadXml(xmlWithManyAttrs.toString());
        Element root = editor.documentElement().orElseThrow();
        assertEquals("value0", root.attribute("attr0"));
        assertEquals("value49", root.attribute("attr49"));
    }

    @Test
    void testSpecialCharactersInElementNames() throws ParseException {
        // Test with valid but unusual element names
        String xml = "<root><element-with-dashes/><element_with_underscores/><element.with.dots/></root>";

        editor.loadXml(xml);
        String result = editor.toXml();
        assertTrue(result.contains("element-with-dashes"));
        assertTrue(result.contains("element_with_underscores"));
        assertTrue(result.contains("element.with.dots"));
    }

    @Test
    void testSpecialCharactersInAttributeNames() throws ParseException {
        // Test with valid but unusual attribute names
        String xml = "<root attr-dash=\"value1\" attr_underscore=\"value2\" attr.dot=\"value3\"/>";

        editor.loadXml(xml);
        Element root = editor.documentElement().orElseThrow();
        assertEquals("value1", root.attribute("attr-dash"));
        assertEquals("value2", root.attribute("attr_underscore"));
        assertEquals("value3", root.attribute("attr.dot"));
    }
}
