package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test cases for namespace handling functionality.
 */
public class NamespaceTest {

    private Editor editor;

    @BeforeEach
    void setUp() {
        editor = new Editor();
    }

    @Test
    void testBasicNamespaceCreation() {
        Element defaultNs = Elements.elementInNamespace("http://example.com/default", "root");
        assertEquals("root", defaultNs.getName());
        assertEquals("root", defaultNs.getLocalName());
        assertNull(defaultNs.getPrefix());
        assertEquals("http://example.com/default", defaultNs.getAttribute("xmlns"));

        Element prefixed = Elements.namespacedElement("ex", "element", "http://example.com/ns");
        assertEquals("ex:element", prefixed.getName());
        assertEquals("element", prefixed.getLocalName());
        assertEquals("ex", prefixed.getPrefix());
        assertEquals("http://example.com/ns", prefixed.getAttribute("xmlns:ex"));
    }

    @Test
    void testNamespaceResolution() {
        String xml =
                """
            <root xmlns="http://example.com/default" xmlns:ns="http://example.com/ns">
                <child>content</child>
                <ns:element>namespaced content</ns:element>
            </root>
            """;

        editor.loadXml(xml);
        Element root = editor.getRootElement();

        assertEquals("root", root.getLocalName());
        assertNull(root.getPrefix());
        assertEquals("http://example.com/default", root.getNamespaceURI());
        assertTrue(root.isInNamespace("http://example.com/default"));

        Element child = root.findChild("child").orElse(null);
        assertNotNull(child);
        assertEquals("child", child.getLocalName());
        assertNull(child.getPrefix());
        assertEquals("http://example.com/default", child.getNamespaceURI());

        Element nsElement = root.findChild("ns:element").orElse(null);
        assertNotNull(nsElement);
        assertEquals("element", nsElement.getLocalName());
        assertEquals("ns", nsElement.getPrefix());
        assertEquals("http://example.com/ns", nsElement.getNamespaceURI());
        assertTrue(nsElement.isInNamespace("http://example.com/ns"));
    }

    @Test
    void testNamespaceAwareNavigation() {
        String xml =
                """
            <root xmlns="http://example.com/default" xmlns:meta="http://example.com/meta">
                <title>Default Title</title>
                <meta:title>Meta Title</meta:title>
                <content>
                    <title>Nested Title</title>
                    <meta:title>Nested Meta Title</meta:title>
                </content>
            </root>
            """;

        editor.loadXml(xml);
        Element root = editor.getRootElement();

        // Find by namespace and local name
        List<Element> defaultTitles = root.descendantsByNamespace("http://example.com/default", "title")
                .collect(Collectors.toList());
        assertEquals(2, defaultTitles.size());
        assertEquals("Default Title", defaultTitles.get(0).getTextContent());
        assertEquals("Nested Title", defaultTitles.get(1).getTextContent());

        List<Element> metaTitles =
                root.descendantsByNamespace("http://example.com/meta", "title").collect(Collectors.toList());
        assertEquals(2, metaTitles.size());
        assertEquals("Meta Title", metaTitles.get(0).getTextContent());
        assertEquals("Nested Meta Title", metaTitles.get(1).getTextContent());

        // Find direct children by namespace
        root.findChildByNamespace("http://example.com/default", "title")
                .ifPresent(title -> assertEquals("Default Title", title.getTextContent()));

        root.findChildByNamespace("http://example.com/meta", "title")
                .ifPresent(title -> assertEquals("Meta Title", title.getTextContent()));
    }

    @Test
    void testNamespaceContext() {
        String xml =
                """
            <root xmlns="http://example.com/default" xmlns:a="http://example.com/a">
                <child xmlns:b="http://example.com/b">
                    <a:element xmlns:c="http://example.com/c">
                        <b:data>content</b:data>
                    </a:element>
                </child>
            </root>
            """;

        editor.loadXml(xml);
        Element root = editor.getRootElement();

        NamespaceContext rootContext = root.getNamespaceContext();
        assertEquals("http://example.com/default", rootContext.getDefaultNamespaceURI());
        assertEquals("http://example.com/a", rootContext.getNamespaceURI("a"));
        assertNull(rootContext.getNamespaceURI("b"));
        assertTrue(rootContext.isPrefixDeclared("a"));
        assertFalse(rootContext.isPrefixDeclared("b"));

        Element child = root.findChild("child").orElse(null);
        assertNotNull(child);
        NamespaceContext childContext = child.getNamespaceContext();
        assertEquals("http://example.com/default", childContext.getDefaultNamespaceURI());
        assertEquals("http://example.com/a", childContext.getNamespaceURI("a"));
        assertEquals("http://example.com/b", childContext.getNamespaceURI("b"));

        Element dataElement = child.descendants()
                .filter(el -> "data".equals(el.getLocalName()))
                .findFirst()
                .orElse(null);
        assertNotNull(dataElement);
        NamespaceContext dataContext = dataElement.getNamespaceContext();
        assertEquals("http://example.com/a", dataContext.getNamespaceURI("a"));
        assertEquals("http://example.com/b", dataContext.getNamespaceURI("b"));
        assertEquals("http://example.com/c", dataContext.getNamespaceURI("c"));
    }

