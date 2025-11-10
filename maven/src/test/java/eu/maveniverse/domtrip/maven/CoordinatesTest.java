package eu.maveniverse.domtrip.maven;

import static org.junit.jupiter.api.Assertions.*;

import eu.maveniverse.domtrip.Element;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for Artifact class methods.
 */
class CoordinatesTest {

    @Test
    void testPredicateGA_matches() {
        Coordinates coordinates = Coordinates.of("org.junit.jupiter", "junit-jupiter", "5.9.2");

        Element element = Element.of("dependency");
        element.addNode(Element.text("groupId", "org.junit.jupiter"));
        element.addNode(Element.text("artifactId", "junit-jupiter"));
        element.addNode(Element.text("version", "5.9.2"));

        Predicate<Element> predicate = coordinates.predicateGA();
        assertTrue(predicate.test(element));
    }

    @Test
    void testPredicateGA_doesNotMatch() {
        Coordinates coordinates = Coordinates.of("org.junit.jupiter", "junit-jupiter", "5.9.2");

        Element element = Element.of("dependency");
        element.addNode(Element.text("groupId", "junit"));
        element.addNode(Element.text("artifactId", "junit"));

        Predicate<Element> predicate = coordinates.predicateGA();
        assertFalse(predicate.test(element));
    }

    @Test
    void testPredicateGA_ignoresVersion() {
        Coordinates coordinates = Coordinates.of("org.junit.jupiter", "junit-jupiter", "5.9.2");

        Element element = Element.of("dependency");
        element.addNode(Element.text("groupId", "org.junit.jupiter"));
        element.addNode(Element.text("artifactId", "junit-jupiter"));
        element.addNode(Element.text("version", "5.10.0")); // Different version

        Predicate<Element> predicate = coordinates.predicateGA();
        assertTrue(predicate.test(element)); // Should still match on GA
    }

    @Test
    void testPredicatePluginGA_withDefaultGroupId() {
        Coordinates coordinates = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");

        // Plugin element without groupId (defaults to org.apache.maven.plugins)
        Element element = Element.of("plugin");
        element.addNode(Element.text("artifactId", "maven-compiler-plugin"));

        Predicate<Element> predicate = coordinates.predicatePluginGA();
        assertTrue(predicate.test(element));
    }

    @Test
    void testPredicatePluginGA_withExplicitGroupId() {
        Coordinates coordinates = Coordinates.of("org.codehaus.mojo", "build-helper-maven-plugin", "3.3.0");

        Element element = Element.of("plugin");
        element.addNode(Element.text("groupId", "org.codehaus.mojo"));
        element.addNode(Element.text("artifactId", "build-helper-maven-plugin"));

        Predicate<Element> predicate = coordinates.predicatePluginGA();
        assertTrue(predicate.test(element));
    }

    @Test
    void testPredicateGATC_matchesWithType() {
        Coordinates coordinates = Coordinates.of("org.example", "my-lib", "1.0.0", null, "jar");

        Element element = Element.of("dependency");
        element.addNode(Element.text("groupId", "org.example"));
        element.addNode(Element.text("artifactId", "my-lib"));
        element.addNode(Element.text("type", "jar"));

        Predicate<Element> predicate = coordinates.predicateGATC();
        assertTrue(predicate.test(element));
    }

    @Test
    void testPredicateGATC_matchesWithClassifier() {
        Coordinates coordinates = Coordinates.of("org.example", "my-lib", "1.0.0", "sources", "jar");

        Element element = Element.of("dependency");
        element.addNode(Element.text("groupId", "org.example"));
        element.addNode(Element.text("artifactId", "my-lib"));
        element.addNode(Element.text("type", "jar"));
        element.addNode(Element.text("classifier", "sources"));

        Predicate<Element> predicate = coordinates.predicateGATC();
        assertTrue(predicate.test(element));
    }

    @Test
    void testPredicateGATC_doesNotMatchDifferentType() {
        Coordinates coordinates = Coordinates.of("org.example", "my-lib", "1.0.0", null, "war");

        Element element = Element.of("dependency");
        element.addNode(Element.text("groupId", "org.example"));
        element.addNode(Element.text("artifactId", "my-lib"));
        element.addNode(Element.text("type", "jar"));

        Predicate<Element> predicate = coordinates.predicateGATC();
        assertFalse(predicate.test(element));
    }

