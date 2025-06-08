package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for Editor constructors, particularly the new Document-based constructors.
 */
class EditorConstructorTest {

    @Test
    void testDefaultConstructor() {
        Editor editor = new Editor();
        assertNotNull(editor);
        assertNull(editor.getDocument());
        assertNotNull(editor.getConfig());
    }

    @Test
    void testConfigConstructor() {
        DomTripConfig config = DomTripConfig.prettyPrint();
        Editor editor = new Editor(config);
        assertNotNull(editor);
        assertNull(editor.getDocument());
        assertEquals(config, editor.getConfig());
    }

    @Test
    void testStringConstructor() throws ParseException {
        String xml = "<?xml version=\"1.0\"?><root><child>value</child></root>";
        Editor editor = new Editor(xml);
        assertNotNull(editor);
        assertNotNull(editor.getDocument());
        assertEquals("root", editor.getRootElement().getName());
    }

    @Test
    void testStringWithConfigConstructor() throws ParseException {
        String xml = "<?xml version=\"1.0\"?><root><child>value</child></root>";
        DomTripConfig config = DomTripConfig.minimal();
        Editor editor = new Editor(xml, config);
        assertNotNull(editor);
        assertNotNull(editor.getDocument());
        assertEquals("root", editor.getRootElement().getName());
        assertEquals(config, editor.getConfig());
    }

    @Test
    void testDocumentConstructor() {
        // Create a document programmatically
        Document doc = Document.builder()
                .withRootElement("project")
                .withXmlDeclaration()
                .build();

        Editor editor = new Editor(doc);
        assertNotNull(editor);
        assertSame(doc, editor.getDocument());
        assertEquals("project", editor.getRootElement().getName());
        assertNotNull(editor.getConfig());
    }

    @Test
    void testDocumentWithConfigConstructor() {
        // Create a document programmatically
        Document doc = Document.builder()
                .withRootElement("maven")
                .withVersion("1.1")
                .withEncoding("UTF-16")
                .build();

        DomTripConfig config = DomTripConfig.prettyPrint().withIndentString("  ");

        Editor editor = new Editor(doc, config);
        assertNotNull(editor);
        assertSame(doc, editor.getDocument());
        assertEquals("maven", editor.getRootElement().getName());
        assertEquals(config, editor.getConfig());
    }

    @Test
    void testWorkingWithExistingDocument() throws ParseException {
        // Parse with Parser directly
        Parser parser = new Parser();
        String xml = "<?xml version=\"1.0\"?><config><database><host>localhost</host></database></config>";
        Document document = parser.parse(xml);

        // Create Editor from existing Document
        Editor editor = new Editor(document);

        // Verify we can use the Editor API
        Element root = editor.getRootElement();
        assertEquals("config", root.getName());

        Element database = editor.findElement("database");
        assertNotNull(database);

        // Add a new element
        editor.addElement(database, "port", "5432");

        // Verify the change
        Element port = editor.findElement("port");
        assertNotNull(port);
        assertEquals("5432", port.getTextContent());

        // Verify serialization works
        String result = editor.toXml();
        assertTrue(result.contains("<port>5432</port>"));
    }

    @Test
    void testBuilderCreatedDocumentWithEditor() {
        // Create document with builder
        Document doc = Document.builder()
                .withRootElement("project")
                .withVersion("1.0")
                .withEncoding("UTF-8")
                .withXmlDeclaration()
                .build();

        // Create Editor with custom config
        DomTripConfig config = DomTripConfig.prettyPrint().withIndentString("    ");
        Editor editor = new Editor(doc, config);

        // Build document structure using Editor
        Element root = editor.getRootElement();
        editor.addElement(root, "groupId", "com.example");
        editor.addElement(root, "artifactId", "my-project");
        editor.addElement(root, "version", "1.0.0");

        // Verify structure
        assertEquals("com.example", editor.findElement("groupId").getTextContent());
        assertEquals("my-project", editor.findElement("artifactId").getTextContent());
        assertEquals("1.0.0", editor.findElement("version").getTextContent());

        // Verify serialization with pretty printing
        String result = editor.toXml();
        assertTrue(result.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(result.contains("<project>"));
        assertTrue(result.contains("<groupId>com.example</groupId>"));
    }
}
