package eu.maveniverse.domtrip;

import java.util.Objects;

/**
 * Represents a qualified XML name with namespace URI, local name, and optional prefix.
 *
 * <p>QName provides a clean, type-safe way to work with XML namespaces, eliminating
 * the need to pass namespace URI and local name as separate parameters throughout
 * the API. It supports both prefixed and unprefixed names, and handles the common
 * case of elements in no namespace.</p>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Simple local name (no namespace)
 * QName version = QName.of("version");
 *
 * // Namespaced element
 * QName dependency = QName.of("http://maven.apache.org/POM/4.0.0", "dependency");
 *
 * // With preferred prefix
 * QName soapEnvelope = QName.of("http://schemas.xmlsoap.org/soap/envelope/", "Envelope", "soap");
 *
 * // Parse from qualified name
 * QName parsed = QName.parse("soap:Envelope");  // prefix:localName
 * }</pre>
 *
 * <h3>Namespace Handling:</h3>
 * <ul>
 *   <li><strong>No Namespace</strong> - Elements with no namespace URI</li>
 *   <li><strong>Default Namespace</strong> - Elements in a namespace without prefix</li>
 *   <li><strong>Prefixed Namespace</strong> - Elements with explicit namespace prefix</li>
 * </ul>
 *
 * @see Element
 * @see NamespaceContext
 * @see NamespaceResolver
 */
public final class QName {

    /** Empty namespace URI constant for elements not in any namespace */
    public static final String NO_NAMESPACE = "";

    private final String namespaceURI;
    private final String localName;
    private final String prefix;
    /**
     * Creates a QName with the specified namespace URI, local name, and prefix.
     *
     * @param namespaceURI the namespace URI, or null/empty for no namespace
     * @param localName the local name, must not be null or empty
     * @param prefix the namespace prefix, or null for no prefix
     * @throws IllegalArgumentException if localName is null or empty
     */
    private QName(String namespaceURI, String localName, String prefix) {
        if (localName == null || localName.trim().isEmpty()) {
            throw new IllegalArgumentException("Local name cannot be null or empty");
        }

        this.namespaceURI = namespaceURI != null ? namespaceURI : NO_NAMESPACE;
        this.localName = localName.trim();
        this.prefix = (prefix != null && !prefix.trim().isEmpty()) ? prefix.trim() : null;
    }

    /**
     * Creates a QName for an element with no namespace.
     *
     * @param localName the local name
     * @return a new QName with no namespace
     * @throws IllegalArgumentException if localName is null or empty
     */
    public static QName of(String localName) {
        return new QName(NO_NAMESPACE, localName, null);
    }

    /**
     * Creates a QName with the specified namespace URI and local name.
     *
     * @param namespaceURI the namespace URI
     * @param localName the local name
     * @return a new QName
     * @throws IllegalArgumentException if localName is null or empty
     */
    public static QName of(String namespaceURI, String localName) {
        return new QName(namespaceURI, localName, null);
    }

    /**
     * Creates a QName with the specified namespace URI, local name, and preferred prefix.
     *
     * @param namespaceURI the namespace URI
     * @param localName the local name
     * @param prefix the preferred namespace prefix
     * @return a new QName
     * @throws IllegalArgumentException if localName is null or empty
     */
    public static QName of(String namespaceURI, String localName, String prefix) {
        return new QName(namespaceURI, localName, prefix);
    }

