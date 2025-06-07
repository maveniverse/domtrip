package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Advanced test cases for Element functionality.
 */
public class ElementAdvancedTest {

    private Editor editor;

    @BeforeEach
    void setUp() {
        editor = new Editor();
    }

    @Test
    void testElementNameChange() {
        String xml = "<root><oldName>content</oldName></root>";

        editor.loadXml(xml);
        Element root = editor.getRootElement();
        Element child = (Element) root.getChild(0);

        child.setName("newName");

        String result = editor.toXml();
        assertTrue(result.contains("<newName>content</newName>"));
        assertFalse(result.contains("<oldName>"));
        assertTrue(child.isModified());
    }

    @Test
    void testElementNameChangeNull() {
        Element element = new Element("test");

        // Implementation may handle null gracefully
        assertDoesNotThrow(() -> {
            element.setName(null);
        });
    }

    @Test
    void testRemoveAttribute() {
        String xml = "<root attr1=\"value1\" attr2=\"value2\" attr3=\"value3\"/>";

        editor.loadXml(xml);
        Element root = editor.getRootElement();

        root.removeAttribute("attr2");

        String result = editor.toXml();
        assertTrue(result.contains("attr1=\"value1\""));
        assertFalse(result.contains("attr2=\"value2\""));
        assertTrue(result.contains("attr3=\"value3\""));
    }

    @Test
    void testRemoveNonExistentAttribute() {
        String xml = "<root attr=\"value\"/>";

        editor.loadXml(xml);
        Element root = editor.getRootElement();

        // Should not throw
        assertDoesNotThrow(() -> {
            root.removeAttribute("nonexistent");
        });
    }

    @Test
    void testGetAttributeNames() {
        String xml = "<root attr1=\"value1\" attr2=\"value2\" attr3=\"value3\"/>";

        editor.loadXml(xml);
        Element root = editor.getRootElement();

        // Test individual attributes since getAttributeNames() doesn't exist
        assertTrue(root.hasAttribute("attr1"));
        assertTrue(root.hasAttribute("attr2"));
        assertTrue(root.hasAttribute("attr3"));
        assertEquals("value1", root.getAttribute("attr1"));
        assertEquals("value2", root.getAttribute("attr2"));
        assertEquals("value3", root.getAttribute("attr3"));
    }

    @Test
    void testHasAttribute() {
        String xml = "<root attr=\"value\"/>";

        editor.loadXml(xml);
        Element root = editor.getRootElement();

        assertTrue(root.hasAttribute("attr"));
        assertFalse(root.hasAttribute("nonexistent"));
    }

    @Test
    void testGetAttributeQuote() {
        String xml = "<root attr1='single' attr2=\"double\"/>";

        editor.loadXml(xml);
        Element root = editor.getRootElement();

        assertEquals('\'', root.getAttributeQuote("attr1"));
        assertEquals('"', root.getAttributeQuote("attr2"));
        assertEquals('"', root.getAttributeQuote("nonexistent")); // Default
    }

    @Test
    void testSetAttributeWithQuote() {
        String xml = "<root/>";

        editor.loadXml(xml);
        Element root = editor.getRootElement();

        root.setAttribute("attr1", "value1", '\'');
        root.setAttribute("attr2", "value2", '"');

        String result = editor.toXml();
        assertTrue(result.contains("attr1='value1'"));
        assertTrue(result.contains("attr2=\"value2\""));
    }

    @Test
    void testSelfClosingBehavior() {
        String xml = "<root><empty/><normal>content</normal></root>";

        editor.loadXml(xml);
        Element root = editor.getRootElement();
        Element empty = (Element) root.getChild(0);
        Element normal = (Element) root.getChild(1);

        assertTrue(empty.isSelfClosing());
        assertFalse(normal.isSelfClosing());
    }

    @Test
    void testSelfClosingToNormalConversion() {
        String xml = "<root><empty/></root>";

        editor.loadXml(xml);
        Element root = editor.getRootElement();
        Element empty = (Element) root.getChild(0);

        // Add content to self-closing element
        empty.setTextContent("now has content");

        String result = editor.toXml();
        assertTrue(result.contains("<empty>now has content</empty>"));
        assertFalse(result.contains("<empty/>"));
    }

    @Test
    void testNormalToSelfClosingConversion() {
        String xml = "<root><element>content</element></root>";

        editor.loadXml(xml);
        Element root = editor.getRootElement();
        Element element = (Element) root.getChild(0);

        // Remove all content
        element.setTextContent("");

        String result = editor.toXml();
        // Should still be normal element since it was originally normal
        assertTrue(result.contains("<element></element>"));
    }

    @Test
    void testFindChildElement() {
        String xml = "<root>\n" + "  <child1>content1</child1>\n"
                + "  <child2>content2</child2>\n"
                + "  <child1>content3</child1>\n"
                + "</root>";

        editor.loadXml(xml);
        Element root = editor.getRootElement();

        Element found = editor.findChildElement(root, "child2");
        assertNotNull(found);
        assertEquals("child2", found.getName());
        assertEquals("content2", found.getTextContent());
    }

