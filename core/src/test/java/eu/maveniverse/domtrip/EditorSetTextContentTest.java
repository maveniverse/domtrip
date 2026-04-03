package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Editor#setTextContent(Element, String)} preserving CDATA node type
 * and surrounding whitespace.
 */
class EditorSetTextContentTest {

    @Test
    void testSetTextContent_preservesCDataFlag() throws Exception {
        String xml = "<root>\n  <version><![CDATA[1.0]]></version>\n</root>";
        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        Element version = doc.root().childElement("version").orElseThrow();
        editor.setTextContent(version, "2.0");

        String result = editor.toXml();
        assertTrue(result.contains("<![CDATA[2.0]]>"), "CDATA wrapping should be preserved, got: " + result);
        assertFalse(result.contains("1.0"), "Old content should be replaced");
    }

    @Test
    void testSetTextContent_preservesEmptyCDataFlag() throws Exception {
        String xml = "<root><version><![CDATA[]]></version></root>";
        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        Element version = doc.root().childElement("version").orElseThrow();
        editor.setTextContent(version, "2.0");

        String result = editor.toXml();
        assertTrue(
                result.contains("<version><![CDATA[2.0]]></version>"),
                "Empty CDATA should be preserved, got: " + result);
    }

    @Test
    void testSetTextContent_preservesWhitespaceOnlyCDataFlag() throws Exception {
        String xml = "<root><version><![CDATA[  ]]></version></root>";
        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        Element version = doc.root().childElement("version").orElseThrow();
        editor.setTextContent(version, "2.0");

        String result = editor.toXml();
        assertTrue(result.contains("<![CDATA["), "Whitespace-only CDATA should be preserved, got: " + result);
        assertTrue(result.contains("2.0"), "New content should be present");
    }

    @Test
    void testSetTextContent_plainTextRemainsPlain() throws Exception {
        String xml = "<root>\n  <version>1.0</version>\n</root>";
        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        Element version = doc.root().childElement("version").orElseThrow();
        editor.setTextContent(version, "2.0");

        String result = editor.toXml();
        assertTrue(result.contains("<version>2.0</version>"), "Plain text should remain plain, got: " + result);
        assertFalse(result.contains("CDATA"), "Should not introduce CDATA");
    }

    @Test
    void testSetTextContent_preservesWhitespaceAroundCData() throws Exception {
        String xml = "<root>\n  <description><![CDATA[  Hello World  ]]></description>\n</root>";
        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        Element desc = doc.root().childElement("description").orElseThrow();
        editor.setTextContent(desc, "Goodbye");

        String result = editor.toXml();
        assertTrue(
                result.contains("<![CDATA[  Goodbye  ]]>"),
                "Surrounding whitespace inside CDATA should be preserved, got: " + result);
    }

    @Test
    void testSetTextContent_preservesWhitespaceAroundPlainText() throws Exception {
        String xml = "<root>\n  <description>  Hello World  </description>\n</root>";
        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        Element desc = doc.root().childElement("description").orElseThrow();
        editor.setTextContent(desc, "Goodbye");

        String result = editor.toXml();
        assertTrue(
                result.contains("<description>  Goodbye  </description>"),
                "Surrounding whitespace should be preserved, got: " + result);
    }

    @Test
    void testSetTextContent_nullContentClearsAllText() throws Exception {
        String xml = "<root>\n  <version><![CDATA[1.0]]></version>\n</root>";
        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        Element version = doc.root().childElement("version").orElseThrow();
        editor.setTextContent(version, null);

        assertEquals("", version.textContent());
    }

    @Test
    void testSetTextContent_emptyContentClearsAllText() throws Exception {
        String xml = "<root>\n  <version><![CDATA[1.0]]></version>\n</root>";
        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        Element version = doc.root().childElement("version").orElseThrow();
        editor.setTextContent(version, "");

        assertEquals("", version.textContent());
    }

