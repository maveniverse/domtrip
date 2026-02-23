package eu.maveniverse.domtrip.maven;

import static org.junit.jupiter.api.Assertions.*;

import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Element;
import org.junit.jupiter.api.Test;

/**
 * Tests for AbstractMavenEditor utility methods.
 */
class MavenEditorUtilityMethodsTest {

    @Test
    void testToGA_complete() throws DomTripException {
        Element element = createDependency("org.junit.jupiter", "junit-jupiter", "5.9.2");
        assertEquals("org.junit.jupiter:junit-jupiter", AbstractMavenEditor.toGA(element));
    }

    @Test
    void testToGA_missingGroupId() throws DomTripException {
        Element element = Element.of("dependency");
        element.addChild(Element.text("artifactId", "my-module"));

        assertNull(AbstractMavenEditor.toGA(element));
    }

    @Test
    void testToGA_missingArtifactId() throws DomTripException {
        Element element = Element.of("dependency");
        element.addChild(Element.text("groupId", "org.example"));

        assertNull(AbstractMavenEditor.toGA(element));
    }

    @Test
    void testToPluginGA_withGroupId() throws DomTripException {
        Element element = Element.of("plugin");
        element.addChild(Element.text("groupId", "org.codehaus.mojo"));
        element.addChild(Element.text("artifactId", "build-helper-maven-plugin"));

        assertEquals("org.codehaus.mojo:build-helper-maven-plugin", AbstractMavenEditor.toPluginGA(element));
    }

    @Test
    void testToPluginGA_withoutGroupId() throws DomTripException {
        Element element = Element.of("plugin");
        element.addChild(Element.text("artifactId", "maven-compiler-plugin"));

        // Should default to org.apache.maven.plugins
        assertEquals("org.apache.maven.plugins:maven-compiler-plugin", AbstractMavenEditor.toPluginGA(element));
    }

    @Test
    void testToPluginGA_missingArtifactId() throws DomTripException {
        Element element = Element.of("plugin");
        element.addChild(Element.text("groupId", "org.apache.maven.plugins"));

        assertNull(AbstractMavenEditor.toPluginGA(element));
    }

    @Test
    void testToGATC_jarType() throws DomTripException {
        Element element = createDependency("org.junit.jupiter", "junit-jupiter", "5.9.2");
        element.addChild(Element.text("type", "jar"));

        assertEquals("org.junit.jupiter:junit-jupiter:jar", AbstractMavenEditor.toGATC(element));
    }

    @Test
    void testToGATC_defaultType() throws DomTripException {
        Element element = createDependency("org.junit.jupiter", "junit-jupiter", "5.9.2");
        // No type specified, should default to jar

        assertEquals("org.junit.jupiter:junit-jupiter:jar", AbstractMavenEditor.toGATC(element));
    }

    @Test
    void testToGATC_withClassifier() throws DomTripException {
        Element element = createDependency("org.example", "my-lib", "1.0.0");
        element.addChild(Element.text("type", "jar"));
        element.addChild(Element.text("classifier", "sources"));

        assertEquals("org.example:my-lib:jar:sources", AbstractMavenEditor.toGATC(element));
    }

    @Test
    void testToGATC_warType() throws DomTripException {
        Element element = createDependency("org.example", "my-webapp", "1.0.0");
        element.addChild(Element.text("type", "war"));

        assertEquals("org.example:my-webapp:war", AbstractMavenEditor.toGATC(element));
    }

    @Test
    void testToGATC_missingGA() throws DomTripException {
        Element element = Element.of("dependency");
        element.addChild(Element.text("type", "jar"));

        assertNull(AbstractMavenEditor.toGATC(element));
    }

    @Test
    void testToArtifact_complete() throws DomTripException {
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
    void testToArtifact_withClassifier() throws DomTripException {
        PomEditor editor = new PomEditor();
        Element element = createDependency("org.example", "my-lib", "1.0.0");
        element.addChild(Element.text("classifier", "sources"));

        Coordinates coordinates = editor.toCoordinates(element, "jar");

        assertEquals("sources", coordinates.classifier());
    }

    @Test
    void testToArtifact_maven4Inference() throws DomTripException {
        PomEditor editor = new PomEditor();
        Element element = Element.of("dependency");
        element.addChild(Element.text("artifactId", "my-module"));
        // No groupId or version - Maven 4 inference

        Coordinates coordinates = editor.toCoordinates(element, "jar");

        assertNull(coordinates.groupId());
        assertEquals("my-module", coordinates.artifactId());
        assertNull(coordinates.version());
    }

    @Test
    void testToArtifact_missingArtifactId() throws DomTripException {
        PomEditor editor = new PomEditor();
        Element element = Element.of("dependency");
        element.addChild(Element.text("groupId", "org.example"));

        // ArtifactId is always required
        assertThrows(DomTripException.class, () -> editor.toCoordinates(element, "jar"));
    }

    @Test
    void testToJarArtifact() throws DomTripException {
        PomEditor editor = new PomEditor();
        Element element = createDependency("junit", "junit", "4.13.2");

        Coordinates coordinates = editor.toJarCoordinates(element);

        assertEquals("jar", coordinates.type());
    }

    @Test
    void testToPomArtifact() throws DomTripException {
        PomEditor editor = new PomEditor();
        Element element = createDependency("org.example", "parent", "1.0.0");

        Coordinates coordinates = editor.toPomCoordinates(element);

        assertEquals("pom", coordinates.type());
    }

    private Element createDependency(String groupId, String artifactId, String version) throws DomTripException {
        Element dep = Element.of("dependency");
        dep.addChild(Element.text("groupId", groupId));
        dep.addChild(Element.text("artifactId", artifactId));
        dep.addChild(Element.text("version", version));
        return dep;
    }
}
