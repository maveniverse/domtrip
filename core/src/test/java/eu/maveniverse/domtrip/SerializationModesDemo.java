package eu.maveniverse.domtrip;

import org.junit.jupiter.api.Test;

/**
 * Demonstration of the different serialization modes available in DomTrip.
 * This class shows practical examples of how to use preserve formatting,
 * pretty print, and raw modes.
 */
public class SerializationModesDemo {

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

        System.out.println("=== ORIGINAL XML ===");
        System.out.println(originalXml);
        System.out.println();

        // 1. Preserve Formatting Mode (Default)
        System.out.println("=== PRESERVE FORMATTING MODE (Default) ===");
        Serializer preserveSerializer = new Serializer();
        String preserveResult = preserveSerializer.serialize(document);
        System.out.println(preserveResult);
        System.out.println("Same as original: " + originalXml.equals(preserveResult));
        System.out.println();

        // 2. Pretty Print Mode
        System.out.println("=== PRETTY PRINT MODE ===");
        Serializer prettySerializer = new Serializer();
        prettySerializer.setPrettyPrint(true);
        prettySerializer.setIndentString("  "); // 2 spaces
        prettySerializer.setLineEnding("\n");
        String prettyResult = prettySerializer.serialize(document);
        System.out.println(prettyResult);
        System.out.println();

        // 3. Raw Mode (No Formatting)
        System.out.println("=== RAW MODE (No Formatting) ===");
        Serializer rawSerializer = new Serializer(DomTripConfig.raw());
        String rawResult = rawSerializer.serialize(document);
        System.out.println(rawResult);
        System.out.println("Contains line breaks: " + rawResult.contains("\n"));
        System.out.println("Length: " + rawResult.length() + " characters");
        System.out.println();

        // 4. Custom Indentation
        System.out.println("=== CUSTOM INDENTATION (Tabs) ===");
        Serializer tabSerializer = new Serializer();
        tabSerializer.setPrettyPrint(true);
        tabSerializer.setIndentString("\t");
        String tabResult = tabSerializer.serialize(document);
        System.out.println(tabResult.replace("\t", "[TAB]")); // Show tabs visually
        System.out.println();

        // 5. Custom Line Endings
        System.out.println("=== CUSTOM LINE ENDINGS ===");
        Serializer customSerializer = new Serializer();
        customSerializer.setPrettyPrint(true);
        customSerializer.setIndentString("--");
        customSerializer.setLineEnding(" | ");
        String customResult = customSerializer.serialize(document);
        System.out.println(customResult);
        System.out.println();

        // 6. Modified Document Behavior
        System.out.println("=== MODIFIED DOCUMENT BEHAVIOR ===");
        Editor editor = new Editor(document);
        Element root = editor.root();
        editor.addElement(root, "newElement", "new content");

        System.out.println("Preserve mode with modified document:");
        String modifiedPreserve = preserveSerializer.serialize(document);
        System.out.println(modifiedPreserve);
        System.out.println();

        System.out.println("Pretty print mode with modified document:");
        String modifiedPretty = prettySerializer.serialize(document);
        System.out.println(modifiedPretty);
        System.out.println();

        System.out.println("Raw mode with modified document:");
        String modifiedRaw = rawSerializer.serialize(document);
        System.out.println(modifiedRaw);
        System.out.println("Contains line breaks: " + modifiedRaw.contains("\n"));
    }

    @Test
    void demonstrateRawModeUseCases() throws DomTripException {
        System.out.println("=== RAW MODE USE CASES ===");

        // Use case 1: Minimizing file size
        String xml = "<config><setting name=\"debug\" value=\"true\"/><setting name=\"port\" value=\"8080\"/></config>";
        Document doc = Document.of(xml);

        Serializer rawSerializer = new Serializer(DomTripConfig.raw());
        String rawResult = rawSerializer.serialize(doc);

        System.out.println("Original: " + xml);
        System.out.println("Raw mode: " + rawResult);
        System.out.println("Same content: " + xml.equals(rawResult));
        System.out.println();

        // Use case 2: Single-line output for logging
        Document logDoc = Document.withRootElement("log");
        Element entry = new Element("entry");
        entry.attribute("timestamp", "2024-01-01T12:00:00Z");
        entry.attribute("level", "INFO");
        entry.textContent("Application started");
        logDoc.root().addChild(entry);

        String logOutput = rawSerializer.serialize(logDoc);
        System.out.println("Log entry (single line): " + logOutput);
        System.out.println();

        // Use case 3: Comparing with pretty print
        Serializer prettySerializer = new Serializer(DomTripConfig.prettyPrint());
        String prettyOutput = prettySerializer.serialize(logDoc);
        System.out.println("Same content, pretty printed:");
        System.out.println(prettyOutput);
    }
}
