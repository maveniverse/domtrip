package eu.maveniverse.domtrip.demos;

import static org.junit.jupiter.api.Assertions.*;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import org.junit.jupiter.api.Test;

/**
 * Demonstration of the lossless XML editor capabilities.
 * Shows how formatting is preserved during round-trip editing.
 */
class EditorDemoTest {

    @Test
    void demonstrateEditorCapabilities() throws DomTripException {
        // Sample XML with various formatting styles
        String originalXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<!-- Sample XML document -->\n"
                + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n"
                + "\n"
                + "    <groupId>com.example</groupId>\n"
                + "    <artifactId   >sample-project</artifactId>\n"
                + "    <version>1.0.0</version>\n"
                + "    \n"
                + "    <properties>\n"
                + "        <maven.compiler.source>11</maven.compiler.source>\n"
                + "        <maven.compiler.target>11</maven.compiler.target>\n"
                + "    </properties>\n"
                + "    \n"
                + "    <dependencies>\n"
                + "        <!-- Core dependency -->\n"
                + "        <dependency>\n"
                + "            <groupId>junit</groupId>\n"
                + "            <artifactId>junit</artifactId>\n"
                + "            <version>4.13.2</version>\n"
                + "            <scope>test</scope>\n"
                + "        </dependency>\n"
                + "    </dependencies>\n"
                + "    \n"
                + "</project>";

        // Create editor and load XML
        Document doc = Document.of(originalXml);
        Editor editor = new Editor(doc);
        assertNotNull(editor.documentStats());

        // Test 1: Round-trip without modifications
        String roundTrip = editor.toXml();
        assertEquals(originalXml, roundTrip, "Round-trip should be identical to original");

        // Test 2: Add a new dependency
        Element dependencies = doc.root().descendant("dependencies").orElseThrow();
        Element newDep = editor.addElement(dependencies, "dependency");
        editor.addElement(newDep, "groupId", "org.mockito");
        editor.addElement(newDep, "artifactId", "mockito-core");
        editor.addElement(newDep, "version", "4.6.1");
        editor.addElement(newDep, "scope", "test");

        String afterAdd = editor.toXml();
        assertTrue(afterAdd.contains("mockito-core"));

        // Test 3: Modify existing element
        Element version = doc.root().descendant("version").orElseThrow();
        editor.setTextContent(version, "1.0.1-SNAPSHOT");

        String afterModify = editor.toXml();
        assertTrue(afterModify.contains("1.0.1-SNAPSHOT"));

        // Test 4: Add comment
        Element properties = doc.root().descendant("properties").orElseThrow();
        editor.addComment(properties, " Updated compiler settings ");

        String afterComment = editor.toXml();
        assertTrue(afterComment.contains("Updated compiler settings"));

        // Test 5: Add attribute
        Element project = editor.root();
        project.attribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");

        String finalResult = editor.toXml();
        assertTrue(finalResult.contains("xmlns:xsi"));

        // Test 6: Pretty print
        String prettyPrinted = editor.toXmlPretty();
        assertNotNull(prettyPrinted);
        assertFalse(prettyPrinted.isEmpty());

        // Demonstrate key features
        verifyKeyFeatures();
    }

    private static void verifyKeyFeatures() throws DomTripException {
        // Feature 1: Whitespace preservation
        String xmlWithSpaces = "<root>\n" + "    <element   attr=\"value\"   >\n"
                + "        <nested>  content  </nested>\n"
                + "    </element>\n"
                + "</root>";

        Editor editor = new Editor(Document.of(xmlWithSpaces));
        assertEquals(xmlWithSpaces, editor.toXml(), "Whitespace should be preserved");

        // Feature 2: Comment preservation
        String xmlWithComments = "<!-- Header -->\n<root>\n  <!-- Inline -->\n  <element/>\n</root>";
        editor = new Editor(Document.of(xmlWithComments));
        assertEquals(xmlWithComments, editor.toXml(), "Comments should be preserved");

        // Feature 3: CDATA preservation
        String xmlWithCData = "<root><script><![CDATA[if (x < y) { alert(\"test\"); }]]></script></root>";
        editor = new Editor(Document.of(xmlWithCData));
        assertEquals(xmlWithCData, editor.toXml(), "CDATA should be preserved");

        // Feature 4: Minimal change serialization
        String complexXml = "<root>\n  <unchanged>content</unchanged>\n  <toModify>old</toModify>\n</root>";
        Document doc = Document.of(complexXml);
        editor = new Editor(doc);
        Element toModify = doc.root().descendant("toModify").orElseThrow();
        editor.setTextContent(toModify, "new");

        String result = editor.toXml();
        assertTrue(result.contains("  <unchanged>content</unchanged>"), "Unchanged element should be preserved");
        assertTrue(result.contains("<toModify>new</toModify>"), "Modified element should be updated");
        assertTrue(result.startsWith("<root>\n"), "Overall structure should be preserved");
    }
}
