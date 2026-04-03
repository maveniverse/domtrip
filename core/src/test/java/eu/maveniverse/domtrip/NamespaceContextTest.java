/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class NamespaceContextTest {

    @Test
    void testEmptyConstructor() {
        NamespaceContext ctx = new NamespaceContext();
        assertNull(ctx.defaultNamespaceURI());
        assertTrue(ctx.declaredPrefixes().isEmpty());
        assertTrue(ctx.declaredNamespaceURIs().isEmpty());
    }

    @Test
    void testConstructorWithPrefixes() {
        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("soap", "http://schemas.xmlsoap.org/soap/envelope/");
        prefixes.put("xsd", "http://www.w3.org/2001/XMLSchema");

        NamespaceContext ctx = new NamespaceContext(prefixes, null);

        assertEquals("http://schemas.xmlsoap.org/soap/envelope/", ctx.namespaceURI("soap"));
        assertEquals("http://www.w3.org/2001/XMLSchema", ctx.namespaceURI("xsd"));
        assertNull(ctx.defaultNamespaceURI());
    }

    @Test
    void testConstructorWithDefaultNamespace() {
        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("soap", "http://schemas.xmlsoap.org/soap/envelope/");

        NamespaceContext ctx = new NamespaceContext(prefixes, "http://example.com/default");

        assertEquals("http://example.com/default", ctx.defaultNamespaceURI());
        assertEquals("http://schemas.xmlsoap.org/soap/envelope/", ctx.namespaceURI("soap"));
    }

    @Test
    void testNamespaceURIWithNullPrefix() {
        NamespaceContext ctx = new NamespaceContext(new HashMap<>(), "http://default.ns");
        assertEquals("http://default.ns", ctx.namespaceURI(null));
    }

    @Test
    void testNamespaceURINullPrefixNoDefault() {
        NamespaceContext ctx = new NamespaceContext();
        assertNull(ctx.namespaceURI(null));
    }

    @Test
    void testNamespaceURIBuiltInXml() {
        NamespaceContext ctx = new NamespaceContext();
        assertEquals("http://www.w3.org/XML/1998/namespace", ctx.namespaceURI("xml"));
    }

    @Test
    void testNamespaceURIBuiltInXmlns() {
        NamespaceContext ctx = new NamespaceContext();
        assertEquals("http://www.w3.org/2000/xmlns/", ctx.namespaceURI("xmlns"));
    }

    @Test
    void testNamespaceURIUnknownPrefix() {
        NamespaceContext ctx = new NamespaceContext();
        assertNull(ctx.namespaceURI("unknown"));
    }

    @Test
    void testPrefixForURI() {
        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("soap", "http://schemas.xmlsoap.org/soap/envelope/");

        NamespaceContext ctx = new NamespaceContext(prefixes, null);
        assertEquals("soap", ctx.prefix("http://schemas.xmlsoap.org/soap/envelope/"));
    }

    @Test
    void testPrefixForNull() {
        NamespaceContext ctx = new NamespaceContext();
        assertNull(ctx.prefix(null));
    }

    @Test
    void testPrefixForBuiltInXml() {
        NamespaceContext ctx = new NamespaceContext();
        assertEquals("xml", ctx.prefix("http://www.w3.org/XML/1998/namespace"));
    }

    @Test
    void testPrefixForBuiltInXmlns() {
        NamespaceContext ctx = new NamespaceContext();
        assertEquals("xmlns", ctx.prefix("http://www.w3.org/2000/xmlns/"));
    }

    @Test
    void testPrefixForDefaultNamespace() {
        NamespaceContext ctx = new NamespaceContext(new HashMap<>(), "http://default.ns");
        // Default namespace has no prefix
        assertNull(ctx.prefix("http://default.ns"));
    }

    @Test
    void testPrefixForUnknownURI() {
        NamespaceContext ctx = new NamespaceContext();
        assertNull(ctx.prefix("http://unknown.ns"));
    }

    @Test
    void testPrefixesForURI() {
        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("soap", "http://schemas.xmlsoap.org/soap/envelope/");
        prefixes.put("s", "http://schemas.xmlsoap.org/soap/envelope/");

        NamespaceContext ctx = new NamespaceContext(prefixes, null);
        List<String> result =
                ctx.prefixes("http://schemas.xmlsoap.org/soap/envelope/").collect(Collectors.toList());
        assertEquals(2, result.size());
        assertTrue(result.contains("soap"));
        assertTrue(result.contains("s"));
    }

    @Test
    void testPrefixesForNull() {
        NamespaceContext ctx = new NamespaceContext();
        List<String> result = ctx.prefixes(null).collect(Collectors.toList());
        assertTrue(result.isEmpty());
    }

    @Test
    void testPrefixesForBuiltInXml() {
        NamespaceContext ctx = new NamespaceContext();
        List<String> result =
                ctx.prefixes("http://www.w3.org/XML/1998/namespace").collect(Collectors.toList());
        assertEquals(1, result.size());
        assertEquals("xml", result.get(0));
    }

    @Test
    void testPrefixesForBuiltInXmlns() {
        NamespaceContext ctx = new NamespaceContext();
        List<String> result = ctx.prefixes("http://www.w3.org/2000/xmlns/").collect(Collectors.toList());
        assertEquals(1, result.size());
        assertEquals("xmlns", result.get(0));
    }

    @Test
    void testPrefixesForUnknownURI() {
        NamespaceContext ctx = new NamespaceContext();
        List<String> result = ctx.prefixes("http://unknown").collect(Collectors.toList());
        assertTrue(result.isEmpty());
    }

    @Test
    void testIsPrefixDeclaredNull() {
        NamespaceContext ctx = new NamespaceContext(new HashMap<>(), "http://default.ns");
        assertTrue(ctx.isPrefixDeclared(null)); // has default namespace
    }

    @Test
    void testIsPrefixDeclaredNullNoDefault() {
        NamespaceContext ctx = new NamespaceContext();
        assertFalse(ctx.isPrefixDeclared(null));
    }

    @Test
    void testIsPrefixDeclaredBuiltIn() {
        NamespaceContext ctx = new NamespaceContext();
        assertTrue(ctx.isPrefixDeclared("xml"));
        assertTrue(ctx.isPrefixDeclared("xmlns"));
    }

    @Test
    void testIsPrefixDeclaredCustom() {
        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("soap", "http://schemas.xmlsoap.org/soap/envelope/");
        NamespaceContext ctx = new NamespaceContext(prefixes, null);
        assertTrue(ctx.isPrefixDeclared("soap"));
        assertFalse(ctx.isPrefixDeclared("xsd"));
    }

    @Test
    void testIsNamespaceUriDeclaredNull() {
        NamespaceContext ctx = new NamespaceContext();
        assertFalse(ctx.isNamespaceUriDeclared(null));
    }

    @Test
    void testIsNamespaceUriDeclaredBuiltIn() {
        NamespaceContext ctx = new NamespaceContext();
        assertTrue(ctx.isNamespaceUriDeclared("http://www.w3.org/XML/1998/namespace"));
        assertTrue(ctx.isNamespaceUriDeclared("http://www.w3.org/2000/xmlns/"));
    }

    @Test
    void testIsNamespaceUriDeclaredDefault() {
        NamespaceContext ctx = new NamespaceContext(new HashMap<>(), "http://default.ns");
        assertTrue(ctx.isNamespaceUriDeclared("http://default.ns"));
    }

    @Test
    void testIsNamespaceUriDeclaredCustom() {
        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("soap", "http://schemas.xmlsoap.org/soap/envelope/");
        NamespaceContext ctx = new NamespaceContext(prefixes, null);
        assertTrue(ctx.isNamespaceUriDeclared("http://schemas.xmlsoap.org/soap/envelope/"));
        assertFalse(ctx.isNamespaceUriDeclared("http://unknown.ns"));
    }

    @Test
    void testDeclaredPrefixes() {
        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("soap", "http://schemas.xmlsoap.org/soap/envelope/");
        prefixes.put("xsd", "http://www.w3.org/2001/XMLSchema");
        NamespaceContext ctx = new NamespaceContext(prefixes, null);

        Set<String> declared = ctx.declaredPrefixes();
        assertEquals(2, declared.size());
        assertTrue(declared.contains("soap"));
        assertTrue(declared.contains("xsd"));
    }

    @Test
    void testDeclaredPrefixesEmpty() {
        NamespaceContext ctx = new NamespaceContext();
        assertTrue(ctx.declaredPrefixes().isEmpty());
    }

    @Test
    void testDeclaredNamespaceURIs() {
        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("soap", "http://schemas.xmlsoap.org/soap/envelope/");
        NamespaceContext ctx = new NamespaceContext(prefixes, "http://default.ns");

        Set<String> uris = ctx.declaredNamespaceURIs();
        assertTrue(uris.contains("http://schemas.xmlsoap.org/soap/envelope/"));
        assertTrue(uris.contains("http://default.ns"));
    }

    @Test
    void testDeclaredNamespaceURIsNoDefault() {
        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("soap", "http://schemas.xmlsoap.org/soap/envelope/");
        NamespaceContext ctx = new NamespaceContext(prefixes, null);

        Set<String> uris = ctx.declaredNamespaceURIs();
        assertEquals(1, uris.size());
        assertTrue(uris.contains("http://schemas.xmlsoap.org/soap/envelope/"));
    }

    @Test
    void testToStringWithDefaultNamespace() {
        NamespaceContext ctx = new NamespaceContext(new HashMap<>(), "http://default.ns");
        String str = ctx.toString();
        assertTrue(str.contains("default=http://default.ns"));
        assertTrue(str.startsWith("NamespaceContext{"));
        assertTrue(str.endsWith("}"));
    }

    @Test
    void testToStringWithPrefixes() {
        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("soap", "http://schemas.xmlsoap.org/soap/envelope/");
        NamespaceContext ctx = new NamespaceContext(prefixes, null);
        String str = ctx.toString();
        assertTrue(str.contains("prefixes="));
        assertTrue(str.contains("soap"));
    }

    @Test
    void testToStringWithDefaultAndPrefixes() {
        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("soap", "http://schemas.xmlsoap.org/soap/envelope/");
        NamespaceContext ctx = new NamespaceContext(prefixes, "http://default.ns");
        String str = ctx.toString();
        assertTrue(str.contains("default=http://default.ns"));
        assertTrue(str.contains("prefixes="));
    }

    @Test
    void testToStringEmpty() {
        NamespaceContext ctx = new NamespaceContext();
        String str = ctx.toString();
        assertTrue(str.startsWith("NamespaceContext{"));
        assertTrue(str.contains("prefixes="));
    }
}
