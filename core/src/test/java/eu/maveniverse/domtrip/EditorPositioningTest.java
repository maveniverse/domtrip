package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for element positioning functionality in Editor.
 */
class EditorPositioningTest {

    private Editor editor;

    @BeforeEach
    void setUp() {
        editor = new Editor();
    }

    @Test
    void testInsertElementAt() throws DomTripException {
        String xml =
                """
            <root>
                <first>content1</first>
                <third>content3</third>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element root = doc.root();

        // Find the position after the "first" element
        int insertPosition = -1;
        for (int i = 0; i < root.nodeCount(); i++) {
            Node node = root.getNode(i);
            if (node instanceof Element element && "first".equals(element.name())) {
                insertPosition = i + 1;
                break;
            }
        }
        assertTrue(insertPosition > 0, "Should find position after first element");

        Element second = editor.insertElementAt(root, insertPosition, "second");

        assertNotNull(second);
        assertEquals("second", second.name());
        assertEquals(root, second.parent());

        // Verify the complete XML structure
        String result = editor.toXml();
        String expected =
                """
            <root>
                <first>content1</first>
                <second></second>
                <third>content3</third>
            </root>""";
        assertEquals(expected, result);
    }

    @Test
    void testInsertElementAtWithTextContent() throws DomTripException {
        String xml = """
            <root>
                <first/>
                <third/>
            </root>""";
        String expected =
                """
            <root>
                <first/>
                <second>content2</second>
                <third/>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element root = doc.root();

        Element second = editor.insertElementAt(root, 1, "second", "content2");

        assertNotNull(second);
        assertEquals("second", second.name());
        assertEquals("content2", second.textContent());

        String result = editor.toXml();
        assertEquals(expected, result);
    }

    @Test
    void testInsertElementBefore() throws DomTripException {
        String xml =
                """
            <root>
                <existing>content</existing>
                <other>other</other>
            </root>""";
        String expected =
                """
            <root>
                <newElement></newElement>
                <existing>content</existing>
                <other>other</other>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element existing = doc.root().child("existing").orElseThrow();

        Element newElement = editor.insertElementBefore(existing, "newElement");

        assertNotNull(newElement);
        assertEquals("newElement", newElement.name());
        assertEquals(doc.root(), newElement.parent());

        // Verify the complete XML structure
        String result = editor.toXml();
        assertEquals(expected, result);
    }

    @Test
    void testInsertElementBeforeWithTextContent() throws DomTripException {
        String xml = """
            <root>
                <existing/>
            </root>""";
        String expected =
                """
            <root>
                <newElement>new content</newElement>
                <existing/>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element existing = doc.root().child("existing").orElseThrow();

        Element newElement = editor.insertElementBefore(existing, "newElement", "new content");

        assertNotNull(newElement);
        assertEquals("newElement", newElement.name());
        assertEquals("new content", newElement.textContent());

        String result = editor.toXml();
        assertEquals(expected, result);
    }

    @Test
    void testInsertElementAfter() throws DomTripException {
        String xml =
                """
            <root>
                <existing>content</existing>
                <other>other</other>
            </root>""";
        String expected =
                """
            <root>
                <existing>content</existing>
                <newElement></newElement>
                <other>other</other>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element existing = doc.root().child("existing").orElseThrow();

        Element newElement = editor.insertElementAfter(existing, "newElement");

        assertNotNull(newElement);
        assertEquals("newElement", newElement.name());
        assertEquals(doc.root(), newElement.parent());

        // Verify the complete XML structure
        String result = editor.toXml();
        assertEquals(expected, result);
    }

    @Test
    void testInsertElementAfterWithTextContent() throws DomTripException {
        String xml = """
            <root>
                <existing/>
            </root>""";
        String expected =
                """
            <root>
                <existing/>
                <newElement>new content</newElement>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element existing = doc.root().child("existing").orElseThrow();

        editor.insertElementAfter(existing, "newElement").textContent("new content");

        String result = editor.toXml();
        assertEquals(expected, result);
    }

