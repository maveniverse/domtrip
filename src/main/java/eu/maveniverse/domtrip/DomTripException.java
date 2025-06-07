package eu.maveniverse.domtrip;

/**
 * Base exception class for all DomTrip-related errors.
 *
 * <p>DomTripException serves as the root exception for all errors that can
 * occur during XML processing with DomTrip. It extends RuntimeException to
 * provide unchecked exception semantics, making the API easier to use while
 * still allowing proper error handling when needed.</p>
 *
 * <h3>Exception Hierarchy:</h3>
 * <ul>
 *   <li>{@link ParseException} - Errors during XML parsing</li>
 *   <li>{@link InvalidXmlException} - Invalid XML structure or content</li>
 *   <li>{@link NodeNotFoundException} - Missing nodes during navigation</li>
 * </ul>
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
 *
 * @author DomTrip Development Team
 * @since 1.0
 * @see ParseException
 * @see InvalidXmlException
 * @see NodeNotFoundException
 */
public class DomTripException extends RuntimeException {

    /**
     * Creates a new DomTripException with the specified message.
     *
     * @param message the detail message explaining the error
     * @since 1.0
     */
    public DomTripException(String message) {
        super(message);
    }

    /**
     * Creates a new DomTripException with the specified message and cause.
     *
     * @param message the detail message explaining the error
     * @param cause the underlying cause of this exception
     * @since 1.0
     */
    public DomTripException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new DomTripException with the specified cause.
     *
     * @param cause the underlying cause of this exception
     * @since 1.0
     */
    public DomTripException(Throwable cause) {
        super(cause);
    }
}
