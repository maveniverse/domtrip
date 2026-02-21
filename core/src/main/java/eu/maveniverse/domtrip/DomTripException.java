package eu.maveniverse.domtrip;

/**
 * Base exception class for all DomTrip-related errors.
 *
 * <p>DomTripException serves as the exception for all errors that can
 * occur during XML processing with DomTrip. It extends RuntimeException to
 * provide unchecked exception semantics, making the API easier to use while
 * still allowing proper error handling when needed.</p>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * try {
 *     Editor editor = new Editor(malformedXml);
 * } catch (DomTripException e) {
 *     // Handle any DomTrip-related error
 *     logger.error("XML processing failed: " + e.getMessage(), e);
 * }
 * }</pre>
 *
 * <h3>Error Recovery:</h3>
 * <p>When catching DomTripException, consider:</p>
 * <ul>
 *   <li>Checking the specific exception type for targeted handling</li>
 *   <li>Examining the cause for underlying issues</li>
 *   <li>Providing meaningful error messages to users</li>
 *   <li>Logging sufficient detail for debugging</li>
 * </ul>
 */
public class DomTripException extends RuntimeException {

    protected final int position;
    protected final String xmlContent;

    /**
     * Creates a new DomTripException with the specified message.
     *
     * @param message the detail message explaining the error
     */
    public DomTripException(String message, int position, String xmlContent) {
        super(message + (position >= 0 ? " at position " + position : ""));
        this.position = position;
        this.xmlContent = xmlContent;
    }

    /**
     * Creates a new DomTripException with the specified message.
     *
     * @param message the detail message explaining the error
     */
    public DomTripException(String message) {
        this(message, null);
    }

    /**
     * Creates a new DomTripException with the specified message and cause.
     *
     * @param message the detail message explaining the error
     * @param cause the underlying cause of this exception
     */
    public DomTripException(String message, Throwable cause) {
        super(message, cause);
        this.position = -1;
        this.xmlContent = null;
    }

    /**
     * Creates a new DomTripException with the specified cause.
     *
     * @param cause the underlying cause of this exception
     */
    public DomTripException(Throwable cause) {
        super(cause);
        this.position = -1;
        this.xmlContent = null;
    }

    /**
     * Gets the character position where the parsing error occurred.
     *
     * @return the character position, or -1 if position is not available
     */
    public int position() {
        return position;
    }

    /** @deprecated Use {@link #position()} instead. */
    @Deprecated
    public int getPosition() {
        return position();
    }

    /**
     * Gets the XML content that was being parsed when the error occurred.
     *
     * <p>This can be used to provide context around the error position
     * for debugging purposes.</p>
     *
     * @return the XML content, or null if not available
     */
    public String xmlContent() {
        return xmlContent;
    }

    /** @deprecated Use {@link #xmlContent()} instead. */
    @Deprecated
    public String getXmlContent() {
        return xmlContent();
    }
}
