package eu.maveniverse.domtrip.demos;

import static org.junit.jupiter.api.Assertions.*;

import eu.maveniverse.domtrip.Attribute;
import eu.maveniverse.domtrip.Comment;
import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripConfig;
import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import eu.maveniverse.domtrip.QName;
import eu.maveniverse.domtrip.QuoteStyle;
import eu.maveniverse.domtrip.Text;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Demonstrates the new builder patterns and factory methods.
 */
class BuilderPatternsDemoTest {

    @Test
    void demonstrateBuilderPatterns() throws DomTripException {
        verifyAttributeFactory();
        verifyElementFactory();
        verifyDocumentBuilder();
        verifyFluentEditorApi();
    }

    private static void verifyAttributeFactory() {
        // Simple attribute
        Attribute simple = Attribute.of("id", "123");
        assertNotNull(simple);
        assertEquals("123", simple.value());

        // Complex attribute with custom formatting
        Attribute complex = Attribute.of("data-config", "complex value with <entities>", QuoteStyle.SINGLE, "  ")
                .rawValue("&lt;raw&gt; value");
        assertNotNull(complex);

        // Demonstrate immutable operations
        Attribute modified = simple.withValue("456").withQuoteStyle(QuoteStyle.SINGLE);
        assertEquals("123", simple.value(), "Original should be unchanged");
        assertEquals("456", modified.value(), "Modified should have new value");
    }

    private static void verifyElementFactory() throws DomTripException {
        // Various element creation patterns
        Element textElement = Element.text("title", "My Document");
        assertNotNull(textElement);
        assertEquals("My Document", textElement.textContent());

        Element emptyElement = Element.of("placeholder");
        assertNotNull(emptyElement);

        Element selfClosing = Element.selfClosing("br");
        assertNotNull(selfClosing);

        Element withAttributes =
                Element.withAttributes("div", Map.of("class", "container", "id", "main", "data-role", "content"));
        assertNotNull(withAttributes);
        assertEquals("container", withAttributes.attribute("class"));

        Element cdataElement = Element.cdata("script", "function test() { return x < y && z > 0; }");
        assertNotNull(cdataElement);

        Element namespaced = Element.of(QName.of("http://www.w3.org/2001/XMLSchema-instance", "type", "xsi"));
        assertNotNull(namespaced);

        // Using element fluent API for complex structures
        Element complex = Element.of("article").attribute("id", "article-1").attribute("class", "blog-post");
        complex.addChild(new Text("Article content here"));
        complex.addChild(Element.text("author", "John Doe"));
        complex.addChild(Element.text("date", "2024-01-15"));
        complex.addChild(new Comment(" Article metadata "));

        String complexXml = complex.toXml();
        assertTrue(complexXml.contains("article-1"));
        assertTrue(complexXml.contains("John Doe"));
    }

    private static void verifyDocumentBuilder() throws DomTripException {
        // Simple document
        Document simple = Document.of().root(new Element("simple"));
        assertNotNull(simple.root());
        assertEquals("simple", simple.root().name());

        // Document with XML declaration
        Document withDeclaration = Document.withXmlDeclaration("1.0", "UTF-8");
        assertNotNull(withDeclaration);

        // Complex document using builder
        Document complex = Document.of()
                .version("1.1")
                .encoding("ISO-8859-1")
                .standalone(true)
                .doctype("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
                        + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">")
                .root(new Element("html"))
                .withXmlDeclaration();

        // Add content to the complex document
        Element html = complex.root();
        html.attribute("xmlns", "http://www.w3.org/1999/xhtml");

        Element head = Element.text("head", "");
        html.addChild(head);
        head.addChild(Element.text("title", "Demo Page"));

        Element body = Element.text("body", "");
        html.addChild(body);
        body.addChild(Element.withAttributes("h1", Map.of("id", "title")));
        body.childElement("h1").ifPresent(h1 -> h1.textContent("Welcome"));

        String complexXml = complex.toXml();
        assertTrue(complexXml.contains("Demo Page"));
        assertTrue(complexXml.contains("Welcome"));
    }

    private static void verifyFluentEditorApi() throws DomTripException {
        // Create editor with configuration
        Editor editor = new Editor(
                Document.of(),
                DomTripConfig.defaults()
                        .withDefaultQuoteStyle(QuoteStyle.SINGLE)
                        .withPrettyPrint(true));

        editor.createDocument("project");
        Element root = editor.root();

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
        Element configuration = plugin.childElement("configuration").orElseThrow();
        editor.add()
                .text()
                .to(configuration)
                .withContent("if (compile) { execute(); }")
                .asCData()
                .build();

        String result = editor.toXml();
        assertNotNull(result);
        assertTrue(result.contains("com.example"));
        assertTrue(result.contains("junit-jupiter"));

        // Verify different serialization options work
        String prettyResult = editor.toXml(DomTripConfig.prettyPrint());
        assertNotNull(prettyResult);

        String minimalResult = editor.toXml(DomTripConfig.minimal());
        assertNotNull(minimalResult);
    }
}
