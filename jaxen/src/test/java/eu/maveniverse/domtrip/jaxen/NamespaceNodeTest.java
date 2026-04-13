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
 * Tests for {@link NamespaceNode} equals, hashCode, and toString.
 */
class NamespaceNodeTest {

    private Element element;

    @BeforeEach
    void setUp() {
        element = Document.of("<root xmlns:ns=\"http://example.com\" xmlns=\"http://default.com\"/>")
                .root();
    }

    @Test
    void equalsSameElementPrefixAndUri() {
        NamespaceNode a = new NamespaceNode(element, "ns", "http://example.com");
        NamespaceNode b = new NamespaceNode(element, "ns", "http://example.com");
        assertEquals(a, b);
    }

    @Test
    void equalsReflexive() {
        NamespaceNode a = new NamespaceNode(element, "ns", "http://example.com");
        assertEquals(a, a);
    }

    @Test
    void notEqualDifferentPrefix() {
        NamespaceNode a = new NamespaceNode(element, "ns", "http://example.com");
        NamespaceNode b = new NamespaceNode(element, "other", "http://example.com");
        assertNotEquals(a, b);
    }

    @Test
    void notEqualDifferentUri() {
        NamespaceNode a = new NamespaceNode(element, "ns", "http://example.com");
        NamespaceNode b = new NamespaceNode(element, "ns", "http://other.com");
        assertNotEquals(a, b);
    }

    @Test
    void notEqualDifferentElement() {
        Element element2 =
                Document.of("<root xmlns:ns=\"http://example.com\"/>").root();
        NamespaceNode a = new NamespaceNode(element, "ns", "http://example.com");
        NamespaceNode b = new NamespaceNode(element2, "ns", "http://example.com");
        // Different element instances (identity check) => not equal
        assertNotEquals(a, b);
    }

    @Test
    void notEqualNull() {
        NamespaceNode a = new NamespaceNode(element, "ns", "http://example.com");
        assertNotEquals(null, a);
    }

    @Test
    void notEqualDifferentType() {
        NamespaceNode a = new NamespaceNode(element, "ns", "http://example.com");
        assertNotEquals("ns", a);
    }

    @Test
    void defaultNamespaceEmptyPrefix() {
        NamespaceNode a = new NamespaceNode(element, "", "http://default.com");
        NamespaceNode b = new NamespaceNode(element, "", "http://default.com");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void hashCodeConsistentWithEquals() {
        NamespaceNode a = new NamespaceNode(element, "ns", "http://example.com");
        NamespaceNode b = new NamespaceNode(element, "ns", "http://example.com");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void hashCodeDifferentForDifferentPrefix() {
        NamespaceNode a = new NamespaceNode(element, "ns", "http://example.com");
        NamespaceNode b = new NamespaceNode(element, "other", "http://example.com");
        // Not strictly required but practically guaranteed for distinct strings
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toStringContainsPrefixAndUri() {
        NamespaceNode a = new NamespaceNode(element, "ns", "http://example.com");
        String str = a.toString();
        assertNotNull(str);
        assertTrue(str.contains("ns"), "toString() should contain the prefix 'ns', was: " + str);
        assertTrue(
                str.contains("http://example.com"),
                "toString() should contain the URI 'http://example.com', was: " + str);
    }
}
