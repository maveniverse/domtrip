package eu.maveniverse.domtrip.snippets;

import eu.maveniverse.domtrip.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Snippet tests for Editor API documentation.
 */
public class EditorApiSnippets extends BaseSnippetTest {

    @Test
    public void demonstrateBasicEditorUsage() {
        // START: basic-editor-usage
        // Create new editor
        Editor editor = new Editor();

        // Create document with root element
        editor.createDocument("project");
        Element root = editor.root();

        // Add elements
        editor.addElement(root, "groupId", "com.example");
        editor.addElement(root, "artifactId", "my-app");
        editor.addElement(root, "version", "1.0.0");

        // Serialize to XML
        String xml = editor.toXml();
        // END: basic-editor-usage

        Assertions.assertNotNull(xml);
        Assertions.assertTrue(xml.contains("com.example"));
        Assertions.assertTrue(xml.contains("my-app"));
        Assertions.assertTrue(xml.contains("1.0.0"));
    }

    @Test
    public void demonstrateSerializationOptions() {
        // START: serialization-options
        String xml = "<root><child>value</child></root>";
        Editor editor = new Editor(Document.of(xml));

        // Default serialization
        String defaultXml = editor.toXml();

        // Pretty printed
        String prettyXml = editor.toXml(DomTripConfig.prettyPrint());

        // Minimal output
        String minimalXml = editor.toXml(DomTripConfig.minimal());
        // END: serialization-options

        Assertions.assertNotNull(defaultXml);
        Assertions.assertNotNull(prettyXml);
        Assertions.assertNotNull(minimalXml);
    }

    @Test
    public void demonstrateElementFinding() {
        // START: element-finding
        String xml = "<project><version>1.0</version><dependency><version>2.0</version></dependency></project>";
        Editor editor = new Editor(Document.of(xml));
        Element root = editor.root();

        // Find first element with name
        Element version = root.child("version").orElse(null);

        // Find all elements with name using streams
        List<Element> allVersions = root.descendants("version").toList();
        // END: element-finding

        Assertions.assertNotNull(version);
        Assertions.assertEquals("1.0", version.textContent());
        Assertions.assertEquals(2, allVersions.size());
    }

    @Test
    public void demonstrateElementAddition() {
        // START: element-addition
        String xml = "<project></project>";
        Editor editor = new Editor(Document.of(xml));
        Element parent = editor.root();

        // Add element without content
        Element child = editor.addElement(parent, "newChild");

        // Add element with text content
        Element version = editor.addElement(parent, "version", "1.0.0");
        // END: element-addition

        Assertions.assertNotNull(child);
        Assertions.assertEquals("newChild", child.name());
        Assertions.assertEquals("1.0.0", version.textContent());
    }

    @Test
    public void demonstrateBatchElementCreation() {
        // START: batch-element-creation
        String xml = "<project></project>";
        Editor editor = new Editor(Document.of(xml));
        Element parent = editor.root();

        Map<String, String> properties = Map.of(
                "groupId", "com.example",
                "artifactId", "my-app",
                "version", "1.0.0");
        editor.addElements(parent, properties);
        // END: batch-element-creation

        Assertions.assertEquals(
                "com.example", parent.child("groupId").orElseThrow().textContent());
        Assertions.assertEquals(
                "my-app", parent.child("artifactId").orElseThrow().textContent());
        Assertions.assertEquals("1.0.0", parent.child("version").orElseThrow().textContent());
    }

    @Test
    public void demonstrateElementRemoval() {
        // START: element-removal
        String xml = "<project><deprecated>old</deprecated><version>1.0</version></project>";
        Editor editor = new Editor(Document.of(xml));
        Element root = editor.root();

        Element toRemove = root.child("deprecated").orElse(null);
        if (toRemove != null) {
            editor.removeElement(toRemove);
        }

        String result = editor.toXml();
        // END: element-removal

        Assertions.assertFalse(result.contains("deprecated"));
        Assertions.assertTrue(result.contains("version"));
    }