    @Test
    void testInsertElementAtInvalidIndex() throws DomTripException {
        String xml = "<root><child/></root>";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element root = doc.root();

        // Test negative index
        assertThrows(DomTripException.class, () -> {
            editor.insertElementAt(root, -1, "invalid");
        });

        // Test index too large
        assertThrows(DomTripException.class, () -> {
            editor.insertElementAt(root, root.nodeCount() + 1, "invalid");
        });
    }

    @Test
    void testInsertElementBeforeNullReference() {
        assertThrows(DomTripException.class, () -> {
            editor.insertElementBefore(null, "newElement");
        });
    }

    @Test
    void testInsertElementAfterNullReference() {
        assertThrows(DomTripException.class, () -> {
            editor.insertElementAfter(null, "newElement");
        });
    }

    @Test
    void testInsertElementBeforeOrphanElement() throws DomTripException {
        Element orphan = new Element("orphan");

        assertThrows(DomTripException.class, () -> {
            editor.insertElementBefore(orphan, "newElement");
        });
    }

    @Test
    void testInsertElementAfterOrphanElement() throws DomTripException {
        Element orphan = new Element("orphan");

        assertThrows(DomTripException.class, () -> {
            editor.insertElementAfter(orphan, "newElement");
        });
    }

    @Test
    void testInsertElementAtBeginning() throws DomTripException {
        String xml =
                """
            <root>
                <second>content2</second>
                <third>content3</third>
            </root>""";
        String expected =
                """
            <root>
                <first>content1</first>
                <second>content2</second>
                <third>content3</third>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element root = doc.root();

        Element first = editor.insertElementAt(root, 0, "first", "content1");

        assertNotNull(first);
        assertEquals("first", first.name());
        assertEquals("content1", first.textContent());

        // Verify the complete XML structure
        String result = editor.toXml();
        assertEquals(expected, result);
    }

    @Test
    void testInsertElementAtEnd() throws DomTripException {
        String xml =
                """
            <root>
                <first>content1</first>
                <second>content2</second>
            </root>""";
        String expected =
                """
            <root>
                <first>content1</first>
                <second>content2</second>
                <third>content3</third>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element root = doc.root();

        Element third = editor.insertElementAt(root, root.nodeCount(), "third", "content3");

        assertNotNull(third);
        assertEquals("third", third.name());
        assertEquals("content3", third.textContent());

        // Verify the complete XML structure
        String result = editor.toXml();
        assertEquals(expected, result);
    }

    @Test
    void testInsertElementWithNullOrEmptyName() throws DomTripException {
        String xml = "<root><child/></root>";
        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element root = doc.root();

        assertThrows(DomTripException.class, () -> {
            editor.insertElementAt(root, 0, null);
        });

        assertThrows(DomTripException.class, () -> {
            editor.insertElementAt(root, 0, "");
        });

        assertThrows(DomTripException.class, () -> {
            editor.insertElementAt(root, 0, "   ");
        });
    }

    @Test
    void testComplexPositioningScenario() throws DomTripException {
        String xml =
                """
            <root>
                <dependencies>
                    <dependency>
                        <groupId>junit</groupId>
                        <artifactId>junit</artifactId>
                    </dependency>
                </dependencies>
            </root>""";
        String expected =
                """
            <root>
                <dependencies>
                    <dependency>
                        <groupId>org.mockito</groupId>
                        <artifactId>mockito-core</artifactId>
                    </dependency>
                    <dependency>
                        <groupId>junit</groupId>
                        <artifactId>junit</artifactId>
                    </dependency>
                    <dependency>
                        <groupId>org.hamcrest</groupId>
                        <artifactId>hamcrest-core</artifactId>
                    </dependency>
                </dependencies>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element dependencies = doc.root().child("dependencies").orElseThrow();
        Element existingDep = dependencies.child("dependency").orElseThrow();

        // Insert before existing dependency
        Element newDep1 = editor.insertElementBefore(existingDep, "dependency");
        editor.addElement(newDep1, "groupId", "org.mockito");
        editor.addElement(newDep1, "artifactId", "mockito-core");

        // Insert after existing dependency
        Element newDep2 = editor.insertElementAfter(existingDep, "dependency");
        editor.addElement(newDep2, "groupId", "org.hamcrest");
        editor.addElement(newDep2, "artifactId", "hamcrest-core");

        String result = editor.toXml();
        assertEquals(expected, result);
    }
}
