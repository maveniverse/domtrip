package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Test whitespace capture during parsing.
 */
class WhitespaceCaptureParsingTest {

    @Test
    void testOpenTagWhitespaceCapture() throws DomTripException {
        String xml = "<element   attr='value'   >content</element>";

        Document doc = Document.of(xml);
        Element element = doc.root();

        assertEquals("   ", element.openTagWhitespace(), "Should capture whitespace before closing >");
    }

    @Test
    void testCloseTagWhitespaceCapture() throws DomTripException {
        String xml = "<element>content</   element   >";

        Document doc = Document.of(xml);
        Element element = doc.root();

        assertEquals(
                "   ", element.closeTagWhitespace(), "Should capture whitespace before element name in closing tag");
    }

    @Test
    void testNodePrecedingWhitespaceCapture() throws DomTripException {
        String xml = "<root>\n    <child>content</child>\n</root>";

        Document doc = Document.of(xml);
        Element root = doc.root();
        Element child = root.childElement("child").orElseThrow();

        assertEquals("\n    ", child.precedingWhitespace(), "Should capture preceding whitespace");
    }

    @Test
    void testNodeFollowingWhitespaceCapture() throws DomTripException {
        String xml = "<root>\n    <child>content</child>\n</root>";

        Document doc = Document.of(xml);
        Element root = doc.root();
        Element child = root.childElement("child").orElseThrow();

        assertEquals("\n", root.innerPrecedingWhitespace(), "Should capture following whitespace");
    }

    @Test
    void testComplexWhitespaceCapture() throws DomTripException {
        String xml = "<root>\n  <parent   attr='value'   >\n    <child>content</   child   >\n  </parent>\n</root>";

        Document doc = Document.of(xml);
        Element root = doc.root();
        Element parent = root.childElement("parent").orElseThrow();
        Element child = parent.childElement("child").orElseThrow();

        // Test some of the captured whitespace
        assertEquals("\n  ", parent.precedingWhitespace(), "Parent should have preceding whitespace");
        assertEquals("   ", parent.openTagWhitespace(), "Parent should have open tag whitespace");
        assertEquals("\n    ", child.precedingWhitespace(), "Child should have preceding whitespace");
        assertEquals("   ", child.closeTagWhitespace(), "Child should have close tag whitespace");
    }

    @Test
    void testWhitespacePreservationAfterCapture() throws DomTripException {
        String xml = "<root>\n  <element   attr='value'   >content</   element   >\n</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        String result = editor.toXml();

        assertEquals(xml, result, "Should preserve all whitespace perfectly");
    }
}
