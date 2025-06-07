package eu.maveniverse.domtrip;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for the Attribute class functionality.
 */
public class AttributeClassTest {
    
    private Attribute attribute;
    
    @BeforeEach
    void setUp() {
        attribute = new Attribute("test", "value");
    }
    
    @Test
    void testBasicAttributeCreation() {
        assertEquals("test", attribute.getName());
        assertEquals("value", attribute.getValue());
        assertEquals('"', attribute.getQuoteChar());
        assertEquals(" ", attribute.getPrecedingWhitespace());
        assertNull(attribute.getRawValue());
    }
    
    @Test
    void testAttributeWithCustomQuote() {
        Attribute attr = new Attribute("name", "value", '\'', "  ");
        
        assertEquals("name", attr.getName());
        assertEquals("value", attr.getValue());
        assertEquals('\'', attr.getQuoteChar());
        assertEquals("  ", attr.getPrecedingWhitespace());
    }
    
    @Test
    void testAttributeWithRawValue() {
        Attribute attr = new Attribute("name", "decoded value", '"', " ", "&quot;encoded&quot;");
        
        assertEquals("name", attr.getName());
        assertEquals("decoded value", attr.getValue());
        assertEquals("&quot;encoded&quot;", attr.getRawValue());
    }
    
    @Test
    void testSetValue() {
        attribute.setValue("new value");
        assertEquals("new value", attribute.getValue());
        assertNull(attribute.getRawValue()); // Should clear raw value
    }
    
    @Test
    void testSetRawValue() {
        attribute.setRawValue("&lt;raw&gt;");
        assertEquals("&lt;raw&gt;", attribute.getRawValue());
    }
    
    @Test
    void testSetQuoteChar() {
        attribute.setQuoteChar('\'');
        assertEquals('\'', attribute.getQuoteChar());
    }
    
    @Test
    void testSetPrecedingWhitespace() {
        attribute.setPrecedingWhitespace("    ");
        assertEquals("    ", attribute.getPrecedingWhitespace());
        
        // Null should default to single space
        attribute.setPrecedingWhitespace(null);
        assertEquals(" ", attribute.getPrecedingWhitespace());
    }
    
    @Test
    void testSerializationValueWithRaw() {
        attribute.setRawValue("&lt;raw&gt;");
        
        // With useRaw=true, should return raw value
        assertEquals("&lt;raw&gt;", attribute.getSerializationValue(true));
        
        // With useRaw=false, should return escaped value
        String escaped = attribute.getSerializationValue(false);
        assertEquals("value", escaped); // "value" doesn't need escaping
    }
    
    @Test
    void testSerializationValueWithoutRaw() {
        // No raw value set
        assertEquals("value", attribute.getSerializationValue(true));
        assertEquals("value", attribute.getSerializationValue(false));
    }
    
    @Test
    void testEscaping() {
        Attribute attr = new Attribute("test", "value with <tags> & \"quotes\"", '"', " ");
        
        String escaped = attr.getSerializationValue(false);
        assertTrue(escaped.contains("&lt;tags&gt;"));
        assertTrue(escaped.contains("&amp;"));
        assertTrue(escaped.contains("&quot;quotes&quot;"));
    }
    
    @Test
    void testEscapingWithSingleQuotes() {
        Attribute attr = new Attribute("test", "value with 'apostrophes' & \"quotes\"", '\'', " ");
        
        String escaped = attr.getSerializationValue(false);
        assertTrue(escaped.contains("&apos;apostrophes&apos;"));
        assertTrue(escaped.contains("&amp;"));
        // Double quotes should not be escaped when using single quotes
        assertTrue(escaped.contains("\"quotes\""));
    }
    
    @Test
    void testToXml() {
        StringBuilder sb = new StringBuilder();
        attribute.toXml(sb, false);
        
        String result = sb.toString();
        assertEquals(" test=\"value\"", result);
    }
    
    @Test
    void testToXmlWithCustomFormatting() {
        Attribute attr = new Attribute("name", "value", '\'', "  ");
        StringBuilder sb = new StringBuilder();
        attr.toXml(sb, false);
        
        String result = sb.toString();
        assertEquals("  name='value'", result);
    }
    
    @Test
    void testToXmlWithRawValue() {
        Attribute attr = new Attribute("name", "decoded", '"', " ", "&quot;raw&quot;");
        StringBuilder sb = new StringBuilder();
        
        // With useRaw=true
        attr.toXml(sb, true);
        assertEquals(" name=\"&quot;raw&quot;\"", sb.toString());
        
        // With useRaw=false
        sb.setLength(0);
        attr.toXml(sb, false);
        assertEquals(" name=\"decoded\"", sb.toString());
    }
    
    @Test
    void testEqualsAndHashCode() {
        Attribute attr1 = new Attribute("name", "value1");
        Attribute attr2 = new Attribute("name", "value2");
        Attribute attr3 = new Attribute("other", "value1");
        
        // Equality is based on name only
        assertEquals(attr1, attr2);
        assertNotEquals(attr1, attr3);
        
        // Hash code should be consistent
        assertEquals(attr1.hashCode(), attr2.hashCode());
    }
    
    @Test
    void testToString() {
        String str = attribute.toString();
        assertTrue(str.contains("test"));
        assertTrue(str.contains("value"));
        assertTrue(str.contains("\""));
    }
}
