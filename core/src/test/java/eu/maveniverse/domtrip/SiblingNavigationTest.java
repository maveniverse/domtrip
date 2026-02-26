package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for sibling navigation methods (Issue #131).
 */
class SiblingNavigationTest {

    @Test
    void testNextSibling() throws DomTripException {
        String xml = "<root><a/><b/><c/></root>";
        Document doc = Document.of(xml);
        Element root = doc.root();

        Element a = root.childElement("a").orElseThrow();
        Element b = root.childElement("b").orElseThrow();
        Element c = root.childElement("c").orElseThrow();

        assertEquals(b, a.nextSibling().orElse(null));
        assertEquals(c, b.nextSibling().orElse(null));
        assertTrue(c.nextSibling().isEmpty());
    }

    @Test
    void testPreviousSibling() throws DomTripException {
        String xml = "<root><a/><b/><c/></root>";
        Document doc = Document.of(xml);
        Element root = doc.root();

        Element a = root.childElement("a").orElseThrow();
        Element b = root.childElement("b").orElseThrow();
        Element c = root.childElement("c").orElseThrow();

        assertTrue(a.previousSibling().isEmpty());
        assertEquals(a, b.previousSibling().orElse(null));
        assertEquals(b, c.previousSibling().orElse(null));
    }

    @Test
    void testSiblingIndexBasic() throws DomTripException {
        String xml = "<root><a/><b/><c/></root>";
        Document doc = Document.of(xml);
        Element root = doc.root();

        Element a = root.childElement("a").orElseThrow();
        Element b = root.childElement("b").orElseThrow();
        Element c = root.childElement("c").orElseThrow();

        assertEquals(0, a.siblingIndex());
        assertEquals(1, b.siblingIndex());
        assertEquals(2, c.siblingIndex());
    }

    @Test
    void testSiblingIndexNoParent() {
        Element orphan = Element.of("orphan");
        assertEquals(-1, orphan.siblingIndex());
    }

    @Test
    void testSiblingsWithMixedNodeTypes() throws DomTripException {
        String xml = "<root><!-- comment --><a/>text<b/></root>";
        Document doc = Document.of(xml);
        Element root = doc.root();

        Element a = root.childElement("a").orElseThrow();

        // a's previous sibling should be the comment
        assertInstanceOf(Comment.class, a.previousSibling().orElseThrow());

        // a's next sibling should be text
        assertInstanceOf(Text.class, a.nextSibling().orElseThrow());
    }

    @Test
    void testNextSiblingElement() throws DomTripException {
        String xml = "<root><!-- comment --><a/>text<b/><!-- another --><c/></root>";
        Document doc = Document.of(xml);
        Element root = doc.root();

        Element a = root.childElement("a").orElseThrow();
        Element b = root.childElement("b").orElseThrow();
        Element c = root.childElement("c").orElseThrow();

        // nextSiblingElement skips non-element nodes
        assertEquals(b, a.nextSiblingElement().orElse(null));
        assertEquals(c, b.nextSiblingElement().orElse(null));
        assertTrue(c.nextSiblingElement().isEmpty());
    }

    @Test
    void testPreviousSiblingElement() throws DomTripException {
        String xml = "<root><a/><!-- comment -->text<b/><!-- another --><c/></root>";
        Document doc = Document.of(xml);
        Element root = doc.root();

        Element a = root.childElement("a").orElseThrow();
        Element b = root.childElement("b").orElseThrow();
        Element c = root.childElement("c").orElseThrow();

        assertTrue(a.previousSiblingElement().isEmpty());
        assertEquals(a, b.previousSiblingElement().orElse(null));
        assertEquals(b, c.previousSiblingElement().orElse(null));
    }

    @Test
    void testSiblingNavigationNoParent() {
        Element orphan = Element.of("orphan");
        assertTrue(orphan.nextSibling().isEmpty());
        assertTrue(orphan.previousSibling().isEmpty());
        assertTrue(orphan.nextSiblingElement().isEmpty());
        assertTrue(orphan.previousSiblingElement().isEmpty());
    }

    @Test
    void testSiblingNavigationWithWhitespace() throws DomTripException {
        String xml = "<root>\n    <a/>\n    <b/>\n    <c/>\n</root>";
        Document doc = Document.of(xml);
        Element root = doc.root();

        Element a = root.childElement("a").orElseThrow();
        Element b = root.childElement("b").orElseThrow();
        Element c = root.childElement("c").orElseThrow();

        // With whitespace normalization, elements should be direct siblings
        assertEquals(b, a.nextSiblingElement().orElse(null));
        assertEquals(c, b.nextSiblingElement().orElse(null));
        assertEquals(a, b.previousSiblingElement().orElse(null));
        assertEquals(b, c.previousSiblingElement().orElse(null));
    }

    @Test
    void testSiblingNavigationSingleChild() throws DomTripException {
        String xml = "<root><only/></root>";
        Document doc = Document.of(xml);
        Element only = doc.root().childElement("only").orElseThrow();

        assertTrue(only.nextSibling().isEmpty());
        assertTrue(only.previousSibling().isEmpty());
        assertTrue(only.nextSiblingElement().isEmpty());
        assertTrue(only.previousSiblingElement().isEmpty());
        assertEquals(0, only.siblingIndex());
    }
}
