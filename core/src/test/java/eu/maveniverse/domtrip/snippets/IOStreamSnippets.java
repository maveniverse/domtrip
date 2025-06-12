package eu.maveniverse.domtrip.snippets;

import eu.maveniverse.domtrip.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Snippet tests for InputStream/OutputStream support and encoding documentation.
 */
public class IOStreamSnippets extends BaseSnippetTest {

    @Test
    public void demonstrateAutomaticEncodingDetection() throws Exception {
        // START: automatic-encoding-detection
        // Parse with automatic encoding detection
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root>content</root>";
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        Document doc = Document.of(inputStream);

        // Encoding is automatically detected and set
        String detectedEncoding = doc.encoding(); // e.g., "UTF-8"
        // END: automatic-encoding-detection

        Assertions.assertEquals("UTF-8", detectedEncoding);
        inputStream.close();
    }

    @Test
    public void demonstrateEncodingDetectionWithFallback() throws Exception {
        // START: encoding-detection-fallback
        // With fallback encoding (String)
        String xml = "<root>content</root>";
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.ISO_8859_1));
        Document doc = Document.of(inputStream, "ISO-8859-1");

        // With fallback encoding (Charset - preferred)
        InputStream inputStream2 = new ByteArrayInputStream(xml.getBytes(StandardCharsets.ISO_8859_1));
        Document doc2 = Document.of(inputStream2, StandardCharsets.ISO_8859_1);
        // END: encoding-detection-fallback

        Assertions.assertNotNull(doc);
        Assertions.assertNotNull(doc2);
        inputStream.close();
        inputStream2.close();
    }

    @Test
    public void demonstrateXmlDeclarationParsing() throws Exception {
        // START: xml-declaration-parsing
        String xml = "<?xml version=\"1.1\" encoding=\"UTF-8\" standalone=\"yes\"?><root/>";
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        Document doc = Document.of(inputStream);

        // All attributes are parsed and applied
        Assertions.assertEquals("1.1", doc.version());
        Assertions.assertEquals("UTF-8", doc.encoding());
        Assertions.assertTrue(doc.isStandalone());
        // END: xml-declaration-parsing

        inputStream.close();
    }

    @Test
    public void demonstrateDocumentSerialization() throws Exception {
        // START: document-serialization
        String xmlString = "<root><child>value</child></root>";
        Document doc = Document.of(xmlString);

        // Use document's encoding
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        doc.toXml(outputStream);

        // Specify encoding explicitly (String)
        ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
        doc.toXml(outputStream2, "UTF-16");

        // Specify encoding explicitly (Charset - preferred)
        ByteArrayOutputStream outputStream3 = new ByteArrayOutputStream();
        doc.toXml(outputStream3, StandardCharsets.UTF_16);
        // END: document-serialization

        Assertions.assertTrue(outputStream.size() > 0);
        Assertions.assertTrue(outputStream2.size() > 0);
        Assertions.assertTrue(outputStream3.size() > 0);

        outputStream.close();
        outputStream2.close();
        outputStream3.close();
    }

    @Test
    public void demonstrateSerializerWithEncoding() throws Exception {
        // START: serializer-with-encoding
        String xmlString = "<root><child>value</child></root>";
        Document doc = Document.of(xmlString);
        Serializer serializer = new Serializer();

        // Use document's encoding
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        serializer.serialize(doc, outputStream);

        // Specify encoding (String)
        ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
        serializer.serialize(doc, outputStream2, "ISO-8859-1");

        // Specify encoding (Charset - preferred)
        ByteArrayOutputStream outputStream3 = new ByteArrayOutputStream();
        serializer.serialize(doc, outputStream3, StandardCharsets.ISO_8859_1);
        // END: serializer-with-encoding

        Assertions.assertTrue(outputStream.size() > 0);
        Assertions.assertTrue(outputStream2.size() > 0);
        Assertions.assertTrue(outputStream3.size() > 0);

        outputStream.close();
        outputStream2.close();
        outputStream3.close();
    }

    @Test
    public void demonstrateNodeSerialization() throws Exception {
        // START: node-serialization
        String xmlString = "<root><child>value</child></root>";
        Document doc = Document.of(xmlString);
        Element element = doc.root();
        Serializer serializer = new Serializer();

        // Serialize node with UTF-8
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        serializer.serialize(element, outputStream);

        // Serialize node with specific encoding (String)
        ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
        serializer.serialize(element, outputStream2, "UTF-16");

        // Serialize node with specific encoding (Charset - preferred)
        ByteArrayOutputStream outputStream3 = new ByteArrayOutputStream();
        serializer.serialize(element, outputStream3, StandardCharsets.UTF_16);
        // END: node-serialization

        Assertions.assertTrue(outputStream.size() > 0);
        Assertions.assertTrue(outputStream2.size() > 0);
        Assertions.assertTrue(outputStream3.size() > 0);

        outputStream.close();
        outputStream2.close();
        outputStream3.close();
    }

    @Test
    public void demonstrateRoundTripProcessing() throws Exception {
        // START: round-trip-processing
        // Parse from InputStream
        String xml = "<root><child>original</child></root>";
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        Document doc = Document.of(inputStream);

        // Make modifications
        Editor editor = new Editor(doc);
        editor.addElement(doc.root(), "newElement", "content");

        // Serialize to OutputStream with same encoding
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        doc.toXml(outputStream); // Uses document's detected encoding
        // END: round-trip-processing

        String result = outputStream.toString(StandardCharsets.UTF_8);
        Assertions.assertTrue(result.contains("newElement"));
        Assertions.assertTrue(result.contains("content"));

        inputStream.close();
        outputStream.close();
    }

    @Test
    public void demonstrateEncodingConsistency() throws Exception {
        // START: encoding-consistency
        // Document with UTF-16 encoding
        String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-16\"?><root>content</root>";
        InputStream utf16Stream = new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_16));

        Document doc = Document.of(utf16Stream);
        Assertions.assertEquals("UTF-16", doc.encoding());

        // Serialization uses the same encoding
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        doc.toXml(outputStream); // Automatically uses UTF-16
        // END: encoding-consistency

        Assertions.assertTrue(outputStream.size() > 0);

        utf16Stream.close();
        outputStream.close();
    }

    @Test
    public void demonstrateEncodingOverride() throws Exception {
        // START: encoding-override
        // Parse with one encoding
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root>content</root>";
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        Document doc = Document.of(inputStream); // UTF-8 detected

        // Serialize with different encoding (String)
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        doc.toXml(outputStream, "UTF-16");

        // Serialize with different encoding (Charset - preferred)
        ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
        doc.toXml(outputStream2, StandardCharsets.UTF_16);
        // END: encoding-override

        Assertions.assertTrue(outputStream.size() > 0);
        Assertions.assertTrue(outputStream2.size() > 0);

        inputStream.close();
        outputStream.close();
        outputStream2.close();
    }

    @Test
    public void demonstrateBomHandling() throws Exception {
        // START: bom-handling
        // UTF-8 with BOM
        String xmlString = "<root>content</root>";
        byte[] bomBytes = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] xmlBytes = xmlString.getBytes(StandardCharsets.UTF_8);
        byte[] xmlWithBom = new byte[bomBytes.length + xmlBytes.length];
        System.arraycopy(bomBytes, 0, xmlWithBom, 0, bomBytes.length);
        System.arraycopy(xmlBytes, 0, xmlWithBom, bomBytes.length, xmlBytes.length);

        InputStream inputStream = new ByteArrayInputStream(xmlWithBom);
        Document doc = Document.of(inputStream);
        // BOM is detected and UTF-8 encoding is used
        // END: bom-handling

        Assertions.assertNotNull(doc);
        Assertions.assertEquals("root", doc.root().name());

        inputStream.close();
    }

    @Test
    public void demonstrateSpecialCharacters() throws Exception {
        // START: special-characters
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<root><text>Special: àáâãäå èéêë</text></root>";

        // Round-trip preserves special characters
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        Document doc = Document.of(inputStream);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        doc.toXml(outputStream);
        // Special characters are preserved
        // END: special-characters

        String result = outputStream.toString(StandardCharsets.UTF_8);
        Assertions.assertTrue(result.contains("àáâãäå"));
        Assertions.assertTrue(result.contains("èéêë"));

        inputStream.close();
        outputStream.close();
    }

    @Test
    public void demonstrateErrorHandling() throws Exception {
        // START: error-handling
        try {
            String xml = "<root><child>value</child></root>";
            InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
            Document doc = Document.of(inputStream);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            doc.toXml(outputStream);

            inputStream.close();
            outputStream.close();
        } catch (DomTripException e) {
            if (e.getCause() instanceof IOException) {
                // Handle I/O errors
                System.err.println("I/O error: " + e.getMessage());
            } else {
                // Handle parsing/encoding errors
                System.err.println("XML error: " + e.getMessage());
            }
        }
        // END: error-handling

        // Test passes if no exception is thrown
        Assertions.assertTrue(true);
    }

    @Test
    public void demonstrateCharsetVsString() throws Exception {
        // START: charset-vs-string
        String xml = "<root>content</root>";

        // ✅ Preferred - Type-safe, no invalid encoding names
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        Document doc = Document.of(inputStream, StandardCharsets.UTF_8);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        doc.toXml(outputStream, StandardCharsets.UTF_16);

        // ❌ Acceptable but less safe - String can be invalid
        InputStream inputStream2 = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        Document doc2 = Document.of(inputStream2, "UTF-8");
        ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
        doc2.toXml(outputStream2, "UTF-16");
        // END: charset-vs-string

        Assertions.assertTrue(outputStream.size() > 0);
        Assertions.assertTrue(outputStream2.size() > 0);

        inputStream.close();
        inputStream2.close();
        outputStream.close();
        outputStream2.close();
    }

    @Test
    public void demonstrateBestPractices() throws Exception {
        // START: best-practices
        // ✅ Proper resource management
        String xml = "<root><child>value</child></root>";
        byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_8);

        try (InputStream inputStream = new ByteArrayInputStream(xmlBytes);
                OutputStream outputStream = new ByteArrayOutputStream()) {

            Document doc = Document.of(inputStream);
            doc.toXml(outputStream);
        }

        // ✅ Type-safe Charset objects
        try (InputStream inputStream = new ByteArrayInputStream(xmlBytes);
                OutputStream outputStream = new ByteArrayOutputStream()) {

            Document doc = Document.of(inputStream, StandardCharsets.UTF_8);
            doc.toXml(outputStream, StandardCharsets.UTF_16);
        }

        // ✅ Automatic detection
        try (InputStream inputStream = new ByteArrayInputStream(xmlBytes)) {
            Document doc = Document.of(inputStream);
            Assertions.assertNotNull(doc);
        }
        // END: best-practices

        // Test passes if no exception is thrown
        Assertions.assertTrue(true);
    }
}
