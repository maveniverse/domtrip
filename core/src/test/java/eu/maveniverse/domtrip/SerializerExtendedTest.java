/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SerializerExtendedTest {

    // ===== Configuration Tests =====

    @Test
    void testDefaultConstructorDefaults() {
        Serializer s = new Serializer();
        assertEquals("  ", s.indentString());
        assertFalse(s.isPrettyPrint());
        assertEquals("\n", s.lineEnding());
        assertEquals(EmptyElementStyle.SELF_CLOSING, s.emptyElementStyle());
    }

    @Test
    void testConfigConstructor() {
        DomTripConfig config =
                DomTripConfig.prettyPrint().withIndentString("\t").withLineEnding("\r\n");
        Serializer s = new Serializer(config);
        assertTrue(s.isPrettyPrint());
        assertEquals("\t", s.indentString());
        assertEquals("\r\n", s.lineEnding());
    }

    @Test
    void testSetIndentString() {
        Serializer s = new Serializer();
        s.setIndentString("    ");
        assertEquals("    ", s.indentString());
    }

    @Test
    void testSetIndentStringNull() {
        Serializer s = new Serializer();
        s.setIndentString(null);
        assertEquals("  ", s.indentString()); // defaults to two spaces
    }

    @Test
    void testSetPrettyPrint() {
        Serializer s = new Serializer();
        s.setPrettyPrint(true);
        assertTrue(s.isPrettyPrint());
        s.setPrettyPrint(false);
        assertFalse(s.isPrettyPrint());
    }

    @Test
    void testSetLineEnding() {
        Serializer s = new Serializer();
        s.setLineEnding("\r\n");
        assertEquals("\r\n", s.lineEnding());
    }

    @Test
    void testSetLineEndingNull() {
        Serializer s = new Serializer();
        s.setLineEnding(null);
        assertEquals("\n", s.lineEnding()); // defaults to \n
    }

    @Test
    void testSetEmptyElementStyle() {
        Serializer s = new Serializer();
        s.setEmptyElementStyle(EmptyElementStyle.EXPANDED);
        assertEquals(EmptyElementStyle.EXPANDED, s.emptyElementStyle());
    }

    @Test
    void testSetEmptyElementStyleNull() {
        Serializer s = new Serializer();
        s.setEmptyElementStyle(null);
        assertEquals(EmptyElementStyle.SELF_CLOSING, s.emptyElementStyle()); // defaults
    }

    @Test
    @SuppressWarnings("deprecation")
    void testDeprecatedGetters() {
        Serializer s = new Serializer();
        assertEquals(s.indentString(), s.getIndentString());
        assertEquals(s.lineEnding(), s.getLineEnding());
        assertEquals(s.emptyElementStyle(), s.getEmptyElementStyle());
    }

    // ===== Document Serialization =====

    @Test
    void testSerializeNullDocument() {
        Serializer s = new Serializer();
        assertEquals("", s.serialize((Document) null));
    }

    @Test
    void testSerializeNullNode() {
        Serializer s = new Serializer();
        assertEquals("", s.serialize((Node) null));
    }

    @Test
    void testSerializeDocumentWithConfig() {
        String xml = "<?xml version=\"1.0\"?>\n<root><child>text</child></root>";
        Document doc = Document.of(xml);
        Serializer s = new Serializer();

        DomTripConfig config = DomTripConfig.prettyPrint().withIndentString("    ");
        String result = s.serialize(doc, config);
        assertNotNull(result);
        assertTrue(result.contains("<root>"));
    }

    @Test
    void testSerializeDocumentWithConfigNull() {
        Serializer s = new Serializer();
        assertEquals("", s.serialize(null, DomTripConfig.defaults()));
    }

    @Test
    void testSerializeUnmodifiedDocumentPreservesFormatting() {
        String xml = "<?xml version=\"1.0\"?>\n<root>\n  <child>text</child>\n</root>";
        Document doc = Document.of(xml);
        Serializer s = new Serializer();
        // Should preserve original formatting since not modified and not pretty print
        String result = s.serialize(doc);
        assertEquals(xml, result);
    }

    @Test
    void testSerializeModifiedDocument() {
        String xml = "<?xml version=\"1.0\"?>\n<root><child>text</child></root>";
        Document doc = Document.of(xml);
        doc.root().addChild(Element.of("newChild"));
        Serializer s = new Serializer();
        String result = s.serialize(doc);
        assertTrue(result.contains("<newChild"));
    }

    // ===== Pretty Print Tests =====

    @Test
    void testPrettyPrintDocument() {
        String xml = "<root><child1>a</child1><child2>b</child2></root>";
        Document doc = Document.of(xml);
        Serializer s = new Serializer();
        s.setPrettyPrint(true);
        s.setIndentString("  ");
        String result = s.serialize(doc);
        assertTrue(result.contains("\n"));
    }

    @Test
    void testPrettyPrintWithDoctype() {
        String xml = "<?xml version=\"1.0\"?>\n<!DOCTYPE root SYSTEM \"root.dtd\">\n<root/>";
        Document doc = Document.of(xml);
        Serializer s = new Serializer();
        s.setPrettyPrint(true);
        String result = s.serialize(doc);
        assertTrue(result.contains("<!DOCTYPE"));
        assertTrue(result.contains("<root"));
    }

    @ParameterizedTest
    @MethodSource("emptyElementStyleProvider")
    void testPrettyPrintEmptyElementStyle(EmptyElementStyle style, String expectedPattern) {
        Document doc = Document.of("<root><empty/></root>");
        doc.root().markModified();
        Serializer s = new Serializer();
        s.setPrettyPrint(true);
        s.setEmptyElementStyle(style);
        String result = s.serialize(doc);
        assertTrue(result.contains(expectedPattern));
    }

    static Stream<Arguments> emptyElementStyleProvider() {
        return Stream.of(
                Arguments.of(EmptyElementStyle.SELF_CLOSING, "/>"),
                Arguments.of(EmptyElementStyle.EXPANDED, "></empty>"),
                Arguments.of(EmptyElementStyle.SELF_CLOSING_SPACED, " />"));
    }

    @ParameterizedTest
    @MethodSource("prettyPrintSpecialNodeProvider")
    void testPrettyPrintSpecialNodes(String xml, String expectedContent) {
        Document doc = Document.of(xml);
        Serializer s = new Serializer();
        s.setPrettyPrint(true);
        String result = s.serialize(doc);
        assertTrue(result.contains(expectedContent));
    }

    static Stream<Arguments> prettyPrintSpecialNodeProvider() {
        return Stream.of(
                Arguments.of("<root><!-- comment --><child/></root>", "<!-- comment -->"),
                Arguments.of("<root><?target data?><child/></root>", "<?target data?>"),
                Arguments.of("<root><?target?><child/></root>", "<?target?>"));
    }

    @Test
    void testRawModeNoLineBreaks() {
        Serializer s = new Serializer(DomTripConfig.raw());
        Document doc = Document.of("<root><child>text</child></root>");
        doc.root().markModified();
        String result = s.serialize(doc);
        assertFalse(result.contains("\n"));
    }

    @Test
    void testPrettyPrintOmitXmlDeclaration() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root><child/></root>";
        Document doc = Document.of(xml);
        DomTripConfig config = DomTripConfig.prettyPrint().withXmlDeclaration(false);
        Serializer s = new Serializer(config);
        String result = s.serialize(doc);
        assertFalse(result.contains("<?xml"));
    }

    // ===== Node Serialization =====

    @Test
    void testSerializeNode() {
        String xml = "<root><child>text</child></root>";
        Editor editor = new Editor(Document.of(xml));
        Element child = editor.root().childElement("child").orElseThrow();
        Serializer s = new Serializer();
        String result = s.serialize(child);
        assertTrue(result.contains("<child>text</child>"));
    }

    @Test
    void testSerializeNodeUnmodified() {
        String xml = "<root><child>text</child></root>";
        Editor editor = new Editor(Document.of(xml));
        Element child = editor.root().childElement("child").orElseThrow();
        Serializer s = new Serializer();
        String result = s.serialize(child);
        assertTrue(result.contains("text"));
    }

    @Test
    void testSerializeNodePrettyPrint() {
        String xml = "<root><parent><child>text</child></parent></root>";
        Editor editor = new Editor(Document.of(xml));
        Element parent = editor.root().childElement("parent").orElseThrow();
        parent.markModified();
        Serializer s = new Serializer();
        s.setPrettyPrint(true);
        String result = s.serialize(parent);
        assertNotNull(result);
    }

    // ===== OutputStream Tests =====

    @Test
    void testSerializeDocumentToOutputStream() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root><child>text</child></root>";
        Document doc = Document.of(xml);
        Serializer s = new Serializer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        s.serialize(doc, baos);
        String result = baos.toString(StandardCharsets.UTF_8);
        assertTrue(result.contains("<root>"));
    }

    @Test
    void testSerializeDocumentToOutputStreamNullDoc() {
        Serializer s = new Serializer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> s.serialize((Document) null, baos));
        assertEquals(0, baos.size());
    }

    @Test
    void testSerializeDocumentToOutputStreamWithCharset() {
        String xml = "<?xml version=\"1.0\"?>\n<root>text</root>";
        Document doc = Document.of(xml);
        Serializer s = new Serializer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        s.serialize(doc, baos, StandardCharsets.UTF_8);
        assertTrue(baos.size() > 0);
    }

    @Test
    void testSerializeDocumentToOutputStreamWithNullCharset() {
        String xml = "<?xml version=\"1.0\"?>\n<root>text</root>";
        Document doc = Document.of(xml);
        Serializer s = new Serializer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        s.serialize(doc, baos, (Charset) null);
        assertTrue(baos.size() > 0); // defaults to UTF-8
    }

    @Test
    void testSerializeDocumentToOutputStreamNullStream() {
        Document doc = Document.of("<root/>");
        Serializer s = new Serializer();
        assertDoesNotThrow(() -> s.serialize(doc, (OutputStream) null, StandardCharsets.UTF_8));
    }

    @Test
    void testSerializeDocumentToOutputStreamWithEncoding() {
        String xml = "<?xml version=\"1.0\"?>\n<root>text</root>";
        Document doc = Document.of(xml);
        Serializer s = new Serializer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        s.serialize(doc, baos, "UTF-8");
        assertTrue(baos.size() > 0);
    }

    @Test
    void testSerializeDocumentToOutputStreamWithNullEncoding() {
        String xml = "<?xml version=\"1.0\"?>\n<root>text</root>";
        Document doc = Document.of(xml);
        Serializer s = new Serializer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        s.serialize(doc, baos, (String) null);
        assertTrue(baos.size() > 0); // defaults to UTF-8
    }

    @Test
    void testSerializeDocumentToOutputStreamWithEmptyEncoding() {
        String xml = "<?xml version=\"1.0\"?>\n<root>text</root>";
        Document doc = Document.of(xml);
        Serializer s = new Serializer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        s.serialize(doc, baos, "");
        assertTrue(baos.size() > 0); // defaults to UTF-8
    }

    @Test
    void testSerializeDocumentToOutputStreamInvalidEncoding() {
        String xml = "<?xml version=\"1.0\"?>\n<root>text</root>";
        Document doc = Document.of(xml);
        Serializer s = new Serializer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertThrows(DomTripException.class, () -> s.serialize(doc, baos, "INVALID_ENCODING_XYZ"));
    }

    @Test
    void testSerializeNodeToOutputStream() {
        String xml = "<root><child>text</child></root>";
        Editor editor = new Editor(Document.of(xml));
        Element child = editor.root().childElement("child").orElseThrow();
        Serializer s = new Serializer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        s.serialize(child, baos);
        String result = baos.toString(StandardCharsets.UTF_8);
        assertTrue(result.contains("text"));
    }

    @Test
    void testSerializeNodeToOutputStreamNullNode() {
        Serializer s = new Serializer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> s.serialize((Node) null, baos));
        assertEquals(0, baos.size());
    }

    @Test
    void testSerializeNodeToOutputStreamNullStream() {
        Element el = Element.of("test");
        Serializer s = new Serializer();
        assertDoesNotThrow(() -> s.serialize(el, (OutputStream) null, StandardCharsets.UTF_8));
    }

    @Test
    void testSerializeNodeToOutputStreamWithCharset() {
        Element el = Element.of("test");
        Serializer s = new Serializer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        s.serialize(el, baos, StandardCharsets.UTF_8);
        assertTrue(baos.size() > 0);
    }

    @Test
    void testSerializeNodeToOutputStreamWithNullCharset() {
        Element el = Element.of("test");
        Serializer s = new Serializer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        s.serialize(el, baos, (Charset) null);
        assertTrue(baos.size() > 0); // defaults to UTF-8
    }

    @Test
    void testSerializeNodeToOutputStreamWithEncoding() {
        Element el = Element.of("test");
        Serializer s = new Serializer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        s.serialize(el, baos, "UTF-8");
        assertTrue(baos.size() > 0);
    }

    @Test
    void testSerializeNodeToOutputStreamWithNullEncoding() {
        Element el = Element.of("test");
        Serializer s = new Serializer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        s.serialize(el, baos, (String) null);
        assertTrue(baos.size() > 0);
    }

    @Test
    void testSerializeNodeToOutputStreamWithEmptyEncoding() {
        Element el = Element.of("test");
        Serializer s = new Serializer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        s.serialize(el, baos, "");
        assertTrue(baos.size() > 0);
    }

    @Test
    void testSerializeNodeToOutputStreamInvalidEncoding() {
        Element el = Element.of("test");
        Serializer s = new Serializer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertThrows(DomTripException.class, () -> s.serialize(el, baos, "INVALID_ENCODING_XYZ"));
    }

    @Test
    void testSerializeToOutputStreamIOException() {
        Document doc = Document.of("<root/>");
        Serializer s = new Serializer();
        OutputStream failingStream = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw new IOException("Simulated failure");
            }
        };
        assertThrows(DomTripException.class, () -> s.serialize(doc, failingStream, StandardCharsets.UTF_8));
    }

    @Test
    void testSerializeNodeToOutputStreamIOException() {
        Element el = Element.of("test");
        Serializer s = new Serializer();
        OutputStream failingStream = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw new IOException("Simulated failure");
            }
        };
        assertThrows(DomTripException.class, () -> s.serialize(el, failingStream, StandardCharsets.UTF_8));
    }

    // ===== CDATA and Text Escaping =====

    @Test
    void testSerializeCdataInPrettyPrint() {
        String xml = "<root><child><![CDATA[<data>]]></child></root>";
        Document doc = Document.of(xml);
        doc.root().markModified();
        Serializer s = new Serializer();
        s.setPrettyPrint(true);
        String result = s.serialize(doc);
        assertTrue(result.contains("<![CDATA[<data>]]>"));
    }

    @Test
    void testSerializeTextEscaping() {
        String xml = "<root><child>text with &amp; and &lt;tag&gt;</child></root>";
        Document doc = Document.of(xml);
        doc.root().markModified();
        Serializer s = new Serializer();
        String result = s.serialize(doc);
        assertTrue(result.contains("&amp;"));
        assertTrue(result.contains("&lt;"));
    }

    // ===== BOM Tests =====

    @Test
    void testSerializeWithBom() throws Exception {
        // Parse XML with BOM
        byte[] bomUtf8 = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] xmlBytes = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root/>".getBytes(StandardCharsets.UTF_8);
        byte[] withBom = new byte[bomUtf8.length + xmlBytes.length];
        System.arraycopy(bomUtf8, 0, withBom, 0, bomUtf8.length);
        System.arraycopy(xmlBytes, 0, withBom, bomUtf8.length, xmlBytes.length);

        Document doc = Document.of(new java.io.ByteArrayInputStream(withBom));
        Serializer s = new Serializer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        s.serialize(doc, baos);

        byte[] output = baos.toByteArray();
        assertTrue(doc.hasBom(), "Document should have detected BOM from input");
        assertTrue(output.length > 3);
        assertEquals((byte) 0xEF, output[0]);
        assertEquals((byte) 0xBB, output[1]);
        assertEquals((byte) 0xBF, output[2]);
    }

    // ===== Document Encoding =====

    @Test
    void testSerializeDocumentToOutputStreamUsesDocumentEncoding() {
        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<root>caf\u00e9</root>";
        Document doc = Document.of(xml);
        Serializer s = new Serializer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        s.serialize(doc, baos);
        byte[] out = baos.toByteArray();
        assertTrue(new String(out, StandardCharsets.ISO_8859_1).contains("caf\u00e9"));
    }

    @Test
    void testSerializeDocumentEmptyEncoding() {
        String xml = "<?xml version=\"1.0\"?>\n<root>caf\u00e9</root>";
        Document doc = Document.of(xml);
        Serializer s = new Serializer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        s.serialize(doc, baos);
        byte[] out = baos.toByteArray();
        assertTrue(new String(out, StandardCharsets.UTF_8).contains("caf\u00e9"));
    }
}
