package eu.maveniverse.domtrip.snippets;

import eu.maveniverse.domtrip.*;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Snippet tests for basic concepts documentation.
 */
public class BasicConceptsSnippets extends BaseSnippetTest {

    @Test
    public void demonstrateLosslessPhilosophy() {
        // START: lossless-philosophy
        String xml = "<project><version>1.0</version></project>";

        // DomTrip approach (preservation-focused)
        Editor editor = new Editor(Document.of(xml));
        Element root = editor.root();
        Element version = root.child("version").orElse(null);
        String value = version.textContent();
        String result = editor.toXml(); // Identical to original if unchanged
        // END: lossless-philosophy

        Assertions.assertEquals("1.0", value);
        Assertions.assertTrue(result.contains("version"));
    }

    @Test
    public void demonstrateNodeHierarchy() {
        // START: node-hierarchy
        // ✅ This works - Element can have children
        Element parent = Element.of("parent");
        parent.addNode(Text.of("content"));

        // Text nodes cannot have children (compile-time safety)
        Text text = Text.of("content");
        // text.addNode(...); // Would not compile
        // END: node-hierarchy

        Assertions.assertEquals("parent", parent.name());
        Assertions.assertEquals("content", text.content());
    }

    @Test
    public void demonstrateModificationTracking() {
        // START: modification-tracking
        String originalXml = "<project><version>1.0</version><name>test</name></project>";
        Editor editor = new Editor(Document.of(originalXml));
        Element root = editor.root();

        // Unmodified nodes track their state
        Element unchanged = root.child("name").orElse(null);
        Assertions.assertFalse(unchanged.isModified()); // false

        // Modified nodes are tracked
        Element changed = root.child("version").orElse(null);
        editor.setTextContent(changed, "2.0.0");
        Assertions.assertTrue(changed.isModified()); // true

        // Only modified sections are reformatted in output
        String result = editor.toXml();
        // END: modification-tracking

        Assertions.assertTrue(result.contains("2.0.0"));
        Assertions.assertTrue(result.contains("test"));
    }

    @Test
    public void demonstrateDualContentStorage() {
        // START: dual-content-storage
        // Original XML: <message>Hello &amp; goodbye</message>
        String xml = "<message>Hello &amp; goodbye</message>";
        Document doc = Document.of(xml);
        Element element = doc.root();

        // For your code - entities are decoded
        String decoded = element.textContent(); // "Hello & goodbye"

        // For serialization - entities are preserved in XML output
        String result = doc.toXml(); // Contains "Hello &amp; goodbye"
        // END: dual-content-storage

        Assertions.assertEquals("Hello & goodbye", decoded);
        Assertions.assertTrue(result.contains("&amp;"));
    }

    @Test
    public void demonstrateAttributeHandling() {
        // START: attribute-handling
        String xml = "<dependency scope='test'></dependency>";
        Editor editor = new Editor(Document.of(xml));
        Element element = editor.root();

        // Access attribute as object for detailed information
        Attribute scope = element.attributeObject("scope");

        String value = scope.value(); // "test"
        QuoteStyle quoteStyle = scope.quoteStyle(); // QuoteStyle.SINGLE
        String whitespace = scope.precedingWhitespace(); // Whitespace before attribute
        // END: attribute-handling

        Assertions.assertEquals("test", value);
        Assertions.assertEquals(QuoteStyle.SINGLE, quoteStyle);
        Assertions.assertNotNull(whitespace);
    }

    @Test
    public void demonstrateWhitespaceInference() {
        // START: whitespace-inference
        // Existing structure with indentation
        String xml =
                """
            <dependencies>
                <dependency>existing</dependency>
            </dependencies>
            """;

        Editor editor = new Editor(Document.of(xml));
        Element dependencies = editor.root();

        // Adding new dependency automatically infers indentation
        Element newDep = editor.addElement(dependencies, "dependency");
        editor.setTextContent(newDep, "new");

        String result = editor.toXml();
        // Result uses same indentation as existing dependencies
        // END: whitespace-inference

        Assertions.assertTrue(result.contains("new"));
        // Check that indentation is preserved
        Assertions.assertTrue(result.contains("    <dependency>new</dependency>"));
    }

