package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test cases for the new Builder API functionality.
 */
public class BuilderApiTest {

    private Editor editor;

    @BeforeEach
    void setUp() {
        editor = new Editor(Document.of());
    }

    @Test
    void testAttributeBuilder() {
        Attribute attr = Attribute.builder()
                .name("test-attr")
                .value("test-value")
                .quoteStyle(QuoteStyle.SINGLE)
                .precedingWhitespace("  ")
                .rawValue("&lt;raw&gt;")
                .build();

        assertEquals("test-attr", attr.name());
        assertEquals("test-value", attr.value());
        assertEquals(QuoteStyle.SINGLE, attr.quoteStyle());
        assertEquals("  ", attr.precedingWhitespace());
        assertEquals("&lt;raw&gt;", attr.rawValue());
    }

    @Test
    void testAttributeBuilderMinimal() {
        Attribute attr = Attribute.builder().name("simple").value("value").build();

        assertEquals("simple", attr.name());
        assertEquals("value", attr.value());
        assertEquals(QuoteStyle.DOUBLE, attr.quoteStyle()); // default
        assertEquals(" ", attr.precedingWhitespace()); // default
    }

    @Test
    void testAttributeBuilderRequiresName() {
        assertThrows(IllegalStateException.class, () -> {
            Attribute.builder().value("value").build();
        });
    }

    @Test
    void testElementsFactory() {
        // Test text element
        Element textEl = Element.text("name", "content");
        assertEquals("name", textEl.name());
        assertEquals("content", textEl.textContent());

        // Test empty element
        Element emptyEl = Element.of("empty");
        assertEquals("empty", emptyEl.name());
        assertEquals(0, emptyEl.nodeCount());

        // Test self-closing element
        Element selfClosing = Element.selfClosing("self");
        assertEquals("self", selfClosing.name());
        assertTrue(selfClosing.selfClosing());

        // Test element with attributes
        Map<String, String> attrs = Map.of("attr1", "val1", "attr2", "val2");
        Element withAttrs = Element.withAttributes("test", attrs);
        assertEquals("test", withAttrs.name());
        assertEquals("val1", withAttrs.attribute("attr1"));
        assertEquals("val2", withAttrs.attribute("attr2"));
    }

    @Test
    void testElementsFluentApi() {
        Element element = Element.of("complex")
                .attribute("attr1", "value1")
                .attribute("attr2", "value2")
                .attribute("attr3", "value3");

        element.addNode(new Text("text content"));
        element.addNode(Element.text("child", "child content"));
        element.addNode(new Comment("This is a comment"));

        assertEquals("complex", element.name());
        assertEquals("value1", element.attribute("attr1"));
        assertEquals("value2", element.attribute("attr2"));
        assertEquals("value3", element.attribute("attr3"));
        assertTrue(element.hasNodeElements());
        assertTrue(element.hasTextContent());
    }

    @Test
    void testDocumentsFactory() {
        // Test empty document
        Document empty = Document.of();
        assertNotNull(empty);
        assertNull(empty.root());

        // Test with XML declaration
        Document withDecl = Document.withXmlDeclaration("1.0", "UTF-8");
        assertEquals("1.0", withDecl.version());
        assertEquals("UTF-8", withDecl.encoding());
        assertTrue(withDecl.xmlDeclaration().contains("version=\"1.0\""));
        assertTrue(withDecl.xmlDeclaration().contains("encoding=\"UTF-8\""));

        // Test with root element
        Document withRoot = Document.of().root(new Element("root"));
        assertNotNull(withRoot.root());
        assertEquals("root", withRoot.root().name());

        // Test minimal document
        Document minimal = Document.minimal("simple");
        assertNotNull(minimal.root());
        assertEquals("simple", minimal.root().name());
        assertTrue(minimal.xmlDeclaration().isEmpty());
    }

    @Test
    void testDocumentsBuilder() {
        Document doc = Document.of()
                .version("1.1")
                .encoding("ISO-8859-1")
                .standalone(true)
                .doctype("<!DOCTYPE html>")
                .root(new Element("html"))
                .withXmlDeclaration();

        assertEquals("1.1", doc.version());
        assertEquals("ISO-8859-1", doc.encoding());
        assertTrue(doc.isStandalone());
        assertEquals("<!DOCTYPE html>", doc.doctype());
        assertNotNull(doc.root());
        assertEquals("html", doc.root().name());
        assertTrue(doc.xmlDeclaration().contains("standalone=\"yes\""));
    }

