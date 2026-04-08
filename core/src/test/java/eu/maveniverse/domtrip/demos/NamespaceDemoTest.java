package eu.maveniverse.domtrip.demos;

import static org.junit.jupiter.api.Assertions.*;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import eu.maveniverse.domtrip.NamespaceContext;
import eu.maveniverse.domtrip.QName;
import org.junit.jupiter.api.Test;

/**
 * Demonstrates the comprehensive namespace handling features in DomTrip.
 */
class NamespaceDemoTest {

    @Test
    void demonstrateNamespaceHandling() throws DomTripException {
        verifyBasicNamespaceCreation();
        verifyNamespaceResolution();
        verifyNamespaceNavigation();
        verifyNamespaceContext();
        verifyComplexNamespaceDocument();
    }

    private static void verifyBasicNamespaceCreation() throws DomTripException {
        // Create elements with different namespace patterns using QName
        Element defaultNs = Element.of(QName.of("http://example.com/default", "root"));
        assertNotNull(defaultNs);

        Element prefixedNs = Element.of(QName.of("http://example.com/ns", "element", "ex"));
        assertNotNull(prefixedNs);

        Element withPreferredPrefix = Element.of(QName.of("http://example.com/api", "data", "api"));
        assertNotNull(withPreferredPrefix);

        Element textInNs = Element.text(QName.of("http://example.com/content", "title"), "My Title");
        assertNotNull(textInNs);
        assertEquals("My Title", textInNs.textContent());
    }

    private static void verifyNamespaceResolution() throws DomTripException {
        // Create a document with nested namespaces
        String xml = """
            <root xmlns="http://example.com/default" xmlns:ns1="http://example.com/ns1">
                <child>
                    <ns1:element xmlns:ns2="http://example.com/ns2">
                        <ns2:data>content</ns2:data>
                        <local>local content</local>
                    </ns1:element>
                </child>
            </root>
            """;

        Editor editor = new Editor(Document.of(xml));
        Element root = editor.root();

        // Demonstrate namespace-aware methods
        assertEquals("root", root.name());
        assertEquals("root", root.localName());
        assertNull(root.prefix());
        assertEquals("http://example.com/default", root.namespaceURI());
        assertTrue(root.inNamespace("http://example.com/default"));

        // Find namespaced elements using QName
        assertTrue(root.childElement(QName.of("http://example.com/default", "child"))
                .isPresent());

        root.childElement(QName.of("http://example.com/default", "child")).ifPresent(child -> {
            assertTrue(child.childElement(QName.of("http://example.com/ns1", "element"))
                    .isPresent());

            child.childElement(QName.of("http://example.com/ns1", "element")).ifPresent(ns1Element -> {
                assertEquals("ns1", ns1Element.prefix());
                assertEquals("element", ns1Element.localName());
                assertEquals("http://example.com/ns1", ns1Element.namespaceURI());

                assertTrue(ns1Element
                        .childElement(QName.of("http://example.com/ns2", "data"))
                        .isPresent());
                ns1Element
                        .childElement(QName.of("http://example.com/ns2", "data"))
                        .ifPresent(ns2Data -> {
                            assertEquals("content", ns2Data.textContent());
                        });
            });
        });
    }

    private static void verifyNamespaceNavigation() throws DomTripException {
        // Create a document with multiple elements in different namespaces
        Element root = Element.of("document")
                .namespaceDeclaration(null, "http://example.com/doc")
                .namespaceDeclaration("meta", "http://example.com/metadata")
                .namespaceDeclaration("content", "http://example.com/content");

        // Add children in different namespaces using QName
        root.addChild(Element.text(QName.of("http://example.com/metadata", "title", "meta"), "Document Title"));
        root.addChild(Element.text(QName.of("http://example.com/metadata", "author", "meta"), "John Doe"));
        root.addChild(Element.text(QName.of("http://example.com/doc", "summary"), "Document summary"));
        root.addChild(Element.text(QName.of("http://example.com/content", "section", "content"), "Section 1"));
        root.addChild(Element.text(QName.of("http://example.com/content", "section", "content"), "Section 2"));

        String xmlOutput = root.toXml();
        assertNotNull(xmlOutput);

        // Navigate using namespace-aware methods with QName
        assertEquals(
                1,
                root.childElements(QName.of("http://example.com/metadata", "title"))
                        .count());
        assertEquals(
                1,
                root.childElements(QName.of("http://example.com/metadata", "author"))
                        .count());
        assertEquals(
                2,
                root.childElements(QName.of("http://example.com/content", "section"))
                        .count());
        assertEquals(
                1,
                root.childElements(QName.of("http://example.com/doc", "summary"))
                        .count());
    }

