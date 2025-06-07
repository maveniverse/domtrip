package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Test cases for the new ContainerNode architecture.
 */
public class ContainerNodeTest {

    @Test
    void testOnlyContainerNodesCanHaveChildren() {
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
    void testContainerNodeChildManagement() {
        Element parent = new Element("parent");
        Element child1 = new Element("child1");
        Element child2 = new Element("child2");
        Text text = new Text("text content");

        // Test adding children
        parent.addChild(child1);
        parent.addChild(child2);
        parent.addChild(text);

        assertEquals(3, parent.getChildCount());
        assertEquals(child1, parent.getChild(0));
        assertEquals(child2, parent.getChild(1));
        assertEquals(text, parent.getChild(2));

        // Test removing children
        assertTrue(parent.removeChild(child2));
        assertEquals(2, parent.getChildCount());
        assertFalse(parent.removeChild(child2)); // Already removed

        // Test child parent relationships
        assertEquals(parent, child1.getParent());
        assertNull(child2.getParent()); // Removed
        assertEquals(parent, text.getParent());
    }

    @Test
    void testElementSpecificNavigation() {
        Element root = new Element("root");
        Element child1 = new Element("child");
        Element child2 = new Element("child");
        Element other = new Element("other");
        Text text = new Text("text");

        root.addChild(child1);
        root.addChild(text);
        root.addChild(child2);
        root.addChild(other);

        // Test element-specific navigation methods
        assertEquals(child1, root.findChild("child").orElse(null));
        assertEquals(2, root.findChildren("child").count());
        assertEquals(3, root.childElements().count()); // Excludes text node

        // Test that these methods are only available on Element, not on leaf nodes
        // This is enforced by the type system - leaf nodes don't have these methods
    }

    @Test
    void testNamespaceMethodsOnlyOnElement() {
        Element element = new Element("ns:test");

        // These methods should only be available on Element
        assertEquals("test", element.getLocalName());
        assertEquals("ns", element.getPrefix());
        assertEquals("ns:test", element.getQualifiedName());

        // Leaf nodes don't have these methods (enforced by type system)
        Text text = new Text("content");
        Comment comment = new Comment("comment");

        // These would be compilation errors:
        // text.getLocalName();  // Method not available
        // comment.getPrefix();  // Method not available
    }

    @Test
    void testMemoryEfficiency() {
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
        element.addChild(text);
        document.addChild(element);

        assertEquals(1, element.getChildCount());
        assertEquals(1, document.getChildCount());
    }

    @Test
    void testBackwardCompatibility() {
        Editor editor = new Editor();
        Element root = new Element("root");

        // Old deprecated methods should still work
        @SuppressWarnings("deprecation")
        Comment comment = editor.addComment(root, "test comment");
        assertNotNull(comment);
        assertEquals("test comment", comment.getContent());

        @SuppressWarnings("deprecation")
        Element found = editor.findChildElement(root, "nonexistent");
        assertNull(found);

        // But they should throw exceptions for invalid node types
        Text text = new Text("content");
        assertThrows(InvalidXmlException.class, () -> {
            @SuppressWarnings("deprecation")
            Comment c = editor.addComment(text, "comment");
        });
    }

    @Test
    void testClearModifiedPropagation() {
        Element root = new Element("root");
        Element child = new Element("child");
        Text text = new Text("content");

        root.addChild(child);
        child.addChild(text);

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
