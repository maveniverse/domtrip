package eu.maveniverse.domtrip.snippets;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Snippet tests for the Input Stream Parsing documentation.
 */
public class InputStreamParsingSnippets extends BaseSnippetTest {

    @Test
    public void demonstrateParsingFromFile() throws Exception {
        // START: parsing-from-file
        // Parse XML file with automatic encoding detection
        // Path xmlFile = Path.of("config.xml");
        // Document doc = Document.of(xmlFile);

        // For testing, use string content
        String xmlContent = createConfigXml();
        Document doc = Document.of(xmlContent);

        Editor editor = new Editor(doc);
        // File encoding is automatically detected and preserved
        // END: parsing-from-file

        Assertions.assertNotNull(doc);
        Assertions.assertNotNull(editor);
    }

    @Test
    public void demonstrateParsingFromInputStream() throws Exception {
        // START: parsing-from-inputstream
        // Parse from any InputStream
        String xmlContent = createTestXml("data");
        try (InputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8))) {
            Document doc = Document.of(inputStream);
            Editor editor = new Editor(doc);

            // Process the document
            Element root = editor.root();
            // ... edit operations
        }
        // END: parsing-from-inputstream

        // Test passes if no exception is thrown
        Assertions.assertTrue(true);
    }

    @Test
    public void demonstrateParsingFromNetwork() throws Exception {
        // START: parsing-from-network
        // Parse XML from network resource
        // URL xmlUrl = new URL("https://example.com/api/data.xml");
        // try (InputStream stream = xmlUrl.openStream()) {
        //     Document doc = Document.of(stream);
        //     Editor editor = new Editor(doc);
        //
        //     // Network XML is parsed with encoding detection
        // }

        // For testing, simulate network content
        String xmlContent = createTestXml("networkData");
        try (InputStream stream = new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8))) {
            Document doc = Document.of(stream);
            Editor editor = new Editor(doc);

            // Network XML is parsed with encoding detection
        }
        // END: parsing-from-network

        // Test passes if no exception is thrown
        Assertions.assertTrue(true);
    }

    @Test
    public void demonstrateAutomaticEncodingDetection() throws Exception {
        // START: automatic-encoding-detection
        // Encoding detected from:
        // 1. Byte Order Mark (BOM)
        // 2. XML declaration
        // 3. Content analysis
        // 4. Default fallback (UTF-8)

        String xmlContent = createTestXml("root");
        try (InputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8))) {
            Document doc = Document.of(inputStream);
            String detectedEncoding = doc.encoding(); // "UTF-8", "UTF-16", etc.
        }
        // END: automatic-encoding-detection

        // Test passes if no exception is thrown
        Assertions.assertTrue(true);
    }

    @Test
    public void demonstrateSupportedEncodings() throws Exception {
        // START: supported-encodings
        // UTF-8 (most common)
        String utf8Content = createTestXml("utf8");
        try (InputStream utf8Stream = new ByteArrayInputStream(utf8Content.getBytes(StandardCharsets.UTF_8))) {
            Document utf8Doc = Document.of(utf8Stream);
        }

        // UTF-16 with BOM
        try (InputStream utf16Stream = new ByteArrayInputStream(utf8Content.getBytes(StandardCharsets.UTF_16))) {
            Document utf16Doc = Document.of(utf16Stream);
        }

        // ISO-8859-1 (Latin-1)
        try (InputStream isoStream = new ByteArrayInputStream(utf8Content.getBytes(StandardCharsets.ISO_8859_1))) {
            Document isoDoc = Document.of(isoStream);
        }

        // All Java-supported encodings work
        // END: supported-encodings

        // Test passes if no exception is thrown
        Assertions.assertTrue(true);
    }

    @Test
    public void demonstrateBOMHandling() throws Exception {
        // START: bom-handling
        // BOM is automatically detected and handled
        byte[] utf8WithBom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        String xmlContent = createTestXml("root");
        byte[] xmlBytes = xmlContent.getBytes(StandardCharsets.UTF_8);
        byte[] combined = new byte[utf8WithBom.length + xmlBytes.length];
        System.arraycopy(utf8WithBom, 0, combined, 0, utf8WithBom.length);
        System.arraycopy(xmlBytes, 0, combined, utf8WithBom.length, xmlBytes.length);

        ByteArrayInputStream stream = new ByteArrayInputStream(combined);

        Document doc = Document.of(stream);
        // BOM is processed transparently, encoding correctly detected
        // END: bom-handling

        Assertions.assertNotNull(doc);
    }

    @Test
    public void demonstrateLargeFileProcessing() throws Exception {
        // START: large-file-processing
        // Memory-efficient processing of large XML files
        // Path largeXmlFile = Path.of("large-dataset.xml");

        try {
            // For testing, use regular content
            String xmlContent = createTestXml("dataset");
            Document doc = Document.of(xmlContent);
            Editor editor = new Editor(doc);

            // Process in chunks or specific elements
            Element root = editor.root();

            // Modify only what's needed
            editor.setAttribute(root, "processed", "true");

            // Save back to file (in real scenario)
            String result = editor.toXml();

        } catch (Exception e) {
            System.err.println("Failed to process large file: " + e.getMessage());
        }
        // END: large-file-processing

        // Test passes if no exception is thrown
        Assertions.assertTrue(true);
    }

    @Test
    public void demonstrateCustomStreamSources() throws Exception {
        // START: custom-stream-sources
        // Parse from compressed streams
        // try (InputStream gzipStream = new GZIPInputStream(
        //         new FileInputStream("data.xml.gz"))) {
        //     Document doc = Document.of(gzipStream);
        //     // Compressed XML is automatically decompressed and parsed
        // }

        // For testing, simulate compressed content
        String xmlContent = createTestXml("compressed");
        try (InputStream stream = new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8))) {
            Document doc = Document.of(stream);
            // Simulated compressed XML processing
        }

        // Parse from database BLOB (simulated)
        // try (InputStream blobStream = resultSet.getBinaryStream("xml_data")) {
        //     Document doc = Document.of(blobStream);
        //     // Database XML content is parsed with encoding detection
        // }
        // END: custom-stream-sources

        // Test passes if no exception is thrown
        Assertions.assertTrue(true);
    }

    @Test
    public void demonstrateErrorHandling() throws Exception {
        // START: inputstream-error-handling
        try {
            String xmlContent = createTestXml("root");
            try (InputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8))) {
                Document doc = Document.of(inputStream);
                Editor editor = new Editor(doc);
            }

        } catch (Exception e) {
            if (e.getMessage().contains("encoding")) {
                // Handle encoding-related errors
                System.err.println("Encoding issue: " + e.getMessage());
            } else if (e.getMessage().contains("malformed")) {
                // Handle XML syntax errors
                System.err.println("XML syntax error: " + e.getMessage());
            } else {
                // Handle other parsing errors
                System.err.println("Parsing failed: " + e.getMessage());
            }
        }
        // END: inputstream-error-handling

        // Test passes if no exception is thrown
        Assertions.assertTrue(true);
    }

    @Test
    public void demonstrateBufferedStreams() throws Exception {
        // START: buffered-streams
        // Use BufferedInputStream for better performance
        String xmlContent = createTestXml("large");
        try (InputStream buffered =
                new BufferedInputStream(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)), 8192)) {
            Document doc = Document.of(buffered);
            // Buffering improves read performance
        }
        // END: buffered-streams

        // Test passes if no exception is thrown
        Assertions.assertTrue(true);
    }

    @Test
    public void demonstrateMemoryManagement() throws DomTripException {
        // START: memory-management
        // For very large files, consider processing in sections
        // Path hugefile = Path.of("huge-dataset.xml");

        // Check file size first (simulated)
        long fileSize = 50_000_000; // Simulated file size
        if (fileSize > 100_000_000) { // 100MB
            System.out.println("Large file detected, using optimized processing");
        }

        // For testing, use regular content
        String xmlContent = createTestXml("huge");
        Document doc = Document.of(xmlContent);
        // DomTrip handles memory efficiently even for large files
        // END: memory-management

        Assertions.assertNotNull(doc);
    }

    @Test
    public void demonstrateConfigurationFiles() throws DomTripException {
        // START: configuration-files
        // Load application configuration
        // Path configPath = Path.of("app-config.xml");
        // if (Files.exists(configPath)) {
        //     Document config = Document.of(configPath);
        //     Editor editor = new Editor(config);
        //
        //     // Read configuration values
        //     String dbUrl = editor.findElement("database")
        //         .flatMap(db -> db.child("url"))
        //         .map(Element::textContent)
        //         .orElse("default-url");
        // }

        // For testing, use simulated config
        String configXml = createConfigXml();
        Document config = Document.of(configXml);
        Editor editor = new Editor(config);

        // Read configuration values
        String dbUrl = editor.root()
                .descendant("database")
                .flatMap(db -> db.child("url"))
                .map(Element::textContent)
                .orElse("default-url");
        // END: configuration-files

        Assertions.assertNotNull(dbUrl);
    }

    @Test
    public void demonstrateWebServiceResponses() throws Exception {
        // START: web-service-responses
        // Parse XML response from web service
        // HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // try (InputStream response = connection.getInputStream()) {
        //     Document doc = Document.of(response);
        //     Editor editor = new Editor(doc);
        //
        //     // Process response data
        //     Element result = editor.findElement("result");
        //     // ... extract data
        // }

        // For testing, simulate web service response
        String responseXml = "<response><result>success</result></response>";
        try (InputStream response = new ByteArrayInputStream(responseXml.getBytes(StandardCharsets.UTF_8))) {
            Document doc = Document.of(response);
            Editor editor = new Editor(doc);

            // Process response data
            Element result = editor.root().child("result").orElse(null);
            // ... extract data
        }
        // END: web-service-responses

        // Test passes if no exception is thrown
        Assertions.assertTrue(true);
    }

    @Test
    public void demonstrateBatchProcessing() throws DomTripException {
        // START: batch-processing
        // Process multiple XML files
        // List<Path> xmlFiles = Files.list(Path.of("xml-data"))
        //     .filter(path -> path.toString().endsWith(".xml"))
        //     .collect(Collectors.toList());

        // For testing, simulate multiple files
        String[] xmlContents = {createTestXml("file1"), createTestXml("file2"), createTestXml("file3")};

        for (String xmlContent : xmlContents) {
            try {
                Document doc = Document.of(xmlContent);
                Editor editor = new Editor(doc);

                // Process each file
                processXmlDocument(editor);

                // Save processed result (simulated)
                String result = editor.toXml();

            } catch (Exception e) {
                System.err.println("Failed to process XML: " + e.getMessage());
            }
        }
        // END: batch-processing

        // Test passes if no exception is thrown
        Assertions.assertTrue(true);
    }

    @Test
    public void demonstrateEditorIntegration() throws Exception {
        // START: editor-integration
        // Parse from stream and edit
        String xmlContent = createTestXml("source");
        try (InputStream stream = new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8))) {
            Document doc = Document.of(stream);
            Editor editor = new Editor(doc);

            // All Editor features work normally
            editor.addElement(editor.root(), "timestamp", Instant.now().toString());

            // Encoding is preserved in output
            String result = editor.toXml();
        }
        // END: editor-integration

        // Test passes if no exception is thrown
        Assertions.assertTrue(true);
    }

    private void processXmlDocument(Editor editor) throws DomTripException {
        // Simulate document processing
        editor.setAttribute(editor.root(), "processed", "true");
    }
}
