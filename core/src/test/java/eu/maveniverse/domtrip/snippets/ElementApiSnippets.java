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

        // Whitespace after the element
        String following = element.followingWhitespace(); // "\n"

        // Set whitespace programmatically
        element.precedingWhitespace("  ");
        element.followingWhitespace("\n\n");
        // END: node-whitespace

        Assertions.assertEquals("    ", preceding);
        Assertions.assertEquals("\n", following);
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
        // Whitespace immediately after opening tag: <parent>WHITESPACE
        String innerFollowing = parent.innerFollowingWhitespace(); // "\n    "

        // Whitespace immediately before closing tag: WHITESPACE</parent>
        String innerPreceding = parent.innerPrecedingWhitespace(); // "\n"

        // Set inner whitespace for elements with only whitespace content
        parent.innerFollowingWhitespace("\n  ");
        parent.innerPrecedingWhitespace("\n");
        // END: inner-element-whitespace

        Assertions.assertEquals("\n    ", innerFollowing);
        Assertions.assertEquals("\n", innerPreceding);
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
}
