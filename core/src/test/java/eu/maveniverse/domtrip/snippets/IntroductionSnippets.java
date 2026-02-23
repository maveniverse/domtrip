package eu.maveniverse.domtrip.snippets;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Snippet tests for the Introduction documentation.
 */
public class IntroductionSnippets extends BaseSnippetTest {

    @Test
    public void demonstrateRoundTripPreservation() throws DomTripException {
        // START: round-trip-preservation
        String originalXml = createTestXml("root");
        Document doc = Document.of(originalXml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();
        // result is IDENTICAL to originalXml if no modifications were made
        // END: round-trip-preservation

        Assertions.assertEquals(originalXml, result);
    }

    @Test
    public void demonstrateIntelligentEditing() throws DomTripException {
        // START: intelligent-editing
        // Add new elements while preserving original formatting
        String xml = createMavenPomXml();
        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element parent = editor.root().descendant("dependencies").orElseThrow();
        Element newDep = editor.addElement(parent, "dependency");
        editor.addElement(newDep, "groupId", "org.example");
        // END: intelligent-editing

        String result = editor.toXml();
        Assertions.assertTrue(result.contains("org.example"));
    }

    @Test
    public void demonstrateModernJavaAPI() throws DomTripException {
        // START: modern-java-api
        // Fluent builders and Stream-based navigation
        Element element = Element.of("dependency").attribute("scope", "test");
        element.addNode(Element.text("groupId", "junit"));

        String xml = createTestXml("root");
        Document doc = Document.of(xml);
        Element root = doc.root();
        Optional<Element> child = root.childElement("dependency");
        Stream<Element> descendants = root.descendants();
        // END: modern-java-api

        Assertions.assertEquals("dependency", element.name());
        Assertions.assertEquals("test", element.attribute("scope"));
        Assertions.assertNotNull(child);
        Assertions.assertNotNull(descendants);
    }

    @Test
    public void demonstrateQuickExample() throws DomTripException {
        // START: quick-example
        // Parse XML while preserving all formatting
        Document doc = Document.of("""
            <?xml version="1.0" encoding="UTF-8"?>
            <!-- Project configuration -->
            <project xmlns="http://maven.apache.org/POM/4.0.0">
                <groupId>com.example</groupId>
                <version>1.0.0</version>
            </project>
            """);
        Editor editor = new Editor(doc);

        // Make targeted changes
        Element version = editor.root().descendant("version").orElseThrow();
        editor.setTextContent(version, "2.0.0");

        // Output preserves ALL original formatting
        String result = editor.toXml();
        // Comments, whitespace, namespaces - everything preserved
        // Only the version number changes
        // END: quick-example

        Assertions.assertTrue(result.contains("2.0.0"));
        Assertions.assertTrue(result.contains("<!-- Project configuration -->"));
        Assertions.assertTrue(result.contains("xmlns=\"http://maven.apache.org/POM/4.0.0\""));
    }
}
