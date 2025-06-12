package eu.maveniverse.domtrip.snippets;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Snippet tests for lossless parsing features documentation.
 */
public class LosslessParsingSnippets extends BaseSnippetTest {

    @Test
    public void demonstrateLosslessRoundTrip() {
        // START: lossless-round-trip
        String originalXml =
                """
            <?xml version="1.0" encoding="UTF-8"?>
            <!-- Project configuration -->
            <project xmlns="http://maven.apache.org/POM/4.0.0">
                <groupId>com.example</groupId>
                <artifactId   >my-app</artifactId>
                <version>1.0.0</version>
            </project>
            """;

        Document doc = Document.of(originalXml);
        Editor editor = new Editor(doc);

        // Make NO changes
        String result = editor.toXml();

        // Result is byte-for-byte identical to original
        Assertions.assertEquals(originalXml, result);
        // END: lossless-round-trip
    }

    @Test
    public void demonstrateEntityPreservation() {
        // START: entity-preservation
        String xmlWithEntities = """
            <message>Hello &amp; goodbye &lt;world&gt;</message>
            """;

        Document doc = Document.of(xmlWithEntities);
        Editor editor = new Editor(doc);

        Element message = doc.root();

        // For your code - entities are decoded
        String decoded = message.textContent(); // "Hello & goodbye <world>"

        // For serialization - entities are preserved in the XML output
        String raw = message.textContent(); // The API handles entity encoding automatically

        String result = editor.toXml();
        // END: entity-preservation

        Assertions.assertEquals("Hello & goodbye <world>", decoded);
        Assertions.assertEquals("Hello & goodbye <world>", raw);
        Assertions.assertTrue(result.contains("&amp;"));
        Assertions.assertTrue(result.contains("&lt;"));
        Assertions.assertTrue(result.contains("&gt;"));
    }

    @Test
    public void demonstrateWhitespacePreservation() {
        // START: whitespace-preservation
        String xmlWithWhitespace =
                """
            <project>

                <groupId>com.example</groupId>

                <artifactId>my-app</artifactId>

            </project>
            """;

        Document doc = Document.of(xmlWithWhitespace);
        Editor editor = new Editor(doc);

        // Whitespace between elements is preserved exactly
        String result = editor.toXml();

        // All blank lines and spacing are maintained
        Assertions.assertEquals(xmlWithWhitespace, result);
        // END: whitespace-preservation
    }

    @Test
    public void demonstrateCommentPreservation() {
        // START: comment-preservation
        String xmlWithComments =
                """
            <project>
                <!-- Main project coordinates -->
                <groupId>com.example</groupId>
                <artifactId>my-app</artifactId> <!-- Application name -->
                <!-- Version information -->
                <version>1.0.0</version>
            </project>
            """;

        Document doc = Document.of(xmlWithComments);
        Editor editor = new Editor(doc);

        // Comments are preserved in their exact positions
        String result = editor.toXml();

        Assertions.assertEquals(xmlWithComments, result);
        // END: comment-preservation
    }

    @Test
    public void demonstrateAttributeQuotePreservation() {
        // START: attribute-quote-preservation
        String xmlWithMixedQuotes =
                """
            <dependency scope='test' optional="true" classifier='sources'/>
            """;

        Document doc = Document.of(xmlWithMixedQuotes);
        Editor editor = new Editor(doc);

        // Quote styles are preserved exactly
        String result = editor.toXml();

        Assertions.assertEquals(xmlWithMixedQuotes, result);
        // END: attribute-quote-preservation
    }

    @Test
    public void demonstrateNamespacePreservation() {
        // START: namespace-preservation
        String xmlWithNamespaces =
                """
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                                         http://maven.apache.org/xsd/maven-4.0.0.xsd">
                <groupId>com.example</groupId>
            </project>
            """;

        Document doc = Document.of(xmlWithNamespaces);
        Editor editor = new Editor(doc);

        // Namespace declarations and formatting are preserved
        String result = editor.toXml();

        Assertions.assertTrue(result.contains("xmlns=\"http://maven.apache.org/POM/4.0.0\""));
        Assertions.assertTrue(result.contains("xmlns:xsi="));
        Assertions.assertTrue(result.contains("xsi:schemaLocation="));
        // END: namespace-preservation
    }

