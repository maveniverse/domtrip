package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for whitespace preservation when modifying text content.
 *
 * This test class ensures that DomTrip preserves formatting and whitespace
 * when using Element.textContent() and related text modification methods.
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

        Document doc = Document.of(originalXml);
        Editor editor = new Editor(doc);
        Element database = doc.root().descendant("database").orElseThrow();
        Element name = database.child("name").orElseThrow();

        // Change the text content
        name.textContent("newdb");

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

        Document doc = Document.of(originalXml);
        Editor editor = new Editor(doc);
        Element description = doc.root().descendant("description").orElseThrow();

        // Set multiline text content
        String newContent = "Line 1\nLine 2\nLine 3";
        description.textContent(newContent);

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

        Document doc = Document.of(originalXml);
        Editor editor = new Editor(doc);
        Element item = doc.root().descendant("item").orElseThrow();

        // Change content but preserve the whitespace pattern
        item.textContent("   new content   ");

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

        Document doc = Document.of(originalXml);
        Editor editor = new Editor(doc);
        Element path = doc.root().descendant("path").orElseThrow();
        Element command = doc.root().descendant("command").orElseThrow();

        // Set content with special characters
        path.textContent("/home/user/my documents/file.txt");
        command.textContent("echo \"hello world\" && ls -la");

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

        Document doc = Document.of(originalXml);
        Editor editor = new Editor(doc);
        Element empty = doc.root().descendant("empty").orElseThrow();
        Element selfClosing = doc.root().descendant("selfClosing").orElseThrow();

        // Add content to empty elements
        empty.textContent("now has content");
        selfClosing.textContent("no longer self-closing");

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

        Document doc = Document.of(originalXml);
        Editor editor = new Editor(doc);
        Element hasContent = doc.root().descendant("hasContent").orElseThrow();
        Element alsoHasContent = doc.root().descendant("alsoHasContent").orElseThrow();

        // Clear content
        hasContent.textContent("");
        alsoHasContent.textContent(null);

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

        Document doc = Document.of(originalXml);
        Editor editor = new Editor(doc);
        Element value = doc.root().descendant("value").orElseThrow();

        // Change the value
        value.textContent("new");

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

        Document doc = Document.of(originalXml);
        Editor editor = new Editor(doc);
        Element message = doc.root().descendant("message").orElseThrow();
        Element data = doc.root().descendant("data").orElseThrow();

        // Set content that needs entity encoding
        message.textContent("Hello & goodbye");
        data.textContent("<xml>content</xml>");

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

        Document doc = Document.of(originalXml);
        Editor editor = new Editor(doc);
        Element version = doc.root().descendant("version").orElseThrow();

        // Update the version
        version.textContent("2.0.0");

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

        Document doc = Document.of(originalXml);
        Editor editor = new Editor(doc);

        // Make multiple text content changes
        Element host = doc.root().descendant("host").orElseThrow();
        Element port = doc.root().descendant("port").orElseThrow();
        Element url = doc.root().descendant("url").orElseThrow();

        host.textContent("production.example.com");
        port.textContent("443");
        url.textContent("jdbc:mysql://production.example.com/prod");

        String result = editor.toXml();

        // Parse the result and verify it's still valid
        Editor roundTripEditor = new Editor(Document.of(result));

        // Verify the changes persisted
        assertEquals(
                "production.example.com",
                roundTripEditor.root().descendant("host").orElseThrow().textContent());
        assertEquals(
                "443", roundTripEditor.root().descendant("port").orElseThrow().textContent());
        assertEquals(
                "jdbc:mysql://production.example.com/prod",
                roundTripEditor.root().descendant("url").orElseThrow().textContent());

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
