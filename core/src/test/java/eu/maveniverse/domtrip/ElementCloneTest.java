package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for Element cloning functionality and parent removal behavior.
 */
public class ElementCloneTest {

    @Test
    void testElementClone() throws DomTripException {
        // Create an element with attributes and children
        Element original = Element.of("parent").attribute("id", "test").attribute("class", "example");

        Element child1 = Element.text("child1", "content1");
        Element child2 = Element.of("child2");
        child2.addChild(Element.text("grandchild", "nested"));

        original.addChild(child1);
        original.addChild(child2);
        original.addChild(Comment.of("test comment"));
        original.addChild(Text.of("some text"));

        // Clone the element
        Element clone = original.copy();

        // Verify basic properties
        assertEquals("parent", clone.name());
        assertEquals("test", clone.attribute("id"));
        assertEquals("example", clone.attribute("class"));
        assertEquals(4, clone.childCount());

        // Verify parent is null
        assertNull(clone.parent());

        // Verify children are cloned
        Element clonedChild1 = (Element) clone.child(0);
        assertEquals("child1", clonedChild1.name());
        assertEquals("content1", clonedChild1.textContent());
        assertEquals(clone, clonedChild1.parent()); // Parent should be the clone

        Element clonedChild2 = (Element) clone.child(1);
        assertEquals("child2", clonedChild2.name());
        assertEquals(1, clonedChild2.childCount());

        Element clonedGrandchild = (Element) clonedChild2.child(0);
        assertEquals("grandchild", clonedGrandchild.name());
        assertEquals("nested", clonedGrandchild.textContent());
        assertEquals(clonedChild2, clonedGrandchild.parent()); // Parent should be cloned child2

        // Verify comment is cloned
        Comment clonedComment = (Comment) clone.child(2);
        assertEquals("test comment", clonedComment.content());
        assertEquals(clone, clonedComment.parent());

        // Verify text is cloned
        Text clonedText = (Text) clone.child(3);
        assertEquals("some text", clonedText.content());
        assertEquals(clone, clonedText.parent());

        // Verify independence - changes to clone don't affect original
        clone.attribute("id", "modified");
        assertEquals("test", original.attribute("id"));
        assertEquals("modified", clone.attribute("id"));
    }

    @Test
    void testParentRemovalOnAddNode() throws DomTripException {
        Element parent1 = Element.of("parent1");
        Element parent2 = Element.of("parent2");
        Element child = Element.of("child");

        // Add child to first parent
        parent1.addChild(child);
        assertEquals(parent1, child.parent());
        assertEquals(1, parent1.childCount());
        assertEquals(0, parent2.childCount());

        // Add same child to second parent - should be removed from first
        parent2.addChild(child);
        assertEquals(parent2, child.parent());
        assertEquals(0, parent1.childCount()); // Child removed from first parent
        assertEquals(1, parent2.childCount());
    }

    @Test
    void testParentRemovalOnInsertNode() throws DomTripException {
        Element parent1 = Element.of("parent1");
        Element parent2 = Element.of("parent2");
        Element child = Element.of("child");
        Element sibling = Element.of("sibling");

        // Add child to first parent
        parent1.addChild(child);
        parent2.addChild(sibling);

        assertEquals(parent1, child.parent());
        assertEquals(1, parent1.childCount());
        assertEquals(1, parent2.childCount());

        // Insert same child into second parent at index 0
        parent2.insertChild(0, child);
        assertEquals(parent2, child.parent());
        assertEquals(0, parent1.childCount()); // Child removed from first parent
        assertEquals(2, parent2.childCount()); // Child + sibling
        assertEquals(child, parent2.child(0)); // Child is at index 0
        assertEquals(sibling, parent2.child(1)); // Sibling moved to index 1
    }

    @Test
    void testCloneWithWhitespacePreservation() throws DomTripException {
        Element original = Element.of("test");
        original.precedingWhitespace("  ");
        original.openTagWhitespace(" ");
        original.closeTagWhitespace(" ");
        original.innerPrecedingWhitespace("\n  ");
        original.selfClosing(true);

        Element clone = original.copy();

        assertEquals("  ", clone.precedingWhitespace());
        assertEquals(" ", clone.openTagWhitespace());
        assertEquals(" ", clone.closeTagWhitespace());
        assertEquals("\n  ", clone.innerPrecedingWhitespace());
        assertTrue(clone.selfClosing());
    }

