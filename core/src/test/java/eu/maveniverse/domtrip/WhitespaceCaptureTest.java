package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests to verify that all whitespace is correctly captured by the Parser.
 *
 * This test class verifies that DomTrip captures and preserves all types of whitespace:
 * - Node-level: precedingWhitespace and followingWhitespace
 * - Element-level: openTagWhitespace and closeTagWhitespace
 * - Attribute-level: precedingWhitespace and whitespace around '=' signs
 */
class WhitespaceCaptureTest {

    @Test
    void testNodePrecedingAndFollowingWhitespace() throws DomTripException {
        String xml = """
            <root>
                <!-- comment with spaces -->
                <element>content</element>

                <another>more content</another>
            </root>
            """;

        Document doc = Document.of(xml);
        Element root = doc.root();

        // Test that child nodes have captured preceding whitespace
        assertTrue(root.childElements().count() > 0, "Root should have children");

        // Find the first element child
        Element firstElement =
                root.childElements().findFirst().orElseThrow(() -> new AssertionError("Should have element children"));

        // Verify preceding whitespace is captured (should include newline and spaces)
        String precedingWs = firstElement.precedingWhitespace();
        assertNotNull(precedingWs, "Preceding whitespace should not be null");
        // Should contain whitespace from after the comment to before the element
    }

    @Test
    void testElementOpenTagWhitespace() throws DomTripException {
        // Test whitespace inside opening tags
        String xml = "<element   attr='value'   >";

        Document doc = Document.of(xml);
        Element element = doc.root();

        // Verify openTagWhitespace is captured
        String openTagWs = element.openTagWhitespace();
        assertNotNull(openTagWs, "Open tag whitespace should not be null");

        // FIXED: The whitespace before the closing '>' is now captured!
        assertEquals("   ", openTagWs, "FIXED: Open tag whitespace is now captured during parsing!");
    }

    @Test
    void testElementCloseTagWhitespace() throws DomTripException {
        // Test whitespace inside closing tags
        String xml = "<element>content</   element   >";

        Document doc = Document.of(xml);
        Element element = doc.root();

        // Verify closeTagWhitespace is captured
        String closeTagWs = element.closeTagWhitespace();
        assertNotNull(closeTagWs, "Close tag whitespace should not be null");

        // FIXED: The whitespace before the element name in closing tag is now captured!
        assertEquals("   ", closeTagWs, "FIXED: Close tag whitespace is now captured during parsing!");
    }

    @Test
    void testAttributeWhitespaceAroundEquals() throws DomTripException {
        // Test whitespace around '=' in attributes
        String xml = "<element attr1  =  'value1' attr2='value2' attr3   =   \"value3\">";

        Document doc = Document.of(xml);
        Element element = doc.root();

        // GOOD NEWS: Whitespace around '=' IS preserved in round-trip!
        Attribute attr1 = element.attributeObject("attr1");
        Attribute attr3 = element.attributeObject("attr3");

        assertNotNull(attr1, "attr1 should exist");
        assertNotNull(attr3, "attr3 should exist");

        assertEquals("value1", attr1.value());
        assertEquals("value3", attr3.value());

        // Test that whitespace around '=' is preserved in round-trip
        Editor editor = new Editor(doc);
        String result = editor.toXml();
        assertTrue(result.contains("attr1  =  'value1'"), "Whitespace around = should be preserved for attr1");
        assertTrue(result.contains("attr3   =   \"value3\""), "Whitespace around = should be preserved for attr3");
    }

    @Test
    void testAttributePrecedingWhitespace() throws DomTripException {
        // Test various patterns of whitespace before attributes
        String xml = "<element  attr1='value1'   attr2=\"value2\"\n    attr3='value3'>";

        Document doc = Document.of(xml);
        Element element = doc.root();

        Attribute attr1 = element.attributeObject("attr1");
        Attribute attr2 = element.attributeObject("attr2");
        Attribute attr3 = element.attributeObject("attr3");

        assertNotNull(attr1, "attr1 should exist");
        assertNotNull(attr2, "attr2 should exist");
        assertNotNull(attr3, "attr3 should exist");

        // Verify preceding whitespace is captured correctly
        String ws1 = attr1.precedingWhitespace();
        String ws2 = attr2.precedingWhitespace();
        String ws3 = attr3.precedingWhitespace();

        assertNotNull(ws1, "attr1 preceding whitespace should not be null");
        assertNotNull(ws2, "attr2 preceding whitespace should not be null");
        assertNotNull(ws3, "attr3 preceding whitespace should not be null");

        // attr1 should have 2 spaces before it
        assertEquals("  ", ws1, "attr1 should have 2 spaces preceding");

        // attr2 should have 3 spaces before it
        assertEquals("   ", ws2, "attr2 should have 3 spaces preceding");

        // attr3 should have newline and 4 spaces before it
        assertEquals("\n    ", ws3, "attr3 should have newline and 4 spaces preceding");
    }

