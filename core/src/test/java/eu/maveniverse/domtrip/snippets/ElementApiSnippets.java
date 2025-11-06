package eu.maveniverse.domtrip.snippets;

import eu.maveniverse.domtrip.*;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Code snippets for Element API documentation.
 */
public class ElementApiSnippets {

    @Test
    public void demonstrateElementCreation() {
        // START: element-creation
        // Create elements using factory methods
        Element simple = Element.of("dependency");
        Element withText = Element.of("groupId").textContent("com.example");
        Element withNamespace = Element.of(QName.of("http://maven.apache.org/POM/4.0.0", "project"));

        // Create empty element
        Element empty = new Element("placeholder");
        // END: element-creation

        Assertions.assertEquals("dependency", simple.name());
        Assertions.assertEquals("com.example", withText.textContent());
        Assertions.assertEquals("project", withNamespace.localName());
    }

    @Test
    public void demonstrateElementBuilder() {
        // START: element-builder
        // Build complex elements with fluent API
        Element dependency = Element.of("dependency")
                .attribute("scope", "test")
                .attribute("optional", "true")
                .textContent("junit:junit:4.13.2");

        // Chain multiple operations
        Element project = Element.of("project").attribute("xmlns", "http://maven.apache.org/POM/4.0.0");
        project.addNode(Element.of("groupId").textContent("com.example"));
        project.addNode(Element.of("artifactId").textContent("my-app"));
        project.addNode(Element.of("version").textContent("1.0.0"));
        // END: element-builder

        Assertions.assertEquals("test", dependency.attribute("scope"));
        Assertions.assertEquals(
                "com.example", project.child("groupId").orElseThrow().textContent());
    }

    @Test
    public void demonstrateBasicAttributes() {
        Element element = Element.of("dependency");

        // START: basic-attributes
        // Set attributes
        element.attribute("groupId", "junit");
        element.attribute("artifactId", "junit");
        element.attribute("version", "4.13.2");

        // Get attributes
        String groupId = element.attribute("groupId"); // "junit"
        String version = element.attribute("version"); // "4.13.2"

        // Check if attribute exists
        boolean hasScope = element.hasAttribute("scope"); // false

        // Remove attribute
        element.removeAttribute("version");
        // END: basic-attributes

        Assertions.assertEquals("junit", groupId);
        Assertions.assertEquals("4.13.2", version);
        Assertions.assertFalse(hasScope);
        Assertions.assertNull(element.attribute("version"));
    }

    @Test
    public void demonstrateTextContent() {
        Element element = Element.of("description");

        // START: text-content
        // Set text content
        element.textContent("This is the description");

        // Get text content
        String content = element.textContent(); // "This is the description"

        // Get trimmed content (removes leading/trailing whitespace)
        String trimmed = element.trimmedTextContent();
        // END: text-content

        Assertions.assertEquals("This is the description", content);
        Assertions.assertEquals("This is the description", trimmed);
    }

    @Test
    public void demonstrateWhitespacePreservingText() {
        String xml = "<item>   old value   </item>";
        Document doc = Document.of(xml);
        Element item = doc.root();

        // START: whitespace-preserving-text
        // Original content with whitespace: "   old value   "
        String original = item.textContent();

        // Update content while preserving whitespace pattern
        item.textPreservingWhitespace("new value");

        // Result maintains whitespace: "   new value   "
        String updated = item.textContent();
        // END: whitespace-preserving-text

        Assertions.assertEquals("   old value   ", original);
        Assertions.assertEquals("   new value   ", updated);
    }

    @Test
    public void demonstrateNodeWhitespace() {
        String xml = "    <element>content</element>\n";
        Document doc = Document.of(xml);
        Element element = doc.root();

        // START: node-whitespace
        // Whitespace before the element
        String preceding = element.precedingWhitespace(); // "    "

        // Set whitespace programmatically
        element.precedingWhitespace("  ");
        // END: node-whitespace

        Assertions.assertEquals("    ", preceding);
    }

    @Test
    public void demonstrateElementTagWhitespace() {
        String xml = "<element  >content</  element>";
        Document doc = Document.of(xml);
        Element element = doc.root();

        // START: element-tag-whitespace
        // Whitespace inside opening tag: <element  >
        String openTag = element.openTagWhitespace(); // "  "

        // Whitespace inside closing tag: </  element>
        String closeTag = element.closeTagWhitespace(); // "  "

        // Set tag whitespace
        element.openTagWhitespace(" ");
        element.closeTagWhitespace("");
        // END: element-tag-whitespace

        Assertions.assertEquals("  ", openTag);
        Assertions.assertEquals("  ", closeTag);
    }

