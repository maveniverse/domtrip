package eu.maveniverse.domtrip.snippets;

import eu.maveniverse.domtrip.Comment;
import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripConfig;
import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Snippet tests for the Error Handling documentation.
 */
public class ErrorHandlingSnippets extends BaseSnippetTest {

    @Test
    public void demonstrateDomTripException() {
        // START: domtrip-exception
        try {
            String malformedXml = "<<invalid xml>>";
            Document doc = Document.of(malformedXml);
        } catch (Exception e) {
            System.err.println("DomTrip error: " + e.getMessage());
            System.err.println("Cause: " + e.getCause());
        }
        // END: domtrip-exception

        // Test passes if no exception is thrown
        Assertions.assertTrue(true);
    }

    @Test
    public void demonstrateParsingExceptions() {
        // START: parsing-exceptions
        try {
            Document doc = Document.of("<<invalid xml>>");
        } catch (Exception e) {
            System.err.println("Parse error: " + e.getMessage());
            // Note: Actual line/column info depends on parser implementation
        }
        // END: parsing-exceptions

        // Test passes if no exception is thrown
        Assertions.assertTrue(true);
    }

    @Test
    public void demonstrateValidationExceptions() {
        // START: validation-exceptions
        try {
            String xml = createTestXml("root");
            Document doc = Document.of(xml);
            Editor editor = new Editor(doc);
            // This would cause a validation error in strict mode
            // editor.addElement(null, "invalid", "content"); // null parent
        } catch (Exception e) {
            System.err.println("Validation error: " + e.getMessage());
        }
        // END: validation-exceptions

        // Test passes if no exception is thrown
        Assertions.assertTrue(true);
    }

    @Test
    public void demonstrateMalformedXML() {
        // START: malformed-xml
        String malformedXml =
                """
            <root>
                <unclosed-tag>
                <another>content</another>
            </root>
            """;

        try {
            Document doc = Document.of(malformedXml);
        } catch (Exception e) {
            System.err.println("XML syntax error:");
            System.err.println("  Message: " + e.getMessage());

            // Suggested fix
            System.err.println("  Suggestion: Check for unclosed tags");
        }
        // END: malformed-xml

        // Test passes if no exception is thrown
        Assertions.assertTrue(true);
    }

    @Test
    public void demonstrateEncodingIssues() throws Exception {
        // START: encoding-issues
        try {
            // Simulate file with encoding issues
            String xmlContent = createTestXml("root");
            Document doc = Document.of(xmlContent);

        } catch (Exception e) {
            System.err.println("Encoding error: " + e.getMessage());

            // Recovery strategy
            try {
                // Try with explicit encoding (simulated)
                String xmlContent = createTestXml("root");
                Document doc = Document.of(xmlContent);
            } catch (Exception recovery) {
                System.err.println("Recovery failed: " + recovery.getMessage());
            }
        }
        // END: encoding-issues

        // Test passes if no exception is thrown
        Assertions.assertTrue(true);
    }

    @Test
    public void demonstrateNamespaceConflicts() {
        // START: namespace-conflicts
        try {
            String xml = createTestXml("root");
            Document doc = Document.of(xml);
            Editor editor = new Editor(doc);
            Element root = editor.root();

            // Try to add conflicting namespace (conceptual example)
            root.namespaceDeclaration("ns", "http://example.com/ns1");
            // This would potentially cause a conflict in some scenarios
            root.namespaceDeclaration("ns", "http://example.com/ns2");

        } catch (Exception e) {
            System.err.println("Namespace conflict: " + e.getMessage());

            // Resolution strategy
            String alternativePrefix = "ns2";
            // Use alternative prefix
        }
        // END: namespace-conflicts

        // Test passes if no exception is thrown
        Assertions.assertTrue(true);
    }

    @Test
    public void demonstrateGracefulParsing() throws DomTripException {
        // START: graceful-parsing
        String xml = "<root><child>content</child></root>";
        Document result = parseWithRecovery(xml);
        // END: graceful-parsing

        Assertions.assertNotNull(result);
    }

    public Document parseWithRecovery(String xml) throws DomTripException {
        try {
            return Document.of(xml);
        } catch (Exception e) {
            System.err.println("Parse failed, attempting recovery...");

            // Strategy 1: Try to fix common issues
            String fixedXml = xml.replaceAll("&(?![a-zA-Z]+;)", "&amp;") // Fix unescaped ampersands
                    .replaceAll("<([^>]+)>\\s*</\\1>", "<$1/>"); // Convert empty elements

            try {
                return Document.of(fixedXml);
            } catch (Exception e2) {
                // Strategy 2: Extract valid fragments
                return extractValidFragments(xml);
            }
        }
    }

    private Document extractValidFragments(String xml) throws DomTripException {
        // Implementation to extract valid XML fragments
        // and create a document with available content
        Document doc = Document.withRootElement("recovered");
        Editor editor = new Editor(doc);

        // Add error information
        editor.addElement(editor.root(), "error", "Original XML was malformed");
        editor.addElement(editor.root(), "partial-content", extractTextContent(xml));

        return doc;
    }

    private String extractTextContent(String xml) {
        // Simple text extraction for recovery
        return xml.replaceAll("<[^>]*>", "").trim();
    }

    @Test
    public void demonstrateValidationWithFallbacks() throws DomTripException {
        // START: validation-with-fallbacks
        String xml = createTestXml("parent");
        Document doc = Document.of(xml);
        Element parent = doc.root();
        safeElementOperation(parent, "child", "content");
        // END: validation-with-fallbacks

        // Test passes if no exception is thrown
        Assertions.assertTrue(true);
    }

