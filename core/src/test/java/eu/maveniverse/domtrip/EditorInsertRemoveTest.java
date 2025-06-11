package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for enhanced insert/remove functionality in Editor with intelligent whitespace handling.
 */
class EditorInsertRemoveTest {

    private Editor editor;

    @BeforeEach
    void setUp() {
        editor = new Editor();
    }

    @Test
    void testRemoveFirstElement() throws DomTripException {
        String xml =
                """
            <root>
                <first>content1</first>
                <second>content2</second>
                <third>content3</third>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element first = doc.root().child("first").orElseThrow();

        editor.removeElement(first);
        String result = editor.toXml();

        // Should remove first element and following whitespace
        assertFalse(result.contains("<first>"));
        assertTrue(result.contains("<second>"));
        assertTrue(result.contains("<third>"));

        // Should maintain proper formatting for remaining elements
        assertTrue(result.contains("    <second>"));
    }

    @Test
    void testRemoveLastElement() throws DomTripException {
        String xml =
                """
            <root>
                <first>content1</first>
                <second>content2</second>
                <third>content3</third>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element third = doc.root().child("third").orElseThrow();

        editor.removeElement(third);
        String result = editor.toXml();

        // Should remove last element and preceding whitespace
        assertFalse(result.contains("<third>"));
        assertTrue(result.contains("<first>"));
        assertTrue(result.contains("<second>"));

        // Should maintain proper formatting
        assertTrue(result.contains("    <second>"));
    }

    @Test
    void testRemoveMiddleElement() throws DomTripException {
        String xml =
                """
            <root>
                <first>content1</first>
                <second>content2</second>
                <third>content3</third>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element second = doc.root().child("second").orElseThrow();

        editor.removeElement(second);
        String result = editor.toXml();

        // Should remove middle element and following whitespace
        assertFalse(result.contains("<second>"));
        assertTrue(result.contains("<first>"));
        assertTrue(result.contains("<third>"));

        // Should maintain proper formatting
        assertTrue(result.contains("    <first>"));
        assertTrue(result.contains("    <third>"));
    }

    @Test
    void testRemoveOnlyElement() throws DomTripException {
        String xml = """
            <root>
                <only>content</only>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element only = doc.root().child("only").orElseThrow();

        editor.removeElement(only);
        String result = editor.toXml();

        // Should remove the only element
        assertFalse(result.contains("<only>"));
        assertTrue(result.contains("<root>"));
        assertTrue(result.contains("</root>"));
    }

    @Test
    void testRemoveElementWithBlankLines() throws DomTripException {
        String xml =
                """
            <root>
                <first>content1</first>

                <second>content2</second>

                <third>content3</third>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element second = doc.root().child("second").orElseThrow();

        editor.removeElement(second);
        String result = editor.toXml();

        // Should remove middle element and its whitespace
        assertFalse(result.contains("<second>"));
        assertTrue(result.contains("<first>"));
        assertTrue(result.contains("<third>"));
    }

    @Test
    void testAddElementWithBlankLineBefore() throws DomTripException {
        String xml = """
            <root>
                <existing>content</existing>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element root = doc.root();

        editor.addElement(root, "newElement", "newContent", true, false);
        String result = editor.toXml();

        // Should add element with blank line before
        assertTrue(result.contains("<existing>"));
        assertTrue(result.contains("<newElement>"));
        assertTrue(result.contains("newContent"));

        // Should have blank line before new element
        assertTrue(result.contains("</existing>\n\n"));
    }

    @Test
    void testAddElementWithBlankLineAfter() throws DomTripException {
        String xml = """
            <root>
                <existing>content</existing>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element root = doc.root();

        editor.addElement(root, "newElement", "newContent", false, true);
        String result = editor.toXml();

        // Should add element with blank line after
        assertTrue(result.contains("<existing>"));
        assertTrue(result.contains("<newElement>"));
        assertTrue(result.contains("newContent"));

        // Should have blank line after new element
        assertTrue(result.contains("</newElement>\n\n"));
    }

    @Test
    void testAddElementWithBothBlankLines() throws DomTripException {
        String xml = """
            <root>
                <existing>content</existing>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element root = doc.root();

        editor.addElement(root, "newElement", "newContent", true, true);
        String result = editor.toXml();

        // Should add element with blank lines before and after
        assertTrue(result.contains("<existing>"));
        assertTrue(result.contains("<newElement>"));
        assertTrue(result.contains("newContent"));

        // Should have blank lines before and after new element
        assertTrue(result.contains("</existing>\n\n"));
        assertTrue(result.contains("</newElement>\n\n"));
    }

    @Test
    void testAddElementWithQNameAndBlankLines() throws DomTripException {
        String xml =
                """
            <root xmlns:ns="http://example.com">
                <existing>content</existing>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element root = doc.root();

        QName qname = QName.of("http://example.com", "newElement", "ns");
        editor.addElement(root, qname, "newContent", true, false);
        String result = editor.toXml();

        // Should add namespaced element with blank line before
        assertTrue(result.contains("<existing>"));
        assertTrue(result.contains("ns:newElement"));
        assertTrue(result.contains("newContent"));

        // Should have blank line before new element
        assertTrue(result.contains("</existing>\n\n"));
    }

    @Test
    void testRemoveElementNullHandling() {
        // Test null element
        assertFalse(editor.removeElement(null));

        // Test element without parent
        Element orphan = new Element("orphan");
        assertFalse(editor.removeElement(orphan));
    }

    @Test
    void testAddElementNullHandling() {
        // Test null parent
        assertThrows(DomTripException.class, () -> editor.addElement(null, "test", false, false));

        // Test null element name
        Element parent = new Element("parent");
        assertThrows(DomTripException.class, () -> editor.addElement(parent, (String) null, false, false));

        // Test empty element name
        assertThrows(DomTripException.class, () -> editor.addElement(parent, "", false, false));
    }

    @Test
    void testComplexRemovalScenario() throws DomTripException {
        String xml =
                """
            <project>
                <groupId>com.example</groupId>
                <artifactId>test</artifactId>
                <version>1.0.0</version>

                <dependencies>
                    <dependency>
                        <groupId>junit</groupId>
                        <artifactId>junit</artifactId>
                        <version>4.13.2</version>
                    </dependency>

                    <dependency>
                        <groupId>mockito</groupId>
                        <artifactId>mockito-core</artifactId>
                        <version>4.6.1</version>
                    </dependency>
                </dependencies>
            </project>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);

        // Remove the first dependency
        Element dependencies = doc.root().child("dependencies").orElseThrow();
        Element firstDep = dependencies.child("dependency").orElseThrow();

        editor.removeElement(firstDep);
        String result = editor.toXml();

        // Should remove first dependency and maintain formatting
        assertFalse(result.contains("<groupId>junit</groupId>"));
        assertTrue(result.contains("<groupId>mockito</groupId>"));
        assertTrue(result.contains("<dependencies>"));
        assertTrue(result.contains("</dependencies>"));
    }
}
