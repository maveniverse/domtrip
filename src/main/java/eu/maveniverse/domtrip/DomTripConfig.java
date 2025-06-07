package eu.maveniverse.domtrip;

/**
 * Configuration options for DomTrip XML processing.
 */
public class DomTripConfig {
    private boolean preserveWhitespace = true;
    private boolean preserveComments = true;
    private boolean preserveEntities = true;
    private boolean preserveProcessingInstructions = true;
    private boolean preserveCData = true;
    private String defaultEncoding = "UTF-8";
    private QuoteStyle defaultQuoteStyle = QuoteStyle.DOUBLE;
    private boolean validateXmlNames = true;
    private boolean strictParsing = false;
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
     * Creates a strict configuration with validation enabled.
     */
    public static DomTripConfig strict() {
        DomTripConfig config = new DomTripConfig();
        config.strictParsing = true;
        config.validateXmlNames = true;
        return config;
    }
    
    /**
     * Creates a lenient configuration with minimal validation.
     */
    public static DomTripConfig lenient() {
        DomTripConfig config = new DomTripConfig();
        config.strictParsing = false;
        config.validateXmlNames = false;
        return config;
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
    
    public DomTripConfig withEntityPreservation(boolean preserve) {
        this.preserveEntities = preserve;
        return this;
    }
    
    public DomTripConfig withProcessingInstructionPreservation(boolean preserve) {
        this.preserveProcessingInstructions = preserve;
        return this;
    }
    
    public DomTripConfig withCDataPreservation(boolean preserve) {
        this.preserveCData = preserve;
        return this;
    }
    
    public DomTripConfig withDefaultEncoding(String encoding) {
        this.defaultEncoding = encoding;
        return this;
    }
    
    public DomTripConfig withDefaultQuoteStyle(QuoteStyle quoteStyle) {
        this.defaultQuoteStyle = quoteStyle;
        return this;
    }
    
    public DomTripConfig withXmlNameValidation(boolean validate) {
        this.validateXmlNames = validate;
        return this;
    }
    
    public DomTripConfig withStrictParsing(boolean strict) {
        this.strictParsing = strict;
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
    public boolean isPreserveWhitespace() { return preserveWhitespace; }
    public boolean isPreserveComments() { return preserveComments; }
    public boolean isPreserveEntities() { return preserveEntities; }
    public boolean isPreserveProcessingInstructions() { return preserveProcessingInstructions; }
    public boolean isPreserveCData() { return preserveCData; }
    public String getDefaultEncoding() { return defaultEncoding; }
    public QuoteStyle getDefaultQuoteStyle() { return defaultQuoteStyle; }
    public boolean isValidateXmlNames() { return validateXmlNames; }
    public boolean isStrictParsing() { return strictParsing; }
    public boolean isPrettyPrint() { return prettyPrint; }
    public String getIndentString() { return indentString; }
    public String getLineEnding() { return lineEnding; }
    public boolean isOmitXmlDeclaration() { return omitXmlDeclaration; }
}
