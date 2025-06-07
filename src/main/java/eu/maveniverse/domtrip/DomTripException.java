package eu.maveniverse.domtrip;

/**
 * Base exception class for all DomTrip-related errors.
 */
public class DomTripException extends RuntimeException {
    
    public DomTripException(String message) {
        super(message);
    }
    
    public DomTripException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public DomTripException(Throwable cause) {
        super(cause);
    }
}
