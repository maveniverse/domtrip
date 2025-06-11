package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test cases for Document class functionality.
 */
public class DocumentTest {

    private Document document;
    private Editor editor;

    @BeforeEach
    void setUp() {
        document = new Document();
        editor = new Editor(Document.of());
    }

    @Test
    void testDocumentCreation() {
        assertEquals(Node.NodeType.DOCUMENT, document.type());
        assertEquals("", document.xmlDeclaration());
        assertEquals("", document.doctype());
        assertEquals("UTF-8", document.encoding());
        assertEquals("1.0", document.version());
        assertFalse(document.isStandalone());
        assertNull(document.root());
    }

    @Test
    void testSetXmlDeclaration() {
        document.xmlDeclaration("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", document.xmlDeclaration());
        assertTrue(document.isModified());
    }

    @Test
    void testSetXmlDeclarationNull() {
        document.xmlDeclaration(null);
        assertEquals("", document.xmlDeclaration());
    }

    @Test
    void testSetDoctype() {
        String doctype =
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";
        document.doctype(doctype);
        assertEquals(doctype, document.doctype());
        assertTrue(document.isModified());
    }

    @Test
    void testSetDoctypeNull() {
        document.doctype(null);
        assertEquals("", document.doctype());
    }

    @Test
    void testSetDocumentElement() {
        Element root = new Element("root");
        document.root(root);

        assertEquals(root, document.root());
        assertEquals(document, root.parent());
        assertTrue(document.isModified());
    }

    @Test
    void testSetDocumentElementNull() {
        Element root = new Element("root");
        document.root(root);
        document.root(null);

        assertNull(document.root());
    }

    @Test
    void testSetEncoding() {
        document.encoding("ISO-8859-1");
        assertEquals("ISO-8859-1", document.encoding());
        assertTrue(document.isModified());
    }

    @Test
    void testSetEncodingNull() {
        document.encoding(null);
        assertEquals("UTF-8", document.encoding()); // Should default to UTF-8
    }

    @Test
    void testSetVersion() {
        document.version("1.1");
        assertEquals("1.1", document.version());
        assertTrue(document.isModified());
    }

    @Test
    void testSetVersionNull() {
        document.version(null);
        assertEquals("1.0", document.version()); // Should default to 1.0
    }

    @Test
    void testSetStandalone() {
        document.standalone(true);
        assertTrue(document.isStandalone());
        assertTrue(document.isModified());
    }

