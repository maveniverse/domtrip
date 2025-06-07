package eu.maveniverse.domtrip;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

/**
 * Test cases for the new Builder API functionality.
 */
public class BuilderApiTest {
    
    private Editor editor;
    
    @BeforeEach
    void setUp() {
        editor = new Editor();
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
        
        assertEquals("test-attr", attr.getName());
        assertEquals("test-value", attr.getValue());
        assertEquals(QuoteStyle.SINGLE, attr.getQuoteStyle());
        assertEquals("  ", attr.getPrecedingWhitespace());
        assertEquals("&lt;raw&gt;", attr.getRawValue());
    }
    
    @Test
    void testAttributeBuilderMinimal() {
        Attribute attr = Attribute.builder()
            .name("simple")
            .value("value")
            .build();
        
        assertEquals("simple", attr.getName());
        assertEquals("value", attr.getValue());
        assertEquals(QuoteStyle.DOUBLE, attr.getQuoteStyle()); // default
        assertEquals(" ", attr.getPrecedingWhitespace()); // default
    }
    
    @Test
    void testAttributeBuilderRequiresName() {
        assertThrows(IllegalStateException.class, () -> {
            Attribute.builder()
                .value("value")
                .build();
        });
    }
    
    @Test
    void testElementsFactory() {
        // Test text element
        Element textEl = Elements.textElement("name", "content");
        assertEquals("name", textEl.getName());
        assertEquals("content", textEl.getTextContent());
        
        // Test empty element
        Element emptyEl = Elements.emptyElement("empty");
        assertEquals("empty", emptyEl.getName());
        assertEquals(0, emptyEl.getChildCount());
        
        // Test self-closing element
        Element selfClosing = Elements.selfClosingElement("self");
        assertEquals("self", selfClosing.getName());
        assertTrue(selfClosing.isSelfClosing());
        
        // Test element with attributes
        Map<String, String> attrs = Map.of("attr1", "val1", "attr2", "val2");
        Element withAttrs = Elements.elementWithAttributes("test", attrs);
        assertEquals("test", withAttrs.getName());
        assertEquals("val1", withAttrs.getAttribute("attr1"));
        assertEquals("val2", withAttrs.getAttribute("attr2"));
    }
    
    @Test
    void testElementsBuilder() {
        Element element = Elements.builder("complex")
            .withText("text content")
            .withAttribute("attr1", "value1")
            .withAttributes(Map.of("attr2", "value2", "attr3", "value3"))
            .withChild(Elements.textElement("child", "child content"))
            .withComment("This is a comment")
            .build();
        
        assertEquals("complex", element.getName());
        assertEquals("value1", element.getAttribute("attr1"));
        assertEquals("value2", element.getAttribute("attr2"));
        assertEquals("value3", element.getAttribute("attr3"));
        assertTrue(element.hasChildElements());
        assertTrue(element.hasTextContent());
    }
    
    @Test
    void testDocumentsFactory() {
        // Test empty document
        Document empty = Documents.empty();
        assertNotNull(empty);
        assertNull(empty.getDocumentElement());
        
        // Test with XML declaration
        Document withDecl = Documents.withXmlDeclaration("1.0", "UTF-8");
        assertEquals("1.0", withDecl.getVersion());
        assertEquals("UTF-8", withDecl.getEncoding());
        assertTrue(withDecl.getXmlDeclaration().contains("version=\"1.0\""));
        assertTrue(withDecl.getXmlDeclaration().contains("encoding=\"UTF-8\""));
        
        // Test with root element
        Document withRoot = Documents.withRootElement("root");
        assertNotNull(withRoot.getDocumentElement());
        assertEquals("root", withRoot.getDocumentElement().getName());
        
        // Test minimal document
        Document minimal = Documents.minimal("simple");
        assertNotNull(minimal.getDocumentElement());
        assertEquals("simple", minimal.getDocumentElement().getName());
        assertTrue(minimal.getXmlDeclaration().isEmpty());
    }
    
