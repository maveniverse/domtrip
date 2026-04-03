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

class NamespaceResolverTest {

    @Test
    void testResolveNamespaceURINullElement() {
        assertNull(NamespaceResolver.resolveNamespaceURI(null, "prefix"));
    }

    @Test
    void testResolveNamespaceURIBuiltInXml() {
        String xml = "<root/>";
        Editor editor = new Editor(Document.of(xml));
        assertEquals(
                "http://www.w3.org/XML/1998/namespace", NamespaceResolver.resolveNamespaceURI(editor.root(), "xml"));
    }

    @Test
    void testResolveNamespaceURIBuiltInXmlns() {
        String xml = "<root/>";
        Editor editor = new Editor(Document.of(xml));
        assertEquals("http://www.w3.org/2000/xmlns/", NamespaceResolver.resolveNamespaceURI(editor.root(), "xmlns"));
    }

    @Test
    void testResolveNamespaceURINullElementBuiltInXml() {
        assertEquals("http://www.w3.org/XML/1998/namespace", NamespaceResolver.resolveNamespaceURI(null, "xml"));
    }

    @Test
    void testResolveNamespaceURINullElementBuiltInXmlns() {
        assertEquals("http://www.w3.org/2000/xmlns/", NamespaceResolver.resolveNamespaceURI(null, "xmlns"));
    }

    @Test
    void testResolveNamespaceURINullElementNullPrefix() {
        assertNull(NamespaceResolver.resolveNamespaceURI(null, null));
    }

    @Test
    void testResolveNamespaceURIFromElement() {
        String xml = "<root xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><child/></root>";
        Editor editor = new Editor(Document.of(xml));
        Element child = editor.root().childElement("child").orElseThrow();
        assertEquals("http://schemas.xmlsoap.org/soap/envelope/", NamespaceResolver.resolveNamespaceURI(child, "soap"));
    }

    @Test
    void testResolveNamespaceURIDefaultNamespace() {
        String xml = "<root xmlns=\"http://example.com\"><child/></root>";
        Editor editor = new Editor(Document.of(xml));
        Element child = editor.root().childElement("child").orElseThrow();
        assertEquals("http://example.com", NamespaceResolver.resolveNamespaceURI(child, null));
    }

    @Test
    void testResolveNamespaceURIInherited() {
        String xml = "<root xmlns:ns=\"http://example.com\"><parent><child/></parent></root>";
        Editor editor = new Editor(Document.of(xml));
        Element child = editor.root()
                .childElement("parent")
                .orElseThrow()
                .childElement("child")
                .orElseThrow();
        assertEquals("http://example.com", NamespaceResolver.resolveNamespaceURI(child, "ns"));
    }

    @Test
    void testResolveNamespaceURINotFound() {
        String xml = "<root><child/></root>";
        Editor editor = new Editor(Document.of(xml));
        assertNull(NamespaceResolver.resolveNamespaceURI(editor.root(), "unknown"));
    }

    @Test
    void testResolvePrefixNullElement() {
        assertNull(NamespaceResolver.resolvePrefix(null, "http://example.com"));
    }

    @Test
    void testResolvePrefixNullURI() {
        String xml = "<root/>";
        Editor editor = new Editor(Document.of(xml));
        assertNull(NamespaceResolver.resolvePrefix(editor.root(), null));
    }

    @Test
    void testResolvePrefixBuiltInXml() {
        String xml = "<root/>";
        Editor editor = new Editor(Document.of(xml));
        assertEquals("xml", NamespaceResolver.resolvePrefix(editor.root(), "http://www.w3.org/XML/1998/namespace"));
    }

    @Test
    void testResolvePrefixBuiltInXmlns() {
        String xml = "<root/>";
        Editor editor = new Editor(Document.of(xml));
        assertEquals("xmlns", NamespaceResolver.resolvePrefix(editor.root(), "http://www.w3.org/2000/xmlns/"));
    }

