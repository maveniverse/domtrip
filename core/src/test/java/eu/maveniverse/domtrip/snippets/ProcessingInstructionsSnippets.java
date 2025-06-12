package eu.maveniverse.domtrip.snippets;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.ProcessingInstruction;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Snippet tests for the Processing Instructions documentation.
 */
public class ProcessingInstructionsSnippets extends BaseSnippetTest {

    @Test
    public void demonstrateCreatingProcessingInstructions() {
        // START: creating-processing-instructions
        // Create a new processing instruction
        ProcessingInstruction pi = ProcessingInstruction.of("xml-stylesheet", "type=\"text/xsl\" href=\"style.xsl\"");

        // Access components
        String target = pi.target(); // "xml-stylesheet"
        String data = pi.data(); // "type=\"text/xsl\" href=\"style.xsl\""
        // END: creating-processing-instructions

        Assertions.assertEquals("xml-stylesheet", target);
        Assertions.assertEquals("type=\"text/xsl\" href=\"style.xsl\"", data);
    }

    @Test
    public void demonstrateParsingDocumentsWithPIs() {
        // START: parsing-documents-with-pis
        String xml =
                """
            <?xml version="1.0" encoding="UTF-8"?>
            <?xml-stylesheet type="text/xsl" href="transform.xsl"?>
            <?sort-order alpha-ascending?>
            <root>
                <data>content</data>
            </root>
            """;

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        // Processing instructions are preserved exactly
        String result = editor.toXml();
        // Result maintains all PIs in their original positions
        // END: parsing-documents-with-pis

        Assertions.assertTrue(result.contains("xml-stylesheet"));
        Assertions.assertTrue(result.contains("sort-order"));
    }

    @Test
    public void demonstrateFindingProcessingInstructions() {
        // START: finding-processing-instructions
        String xmlWithPIs =
                """
            <?xml version="1.0"?>
            <?xml-stylesheet type="text/xsl" href="style.xsl"?>
            <root>content</root>
            """;
        Document doc = Document.of(xmlWithPIs);

        // Find all processing instructions in document
        List<ProcessingInstruction> pis = doc.nodes()
                .filter(node -> node instanceof ProcessingInstruction)
                .map(node -> (ProcessingInstruction) node)
                .collect(Collectors.toList());

        // Find specific PI by target
        Optional<ProcessingInstruction> stylesheet = doc.nodes()
                .filter(node -> node instanceof ProcessingInstruction)
                .map(node -> (ProcessingInstruction) node)
                .filter(pi -> "xml-stylesheet".equals(pi.target()))
                .findFirst();
        // END: finding-processing-instructions

        Assertions.assertTrue(pis.size() > 0);
        Assertions.assertTrue(stylesheet.isPresent());
    }

    @Test
    public void demonstrateModifyingProcessingInstructions() {
        // START: modifying-processing-instructions
        ProcessingInstruction pi = ProcessingInstruction.of("target", "old-data");

        // Modify target and data
        pi.target("new-target");
        pi.data("new-data with parameters");

        // Get updated content
        String target = pi.target(); // "new-target"
        String data = pi.data(); // "new-data with parameters"
        // END: modifying-processing-instructions

        Assertions.assertEquals("new-target", target);
        Assertions.assertEquals("new-data with parameters", data);
    }

    @Test
    public void demonstrateSpecialCharacters() {
        // START: special-characters
        String xml =
                """
            <?target data with <special> &amp; characters?>
            <root/>
            """;

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        // Special characters are preserved exactly
        String result = editor.toXml();
        // END: special-characters

        Assertions.assertTrue(result.contains("<special>"));
        Assertions.assertTrue(result.contains("&amp;"));
    }

    @Test
    public void demonstratePositionAndWhitespace() {
        // START: position-whitespace-preservation
        String xml =
                """
            <?xml version="1.0"?>

            <?xml-stylesheet href="style.css"?>

            <root/>
            """;

        Document doc = Document.of(xml);

        // Find stylesheet PI
        Optional<ProcessingInstruction> stylesheet = doc.nodes()
                .filter(node -> node instanceof ProcessingInstruction)
                .map(node -> (ProcessingInstruction) node)
                .filter(pi -> "xml-stylesheet".equals(pi.target()))
                .findFirst();

        // Whitespace around PIs is preserved in the document structure
        // END: position-whitespace-preservation

        Assertions.assertTrue(stylesheet.isPresent());
    }

    @Test
    public void demonstrateXmlStylesheetDeclaration() {
        // START: xml-stylesheet-declaration
        // Add stylesheet PI to document
        Document doc = Document.withRootElement("html");
        Editor editor = new Editor(doc);

        ProcessingInstruction stylesheet =
                ProcessingInstruction.of("xml-stylesheet", "type=\"text/xsl\" href=\"transform.xsl\"");

        // Insert PI before root element
        doc.addNode(stylesheet);
        // END: xml-stylesheet-declaration

        String result = editor.toXml();
        Assertions.assertTrue(result.contains("xml-stylesheet"));
        Assertions.assertTrue(result.contains("transform.xsl"));
    }

    @Test
    public void demonstratePhpProcessingInstructions() {
        // START: php-processing-instructions
        String phpXml =
                """
            <?xml version="1.0"?>
            <?php
                $title = "Dynamic Title";
                echo "<title>$title</title>";
            ?>
            <html>
                <head></head>
                <body>Content</body>
            </html>
            """;

        Document doc = Document.of(phpXml);
        // PHP PI content is preserved exactly, including newlines and formatting
        // END: php-processing-instructions

        String result = doc.toXml();
        Assertions.assertTrue(result.contains("<?php"));
        Assertions.assertTrue(result.contains("$title"));
    }

    @Test
    public void demonstrateApplicationSpecificInstructions() {
        // START: application-specific-instructions
        // Custom processing instructions for application logic
        ProcessingInstruction sortOrder = ProcessingInstruction.of("sort-order", "alpha-ascending");
        ProcessingInstruction cacheHint = ProcessingInstruction.of("cache-duration", "3600");

        // Add to document
        Document doc = Document.withRootElement("data");
        doc.addNode(sortOrder);
        doc.addNode(cacheHint);
        // END: application-specific-instructions

        String result = doc.toXml();
        Assertions.assertTrue(result.contains("sort-order"));
        Assertions.assertTrue(result.contains("cache-duration"));
        Assertions.assertTrue(result.contains("3600"));
    }

    @Test
    public void demonstrateEditorIntegration() {
        // START: editor-integration
        String xmlWithPIs =
                """
            <?xml version="1.0"?>
            <?xml-stylesheet type="text/css" href="style.css"?>
            <root>
                <item>content</item>
            </root>
            """;
        Editor editor = new Editor(Document.of(xmlWithPIs));

        // PIs are automatically preserved during editing
        editor.addElement(editor.root(), "newElement", "content");

        // Original PIs remain in their positions with exact formatting
        String result = editor.toXml();
        // END: editor-integration

        Assertions.assertTrue(result.contains("xml-stylesheet"));
        Assertions.assertTrue(result.contains("newElement"));
    }
}
