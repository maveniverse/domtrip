package eu.maveniverse.domtrip.demos;

import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Demonstrates the enhanced navigation features.
 */
public class NavigationDemo {

    public static void main(String[] args) {
        System.out.println("=== Enhanced Navigation Demo ===\n");

        // Create a complex XML structure for navigation
        String complexXml = createComplexXml();
        Editor editor = new Editor(complexXml);

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

    private static void demonstrateBasicNavigation(Editor editor) {
        System.out.println("1. Basic Navigation Demo:");

        Element root = editor.getRootElement();

        // Find direct children
        root.findChild("metadata").ifPresent(metadata -> {
            System.out.println("Library name: "
                    + metadata.findChild("name").map(Element::getTextContent).orElse("Unknown"));
            System.out.println("Established: "
                    + metadata.findChild("established")
                            .map(Element::getTextContent)
                            .orElse("Unknown"));
        });

        // Find all sections
        root.findChild("sections").ifPresent(sections -> {
            List<Element> sectionList = sections.findChildren("section").collect(Collectors.toList());
            System.out.println("Number of sections: " + sectionList.size());

            sectionList.forEach(section -> {
                String id = section.getAttribute("id");
                String floor = section.getAttribute("floor");
                String name =
                        section.findChild("name").map(Element::getTextContent).orElse("Unknown");
                System.out.println("  Section: " + name + " (ID: " + id + ", Floor: " + floor + ")");
            });
        });

        // Find deeply nested elements
        root.findDescendant("title").ifPresent(title -> {
            System.out.println("First book title found: " + title.getTextContent());
        });

        System.out.println();
    }

    private static void demonstrateStreamNavigation(Editor editor) {
        System.out.println("2. Stream-Based Navigation Demo:");

        Element root = editor.getRootElement();

        // Find all books using streams
        List<Element> allBooks =
                root.descendants().filter(el -> "book".equals(el.getName())).collect(Collectors.toList());

        System.out.println("Total books in library: " + allBooks.size());

        // Find available books
        List<Element> availableBooks = allBooks.stream()
                .filter(book -> "true".equals(book.getAttribute("available")))
                .collect(Collectors.toList());

        System.out.println("Available books:");
        availableBooks.forEach(book -> {
            String title = book.findChild("title").map(Element::getTextContent).orElse("Unknown");
            String author =
                    book.findChild("author").map(Element::getTextContent).orElse("Unknown");
            System.out.println("  - " + title + " by " + author);
        });

        // Find all authors
        List<String> authors = root.descendants()
                .filter(el -> "author".equals(el.getName()))
                .map(Element::getTextContent)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        System.out.println("All authors: " + String.join(", ", authors));

        // Find books by genre
        System.out.println("Classic books:");
        root.descendants()
                .filter(el -> "book".equals(el.getName()))
                .filter(book -> book.findChild("genre")
                        .map(genre -> "Classic".equals(genre.getTextContent()))
                        .orElse(false))
                .forEach(book -> {
                    String title =
                            book.findChild("title").map(Element::getTextContent).orElse("Unknown");
                    System.out.println("  - " + title);
                });

        System.out.println();
    }

    private static void demonstrateRelationshipMethods(Editor editor) {
        System.out.println("3. Relationship Methods Demo:");

        Element root = editor.getRootElement();

        // Find a deeply nested element and explore relationships
        root.findDescendant("title").ifPresent(title -> {
            System.out.println("Analyzing element: " + title.getName() + " = '" + title.getTextContent() + "'");
            System.out.println("  Depth in tree: " + title.getDepth());
            System.out.println("  Root element: " + title.getRoot().getClass().getSimpleName());

            // Check parent relationships
            Element parent = (Element) title.getParent();
            if (parent != null) {
                System.out.println("  Parent: " + parent.getName());
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
        root.findChild("sections")
                .flatMap(sections -> sections.findChild("section"))
                .ifPresent(section -> printTreeStructure(section, 0));

        System.out.println();
    }

    private static void demonstrateAdvancedQueries(Editor editor) {
        System.out.println("4. Advanced Query Demo:");

        Element root = editor.getRootElement();

        // Complex query: Find books published after 1950 that are available
        System.out.println("Books published after 1950 that are available:");
        root.descendants()
                .filter(el -> "book".equals(el.getName()))
                .filter(book -> "true".equals(book.getAttribute("available")))
                .filter(book -> {
                    return book.findChild("year")
                            .map(Element::getTextContent)
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
                    String title =
                            book.findChild("title").map(Element::getTextContent).orElse("Unknown");
                    String year =
                            book.findChild("year").map(Element::getTextContent).orElse("Unknown");
                    String author = book.findChild("author")
                            .map(Element::getTextContent)
                            .orElse("Unknown");
                    System.out.println("  - " + title + " (" + year + ") by " + author);
                });

        // Find sections with more than 1 book
        System.out.println("\nSections with multiple books:");
        root.findChild("sections").ifPresent(sections -> {
            sections.findChildren("section")
                    .filter(section -> {
                        return section.findChild("books")
                                .map(books -> books.childElements().count() > 1)
                                .orElse(false);
                    })
                    .forEach(section -> {
                        String name = section.findChild("name")
                                .map(Element::getTextContent)
                                .orElse("Unknown");
                        long bookCount = section.findChild("books")
                                .map(books -> books.childElements().count())
                                .orElse(0L);
                        System.out.println("  - " + name + " (" + bookCount + " books)");
                    });
        });

        // Find all elements with specific attributes
        System.out.println("\nElements with 'id' attribute:");
        root.descendants().filter(el -> el.getAttribute("id") != null).forEach(el -> {
            System.out.println("  - " + el.getName() + " (id=" + el.getAttribute("id") + ")");
        });

        // Chain multiple navigation operations
        System.out.println("\nLibrarians in Reference department:");
        root.findChild("staff")
                .map(staff -> staff.findChildren("librarian")
                        .filter(lib -> lib.findChild("department")
                                .map(dept -> "Reference".equals(dept.getTextContent()))
                                .orElse(false))
                        .map(lib -> lib.findChild("name")
                                .map(Element::getTextContent)
                                .orElse("Unknown"))
                        .collect(Collectors.toList()))
                .orElse(List.of())
                .forEach(name -> System.out.println("  - " + name));

        System.out.println();
    }

    private static void printTreeStructure(Element element, int depth) {
        String indent = "  ".repeat(depth);
        String attrs = element.getAttributes().isEmpty()
                ? ""
                : " ["
                        + element.getAttributes().entrySet().stream()
                                .map(e -> e.getKey() + "=" + e.getValue())
                                .collect(Collectors.joining(", "))
                        + "]";

        System.out.println(indent + element.getName() + attrs);

        if (element.hasTextContent() && !element.hasChildElements()) {
            System.out.println(indent + "  \"" + element.getTextContent().trim() + "\"");
        }

        element.childElements().forEach(child -> printTreeStructure(child, depth + 1));
    }
}
