package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for the Parser's automatic whitespace normalization feature.
 *
 * The Parser now automatically normalizes whitespace during parsing to ensure:
 * 1. No Text nodes are created that contain only whitespace
 * 2. Whitespace is stored in element properties instead of text nodes
 * 3. Mixed content (text with whitespace) preserves backward compatibility
 */
public class ParserWhitespaceNormalizationTest {

    @Test
    void testNoWhitespaceOnlyTextNodesCreated() throws DomTripException {
        // XML with whitespace between elements
        String xml = """
            <root>
                <child1>content1</child1>
                <child2>content2</child2>
            </root>
            """;

        Document doc = Document.of(xml);
        Element root = doc.root();

        // Verify that no whitespace-only text nodes were created
        for (Node child : root.children) {
            if (child instanceof Text text) {
                assertFalse(
                        text.isWhitespaceOnly(),
                        "Parser should not create whitespace-only text nodes, but found: '" + text.content() + "'");
            }
        }

        // Verify that whitespace is captured in element properties
        Element child1 = (Element) root.children.get(0);
        Element child2 = (Element) root.children.get(1);

        assertTrue(
                child1.precedingWhitespace().contains("\n"),
                "Leading whitespace should be captured in precedingWhitespace");
        assertTrue(
                child2.precedingWhitespace().contains("\n"),
                "Leading whitespace should be captured in precedingWhitespace");
        assertTrue(
                root.innerPrecedingWhitespace().contains("\n"),
                "Trailing whitespace should be captured in innerPrecedingWhitespace");
    }

    @Test
    void testMixedContentPreservesBackwardCompatibility() throws DomTripException {
        // XML with mixed content (text with leading/trailing whitespace)
        String xml = "<message>   Welcome to our application!   </message>";

        Document doc = Document.of(xml);
        Element message = doc.root();

        // Should have exactly one text node (preserving existing behavior)
        assertEquals(1, message.children.size(), "Mixed content should preserve single text node");

        Text textNode = (Text) message.children.get(0);
        assertEquals(
                "   Welcome to our application!   ",
                textNode.content(),
                "Mixed content should preserve original text including whitespace");
        assertEquals("Welcome to our application!", textNode.trimmedContent(), "Trimmed content should be available");
        assertEquals("   ", textNode.leadingWhitespace(), "Leading whitespace should be extractable");
        assertEquals("   ", textNode.trailingWhitespace(), "Trailing whitespace should be extractable");
    }

    @Test
    void testComplexWhitespaceStructure() throws DomTripException {
        // XML with complex whitespace patterns
        String xml = """
            <config>
                <section name="database">
                    <host>localhost</host>
                    <port>5432</port>
                </section>
                <section name="cache">
                    <enabled>true</enabled>
                </section>
            </config>
            """;

        Document doc = Document.of(xml);
        Element config = doc.root();

        // Count text nodes in the entire document
        int textNodeCount = countTextNodes(config);
        int whitespaceOnlyTextNodes = countWhitespaceOnlyTextNodes(config);

        // Verify no whitespace-only text nodes exist
        assertEquals(0, whitespaceOnlyTextNodes, "Parser should not create any whitespace-only text nodes");

        // Verify that actual content text nodes still exist
        assertTrue(textNodeCount > 0, "Content text nodes should still be created");

        // Verify XML round-trip preserves formatting
        String result = doc.toXml();
        assertTrue(result.contains("    <section"), "Indentation should be preserved in output");
        assertTrue(result.contains("        <host>"), "Nested indentation should be preserved");
    }

    @Test
    void testEmptyElementsWithWhitespace() throws DomTripException {
        // XML with empty elements and whitespace
        String xml = """
            <root>
                <empty1/>
                <empty2></empty2>
                <empty3>   </empty3>
            </root>
            """;

        Document doc = Document.of(xml);
        Element root = doc.root();

        // Verify structure
        assertEquals(3, root.children.size(), "Should have 3 child elements");

        for (Node child : root.children) {
            assertTrue(child instanceof Element, "All children should be elements");
            Element element = (Element) child;

            // Check for whitespace-only text nodes in each element
            for (Node grandchild : element.children) {
                if (grandchild instanceof Text text) {
                    assertFalse(
                            text.isWhitespaceOnly(),
                            "No whitespace-only text nodes should exist in element: " + element.name());
                }
            }
        }

        // For empty3, the whitespace should be captured as innerPrecedingWhitespace
        Element empty3 = (Element) root.children.get(2);
        assertEquals(
                "   ",
                empty3.innerPrecedingWhitespace(),
                "Whitespace in empty element should be captured as innerPrecedingWhitespace");
    }

    @Test
    void testDocumentLevelWhitespace() throws DomTripException {
        // XML with whitespace at document level
        String xml = "\n  <root>content</root>\n  ";

        Document doc = Document.of(xml);

        // Count whitespace-only text nodes at document level
        int whitespaceOnlyNodes = 0;
        for (Node node : doc.children) {
            if (node instanceof Text text && text.isWhitespaceOnly()) {
                whitespaceOnlyNodes++;
            }
        }

        // Document level whitespace handling may still create some text nodes
        // as they're not between elements, but the key is that element-level
        // whitespace should be normalized
        Element root = doc.root();
        assertTrue(
                root.precedingWhitespace().contains("\n"),
                "Document-level whitespace should be captured in root element precedingWhitespace");
    }

