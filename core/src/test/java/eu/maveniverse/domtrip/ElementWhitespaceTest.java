package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for Element whitespace-preserving text content methods.
 */
class ElementWhitespaceTest {

    @Test
    void testSetTextContentPreservingWhitespace() throws Exception {
        String xml = """
            <root>
                <item>   original value   </item>
                <path>  /usr/local/bin  </path>
                <empty></empty>
            </root>
            """;

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element item = doc.root().descendant("item").orElseThrow();
        Element path = doc.root().descendant("path").orElseThrow();
        Element empty = doc.root().descendant("empty").orElseThrow();

        // Test preserving whitespace pattern
        item.textPreservingWhitespace("new value");
        assertEquals("   new value   ", item.textContent());

        path.textPreservingWhitespace("/home/user/bin");
        assertEquals("  /home/user/bin  ", path.textContent());

        // Test with empty element (no existing whitespace to preserve)
        empty.textPreservingWhitespace("now has content");
        assertEquals("now has content", empty.textContent());

        // Verify XML structure is preserved
        String result = editor.toXml();
        assertTrue(result.contains("<item>   new value   </item>"));
        assertTrue(result.contains("<path>  /home/user/bin  </path>"));
        assertTrue(result.contains("<empty>now has content</empty>"));
    }

    @Test
    void testGetTrimmedTextContent() throws Exception {
        String xml = """
            <root>
                <item>   value with spaces   </item>
                <clean>no spaces</clean>
                <empty></empty>
                <whitespace>   </whitespace>
            </root>
            """;

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element item = doc.root().descendant("item").orElseThrow();
        Element clean = doc.root().descendant("clean").orElseThrow();
        Element empty = doc.root().descendant("empty").orElseThrow();
        Element whitespace = doc.root().descendant("whitespace").orElseThrow();

        assertEquals("value with spaces", item.textContentTrimmed());
        assertEquals("no spaces", clean.textContentTrimmed());
        assertEquals("", empty.textContentTrimmed());
        assertEquals("", whitespace.textContentTrimmed());

        assertEquals("value with spaces", item.textContentTrimmedOr("default"));
        assertEquals("no spaces", clean.textContentTrimmedOr("default"));
        assertEquals("default", empty.textContentTrimmedOr("default"));
        assertEquals("default", whitespace.textContentTrimmedOr("default"));

        // Verify original content is unchanged
        assertEquals("   value with spaces   ", item.textContent());
        assertEquals("no spaces", clean.textContent());
        assertEquals("", empty.textContent());
        assertEquals("   ", whitespace.innerPrecedingWhitespace());
    }

    @Test
    void testSetTextContentVsPreservingWhitespace() throws Exception {
        String xml = """
            <root>
                <test1>   original   </test1>
                <test2>   original   </test2>
            </root>
            """;

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element test1 = doc.root().descendant("test1").orElseThrow();
        Element test2 = doc.root().descendant("test2").orElseThrow();

        // Regular setTextContent destroys whitespace
        test1.textContent("new content");
        assertEquals("new content", test1.textContent());

        // Whitespace-preserving method keeps pattern
        test2.textPreservingWhitespace("new content");
        assertEquals("   new content   ", test2.textContent());

        String result = editor.toXml();
        assertTrue(result.contains("<test1>new content</test1>"));
        assertTrue(result.contains("<test2>   new content   </test2>"));
    }

    @Test
    void testComplexWhitespacePatterns() throws Exception {
        String xml = """
            <config>
                <description>
                    Multi-line description
                    with indentation
                </description>
            </config>
            """;

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element description = doc.root().descendant("description").orElseThrow();

        String originalContent = description.textContent();
        String trimmedContent = description.textContentTrimmed();

        // The actual indentation in the XML has 8 spaces (2 levels of 4-space indentation)
        assertEquals("Multi-line description\n        with indentation", trimmedContent);

        // Preserve the complex whitespace pattern
        description.textPreservingWhitespace("New description\nwith new content");

        String newTrimmed = description.textContentTrimmed();
        assertEquals("New description\nwith new content", newTrimmed);

        // Verify the content was updated successfully
        assertTrue(description.textContent().contains("New description"));
        assertTrue(description.textContent().contains("with new content"));
    }