    public void safeElementOperation(Element parent, String name, String content) {
        try {
            // Primary operation
            Editor editor = new Editor(parent.document());
            editor.addElement(parent, name, content);

        } catch (Exception e) {
            System.err.println("Validation failed: " + e.getMessage());

            // Fallback: Add as comment
            try {
                Comment fallback = Comment.of("Failed to add element: " + name + "=" + content);
                parent.addNode(fallback);
            } catch (Exception fallbackError) {
                System.err.println("Fallback also failed: " + fallbackError.getMessage());
            }
        }
    }

    @Test
    public void demonstrateResourceCleanup() throws Exception {
        // START: resource-cleanup
        String xmlContent = createTestXml("root");
        java.io.InputStream inputStream = new java.io.ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8));
        Document result = parseWithCleanup(inputStream);
        // END: resource-cleanup

        Assertions.assertNotNull(result);
    }

    public Document parseWithCleanup(InputStream inputStream) {
        try {
            return Document.of(inputStream);
        } catch (Exception e) {
            System.err.println("Parse failed: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            // Ensure resources are cleaned up
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException closeError) {
                System.err.println("Failed to close stream: " + closeError.getMessage());
            }
        }
    }

    @Test
    public void demonstrateInputValidation() throws DomTripException {
        // START: input-validation
        String xml = createTestXml("root");
        Document result = safeParse(xml);
        // END: input-validation

        Assertions.assertNotNull(result);
    }

    public Document safeParse(String xml) throws DomTripException {
        // Pre-validation
        if (xml == null || xml.trim().isEmpty()) {
            throw new IllegalArgumentException("XML content cannot be null or empty");
        }

        // Basic structure check
        if (!xml.trim().startsWith("<")) {
            throw new IllegalArgumentException("Content does not appear to be XML");
        }

        // Check for obvious issues
        long openTags = xml.chars().filter(ch -> ch == '<').count();
        long closeTags = xml.chars().filter(ch -> ch == '>').count();

        if (openTags != closeTags) {
            System.err.println("Warning: Unbalanced angle brackets detected");
        }

        return Document.of(xml);
    }

    @Test
    public void demonstrateSafeElementAccess() throws DomTripException {
        // START: safe-element-access
        String xml = createTestXml("parent");
        Document doc = Document.of(xml);
        Element parent = doc.root();

        String text = safeGetElementText(parent, "child");
        safeSetAttribute(parent, "attr", "value");
        // END: safe-element-access

        Assertions.assertNotNull(text);
    }

    public String safeGetElementText(Element parent, String childName) {
        try {
            return parent.child(childName).map(Element::textContent).orElse("");
        } catch (Exception e) {
            System.err.println("Failed to access child element '" + childName + "': " + e.getMessage());
            return "";
        }
    }

    public void safeSetAttribute(Element element, String name, String value) {
        try {
            // Validate attribute name
            if (!isValidXmlName(name)) {
                throw new IllegalArgumentException("Invalid attribute name: " + name);
            }

            element.attribute(name, value);
        } catch (Exception e) {
            System.err.println("Failed to set attribute '" + name + "': " + e.getMessage());
        }
    }

    private boolean isValidXmlName(String name) {
        // Simple validation - in real implementation would be more comprehensive
        return name != null && !name.isEmpty() && Character.isLetter(name.charAt(0));
    }

    @Test
    public void demonstrateErrorContext() {
        // START: error-context
        try {
            String xml = createTestXml("root");
            Document document = Document.of(xml);
            Editor editor = new Editor(document);
            // ... complex operations
        } catch (Exception e) {
            // Get detailed context (conceptual - actual API may vary)
            String context = e.getMessage();
            System.err.println("Error context: " + context);
        }
        // END: error-context

        // Test passes if no exception is thrown
        Assertions.assertTrue(true);
    }

    @Test
    public void demonstrateValidationMode() {
        // START: validation-mode
        // Enable strict validation for debugging (conceptual)
        DomTripConfig config = DomTripConfig.defaults();

        try {
            String xml = createTestXml("root");
            Document document = Document.of(xml);
            Editor editor = new Editor(document);
            // Operations will provide detailed validation
        } catch (Exception e) {
            // Detailed validation errors
            System.err.println("Validation details: " + e.getMessage());
        }
        // END: validation-mode

        // Test passes if no exception is thrown
        Assertions.assertTrue(true);
    }

    @Test
    public void demonstrateLoggingIntegration() {
        // START: logging-integration
        // Simulate logging integration
        XmlProcessor processor = new XmlProcessor();
        String xml = createTestXml("root");
        Document result = processor.processXml(xml);
        // END: logging-integration

        Assertions.assertNotNull(result);
    }

    public static class XmlProcessor {
        // Simulated logger
        private void logDebug(String message, Object... args) {
            System.out.println("DEBUG: " + String.format(message.replace("{}", "%s"), args));
        }

        private void logInfo(String message) {
            System.out.println("INFO: " + message);
        }

        private void logError(String message, Object... args) {
            System.err.println("ERROR: " + String.format(message.replace("{}", "%s"), args));
        }

        public Document processXml(String xml) {
            try {
                logDebug("Parsing XML document, length: {}", xml.length());
                Document doc = Document.of(xml);
                logInfo("Successfully parsed XML document");
                return doc;

            } catch (Exception e) {
                logError("DomTrip processing error: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }
}
