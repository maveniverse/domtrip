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
    public void demonstrateBasicElementCreation() throws DomTripException {
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
    public void demonstrateAdvancedElementCreation() throws DomTripException {
        // START: advanced-element-creation
        // Element with complex structure using fluent API
        Element project = Element.of("project").attribute("xmlns", "http://maven.apache.org/POM/4.0.0");

        project.addChild(Element.text("modelVersion", "4.0.0"));
        project.addChild(Element.text("groupId", "com.example"));
        project.addChild(Element.text("artifactId", "my-project"));
        project.addChild(Element.text("version", "1.0.0"));

        // Element with CDATA content
        Element script = Element.of("script").attribute("type", "text/javascript");
        script.addChild(Text.cdata("function test() { return x < y && z > 0; }"));
        // END: advanced-element-creation

        Assertions.assertEquals("http://maven.apache.org/POM/4.0.0", project.attribute("xmlns"));
        Assertions.assertEquals(
                "com.example", project.childElement("groupId").orElseThrow().textContent());
        Assertions.assertTrue(script.textContent().contains("function test()"));
    }

    @Test
    public void demonstrateNamespacedElements() throws DomTripException {
        // START: namespaced-elements
        // Namespaced element using QName
        QName soapEnvelope = QName.of("http://schemas.xmlsoap.org/soap/envelope/", "Envelope", "soap");
        Element envelope = Element.of(soapEnvelope);

        QName soapBody = QName.of("http://schemas.xmlsoap.org/soap/envelope/", "Body", "soap");
        Element body = Element.text(soapBody, "Body content");
        envelope.addChild(body);
        // END: namespaced-elements

        Assertions.assertEquals("soap:Envelope", envelope.qualifiedName());
        Assertions.assertEquals("Body content", body.textContent());
        Assertions.assertEquals("http://schemas.xmlsoap.org/soap/envelope/", envelope.namespaceURI());
    }

    @Test
    public void demonstrateDocumentCreation() throws DomTripException {
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
                "value", parsedDoc.root().childElement("child").orElseThrow().textContent());
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
    public void demonstrateFluentElementAddition() throws DomTripException {
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

        Element dependency = root.childElement("dependency").orElseThrow();
        Assertions.assertEquals("test", dependency.attribute("scope"));
        Assertions.assertEquals("junit", dependency.textContent());

        Comment comment = root.children()
                .filter(node -> node instanceof Comment)
                .map(node -> (Comment) node)
                .findFirst()
                .orElseThrow();
        Assertions.assertEquals(" Configuration section ", comment.content());
    }

    @Test
    public void demonstrateAttributeCreation() throws DomTripException {
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
    public void demonstrateComplexStructureCreation() throws DomTripException {
        // START: complex-structure-creation
        // Create a complete Maven dependency structure
        Element dependency = Element.of("dependency");
        dependency.addChild(Element.text("groupId", "junit"));
        dependency.addChild(Element.text("artifactId", "junit"));
        dependency.addChild(Element.text("version", "4.13.2"));
        dependency.addChild(Element.text("scope", "test"));

        // Create dependencies container
        Element dependencies = Element.of("dependencies");
        dependencies.addChild(dependency);

        // Create complete project
        Element project = Element.of("project").attribute("xmlns", "http://maven.apache.org/POM/4.0.0");
        project.addChild(Element.text("modelVersion", "4.0.0"));
        project.addChild(Element.text("groupId", "com.example"));
        project.addChild(Element.text("artifactId", "my-app"));
        project.addChild(dependencies);
        // END: complex-structure-creation

        Assertions.assertEquals(
                "junit", dependency.childElement("groupId").orElseThrow().textContent());
        Assertions.assertEquals(
                "test", dependency.childElement("scope").orElseThrow().textContent());
        Assertions.assertEquals(
                "my-app", project.childElement("artifactId").orElseThrow().textContent());
        Assertions.assertTrue(project.childElement("dependencies").isPresent());
    }

    @Test
    public void demonstrateFactoryMethodBestPractices() throws DomTripException {
        // START: factory-method-best-practices
        // ✅ Good - clean and direct
        Element version = Element.text("version", "1.0.0");
        Comment comment = Comment.of("Configuration");
        Text cdata = Text.cdata("script content");

        // ✅ Good - readable fluent chain
        Element dependency = Element.of("dependency").attribute("scope", "test").attribute("optional", "true");

        dependency.addChild(Element.text("groupId", "junit"));
        dependency.addChild(Element.text("artifactId", "junit"));

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
    public void demonstrateReusableFactoryMethods() throws DomTripException {
        // START: reusable-factory-methods
        // Extract complex structures to methods for reusability
        Element junitDep = createDependency("junit", "junit", "4.13.2");
        Element mockitoDep = createDependency("org.mockito", "mockito-core", "4.6.1");

        // Use in document
        Element dependencies = Element.of("dependencies");
        dependencies.addChild(junitDep);
        dependencies.addChild(mockitoDep);
        // END: reusable-factory-methods

        Assertions.assertEquals(
                "junit", junitDep.childElement("groupId").orElseThrow().textContent());
        Assertions.assertEquals(
                "org.mockito", mockitoDep.childElement("groupId").orElseThrow().textContent());
        Assertions.assertEquals(2, dependencies.childElements().count());
    }

    // Helper method for the reusable factory methods example
    @Test
    public void demonstrateSimpleDocumentCreation() throws DomTripException {
        // START: simple-document-creation
        // Basic document
        Document doc = Document.withRootElement("project");

        // Document with XML declaration
        Document docWithDecl = Document.withXmlDeclaration("1.0", "UTF-8");
        docWithDecl.root(Element.of("project"));

        // Parse existing XML from string
        String xmlString = "<root><child>value</child></root>";
        Document parsedDoc = Document.of(xmlString);
        // END: simple-document-creation

        Assertions.assertEquals("project", doc.root().name());
        Assertions.assertEquals("project", docWithDecl.root().name());
        Assertions.assertEquals("root", parsedDoc.root().name());
    }

    @Test
    public void demonstrateFileBasedDocumentLoading() throws Exception {
        // START: file-based-document-loading
        // Basic file loading with automatic encoding detection
        // Document doc = Document.of(Path.of("config.xml"));

        // Error handling
        try {
            String xmlContent = createConfigXml();
            Document doc = Document.of(xmlContent);
            Editor editor = new Editor(doc);
            // ... process document
            String result = editor.toXml();
            Assertions.assertNotNull(result);
        } catch (Exception e) {
            System.err.println("Failed to parse XML: " + e.getMessage());
        }
        // END: file-based-document-loading
    }

    @Test
    public void demonstrateAdvancedDocumentCreation() throws DomTripException {
        // START: advanced-document-creation
        // Document with processing instructions
        Document doc = Document.withXmlDeclaration("1.0", "UTF-8");
        doc.addChild(ProcessingInstruction.of("xml-stylesheet", "type=\"text/xsl\" href=\"style.xsl\""));
        doc.root(Element.of("project"));
        // END: advanced-document-creation

        Assertions.assertEquals("project", doc.root().name());
    }

    @Test
    public void demonstrateTextNodeCreation() {
        // START: text-node-creation
        // Simple text
        Text text = Text.of("Hello World");

        // CDATA text
        Text cdata = Text.cdata("<script>alert('test');</script>");

        // Text with explicit CDATA flag
        Text explicitCdata = Text.of("content", true);
        // END: text-node-creation

        Assertions.assertEquals("Hello World", text.content());
        Assertions.assertTrue(cdata.cdata());
        Assertions.assertTrue(explicitCdata.cdata());
    }

    @Test
    public void demonstrateCommentCreation() {
        // START: comment-creation
        // Simple comment
        Comment comment = Comment.of("This is a comment");

        // Modify comment content
        Comment modified = Comment.of("Initial content");
        modified.content("Updated content");
        // END: comment-creation

        Assertions.assertEquals("This is a comment", comment.content());
        Assertions.assertEquals("Updated content", modified.content());
    }

    @Test
    public void demonstrateProcessingInstructionCreation() {
        // START: processing-instruction-creation
        // Processing instruction with target and data
        ProcessingInstruction pi = ProcessingInstruction.of("xml-stylesheet", "type=\"text/css\" href=\"style.css\"");

        // Processing instruction with target only
        ProcessingInstruction simple = ProcessingInstruction.of("target");

        // Modify processing instruction
        ProcessingInstruction modified = ProcessingInstruction.of("target", "data");
        modified.target("new-target");
        modified.data("new data");
        // END: processing-instruction-creation

        Assertions.assertEquals("xml-stylesheet", pi.target());
        Assertions.assertEquals("target", simple.target());
        Assertions.assertEquals("new-target", modified.target());
        Assertions.assertEquals("new data", modified.data());
    }

    @Test
    public void demonstrateFluentChaining() throws DomTripException {
        // START: fluent-chaining
        // ✅ Good - readable fluent chain
        Element dependency = Element.of("dependency").attribute("scope", "test").attribute("optional", "true");

        dependency.addChild(Element.text("groupId", "junit"));
        dependency.addChild(Element.text("artifactId", "junit"));
        // END: fluent-chaining

        Assertions.assertEquals("test", dependency.attribute("scope"));
        Assertions.assertEquals("true", dependency.attribute("optional"));
    }

    @Test
    public void demonstratePerformanceOptimizations() throws DomTripException {
        // START: performance-optimizations
        // Efficient - direct object creation and modification
        Element element = Element.of("dependency").attribute("scope", "test").attribute("optional", "true");

        // All operations modify the same object instance
        // No intermediate objects or copying involved
        // END: performance-optimizations

        Assertions.assertEquals("dependency", element.name());
        Assertions.assertEquals("test", element.attribute("scope"));
    }

    private Element createDependency(String groupId, String artifactId, String version) throws DomTripException {
        Element dependency = Element.of("dependency");
        dependency.addChild(Element.text("groupId", groupId));
        dependency.addChild(Element.text("artifactId", artifactId));
        dependency.addChild(Element.text("version", version));
        return dependency;
    }
}