    @Test
    void testCloneWithCDataAndProcessingInstruction() throws DomTripException {
        Element original = Element.of("root");
        original.addChild(Text.cdata("<script>alert('test');</script>"));
        original.addChild(ProcessingInstruction.of("xml-stylesheet", "type=\"text/css\""));

        Element clone = original.copy();

        Text clonedCData = (Text) clone.child(0);
        assertTrue(clonedCData.cdata());
        assertEquals("<script>alert('test');</script>", clonedCData.content());

        ProcessingInstruction clonedPI = (ProcessingInstruction) clone.child(1);
        assertEquals("xml-stylesheet", clonedPI.target());
        assertEquals("type=\"text/css\"", clonedPI.data());
    }

    @Test
    void testDocumentClone() throws DomTripException {
        Document original = Document.withXmlDeclaration("1.1", "ISO-8859-1")
                .standalone(true)
                .doctype("<!DOCTYPE html>")
                .root(Element.of("html"));

        original.root().addChild(Element.of("head"));
        Element body = Element.of("body");
        body.addChild(Text.of("Hello World"));
        original.root().addChild(body);

        Document clone = original.copy();

        // Verify document properties
        assertEquals("1.1", clone.version());
        assertEquals("ISO-8859-1", clone.encoding());
        assertTrue(clone.isStandalone());
        assertEquals("<!DOCTYPE html>", clone.doctype());
        assertNull(clone.parent()); // Document should have no parent

        // Verify structure is cloned
        assertNotNull(clone.root());
        assertEquals("html", clone.root().name());
        assertEquals(2, clone.root().childCount());

        Element clonedHead = (Element) clone.root().child(0);
        assertEquals("head", clonedHead.name());
        assertEquals(clone.root(), clonedHead.parent());

        Element clonedBody = (Element) clone.root().child(1);
        assertEquals("body", clonedBody.name());
        assertEquals(clone.root(), clonedBody.parent());

        Text clonedText = (Text) clonedBody.child(0);
        assertEquals("Hello World", clonedText.content());
        assertEquals(clonedBody, clonedText.parent());

        // Verify independence
        clone.version("2.0");
        assertEquals("1.1", original.version());
        assertEquals("2.0", clone.version());
    }

    @Test
    void testDocumentCloneWithoutRoot() {
        Document original = new Document();
        original.version("1.0");
        original.encoding("UTF-8");

        Document clone = original.copy();

        assertEquals("1.0", clone.version());
        assertEquals("UTF-8", clone.encoding());
        assertNull(clone.root());
    }

    @SuppressWarnings("deprecation")
    @Test
    void testDeprecatedCloneDelegatesToCopy() throws DomTripException {
        Element original = Element.of("test").attribute("id", "1");
        original.addChild(Text.of("content"));

        Element cloned = original.clone();

        assertEquals("test", cloned.name());
        assertEquals("1", cloned.attribute("id"));
        assertEquals(1, cloned.childCount());
        assertNull(cloned.parent());
        assertNotSame(original, cloned);
    }

    @SuppressWarnings("deprecation")
    @Test
    void testDeprecatedCloneOnAllNodeTypes() throws DomTripException {
        // Attribute
        Attribute attr = Attribute.of("name", "value");
        Attribute attrClone = attr.clone();
        assertEquals("name", attrClone.name());
        assertEquals("value", attrClone.value());
        assertNotSame(attr, attrClone);

        // Comment
        Comment comment = Comment.of("test comment");
        Comment commentClone = comment.clone();
        assertEquals("test comment", commentClone.content());
        assertNotSame(comment, commentClone);

        // Text
        Text text = Text.of("hello");
        Text textClone = text.clone();
        assertEquals("hello", textClone.content());
        assertNotSame(text, textClone);

        // ProcessingInstruction
        ProcessingInstruction pi = ProcessingInstruction.of("target", "data");
        ProcessingInstruction piClone = pi.clone();
        assertEquals("target", piClone.target());
        assertEquals("data", piClone.data());
        assertNotSame(pi, piClone);

        // Document
        Document doc = Document.of("<root/>");
        Document docClone = doc.clone();
        assertNotNull(docClone.root());
        assertEquals("root", docClone.root().name());
        assertNotSame(doc, docClone);
    }
}
