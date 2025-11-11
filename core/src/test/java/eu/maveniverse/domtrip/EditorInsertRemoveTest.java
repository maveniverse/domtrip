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

        String expected =
                """
            <root>
                <second>content2</second>
                <third>content3</third>
            </root>""";

        assertEquals(expected, result);
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

        String expected =
                """
            <root>
                <first>content1</first>
                <third>content3</third>
            </root>""";

        assertEquals(expected, result);
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

        String expected = """
            <root>
            </root>""";

        assertEquals(expected, result);
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

        String expected =
                """
            <root>
                <first>content1</first>

                <third>content3</third>
            </root>""";

        assertEquals(expected, result);
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

        Element newElement = editor.addElement(root, "newElement", "newContent");
        editor.addBlankLineBefore(newElement);
        String result = editor.toXml();

        String expected =
                """
            <root>
                <existing>content</existing>

                <newElement>newContent</newElement>
            </root>""";

        assertEquals(expected, result);
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

        Element newElement = editor.addElement(root, "newElement", "newContent");
        editor.addBlankLineAfter(newElement);
        String result = editor.toXml();

        String expected =
                """
            <root>
                <existing>content</existing>
                <newElement>newContent</newElement>

            </root>""";

        assertEquals(expected, result);
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

        Element newElement = editor.addElement(root, "newElement", "newContent");
        editor.addBlankLineBefore(newElement);
        editor.addBlankLineAfter(newElement);
        String result = editor.toXml();

        String expected =
                """
            <root>
                <existing>content</existing>

                <newElement>newContent</newElement>

            </root>""";

        assertEquals(expected, result);
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
        Element newElement = editor.addElement(root, qname, "newContent");
        editor.addBlankLineBefore(newElement);
        String result = editor.toXml();

        String expected =
                """
            <root xmlns:ns="http://example.com">
                <existing>content</existing>

                <ns:newElement>newContent</ns:newElement>
            </root>""";

        assertEquals(expected, result);
    }

    @Test
    void testRemoveElementNullHandling() throws DomTripException {
        // Test null element
        assertFalse(editor.removeElement(null));

        // Test element without parent
        Element orphan = new Element("orphan");
        assertFalse(editor.removeElement(orphan));
    }

    @Test
    void testAddElementNullHandling() throws DomTripException {
        // Test null parent
        assertThrows(DomTripException.class, () -> editor.addElement(null, "test"));

        // Test null element name
        Element parent = new Element("parent");
        assertThrows(DomTripException.class, () -> editor.addElement(parent, (String) null));

        // Test empty element name
        assertThrows(DomTripException.class, () -> editor.addElement(parent, ""));
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

        String expected =
                """
            <project>
                <groupId>com.example</groupId>
                <artifactId>test</artifactId>
                <version>1.0.0</version>

                <dependencies>
                    <dependency>
                        <groupId>mockito</groupId>
                        <artifactId>mockito-core</artifactId>
                        <version>4.6.1</version>
                    </dependency>
                </dependencies>
            </project>""";

        assertEquals(expected, result);
    }
}
