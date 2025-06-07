package eu.maveniverse.domtrip;

/**
 * Exception thrown when XML parsing fails.
 */
public class ParseException extends DomTripException {

    private final int position;
    private final String xmlContent;

    public ParseException(String message) {
        this(message, -1, null);
    }

    public ParseException(String message, int position) {
        this(message, position, null);
    }

    public ParseException(String message, int position, String xmlContent) {
        super(message + (position >= 0 ? " at position " + position : ""));
        this.position = position;
        this.xmlContent = xmlContent;
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
        this.position = -1;
        this.xmlContent = null;
    }

    public int getPosition() {
        return position;
    }

    public String getXmlContent() {
        return xmlContent;
    }
}
