package eu.maveniverse.domtrip;

/**
 * Enumeration for XML attribute quote styles.
 */
public enum QuoteStyle {
    DOUBLE('"'),
    SINGLE('\'');

    private final char character;

    QuoteStyle(char character) {
        this.character = character;
    }

    public char getCharacter() {
        return character;
    }

    public static QuoteStyle fromChar(char c) {
        for (QuoteStyle style : values()) {
            if (style.character == c) {
                return style;
            }
        }
        throw new IllegalArgumentException("Invalid quote character: " + c);
    }

    @Override
    public String toString() {
        return String.valueOf(character);
    }
}
