package eu.maveniverse.domtrip.snippets;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripConfig;
import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Snippet tests for API documentation and advanced features.
 */
public class ApiDocumentationSnippets extends BaseSnippetTest {

    @Test
    public void demonstrateElementBuilders() throws DomTripException {
        // START: fluent-element-builders
        String xml = createTestXml("parent");
        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element parent = editor.root();

        // Using fluent builders
        Element dependency = Element.of("dependency").attribute("scope", "test");
        dependency.addNode(Element.text("groupId", "junit"));
        dependency.addNode(Element.text("artifactId", "junit"));
        parent.addNode(dependency);
        // END: fluent-element-builders

        String result = editor.toXml();
        Assertions.assertTrue(result.contains("scope=\"test\""));
        Assertions.assertTrue(result.contains("groupId"));
    }

    @Test
    public void demonstrateNamespaceSupport() throws DomTripException {
        // START: namespace-support
        // Create elements with namespaces
        Element soapEnvelope =
                Element.of("soap:Envelope").attribute("xmlns:soap", "http://schemas.xmlsoap.org/soap/envelope/");

        // Namespace-aware navigation
        String xml = """
            <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                <soap:Body>
                    <content>Hello</content>
                </soap:Body>
            </soap:Envelope>
            """;
        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element root = editor.root();

        Optional<Element> body = root.child("soap:Body");
        if (body.isPresent()) {
            System.out.println("Found SOAP body");
        }

        // Get namespace information
        String localName = root.localName();
        String namespaceURI = root.namespaceURI();
        // END: namespace-support

        Assertions.assertTrue(body.isPresent());
        Assertions.assertEquals("Envelope", localName);
    }

    @Test
    public void demonstrateRealWorldMavenExample() throws Exception {
        // Test the method
        Path tempFile = Files.createTempFile("test-pom", ".xml");
        try {
            Files.writeString(tempFile, createMavenPomXml());
            addDependencyExample(tempFile.toString(), "junit", "junit", "4.13.2");

            String result = new String(Files.readAllBytes(tempFile), StandardCharsets.UTF_8);
            Assertions.assertTrue(result.contains("<groupId>junit</groupId>"));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    // START: real-world-maven-example
    public void addDependencyExample(String pomPath, String groupId, String artifactId, String version)
            throws Exception {
        // Load POM with automatic encoding detection
        Document doc = Document.of(Path.of(pomPath));
        Editor editor = new Editor(doc);

        // Find or create dependencies section
        Element project = editor.root();
        Element dependencies = project.descendant("dependencies").orElse(null);
        if (dependencies == null) {
            dependencies = editor.addElement(project, "dependencies");
        }

        // Add new dependency
        Element dependency = editor.addElement(dependencies, "dependency");
        editor.addElement(dependency, "groupId", groupId);
        editor.addElement(dependency, "artifactId", artifactId);
        editor.addElement(dependency, "version", version);

        // Save back to file (String-based)
        Files.writeString(Path.of(pomPath), editor.toXml());

        // Or save to OutputStream with proper encoding
        try (OutputStream outputStream = Files.newOutputStream(Path.of(pomPath))) {
            editor.document().toXml(outputStream);
        }

        System.out.println("✅ Added dependency: " + groupId + ":" + artifactId);
    }
    // END: real-world-maven-example

    @Test
    public void demonstrateWorkingWithExistingDocuments() throws DomTripException {
        // START: working-with-existing-documents
        // Parse with Document directly
        String xmlString = createConfigXml();
        Document document = Document.of(xmlString);

        // Create Editor from existing Document
        Editor editor = new Editor(document);

        // Now use the convenient Editor API
        Element root = editor.root();
        editor.addElement(root, "newChild", "value");
        editor.setAttribute(root, "version", "2.0");

        // Serialize with preserved formatting
        String result = editor.toXml();
        // END: working-with-existing-documents

        Assertions.assertTrue(result.contains("newChild"));
        Assertions.assertTrue(result.contains("version=\"2.0\""));
    }

    @Test
    public void demonstrateProgrammaticDocumentCreation() throws DomTripException {
        // START: programmatic-document-creation
        // Create document programmatically
        Document doc = Document.withRootElement("project");

        // Create Editor with custom config
        DomTripConfig config = DomTripConfig.prettyPrint().withIndentString("  ");
        Editor editor = new Editor(doc, config);

        // Build document structure
        Element root = editor.root();
        editor.addElement(root, "groupId", "com.example");
        editor.addElement(root, "artifactId", "my-project");
        // END: programmatic-document-creation

        String result = editor.toXml();
        Assertions.assertTrue(result.contains("com.example"));
        Assertions.assertTrue(result.contains("my-project"));
    }

    @Test
    public void demonstrateErrorHandling() throws DomTripException {
        // START: error-handling
        try {
            // Attempt to parse malformed XML
            String malformedXml = "<root><unclosed>";
            Document doc = Document.of(malformedXml);

            // This won't be reached due to parsing error
            Editor editor = new Editor(doc);
        } catch (Exception e) {
            // Handle parsing errors gracefully
            System.err.println("XML parsing failed: " + e.getMessage());

            // Provide fallback or user-friendly error message
            System.out.println("Please check your XML syntax and try again.");
        }

        // Safe navigation with Optional
        String xml = createConfigXml();
        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        editor.root()
                .descendant("nonexistent")
                .ifPresentOrElse(
                        element -> System.out.println("Found: " + element.name()),
                        () -> System.out.println("Element not found - using default behavior"));
        // END: error-handling

        // Test passes if we reach here without exceptions
        Assertions.assertTrue(true);
    }
}
