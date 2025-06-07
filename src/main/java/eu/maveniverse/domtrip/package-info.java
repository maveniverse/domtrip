/**
 * DomTrip - A lossless XML processing library for Java.
 *
 * <p>This package provides a comprehensive XML processing library that preserves
 * all formatting information including whitespace, comments, processing instructions,
 * attribute quote styles, and entity references during round-trip parsing and serialization.</p>
 *
 * <h2>Key Features</h2>
 * <ul>
 *   <li><strong>Lossless Processing</strong> - Preserves exact formatting for unmodified content</li>
 *   <li><strong>Attribute Quote Preservation</strong> - Maintains single vs double quotes</li>
 *   <li><strong>Whitespace Preservation</strong> - Keeps original indentation and spacing</li>
 *   <li><strong>Entity Preservation</strong> - Maintains entity references in original form</li>
 *   <li><strong>Comment Preservation</strong> - Preserves XML comments and processing instructions</li>
 *   <li><strong>Namespace Support</strong> - Comprehensive namespace handling with resolution and context management</li>
 *   <li><strong>Builder Patterns</strong> - Fluent APIs for creating XML structures</li>
 *   <li><strong>Type Safety</strong> - Enums for quote styles and whitespace patterns</li>
 * </ul>
 *
 * <h2>Type-Safe Node Hierarchy</h2>
 * <ul>
 *   <li>{@link eu.maveniverse.domtrip.Node} - Base class for all XML nodes</li>
 *   <li>{@link eu.maveniverse.domtrip.ContainerNode} - Abstract base for nodes that can contain children</li>
 *   <li>{@link eu.maveniverse.domtrip.Document} - Root XML document (extends ContainerNode)</li>
 *   <li>{@link eu.maveniverse.domtrip.Element} - XML elements with attributes and namespace support (extends ContainerNode)</li>
 *   <li>{@link eu.maveniverse.domtrip.Text} - Text content nodes (leaf node)</li>
 *   <li>{@link eu.maveniverse.domtrip.Comment} - XML comment nodes (leaf node)</li>
 *   <li>{@link eu.maveniverse.domtrip.ProcessingInstruction} - Processing instruction nodes (leaf node)</li>
 * </ul>
 *
 * <h2>Core Services</h2>
 * <ul>
 *   <li>{@link eu.maveniverse.domtrip.Editor} - High-level API for XML editing</li>
 *   <li>{@link eu.maveniverse.domtrip.NamespaceContext} - Namespace resolution and context management</li>
 *   <li>{@link eu.maveniverse.domtrip.NamespaceResolver} - Namespace utility methods</li>
 *   <li>{@link eu.maveniverse.domtrip.DomTripConfig} - Configuration options</li>
 *   <li>{@link eu.maveniverse.domtrip.Serializer} - XML serialization</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Parse XML while preserving formatting
 * Editor editor = new Editor(xmlString);
 *
 * // Make modifications
 * Element root = editor.getRootElement();
 * editor.addElement(root, "newChild", "content");
 *
 * // Serialize with preserved formatting
 * String result = editor.toXml();
 *
 * // Use configuration for different output styles
 * String prettyXml = editor.toXml(DomTripConfig.prettyPrint());
 * String minimalXml = editor.toXml(DomTripConfig.minimal());
 * }</pre>
 *
 * <h2>Configuration</h2>
 * <p>Use {@link eu.maveniverse.domtrip.DomTripConfig} to control parsing and serialization behavior:</p>
 * <pre>{@code
 * DomTripConfig config = DomTripConfig.defaults()
 *     .withWhitespacePreservation(true)
 *     .withCommentPreservation(true)
 *     .withPrettyPrint(false);
 *
 * Editor editor = new Editor(xmlString, config);
 * }</pre>
 *
 * <h2>Namespace Support</h2>
 * <p>DomTrip provides comprehensive namespace handling:</p>
 * <pre>{@code
 * // Create elements with namespaces
 * Element soapEnvelope = Elements.namespacedElement("soap", "Envelope",
 *     "http://schemas.xmlsoap.org/soap/envelope/");
 *
 * // Namespace-aware navigation
 * Optional<Element> body = root.findChildByNamespace(
 *     "http://schemas.xmlsoap.org/soap/envelope/", "Body");
 *
 * // Namespace resolution
 * String namespaceURI = element.getNamespaceURI();
 * String localName = element.getLocalName();
 * NamespaceContext context = element.getNamespaceContext();
 * }</pre>
 *
 * <h2>Architectural Benefits</h2>
 * <p>The type-safe node hierarchy provides several key benefits:</p>
 * <ul>
 *   <li><strong>Memory Efficiency</strong> - Leaf nodes don't waste memory on unused children lists</li>
 *   <li><strong>Compile-Time Safety</strong> - Impossible to add children to leaf nodes</li>
 *   <li><strong>Clear API</strong> - Child management methods only exist where they make sense</li>
 *   <li><strong>Element-Specific Navigation</strong> - Element finding methods are only on Element class</li>
 *   <li><strong>Industry Alignment</strong> - Follows patterns from W3C DOM and DOM4J</li>
 * </ul>
 *
 * @author DomTrip Development Team
 * @version 1.0
 * @since 1.0
 */
package eu.maveniverse.domtrip;