    @Test
    public void demonstrateTextContentOperations() {
        // START: text-content-operations
        String xml = "<project><version>1.0</version></project>";
        Editor editor = new Editor(Document.of(xml));
        Element root = editor.root();

        Element version = root.child("version").orElse(null);

        // Get text content
        String content = version.textContent();

        // Set text content
        editor.setTextContent(version, "2.0.0");
        // END: text-content-operations

        Assertions.assertEquals("1.0", content);
        Assertions.assertEquals("2.0.0", version.textContent());
    }

    @Test
    public void demonstrateAttributeOperations() {
        // START: attribute-operations
        String xml = "<dependency scope='test'></dependency>";
        Editor editor = new Editor(Document.of(xml));
        Element element = editor.root();

        // Get attribute value
        String scope = element.attribute("scope");

        // Set attribute value
        editor.setAttribute(element, "optional", "true");

        // Remove attribute
        editor.removeAttribute(element, "scope");
        // END: attribute-operations

        Assertions.assertEquals("test", scope);
        Assertions.assertEquals("true", element.attribute("optional"));
        Assertions.assertNull(element.attribute("scope"));
    }

    @Test
    public void demonstrateBatchAttributeOperations() {
        // START: batch-attribute-operations
        String xml = "<dependency></dependency>";
        Editor editor = new Editor(Document.of(xml));
        Element element = editor.root();

        Map<String, String> attrs = Map.of(
                "scope", "test",
                "optional", "true");
        editor.setAttributes(element, attrs);
        // Each attribute uses inferred formatting based on existing patterns
        // END: batch-attribute-operations

        Assertions.assertEquals("test", element.attribute("scope"));
        Assertions.assertEquals("true", element.attribute("optional"));
    }

    @Test
    public void demonstrateAdvancedAttributeFormatting() {
        // START: advanced-attribute-formatting
        String xml = "<element attr1='existing'></element>";
        Editor editor = new Editor(Document.of(xml));
        Element element = editor.root();

        // Get attribute object for advanced manipulation
        Attribute attr = element.attributeObject("attr1");
        if (attr != null) {
            attr.value("updated"); // Preserves all formatting
        }

        // Create custom formatted attribute
        Attribute customAttr = Attribute.of("newAttr", "value", QuoteStyle.SINGLE);
        element.attributeObject("newAttr", customAttr);
        // END: advanced-attribute-formatting

        Assertions.assertEquals("updated", element.attribute("attr1"));
        Assertions.assertEquals("value", element.attribute("newAttr"));
    }

    @Test
    public void demonstrateCommentOperations() {
        // START: comment-operations
        String xml = "<project><version>1.0</version></project>";
        Editor editor = new Editor(Document.of(xml));
        Element parent = editor.root();
        Element version = parent.child("version").orElse(null);

        // Add comment as child
        editor.addComment(parent, " This is a comment ");

        // Add comment using fluent API
        editor.add().comment().to(parent).withContent(" Configuration section ").build();
        // END: comment-operations

        String result = editor.toXml();
        Assertions.assertTrue(result.contains("This is a comment"));
        Assertions.assertTrue(result.contains("Configuration section"));
    }

    @Test
    public void demonstrateFluentBuilderApi() {
        // START: fluent-builder-api
        String xml = "<project></project>";
        Editor editor = new Editor(Document.of(xml));
        Element parent = editor.root();

        editor.add()
                .element("dependency")
                .to(parent)
                .withAttribute("scope", "test")
                .withText("content")
                .build();

        editor.add().comment().to(parent).withContent(" This is a comment ").build();
        // END: fluent-builder-api

        String result = editor.toXml();
        Assertions.assertTrue(result.contains("dependency"));
        Assertions.assertTrue(result.contains("scope=\"test\""));
        Assertions.assertTrue(result.contains("content"));
        Assertions.assertTrue(result.contains("This is a comment"));
    }

    @Test
    public void demonstrateExceptionHandling() {
        // START: exception-handling
        try {
            String malformedXml = "<root><unclosed>";
            Editor editor = new Editor(Document.of(malformedXml));
            // ... editing operations
        } catch (DomTripException e) {
            // Handle parsing/editing errors
            System.err.println("XML error: " + e.getMessage());
        }
        // END: exception-handling

        // Test passes if no unexpected exception is thrown
        Assertions.assertTrue(true);
    }

