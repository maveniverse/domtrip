package eu.maveniverse.domtrip;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Fluent API for querying and filtering XML elements.
 *
 * <p>ElementQuery provides a powerful, chainable interface for finding elements
 * based on various criteria including name, namespace, attributes, text content,
 * and structural relationships. It supports both immediate and lazy evaluation
 * of query results.</p>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Find all test dependencies
 * List<Element> testDeps = root.query()
 *     .withName("dependency")
 *     .withAttribute("scope", "test")
 *     .toList();
 *
 * // Find first element in specific namespace
 * Optional<Element> soapBody = root.query()
 *     .withQName(QName.of("http://schemas.xmlsoap.org/soap/envelope/", "Body"))
 *     .first();
 *
 * // Complex query with multiple criteria
 * Stream<Element> results = root.query()
 *     .withNamespace("http://maven.apache.org/POM/4.0.0")
 *     .withTextContent("junit")
 *     .atDepth(3)
 *     .all();
 * }</pre>
 *
 * <h3>Query Types:</h3>
 * <ul>
 *   <li><strong>Name-based</strong> - Filter by element name or QName</li>
 *   <li><strong>Attribute-based</strong> - Filter by attribute presence or value</li>
 *   <li><strong>Content-based</strong> - Filter by text content</li>
 *   <li><strong>Structural</strong> - Filter by depth or position</li>
 *   <li><strong>Namespace-based</strong> - Filter by namespace URI</li>
 * </ul>
 *
 * @see Element
 * @see QName
 */
public class ElementQuery {

    private final Element rootElement;
    private final Stream<Element> baseStream;
    private final Predicate<Element> filter;

    /**
     * Creates a new ElementQuery starting from the given element.
     *
     * @param rootElement the root element to query from
     */
    ElementQuery(Element rootElement) {
        this.rootElement = rootElement;
        this.baseStream = rootElement.descendants();
        this.filter = element -> true; // Start with no filtering
    }

    /**
     * Creates a new ElementQuery with a custom base stream and filter.
     *
     * @param rootElement the root element
     * @param baseStream the base stream of elements to filter
     * @param filter the current filter predicate
     */
    private ElementQuery(Element rootElement, Stream<Element> baseStream, Predicate<Element> filter) {
        this.rootElement = rootElement;
        this.baseStream = baseStream;
        this.filter = filter;
    }

    /**
     * Filters elements by local name.
     *
     * @param name the local name to match
     * @return a new ElementQuery with the name filter applied
     */
    public ElementQuery withName(String name) {
        if (name == null) {
            return this;
        }
        return new ElementQuery(rootElement, baseStream, filter.and(element -> name.equals(element.localName())));
    }

    /**
     * Filters elements by QName (namespace URI and local name).
     *
     * @param qname the QName to match
     * @return a new ElementQuery with the QName filter applied
     */
    public ElementQuery withQName(QName qname) {
        if (qname == null) {
            return this;
        }
        return new ElementQuery(
                rootElement,
                baseStream,
                filter.and(element -> qname.matches(element.namespaceURI(), element.localName())));
    }

    /**
     * Filters elements by namespace URI.
     *
     * @param namespaceURI the namespace URI to match
     * @return a new ElementQuery with the namespace filter applied
     */
    public ElementQuery withNamespace(String namespaceURI) {
        if (namespaceURI == null) {
            return this;
        }
        return new ElementQuery(
                rootElement, baseStream, filter.and(element -> namespaceURI.equals(element.namespaceURI())));
    }

    /**
     * Filters elements that have the specified attribute.
     *
     * @param attributeName the attribute name
     * @return a new ElementQuery with the attribute presence filter applied
     */
    public ElementQuery withAttribute(String attributeName) {
        if (attributeName == null) {
            return this;
        }
        return new ElementQuery(rootElement, baseStream, filter.and(element -> element.hasAttribute(attributeName)));
    }

    /**
     * Filters elements that have the specified attribute with the given value.
     *
     * @param attributeName the attribute name
     * @param attributeValue the attribute value to match
     * @return a new ElementQuery with the attribute value filter applied
     */
    public ElementQuery withAttribute(String attributeName, String attributeValue) {
        if (attributeName == null) {
            return this;
        }
        return new ElementQuery(
                rootElement,
                baseStream,
                filter.and(element -> attributeValue != null
                        ? attributeValue.equals(element.attribute(attributeName))
                        : element.hasAttribute(attributeName)));
    }

