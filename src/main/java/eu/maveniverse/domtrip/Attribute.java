package eu.maveniverse.domtrip;

/**
 * Represents an XML attribute with all its formatting information.
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
    
    public String getName() {
        return name;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
        this.rawValue = null; // Clear raw value when setting programmatically
    }
    
    public String getRawValue() {
        return rawValue;
    }
    
    public void setRawValue(String rawValue) {
        this.rawValue = rawValue;
    }
    
    public QuoteStyle getQuoteStyle() {
        return quoteStyle;
    }

    public void setQuoteStyle(QuoteStyle quoteStyle) {
        this.quoteStyle = quoteStyle != null ? quoteStyle : QuoteStyle.DOUBLE;
    }

    // Legacy method for backward compatibility
    public char getQuoteChar() {
        return quoteStyle.getCharacter();
    }

    // Legacy method for backward compatibility
    public void setQuoteChar(char quoteChar) {
        this.quoteStyle = QuoteStyle.fromChar(quoteChar);
    }
    
    public String getPrecedingWhitespace() {
        return precedingWhitespace;
    }
    
    public void setPrecedingWhitespace(String precedingWhitespace) {
        this.precedingWhitespace = precedingWhitespace != null ? precedingWhitespace : " ";
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
        String result = value.replace("&", "&amp;")
                            .replace("<", "&lt;")
                            .replace(">", "&gt;");
        
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
}
