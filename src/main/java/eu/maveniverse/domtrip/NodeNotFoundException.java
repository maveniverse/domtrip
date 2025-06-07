package eu.maveniverse.domtrip;

/**
 * Exception thrown when a requested node cannot be found.
 */
public class NodeNotFoundException extends DomTripException {

    private final String nodeName;
    private final String xpath;

    public NodeNotFoundException(String message) {
        this(message, null, null);
    }

    public NodeNotFoundException(String message, String nodeName) {
        this(message, nodeName, null);
    }

    public NodeNotFoundException(String message, String nodeName, String xpath) {
        super(message);
        this.nodeName = nodeName;
        this.xpath = xpath;
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getXpath() {
        return xpath;
    }
}