    /**
     * Filters elements that have the specified QName attribute.
     *
     * @param attributeQName the attribute QName
     * @return a new ElementQuery with the QName attribute presence filter applied
     */
    public ElementQuery withAttribute(QName attributeQName) {
        if (attributeQName == null) {
            return this;
        }
        return new ElementQuery(
                rootElement, baseStream, filter.and(element -> element.hasAttribute(attributeQName.qualifiedName())));
    }

    /**
     * Filters elements that have the specified QName attribute with the given value.
     *
     * @param attributeQName the attribute QName
     * @param attributeValue the attribute value to match
     * @return a new ElementQuery with the QName attribute value filter applied
     */
    public ElementQuery withAttribute(QName attributeQName, String attributeValue) {
        if (attributeQName == null) {
            return this;
        }
        return new ElementQuery(
                rootElement,
                baseStream,
                filter.and(element -> attributeValue != null
                        ? attributeValue.equals(element.attribute(attributeQName.qualifiedName()))
                        : element.hasAttribute(attributeQName.qualifiedName())));
    }

    /**
     * Filters elements by text content.
     *
     * @param textContent the text content to match
     * @return a new ElementQuery with the text content filter applied
     */
    public ElementQuery withTextContent(String textContent) {
        if (textContent == null) {
            return this;
        }
        return new ElementQuery(
                rootElement, baseStream, filter.and(element -> textContent.equals(element.textContent())));
    }

    /**
     * Filters elements that contain the specified text.
     *
     * @param text the text to search for
     * @return a new ElementQuery with the text contains filter applied
     */
    public ElementQuery containingText(String text) {
        if (text == null) {
            return this;
        }
        return new ElementQuery(rootElement, baseStream, filter.and(element -> {
            String content = element.textContent();
            return content != null && content.contains(text);
        }));
    }

    /**
     * Filters elements at the specified depth from the root element.
     *
     * @param depth the depth level (0 = root element, 1 = direct children, etc.)
     * @return a new ElementQuery with the depth filter applied
     */
    public ElementQuery atDepth(int depth) {
        if (depth < 0) {
            return this;
        }
        return new ElementQuery(
                rootElement, baseStream, filter.and(element -> element.depth() - rootElement.depth() == depth + 1));
    }

    /**
     * Filters elements that have child elements.
     *
     * @return a new ElementQuery with the has children filter applied
     */
    public ElementQuery withChildren() {
        return new ElementQuery(rootElement, baseStream, filter.and(Element::hasNodeElements));
    }

    /**
     * Filters elements that have no child elements.
     *
     * @return a new ElementQuery with the no children filter applied
     */
    public ElementQuery withoutChildren() {
        return new ElementQuery(rootElement, baseStream, filter.and(element -> !element.hasNodeElements()));
    }

    /**
     * Applies a custom filter predicate.
     *
     * @param customFilter the custom filter predicate
     * @return a new ElementQuery with the custom filter applied
     */
    public ElementQuery where(Predicate<Element> customFilter) {
        if (customFilter == null) {
            return this;
        }
        return new ElementQuery(rootElement, baseStream, filter.and(customFilter));
    }

    /**
     * Returns the first element matching the query criteria.
     *
     * @return an Optional containing the first matching element, or empty if none found
     */
    public Optional<Element> first() {
        return baseStream.filter(filter).findFirst();
    }

    /**
     * Returns all elements matching the query criteria as a Stream.
     *
     * @return a Stream of matching elements
     */
    public Stream<Element> all() {
        return baseStream.filter(filter);
    }

    /**
     * Returns all elements matching the query criteria as a List.
     *
     * @return a List of matching elements
     */
    public List<Element> toList() {
        return all().collect(Collectors.toList());
    }

    /**
     * Counts the number of elements matching the query criteria.
     *
     * @return the count of matching elements
     */
    public long count() {
        return all().count();
    }

    /**
     * Checks if any elements match the query criteria.
     *
     * @return true if at least one element matches
     */
    public boolean exists() {
        return first().isPresent();
    }
}
