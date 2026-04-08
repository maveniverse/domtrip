package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Demonstration of the different serialization modes available in DomTrip.
 * This class shows practical examples of how to use preserve formatting,
 * pretty print, and raw modes.
 */
class SerializationModesDemo {

    @Test
    void demonstrateSerializationModes() throws DomTripException {
        // Create a sample document
        String originalXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<!-- Document comment -->\n"
                + "<root xmlns=\"http://example.com\">\n"
                + "  <element attr=\"value\">text content</element>\n"
                + "  <nested>\n"
                + "    <child>nested content</child>\n"
                + "  </nested>\n"
                + "  <!-- inline comment -->\n"
                + "  <empty/>\n"
                + "</root>";

        Document document = Document.of(originalXml);

        // 1. Preserve Formatting Mode (Default)
        Serializer preserveSerializer = new Serializer();
        String preserveResult = preserveSerializer.serialize(document);
        assertEquals(originalXml, preserveResult, "Preserve mode should produce identical output");

        // 2. Pretty Print Mode
        Serializer prettySerializer = new Serializer();
        prettySerializer.setPrettyPrint(true);
        prettySerializer.setIndentString("  "); // 2 spaces
        prettySerializer.setLineEnding("\n");
        String prettyResult = prettySerializer.serialize(document);
        assertNotNull(prettyResult, "Pretty print result should not be null");
        assertTrue(prettyResult.contains("<root"), "Pretty print should contain root element");

        // 3. Raw Mode (No Formatting)
        Serializer rawSerializer = new Serializer(DomTripConfig.raw());
        String rawResult = rawSerializer.serialize(document);
        assertNotNull(rawResult, "Raw mode result should not be null");
        assertFalse(rawResult.contains("\n"), "Raw mode should not contain line breaks");

        // 4. Custom Indentation
        Serializer tabSerializer = new Serializer();
        tabSerializer.setPrettyPrint(true);
        tabSerializer.setIndentString("\t");
        String tabResult = tabSerializer.serialize(document);
        assertTrue(tabResult.contains("\t"), "Tab indentation should use tab characters");

        // 5. Custom Line Endings
        Serializer customSerializer = new Serializer();
        customSerializer.setPrettyPrint(true);
        customSerializer.setIndentString("--");
        customSerializer.setLineEnding(" | ");
        String customResult = customSerializer.serialize(document);
        assertTrue(customResult.contains(" | "), "Custom line endings should be present");

        // 6. Modified Document Behavior
        Editor editor = new Editor(document);
        Element root = editor.root();
        editor.addElement(root, "newElement", "new content");

        String modifiedPreserve = preserveSerializer.serialize(document);
        assertTrue(modifiedPreserve.contains("newElement"), "Modified document should contain new element");

        String modifiedRaw = rawSerializer.serialize(document);
        assertTrue(modifiedRaw.contains("newElement"), "Raw mode should also contain new element");
    }

    @Test
    void demonstrateRawModeUseCases() throws DomTripException {
        // Use case 1: Minimizing file size
        String xml = "<config><setting name=\"debug\" value=\"true\"/><setting name=\"port\" value=\"8080\"/></config>";
        Document doc = Document.of(xml);

        Serializer rawSerializer = new Serializer(DomTripConfig.raw());
        String rawResult = rawSerializer.serialize(doc);
        assertEquals(xml, rawResult, "Raw mode should preserve single-line formatting");

        // Use case 2: Single-line output for logging
        Document logDoc = Document.withRootElement("log");
        Element entry = new Element("entry");
        entry.attribute("timestamp", "2024-01-01T12:00:00Z");
        entry.attribute("level", "INFO");
        entry.textContent("Application started");
        logDoc.root().addChild(entry);

        String logOutput = rawSerializer.serialize(logDoc);
        assertNotNull(logOutput, "Log output should not be null");
        assertTrue(logOutput.contains("Application started"), "Log output should contain text content");

        // Use case 3: Comparing with pretty print
        Serializer prettySerializer = new Serializer(DomTripConfig.prettyPrint());
        String prettyOutput = prettySerializer.serialize(logDoc);
        assertTrue(prettyOutput.contains("entry"), "Pretty output should contain entry element");
        assertTrue(
                prettyOutput.length() >= logOutput.length(),
                "Pretty output should generally be at least as long as raw");
    }
}