    @Test
    void testResolvePrefixFromElement() {
        String xml = "<root xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><child/></root>";
        Editor editor = new Editor(Document.of(xml));
        Element child = editor.root().childElement("child").orElseThrow();
        assertEquals("soap", NamespaceResolver.resolvePrefix(child, "http://schemas.xmlsoap.org/soap/envelope/"));
    }

    @Test
    void testResolvePrefixDefaultNamespace() {
        String xml = "<root xmlns=\"http://example.com\"><child/></root>";
        Editor editor = new Editor(Document.of(xml));
        Element child = editor.root().childElement("child").orElseThrow();
        // Default namespace returns null for prefix
        assertNull(NamespaceResolver.resolvePrefix(child, "http://example.com"));
    }

    @Test
    void testResolvePrefixInherited() {
        String xml = "<root xmlns:ns=\"http://example.com\"><parent><child/></parent></root>";
        Editor editor = new Editor(Document.of(xml));
        Element child = editor.root()
                .childElement("parent")
                .orElseThrow()
                .childElement("child")
                .orElseThrow();
        assertEquals("ns", NamespaceResolver.resolvePrefix(child, "http://example.com"));
    }

    @Test
    void testResolvePrefixNotFound() {
        String xml = "<root><child/></root>";
        Editor editor = new Editor(Document.of(xml));
        assertNull(NamespaceResolver.resolvePrefix(editor.root(), "http://unknown.ns"));
    }

    @Test
    void testIsNamespaceInScopeNull() {
        String xml = "<root/>";
        Editor editor = new Editor(Document.of(xml));
        assertFalse(NamespaceResolver.isNamespaceInScope(editor.root(), null));
    }

    @Test
    void testIsNamespaceInScopeBuiltInXml() {
        String xml = "<root/>";
        Editor editor = new Editor(Document.of(xml));
        assertTrue(NamespaceResolver.isNamespaceInScope(editor.root(), "http://www.w3.org/XML/1998/namespace"));
    }

    @Test
    void testIsNamespaceInScopeBuiltInXmlns() {
        String xml = "<root/>";
        Editor editor = new Editor(Document.of(xml));
        assertTrue(NamespaceResolver.isNamespaceInScope(editor.root(), "http://www.w3.org/2000/xmlns/"));
    }

    @Test
    void testIsNamespaceInScopePrefixed() {
        String xml = "<root xmlns:ns=\"http://example.com\"><child/></root>";
        Editor editor = new Editor(Document.of(xml));
        Element child = editor.root().childElement("child").orElseThrow();
        assertTrue(NamespaceResolver.isNamespaceInScope(child, "http://example.com"));
    }

    @Test
    void testIsNamespaceInScopeDefault() {
        String xml = "<root xmlns=\"http://example.com\"><child/></root>";
        Editor editor = new Editor(Document.of(xml));
        Element child = editor.root().childElement("child").orElseThrow();
        assertTrue(NamespaceResolver.isNamespaceInScope(child, "http://example.com"));
    }

    @Test
    void testIsNamespaceInScopeNotInScope() {
        String xml = "<root><child/></root>";
        Editor editor = new Editor(Document.of(xml));
        assertFalse(NamespaceResolver.isNamespaceInScope(editor.root(), "http://unknown.ns"));
    }

    @Test
    void testBuildNamespaceContextNullElement() {
        NamespaceContext ctx = NamespaceResolver.buildNamespaceContext(null);
        assertNotNull(ctx);
        assertNull(ctx.defaultNamespaceURI());
        assertTrue(ctx.declaredPrefixes().isEmpty());
    }

    @Test
    void testBuildNamespaceContextWithPrefixes() {
        String xml =
                "<root xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><child/></root>";
        Editor editor = new Editor(Document.of(xml));
        Element child = editor.root().childElement("child").orElseThrow();

        NamespaceContext ctx = NamespaceResolver.buildNamespaceContext(child);
        assertEquals("http://schemas.xmlsoap.org/soap/envelope/", ctx.namespaceURI("soap"));
        assertEquals("http://www.w3.org/2001/XMLSchema", ctx.namespaceURI("xsd"));
    }