    @Test
    public void demonstrateConfigurationSystem() {
        // START: configuration-system
        // Preset configurations
        DomTripConfig defaults = DomTripConfig.defaults(); // Maximum preservation
        DomTripConfig pretty = DomTripConfig.prettyPrint(); // Clean output
        DomTripConfig minimal = DomTripConfig.minimal(); // Compact output

        // Custom configuration
        DomTripConfig custom = DomTripConfig.defaults()
                .withIndentString("  ") // 2 spaces
                .withWhitespacePreservation(true) // Keep original whitespace
                .withCommentPreservation(true) // Keep comments
                .withDefaultQuoteStyle(QuoteStyle.DOUBLE); // Prefer double quotes
        // END: configuration-system

        Assertions.assertNotNull(defaults);
        Assertions.assertNotNull(pretty);
        Assertions.assertNotNull(minimal);
        Assertions.assertNotNull(custom);
    }

    @Test
    public void demonstrateOptionalBasedNavigation() {
        // START: optional-based-navigation
        String xml = "<root><child>value</child></root>";
        Editor editor = new Editor(Document.of(xml));
        Element root = editor.root();

        Optional<Element> child = root.child("child");
        child.ifPresent(element -> {
            // Safe navigation - no null checks needed
            String value = element.textContent();
            Assertions.assertEquals("value", value);
        });
        // END: optional-based-navigation

        Assertions.assertTrue(child.isPresent());
    }

    @Test
    public void demonstrateStreamBasedNavigation() {
        // START: stream-based-navigation
        String xml =
                """
            <root>
                <dependency status="active"><artifactId>junit</artifactId></dependency>
                <dependency status="inactive"><artifactId>mockito</artifactId></dependency>
                <dependency status="active"><artifactId>hamcrest</artifactId></dependency>
            </root>
            """;

        Editor editor = new Editor(Document.of(xml));
        Element root = editor.root();

        // Find all active dependencies
        java.util.List<String> activeArtifacts = root.children("dependency")
                .filter(dep -> "active".equals(dep.attribute("status")))
                .map(dep -> dep.child("artifactId").orElse(null))
                .filter(java.util.Objects::nonNull)
                .map(Element::textContent)
                .toList();
        // END: stream-based-navigation

        Assertions.assertEquals(2, activeArtifacts.size());
        Assertions.assertTrue(activeArtifacts.contains("junit"));
        Assertions.assertTrue(activeArtifacts.contains("hamcrest"));
    }

    @Test
    public void demonstrateNamespaceAwareNavigation() {
        // START: namespace-aware-navigation
        String xml =
                """
            <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                <soap:Body>
                    <item xmlns="http://example.com/items">content</item>
                </soap:Body>
            </soap:Envelope>
            """;

        Editor editor = new Editor(Document.of(xml));
        Element root = editor.root();

        // Find elements by qualified name
        Optional<Element> soapBody = root.child("soap:Body");

        // Access namespace information
        String namespaceURI = root.namespaceURI(); // "http://schemas.xmlsoap.org/soap/envelope/"
        String localName = root.localName(); // "Envelope"
        String prefix = root.prefix(); // "soap"
        // END: namespace-aware-navigation

        Assertions.assertTrue(soapBody.isPresent());
        Assertions.assertEquals("http://schemas.xmlsoap.org/soap/envelope/", namespaceURI);
        Assertions.assertEquals("Envelope", localName);
        Assertions.assertEquals("soap", prefix);
    }

    @Test
    public void demonstrateErrorHandling() {
        // START: error-handling
        try {
            String xmlString = "<root><child>valid</child></root>";
            Editor editor = new Editor(Document.of(xmlString));
            // ... editing operations
            String result = editor.toXml();
            Assertions.assertTrue(result.contains("valid"));
        } catch (DomTripException e) {
            // General DomTrip error
            System.err.println("DomTrip error: " + e.getMessage());
        }
        // END: error-handling

        // Test passes if no exception is thrown
        Assertions.assertTrue(true);
    }
}
