package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

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
        assertEquals(
                "Hello World", doc.root().childElement("child").orElseThrow().textContent());
    }

    @Test
    void testParseFromInputStreamWithUTF16() throws DomTripException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-16\"?>\n<root><child>Hello UTF-16</child></root>";
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_16));

        Document doc = Document.of(inputStream);

        assertNotNull(doc);
        assertTrue(doc.encoding().startsWith("UTF-16"), "Expected UTF-16 encoding but got: " + doc.encoding());
        assertEquals("1.0", doc.version());
        assertNotNull(doc.root());
        assertEquals("root", doc.root().name());
        assertEquals(
                "Hello UTF-16", doc.root().childElement("child").orElseThrow().textContent());
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
        assertEquals("Hello ISO", doc.root().childElement("child").orElseThrow().textContent());
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
        assertEquals("Hello BOM", doc.root().childElement("child").orElseThrow().textContent());
    }

    @Test
    void testParseFromInputStreamWithStandalone() throws DomTripException {
        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<root><child>Standalone</child></root>";
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        Document doc = Document.of(inputStream);

        assertNotNull(doc);
        assertEquals("UTF-8", doc.encoding());
        assertEquals("1.0", doc.version());
        assertTrue(doc.isStandalone());
        assertNotNull(doc.root());
        assertEquals("root", doc.root().name());
        assertEquals(
                "Standalone", doc.root().childElement("child").orElseThrow().textContent());
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
        assertEquals(
                "No encoding", doc.root().childElement("child").orElseThrow().textContent());
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
        assertEquals(
                "Simple XML", doc.root().childElement("child").orElseThrow().textContent());
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
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<root>\n"
                + "  <child attr=\"value\">text content</child>\n"
                + "  <!-- comment -->\n"
                + "</root>";
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
        String xml = "<?xml version=\"1.1\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
                + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n"
                + "  <groupId>com.example</groupId>\n"
                + "  <artifactId>test-project</artifactId>\n"
                + "  <version>1.0.0</version>\n"
                + "  <dependencies>\n"
                + "    <dependency>\n"
                + "      <groupId>junit</groupId>\n"
                + "      <artifactId>junit</artifactId>\n"
                + "      <version>4.13.2</version>\n"
                + "      <scope>test</scope>\n"
                + "    </dependency>\n"
                + "  </dependencies>\n"
                + "</project>";

        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        Document doc = Document.of(inputStream);

        assertNotNull(doc);
        assertEquals("UTF-8", doc.encoding());
        assertEquals("1.1", doc.version());
        assertFalse(doc.isStandalone());
        assertNotNull(doc.root());
        assertEquals("project", doc.root().name());

        // Verify complex structure is preserved
        Element groupId = doc.root().childElement("groupId").orElseThrow();
        assertEquals("com.example", groupId.textContent());

        Element dependencies = doc.root().childElement("dependencies").orElseThrow();
        Element dependency = dependencies.childElement("dependency").orElseThrow();
        Element junitGroupId = dependency.childElement("groupId").orElseThrow();
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
        assertTrue(doc.encoding().startsWith("UTF-16"), "Expected UTF-16 encoding but got: " + doc.encoding());

        // Serialize to OutputStream with same encoding
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        doc.toXml(outputStream, "UTF-16");

        // Parse back to verify
        InputStream inputStream2 = new ByteArrayInputStream(outputStream.toByteArray());
        Document doc2 = Document.of(inputStream2);

        assertTrue(doc2.encoding().startsWith("UTF-16"), "Expected UTF-16 encoding but got: " + doc2.encoding());
        assertEquals(
                "UTF-16 test", doc2.root().childElement("child").orElseThrow().textContent());
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
        String xml =
                "<?xml version=\"1.1\" encoding=\"ISO-8859-1\" standalone=\"no\"?>\n<root><child>ISO test</child></root>";
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

    @Test
    void testParseFromInputStreamWithCharsetObject() throws DomTripException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-16\"?>\n<root><child>Charset object test</child></root>";
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_16));

        // Use Charset object instead of String
        Document doc = Document.of(inputStream, StandardCharsets.UTF_16);

        assertNotNull(doc);
        assertTrue(doc.encoding().startsWith("UTF-16"), "Expected UTF-16 encoding but got: " + doc.encoding());
        assertEquals("1.0", doc.version());
        assertNotNull(doc.root());
        assertEquals("root", doc.root().name());
        assertEquals(
                "Charset object test",
                doc.root().childElement("child").orElseThrow().textContent());
    }

    @Test
    void testRoundTripWithCharsetObjects() throws DomTripException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root><child>Charset round trip</child></root>";
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        // Parse with Charset object
        Document doc = Document.of(inputStream, StandardCharsets.UTF_8);

        // Serialize with Charset object
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        doc.toXml(outputStream, StandardCharsets.UTF_8);

        // Verify round trip
        String result = outputStream.toString(StandardCharsets.UTF_8);
        assertEquals(xml, result);
    }

    @Test
    void testCharsetConsistencyInRoundTrip() throws DomTripException {
        // Test that using Charset objects maintains consistency
        Charset[] charsets = {StandardCharsets.UTF_8, StandardCharsets.UTF_16, StandardCharsets.ISO_8859_1};

        for (Charset charset : charsets) {
            String xml = "<?xml version=\"1.0\" encoding=\"" + charset.name() + "\"?>\n<root><child>Test "
                    + charset.name() + "</child></root>";
            InputStream inputStream = new ByteArrayInputStream(xml.getBytes(charset));

            // Parse with Charset object
            Document doc = Document.of(inputStream, charset);
            if (charset.name().equals("UTF-16")) {
                assertTrue(doc.encoding().startsWith("UTF-16"), "Expected UTF-16 encoding but got: " + doc.encoding());
            } else {
                assertEquals(charset.name(), doc.encoding());
            }

            // Serialize with same Charset object
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            doc.toXml(outputStream, charset);

            // Verify content is preserved
            String result = outputStream.toString(charset);
            assertTrue(result.contains("Test " + charset.name()));
        }
    }

    @Test
    void testNullCharsetHandling() throws DomTripException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root><child>Null charset test</child></root>";
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        // Null charset should default to UTF-8
        Document doc = Document.of(inputStream, (Charset) null);

        assertNotNull(doc);
        assertEquals("UTF-8", doc.encoding());
        assertEquals(
                "Null charset test",
                doc.root().childElement("child").orElseThrow().textContent());
    }
}