    @Test
    void testWithMultipleTextNodes() throws Exception {
        // Create element with multiple text nodes manually
        Element element = new Element("mixed");
        element.addChild(new Text("  first  "));
        element.addChild(new Element("child"));
        element.addChild(new Text("  second  "));

        // setTextContentPreservingWhitespace should work with first text node
        element.textPreservingWhitespace("updated");

        // getTextContent() concatenates all text nodes, so we get both
        // The first text node gets updated, but the second remains
        assertEquals("  updated    second  ", element.textContent());

        // Verify that only the first text node was actually modified
        Text firstText = (Text) element.children.get(0);
        assertEquals("  updated  ", firstText.content());
    }

    @Test
    void testWithNoTextContent() throws Exception {
        String xml = """
            <root>
                <parent>
                    <child>content</child>
                </parent>
            </root>
            """;

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element parent = doc.root().descendant("parent").orElseThrow();

        // Element has whitespace text nodes around the child element
        // getTrimmedTextContent() returns the trimmed concatenation of all text
        assertEquals("", parent.textContentTrimmed());

        // Adding text content to element with existing whitespace
        parent.textPreservingWhitespace("new text");

        // The result includes the preserved whitespace pattern from existing text nodes
        String result = parent.textContent();
        assertTrue(result.contains("new text"));
        // The exact whitespace pattern depends on the existing structure
    }

    @Test
    void testWithCDataContent() throws Exception {
        Element element = new Element("script");
        Text cdata = new Text("   <script>alert('test');</script>   ", true);
        element.addChild(cdata);

        assertEquals("<script>alert('test');</script>", element.textContentTrimmed());

        // Preserve whitespace in CDATA
        element.textPreservingWhitespace("<script>console.log('hello');</script>");

        // Should preserve the CDATA whitespace pattern
        assertEquals("   <script>console.log('hello');</script>   ", element.textContent());
    }

    @Test
    void testWithEntities() throws Exception {
        String xml = """
            <message>   Hello &amp; welcome   </message>
            """;

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element message = doc.root();

        // DomTrip unescapes entities when parsing, so &amp; becomes &
        assertEquals("Hello & welcome", message.textContentTrimmed());

        message.textPreservingWhitespace("Goodbye <world>");
        assertEquals("   Goodbye <world>   ", message.textContent());

        // When serialized, entities are properly escaped again
        String result = editor.toXml();
        assertTrue(result.contains("Goodbye &lt;world&gt;"));
    }

    @Test
    void testRealWorldConfigExample() throws Exception {
        String configXml = """
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

        Document doc = Document.of(configXml);
        Editor editor = new Editor(doc);

        // Get elements
        Element host = doc.root().descendant("host").orElseThrow();
        Element port = doc.root().descendant("port").orElseThrow();
        Element name = doc.root().descendant("name").orElseThrow();
        Element level = doc.root().descendant("level").orElseThrow();

        // Verify trimmed content
        assertEquals("localhost", host.textContentTrimmed());
        assertEquals("5432", port.textContentTrimmed());
        assertEquals("myapp_dev", name.textContentTrimmed());
        assertEquals("DEBUG", level.textContentTrimmed());

        // Update to production settings preserving formatting
        host.textPreservingWhitespace("prod.example.com");
        port.textPreservingWhitespace("5432");
        name.textPreservingWhitespace("myapp_prod");
        level.textPreservingWhitespace("INFO");

        // Verify whitespace is preserved
        assertEquals("   prod.example.com   ", host.textContent());
        assertEquals("   5432   ", port.textContent());
        assertEquals("   myapp_prod   ", name.textContent());
        assertEquals("   INFO   ", level.textContent());

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
        String xml = """
            <root>
                <item>   original   </item>
            </root>
            """;

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element item = doc.root().descendant("item").orElseThrow();

        // Test with null content
        item.textPreservingWhitespace(null);
        assertEquals("      ", item.textContent()); // Just the whitespace

        // Reset
        item.textContent("   original   ");

        // Test with empty content
        item.textPreservingWhitespace("");
        assertEquals("      ", item.textContent()); // Just the whitespace
    }

    @Test
    void testModificationTracking() throws Exception {
        String xml = """
            <item>   original   </item>
            """;

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element item = doc.root();

        // getTrimmedTextContent should not mark as modified
        assertFalse(item.isModified());
        item.textContentTrimmed();
        assertFalse(item.isModified());

        // setTextContentPreservingWhitespace should mark as modified
        item.textPreservingWhitespace("new content");
        assertTrue(item.isModified());
    }
}
