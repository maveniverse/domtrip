package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests for XML specification normalization requirements that DomTrip now implements
 * while preserving round-tripping.
 *
 * <ul>
 *   <li>§2.11 — Line ending normalization: {@code \r\n} → {@code \n}, standalone {@code \r} → {@code \n}</li>
 *   <li>§3.3.3 — Attribute value normalization: {@code \t}, {@code \n}, {@code \r} → space</li>
 * </ul>
 *
 * <p>The key invariant is: API-reported values are spec-conformant while serialization
 * preserves the original form for perfect round-tripping.</p>
 *
 * @see <a href="https://www.w3.org/TR/2008/REC-xml-20081126/#sec-line-ends">XML 1.0 §2.11</a>
 * @see <a href="https://www.w3.org/TR/2008/REC-xml-20081126/#AVNormalize">XML 1.0 §3.3.3</a>
 */
class XmlSpecNormalizationTest {

    // ========== §2.11 Line Ending Normalization ==========

    @Nested
    class LineEndingNormalization {

        @Test
        void crlfInTextContentNormalizedToLf() throws DomTripException {
            // Construct XML with \r\n line endings in text content
            String xml = "<root>line1\r\nline2</root>";
            Document doc = Document.of(xml);
            Element root = doc.root();

            assertEquals("line1\nline2", root.textContent(), "\\r\\n should be normalized to \\n in textContent()");
            assertEquals(xml, doc.toXml(), "Serialization should preserve original \\r\\n");
        }

        @Test
        void standaloneCrInTextContentNormalizedToLf() throws DomTripException {
            String xml = "<root>line1\rline2</root>";
            Document doc = Document.of(xml);
            Element root = doc.root();

            assertEquals("line1\nline2", root.textContent(), "Standalone \\r should be normalized to \\n");
            assertEquals(xml, doc.toXml(), "Serialization should preserve original \\r");
        }

        @Test
        void lfInTextContentUnchanged() throws DomTripException {
            String xml = "<root>line1\nline2</root>";
            Document doc = Document.of(xml);
            Element root = doc.root();

            assertEquals("line1\nline2", root.textContent(), "\\n should remain unchanged");
            assertEquals(xml, doc.toXml(), "Serialization should preserve \\n");
        }

        @Test
        void mixedLineEndingsInTextContent() throws DomTripException {
            // Mix of \r\n, standalone \r, and \n
            String xml = "<root>a\r\nb\rc\nd</root>";
            Document doc = Document.of(xml);
            Element root = doc.root();

            assertEquals("a\nb\nc\nd", root.textContent(), "All line endings should be normalized to \\n");
            assertEquals(xml, doc.toXml(), "Serialization should preserve original line endings");
        }

        @Test
        void lineEndingsInCdataSection() throws DomTripException {
            String xml = "<root><![CDATA[line1\r\nline2\rline3]]></root>";
            Document doc = Document.of(xml);
            Element root = doc.root();

            Text cdata = root.children()
                    .filter(Text.class::isInstance)
                    .map(Text.class::cast)
                    .findFirst()
                    .orElseThrow();

            assertEquals("line1\nline2\nline3", cdata.content(), "CDATA content should have line endings normalized");
            assertEquals(xml, doc.toXml(), "Serialization should preserve original CDATA content");
        }

        @Test
        void lineEndingsInComments() throws DomTripException {
            String xml = "<root><!-- comment\r\nwith\rline endings --></root>";
            Document doc = Document.of(xml);
            Element root = doc.root();

            Comment comment = root.children()
                    .filter(Comment.class::isInstance)
                    .map(Comment.class::cast)
                    .findFirst()
                    .orElseThrow();

            assertEquals(
                    " comment\nwith\nline endings ",
                    comment.content(),
                    "Comment content should have line endings normalized");
            assertEquals(xml, doc.toXml(), "Serialization should preserve original comment content");
        }

        @Test
        void lineEndingsInProcessingInstructionData() throws DomTripException {
            ProcessingInstruction pi = new ProcessingInstruction("target", "data\r\nwith\rline endings");

            assertEquals(
                    "data\nwith\nline endings", pi.data(), "PI data should have line endings normalized in getter");
        }

        @Test
        void textContentWithNoLineEndingsUnchanged() throws DomTripException {
            String xml = "<root>simple text content</root>";
            Document doc = Document.of(xml);

            assertEquals("simple text content", doc.root().textContent(), "Content without line endings is unchanged");
        }

        @Test
        void emptyTextContentUnchanged() throws DomTripException {
            String xml = "<root></root>";
            Document doc = Document.of(xml);

            assertEquals("", doc.root().textContent(), "Empty content should remain empty");
        }

