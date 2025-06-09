package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for the new Node navigation methods: getParent(), getParentElement(), and getDocument().
 */
class NodeNavigationTest {

    @Test
    void testGetParentReturnsContainerNode() {
        Document doc = new Document();
        Element root = new Element("root");
        Element child = new Element("child");
        Text text = new Text("Hello");
        Comment comment = new Comment("A comment");

        doc.addChild(root);
        root.addChild(child);
        child.addChild(text);
        child.addChild(comment);

        // Test that getParent() returns ContainerNode
        ContainerNode rootParent = root.getParent();
        assertSame(doc, rootParent);
        assertTrue(rootParent instanceof Document);

        ContainerNode childParent = child.getParent();
        assertSame(root, childParent);
        assertTrue(childParent instanceof Element);

        ContainerNode textParent = text.getParent();
        assertSame(child, textParent);
        assertTrue(textParent instanceof Element);

        ContainerNode commentParent = comment.getParent();
        assertSame(child, commentParent);
        assertTrue(commentParent instanceof Element);

        // Document has no parent
        assertNull(doc.getParent());
    }

    @Test
    void testGetParentElement() {
        Document doc = new Document();
        Element root = new Element("root");
        Element child = new Element("child");
        Text text = new Text("Hello");
        Comment comment = new Comment("A comment");

        doc.addChild(root);
        root.addChild(child);
        child.addChild(text);
        child.addChild(comment);

        // Root element's parent is Document, so getParentElement() returns null
        assertNull(root.getParentElement());

        // Child element's parent is root Element
        Element childParentElement = child.getParentElement();
        assertSame(root, childParentElement);

        // Text node's parent is child Element
        Element textParentElement = text.getParentElement();
        assertSame(child, textParentElement);

        // Comment's parent is child Element
        Element commentParentElement = comment.getParentElement();
        assertSame(child, commentParentElement);

        // Document has no parent element
        assertNull(doc.getParentElement());
    }

    @Test
    void testGetDocument() {
        Document doc = new Document();
        Element root = new Element("root");
        Element child = new Element("child");
        Text text = new Text("Hello");
        Comment comment = new Comment("A comment");

        doc.addChild(root);
        root.addChild(child);
        child.addChild(text);
        child.addChild(comment);

        // All nodes should return the same document
        assertSame(doc, root.getDocument());
        assertSame(doc, child.getDocument());
        assertSame(doc, text.getDocument());
        assertSame(doc, comment.getDocument());

        // Document itself returns null (it's the root)
        assertNull(doc.getDocument());
    }

    @Test
    void testGetDocumentFromAllNodes() {
        Document doc = new Document();
        Element root = new Element("root");
        Element child = new Element("child");
        Text text = new Text("Hello");

        doc.addChild(root);
        root.addChild(child);
        child.addChild(text);

        // All nodes should return the document
        Document docDoc = doc.getDocument();
        Document rootDoc = root.getDocument();
        Document childDoc = child.getDocument();
        Document textDoc = text.getDocument();

        assertNull(docDoc); // Document itself returns null
        assertSame(doc, rootDoc);
        assertSame(doc, childDoc);
        assertSame(doc, textDoc);
    }

    @Test
    void testNavigationWithFragmentRoot() {
        // Test case where root is an Element (fragment), not Document
        Element fragmentRoot = new Element("fragment");
        Element child = new Element("child");
        Text text = new Text("Hello");

        fragmentRoot.addChild(child);
        child.addChild(text);

        // getParent() tests
        assertNull(fragmentRoot.getParent());
        assertSame(fragmentRoot, child.getParent());
        assertSame(child, text.getParent());

        // getParentElement() tests
        assertNull(fragmentRoot.getParentElement()); // No parent
        assertSame(fragmentRoot, child.getParentElement());
        assertSame(child, text.getParentElement());

        // getDocument() tests - should return null since no Document in tree
        assertNull(fragmentRoot.getDocument());
        assertNull(child.getDocument());
        assertNull(text.getDocument());

        // No getRoot() method anymore - getDocument() handles the main use case
    }
}
