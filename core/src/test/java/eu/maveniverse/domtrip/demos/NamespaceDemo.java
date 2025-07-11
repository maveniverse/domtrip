package eu.maveniverse.domtrip.demos;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import eu.maveniverse.domtrip.NamespaceContext;
import eu.maveniverse.domtrip.QName;

/**
 * Demonstrates the comprehensive namespace handling features in DomTrip.
 */
public class NamespaceDemo {

    public static void main(String[] args) {
        System.out.println("=== DomTrip Namespace Handling Demo ===\n");

        demonstrateBasicNamespaceCreation();
        demonstrateNamespaceResolution();
        demonstrateNamespaceNavigation();
        demonstrateNamespaceContext();
        demonstrateComplexNamespaceDocument();

        System.out.println("\n=== Demo Complete ===");
    }

    private static void demonstrateBasicNamespaceCreation() {
        System.out.println("1. Basic Namespace Creation:");

        // Create elements with different namespace patterns using QName
        Element defaultNs = Element.of(QName.of("http://example.com/default", "root"));
        Element prefixedNs = Element.of(QName.of("http://example.com/ns", "element", "ex"));
        Element withPreferredPrefix = Element.of(QName.of("http://example.com/api", "data", "api"));
        Element textInNs = Element.text(QName.of("http://example.com/content", "title"), "My Title");

        System.out.println("Default namespace element: " + defaultNs.toXml());
        System.out.println("Prefixed namespace element: " + prefixedNs.toXml());
        System.out.println("With preferred prefix: " + withPreferredPrefix.toXml());
        System.out.println("Text in namespace: " + textInNs.toXml());
        System.out.println();
    }

    private static void demonstrateNamespaceResolution() {
        System.out.println("2. Namespace Resolution:");

        // Create a document with nested namespaces
        String xml =
                """
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
        System.out.println("Root element:");
        System.out.println("  Name: " + root.name());
        System.out.println("  Local name: " + root.localName());
        System.out.println("  Prefix: " + root.prefix());
        System.out.println("  Namespace URI: " + root.namespaceURI());
        System.out.println("  Is in default namespace: " + root.inNamespace("http://example.com/default"));

        // Find namespaced elements using QName
        root.child(QName.of("http://example.com/default", "child")).ifPresent(child -> {
            System.out.println("\nFound child in default namespace: " + child.name());

            child.child(QName.of("http://example.com/ns1", "element")).ifPresent(ns1Element -> {
                System.out.println("Found ns1:element: " + ns1Element.name());
                System.out.println("  Prefix: " + ns1Element.prefix());
                System.out.println("  Local name: " + ns1Element.localName());
                System.out.println("  Namespace URI: " + ns1Element.namespaceURI());

                ns1Element.child(QName.of("http://example.com/ns2", "data")).ifPresent(ns2Data -> {
                    System.out.println("Found ns2:data: " + ns2Data.name());
                    System.out.println("  Content: " + ns2Data.textContent());
                });
            });
        });
        System.out.println();
    }

    private static void demonstrateNamespaceNavigation() {
        System.out.println("3. Namespace-Aware Navigation:");

        // Create a document with multiple elements in different namespaces
        Element root = Element.of("document")
                .namespaceDeclaration(null, "http://example.com/doc")
                .namespaceDeclaration("meta", "http://example.com/metadata")
                .namespaceDeclaration("content", "http://example.com/content");

        // Add children in different namespaces using QName
        root.addNode(Element.text(QName.of("http://example.com/metadata", "title", "meta"), "Document Title"));
        root.addNode(Element.text(QName.of("http://example.com/metadata", "author", "meta"), "John Doe"));
        root.addNode(Element.text(QName.of("http://example.com/doc", "summary"), "Document summary"));
        root.addNode(Element.text(QName.of("http://example.com/content", "section", "content"), "Section 1"));
        root.addNode(Element.text(QName.of("http://example.com/content", "section", "content"), "Section 2"));

        System.out.println("Created document:\n" + root.toXml());

        // Navigate using namespace-aware methods with QName
        System.out.println("Metadata elements:");
        root.children(QName.of("http://example.com/metadata", "title"))
                .forEach(title -> System.out.println("  Title: " + title.textContent()));
        root.children(QName.of("http://example.com/metadata", "author"))
                .forEach(author -> System.out.println("  Author: " + author.textContent()));

        System.out.println("Content sections:");
        root.children(QName.of("http://example.com/content", "section"))
                .forEach(section -> System.out.println("  Section: " + section.textContent()));

        System.out.println("Default namespace elements:");
        root.children(QName.of("http://example.com/doc", "summary"))
                .forEach(summary -> System.out.println("  Summary: " + summary.textContent()));
        System.out.println();
    }

    private static void demonstrateNamespaceContext() {
        System.out.println("4. Namespace Context:");

        String xml =
                """
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

        // Get namespace context at different levels
        NamespaceContext rootContext = root.namespaceContext();
        System.out.println("Root namespace context: " + rootContext);
        System.out.println("  Default namespace: " + rootContext.defaultNamespaceURI());
        System.out.println("  Declared prefixes: " + rootContext.declaredPrefixes());
        System.out.println("  Declared URIs: " + rootContext.declaredNamespaceURIs());

        root.child("child").ifPresent(child -> {
            NamespaceContext childContext = child.namespaceContext();
            System.out.println("\nChild namespace context: " + childContext);
            System.out.println("  Prefix 'c' maps to: " + childContext.namespaceURI("c"));
            System.out.println(
                    "  URI 'http://example.com/a' has prefix: " + childContext.prefix("http://example.com/a"));
        });

        // Demonstrate namespace resolution
        root.descendants()
                .filter(el -> "data".equals(el.localName()))
                .findFirst()
                .ifPresent(dataElement -> {
                    System.out.println("\nData element namespace resolution:");
                    System.out.println("  Element: " + dataElement.name());
                    System.out.println("  Resolved namespace URI: " + dataElement.namespaceURI());

                    NamespaceContext context = dataElement.namespaceContext();
                    System.out.println("  Available prefixes: " + context.declaredPrefixes());
                    System.out.println("  Can resolve 'a': " + (context.namespaceURI("a") != null));
                    System.out.println("  Can resolve 'c': " + (context.namespaceURI("c") != null));
                    System.out.println("  Can resolve 'd': " + (context.namespaceURI("d") != null));
                });
        System.out.println();
    }

