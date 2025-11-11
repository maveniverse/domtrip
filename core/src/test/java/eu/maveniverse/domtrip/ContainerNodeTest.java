package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Test cases for the new ContainerNode architecture.
 */
public class ContainerNodeTest {

    @Test
    void testOnlyContainerNodesCanHaveChildren() throws DomTripException {
        // Container nodes can have children
        Document document = new Document();
        Element element = new Element("test");

        assertTrue(document instanceof ContainerNode);
        assertTrue(element instanceof ContainerNode);

        // Leaf nodes cannot have children
        Text text = new Text("content");
        Comment comment = new Comment("comment");
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");

        // These are leaf nodes - they don't extend ContainerNode
        assertTrue(text instanceof Node);
        assertTrue(comment instanceof Node);
        assertTrue(pi instanceof Node);

        // But they are NOT container nodes (this is enforced by the type system)
        // We can't even cast them to ContainerNode - compilation would fail
        // This demonstrates the type safety of our new architecture
    }

    @Test
    void testContainerNodeChildManagement() throws DomTripException {
        Element parent = new Element("parent");
        Element child1 = new Element("child1");
        Element child2 = new Element("child2");
        Text text = new Text("text content");

        // Test adding children
        parent.addNode(child1);
        parent.addNode(child2);
        parent.addNode(text);

        assertEquals(3, parent.nodeCount());
        assertEquals(child1, parent.getNode(0));
        assertEquals(child2, parent.getNode(1));
        assertEquals(text, parent.getNode(2));

        // Test removing children
        assertTrue(parent.removeNode(child2));
        assertEquals(2, parent.nodeCount());
        assertFalse(parent.removeNode(child2)); // Already removed

        // Test child parent relationships
        assertEquals(parent, child1.parent());
        assertNull(child2.parent()); // Removed
        assertEquals(parent, text.parent());
    }

    @Test
    void testElementSpecificNavigation() throws DomTripException {
        Element root = new Element("root");
        Element child1 = new Element("child");
        Element child2 = new Element("child");
        Element other = new Element("other");
        Text text = new Text("text");

        root.addNode(child1);
        root.addNode(text);
        root.addNode(child2);
        root.addNode(other);

        // Test element-specific navigation methods
        assertEquals(child1, root.child("child").orElse(null));
        assertEquals(2, root.children("child").count());
        assertEquals(3, root.children().count()); // Excludes text node

        // Test that these methods are only available on Element, not on leaf nodes
        // This is enforced by the type system - leaf nodes don't have these methods
    }

    @Test
    void testNamespaceMethodsOnlyOnElement() throws DomTripException {
        Element element = new Element("ns:test");

        // These methods should only be available on Element
        assertEquals("test", element.localName());
        assertEquals("ns", element.prefix());
        assertEquals("ns:test", element.qualifiedName());

        // Leaf nodes don't have these methods (enforced by type system)
        Text text = new Text("content");
        Comment comment = new Comment("comment");

        // These would be compilation errors:
        // text.localName();  // Method not available
        // comment.prefix();  // Method not available
    }

    @Test
    void testMemoryEfficiency() throws DomTripException {
        // Leaf nodes should not have children list
        Text text = new Text("content");
        Comment comment = new Comment("comment");
        ProcessingInstruction pi = new ProcessingInstruction("target", "data");

        // These nodes should not have child management methods
        // This is enforced by the type system - they don't extend ContainerNode

        // Only container nodes have children
        Element element = new Element("test");
        Document document = new Document();

        assertTrue(element instanceof ContainerNode);
        assertTrue(document instanceof ContainerNode);

        // Verify they can manage children
        element.addNode(text);
        document.addNode(element);

        assertEquals(1, element.nodeCount());
        assertEquals(1, document.nodeCount());
    }

    @Test
    void testClearModifiedPropagation() throws DomTripException {
        Element root = new Element("root");
        Element child = new Element("child");
        Text text = new Text("content");

        root.addNode(child);
        child.addNode(text);

        // Mark nodes as modified
        root.markModified();
        child.markModified();
        text.markModified();

        assertTrue(root.isModified());
        assertTrue(child.isModified());
        assertTrue(text.isModified());

        // Clear modified should propagate to children
        root.clearModified();

        assertFalse(root.isModified());
        assertFalse(child.isModified());
        assertFalse(text.isModified());
    }
}