    @Test
    public void demonstrateSafeNavigation() {
        // START: safe-navigation
        String xml = "<project><version>1.0</version></project>";
        Editor editor = new Editor(Document.of(xml));
        Element root = editor.root();

        // ✅ Safe navigation using Optional
        Optional<Element> element = root.child("optional");
        element.ifPresent(el -> editor.setTextContent(el, "value"));

        // ✅ Or use null check pattern
        Element version = root.child("version").orElse(null);
        if (version != null) {
            editor.setTextContent(version, "updated");
        }
        // END: safe-navigation

        // Test passes - demonstrates safe patterns
        Assertions.assertNotNull(root);
    }

    @Test
    public void demonstrateBestPractices() {
        // START: best-practices
        String xml = "<project></project>";
        Editor editor = new Editor(Document.of(xml));
        Element parent = editor.root();

        // ✅ Efficient batch operation
        Map<String, String> properties = Map.of(
                "groupId", "com.example",
                "artifactId", "my-app");
        editor.addElements(parent, properties);

        // ❌ Less efficient individual operations (shown for comparison)
        // editor.addElement(parent, "groupId", "com.example");
        // editor.addElement(parent, "artifactId", "my-app");
        // END: best-practices

        Assertions.assertEquals(
                "com.example", parent.child("groupId").orElseThrow().textContent());
        Assertions.assertEquals(
                "my-app", parent.child("artifactId").orElseThrow().textContent());
    }

    @Test
    public void demonstrateThreadSafetyPattern() {
        // START: thread-safety-pattern
        String xml = "<project></project>";
        Editor editor = new Editor(Document.of(xml));
        Element parent = editor.root();

        // ✅ Thread-safe usage (conceptual example)
        synchronized (editor) {
            editor.addElement(parent, "child", "value");
            String result = editor.toXml();
            Assertions.assertTrue(result.contains("child"));
        }

        // ✅ Or use separate editor instances per thread
        Editor editorForThread = new Editor(Document.of(xml));
        editorForThread.addElement(editorForThread.root(), "thread-child", "value");
        // END: thread-safety-pattern

        String result = editorForThread.toXml();
        Assertions.assertTrue(result.contains("thread-child"));
    }

    @Test
    public void demonstrateConfigurationAccess() {
        // START: configuration-access
        DomTripConfig config = DomTripConfig.prettyPrint();
        Editor editor = new Editor(config);

        // Get the configuration used by this editor
        DomTripConfig usedConfig = editor.config();
        // END: configuration-access

        Assertions.assertEquals(config, usedConfig);
    }

    @Test
    public void demonstrateBasicConstructors() {
        // START: basic-constructors
        // Default constructor
        Editor editor = new Editor();

        // With custom configuration
        DomTripConfig config = DomTripConfig.prettyPrint();
        Editor configuredEditor = new Editor(config);

        // With existing document
        String xml = "<project></project>";
        Document doc = Document.of(xml);
        Editor documentEditor = new Editor(doc);

        // With document and configuration
        Editor fullEditor = new Editor(doc, DomTripConfig.defaults());
        // END: basic-constructors

        Assertions.assertNotNull(editor);
        Assertions.assertNotNull(configuredEditor);
        Assertions.assertNotNull(documentEditor);
        Assertions.assertNotNull(fullEditor);
    }

    @Test
    public void demonstrateAdvancedConstructorExamples() {
        // START: advanced-constructor-examples
        // Working with an existing document
        String xmlString = "<project><version>1.0</version></project>";
        Document existingDoc = Document.of(xmlString);
        Editor editor = new Editor(existingDoc);

        // Working with a programmatically created document
        Document doc = Document.withRootElement("project");
        Editor programmaticEditor = new Editor(doc);

        // Continue editing
        Element root = programmaticEditor.root();
        programmaticEditor.addElement(root, "version", "1.0");
        // END: advanced-constructor-examples

        Assertions.assertNotNull(editor);
        Assertions.assertNotNull(programmaticEditor);
        Assertions.assertEquals(
                "1.0", programmaticEditor.root().child("version").orElseThrow().textContent());
    }

