package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for whitespace preservation when modifying text content.
 *
 * This test class ensures that DomTrip preserves formatting and whitespace
 * when using Element.setTextContent() and related text modification methods.
 */
class WhitespacePreservationTest {

    @Test
    void testSetTextContentPreservesIndentation() throws Exception {
        String originalXml =
                """
            <?xml version="1.0" encoding="UTF-8"?>
            <config>
                <database>
                    <host>localhost</host>
                    <port>5432</port>
                    <name>mydb</name>
                </database>
            </config>
            """;

        Editor editor = new Editor(originalXml);
        Element database = editor.findElement("database");
        Element name = database.findChild("name").orElseThrow();

        // Change the text content
        name.setTextContent("newdb");

        String result = editor.toXml();

        // Verify the indentation is preserved
        assertTrue(result.contains("    <host>localhost</host>"));
        assertTrue(result.contains("    <port>5432</port>"));
        assertTrue(result.contains("    <name>newdb</name>"));

        // Verify overall structure is maintained
        assertTrue(result.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(result.contains("<config>"));
        assertTrue(result.contains("    <database>"));
        assertTrue(result.contains("    </database>"));
        assertTrue(result.contains("</config>"));
    }

    @Test
    void testSetTextContentWithMultilineValue() throws Exception {
        String originalXml =
                """
            <root>
                <description>
                    Single line description
                </description>
            </root>
            """;

        Editor editor = new Editor(originalXml);
        Element description = editor.findElement("description");

        // Set multiline text content
        String newContent = "Line 1\nLine 2\nLine 3";
        description.setTextContent(newContent);

        String result = editor.toXml();

        // Verify the multiline content is preserved
        assertTrue(result.contains("Line 1\nLine 2\nLine 3"));

        // Verify surrounding whitespace/indentation is maintained
        assertTrue(result.contains("    <description>"));
        assertTrue(result.contains("</description>"));
    }

    @Test
    void testSetTextContentPreservesLeadingAndTrailingWhitespace() throws Exception {
        String originalXml =
                """
            <root>
                <item>   original content   </item>
            </root>
            """;

        Editor editor = new Editor(originalXml);
        Element item = editor.findElement("item");

        // Change content but preserve the whitespace pattern
        item.setTextContent("   new content   ");

        String result = editor.toXml();

        // Verify the new content with whitespace is preserved
        assertTrue(result.contains("<item>   new content   </item>"));
    }

    @Test
    void testSetTextContentWithSpecialCharacters() throws Exception {
        String originalXml =
                """
            <config>
                <path>/usr/local/bin</path>
                <command>echo "hello"</command>
            </config>
            """;

        Editor editor = new Editor(originalXml);
        Element path = editor.findElement("path");
        Element command = editor.findElement("command");

        // Set content with special characters
        path.setTextContent("/home/user/my documents/file.txt");
        command.setTextContent("echo \"hello world\" && ls -la");

        String result = editor.toXml();

        // Verify special characters are properly handled
        assertTrue(result.contains("<path>/home/user/my documents/file.txt</path>"));
        // XML entities are properly encoded: && becomes &amp;&amp;
        assertTrue(result.contains("echo \"hello world\" &amp;&amp; ls -la")
                || result.contains("echo \"hello world\" && ls -la"));

        // Verify indentation is preserved
        assertTrue(result.contains("    <path>"));
        assertTrue(result.contains("    <command>"));
    }

    @Test
    void testSetTextContentEmptyToNonEmpty() throws Exception {
        String originalXml =
                """
            <root>
                <empty></empty>
                <selfClosing/>
            </root>
            """;

        Editor editor = new Editor(originalXml);
        Element empty = editor.findElement("empty");
        Element selfClosing = editor.findElement("selfClosing");

        // Add content to empty elements
        empty.setTextContent("now has content");
        selfClosing.setTextContent("no longer self-closing");

        String result = editor.toXml();

        // Verify content is added correctly
        assertTrue(result.contains("<empty>now has content</empty>"));
        assertTrue(result.contains("<selfClosing>no longer self-closing</selfClosing>"));

        // Verify indentation is preserved
        assertTrue(result.contains("    <empty>"));
        assertTrue(result.contains("    <selfClosing>"));
    }

    @Test
    void testSetTextContentNonEmptyToEmpty() throws Exception {
        String originalXml =
                """
            <root>
                <hasContent>some text</hasContent>
                <alsoHasContent>more text</alsoHasContent>
            </root>
            """;

        Editor editor = new Editor(originalXml);
        Element hasContent = editor.findElement("hasContent");
        Element alsoHasContent = editor.findElement("alsoHasContent");

        // Clear content
        hasContent.setTextContent("");
        alsoHasContent.setTextContent(null);

        String result = editor.toXml();

        // Verify content is cleared but structure preserved
        assertTrue(result.contains("<hasContent></hasContent>") || result.contains("<hasContent/>"));
        assertTrue(result.contains("<alsoHasContent></alsoHasContent>") || result.contains("<alsoHasContent/>"));

        // Verify indentation is preserved
        assertTrue(result.contains("    <hasContent"));
        assertTrue(result.contains("    <alsoHasContent"));
    }

    @Test
    void testSetTextContentPreservesComments() throws Exception {
        String originalXml =
                """
            <root>
                <!-- Important comment -->
                <value>old</value>
                <!-- Another comment -->
            </root>
            """;

        Editor editor = new Editor(originalXml);
        Element value = editor.findElement("value");

        // Change the value
        value.setTextContent("new");

        String result = editor.toXml();

        // Verify comments are preserved
        assertTrue(result.contains("<!-- Important comment -->"));
        assertTrue(result.contains("<!-- Another comment -->"));

        // Verify the value changed
        assertTrue(result.contains("<value>new</value>"));

        // Verify indentation is preserved
        assertTrue(result.contains("    <!-- Important comment -->"));
        assertTrue(result.contains("    <value>new</value>"));
        assertTrue(result.contains("    <!-- Another comment -->"));
    }

    @Test
    void testSetTextContentWithXmlEntities() throws Exception {
        String originalXml =
                """
            <root>
                <message>Hello &amp; welcome</message>
                <data>&lt;test&gt;</data>
            </root>
            """;

        Editor editor = new Editor(originalXml);
        Element message = editor.findElement("message");
        Element data = editor.findElement("data");

        // Set content that needs entity encoding
        message.setTextContent("Hello & goodbye");
        data.setTextContent("<xml>content</xml>");

        String result = editor.toXml();

        // Verify entities are properly encoded
        assertTrue(
                result.contains("Hello &amp; goodbye") || result.contains("Hello & goodbye")); // Either is acceptable
        assertTrue(result.contains("&lt;xml&gt;content&lt;/xml&gt;")
                || result.contains("<xml>content</xml>")); // Either is acceptable

        // Verify indentation is preserved
        assertTrue(result.contains("    <message>"));
        assertTrue(result.contains("    <data>"));
    }

    @Test
    void testSetTextContentInNestedStructure() throws Exception {
        String originalXml =
                """
            <project>
                <dependencies>
                    <dependency>
                        <groupId>org.example</groupId>
                        <artifactId>example-lib</artifactId>
                        <version>1.0.0</version>
                    </dependency>
                </dependencies>
            </project>
            """;

        Editor editor = new Editor(originalXml);
        Element version = editor.findElement("version");

        // Update the version
        version.setTextContent("2.0.0");

        String result = editor.toXml();

        // Verify the version changed
        assertTrue(result.contains("<version>2.0.0</version>"));

        // Verify all indentation levels are preserved
        assertTrue(result.contains("    <dependencies>"));
        assertTrue(result.contains("        <dependency>"));
        assertTrue(result.contains("            <groupId>org.example</groupId>"));
        assertTrue(result.contains("            <artifactId>example-lib</artifactId>"));
        assertTrue(result.contains("            <version>2.0.0</version>"));
        assertTrue(result.contains("        </dependency>"));
        assertTrue(result.contains("    </dependencies>"));
    }

    @Test
    void testSetTextContentRoundTrip() throws Exception {
        String originalXml =
                """
            <?xml version="1.0" encoding="UTF-8"?>
            <!-- Configuration file -->
            <config>
                <server>
                    <host>localhost</host>
                    <port>8080</port>
                </server>
                <!-- Database settings -->
                <database>
                    <url>jdbc:mysql://localhost/test</url>
                </database>
            </config>
            """;

        Editor editor = new Editor(originalXml);

        // Make multiple text content changes
        Element host = editor.findElement("host");
        Element port = editor.findElement("port");
        Element url = editor.findElement("url");

        host.setTextContent("production.example.com");
        port.setTextContent("443");
        url.setTextContent("jdbc:mysql://production.example.com/prod");

        String result = editor.toXml();

        // Parse the result and verify it's still valid
        Editor roundTripEditor = new Editor(result);

        // Verify the changes persisted
        assertEquals(
                "production.example.com", roundTripEditor.findElement("host").getTextContent());
        assertEquals("443", roundTripEditor.findElement("port").getTextContent());
        assertEquals(
                "jdbc:mysql://production.example.com/prod",
                roundTripEditor.findElement("url").getTextContent());

        // Verify structure is preserved
        assertTrue(result.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(result.contains("<!-- Configuration file -->"));
        assertTrue(result.contains("<!-- Database settings -->"));

        // Verify indentation is preserved
        assertTrue(result.contains("    <server>"));
        assertTrue(result.contains("        <host>production.example.com</host>"));
        assertTrue(result.contains("        <port>443</port>"));
        assertTrue(result.contains("    <database>"));
        assertTrue(result.contains("        <url>jdbc:mysql://production.example.com/prod</url>"));
    }
}