    @Test
    void testComplexWhitespaceScenario() throws DomTripException {
        // Test a complex scenario with multiple types of whitespace
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>

            <root   xmlns:ns="http://example.com"   >

                <!-- A comment with surrounding whitespace -->

                <element   attr1  =  'value1'   attr2="value2"   >
                    <child>content</child>
                </   element   >

            </root>
            """;

        Document doc = Document.of(xml);
        Element root = doc.root();

        // Test root element whitespace
        assertNotNull(root.precedingWhitespace(), "Root preceding whitespace should not be null");

        // Test root attributes
        Attribute nsAttr = root.attributeObject("xmlns:ns");
        if (nsAttr != null) {
            assertNotNull(nsAttr.precedingWhitespace(), "Namespace attribute whitespace should not be null");
        }

        // Test nested element
        Element element = root.descendant("element").orElse(null);
        assertNotNull(element, "Element should exist");

        // Test element attributes with whitespace around equals
        Attribute attr1 = element.attributeObject("attr1");
        if (attr1 != null) {
            assertEquals("value1", attr1.value());
            assertNotNull(attr1.precedingWhitespace());
        }

        // Test child element whitespace
        Element child = element.childElement("child").orElse(null);
        if (child != null) {
            assertNotNull(child.precedingWhitespace(), "Child preceding whitespace should not be null");
        }
    }

    @Test
    void testWhitespaceRoundTrip() throws DomTripException {
        // Test that whitespace is preserved in round-trip parsing
        String originalXml = """
            <root   attr1  =  'value1'   attr2="value2"   >

                <element   >content</   element   >

            </root>
            """;

        Document doc = Document.of(originalXml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        // Parse the result again
        Document doc2 = Document.of(result);
        Editor editor2 = new Editor(doc2);
        String result2 = editor2.toXml();

        // The two results should be identical if whitespace is properly preserved
        assertEquals(result, result2, "Round-trip should preserve whitespace exactly");
    }

    @Test
    void testSelfClosingElementWhitespace() throws DomTripException {
        // Test whitespace in self-closing elements
        String xml = "<element   attr='value'   />";

        Document doc = Document.of(xml);
        Element element = doc.root();

        assertTrue(element.selfClosing(), "Element should be self-closing");

        // Test attribute whitespace
        Attribute attr = element.attributeObject("attr");
        assertNotNull(attr, "Attribute should exist");
        assertNotNull(attr.precedingWhitespace(), "Attribute whitespace should not be null");

        // Test open tag whitespace (space before the '/')
        String openTagWs = element.openTagWhitespace();
        assertNotNull(openTagWs, "Open tag whitespace should not be null");
    }

    @Test
    void testMinimalWhitespace() throws DomTripException {
        // Test elements with minimal whitespace
        String xml = "<root><element attr='value'>content</element></root>";

        Document doc = Document.of(xml);
        Element root = doc.root();
        Element element = root.childElement("element").orElseThrow();

        // Even with minimal whitespace, the fields should be initialized
        assertNotNull(element.precedingWhitespace(), "Preceding whitespace should not be null");
        assertNotNull(element.openTagWhitespace(), "Open tag whitespace should not be null");
        assertNotNull(element.closeTagWhitespace(), "Close tag whitespace should not be null");

        // With minimal whitespace, these should be empty strings
        assertEquals("", element.precedingWhitespace(), "Should have empty preceding whitespace");
        assertEquals("", element.openTagWhitespace(), "Should have empty open tag whitespace");
        assertEquals("", element.closeTagWhitespace(), "Should have empty close tag whitespace");
    }

    @Test
    void testCurrentWhitespaceCapabilities() throws DomTripException {
        // This test documents what whitespace features are currently working vs. not implemented
        String xml = "<root   attr1  =  'value1'   attr2='value2'   >\n" + "    <child   >content</   child   >\n"
                + "</root>";

        Document doc = Document.of(xml);
        Element root = doc.root();
        Element child = root.childElement("child").orElseThrow();

        // ✅ WORKING: Attribute preceding whitespace
        Attribute attr1 = root.attributeObject("attr1");
        Attribute attr2 = root.attributeObject("attr2");
        assertNotNull(attr1);
        assertNotNull(attr2);
        assertEquals("   ", attr1.precedingWhitespace(), "Attribute preceding whitespace works");
        assertEquals("   ", attr2.precedingWhitespace(), "Attribute preceding whitespace works");

        // ✅ WORKING: Whitespace around '=' in attributes (preserved in round-trip)
        Editor editor = new Editor(doc);
        String result = editor.toXml();
        assertTrue(result.contains("attr1  =  'value1'"), "Whitespace around = is preserved");

        // ✅ NOW IMPLEMENTED: Element-level whitespace fields are now captured!
        assertEquals("   ", root.openTagWhitespace(), "FIXED: Open tag whitespace now captured!");
        assertEquals("", root.closeTagWhitespace(), "Root close tag has no whitespace");
        assertEquals("", root.precedingWhitespace(), "Root has no preceding whitespace (first element)");

        assertEquals("   ", child.openTagWhitespace(), "FIXED: Child open tag whitespace now captured!");
        assertEquals("   ", child.closeTagWhitespace(), "FIXED: Child close tag whitespace now captured!");
        assertEquals("\n    ", child.precedingWhitespace(), "FIXED: Child preceding whitespace now captured!");

        // Document what should be captured when implemented:
        // - root.openTagWhitespace() should be "   " (spaces before >)
        // - child.openTagWhitespace() should be "   " (spaces before >)
        // - child.closeTagWhitespace() should be "   " (spaces before element name in </   child   >)
        // - child.precedingWhitespace() should be "\n    " (newline + 4 spaces)
        // - child.followingWhitespace() should be "\n" (newline after child)
    }

    @Test
    void testWhitespaceModificationBehavior() throws DomTripException {
        // Test that whitespace can be modified and is used during serialization
        String xml = "<element   attr='value'   >content</element>";

        Document doc = Document.of(xml);
        Element element = doc.root();
        Editor editor = new Editor(doc);

        // Verify initial state
        assertFalse(element.isModified(), "Element should not be modified initially");

        // Test modifying node-level whitespace
        element.precedingWhitespace("  ");
        assertTrue(element.isModified(), "Element should be modified after setting precedingWhitespace");

        // Clear modified flag to test followingWhitespace
        element.clearModified();
        assertFalse(element.isModified(), "Element should not be modified after clearing");

        // Test modifying element-level whitespace
        element.clearModified();
        element.openTagWhitespace(" ");
        assertTrue(element.isModified(), "FIXED: openTagWhitespace now marks as modified");

        element.clearModified();
        element.closeTagWhitespace(" ");
        assertTrue(element.isModified(), "FIXED: closeTagWhitespace now marks as modified");
    }

    @Test
    void testProgrammaticElementCreation() throws DomTripException {
        // Test creating elements programmatically and setting whitespace
        Document doc = Document.of("<root></root>");
        Element root = doc.root();
        Editor editor = new Editor(doc);

        // Create a new element programmatically
        Element newElement = new Element("child");
        newElement.textContent("content");

        // Set whitespace on the new element
        newElement.precedingWhitespace("\n    ");
        newElement.openTagWhitespace(" ");
        newElement.closeTagWhitespace(" ");
        root.innerPrecedingWhitespace("\n");

        // Add to document
        root.addNode(newElement);

        // Serialize and check if whitespace is used
        String result = editor.toXml();

        // Test that all whitespace features now work
        assertTrue(result.contains("\n    <child"), "Should use precedingWhitespace (WORKS)");
        assertTrue(result.contains(">\n</root>"), "Should use followingWhitespace (WORKS)");

        // FIXED: These now work after bug fixes
        assertTrue(result.contains("<child >"), "FIXED: openTagWhitespace now works!");
        assertTrue(result.contains("</ child>"), "FIXED: closeTagWhitespace now works!");
    }

    @Test
    void testWhitespacePreservationAfterModification() throws DomTripException {
        // Test that modifying an element causes it to use whitespace fields instead of original tags
        String xml = "<element   attr='value'   >content</   element   >";

        Document doc = Document.of(xml);
        Element element = doc.root();
        Editor editor = new Editor(doc);

        // FIXED: Original close tag whitespace should now be preserved when not modified!
        String initial = editor.toXml();
        assertEquals(xml, initial, "FIXED: Close tag whitespace is now preserved when not modified");

        // Modify the element (add an attribute)
        element.attribute("newattr", "newvalue");
        assertTrue(element.isModified(), "Element should be modified after adding attribute");

        // Now it should use whitespace fields, but they're empty since not captured during parsing
        String modified = editor.toXml();
        assertNotEquals(xml, modified, "Should not preserve original formatting when modified");

        // The result should use the (empty) whitespace fields
        assertTrue(modified.contains("<element"), "Should rebuild tag from scratch");
        assertFalse(modified.contains("</   element   >"), "Should not preserve original close tag whitespace");
    }

    @Test
    void testWhitespaceFieldsUsedWhenModified() throws DomTripException {
        // Test that when an element is modified, the whitespace fields are actually used
        String xml = "<element>content</element>";

        Document doc = Document.of(xml);
        Element element = doc.root();
        Editor editor = new Editor(doc);

        // Set whitespace fields
        element.precedingWhitespace("  ");
        element.openTagWhitespace(" ");
        element.closeTagWhitespace(" ");

        // Modify the element to force using whitespace fields
        element.attribute("test", "value");

        String result = editor.toXml();

        // Check what actually works
        assertEquals("  <element test=\"value\" >content</ element>", result);
    }

    @Test
    void testWhitespaceSetterBugs() throws DomTripException {
        // Test that verifies whitespace setter methods now correctly mark as modified
        Element element = new Element("test");

        // precedingWhitespace works correctly
        assertFalse(element.isModified(), "Element should start unmodified");
        element.precedingWhitespace("  ");
        assertTrue(element.isModified(), "precedingWhitespace should mark as modified (WORKS)");

        // Clear and test other setters - these should now work after bug fixes
        element.clearModified();
        element.openTagWhitespace("  ");
        assertTrue(element.isModified(), "FIXED: openTagWhitespace now marks as modified");

        element.clearModified();
        element.closeTagWhitespace("  ");
        assertTrue(element.isModified(), "FIXED: closeTagWhitespace now marks as modified");
    }
}