    @Test
    void testEditorFluentBuilder() {
        editor.createDocument("root");
        Element root = editor.root().orElseThrow();

        // Test element builder
        Element element = editor.add()
                .element("test-element")
                .to(root)
                .withText("element content")
                .withAttribute("id", "123")
                .withAttribute("class", "test")
                .build();

        assertEquals("test-element", element.name());
        assertEquals("element content", element.textContent());
        assertEquals("123", element.attribute("id"));
        assertEquals("test", element.attribute("class"));
        assertEquals(root, element.parent());

        // Test comment builder
        Comment comment =
                editor.add().comment().to(root).withContent(" Test comment ").build();

        assertEquals(" Test comment ", comment.content());
        assertEquals(root, comment.parent());

        // Test text builder
        Text text =
                editor.add().text().to(element).withContent("additional text").build();

        assertEquals("additional text", text.content());
        assertFalse(text.cdata());

        // Test CDATA builder
        Text cdata = editor.add()
                .text()
                .to(element)
                .withContent("cdata content")
                .asCData()
                .build();

        assertEquals("cdata content", cdata.content());
        assertTrue(cdata.cdata());
    }

    @Test
    void testEditorBuilderRequiresParent() {
        editor.createDocument("root");

        // Element builder should require parent
        assertThrows(IllegalStateException.class, () -> {
            try {
                editor.add().element("test").build();
            } catch (InvalidXmlException e) {
                throw new RuntimeException(e);
            }
        });

        // Comment builder should require parent
        assertThrows(IllegalStateException.class, () -> {
            try {
                editor.add().comment().withContent("test").build();
            } catch (InvalidXmlException e) {
                throw new RuntimeException(e);
            }
        });

        // Text builder should require parent
        assertThrows(IllegalStateException.class, () -> {
            editor.add().text().withContent("test").build();
        });
    }

    @Test
    void testEditorBuilderSelfClosing() {
        editor.createDocument("root");
        Element root = editor.root().orElseThrow();

        Element selfClosing = editor.add()
                .element("self-closing")
                .to(root)
                .withAttribute("attr", "value")
                .selfClosing()
                .build();

        assertTrue(selfClosing.selfClosing());
        assertEquals("value", selfClosing.attribute("attr"));
    }

    @Test
    void testEditorBuilderWithMultipleAttributes() {
        editor.createDocument("root");
        Element root = editor.root().orElseThrow();

        Map<String, String> attributes = Map.of(
                "id", "test-id",
                "class", "test-class",
                "data-value", "test-data");

        Element element = editor.add()
                .element("multi-attr")
                .to(root)
                .withAttributes(attributes)
                .withAttribute("extra", "extra-value")
                .build();

        assertEquals("test-id", element.attribute("id"));
        assertEquals("test-class", element.attribute("class"));
        assertEquals("test-data", element.attribute("data-value"));
        assertEquals("extra-value", element.attribute("extra"));
    }

    @Test
    void testBuilderIntegrationWithSerialization() {
        // Create a complex document using builders
        Document doc = Document.of()
                .version("1.0")
                .encoding("UTF-8")
                .root(new Element("project"))
                .withXmlDeclaration();

        Element root = doc.root();

        // Add dependencies using element builder
        Element dependencies = Element.of("dependencies");
        root.addNode(dependencies);

        Element dependency = Element.of("dependency").attribute("scope", "test").attribute("optional", "true");
        dependencies.addNode(dependency);

        dependency.addNode(Element.text("groupId", "junit"));
        dependency.addNode(Element.text("artifactId", "junit"));
        dependency.addNode(Element.text("version", "4.13.2"));

        // Serialize and verify
        String xml = doc.toXml();
        assertTrue(xml.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(xml.contains("<project>"));
        assertTrue(xml.contains("<dependencies>"));
        assertTrue(xml.contains("scope=\"test\""));
        assertTrue(xml.contains("optional=\"true\""));
        assertTrue(xml.contains("<groupId>junit</groupId>"));
        assertTrue(xml.contains("<artifactId>junit</artifactId>"));
        assertTrue(xml.contains("<version>4.13.2</version>"));
    }
}
