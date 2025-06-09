package eu.maveniverse.domtrip;

/**
 * Enumeration for XML attribute quote styles, supporting both single and double quotes.
 *
 * <p>XML attributes can be quoted with either single quotes (') or double quotes (").
 * DomTrip preserves the original quote style during round-trip processing and allows
 * explicit control over quote style when creating new attributes.</p>
 *
 * <h3>Quote Style Preservation:</h3>
 * <p>When parsing XML, DomTrip automatically detects and preserves the original
 * quote style for each attribute. This ensures that the serialized XML maintains
 * the exact same formatting as the input.</p>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Set attribute with specific quote style
 * element.setAttribute("class", "important", QuoteStyle.SINGLE);
 * // Results in: class='important'
 *
 * element.setAttribute("id", "main", QuoteStyle.DOUBLE);
 * // Results in: id="main"
 *
 * // Get quote style from character
 * QuoteStyle style = QuoteStyle.fromChar('"');  // DOUBLE
 * QuoteStyle style2 = QuoteStyle.fromChar('\''); // SINGLE
 *
 * // Use in configuration
 * DomTripConfig config = DomTripConfig.defaults()
 *     .withQuoteStyle(QuoteStyle.SINGLE);
 * }</pre>
 *
 * <h3>XML Specification Compliance:</h3>
 * <p>Both quote styles are valid according to the XML specification. The choice
 * between them is often a matter of style preference or necessity when the
 * attribute value contains one type of quote character.</p>
 *
 * @author DomTrip Development Team
 * @since 1.0
 * @see Attribute
 * @see Element#setAttribute(String, String, QuoteStyle)
 * @see DomTripConfig#withQuoteStyle(QuoteStyle)
 */
public enum QuoteStyle {
    /** Double quote character (") for attribute values */
    DOUBLE('"'),
    /** Single quote character (') for attribute values */
    SINGLE('\'');

    private final char character;

    /**
     * Creates a QuoteStyle with the specified quote character.
     *
     * @param character the quote character
     */
    QuoteStyle(char character) {
        this.character = character;
    }

    /**
     * Gets the quote character for this style.
     *
     * @return the quote character (either '"' or '\'')
     * @since 1.0
     */
    public char getCharacter() {
        return character;
    }

    /**
     * Returns the QuoteStyle corresponding to the given character.
     *
     * <p>This method is useful when parsing XML attributes to determine
     * the original quote style used.</p>
     *
     * @param c the quote character to convert
     * @return the corresponding QuoteStyle
     * @throws IllegalArgumentException if the character is not a valid quote character
     * @since 1.0
     */
    public static QuoteStyle fromChar(char c) {
        for (QuoteStyle style : values()) {
            if (style.character == c) {
                return style;
            }
        }
        throw new IllegalArgumentException("Invalid quote character: " + c + ". Valid characters are '\"' and '\''");
    }

    /**
     * Returns the quote character as a string.
     *
     * @return the quote character as a single-character string
     * @since 1.0
     */
    @Override
    public String toString() {
        return String.valueOf(character);
    }
}