    @Test
    void testWhitespaceAfterDocumentElementClosingTag() throws DomTripException {
        // XML with various types of whitespace after the root element
        String xml1 = "<root>content</root>\n";
        String xml2 = "<root>content</root>\n\n";
        String xml3 = "<root>content</root>   \n  ";
        String xml4 = "<root>content</root>\t\n  <!-- comment -->";

        // Test single newline after root
        Document doc1 = Document.of(xml1);
        assertEquals(2, doc1.children.size(), "Should have root element + trailing whitespace text node");

        Node lastNode1 = doc1.children.get(doc1.children.size() - 1);
        assertTrue(lastNode1 instanceof Text, "Last node should be a text node for trailing whitespace");
        Text trailingText1 = (Text) lastNode1;
        assertTrue(trailingText1.isWhitespaceOnly(), "Trailing text should be whitespace-only");
        assertEquals("\n", trailingText1.content(), "Should preserve single newline");

        // Test double newline after root
        Document doc2 = Document.of(xml2);
        assertEquals(2, doc2.children.size(), "Should have root element + trailing whitespace text node");

        Node lastNode2 = doc2.children.get(doc2.children.size() - 1);
        assertTrue(lastNode2 instanceof Text, "Last node should be a text node for trailing whitespace");
        Text trailingText2 = (Text) lastNode2;
        assertTrue(trailingText2.isWhitespaceOnly(), "Trailing text should be whitespace-only");
        assertEquals("\n\n", trailingText2.content(), "Should preserve double newline");

        // Test mixed whitespace after root
        Document doc3 = Document.of(xml3);
        assertEquals(2, doc3.children.size(), "Should have root element + trailing whitespace text node");

        Node lastNode3 = doc3.children.get(doc3.children.size() - 1);
        assertTrue(lastNode3 instanceof Text, "Last node should be a text node for trailing whitespace");
        Text trailingText3 = (Text) lastNode3;
        assertTrue(trailingText3.isWhitespaceOnly(), "Trailing text should be whitespace-only");
        assertEquals("   \n  ", trailingText3.content(), "Should preserve mixed whitespace");

        // Test whitespace followed by comment
        Document doc4 = Document.of(xml4);
        assertEquals(2, doc4.children.size(), "Should have root element + comment (whitespace assigned to comment)");

        Node secondNode = doc4.children.get(1);
        assertTrue(secondNode instanceof Comment, "Second node should be comment");
        Comment comment = (Comment) secondNode;
        assertEquals(" comment ", comment.content(), "Comment content should be preserved");
        assertEquals(
                "\t\n  ",
                comment.precedingWhitespace(),
                "Whitespace should be assigned to comment's precedingWhitespace");

        // Verify XML round-trip preserves trailing whitespace
        assertEquals(xml1, doc1.toXml(), "XML round-trip should preserve single newline");
        assertEquals(xml2, doc2.toXml(), "XML round-trip should preserve double newline");
        assertEquals(xml3, doc3.toXml(), "XML round-trip should preserve mixed whitespace");
        assertEquals(xml4, doc4.toXml(), "XML round-trip should preserve whitespace and comment");
    }

    @Test
    void testPrecedingWhitespaceSetOnAllNodeTypes() throws DomTripException {
        // Issue #130: precedingWhitespace should be set consistently on all node types
        String xml = "<root>\n    <!-- a comment -->\n    <?pi data?>\n    <![CDATA[cdata]]>\n    <child/>\n</root>";

        Document doc = Document.of(xml);
        Element root = doc.root();

        // Comment should have precedingWhitespace
        Comment comment = root.children()
                .filter(n -> n instanceof Comment)
                .map(n -> (Comment) n)
                .findFirst()
                .orElseThrow();
        assertEquals("\n    ", comment.precedingWhitespace(), "Comment should have precedingWhitespace set by parser");

        // ProcessingInstruction should have precedingWhitespace
        ProcessingInstruction pi = root.children()
                .filter(n -> n instanceof ProcessingInstruction)
                .map(n -> (ProcessingInstruction) n)
                .findFirst()
                .orElseThrow();
        assertEquals("\n    ", pi.precedingWhitespace(), "PI should have precedingWhitespace set by parser");

        // CDATA (Text node) should have precedingWhitespace
        Text cdata = root.children()
                .filter(n -> n instanceof Text && ((Text) n).cdata())
                .map(n -> (Text) n)
                .findFirst()
                .orElseThrow();
        assertEquals("\n    ", cdata.precedingWhitespace(), "CDATA should have precedingWhitespace set by parser");

        // Element should have precedingWhitespace
        Element child = root.childElement("child").orElseThrow();
        assertEquals("\n    ", child.precedingWhitespace(), "Element should have precedingWhitespace set by parser");

        // Verify round-trip preserves all whitespace
        assertEquals(xml, root.toXml());
    }

    /**
     * Helper method to count all text nodes in an element tree
     */
    private int countTextNodes(Element element) {
        int count = 0;
        for (Node child : element.children) {
            if (child instanceof Text) {
                count++;
            } else if (child instanceof Element childElement) {
                count += countTextNodes(childElement);
            }
        }
        return count;
    }

    /**
     * Helper method to count whitespace-only text nodes in an element tree
     */
    private int countWhitespaceOnlyTextNodes(Element element) {
        int count = 0;
        for (Node child : element.children) {
            if (child instanceof Text text && text.isWhitespaceOnly()) {
                count++;
            } else if (child instanceof Element childElement) {
                count += countWhitespaceOnlyTextNodes(childElement);
            }
        }
        return count;
    }
}
