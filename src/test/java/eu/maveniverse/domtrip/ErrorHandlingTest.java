package eu.maveniverse.domtrip;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

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
        assertThrows(IllegalArgumentException.class, () -> {
            parser.parse(null);
        });
    }
    
    @Test
    void testParseEmptyXml() {
        assertThrows(IllegalArgumentException.class, () -> {
            parser.parse("");
        });
    }
    
    @Test
    void testParseWhitespaceOnlyXml() {
        assertThrows(IllegalArgumentException.class, () -> {
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
        assertThrows(IllegalArgumentException.class, () -> {
            editor.addElement(null, "test");
        });
    }
    
    @Test
    void testAddElementWithNullName() {
        String xml = "<root/>";
        editor.loadXml(xml);
        Element root = editor.getRootElement();
        
        assertThrows(IllegalArgumentException.class, () -> {
            editor.addElement(root, null);
        });
    }
    
    @Test
    void testAddElementWithEmptyName() {
        String xml = "<root/>";
        editor.loadXml(xml);
        Element root = editor.getRootElement();
        
        assertThrows(IllegalArgumentException.class, () -> {
            editor.addElement(root, "");
        });
    }
    
    @Test
    void testAddElementWithWhitespaceOnlyName() {
        String xml = "<root/>";
        editor.loadXml(xml);
        Element root = editor.getRootElement();
        
        assertThrows(IllegalArgumentException.class, () -> {
            editor.addElement(root, "   ");
        });
    }
    
    @Test
    void testAddCommentWithNullParent() {
        assertThrows(IllegalArgumentException.class, () -> {
            editor.addComment(null, "test comment");
        });
    }
    
    @Test
    void testAddCommentWithNullContent() {
        String xml = "<root/>";
        editor.loadXml(xml);
        Element root = editor.getRootElement();
        
        // Should not throw - null content should be handled gracefully
        Comment comment = editor.addComment(root, null);
        assertNotNull(comment);
        assertEquals("", comment.getContent());
    }
    
    @Test
    void testSetAttributeWithNullName() {
        String xml = "<root/>";
        editor.loadXml(xml);
        Element root = editor.getRootElement();

        // Implementation may handle null name gracefully
        assertDoesNotThrow(() -> {
            root.setAttribute(null, "value");
        });
    }
    
    @Test
    void testSetAttributeWithNullValue() {
        String xml = "<root/>";
        editor.loadXml(xml);
        Element root = editor.getRootElement();
        
        // Should handle null value gracefully
        root.setAttribute("test", null);
        assertNull(root.getAttribute("test"));
    }
    
    @Test
    void testRemoveNonExistentElement() {
        String xml = "<root><child/></root>";
        editor.loadXml(xml);
        Element root = editor.getRootElement();
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
        Element result = editor.findElement("nonexistent");
        assertNull(result);
    }
    
    @Test
    void testFindElementWithNullName() {
        String xml = "<root><child/></root>";
        editor.loadXml(xml);

        // This may throw NPE in current implementation
        assertThrows(NullPointerException.class, () -> {
            editor.findElement(null);
        });
    }
    
    @Test
    void testToXmlWithEmptyDocument() {
        // Don't load any XML
        String result = editor.toXml();
        assertEquals("", result);
    }
    
    @Test
    void testCreateDocumentWithNullRootName() {
        assertThrows(IllegalArgumentException.class, () -> {
            editor.createDocument(null);
        });
    }
    
    @Test
    void testCreateDocumentWithEmptyRootName() {
        assertThrows(IllegalArgumentException.class, () -> {
            editor.createDocument("");
        });
    }
    
    @Test
    void testLargeXmlDocument() {
        // Test with a large XML document to ensure no memory issues
        StringBuilder largeXml = new StringBuilder("<root>");
        for (int i = 0; i < 1000; i++) {
            largeXml.append("<element").append(i).append(">content").append(i).append("</element").append(i).append(">");
        }
        largeXml.append("</root>");
        
        assertDoesNotThrow(() -> {
            editor.loadXml(largeXml.toString());
            String result = editor.toXml();
            assertNotNull(result);
            assertTrue(result.length() > 0);
        });
    }
    
    @Test
    void testDeeplyNestedXml() {
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
        
        assertDoesNotThrow(() -> {
            editor.loadXml(nestedXml.toString());
            String result = editor.toXml();
            assertNotNull(result);
            assertTrue(result.contains("content"));
        });
    }
    
    @Test
    void testXmlWithManyAttributes() {
        // Test element with many attributes
        StringBuilder xmlWithManyAttrs = new StringBuilder("<root");
        for (int i = 0; i < 50; i++) {
            xmlWithManyAttrs.append(" attr").append(i).append("=\"value").append(i).append("\"");
        }
        xmlWithManyAttrs.append("/>");
        
        assertDoesNotThrow(() -> {
            editor.loadXml(xmlWithManyAttrs.toString());
            Element root = editor.getRootElement();
            assertEquals("value0", root.getAttribute("attr0"));
            assertEquals("value49", root.getAttribute("attr49"));
        });
    }
    
    @Test
    void testSpecialCharactersInElementNames() {
        // Test with valid but unusual element names
        String xml = "<root><element-with-dashes/><element_with_underscores/><element.with.dots/></root>";
        
        assertDoesNotThrow(() -> {
            editor.loadXml(xml);
            String result = editor.toXml();
            assertTrue(result.contains("element-with-dashes"));
            assertTrue(result.contains("element_with_underscores"));
            assertTrue(result.contains("element.with.dots"));
        });
    }
    
    @Test
    void testSpecialCharactersInAttributeNames() {
        // Test with valid but unusual attribute names
        String xml = "<root attr-dash=\"value1\" attr_underscore=\"value2\" attr.dot=\"value3\"/>";
        
        assertDoesNotThrow(() -> {
            editor.loadXml(xml);
            Element root = editor.getRootElement();
            assertEquals("value1", root.getAttribute("attr-dash"));
            assertEquals("value2", root.getAttribute("attr_underscore"));
            assertEquals("value3", root.getAttribute("attr.dot"));
        });
    }
}
