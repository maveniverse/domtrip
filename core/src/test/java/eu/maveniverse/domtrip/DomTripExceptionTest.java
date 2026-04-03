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

class DomTripExceptionTest {

    @Test
    void testMessageOnly() {
        DomTripException ex = new DomTripException("test error");
        assertEquals("test error", ex.getMessage());
        assertNull(ex.getCause());
        assertEquals(-1, ex.position());
        assertNull(ex.xmlContent());
    }

    @Test
    void testMessageAndCause() {
        RuntimeException cause = new RuntimeException("root cause");
        DomTripException ex = new DomTripException("test error", cause);
        assertEquals("test error", ex.getMessage());
        assertSame(cause, ex.getCause());
        assertEquals(-1, ex.position());
        assertNull(ex.xmlContent());
    }

    @Test
    void testCauseOnly() {
        RuntimeException cause = new RuntimeException("root cause");
        DomTripException ex = new DomTripException(cause);
        assertSame(cause, ex.getCause());
        assertEquals(-1, ex.position());
        assertNull(ex.xmlContent());
    }

    @Test
    void testMessagePositionAndXmlContent() {
        DomTripException ex = new DomTripException("parse error", 42, "<root>bad</root>");
        assertTrue(ex.getMessage().contains("parse error"));
        assertTrue(ex.getMessage().contains("at position 42"));
        assertEquals(42, ex.position());
        assertEquals("<root>bad</root>", ex.xmlContent());
    }

    @Test
    void testNegativePosition() {
        DomTripException ex = new DomTripException("error", -1, "<root/>");
        assertEquals("error", ex.getMessage());
        assertFalse(ex.getMessage().contains("at position"));
        assertEquals(-1, ex.position());
        assertEquals("<root/>", ex.xmlContent());
    }

    @Test
    void testZeroPosition() {
        DomTripException ex = new DomTripException("error at start", 0, "<bad/>");
        assertTrue(ex.getMessage().contains("at position 0"));
        assertEquals(0, ex.position());
    }

    @Test
    @SuppressWarnings("deprecation")
    void testDeprecatedGetPosition() {
        DomTripException ex = new DomTripException("error", 10, "<xml/>");
        assertEquals(10, ex.getPosition());
        assertEquals(ex.position(), ex.getPosition());
    }

    @Test
    @SuppressWarnings("deprecation")
    void testDeprecatedGetXmlContent() {
        DomTripException ex = new DomTripException("error", 10, "<xml/>");
        assertEquals("<xml/>", ex.getXmlContent());
        assertEquals(ex.xmlContent(), ex.getXmlContent());
    }

    @Test
    void testNullXmlContent() {
        DomTripException ex = new DomTripException("error", 5, null);
        assertNull(ex.xmlContent());
    }

    @Test
    void testMessageWithNullCause() {
        DomTripException ex = new DomTripException("test error", null);
        assertEquals("test error", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void testIsRuntimeException() {
        DomTripException ex = new DomTripException("test");
        assertInstanceOf(RuntimeException.class, ex);
    }
}
