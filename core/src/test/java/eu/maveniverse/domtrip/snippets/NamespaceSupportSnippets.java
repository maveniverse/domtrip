package eu.maveniverse.domtrip.snippets;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import eu.maveniverse.domtrip.QName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Snippet tests for namespace support features documentation.
 */
public class NamespaceSupportSnippets extends BaseSnippetTest {

    @Test
    public void demonstrateBasicNamespaceHandling() {
        // START: basic-namespace-handling
        String xml =
                """
            <project xmlns="http://maven.apache.org/POM/4.0.0">
                <groupId>com.example</groupId>
                <artifactId>my-app</artifactId>
            </project>
            """;

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        Element root = doc.root();

        // Access namespace information
        String namespace = root.namespaceURI(); // "http://maven.apache.org/POM/4.0.0"
        String localName = root.localName(); // "project"
        String qualifiedName = root.qualifiedName(); // "project"
        // END: basic-namespace-handling

        Assertions.assertEquals("http://maven.apache.org/POM/4.0.0", namespace);
        Assertions.assertEquals("project", localName);
        Assertions.assertEquals("project", qualifiedName);
    }

    @Test
    public void demonstratePrefixedNamespaces() {
        // START: prefixed-namespaces
        String xml =
                """
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                <groupId>com.example</groupId>
                <xsi:schemaLocation>http://maven.apache.org/POM/4.0.0
                                    http://maven.apache.org/xsd/maven-4.0.0.xsd</xsi:schemaLocation>
            </project>
            """;

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        Element schemaLocation = doc.root().child("xsi:schemaLocation").orElseThrow();

        // Access prefixed element information
        String namespace = schemaLocation.namespaceURI(); // "http://www.w3.org/2001/XMLSchema-instance"
        String localName = schemaLocation.localName(); // "schemaLocation"
        String prefix = schemaLocation.prefix(); // "xsi"
        String qualifiedName = schemaLocation.qualifiedName(); // "xsi:schemaLocation"
        // END: prefixed-namespaces

        Assertions.assertEquals("http://www.w3.org/2001/XMLSchema-instance", namespace);
        Assertions.assertEquals("schemaLocation", localName);
        Assertions.assertEquals("xsi", prefix);
        Assertions.assertEquals("xsi:schemaLocation", qualifiedName);
    }

    @Test
    public void demonstrateNamespaceDeclarations() {
        // START: namespace-declarations
        String xml =
                """
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xmlns:custom="http://example.com/custom">
                <groupId>com.example</groupId>
            </project>
            """;

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        Element root = doc.root();

        // Access namespace declarations
        String defaultNamespace = root.namespaceDeclaration(""); // Default namespace
        String xsiNamespace = root.namespaceDeclaration("xsi");
        String customNamespace = root.namespaceDeclaration("custom");
        // END: namespace-declarations

        Assertions.assertEquals("http://maven.apache.org/POM/4.0.0", defaultNamespace);
        Assertions.assertEquals("http://www.w3.org/2001/XMLSchema-instance", xsiNamespace);
        Assertions.assertEquals("http://example.com/custom", customNamespace);
    }

    @Test
    public void demonstrateAddingNamespaceDeclarations() {
        // START: adding-namespace-declarations
        String xml =
                """
            <project xmlns="http://maven.apache.org/POM/4.0.0">
                <groupId>com.example</groupId>
            </project>
            """;

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        Element root = doc.root();

        // Add new namespace declaration
        root.namespaceDeclaration("custom", "http://example.com/custom");

        // Now you can use the namespace
        Element metadata = editor.addElement(root, "custom:metadata");
        editor.setTextContent(metadata, "Custom data");

        String result = editor.toXml();
        // END: adding-namespace-declarations

        Assertions.assertTrue(result.contains("xmlns:custom=\"http://example.com/custom\""));
        Assertions.assertTrue(result.contains("custom:metadata"));
    }