    /**
     * Parses a qualified name string into a QName.
     *
     * <p>Supports the following formats:</p>
     * <ul>
     *   <li><code>localName</code> - Element with no namespace</li>
     *   <li><code>prefix:localName</code> - Element with namespace prefix</li>
     * </ul>
     *
     * <p>Note: This method only parses the syntactic structure. The actual
     * namespace URI must be resolved using a {@link NamespaceContext}.</p>
     *
     * @param qualifiedName the qualified name to parse
     * @return a new QName with the parsed prefix and local name
     * @throws IllegalArgumentException if qualifiedName is null, empty, or invalid
     */
    public static QName parse(String qualifiedName) {
        if (qualifiedName == null || qualifiedName.trim().isEmpty()) {
            throw new IllegalArgumentException("Qualified name cannot be null or empty");
        }

        String trimmed = qualifiedName.trim();
        int colonIndex = trimmed.indexOf(':');

        if (colonIndex == -1) {
            // No prefix
            return new QName(NO_NAMESPACE, trimmed, null);
        } else if (colonIndex == 0 || colonIndex == trimmed.length() - 1) {
            // Invalid: starts or ends with colon
            throw new IllegalArgumentException("Invalid qualified name: " + qualifiedName);
        } else {
            // Has prefix
            String prefix = trimmed.substring(0, colonIndex);
            String localName = trimmed.substring(colonIndex + 1);
            return new QName(NO_NAMESPACE, localName, prefix);
        }
    }

    /**
     * Gets the namespace URI.
     *
     * @return the namespace URI, or empty string if not in any namespace
     */
    public String namespaceURI() {
        return namespaceURI;
    }

    /**
     * Gets the local name.
     *
     * @return the local name, never null or empty
     */
    public String localName() {
        return localName;
    }

    /**
     * Gets the namespace prefix.
     *
     * @return the namespace prefix, or null if no prefix
     */
    public String prefix() {
        return prefix;
    }

    /**
     * Checks if this QName has a namespace.
     *
     * @return true if this QName has a non-empty namespace URI
     */
    public boolean hasNamespace() {
        return !NO_NAMESPACE.equals(namespaceURI);
    }

    /**
     * Checks if this QName has a prefix.
     *
     * @return true if this QName has a non-null prefix
     */
    public boolean hasPrefix() {
        return prefix != null;
    }

    /**
     * Gets the qualified name (prefix:localName or just localName).
     *
     * @return the qualified name
     */
    public String qualifiedName() {
        return hasPrefix() ? prefix + ":" + localName : localName;
    }

    /**
     * Creates a new QName with the same local name and prefix but different namespace URI.
     *
     * @param newNamespaceURI the new namespace URI
     * @return a new QName with the updated namespace URI
     */
    public QName withNamespaceURI(String newNamespaceURI) {
        return new QName(newNamespaceURI, localName, prefix);
    }

    /**
     * Creates a new QName with the same namespace URI and local name but different prefix.
     *
     * @param newPrefix the new prefix
     * @return a new QName with the updated prefix
     */
    public QName withPrefix(String newPrefix) {
        return new QName(namespaceURI, localName, newPrefix);
    }

    /**
     * Checks if this QName matches the given namespace URI and local name.
     *
     * @param namespaceURI the namespace URI to match
     * @param localName the local name to match
     * @return true if both namespace URI and local name match
     */
    public boolean matches(String namespaceURI, String localName) {
        return Objects.equals(this.namespaceURI, namespaceURI != null ? namespaceURI : NO_NAMESPACE)
                && Objects.equals(this.localName, localName);
    }

    /**
     * Checks if this QName matches the given QName.
     *
     * <p>Two QNames match if they have the same namespace URI and local name.
     * The prefix is not considered for matching.</p>
     *
     * @param other the QName to match against
     * @return true if namespace URI and local name match
     */
    public boolean matches(QName other) {
        return other != null && matches(other.namespaceURI, other.localName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        QName qname = (QName) obj;
        return Objects.equals(namespaceURI, qname.namespaceURI)
                && Objects.equals(localName, qname.localName)
                && Objects.equals(prefix, qname.prefix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespaceURI, localName, prefix);
    }

    @Override
    public String toString() {
        if (hasNamespace()) {
            return String.format("QName{%s}%s", namespaceURI, qualifiedName());
        } else {
            return String.format("QName{%s}", localName);
        }
    }
}
