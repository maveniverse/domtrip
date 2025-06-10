package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for the new whitespace convenience methods in the Text class.
 */
class TextWhitespaceConvenienceTest {

    @Test
    void testGetTrimmedContent() {
        // Test with leading and trailing whitespace
        Text text1 = new Text("   Hello World   ");
        assertEquals("Hello World", text1.trimmedContent());
        assertEquals("   Hello World   ", text1.content()); // Original unchanged

        // Test with only leading whitespace
        Text text2 = new Text("   Hello World");
        assertEquals("Hello World", text2.trimmedContent());

        // Test with only trailing whitespace
        Text text3 = new Text("Hello World   ");
        assertEquals("Hello World", text3.trimmedContent());

        // Test with no whitespace
        Text text4 = new Text("Hello World");
        assertEquals("Hello World", text4.trimmedContent());

        // Test with empty content
        Text text5 = new Text("");
        assertEquals("", text5.trimmedContent());

        // Test with whitespace-only content
        Text text6 = new Text("   ");
        assertEquals("", text6.trimmedContent());
    }

    @Test
    void testGetLeadingWhitespace() {
        // Test with spaces
        Text text1 = new Text("   Hello World");
        assertEquals("   ", text1.leadingWhitespace());

        // Test with mixed whitespace
        Text text2 = new Text(" \t\n Hello World");
        assertEquals(" \t\n ", text2.leadingWhitespace());

        // Test with no leading whitespace
        Text text3 = new Text("Hello World   ");
        assertEquals("", text3.leadingWhitespace());

        // Test with empty content
        Text text4 = new Text("");
        assertEquals("", text4.leadingWhitespace());

        // Test with whitespace-only content
        Text text5 = new Text("   ");
        assertEquals("   ", text5.leadingWhitespace());
    }

    @Test
    void testGetTrailingWhitespace() {
        // Test with spaces
        Text text1 = new Text("Hello World   ");
        assertEquals("   ", text1.trailingWhitespace());

        // Test with mixed whitespace
        Text text2 = new Text("Hello World \t\n ");
        assertEquals(" \t\n ", text2.trailingWhitespace());

        // Test with no trailing whitespace
        Text text3 = new Text("   Hello World");
        assertEquals("", text3.trailingWhitespace());

        // Test with empty content
        Text text4 = new Text("");
        assertEquals("", text4.trailingWhitespace());

        // Test with whitespace-only content
        Text text5 = new Text("   ");
        assertEquals("   ", text5.trailingWhitespace());
    }

    @Test
    void testSetContentPreservingWhitespace() {
        // Test with leading and trailing whitespace
        Text text1 = new Text("   Hello World   ");
        text1.contentPreservingWhitespace("Goodbye");
        assertEquals("   Goodbye   ", text1.content());

        // Test with only leading whitespace
        Text text2 = new Text("   Hello World");
        text2.contentPreservingWhitespace("Goodbye");
        assertEquals("   Goodbye", text2.content());

        // Test with only trailing whitespace
        Text text3 = new Text("Hello World   ");
        text3.contentPreservingWhitespace("Goodbye");
        assertEquals("Goodbye   ", text3.content());

        // Test with no whitespace
        Text text4 = new Text("Hello World");
        text4.contentPreservingWhitespace("Goodbye");
        assertEquals("Goodbye", text4.content());

        // Test with null content
        Text text5 = new Text("   Hello World   ");
        text5.contentPreservingWhitespace(null);
        assertEquals("      ", text5.content()); // Just the whitespace

        // Test with empty content
        Text text6 = new Text("   Hello World   ");
        text6.contentPreservingWhitespace("");
        assertEquals("      ", text6.content()); // Just the whitespace
    }

    @Test
    void testHasLeadingWhitespace() {
        assertTrue(new Text("   Hello").hasLeadingWhitespace());
        assertTrue(new Text(" Hello").hasLeadingWhitespace());
        assertTrue(new Text("\tHello").hasLeadingWhitespace());
        assertTrue(new Text("\nHello").hasLeadingWhitespace());

        assertFalse(new Text("Hello   ").hasLeadingWhitespace());
        assertFalse(new Text("Hello").hasLeadingWhitespace());
        assertFalse(new Text("").hasLeadingWhitespace());

        // Whitespace-only content has leading whitespace
        assertTrue(new Text("   ").hasLeadingWhitespace());
    }

