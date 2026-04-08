package eu.maveniverse.domtrip.demos;

import static org.junit.jupiter.api.Assertions.*;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.Editor;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Demonstration of parsing XML from InputStream with encoding detection.
 */
class InputStreamParsingDemoTest {

    @Test
    void demonstrateInputStreamParsing() {
        verifyBasicInputStreamParsing();
        verifyEncodingDetection();
        verifyBOMHandling();
        verifyXmlDeclarationParsing();
        verifyComplexDocument();
    }

    private static void verifyBasicInputStreamParsing() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root><child>Hello World</child></root>";
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        Document doc = Document.of(inputStream);
        assertNotNull(doc);
        assertEquals("UTF-8", doc.encoding());
        assertEquals("1.0", doc.version());
        assertEquals("root", doc.root().name());
        assertEquals(
                "Hello World", doc.root().childElement("child").orElseThrow().textContent());
    }

    private static void verifyEncodingDetection() {
        // Test UTF-8 encoding
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root><child>Content in UTF-8</child></root>";
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        Document doc = Document.of(inputStream);
        assertEquals("UTF-8", doc.encoding());
        assertEquals(
                "Content in UTF-8",
                doc.root().childElement("child").orElseThrow().textContent());
    }

    private static void verifyBOMHandling() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root><child>BOM Test</child></root>";
        byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_8);

        // Add UTF-8 BOM
        byte[] bomBytes = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] xmlWithBom = new byte[bomBytes.length + xmlBytes.length];
        System.arraycopy(bomBytes, 0, xmlWithBom, 0, bomBytes.length);
        System.arraycopy(xmlBytes, 0, xmlWithBom, bomBytes.length, xmlBytes.length);

        InputStream inputStream = new ByteArrayInputStream(xmlWithBom);
        Document doc = Document.of(inputStream);
        assertNotNull(doc);
        assertEquals("BOM Test", doc.root().childElement("child").orElseThrow().textContent());
    }

    private static void verifyXmlDeclarationParsing() {
        String xml =
                "<?xml version=\"1.1\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<root><child>Declaration Test</child></root>";
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        Document doc = Document.of(inputStream);
        assertEquals("1.1", doc.version());
        assertEquals("UTF-8", doc.encoding());
        assertTrue(doc.isStandalone());
        assertNotNull(doc.xmlDeclaration());
    }

    private static void verifyComplexDocument() {
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

        Document doc = Document.of(inputStream);
        Editor editor = new Editor(doc);

        assertEquals("UTF-8", doc.encoding());
        assertEquals("project", doc.root().name());
        assertEquals(
                "http://maven.apache.org/POM/4.0.0",
                Optional.ofNullable(doc.root().attribute("xmlns")).orElse("none"));

        // Verify structure
        String groupId = doc.root().childElement("groupId").orElseThrow().textContent();
        assertEquals("com.example", groupId);

        // Show that formatting is preserved
        String result = editor.toXml();
        assertTrue(result.contains("<!-- Dependencies section -->"));
        assertTrue(result.contains("    <dependency>"));
    }
}
