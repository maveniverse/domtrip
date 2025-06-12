package eu.maveniverse.domtrip.snippets;

import eu.maveniverse.domtrip.*;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Snippet tests for factory methods and fluent API documentation.
 */
public class FactoryMethodsSnippets extends BaseSnippetTest {

    @Test
    public void demonstrateBasicElementCreation() {
        // START: basic-element-creation
        // Simple elements
        Element version = Element.of("version");
        Element textElement = Element.text("version", "1.0.0");

        // Element with attributes using fluent API
        Element dependency = Element.of("dependency").attribute("scope", "test").attribute("optional", "true");

        // Element with multiple attributes at once
        Element div = Element.withAttributes(
                "div",
                Map.of(
                        "class", "container",
                        "id", "main",
                        "data-role", "content"));
        // END: basic-element-creation

        Assertions.assertEquals("version", version.name());
        Assertions.assertEquals("1.0.0", textElement.textContent());
        Assertions.assertEquals("test", dependency.attribute("scope"));
        Assertions.assertEquals("container", div.attribute("class"));
    }

    @Test
    public void demonstrateAdvancedElementCreation() {
        // START: advanced-element-creation
        // Element with complex structure using fluent API
        Element project = Element.of("project").attribute("xmlns", "http://maven.apache.org/POM/4.0.0");

        project.addNode(Element.text("modelVersion", "4.0.0"));
        project.addNode(Element.text("groupId", "com.example"));
        project.addNode(Element.text("artifactId", "my-project"));
        project.addNode(Element.text("version", "1.0.0"));

        // Element with CDATA content
        Element script = Element.of("script").attribute("type", "text/javascript");
        script.addNode(Text.cdata("function test() { return x < y && z > 0; }"));
        // END: advanced-element-creation

        Assertions.assertEquals("http://maven.apache.org/POM/4.0.0", project.attribute("xmlns"));
        Assertions.assertEquals(
                "com.example", project.child("groupId").orElseThrow().textContent());
        Assertions.assertTrue(script.textContent().contains("function test()"));
    }

    @Test
    public void demonstrateNamespacedElements() {
        // START: namespaced-elements
        // Namespaced element using QName
        QName soapEnvelope = QName.of("http://schemas.xmlsoap.org/soap/envelope/", "Envelope", "soap");
        Element envelope = Element.of(soapEnvelope);

        QName soapBody = QName.of("http://schemas.xmlsoap.org/soap/envelope/", "Body", "soap");
        Element body = Element.text(soapBody, "Body content");
        envelope.addNode(body);
        // END: namespaced-elements

        Assertions.assertEquals("soap:Envelope", envelope.qualifiedName());
        Assertions.assertEquals("Body content", body.textContent());
        Assertions.assertEquals("http://schemas.xmlsoap.org/soap/envelope/", envelope.namespaceURI());
    }

    @Test
    public void demonstrateDocumentCreation() {
        // START: document-creation
        // Basic document with root element
        Document doc = Document.withRootElement("project");

        // Document with XML declaration
        Document docWithDecl = Document.withXmlDeclaration("1.0", "UTF-8");
        docWithDecl.root(Element.of("project"));

        // Parse existing XML from string
        String xml = "<root><child>value</child></root>";
        Document parsedDoc = Document.of(xml);
        // END: document-creation

        Assertions.assertEquals("project", doc.root().name());
        Assertions.assertEquals("1.0", docWithDecl.version());
        Assertions.assertEquals("UTF-8", docWithDecl.encoding());
        Assertions.assertEquals(
                "value", parsedDoc.root().child("child").orElseThrow().textContent());
    }

    @Test
    public void demonstrateTextAndCommentCreation() {
        // START: text-comment-creation
        // Simple text
        Text text = Text.of("Hello World");

        // CDATA text
        Text cdata = Text.cdata("<script>alert('test');</script>");

        // Simple comment
        Comment comment = Comment.of("This is a comment");

        // Processing instruction with target and data
        ProcessingInstruction pi = ProcessingInstruction.of("xml-stylesheet", "type=\"text/css\" href=\"style.css\"");
        // END: text-comment-creation

        Assertions.assertEquals("Hello World", text.content());
        Assertions.assertTrue(cdata.cdata());
        Assertions.assertEquals("This is a comment", comment.content());
        Assertions.assertEquals("xml-stylesheet", pi.target());
    }

