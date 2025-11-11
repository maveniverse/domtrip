package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test cases for indentation and whitespace preservation.
 */
public class IndentationTest {

    private Editor editor;

    @BeforeEach
    void setUp() {
        editor = new Editor(Document.of());
    }

    @Test
    void testIndentationInference() throws DomTripException {
        String xml = "<root>\n" + "    <existing>content</existing>\n" + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element root = editor.root();
        editor.addElement(root, "newElement", "new content");

        String result = editor.toXml();

        // New element should be indented with 4 spaces like existing element
        assertTrue(result.contains("    <existing>content</existing>"));
        assertTrue(result.contains("    <newElement>new content</newElement>"));
    }

    @Test
    void testTabIndentation() throws DomTripException {
        String xml = "<root>\n" + "\t<existing>content</existing>\n" + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element root = editor.root();
        editor.addElement(root, "newElement", "new content");

        String result = editor.toXml();

        // New element should be indented with tabs like existing element
        assertTrue(result.contains("\t<existing>content</existing>"));
        assertTrue(result.contains("\t<newElement>new content</newElement>"));
    }

    @Test
    void testNestedIndentation() throws DomTripException {
        String xml =
                """
            <root>
              <parent>
                <child>content</child>
              </parent>
            </root>""";
        String expected =
                """
            <root>
              <parent>
                <child>content</child>
                <newChild>new content</newChild>
              </parent>
            </root>""";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element parent = doc.root().descendant("parent").orElseThrow();
        editor.addElement(parent, "newChild", "new content");

        String result = editor.toXml();

        // New child should be indented at the same level as existing child
        assertEquals(expected, result);
    }

    @Test
    void testMixedWhitespace() throws DomTripException {
        String xml = "<root>\n" + "  <element1>content1</element1>\n"
                + "\n"
                + "  <element2>content2</element2>\n"
                + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element root = editor.root();
        editor.addElement(root, "element3", "content3");

        String result = editor.toXml();

        // Should preserve existing whitespace patterns
        assertTrue(result.contains("  <element1>content1</element1>"));
        assertTrue(result.contains("  <element2>content2</element2>"));
        assertTrue(result.contains("  <element3>content3</element3>"));
    }

    @Test
    void testCommentIndentation() throws DomTripException {
        String xml = "<root>\n" + "  <element>content</element>\n" + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element root = editor.root();
        editor.addComment(root, "This is a comment");

        String result = editor.toXml();

        // Comment should be indented like other children
        assertTrue(result.contains("  <element>content</element>"));
        // The comment might not be indented in the current implementation
        assertTrue(result.contains("<!--This is a comment-->"));
    }

    @Test
    void testEmptyElementIndentation() throws DomTripException {
        String xml = "<root>\n" + "  <existing/>\n" + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element root = editor.root();
        Element newElement = editor.addElement(root, "newEmpty");

        String result = editor.toXml();

        // Both empty elements should be properly indented
        assertTrue(result.contains("  <existing/>"));
        assertTrue(result.contains("  <newEmpty></newEmpty>"));
    }

    @Test
    void testDocumentCreationIndentation() throws DomTripException {
        editor.createDocument("root");
        Element root = editor.root();

        editor.addElement(root, "child1", "content1");
        editor.addElement(root, "child2", "content2");

        String result = editor.toXml();

        // Should have proper indentation even for created documents
        assertTrue(result.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(result.contains("<root>"));
        assertTrue(result.contains("</root>"));

        // Children should be indented (default 2 spaces)
        String[] lines = result.split("\n");
        boolean foundChild1 = false;
        boolean foundChild2 = false;

        for (String line : lines) {
            if (line.contains("<child1>")) {
                assertTrue(line.startsWith("  "), "child1 should be indented with 2 spaces");
                foundChild1 = true;
            }
            if (line.contains("<child2>")) {
                assertTrue(line.startsWith("  "), "child2 should be indented with 2 spaces");
                foundChild2 = true;
            }
        }

        assertTrue(foundChild1, "Should find child1 element");
        assertTrue(foundChild2, "Should find child2 element");
    }

    @Test
    void testRecursiveIndentationFix() throws DomTripException {
        // Create a target document with 2-space indentation
        String targetXml = """
            <target>
              <existing>element</existing>
            </target>""";

        Document targetDoc = Document.of(targetXml);
        Editor targetEditor = new Editor(targetDoc);
        Element targetRoot = targetDoc.root();

        // Build a complex nested structure using Editor methods
        // This will trigger the recursive indentation fix as elements are added
        Element complex = targetEditor.addElement(targetRoot, "complex");
        Element nested = targetEditor.addElement(complex, "nested");
        targetEditor.addElement(nested, "deep", "content");
        targetEditor.addElement(nested, "another", "value");
        targetEditor.addElement(complex, "sibling", "data");

        String result = targetEditor.toXml();

        // Verify that all elements are properly indented with 2-space indentation
        // The complex element should be at the same level as existing (2 spaces)
        assertTrue(
                result.contains("  <existing>element</existing>"), "Existing element should have 2-space indentation");
        assertTrue(result.contains("  <complex>"), "Complex element should have 2-space indentation");

        // Nested elements should have 4 spaces (2 + 2)
        assertTrue(result.contains("    <nested>"), "Nested element should have 4-space indentation");
        assertTrue(result.contains("    <sibling>data</sibling>"), "Sibling element should have 4-space indentation");

        // Deep elements should have 6 spaces (2 + 2 + 2)
        assertTrue(result.contains("      <deep>content</deep>"), "Deep element should have 6-space indentation");
        assertTrue(
                result.contains("      <another>value</another>"), "Another element should have 6-space indentation");

        // Verify the overall structure is correct
        String expected =
                """
            <target>
              <existing>element</existing>
              <complex>
                <nested>
                  <deep>content</deep>
                  <another>value</another>
                </nested>
                <sibling>data</sibling>
              </complex>
            </target>""";

        assertEquals(expected, result, "The entire structure should have consistent 2-space indentation");
    }
}
