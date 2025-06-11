package eu.maveniverse.domtrip.demos;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Editor;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Demonstration of parsing XML from Path using Document.of(Path).
 */
public class PathParsingDemo {

    public static void main(String[] args) {
        System.out.println("=== DomTrip Path Parsing Demo ===\n");

        demonstrateBasicPathParsing();
        demonstrateEncodingHandling();
        demonstrateComplexDocument();
        demonstrateErrorHandling();
    }

    private static void demonstrateBasicPathParsing() {
        System.out.println("1. Basic Path Parsing:");

        try {
            // Create a temporary XML file
            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root><child>Hello from file!</child></root>";
            Path tempFile = Files.createTempFile("demo", ".xml");
            Files.writeString(tempFile, xml);

            // Parse using Document.of(Path)
            Document doc = Document.of(tempFile);
            System.out.println("   Parsed successfully!");
            System.out.println("   Encoding: " + doc.encoding());
            System.out.println("   Version: " + doc.version());
            System.out.println("   Root element: " + doc.root().name());
            System.out.println("   Child content: "
                    + doc.root().child("child").orElseThrow().textContent());

            // Clean up
            Files.deleteIfExists(tempFile);
        } catch (IOException | DomTripException e) {
            System.err.println("   Error: " + e.getMessage());
        }
        System.out.println();
    }

    private static void demonstrateEncodingHandling() {
        System.out.println("2. Encoding Detection:");

        String[] encodings = {"UTF-8", "UTF-16", "ISO-8859-1"};
        for (String encoding : encodings) {
            try {
                String xml = "<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>\n<root><child>Content in "
                        + encoding + "</child></root>";
                Path tempFile = Files.createTempFile("demo-" + encoding.toLowerCase(), ".xml");

                // Write with specific encoding
                Files.write(tempFile, xml.getBytes(Charset.forName(encoding)));

                // Parse using Document.of(Path) - should auto-detect encoding
                Document doc = Document.of(tempFile);
                System.out.println("   " + encoding + " - Detected: " + doc.encoding());
                System.out.println(
                        "   Content: " + doc.root().child("child").orElseThrow().textContent());

                // Clean up
                Files.deleteIfExists(tempFile);
            } catch (IOException | DomTripException e) {
                System.err.println("   " + encoding + " - Error: " + e.getMessage());
            }
        }
        System.out.println();
    }

    private static void demonstrateComplexDocument() {
        System.out.println("3. Complex Document with Formatting Preservation:");

        try {
            String xml =
                    """
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
            Files.writeString(tempFile, xml);

            // Parse using Document.of(Path)
            Document doc = Document.of(tempFile);
            Editor editor = new Editor(doc);

            System.out.println("   Complex document parsed successfully!");
            System.out.println("   Encoding: " + doc.encoding());
            System.out.println("   Root element: " + doc.root().name());

            // Verify structure
            String host = doc.root()
                    .child("database")
                    .orElseThrow()
                    .child("host")
                    .orElseThrow()
                    .textContent();
            System.out.println("   Database host: " + host);

            // Show that formatting is preserved
            String result = editor.toXml();
            System.out.println("   Comments preserved: " + result.contains("<!-- Configuration file -->"));
            System.out.println("   Processing instructions preserved: " + result.contains("<?xml-stylesheet"));
            System.out.println("   Indentation preserved: " + result.contains("    <database>"));

            // Clean up
            Files.deleteIfExists(tempFile);
        } catch (IOException | DomTripException e) {
            System.err.println("   Error: " + e.getMessage());
        }
        System.out.println();
    }

    private static void demonstrateErrorHandling() {
        System.out.println("4. Error Handling:");

        // Test with non-existent file
        try {
            Document.of(Path.of("non-existent-file.xml"));
            System.out.println("   Non-existent file: Should have thrown exception!");
        } catch (DomTripException e) {
            System.out.println("   Non-existent file: Correctly threw exception - " + e.getMessage());
        }

        // Test with null path
        try {
            Document.of((Path) null);
            System.out.println("   Null path: Should have thrown exception!");
        } catch (DomTripException e) {
            System.out.println("   Null path: Correctly threw exception - " + e.getMessage());
        }

        // Test with empty file
        try {
            Path emptyFile = Files.createTempFile("empty-demo", ".xml");
            Document.of(emptyFile);
            System.out.println("   Empty file: Should have thrown exception!");
            Files.deleteIfExists(emptyFile);
        } catch (IOException | DomTripException e) {
            System.out.println("   Empty file: Correctly threw exception - " + e.getMessage());
        }

        System.out.println();
    }
}
