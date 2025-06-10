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
        Element defaultNs = Element.element(QName.of("http://example.com/default", "root"));
        assertEquals("root", defaultNs.name());
        assertEquals("root", defaultNs.localName());
        assertNull(defaultNs.prefix());
        assertEquals("http://example.com/default", defaultNs.attribute("xmlns"));

        Element prefixed = Element.element(QName.of("http://example.com/ns", "element", "ex"));
        assertEquals("ex:element", prefixed.name());
        assertEquals("element", prefixed.localName());
        assertEquals("ex", prefixed.prefix());
        assertEquals("http://example.com/ns", prefixed.attribute("xmlns:ex"));
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
        Element root = editor.documentElement().orElseThrow();

        assertEquals("root", root.localName());
        assertNull(root.prefix());
        assertEquals("http://example.com/default", root.namespaceURI());
        assertTrue(root.inNamespace("http://example.com/default"));

        Element child = root.child("child").orElse(null);
        assertNotNull(child);
        assertEquals("child", child.localName());
        assertNull(child.prefix());
        assertEquals("http://example.com/default", child.namespaceURI());

        Element nsElement = root.child("ns:element").orElse(null);
        assertNotNull(nsElement);
        assertEquals("element", nsElement.localName());
        assertEquals("ns", nsElement.prefix());
        assertEquals("http://example.com/ns", nsElement.namespaceURI());
        assertTrue(nsElement.inNamespace("http://example.com/ns"));
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
        Element root = editor.documentElement().orElseThrow();

        // Find by namespace and local name using QName
        List<Element> defaultTitles = root.descendants(QName.of("http://example.com/default", "title"))
                .collect(Collectors.toList());
        assertEquals(2, defaultTitles.size());
        assertEquals("Default Title", defaultTitles.get(0).textContent());
        assertEquals("Nested Title", defaultTitles.get(1).textContent());

        List<Element> metaTitles =
                root.descendants(QName.of("http://example.com/meta", "title")).collect(Collectors.toList());
        assertEquals(2, metaTitles.size());
        assertEquals("Meta Title", metaTitles.get(0).textContent());
        assertEquals("Nested Meta Title", metaTitles.get(1).textContent());

        // Find direct children by namespace using QName
        root.child(QName.of("http://example.com/default", "title"))
                .ifPresent(title -> assertEquals("Default Title", title.textContent()));

        root.child(QName.of("http://example.com/meta", "title"))
                .ifPresent(title -> assertEquals("Meta Title", title.textContent()));
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
        Element root = editor.documentElement().orElseThrow();

        NamespaceContext rootContext = root.namespaceContext();
        assertEquals("http://example.com/default", rootContext.defaultNamespaceURI());
        assertEquals("http://example.com/a", rootContext.namespaceURI("a"));
        assertNull(rootContext.namespaceURI("b"));
        assertTrue(rootContext.isPrefixDeclared("a"));
        assertFalse(rootContext.isPrefixDeclared("b"));

        Element child = root.child("child").orElse(null);
        assertNotNull(child);
        NamespaceContext childContext = child.namespaceContext();
        assertEquals("http://example.com/default", childContext.defaultNamespaceURI());
        assertEquals("http://example.com/a", childContext.namespaceURI("a"));
        assertEquals("http://example.com/b", childContext.namespaceURI("b"));

        Element dataElement = child.descendants()
                .filter(el -> "data".equals(el.localName()))
                .findFirst()
                .orElse(null);
        assertNotNull(dataElement);
        NamespaceContext dataContext = dataElement.namespaceContext();
        assertEquals("http://example.com/a", dataContext.namespaceURI("a"));
        assertEquals("http://example.com/b", dataContext.namespaceURI("b"));
        assertEquals("http://example.com/c", dataContext.namespaceURI("c"));
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
        assertEquals("http://www.w3.org/XML/1998/namespace", context.namespaceURI("xml"));
        assertEquals("http://www.w3.org/2000/xmlns/", context.namespaceURI("xmlns"));
        assertEquals("xml", context.prefix("http://www.w3.org/XML/1998/namespace"));
        assertEquals("xmlns", context.prefix("http://www.w3.org/2000/xmlns/"));
    }

    @Test
    void testNamespaceDeclarationMethods() {
        Element element = new Element("test");

        // Test setting namespace declarations
        element.setNamespaceDeclaration("ex", "http://example.com/ns");
        assertEquals("http://example.com/ns", element.attribute("xmlns:ex"));
        assertEquals("http://example.com/ns", element.namespaceDeclaration("ex"));

        element.setNamespaceDeclaration(null, "http://example.com/default");
        assertEquals("http://example.com/default", element.attribute("xmlns"));
        assertEquals("http://example.com/default", element.namespaceDeclaration(null));

        // Test removing namespace declarations
        element.removeNamespaceDeclaration("ex");
        assertNull(element.attribute("xmlns:ex"));
        assertNull(element.namespaceDeclaration("ex"));

        element.removeNamespaceDeclaration(null);
        assertNull(element.attribute("xmlns"));
        assertNull(element.namespaceDeclaration(null));
    }

    @Test
    void testElementsBuilderWithNamespaces() {
        Element element = Element.builder("test")
                .withNamespace("ex", "http://example.com/ns")
                .withDefaultNamespace("http://example.com/default")
                .withText("content")
                .build();

        assertEquals("http://example.com/ns", element.attribute("xmlns:ex"));
        assertEquals("http://example.com/default", element.attribute("xmlns"));
        assertEquals("content", element.textContent());
    }

    @Test
    void testNamespaceFactoryMethods() {
        Element textInNs = Element.text(QName.of("http://example.com/ns", "title"), "My Title");
        assertEquals("title", textInNs.name());
        assertEquals("http://example.com/ns", textInNs.attribute("xmlns"));
        assertEquals("My Title", textInNs.textContent());

        Element namespacedText = Element.text(QName.of("http://example.com/ns", "title", "ex"), "My Title");
        assertEquals("ex:title", namespacedText.name());
        assertEquals("http://example.com/ns", namespacedText.attribute("xmlns:ex"));
        assertEquals("My Title", namespacedText.textContent());

        Element withPreferred = Element.element(QName.of("http://example.com/api", "data", "api"));
        assertEquals("api:data", withPreferred.name());
        assertEquals("http://example.com/api", withPreferred.attribute("xmlns:api"));

        Element withoutPrefix = Element.element(QName.of("http://example.com/api", "data"));
        assertEquals("data", withoutPrefix.name());
        assertEquals("http://example.com/api", withoutPrefix.attribute("xmlns"));
    }
}