        @Test
        void textNodeContentDirectAccess() {
            Text text = new Text("line1\r\nline2\rline3");
            assertEquals("line1\nline2\nline3", text.content(), "Text.content() should normalize line endings");
        }

        @Test
        void normalizeLineEndingsUtility() {
            assertNull(Text.normalizeLineEndings(null), "null input returns null");
            assertEquals("", Text.normalizeLineEndings(""), "empty input returns empty");
            assertEquals("no cr", Text.normalizeLineEndings("no cr"), "no-op when no \\r present");
            assertEquals("a\nb", Text.normalizeLineEndings("a\r\nb"), "\\r\\n → \\n");
            assertEquals("a\nb", Text.normalizeLineEndings("a\rb"), "standalone \\r → \\n");
            assertEquals("a\nb\nc\nd", Text.normalizeLineEndings("a\r\nb\rc\nd"), "mixed line endings");
            assertEquals("\n\n", Text.normalizeLineEndings("\r\n\r"), "consecutive \\r\\n then \\r");
            assertEquals("\n", Text.normalizeLineEndings("\r\n"), "lone \\r\\n");
            assertEquals("\n", Text.normalizeLineEndings("\r"), "lone \\r");
        }
    }

    // ========== §3.3.3 Attribute Value Normalization ==========

    @Nested
    class AttributeWhitespaceNormalization {

        @Test
        void newlineCharRefInAttributeNormalizedToSpace() throws DomTripException {
            String xml = "<root attr=\"line1&#10;line2\"/>";
            Document doc = Document.of(xml);

            assertEquals(
                    "line1 line2",
                    doc.root().attribute("attr"),
                    "Newline in attribute value should be normalized to space");
            assertEquals(xml, doc.toXml(), "Serialization should preserve &#10;");
        }

        @Test
        void tabCharRefInAttributeNormalizedToSpace() throws DomTripException {
            String xml = "<root attr=\"tab&#9;here\"/>";
            Document doc = Document.of(xml);

            assertEquals(
                    "tab here", doc.root().attribute("attr"), "Tab in attribute value should be normalized to space");
            assertEquals(xml, doc.toXml(), "Serialization should preserve &#9;");
        }

        @Test
        void crCharRefInAttributeNormalizedToSpace() throws DomTripException {
            String xml = "<root attr=\"cr&#13;here\"/>";
            Document doc = Document.of(xml);

            assertEquals(
                    "cr here", doc.root().attribute("attr"), "CR in attribute value should be normalized to space");
            assertEquals(xml, doc.toXml(), "Serialization should preserve &#13;");
        }

        @Test
        void multipleConsecutiveWhitespaceNotCollapsed() throws DomTripException {
            // Per §3.3.3: each whitespace char is replaced individually, not collapsed
            String xml = "<root attr=\"a&#10;&#10;b\"/>";
            Document doc = Document.of(xml);

            assertEquals(
                    "a  b",
                    doc.root().attribute("attr"),
                    "Each whitespace char should be replaced individually, not collapsed");
            assertEquals(xml, doc.toXml(), "Serialization should preserve original");
        }

        @Test
        void attributeWithLiteralTabNormalized() {
            Attribute attr = new Attribute("name", "value\twith\ttabs");
            assertEquals("value with tabs", attr.value(), "Literal tabs should be normalized to spaces");
        }

        @Test
        void attributeWithLiteralNewlineNormalized() {
            Attribute attr = new Attribute("name", "value\nwith\nnewlines");
            assertEquals("value with newlines", attr.value(), "Literal newlines should be normalized to spaces");
        }

        @Test
        void attributeWithLiteralCrNormalized() {
            Attribute attr = new Attribute("name", "value\rwith\rcr");
            assertEquals("value with cr", attr.value(), "Literal CRs should be normalized to spaces");
        }

        @Test
        void attributeWithMixedWhitespaceNormalized() {
            Attribute attr = new Attribute("name", "a\tb\nc\rd");
            assertEquals("a b c d", attr.value(), "All whitespace types should be normalized to space");
        }

        @Test
        void attributeWithNoSpecialWhitespaceUnchanged() {
            Attribute attr = new Attribute("name", "normal value with spaces");
            assertEquals("normal value with spaces", attr.value(), "Regular spaces should not be affected");
        }

        @Test
        void emptyAttributeValueUnchanged() {
            Attribute attr = new Attribute("name", "");
            assertEquals("", attr.value(), "Empty attribute value should remain empty");
        }

