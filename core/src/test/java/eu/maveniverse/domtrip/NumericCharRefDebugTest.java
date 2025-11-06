package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class NumericCharRefDebugTest {

    @Test
    void debugNumericCharacterReferences() {
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
    void debugHexNumericCharacterReferences() {
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
}