    @Test
    public void demonstrateInnerElementWhitespace() {
        String xml = "<parent>\n    \n</parent>";
        Document doc = Document.of(xml);
        Element parent = doc.root();

        // START: inner-element-whitespace
        // Whitespace immediately before closing tag: WHITESPACE</parent>
        String innerPreceding = parent.innerPrecedingWhitespace(); // "\n"

        // Set inner whitespace for elements with only whitespace content
        parent.innerPrecedingWhitespace("\n    \n");
        // END: inner-element-whitespace

        Assertions.assertEquals("\n    \n", innerPreceding);
    }

    @Test
    public void demonstrateChildNavigation() {
        Document doc = Document.of(
                """
            <project>
                <groupId>com.example</groupId>
                <artifactId>my-app</artifactId>
                <version>1.0.0</version>
            </project>
            """);
        Element project = doc.root();

        // START: child-navigation
        // Find first child with name
        Optional<Element> groupId = project.child("groupId");
        String group = groupId.map(Element::textContent).orElse("unknown");

        // Find descendant anywhere in tree
        Optional<Element> version = project.descendant("version");

        // Check if child exists
        boolean hasArtifactId = project.child("artifactId").isPresent();
        // END: child-navigation

        Assertions.assertEquals("com.example", group);
        Assertions.assertTrue(version.isPresent());
        Assertions.assertTrue(hasArtifactId);
    }

    @Test
    public void demonstrateElementStreams() {
        Document doc = Document.of(
                """
            <dependencies>
                <dependency scope="test">junit</dependency>
                <dependency scope="compile">commons-lang</dependency>
                <dependency scope="test">mockito</dependency>
            </dependencies>
            """);
        Element dependencies = doc.root();

        // START: element-streams
        // Stream all child elements
        var allDeps = dependencies.children().collect(Collectors.toList());

        // Stream children with specific name
        var depElements = dependencies.children("dependency").collect(Collectors.toList());

        // Filter and transform
        var testDeps = dependencies
                .children("dependency")
                .filter(dep -> "test".equals(dep.attribute("scope")))
                .map(Element::textContent)
                .collect(Collectors.toList());
        // END: element-streams

        Assertions.assertEquals(3, allDeps.size());
        Assertions.assertEquals(3, depElements.size());
        Assertions.assertEquals(2, testDeps.size());
        Assertions.assertTrue(testDeps.contains("junit"));
        Assertions.assertTrue(testDeps.contains("mockito"));
    }

    @Test
    public void demonstrateAttributeFormatting() {
        // START: attribute-formatting
        Element element = Element.of("dependency");

        // Set attribute with specific quote style
        Attribute singleQuote = Attribute.of("groupId", "junit", QuoteStyle.SINGLE);
        element.attributeObject("groupId", singleQuote);

        // Set attribute with double quotes (default)
        element.attribute("artifactId", "junit");

        // Preserve existing quote style when updating
        String xml = "<element attr='value'/>";
        Document doc = Document.of(xml);
        Element parsed = doc.root();
        // Updating preserves the single quote style
        parsed.attribute("attr", "new-value");
        // END: attribute-formatting

        Assertions.assertEquals("junit", element.attribute("groupId"));
        Assertions.assertEquals("junit", element.attribute("artifactId"));
    }

    @Test
    public void demonstrateNamespaceOperations() {
        // START: namespace-operations
        // Create element with namespace
        QName qname = QName.of("http://maven.apache.org/POM/4.0.0", "project");
        Element project = Element.of(qname);

        // Declare namespace
        project.namespaceDeclaration("", "http://maven.apache.org/POM/4.0.0");
        project.namespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");

        // Access namespace information
        String namespace = project.namespace();
        String localName = project.localName();
        String prefix = project.prefix();
        // END: namespace-operations

        Assertions.assertEquals("http://maven.apache.org/POM/4.0.0", namespace);
        Assertions.assertEquals("project", localName);
    }

