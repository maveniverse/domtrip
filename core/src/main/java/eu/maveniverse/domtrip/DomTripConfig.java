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
 * // Default preservation
 * DomTripConfig defaults = DomTripConfig.defaults();
 *
 * // Pretty printing for readable output
 * DomTripConfig pretty = DomTripConfig.prettyPrint()
 *     .withIndentString("  ")
 *     .withDefaultQuoteStyle(QuoteStyle.DOUBLE);
 *
 * // Minimal output for size optimization
 * DomTripConfig minimal = DomTripConfig.minimal()
 *     .withCommentPreservation(false);
 *
 * // Custom configuration
 * DomTripConfig custom = DomTripConfig.defaults()
 *     .withDefaultQuoteStyle(QuoteStyle.SINGLE)
 *     .withEmptyElementStyle(EmptyElementStyle.SELF_CLOSING_SPACED);
 * }</pre>
 *
 * <h3>Builder Pattern:</h3>
 * <p>DomTripConfig uses a fluent builder pattern for easy configuration:</p>
 * <pre>{@code
 * DomTripConfig config = DomTripConfig.defaults()
 *     .withCommentPreservation(true)
 *     .withPrettyPrint(false)
 *     .withIndentString("    ")
 *     .withDefaultQuoteStyle(QuoteStyle.SINGLE)
 *     .withEmptyElementStyle(EmptyElementStyle.EXPANDED);
 * }</pre>
 *
 * @see Editor
 * @see Parser
 * @see Serializer
 * @see QuoteStyle
 * @see EmptyElementStyle
 */
public class DomTripConfig {
    private boolean preserveComments = true;
    private boolean preserveProcessingInstructions = true;
    private QuoteStyle defaultQuoteStyle = QuoteStyle.DOUBLE;
    private EmptyElementStyle emptyElementStyle = EmptyElementStyle.SELF_CLOSING;
    private boolean forceEmptyElementStyle = false;
    private boolean prettyPrint = false;
    private String indentString = "    ";
    private String lineEnding = "\n";
    private boolean omitXmlDeclaration = false;
    private boolean ignoreInvalidEncoding = true;

    private DomTripConfig() {}

    /**
     * Creates a default configuration with all preservation features enabled.
     */
    public static DomTripConfig defaults() {
        return new DomTripConfig();
    }

    /**
     * Creates a configuration optimized for pretty printing.
     */
    public static DomTripConfig prettyPrint() {
        DomTripConfig config = new DomTripConfig();
        config.prettyPrint = true;
        return config;
    }

    /**
     * Creates a minimal configuration for compact output.
     */
    public static DomTripConfig minimal() {
        DomTripConfig config = new DomTripConfig();
        config.preserveComments = false;
        config.preserveProcessingInstructions = false;
        config.omitXmlDeclaration = true;
        return config;
    }

    /**
     * Creates a raw configuration for completely unformatted output.
     *
     * <p>Raw mode produces XML with no line breaks or indentation whatsoever,
     * resulting in a single continuous line of XML. This is useful for
     * minimizing file size or when formatting is not desired.</p>
     */
    public static DomTripConfig raw() {
        DomTripConfig config = new DomTripConfig();
        config.prettyPrint = true;
        config.indentString = "";
        config.lineEnding = "";
        return config;
    }

    // Fluent setters
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

    public DomTripConfig withEmptyElementStyle(EmptyElementStyle emptyElementStyle) {
        this.emptyElementStyle = emptyElementStyle;
        return this;
    }

    public DomTripConfig withIgnoreInvalidEncoding(boolean ignoreInvalidEncoding) {
        this.ignoreInvalidEncoding = ignoreInvalidEncoding;
        return this;
    }

    /**
     * Automatically detects and configures the empty element style based on existing
     * empty elements in the provided document.
     *
     * <p>This method analyzes all empty elements in the document to determine the
     * predominant style and configures this DomTripConfig to use that style.
     * If no empty elements are found, the current style is preserved.</p>
     *
     * @param document the document to analyze for empty element styles
     * @return this DomTripConfig for method chaining
     */
    public DomTripConfig withAutoDetectedEmptyElementStyle(Document document) {
        EmptyElementStyle detected = EmptyElementStyle.detectFromDocument(document);
        this.emptyElementStyle = detected;
        return this;
    }

    // Getters
    public boolean isPreserveComments() {
        return preserveComments;
    }

    public boolean isPreserveProcessingInstructions() {
        return preserveProcessingInstructions;
    }

    public QuoteStyle defaultQuoteStyle() {
        return defaultQuoteStyle;
    }

    public EmptyElementStyle emptyElementStyle() {
        return emptyElementStyle;
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

    public boolean isIgnoreInvalidEncoding() {
        return ignoreInvalidEncoding;
    }
}