    @Test
    void testHasTrailingWhitespace() {
        assertTrue(new Text("Hello   ").hasTrailingWhitespace());
        assertTrue(new Text("Hello ").hasTrailingWhitespace());
        assertTrue(new Text("Hello\t").hasTrailingWhitespace());
        assertTrue(new Text("Hello\n").hasTrailingWhitespace());

        assertFalse(new Text("   Hello").hasTrailingWhitespace());
        assertFalse(new Text("Hello").hasTrailingWhitespace());
        assertFalse(new Text("").hasTrailingWhitespace());

        // Whitespace-only content has trailing whitespace
        assertTrue(new Text("   ").hasTrailingWhitespace());
    }

    @Test
    void testComplexWhitespacePattern() {
        // Test with complex whitespace pattern
        Text text = new Text(" \t\n  Hello World  \n\t ");

        assertEquals("Hello World", text.trimmedContent());
        assertEquals(" \t\n  ", text.leadingWhitespace());
        assertEquals("  \n\t ", text.trailingWhitespace());
        assertTrue(text.hasLeadingWhitespace());
        assertTrue(text.hasTrailingWhitespace());

        // Preserve the pattern with new content
        text.contentPreservingWhitespace("Goodbye Universe");
        assertEquals(" \t\n  Goodbye Universe  \n\t ", text.content());
    }

    @Test
    void testRealWorldXmlExample() throws Exception {
        // Test with realistic XML content
        String xml =
                """
            <config>
                <message>   Welcome to our application!   </message>
                <path>  /usr/local/bin  </path>
            </config>
            """;

        Editor editor = new Editor(xml);
        Element message = editor.element("message").orElseThrow();
        Element path = editor.element("path").orElseThrow();

        // Get the text nodes
        Text messageText = (Text) message.nodes.get(0);
        Text pathText = (Text) path.nodes.get(0);

        // Test convenience methods
        assertEquals("Welcome to our application!", messageText.trimmedContent());
        assertEquals("/usr/local/bin", pathText.trimmedContent());

        assertEquals("   ", messageText.leadingWhitespace());
        assertEquals("   ", messageText.trailingWhitespace());
        assertEquals("  ", pathText.leadingWhitespace());
        assertEquals("  ", pathText.trailingWhitespace());

        // Update content preserving whitespace
        messageText.contentPreservingWhitespace("Thank you for using our app!");
        pathText.contentPreservingWhitespace("/home/user/bin");

        // Verify the XML structure is preserved
        String result = editor.toXml();
        assertTrue(result.contains("<message>   Thank you for using our app!   </message>"));
        assertTrue(result.contains("<path>  /home/user/bin  </path>"));

        // Verify indentation is still preserved
        assertTrue(result.contains("    <message>"));
        assertTrue(result.contains("    <path>"));
    }

    @Test
    void testWhitespaceConvenienceWithCData() {
        // Test that convenience methods work with CDATA
        Text cdata = new Text("   <script>alert('test');</script>   ", true);

        assertEquals("<script>alert('test');</script>", cdata.trimmedContent());
        assertEquals("   ", cdata.leadingWhitespace());
        assertEquals("   ", cdata.trailingWhitespace());

        cdata.contentPreservingWhitespace("<script>console.log('hello');</script>");
        assertEquals("   <script>console.log('hello');</script>   ", cdata.content());
        assertTrue(cdata.cdata());
    }

    @Test
    void testWhitespaceConvenienceWithEntities() {
        // Test with content that has entities
        Text text = new Text("   Hello &amp; welcome   ");

        assertEquals("Hello &amp; welcome", text.trimmedContent());
        assertEquals("   ", text.leadingWhitespace());
        assertEquals("   ", text.trailingWhitespace());

        text.contentPreservingWhitespace("Goodbye &lt;world&gt;");
        assertEquals("   Goodbye &lt;world&gt;   ", text.content());
    }

    @Test
    void testModificationTracking() {
        Text text = new Text("   Hello World   ");

        // Convenience methods should not mark as modified
        assertFalse(text.isModified());
        text.trimmedContent();
        text.leadingWhitespace();
        text.trailingWhitespace();
        text.hasLeadingWhitespace();
        text.hasTrailingWhitespace();
        assertFalse(text.isModified());

        // setContentPreservingWhitespace should mark as modified
        text.contentPreservingWhitespace("Goodbye");
        assertTrue(text.isModified());
    }
}
