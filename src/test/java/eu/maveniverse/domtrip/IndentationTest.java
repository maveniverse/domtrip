package eu.maveniverse.domtrip;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for indentation and whitespace preservation.
 */
public class IndentationTest {
    
    private Editor editor;
    
    @BeforeEach
    void setUp() {
        editor = new Editor();
    }
    
    @Test
    void testIndentationInference() {
        String xml = "<root>\n" +
                   "    <existing>content</existing>\n" +
                   "</root>";
        
        editor.loadXml(xml);
        Element root = editor.getRootElement();
        editor.addElement(root, "newElement", "new content");
        
        String result = editor.toXml();
        
        // New element should be indented with 4 spaces like existing element
        assertTrue(result.contains("    <existing>content</existing>"));
        assertTrue(result.contains("    <newElement>new content</newElement>"));
    }
    
    @Test
    void testTabIndentation() {
        String xml = "<root>\n" +
                   "\t<existing>content</existing>\n" +
                   "</root>";
        
        editor.loadXml(xml);
        Element root = editor.getRootElement();
        editor.addElement(root, "newElement", "new content");
        
        String result = editor.toXml();
        
        // New element should be indented with tabs like existing element
        assertTrue(result.contains("\t<existing>content</existing>"));
        assertTrue(result.contains("\t<newElement>new content</newElement>"));
    }
    
    @Test
    void testNestedIndentation() {
        String xml = "<root>\n" +
                   "  <parent>\n" +
                   "    <child>content</child>\n" +
                   "  </parent>\n" +
                   "</root>";
        
        editor.loadXml(xml);
        Element parent = editor.findElement("parent");
        editor.addElement(parent, "newChild", "new content");
        
        String result = editor.toXml();
        
        // New child should be indented at the same level as existing child
        assertTrue(result.contains("    <child>content</child>"));
        assertTrue(result.contains("    <newChild>new content</newChild>"));
    }
    
    @Test
    void testMixedWhitespace() {
        String xml = "<root>\n" +
                   "  <element1>content1</element1>\n" +
                   "\n" +
                   "  <element2>content2</element2>\n" +
                   "</root>";
        
        editor.loadXml(xml);
        Element root = editor.getRootElement();
        editor.addElement(root, "element3", "content3");
        
        String result = editor.toXml();
        
        // Should preserve existing whitespace patterns
        assertTrue(result.contains("  <element1>content1</element1>"));
        assertTrue(result.contains("  <element2>content2</element2>"));
        assertTrue(result.contains("  <element3>content3</element3>"));
    }
    
    @Test
    void testCommentIndentation() {
        String xml = "<root>\n" +
                   "  <element>content</element>\n" +
                   "</root>";
        
        editor.loadXml(xml);
        Element root = editor.getRootElement();
        editor.addComment(root, "This is a comment");
        
        String result = editor.toXml();
        
        // Comment should be indented like other children
        assertTrue(result.contains("  <element>content</element>"));
        assertTrue(result.contains("  <!--This is a comment-->"));
    }
    
    @Test
    void testEmptyElementIndentation() {
        String xml = "<root>\n" +
                   "  <existing/>\n" +
                   "</root>";
        
        editor.loadXml(xml);
        Element root = editor.getRootElement();
        Element newElement = editor.addElement(root, "newEmpty");
        
        String result = editor.toXml();
        
        // Both empty elements should be properly indented
        assertTrue(result.contains("  <existing/>"));
        assertTrue(result.contains("  <newEmpty></newEmpty>"));
    }
    
    @Test
    void testDocumentCreationIndentation() {
        editor.createDocument("root");
        Element root = editor.getRootElement();
        
        editor.addElement(root, "child1", "content1");
        editor.addElement(root, "child2", "content2");
        
        String result = editor.toXml();
        
        // Should have proper indentation even for created documents
        assertTrue(result.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(result.contains("<root>"));
        assertTrue(result.contains("</root>"));
        
        // Children should be indented (default 2 spaces)
        String[] lines = result.split("\n");
        boolean foundChild1 = false;
        boolean foundChild2 = false;
        
        for (String line : lines) {
            if (line.contains("<child1>")) {
                assertTrue(line.startsWith("  "), "child1 should be indented with 2 spaces");
                foundChild1 = true;
            }
            if (line.contains("<child2>")) {
                assertTrue(line.startsWith("  "), "child2 should be indented with 2 spaces");
                foundChild2 = true;
            }
        }
        
        assertTrue(foundChild1, "Should find child1 element");
        assertTrue(foundChild2, "Should find child2 element");
    }
}
