package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class NumericCharRefDebugTest {

    @Test
    void debugNumericCharacterReferences() {
        String xml = "<root attr=\"line1&#10;line2\"/>";
        Document doc = Document.of(xml);

        Element root = doc.root();
        System.out.println("Is modified: " + root.isModified());
        System.out.println("Attribute value: [" + root.attribute("attr") + "]");

        Attribute attrObj = root.attributeObject("attr");
        System.out.println("Attribute object: " + attrObj);
        System.out.println("Raw value: [" + attrObj.rawValue() + "]");
        System.out.println("Decoded value: [" + attrObj.value() + "]");

        String result = doc.toXml();
        System.out.println("Result: [" + result + "]");
        System.out.println("Contains &#10;: " + result.contains("&#10;"));
        System.out.println("Contains newline: " + result.contains("\n"));

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

        System.out.println("Hex - Raw value: [" + attrObj.rawValue() + "]");
        System.out.println("Hex - Decoded value: [" + attrObj.value() + "]");

        String result = doc.toXml();
        System.out.println("Hex - Result: [" + result + "]");
        System.out.println("Hex - Contains &#x3C;: " + result.contains("&#x3C;"));

        // The attribute value should be decoded
        assertEquals("<test>", root.attribute("attr"), "Should decode hex refs");

        // The raw value should be preserved
        assertEquals("&#x3C;test&#x3E;", attrObj.rawValue(), "Raw value should preserve &#x3C;");

        // The output should use raw value when not modified
        assertTrue(result.contains("&#x3C;") && result.contains("&#x3E;"), "Output should preserve hex refs");
    }
}
