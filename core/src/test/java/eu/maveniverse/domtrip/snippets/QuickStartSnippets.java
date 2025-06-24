package eu.maveniverse.domtrip.snippets;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripConfig;
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Snippet tests for the Quick Start documentation.
 */
public class QuickStartSnippets extends BaseSnippetTest {

    @Test
    public void demonstrateBasicUsage() throws Exception {
        // START: quick-start-basic
        // Parse XML while preserving all formatting
        String originalXml =
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

        Document doc = Document.of(originalXml);
        Editor editor = new Editor(doc);

        // Make some changes
        Element database = editor.root().descendant("database").orElseThrow();
        editor.addElement(database, "username", "admin");
        editor.addElement(database, "password", "secret");

        // Serialize back to XML
        String result = editor.toXml();
        System.out.println(result);
        // END: quick-start-basic

        // Verify the snippet works correctly
        Assertions.assertTrue(result.contains("<username>admin</username>"));
        Assertions.assertTrue(result.contains("<password>secret</password>"));
        Assertions.assertTrue(result.contains("<!-- Configuration file -->"));
    }

    @Test
    public void demonstrateLoadingXml() throws Exception {
        // START: loading-xml-string
        // From string
        String xmlString = createConfigXml();
        Document doc = Document.of(xmlString);
        Editor editor = new Editor(doc);
        // END: loading-xml-string

        Assertions.assertEquals("config", editor.root().name());

        // Create a temporary file for testing file-based loading
        Path tempFile = Files.createTempFile("config", ".xml");
        Files.writeString(tempFile, xmlString);

        try {
            // START: loading-xml-from-file
            // From file (recommended - handles encoding automatically)
            // import java.nio.file.Path;
            Document doc2 = Document.of(tempFile);
            Editor editor2 = new Editor(doc2);
            // END: loading-xml-from-file

            Assertions.assertEquals("config", editor2.root().name());

            // START: loading-xml-from-inputstream
            // From InputStream with automatic encoding detection
            // import java.io.InputStream;
            // import java.nio.file.Files;
            // import java.nio.file.Path;
            try (InputStream inputStream = Files.newInputStream(tempFile)) {
                Document doc3 = Document.of(inputStream);
                Editor editor3 = new Editor(doc3);
                Assertions.assertEquals("config", editor3.root().name());
            }
            // END: loading-xml-from-inputstream
        } finally {
            Files.deleteIfExists(tempFile);
        }

        // START: loading-xml-config
        // With custom configuration
        Document docWithConfig = Document.of(xmlString);
        Editor editorWithConfig = new Editor(docWithConfig, DomTripConfig.prettyPrint());
        // END: loading-xml-config

        Assertions.assertNotNull(editorWithConfig.config());
    }

    @Test
    public void demonstrateFindingElements() {
        // START: finding-elements-basic
        String xml = createConfigXml();
        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        // Find by name
        Element root = editor.root();
        Element database = root.descendant("database").orElseThrow();

        // Modern navigation with Optional
        Optional<Element> host = database.child("host");
        if (host.isPresent()) {
            System.out.println("Host: " + host.orElseThrow().textContent());
        }
        // END: finding-elements-basic

        Assertions.assertTrue(host.isPresent());
        Assertions.assertEquals("localhost", host.orElseThrow().textContent());
    }

    @Test
    public void demonstrateAddingElements() {
        // START: adding-elements-simple
        String xml = createTestXml("parent");
        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element parent = editor.root();

        // Simple element with text
        editor.addElement(parent, "name", "value");
        // END: adding-elements-simple

        Assertions.assertTrue(editor.toXml().contains("<name>value</name>"));

        // START: adding-elements-attributes
        // Element with attributes
        Element element = editor.addElement(parent, "dependency");
        editor.setAttribute(element, "scope", "test");
        editor.addElement(element, "groupId", "junit");
        // END: adding-elements-attributes

        Assertions.assertTrue(editor.toXml().contains("scope=\"test\""));
    }

    @Test
    public void demonstrateModifyingContent() {
        // START: modifying-content
        String xml = createConfigXml();
        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        // Change text content
        Element version = editor.root().descendant("version").orElseThrow();
        editor.setTextContent(version, "2.0.0");

        // Modify attributes
        Element database = editor.root().descendant("database").orElseThrow();
        editor.setAttribute(database, "id", "main-db");

        // Add comments
        editor.addComment(editor.root(), " This is a comment ");
        // END: modifying-content

        String result = editor.toXml();
        Assertions.assertTrue(result.contains("2.0.0"));
        Assertions.assertTrue(result.contains("id=\"main-db\""));
        Assertions.assertTrue(result.contains("<!-- This is a comment -->"));
    }

    @Test
    public void demonstrateConfiguration() {
        // START: configuration-options
        String xml = createConfigXml();
        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        // Pretty printing
        DomTripConfig config = DomTripConfig.prettyPrint();
        String prettyXml = editor.toXml(config);

        // Minimal output
        String minimalXml = editor.toXml(DomTripConfig.minimal());

        // Custom configuration
        DomTripConfig custom = DomTripConfig.defaults()
                .withIndentString("  ") // 2 spaces
                .withCommentPreservation(true);
        String customXml = editor.toXml(custom);
        // END: configuration-options

        Assertions.assertFalse(prettyXml.isEmpty());
        Assertions.assertFalse(minimalXml.isEmpty());
        Assertions.assertFalse(customXml.isEmpty());
    }

    @Test
    public void demonstrateStreamBasedNavigation() {
        // START: stream-based-navigation
        String xml = createConfigXml();
        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        // Stream-based navigation
        editor.root()
                .descendants()
                .filter(e -> e.name().equals("port"))
                .findFirst()
                .ifPresent(port -> System.out.println("Port: " + port.textContent()));
        // END: stream-based-navigation

        Assertions.assertTrue(editor.root().descendants().anyMatch(e -> e.name().equals("port")));
    }

    @Test
    public void demonstrateElementBuilders() {
        // START: element-builders
        String xml = createTestXml("parent");
        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element parent = editor.root();

        // Using element builders for complex structures
        Element dependency = editor.addElement(parent, "dependency");
        editor.addElement(dependency, "groupId", "org.example");
        editor.addElement(dependency, "artifactId", "my-library");
        editor.addElement(dependency, "version", "1.0.0");
        editor.setAttribute(dependency, "scope", "compile");
        // END: element-builders

        Assertions.assertTrue(editor.toXml().contains("org.example"));
    }

    @Test
    public void demonstrateRemovingElements() {
        // START: removing-elements
        String xml = createConfigXml();
        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        // Remove element by reference
        Element database = editor.root().descendant("database").orElseThrow();
        editor.removeElement(database);

        // Remove by finding first occurrence
        Element version = editor.root().descendant("version").orElse(null);
        if (version != null) {
            editor.removeElement(version);
        }
        // END: removing-elements

        Assertions.assertFalse(editor.toXml().contains("database"));
    }
}
