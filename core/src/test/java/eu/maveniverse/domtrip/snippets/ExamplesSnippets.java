package eu.maveniverse.domtrip.snippets;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Snippet tests for the Examples documentation.
 *
 * <p>This class contains practical examples of using DomTrip for various
 * XML editing scenarios that are referenced in the Examples documentation.</p>
 */
public class ExamplesSnippets extends BaseSnippetTest {

    @Test
    public void demonstrateSimpleElementModification() {
        // START: simple-element-modification
        String xml =
                """
            <config>
                <database>
                    <host>localhost</host>
                    <port>5432</port>
                </database>
            </config>
            """;

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        // Find and update the host
        Element host = editor.root().descendant("host").orElseThrow();
        editor.setTextContent(host, "production-db.example.com");

        // Find and update the port
        Element port = editor.root().descendant("port").orElseThrow();
        editor.setTextContent(port, "5433");

        String result = editor.toXml();
        // END: simple-element-modification

        Assertions.assertTrue(result.contains("production-db.example.com"));
        Assertions.assertTrue(result.contains("5433"));
    }

    @Test
    public void demonstrateAddingNewElements() {
        // START: adding-new-elements
        Document doc = Document.of("<project></project>");
        Editor editor = new Editor(doc);

        Element root = editor.root();

        // Add dependencies section
        Element dependencies = editor.addElement(root, "dependencies");

        // Add a dependency with multiple children
        Element dependency = editor.addElement(dependencies, "dependency");
        editor.addElement(dependency, "groupId", "junit");
        editor.addElement(dependency, "artifactId", "junit");
        editor.addElement(dependency, "version", "4.13.2");
        editor.addElement(dependency, "scope", "test");

        String result = editor.toXml();
        // END: adding-new-elements

        Assertions.assertTrue(result.contains("<dependencies>"));
        Assertions.assertTrue(result.contains("<groupId>junit</groupId>"));
        Assertions.assertTrue(result.contains("<scope>test</scope>"));
    }

    @Test
    public void demonstrateMavenPomEditing() {
        // START: maven-pom-adding-dependencies
        String pomXml =
                """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.example</groupId>
                <artifactId>my-app</artifactId>
                <version>1.0.0</version>
            </project>
            """;

        Document doc = Document.of(pomXml);
        Editor editor = new Editor(doc);
        Element project = editor.root();

        // Add dependencies section if it doesn't exist
        Element dependencies = project.descendant("dependencies").orElse(null);
        if (dependencies == null) {
            dependencies = editor.addElement(project, "dependencies");
        }

        // Add Spring Boot starter
        Element springDep = editor.addElement(dependencies, "dependency");
        editor.addElement(springDep, "groupId", "org.springframework.boot");
        editor.addElement(springDep, "artifactId", "spring-boot-starter-web");
        editor.addElement(springDep, "version", "3.2.0");

        String result = editor.toXml();
        // END: maven-pom-adding-dependencies

        Assertions.assertTrue(result.contains("spring-boot-starter-web"));
        Assertions.assertTrue(result.contains("3.2.0"));
    }

    @Test
    public void demonstrateUpdatingVersion() {
        // START: maven-pom-updating-version
        String pomContent = createMavenPomXml();
        Document doc = Document.of(pomContent);
        Editor editor = new Editor(doc);

        // Update project version
        Element version = editor.root().descendant("version").orElse(null);
        if (version != null) {
            editor.setTextContent(version, "2.0.0");
        }

        // Update parent version if exists
        Element parent = editor.root().descendant("parent").orElse(null);
        if (parent != null) {
            Element parentVersion = parent.descendant("version").orElse(null);
            if (parentVersion != null) {
                editor.setTextContent(parentVersion, "2.1.0");
            }
        }
        // END: maven-pom-updating-version

        String result = editor.toXml();
        Assertions.assertTrue(result.contains("2.0.0"));
    }

    @Test
    public void demonstrateAttributeManipulation() {
        // START: attribute-manipulation
        String xmlContent = createConfigXml();
        Document doc = Document.of(xmlContent);
        Editor editor = new Editor(doc);

        // Set attributes
        Element element = editor.root().descendant("database").orElseThrow();
        editor.setAttribute(element, "scope", "test");
        editor.setAttribute(element, "optional", "true");

        // Remove attributes
        editor.removeAttribute(element, "scope");

        // Check if attribute exists
        if (element.hasAttribute("optional")) {
            String value = element.attribute("optional");
            System.out.println("Optional: " + value);
        }
        // END: attribute-manipulation

        String result = editor.toXml();
        Assertions.assertTrue(result.contains("optional=\"true\""));
        Assertions.assertFalse(result.contains("scope="));
    }

    @Test
    public void demonstrateErrorHandling() {
        // START: safe-element-handling
        try {
            String xmlContent = createConfigXml();
            Document doc = Document.of(xmlContent);
            Editor editor = new Editor(doc);

            // Safe element finding
            Element element = editor.root().descendant("nonexistent").orElse(null);
            if (element == null) {
                System.out.println("Element not found");
                return;
            }

            // Modify element
            editor.setTextContent(element, "new value");

        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
        // END: safe-element-handling

        // Test passes if no exception is thrown
        Assertions.assertTrue(true);
    }

    @Test
    public void demonstrateBestPracticesOptional() {
        // START: best-practices-optional
        String xml = createMavenPomXml();
        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        editor.root()
                .descendant("dependencies")
                .map(deps -> editor.addElement(deps, "dependency"))
                .ifPresent(dep -> {
                    editor.addElement(dep, "groupId", "org.example");
                    editor.addElement(dep, "artifactId", "example-lib");
                });
        // END: best-practices-optional

        String result = editor.toXml();
        // Since dependencies doesn't exist in the base POM, nothing should be added
        Assertions.assertFalse(result.contains("org.example"));
    }

    @Test
    public void demonstratePreserveFormatting() {
        // START: best-practices-preserve-formatting
        String xml = createConfigXml();
        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        // DomTrip automatically preserves formatting
        // No special configuration needed
        String result = editor.toXml(); // Maintains original indentation and style
        // END: best-practices-preserve-formatting

        Assertions.assertTrue(result.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
    }
}
