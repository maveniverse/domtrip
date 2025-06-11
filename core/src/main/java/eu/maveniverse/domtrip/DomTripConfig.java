package eu.maveniverse.domtrip;

/**
 * Configuration options for controlling DomTrip XML processing behavior.
 *
 * <p>DomTripConfig provides comprehensive control over how XML documents are
 * parsed, processed, and serialized. It supports various formatting styles
 * from strict preservation to pretty printing, allowing users to choose the
 * appropriate balance between formatting preservation and readability.</p>
 *
 * <h3>Configuration Categories:</h3>
 * <ul>
 *   <li><strong>Preservation Settings</strong> - Control what formatting elements to preserve</li>
 *   <li><strong>Output Formatting</strong> - Configure pretty printing and indentation</li>
 *   <li><strong>Parsing Behavior</strong> - Control validation and error handling</li>
 *   <li><strong>Default Values</strong> - Set defaults for encoding, quotes, etc.</li>
 * </ul>
 *
 * <h3>Common Usage Patterns:</h3>
 * <pre>{@code
 * // Strict preservation (default)
 * DomTripConfig strict = DomTripConfig.defaults();
 *
 * // Pretty printing for readable output
 * DomTripConfig pretty = DomTripConfig.prettyPrint()
 *     .withIndentString("  ")
 *     .withDefaultQuoteStyle(QuoteStyle.DOUBLE);
 *
 * // Minimal output for size optimization
 * DomTripConfig minimal = DomTripConfig.minimal()
 *     .withCommentPreservation(false)
 *     .withWhitespacePreservation(false);
 *
 * // Custom configuration
 * DomTripConfig custom = DomTripConfig.defaults()
 *     .withWhitespacePreservation(true)
 *     .withDefaultQuoteStyle(QuoteStyle.SINGLE);
 * }</pre>
 *
 * <h3>Builder Pattern:</h3>
 * <p>DomTripConfig uses a fluent builder pattern for easy configuration:</p>
 * <pre>{@code
 * DomTripConfig config = DomTripConfig.defaults()
 *     .withWhitespacePreservation(true)
 *     .withCommentPreservation(true)
 *     .withPrettyPrint(false)
 *     .withIndentString("    ")
 *     .withDefaultQuoteStyle(QuoteStyle.SINGLE);
 * }</pre>
 *
 * @see Editor
 * @see Parser
 * @see Serializer
 * @see QuoteStyle
 */
public class DomTripConfig {
    private boolean preserveWhitespace = true;
    private boolean preserveComments = true;
    private boolean preserveProcessingInstructions = true;
    private QuoteStyle defaultQuoteStyle = QuoteStyle.DOUBLE;
    private boolean prettyPrint = false;
    private String indentString = "    ";
    private String lineEnding = "\n";
    private boolean omitXmlDeclaration = false;

    private DomTripConfig() {}

    /**
     * Creates a default configuration with all preservation features enabled.
     */
    public static DomTripConfig defaults() {
        return new DomTripConfig();
    }

    /**
     * Creates a strict configuration with all preservation features enabled.
     * This is an alias for defaults() for backward compatibility.
     */
    public static DomTripConfig strict() {
        return new DomTripConfig();
    }

    /**
     * Creates a lenient configuration with all preservation features enabled.
     * This is an alias for defaults() for backward compatibility.
     */
    public static DomTripConfig lenient() {
        return new DomTripConfig();
    }

    /**
     * Creates a configuration optimized for pretty printing.
     */
    public static DomTripConfig prettyPrint() {
        DomTripConfig config = new DomTripConfig();
        config.prettyPrint = true;
        config.preserveWhitespace = false;
        return config;
    }

    /**
     * Creates a minimal configuration for compact output.
     */
    public static DomTripConfig minimal() {
        DomTripConfig config = new DomTripConfig();
        config.preserveWhitespace = false;
        config.preserveComments = false;
        config.preserveProcessingInstructions = false;
        config.omitXmlDeclaration = true;
        return config;
    }

    // Fluent setters
    public DomTripConfig withWhitespacePreservation(boolean preserve) {
        this.preserveWhitespace = preserve;
        return this;
    }

    public DomTripConfig withCommentPreservation(boolean preserve) {
        this.preserveComments = preserve;
        return this;
    }

    public DomTripConfig withProcessingInstructionPreservation(boolean preserve) {
        this.preserveProcessingInstructions = preserve;
        return this;
    }

    public DomTripConfig withDefaultQuoteStyle(QuoteStyle quoteStyle) {
        this.defaultQuoteStyle = quoteStyle;
        return this;
    }

    public DomTripConfig withPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
        return this;
    }

    public DomTripConfig withIndentString(String indentString) {
        this.indentString = indentString;
        return this;
    }

    public DomTripConfig withLineEnding(String lineEnding) {
        this.lineEnding = lineEnding;
        return this;
    }

    public DomTripConfig withXmlDeclaration(boolean include) {
        this.omitXmlDeclaration = !include;
        return this;
    }

    // Getters
    public boolean isPreserveWhitespace() {
        return preserveWhitespace;
    }

    public boolean isPreserveComments() {
        return preserveComments;
    }

    public boolean isPreserveProcessingInstructions() {
        return preserveProcessingInstructions;
    }

    public QuoteStyle defaultQuoteStyle() {
        return defaultQuoteStyle;
    }

    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    public String indentString() {
        return indentString;
    }

    public String lineEnding() {
        return lineEnding;
    }

    public boolean isOmitXmlDeclaration() {
        return omitXmlDeclaration;
    }
}
