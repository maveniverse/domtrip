package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        Element root = editor.documentElement().orElseThrow();

        // Update existing attributes - should preserve quote styles
        root.attribute("attr1", "updated1");
        root.attribute("attr2", "updated2");

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
        Element root = editor.documentElement().orElseThrow();

        // Get original whitespace patterns
        String originalWhitespace1 = root.attributeObject("attr1").precedingWhitespace();
        String originalWhitespace2 = root.attributeObject("attr2").precedingWhitespace();

        // Update existing attributes
        root.attribute("attr1", "updated1");
        root.attribute("attr2", "updated2");

        // Verify whitespace is preserved
        assertEquals(originalWhitespace1, root.attributeObject("attr1").precedingWhitespace());
        assertEquals(originalWhitespace2, root.attributeObject("attr2").precedingWhitespace());
    }

    @Test
    void testSetAttributeWithQuoteCharPreservesWhitespace() {
        // Original XML with custom whitespace
        String xml = "<root   attr1='value1'    attr2=\"value2\"/>";

        editor.loadXml(xml);
        Element root = editor.documentElement().orElseThrow();

        // Get original whitespace patterns
        String originalWhitespace1 = root.attributeObject("attr1").precedingWhitespace();
        String originalWhitespace2 = root.attributeObject("attr2").precedingWhitespace();

        // Update with specific quote characters
        root.attribute("attr1", "updated1", '"'); // Change to double quotes
        root.attribute("attr2", "updated2", '\''); // Change to single quotes

        // Verify whitespace is preserved but quotes are changed
        assertEquals(originalWhitespace1, root.attributeObject("attr1").precedingWhitespace());
        assertEquals(originalWhitespace2, root.attributeObject("attr2").precedingWhitespace());
        assertEquals('"', root.attributeObject("attr1").quoteStyle().getCharacter());
        assertEquals('\'', root.attributeObject("attr2").quoteStyle().getCharacter());
    }

    @Test
    void testNewAttributesUseDefaults() {
        String xml = "<root existing='value'/>";

        editor.loadXml(xml);
        Element root = editor.documentElement().orElseThrow();

        // Add new attributes
        root.attribute("new1", "value1");
        root.attribute("new2", "value2", '\'');

        // Verify new attributes use defaults
        Attribute new1 = root.attributeObject("new1");
        Attribute new2 = root.attributeObject("new2");

        assertEquals('"', new1.quoteStyle().getCharacter(), "New attribute should use default double quotes");
        assertEquals(" ", new1.precedingWhitespace(), "New attribute should use default whitespace");
        assertEquals('\'', new2.quoteStyle().getCharacter(), "New attribute should use specified quote char");
        assertEquals(" ", new2.precedingWhitespace(), "New attribute should use default whitespace");
    }

    @Test
    void testMavenCombineAttributeExample() {
        // Real-world Maven POM example with combine.children attribute
        String xml = "<configuration   combine.children='append'   combine.self=\"override\">\n" + "  <items>\n"
                + "    <item>value</item>\n"
                + "  </items>\n"
                + "</configuration>";

        editor.loadXml(xml);
        Element config = editor.documentElement().orElseThrow();

        // Update combine.children value - should preserve single quotes and whitespace
        config.attribute("combine.children", "merge");

        String result = editor.toXml();

        // Verify formatting is preserved
        assertTrue(result.contains("combine.children='merge'"), "combine.children should preserve single quotes");
        assertTrue(result.contains("combine.self=\"override\""), "combine.self should preserve double quotes");
    }

    @Test
    void testComplexAttributeFormatting() {
        // XML with various formatting patterns
        String xml = "<element\n" + "    attr1=\"value1\"\n" + "  attr2='value2'\n" + "     attr3=\"value3\"/>";

        editor.loadXml(xml);
        Element element = editor.documentElement().orElseThrow();

        // Store original formatting
        String ws1 = element.attributeObject("attr1").precedingWhitespace();
        String ws2 = element.attributeObject("attr2").precedingWhitespace();
        String ws3 = element.attributeObject("attr3").precedingWhitespace();
        char q1 = element.attributeObject("attr1").quoteStyle().getCharacter();
        char q2 = element.attributeObject("attr2").quoteStyle().getCharacter();
        char q3 = element.attributeObject("attr3").quoteStyle().getCharacter();

        // Update all attributes
        element.attribute("attr1", "updated1");
        element.attribute("attr2", "updated2");
        element.attribute("attr3", "updated3");

        // Verify all formatting is preserved
        assertEquals(ws1, element.attributeObject("attr1").precedingWhitespace());
        assertEquals(ws2, element.attributeObject("attr2").precedingWhitespace());
        assertEquals(ws3, element.attributeObject("attr3").precedingWhitespace());
        assertEquals(q1, element.attributeObject("attr1").quoteStyle().getCharacter());
        assertEquals(q2, element.attributeObject("attr2").quoteStyle().getCharacter());
        assertEquals(q3, element.attributeObject("attr3").quoteStyle().getCharacter());
    }

    @Test
    void testRawValueIsCleared() {
        // Create element with raw value
        Element element = new Element("test");
        Attribute attr = new Attribute("attr", "decoded", '"', " ", "&quot;raw&quot;");
        element.attributeObject("attr", attr);

        // Verify raw value exists
        assertNotNull(element.attributeObject("attr").rawValue());

        // Update using setAttribute
        element.attribute("attr", "updated");

        // Verify raw value is cleared (as expected when setting programmatically)
        assertNull(element.attributeObject("attr").rawValue());
        assertEquals("updated", element.attributeObject("attr").value());
    }

    @Test
    void testBackwardCompatibility() {
        // Test that the new behavior doesn't break existing code patterns
        Element element = new Element("test");

        // Setting new attributes should work as before
        element.attribute("attr1", "value1");
        element.attribute("attr2", "value2", '\'');

        assertEquals("value1", element.attribute("attr1"));
        assertEquals("value2", element.attribute("attr2"));
        assertEquals('"', element.attributeQuote("attr1"));
        assertEquals('\'', element.attributeQuote("attr2"));
    }

    @Test
    void testNullAndEmptyValues() {
        String xml = "<root attr='existing'/>";

        editor.loadXml(xml);
        Element root = editor.documentElement().orElseThrow();

        // Test null value
        root.attribute("attr", null);
        assertNull(root.attribute("attr"));
        assertEquals('\'', root.attributeQuote("attr")); // Quote style preserved

        // Test empty value
        root.attribute("attr", "");
        assertEquals("", root.attribute("attr"));
        assertEquals('\'', root.attributeQuote("attr")); // Quote style preserved
    }

    @Test
    void testEditorSetAttributeInfersQuoteStyle() {
        // XML with predominantly single quotes
        String xml = "<root attr1='value1' attr2='value2' attr3=\"value3\"/>";

        editor.loadXml(xml);
        Element root = editor.documentElement().orElseThrow();

        // Add new attribute via Editor - should infer single quotes (majority)
        root.attribute("attr4", "value4");

        assertEquals('\'', root.attributeQuote("attr4"));
        assertEquals("value4", root.attribute("attr4"));
    }

    @Test
    void testEditorSetAttributeInfersDoubleQuotes() {
        // XML with predominantly double quotes
        String xml = "<root attr1=\"value1\" attr2=\"value2\" attr3='value3'/>";

        editor.loadXml(xml);
        Element root = editor.documentElement().orElseThrow();

        // Add new attribute via Editor - should infer double quotes (majority)
        root.attribute("attr4", "value4");

        assertEquals('"', root.attributeQuote("attr4"));
        assertEquals("value4", root.attribute("attr4"));
    }

    @Test
    void testEditorSetAttributeInfersCustomWhitespace() {
        // XML with custom spacing
        String xml = "<root  attr1=\"value1\"   attr2=\"value2\"/>";

        editor.loadXml(xml);
        Element root = editor.documentElement().orElseThrow();

        // Get the custom whitespace pattern from existing attributes
        String existingWhitespace = root.attributeObject("attr2").precedingWhitespace();

        // Verify we're getting some custom whitespace (not just a single space)
        assertTrue(existingWhitespace.length() > 1, "attr2 should have custom whitespace");

        // Add new attribute via Editor - should infer custom spacing
        root.attribute("attr3", "value3");

        String attr3Whitespace = root.attributeObject("attr3").precedingWhitespace();
        assertEquals(existingWhitespace, attr3Whitespace);
    }

    @Test
    void testEditorSetAttributeInfersMultiLineAlignment() {
        // XML with multi-line attribute alignment - exact example from documentation
        String xml = "<element attr1=\"value1\"\n" + "         attr2=\"value2\"/>";

        editor.loadXml(xml);
        Element element = editor.documentElement().orElseThrow();

        // Add new attribute via Editor - should infer alignment pattern
        element.attribute("attr3", "value3");

        String result = editor.toXml();

        // Verify the new attribute follows the alignment pattern
        assertTrue(result.contains("attr1=\"value1\""));
        assertTrue(result.contains("attr2=\"value2\""));
        assertTrue(result.contains("attr3=\"value3\""));

        // Check that attr3 has newline-based whitespace matching the pattern
        String attr3Whitespace = element.attributeObject("attr3").precedingWhitespace();
        assertTrue(attr3Whitespace.contains("\n"), "New attribute should have newline-based whitespace");

        // Verify the alignment is maintained by checking the whitespace pattern
        String attr2Whitespace = element.attributeObject("attr2").precedingWhitespace();
        assertEquals(
                attr2Whitespace,
                attr3Whitespace,
                "New attribute should use the same alignment pattern as existing attributes");

        // Verify the result maintains proper alignment structure
        String[] lines = result.split("\n");
        boolean foundAlignedAttributes = false;
        for (int i = 0; i < lines.length - 1; i++) {
            if (lines[i].contains("attr1=\"value1\"") && lines[i + 1].trim().startsWith("attr2=\"value2\"")) {
                foundAlignedAttributes = true;
                // Check that attr3 follows the same pattern if it exists on the next line
                if (i + 2 < lines.length && lines[i + 2].trim().contains("attr3=\"value3\"")) {
                    // Verify consistent indentation
                    String attr2Indent = lines[i + 1].substring(0, lines[i + 1].indexOf("attr2"));
                    String attr3Indent = lines[i + 2].substring(0, lines[i + 2].indexOf("attr3"));
                    assertEquals(attr2Indent, attr3Indent, "attr3 should have the same indentation as attr2");
                }
                break;
            }
        }
        assertTrue(foundAlignedAttributes, "Should maintain multi-line attribute alignment structure");
    }

    @Test
    void testEditorSetAttributeDocumentationExample() {
        // Exact example from documentation comments
        String xml = "<element attr1=\"value1\"\n" + "         attr2=\"value2\"/>";

        editor.loadXml(xml);
        Element element = editor.documentElement().orElseThrow();

        // This is the exact call from the documentation
        element.attribute("attr3", "value3");

        String result = editor.toXml();

        // The result should maintain alignment pattern automatically
        // Expected: <element attr1="value1"
        //                    attr2="value2"
        //                    attr3="value3"/>

        // Verify all attributes are present
        assertTrue(result.contains("attr1=\"value1\""), "attr1 should be preserved");
        assertTrue(result.contains("attr2=\"value2\""), "attr2 should be preserved");
        assertTrue(result.contains("attr3=\"value3\""), "attr3 should be added");

        // Verify the formatting inference worked
        Attribute attr3 = element.attributeObject("attr3");
        assertNotNull(attr3, "attr3 should exist");

        // Should infer double quotes from existing attributes
        assertEquals('"', attr3.quoteStyle().getCharacter(), "Should infer double quotes from existing attributes");

        // Should infer multi-line alignment pattern
        String attr3Whitespace = attr3.precedingWhitespace();
        assertTrue(attr3Whitespace.contains("\n"), "Should infer newline-based alignment");
        assertTrue(attr3Whitespace.length() > 1, "Should have proper indentation spacing");

        // Verify it matches the pattern of attr2
        String attr2Whitespace = element.attributeObject("attr2").precedingWhitespace();
        assertEquals(attr2Whitespace, attr3Whitespace, "Should use the same alignment pattern as attr2");
    }

    @Test
    void testEditorSetAttributeWithNoExistingAttributes() {
        String xml = "<root/>";

        editor.loadXml(xml);
        Element root = editor.documentElement().orElseThrow();

        // Add attribute to element with no existing attributes
        root.attribute("newAttr", "newValue");

        // Should use defaults
        assertEquals('"', root.attributeQuote("newAttr"));
        assertEquals(" ", root.attributeObject("newAttr").precedingWhitespace());
        assertEquals("newValue", root.attribute("newAttr"));
    }

    @Test
    void testEditorSetAttributePreservesExistingFormatting() {
        String xml = "<root attr1='existing'/>";

        editor.loadXml(xml);
        Element root = editor.documentElement().orElseThrow();

        // Update existing attribute via Editor - should preserve formatting
        root.attribute("attr1", "updated");

        assertEquals('\'', root.attributeQuote("attr1"));
        assertEquals("updated", root.attribute("attr1"));
    }

    @Test
    void testComplexMultiLineAttributeInference() {
        // Complex Maven-style XML with aligned attributes
        String xml = "<plugin>\n" + "  <groupId>org.apache.maven.plugins</groupId>\n"
                + "  <artifactId>maven-compiler-plugin</artifactId>\n"
                + "  <configuration combine.children='append'\n"
                + "               combine.self=\"override\"\n"
                + "               xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>\n"
                + "    <source>17</source>\n"
                + "  </configuration>\n"
                + "</plugin>";

        editor.loadXml(xml);
        Element config = editor.element("configuration").orElseThrow();

        // Add new attribute via Editor - should infer alignment
        config.attribute("newAttr", "newValue");

        String result = editor.toXml();

        // Verify the new attribute follows the alignment pattern
        assertTrue(result.contains("combine.children='append'"));
        assertTrue(result.contains("combine.self=\"override\""));
        assertTrue(result.contains("newAttr="));

        // Check that the new attribute has appropriate whitespace
        String newAttrWhitespace = config.attributeObject("newAttr").precedingWhitespace();
        assertTrue(
                newAttrWhitespace.contains("\n"), "New attribute should have newline-based whitespace for alignment");
    }
}
