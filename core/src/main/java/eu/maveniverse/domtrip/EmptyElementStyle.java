package eu.maveniverse.domtrip;

/**
 * Enumeration for XML empty element formatting styles.
 *
 * <p>XML empty elements can be written in three different styles according to the XML specification.
 * DomTrip can automatically detect the existing style in a document and preserve it, or you can
 * explicitly configure which style to use for new empty elements.</p>
 *
 * <h3>Style Examples:</h3>
 * <ul>
 *   <li><strong>EXPANDED</strong> - {@code <element></element>} (separate opening and closing tags)</li>
 *   <li><strong>SELF_CLOSING</strong> - {@code <element/>} (self-closing tag without space)</li>
 *   <li><strong>SELF_CLOSING_SPACED</strong> - {@code <element />} (self-closing tag with space)</li>
 * </ul>
 *
 * <h3>Auto-Detection:</h3>
 * <p>When parsing existing XML documents, DomTrip can automatically detect the predominant
 * empty element style and use it for new empty elements. This ensures consistency with
 * the existing document formatting.</p>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Configure explicit style
 * DomTripConfig config = DomTripConfig.defaults()
 *     .withEmptyElementStyle(EmptyElementStyle.SELF_CLOSING_SPACED);
 *
 * // Create element that will use configured style when empty
 * Element element = Element.of("placeholder");
 * // When serialized: <placeholder />
 *
 * // Auto-detect from existing document
 * Document doc = Document.of("<root><empty/><another/></root>");
 * EmptyElementStyle detected = EmptyElementStyle.detectFromDocument(doc);
 * // Returns: SELF_CLOSING
 * }</pre>
 *
 * <h3>XML Specification Compliance:</h3>
 * <p>All three styles are valid according to the XML specification. The choice between
 * them is typically a matter of style preference, tool conventions, or organizational
 * standards.</p>
 *
 * @see DomTripConfig#withEmptyElementStyle(EmptyElementStyle)
 * @see Element
 * @see Serializer
 */
public enum EmptyElementStyle {
    /**
     * Expanded form with separate opening and closing tags.
     * <p>Example: {@code <element></element>}</p>
     * <p>This style is more verbose but may be preferred in some contexts
     * for clarity or compatibility with older XML processors.</p>
     */
    EXPANDED,

    /**
     * Self-closing tag without space before the slash.
     * <p>Example: {@code <element/>}</p>
     * <p>This is the most compact form and is commonly used in modern XML.</p>
     */
    SELF_CLOSING,

    /**
     * Self-closing tag with space before the slash.
     * <p>Example: {@code <element />}</p>
     * <p>This style is often preferred for better readability and is
     * compatible with XHTML conventions.</p>
     */
    SELF_CLOSING_SPACED;

    /**
     * Detects the predominant empty element style used in a document.
     *
     * <p>This method analyzes all empty elements in the document and returns
     * the most commonly used style. If no empty elements are found, or if
     * there's a tie between styles, it returns {@link #SELF_CLOSING} as the default.</p>
     *
     * @param document the document to analyze
     * @return the detected empty element style, or SELF_CLOSING if none detected
     */
    public static EmptyElementStyle detectFromDocument(Document document) {
        if (document == null || document.root() == null) {
            return SELF_CLOSING;
        }

        int expandedCount = 0;
        int selfClosingCount = 0;
        int selfClosingSpacedCount = 0;

        // Recursively analyze all elements in the document
        EmptyElementStyleCounter counter = new EmptyElementStyleCounter();
        analyzeElement(document.root(), counter);

        expandedCount = counter.expandedCount;
        selfClosingCount = counter.selfClosingCount;
        selfClosingSpacedCount = counter.selfClosingSpacedCount;

        // Return the most common style, with SELF_CLOSING as default
        if (expandedCount > selfClosingCount && expandedCount > selfClosingSpacedCount) {
            return EXPANDED;
        } else if (selfClosingSpacedCount > selfClosingCount && selfClosingSpacedCount > expandedCount) {
            return SELF_CLOSING_SPACED;
        } else {
            return SELF_CLOSING;
        }
    }

    /**
     * Helper method to recursively analyze elements for empty element styles.
     */
    private static void analyzeElement(Element element, EmptyElementStyleCounter counter) {
        // Check if this element is empty (no child nodes)
        if (element.nodeCount() == 0) {
            // Analyze the original formatting to determine style
            String originalTag = element.originalOpenTag();
            if (!originalTag.isEmpty()) {
                if (element.selfClosing()) {
                    // Check for space before />
                    if (originalTag.endsWith(" />")) {
                        counter.selfClosingSpacedCount++;
                    } else if (originalTag.endsWith("/>")) {
                        counter.selfClosingCount++;
                    }
                } else {
                    // Has separate closing tag
                    counter.expandedCount++;
                }
            } else {
                // No original formatting available, check current state
                if (element.selfClosing()) {
                    // Default to SELF_CLOSING if no space information available
                    counter.selfClosingCount++;
                } else {
                    counter.expandedCount++;
                }
            }
        }

        // Recursively analyze child elements
        for (Node child : element.nodes().toList()) {
            if (child instanceof Element) {
                analyzeElement((Element) child, counter);
            }
        }
    }

    /**
     * Helper class to count different empty element styles.
     */
    private static class EmptyElementStyleCounter {
        int expandedCount = 0;
        int selfClosingCount = 0;
        int selfClosingSpacedCount = 0;
    }

    /**
     * Formats an empty element according to this style.
     *
     * @param elementName the name of the element
     * @param attributes the attributes string (may be empty)
     * @return the formatted empty element string
     */
    public String format(String elementName, String attributes) {
        String attrString = attributes.isEmpty() ? "" : " " + attributes.trim();

        switch (this) {
            case EXPANDED:
                return "<" + elementName + attrString + "></" + elementName + ">";
            case SELF_CLOSING:
                return "<" + elementName + attrString + "/>";
            case SELF_CLOSING_SPACED:
                return "<" + elementName + attrString + " />";
            default:
                return "<" + elementName + attrString + "/>";
        }
    }

    /**
     * Returns a human-readable description of this style.
     *
     * @return a description of the empty element style
     */
    @Override
    public String toString() {
        switch (this) {
            case EXPANDED:
                return "expanded (<element></element>)";
            case SELF_CLOSING:
                return "self-closing (<element/>)";
            case SELF_CLOSING_SPACED:
                return "self-closing with space (<element />)";
            default:
                return "unknown";
        }
    }
}
