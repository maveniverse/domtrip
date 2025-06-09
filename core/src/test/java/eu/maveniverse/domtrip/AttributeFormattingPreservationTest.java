package eu.maveniverse.domtrip;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for attribute formatting preservation in setAttribute methods.
 * 
 * <p>These tests verify that setAttribute preserves existing attribute formatting
 * (quote style, whitespace) when updating existing attributes, while using
 * sensible defaults for new attributes.</p>
 */
public class AttributeFormattingPreservationTest {

    private Editor editor;

    @BeforeEach
    void setUp() {
        editor = new Editor();
    }

    @Test
    void testSetAttributePreservesQuoteStyle() {
        // Original XML with mixed quote styles
        String xml = "<root attr1='single' attr2=\"double\"/>";
        
        editor.loadXml(xml);
        Element root = editor.getDocumentElement();
        
        // Update existing attributes - should preserve quote styles
        root.setAttribute("attr1", "updated1");
        root.setAttribute("attr2", "updated2");
        
        String result = editor.toXml();
        
        // Verify quote styles are preserved
        assertTrue(result.contains("attr1='updated1'"), "Single quotes should be preserved");
        assertTrue(result.contains("attr2=\"updated2\""), "Double quotes should be preserved");
    }

    @Test
    void testSetAttributePreservesWhitespace() {
        // Original XML with custom whitespace
        String xml = "<root  attr1=\"value1\"   attr2=\"value2\"/>";
        
        editor.loadXml(xml);
        Element root = editor.getDocumentElement();
        
        // Get original whitespace patterns
        String originalWhitespace1 = root.getAttributeObject("attr1").getPrecedingWhitespace();
        String originalWhitespace2 = root.getAttributeObject("attr2").getPrecedingWhitespace();
        
        // Update existing attributes
        root.setAttribute("attr1", "updated1");
        root.setAttribute("attr2", "updated2");
        
        // Verify whitespace is preserved
        assertEquals(originalWhitespace1, root.getAttributeObject("attr1").getPrecedingWhitespace());
        assertEquals(originalWhitespace2, root.getAttributeObject("attr2").getPrecedingWhitespace());
    }

    @Test
    void testSetAttributeWithQuoteCharPreservesWhitespace() {
        // Original XML with custom whitespace
        String xml = "<root   attr1='value1'    attr2=\"value2\"/>";
        
        editor.loadXml(xml);
        Element root = editor.getDocumentElement();
        
        // Get original whitespace patterns
        String originalWhitespace1 = root.getAttributeObject("attr1").getPrecedingWhitespace();
        String originalWhitespace2 = root.getAttributeObject("attr2").getPrecedingWhitespace();
        
        // Update with specific quote characters
        root.setAttribute("attr1", "updated1", '"');  // Change to double quotes
        root.setAttribute("attr2", "updated2", '\''); // Change to single quotes
        
        // Verify whitespace is preserved but quotes are changed
        assertEquals(originalWhitespace1, root.getAttributeObject("attr1").getPrecedingWhitespace());
        assertEquals(originalWhitespace2, root.getAttributeObject("attr2").getPrecedingWhitespace());
        assertEquals('"', root.getAttributeObject("attr1").getQuoteChar());
        assertEquals('\'', root.getAttributeObject("attr2").getQuoteChar());
    }

    @Test
    void testNewAttributesUseDefaults() {
        String xml = "<root existing='value'/>";
        
        editor.loadXml(xml);
        Element root = editor.getDocumentElement();
        
        // Add new attributes
        root.setAttribute("new1", "value1");
        root.setAttribute("new2", "value2", '\'');
        
        // Verify new attributes use defaults
        Attribute new1 = root.getAttributeObject("new1");
        Attribute new2 = root.getAttributeObject("new2");
        
        assertEquals('"', new1.getQuoteChar(), "New attribute should use default double quotes");
        assertEquals(" ", new1.getPrecedingWhitespace(), "New attribute should use default whitespace");
        assertEquals('\'', new2.getQuoteChar(), "New attribute should use specified quote char");
        assertEquals(" ", new2.getPrecedingWhitespace(), "New attribute should use default whitespace");
    }

