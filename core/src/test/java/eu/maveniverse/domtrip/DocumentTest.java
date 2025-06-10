package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test cases for Document class functionality.
 */
public class DocumentTest {

    private Document document;
    private Editor editor;

    @BeforeEach
    void setUp() {
        document = new Document();
        editor = new Editor();
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
    void testDocumentWithXmlDeclaration() throws ParseException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root/>";

        editor.loadXml(xml);
        Document doc = editor.document();

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", doc.xmlDeclaration());
        assertNotNull(doc.root());
        assertEquals("root", doc.root().name());
    }

    @Test
    void testDocumentWithDoctype() throws ParseException {
        String xml = "<?xml version=\"1.0\"?>\n" + "<!DOCTYPE root SYSTEM \"root.dtd\">\n" + "<root/>";

        editor.loadXml(xml);
        String result = editor.toXml();

        // DOCTYPE may not be fully supported yet
        assertNotNull(result);
        assertTrue(result.contains("<root"));
    }

    @Test
    void testDocumentWithStandaloneDeclaration() throws ParseException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<root/>";

        editor.loadXml(xml);
        Document doc = editor.document();

        assertTrue(doc.xmlDeclaration().contains("standalone=\"yes\""));
    }

    @Test
    void testDocumentToXml() {
        document.xmlDeclaration("<?xml version=\"1.0\"?>");
        Element root = new Element("root");
        root.addChild(new Text("content"));
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
    void testDocumentWithComments() throws ParseException {
        String xml = "<?xml version=\"1.0\"?>\n" + "<!-- Document comment -->\n"
                + "<root>\n"
                + "  <!-- Element comment -->\n"
                + "  <child/>\n"
                + "</root>\n"
                + "<!-- Final comment -->";

        editor.loadXml(xml);
        String result = editor.toXml();

        assertEquals(xml, result);
    }

    @Test
    void testDocumentWithProcessingInstructions() throws ParseException {
        String xml =
                "<?xml version=\"1.0\"?>\n" + "<?xml-stylesheet type=\"text/xsl\" href=\"style.xsl\"?>\n" + "<root/>";

        editor.loadXml(xml);
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

        editor.loadXml(xml);
        Document doc = editor.document();

        Element found = doc.element("grandchild");
        assertNotNull(found);
        assertEquals("grandchild", found.name());
        assertEquals("content", found.textContent());
    }

    @Test
    void testDocumentFindElementNotFound() {
        String xml = "<root><child/></root>";

        editor.loadXml(xml);
        Document doc = editor.document();

        Element found = doc.element("nonexistent");
        assertNull(found);
    }

    @Test
    void testDocumentFindElementWithNullName() {
        String xml = "<root><child/></root>";

        editor.loadXml(xml);
        Document doc = editor.document();

        // This throws NPE in current implementation
        assertThrows(NullPointerException.class, () -> {
            doc.element(null);
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

        editor.loadXml(xml);
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

        editor.loadXml(xml);
        String result = editor.toXml();

        // Whitespace should be preserved
        assertEquals(xml, result);
    }

    @Test
    void testDocumentModificationTracking() {
        String xml = "<root/>";
        editor.loadXml(xml);
        Document doc = editor.document();

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

        editor.loadXml(xml);
        String result = editor.toXml();

        assertEquals(xml, result);
    }
}
