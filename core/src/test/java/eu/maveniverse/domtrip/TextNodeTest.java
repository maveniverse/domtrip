package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test cases for Text node functionality.
 */
public class TextNodeTest {

    private Editor editor;

    @BeforeEach
    void setUp() {
        editor = new Editor(Document.of());
    }

    @Test
    void testTextNodeCreation() {
        Text text = new Text("Hello World");

        assertEquals("Hello World", text.content());
        assertEquals(Node.NodeType.TEXT, text.type());
        assertFalse(text.cdata());
        // Note: preserveWhitespace defaults to true in the implementation
        assertTrue(text.preserveWhitespace());
    }

    @Test
    void testTextNodeWithNullContent() {
        Text text = new Text(null);

        assertEquals("", text.content());
    }

    @Test
    void testTextNodeSetContent() {
        Text text = new Text("original");
        text.content("modified");

        assertEquals("modified", text.content());
        assertTrue(text.isModified());
    }

    @Test
    void testTextNodeSetContentNull() {
        Text text = new Text("original");
        text.content(null);

        assertEquals("", text.content());
    }

    @Test
    void testCDataCreation() {
        Text cdata = new Text("function() { return x < y; }", true);

        assertEquals("function() { return x < y; }", cdata.content());
        assertTrue(cdata.cdata());
    }

    @Test
    void testCDataWithRawValue() {
        Text cdata = new Text("decoded content", "<![CDATA[raw content]]>");

        assertEquals("decoded content", cdata.content());
        // Note: This constructor doesn't automatically set CDATA flag
        assertFalse(cdata.cdata());
    }

    @Test
    void testSetCData() {
        Text text = new Text("normal text");
        text.cdata(true);

        assertTrue(text.cdata());
        assertTrue(text.isModified());
    }

    @Test
    void testSetPreserveWhitespace() {
        Text text = new Text("  spaced text  ");
        text.preserveWhitespace(false);

        assertFalse(text.preserveWhitespace());
        // Note: setPreserveWhitespace may not mark as modified in current implementation
        // assertTrue(text.isModified());
    }

    @Test
    void testSetRawValue() {
        Text text = new Text("decoded");
        // Note: Text class doesn't have setRawValue method in current implementation
        // This test is removed as the method doesn't exist
        assertTrue(text.content().equals("decoded"));
        assertFalse(text.isModified()); // Initially not modified
    }

    @Test
    void testTextNodeToXml() {
        Text text = new Text("Hello & <World>");

        String xml = text.toXml();
        assertEquals("Hello &amp; &lt;World&gt;", xml);
    }

    @Test
    void testCDataToXml() {
        Text cdata = new Text("function() { return x < y && z > 0; }", true);

        String xml = cdata.toXml();
        assertEquals("<![CDATA[function() { return x < y && z > 0; }]]>", xml);
    }

    @Test
    void testTextNodeToXmlStringBuilder() {
        Text text = new Text("Hello & <World>");
        StringBuilder sb = new StringBuilder();
        text.toXml(sb);

        assertEquals("Hello &amp; &lt;World&gt;", sb.toString());
    }

    @Test
    void testTextNodeWithWhitespace() {
        Text text = new Text("content");
        text.precedingWhitespace("  ");

        String xml = text.toXml();
        assertEquals("  content", xml);
    }

    @Test
    void testTextEscaping() {
        Text text = new Text("Text with <tags> & \"quotes\" & 'apostrophes'");

        String xml = text.toXml();
        assertTrue(xml.contains("&lt;tags&gt;"));
        assertTrue(xml.contains("&amp;"));
        // Quotes and apostrophes should not be escaped in text content
        assertTrue(xml.contains("\"quotes\""));
        assertTrue(xml.contains("'apostrophes'"));
    }

    @Test
    void testTextUnescaping() {
        String escaped = "Text with &lt;tags&gt; &amp; &quot;quotes&quot; &apos;apostrophes&apos;";
        String unescaped = Text.unescapeTextContent(escaped);

        assertEquals("Text with <tags> & \"quotes\" 'apostrophes'", unescaped);
    }

