package eu.maveniverse.domtrip;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for parsing XML from InputStream with encoding detection.
 */
public class InputStreamParsingTest {

    @Test
    void testParseFromInputStreamWithUTF8() throws DomTripException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root><child>Hello World</child></root>";
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        
        Document doc = Document.of(inputStream);
        
        assertNotNull(doc);
        assertEquals("UTF-8", doc.encoding());
        assertEquals("1.0", doc.version());
        assertNotNull(doc.root());
        assertEquals("root", doc.root().name());
        assertEquals("Hello World", doc.root().child("child").orElseThrow().textContent());
    }

    @Test
    void testParseFromInputStreamWithUTF16() throws DomTripException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-16\"?>\n<root><child>Hello UTF-16</child></root>";
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_16));
        
        Document doc = Document.of(inputStream);
        
        assertNotNull(doc);
        assertEquals("UTF-16", doc.encoding());
        assertEquals("1.0", doc.version());
        assertNotNull(doc.root());
        assertEquals("root", doc.root().name());
        assertEquals("Hello UTF-16", doc.root().child("child").orElseThrow().textContent());
    }

    @Test
    void testParseFromInputStreamWithISO88591() throws DomTripException {
        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<root><child>Hello ISO</child></root>";
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.ISO_8859_1));
        
        Document doc = Document.of(inputStream, "ISO-8859-1");
        
        assertNotNull(doc);
        assertEquals("ISO-8859-1", doc.encoding());
        assertEquals("1.0", doc.version());
        assertNotNull(doc.root());
        assertEquals("root", doc.root().name());
        assertEquals("Hello ISO", doc.root().child("child").orElseThrow().textContent());
    }

    @Test
    void testParseFromInputStreamWithBOM() throws DomTripException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root><child>Hello BOM</child></root>";
        byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_8);
        
        // Add UTF-8 BOM
        byte[] bomBytes = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] xmlWithBom = new byte[bomBytes.length + xmlBytes.length];
        System.arraycopy(bomBytes, 0, xmlWithBom, 0, bomBytes.length);
        System.arraycopy(xmlBytes, 0, xmlWithBom, bomBytes.length, xmlBytes.length);
        
        InputStream inputStream = new ByteArrayInputStream(xmlWithBom);
        
        Document doc = Document.of(inputStream);
        
        assertNotNull(doc);
        assertEquals("UTF-8", doc.encoding());
        assertEquals("1.0", doc.version());
        assertNotNull(doc.root());
        assertEquals("root", doc.root().name());
        assertEquals("Hello BOM", doc.root().child("child").orElseThrow().textContent());
    }

    @Test
    void testParseFromInputStreamWithStandalone() throws DomTripException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<root><child>Standalone</child></root>";
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        
        Document doc = Document.of(inputStream);
        
        assertNotNull(doc);
        assertEquals("UTF-8", doc.encoding());
        assertEquals("1.0", doc.version());
        assertTrue(doc.isStandalone());
        assertNotNull(doc.root());
        assertEquals("root", doc.root().name());
        assertEquals("Standalone", doc.root().child("child").orElseThrow().textContent());
    }

    @Test
    void testParseFromInputStreamNoEncoding() throws DomTripException {
        String xml = "<?xml version=\"1.0\"?>\n<root><child>No encoding</child></root>";
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        
        Document doc = Document.of(inputStream);
        
        assertNotNull(doc);
        assertEquals("UTF-8", doc.encoding()); // Should default to UTF-8
        assertEquals("1.0", doc.version());
        assertNotNull(doc.root());
        assertEquals("root", doc.root().name());
        assertEquals("No encoding", doc.root().child("child").orElseThrow().textContent());
    }

    @Test
    void testParseFromInputStreamWithDefaultEncoding() throws DomTripException {
        String xml = "<root><child>Simple XML</child></root>"; // No XML declaration
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        
        Document doc = Document.of(inputStream, "UTF-8");
        
        assertNotNull(doc);
        assertEquals("UTF-8", doc.encoding());
        assertNotNull(doc.root());
        assertEquals("root", doc.root().name());
        assertEquals("Simple XML", doc.root().child("child").orElseThrow().textContent());
    }

    @Test
    void testParseFromInputStreamNullStream() {
        assertThrows(DomTripException.class, () -> {
            Document.of((InputStream) null);
        });
    }

    @Test
    void testParseFromInputStreamEmptyStream() {
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        
        assertThrows(DomTripException.class, () -> {
            Document.of(inputStream);
        });
    }

    @Test
    void testParseFromInputStreamPreservesFormatting() throws DomTripException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<root>\n" +
                     "  <child attr=\"value\">text content</child>\n" +
                     "  <!-- comment -->\n" +
                     "</root>";
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        
        Document doc = Document.of(inputStream);
        Editor editor = new Editor(doc);
        String result = editor.toXml();
        
        // Should preserve formatting
        assertTrue(result.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(result.contains("attr=\"value\""));
        assertTrue(result.contains("<!-- comment -->"));
        assertTrue(result.contains("text content"));
    }

    @Test
    void testParseFromInputStreamWithComplexContent() throws DomTripException {
        String xml = "<?xml version=\"1.1\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                     "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
                     "  <groupId>com.example</groupId>\n" +
                     "  <artifactId>test-project</artifactId>\n" +
                     "  <version>1.0.0</version>\n" +
                     "  <dependencies>\n" +
                     "    <dependency>\n" +
                     "      <groupId>junit</groupId>\n" +
                     "      <artifactId>junit</artifactId>\n" +
                     "      <version>4.13.2</version>\n" +
                     "      <scope>test</scope>\n" +
                     "    </dependency>\n" +
                     "  </dependencies>\n" +
                     "</project>";
        
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        
        Document doc = Document.of(inputStream);
        
        assertNotNull(doc);
        assertEquals("UTF-8", doc.encoding());
        assertEquals("1.1", doc.version());
        assertFalse(doc.isStandalone());
        assertNotNull(doc.root());
        assertEquals("project", doc.root().name());
        
        // Verify complex structure is preserved
        Element groupId = doc.root().child("groupId").orElseThrow();
        assertEquals("com.example", groupId.textContent());
        
        Element dependencies = doc.root().child("dependencies").orElseThrow();
        Element dependency = dependencies.child("dependency").orElseThrow();
        Element junitGroupId = dependency.child("groupId").orElseThrow();
        assertEquals("junit", junitGroupId.textContent());
    }

    @Test
    void testRoundTripInputStreamToOutputStream() throws DomTripException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root><child>Round trip test</child></root>";
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        // Parse from InputStream
        Document doc = Document.of(inputStream);

        // Serialize to OutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        doc.toXml(outputStream);

        // Verify round trip
        String result = outputStream.toString(StandardCharsets.UTF_8);
        assertEquals(xml, result);
    }

    @Test
    void testRoundTripWithDifferentEncodings() throws DomTripException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-16\"?>\n<root><child>UTF-16 test</child></root>";
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_16));

        // Parse from InputStream (should detect UTF-16)
        Document doc = Document.of(inputStream);
        assertEquals("UTF-16", doc.encoding());

        // Serialize to OutputStream with same encoding
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        doc.toXml(outputStream, "UTF-16");

        // Parse back to verify
        InputStream inputStream2 = new ByteArrayInputStream(outputStream.toByteArray());
        Document doc2 = Document.of(inputStream2);

        assertEquals("UTF-16", doc2.encoding());
        assertEquals("UTF-16 test", doc2.root().child("child").orElseThrow().textContent());
    }

    @Test
    void testRoundTripWithSpecialCharacters() throws DomTripException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root><child>Special: àáâãäå èéêë</child></root>";
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        // Parse from InputStream
        Document doc = Document.of(inputStream);

        // Serialize to OutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        doc.toXml(outputStream);

        // Verify special characters are preserved
        String result = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(result.contains("Special: àáâãäå èéêë"));
        assertEquals(xml, result);
    }

    @Test
    void testEncodingConsistencyAfterParsing() throws DomTripException {
        String xml = "<?xml version=\"1.1\" encoding=\"ISO-8859-1\" standalone=\"no\"?>\n<root><child>ISO test</child></root>";
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.ISO_8859_1));

        Document doc = Document.of(inputStream, "ISO-8859-1");

        // Verify all XML declaration attributes are parsed correctly
        assertEquals("1.1", doc.version());
        assertEquals("ISO-8859-1", doc.encoding());
        assertFalse(doc.isStandalone());

        // Verify serialization maintains encoding information
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        doc.toXml(outputStream, "ISO-8859-1");

        String result = outputStream.toString(StandardCharsets.ISO_8859_1);
        assertTrue(result.contains("encoding=\"ISO-8859-1\""));
        assertTrue(result.contains("standalone=\"no\""));
    }
}