    @Test
    public void demonstrateBasicOperations() {
        // START: basic-operations
        // Get document
        Editor editor = new Editor();
        editor.createDocument("project");
        Document document = editor.document();

        // Get root element
        Element root = editor.root();

        // Create document with root element
        editor.createDocument("project");
        Element newRoot = editor.root(); // <project></project>

        // Serialize to XML
        String xml = editor.toXml();

        // Pretty printing
        String prettyXml = editor.toXml(DomTripConfig.prettyPrint());
        // END: basic-operations

        Assertions.assertNotNull(document);
        Assertions.assertNotNull(root);
        Assertions.assertNotNull(xml);
        Assertions.assertNotNull(prettyXml);
    }

    @Test
    public void demonstrateElementOperations() {
        // START: element-operations
        String xml = "<project><version>1.0</version><dependency><version>2.0</version></dependency></project>";
        Editor editor = new Editor(Document.of(xml));
        Element root = editor.root();

        // Find first element with name
        Element version = root.child("version").orElse(null);

        // Find all elements with name using descendants
        List<Element> allVersions = root.descendants("version").toList();

        // Add new child element
        Element child = editor.addElement(root, "newChild");

        // Remove element (if it exists)
        Element toRemove = root.child("deprecated").orElse(null);
        if (toRemove != null) {
            editor.removeElement(toRemove);
        }

        // Get text content
        String content = version != null ? version.textContent() : "";
        // END: element-operations

        Assertions.assertNotNull(version);
        Assertions.assertNotNull(allVersions);
        Assertions.assertNotNull(child);
        Assertions.assertEquals("1.0", content);
    }

    @Test
    public void demonstrateAttributeManagement() {
        // START: attribute-management
        String xml = "<dependency scope='test'></dependency>";
        Editor editor = new Editor(Document.of(xml));
        Element element = editor.root();

        // For XML: <element attr1='existing' attr2="another"/>
        editor.setAttribute(element, "attr1", "updated"); // Preserves single quotes
        editor.setAttribute(element, "attr3", "new"); // Infers quote style from existing

        // Remove attribute
        editor.removeAttribute(element, "deprecated");

        // Set multiple attributes
        Map<String, String> attrs = Map.of(
                "scope", "test",
                "optional", "true");
        editor.setAttributes(element, attrs);

        // Get attribute value
        String scopeValue = element.attribute("scope");

        // Check if attribute exists
        boolean hasOptional = element.hasAttribute("optional");
        // END: attribute-management

        Assertions.assertEquals("updated", element.attribute("attr1"));
        Assertions.assertEquals("new", element.attribute("attr3"));
        Assertions.assertEquals("test", scopeValue);
        Assertions.assertTrue(hasOptional);
    }

    @Test
    public void demonstrateCommentManagement() {
        // START: comment-management
        String xml = "<project><version>1.0</version></project>";
        Editor editor = new Editor(Document.of(xml));
        Element parent = editor.root();

        // Add comment as child of parent
        editor.addComment(parent, " Configuration section ");

        // Using fluent builder API for comments
        editor.add().comment().to(parent).withContent(" End of configuration ").build();
        // END: comment-management

        String result = editor.toXml();
        Assertions.assertTrue(result.contains("Configuration section"));
        Assertions.assertTrue(result.contains("End of configuration"));
    }

    @Test
    public void demonstrateSpecificExceptionHandling() {
        // START: specific-exception-handling
        // ✅ Specific exception handling
        try {
            String xmlContent = "<root><unclosed>";
            Document doc = Document.of(xmlContent);
            Editor editor = new Editor(doc);
        } catch (DomTripException e) {
            // Handle DomTrip errors (including parsing errors)
            System.err.println("DomTrip error: " + e.getMessage());
        }
        // END: specific-exception-handling

        // Test passes - demonstrates exception handling patterns
        Assertions.assertTrue(true);
    }
}
