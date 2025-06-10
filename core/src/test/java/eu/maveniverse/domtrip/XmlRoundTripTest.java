package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test cases demonstrating the lossless XML round-trip capabilities.
 * These tests show how formatting is preserved when making changes.
 */
public class XmlRoundTripTest {

    private Editor editor;

    @BeforeEach
    void setUp() {
        editor = new Editor();
    }

    @Test
    void testBasicRoundTrip() {
        String originalXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<root>\n"
                + "  <child attr=\"value\">text content</child>\n"
                + "  <!-- comment -->\n"
                + "</root>";

        editor.loadXml(originalXml);
        String roundTripXml = editor.toXml();

        // Should preserve exact formatting for unmodified content
        assertEquals(originalXml, roundTripXml);
    }

    @Test
    void testWhitespacePreservation() {
        String originalXml = "<root>\n" + "    <element   attr1=\"val1\"   attr2=\"val2\"  >\n"
                + "        <nested>  content with spaces  </nested>\n"
                + "    </element>\n"
                + "</root>";

        editor.loadXml(originalXml);
        String roundTripXml = editor.toXml();

        assertEquals(originalXml, roundTripXml);
    }

    @Test
    void testCommentPreservation() {
        String originalXml = "<!-- Header comment -->\n" + "<root>\n"
                + "  <!-- Inline comment -->\n"
                + "  <element>content</element>\n"
                + "  <!-- Another comment with\n"
                + "       multiple lines -->\n"
                + "</root>\n"
                + "<!-- Footer comment -->";

        editor.loadXml(originalXml);
        String roundTripXml = editor.toXml();

        assertEquals(originalXml, roundTripXml);
    }

    @Test
    void testCDataPreservation() {
        String originalXml = "<root>\n" + "  <script><![CDATA[\n"
                + "    function test() {\n"
                + "      if (x < y && y > z) {\n"
                + "        return \"<test>\";\n"
                + "      }\n"
                + "    }\n"
                + "  ]]></script>\n"
                + "</root>";

        editor.loadXml(originalXml);
        String roundTripXml = editor.toXml();

        assertEquals(originalXml, roundTripXml);
    }

    @Test
    void testAddElementPreservesFormatting() {
        String originalXml = "<root>\n" + "  <existing>content</existing>\n" + "</root>";

        editor.loadXml(originalXml);
        Element root = editor.root().orElseThrow();
        Element newElement = editor.addElement(root, "newElement", "new content");

        String result = editor.toXml();

        // Should preserve original formatting and add new element with appropriate indentation
        assertTrue(result.contains("<existing>content</existing>"));
        assertTrue(result.contains("<newElement>new content</newElement>"));
        assertTrue(result.contains("  <newElement>")); // Should be indented
    }

    @Test
    void testModifyAttributePreservesOtherFormatting() {
        String originalXml = "<root>\n" + "  <element   attr1=\"original\"   attr2=\"value2\"  >\n"
                + "    <child>content</child>\n"
                + "  </element>\n"
                + "</root>";

        editor.loadXml(originalXml);
        Element element = editor.element("element").orElseThrow();
        element.attribute("attr1", "modified");

        String result = editor.toXml();

        // Should preserve structure and other attributes
        assertTrue(result.contains("attr1=\"modified\""));
        assertTrue(result.contains("attr2=\"value2\""));
        assertTrue(result.contains("<child>content</child>"));
    }

    @Test
    void testRemoveElementPreservesFormatting() {
        String originalXml = "<root>\n" + "  <keep>content1</keep>\n"
                + "  <remove>content2</remove>\n"
                + "  <keep>content3</keep>\n"
                + "</root>";

        editor.loadXml(originalXml);
        Element toRemove = editor.child(editor.root().orElseThrow(), "remove")
                .orElseThrow();
        editor.removeElement(toRemove);

        String result = editor.toXml();

        // Should preserve formatting of remaining elements
        assertTrue(result.contains("<keep>content1</keep>"));
        assertTrue(result.contains("<keep>content3</keep>"));
        assertFalse(result.contains("<remove>"));
    }

