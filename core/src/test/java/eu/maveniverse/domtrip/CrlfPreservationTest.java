package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests that CRLF line endings are preserved during XML modifications.
 *
 * @see <a href="https://github.com/maveniverse/domtrip/issues/230">Issue #230</a>
 */
class CrlfPreservationTest {

    private static String crlfXml(String... lines) {
        return String.join("\r\n", lines);
    }

    @Test
    void testRoundTripPreservesCrlf() throws Exception {
        String xml = crlfXml(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
                "<project>",
                "    <groupId>com.example</groupId>",
                "    <artifactId>test</artifactId>",
                "    <version>1.0</version>",
                "</project>",
                "");

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        assertEquals(xml, result);
    }

    @Test
    void testModifyTextPreservesCrlf() throws Exception {
        String xml = crlfXml(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
                "<project>",
                "    <groupId>com.example</groupId>",
                "    <artifactId>test</artifactId>",
                "    <version>1.0</version>",
                "</project>",
                "");

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element version = doc.root().childElement("version").orElseThrow();
        version.textContent("2.0");

        String result = editor.toXml();

        assertFalse(result.contains("\r\n2.0"), "Modified text should not introduce bare LF before value");
        assertTrue(result.contains("2.0"), "Modified text should be present");
        assertNoCrlfCorruption(result);
    }

    @Test
    void testAddElementPreservesCrlf() throws Exception {
        String xml = crlfXml(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
                "<project>",
                "    <groupId>com.example</groupId>",
                "    <artifactId>test</artifactId>",
                "</project>",
                "");

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        editor.addElement(doc.root(), "version", "1.0");

        String result = editor.toXml();

        assertTrue(result.contains("<version>1.0</version>"));
        assertNoCrlfCorruption(result);
    }

    @Test
    void testRemoveElementPreservesCrlf() throws Exception {
        String xml = crlfXml(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
                "<project>",
                "    <groupId>com.example</groupId>",
                "    <artifactId>test</artifactId>",
                "    <version>1.0</version>",
                "</project>",
                "");

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element version = doc.root().childElement("version").orElseThrow();
        editor.removeElement(version);

        String result = editor.toXml();

        assertFalse(result.contains("<version>"));
        assertNoCrlfCorruption(result);
    }

    @Test
    void testRemoveOnlyChildPreservesCrlf() throws Exception {
        String xml = crlfXml(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
                "<project>",
                "    <dependencies>",
                "        <dependency>",
                "            <groupId>junit</groupId>",
                "        </dependency>",
                "    </dependencies>",
                "</project>",
                "");

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element deps = doc.root().childElement("dependencies").orElseThrow();
        Element dep = deps.childElement("dependency").orElseThrow();
        editor.removeElement(dep);

        String result = editor.toXml();

        assertFalse(result.contains("<dependency>"));
        assertNoCrlfCorruption(result);
    }

    @Test
    void testAddAttributePreservesCrlf() throws Exception {
        String xml = crlfXml(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
                "<project>",
                "    <groupId>com.example</groupId>",
                "</project>",
                "");

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        editor.setAttribute(doc.root().childElement("groupId").orElseThrow(), "scope", "test");

        String result = editor.toXml();

        assertTrue(result.contains("scope"));
        assertNoCrlfCorruption(result);
    }

    @Test
    void testRemoveMiddleElementWithInlineNextPreservesCrlf() throws Exception {
        String xml = "<root>\r\n  <a/>\r\n  <b/> <c/>\r\n</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element b = doc.root().childElement("b").orElseThrow();
        editor.removeElement(b);

        String result = editor.toXml();

        assertTrue(result.contains("<a/>"));
        assertTrue(result.contains("<c/>"));
        assertFalse(result.contains("<b/>"));
        assertNoCrlfCorruption(result);
    }

    @Test
    void testMultipleModificationsPreserveCrlf() throws Exception {
        String xml = crlfXml(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
                "<project>",
                "    <groupId>com.example</groupId>",
                "    <artifactId>test</artifactId>",
                "    <version>1.0</version>",
                "    <packaging>jar</packaging>",
                "</project>",
                "");

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);

        doc.root().childElement("version").orElseThrow().textContent("2.0");
        editor.removeElement(doc.root().childElement("packaging").orElseThrow());
        editor.addElement(doc.root(), "name", "My Project");

        String result = editor.toXml();

        assertTrue(result.contains("2.0"));
        assertFalse(result.contains("<packaging>"));
        assertTrue(result.contains("<name>My Project</name>"));
        assertNoCrlfCorruption(result);
    }

    private void assertNoCrlfCorruption(String xml) {
        for (int i = 0; i < xml.length(); i++) {
            if (xml.charAt(i) == '\n' && (i == 0 || xml.charAt(i - 1) != '\r')) {
                int start = Math.max(0, i - 30);
                int end = Math.min(xml.length(), i + 30);
                String context = xml.substring(start, end).replace("\r", "\\r").replace("\n", "\\n");
                fail("Found bare LF (without CR) at position " + i + ", context: ..." + context + "...");
            }
        }
    }
}
