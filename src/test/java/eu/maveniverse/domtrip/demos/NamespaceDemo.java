package eu.maveniverse.domtrip.demos;

import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import eu.maveniverse.domtrip.Elements;
import eu.maveniverse.domtrip.NamespaceContext;

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

        // Create elements with different namespace patterns
        Element defaultNs = Elements.elementInNamespace("http://example.com/default", "root");
        Element prefixedNs = Elements.namespacedElement("ex", "element", "http://example.com/ns");
        Element withPreferredPrefix = Elements.elementWithNamespace("http://example.com/api", "data", "api");
        Element textInNs = Elements.textElementInNamespace("http://example.com/content", "title", "My Title");

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

        Editor editor = new Editor(xml);
        Element root = editor.getRootElement();

        // Demonstrate namespace-aware methods
        System.out.println("Root element:");
        System.out.println("  Name: " + root.getName());
        System.out.println("  Local name: " + root.getLocalName());
        System.out.println("  Prefix: " + root.getPrefix());
        System.out.println("  Namespace URI: " + root.getNamespaceURI());
        System.out.println("  Is in default namespace: " + root.isInNamespace("http://example.com/default"));

        // Find namespaced elements
        root.findChildByNamespace("http://example.com/default", "child").ifPresent(child -> {
            System.out.println("\nFound child in default namespace: " + child.getName());

            child.findChildByNamespace("http://example.com/ns1", "element").ifPresent(ns1Element -> {
                System.out.println("Found ns1:element: " + ns1Element.getName());
                System.out.println("  Prefix: " + ns1Element.getPrefix());
                System.out.println("  Local name: " + ns1Element.getLocalName());
                System.out.println("  Namespace URI: " + ns1Element.getNamespaceURI());

                ns1Element
                        .findChildByNamespace("http://example.com/ns2", "data")
                        .ifPresent(ns2Data -> {
                            System.out.println("Found ns2:data: " + ns2Data.getName());
                            System.out.println("  Content: " + ns2Data.getTextContent());
                        });
            });
        });
        System.out.println();
    }

    private static void demonstrateNamespaceNavigation() {
        System.out.println("3. Namespace-Aware Navigation:");

        // Create a document with multiple elements in different namespaces
        Element root = Elements.builder("document")
                .withDefaultNamespace("http://example.com/doc")
                .withNamespace("meta", "http://example.com/metadata")
                .withNamespace("content", "http://example.com/content")
                .build();

        // Add children in different namespaces
        root.addChild(Elements.namespacedTextElement("meta", "title", "http://example.com/metadata", "Document Title"));
        root.addChild(Elements.namespacedTextElement("meta", "author", "http://example.com/metadata", "John Doe"));
        root.addChild(Elements.textElementInNamespace("http://example.com/doc", "summary", "Document summary"));
        root.addChild(Elements.namespacedTextElement("content", "section", "http://example.com/content", "Section 1"));
        root.addChild(Elements.namespacedTextElement("content", "section", "http://example.com/content", "Section 2"));

        System.out.println("Created document:\n" + root.toXml());

        // Navigate using namespace-aware methods
        System.out.println("Metadata elements:");
        root.findChildrenByNamespace("http://example.com/metadata", "title")
                .forEach(title -> System.out.println("  Title: " + title.getTextContent()));
        root.findChildrenByNamespace("http://example.com/metadata", "author")
                .forEach(author -> System.out.println("  Author: " + author.getTextContent()));

        System.out.println("Content sections:");
        root.findChildrenByNamespace("http://example.com/content", "section")
                .forEach(section -> System.out.println("  Section: " + section.getTextContent()));

        System.out.println("Default namespace elements:");
        root.findChildrenByNamespace("http://example.com/doc", "summary")
                .forEach(summary -> System.out.println("  Summary: " + summary.getTextContent()));
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

        Editor editor = new Editor(xml);
        Element root = editor.getRootElement();

        // Get namespace context at different levels
        NamespaceContext rootContext = root.getNamespaceContext();
        System.out.println("Root namespace context: " + rootContext);
        System.out.println("  Default namespace: " + rootContext.getDefaultNamespaceURI());
        System.out.println("  Declared prefixes: " + rootContext.getDeclaredPrefixes());
        System.out.println("  Declared URIs: " + rootContext.getDeclaredNamespaceURIs());

        root.findChild("child").ifPresent(child -> {
            NamespaceContext childContext = child.getNamespaceContext();
            System.out.println("\nChild namespace context: " + childContext);
            System.out.println("  Prefix 'c' maps to: " + childContext.getNamespaceURI("c"));
            System.out.println(
                    "  URI 'http://example.com/a' has prefix: " + childContext.getPrefix("http://example.com/a"));
        });

        // Demonstrate namespace resolution
        root.descendants()
                .filter(el -> "data".equals(el.getLocalName()))
                .findFirst()
                .ifPresent(dataElement -> {
                    System.out.println("\nData element namespace resolution:");
                    System.out.println("  Element: " + dataElement.getName());
                    System.out.println("  Resolved namespace URI: " + dataElement.getNamespaceURI());

                    NamespaceContext context = dataElement.getNamespaceContext();
                    System.out.println("  Available prefixes: " + context.getDeclaredPrefixes());
                    System.out.println("  Can resolve 'a': " + (context.getNamespaceURI("a") != null));
                    System.out.println("  Can resolve 'c': " + (context.getNamespaceURI("c") != null));
                    System.out.println("  Can resolve 'd': " + (context.getNamespaceURI("d") != null));
                });
        System.out.println();
    }

    private static void demonstrateComplexNamespaceDocument() {
        System.out.println("5. Complex Namespace Document Creation:");

        // Create a complex document using builder pattern with namespaces
        Element soapEnvelope = Elements.builder("Envelope")
                .withNamespace("soap", "http://schemas.xmlsoap.org/soap/envelope/")
                .withNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance")
                .withNamespace("xsd", "http://www.w3.org/2001/XMLSchema")
                .build();

        // Set the element name to include namespace prefix
        soapEnvelope.setName("soap:Envelope");

        // Create header
        Element header = Elements.namespacedElement("soap", "Header", "http://schemas.xmlsoap.org/soap/envelope/");
        soapEnvelope.addChild(header);

        // Create body
        Element body = Elements.namespacedElement("soap", "Body", "http://schemas.xmlsoap.org/soap/envelope/");

        // Add a custom method call in body
        Element methodCall = Elements.builder("GetUserInfo")
                .withDefaultNamespace("http://example.com/userservice")
                .withChild(Elements.textElementInNamespace("http://example.com/userservice", "userId", "12345"))
                .withChild(Elements.textElementInNamespace("http://example.com/userservice", "includeDetails", "true"))
                .build();

        body.addChild(methodCall);
        soapEnvelope.addChild(body);

        System.out.println("SOAP envelope with namespaces:");
        System.out.println(soapEnvelope.toXml());

        // Demonstrate namespace-aware querying
        System.out.println("\nNamespace-aware querying:");
        soapEnvelope
                .descendantsByNamespace("http://schemas.xmlsoap.org/soap/envelope/", "Body")
                .findFirst()
                .ifPresent(soapBody -> {
                    System.out.println("Found SOAP Body");
                    soapBody.findChildByNamespace("http://example.com/userservice", "GetUserInfo")
                            .ifPresent(method -> {
                                System.out.println("Found method: " + method.getLocalName());
                                method.findChildByNamespace("http://example.com/userservice", "userId")
                                        .ifPresent(
                                                userId -> System.out.println("  User ID: " + userId.getTextContent()));
                            });
                });
        System.out.println();
    }
}