    @Test
    void testComplexDocumentModification() {
        String originalXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<!-- Document comment -->\n"
                + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n"
                + "  <groupId>com.example</groupId>\n"
                + "  <artifactId>test-project</artifactId>\n"
                + "  <version>1.0.0</version>\n"
                + "  \n"
                + "  <dependencies>\n"
                + "    <!-- Existing dependency -->\n"
                + "    <dependency>\n"
                + "      <groupId>junit</groupId>\n"
                + "      <artifactId>junit</artifactId>\n"
                + "      <version>4.13.2</version>\n"
                + "    </dependency>\n"
                + "  </dependencies>\n"
                + "</project>";

        editor.loadXml(originalXml);

        // Add a new dependency
        Element dependencies = editor.element("dependencies").orElseThrow();
        Element newDep = editor.addElement(dependencies, "dependency");
        editor.addElement(newDep, "groupId", "org.mockito");
        editor.addElement(newDep, "artifactId", "mockito-core");
        editor.addElement(newDep, "version", "4.6.1");

        // Modify version
        Element version = editor.element("version").orElseThrow();
        editor.setTextContent(version, "1.0.1");

        String result = editor.toXml();

        // Verify original structure is preserved
        assertTrue(result.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(result.contains("<!-- Document comment -->"));
        assertTrue(result.contains("xmlns=\"http://maven.apache.org/POM/4.0.0\""));
        assertTrue(result.contains("<!-- Existing dependency -->"));

        // Verify modifications
        assertTrue(result.contains("<version>1.0.1</version>"));
        assertTrue(result.contains("<groupId>org.mockito</groupId>"));
        assertTrue(result.contains("<artifactId>mockito-core</artifactId>"));

        // Verify structure
        assertTrue(result.contains("<dependencies>"));
        assertTrue(result.contains("</dependencies>"));
    }

    @Test
    void testSelfClosingTagPreservation() {
        String originalXml =
                "<root>\n" + "  <self-closing attr=\"value\"/>\n" + "  <normal>content</normal>\n" + "</root>";

        editor.loadXml(originalXml);
        String roundTripXml = editor.toXml();

        assertEquals(originalXml, roundTripXml);
    }

    @Test
    void testEntityPreservation() {
        String originalXml = "<root>\n" + "  <content>Text with &lt;entities&gt; &amp; symbols</content>\n"
                + "  <attr value=\"&quot;quoted&quot; &amp; escaped\"/>\n"
                + "</root>";

        editor.loadXml(originalXml);
        String roundTripXml = editor.toXml();

        // Entities should be preserved in output
        assertTrue(roundTripXml.contains("&lt;entities&gt;"));
        assertTrue(roundTripXml.contains("&amp; symbols"));
        assertTrue(roundTripXml.contains("&quot;quoted&quot;"));
    }

    @Test
    void testDocumentCreation() {
        editor.createDocument("newRoot");
        Element root = editor.root().orElseThrow();

        editor.addElement(root, "child1", "content1");
        editor.addElement(root, "child2", "content2");

        String xml = editor.toXml();

        assertTrue(xml.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(xml.contains("<newRoot>"));
        assertTrue(xml.contains("<child1>content1</child1>"));
        assertTrue(xml.contains("<child2>content2</child2>"));
        assertTrue(xml.contains("</newRoot>"));
    }

    @Test
    void testDocumentStats() {
        String xml = "<root>\n" + "  <element>text</element>\n" + "  <!-- comment -->\n" + "  <another/>\n" + "</root>";

        editor.loadXml(xml);
        String stats = editor.documentStats();

        assertTrue(stats.contains("3 elements")); // root, element, another
        assertTrue(stats.contains("comment"));
    }
}
