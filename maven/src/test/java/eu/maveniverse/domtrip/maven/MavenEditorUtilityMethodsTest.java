package eu.maveniverse.domtrip.maven;

import static org.junit.jupiter.api.Assertions.*;

import eu.maveniverse.domtrip.Element;
import org.junit.jupiter.api.Test;

/**
 * Tests for AbstractMavenEditor utility methods.
 */
class MavenEditorUtilityMethodsTest {

    @Test
    void testToGA_complete() {
        Element element = createDependency("org.junit.jupiter", "junit-jupiter", "5.9.2");
        assertEquals("org.junit.jupiter:junit-jupiter", AbstractMavenEditor.toGA(element));
    }

    @Test
    void testToGA_missingGroupId() {
        Element element = Element.of("dependency");
        element.addNode(Element.text("artifactId", "my-module"));

        assertNull(AbstractMavenEditor.toGA(element));
    }

    @Test
    void testToGA_missingArtifactId() {
        Element element = Element.of("dependency");
        element.addNode(Element.text("groupId", "org.example"));

        assertNull(AbstractMavenEditor.toGA(element));
    }

    @Test
    void testToPluginGA_withGroupId() {
        Element element = Element.of("plugin");
        element.addNode(Element.text("groupId", "org.codehaus.mojo"));
        element.addNode(Element.text("artifactId", "build-helper-maven-plugin"));

        assertEquals("org.codehaus.mojo:build-helper-maven-plugin", AbstractMavenEditor.toPluginGA(element));
    }

    @Test
    void testToPluginGA_withoutGroupId() {
        Element element = Element.of("plugin");
        element.addNode(Element.text("artifactId", "maven-compiler-plugin"));

        // Should default to org.apache.maven.plugins
        assertEquals("org.apache.maven.plugins:maven-compiler-plugin", AbstractMavenEditor.toPluginGA(element));
    }

    @Test
    void testToPluginGA_missingArtifactId() {
        Element element = Element.of("plugin");
        element.addNode(Element.text("groupId", "org.apache.maven.plugins"));

        assertNull(AbstractMavenEditor.toPluginGA(element));
    }

    @Test
    void testToGATC_jarType() {
        Element element = createDependency("org.junit.jupiter", "junit-jupiter", "5.9.2");
        element.addNode(Element.text("type", "jar"));

        assertEquals("org.junit.jupiter:junit-jupiter:jar", AbstractMavenEditor.toGATC(element));
    }

    @Test
    void testToGATC_defaultType() {
        Element element = createDependency("org.junit.jupiter", "junit-jupiter", "5.9.2");
        // No type specified, should default to jar

        assertEquals("org.junit.jupiter:junit-jupiter:jar", AbstractMavenEditor.toGATC(element));
    }

    @Test
    void testToGATC_withClassifier() {
        Element element = createDependency("org.example", "my-lib", "1.0.0");
        element.addNode(Element.text("type", "jar"));
        element.addNode(Element.text("classifier", "sources"));

        assertEquals("org.example:my-lib:jar:sources", AbstractMavenEditor.toGATC(element));
    }

    @Test
    void testToGATC_warType() {
        Element element = createDependency("org.example", "my-webapp", "1.0.0");
        element.addNode(Element.text("type", "war"));

        assertEquals("org.example:my-webapp:war", AbstractMavenEditor.toGATC(element));
    }

    @Test
    void testToGATC_missingGA() {
        Element element = Element.of("dependency");
        element.addNode(Element.text("type", "jar"));

        assertNull(AbstractMavenEditor.toGATC(element));
    }

    @Test
    void testToArtifact_complete() {
        PomEditor editor = new PomEditor();
        Element element = createDependency("org.junit.jupiter", "junit-jupiter", "5.9.2");

        Coordinates coordinates = editor.toCoordinates(element, "jar");

        assertEquals("org.junit.jupiter", coordinates.groupId());
        assertEquals("junit-jupiter", coordinates.artifactId());
        assertEquals("5.9.2", coordinates.version());
        assertNull(coordinates.classifier());
        assertEquals("jar", coordinates.type());
    }

    @Test
    void testToArtifact_withClassifier() {
        PomEditor editor = new PomEditor();
        Element element = createDependency("org.example", "my-lib", "1.0.0");
        element.addNode(Element.text("classifier", "sources"));

        Coordinates coordinates = editor.toCoordinates(element, "jar");

        assertEquals("sources", coordinates.classifier());
    }

    @Test
    void testToArtifact_maven4Inference() {
        PomEditor editor = new PomEditor();
        Element element = Element.of("dependency");
        element.addNode(Element.text("artifactId", "my-module"));
        // No groupId or version - Maven 4 inference

        Coordinates coordinates = editor.toCoordinates(element, "jar");

        assertNull(coordinates.groupId());
        assertEquals("my-module", coordinates.artifactId());
        assertNull(coordinates.version());
    }

    @Test
    void testToArtifact_missingArtifactId() {
        PomEditor editor = new PomEditor();
        Element element = Element.of("dependency");
        element.addNode(Element.text("groupId", "org.example"));

        // ArtifactId is always required
        assertThrows(IllegalArgumentException.class, () -> editor.toCoordinates(element, "jar"));
    }

    @Test
    void testToJarArtifact() {
        PomEditor editor = new PomEditor();
        Element element = createDependency("junit", "junit", "4.13.2");

        Coordinates coordinates = editor.toJarCoordinates(element);

        assertEquals("jar", coordinates.type());
    }

    @Test
    void testToPomArtifact() {
        PomEditor editor = new PomEditor();
        Element element = createDependency("org.example", "parent", "1.0.0");

        Coordinates coordinates = editor.toPomCoordinates(element);

        assertEquals("pom", coordinates.type());
    }

    private Element createDependency(String groupId, String artifactId, String version) {
        Element dep = Element.of("dependency");
        dep.addNode(Element.text("groupId", groupId));
        dep.addNode(Element.text("artifactId", artifactId));
        dep.addNode(Element.text("version", version));
        return dep;
    }
}
