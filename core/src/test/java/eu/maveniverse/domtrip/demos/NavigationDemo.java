package eu.maveniverse.domtrip.demos;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Demonstrates the enhanced navigation features.
 */
public class NavigationDemo {

    public static void main(String[] args) throws DomTripException {
        System.out.println("=== Enhanced Navigation Demo ===\n");

        // Create a complex XML structure for navigation
        String complexXml = createComplexXml();
        Editor editor = new Editor(Document.of(complexXml));

        demonstrateBasicNavigation(editor);
        demonstrateStreamNavigation(editor);
        demonstrateRelationshipMethods(editor);
        demonstrateAdvancedQueries(editor);

        System.out.println("\n=== Demo Complete ===");
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

    private static void demonstrateBasicNavigation(Editor editor) throws DomTripException {
        System.out.println("1. Basic Navigation Demo:");

        Element root = editor.root();

        // Find direct children
        root.childElement("metadata").ifPresent(metadata -> {
            System.out.println("Library name: "
                    + metadata.childElement("name")
                            .map(element1 -> element1.textContent())
                            .orElse("Unknown"));
            System.out.println("Established: "
                    + metadata.childElement("established")
                            .map(element -> element.textContent())
                            .orElse("Unknown"));
        });

        // Find all sections
        root.childElement("sections").ifPresent(sections -> {
            List<Element> sectionList = sections.childElements("section").collect(Collectors.toList());
            System.out.println("Number of sections: " + sectionList.size());

            sectionList.forEach(section -> {
                String id = section.attribute("id");
                String floor = section.attribute("floor");
                String name = section.childElement("name")
                        .map(element -> element.textContent())
                        .orElse("Unknown");
                System.out.println("  Section: " + name + " (ID: " + id + ", Floor: " + floor + ")");
            });
        });

        // Find deeply nested elements
        root.descendant("title").ifPresent(title -> {
            System.out.println("First book title found: " + title.textContent());
        });

        System.out.println();
    }

    private static void demonstrateStreamNavigation(Editor editor) throws DomTripException {
        System.out.println("2. Stream-Based Navigation Demo:");

        Element root = editor.root();

        // Find all books using streams
        List<Element> allBooks =
                root.descendants().filter(el -> "book".equals(el.name())).toList();

        System.out.println("Total books in library: " + allBooks.size());

        // Find available books
        List<Element> availableBooks = allBooks.stream()
                .filter(book -> "true".equals(book.attribute("available")))
                .toList();

        System.out.println("Available books:");
        availableBooks.forEach(book -> {
            String title = book.childElement("title")
                    .map(element1 -> element1.textContent())
                    .orElse("Unknown");
            String author = book.childElement("author")
                    .map(element -> element.textContent())
                    .orElse("Unknown");
            System.out.println("  - " + title + " by " + author);
        });

        // Find all authors
        List<String> authors = root.descendants()
                .filter(el -> "author".equals(el.name()))
                .map(element1 -> element1.textContent())
                .distinct()
                .sorted()
                .toList();

        System.out.println("All authors: " + String.join(", ", authors));

        // Find books by genre
        System.out.println("Classic books:");
        root.descendants()
                .filter(el -> "book".equals(el.name()))
                .filter(book -> book.childElement("genre")
                        .map(genre -> "Classic".equals(genre.textContent()))
                        .orElse(false))
                .forEach(book -> {
                    String title = book.childElement("title")
                            .map(element -> element.textContent())
                            .orElse("Unknown");
                    System.out.println("  - " + title);
                });

        System.out.println();
    }

    private static void demonstrateRelationshipMethods(Editor editor) throws DomTripException {
        System.out.println("3. Relationship Methods Demo:");

        Element root = editor.root();

        // Find a deeply nested element and explore relationships
        root.descendant("title").ifPresent(title -> {
            System.out.println("Analyzing element: " + title.name() + " = '" + title.textContent() + "'");
            System.out.println("  Depth in tree: " + title.depth());
            Document doc = title.document();
            System.out.println("  Document: " + (doc != null ? "Found" : "Not in document"));

            // Check parent relationships
            Element parent = (Element) title.parent();
            if (parent != null) {
                System.out.println("  Parent: " + parent.name());
                System.out.println("  Is descendant of root: " + title.isDescendantOf(root));
                System.out.println("  Is descendant of parent: " + title.isDescendantOf(parent));
            }

            // Check if parent has multiple children
            if (parent != null) {
                System.out.println("  Parent has child elements: " + parent.hasChildElements());
                System.out.println("  Parent has text content: " + parent.hasTextContent());
                System.out.println("  Number of sibling elements: "
                        + (parent.childElements().count() - 1)); // Subtract self
            }
        });

        // Demonstrate tree traversal
        System.out.println("\nTree structure (first section):");
        root.childElement("sections")
                .flatMap(sections -> sections.childElement("section"))
                .ifPresent(section -> printTreeStructure(section, 0));

        System.out.println();
    }

    private static void demonstrateAdvancedQueries(Editor editor) throws DomTripException {
        System.out.println("4. Advanced Query Demo:");

        Element root = editor.root();

        // Complex query: Find books published after 1950 that are available
        System.out.println("Books published after 1950 that are available:");
        root.descendants()
                .filter(el -> "book".equals(el.name()))
                .filter(book -> "true".equals(book.attribute("available")))
                .filter(book -> {
                    return book.childElement("year")
                            .map(element -> element.textContent())
                            .map(year -> {
                                try {
                                    return Integer.parseInt(year) > 1950;
                                } catch (NumberFormatException e) {
                                    return false;
                                }
                            })
                            .orElse(false);
                })
                .forEach(book -> {
                    String title = book.childElement("title")
                            .map(element2 -> element2.textContent())
                            .orElse("Unknown");
                    String year = book.childElement("year")
                            .map(element1 -> element1.textContent())
                            .orElse("Unknown");
                    String author = book.childElement("author")
                            .map(element -> element.textContent())
                            .orElse("Unknown");
                    System.out.println("  - " + title + " (" + year + ") by " + author);
                });

        // Find sections with more than 1 book
        System.out.println("\nSections with multiple books:");
        root.childElement("sections").ifPresent(sections -> {
            sections.childElements("section")
                    .filter(section -> {
                        return section.childElement("books")
                                .map(books -> books.childElements().count() > 1)
                                .orElse(false);
                    })
                    .forEach(section -> {
                        String name = section.childElement("name")
                                .map(element -> element.textContent())
                                .orElse("Unknown");
                        long bookCount = section.childElement("books")
                                .map(books -> books.childElements().count())
                                .orElse(0L);
                        System.out.println("  - " + name + " (" + bookCount + " books)");
                    });
        });

        // Find all elements with specific attributes
        System.out.println("\nElements with 'id' attribute:");
        root.descendants().filter(el -> el.attribute("id") != null).forEach(el -> {
            System.out.println("  - " + el.name() + " (id=" + el.attribute("id") + ")");
        });

        // Chain multiple navigation operations
        System.out.println("\nLibrarians in Reference department:");
        root.childElement("staff")
                .map(staff -> staff.childElements("librarian")
                        .filter(lib -> lib.childElement("department")
                                .map(dept -> "Reference".equals(dept.textContent()))
                                .orElse(false))
                        .map(lib -> lib.childElement("name")
                                .map(element -> element.textContent())
                                .orElse("Unknown"))
                        .toList())
                .orElse(List.of())
                .forEach(name -> System.out.println("  - " + name));

        System.out.println();
    }

    private static void printTreeStructure(Element element, int depth) {
        String indent = "  ".repeat(depth);
        String attrs = element.attributes().isEmpty()
                ? ""
                : " ["
                        + element.attributes().entrySet().stream()
                                .map(e -> e.getKey() + "=" + e.getValue())
                                .collect(Collectors.joining(", "))
                        + "]";

        System.out.println(indent + element.name() + attrs);

        if (element.hasTextContent() && !element.hasChildElements()) {
            System.out.println(indent + "  \"" + element.textContent().trim() + "\"");
        }

        element.childElements().forEach(child -> printTreeStructure(child, depth + 1));
    }
}
