package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class NumericCharRefDebugTest {

    @Test
    void debugNumericCharacterReferences() throws DomTripException {
        String xml = "<root attr=\"line1&#10;line2\"/>";
        Document doc = Document.of(xml);

        Element root = doc.root();
        Attribute attrObj = root.attributeObject("attr");
        String result = doc.toXml();

        // The attribute value should be decoded
        assertTrue(root.attribute("attr").contains("\n"), "Should contain newline");

        // The raw value should be preserved
        assertEquals("line1&#10;line2", attrObj.rawValue(), "Raw value should preserve &#10;");

        // The output should use raw value when not modified
        assertTrue(result.contains("&#10;"), "Output should preserve &#10; from raw value");
    }

    @Test
    void debugHexNumericCharacterReferences() throws DomTripException {
        String xml = "<root attr=\"&#x3C;test&#x3E;\"/>";
        Document doc = Document.of(xml);

        Element root = doc.root();
        Attribute attrObj = root.attributeObject("attr");
        String result = doc.toXml();

        // The attribute value should be decoded
        assertEquals("<test>", root.attribute("attr"), "Should decode hex refs");

        // The raw value should be preserved
        assertEquals("&#x3C;test&#x3E;", attrObj.rawValue(), "Raw value should preserve &#x3C;");

        // The output should use raw value when not modified
        assertTrue(result.contains("&#x3C;") && result.contains("&#x3E;"), "Output should preserve hex refs");
    }

    @Test
    void invalidXmlCodePointsAreNotDecoded() {
        // Null character (0x0) is not valid in XML
        assertEquals("&#0;", Text.unescapeTextContent("&#0;"), "Null char should not be decoded");

        // Surrogate code point (0xD800) is not valid in XML
        assertEquals("&#xD800;", Text.unescapeTextContent("&#xD800;"), "Surrogate should not be decoded");

        // 0xFFFE is not valid in XML
        assertEquals("&#xFFFE;", Text.unescapeTextContent("&#xFFFE;"), "0xFFFE should not be decoded");

        // 0xFFFF is not valid in XML
        assertEquals("&#xFFFF;", Text.unescapeTextContent("&#xFFFF;"), "0xFFFF should not be decoded");

        // Control char 0x1 is not valid in XML
        assertEquals("&#1;", Text.unescapeTextContent("&#1;"), "Control char should not be decoded");
    }

    @Test
    void validXmlCodePointsAreDecoded() {
        // Tab (0x9) is valid
        assertEquals("\t", Text.unescapeTextContent("&#9;"), "Tab should be decoded");

        // Newline (0xA) is valid
        assertEquals("\n", Text.unescapeTextContent("&#10;"), "Newline should be decoded");

        // Carriage return (0xD) is valid
        assertEquals("\r", Text.unescapeTextContent("&#13;"), "CR should be decoded");

        // Space (0x20) is valid
        assertEquals(" ", Text.unescapeTextContent("&#32;"), "Space should be decoded");

        // Valid hex code points
        assertEquals("A", Text.unescapeTextContent("&#x41;"), "Hex A should be decoded");

        // BMP upper range
        assertEquals("\uD7FF", Text.unescapeTextContent("&#xD7FF;"), "0xD7FF should be decoded");

        // Private use area start
        assertEquals("\uE000", Text.unescapeTextContent("&#xE000;"), "0xE000 should be decoded");
    }

    @Test
    void numericReferenceWithoutSemicolonIsLiteral() {
        // &#65 without semicolon should be treated as literal text
        assertEquals("&#65", Text.unescapeTextContent("&#65"), "Missing semicolon should not decode");
        assertEquals("text&#10more", Text.unescapeTextContent("text&#10more"), "Missing semicolon mid-text");
    }

    @Test
    void doubleEncodedEntitiesAreNotReDecoded() {
        // &#38; decodes to '&', so &#38;lt; should yield "&lt;" not "<"
        assertEquals("&lt;", Text.unescapeTextContent("&#38;lt;"), "Double-encoded lt should not re-decode");
        assertEquals("&gt;", Text.unescapeTextContent("&#38;gt;"), "Double-encoded gt should not re-decode");
        assertEquals("&amp;", Text.unescapeTextContent("&#38;amp;"), "Double-encoded amp should not re-decode");
        // &#x26; is also '&' in hex
        assertEquals("&lt;", Text.unescapeTextContent("&#x26;lt;"), "Hex double-encoded lt should not re-decode");
    }

    @Test
    void namedEntitiesAreDecoded() {
        assertEquals("<", Text.unescapeTextContent("&lt;"));
        assertEquals(">", Text.unescapeTextContent("&gt;"));
        assertEquals("\"", Text.unescapeTextContent("&quot;"));
        assertEquals("'", Text.unescapeTextContent("&apos;"));
        assertEquals("&", Text.unescapeTextContent("&amp;"));
        // Mixed named entities in a single string exercises all fall-through branches
        assertEquals("< > \" ' &", Text.unescapeTextContent("&lt; &gt; &quot; &apos; &amp;"));
    }

    @Test
    void unknownEntityIsPreservedLiteral() {
        // Unknown entity references should be kept as-is
        assertEquals("&foo;", Text.unescapeTextContent("&foo;"));
        assertEquals("& bare", Text.unescapeTextContent("& bare"));
        // Ampersand at end of string
        assertEquals("end&", Text.unescapeTextContent("end&"));
        // Ampersand followed by non-# non-entity
        assertEquals("&z", Text.unescapeTextContent("&z"));
    }

    @Test
    void mixedNumericAndNamedEntities() {
        // Numeric ref followed by named entity in same string
        assertEquals("A<", Text.unescapeTextContent("&#65;&lt;"));
        // Named entity followed by numeric ref
        assertEquals(">B", Text.unescapeTextContent("&gt;&#66;"));
        // Failed numeric ref (invalid) falls through, then named entity on next &
        assertEquals("&#xyz;&", Text.unescapeTextContent("&#xyz;&amp;"));
    }

    @Test
    void plainTextWithoutEntities() {
        assertEquals("", Text.unescapeTextContent(""));
        assertEquals("hello world", Text.unescapeTextContent("hello world"));
        assertNull(null, Text.unescapeTextContent(null));
    }
}
