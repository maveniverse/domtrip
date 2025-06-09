package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for Element whitespace-preserving text content methods.
 */
class ElementWhitespaceTest {

    @Test
    void testSetTextContentPreservingWhitespace() throws Exception {
        String xml =
                """
            <root>
                <item>   original value   </item>
                <path>  /usr/local/bin  </path>
                <empty></empty>
            </root>
            """;

        Editor editor = new Editor(xml);
        Element item = editor.findElement("item");
        Element path = editor.findElement("path");
        Element empty = editor.findElement("empty");

        // Test preserving whitespace pattern
        item.setTextContentPreservingWhitespace("new value");
        assertEquals("   new value   ", item.getTextContent());

        path.setTextContentPreservingWhitespace("/home/user/bin");
        assertEquals("  /home/user/bin  ", path.getTextContent());

        // Test with empty element (no existing whitespace to preserve)
        empty.setTextContentPreservingWhitespace("now has content");
        assertEquals("now has content", empty.getTextContent());

        // Verify XML structure is preserved
        String result = editor.toXml();
        assertTrue(result.contains("<item>   new value   </item>"));
        assertTrue(result.contains("<path>  /home/user/bin  </path>"));
        assertTrue(result.contains("<empty>now has content</empty>"));
    }

    @Test
    void testGetTrimmedTextContent() throws Exception {
        String xml =
                """
            <root>
                <item>   value with spaces   </item>
                <clean>no spaces</clean>
                <empty></empty>
                <whitespace>   </whitespace>
            </root>
            """;

        Editor editor = new Editor(xml);
        Element item = editor.findElement("item");
        Element clean = editor.findElement("clean");
        Element empty = editor.findElement("empty");
        Element whitespace = editor.findElement("whitespace");

        assertEquals("value with spaces", item.getTrimmedTextContent());
        assertEquals("no spaces", clean.getTrimmedTextContent());
        assertEquals("", empty.getTrimmedTextContent());
        assertEquals("", whitespace.getTrimmedTextContent());

        // Verify original content is unchanged
        assertEquals("   value with spaces   ", item.getTextContent());
        assertEquals("no spaces", clean.getTextContent());
        assertEquals("", empty.getTextContent());
        assertEquals("   ", whitespace.getTextContent());
    }

    @Test
    void testSetTextContentVsPreservingWhitespace() throws Exception {
        String xml =
                """
            <root>
                <test1>   original   </test1>
                <test2>   original   </test2>
            </root>
            """;

        Editor editor = new Editor(xml);
        Element test1 = editor.findElement("test1");
        Element test2 = editor.findElement("test2");

        // Regular setTextContent destroys whitespace
        test1.setTextContent("new content");
        assertEquals("new content", test1.getTextContent());

        // Whitespace-preserving method keeps pattern
        test2.setTextContentPreservingWhitespace("new content");
        assertEquals("   new content   ", test2.getTextContent());

        String result = editor.toXml();
        assertTrue(result.contains("<test1>new content</test1>"));
        assertTrue(result.contains("<test2>   new content   </test2>"));
    }

    @Test
    void testComplexWhitespacePatterns() throws Exception {
        String xml =
                """
            <config>
                <description>
                    Multi-line description
                    with indentation
                </description>
            </config>
            """;

        Editor editor = new Editor(xml);
        Element description = editor.findElement("description");

        String originalContent = description.getTextContent();
        String trimmedContent = description.getTrimmedTextContent();

        // The actual indentation in the XML has 8 spaces (2 levels of 4-space indentation)
        assertEquals("Multi-line description\n        with indentation", trimmedContent);

        // Preserve the complex whitespace pattern
        description.setTextContentPreservingWhitespace("New description\nwith new content");

        String newTrimmed = description.getTrimmedTextContent();
        assertEquals("New description\nwith new content", newTrimmed);

        // Verify the content was updated successfully
        assertTrue(description.getTextContent().contains("New description"));
        assertTrue(description.getTextContent().contains("with new content"));
    }

    @Test
    void testWithMultipleTextNodes() throws Exception {
        // Create element with multiple text nodes manually
        Element element = new Element("mixed");
        element.addChild(new Text("  first  "));
        element.addChild(new Element("child"));
        element.addChild(new Text("  second  "));

        // setTextContentPreservingWhitespace should work with first text node
        element.setTextContentPreservingWhitespace("updated");

        // getTextContent() concatenates all text nodes, so we get both
        // The first text node gets updated, but the second remains
        assertEquals("  updated    second  ", element.getTextContent());

        // Verify that only the first text node was actually modified
        Text firstText = (Text) element.getChildren().get(0);
        assertEquals("  updated  ", firstText.getContent());
    }

