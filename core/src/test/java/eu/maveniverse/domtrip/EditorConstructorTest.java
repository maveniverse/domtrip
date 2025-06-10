package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Tests for Editor constructors, particularly the new Document-based constructors.
 */
class EditorConstructorTest {

    @Test
    void testDefaultConstructor() {
        Editor editor = new Editor();
        assertNotNull(editor);
        assertNull(editor.document());
        assertNotNull(editor.config());
    }

    @Test
    void testConfigConstructor() {
        DomTripConfig config = DomTripConfig.prettyPrint();
        Editor editor = new Editor(config);
        assertNotNull(editor);
        assertNull(editor.document());
        assertEquals(config, editor.config());
    }

    @Test
    void testStringConstructor() throws ParseException {
        String xml = "<?xml version=\"1.0\"?><root><child>value</child></root>";
        Editor editor = new Editor(Document.of(xml));
        assertNotNull(editor);
        assertNotNull(editor.document());
        assertEquals("root", editor.root().orElseThrow().name());
    }

    @Test
    void testStringWithConfigConstructor() throws ParseException {
        String xml = "<?xml version=\"1.0\"?><root><child>value</child></root>";
        DomTripConfig config = DomTripConfig.minimal();
        Editor editor = new Editor(Document.of(xml), config);
        assertNotNull(editor);
        assertNotNull(editor.document());
        assertEquals("root", editor.root().orElseThrow().name());
        assertEquals(config, editor.config());
    }

    @Test
    void testDocumentConstructor() {
        // Create a document programmatically
        Document doc = Document.of().root(new Element("project"));

        Editor editor = new Editor(doc);
        assertNotNull(editor);
        assertSame(doc, editor.document());
        assertEquals("project", editor.root().orElseThrow().name());
        assertNotNull(editor.config());
    }

    @Test
    void testDocumentWithConfigConstructor() {
        // Create a document programmatically
        Document doc = Document.of().root(new Element("maven")).version("1.1").encoding("UTF-16");

        DomTripConfig config = DomTripConfig.prettyPrint().withIndentString("  ");

        Editor editor = new Editor(doc, config);
        assertNotNull(editor);
        assertSame(doc, editor.document());
        assertEquals("maven", editor.root().orElseThrow().name());
        assertEquals(config, editor.config());
    }

    @Test
    void testWorkingWithExistingDocument() throws ParseException {
        // Parse with Parser directly
        Parser parser = new Parser();
        String xml = "<?xml version=\"1.0\"?><config><database><host>localhost</host></database></config>";
        Document doc = parser.parse(xml);

        // Create Editor from existing Document
        Editor editor = new Editor(doc);

        // Verify we can use the Editor API
        Element root = editor.root().orElseThrow();
        assertEquals("config", root.name());

        Optional<Element> database = doc.root().descendant("database");
        assertTrue(database.isPresent());

        // Add a new element
        editor.addElement(database.orElseThrow(), "port", "5432");

        // Verify the change
        Optional<Element> port = doc.root().descendant("port");
        assertTrue(port.isPresent());
        assertEquals("5432", port.orElseThrow().textContent());

        // Verify serialization works
        String result = editor.toXml();
        assertTrue(result.contains("<port>5432</port>"));
    }

    @Test
    void testBuilderCreatedDocumentWithEditor() {
        // Create document with builder
        Document doc = Document.of()
                .root(new Element("project"))
                .version("1.0")
                .encoding("UTF-8")
                .withXmlDeclaration();

        // Create Editor with custom config
        DomTripConfig config = DomTripConfig.prettyPrint().withIndentString("    ");
        Editor editor = new Editor(doc, config);

        // Build document structure using Editor
        Element root = editor.root().orElseThrow();
        editor.addElement(root, "groupId", "com.example");
        editor.addElement(root, "artifactId", "my-project");
        editor.addElement(root, "version", "1.0.0");

        // Verify structure
        assertEquals(
                "com.example", doc.root().descendant("groupId").orElseThrow().textContent());
        assertEquals(
                "my-project", doc.root().descendant("artifactId").orElseThrow().textContent());
        assertEquals("1.0.0", doc.root().descendant("version").orElseThrow().textContent());

        // Verify serialization with pretty printing
        String result = editor.toXml();
        assertTrue(result.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(result.contains("<project>"));
        assertTrue(result.contains("<groupId>com.example</groupId>"));
    }
}
