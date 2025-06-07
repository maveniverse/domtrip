package eu.maveniverse.domtrip;

/**
 * Enumeration for common whitespace patterns.
 */
public enum WhitespaceStyle {
    SINGLE_SPACE(" "),
    DOUBLE_SPACE("  "),
    TAB("\t"),
    NEWLINE("\n"),
    NEWLINE_WITH_INDENT("\n    "),
    EMPTY("");

    private final String value;

    WhitespaceStyle(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static WhitespaceStyle fromString(String whitespace) {
        if (whitespace == null) {
            return SINGLE_SPACE;
        }

        for (WhitespaceStyle style : values()) {
            if (style.value.equals(whitespace)) {
                return style;
            }
        }

        // Return custom whitespace as SINGLE_SPACE for now
        // In a real implementation, we might want to support custom patterns
        return SINGLE_SPACE;
    }

    @Override
    public String toString() {
        return value;
    }
}