    @Test
    void testFromPom_simple(@TempDir Path tempDir) throws IOException {
        String pomContent =
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                    <groupId>org.example</groupId>
                    <artifactId>my-app</artifactId>
                    <version>1.0.0</version>
                </project>
                """;

        Path pomFile = tempDir.resolve("pom.xml");
        Files.writeString(pomFile, pomContent);

        Coordinates coordinates = Coordinates.fromPom(pomFile);

        assertEquals("org.example", coordinates.groupId());
        assertEquals("my-app", coordinates.artifactId());
        assertEquals("1.0.0", coordinates.version());
        assertNull(coordinates.classifier());
        assertEquals("pom", coordinates.type());
    }

    @Test
    void testFromPom_withParent(@TempDir Path tempDir) throws IOException {
        String pomContent =
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                    <parent>
                        <groupId>org.example</groupId>
                        <artifactId>parent</artifactId>
                        <version>1.0.0</version>
                    </parent>
                    <artifactId>my-app</artifactId>
                </project>
                """;

        Path pomFile = tempDir.resolve("pom.xml");
        Files.writeString(pomFile, pomContent);

        Coordinates coordinates = Coordinates.fromPom(pomFile);

        assertEquals("org.example", coordinates.groupId()); // Inherited from parent
        assertEquals("my-app", coordinates.artifactId());
        assertEquals("1.0.0", coordinates.version()); // Inherited from parent
        assertEquals("pom", coordinates.type());
    }

    @Test
    void testFromPom_maven4Inference(@TempDir Path tempDir) throws IOException {
        // Maven 4 can infer groupId and version from reactor
        String pomContent =
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                    <artifactId>my-module</artifactId>
                </project>
                """;

        Path pomFile = tempDir.resolve("pom.xml");
        Files.writeString(pomFile, pomContent);

        Coordinates coordinates = Coordinates.fromPom(pomFile);

        assertNull(coordinates.groupId()); // Not present, Maven 4 will infer
        assertEquals("my-module", coordinates.artifactId());
        assertNull(coordinates.version()); // Not present, Maven 4 will infer
        assertEquals("pom", coordinates.type());
    }

    @Test
    void testFromPom_missingArtifactId(@TempDir Path tempDir) throws IOException {
        String pomContent =
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                    <groupId>org.example</groupId>
                    <version>1.0.0</version>
                </project>
                """;

        Path pomFile = tempDir.resolve("pom.xml");
        Files.writeString(pomFile, pomContent);

        // ArtifactId is always required
        assertThrows(IllegalArgumentException.class, () -> Coordinates.fromPom(pomFile));
    }

    @Test
    void testPredicateGA_withNullGroupId() {
        // Maven 4 inference - artifact with null groupId
        // Note: Artifact.toGA() returns "null:my-module" (string concat with null)
        // while AbstractMavenEditor.toGA() returns null when groupId is missing
        // So they won't match - this is expected behavior
        Coordinates coordinates = Coordinates.of(null, "my-module", null, null, "jar");

        Element element = Element.of("dependency");
        element.addNode(Element.text("artifactId", "my-module"));

        Predicate<Element> predicate = coordinates.predicateGA();
        // artifact.toGA() = "null:my-module", AbstractMavenEditor.toGA(element) = null
        // They don't match
        assertFalse(predicate.test(element));
    }

    @Test
    void testPredicateFiltering() {
        Coordinates junit = Coordinates.of("org.junit.jupiter", "junit-jupiter", "5.9.2");

        Element deps = Element.of("dependencies");
        deps.addNode(createDependency("org.junit.jupiter", "junit-jupiter", "5.9.2"));
        deps.addNode(createDependency("junit", "junit", "4.13.2"));
        deps.addNode(createDependency("org.mockito", "mockito-core", "5.0.0"));

        long count = deps.children("dependency").filter(junit.predicateGA()).count();
        assertEquals(1, count);
    }

    private Element createDependency(String groupId, String artifactId, String version) {
        Element dep = Element.of("dependency");
        dep.addNode(Element.text("groupId", groupId));
        dep.addNode(Element.text("artifactId", artifactId));
        dep.addNode(Element.text("version", version));
        return dep;
    }
}