    @Test
    public void demonstrateFluentElementAddition() {
        // START: fluent-element-addition
        Document doc = Document.withRootElement("project");
        Editor editor = new Editor(doc);
        Element root = editor.root();

        // Fluent element addition
        editor.add()
                .element("dependency")
                .to(root)
                .withAttribute("scope", "test")
                .withText("junit")
                .build();

        // Add comment
        editor.add().comment().to(root).withContent(" Configuration section ").build();
        // END: fluent-element-addition

        Element dependency = root.child("dependency").orElseThrow();
        Assertions.assertEquals("test", dependency.attribute("scope"));
        Assertions.assertEquals("junit", dependency.textContent());

        Comment comment = root.nodes()
                .filter(node -> node instanceof Comment)
                .map(node -> (Comment) node)
                .findFirst()
                .orElseThrow();
        Assertions.assertEquals(" Configuration section ", comment.content());
    }

    @Test
    public void demonstrateAttributeCreation() {
        // START: attribute-creation
        // Simple attribute
        Attribute attr = Attribute.of("class", "important");

        // Attribute with specific quote style
        Attribute quoted = Attribute.of("id", "main", QuoteStyle.SINGLE);

        // Apply attributes to element
        Element element = Element.of("div");
        element.attribute("class", attr.value());
        element.attribute("id", quoted.value(), quoted.quoteStyle());
        // END: attribute-creation

        Assertions.assertEquals("important", attr.value());
        Assertions.assertEquals(QuoteStyle.SINGLE, quoted.quoteStyle());
        Assertions.assertEquals("important", element.attribute("class"));
        Assertions.assertEquals("main", element.attribute("id"));
    }

    @Test
    public void demonstrateComplexStructureCreation() {
        // START: complex-structure-creation
        // Create a complete Maven dependency structure
        Element dependency = Element.of("dependency");
        dependency.addNode(Element.text("groupId", "junit"));
        dependency.addNode(Element.text("artifactId", "junit"));
        dependency.addNode(Element.text("version", "4.13.2"));
        dependency.addNode(Element.text("scope", "test"));

        // Create dependencies container
        Element dependencies = Element.of("dependencies");
        dependencies.addNode(dependency);

        // Create complete project
        Element project = Element.of("project").attribute("xmlns", "http://maven.apache.org/POM/4.0.0");
        project.addNode(Element.text("modelVersion", "4.0.0"));
        project.addNode(Element.text("groupId", "com.example"));
        project.addNode(Element.text("artifactId", "my-app"));
        project.addNode(dependencies);
        // END: complex-structure-creation

        Assertions.assertEquals(
                "junit", dependency.child("groupId").orElseThrow().textContent());
        Assertions.assertEquals("test", dependency.child("scope").orElseThrow().textContent());
        Assertions.assertEquals(
                "my-app", project.child("artifactId").orElseThrow().textContent());
        Assertions.assertTrue(project.child("dependencies").isPresent());
    }

    @Test
    public void demonstrateFactoryMethodBestPractices() {
        // START: factory-method-best-practices
        // ✅ Good - clean and direct
        Element version = Element.text("version", "1.0.0");
        Comment comment = Comment.of("Configuration");
        Text cdata = Text.cdata("script content");

        // ✅ Good - readable fluent chain
        Element dependency = Element.of("dependency").attribute("scope", "test").attribute("optional", "true");

        dependency.addNode(Element.text("groupId", "junit"));
        dependency.addNode(Element.text("artifactId", "junit"));

        // ✅ Good - use appropriate factory methods
        Document withDecl = Document.withXmlDeclaration("1.0", "UTF-8");
        withDecl.root(Element.of("project"));
        // END: factory-method-best-practices

        Assertions.assertEquals("1.0.0", version.textContent());
        Assertions.assertEquals("Configuration", comment.content());
        Assertions.assertTrue(cdata.cdata());
        Assertions.assertEquals("test", dependency.attribute("scope"));
        Assertions.assertEquals("1.0", withDecl.version());
    }

    @Test
    public void demonstrateReusableFactoryMethods() {
        // START: reusable-factory-methods
        // Extract complex structures to methods for reusability
        Element junitDep = createDependency("junit", "junit", "4.13.2");
        Element mockitoDep = createDependency("org.mockito", "mockito-core", "4.6.1");

        // Use in document
        Element dependencies = Element.of("dependencies");
        dependencies.addNode(junitDep);
        dependencies.addNode(mockitoDep);
        // END: reusable-factory-methods

        Assertions.assertEquals("junit", junitDep.child("groupId").orElseThrow().textContent());
        Assertions.assertEquals(
                "org.mockito", mockitoDep.child("groupId").orElseThrow().textContent());
        Assertions.assertEquals(2, dependencies.children().count());
    }

    // Helper method for the reusable factory methods example
    private Element createDependency(String groupId, String artifactId, String version) {
        Element dependency = Element.of("dependency");
        dependency.addNode(Element.text("groupId", groupId));
        dependency.addNode(Element.text("artifactId", artifactId));
        dependency.addNode(Element.text("version", version));
        return dependency;
    }
}
