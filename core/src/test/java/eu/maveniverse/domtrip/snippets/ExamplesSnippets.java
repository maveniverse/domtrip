package eu.maveniverse.domtrip.snippets;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripException;
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
    public void demonstrateSimpleElementModification() throws DomTripException {
        // START: simple-element-modification
        String xml = """
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
    public void demonstrateAddingNewElements() throws DomTripException {
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
    public void demonstrateMavenPomEditing() throws DomTripException {
        // START: maven-pom-adding-dependencies
        String pomXml = """
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
    public void demonstrateUpdatingVersion() throws DomTripException {
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
    public void demonstrateAttributeManipulation() throws DomTripException {
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
    public void demonstrateBestPracticesOptional() throws DomTripException {
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
        // Since dependencies exists in the base POM, the dependency should be added
        Assertions.assertTrue(result.contains("org.example"));
    }

    @Test
    public void demonstratePreserveFormatting() throws DomTripException {
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

    @Test
    public void demonstrateSpringConfiguration() throws DomTripException {
        // START: spring-configuration
        String springConfig = """
            <?xml version="1.0" encoding="UTF-8"?>
            <beans xmlns="http://www.springframework.org/schema/beans">
                <bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource">
                    <property name="driverClassName" value="org.postgresql.Driver"/>
                    <property name="url" value="jdbc:postgresql://localhost:5432/mydb"/>
                </bean>
            </beans>
            """;

        Document doc = Document.of(springConfig);
        Editor editor = new Editor(doc);

        // Find the dataSource bean
        Element dataSource = editor.root()
                .descendants()
                .filter(e -> "bean".equals(e.name()) && "dataSource".equals(e.attribute("id")))
                .findFirst()
                .orElseThrow();

        // Update the URL property
        Element urlProperty = dataSource
                .childElements()
                .filter(e -> "property".equals(e.name()) && "url".equals(e.attribute("name")))
                .findFirst()
                .orElseThrow();

        editor.setAttribute(urlProperty, "value", "jdbc:postgresql://prod-db:5432/mydb");

        // Add new property
        Element newProperty = editor.addElement(dataSource, "property");
        editor.setAttribute(newProperty, "name", "maxActive");
        editor.setAttribute(newProperty, "value", "100");
        // END: spring-configuration

        String result = editor.toXml();
        Assertions.assertTrue(result.contains("prod-db:5432"));
        Assertions.assertTrue(result.contains("maxActive"));
        Assertions.assertTrue(result.contains("100"));
    }

    @Test
    public void demonstrateWorkingWithNamespaces() throws DomTripException {
        // START: working-with-namespaces
        String xmlWithNamespaces = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                <modelVersion>4.0.0</modelVersion>
            </project>
            """;

        Document doc = Document.of(xmlWithNamespaces);
        Editor editor = new Editor(doc);

        // Add element with namespace
        Element project = editor.root();
        Element build = editor.addElement(project, "build");
        Element plugins = editor.addElement(build, "plugins");

        // Elements inherit the default namespace automatically
        Element plugin = editor.addElement(plugins, "plugin");
        editor.addElement(plugin, "groupId", "org.apache.maven.plugins");
        editor.addElement(plugin, "artifactId", "maven-compiler-plugin");
        // END: working-with-namespaces

        String result = editor.toXml();
        Assertions.assertTrue(result.contains("maven-compiler-plugin"));
        Assertions.assertTrue(result.contains("<build>"));
        Assertions.assertTrue(result.contains("<plugins>"));
    }

    @Test
    public void demonstrateBuilderPatterns() throws DomTripException {
        // START: using-builder-patterns
        // Create elements using factory methods (simplified builder pattern)
        Element dependency = Element.of("dependency");
        dependency.addNode(Element.text("groupId", "org.junit.jupiter"));
        dependency.addNode(Element.text("artifactId", "junit-jupiter"));
        dependency.addNode(Element.text("version", "5.9.2"));
        dependency.addNode(Element.text("scope", "test"));

        // Add to existing document
        String pomXml = createMavenPomXml();
        Document doc = Document.of(pomXml);
        Editor editor = new Editor(doc);

        Element dependencies = editor.root().descendant("dependencies").orElse(null);
        if (dependencies == null) {
            dependencies = editor.addElement(editor.root(), "dependencies");
        }
        dependencies.addNode(dependency);
        // END: using-builder-patterns

        String result = editor.toXml();
        Assertions.assertTrue(result.contains("junit-jupiter"));
        Assertions.assertTrue(result.contains("5.9.2"));
        Assertions.assertTrue(result.contains("test"));
    }
}
