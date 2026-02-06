package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for DOCTYPE declaration preservation during parsing and serialization.
 */
class DoctypePreservationTest {

    @Test
    void testSimpleDoctype() throws DomTripException {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE html>
            <html>
                <head><title>Test</title></head>
                <body>Content</body>
            </html>
            """;

        Parser parser = new Parser();
        Document doc = parser.parse(xml);

        // Verify DOCTYPE was captured
        assertEquals("<!DOCTYPE html>", doc.doctype());

        // Verify round-trip preservation
        String serialized = doc.toXml();
        assertTrue(serialized.contains("<!DOCTYPE html>"));
    }

    @Test
    void testDoctypeWithPublicId() throws DomTripException {
        String xml = """
            <?xml version="1.0"?>
            <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
            <html xmlns="http://www.w3.org/1999/xhtml">
                <head><title>XHTML Test</title></head>
                <body><p>Content</p></body>
            </html>
            """;

        Parser parser = new Parser();
        Document doc = parser.parse(xml);

        // Verify DOCTYPE was captured with PUBLIC ID
        String expectedDoctype =
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";
        assertEquals(expectedDoctype, doc.doctype());

        // Verify round-trip preservation
        String serialized = doc.toXml();
        assertTrue(serialized.contains(expectedDoctype));
    }

    @Test
    void testDoctypeWithSystemId() throws DomTripException {
        String xml = """
            <!DOCTYPE note SYSTEM "note.dtd">
            <note>
                <to>Tove</to>
                <from>Jani</from>
                <heading>Reminder</heading>
                <body>Don't forget me this weekend!</body>
            </note>
            """;

        Parser parser = new Parser();
        Document doc = parser.parse(xml);

        // Verify DOCTYPE was captured
        assertEquals("<!DOCTYPE note SYSTEM \"note.dtd\">", doc.doctype());

        // Verify round-trip preservation
        String serialized = doc.toXml();
        assertTrue(serialized.contains("<!DOCTYPE note SYSTEM \"note.dtd\">"));
    }

    @Test
    void testDoctypeWithInternalSubset() throws DomTripException {
        String xml = """
            <!DOCTYPE note [
                <!ELEMENT note (to,from,heading,body)>
                <!ELEMENT to (#PCDATA)>
                <!ELEMENT from (#PCDATA)>
                <!ELEMENT heading (#PCDATA)>
                <!ELEMENT body (#PCDATA)>
            ]>
            <note>
                <to>Tove</to>
                <from>Jani</from>
                <heading>Reminder</heading>
                <body>Don't forget me this weekend!</body>
            </note>
            """;

        Parser parser = new Parser();
        Document doc = parser.parse(xml);

        // Verify DOCTYPE with internal subset was captured
        String doctype = doc.doctype();
        assertTrue(doctype.startsWith("<!DOCTYPE note ["));
        assertTrue(doctype.contains("<!ELEMENT note (to,from,heading,body)>"));
        assertTrue(doctype.contains("<!ELEMENT to (#PCDATA)>"));
        assertTrue(doctype.endsWith("]>"));

        // Verify round-trip preservation
        String serialized = doc.toXml();
        assertTrue(serialized.contains("<!DOCTYPE note ["));
        assertTrue(serialized.contains("<!ELEMENT note (to,from,heading,body)>"));
    }

    @Test
    void testDoctypeWithMixedPublicAndInternal() throws DomTripException {
        String xml = """
            <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
                "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" [
                <!ENTITY nbsp "&#160;">
                <!ENTITY copy "&#169;">
            ]>
            <html>
                <head><title>Test</title></head>
                <body>Content</body>
            </html>
            """;

        Parser parser = new Parser();
        Document doc = parser.parse(xml);

        // Verify complex DOCTYPE was captured
        String doctype = doc.doctype();
        assertTrue(doctype.contains("PUBLIC"));
        assertTrue(doctype.contains("-//W3C//DTD XHTML 1.0 Transitional//EN"));
        assertTrue(doctype.contains("http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"));
        assertTrue(doctype.contains("<!ENTITY nbsp"));
        assertTrue(doctype.contains("<!ENTITY copy"));

        // Verify round-trip preservation
        String serialized = doc.toXml();
        assertTrue(serialized.contains("PUBLIC"));
        assertTrue(serialized.contains("<!ENTITY nbsp"));
    }

    @Test
    void testNoDoctypePreservation() throws DomTripException {
        String xml = """
            <?xml version="1.0"?>
            <root>
                <child>Content</child>
            </root>
            """;

        Parser parser = new Parser();
        Document doc = parser.parse(xml);

        // Verify no DOCTYPE
        assertTrue(doc.doctype().isEmpty());

        // Verify serialization doesn't add DOCTYPE
        String serialized = doc.toXml();
        assertFalse(serialized.contains("<!DOCTYPE"));
    }

    @Test
    void testLosslessRoundTrip() throws DomTripException {
        String originalXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE bookstore [
                <!ELEMENT bookstore (book+)>
                <!ELEMENT book (title, author, price)>
                <!ELEMENT title (#PCDATA)>
                <!ELEMENT author (#PCDATA)>
                <!ELEMENT price (#PCDATA)>
                <!ATTLIST book id ID #REQUIRED>
            ]>
            <bookstore>
                <book id="book1">
                    <title>XML Processing</title>
                    <author>John Doe</author>
                    <price>29.99</price>
                </book>
            </bookstore>
            """;

        Parser parser = new Parser();
        Document doc = parser.parse(originalXml);

        // Verify DOCTYPE preservation
        String doctype = doc.doctype();
        assertTrue(doctype.contains("<!DOCTYPE bookstore ["));
        assertTrue(doctype.contains("<!ELEMENT bookstore (book+)>"));
        assertTrue(doctype.contains("<!ATTLIST book id ID #REQUIRED>"));

        // Test that we can modify content without losing DOCTYPE
        Element root = doc.root();
        Element book = root.child("book").orElseThrow();
        Element price = book.child("price").orElseThrow();
        price.textContent("39.99");

        // Verify DOCTYPE is still preserved after modification
        String modifiedXml = doc.toXml();
        assertTrue(modifiedXml.contains("<!DOCTYPE bookstore ["));
        assertTrue(modifiedXml.contains("<!ELEMENT bookstore (book+)>"));
        assertTrue(modifiedXml.contains("<price>39.99</price>"));
    }
}
