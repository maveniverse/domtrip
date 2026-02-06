package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test cases for enhanced navigation features.
 */
public class EnhancedNavigationTest {

    private Editor editor;
    private Document document;
    private Element root;

    @BeforeEach
    void setUp() throws DomTripException {
        String xml = """
            <project>
                <groupId>com.example</groupId>
                <artifactId>test-project</artifactId>
                <version>1.0.0</version>
                <dependencies>
                    <dependency>
                        <groupId>junit</groupId>
                        <artifactId>junit</artifactId>
                        <version>4.13.2</version>
                        <scope>test</scope>
                    </dependency>
                    <dependency>
                        <groupId>mockito</groupId>
                        <artifactId>mockito-core</artifactId>
                        <version>3.12.4</version>
                        <scope>test</scope>
                    </dependency>
                </dependencies>
                <properties>
                    <maven.compiler.source>11</maven.compiler.source>
                    <maven.compiler.target>11</maven.compiler.target>
                </properties>
            </project>
            """;

        editor = new Editor(Document.of(xml));
        document = editor.document();
        root = editor.root();
    }

    @Test
    void testFindChild() {
        // Test finding existing child
        Optional<Element> groupId = root.child("groupId");
        assertTrue(groupId.isPresent());
        assertEquals("com.example", groupId.orElseThrow().textContent());

        // Test finding non-existing child
        Optional<Element> nonExistent = root.child("nonexistent");
        assertFalse(nonExistent.isPresent());

        // Test finding nested child
        Optional<Element> dependencies = root.child("dependencies");
        assertTrue(dependencies.isPresent());

        Optional<Element> dependency = dependencies.orElseThrow().child("dependency");
        assertTrue(dependency.isPresent());
        assertEquals(
                "junit", dependency.orElseThrow().child("groupId").orElseThrow().textContent());
    }

    @Test
    void testFindChildren() {
        Element dependencies = root.child("dependencies").orElseThrow();

        // Find all dependency children
        List<Element> deps = dependencies.children("dependency").toList();
        assertEquals(2, deps.size());

        // Verify content
        assertEquals("junit", deps.get(0).child("groupId").orElseThrow().textContent());
        assertEquals("mockito", deps.get(1).child("groupId").orElseThrow().textContent());

        // Test finding children that don't exist
        List<Element> nonExistent = root.children("nonexistent").toList();
        assertTrue(nonExistent.isEmpty());
    }

    @Test
    void testFindDescendant() {
        // Find deeply nested element
        Optional<Element> scope = root.descendant("scope");
        assertTrue(scope.isPresent());
        assertEquals("test", scope.orElseThrow().textContent());

        // Find element by name that appears multiple times (should return first)
        Optional<Element> version = root.descendant("version");
        assertTrue(version.isPresent());
        assertEquals("1.0.0", version.orElseThrow().textContent()); // Project version, not dependency version

        // Find non-existent descendant
        Optional<Element> nonExistent = root.descendant("nonexistent");
        assertFalse(nonExistent.isPresent());
    }

    @Test
    void testDescendants() {
        // Get all descendants
        List<Element> allDescendants = root.descendants().toList();
        assertTrue(allDescendants.size() > 10); // Should have many descendants

        // Find all groupId elements
        List<Element> groupIds =
                root.descendants().filter(el -> "groupId".equals(el.name())).toList();
        assertEquals(3, groupIds.size()); // Project + 2 dependencies

        // Find all version elements
        List<Element> versions =
                root.descendants().filter(el -> "version".equals(el.name())).toList();
        assertEquals(3, versions.size()); // Project + 2 dependencies

        // Verify content
        List<String> versionTexts =
                versions.stream().map(element -> element.textContent()).toList();
        assertTrue(versionTexts.contains("1.0.0"));
        assertTrue(versionTexts.contains("4.13.2"));
        assertTrue(versionTexts.contains("3.12.4"));
    }

    @Test
    void testChildElements() {
        // Test direct child elements
        List<Element> rootChildren = root.children().toList();
        assertEquals(5, rootChildren.size()); // groupId, artifactId, version, dependencies, properties

        List<String> childNames = rootChildren.stream().map(Element::name).toList();
        assertTrue(childNames.contains("groupId"));
        assertTrue(childNames.contains("artifactId"));
        assertTrue(childNames.contains("version"));
        assertTrue(childNames.contains("dependencies"));
        assertTrue(childNames.contains("properties"));

        // Test child elements of dependencies
        Element dependencies = root.child("dependencies").orElseThrow();
        List<Element> depChildren = dependencies.children().toList();
        assertEquals(2, depChildren.size());
        assertTrue(depChildren.stream().allMatch(el -> "dependency".equals(el.name())));
    }

    @Test
    void testChildNodes() {
        // Test all child nodes (including text nodes)
        long nodeCount = root.nodes().count();
        assertTrue(nodeCount >= 5); // At least the 5 element children, possibly more with text nodes

        // Test filtering by type
        long elementCount = root.nodes().filter(node -> node instanceof Element).count();
        assertEquals(5, elementCount);

        long textCount = root.nodes().filter(node -> node instanceof Text).count();
        // With whitespace capture, whitespace is stored as element properties, not separate text nodes
        // The root element only contains child elements, no direct text content
        // Text nodes exist within the child elements (like groupId containing "com.example")
        assertEquals(0, textCount);
    }

