/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class ContainerNodeExtendedTest {

    // ===== insertChild =====

    @Test
    void testInsertChildAtStart() {
        Element parent = Element.of("parent");
        parent.addChild(Element.of("child1"));
        parent.addChild(Element.of("child2"));

        Element newChild = Element.of("first");
        parent.insertChild(0, newChild);

        assertEquals("first", ((Element) parent.child(0)).name());
        assertEquals(3, parent.childCount());
    }

    @Test
    void testInsertChildAtEnd() {
        Element parent = Element.of("parent");
        parent.addChild(Element.of("child1"));

        Element newChild = Element.of("last");
        parent.insertChild(1, newChild);

        assertEquals("last", ((Element) parent.child(1)).name());
    }

    @Test
    void testInsertChildAtMiddle() {
        Element parent = Element.of("parent");
        parent.addChild(Element.of("child1"));
        parent.addChild(Element.of("child3"));

        Element newChild = Element.of("child2");
        parent.insertChild(1, newChild);

        assertEquals("child2", ((Element) parent.child(1)).name());
        assertEquals(3, parent.childCount());
    }

    @Test
    void testInsertChildNullThrows() {
        Element parent = Element.of("parent");
        assertThrows(IllegalArgumentException.class, () -> parent.insertChild(0, null));
    }

    @Test
    void testInsertChildNegativeIndexThrows() {
        Element parent = Element.of("parent");
        assertThrows(IndexOutOfBoundsException.class, () -> parent.insertChild(-1, Element.of("child")));
    }

    @Test
    void testInsertChildTooLargeIndexThrows() {
        Element parent = Element.of("parent");
        assertThrows(IndexOutOfBoundsException.class, () -> parent.insertChild(1, Element.of("child")));
    }

    @Test
    void testInsertChildReparents() {
        Element oldParent = Element.of("oldParent");
        Element child = Element.of("child");
        oldParent.addChild(child);

        Element newParent = Element.of("newParent");
        newParent.insertChild(0, child);

        assertSame(newParent, child.parent());
        assertEquals(0, oldParent.childCount());
        assertEquals(1, newParent.childCount());
    }

    @Test
    void testInsertChildOnSelfClosingElement() {
        String xml = "<root><empty/></root>";
        Editor editor = new Editor(Document.of(xml));
        Element empty = editor.root().childElement("empty").orElseThrow();
        assertTrue(empty.selfClosing());

        empty.insertChild(0, Element.of("child"));
        assertFalse(empty.selfClosing());
    }

    // ===== insertChildBefore / insertChildAfter =====

    @Test
    void testInsertChildBeforeNullRef() {
        Element parent = Element.of("parent");
        Element child = Element.of("child");
        parent.insertChildBefore(null, child);
        assertEquals(1, parent.childCount());
        assertSame(child, parent.child(0));
    }

    @Test
    void testInsertChildBeforeNullNewNodeThrows() {
        Element parent = Element.of("parent");
        Element ref = Element.of("ref");
        parent.addChild(ref);
        assertThrows(IllegalArgumentException.class, () -> parent.insertChildBefore(ref, null));
    }

    @Test
    void testInsertChildBeforeInvalidRefThrows() {
        Element parent = Element.of("parent");
        Element ref = Element.of("ref");
        Element newNode = Element.of("new");
        assertThrows(IllegalArgumentException.class, () -> parent.insertChildBefore(ref, newNode));
    }

    @Test
    void testInsertChildBeforeReparents() {
        Element oldParent = Element.of("old");
        Element child = Element.of("child");
        oldParent.addChild(child);

        Element newParent = Element.of("new");
        Element ref = Element.of("ref");
        newParent.addChild(ref);
        newParent.insertChildBefore(ref, child);

        assertSame(newParent, child.parent());
        assertEquals(0, oldParent.childCount());
    }

    @Test
    void testInsertChildAfterNullRef() {
        Element parent = Element.of("parent");
        Element child = Element.of("child");
        parent.insertChildAfter(null, child);
        assertEquals(1, parent.childCount());
    }

    @Test
    void testInsertChildAfterNullNewNodeThrows() {
        Element parent = Element.of("parent");
        Element ref = Element.of("ref");
        parent.addChild(ref);
        assertThrows(IllegalArgumentException.class, () -> parent.insertChildAfter(ref, null));
    }

    @Test
    void testInsertChildAfterInvalidRefThrows() {
        Element parent = Element.of("parent");
        Element ref = Element.of("ref");
        Element newNode = Element.of("new");
        assertThrows(IllegalArgumentException.class, () -> parent.insertChildAfter(ref, newNode));
    }

    @Test
    void testInsertChildAfterLastChild() {
        Element parent = Element.of("parent");
        Element child1 = Element.of("child1");
        parent.addChild(child1);

        Element child2 = Element.of("child2");
        parent.insertChildAfter(child1, child2);

        assertEquals(2, parent.childCount());
        assertSame(child2, parent.child(1));
    }

    @Test
    void testInsertChildAfterReparents() {
        Element oldParent = Element.of("old");
        Element child = Element.of("child");
        oldParent.addChild(child);

        Element newParent = Element.of("new");
        Element ref = Element.of("ref");
        newParent.addChild(ref);
        newParent.insertChildAfter(ref, child);

        assertSame(newParent, child.parent());
        assertEquals(0, oldParent.childCount());
    }

    @Test
    void testInsertChildAfterOnSelfClosing() {
        String xml = "<root><empty/></root>";
        Editor editor = new Editor(Document.of(xml));
        Element empty = editor.root().childElement("empty").orElseThrow();
        assertTrue(empty.selfClosing());

        Element ref = Element.of("ref");
        empty.insertChild(0, ref);
        Element newNode = Element.of("new");
        empty.insertChildAfter(ref, newNode);
        assertFalse(empty.selfClosing());
    }

    // ===== replaceChild =====

    @Test
    void testReplaceChildNullExisting() {
        Element parent = Element.of("parent");
        Element replacement = Element.of("replacement");
        parent.replaceChild(null, replacement);
        assertEquals(1, parent.childCount());
    }

    @Test
    void testReplaceChildNullReplacementThrows() {
        Element parent = Element.of("parent");
        Element child = Element.of("child");
        parent.addChild(child);
        assertThrows(IllegalArgumentException.class, () -> parent.replaceChild(child, null));
    }

    @Test
    void testReplaceChildInvalidExistingThrows() {
        Element parent = Element.of("parent");
        Element notChild = Element.of("notChild");
        Element replacement = Element.of("replacement");
        assertThrows(IllegalArgumentException.class, () -> parent.replaceChild(notChild, replacement));
    }

    @Test
    void testReplaceChildReparents() {
        Element oldParent = Element.of("old");
        Element replacement = Element.of("replacement");
        oldParent.addChild(replacement);

        Element parent = Element.of("parent");
        Element existing = Element.of("existing");
        parent.addChild(existing);

        parent.replaceChild(existing, replacement);
        assertSame(parent, replacement.parent());
        assertEquals(0, oldParent.childCount());
    }

    // ===== addChild =====

    @Test
    void testAddChildNullThrows() {
        Element parent = Element.of("parent");
        assertThrows(IllegalArgumentException.class, () -> parent.addChild(null));
    }

    @Test
    void testAddChildReparents() {
        Element oldParent = Element.of("old");
        Element child = Element.of("child");
        oldParent.addChild(child);
        assertEquals(1, oldParent.childCount());

        Element newParent = Element.of("new");
        newParent.addChild(child);

        assertSame(newParent, child.parent());
        assertEquals(0, oldParent.childCount());
        assertEquals(1, newParent.childCount());
    }

    @Test
    void testAddChildOnSelfClosing() {
        String xml = "<root><empty/></root>";
        Editor editor = new Editor(Document.of(xml));
        Element empty = editor.root().childElement("empty").orElseThrow();
        assertTrue(empty.selfClosing());

        empty.addChild(Element.of("child"));
        assertFalse(empty.selfClosing());
    }

    // ===== removeChild =====

    @Test
    void testRemoveChildReturnsTrue() {
        Element parent = Element.of("parent");
        Element child = Element.of("child");
        parent.addChild(child);

        assertTrue(parent.removeChild(child));
        assertEquals(0, parent.childCount());
        assertNull(child.parent());
    }

    @Test
    void testRemoveChildReturnsFalse() {
        Element parent = Element.of("parent");
        Element notChild = Element.of("notChild");
        assertFalse(parent.removeChild(notChild));
    }

    // ===== clearChildren =====

    @Test
    void testClearChildren() {
        Element parent = Element.of("parent");
        Element child1 = Element.of("child1");
        Element child2 = Element.of("child2");
        parent.addChild(child1);
        parent.addChild(child2);

        parent.clearChildren();
        assertEquals(0, parent.childCount());
        assertTrue(parent.isEmpty());
        assertNull(child1.parent());
        assertNull(child2.parent());
    }

    @Test
    void testClearChildrenEmpty() {
        Element parent = Element.of("parent");
        assertDoesNotThrow(parent::clearChildren);
        assertEquals(0, parent.childCount());
    }

    // ===== isEmpty, hasChildElements, hasTextContent =====

    @Test
    void testIsEmpty() {
        Element el = Element.of("element");
        assertTrue(el.isEmpty());
        el.addChild(Element.of("child"));
        assertFalse(el.isEmpty());
    }

    @Test
    void testHasChildElements() {
        Element parent = Element.of("parent");
        assertFalse(parent.hasChildElements());

        parent.addChild(new Text("text"));
        assertFalse(parent.hasChildElements()); // text node, not element

        parent.addChild(Element.of("child"));
        assertTrue(parent.hasChildElements());
    }

    @Test
    void testHasTextContent() {
        Element parent = Element.of("parent");
        assertFalse(parent.hasTextContent());

        parent.addChild(Element.of("child"));
        assertFalse(parent.hasTextContent()); // element, not text

        parent.addChild(new Text("text"));
        assertTrue(parent.hasTextContent());
    }

    // ===== firstChild, lastChild =====

    @Test
    void testFirstChildEmpty() {
        Element parent = Element.of("parent");
        assertFalse(parent.firstChild().isPresent());
    }

    @Test
    void testFirstChild() {
        Element parent = Element.of("parent");
        Element first = Element.of("first");
        parent.addChild(first);
        parent.addChild(Element.of("second"));

        Optional<Node> result = parent.firstChild();
        assertTrue(result.isPresent());
        assertSame(first, result.get());
    }

    @Test
    void testLastChildEmpty() {
        Element parent = Element.of("parent");
        assertFalse(parent.lastChild().isPresent());
    }

    @Test
    void testLastChild() {
        Element parent = Element.of("parent");
        parent.addChild(Element.of("first"));
        Element last = Element.of("last");
        parent.addChild(last);

        Optional<Node> result = parent.lastChild();
        assertTrue(result.isPresent());
        assertSame(last, result.get());
    }

    // ===== findTextNode, textContent =====

    @Test
    void testFindTextNode() {
        Element el = Element.of("element");
        assertFalse(el.findTextNode().isPresent());

        Text text = new Text("content");
        el.addChild(text);
        Optional<Text> found = el.findTextNode();
        assertTrue(found.isPresent());
        assertSame(text, found.get());
    }

    @Test
    void testTextContent() {
        Element el = Element.of("element");
        assertEquals("", el.textContent());

        el.addChild(new Text("hello"));
        assertEquals("hello", el.textContent());
    }

    @Test
    void testTextContentMultipleNodes() {
        Element el = Element.of("element");
        el.addChild(new Text("hello "));
        el.addChild(Element.of("separator")); // non-text node
        el.addChild(new Text("world"));
        assertEquals("hello world", el.textContent());
    }

    // ===== clearModified =====

    @Test
    void testClearModifiedPropagates() {
        Element parent = Element.of("parent");
        Element child = Element.of("child");
        parent.addChild(child);

        parent.markModified();
        assertTrue(parent.isModified());

        parent.clearModified();
        assertFalse(parent.isModified());
        assertFalse(child.isModified());
    }

    // ===== child(index) =====

    @Test
    void testChildAtIndex() {
        Element parent = Element.of("parent");
        Element child0 = Element.of("child0");
        Element child1 = Element.of("child1");
        parent.addChild(child0);
        parent.addChild(child1);

        assertSame(child0, parent.child(0));
        assertSame(child1, parent.child(1));
    }

    @Test
    void testChildAtInvalidIndex() {
        Element parent = Element.of("parent");
        assertThrows(IndexOutOfBoundsException.class, () -> parent.child(0));
    }

    @Test
    @SuppressWarnings("deprecation")
    void testDeprecatedGetNode() {
        Element parent = Element.of("parent");
        Element child = Element.of("child");
        parent.addChild(child);
        assertSame(child, parent.getNode(0));
    }

    // ===== children stream =====

    @Test
    void testChildrenStream() {
        Element parent = Element.of("parent");
        parent.addChild(Element.of("child1"));
        parent.addChild(Element.of("child2"));
        parent.addChild(new Text("text"));

        assertEquals(3, parent.children().count());
    }

    @Test
    void testChildrenStreamEmpty() {
        Element parent = Element.of("parent");
        assertEquals(0, parent.children().count());
    }
}
