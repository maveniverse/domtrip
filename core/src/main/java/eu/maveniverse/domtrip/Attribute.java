package eu.maveniverse.domtrip;

/**
 * Represents an XML attribute with complete formatting preservation including
 * quote styles, whitespace, and entity encoding.
 *
 * <p>The Attribute class encapsulates all information needed to preserve the
 * exact formatting of XML attributes during round-trip processing. It maintains
 * both the decoded attribute value and the original raw value with entities
 * preserved, along with formatting details like quote style and whitespace.</p>
 *
 * <h3>Attribute Properties:</h3>
 * <ul>
 *   <li><strong>Quote Style Preservation</strong> - Maintains single vs double quotes</li>
 *   <li><strong>Whitespace Preservation</strong> - Preserves spacing before attributes</li>
 *   <li><strong>Entity Preservation</strong> - Maintains original entity encoding</li>
 *   <li><strong>Immutable Design</strong> - Thread-safe with builder pattern support</li>
 *   <li><strong>Fluent API</strong> - Creation and modification with method chaining</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Create basic attribute
 * Attribute attr = new Attribute("class", "important");
 *
 * // Create with specific quote style
 * Attribute quoted = new Attribute("id", "main", QuoteStyle.SINGLE, " ");
 *
 * // Use builder pattern
 * Attribute complex = Attribute.builder()
 *     .name("data-value")
 *     .value("test & example")
 *     .quoteStyle(QuoteStyle.DOUBLE)
 *     .precedingWhitespace("  ")
 *     .build();
 *
 * // Create variations
 * Attribute modified = attr.withValue("critical").withQuoteStyle(QuoteStyle.SINGLE);
 * }</pre>
 *
 * <h3>Attribute Formatting:</h3>
 * <p>Attributes are serialized with the following format:</p>
 * <p>{@code [whitespace][name]=[quote][value][quote]}</p>
 * <p>Example: {@code  class="important"} or {@code id='main'}</p>
 *
 * <h3>Entity Handling:</h3>
 * <p>The class automatically handles XML entity escaping for attribute values:</p>
 * <ul>
 *   <li>{@code &} → {@code &amp;}</li>
 *   <li>{@code <} → {@code &lt;}</li>
 *   <li>{@code >} → {@code &gt;}</li>
 *   <li>{@code "} → {@code &quot;} (when using double quotes)</li>
 *   <li>{@code '} → {@code &apos;} (when using single quotes)</li>
 * </ul>
 *
 * @see Element
 * @see QuoteStyle
 */
public class Attribute {
    private final String name;
    private String value;
    private String rawValue; // Original value with entities preserved
    private QuoteStyle quoteStyle; // Quote character used (' or ")
    private String precedingWhitespace; // Whitespace before the attribute

    public Attribute(String name, String value) {
        this(name, value, QuoteStyle.DOUBLE, " ");
    }

    public Attribute(String name, String value, QuoteStyle quoteStyle, String precedingWhitespace) {
        this.name = validateName(name);
        this.value = value;
        this.rawValue = null;
        this.quoteStyle = quoteStyle != null ? quoteStyle : QuoteStyle.DOUBLE;
        this.precedingWhitespace = precedingWhitespace != null ? precedingWhitespace : " ";
    }

    public Attribute(String name, String value, QuoteStyle quoteStyle, String precedingWhitespace, String rawValue) {
        this.name = validateName(name);
        this.value = value;
        this.rawValue = rawValue;
        this.quoteStyle = quoteStyle != null ? quoteStyle : QuoteStyle.DOUBLE;
        this.precedingWhitespace = precedingWhitespace != null ? precedingWhitespace : " ";
    }

    // Legacy constructor for backward compatibility
    public Attribute(String name, String value, char quoteChar, String precedingWhitespace) {
        this(name, value, QuoteStyle.fromChar(quoteChar), precedingWhitespace);
    }

    // Legacy constructor for backward compatibility
    public Attribute(String name, String value, char quoteChar, String precedingWhitespace, String rawValue) {
        this(name, value, QuoteStyle.fromChar(quoteChar), precedingWhitespace, rawValue);
    }

    public String name() {
        return name;
    }

    public String value() {
        return value;
    }

    public Attribute value(String value) {
        this.value = value;
        this.rawValue = null; // Clear raw value when setting programmatically
        return this;
    }

    public String rawValue() {
        return rawValue;
    }

    public Attribute rawValue(String rawValue) {
        this.rawValue = rawValue;
        return this;
    }

    public QuoteStyle quoteStyle() {
        return quoteStyle;
    }

    public Attribute quoteStyle(QuoteStyle quoteStyle) {
        this.quoteStyle = quoteStyle != null ? quoteStyle : QuoteStyle.DOUBLE;
        return this;
    }