    @Test
    public void demonstrateQnameSupport() {
        // START: qname-support
        // Create QName with namespace
        QName projectQName = QName.of("http://maven.apache.org/POM/4.0.0", "project");
        Element project = Element.of(projectQName);

        // Create QName with prefix
        QName xsiQName = QName.of("http://www.w3.org/2001/XMLSchema-instance", "xsi", "schemaLocation");

        // Access QName components
        String namespace = projectQName.namespace();
        String localName = projectQName.localName();
        String prefix = xsiQName.prefix();
        // END: qname-support

        Assertions.assertEquals("http://maven.apache.org/POM/4.0.0", namespace);
        Assertions.assertEquals("project", localName);
        Assertions.assertEquals("xsi", prefix);
    }

    @Test
    public void demonstrateAddingChildren() {
        // START: adding-children
        Element parent = Element.of("dependencies");

        // Add child element
        Element dependency = Element.of("dependency");
        parent.addNode(dependency);

        // Add multiple children
        dependency.addNode(Element.of("groupId").textContent("junit"));
        dependency.addNode(Element.of("artifactId").textContent("junit"));
        dependency.addNode(Element.of("version").textContent("4.13.2"));

        // Add child with text
        Element scope = Element.of("scope").textContent("test");
        dependency.addNode(scope);
        // END: adding-children

        Assertions.assertEquals(1, parent.children().count());
        Assertions.assertEquals(4, dependency.children().count());
    }

    @Test
    public void demonstrateRemovingElements() {
        Document doc = Document.of(
                """
            <project>
                <dependency>junit</dependency>
                <dependency>mockito</dependency>
                <other>keep</other>
            </project>
            """);
        Element project = doc.root();

        // START: removing-elements
        // Find and remove specific element
        Optional<Element> toRemove = project.child("dependency");
        toRemove.ifPresent(element -> project.removeNode(element));

        // Remove all elements with specific name
        project.children("dependency").forEach(project::removeNode);

        // Remove by condition
        project.children()
                .filter(child -> "deprecated".equals(child.attribute("status")))
                .forEach(project::removeNode);
        // END: removing-elements

        Assertions.assertEquals(1, project.children().count());
        Assertions.assertEquals(
                "other", project.children().findFirst().orElseThrow().name());
    }

    @Test
    public void demonstrateElementCloning() {
        // START: element-cloning
        Element original = Element.of("dependency").attribute("scope", "test").attribute("optional", "true");
        original.addNode(Element.of("groupId").textContent("junit"));
        original.addNode(Element.of("artifactId").textContent("junit"));

        // Clone the element (deep copy)
        Element clone = original.clone();

        // Modify clone without affecting original
        clone.attribute("scope", "compile");
        clone.child("groupId").ifPresent(g -> g.textContent("mockito"));
        // END: element-cloning

        Assertions.assertEquals("test", original.attribute("scope"));
        Assertions.assertEquals("compile", clone.attribute("scope"));
        Assertions.assertEquals("junit", original.child("groupId").orElseThrow().textContent());
        Assertions.assertEquals("mockito", clone.child("groupId").orElseThrow().textContent());
    }

    @Test
    public void demonstrateModificationTracking() {
        String xml = "<element attr='value'>content</element>";
        Document doc = Document.of(xml);
        Element element = doc.root();

        // START: modification-tracking
        // Check if element has been modified
        boolean wasModified = element.modified();

        // Modify the element
        element.attribute("attr", "new-value");
        element.textContent("new content");

        // Now it's marked as modified
        boolean isModified = element.modified();

        // Mark as unmodified (used internally by serializer)
        element.modified(false);
        // END: modification-tracking

        Assertions.assertFalse(wasModified);
        Assertions.assertTrue(isModified);
    }

    @Test
    public void demonstrateElementEditorIntegration() {
        String xml = "<project></project>";
        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        // START: element-editor-integration
        Element root = editor.root();

        // Use Editor methods for modifications
        Element dependency = editor.addElement(root, "dependency");
        editor.setAttribute(dependency, "scope", "test");
        editor.addElement(dependency, "groupId", "junit");

        // Use Element methods for navigation
        Optional<Element> groupId = dependency.child("groupId");
        String value = groupId.map(Element::textContent).orElse("unknown");

        // Combine both approaches
        dependency.children().forEach(child -> {
            editor.setAttribute(child, "modified", "true");
        });
        // END: element-editor-integration

        Assertions.assertEquals("junit", value);
        Assertions.assertEquals("test", dependency.attribute("scope"));
    }
}
