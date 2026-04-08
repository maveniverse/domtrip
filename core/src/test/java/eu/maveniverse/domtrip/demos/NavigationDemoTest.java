package eu.maveniverse.domtrip.demos;

import static org.junit.jupiter.api.Assertions.*;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * Demonstrates the enhanced navigation features.
 */
class NavigationDemoTest {

    @Test
    void demonstrateNavigation() throws DomTripException {
        // Create a complex XML structure for navigation
        String complexXml = createComplexXml();
        Editor editor = new Editor(Document.of(complexXml));

        verifyBasicNavigation(editor);
        verifyStreamNavigation(editor);
        verifyRelationshipMethods(editor);
        verifyAdvancedQueries(editor);
    }

    private static String createComplexXml() {
        return """
            <library>
                <metadata>
                    <name>City Library</name>
                    <location>Downtown</location>
                    <established>1925</established>
                </metadata>
                <sections>
                    <section id="fiction" floor="1">
                        <name>Fiction</name>
                        <books>
                            <book id="book1" available="true">
                                <title>The Great Gatsby</title>
                                <author>F. Scott Fitzgerald</author>
                                <genre>Classic</genre>
                                <year>1925</year>
                            </book>
                            <book id="book2" available="false">
                                <title>To Kill a Mockingbird</title>
                                <author>Harper Lee</author>
                                <genre>Classic</genre>
                                <year>1960</year>
                            </book>
                        </books>
                    </section>
                    <section id="science" floor="2">
                        <name>Science</name>
                        <books>
                            <book id="book3" available="true">
                                <title>A Brief History of Time</title>
                                <author>Stephen Hawking</author>
                                <genre>Physics</genre>
                                <year>1988</year>
                            </book>
                            <book id="book4" available="true">
                                <title>The Selfish Gene</title>
                                <author>Richard Dawkins</author>
                                <genre>Biology</genre>
                                <year>1976</year>
                            </book>
                        </books>
                    </section>
                </sections>
                <staff>
                    <librarian id="lib1">
                        <name>Alice Johnson</name>
                        <department>Reference</department>
                    </librarian>
                    <librarian id="lib2">
                        <name>Bob Smith</name>
                        <department>Children</department>
                    </librarian>
                </staff>
            </library>
            """;
    }

    private static void verifyBasicNavigation(Editor editor) throws DomTripException {
        Element root = editor.root();

        // Find direct children
        assertTrue(root.childElement("metadata").isPresent());
        root.childElement("metadata").ifPresent(metadata -> {
            assertEquals(
                    "City Library",
                    metadata.childElement("name").map(Element::textContent).orElse(null));
            assertEquals(
                    "1925",
                    metadata.childElement("established")
                            .map(Element::textContent)
                            .orElse(null));
        });

        // Find all sections
        root.childElement("sections").ifPresent(sections -> {
            List<Element> sectionList = sections.childElements("section").collect(Collectors.toList());
            assertEquals(2, sectionList.size());

            assertEquals("fiction", sectionList.get(0).attribute("id"));
            assertEquals("1", sectionList.get(0).attribute("floor"));
        });

        // Find deeply nested elements
        assertTrue(root.descendant("title").isPresent());
        assertEquals(
                "The Great Gatsby",
                root.descendant("title").map(Element::textContent).orElse(null));
    }

    private static void verifyStreamNavigation(Editor editor) throws DomTripException {
        Element root = editor.root();

        // Find all books using streams
        List<Element> allBooks =
                root.descendants().filter(el -> "book".equals(el.name())).toList();
        assertEquals(4, allBooks.size());

        // Find available books
        List<Element> availableBooks = allBooks.stream()
                .filter(book -> "true".equals(book.attribute("available")))
                .toList();
        assertEquals(3, availableBooks.size());

        // Find all authors
        List<String> authors = root.descendants()
                .filter(el -> "author".equals(el.name()))
                .map(Element::textContent)
                .distinct()
                .sorted()
                .toList();
        assertEquals(4, authors.size());

        // Find books by genre
        long classicCount = root.descendants()
                .filter(el -> "book".equals(el.name()))
                .filter(book -> book.childElement("genre")
                        .map(genre -> "Classic".equals(genre.textContent()))
                        .orElse(false))
                .count();
        assertEquals(2, classicCount);
    }

    private static void verifyRelationshipMethods(Editor editor) throws DomTripException {
        Element root = editor.root();

        // Find a deeply nested element and explore relationships
        root.descendant("title").ifPresent(title -> {
            assertTrue(title.depth() > 0);
            Document doc = title.document();
            assertNotNull(doc);

            // Check parent relationships
            Element parent = (Element) title.parent();
            assertNotNull(parent);
            assertEquals("book", parent.name());
            assertTrue(title.isDescendantOf(root));
            assertTrue(title.isDescendantOf(parent));

            // Check if parent has multiple children
            assertTrue(parent.hasChildElements());
            assertTrue(parent.childElements().count() > 1);
        });

        // Demonstrate tree traversal
        assertTrue(root.childElement("sections")
                .flatMap(sections -> sections.childElement("section"))
                .isPresent());
    }

    private static void verifyAdvancedQueries(Editor editor) throws DomTripException {
        Element root = editor.root();

        // Complex query: Find books published after 1950 that are available
        long postFiftiesAvailable = root.descendants()
                .filter(el -> "book".equals(el.name()))
                .filter(book -> "true".equals(book.attribute("available")))
                .filter(book -> {
                    return book.childElement("year")
                            .map(Element::textContent)
                            .map(year -> {
                                try {
                                    return Integer.parseInt(year) > 1950;
                                } catch (NumberFormatException e) {
                                    return false;
                                }
                            })
                            .orElse(false);
                })
                .count();
        assertTrue(postFiftiesAvailable > 0);

        // Find sections with more than 1 book
        root.childElement("sections").ifPresent(sections -> {
            long sectionsWithMultipleBooks = sections.childElements("section")
                    .filter(section -> section.childElement("books")
                            .map(books -> books.childElements().count() > 1)
                            .orElse(false))
                    .count();
            assertEquals(2, sectionsWithMultipleBooks);
        });

        // Find all elements with specific attributes
        long elementsWithId =
                root.descendants().filter(el -> el.attribute("id") != null).count();
        assertTrue(elementsWithId > 0);

        // Chain multiple navigation operations - librarians in Reference department
        List<String> referenceLibrarians = root.childElement("staff")
                .map(staff -> staff.childElements("librarian")
                        .filter(lib -> lib.childElement("department")
                                .map(dept -> "Reference".equals(dept.textContent()))
                                .orElse(false))
                        .map(lib -> lib.childElement("name")
                                .map(Element::textContent)
                                .orElse("Unknown"))
                        .toList())
                .orElse(List.of());
        assertEquals(1, referenceLibrarians.size());
        assertEquals("Alice Johnson", referenceLibrarians.get(0));
    }
}