    @Test
    public void demonstrateProcessingInstructionPreservation() {
        // START: processing-instruction-preservation
        String xmlWithPI =
                """
            <?xml version="1.0" encoding="UTF-8"?>
            <?xml-stylesheet type="text/xsl" href="style.xsl"?>
            <project>
                <groupId>com.example</groupId>
            </project>
            """;

        Document doc = Document.of(xmlWithPI);
        Editor editor = new Editor(doc);

        // Processing instructions are preserved
        String result = editor.toXml();

        Assertions.assertTrue(result.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        Assertions.assertTrue(result.contains("<?xml-stylesheet"));
        // END: processing-instruction-preservation
    }

    @Test
    public void demonstrateCDataPreservation() {
        // START: cdata-preservation
        String xmlWithCData =
                """
            <script>
                <![CDATA[
                function example() {
                    if (x < y && y > z) {
                        return "complex & special chars";
                    }
                }
                ]]>
            </script>
            """;

        Document doc = Document.of(xmlWithCData);
        Editor editor = new Editor(doc);

        // CDATA sections are preserved exactly
        String result = editor.toXml();

        Assertions.assertTrue(result.contains("<![CDATA["));
        Assertions.assertTrue(result.contains("]]>"));
        Assertions.assertTrue(result.contains("x < y && y > z"));
        // END: cdata-preservation
    }

    @Test
    public void demonstrateDocumentTypePreservation() {
        // START: document-type-preservation
        String xmlWithDoctype =
                """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE project SYSTEM "project.dtd">
            <project>
                <groupId>com.example</groupId>
            </project>
            """;

        Document doc = Document.of(xmlWithDoctype);
        Editor editor = new Editor(doc);

        // Document type declarations are preserved
        String result = editor.toXml();

        Assertions.assertTrue(result.contains("<!DOCTYPE project SYSTEM \"project.dtd\">"));
        // END: document-type-preservation
    }

    @Test
    public void demonstrateMinimalModification() {
        // START: minimal-modification
        String originalXml =
                """
            <project>
                <!-- Keep this comment -->
                <groupId>com.example</groupId>
                <artifactId>my-app</artifactId>
                <version>1.0.0</version>
                <!-- Keep this too -->
            </project>
            """;

        Document doc = Document.of(originalXml);
        Editor editor = new Editor(doc);

        // Only modify the version
        Element version = doc.root().child("version").orElseThrow();
        editor.setTextContent(version, "2.0.0");

        String result = editor.toXml();

        // Only the version element is rebuilt
        // Everything else remains exactly as it was
        Assertions.assertTrue(result.contains("2.0.0"));
        Assertions.assertFalse(result.contains("1.0.0"));
        Assertions.assertTrue(result.contains("<!-- Keep this comment -->"));
        Assertions.assertTrue(result.contains("<!-- Keep this too -->"));
        // END: minimal-modification
    }

    @Test
    public void demonstrateEncodingPreservation() {
        // START: encoding-preservation
        String xmlWithEncoding =
                """
            <?xml version="1.0" encoding="ISO-8859-1"?>
            <project>
                <name>Café Application</name>
            </project>
            """;

        Document doc = Document.of(xmlWithEncoding);
        Editor editor = new Editor(doc);

        // Encoding declaration is preserved
        String result = editor.toXml();

        Assertions.assertTrue(result.contains("encoding=\"ISO-8859-1\""));
        Assertions.assertTrue(result.contains("Café Application"));
        // END: encoding-preservation
    }

    @Test
    public void demonstrateComplexStructurePreservation() {
        // START: complex-structure-preservation
        String complexXml =
                """
            <?xml version="1.0" encoding="UTF-8"?>
            <!-- Multi-line comment
                 with specific formatting -->
            <project xmlns="http://maven.apache.org/POM/4.0.0">

                <groupId>com.example</groupId>
                <artifactId   >my-app</artifactId>

                <dependencies>
                    <dependency scope='test'>
                        <groupId>junit</groupId>
                        <artifactId>junit</artifactId>
                    </dependency>
                </dependencies>

            </project>
            """;

        Document doc = Document.of(complexXml);
        Editor editor = new Editor(doc);

        // Add one element without changing anything else
        Element dependencies = doc.root().child("dependencies").orElseThrow();
        Element newDep = editor.addElement(dependencies, "dependency");
        editor.addElement(newDep, "groupId", "mockito");

        String result = editor.toXml();

        // All original formatting, comments, whitespace preserved
        // Only the new dependency is added with inferred formatting
        Assertions.assertTrue(result.contains("mockito"));
        Assertions.assertTrue(result.contains("<!-- Multi-line comment"));
        Assertions.assertTrue(result.contains("scope='test'"));
        Assertions.assertTrue(result.contains("<artifactId   >my-app</artifactId>"));
        // END: complex-structure-preservation
    }

    @Test
    public void demonstrateProcessingInstructionsWithData() {
        // START: processing-instructions-with-data
        String xml =
                """
            <?xml version="1.0" encoding="UTF-8"?>
            <?xml-stylesheet type="text/xsl" href="style.xsl"?>
            <document>
                <?custom-instruction data="value"?>
                <content>text</content>
            </document>
            """;

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        // Processing instructions with data are preserved exactly
        String result = editor.toXml();

        Assertions.assertTrue(result.contains("<?xml-stylesheet type=\"text/xsl\" href=\"style.xsl\"?>"));
        Assertions.assertTrue(result.contains("<?custom-instruction data=\"value\"?>"));
        // END: processing-instructions-with-data
    }

    @Test
    public void demonstrateRoundTripVerification() throws Exception {
        // START: round-trip-verification
        // Create a temporary file for testing
        String complexXml =
                """
            <?xml version="1.0" encoding="UTF-8"?>
            <!-- Configuration file -->
            <config>
                <database>
                    <host>localhost</host>
                    <port>5432</port>
                </database>
            </config>
            """;

        // Load with automatic encoding detection
        Document doc = Document.of(complexXml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        // Load again to verify round-trip preservation
        Document doc2 = Document.of(result);
        Editor editor2 = new Editor(doc2);
        String result2 = editor2.toXml();

        // Should be identical
        Assertions.assertEquals(result, result2);
        // END: round-trip-verification
    }

    @Test
    public void demonstrateBestPracticesForEditing() {
        // START: best-practices-editing
        // ✅ Perfect for editing existing files
        String existingConfigXml = createConfigXml();
        Document doc = Document.of(existingConfigXml);
        Editor editor = new Editor(doc);

        Element root = editor.root();
        editor.addElement(root, "newSetting", "value");

        String result = editor.toXml();
        // Result preserves all original formatting
        // END: best-practices-editing

        Assertions.assertTrue(result.contains("<newSetting>value</newSetting>"));
        Assertions.assertTrue(result.contains("<!-- Configuration file -->") || result.contains("<config>"));
    }

    @Test
    public void demonstrateLargeFileHandling() {
        // START: large-file-handling
        // ✅ For large files, consider streaming or chunking
        String xmlContent = createConfigXml();
        long fileSize = xmlContent.length();

        if (fileSize > 10_000_000) { // 10MB
            // Consider alternative approaches for very large files
            System.out.println("Large file detected, consider streaming approach");
        }

        // For normal-sized files, DomTrip works efficiently
        Document doc = Document.of(xmlContent);
        Editor editor = new Editor(doc);
        String result = editor.toXml();
        // END: large-file-handling

        Assertions.assertNotNull(result);
    }
}
