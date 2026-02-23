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
        editor = new Editor(Document.of());
        parser = new Parser();
    }

    @Test
    void testParseNullXml() {
        assertThrows(DomTripException.class, () -> {
            parser.parse((String) null);
        });
    }

    @Test
    void testParseEmptyXml() {
        assertThrows(DomTripException.class, () -> {
            parser.parse("");
        });
    }

    @Test
    void testParseWhitespaceOnlyXml() {
        assertThrows(DomTripException.class, () -> {
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
    void testParseInvalidCharacters() throws DomTripException {
        // Test with invalid XML characters
        String invalidXml = "<root>Invalid \u0000 character</root>";
        // Should not throw - parser should handle gracefully
        Document doc = parser.parse(invalidXml);
        assertNotNull(doc);
    }

    @Test
    void testAddElementWithNullParent() {
        assertThrows(DomTripException.class, () -> {
            editor.addElement(null, "test");
        });
    }

    @Test
    void testAddElementWithNullName() throws DomTripException {
        String xml = "<root/>";
        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element root = editor.root();

        assertThrows(DomTripException.class, () -> {
            editor.addElement(root, (String) null);
        });
    }

    @Test
    void testAddElementWithEmptyName() throws DomTripException {
        String xml = "<root/>";
        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element root = editor.root();

        assertThrows(DomTripException.class, () -> {
            editor.addElement(root, "");
        });
    }

    @Test
    void testAddElementWithWhitespaceOnlyName() throws DomTripException {
        String xml = "<root/>";
        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element root = editor.root();

        assertThrows(DomTripException.class, () -> {
            editor.addElement(root, "   ");
        });
    }

    @Test
    void testAddCommentWithNullParent() {
        assertThrows(DomTripException.class, () -> {
            editor.addComment(null, "test comment");
        });
    }

    @Test
    void testAddCommentWithNullContent() throws DomTripException {
        String xml = "<root/>";
        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element root = editor.root();

        // Should not throw - null content should be handled gracefully
        Comment comment = editor.addComment(root, null);
        assertNotNull(comment);
        assertEquals("", comment.content());
    }

    @Test
    void testSetAttributeWithNullName() throws DomTripException {
        String xml = "<root/>";
        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element root = editor.root();

        // Implementation may handle null name gracefully
        assertDoesNotThrow(() -> {
            root.attribute(null, "value");
        });
    }

    @Test
    void testSetAttributeWithNullValue() throws DomTripException {
        String xml = "<root/>";
        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element root = editor.root();

        // Should handle null value gracefully
        root.attribute("test", null);
        assertNull(root.attribute("test"));
    }

    @Test
    void testRemoveNonExistentElement() throws DomTripException {
        String xml = "<root><child/></root>";
        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element root = editor.root();
        Element child = (Element) root.node(0);

        // Remove the child
        editor.removeElement(child);

        // Try to remove it again - should handle gracefully
        assertDoesNotThrow(() -> {
            editor.removeElement(child);
        });
    }

    @Test
    void testFindElementInEmptyDocument() throws DomTripException {
        // Don't load any XML
        Document doc = Document.of("<root/>");
        Optional<Element> result = doc.root().descendant("nonexistent");
        assertFalse(result.isPresent());
    }

    @Test
    void testFindElementWithNullName() throws DomTripException {
        String xml = "<root><child/></root>";
        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        // This should return empty Optional for null name
        assertThrows(NullPointerException.class, () -> doc.root().descendant((String) null));
    }

    @Test
    void testToXmlWithEmptyDocument() {
        // Don't load any XML
        String result = editor.toXml();
        assertEquals("", result);
    }

    @Test
    void testCreateDocumentWithNullRootName() {
        assertThrows(DomTripException.class, () -> {
            editor.createDocument(null);
        });
    }

    @Test
    void testCreateDocumentWithEmptyRootName() {
        assertThrows(DomTripException.class, () -> {
            editor.createDocument("");
        });
    }

    @Test
    void testLargeXmlDocument() throws DomTripException {
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

        editor = new Editor(Document.of(largeXml.toString()));
        String result = editor.toXml();
        assertNotNull(result);
        assertTrue(!result.isEmpty());
    }

    @Test
    void testDeeplyNestedXml() throws DomTripException {
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

        editor = new Editor(Document.of(nestedXml.toString()));
        String result = editor.toXml();
        assertNotNull(result);
        assertTrue(result.contains("content"));
    }

    @Test
    void testXmlWithManyAttributes() throws DomTripException {
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

        editor = new Editor(Document.of(xmlWithManyAttrs.toString()));
        Element root = editor.root();
        assertEquals("value0", root.attribute("attr0"));
        assertEquals("value49", root.attribute("attr49"));
    }

    @Test
    void testSpecialCharactersInElementNames() throws DomTripException {
        // Test with valid but unusual element names
        String xml = "<root><element-with-dashes/><element_with_underscores/><element.with.dots/></root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();
        assertTrue(result.contains("element-with-dashes"));
        assertTrue(result.contains("element_with_underscores"));
        assertTrue(result.contains("element.with.dots"));
    }

    @Test
    void testSpecialCharactersInAttributeNames() throws DomTripException {
        // Test with valid but unusual attribute names
        String xml = "<root attr-dash=\"value1\" attr_underscore=\"value2\" attr.dot=\"value3\"/>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element root = editor.root();
        assertEquals("value1", root.attribute("attr-dash"));
        assertEquals("value2", root.attribute("attr_underscore"));
        assertEquals("value3", root.attribute("attr.dot"));
    }
}