    private static void verifyNamespaceContext() throws DomTripException {
        String xml = """
            <root xmlns="http://example.com/default"
                  xmlns:a="http://example.com/a"
                  xmlns:b="http://example.com/b">
                <child xmlns:c="http://example.com/c">
                    <a:element xmlns:d="http://example.com/d">
                        <b:data>content</b:data>
                    </a:element>
                </child>
            </root>
            """;

        Editor editor = new Editor(Document.of(xml));
        Element root = editor.root();

        // Get namespace context at root level
        NamespaceContext rootContext = root.namespaceContext();
        assertNotNull(rootContext);
        assertEquals("http://example.com/default", rootContext.defaultNamespaceURI());
        assertNotNull(rootContext.declaredPrefixes());
        assertNotNull(rootContext.declaredNamespaceURIs());

        root.childElement("child").ifPresent(child -> {
            NamespaceContext childContext = child.namespaceContext();
            assertNotNull(childContext);
            assertEquals("http://example.com/c", childContext.namespaceURI("c"));
            assertNotNull(childContext.prefix("http://example.com/a"));
        });

        // Demonstrate namespace resolution
        root.descendants()
                .filter(el -> "data".equals(el.localName()))
                .findFirst()
                .ifPresent(dataElement -> {
                    assertEquals("http://example.com/b", dataElement.namespaceURI());

                    NamespaceContext context = dataElement.namespaceContext();
                    assertNotNull(context.namespaceURI("a"));
                    assertNotNull(context.namespaceURI("c"));
                    assertNotNull(context.namespaceURI("d"));
                });
    }

    private static void verifyComplexNamespaceDocument() throws DomTripException {
        // Create a complex document using builder pattern with namespaces
        Element soapEnvelope = Element.of("Envelope")
                .namespaceDeclaration("soap", "http://schemas.xmlsoap.org/soap/envelope/")
                .namespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance")
                .namespaceDeclaration("xsd", "http://www.w3.org/2001/XMLSchema");

        // Set the element name to include namespace prefix
        soapEnvelope.name("soap:Envelope");

        // Create header using QName
        Element header = Element.of(QName.of("http://schemas.xmlsoap.org/soap/envelope/", "Header", "soap"));
        soapEnvelope.addChild(header);

        // Create body using QName
        Element body = Element.of(QName.of("http://schemas.xmlsoap.org/soap/envelope/", "Body", "soap"));

        // Add a custom method call in body using QName
        Element methodCall = Element.of("GetUserInfo").namespaceDeclaration(null, "http://example.com/userservice");
        methodCall.addChild(Element.text(QName.of("http://example.com/userservice", "userId"), "12345"));
        methodCall.addChild(Element.text(QName.of("http://example.com/userservice", "includeDetails"), "true"));

        body.addChild(methodCall);
        soapEnvelope.addChild(body);

        String result = soapEnvelope.toXml();
        assertNotNull(result);
        assertTrue(result.contains("soap:Envelope"));
        assertTrue(result.contains("GetUserInfo"));

        // Demonstrate namespace-aware querying with QName
        assertTrue(soapEnvelope
                .descendants(QName.of("http://schemas.xmlsoap.org/soap/envelope/", "Body"))
                .findFirst()
                .isPresent());

        soapEnvelope
                .descendants(QName.of("http://schemas.xmlsoap.org/soap/envelope/", "Body"))
                .findFirst()
                .ifPresent(soapBody -> {
                    assertTrue(soapBody.childElement(QName.of("http://example.com/userservice", "GetUserInfo"))
                            .isPresent());
                    soapBody.childElement(QName.of("http://example.com/userservice", "GetUserInfo"))
                            .ifPresent(method -> {
                                assertEquals("GetUserInfo", method.localName());
                                method.childElement(QName.of("http://example.com/userservice", "userId"))
                                        .ifPresent(userId -> assertEquals("12345", userId.textContent()));
                            });
                });
    }
}
