package eu.maveniverse.domtrip;

/**
 * Represents an XML attribute with all its formatting information.
 */
public class Attribute {
    private final String name;
    private String value;
    private String rawValue; // Original value with entities preserved
    private char quoteChar; // Quote character used (' or ")
    private String precedingWhitespace; // Whitespace before the attribute
    
    public Attribute(String name, String value) {
        this(name, value, '"', " ");
    }
    
    public Attribute(String name, String value, char quoteChar, String precedingWhitespace) {
        this.name = name;
        this.value = value;
        this.rawValue = null;
        this.quoteChar = quoteChar;
        this.precedingWhitespace = precedingWhitespace != null ? precedingWhitespace : " ";
    }
    
    public Attribute(String name, String value, char quoteChar, String precedingWhitespace, String rawValue) {
        this.name = name;
        this.value = value;
        this.rawValue = rawValue;
        this.quoteChar = quoteChar;
        this.precedingWhitespace = precedingWhitespace != null ? precedingWhitespace : " ";
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
    
    public char getQuoteChar() {
        return quoteChar;
    }
    
    public void setQuoteChar(char quoteChar) {
        this.quoteChar = quoteChar;
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
        return escapeAttributeValue(value, quoteChar);
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
          .append(quoteChar)
          .append(getSerializationValue(useRaw))
          .append(quoteChar);
    }
    
    @Override
    public String toString() {
        return "Attribute{name='" + name + "', value='" + value + "', quote=" + quoteChar + "}";
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
}
