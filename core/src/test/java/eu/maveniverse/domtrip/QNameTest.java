/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class QNameTest {

    @Test
    void testOfLocalNameOnly() {
        QName qname = QName.of("element");
        assertEquals("element", qname.localName());
        assertEquals("", qname.namespaceURI());
        assertNull(qname.prefix());
        assertFalse(qname.hasNamespace());
        assertFalse(qname.hasPrefix());
    }

    @Test
    void testOfWithNamespace() {
        QName qname = QName.of("http://example.com", "element");
        assertEquals("element", qname.localName());
        assertEquals("http://example.com", qname.namespaceURI());
        assertNull(qname.prefix());
        assertTrue(qname.hasNamespace());
        assertFalse(qname.hasPrefix());
    }

    @Test
    void testOfWithNamespaceAndPrefix() {
        QName qname = QName.of("http://example.com", "element", "ns");
        assertEquals("element", qname.localName());
        assertEquals("http://example.com", qname.namespaceURI());
        assertEquals("ns", qname.prefix());
        assertTrue(qname.hasNamespace());
        assertTrue(qname.hasPrefix());
    }

    @Test
    void testOfNullNamespace() {
        QName qname = QName.of(null, "element");
        assertEquals("", qname.namespaceURI());
        assertFalse(qname.hasNamespace());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void testOfNullEmptyOrBlankPrefix(String prefix) {
        QName qname = QName.of("http://example.com", "element", prefix);
        assertNull(qname.prefix());
        assertFalse(qname.hasPrefix());
    }

    @Test
    void testOfNullLocalNameThrows() {
        assertThrows(DomTripException.class, () -> QName.of(null));
    }

    @Test
    void testOfEmptyLocalNameThrows() {
        assertThrows(DomTripException.class, () -> QName.of(""));
    }

    @Test
    void testOfBlankLocalNameThrows() {
        assertThrows(DomTripException.class, () -> QName.of("  "));
    }

    @Test
    void testQualifiedNameWithPrefix() {
        QName qname = QName.of("http://example.com", "element", "ns");
        assertEquals("ns:element", qname.qualifiedName());
    }

    @Test
    void testQualifiedNameWithoutPrefix() {
        QName qname = QName.of("element");
        assertEquals("element", qname.qualifiedName());
    }

    @Test
    void testParseSimpleName() {
        QName qname = QName.parse("element");
        assertEquals("element", qname.localName());
        assertNull(qname.prefix());
    }

    @Test
    void testParsePrefixedName() {
        QName qname = QName.parse("soap:Envelope");
        assertEquals("Envelope", qname.localName());
        assertEquals("soap", qname.prefix());
    }

    @Test
    void testParseNullThrows() {
        assertThrows(DomTripException.class, () -> QName.parse(null));
    }

    @Test
    void testParseEmptyThrows() {
        assertThrows(DomTripException.class, () -> QName.parse(""));
    }

    @Test
    void testParseBlankThrows() {
        assertThrows(DomTripException.class, () -> QName.parse("  "));
    }

    @Test
    void testParseLeadingColonThrows() {
        assertThrows(DomTripException.class, () -> QName.parse(":element"));
    }

    @Test
    void testParseTrailingColonThrows() {
        assertThrows(DomTripException.class, () -> QName.parse("prefix:"));
    }

    @Test
    void testWithNamespaceURI() {
        QName original = QName.of("http://old.com", "element", "ns");
        QName updated = original.withNamespaceURI("http://new.com");
        assertEquals("http://new.com", updated.namespaceURI());
        assertEquals("element", updated.localName());
        assertEquals("ns", updated.prefix());
    }

    @Test
    void testWithPrefix() {
        QName original = QName.of("http://example.com", "element", "old");
        QName updated = original.withPrefix("new");
        assertEquals("new", updated.prefix());
        assertEquals("http://example.com", updated.namespaceURI());
        assertEquals("element", updated.localName());
    }

    @Test
    void testWithPrefixNull() {
        QName original = QName.of("http://example.com", "element", "ns");
        QName updated = original.withPrefix(null);
        assertNull(updated.prefix());
    }

    @Test
    void testMatchesNamespaceAndLocalName() {
        QName qname = QName.of("http://example.com", "element");
        assertTrue(qname.matches("http://example.com", "element"));
        assertFalse(qname.matches("http://other.com", "element"));
        assertFalse(qname.matches("http://example.com", "other"));
    }

    @Test
    void testMatchesNullNamespace() {
        QName qname = QName.of("element");
        assertTrue(qname.matches(null, "element"));
        assertTrue(qname.matches("", "element"));
    }

    @Test
    void testMatchesQName() {
        QName qname1 = QName.of("http://example.com", "element", "ns1");
        QName qname2 = QName.of("http://example.com", "element", "ns2");
        // Prefix is not considered for matching
        assertTrue(qname1.matches(qname2));
    }

    @Test
    void testMatchesQNameNull() {
        QName qname = QName.of("element");
        assertFalse(qname.matches((QName) null));
    }

    @Test
    void testMatchesQNameDifferentNamespace() {
        QName qname1 = QName.of("http://one.com", "element");
        QName qname2 = QName.of("http://two.com", "element");
        assertFalse(qname1.matches(qname2));
    }

    @Test
    void testEqualsAndHashCode() {
        QName q1 = QName.of("http://example.com", "element", "ns");
        QName q2 = QName.of("http://example.com", "element", "ns");
        assertEquals(q1, q2);
        assertEquals(q1.hashCode(), q2.hashCode());
    }

    @Test
    void testEqualsSameInstance() {
        QName q = QName.of("element");
        assertEquals(q, q);
    }

    @Test
    void testNotEqualsNull() {
        QName q = QName.of("element");
        assertNotEquals(null, q);
    }

    @Test
    void testNotEqualsDifferentType() {
        QName q = QName.of("element");
        assertNotEquals("element", q);
    }

    @Test
    void testNotEqualsDifferentPrefix() {
        // Prefix IS considered for equals (unlike matches)
        QName q1 = QName.of("http://example.com", "element", "ns1");
        QName q2 = QName.of("http://example.com", "element", "ns2");
        assertNotEquals(q1, q2);
    }

    @Test
    void testNotEqualsDifferentLocalName() {
        QName q1 = QName.of("http://example.com", "element1");
        QName q2 = QName.of("http://example.com", "element2");
        assertNotEquals(q1, q2);
    }

    @Test
    void testNotEqualsDifferentNamespace() {
        QName q1 = QName.of("http://one.com", "element");
        QName q2 = QName.of("http://two.com", "element");
        assertNotEquals(q1, q2);
    }

    @Test
    void testToStringWithNamespace() {
        QName qname = QName.of("http://example.com", "element", "ns");
        String str = qname.toString();
        assertTrue(str.contains("http://example.com"));
        assertTrue(str.contains("ns:element"));
    }

    @Test
    void testToStringWithoutNamespace() {
        QName qname = QName.of("element");
        String str = qname.toString();
        assertTrue(str.contains("element"));
        assertFalse(str.contains("http"));
    }

    @Test
    void testLocalNameTrimmed() {
        QName qname = QName.of("  element  ");
        assertEquals("element", qname.localName());
    }

    @Test
    void testPrefixTrimmed() {
        QName qname = QName.of("http://example.com", "element", "  ns  ");
        assertEquals("ns", qname.prefix());
    }

    @Test
    void testParseTrimmed() {
        QName qname = QName.parse("  soap:Envelope  ");
        assertEquals("soap", qname.prefix());
        assertEquals("Envelope", qname.localName());
    }
}