        @Test
        void elementAttributeMethodReturnsNormalized() throws DomTripException {
            String xml = "<root attr=\"line1&#10;line2\" tab=\"a&#9;b\"/>";
            Document doc = Document.of(xml);
            Element root = doc.root();

            assertEquals("line1 line2", root.attribute("attr"), "Element.attribute() should return normalized value");
            assertEquals("a b", root.attribute("tab"), "Element.attribute() should normalize tabs too");
        }

        @Test
        void elementAttributesMapReturnsNormalized() throws DomTripException {
            String xml = "<root attr=\"line1&#10;line2\"/>";
            Document doc = Document.of(xml);

            assertEquals(
                    "line1 line2",
                    doc.root().attributes().get("attr"),
                    "Element.attributes() map should contain normalized values");
        }

        @Test
        void normalizeAttributeWhitespaceUtility() {
            assertNull(Attribute.normalizeAttributeWhitespace(null), "null input returns null");
            assertEquals("", Attribute.normalizeAttributeWhitespace(""), "empty input returns empty");
            assertEquals("no ws", Attribute.normalizeAttributeWhitespace("no ws"), "no-op when no special whitespace");
            assertEquals("a b", Attribute.normalizeAttributeWhitespace("a\tb"), "tab → space");
            assertEquals("a b", Attribute.normalizeAttributeWhitespace("a\nb"), "newline → space");
            assertEquals("a b", Attribute.normalizeAttributeWhitespace("a\rb"), "CR → space");
            assertEquals("a b c d", Attribute.normalizeAttributeWhitespace("a\tb\nc\rd"), "mixed whitespace");
        }
    }

    // ========== Round-Trip Preservation ==========

    @Nested
    class RoundTripPreservation {

        static Stream<String> roundTripXmlSamples() {
            return Stream.of(
                    "<root>line1\r\nline2</root>",
                    "<root>line1\rline2</root>",
                    "<root attr=\"line1&#10;line2\" tab=\"a&#9;b\" cr=\"x&#13;y\"/>",
                    "<root><![CDATA[line1\r\nline2]]></root>",
                    "<root><!-- comment\r\nwith cr-lf --></root>");
        }

        @ParameterizedTest(name = "round-trip preserves input [{index}]")
        @MethodSource("roundTripXmlSamples")
        void roundTripPreservesInput(String xml) throws DomTripException {
            assertEquals(xml, Document.of(xml).toXml());
        }

        @Test
        void complexDocumentWithMixedLineEndingsRoundTrips() throws DomTripException {
            String xml = "<root attr=\"value&#10;here\">\r\n"
                    + "  <child>text\r\ncontent</child>\r\n"
                    + "  <!-- comment\r\nhere -->\r\n"
                    + "  <![CDATA[cdata\r\ncontent]]>\r\n"
                    + "</root>";
            Document doc = Document.of(xml);
            assertEquals(xml, doc.toXml(), "Complex document with mixed line endings should round-trip exactly");
        }

        @Test
        void existingTestsNotBroken() throws DomTripException {
            // Verify that standard LF-only content is unaffected
            String xml = "<root>\n  <child>content</child>\n</root>";
            Document doc = Document.of(xml);
            assertEquals(xml, doc.toXml(), "Standard LF content should still round-trip");
            assertEquals(
                    "content", doc.root().childElement("child").orElseThrow().textContent());
        }
    }

    // ========== Serializer Path Round-Trip Preservation ==========

    @Nested
    class SerializerRoundTripPreservation {

        static Stream<String> serializerRoundTripSamples() {
            return Stream.of(
                    "<root>line1\r\nline2</root>",
                    "<root><![CDATA[line1\r\nline2]]></root>",
                    "<root><!-- comment\r\nwith cr-lf --></root>",
                    "<root><?target data\r\nwith cr-lf?></root>",
                    "<root>&lt;&gt;&amp;</root>");
        }

        @ParameterizedTest(name = "serializer round-trip preserves input [{index}]")
        @MethodSource("serializerRoundTripSamples")
        void serializerRoundTripPreservesInput(String xml) throws DomTripException {
            Serializer serializer = new Serializer();
            assertEquals(xml, serializer.serialize(Document.of(xml)));
        }

        @Test
        void serializerComplexDocumentRoundTrips() throws DomTripException {
            String xml = "<root attr=\"value&#10;here\">\r\n"
                    + "  <child>text\r\ncontent</child>\r\n"
                    + "  <!-- comment\r\nhere -->\r\n"
                    + "  <![CDATA[cdata\r\ncontent]]>\r\n"
                    + "</root>";
            Document doc = Document.of(xml);
            Serializer serializer = new Serializer();
            assertEquals(xml, serializer.serialize(doc), "Serializer should round-trip complex document exactly");
        }
    }
}
