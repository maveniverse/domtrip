package eu.maveniverse.domtrip;

/**
 * Exception thrown when XML content is invalid.
 */
public class InvalidXmlException extends DomTripException {

    public InvalidXmlException(String message) {
        super(message);
    }

    public InvalidXmlException(String message, Throwable cause) {
        super(message, cause);
    }
}
