package eu.maveniverse.domtrip.demos;

import eu.maveniverse.domtrip.Attribute;
import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.Documents;
import eu.maveniverse.domtrip.DomTripConfig;
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import eu.maveniverse.domtrip.Elements;
import eu.maveniverse.domtrip.QuoteStyle;
import java.util.Map;

/**
 * Demonstration of the improved DomTrip API features.
 */
public class ImprovedApiDemo {

    public static void main(String[] args) {
        System.out.println("=== DomTrip Improved API Demo ===\n");

        // Demo 1: Using factory methods
        demonstrateFactoryMethods();

        // Demo 2: Using fluent builders
        demonstrateFluentBuilders();

        // Demo 3: Using configuration options
        demonstrateConfigurationOptions();

        // Demo 4: Using enhanced navigation
        demonstrateEnhancedNavigation();

        // Demo 5: Using serialization options
        demonstrateSerializationOptions();

        System.out.println("\n=== Demo Complete ===");
    }

    private static void demonstrateFactoryMethods() {
        System.out.println("1. Factory Methods Demo:");

        // Create document using factory
        Document doc = Documents.builder()
                .withVersion("1.0")
                .withEncoding("UTF-8")
                .withRootElement("project")
                .withXmlDeclaration()
                .build();

        // Create elements using factory
        Element dependencies = Elements.textElement("dependencies", "");
        Element dependency = Elements.elementWithAttributes("dependency", Map.of("scope", "test", "optional", "true"));

        // Add using factory-created elements
        doc.getDocumentElement().addChild(dependencies);
        dependencies.addChild(dependency);

        // Add child elements
        dependency.addChild(Elements.textElement("groupId", "junit"));
        dependency.addChild(Elements.textElement("artifactId", "junit"));
        dependency.addChild(Elements.textElement("version", "4.13.2"));

        System.out.println("Created document using factories:");
        System.out.println(doc.toXml());
        System.out.println();
    }

    private static void demonstrateFluentBuilders() {
        System.out.println("2. Fluent Builders Demo:");

        // Create editor with configuration
        Editor editor = new Editor(DomTripConfig.defaults());
        editor.createDocument("configuration");

        Element root = editor.getRootElement();

        // Use fluent builder API
        editor.add()
                .element("database")
                .to(root)
                .withAttribute("type", "postgresql")
                .withAttribute("host", "localhost")
                .build();

        Element database = root.findChild("database").orElse(null);

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
        database.setAttribute("custom-attr", "custom-value");

        // Show how to work with attribute objects for advanced formatting
        Attribute originalAttr = database.getAttributeObject("custom-attr");
        if (originalAttr != null) {
            // Create a modified version with different quote style (immutable operation)
            Attribute customAttr = originalAttr.withQuoteStyle(QuoteStyle.SINGLE);
            System.out.println("Original attribute: " + originalAttr);
            System.out.println("Modified attribute: " + customAttr);
        }

        System.out.println("Created using fluent builders:");
        System.out.println(editor.toXml());
        System.out.println();
    }

    private static void demonstrateConfigurationOptions() {
        System.out.println("3. Configuration Options Demo:");

        String xml = "<root><element attr=\"value\">content</element></root>";

        // Default configuration
        Editor defaultEditor = new Editor(xml, DomTripConfig.defaults());
        System.out.println("Default config: " + defaultEditor.toXml());

        // Strict configuration
        Editor strictEditor = new Editor(xml, DomTripConfig.strict());
        System.out.println("Strict config: " + strictEditor.toXml());

        // Pretty print configuration
        Editor prettyEditor = new Editor(xml, DomTripConfig.prettyPrint());
        System.out.println("Pretty print config: " + prettyEditor.toXml());
        System.out.println();
    }

    private static void demonstrateEnhancedNavigation() {
        System.out.println("4. Enhanced Navigation Demo:");

        String xml =
                """
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

        Editor editor = new Editor(xml);
        Element root = editor.getRootElement();

        // Enhanced navigation methods
        System.out.println("Finding dependencies using new navigation:");
        root.findChild("dependencies").ifPresent(deps -> {
            System.out.println("Found dependencies element");

            // Stream-based navigation
            deps.findChildren("dependency").forEach(dep -> {
                dep.findChild("groupId")
                        .ifPresent(groupId -> System.out.println("  GroupId: " + groupId.getTextContent()));
            });
        });

        // Find all descendants
        System.out.println("All groupId elements in document:");
        root.descendants()
                .filter(el -> "groupId".equals(el.getName()))
                .forEach(el -> System.out.println("  " + el.getTextContent()));

        // Check relationships
        Element properties = root.findDescendant("properties").orElse(null);
        if (properties != null) {
            System.out.println("Properties depth: " + properties.getDepth());
            System.out.println("Is descendant of root: " + properties.isDescendantOf(root));
        }
        System.out.println();
    }

    private static void demonstrateSerializationOptions() {
        System.out.println("5. Serialization Options Demo:");

        String xml = "<?xml version=\"1.0\"?>\n<!-- Comment -->\n<root attr=\"value\">content</root>";
        Editor editor = new Editor(xml);

        // Different serialization options
        System.out.println("Default serialization:");
        System.out.println(editor.toXml(DomTripConfig.defaults()));

        System.out.println("\nPretty print:");
        System.out.println(editor.toXml(DomTripConfig.prettyPrint()));

        System.out.println("\nMinimal (no comments, no declaration):");
        System.out.println(editor.toXml(DomTripConfig.minimal()));

        System.out.println("\nCustom options:");
        DomTripConfig custom = DomTripConfig.defaults()
                .withPrettyPrint(true)
                .withIndentString("\t")
                .withCommentPreservation(false);
        System.out.println(editor.toXml(custom));
        System.out.println();
    }
}
