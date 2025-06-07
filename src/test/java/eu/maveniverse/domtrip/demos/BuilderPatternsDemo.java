package eu.maveniverse.domtrip.demos;

import eu.maveniverse.domtrip.Attribute;
import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripConfig;
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import eu.maveniverse.domtrip.QuoteStyle;
import java.util.Map;

/**
 * Demonstrates the new builder patterns and factory methods.
 */
public class BuilderPatternsDemo {

    public static void main(String[] args) {
        System.out.println("=== Builder Patterns Demo ===\n");

        demonstrateAttributeBuilder();
        demonstrateElementFactory();
        demonstrateDocumentBuilder();
        demonstrateFluentEditorApi();

        System.out.println("\n=== Demo Complete ===");
    }

    private static void demonstrateAttributeBuilder() {
        System.out.println("1. Attribute Builder Demo:");

        // Simple attribute
        Attribute simple = Attribute.builder().name("id").value("123").build();

        // Complex attribute with custom formatting
        Attribute complex = Attribute.builder()
                .name("data-config")
                .value("complex value with <entities>")
                .quoteStyle(QuoteStyle.SINGLE)
                .precedingWhitespace("  ")
                .rawValue("&lt;raw&gt; value")
                .build();

        System.out.println("Simple attribute: " + simple);
        System.out.println("Complex attribute: " + complex);

        // Demonstrate immutable operations
        Attribute modified = simple.withValue("456").withQuoteStyle(QuoteStyle.SINGLE);
        System.out.println("Original: " + simple);
        System.out.println("Modified: " + modified);
        System.out.println();
    }

    private static void demonstrateElementFactory() {
        System.out.println("2. Element Factory Demo:");

        // Various element creation patterns
        Element textElement = Element.textElement("title", "My Document");
        Element emptyElement = Element.emptyElement("placeholder");
        Element selfClosing = Element.selfClosingElement("br");

        Element withAttributes = Element.elementWithAttributes(
                "div", Map.of("class", "container", "id", "main", "data-role", "content"));

        Element cdataElement = Element.cdataElement("script", "function test() { return x < y && z > 0; }");

        Element namespaced = Element.namespacedElement("xsi", "type", "http://www.w3.org/2001/XMLSchema-instance");

        // Using element builder for complex structures
        Element complex = Element.builder("article")
                .withAttribute("id", "article-1")
                .withAttribute("class", "blog-post")
                .withText("Article content here")
                .withChild(Element.textElement("author", "John Doe"))
                .withChild(Element.textElement("date", "2024-01-15"))
                .withComment(" Article metadata ")
                .build();

        System.out.println("Text element: " + textElement.toXml());
        System.out.println("Self-closing: " + selfClosing.toXml());
        System.out.println("With attributes: " + withAttributes.toXml());
        System.out.println("CDATA element: " + cdataElement.toXml());
        System.out.println("Namespaced: " + namespaced.toXml());
        System.out.println("Complex element: " + complex.toXml());
        System.out.println();
    }

    private static void demonstrateDocumentBuilder() {
        System.out.println("3. Document Builder Demo:");

        // Simple document
        Document simple = Document.withRootElement("simple");

        // Document with XML declaration
        Document withDeclaration = Document.withXmlDeclaration("1.0", "UTF-8");

        // Complex document using builder
        Document complex = Document.builder()
                .withVersion("1.1")
                .withEncoding("ISO-8859-1")
                .withStandalone(true)
                .withDoctype("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
                        + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">")
                .withRootElement("html")
                .withXmlDeclaration()
                .build();

        // Add content to the complex document
        Element html = complex.getDocumentElement();
        html.setAttribute("xmlns", "http://www.w3.org/1999/xhtml");

        Element head = Element.textElement("head", "");
        html.addChild(head);
        head.addChild(Element.textElement("title", "Demo Page"));

        Element body = Element.textElement("body", "");
        html.addChild(body);
        body.addChild(Element.elementWithAttributes("h1", Map.of("id", "title")));
        body.findChild("h1").ifPresent(h1 -> h1.setTextContent("Welcome"));

        System.out.println("Simple document:");
        System.out.println(simple.toXml());
        System.out.println("\nWith declaration:");
        System.out.println(withDeclaration.toXml());
        System.out.println("\nComplex document:");
        System.out.println(complex.toXml());
        System.out.println();
    }

    private static void demonstrateFluentEditorApi() {
        System.out.println("4. Fluent Editor API Demo:");

        // Create editor with configuration
        Editor editor = new Editor(DomTripConfig.defaults()
                .withDefaultQuoteStyle(QuoteStyle.SINGLE)
                .withPrettyPrint(true));

        editor.createDocument("project");
        Element root = editor.getRootElement();

        // Build complex structure using fluent API
        editor.add().element("modelVersion").to(root).withText("4.0.0").build();

        editor.add().element("groupId").to(root).withText("com.example").build();

        editor.add().element("artifactId").to(root).withText("demo-project").build();

        editor.add().element("version").to(root).withText("1.0.0").build();

        // Add properties section
        Element properties = editor.add().element("properties").to(root).build();

        editor.add().comment().to(properties).withContent(" Compiler settings ").build();

        editor.add()
                .element("maven.compiler.source")
                .to(properties)
                .withText("17")
                .build();

        editor.add()
                .element("maven.compiler.target")
                .to(properties)
                .withText("17")
                .build();

        // Add dependencies section
        Element dependencies = editor.add().element("dependencies").to(root).build();

        Element dependency = editor.add()
                .element("dependency")
                .to(dependencies)
                .withAttributes(Map.of("scope", "test", "optional", "false"))
                .build();

        editor.add()
                .element("groupId")
                .to(dependency)
                .withText("org.junit.jupiter")
                .build();

        editor.add()
                .element("artifactId")
                .to(dependency)
                .withText("junit-jupiter")
                .build();

        editor.add().element("version").to(dependency).withText("5.9.2").build();

        // Add CDATA section for build script
        Element build = editor.add().element("build").to(root).build();

        Element plugins = editor.add().element("plugins").to(build).build();

        Element plugin = editor.add().element("plugin").to(plugins).build();

        editor.add().element("configuration").to(plugin).build();

        Element configuration = plugin.findChild("configuration").orElseThrow();

        editor.add()
                .text()
                .to(configuration)
                .withContent("if (compile) { execute(); }")
                .asCData()
                .build();

        System.out.println("Generated Maven POM:");
        System.out.println(editor.toXml());

        // Demonstrate different serialization options
        System.out.println("\nWith pretty printing:");
        System.out.println(editor.toXml(DomTripConfig.prettyPrint()));

        System.out.println("\nMinimal output:");
        System.out.println(editor.toXml(DomTripConfig.minimal()));
        System.out.println();
    }
}
