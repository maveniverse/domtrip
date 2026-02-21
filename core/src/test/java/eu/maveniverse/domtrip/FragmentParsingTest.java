package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for XML fragment parsing support (Issue #128).
 */
class FragmentParsingTest {

    @Test
    void testParseFragmentWithMultipleElements() throws DomTripException {
        List<Node> nodes = Document.parseFragment("<foo>bar</foo><bar>baz</bar>");
        assertEquals(2, nodes.size());
        assertInstanceOf(Element.class, nodes.get(0));
        assertInstanceOf(Element.class, nodes.get(1));
        assertEquals("foo", ((Element) nodes.get(0)).name());
        assertEquals("bar", ((Element) nodes.get(1)).name());
    }

    @Test
    void testParseFragmentWithCommentAndElements() throws DomTripException {
        String fragment = "<!-- This is a fragment starting with a comment -->\n<foo>bar</foo>\n<bar>baz</bar>";
        List<Node> nodes = Document.parseFragment(fragment);
        assertEquals(3, nodes.size());
        assertInstanceOf(Comment.class, nodes.get(0));
        assertInstanceOf(Element.class, nodes.get(1));
        assertInstanceOf(Element.class, nodes.get(2));
    }

    @Test
    void testParseFragmentWithTrailingText() throws DomTripException {
        String fragment = "<foo>bar</foo>\n... and ending with a text node";
        List<Node> nodes = Document.parseFragment(fragment);
        assertTrue(nodes.size() >= 2);
        assertInstanceOf(Element.class, nodes.get(0));
        // Last node should be text
        Node last = nodes.get(nodes.size() - 1);
        assertInstanceOf(Text.class, last);
        assertTrue(((Text) last).content().contains("and ending with a text node"));
    }

    @Test
    void testParseFragmentSingleElement() throws DomTripException {
        List<Node> nodes = Document.parseFragment("<root>content</root>");
        assertEquals(1, nodes.size());
        assertInstanceOf(Element.class, nodes.get(0));
        assertEquals("root", ((Element) nodes.get(0)).name());
    }

    @Test
    void testParseFragmentWithProcessingInstruction() throws DomTripException {
        String fragment = "<?xml-stylesheet type=\"text/xsl\" href=\"style.xsl\"?>\n<root/>";
        List<Node> nodes = Document.parseFragment(fragment);
        assertTrue(nodes.size() >= 2);
        assertInstanceOf(ProcessingInstruction.class, nodes.get(0));
    }

    @Test
    void testParseFragmentPreservesWhitespace() throws DomTripException {
        String fragment = "    <foo>bar</foo>\n    <bar>baz</bar>\n";
        List<Node> nodes = Document.parseFragment(fragment);
        // Verify that whitespace is preserved
        Element first = (Element) nodes.get(0);
        assertFalse(first.precedingWhitespace().isEmpty(), "First element should have preceding whitespace");
    }

    @Test
    void testParseFragmentRoundTrip() throws DomTripException {
        String fragment = "<!-- comment -->\n<foo>bar</foo>\n<bar>baz</bar>";
        List<Node> nodes = Document.parseFragment(fragment);

        // Rebuild fragment from nodes
        StringBuilder sb = new StringBuilder();
        for (Node node : nodes) {
            node.toXml(sb);
        }
        assertEquals(fragment, sb.toString());
    }

    @Test
    void testParseFragmentEmpty() throws DomTripException {
        List<Node> nodes = Document.parseFragment("");
        assertTrue(nodes.isEmpty());
    }

    @Test
    void testParseFragmentNull() throws DomTripException {
        List<Node> nodes = Document.parseFragment(null);
        assertTrue(nodes.isEmpty());
    }

    @Test
    void testDocumentOfNodesEquivalence() throws DomTripException {
        // Verify that Document.of(xml).nodes().toList() gives the same result
        String fragment = "<!-- comment -->\n<foo>bar</foo>\n<bar>baz</bar>";
        List<Node> fromFragment = Document.parseFragment(fragment);
        List<Node> fromDocument = Document.of(fragment).nodes().toList();
        assertEquals(fromDocument.size(), fromFragment.size());
        for (int i = 0; i < fromFragment.size(); i++) {
            assertEquals(fromDocument.get(i).toXml(), fromFragment.get(i).toXml());
        }
    }
}
