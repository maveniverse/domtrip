package eu.maveniverse.domtrip.demos;

import static org.junit.jupiter.api.Assertions.*;

import eu.maveniverse.domtrip.Attribute;
import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripConfig;
import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import eu.maveniverse.domtrip.QuoteStyle;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Demonstration of the improved DomTrip API features.
 */
class ImprovedApiDemoTest {

    @Test
    void demonstrateImprovedApi() throws DomTripException {
        verifyFactoryMethods();
        verifyFluentBuilders();
        verifyConfigurationOptions();
        verifyEnhancedNavigation();
        verifySerializationOptions();
    }

    private static void verifyFactoryMethods() throws DomTripException {
        // Create document using factory
        Document doc = Document.of()
                .version("1.0")
                .encoding("UTF-8")
                .root(new Element("project"))
                .withXmlDeclaration();

        // Create elements using factory
        Element dependencies = Element.text("dependencies", "");
        Element dependency = Element.withAttributes("dependency", Map.of("scope", "test", "optional", "true"));

        // Add using factory-created elements
        doc.root().addChild(dependencies);
        dependencies.addChild(dependency);

        // Add child elements
        dependency.addChild(Element.text("groupId", "junit"));
        dependency.addChild(Element.text("artifactId", "junit"));
        dependency.addChild(Element.text("version", "4.13.2"));

        String result = doc.toXml();
        assertNotNull(result);
        assertTrue(result.contains("junit"));
        assertTrue(result.contains("4.13.2"));
    }

    private static void verifyFluentBuilders() throws DomTripException {
        // Create editor with configuration
        Editor editor = new Editor(DomTripConfig.defaults());
        editor.createDocument("configuration");

        Element root = editor.root();

        // Use fluent builder API
        editor.add()
                .element("database")
                .to(root)
                .withAttribute("type", "postgresql")
                .withAttribute("host", "localhost")
                .build();

        Element database = root.childElement("database").orElse(null);
        assertNotNull(database);

        editor.add()
                .element("connection")
                .to(database)
                .withText("jdbc:postgresql://localhost:5432/mydb")
                .build();

        editor.add()
                .comment()
                .to(database)
                .withContent(" Database configuration ")
                .build();

        // Demonstrate attribute builder and immutable operations
        database.attribute("custom-attr", "custom-value");

        // Show how to work with attribute objects for advanced formatting
        Attribute originalAttr = database.attributeObject("custom-attr");
        assertNotNull(originalAttr);

        // Create a modified version with different quote style (immutable operation)
        Attribute customAttr = originalAttr.withQuoteStyle(QuoteStyle.SINGLE);
        assertNotEquals(originalAttr.quoteStyle(), customAttr.quoteStyle());

        String result = editor.toXml();
        assertTrue(result.contains("postgresql"));
        assertTrue(result.contains("Database configuration"));
    }

    private static void verifyConfigurationOptions() throws DomTripException {
        String xml = "<root><element attr=\"value\">content</element></root>";

        // Default configuration
        Editor defaultEditor = new Editor(Document.of(xml), DomTripConfig.defaults());
        assertNotNull(defaultEditor.toXml());

        // Pretty print configuration
        Editor prettyEditor = new Editor(Document.of(xml), DomTripConfig.prettyPrint());
        assertNotNull(prettyEditor.toXml());
    }

    private static void verifyEnhancedNavigation() throws DomTripException {
        String xml = """
            <project>
                <dependencies>
                    <dependency>
                        <groupId>junit</groupId>
                        <artifactId>junit</artifactId>
                    </dependency>
                    <dependency>
                        <groupId>mockito</groupId>
                        <artifactId>mockito-core</artifactId>
                    </dependency>
                </dependencies>
                <properties>
                    <maven.compiler.source>11</maven.compiler.source>
                </properties>
            </project>
            """;

        Editor editor = new Editor(Document.of(xml));
        Element root = editor.root();

        // Enhanced navigation methods
        assertTrue(root.childElement("dependencies").isPresent());
        root.childElement("dependencies").ifPresent(deps -> {
            long depCount = deps.childElements("dependency").count();
            assertEquals(2, depCount);

            // Stream-based navigation
            deps.childElements("dependency").forEach(dep -> {
                assertTrue(dep.childElement("groupId").isPresent());
            });
        });

        // Find all descendants
        long groupIdCount =
                root.descendants().filter(el -> "groupId".equals(el.name())).count();
        assertEquals(2, groupIdCount);

        // Check relationships
        Element properties = root.descendant("properties").orElse(null);
        assertNotNull(properties);
        assertTrue(properties.depth() > 0);
        assertTrue(properties.isDescendantOf(root));
    }

    private static void verifySerializationOptions() throws DomTripException {
        String xml = "<?xml version=\"1.0\"?>\n<!-- Comment -->\n<root attr=\"value\">content</root>";
        Editor editor = new Editor(Document.of(xml));

        // Different serialization options
        String defaultResult = editor.toXml(DomTripConfig.defaults());
        assertNotNull(defaultResult);

        String prettyResult = editor.toXml(DomTripConfig.prettyPrint());
        assertNotNull(prettyResult);

        String minimalResult = editor.toXml(DomTripConfig.minimal());
        assertNotNull(minimalResult);
        assertFalse(minimalResult.contains("<!--"));

        DomTripConfig custom = DomTripConfig.defaults()
                .withPrettyPrint(true)
                .withIndentString("\t")
                .withCommentPreservation(false);
        String customResult = editor.toXml(custom);
        assertNotNull(customResult);
        assertFalse(customResult.contains("<!--"));
    }
}
