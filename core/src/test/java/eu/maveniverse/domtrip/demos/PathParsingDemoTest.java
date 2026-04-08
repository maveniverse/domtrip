package eu.maveniverse.domtrip.demos;

import static org.junit.jupiter.api.Assertions.*;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Editor;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Demonstration of parsing XML from Path using Document.of(Path).
 */
class PathParsingDemoTest {

    @Test
    void demonstratePathParsing() throws IOException {
        verifyBasicPathParsing();
        verifyEncodingHandling();
        verifyComplexDocument();
        verifyErrorHandling();
    }

    private static void verifyBasicPathParsing() throws IOException {
        // Create a temporary XML file
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root><child>Hello from file!</child></root>";
        Path tempFile = Files.createTempFile("demo", ".xml");
        try {
            Files.writeString(tempFile, xml);

            // Parse using Document.of(Path)
            Document doc = Document.of(tempFile);
            assertEquals("UTF-8", doc.encoding());
            assertEquals("1.0", doc.version());
            assertEquals("root", doc.root().name());
            assertEquals(
                    "Hello from file!",
                    doc.root().childElement("child").orElseThrow().textContent());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private static void verifyEncodingHandling() throws IOException {
        String[] encodings = {"UTF-8", "ISO-8859-1"};
        for (String encoding : encodings) {
            String xml = "<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>\n<root><child>Content in " + encoding
                    + "</child></root>";
            Path tempFile = Files.createTempFile("demo-" + encoding.toLowerCase(), ".xml");
            try {
                Files.write(tempFile, xml.getBytes(Charset.forName(encoding)));

                Document doc = Document.of(tempFile);
                assertNotNull(doc.encoding());
                assertEquals(
                        "Content in " + encoding,
                        doc.root().childElement("child").orElseThrow().textContent());
            } finally {
                Files.deleteIfExists(tempFile);
            }
        }
    }

    private static void verifyComplexDocument() throws IOException {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <!-- Configuration file -->
                <?xml-stylesheet type="text/xsl" href="style.xsl"?>
                <config>
                    <database>
                        <host>localhost</host>
                        <port>5432</port>
                    </database>
                    <!-- End of database config -->
                </config>""";

        Path tempFile = Files.createTempFile("complex-demo", ".xml");
        try {
            Files.writeString(tempFile, xml);

            Document doc = Document.of(tempFile);
            Editor editor = new Editor(doc);

            assertEquals("UTF-8", doc.encoding());
            assertEquals("config", doc.root().name());

            // Verify structure
            String host = doc.root()
                    .childElement("database")
                    .orElseThrow()
                    .childElement("host")
                    .orElseThrow()
                    .textContent();
            assertEquals("localhost", host);

            // Show that formatting is preserved
            String result = editor.toXml();
            assertTrue(result.contains("<!-- Configuration file -->"));
            assertTrue(result.contains("<?xml-stylesheet"));
            assertTrue(result.contains("    <database>"));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private static void verifyErrorHandling() {
        // Test with non-existent file
        Path nonExistent = Path.of("non-existent-file.xml");
        assertThrows(DomTripException.class, () -> Document.of(nonExistent));

        // Test with null path
        assertThrows(DomTripException.class, () -> Document.of((Path) null));

        // Test with empty file
        try {
            Path emptyFile = Files.createTempFile("empty-demo", ".xml");
            try {
                assertThrows(DomTripException.class, () -> Document.of(emptyFile));
            } finally {
                Files.deleteIfExists(emptyFile);
            }
        } catch (IOException e) {
            fail("Failed to create temp file: " + e.getMessage());
        }
    }
}