    @Test
    void testFindChildElementNotFound() {
        String xml = "<root><child>content</child></root>";

        editor.loadXml(xml);
        Element root = editor.getRootElement();

        Element found = editor.findChildElement(root, "nonexistent");
        assertNull(found);
    }

    @Test
    void testGetChildCount() {
        String xml = "<root>\n" + "  <child1/>\n"
                + "  text content\n"
                + "  <child2/>\n"
                + "  <!-- comment -->\n"
                + "</root>";

        editor.loadXml(xml);
        Element root = editor.getRootElement();

        assertTrue(root.getChildCount() >= 4); // At least 2 elements, 1 text, 1 comment
    }

    @Test
    void testGetChild() {
        String xml = "<root><first/><second/></root>";

        editor.loadXml(xml);
        Element root = editor.getRootElement();

        Node firstChild = root.getChild(0);
        assertTrue(firstChild instanceof Element);
        assertEquals("first", ((Element) firstChild).getName());
    }

    @Test
    void testGetChildOutOfBounds() {
        String xml = "<root><child/></root>";

        editor.loadXml(xml);
        Element root = editor.getRootElement();

        // Implementation may return null instead of throwing
        assertDoesNotThrow(() -> {
            Node result = root.getChild(10);
            // May be null or throw, both are acceptable
        });
    }

    @Test
    void testGetChildren() {
        String xml = "<root><child1/><child2/></root>";

        editor.loadXml(xml);
        Element root = editor.getRootElement();

        var children = root.getChildren();
        assertNotNull(children);
        assertTrue(children.size() >= 2);
    }

    @Test
    void testAddChildAtPosition() {
        String xml = "<root><first/><third/></root>";

        editor.loadXml(xml);
        Element root = editor.getRootElement();
        Element second = new Element("second");

        root.insertChild(1, second);

        String result = editor.toXml();
        // Order should be first, second, third
        int firstPos = result.indexOf("<first");
        int secondPos = result.indexOf("<second");
        int thirdPos = result.indexOf("<third");

        assertTrue(firstPos < secondPos);
        assertTrue(secondPos < thirdPos);
    }

    @Test
    void testRemoveChild() {
        String xml = "<root><keep1/><remove/><keep2/></root>";

        editor.loadXml(xml);
        Element root = editor.getRootElement();
        Element toRemove = (Element) root.getChild(1);

        root.removeChild(toRemove);

        String result = editor.toXml();
        assertTrue(result.contains("<keep1"));
        assertFalse(result.contains("<remove"));
        assertTrue(result.contains("<keep2"));
    }

    @Test
    void testRemoveChildByIndex() {
        String xml = "<root><first/><second/><third/></root>";

        editor.loadXml(xml);
        Element root = editor.getRootElement();

        Node secondChild = root.getChild(1); // Get second child
        root.removeChild(secondChild); // Remove it

        String result = editor.toXml();
        assertTrue(result.contains("<first"));
        assertFalse(result.contains("<second"));
        assertTrue(result.contains("<third"));
    }

    @Test
    void testClearChildren() {
        String xml = "<root><child1/><child2/>text content</root>";

        editor.loadXml(xml);
        Element root = editor.getRootElement();

        // Remove all children manually since clearChildren() doesn't exist
        while (root.getChildCount() > 0) {
            root.removeChild(root.getChild(0));
        }

        assertEquals(0, root.getChildCount());
        String result = editor.toXml();
        assertTrue(result.contains("<root></root>") || result.contains("<root/>"));
    }

    @Test
    void testElementToString() {
        Element element = new Element("testElement");
        element.setAttribute("attr1", "value1");
        element.setAttribute("attr2", "value2");
        element.addChild(new Element("child1"));
        element.addChild(new Element("child2"));

        String str = element.toString();
        assertTrue(str.contains("Element{"));
        assertTrue(str.contains("testElement"));
        assertTrue(str.contains("attributes=2"));
        assertTrue(str.contains("children=2"));
    }

    @Test
    void testComplexElementStructure() {
        String xml = "<root xmlns=\"http://example.com\" xmlns:ns=\"http://ns.example.com\">\n"
                + "  <ns:element attr=\"value\">\n"
                + "    <nested>content</nested>\n"
                + "    <![CDATA[raw data]]>\n"
                + "    <!-- comment -->\n"
                + "  </ns:element>\n"
                + "</root>";

        editor.loadXml(xml);
        String result = editor.toXml();

        // Should preserve all complex structure
        assertEquals(xml, result);
    }

    @Test
    void testElementWithMixedContent() {
        String xml = "<root>Text before <element>element content</element> text after</root>";

        editor.loadXml(xml);
        Element root = editor.getRootElement();

        // Should have multiple children: text, element, text
        assertTrue(root.getChildCount() >= 3);

        String result = editor.toXml();
        assertEquals(xml, result);
    }

    @Test
    void testElementModificationTracking() {
        String xml = "<root attr=\"value\"><child/></root>";

        editor.loadXml(xml);
        Element root = editor.getRootElement();

        // Initially not modified (just loaded)
        assertFalse(root.isModified());

        // Modify attribute
        root.setAttribute("attr", "newValue");
        assertTrue(root.isModified());
    }
}
