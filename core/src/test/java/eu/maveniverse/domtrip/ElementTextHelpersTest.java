package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for Element text content helper methods.
 */
class ElementTextHelpersTest {

    @Test
    void testTextContentOr_withContent() throws DomTripException {
        Element element = Element.text("version", "1.0.0");
        assertEquals("1.0.0", element.textContentOr("default"));
    }

    @Test
    void testTextContentOr_withEmptyContent() throws DomTripException {
        Element element = Element.of("empty");
        assertEquals("default", element.textContentOr("default"));
    }

    @Test
    void testTextContentOr_withNullDefault() throws DomTripException {
        Element element = Element.of("empty");
        assertNull(element.textContentOr(null));
    }

    @Test
    void testTextContentOr_withWhitespaceOnly() throws DomTripException {
        Element element = Element.of("whitespace");
        element.addNode(Text.of("   "));
        assertEquals("   ", element.textContentOr("default"));
    }

    @Test
    void testChildTextOr_withExistingChild() throws DomTripException {
        Element parent = Element.of("dependency");
        parent.addNode(Element.text("groupId", "org.junit.jupiter"));
        parent.addNode(Element.text("artifactId", "junit-jupiter"));

        assertEquals("org.junit.jupiter", parent.childTextOr("groupId", "default"));
        assertEquals("junit-jupiter", parent.childTextOr("artifactId", "default"));
    }

    @Test
    void testChildTextOr_withMissingChild() throws DomTripException {
        Element parent = Element.of("dependency");
        parent.addNode(Element.text("artifactId", "junit-jupiter"));

        assertEquals("default", parent.childTextOr("groupId", "default"));
    }

    @Test
    void testChildTextOr_withNullDefault() throws DomTripException {
        Element parent = Element.of("dependency");
        assertNull(parent.childTextOr("missing", null));
    }

    @Test
    void testChildTextOr_withEmptyChildContent() throws DomTripException {
        Element parent = Element.of("dependency");
        parent.addNode(Element.of("scope")); // Empty element

        // Should return default value when child exists but has no text
        assertEquals("compile", parent.childTextOr("scope", "compile"));
    }

    @Test
    void testChildTextRequired_withExistingChild() throws DomTripException {
        Element parent = Element.of("dependency");
        parent.addNode(Element.text("groupId", "org.junit.jupiter"));

        assertEquals("org.junit.jupiter", parent.childTextRequired("groupId"));
    }

    @Test
    void testChildTextRequired_withMissingChild() throws DomTripException {
        Element parent = Element.of("dependency");

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> parent.childTextRequired("groupId"));

        assertTrue(exception.getMessage().contains("groupId"));
        assertTrue(exception.getMessage().contains("dependency"));
    }

    @Test
    void testChildTextRequired_withEmptyChild() throws DomTripException {
        Element parent = Element.of("dependency");
        parent.addNode(Element.of("groupId")); // Empty element

        // Should return empty string, not throw
        assertEquals("", parent.childTextRequired("groupId"));
    }

    @Test
    void testChildTextOr_withMultipleChildren() throws DomTripException {
        Element parent = Element.of("project");
        parent.addNode(Element.text("groupId", "org.example"));
        parent.addNode(Element.text("artifactId", "my-app"));
        parent.addNode(Element.text("version", "1.0.0"));

        assertEquals("org.example", parent.childTextOr("groupId", null));
        assertEquals("my-app", parent.childTextOr("artifactId", null));
        assertEquals("1.0.0", parent.childTextOr("version", null));
        assertEquals("jar", parent.childTextOr("packaging", "jar"));
    }

    @Test
    void testChainedUsage() throws DomTripException {
        Element project = Element.of("project");
        Element dependencies = Element.of("dependencies");
        Element dependency = Element.of("dependency");
        dependency.addNode(Element.text("groupId", "junit"));
        dependency.addNode(Element.text("artifactId", "junit"));
        dependencies.addNode(dependency);
        project.addNode(dependencies);

        // Chained usage
        String groupId = project.child("dependencies")
                .flatMap(deps -> deps.child("dependency"))
                .map(dep -> dep.childTextOr("groupId", "unknown"))
                .orElse("not-found");

        assertEquals("junit", groupId);
    }

    @Test
    void testTextContentOr_preservesWhitespace() throws DomTripException {
        Element element = Element.of("description");
        element.addNode(Text.of("  Some text with spaces  "));

        // textContentOr should preserve whitespace
        assertEquals("  Some text with spaces  ", element.textContentOr("default"));
    }

    @Test
    void testChildTextOr_withMultipleTextNodes() throws DomTripException {
        Element parent = Element.of("parent");
        Element child = Element.of("child");
        child.addNode(Text.of("Hello "));
        child.addNode(Text.of("World"));
        parent.addNode(child);

        assertEquals("Hello World", parent.childTextOr("child", "default"));
    }
}
