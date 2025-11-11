package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for the new Node navigation methods: getParent(), getParentElement(), and getDocument().
 */
class NodeNavigationTest {

    @Test
    void testGetParentReturnsContainerNode() throws DomTripException {
        Document doc = new Document();
        Element root = new Element("root");
        Element child = new Element("child");
        Text text = new Text("Hello");
        Comment comment = new Comment("A comment");

        doc.addNode(root);
        root.addNode(child);
        child.addNode(text);
        child.addNode(comment);

        // Test that getParent() returns ContainerNode
        ContainerNode rootParent = root.parent();
        assertSame(doc, rootParent);
        assertTrue(rootParent instanceof Document);

        ContainerNode childParent = child.parent();
        assertSame(root, childParent);
        assertTrue(childParent instanceof Element);

        ContainerNode textParent = text.parent();
        assertSame(child, textParent);
        assertTrue(textParent instanceof Element);

        ContainerNode commentParent = comment.parent();
        assertSame(child, commentParent);
        assertTrue(commentParent instanceof Element);

        // Document has no parent
        assertNull(doc.parent());
    }

    @Test
    void testGetParentElement() throws DomTripException {
        Document doc = new Document();
        Element root = new Element("root");
        Element child = new Element("child");
        Text text = new Text("Hello");
        Comment comment = new Comment("A comment");

        doc.addNode(root);
        root.addNode(child);
        child.addNode(text);
        child.addNode(comment);

        // Root element's parent is Document, so getParentElement() returns null
        assertNull(root.parentElement());

        // Child element's parent is root Element
        Element childParentElement = child.parentElement();
        assertSame(root, childParentElement);

        // Text node's parent is child Element
        Element textParentElement = text.parentElement();
        assertSame(child, textParentElement);

        // Comment's parent is child Element
        Element commentParentElement = comment.parentElement();
        assertSame(child, commentParentElement);

        // Document has no parent element
        assertNull(doc.parentElement());
    }

    @Test
    void testGetDocument() throws DomTripException {
        Document doc = new Document();
        Element root = new Element("root");
        Element child = new Element("child");
        Text text = new Text("Hello");
        Comment comment = new Comment("A comment");

        doc.addNode(root);
        root.addNode(child);
        child.addNode(text);
        child.addNode(comment);

        // All nodes should return the same document
        assertSame(doc, root.document());
        assertSame(doc, child.document());
        assertSame(doc, text.document());
        assertSame(doc, comment.document());

        // Document itself returns null (it's the root)
        assertNull(doc.document());
    }

    @Test
    void testGetDocumentFromAllNodes() throws DomTripException {
        Document doc = new Document();
        Element root = new Element("root");
        Element child = new Element("child");
        Text text = new Text("Hello");

        doc.addNode(root);
        root.addNode(child);
        child.addNode(text);

        // All nodes should return the document
        Document docDoc = doc.document();
        Document rootDoc = root.document();
        Document childDoc = child.document();
        Document textDoc = text.document();

        assertNull(docDoc); // Document itself returns null
        assertSame(doc, rootDoc);
        assertSame(doc, childDoc);
        assertSame(doc, textDoc);
    }

    @Test
    void testNavigationWithFragmentRoot() throws DomTripException {
        // Test case where root is an Element (fragment), not Document
        Element fragmentRoot = new Element("fragment");
        Element child = new Element("child");
        Text text = new Text("Hello");

        fragmentRoot.addNode(child);
        child.addNode(text);

        // getParent() tests
        assertNull(fragmentRoot.parent());
        assertSame(fragmentRoot, child.parent());
        assertSame(child, text.parent());

        // getParentElement() tests
        assertNull(fragmentRoot.parentElement()); // No parent
        assertSame(fragmentRoot, child.parentElement());
        assertSame(child, text.parentElement());

        // getDocument() tests - should return null since no Document in tree
        assertNull(fragmentRoot.document());
        assertNull(child.document());
        assertNull(text.document());

        // No getRoot() method anymore - getDocument() handles the main use case
    }
}