    @Test
    void testMavenCombineAttributeExample() {
        // Real-world Maven POM example with combine.children attribute
        String xml = "<configuration   combine.children='append'   combine.self=\"override\">\n" +
                    "  <items>\n" +
                    "    <item>value</item>\n" +
                    "  </items>\n" +
                    "</configuration>";
        
        editor.loadXml(xml);
        Element config = editor.getDocumentElement();
        
        // Update combine.children value - should preserve single quotes and whitespace
        config.setAttribute("combine.children", "merge");
        
        String result = editor.toXml();
        
        // Verify formatting is preserved
        assertTrue(result.contains("combine.children='merge'"), 
                  "combine.children should preserve single quotes");
        assertTrue(result.contains("combine.self=\"override\""), 
                  "combine.self should preserve double quotes");
    }

    @Test
    void testComplexAttributeFormatting() {
        // XML with various formatting patterns
        String xml = "<element\n" +
                    "    attr1=\"value1\"\n" +
                    "  attr2='value2'\n" +
                    "     attr3=\"value3\"/>";
        
        editor.loadXml(xml);
        Element element = editor.getDocumentElement();
        
        // Store original formatting
        String ws1 = element.getAttributeObject("attr1").getPrecedingWhitespace();
        String ws2 = element.getAttributeObject("attr2").getPrecedingWhitespace();
        String ws3 = element.getAttributeObject("attr3").getPrecedingWhitespace();
        char q1 = element.getAttributeObject("attr1").getQuoteChar();
        char q2 = element.getAttributeObject("attr2").getQuoteChar();
        char q3 = element.getAttributeObject("attr3").getQuoteChar();
        
        // Update all attributes
        element.setAttribute("attr1", "updated1");
        element.setAttribute("attr2", "updated2");
        element.setAttribute("attr3", "updated3");
        
        // Verify all formatting is preserved
        assertEquals(ws1, element.getAttributeObject("attr1").getPrecedingWhitespace());
        assertEquals(ws2, element.getAttributeObject("attr2").getPrecedingWhitespace());
        assertEquals(ws3, element.getAttributeObject("attr3").getPrecedingWhitespace());
        assertEquals(q1, element.getAttributeObject("attr1").getQuoteChar());
        assertEquals(q2, element.getAttributeObject("attr2").getQuoteChar());
        assertEquals(q3, element.getAttributeObject("attr3").getQuoteChar());
    }

    @Test
    void testRawValueIsCleared() {
        // Create element with raw value
        Element element = new Element("test");
        Attribute attr = new Attribute("attr", "decoded", '"', " ", "&quot;raw&quot;");
        element.setAttributeObject("attr", attr);
        
        // Verify raw value exists
        assertNotNull(element.getAttributeObject("attr").getRawValue());
        
        // Update using setAttribute
        element.setAttribute("attr", "updated");
        
        // Verify raw value is cleared (as expected when setting programmatically)
        assertNull(element.getAttributeObject("attr").getRawValue());
        assertEquals("updated", element.getAttributeObject("attr").getValue());
    }

    @Test
    void testBackwardCompatibility() {
        // Test that the new behavior doesn't break existing code patterns
        Element element = new Element("test");
        
        // Setting new attributes should work as before
        element.setAttribute("attr1", "value1");
        element.setAttribute("attr2", "value2", '\'');
        
        assertEquals("value1", element.getAttribute("attr1"));
        assertEquals("value2", element.getAttribute("attr2"));
        assertEquals('"', element.getAttributeQuote("attr1"));
        assertEquals('\'', element.getAttributeQuote("attr2"));
    }

    @Test
    void testNullAndEmptyValues() {
        String xml = "<root attr='existing'/>";
        
        editor.loadXml(xml);
        Element root = editor.getDocumentElement();
        
        // Test null value
        root.setAttribute("attr", null);
        assertNull(root.getAttribute("attr"));
        assertEquals('\'', root.getAttributeQuote("attr")); // Quote style preserved
        
        // Test empty value
        root.setAttribute("attr", "");
        assertEquals("", root.getAttribute("attr"));
        assertEquals('\'', root.getAttributeQuote("attr")); // Quote style preserved
    }
}
