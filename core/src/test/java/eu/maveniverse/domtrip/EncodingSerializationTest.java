package eu.maveniverse.domtrip;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for encoding-aware serialization functionality.
 */
public class EncodingSerializationTest {

    @Test
    void testSerializeToOutputStreamWithDocumentEncoding() throws DomTripException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root><child>Hello World</child></root>";
        Document doc = Document.of(xml);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Serializer serializer = new Serializer();
        
        serializer.serialize(doc, outputStream);
        
        String result = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(result.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(result.contains("<root><child>Hello World</child></root>"));
    }

    @Test
    void testSerializeToOutputStreamWithSpecificEncoding() throws DomTripException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root><child>Hello World</child></root>";
        Document doc = Document.of(xml);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Serializer serializer = new Serializer();
        
        serializer.serialize(doc, outputStream, "ISO-8859-1");
        
        String result = outputStream.toString(StandardCharsets.ISO_8859_1);
        assertTrue(result.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(result.contains("<root><child>Hello World</child></root>"));
    }

    @Test
    void testSerializeToOutputStreamWithUTF16() throws DomTripException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-16\"?>\n<root><child>UTF-16 Content</child></root>";
        Document doc = Document.of(xml);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Serializer serializer = new Serializer();
        
        serializer.serialize(doc, outputStream, "UTF-16");
        
        String result = outputStream.toString(StandardCharsets.UTF_16);
        assertTrue(result.contains("<?xml version=\"1.0\" encoding=\"UTF-16\"?>"));
        assertTrue(result.contains("<root><child>UTF-16 Content</child></root>"));
    }

    @Test
    void testDocumentToXmlOutputStream() throws DomTripException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root><child>Document Method</child></root>";
        Document doc = Document.of(xml);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        doc.toXml(outputStream);
        
        String result = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(result.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(result.contains("<root><child>Document Method</child></root>"));
    }

    @Test
    void testDocumentToXmlOutputStreamWithEncoding() throws DomTripException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root><child>Custom Encoding</child></root>";
        Document doc = Document.of(xml);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        doc.toXml(outputStream, "ISO-8859-1");
        
        String result = outputStream.toString(StandardCharsets.ISO_8859_1);
        assertTrue(result.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(result.contains("<root><child>Custom Encoding</child></root>"));
    }

    @Test
    void testRoundTripWithDifferentEncodings() throws DomTripException {
        // Create document with UTF-8
        String originalXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root><child>Special chars: àáâãäå</child></root>";
        Document doc = Document.of(originalXml);
        
        // Serialize to UTF-16
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        doc.toXml(outputStream, "UTF-16");
        
        // Parse back from UTF-16
        byte[] utf16Bytes = outputStream.toByteArray();
        InputStream inputStream = new ByteArrayInputStream(utf16Bytes);
        Document parsedDoc = Document.of(inputStream);
        
        // Verify content is preserved
        assertEquals("UTF-16", parsedDoc.encoding());
        assertEquals("1.0", parsedDoc.version());
        assertNotNull(parsedDoc.root());
        assertEquals("root", parsedDoc.root().name());
        assertEquals("Special chars: àáâãäå", parsedDoc.root().child("child").orElseThrow().textContent());
    }

    @Test
    void testSerializeNodeToOutputStream() throws DomTripException {
        Element element = new Element("test");
        element.attribute("attr", "value");
        element.textContent("Node content");
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Serializer serializer = new Serializer();
        
        serializer.serialize(element, outputStream);
        
        String result = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(result.contains("<test attr=\"value\">Node content</test>"));
    }

    @Test
    void testSerializeNodeToOutputStreamWithEncoding() throws DomTripException {
        Element element = new Element("test");
        element.attribute("attr", "value with special chars: àáâ");
        element.textContent("Content with special chars: èéê");
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Serializer serializer = new Serializer();
        
        serializer.serialize(element, outputStream, "UTF-8");
        
        String result = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(result.contains("value with special chars: àáâ"));
        assertTrue(result.contains("Content with special chars: èéê"));
    }

    @Test
    void testSerializeWithNullOutputStream() {
        Document doc = Document.of("<root/>");
        Serializer serializer = new Serializer();
        
        // Should not throw exception, just return silently
        assertDoesNotThrow(() -> {
            serializer.serialize(doc, null);
        });
    }

    @Test
    void testSerializeWithNullDocument() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Serializer serializer = new Serializer();
        
        // Should not throw exception, just return silently
        assertDoesNotThrow(() -> {
            serializer.serialize(null, outputStream);
        });
        
        assertEquals(0, outputStream.size());
    }

    @Test
    void testSerializeWithInvalidEncoding() {
        Document doc = Document.of("<root/>");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Serializer serializer = new Serializer();
        
        assertThrows(DomTripException.class, () -> {
            serializer.serialize(doc, outputStream, "INVALID-ENCODING");
        });
    }

    @Test
    void testSerializeComplexDocumentWithEncoding() throws DomTripException {
        String xml = "<?xml version=\"1.1\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                     "<!-- Document comment -->\n" +
                     "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
                     "  <groupId>com.example</groupId>\n" +
                     "  <artifactId>test-project</artifactId>\n" +
                     "  <version>1.0.0</version>\n" +
                     "  <description>Project with special chars: àáâãäå</description>\n" +
                     "  <dependencies>\n" +
                     "    <dependency>\n" +
                     "      <groupId>junit</groupId>\n" +
                     "      <artifactId>junit</artifactId>\n" +
                     "      <version>4.13.2</version>\n" +
                     "      <scope>test</scope>\n" +
                     "    </dependency>\n" +
                     "  </dependencies>\n" +
                     "</project>";
        
        Document doc = Document.of(xml);
        
        // Test with different encodings
        String[] encodings = {"UTF-8", "UTF-16", "ISO-8859-1"};
        
        for (String encoding : encodings) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            doc.toXml(outputStream, encoding);
            
            // Verify output is not empty
            assertTrue(outputStream.size() > 0, "Output should not be empty for encoding: " + encoding);
            
            // For UTF-8 and UTF-16, verify special characters are preserved
            if ("UTF-8".equals(encoding) || "UTF-16".equals(encoding)) {
                String result = outputStream.toString(encoding);
                assertTrue(result.contains("àáâãäå"), "Special characters should be preserved in " + encoding);
            }
        }
    }

    @Test
    void testSerializeWithDefaultEncodingFallback() throws DomTripException {
        Document doc = new Document(); // No encoding set
        doc.root(new Element("root"));
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Serializer serializer = new Serializer();
        
        // Should use UTF-8 as default
        serializer.serialize(doc, outputStream);
        
        String result = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(result.contains("<root"));
    }

    @Test
    void testSerializePreservesFormattingWithEncoding() throws DomTripException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<root>\n" +
                     "  <child attr=\"value\">text content</child>\n" +
                     "  <!-- comment -->\n" +
                     "</root>";
        
        Document doc = Document.of(xml);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        doc.toXml(outputStream);
        
        String result = outputStream.toString(StandardCharsets.UTF_8);
        
        // Should preserve formatting
        assertTrue(result.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(result.contains("attr=\"value\""));
        assertTrue(result.contains("<!-- comment -->"));
        assertTrue(result.contains("text content"));
        assertEquals(xml, result);
    }
}
