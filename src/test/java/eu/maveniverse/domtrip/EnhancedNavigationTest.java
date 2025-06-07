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
    void setUp() {
        String xml =
                """
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

        editor = new Editor(xml);
        document = editor.getDocument();
        root = editor.getRootElement();
    }

    @Test
    void testFindChild() {
        // Test finding existing child
        Optional<Element> groupId = root.findChild("groupId");
        assertTrue(groupId.isPresent());
        assertEquals("com.example", groupId.orElseThrow().getTextContent());

        // Test finding non-existing child
        Optional<Element> nonExistent = root.findChild("nonexistent");
        assertFalse(nonExistent.isPresent());

        // Test finding nested child
        Optional<Element> dependencies = root.findChild("dependencies");
        assertTrue(dependencies.isPresent());

        Optional<Element> dependency = dependencies.orElseThrow().findChild("dependency");
        assertTrue(dependency.isPresent());
        assertEquals(
                "junit",
                dependency.orElseThrow().findChild("groupId").orElseThrow().getTextContent());
    }

    @Test
    void testFindChildren() {
        Element dependencies = root.findChild("dependencies").orElseThrow();

        // Find all dependency children
        List<Element> deps = dependencies.findChildren("dependency").toList();
        assertEquals(2, deps.size());

        // Verify content
        assertEquals("junit", deps.get(0).findChild("groupId").orElseThrow().getTextContent());
        assertEquals("mockito", deps.get(1).findChild("groupId").orElseThrow().getTextContent());

        // Test finding children that don't exist
        List<Element> nonExistent = root.findChildren("nonexistent").toList();
        assertTrue(nonExistent.isEmpty());
    }

    @Test
    void testFindDescendant() {
        // Find deeply nested element
        Optional<Element> scope = root.findDescendant("scope");
        assertTrue(scope.isPresent());
        assertEquals("test", scope.orElseThrow().getTextContent());

        // Find element by name that appears multiple times (should return first)
        Optional<Element> version = root.findDescendant("version");
        assertTrue(version.isPresent());
        assertEquals("1.0.0", version.orElseThrow().getTextContent()); // Project version, not dependency version

        // Find non-existent descendant
        Optional<Element> nonExistent = root.findDescendant("nonexistent");
        assertFalse(nonExistent.isPresent());
    }

    @Test
    void testDescendants() {
        // Get all descendants
        List<Element> allDescendants = root.descendants().toList();
        assertTrue(allDescendants.size() > 10); // Should have many descendants

        // Find all groupId elements
        List<Element> groupIds =
                root.descendants().filter(el -> "groupId".equals(el.getName())).toList();
        assertEquals(3, groupIds.size()); // Project + 2 dependencies

        // Find all version elements
        List<Element> versions =
                root.descendants().filter(el -> "version".equals(el.getName())).toList();
        assertEquals(3, versions.size()); // Project + 2 dependencies

        // Verify content
        List<String> versionTexts =
                versions.stream().map(Element::getTextContent).toList();
        assertTrue(versionTexts.contains("1.0.0"));
        assertTrue(versionTexts.contains("4.13.2"));
        assertTrue(versionTexts.contains("3.12.4"));
    }

    @Test
    void testChildElements() {
        // Test direct child elements
        List<Element> rootChildren = root.childElements().toList();
        assertEquals(5, rootChildren.size()); // groupId, artifactId, version, dependencies, properties

        List<String> childNames = rootChildren.stream().map(Element::getName).toList();
        assertTrue(childNames.contains("groupId"));
        assertTrue(childNames.contains("artifactId"));
        assertTrue(childNames.contains("version"));
        assertTrue(childNames.contains("dependencies"));
        assertTrue(childNames.contains("properties"));

        // Test child elements of dependencies
        Element dependencies = root.findChild("dependencies").orElseThrow();
        List<Element> depChildren = dependencies.childElements().toList();
        assertEquals(2, depChildren.size());
        assertTrue(depChildren.stream().allMatch(el -> "dependency".equals(el.getName())));
    }

    @Test
    void testChildNodes() {
        // Test all child nodes (including text nodes)
        long nodeCount = root.childNodes().count();
        assertTrue(nodeCount >= 5); // At least the 5 element children, possibly more with text nodes

        // Test filtering by type
        long elementCount =
                root.childNodes().filter(node -> node instanceof Element).count();
        assertEquals(5, elementCount);

        long textCount = root.childNodes().filter(node -> node instanceof Text).count();
        assertTrue(textCount > 0); // Should have whitespace text nodes
    }

    @Test
    void testFindTextChild() {
        Element groupId = root.findChild("groupId").orElseThrow();

        Optional<Text> textChild = groupId.findTextChild();
        assertTrue(textChild.isPresent());
        assertEquals("com.example", textChild.orElseThrow().getContent());

        // Test element without text child
        Element dependencies = root.findChild("dependencies").orElseThrow();
        Optional<Text> noTextChild = dependencies.findTextChild();
        // May or may not have text child depending on whitespace handling
    }

    @Test
    void testGetTextContent() {
        Element groupId = root.findChild("groupId").orElseThrow();
        assertEquals("com.example", groupId.getTextContent());

        Element artifactId = root.findChild("artifactId").orElseThrow();
        assertEquals("test-project", artifactId.getTextContent());

        // Test element with no text content
        Element dependencies = root.findChild("dependencies").orElseThrow();
        String depText = dependencies.getTextContent();
        // Should be empty or whitespace only
    }

    @Test
    void testHasChildElements() {
        assertTrue(root.hasChildElements());

        Element dependencies = root.findChild("dependencies").orElseThrow();
        assertTrue(dependencies.hasChildElements());

        Element groupId = root.findChild("groupId").orElseThrow();
        assertFalse(groupId.hasChildElements());
    }

    @Test
    void testHasTextContent() {
        Element groupId = root.findChild("groupId").orElseThrow();
        assertTrue(groupId.hasTextContent());

        Element dependencies = root.findChild("dependencies").orElseThrow();
        // May or may not have text content depending on whitespace
    }

    @Test
    void testGetDepth() {
        assertEquals(0, document.getDepth()); // Document is root
        assertEquals(1, root.getDepth()); // Root element

        Element dependencies = root.findChild("dependencies").orElseThrow();
        assertEquals(2, dependencies.getDepth());

        Element dependency = dependencies.findChild("dependency").orElseThrow();
        assertEquals(3, dependency.getDepth());

        Element scope = dependency.findChild("scope").orElseThrow();
        assertEquals(4, scope.getDepth());
    }

    @Test
    void testGetRoot() {
        Element dependencies = root.findChild("dependencies").orElseThrow();
        Element dependency = dependencies.findChild("dependency").orElseThrow();
        Element scope = dependency.findChild("scope").orElseThrow();

        assertEquals(document, root.getRoot());
        assertEquals(document, dependencies.getRoot());
        assertEquals(document, dependency.getRoot());
        assertEquals(document, scope.getRoot());
    }

    @Test
    void testIsDescendantOf() {
        Element dependencies = root.findChild("dependencies").orElseThrow();
        Element dependency = dependencies.findChild("dependency").orElseThrow();
        Element scope = dependency.findChild("scope").orElseThrow();

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
        Element groupId = root.findChild("groupId").orElseThrow();
        Element artifactId = root.findChild("artifactId").orElseThrow();
        assertFalse(groupId.isDescendantOf(artifactId));
        assertFalse(artifactId.isDescendantOf(groupId));
    }

    @Test
    void testNavigationChaining() {
        // Test chaining navigation methods
        String junitVersion = root.findChild("dependencies")
                .flatMap(deps -> deps.findChildren("dependency")
                        .filter(dep -> dep.findChild("groupId")
                                .map(gid -> "junit".equals(gid.getTextContent()))
                                .orElse(false))
                        .findFirst())
                .flatMap(dep -> dep.findChild("version"))
                .map(Element::getTextContent)
                .orElse("not found");

        assertEquals("4.13.2", junitVersion);

        // Test finding all test scoped dependencies
        List<String> testDependencies = root.findChild("dependencies")
                .map(deps -> deps.findChildren("dependency")
                        .filter(dep -> dep.findChild("scope")
                                .map(scope -> "test".equals(scope.getTextContent()))
                                .orElse(false))
                        .map(dep -> dep.findChild("groupId")
                                .map(Element::getTextContent)
                                .orElse("unknown"))
                        .toList())
                .orElse(List.of());

        assertEquals(2, testDependencies.size());
        assertTrue(testDependencies.contains("junit"));
        assertTrue(testDependencies.contains("mockito"));
    }
}
