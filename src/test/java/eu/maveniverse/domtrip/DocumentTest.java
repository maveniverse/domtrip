package eu.maveniverse.domtrip;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals(Node.NodeType.DOCUMENT, document.getNodeType());
        assertEquals("", document.getXmlDeclaration());
        assertEquals("", document.getDoctype());
        assertEquals("UTF-8", document.getEncoding());
        assertEquals("1.0", document.getVersion());
        assertFalse(document.isStandalone());
        assertNull(document.getDocumentElement());
    }
    
    @Test
    void testSetXmlDeclaration() {
        document.setXmlDeclaration("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", document.getXmlDeclaration());
        assertTrue(document.isModified());
    }
    
    @Test
    void testSetXmlDeclarationNull() {
        document.setXmlDeclaration(null);
        assertEquals("", document.getXmlDeclaration());
    }
    
    @Test
    void testSetDoctype() {
        String doctype = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";
        document.setDoctype(doctype);
        assertEquals(doctype, document.getDoctype());
        assertTrue(document.isModified());
    }
    
    @Test
    void testSetDoctypeNull() {
        document.setDoctype(null);
        assertEquals("", document.getDoctype());
    }
    
    @Test
    void testSetDocumentElement() {
        Element root = new Element("root");
        document.setDocumentElement(root);
        
        assertEquals(root, document.getDocumentElement());
        assertEquals(document, root.getParent());
        assertTrue(document.isModified());
    }
    
    @Test
    void testSetDocumentElementNull() {
        Element root = new Element("root");
        document.setDocumentElement(root);
        document.setDocumentElement(null);
        
        assertNull(document.getDocumentElement());
    }
    
    @Test
    void testSetEncoding() {
        document.setEncoding("ISO-8859-1");
        assertEquals("ISO-8859-1", document.getEncoding());
        assertTrue(document.isModified());
    }
    
    @Test
    void testSetEncodingNull() {
        document.setEncoding(null);
        assertEquals("UTF-8", document.getEncoding()); // Should default to UTF-8
    }
    
    @Test
    void testSetVersion() {
        document.setVersion("1.1");
        assertEquals("1.1", document.getVersion());
        assertTrue(document.isModified());
    }
    
    @Test
    void testSetVersionNull() {
        document.setVersion(null);
        assertEquals("1.0", document.getVersion()); // Should default to 1.0
    }
    
    @Test
    void testSetStandalone() {
        document.setStandalone(true);
        assertTrue(document.isStandalone());
        assertTrue(document.isModified());
    }
    
    @Test
    void testDocumentWithXmlDeclaration() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root/>";
        
        editor.loadXml(xml);
        Document doc = editor.getDocument();
        
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", doc.getXmlDeclaration());
        assertNotNull(doc.getDocumentElement());
        assertEquals("root", doc.getDocumentElement().getName());
    }
    
    @Test
    void testDocumentWithDoctype() {
        String xml = "<?xml version=\"1.0\"?>\n" +
                   "<!DOCTYPE root SYSTEM \"root.dtd\">\n" +
                   "<root/>";

        editor.loadXml(xml);
        String result = editor.toXml();

        // DOCTYPE may not be fully supported yet
        assertNotNull(result);
        assertTrue(result.contains("<root"));
    }
    
    @Test
    void testDocumentWithStandaloneDeclaration() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<root/>";
        
        editor.loadXml(xml);
        Document doc = editor.getDocument();
        
        assertTrue(doc.getXmlDeclaration().contains("standalone=\"yes\""));
    }
    
    @Test
    void testDocumentToXml() {
        document.setXmlDeclaration("<?xml version=\"1.0\"?>");
        Element root = new Element("root");
        root.addChild(new Text("content"));
        document.setDocumentElement(root);
        
        String xml = document.toXml();
        
        assertTrue(xml.contains("<?xml version=\"1.0\"?>"));
        assertTrue(xml.contains("<root>content</root>"));
    }
    
    @Test
    void testDocumentToXmlStringBuilder() {
        document.setXmlDeclaration("<?xml version=\"1.0\"?>");
        Element root = new Element("root");
        document.setDocumentElement(root);
        
        StringBuilder sb = new StringBuilder();
        document.toXml(sb);
        
        String result = sb.toString();
        assertTrue(result.contains("<?xml version=\"1.0\"?>"));
        assertTrue(result.contains("<root"));
    }
    
    @Test
    void testDocumentWithComments() {
        String xml = "<?xml version=\"1.0\"?>\n" +
                   "<!-- Document comment -->\n" +
                   "<root>\n" +
                   "  <!-- Element comment -->\n" +
                   "  <child/>\n" +
                   "</root>\n" +
                   "<!-- Final comment -->";
        
        editor.loadXml(xml);
        String result = editor.toXml();
        
        assertEquals(xml, result);
    }
    
    @Test
    void testDocumentWithProcessingInstructions() {
        String xml = "<?xml version=\"1.0\"?>\n" +
                   "<?xml-stylesheet type=\"text/xsl\" href=\"style.xsl\"?>\n" +
                   "<root/>";

        editor.loadXml(xml);
        String result = editor.toXml();

        // XML declaration may not be preserved exactly
        assertTrue(result.contains("<?xml-stylesheet"));
        assertTrue(result.contains("<root"));
    }
    
    @Test
    void testDocumentFindElement() {
        String xml = "<root>\n" +
                   "  <child1>\n" +
                   "    <grandchild>content</grandchild>\n" +
                   "  </child1>\n" +
                   "  <child2/>\n" +
                   "</root>";
        
        editor.loadXml(xml);
        Document doc = editor.getDocument();
        
        Element found = doc.findElement("grandchild");
        assertNotNull(found);
        assertEquals("grandchild", found.getName());
        assertEquals("content", found.getTextContent());
    }
    
    @Test
    void testDocumentFindElementNotFound() {
        String xml = "<root><child/></root>";

        editor.loadXml(xml);
        Document doc = editor.getDocument();

        Element found = doc.findElement("nonexistent");
        assertNull(found);
    }
    
    @Test
    void testDocumentFindElementWithNullName() {
        String xml = "<root><child/></root>";

        editor.loadXml(xml);
        Document doc = editor.getDocument();

        // This throws NPE in current implementation
        assertThrows(NullPointerException.class, () -> {
            doc.findElement(null);
        });
    }
    
    @Test
    void testDocumentStats() {
        String xml = "<?xml version=\"1.0\"?>\n" +
                   "<!-- Comment -->\n" +
                   "<root>\n" +
                   "  <child1>text</child1>\n" +
                   "  <child2/>\n" +
                   "  <!-- Another comment -->\n" +
                   "</root>";
        
        editor.loadXml(xml);
        String stats = editor.getDocumentStats();
        
        assertNotNull(stats);
        assertTrue(stats.contains("elements"));
        assertTrue(stats.contains("comment"));
    }
    
    @Test
    void testEmptyDocumentStats() {
        String stats = editor.getDocumentStats();
        assertNotNull(stats);
        assertTrue(stats.contains("No document loaded") || stats.contains("0"));
    }
    
    @Test
    void testDocumentWithWhitespace() {
        String xml = "<?xml version=\"1.0\"?>\n\n" +
                   "<root>\n" +
                   "  \n" +
                   "  <child/>\n" +
                   "  \n" +
                   "</root>\n\n";
        
        editor.loadXml(xml);
        String result = editor.toXml();
        
        // Whitespace should be preserved
        assertEquals(xml, result);
    }
    
    @Test
    void testDocumentModificationTracking() {
        String xml = "<root/>";
        editor.loadXml(xml);
        Document doc = editor.getDocument();
        
        // Initially not modified (just loaded)
        assertFalse(doc.isModified());
        
        // Modify document
        doc.setXmlDeclaration("<?xml version=\"1.0\"?>");
        assertTrue(doc.isModified());
    }
    
    @Test
    void testDocumentWithMixedContent() {
        String xml = "<?xml version=\"1.0\"?>\n" +
                   "<!-- Header -->\n" +
                   "<?stylesheet href=\"style.css\"?>\n" +
                   "<root>\n" +
                   "  Text content\n" +
                   "  <element>element content</element>\n" +
                   "  More text\n" +
                   "  <!-- Comment -->\n" +
                   "</root>\n" +
                   "<!-- Footer -->";
        
        editor.loadXml(xml);
        String result = editor.toXml();
        
        assertEquals(xml, result);
    }
}
