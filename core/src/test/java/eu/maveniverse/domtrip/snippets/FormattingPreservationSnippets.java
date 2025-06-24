package eu.maveniverse.domtrip.snippets;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripConfig;
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Snippet tests for formatting preservation features documentation.
 */
public class FormattingPreservationSnippets extends BaseSnippetTest {

    @Test
    public void demonstrateBasicFormatPreservation() {
        // START: basic-format-preservation
        // Original XML with specific formatting
        String xml =
                """
            <project>
                <groupId>com.example</groupId>
                <artifactId>my-app</artifactId>
                <version>1.0.0</version>
            </project>
            """;

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        // Make a change
        Element version = doc.root().child("version").orElseThrow();
        editor.setTextContent(version, "2.0.0");

        String result = editor.toXml();
        // Original formatting is preserved, only the version content changed
        // END: basic-format-preservation

        Assertions.assertTrue(result.contains("2.0.0"));
        Assertions.assertTrue(result.contains("    <groupId>com.example</groupId>"));
        Assertions.assertTrue(result.contains("    <artifactId>my-app</artifactId>"));
    }

    @Test
    public void demonstrateWhitespaceTracking() {
        String xml =
                """
            <project>
                <groupId>com.example</groupId>
            </project>
            """;

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        // START: whitespace-tracking
        // For this XML: "  <element>content</element>\n"
        Element element = doc.root().child("groupId").orElseThrow();
        String before = element.precedingWhitespace(); // "\n    " (newline + 4 spaces)
        String after = doc.root().innerPrecedingWhitespace(); // "\n"
        // END: whitespace-tracking

        Assertions.assertEquals("\n    ", before);
        Assertions.assertEquals("\n", after);
    }

    @Test
    public void demonstrateElementWhitespace() {
        String xml = "<project>  <groupId  >com.example</  groupId>  </project>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        // START: element-whitespace
        Element element = doc.root().child("groupId").orElseThrow();

        // Whitespace inside opening tag: <element >
        String openTagWhitespace = element.openTagWhitespace(); // "  "

        // Whitespace inside closing tag: </ element>
        String closeTagWhitespace = element.closeTagWhitespace(); // "  "
        // END: element-whitespace

        Assertions.assertEquals("  ", openTagWhitespace);
        Assertions.assertEquals("  ", closeTagWhitespace);
    }

    @Test
    public void demonstrateInnerElementWhitespace() {
        String xml = "<parent>\n    \n</parent>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        // START: inner-element-whitespace
        Element element = doc.root();

        // Whitespace immediately before closing tag: WHITESPACE</element>
        String innerPreceding = element.innerPrecedingWhitespace(); // "\n    \n"

        // This field is used when an element contains only whitespace
        // (no child elements), providing a cleaner model than Text nodes
        // END: inner-element-whitespace

        Assertions.assertEquals("\n    \n", innerPreceding);
    }

    @Test
    public void demonstrateIntelligentInference() {
        // START: intelligent-inference
        // Existing structure:
        //   <dependencies>
        //       <dependency>...</dependency>
        //   </dependencies>
        Document doc = Document.of(
                """
            <dependencies>
                <dependency>
                    <groupId>junit</groupId>
                    <artifactId>junit</artifactId>
                </dependency>
            </dependencies>
            """);

        Editor editor = new Editor(doc);

        // Adding new dependency automatically infers indentation
        Element dependencies = doc.root();
        Element newDep = editor.addElement(dependencies, "dependency");
        editor.addElement(newDep, "groupId", "mockito");
        editor.addElement(newDep, "artifactId", "mockito-core");

        String result = editor.toXml();
        // Result uses same indentation as existing dependencies
        // END: intelligent-inference

        Assertions.assertTrue(result.contains("mockito"));
        // Verify proper indentation is inferred
        Assertions.assertTrue(result.contains("    <dependency>"));
        Assertions.assertTrue(result.contains("        <groupId>mockito</groupId>"));
    }

    @Test
    public void demonstrateConfigurationControl() {
        String xml = "<project><groupId>com.example</groupId></project>";

        // START: configuration-control
        // Preset configurations
        DomTripConfig defaults = DomTripConfig.defaults(); // Default preservation
        DomTripConfig pretty = DomTripConfig.prettyPrint(); // Clean output

        // Custom configuration
        DomTripConfig custom = DomTripConfig.defaults()
                .withIndentString("  ") // 2 spaces
                .withCommentPreservation(true); // Keep comments
        // END: configuration-control

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc, custom);

        Assertions.assertNotNull(defaults);
        Assertions.assertNotNull(pretty);
        Assertions.assertNotNull(custom);
    }

    @Test
    public void demonstrateModificationTracking() {
        String xml =
                """
            <project>
                <groupId>com.example</groupId>
                <version>1.0.0</version>
            </project>
            """;

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        // START: modification-tracking
        // Unmodified nodes use original formatting
        Element unchanged = doc.root().child("groupId").orElseThrow();
        Assertions.assertFalse(unchanged.isModified()); // false

        // Modified nodes are rebuilt with inferred formatting
        Element changed = doc.root().child("version").orElseThrow();
        editor.setTextContent(changed, "2.0.0");
        Assertions.assertTrue(changed.isModified()); // true
        // END: modification-tracking
    }

    @Test
    public void demonstrateAttributeFormatting() {
        String xml = "<dependency scope='test'  optional=\"true\" />";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        // START: attribute-formatting
        Element element = doc.root();

        // Attributes preserve their original quote style and whitespace
        String scope = element.attribute("scope"); // "test"
        String optional = element.attribute("optional"); // "true"

        // When serialized, original formatting is maintained:
        // scope='test'  optional="true"
        String result = editor.toXml();
        // END: attribute-formatting

        Assertions.assertEquals("test", scope);
        Assertions.assertEquals("true", optional);
        Assertions.assertTrue(result.contains("scope='test'"));
        Assertions.assertTrue(result.contains("optional=\"true\""));
    }

    @Test
    public void demonstrateCommentPreservation() {
        // START: comment-preservation
        String xml =
                """
            <project>
                <!-- This is an important comment -->
                <groupId>com.example</groupId>
                <!-- Another comment -->
                <artifactId>my-app</artifactId>
            </project>
            """;

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        // Add new element - comments are preserved
        editor.addElement(doc.root(), "version", "1.0.0");

        String result = editor.toXml();
        // Comments remain in their original positions with original formatting
        // END: comment-preservation

        Assertions.assertTrue(result.contains("<!-- This is an important comment -->"));
        Assertions.assertTrue(result.contains("<!-- Another comment -->"));
        Assertions.assertTrue(result.contains("<version>1.0.0</version>"));
    }

    @Test
    public void demonstrateMinimalChangeSerialization() {
        // START: minimal-change-serialization
        String originalXml =
                """
            <project>
                <groupId>com.example</groupId>
                <artifactId>my-app</artifactId>
                <version>1.0.0</version>
            </project>
            """;

        Document doc = Document.of(originalXml);
        Editor editor = new Editor(doc);

        // Only modify one element
        Element version = doc.root().child("version").orElseThrow();
        editor.setTextContent(version, "2.0.0");

        String result = editor.toXml();

        // Only the modified element is rebuilt
        // All other elements retain their exact original formatting
        // END: minimal-change-serialization

        Assertions.assertTrue(result.contains("2.0.0"));
        Assertions.assertFalse(result.contains("1.0.0"));
        // Verify other elements maintain original formatting
        Assertions.assertTrue(result.contains("    <groupId>com.example</groupId>"));
        Assertions.assertTrue(result.contains("    <artifactId>my-app</artifactId>"));
    }
}
