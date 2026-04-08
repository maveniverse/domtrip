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
class ErrorHandlingTest {

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
        // Test unclosed tag - parser should throw
        assertThrows(DomTripException.class, () -> parser.parse("<root><unclosed>"));
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
        Editor localEditor = new Editor(doc);
        Element root = localEditor.root();

        assertThrows(DomTripException.class, () -> {
            localEditor.addElement(root, (String) null);
        });
    }

    @Test
    void testAddElementWithEmptyName() throws DomTripException {
        String xml = "<root/>";
        Document doc = Document.of(xml);
        Editor localEditor = new Editor(doc);
        Element root = localEditor.root();

        assertThrows(DomTripException.class, () -> {
            localEditor.addElement(root, "");
        });
    }

    @Test
    void testAddElementWithWhitespaceOnlyName() throws DomTripException {
        String xml = "<root/>";
        Document doc = Document.of(xml);
        Editor localEditor = new Editor(doc);
        Element root = localEditor.root();

        assertThrows(DomTripException.class, () -> {
            localEditor.addElement(root, "   ");
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
        Editor localEditor = new Editor(doc);
        Element root = localEditor.root();

        // Should not throw - null content should be handled gracefully
        Comment comment = localEditor.addComment(root, null);
        assertNotNull(comment);
        assertEquals("", comment.content());
    }

    @Test
    void testSetAttributeWithNullName() throws DomTripException {
        String xml = "<root/>";
        Document doc = Document.of(xml);
        Editor localEditor = new Editor(doc);
        Element root = localEditor.root();

        // Implementation may handle null name gracefully
        assertDoesNotThrow(() -> {
            root.attribute(null, "value");
        });
    }

    @Test
    void testSetAttributeWithNullValue() throws DomTripException {
        String xml = "<root/>";
        Document doc = Document.of(xml);
        Editor localEditor = new Editor(doc);
        Element root = localEditor.root();

        // Should handle null value gracefully
        root.attribute("test", null);
        assertNull(root.attribute("test"));
    }

    @Test
    void testRemoveNonExistentElement() throws DomTripException {
        String xml = "<root><child/></root>";
        Document doc = Document.of(xml);
        Editor localEditor = new Editor(doc);
        Element root = localEditor.root();
        Element child = (Element) root.child(0);

        // Remove the child
        localEditor.removeElement(child);

        // Try to remove it again - should handle gracefully
        assertDoesNotThrow(() -> {
            localEditor.removeElement(child);
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
    @SuppressWarnings("java:S5778")
    void testFindElementWithNullName() throws DomTripException {
        String xml = "<root><child/></root>";
        Document doc = Document.of(xml);

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
        Editor localEditor = new Editor(doc);
        String result = localEditor.toXml();
        assertTrue(result.contains("element-with-dashes"));
        assertTrue(result.contains("element_with_underscores"));
        assertTrue(result.contains("element.with.dots"));
    }

    @Test
    void testSpecialCharactersInAttributeNames() throws DomTripException {
        // Test with valid but unusual attribute names
        String xml = "<root attr-dash=\"value1\" attr_underscore=\"value2\" attr.dot=\"value3\"/>";

        Document doc = Document.of(xml);
        Editor localEditor = new Editor(doc);
        Element root = localEditor.root();
        assertEquals("value1", root.attribute("attr-dash"));
        assertEquals("value2", root.attribute("attr_underscore"));
        assertEquals("value3", root.attribute("attr.dot"));
    }

    @Test
    void testTruncatedXmlEndingWithLessThan() {
        // XML ending with '<' should throw, not loop infinitely
        String xml = "<root>text<";
        assertThrows(DomTripException.class, () -> Document.of(xml));
    }

    @Test
    void testUnclosedDeclaration() {
        // Declaration without closing '>' should throw
        assertThrows(DomTripException.class, () -> Document.of("<!foo"));
    }

    @Test
    void testUnclosedOpeningTag() {
        // Opening tag without closing '>' should throw
        assertThrows(DomTripException.class, () -> Document.of("<root"));
        assertThrows(DomTripException.class, () -> Document.of("<root attr=\"value\""));
    }

    @Test
    void testMismatchedClosingTag() {
        // Closing tag that doesn't match the open element should throw
        assertThrows(DomTripException.class, () -> Document.of("<root><child></root>"));
    }

    @Test
    void testUnexpectedClosingTag() {
        // Closing tag with no matching open element should throw
        assertThrows(DomTripException.class, () -> Document.of("</root>"));
        assertThrows(DomTripException.class, () -> Document.of("<root></root></extra>"));
    }

    @Test
    void testUnclosedElementAtEof() {
        // Element that is never closed should throw
        assertThrows(DomTripException.class, () -> Document.of("<root><child>text"));
        assertThrows(DomTripException.class, () -> Document.of("<root>"));
    }
}