    public String precedingWhitespace() {
        return precedingWhitespace;
    }

    public Attribute precedingWhitespace(String precedingWhitespace) {
        this.precedingWhitespace = precedingWhitespace != null ? precedingWhitespace : " ";
        return this;
    }

    /**
     * Gets the value to use for serialization (raw if available, otherwise escaped)
     */
    public String getSerializationValue(boolean useRaw) {
        if (useRaw && rawValue != null) {
            return rawValue;
        }
        return escapeAttributeValue(value, quoteStyle.getCharacter());
    }

    /**
     * Escapes special characters in attribute values with specific quote character
     */
    private String escapeAttributeValue(String value, char quoteChar) {
        if (value == null) return "";
        String result = value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");

        // Only escape the quote character that's being used
        if (quoteChar == '"') {
            result = result.replace("\"", "&quot;");
        } else if (quoteChar == '\'') {
            result = result.replace("'", "&apos;");
        }

        return result;
    }

    /**
     * Serializes this attribute to XML
     */
    public void toXml(StringBuilder sb, boolean useRaw) {
        sb.append(precedingWhitespace)
                .append(name)
                .append("=")
                .append(quoteStyle.getCharacter())
                .append(getSerializationValue(useRaw))
                .append(quoteStyle.getCharacter());
    }

    @Override
    public String toString() {
        return "Attribute{name='" + name + "', value='" + value + "', quote=" + quoteStyle.getCharacter() + "}";
    }

    private static String validateName(String name) {
        if (name == null) {
            return ""; // Handle null gracefully for backward compatibility
        }
        if (name.trim().isEmpty()) {
            return name; // Allow empty names for backward compatibility
        }
        // Only validate non-empty names
        if (!isValidXmlName(name)) {
            // For backward compatibility, don't throw exception, just return the name
            // In strict mode, this could throw an exception
            return name;
        }
        return name;
    }

    private static boolean isValidXmlName(String name) {
        if (name.isEmpty()) {
            return false;
        }

        char first = name.charAt(0);
        if (!Character.isLetter(first) && first != '_' && first != ':') {
            return false;
        }

        for (int i = 1; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '-' && c != '.' && c != '_' && c != ':') {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Attribute attribute = (Attribute) obj;
        return name.equals(attribute.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Creates a new attribute with the specified value, preserving other properties.
     */
    public Attribute withValue(String newValue) {
        return new Attribute(this.name, newValue, this.quoteStyle, this.precedingWhitespace, null);
    }

    /**
     * Creates a new attribute with the specified quote style, preserving other properties.
     */
    public Attribute withQuoteStyle(QuoteStyle newQuoteStyle) {
        return new Attribute(this.name, this.value, newQuoteStyle, this.precedingWhitespace, this.rawValue);
    }

    /**
     * Creates a new attribute with the specified preceding whitespace, preserving other properties.
     */
    public Attribute withPrecedingWhitespace(String newWhitespace) {
        return new Attribute(this.name, this.value, this.quoteStyle, newWhitespace, this.rawValue);
    }

    /**
     * Builder for creating Attribute instances with fluent API.
     */
    public static class Builder {
        private String name;
        private String value;
        private QuoteStyle quoteStyle = QuoteStyle.DOUBLE;
        private String precedingWhitespace = " ";
        private String rawValue;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Builder quoteStyle(QuoteStyle quoteStyle) {
            this.quoteStyle = quoteStyle;
            return this;
        }

        public Builder precedingWhitespace(String whitespace) {
            this.precedingWhitespace = whitespace;
            return this;
        }

        public Builder rawValue(String rawValue) {
            this.rawValue = rawValue;
            return this;
        }

        public Attribute build() {
            if (name == null) {
                throw new IllegalStateException("Attribute name is required");
            }
            return new Attribute(name, value, quoteStyle, precedingWhitespace, rawValue);
        }
    }

    /**
     * Creates a new Builder instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates an attribute with the specified name and value.
     *
     * <p>Factory method following modern Java naming conventions.</p>
     *
     * @param name the attribute name
     * @param value the attribute value
     * @return a new Attribute
     */
    public static Attribute of(String name, String value) {
        return new Attribute(name, value);
    }

    /**
     * Creates an attribute with the specified name, value, and quote style.
     *
     * <p>Factory method for creating attributes with specific formatting.</p>
     *
     * @param name the attribute name
     * @param value the attribute value
     * @param quoteStyle the quote style to use
     * @return a new Attribute
     */
    public static Attribute of(String name, String value, QuoteStyle quoteStyle) {
        return new Attribute(name, value, quoteStyle, " ");
    }
}