    private static void demonstrateComplexNamespaceDocument() {
        System.out.println("5. Complex Namespace Document Creation:");

        // Create a complex document using builder pattern with namespaces
        Element soapEnvelope = Element.of("Envelope")
                .namespaceDeclaration("soap", "http://schemas.xmlsoap.org/soap/envelope/")
                .namespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance")
                .namespaceDeclaration("xsd", "http://www.w3.org/2001/XMLSchema");

        // Set the element name to include namespace prefix
        soapEnvelope.name("soap:Envelope");

        // Create header using QName
        Element header = Element.of(QName.of("http://schemas.xmlsoap.org/soap/envelope/", "Header", "soap"));
        soapEnvelope.addNode(header);

        // Create body using QName
        Element body = Element.of(QName.of("http://schemas.xmlsoap.org/soap/envelope/", "Body", "soap"));

        // Add a custom method call in body using QName
        Element methodCall = Element.of("GetUserInfo").namespaceDeclaration(null, "http://example.com/userservice");
        methodCall.addNode(Element.text(QName.of("http://example.com/userservice", "userId"), "12345"));
        methodCall.addNode(Element.text(QName.of("http://example.com/userservice", "includeDetails"), "true"));

        body.addNode(methodCall);
        soapEnvelope.addNode(body);

        System.out.println("SOAP envelope with namespaces:");
        System.out.println(soapEnvelope.toXml());

        // Demonstrate namespace-aware querying with QName
        System.out.println("\nNamespace-aware querying:");
        soapEnvelope
                .descendants(QName.of("http://schemas.xmlsoap.org/soap/envelope/", "Body"))
                .findFirst()
                .ifPresent(soapBody -> {
                    System.out.println("Found SOAP Body");
                    soapBody.child(QName.of("http://example.com/userservice", "GetUserInfo"))
                            .ifPresent(method -> {
                                System.out.println("Found method: " + method.localName());
                                method.child(QName.of("http://example.com/userservice", "userId"))
                                        .ifPresent(userId -> System.out.println("  User ID: " + userId.textContent()));
                            });
                });
        System.out.println();
    }
}
