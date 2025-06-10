package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test cases for entity and quote preservation features.
 */
public class EntityPreservationTest {

    private Editor editor;

    @BeforeEach
    void setUp() {
        editor = new Editor(Document.of());
    }

    @Test
    void testEntityPreservation() {
        String xmlWithEntities = "<root>\n" + "  <content>Text with &lt;entities&gt; &amp; symbols</content>\n"
                + "  <mixed>More &lt;content&gt; with &quot;quotes&quot;</mixed>\n"
                + "</root>";

        Document doc = Document.of(xmlWithEntities);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        // Entities should be preserved exactly
        assertTrue(result.contains("&lt;entities&gt; &amp; symbols"));
        assertTrue(result.contains("&lt;content&gt; with &quot;quotes&quot;"));

        // Round-trip should be identical
        assertEquals(xmlWithEntities, result);
    }

    @Test
    void testAttributeQuotePreservation() {
        String xmlWithMixedQuotes = "<root attr1='single quotes' attr2=\"double quotes\">\n"
                + "  <element other=\"normal\"/>\n" + "</root>";

        Document doc = Document.of(xmlWithMixedQuotes);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        // Quote styles should be preserved
        assertTrue(result.contains("attr1='single quotes'"));
        assertTrue(result.contains("attr2=\"double quotes\""));
        assertTrue(result.contains("other=\"normal\""));

        // Round-trip should be identical
        assertEquals(xmlWithMixedQuotes, result);
    }

    @Test
    void testNewAttributeQuoteStyle() {
        String xml = "<root existing='value'/>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element root = editor.root().orElseThrow();

        // Add new attribute - should use default double quotes
        root.attribute("newAttr", "newValue");

        String result = editor.toXml();

        // Existing attribute should keep single quotes
        assertTrue(result.contains("existing='value'"));
        // New attribute should use double quotes (default)
        assertTrue(result.contains("newAttr=\"newValue\""));
    }

    @Test
    void testEntityInNewContent() {
        String xml = "<root/>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element root = editor.root().orElseThrow();

        // Add new element with entities
        editor.addElement(root, "content", "Text with <tags> & entities");

        String result = editor.toXml();

        // New content should be properly escaped
        assertTrue(result.contains("Text with &lt;tags&gt; &amp; entities"));
    }

    @Test
    void testCDataPreservation() {
        String xmlWithCData =
                "<root>\n" + "  <script><![CDATA[function() { return x < y && z > 0; }]]></script>\n" + "</root>";

        Document doc = Document.of(xmlWithCData);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        // CDATA should be preserved exactly
        assertTrue(result.contains("<![CDATA[function() { return x < y && z > 0; }]]>"));

        // Round-trip should be identical
        assertEquals(xmlWithCData, result);
    }

    @Test
    void testComplexEntityMix() {
        String complexXml = "<config version='1.0' encoding=\"UTF-8\">\n"
                + "  <database url='jdbc:mysql://localhost/db?user=admin&amp;password=secret'/>\n"
                + "  <message>Welcome &lt;user&gt;! Your balance is &gt; $100.</message>\n"
                + "  <template><![CDATA[<html><body>Hello &world;</body></html>]]></template>\n"
                + "</config>";

        Document doc = Document.of(complexXml);
        Editor editor = new Editor(doc);
        String result = editor.toXml();

        // All formatting should be preserved
        assertTrue(result.contains("version='1.0'"));
        assertTrue(result.contains("encoding=\"UTF-8\""));
        assertTrue(result.contains("user=admin&amp;password=secret"));
        assertTrue(result.contains("&lt;user&gt;"));
        assertTrue(result.contains("&gt; $100"));
        assertTrue(result.contains("<![CDATA[<html><body>Hello &world;</body></html>]]>"));

        // Round-trip should be identical
        assertEquals(complexXml, result);
    }

    @Test
    void testModificationPreservesUnchangedParts() {
        String xml = "<root attr1='keep' attr2=\"also keep\">\n" + "  <keep>Text with &lt;entities&gt;</keep>\n"
                + "  <modify>old content</modify>\n"
                + "</root>";

        Document doc = Document.of(xml);
        Editor editor = new Editor(doc);
        Element modify = (Element) editor.root().orElseThrow().getNode(3); // Find modify element
        modify.textContent("new content");

        String result = editor.toXml();

        // Unchanged parts should preserve their formatting
        assertTrue(result.contains("attr1='keep'"));
        assertTrue(result.contains("attr2=\"also keep\""));
        assertTrue(result.contains("Text with &lt;entities&gt;"));

        // Modified part should have new content
        assertTrue(result.contains("<modify>new content</modify>"));
    }
}
