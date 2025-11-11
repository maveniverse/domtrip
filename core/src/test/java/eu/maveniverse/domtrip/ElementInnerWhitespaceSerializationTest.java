package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for Element.toXml() serialization of inner whitespace fields.
 * Verifies that innerFollowingWhitespace and innerPrecedingWhitespace are properly included in XML output.
 */
public class ElementInnerWhitespaceSerializationTest {

    @Test
    public void testInnerWhitespaceSerializationInEmptyElement() throws DomTripException {
        // Create an element with inner whitespace but no children
        Element element = Element.of("parent");
        element.innerPrecedingWhitespace("\n  \n");

        String xml = element.toXml();
        assertEquals("<parent>\n  \n</parent>", xml);
    }

    @Test
    public void testInnerWhitespaceSerializationWithChildren() throws DomTripException {
        // Create an element with both inner whitespace and children
        Element parent = Element.of("parent");
        parent.innerPrecedingWhitespace("\n");

        parent.addNode(Element.of("child").textContent("content").precedingWhitespace("\n  "));

        String xml = parent.toXml();
        // Inner whitespace should be preserved even with children
        assertEquals("<parent>\n  <child>content</child>\n</parent>", xml);
    }

    @Test
    public void testInnerWhitespaceSerializationFromParsedXml() throws DomTripException {
        // Test that parsed XML with inner whitespace is properly serialized
        String originalXml = "<root>\n    \n</root>";
        Document doc = Document.of(originalXml);
        Element root = doc.root();

        // Verify the inner whitespace was captured during parsing
        assertEquals("\n    \n", root.innerPrecedingWhitespace());

        // Verify it's properly serialized back
        String serializedXml = root.toXml();
        assertEquals(originalXml, serializedXml);
    }

    @Test
    public void testInnerWhitespaceSerializationWithComplexWhitespace() throws DomTripException {
        // Test with more complex whitespace patterns
        Element element = Element.of("config");
        element.addNode(Comment.of(" comment space ").precedingWhitespace("\n    "));
        element.addNode(Comment.of(" end comment ").precedingWhitespace("\n    \n    "));
        element.innerPrecedingWhitespace("\n");

        String xml = element.toXml();
        assertEquals("<config>\n    <!-- comment space -->\n    \n    <!-- end comment -->\n</config>", xml);
    }

    @Test
    public void testInnerWhitespaceSerializationWithAttributes() throws DomTripException {
        // Test that inner whitespace works correctly with attributes
        Element element = Element.of("item");
        element.attribute("id", "123");
        element.attribute("type", "test");
        element.innerPrecedingWhitespace("\n  \n");

        String xml = element.toXml();
        assertEquals("<item id=\"123\" type=\"test\">\n  \n</item>", xml);
    }

    @Test
    public void testInnerWhitespaceSerializationWithSelfClosingElement() throws DomTripException {
        // Test that inner whitespace is ignored for self-closing elements
        Element element = Element.of("br");
        element.selfClosing(true);
        element.innerPrecedingWhitespace("\n  \n");

        String xml = element.toXml();
        // Self-closing elements should not include inner whitespace
        assertEquals("<br/>", xml);
    }

    @Test
    public void testInnerWhitespaceSerializationWithMixedContent() throws DomTripException {
        // Test inner whitespace with mixed text and element content
        Element parent = Element.of("paragraph");
        parent.innerPrecedingWhitespace("\n");

        parent.addNode(new Text("Some text ").precedingWhitespace("\n  "));
        parent.addNode(Element.of("em").textContent("emphasized"));
        parent.addNode(new Text(" more text"));

        String xml = parent.toXml();
        assertEquals("<paragraph>\n  Some text <em>emphasized</em> more text\n</paragraph>", xml);
    }

    @Test
    public void testInnerWhitespaceSerializationRoundTrip() throws DomTripException {
        // Test that inner whitespace survives round-trip parsing and serialization
        String originalXml = """
            <project>

            </project>""";

        Document doc = Document.of(originalXml);
        Element project = doc.root();

        // Verify inner whitespace was captured
        assertFalse(project.innerPrecedingWhitespace().isEmpty());

        // Serialize and parse again
        String serialized = doc.toXml();
        Document doc2 = Document.of(serialized);
        Element project2 = doc2.root();

        // Verify inner whitespace is preserved
        assertEquals(project.innerPrecedingWhitespace(), project2.innerPrecedingWhitespace());
        assertEquals(originalXml, doc2.toXml());
    }

    @Test
    public void testInnerWhitespaceSerializationWithOriginalFormatting() throws DomTripException {
        // Test that inner whitespace works with original formatting preservation
        String originalXml = "<config   >\n    \n</  config>";
        Document doc = Document.of(originalXml);
        Element config = doc.root();

        // Element should not be marked as modified
        assertFalse(config.isModified());

        // Should preserve original formatting including inner whitespace
        String serialized = config.toXml();
        assertEquals(originalXml, serialized);
    }

    @Test
    public void testInnerWhitespaceSerializationWithModification() throws DomTripException {
        // Test that inner whitespace works correctly when element is modified
        Element element = Element.of("config");
        element.innerPrecedingWhitespace("\n    \n");

        // Add an attribute to modify the element
        element.attribute("version", "1.0");

        String xml = element.toXml();
        assertEquals("<config version=\"1.0\">\n    \n</config>", xml);
    }

    @Test
    public void testEmptyInnerWhitespaceFields() throws DomTripException {
        // Test that empty inner whitespace fields don't add unwanted content
        Element element = Element.of("empty");
        element.innerPrecedingWhitespace("");

        String xml = element.toXml();
        assertEquals("<empty></empty>", xml);
    }

    @Test
    public void testNullInnerWhitespaceFields() throws DomTripException {
        // Test that null inner whitespace fields are handled gracefully
        Element element = Element.of("test");
        // Inner whitespace fields should default to empty strings

        String xml = element.toXml();
        assertEquals("<test></test>", xml);
    }
}