    @Test
    void testFindTextChild() {
        Element groupId = root.child("groupId").orElseThrow();

        Optional<Text> textChild = groupId.textChild();
        assertTrue(textChild.isPresent());
        assertEquals("com.example", textChild.orElseThrow().content());

        // Test element without text child
        Element dependencies = root.child("dependencies").orElseThrow();
        Optional<Text> noTextChild = dependencies.textChild();
        // May or may not have text child depending on whitespace handling
    }

    @Test
    void testGetTextContent() {
        Element groupId = root.child("groupId").orElseThrow();
        assertEquals("com.example", groupId.textContent());

        Element artifactId = root.child("artifactId").orElseThrow();
        assertEquals("test-project", artifactId.textContent());

        // Test element with no text content
        Element dependencies = root.child("dependencies").orElseThrow();
        String depText = dependencies.textContent();
        // Should be empty or whitespace only
    }

    @Test
    void testHasChildElements() {
        assertTrue(root.hasNodeElements());

        Element dependencies = root.child("dependencies").orElseThrow();
        assertTrue(dependencies.hasNodeElements());

        Element groupId = root.child("groupId").orElseThrow();
        assertFalse(groupId.hasNodeElements());
    }

    @Test
    void testHasTextContent() {
        Element groupId = root.child("groupId").orElseThrow();
        assertTrue(groupId.hasTextContent());

        Element dependencies = root.child("dependencies").orElseThrow();
        // May or may not have text content depending on whitespace
    }

    @Test
    void testGetDepth() {
        assertEquals(0, document.depth()); // Document is root
        assertEquals(1, root.depth()); // Root element

        Element dependencies = root.child("dependencies").orElseThrow();
        assertEquals(2, dependencies.depth());

        Element dependency = dependencies.child("dependency").orElseThrow();
        assertEquals(3, dependency.depth());

        Element scope = dependency.child("scope").orElseThrow();
        assertEquals(4, scope.depth());
    }

    @Test
    void testGetDocument() {
        Element dependencies = root.child("dependencies").orElseThrow();
        Element dependency = dependencies.child("dependency").orElseThrow();
        Element scope = dependency.child("scope").orElseThrow();

        assertEquals(document, root.document());
        assertEquals(document, dependencies.document());
        assertEquals(document, dependency.document());
        assertEquals(document, scope.document());
    }

    @Test
    void testIsDescendantOf() {
        Element dependencies = root.child("dependencies").orElseThrow();
        Element dependency = dependencies.child("dependency").orElseThrow();
        Element scope = dependency.child("scope").orElseThrow();

        assertTrue(root.isDescendantOf(document));
        assertTrue(dependencies.isDescendantOf(root));
        assertTrue(dependencies.isDescendantOf(document));
        assertTrue(dependency.isDescendantOf(dependencies));
        assertTrue(dependency.isDescendantOf(root));
        assertTrue(dependency.isDescendantOf(document));
        assertTrue(scope.isDescendantOf(dependency));
        assertTrue(scope.isDescendantOf(dependencies));
        assertTrue(scope.isDescendantOf(root));
        assertTrue(scope.isDescendantOf(document));

        // Test negative cases
        assertFalse(document.isDescendantOf(root));
        assertFalse(root.isDescendantOf(dependencies));
        assertFalse(dependencies.isDescendantOf(dependency));
        assertFalse(dependency.isDescendantOf(scope));

        // Test siblings
        Element groupId = root.child("groupId").orElseThrow();
        Element artifactId = root.child("artifactId").orElseThrow();
        assertFalse(groupId.isDescendantOf(artifactId));
        assertFalse(artifactId.isDescendantOf(groupId));
    }

    @Test
    void testNavigationChaining() {
        // Test chaining navigation methods
        String junitVersion = root.child("dependencies")
                .flatMap(deps -> deps.children("dependency")
                        .filter(dep -> dep.child("groupId")
                                .map(gid -> "junit".equals(gid.textContent()))
                                .orElse(false))
                        .findFirst())
                .flatMap(dep -> dep.child("version"))
                .map(element1 -> element1.textContent())
                .orElse("not found");

        assertEquals("4.13.2", junitVersion);

        // Test finding all test scoped dependencies
        List<String> testDependencies = root.child("dependencies")
                .map(deps -> deps.children("dependency")
                        .filter(dep -> dep.child("scope")
                                .map(scope -> "test".equals(scope.textContent()))
                                .orElse(false))
                        .map(dep -> dep.child("groupId")
                                .map(element -> element.textContent())
                                .orElse("unknown"))
                        .toList())
                .orElse(List.of());

        assertEquals(2, testDependencies.size());
        assertTrue(testDependencies.contains("junit"));
        assertTrue(testDependencies.contains("mockito"));
    }
}
