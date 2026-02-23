package eu.maveniverse.domtrip.demos;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Editor;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Demonstration of parsing XML from InputStream with encoding detection.
 */
public class InputStreamParsingDemo {

    public static void main(String[] args) {
        System.out.println("=== DomTrip InputStream Parsing Demo ===\n");

        demonstrateBasicInputStreamParsing();
        demonstrateEncodingDetection();
        demonstrateBOMHandling();
        demonstrateXmlDeclarationParsing();
        demonstrateComplexDocument();
    }

    private static void demonstrateBasicInputStreamParsing() {
        System.out.println("1. Basic InputStream Parsing:");

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root><child>Hello World</child></root>";
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        try {
            Document doc = Document.of(inputStream);
            System.out.println("   Parsed successfully!");
            System.out.println("   Encoding: " + doc.encoding());
            System.out.println("   Version: " + doc.version());
            System.out.println("   Root element: " + doc.root().name());
            System.out.println("   Child content: "
                    + doc.root().childElement("child").orElseThrow().textContent());
        } catch (DomTripException e) {
            System.err.println("   Error: " + e.getMessage());
        }
        System.out.println();
    }

    private static void demonstrateEncodingDetection() {
        System.out.println("2. Encoding Detection:");

        // Test different encodings
        String[] encodings = {"UTF-8", "UTF-16", "ISO-8859-1"};

        for (String encoding : encodings) {
            String xml = "<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>\n<root><child>Content in " + encoding
                    + "</child></root>";

            try {
                byte[] xmlBytes;
                if ("UTF-16".equals(encoding)) {
                    xmlBytes = xml.getBytes(StandardCharsets.UTF_16);
                } else if ("ISO-8859-1".equals(encoding)) {
                    xmlBytes = xml.getBytes(StandardCharsets.ISO_8859_1);
                } else {
                    xmlBytes = xml.getBytes(StandardCharsets.UTF_8);
                }

                InputStream inputStream = new ByteArrayInputStream(xmlBytes);
                Document doc = Document.of(inputStream);

                System.out.println("   " + encoding + " - Detected: " + doc.encoding());
                System.out.println("   Content: "
                        + doc.root().childElement("child").orElseThrow().textContent());
            } catch (DomTripException e) {
                System.err.println("   " + encoding + " - Error: " + e.getMessage());
            }
        }
        System.out.println();
    }

    private static void demonstrateBOMHandling() {
        System.out.println("3. BOM (Byte Order Mark) Handling:");

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root><child>BOM Test</child></root>";
        byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_8);

        // Add UTF-8 BOM
        byte[] bomBytes = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] xmlWithBom = new byte[bomBytes.length + xmlBytes.length];
        System.arraycopy(bomBytes, 0, xmlWithBom, 0, bomBytes.length);
        System.arraycopy(xmlBytes, 0, xmlWithBom, bomBytes.length, xmlBytes.length);

        try {
            InputStream inputStream = new ByteArrayInputStream(xmlWithBom);
            Document doc = Document.of(inputStream);

            System.out.println("   BOM detected and handled successfully!");
            System.out.println("   Encoding: " + doc.encoding());
            System.out.println("   Content: "
                    + doc.root().childElement("child").orElseThrow().textContent());
        } catch (DomTripException e) {
            System.err.println("   Error: " + e.getMessage());
        }
        System.out.println();
    }

    private static void demonstrateXmlDeclarationParsing() {
        System.out.println("4. XML Declaration Attribute Parsing:");

        String xml =
                "<?xml version=\"1.1\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<root><child>Declaration Test</child></root>";
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        try {
            Document doc = Document.of(inputStream);

            System.out.println("   XML Declaration parsed successfully!");
            System.out.println("   Version: " + doc.version());
            System.out.println("   Encoding: " + doc.encoding());
            System.out.println("   Standalone: " + doc.isStandalone());
            System.out.println("   Original declaration: " + doc.xmlDeclaration());
        } catch (DomTripException e) {
            System.err.println("   Error: " + e.getMessage());
        }
        System.out.println();
    }

    private static void demonstrateComplexDocument() {
        System.out.println("5. Complex Document with Formatting Preservation:");

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n"
                + "  <groupId>com.example</groupId>\n"
                + "  <artifactId>test-project</artifactId>\n"
                + "  <version>1.0.0</version>\n"
                + "  <!-- Dependencies section -->\n"
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

        try {
            Document doc = Document.of(inputStream);
            Editor editor = new Editor(doc);

            System.out.println("   Complex document parsed successfully!");
            System.out.println("   Encoding: " + doc.encoding());
            System.out.println("   Root element: " + doc.root().name());
            System.out.println("   Namespace: "
                    + Optional.ofNullable(doc.root().attribute("xmlns")).orElse("none"));

            // Verify structure
            String groupId = doc.root().childElement("groupId").orElseThrow().textContent();
            System.out.println("   GroupId: " + groupId);

            // Show that formatting is preserved
            String result = editor.toXml();
            System.out.println("   Formatting preserved: " + result.contains("<!-- Dependencies section -->"));
            System.out.println("   Indentation preserved: " + result.contains("    <dependency>"));

        } catch (DomTripException e) {
            System.err.println("   Error: " + e.getMessage());
        }
        System.out.println();
    }
}