    @Test
    void testNamespaceResolver() {
        // Test qualified name splitting
        String[] parts1 = NamespaceResolver.splitQualifiedName("prefix:localName");
        assertEquals("prefix", parts1[0]);
        assertEquals("localName", parts1[1]);

        String[] parts2 = NamespaceResolver.splitQualifiedName("localName");
        assertNull(parts2[0]);
        assertEquals("localName", parts2[1]);

        String[] parts3 = NamespaceResolver.splitQualifiedName("");
        assertNull(parts3[0]);
        assertEquals("", parts3[1]);

        // Test qualified name creation
        assertEquals("prefix:localName", NamespaceResolver.createQualifiedName("prefix", "localName"));
        assertEquals("localName", NamespaceResolver.createQualifiedName(null, "localName"));
        assertEquals("localName", NamespaceResolver.createQualifiedName("", "localName"));
    }

    @Test
    void testBuiltInNamespaces() {
        Element element = new Element("test");

        // Test built-in XML namespace
        assertEquals("http://www.w3.org/XML/1998/namespace", NamespaceResolver.resolveNamespaceURI(element, "xml"));

        // Test built-in XMLNS namespace
        assertEquals("http://www.w3.org/2000/xmlns/", NamespaceResolver.resolveNamespaceURI(element, "xmlns"));

        NamespaceContext context = new NamespaceContext();
        assertEquals("http://www.w3.org/XML/1998/namespace", context.getNamespaceURI("xml"));
        assertEquals("http://www.w3.org/2000/xmlns/", context.getNamespaceURI("xmlns"));
        assertEquals("xml", context.getPrefix("http://www.w3.org/XML/1998/namespace"));
        assertEquals("xmlns", context.getPrefix("http://www.w3.org/2000/xmlns/"));
    }

    @Test
    void testNamespaceDeclarationMethods() {
        Element element = new Element("test");

        // Test setting namespace declarations
        element.setNamespaceDeclaration("ex", "http://example.com/ns");
        assertEquals("http://example.com/ns", element.getAttribute("xmlns:ex"));
        assertEquals("http://example.com/ns", element.getNamespaceDeclaration("ex"));

        element.setNamespaceDeclaration(null, "http://example.com/default");
        assertEquals("http://example.com/default", element.getAttribute("xmlns"));
        assertEquals("http://example.com/default", element.getNamespaceDeclaration(null));

        // Test removing namespace declarations
        element.removeNamespaceDeclaration("ex");
        assertNull(element.getAttribute("xmlns:ex"));
        assertNull(element.getNamespaceDeclaration("ex"));

        element.removeNamespaceDeclaration(null);
        assertNull(element.getAttribute("xmlns"));
        assertNull(element.getNamespaceDeclaration(null));
    }

    @Test
    void testElementsBuilderWithNamespaces() {
        Element element = Elements.builder("test")
                .withNamespace("ex", "http://example.com/ns")
                .withDefaultNamespace("http://example.com/default")
                .withText("content")
                .build();

        assertEquals("http://example.com/ns", element.getAttribute("xmlns:ex"));
        assertEquals("http://example.com/default", element.getAttribute("xmlns"));
        assertEquals("content", element.getTextContent());
    }

    @Test
    void testNamespaceFactoryMethods() {
        Element textInNs = Elements.textElementInNamespace("http://example.com/ns", "title", "My Title");
        assertEquals("title", textInNs.getName());
        assertEquals("http://example.com/ns", textInNs.getAttribute("xmlns"));
        assertEquals("My Title", textInNs.getTextContent());

        Element namespacedText = Elements.namespacedTextElement("ex", "title", "http://example.com/ns", "My Title");
        assertEquals("ex:title", namespacedText.getName());
        assertEquals("http://example.com/ns", namespacedText.getAttribute("xmlns:ex"));
        assertEquals("My Title", namespacedText.getTextContent());

        Element withPreferred = Elements.elementWithNamespace("http://example.com/api", "data", "api");
        assertEquals("api:data", withPreferred.getName());
        assertEquals("http://example.com/api", withPreferred.getAttribute("xmlns:api"));

        Element withoutPrefix = Elements.elementWithNamespace("http://example.com/api", "data", null);
        assertEquals("data", withoutPrefix.getName());
        assertEquals("http://example.com/api", withoutPrefix.getAttribute("xmlns"));
    }
}