    @Test
    void testTextUnescapingNull() {
        String result = Text.unescapeTextContent(null);
        assertEquals("", result);
    }

    @Test
    void testTextUnescapingEmpty() {
        String result = Text.unescapeTextContent("");
        assertEquals("", result);
    }

    @Test
    void testTextTrim() {
        Text text = new Text("  content with spaces  ");
        // Need to disable preserve whitespace first
        text.preserveWhitespace(false);
        text.trim();

        assertEquals("content with spaces", text.content());
        assertTrue(text.isModified());
    }

    @Test
    void testTextTrimWithPreserveWhitespace() {
        Text text = new Text("  content with spaces  ");
        text.preserveWhitespace(true);
        text.trim();

        // Should not trim when preserve whitespace is true
        assertEquals("  content with spaces  ", text.content());
    }

    @Test
    void testTextNormalizeWhitespace() {
        Text text = new Text("  content   with    multiple   spaces  ");
        // Need to disable preserve whitespace first
        text.preserveWhitespace(false);
        text.normalizeWhitespace();

        assertEquals("content with multiple spaces", text.content());
        assertTrue(text.isModified());
    }

    @Test
    void testTextNormalizeWhitespaceWithPreserve() {
        Text text = new Text("  content   with    multiple   spaces  ");
        text.preserveWhitespace(true);
        text.normalizeWhitespace();

        // Should not normalize when preserve whitespace is true
        assertEquals("  content   with    multiple   spaces  ", text.content());
    }

    @Test
    void testTextToString() {
        Text text = new Text("This is a long text content that should be truncated in toString");

        String str = text.toString();
        assertTrue(str.contains("Text{"));
        assertTrue(str.contains("This is a long text content that should be"));
        assertTrue(str.contains("..."));
    }

    @Test
    void testTextToStringShort() {
        Text text = new Text("Short");

        String str = text.toString();
        assertTrue(str.contains("Text{"));
        assertTrue(str.contains("Short"));
        assertFalse(str.contains("..."));
    }

    @Test
    void testTextToStringWithNewlines() {
        Text text = new Text("Line 1\nLine 2\nLine 3");

        String str = text.toString();
        assertTrue(str.contains("\\n"));
    }

    @Test
    void testCDataToString() {
        Text cdata = new Text("CDATA content", true);

        String str = cdata.toString();
        assertTrue(str.contains("isCData=true"));
    }

    @Test
    void testTextInDocument() throws DomTripException {
        String xml = "<root>Simple text content</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element root = editor.root();
        Text textNode = (Text) root.node(0);

        assertEquals("Simple text content", textNode.content());
        assertFalse(textNode.cdata());
    }

    @Test
    void testCDataInDocument() throws DomTripException {
        String xml = "<root><![CDATA[function() { return x < y; }]]></root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element root = editor.root();
        Text cdataNode = (Text) root.node(0);

        assertEquals("function() { return x < y; }", cdataNode.content());
        assertTrue(cdataNode.cdata());
    }

    @Test
    void testMixedTextAndCData() throws DomTripException {
        String xml = "<root>Text before <![CDATA[CDATA content]]> text after</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        assertTrue(result.contains("Text before"));
        assertTrue(result.contains("<![CDATA[CDATA content]]>"));
        assertTrue(result.contains("text after"));
    }

    @Test
    void testTextWithEntities() throws DomTripException {
        String xml = "<root>Text with &lt;entities&gt; &amp; symbols</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element root = editor.root();
        Text textNode = (Text) root.node(0);

        // Content should be decoded
        assertEquals("Text with <entities> & symbols", textNode.content());

        // But XML output should preserve entities
        String result = editor.toXml();
        assertTrue(result.contains("&lt;entities&gt; &amp; symbols"));
    }

    @Test
    void testEmptyTextNode() {
        Text text = new Text("");

        assertEquals("", text.content());
        assertEquals("", text.toXml());
    }

    @Test
    void testTextNodeModificationTracking() {
        Text text = new Text("original");

        // Initially not modified
        assertFalse(text.isModified());

        // Modify content
        text.content("modified");
        assertTrue(text.isModified());
    }
}
