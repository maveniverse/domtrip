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
        String xml = """
            <root>
                <first>content1</first>
                <second>content2</second>
                <third>content3</third>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element first = doc.root().childElement("first").orElseThrow();

        editor.removeElement(first);
        String result = editor.toXml();

        String expected = """
            <root>
                <second>content2</second>
                <third>content3</third>
            </root>""";

        assertEquals(expected, result);
    }

    @Test
    void testRemoveLastElement() throws DomTripException {
        String xml = """
            <root>
                <first>content1</first>
                <second>content2</second>
                <third>content3</third>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element third = doc.root().childElement("third").orElseThrow();

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
        String xml = """
            <root>
                <first>content1</first>
                <second>content2</second>
                <third>content3</third>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element second = doc.root().childElement("second").orElseThrow();

        editor.removeElement(second);
        String result = editor.toXml();

        String expected = """
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
        Element only = doc.root().childElement("only").orElseThrow();

        editor.removeElement(only);
        String result = editor.toXml();

        String expected = """
            <root>
            </root>""";

        assertEquals(expected, result);
    }

    @Test
    void testRemoveElementWithBlankLines() throws DomTripException {
        String xml = """
            <root>
                <first>content1</first>

                <second>content2</second>

                <third>content3</third>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element second = doc.root().childElement("second").orElseThrow();

        editor.removeElement(second);
        String result = editor.toXml();

        String expected = """
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

        String expected = """
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

        String expected = """
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

        String expected = """
            <root>
                <existing>content</existing>

                <newElement>newContent</newElement>

            </root>""";

        assertEquals(expected, result);
    }

    @Test
    void testAddElementWithQNameAndBlankLines() throws DomTripException {
        String xml = """
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

        String expected = """
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

        // Test null QName
        assertThrows(DomTripException.class, () -> editor.addElement(parent, (QName) null));

        // Test null parent with QName
        QName qname = QName.of("http://example.com", "test", "ns");
        assertThrows(DomTripException.class, () -> editor.addElement(null, qname));
    }

    @Test
    void testComplexRemovalScenario() throws DomTripException {
        String xml = """
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
        Element dependencies = doc.root().childElement("dependencies").orElseThrow();
        Element firstDep = dependencies.childElement("dependency").orElseThrow();

        editor.removeElement(firstDep);
        String result = editor.toXml();

        String expected = """
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

    @Test
    void testRemoveMiddleElementTransfersWhitespace() throws DomTripException {
        String xml = "<root>\n  <a/>\n  <b/>\n  <c/>\n</root>";
        Document doc = Document.of(xml);
        Editor ed = new Editor(doc);

        // Remove middle element - should transfer whitespace properly
        Element b = doc.root().childElement("b").orElseThrow();
        ed.removeElement(b);

        String result = doc.toXml();
        // After removing middle element, remaining elements should still be properly formatted
        assertTrue(result.contains("<a/>"));
        assertTrue(result.contains("<c/>"));
        assertFalse(result.contains("<b/>"));
    }

    @Test
    void testRemoveMiddleElementWithNoNewlineInWhitespace() throws DomTripException {
        // Create a document programmatically with elements having no-newline whitespace
        Document doc = Document.of("<root><a/> <b/> <c/></root>");
        Editor ed = new Editor(doc);

        Element b = doc.root().childElement("b").orElseThrow();
        ed.removeElement(b);

        String result = doc.toXml();
        assertTrue(result.contains("<a/>"));
        assertTrue(result.contains("<c/>"));
        assertFalse(result.contains("<b/>"));
    }

    @Test
    void testIsTrulyRawFormattingWithSignificantWhitespace() throws DomTripException {
        // A document with significant whitespace (newlines, indentation)
        String xml = "<root>\n  <child>text</child>\n</root>";
        Document doc = Document.of(xml);
        Editor ed = new Editor(doc);

        // Should detect significant whitespace and not be "truly raw"
        // Adding an element should use proper formatting
        ed.insertElementAfter(doc.root().childElement("child").orElseThrow(), "child2");
        String result = doc.toXml();
        assertNotNull(result);
        assertTrue(result.contains("<child2"));
    }

    @Test
    void testToXmlPretty() throws DomTripException {
        String xml = "<root><child>text</child></root>";
        Document doc = Document.of(xml);
        Editor ed = new Editor(doc);
        String pretty = ed.toXmlPretty();
        assertNotNull(pretty);
        assertTrue(pretty.contains("<child>"));

        // null document editor
        Editor emptyEditor = new Editor();
        assertEquals("", emptyEditor.toXmlPretty());
    }

    @Test
    void testIsTrulyRawFormattingWithRawDocument() throws DomTripException {
        // A document with no whitespace at all (truly raw)
        String xml = "<root><child>text</child></root>";
        Document doc = Document.of(xml);
        Editor ed = new Editor(doc);

        ed.insertElementAfter(doc.root().childElement("child").orElseThrow(), "child2");
        String result = doc.toXml();
        assertNotNull(result);
        assertTrue(result.contains("<child2"));
    }

    @Test
    void testRemoveOnlyElementFromDocumentContainer() throws DomTripException {
        // Remove the root element from a Document container (not an Element container).
        // This exercises handleOnlyElementRemoval with container instanceof Element == false.
        Document doc = Document.of("<root/>");
        Editor ed = new Editor(doc);
        Element root = doc.root();

        // root's parent is Document (not Element), so handleOnlyElementRemoval
        // will hit the false branch of (container instanceof Element)
        boolean removed = ed.removeElement(root);
        assertTrue(removed, "Should successfully remove the root element");
    }

    @Test
    void testInsertNormalizesWhitespaceTextNodes() throws DomTripException {
        // Build a DOM with explicit whitespace Text nodes between elements.
        // normalizeWhitespaces should convert these to element whitespace properties.
        Document doc = Document.of("<parent><child1/></parent>");
        Element parent = doc.root();
        Element child1 = parent.childElement("child1").orElseThrow();

        // Manually add whitespace Text nodes to simulate programmatic DOM construction
        parent.addChild(new Text("\n    "));
        Element child2 = new Element("child2");
        parent.addChild(child2);
        parent.addChild(new Text("\n"));

        // Insert triggers normalizeWhitespaces, which should transfer Text whitespace
        Editor ed = new Editor(doc);
        ed.insertElementAfter(child1, "newChild");

        String result = doc.toXml();
        assertTrue(result.contains("<newChild"), "Inserted element should appear in output");
    }

    @Test
    void testInsertNormalizesTrailingWhitespaceTextNode() throws DomTripException {
        // Test the "last node" branch of transferWhitespaceAndRemove:
        // trailing whitespace Text node becomes innerPrecedingWhitespace
        Document doc = Document.of("<parent><child1/></parent>");
        Element parent = doc.root();
        Element child1 = parent.childElement("child1").orElseThrow();

        // Add only a trailing whitespace Text node (last child)
        parent.addChild(new Text("\n  "));

        Editor ed = new Editor(doc);
        ed.insertElementAfter(child1, "newChild");

        String result = doc.toXml();
        assertTrue(result.contains("<newChild"), "Inserted element should appear");
    }

    @Test
    void testInsertNormalizesMiddleWhitespaceTextNodeBeforeNonElement() throws DomTripException {
        // Test the "middle node followed by non-Element" branch of transferWhitespaceAndRemove
        // (returns false - text node is NOT removed)
        Document doc = Document.of("<parent><child1/></parent>");
        Element parent = doc.root();
        Element child1 = parent.childElement("child1").orElseThrow();

        // Add a whitespace Text node followed by another Text node (non-Element)
        parent.addChild(new Text("\n    "));
        parent.addChild(new Text("some content"));

        Editor ed = new Editor(doc);
        ed.insertElementAfter(child1, "newChild");

        String result = doc.toXml();
        assertTrue(result.contains("<newChild"), "Inserted element should appear");
    }

    @Test
    void testInsertIntoNonElementContainer() throws DomTripException {
        // Test normalizeWhitespaces early return when parent is not an Element (e.g., Document)
        Document doc = Document.of("<root/>");
        Editor ed = new Editor(doc);
        Element root = doc.root();

        // addElement on root (Element container) works normally; but we need to
        // test inserting into root when it has Text children to trigger normalize
        root.addChild(new Text("\n    "));
        Element existing = new Element("existing");
        root.addChild(existing);
        root.addChild(new Text("\n"));

        ed.addElement(root, "added");
        String result = doc.toXml();
        assertTrue(result.contains("<added"));
    }

    @Test
    void testSignificantWhitespaceDetection() throws DomTripException {
        // Test hasSignificantTextWhitespace: whitespace-only Text with newline content
        // This creates a document where a whitespace-only Text node has significant content
        // (not empty, not just a space) which should be detected as "significant"
        String xml = "<root>\n    <child>text</child>\n</root>";
        Document doc = Document.of(xml);
        Editor ed = new Editor(doc);

        // Inserting should detect significant whitespace (not "truly raw")
        Element child = doc.root().childElement("child").orElseThrow();
        Element added = ed.insertElementAfter(child, "child2");
        assertNotNull(added);

        String result = doc.toXml();
        assertTrue(result.contains("<child2"));
    }

    @Test
    void testElementWithOpenTagWhitespace() throws DomTripException {
        // Test hasSignificantElementWhitespace: element with openTagWhitespace
        String xml = "<root   >\n    <child>text</child>\n</root>";
        Document doc = Document.of(xml);
        Editor ed = new Editor(doc);

        // The root has openTagWhitespace "   ", which is significant
        Element child = doc.root().childElement("child").orElseThrow();
        Element added = ed.insertElementAfter(child, "child2");
        assertNotNull(added);
    }
}
