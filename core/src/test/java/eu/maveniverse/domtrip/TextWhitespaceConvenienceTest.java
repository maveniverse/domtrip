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
        assertEquals("Hello World", text1.getTrimmedContent());
        assertEquals("   Hello World   ", text1.getContent()); // Original unchanged

        // Test with only leading whitespace
        Text text2 = new Text("   Hello World");
        assertEquals("Hello World", text2.getTrimmedContent());

        // Test with only trailing whitespace
        Text text3 = new Text("Hello World   ");
        assertEquals("Hello World", text3.getTrimmedContent());

        // Test with no whitespace
        Text text4 = new Text("Hello World");
        assertEquals("Hello World", text4.getTrimmedContent());

        // Test with empty content
        Text text5 = new Text("");
        assertEquals("", text5.getTrimmedContent());

        // Test with whitespace-only content
        Text text6 = new Text("   ");
        assertEquals("", text6.getTrimmedContent());
    }

    @Test
    void testGetLeadingWhitespace() {
        // Test with spaces
        Text text1 = new Text("   Hello World");
        assertEquals("   ", text1.getLeadingWhitespace());

        // Test with mixed whitespace
        Text text2 = new Text(" \t\n Hello World");
        assertEquals(" \t\n ", text2.getLeadingWhitespace());

        // Test with no leading whitespace
        Text text3 = new Text("Hello World   ");
        assertEquals("", text3.getLeadingWhitespace());

        // Test with empty content
        Text text4 = new Text("");
        assertEquals("", text4.getLeadingWhitespace());

        // Test with whitespace-only content
        Text text5 = new Text("   ");
        assertEquals("   ", text5.getLeadingWhitespace());
    }

    @Test
    void testGetTrailingWhitespace() {
        // Test with spaces
        Text text1 = new Text("Hello World   ");
        assertEquals("   ", text1.getTrailingWhitespace());

        // Test with mixed whitespace
        Text text2 = new Text("Hello World \t\n ");
        assertEquals(" \t\n ", text2.getTrailingWhitespace());

        // Test with no trailing whitespace
        Text text3 = new Text("   Hello World");
        assertEquals("", text3.getTrailingWhitespace());

        // Test with empty content
        Text text4 = new Text("");
        assertEquals("", text4.getTrailingWhitespace());

        // Test with whitespace-only content
        Text text5 = new Text("   ");
        assertEquals("   ", text5.getTrailingWhitespace());
    }

    @Test
    void testSetContentPreservingWhitespace() {
        // Test with leading and trailing whitespace
        Text text1 = new Text("   Hello World   ");
        text1.setContentPreservingWhitespace("Goodbye");
        assertEquals("   Goodbye   ", text1.getContent());

        // Test with only leading whitespace
        Text text2 = new Text("   Hello World");
        text2.setContentPreservingWhitespace("Goodbye");
        assertEquals("   Goodbye", text2.getContent());

        // Test with only trailing whitespace
        Text text3 = new Text("Hello World   ");
        text3.setContentPreservingWhitespace("Goodbye");
        assertEquals("Goodbye   ", text3.getContent());

        // Test with no whitespace
        Text text4 = new Text("Hello World");
        text4.setContentPreservingWhitespace("Goodbye");
        assertEquals("Goodbye", text4.getContent());

        // Test with null content
        Text text5 = new Text("   Hello World   ");
        text5.setContentPreservingWhitespace(null);
        assertEquals("      ", text5.getContent()); // Just the whitespace

        // Test with empty content
        Text text6 = new Text("   Hello World   ");
        text6.setContentPreservingWhitespace("");
        assertEquals("      ", text6.getContent()); // Just the whitespace
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

        assertEquals("Hello World", text.getTrimmedContent());
        assertEquals(" \t\n  ", text.getLeadingWhitespace());
        assertEquals("  \n\t ", text.getTrailingWhitespace());
        assertTrue(text.hasLeadingWhitespace());
        assertTrue(text.hasTrailingWhitespace());

        // Preserve the pattern with new content
        text.setContentPreservingWhitespace("Goodbye Universe");
        assertEquals(" \t\n  Goodbye Universe  \n\t ", text.getContent());
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
        Element message = editor.findElement("message");
        Element path = editor.findElement("path");

        // Get the text nodes
        Text messageText = (Text) message.getChildren().get(0);
        Text pathText = (Text) path.getChildren().get(0);

        // Test convenience methods
        assertEquals("Welcome to our application!", messageText.getTrimmedContent());
        assertEquals("/usr/local/bin", pathText.getTrimmedContent());

        assertEquals("   ", messageText.getLeadingWhitespace());
        assertEquals("   ", messageText.getTrailingWhitespace());
        assertEquals("  ", pathText.getLeadingWhitespace());
        assertEquals("  ", pathText.getTrailingWhitespace());

        // Update content preserving whitespace
        messageText.setContentPreservingWhitespace("Thank you for using our app!");
        pathText.setContentPreservingWhitespace("/home/user/bin");

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

        assertEquals("<script>alert('test');</script>", cdata.getTrimmedContent());
        assertEquals("   ", cdata.getLeadingWhitespace());
        assertEquals("   ", cdata.getTrailingWhitespace());

        cdata.setContentPreservingWhitespace("<script>console.log('hello');</script>");
        assertEquals("   <script>console.log('hello');</script>   ", cdata.getContent());
        assertTrue(cdata.isCData());
    }

    @Test
    void testWhitespaceConvenienceWithEntities() {
        // Test with content that has entities
        Text text = new Text("   Hello &amp; welcome   ");

        assertEquals("Hello &amp; welcome", text.getTrimmedContent());
        assertEquals("   ", text.getLeadingWhitespace());
        assertEquals("   ", text.getTrailingWhitespace());

        text.setContentPreservingWhitespace("Goodbye &lt;world&gt;");
        assertEquals("   Goodbye &lt;world&gt;   ", text.getContent());
    }

    @Test
    void testModificationTracking() {
        Text text = new Text("   Hello World   ");

        // Convenience methods should not mark as modified
        assertFalse(text.isModified());
        text.getTrimmedContent();
        text.getLeadingWhitespace();
        text.getTrailingWhitespace();
        text.hasLeadingWhitespace();
        text.hasTrailingWhitespace();
        assertFalse(text.isModified());

        // setContentPreservingWhitespace should mark as modified
        text.setContentPreservingWhitespace("Goodbye");
        assertTrue(text.isModified());
    }
}