    @Test
    void testWithNoTextContent() throws Exception {
        String xml =
                """
            <root>
                <parent>
                    <child>content</child>
                </parent>
            </root>
            """;

        Editor editor = new Editor(xml);
        Element parent = editor.findElement("parent");

        // Element has whitespace text nodes around the child element
        // getTrimmedTextContent() returns the trimmed concatenation of all text
        assertEquals("", parent.getTrimmedTextContent());

        // Adding text content to element with existing whitespace
        parent.setTextContentPreservingWhitespace("new text");

        // The result includes the preserved whitespace pattern from existing text nodes
        String result = parent.getTextContent();
        assertTrue(result.contains("new text"));
        // The exact whitespace pattern depends on the existing structure
    }

    @Test
    void testWithCDataContent() throws Exception {
        Element element = new Element("script");
        Text cdata = new Text("   <script>alert('test');</script>   ", true);
        element.addChild(cdata);

        assertEquals("<script>alert('test');</script>", element.getTrimmedTextContent());

        // Preserve whitespace in CDATA
        element.setTextContentPreservingWhitespace("<script>console.log('hello');</script>");

        // Should preserve the CDATA whitespace pattern
        assertEquals("   <script>console.log('hello');</script>   ", element.getTextContent());
    }

    @Test
    void testWithEntities() throws Exception {
        String xml = """
            <message>   Hello &amp; welcome   </message>
            """;

        Editor editor = new Editor(xml);
        Element message = editor.findElement("message");

        // DomTrip unescapes entities when parsing, so &amp; becomes &
        assertEquals("Hello & welcome", message.getTrimmedTextContent());

        message.setTextContentPreservingWhitespace("Goodbye <world>");
        assertEquals("   Goodbye <world>   ", message.getTextContent());

        // When serialized, entities are properly escaped again
        String result = editor.toXml();
        assertTrue(result.contains("Goodbye &lt;world&gt;"));
    }

    @Test
    void testRealWorldConfigExample() throws Exception {
        String configXml =
                """
            <?xml version="1.0" encoding="UTF-8"?>
            <configuration>
                <database>
                    <host>   localhost   </host>
                    <port>   5432   </port>
                    <name>   myapp_dev   </name>
                </database>
                <logging>
                    <level>   DEBUG   </level>
                </logging>
            </configuration>
            """;

        Editor editor = new Editor(configXml);

        // Get elements
        Element host = editor.findElement("host");
        Element port = editor.findElement("port");
        Element name = editor.findElement("name");
        Element level = editor.findElement("level");

        // Verify trimmed content
        assertEquals("localhost", host.getTrimmedTextContent());
        assertEquals("5432", port.getTrimmedTextContent());
        assertEquals("myapp_dev", name.getTrimmedTextContent());
        assertEquals("DEBUG", level.getTrimmedTextContent());

        // Update to production settings preserving formatting
        host.setTextContentPreservingWhitespace("prod.example.com");
        port.setTextContentPreservingWhitespace("5432");
        name.setTextContentPreservingWhitespace("myapp_prod");
        level.setTextContentPreservingWhitespace("INFO");

        // Verify whitespace is preserved
        assertEquals("   prod.example.com   ", host.getTextContent());
        assertEquals("   5432   ", port.getTextContent());
        assertEquals("   myapp_prod   ", name.getTextContent());
        assertEquals("   INFO   ", level.getTextContent());

        // Verify XML structure is maintained
        String result = editor.toXml();
        assertTrue(result.contains("<host>   prod.example.com   </host>"));
        assertTrue(result.contains("<port>   5432   </port>"));
        assertTrue(result.contains("<name>   myapp_prod   </name>"));
        assertTrue(result.contains("<level>   INFO   </level>"));

        // Verify indentation is preserved
        assertTrue(result.contains("    <host>"));
        assertTrue(result.contains("    <port>"));
        assertTrue(result.contains("    <name>"));
        assertTrue(result.contains("    <level>"));
    }

    @Test
    void testNullAndEmptyContent() throws Exception {
        String xml =
                """
            <root>
                <item>   original   </item>
            </root>
            """;

        Editor editor = new Editor(xml);
        Element item = editor.findElement("item");

        // Test with null content
        item.setTextContentPreservingWhitespace(null);
        assertEquals("      ", item.getTextContent()); // Just the whitespace

        // Reset
        item.setTextContent("   original   ");

        // Test with empty content
        item.setTextContentPreservingWhitespace("");
        assertEquals("      ", item.getTextContent()); // Just the whitespace
    }

    @Test
    void testModificationTracking() throws Exception {
        String xml = """
            <item>   original   </item>
            """;

        Editor editor = new Editor(xml);
        Element item = editor.findElement("item");

        // getTrimmedTextContent should not mark as modified
        assertFalse(item.isModified());
        item.getTrimmedTextContent();
        assertFalse(item.isModified());

        // setTextContentPreservingWhitespace should mark as modified
        item.setTextContentPreservingWhitespace("new content");
        assertTrue(item.isModified());
    }
}
