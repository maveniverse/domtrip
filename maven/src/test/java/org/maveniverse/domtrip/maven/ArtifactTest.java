package org.maveniverse.domtrip.maven;

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
class ArtifactTest {

    @Test
    void testPredicateGA_matches() {
        Artifact artifact = Artifact.of("org.junit.jupiter", "junit-jupiter", "5.9.2");

        Element element = Element.of("dependency");
        element.addNode(Element.text("groupId", "org.junit.jupiter"));
        element.addNode(Element.text("artifactId", "junit-jupiter"));
        element.addNode(Element.text("version", "5.9.2"));

        Predicate<Element> predicate = artifact.predicateGA();
        assertTrue(predicate.test(element));
    }

    @Test
    void testPredicateGA_doesNotMatch() {
        Artifact artifact = Artifact.of("org.junit.jupiter", "junit-jupiter", "5.9.2");

        Element element = Element.of("dependency");
        element.addNode(Element.text("groupId", "junit"));
        element.addNode(Element.text("artifactId", "junit"));

        Predicate<Element> predicate = artifact.predicateGA();
        assertFalse(predicate.test(element));
    }

    @Test
    void testPredicateGA_ignoresVersion() {
        Artifact artifact = Artifact.of("org.junit.jupiter", "junit-jupiter", "5.9.2");

        Element element = Element.of("dependency");
        element.addNode(Element.text("groupId", "org.junit.jupiter"));
        element.addNode(Element.text("artifactId", "junit-jupiter"));
        element.addNode(Element.text("version", "5.10.0")); // Different version

        Predicate<Element> predicate = artifact.predicateGA();
        assertTrue(predicate.test(element)); // Should still match on GA
    }

    @Test
    void testPredicatePluginGA_withDefaultGroupId() {
        Artifact artifact = Artifact.of("org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");

        // Plugin element without groupId (defaults to org.apache.maven.plugins)
        Element element = Element.of("plugin");
        element.addNode(Element.text("artifactId", "maven-compiler-plugin"));

        Predicate<Element> predicate = artifact.predicatePluginGA();
        assertTrue(predicate.test(element));
    }

    @Test
    void testPredicatePluginGA_withExplicitGroupId() {
        Artifact artifact = Artifact.of("org.codehaus.mojo", "build-helper-maven-plugin", "3.3.0");

        Element element = Element.of("plugin");
        element.addNode(Element.text("groupId", "org.codehaus.mojo"));
        element.addNode(Element.text("artifactId", "build-helper-maven-plugin"));

        Predicate<Element> predicate = artifact.predicatePluginGA();
        assertTrue(predicate.test(element));
    }

    @Test
    void testPredicateGATC_matchesWithType() {
        Artifact artifact = Artifact.of("org.example", "my-lib", "1.0.0", null, "jar");

        Element element = Element.of("dependency");
        element.addNode(Element.text("groupId", "org.example"));
        element.addNode(Element.text("artifactId", "my-lib"));
        element.addNode(Element.text("type", "jar"));

        Predicate<Element> predicate = artifact.predicateGATC();
        assertTrue(predicate.test(element));
    }

    @Test
    void testPredicateGATC_matchesWithClassifier() {
        Artifact artifact = Artifact.of("org.example", "my-lib", "1.0.0", "sources", "jar");

        Element element = Element.of("dependency");
        element.addNode(Element.text("groupId", "org.example"));
        element.addNode(Element.text("artifactId", "my-lib"));
        element.addNode(Element.text("type", "jar"));
        element.addNode(Element.text("classifier", "sources"));

        Predicate<Element> predicate = artifact.predicateGATC();
        assertTrue(predicate.test(element));
    }

    @Test
    void testPredicateGATC_doesNotMatchDifferentType() {
        Artifact artifact = Artifact.of("org.example", "my-lib", "1.0.0", null, "war");

        Element element = Element.of("dependency");
        element.addNode(Element.text("groupId", "org.example"));
        element.addNode(Element.text("artifactId", "my-lib"));
        element.addNode(Element.text("type", "jar"));

        Predicate<Element> predicate = artifact.predicateGATC();
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

        Artifact artifact = Artifact.fromPom(pomFile);

        assertEquals("org.example", artifact.groupId());
        assertEquals("my-app", artifact.artifactId());
        assertEquals("1.0.0", artifact.version());
        assertNull(artifact.classifier());
        assertEquals("pom", artifact.type());
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

        Artifact artifact = Artifact.fromPom(pomFile);

        assertEquals("org.example", artifact.groupId()); // Inherited from parent
        assertEquals("my-app", artifact.artifactId());
        assertEquals("1.0.0", artifact.version()); // Inherited from parent
        assertEquals("pom", artifact.type());
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

        Artifact artifact = Artifact.fromPom(pomFile);

        assertNull(artifact.groupId()); // Not present, Maven 4 will infer
        assertEquals("my-module", artifact.artifactId());
        assertNull(artifact.version()); // Not present, Maven 4 will infer
        assertEquals("pom", artifact.type());
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
        assertThrows(IllegalArgumentException.class, () -> Artifact.fromPom(pomFile));
    }

    @Test
    void testPredicateGA_withNullGroupId() {
        // Maven 4 inference - artifact with null groupId
        // Note: Artifact.toGA() returns "null:my-module" (string concat with null)
        // while AbstractMavenEditor.toGA() returns null when groupId is missing
        // So they won't match - this is expected behavior
        Artifact artifact = Artifact.of(null, "my-module", null, null, "jar");

        Element element = Element.of("dependency");
        element.addNode(Element.text("artifactId", "my-module"));

        Predicate<Element> predicate = artifact.predicateGA();
        // artifact.toGA() = "null:my-module", AbstractMavenEditor.toGA(element) = null
        // They don't match
        assertFalse(predicate.test(element));
    }

    @Test
    void testPredicateFiltering() {
        Artifact junit = Artifact.of("org.junit.jupiter", "junit-jupiter", "5.9.2");

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