    @Test
    public void demonstrateQNameUsage() {
        // START: qname-usage
        // Create QName for namespaced element
        QName soapEnvelope = QName.of("http://schemas.xmlsoap.org/soap/envelope/", "Envelope", "soap");

        // Create element using QName
        Element envelope = Element.of(soapEnvelope);

        // QName provides namespace information
        String namespace = soapEnvelope.namespaceURI(); // "http://schemas.xmlsoap.org/soap/envelope/"
        String localName = soapEnvelope.localName(); // "Envelope"
        String prefix = soapEnvelope.prefix(); // "soap"
        String qualified = soapEnvelope.qualifiedName(); // "soap:Envelope"
        // END: qname-usage

        Assertions.assertEquals("http://schemas.xmlsoap.org/soap/envelope/", namespace);
        Assertions.assertEquals("Envelope", localName);
        Assertions.assertEquals("soap", prefix);
        Assertions.assertEquals("soap:Envelope", qualified);
    }

    @Test
    public void demonstrateNamespaceAwareNavigation() {
        // START: namespace-aware-navigation
        String xml =
                """
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                     xmlns:custom="http://example.com/custom">
                <groupId>com.example</groupId>
                <custom:metadata>
                    <custom:author>John Doe</custom:author>
                </custom:metadata>
            </project>
            """;

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        Element root = doc.root();

        // Find elements by qualified name (prefix:localName)
        Element metadata = root.child("custom:metadata").orElseThrow();
        Element author = metadata.child("custom:author").orElseThrow();

        String authorName = author.textContent(); // "John Doe"
        // END: namespace-aware-navigation

        Assertions.assertEquals("John Doe", authorName);
        Assertions.assertEquals("http://example.com/custom", metadata.namespaceURI());
        Assertions.assertEquals("http://example.com/custom", author.namespaceURI());
    }

    @Test
    public void demonstrateNamespaceInheritance() {
        // START: namespace-inheritance
        String xml =
                """
            <project xmlns="http://maven.apache.org/POM/4.0.0">
                <dependencies>
                    <dependency>
                        <groupId>junit</groupId>
                    </dependency>
                </dependencies>
            </project>
            """;

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        Element dependencies = doc.root().child("dependencies").orElseThrow();
        Element dependency = dependencies.child("dependency").orElseThrow();
        Element groupId = dependency.child("groupId").orElseThrow();

        // All inherit the default namespace
        Assertions.assertEquals("http://maven.apache.org/POM/4.0.0", dependencies.namespaceURI());
        Assertions.assertEquals("http://maven.apache.org/POM/4.0.0", dependency.namespaceURI());
        Assertions.assertEquals("http://maven.apache.org/POM/4.0.0", groupId.namespaceURI());
        // END: namespace-inheritance
    }

    @Test
    public void demonstrateNamespaceAttributeHandling() {
        // START: namespace-attribute-handling
        String xml =
                """
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 schema.xsd">
                <groupId>com.example</groupId>
            </project>
            """;

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        Element root = doc.root();

        // Access namespaced attributes by qualified name
        String schemaLocation = root.attribute("xsi:schemaLocation");

        // Set namespaced attributes
        editor.setAttribute(root, "xsi:noNamespaceSchemaLocation", "local.xsd");

        String result = editor.toXml();
        // END: namespace-attribute-handling

        Assertions.assertEquals("http://maven.apache.org/POM/4.0.0 schema.xsd", schemaLocation);
        Assertions.assertTrue(result.contains("xsi:noNamespaceSchemaLocation=\"local.xsd\""));
    }

    @Test
    public void demonstrateComplexNamespaceScenario() {
        // START: complex-namespace-scenario
        // Create a SOAP envelope with multiple namespaces
        QName soapEnvelope = QName.of("http://schemas.xmlsoap.org/soap/envelope/", "Envelope", "soap");
        Element envelope = Element.of(soapEnvelope);

        // Add body with same namespace
        QName soapBody = QName.of("http://schemas.xmlsoap.org/soap/envelope/", "Body", "soap");
        Element body = Element.of(soapBody);
        envelope.addNode(body);

        // Add custom content with different namespace
        Element customElement = Element.of("GetUserInfo").namespaceDeclaration(null, "http://example.com/userservice");
        body.addNode(customElement);

        // Create document and editor
        Document doc = new Document();
        doc.root(envelope);
        doc.addNode(envelope);
        Editor editor = new Editor(doc);

        String result = editor.toXml();
        // END: complex-namespace-scenario

        Assertions.assertTrue(result.contains("soap:Envelope"));
        Assertions.assertTrue(result.contains("soap:Body"));
        Assertions.assertTrue(result.contains("GetUserInfo"));
        Assertions.assertTrue(result.contains("xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\""));
    }
}
