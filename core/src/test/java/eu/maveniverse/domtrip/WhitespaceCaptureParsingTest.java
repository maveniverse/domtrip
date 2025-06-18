package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Test whitespace capture during parsing.
 */
class WhitespaceCaptureParsingTest {

    @Test
    void testOpenTagWhitespaceCapture() {
        String xml = "<element   attr='value'   >content</element>";

        Document doc = Document.of(xml);
        Element element = doc.root();

        System.out.println("=== Open Tag Whitespace Capture Test ===");
        System.out.println("XML: " + xml);
        System.out.println("openTagWhitespace: '" + element.openTagWhitespace() + "'");
        System.out.println("Expected: '   ' (3 spaces)");

        assertEquals("   ", element.openTagWhitespace(), "Should capture whitespace before closing >");
    }

    @Test
    void testCloseTagWhitespaceCapture() {
        String xml = "<element>content</   element   >";

        Document doc = Document.of(xml);
        Element element = doc.root();

        System.out.println("\n=== Close Tag Whitespace Capture Test ===");
        System.out.println("XML: " + xml);
        System.out.println("closeTagWhitespace: '" + element.closeTagWhitespace() + "'");
        System.out.println("Expected: '   ' (3 spaces)");

        assertEquals(
                "   ", element.closeTagWhitespace(), "Should capture whitespace before element name in closing tag");
    }

    @Test
    void testNodePrecedingWhitespaceCapture() {
        String xml = "<root>\n    <child>content</child>\n</root>";

        Document doc = Document.of(xml);
        Element root = doc.root();
        Element child = root.child("child").orElseThrow();

        System.out.println("\n=== Node Preceding Whitespace Capture Test ===");
        System.out.println("XML: " + xml);
        System.out.println("child precedingWhitespace: '" + child.precedingWhitespace() + "'");
        System.out.println("Expected: '\\n    ' (newline + 4 spaces)");

        assertEquals("\n    ", child.precedingWhitespace(), "Should capture preceding whitespace");
    }

    @Test
    void testNodeFollowingWhitespaceCapture() {
        String xml = "<root>\n    <child>content</child>\n</root>";

        Document doc = Document.of(xml);
        Element root = doc.root();
        Element child = root.child("child").orElseThrow();

        System.out.println("\n=== Node Following Whitespace Capture Test ===");
        System.out.println("XML: " + xml);
        System.out.println("Expected: '\\n' (newline)");

        assertEquals("\n", root.innerPrecedingWhitespace(), "Should capture following whitespace");
    }

    @Test
    void testComplexWhitespaceCapture() {
        String xml = "<root>\n  <parent   attr='value'   >\n    <child>content</   child   >\n  </parent>\n</root>";

        Document doc = Document.of(xml);
        Element root = doc.root();
        Element parent = root.child("parent").orElseThrow();
        Element child = parent.child("child").orElseThrow();

        System.out.println("\n=== Complex Whitespace Capture Test ===");
        System.out.println("XML: " + xml);

        System.out.println("parent precedingWhitespace: '" + parent.precedingWhitespace() + "'");
        System.out.println("parent openTagWhitespace: '" + parent.openTagWhitespace() + "'");

        System.out.println("child precedingWhitespace: '" + child.precedingWhitespace() + "'");
        System.out.println("child closeTagWhitespace: '" + child.closeTagWhitespace() + "'");

        // Test some of the captured whitespace
        assertEquals("\n  ", parent.precedingWhitespace(), "Parent should have preceding whitespace");
        assertEquals("   ", parent.openTagWhitespace(), "Parent should have open tag whitespace");
        assertEquals("\n    ", child.precedingWhitespace(), "Child should have preceding whitespace");
        assertEquals("   ", child.closeTagWhitespace(), "Child should have close tag whitespace");
    }

    @Test
    void testWhitespacePreservationAfterCapture() {
        String xml = "<root>\n  <element   attr='value'   >content</   element   >\n</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        System.out.println("\n=== Whitespace Preservation After Capture Test ===");
        System.out.println("Original XML: " + xml);

        String result = editor.toXml();
        System.out.println("Serialized XML: " + result);

        assertEquals(xml, result, "Should preserve all whitespace perfectly");
    }
}
