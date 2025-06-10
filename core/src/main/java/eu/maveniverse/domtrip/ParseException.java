package eu.maveniverse.domtrip;

/**
 * Exception thrown when XML parsing fails due to malformed or invalid XML content.
 *
 * <p>ParseException provides detailed information about parsing failures, including
 * the exact position where the error occurred and context from the source XML.
 * This information helps developers quickly identify and fix XML syntax issues.</p>
 *
 * <h3>Error Information:</h3>
 * <ul>
 *   <li><strong>Position</strong> - Character offset where the error occurred</li>
 *   <li><strong>Context</strong> - Surrounding XML content for debugging</li>
 *   <li><strong>Message</strong> - Descriptive error message</li>
 * </ul>
 *
 * <h3>Common Parsing Errors:</h3>
 * <ul>
 *   <li>Unclosed tags or attributes</li>
 *   <li>Invalid character sequences</li>
 *   <li>Malformed XML declarations</li>
 *   <li>Unescaped special characters</li>
 *   <li>Invalid nesting structures</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * try {
 *     Parser parser = new Parser();
 *     Document doc = parser.parse(xmlString);
 * } catch (ParseException e) {
 *     System.err.println("Parse error: " + e.getMessage());
 *     if (e.getPosition() >= 0) {
 *         System.err.println("Error at character position: " + e.getPosition());
 *     }
 *     if (e.getXmlContent() != null) {
 *         // Show context around the error
 *         showErrorContext(e.getXmlContent(), e.getPosition());
 *     }
 * }
 * }</pre>
 *
 * @see Parser
 * @see DomTripException
 */
public class ParseException extends DomTripException {

    private final int position;
    private final String xmlContent;

    /**
     * Creates a ParseException with the specified message.
     *
     * @param message the error message
     */
    public ParseException(String message) {
        this(message, -1, null);
    }

    /**
     * Creates a ParseException with the specified message and position.
     *
     * @param message the error message
     * @param position the character position where the error occurred
     */
    public ParseException(String message, int position) {
        this(message, position, null);
    }

    /**
     * Creates a ParseException with the specified message, position, and XML content.
     *
     * @param message the error message
     * @param position the character position where the error occurred
     * @param xmlContent the XML content being parsed (for context)
     */
    public ParseException(String message, int position, String xmlContent) {
        super(message + (position >= 0 ? " at position " + position : ""));
        this.position = position;
        this.xmlContent = xmlContent;
    }

    /**
     * Creates a ParseException with the specified message and underlying cause.
     *
     * @param message the error message
     * @param cause the underlying cause of the parsing failure
     */
    public ParseException(String message, Throwable cause) {
        super(message, cause);
        this.position = -1;
        this.xmlContent = null;
    }

    /**
     * Gets the character position where the parsing error occurred.
     *
     * @return the character position, or -1 if position is not available
     */
    public int getPosition() {
        return position;
    }

    /**
     * Gets the XML content that was being parsed when the error occurred.
     *
     * <p>This can be used to provide context around the error position
     * for debugging purposes.</p>
     *
     * @return the XML content, or null if not available
     */
    public String getXmlContent() {
        return xmlContent;
    }
}