    @Test
    void testSetTextContent_noExistingTextCreatesPlainNode() throws Exception {
        String xml = "<root>\n  <version/>\n</root>";
        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        Element version = doc.root().childElement("version").orElseThrow();
        editor.setTextContent(version, "1.0");

        String result = editor.toXml();
        assertTrue(result.contains("1.0"), "Content should be set");
        assertFalse(result.contains("CDATA"), "Should not introduce CDATA for new content");
    }

    @Test
    void testSetTextContent_mixedContentKeepsFirstNonWhitespace() throws Exception {
        // Create element with multiple non-whitespace text nodes
        Element element = Element.of("item");
        element.addChild(new Text("first"));
        element.addChild(new Text("second"));
        element.addChild(new Text("third"));

        Document doc = Document.withRootElement("root");
        doc.root().addChild(element);
        Editor editor = new Editor(doc);

        editor.setTextContent(element, "replacement");

        assertEquals("replacement", element.textContent());
        // Should have only one text child remaining
        long textChildCount = element.children().filter(Text.class::isInstance).count();
        assertEquals(1, textChildCount, "Should have exactly one text child after mixed content cleanup");
    }

    @Test
    void testSetTextContent_preservesWhitespaceOnlyTextNodes() throws Exception {
        // Element with whitespace text + CDATA + whitespace text
        Element element = Element.of("item");
        element.addChild(new Text("\n  "));
        element.addChild(new Text("value", true)); // CDATA
        element.addChild(new Text("\n"));

        Document doc = Document.withRootElement("root");
        doc.root().addChild(element);
        Editor editor = new Editor(doc);

        editor.setTextContent(element, "new-value");

        // The CDATA node should be updated, whitespace nodes should be preserved
        long textChildCount = element.children().filter(Text.class::isInstance).count();
        assertEquals(3, textChildCount, "Whitespace-only text nodes should be preserved");

        // Verify the CDATA node was updated
        Text cdataNode = element.children()
                .filter(Text.class::isInstance)
                .map(Text.class::cast)
                .filter(t -> !t.isWhitespaceOnly())
                .findFirst()
                .orElseThrow();
        assertTrue(cdataNode.cdata(), "CDATA flag should be preserved");
        assertEquals("new-value", cdataNode.trimmedContent());
    }

    @Test
    void testSetTextContent_nullElementThrowsException() {
        Editor editor = new Editor();
        assertThrows(DomTripException.class, () -> editor.setTextContent(null, "value"));
    }

    @Test
    void testSetTextContent_roundTripWithParsedCData() throws Exception {
        // Full round-trip test simulating the Maven Release Plugin use case
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<project>\n"
                + "  <groupId>org.example</groupId>\n"
                + "  <artifactId>my-app</artifactId>\n"
                + "  <version><![CDATA[1.0-SNAPSHOT]]></version>\n"
                + "</project>\n";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        Element version = doc.root().childElement("version").orElseThrow();
        editor.setTextContent(version, "1.0");

        String result = editor.toXml();
        assertTrue(
                result.contains("<version><![CDATA[1.0]]></version>"),
                "CDATA should be preserved during version update, got: " + result);
        // Other elements should remain unchanged
        assertTrue(result.contains("<groupId>org.example</groupId>"));
        assertTrue(result.contains("<artifactId>my-app</artifactId>"));
    }

    @Test
    void testSetTextContent_roundTripWithPlainText() throws Exception {
        // Verify plain text elements are not affected
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<project>\n"
                + "  <groupId>org.example</groupId>\n"
                + "  <artifactId>my-app</artifactId>\n"
                + "  <version>1.0-SNAPSHOT</version>\n"
                + "</project>\n";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        Element version = doc.root().childElement("version").orElseThrow();
        editor.setTextContent(version, "1.0");

        String result = editor.toXml();
        assertTrue(result.contains("<version>1.0</version>"), "Plain text should remain plain, got: " + result);
        assertFalse(result.contains("CDATA"));
    }
}
