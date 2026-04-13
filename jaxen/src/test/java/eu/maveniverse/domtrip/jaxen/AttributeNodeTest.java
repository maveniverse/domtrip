/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.jaxen;

import static org.junit.jupiter.api.Assertions.*;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AttributeNode} equals, hashCode, and toString.
 */
class AttributeNodeTest {

    private Element element;

    @BeforeEach
    void setUp() {
        element = Document.of("<root id=\"1\" class=\"main\"/>").root();
    }

    @Test
    void equalsSameElementAndName() {
        AttributeNode a =
                new AttributeNode(element, "id", element.attributeObjects().get("id"));
        AttributeNode b =
                new AttributeNode(element, "id", element.attributeObjects().get("id"));
        assertEquals(a, b);
    }

    @Test
    void equalsReflexive() {
        AttributeNode a =
                new AttributeNode(element, "id", element.attributeObjects().get("id"));
        assertEquals(a, a);
    }

    @Test
    void notEqualDifferentName() {
        AttributeNode a =
                new AttributeNode(element, "id", element.attributeObjects().get("id"));
        AttributeNode b =
                new AttributeNode(element, "class", element.attributeObjects().get("class"));
        assertNotEquals(a, b);
    }

    @Test
    void notEqualDifferentElement() {
        Element element2 = Document.of("<root id=\"1\"/>").root();
        AttributeNode a =
                new AttributeNode(element, "id", element.attributeObjects().get("id"));
        AttributeNode b =
                new AttributeNode(element2, "id", element2.attributeObjects().get("id"));
        // Different element instances (identity check) => not equal
        assertNotEquals(a, b);
    }

    @Test
    void notEqualNull() {
        AttributeNode a =
                new AttributeNode(element, "id", element.attributeObjects().get("id"));
        assertNotEquals(a, null);
    }

    @Test
    void notEqualDifferentType() {
        AttributeNode a =
                new AttributeNode(element, "id", element.attributeObjects().get("id"));
        assertNotEquals(a, "id");
    }

    @Test
    void hashCodeConsistentWithEquals() {
        AttributeNode a =
                new AttributeNode(element, "id", element.attributeObjects().get("id"));
        AttributeNode b =
                new AttributeNode(element, "id", element.attributeObjects().get("id"));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void hashCodeDifferentForDifferentName() {
        AttributeNode a =
                new AttributeNode(element, "id", element.attributeObjects().get("id"));
        AttributeNode b =
                new AttributeNode(element, "class", element.attributeObjects().get("class"));
        // Not strictly required by contract, but extremely likely for distinct strings
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toStringContainsNameAndValue() {
        AttributeNode a =
                new AttributeNode(element, "id", element.attributeObjects().get("id"));
        String str = a.toString();
        assertNotNull(str);
        assertTrue(str.contains("id"), "toString() should contain the attribute name 'id', was: " + str);
        assertTrue(str.contains("1"), "toString() should contain the attribute value '1', was: " + str);
    }
}