    @Test
    void testBuildNamespaceContextWithDefaultNamespace() {
        String xml = "<root xmlns=\"http://example.com\"><child/></root>";
        Editor editor = new Editor(Document.of(xml));
        Element child = editor.root().childElement("child").orElseThrow();

        NamespaceContext ctx = NamespaceResolver.buildNamespaceContext(child);
        assertEquals("http://example.com", ctx.defaultNamespaceURI());
    }

    @Test
    void testBuildNamespaceContextInheritance() {
        String xml = "<root xmlns:outer=\"http://outer.com\">"
                + "<parent xmlns:inner=\"http://inner.com\">"
                + "<child/>"
                + "</parent>"
                + "</root>";
        Editor editor = new Editor(Document.of(xml));
        Element child = editor.root()
                .childElement("parent")
                .orElseThrow()
                .childElement("child")
                .orElseThrow();

        NamespaceContext ctx = NamespaceResolver.buildNamespaceContext(child);
        assertEquals("http://outer.com", ctx.namespaceURI("outer"));
        assertEquals("http://inner.com", ctx.namespaceURI("inner"));
    }

    @Test
    void testBuildNamespaceContextCloserDeclarationWins() {
        String xml = "<root xmlns:ns=\"http://outer.com\">" + "<child xmlns:ns=\"http://inner.com\"/>" + "</root>";
        Editor editor = new Editor(Document.of(xml));
        Element child = editor.root().childElement("child").orElseThrow();

        NamespaceContext ctx = NamespaceResolver.buildNamespaceContext(child);
        // Closer declaration should win
        assertEquals("http://inner.com", ctx.namespaceURI("ns"));
    }

    @Test
    void testSplitQualifiedNameNull() {
        String[] parts = NamespaceResolver.splitQualifiedName(null);
        assertNull(parts[0]);
        assertEquals("", parts[1]);
    }

    @Test
    void testSplitQualifiedNameEmpty() {
        String[] parts = NamespaceResolver.splitQualifiedName("");
        assertNull(parts[0]);
        assertEquals("", parts[1]);
    }

    @Test
    void testSplitQualifiedNameNoPrefix() {
        String[] parts = NamespaceResolver.splitQualifiedName("localName");
        assertNull(parts[0]);
        assertEquals("localName", parts[1]);
    }

    @Test
    void testSplitQualifiedNameWithPrefix() {
        String[] parts = NamespaceResolver.splitQualifiedName("prefix:localName");
        assertEquals("prefix", parts[0]);
        assertEquals("localName", parts[1]);
    }

    @Test
    void testSplitQualifiedNameEmptyPrefix() {
        String[] parts = NamespaceResolver.splitQualifiedName(":localName");
        assertNull(parts[0]); // empty prefix becomes null
        assertEquals("localName", parts[1]);
    }

    @Test
    void testCreateQualifiedNameBothPresent() {
        assertEquals("prefix:localName", NamespaceResolver.createQualifiedName("prefix", "localName"));
    }

    @Test
    void testCreateQualifiedNameNullPrefix() {
        assertEquals("localName", NamespaceResolver.createQualifiedName(null, "localName"));
    }

    @Test
    void testCreateQualifiedNameEmptyPrefix() {
        assertEquals("localName", NamespaceResolver.createQualifiedName("", "localName"));
    }

    @Test
    void testCreateQualifiedNameNullLocalName() {
        assertEquals("prefix", NamespaceResolver.createQualifiedName("prefix", null));
    }

    @Test
    void testCreateQualifiedNameEmptyLocalName() {
        assertEquals("prefix", NamespaceResolver.createQualifiedName("prefix", ""));
    }

    @Test
    void testCreateQualifiedNameBothNull() {
        assertEquals("", NamespaceResolver.createQualifiedName(null, null));
    }

    @Test
    void testCreateQualifiedNameBothEmpty() {
        assertEquals("", NamespaceResolver.createQualifiedName("", ""));
    }
}
