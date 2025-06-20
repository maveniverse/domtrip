package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for Element cloning functionality and parent removal behavior.
 */
public class ElementCloneTest {

    @Test
    void testElementClone() {
        // Create an element with attributes and children
        Element original = Element.of("parent").attribute("id", "test").attribute("class", "example");

        Element child1 = Element.text("child1", "content1");
        Element child2 = Element.of("child2");
        child2.addNode(Element.text("grandchild", "nested"));

        original.addNode(child1);
        original.addNode(child2);
        original.addNode(Comment.of("test comment"));
        original.addNode(Text.of("some text"));

        // Clone the element
        Element clone = original.clone();

        // Verify basic properties
        assertEquals("parent", clone.name());
        assertEquals("test", clone.attribute("id"));
        assertEquals("example", clone.attribute("class"));
        assertEquals(4, clone.nodeCount());

        // Verify parent is null
        assertNull(clone.parent());

        // Verify children are cloned
        Element clonedChild1 = (Element) clone.getNode(0);
        assertEquals("child1", clonedChild1.name());
        assertEquals("content1", clonedChild1.textContent());
        assertEquals(clone, clonedChild1.parent()); // Parent should be the clone

        Element clonedChild2 = (Element) clone.getNode(1);
        assertEquals("child2", clonedChild2.name());
        assertEquals(1, clonedChild2.nodeCount());

        Element clonedGrandchild = (Element) clonedChild2.getNode(0);
        assertEquals("grandchild", clonedGrandchild.name());
        assertEquals("nested", clonedGrandchild.textContent());
        assertEquals(clonedChild2, clonedGrandchild.parent()); // Parent should be cloned child2

        // Verify comment is cloned
        Comment clonedComment = (Comment) clone.getNode(2);
        assertEquals("test comment", clonedComment.content());
        assertEquals(clone, clonedComment.parent());

        // Verify text is cloned
        Text clonedText = (Text) clone.getNode(3);
        assertEquals("some text", clonedText.content());
        assertEquals(clone, clonedText.parent());

        // Verify independence - changes to clone don't affect original
        clone.attribute("id", "modified");
        assertEquals("test", original.attribute("id"));
        assertEquals("modified", clone.attribute("id"));
    }

    @Test
    void testParentRemovalOnAddNode() {
        Element parent1 = Element.of("parent1");
        Element parent2 = Element.of("parent2");
        Element child = Element.of("child");

        // Add child to first parent
        parent1.addNode(child);
        assertEquals(parent1, child.parent());
        assertEquals(1, parent1.nodeCount());
        assertEquals(0, parent2.nodeCount());

        // Add same child to second parent - should be removed from first
        parent2.addNode(child);
        assertEquals(parent2, child.parent());
        assertEquals(0, parent1.nodeCount()); // Child removed from first parent
        assertEquals(1, parent2.nodeCount());
    }

    @Test
    void testParentRemovalOnInsertNode() {
        Element parent1 = Element.of("parent1");
        Element parent2 = Element.of("parent2");
        Element child = Element.of("child");
        Element sibling = Element.of("sibling");

        // Add child to first parent
        parent1.addNode(child);
        parent2.addNode(sibling);

        assertEquals(parent1, child.parent());
        assertEquals(1, parent1.nodeCount());
        assertEquals(1, parent2.nodeCount());

        // Insert same child into second parent at index 0
        parent2.insertNode(0, child);
        assertEquals(parent2, child.parent());
        assertEquals(0, parent1.nodeCount()); // Child removed from first parent
        assertEquals(2, parent2.nodeCount()); // Child + sibling
        assertEquals(child, parent2.getNode(0)); // Child is at index 0
        assertEquals(sibling, parent2.getNode(1)); // Sibling moved to index 1
    }

    @Test
    void testCloneWithWhitespacePreservation() {
        Element original = Element.of("test");
        original.precedingWhitespace("  ");
        original.openTagWhitespace(" ");
        original.closeTagWhitespace(" ");
        original.innerPrecedingWhitespace("\n  ");
        original.selfClosing(true);

        Element clone = original.clone();

        assertEquals("  ", clone.precedingWhitespace());
        assertEquals(" ", clone.openTagWhitespace());
        assertEquals(" ", clone.closeTagWhitespace());
        assertEquals("\n  ", clone.innerPrecedingWhitespace());
        assertTrue(clone.selfClosing());
    }

    @Test
    void testCloneWithCDataAndProcessingInstruction() {
        Element original = Element.of("root");
        original.addNode(Text.cdata("<script>alert('test');</script>"));
        original.addNode(ProcessingInstruction.of("xml-stylesheet", "type=\"text/css\""));

        Element clone = original.clone();

        Text clonedCData = (Text) clone.getNode(0);
        assertTrue(clonedCData.cdata());
        assertEquals("<script>alert('test');</script>", clonedCData.content());

        ProcessingInstruction clonedPI = (ProcessingInstruction) clone.getNode(1);
        assertEquals("xml-stylesheet", clonedPI.target());
        assertEquals("type=\"text/css\"", clonedPI.data());
    }

    @Test
    void testDocumentClone() {
        Document original = Document.withXmlDeclaration("1.1", "ISO-8859-1")
                .standalone(true)
                .doctype("<!DOCTYPE html>")
                .root(Element.of("html"));

        original.root().addNode(Element.of("head"));
        Element body = Element.of("body");
        body.addNode(Text.of("Hello World"));
        original.root().addNode(body);

        Document clone = original.clone();

        // Verify document properties
        assertEquals("1.1", clone.version());
        assertEquals("ISO-8859-1", clone.encoding());
        assertTrue(clone.isStandalone());
        assertEquals("<!DOCTYPE html>", clone.doctype());
        assertNull(clone.parent()); // Document should have no parent

        // Verify structure is cloned
        assertNotNull(clone.root());
        assertEquals("html", clone.root().name());
        assertEquals(2, clone.root().nodeCount());

        Element clonedHead = (Element) clone.root().getNode(0);
        assertEquals("head", clonedHead.name());
        assertEquals(clone.root(), clonedHead.parent());

        Element clonedBody = (Element) clone.root().getNode(1);
        assertEquals("body", clonedBody.name());
        assertEquals(clone.root(), clonedBody.parent());

        Text clonedText = (Text) clonedBody.getNode(0);
        assertEquals("Hello World", clonedText.content());
        assertEquals(clonedBody, clonedText.parent());

        // Verify independence
        clone.version("2.0");
        assertEquals("1.1", original.version());
        assertEquals("2.0", clone.version());
    }
}