    @Test
    void testDocumentsBuilder() {
        Document doc = Documents.builder()
            .withVersion("1.1")
            .withEncoding("ISO-8859-1")
            .withStandalone(true)
            .withDoctype("<!DOCTYPE html>")
            .withRootElement("html")
            .withXmlDeclaration()
            .build();
        
        assertEquals("1.1", doc.getVersion());
        assertEquals("ISO-8859-1", doc.getEncoding());
        assertTrue(doc.isStandalone());
        assertEquals("<!DOCTYPE html>", doc.getDoctype());
        assertNotNull(doc.getDocumentElement());
        assertEquals("html", doc.getDocumentElement().getName());
        assertTrue(doc.getXmlDeclaration().contains("standalone=\"yes\""));
    }
    
    @Test
    void testEditorFluentBuilder() {
        editor.createDocument("root");
        Element root = editor.getRootElement();
        
        // Test element builder
        Element element = editor.add().element("test-element")
            .to(root)
            .withText("element content")
            .withAttribute("id", "123")
            .withAttribute("class", "test")
            .build();
        
        assertEquals("test-element", element.getName());
        assertEquals("element content", element.getTextContent());
        assertEquals("123", element.getAttribute("id"));
        assertEquals("test", element.getAttribute("class"));
        assertEquals(root, element.getParent());
        
        // Test comment builder
        Comment comment = editor.add().comment()
            .to(root)
            .withContent(" Test comment ")
            .build();
        
        assertEquals(" Test comment ", comment.getContent());
        assertEquals(root, comment.getParent());
        
        // Test text builder
        Text text = editor.add().text()
            .to(element)
            .withContent("additional text")
            .build();
        
        assertEquals("additional text", text.getContent());
        assertFalse(text.isCData());
        
        // Test CDATA builder
        Text cdata = editor.add().text()
            .to(element)
            .withContent("cdata content")
            .asCData()
            .build();
        
        assertEquals("cdata content", cdata.getContent());
        assertTrue(cdata.isCData());
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
        Element root = editor.getRootElement();
        
        Element selfClosing = editor.add().element("self-closing")
            .to(root)
            .withAttribute("attr", "value")
            .selfClosing()
            .build();
        
        assertTrue(selfClosing.isSelfClosing());
        assertEquals("value", selfClosing.getAttribute("attr"));
    }
    
    @Test
    void testEditorBuilderWithMultipleAttributes() {
        editor.createDocument("root");
        Element root = editor.getRootElement();
        
        Map<String, String> attributes = Map.of(
            "id", "test-id",
            "class", "test-class",
            "data-value", "test-data"
        );
        
        Element element = editor.add().element("multi-attr")
            .to(root)
            .withAttributes(attributes)
            .withAttribute("extra", "extra-value")
            .build();
        
        assertEquals("test-id", element.getAttribute("id"));
        assertEquals("test-class", element.getAttribute("class"));
        assertEquals("test-data", element.getAttribute("data-value"));
        assertEquals("extra-value", element.getAttribute("extra"));
    }
    
    @Test
    void testBuilderIntegrationWithSerialization() {
        // Create a complex document using builders
        Document doc = Documents.builder()
            .withVersion("1.0")
            .withEncoding("UTF-8")
            .withRootElement("project")
            .withXmlDeclaration()
            .build();
        
        Element root = doc.getDocumentElement();
        
        // Add dependencies using element builder
        Element dependencies = Elements.builder("dependencies")
            .build();
        root.addChild(dependencies);
        
        Element dependency = Elements.builder("dependency")
            .withAttributes(Map.of("scope", "test", "optional", "true"))
            .build();
        dependencies.addChild(dependency);
        
        dependency.addChild(Elements.textElement("groupId", "junit"));
        dependency.addChild(Elements.textElement("artifactId", "junit"));
        dependency.addChild(Elements.textElement("version", "4.13.2"));
        
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
