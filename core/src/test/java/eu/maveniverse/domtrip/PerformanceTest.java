package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test cases for performance optimizations like toXml(StringBuilder).
 */
public class PerformanceTest {

    private Editor editor;

    @BeforeEach
    void setUp() {
        editor = new Editor(Document.of());
    }

    @Test
    void testToXmlStringBuilderMethod() {
        String xml = "<root>\n" + "  <child attr='value'>content</child>\n" + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        // Test that toXml(StringBuilder) produces same result as toXml()
        String directResult = doc.toXml();

        StringBuilder sb = new StringBuilder();
        doc.toXml(sb);
        String builderResult = sb.toString();

        assertEquals(directResult, builderResult);
    }

    @Test
    void testElementToXmlStringBuilder() {
        String xml = "<element attr1='value1' attr2=\"value2\">text content</element>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element element = editor.root().orElseThrow();

        // Test element's toXml(StringBuilder) method
        String directResult = element.toXml();

        StringBuilder sb = new StringBuilder();
        element.toXml(sb);
        String builderResult = sb.toString();

        assertEquals(directResult, builderResult);
    }

    @Test
    void testTextNodeToXmlStringBuilder() {
        String xml = "<root>Text with &lt;entities&gt;</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element root = editor.root().orElseThrow();
        Text textNode = (Text) root.getNode(0);

        // Test text node's toXml(StringBuilder) method
        String directResult = textNode.toXml();

        StringBuilder sb = new StringBuilder();
        textNode.toXml(sb);
        String builderResult = sb.toString();

        assertEquals(directResult, builderResult);
    }

    @Test
    void testCommentToXmlStringBuilder() {
        String xml = "<root><!-- This is a comment --></root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element root = editor.root().orElseThrow();
        Comment comment = (Comment) root.getNode(0);

        // Test comment's toXml(StringBuilder) method
        String directResult = comment.toXml();

        StringBuilder sb = new StringBuilder();
        comment.toXml(sb);
        String builderResult = sb.toString();

        assertEquals(directResult, builderResult);
    }

    @Test
    void testLargeDocumentPerformance() {
        // Create a moderately large document
        editor.createDocument("root");
        Element root = editor.root().orElseThrow();

        // Add many elements
        for (int i = 0; i < 100; i++) {
            Element child = editor.addElement(root, "item" + i, "content " + i);
            child.attribute("id", String.valueOf(i));
            child.attribute("type", "test");
        }

        // Measure time for both methods (not a strict performance test, just ensuring they work)
        long start1 = System.nanoTime();
        String result1 = editor.toXml();
        long time1 = System.nanoTime() - start1;

        long start2 = System.nanoTime();
        StringBuilder sb = new StringBuilder();
        editor.document().toXml(sb);
        String result2 = sb.toString();
        long time2 = System.nanoTime() - start2;

        // Results should be identical
        assertEquals(result1, result2);

        // Both should complete in reasonable time (less than 100ms)
        assertTrue(time1 < 100_000_000, "Direct method took too long: " + time1 + "ns");
        assertTrue(time2 < 100_000_000, "StringBuilder method took too long: " + time2 + "ns");

        // Verify the document structure
        assertTrue(result1.contains("<item0"));
        assertTrue(result1.contains("<item99"));
        assertTrue(result1.contains("id=\"0\""));
        assertTrue(result1.contains("id=\"99\""));
    }

    @Test
    void testNestedStringBuilderCalls() {
        String xml = "<root>\n" + "  <parent>\n" + "    <child>content</child>\n" + "  </parent>\n" + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        // Test that nested calls to toXml(StringBuilder) work correctly
        StringBuilder sb = new StringBuilder();
        sb.append("PREFIX:");
        editor.document().toXml(sb);
        sb.append(":SUFFIX");

        String result = sb.toString();

        assertTrue(result.startsWith("PREFIX:"));
        assertTrue(result.endsWith(":SUFFIX"));
        assertTrue(result.contains("<root>"));
        assertTrue(result.contains("<parent>"));
        assertTrue(result.contains("<child>content</child>"));
    }
}