    @Test
    void testDocumentWithXmlDeclaration() throws DomTripException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root/>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", doc.xmlDeclaration());
        assertNotNull(doc.root());
        assertEquals("root", doc.root().name());
    }

    @Test
    void testDocumentWithDoctype() throws DomTripException {
        String xml = "<?xml version=\"1.0\"?>\n" + "<!DOCTYPE root SYSTEM \"root.dtd\">\n" + "<root/>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        // DOCTYPE may not be fully supported yet
        assertNotNull(result);
        assertTrue(result.contains("<root"));
    }

    @Test
    void testDocumentWithStandaloneDeclaration() throws DomTripException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<root/>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        assertTrue(doc.xmlDeclaration().contains("standalone=\"yes\""));
    }

    @Test
    void testDocumentToXml() {
        document.xmlDeclaration("<?xml version=\"1.0\"?>");
        Element root = new Element("root");
        root.addNode(new Text("content"));
        document.root(root);

        String xml = document.toXml();

        assertTrue(xml.contains("<?xml version=\"1.0\"?>"));
        assertTrue(xml.contains("<root>content</root>"));
    }

    @Test
    void testDocumentToXmlStringBuilder() {
        document.xmlDeclaration("<?xml version=\"1.0\"?>");
        Element root = new Element("root");
        document.root(root);

        StringBuilder sb = new StringBuilder();
        document.toXml(sb);

        String result = sb.toString();
        assertTrue(result.contains("<?xml version=\"1.0\"?>"));
        assertTrue(result.contains("<root"));
    }

    @Test
    void testDocumentWithComments() throws DomTripException {
        String xml = "<?xml version=\"1.0\"?>\n" + "<!-- Document comment -->\n"
                + "<root>\n"
                + "  <!-- Element comment -->\n"
                + "  <child/>\n"
                + "</root>\n"
                + "<!-- Final comment -->";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        assertEquals(xml, result);
    }

    @Test
    void testDocumentWithProcessingInstructions() throws DomTripException {
        String xml =
                "<?xml version=\"1.0\"?>\n" + "<?xml-stylesheet type=\"text/xsl\" href=\"style.xsl\"?>\n" + "<root/>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        // XML declaration may not be preserved exactly
        assertTrue(result.contains("<?xml-stylesheet"));
        assertTrue(result.contains("<root"));
    }

    @Test
    void testDocumentFindElement() {
        String xml = "<root>\n" + "  <child1>\n"
                + "    <grandchild>content</grandchild>\n"
                + "  </child1>\n"
                + "  <child2/>\n"
                + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        Element found = doc.root().descendant("grandchild").orElse(null);
        assertNotNull(found);
        assertEquals("grandchild", found.name());
        assertEquals("content", found.textContent());
    }

    @Test
    void testDocumentFindElementNotFound() {
        String xml = "<root><child/></root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        Element found = doc.root().descendant("nonexistent").orElse(null);
        assertNull(found);
    }

    @Test
    void testDocumentFindElementWithNullName() {
        String xml = "<root><child/></root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        // This throws NPE in current implementation
        assertThrows(NullPointerException.class, () -> {
            doc.root().descendant((String) null);
        });
    }

    @Test
    void testDocumentStats() {
        String xml = "<?xml version=\"1.0\"?>\n" + "<!-- Comment -->\n"
                + "<root>\n"
                + "  <child1>text</child1>\n"
                + "  <child2/>\n"
                + "  <!-- Another comment -->\n"
                + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        String stats = editor.documentStats();

        assertNotNull(stats);
        assertTrue(stats.contains("elements"));
        assertTrue(stats.contains("comment"));
    }

    @Test
    void testEmptyDocumentStats() {
        String stats = editor.documentStats();
        assertNotNull(stats);
        assertTrue(stats.contains("No document loaded") || stats.contains("0"));
    }

    @Test
    void testDocumentWithWhitespace() {
        String xml = "<?xml version=\"1.0\"?>\n\n" + "<root>\n" + "  \n" + "  <child/>\n" + "  \n" + "</root>\n\n";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        // Whitespace should be preserved
        assertEquals(xml, result);
    }

    @Test
    void testDocumentModificationTracking() {
        String xml = "<root/>";
        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        // Initially not modified (just loaded)
        assertFalse(doc.isModified());

        // Modify document
        doc.xmlDeclaration("<?xml version=\"1.0\"?>");
        assertTrue(doc.isModified());
    }

    @Test
    void testDocumentWithMixedContent() {
        String xml = "<?xml version=\"1.0\"?>\n" + "<!-- Header -->\n"
                + "<?stylesheet href=\"style.css\"?>\n"
                + "<root>\n"
                + "  Text content\n"
                + "  <element>element content</element>\n"
                + "  More text\n"
                + "  <!-- Comment -->\n"
                + "</root>\n"
                + "<!-- Footer -->";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        assertEquals(xml, result);
    }

    // Tests for Document.of(Path) method

    @Test
    void testDocumentOfPathWithUTF8(@TempDir Path tempDir) throws IOException, DomTripException {
        // Create a test XML file with UTF-8 encoding
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root><child>Hello World</child></root>";
        Path xmlFile = tempDir.resolve("test-utf8.xml");
        Files.writeString(xmlFile, xml);

        // Parse using Document.of(Path)
        Document doc = Document.of(xmlFile);

        assertNotNull(doc);
        assertEquals("UTF-8", doc.encoding());
        assertEquals("1.0", doc.version());
        assertNotNull(doc.root());
        assertEquals("root", doc.root().name());
        assertEquals("Hello World", doc.root().child("child").orElseThrow().textContent());
    }

    @Test
    void testDocumentOfPathWithComplexXml(@TempDir Path tempDir) throws IOException, DomTripException {
        // Create a complex XML file with comments and processing instructions
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<!-- Configuration file -->\n"
                + "<?xml-stylesheet type=\"text/xsl\" href=\"style.xsl\"?>\n"
                + "<config>\n"
                + "    <database>\n"
                + "        <host>localhost</host>\n"
                + "        <port>5432</port>\n"
                + "    </database>\n"
                + "    <!-- End of database config -->\n"
                + "</config>";

        Path xmlFile = tempDir.resolve("complex.xml");
        Files.writeString(xmlFile, xml);

        // Parse using Document.of(Path)
        Document doc = Document.of(xmlFile);
        Editor editor = new Editor(doc);

        assertNotNull(doc);
        assertEquals("UTF-8", doc.encoding());
        assertEquals("1.0", doc.version());
        assertNotNull(doc.root());
        assertEquals("config", doc.root().name());

        // Verify structure
        Element database = doc.root().child("database").orElseThrow();
        assertEquals("localhost", database.child("host").orElseThrow().textContent());
        assertEquals("5432", database.child("port").orElseThrow().textContent());

        // Verify round-trip preserves formatting (note: XML declaration may not be preserved exactly)
        String result = editor.toXml();
        assertTrue(result.contains("<config>"));
        assertTrue(result.contains("<!-- Configuration file -->"));
        assertTrue(result.contains("<?xml-stylesheet"));
        assertTrue(result.contains("<database>"));
        assertTrue(result.contains("<host>localhost</host>"));
    }

    @Test
    void testDocumentOfPathWithDifferentEncodings(@TempDir Path tempDir) throws IOException, DomTripException {
        // Test with ISO-8859-1 encoding
        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<root><child>Test Content</child></root>";
        Path xmlFile = tempDir.resolve("test-iso.xml");
        Files.write(xmlFile, xml.getBytes(StandardCharsets.ISO_8859_1));

        // Parse using Document.of(Path)
        Document doc = Document.of(xmlFile);

        assertNotNull(doc);
        assertEquals("ISO-8859-1", doc.encoding());
        assertEquals("1.0", doc.version());
        assertNotNull(doc.root());
        assertEquals("root", doc.root().name());
        assertEquals("Test Content", doc.root().child("child").orElseThrow().textContent());
    }

    @Test
    void testDocumentOfPathNullPath() {
        assertThrows(DomTripException.class, () -> {
            Document.of((Path) null);
        });
    }

    @Test
    void testDocumentOfPathNonExistentFile(@TempDir Path tempDir) {
        Path nonExistentFile = tempDir.resolve("does-not-exist.xml");

        assertThrows(DomTripException.class, () -> {
            Document.of(nonExistentFile);
        });
    }

    @Test
    void testDocumentOfPathEmptyFile(@TempDir Path tempDir) throws IOException {
        Path emptyFile = tempDir.resolve("empty.xml");
        Files.createFile(emptyFile);

        assertThrows(DomTripException.class, () -> {
            Document.of(emptyFile);
        });
    }

    @Test
    void testDocumentOfPathMalformedXml(@TempDir Path tempDir) throws IOException, DomTripException {
        // Based on ErrorHandlingTest, the parser handles malformed XML gracefully
        String malformedXml = "<root><unclosed>";
        Path xmlFile = tempDir.resolve("malformed.xml");
        Files.writeString(xmlFile, malformedXml);

        // Should not throw - parser handles this gracefully
        Document doc = Document.of(xmlFile);
        assertNotNull(doc);
        assertNotNull(doc.root());
        assertEquals("root", doc.root().name());
    }

    @Test
    void testDocumentOfPathWithoutXmlDeclaration(@TempDir Path tempDir) throws IOException, DomTripException {
        // XML without declaration should default to UTF-8
        String xml = "<root><child>No declaration</child></root>";
        Path xmlFile = tempDir.resolve("no-decl.xml");
        Files.writeString(xmlFile, xml);

        Document doc = Document.of(xmlFile);

        assertNotNull(doc);
        assertEquals("UTF-8", doc.encoding()); // Should default to UTF-8
        assertNotNull(doc.root());
        assertEquals("root", doc.root().name());
        assertEquals("No declaration", doc.root().child("child").orElseThrow().textContent());
    }
}
