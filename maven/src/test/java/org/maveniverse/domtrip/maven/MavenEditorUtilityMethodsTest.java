package org.maveniverse.domtrip.maven;

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

        Artifact artifact = editor.toArtifact(element, "jar");

        assertEquals("org.junit.jupiter", artifact.groupId());
        assertEquals("junit-jupiter", artifact.artifactId());
        assertEquals("5.9.2", artifact.version());
        assertNull(artifact.classifier());
        assertEquals("jar", artifact.type());
    }

    @Test
    void testToArtifact_withClassifier() {
        PomEditor editor = new PomEditor();
        Element element = createDependency("org.example", "my-lib", "1.0.0");
        element.addNode(Element.text("classifier", "sources"));

        Artifact artifact = editor.toArtifact(element, "jar");

        assertEquals("sources", artifact.classifier());
    }

    @Test
    void testToArtifact_maven4Inference() {
        PomEditor editor = new PomEditor();
        Element element = Element.of("dependency");
        element.addNode(Element.text("artifactId", "my-module"));
        // No groupId or version - Maven 4 inference

        Artifact artifact = editor.toArtifact(element, "jar");

        assertNull(artifact.groupId());
        assertEquals("my-module", artifact.artifactId());
        assertNull(artifact.version());
    }

    @Test
    void testToArtifact_missingArtifactId() {
        PomEditor editor = new PomEditor();
        Element element = Element.of("dependency");
        element.addNode(Element.text("groupId", "org.example"));

        // ArtifactId is always required
        assertThrows(IllegalArgumentException.class, () -> editor.toArtifact(element, "jar"));
    }

    @Test
    void testToJarArtifact() {
        PomEditor editor = new PomEditor();
        Element element = createDependency("junit", "junit", "4.13.2");

        Artifact artifact = editor.toJarArtifact(element);

        assertEquals("jar", artifact.type());
    }

    @Test
    void testToPomArtifact() {
        PomEditor editor = new PomEditor();
        Element element = createDependency("org.example", "parent", "1.0.0");

        Artifact artifact = editor.toPomArtifact(element);

        assertEquals("pom", artifact.type());
    }

    private Element createDependency(String groupId, String artifactId, String version) {
        Element dep = Element.of("dependency");
        dep.addNode(Element.text("groupId", groupId));
        dep.addNode(Element.text("artifactId", artifactId));
        dep.addNode(Element.text("version", version));
        return dep;
    }
}
