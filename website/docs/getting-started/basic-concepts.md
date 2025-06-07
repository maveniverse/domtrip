---
sidebar_position: 3
---

# Basic Concepts

Understanding DomTrip's core concepts will help you use the library effectively. This guide covers the fundamental ideas behind DomTrip's design and how they differ from traditional XML libraries.

## The Lossless Philosophy

Traditional XML libraries focus on **data extraction** - they parse XML to get the information you need, often discarding formatting details in the process. DomTrip takes a different approach: **preservation first**.

```java
// Traditional approach (data-focused)
Document doc = parser.parse(xml);
String value = doc.selectSingleNode("//version").getText();
// Formatting is lost when you serialize back

// DomTrip approach (preservation-focused)  
Editor editor = new Editor(xml);
Element version = editor.findElement("version");
String value = version.getTextContent();
String result = editor.toXml(); // Identical to original if unchanged
```

## Node Hierarchy

DomTrip uses a clean, type-safe node hierarchy that reflects XML structure:

```
Node (abstract base)
├── ContainerNode (abstract)
│   ├── Document (root container)
│   └── Element (XML elements)
└── Leaf Nodes
    ├── Text (text content, CDATA)
    ├── Comment (XML comments)
    └── ProcessingInstruction (PIs)
```

### Why This Design?

1. **Memory Efficiency**: Leaf nodes don't waste memory on unused children collections
2. **Type Safety**: Impossible to add children to text nodes at compile time
3. **Clear API**: Child management methods only exist where they make sense

```java
// ✅ This works - Element can have children
Element parent = new Element("parent");
parent.addChild(new Text("content"));

// ❌ This won't compile - Text cannot have children
Text text = new Text("content");
text.addChild(new Element("child")); // Compilation error
```

## Modification Tracking

Every node tracks whether it has been modified since parsing. This enables **minimal-change serialization**:

```java
Editor editor = new Editor(originalXml);

// Unmodified nodes use original formatting
Element unchanged = editor.findElement("unchanged");
unchanged.isModified(); // false

// Modified nodes are rebuilt with inferred formatting
Element changed = editor.findElement("version");
editor.setTextContent(changed, "2.0.0");
changed.isModified(); // true

// Only modified sections are reformatted in output
String result = editor.toXml();
```

## Dual Content Storage

Text nodes store content in two forms:

1. **Decoded Content**: For your application logic
2. **Raw Content**: For preservation during serialization

```java
// Original XML: <message>Hello &amp; goodbye</message>
Text textNode = element.getTextNode();

// For your code
String decoded = textNode.getTextContent(); // "Hello & goodbye"

// For serialization  
String raw = textNode.getRawContent(); // "Hello &amp; goodbye"
```

This allows you to work with normal strings while preserving entity encoding.

## Attribute Handling

Attributes are first-class objects that preserve formatting details:

```java
public class Attribute {
    private String value;           // "test-value"
    private QuoteStyle quoteStyle;  // SINGLE or DOUBLE  
    private String whitespace;      // " " (space before attribute)
    private String rawValue;        // "test-value" (with entities preserved)
}

// Usage
Element element = editor.findElement("dependency");
Attribute scope = element.getAttributeObject("scope");

scope.getValue();      // "test"
scope.getQuoteStyle(); // QuoteStyle.SINGLE
scope.getWhitespace(); // " "
```

## Whitespace Management

DomTrip tracks whitespace at multiple levels:

### 1. Node-Level Whitespace

```java
public abstract class Node {
    protected String precedingWhitespace;  // Before the node
    protected String followingWhitespace;  // After the node
}
```

### 2. Element-Level Whitespace

```java
public class Element extends ContainerNode {
    private String openTagWhitespace;   // Inside opening tag: <element >
    private String closeTagWhitespace;  // Inside closing tag: </ element>
}
```

### 3. Intelligent Inference

For new content, DomTrip infers formatting from surrounding context:

```java
// Existing structure:
//   <dependencies>
//       <dependency>...</dependency>
//   </dependencies>

// Adding new dependency automatically infers indentation
Element dependencies = editor.findElement("dependencies");
Element newDep = editor.addElement(dependencies, "dependency");
// Result uses same indentation as existing dependencies
```

## Configuration System

DomTrip behavior is controlled through `DomTripConfig`:

```java
// Preset configurations
DomTripConfig strict = DomTripConfig.strict();        // Maximum preservation
DomTripConfig lenient = DomTripConfig.lenient();      // Flexible formatting
DomTripConfig pretty = DomTripConfig.prettyPrint();   // Clean output

// Custom configuration
DomTripConfig custom = DomTripConfig.defaults()
    .withIndentation("  ")                    // 2 spaces
    .withPreserveWhitespace(true)            // Keep original whitespace
    .withPreserveComments(true)              // Keep comments
    .withQuoteStyle(QuoteStyle.DOUBLE);      // Prefer double quotes
```

## Navigation Patterns

DomTrip provides multiple ways to navigate XML structures:

### 1. Traditional Navigation

```java
Element root = editor.getRootElement();
Element child = root.getChild("child-name");
List<Element> children = root.getChildren("item");
```

### 2. Optional-Based Navigation

```java
Optional<Element> child = root.findChild("child-name");
child.ifPresent(element -> {
    // Safe navigation - no null checks needed
    String value = element.getTextContent();
});
```

### 3. Stream-Based Navigation

```java
// Find all active dependencies
root.findChildren("dependency")
    .filter(dep -> "active".equals(dep.getAttribute("status")))
    .map(dep -> dep.findChild("artifactId").orElse(null))
    .filter(Objects::nonNull)
    .map(Element::getTextContent)
    .forEach(System.out::println);
```

### 4. Namespace-Aware Navigation

```java
// Find elements by namespace URI and local name
Optional<Element> soapBody = root.findChildByNamespace(
    "http://schemas.xmlsoap.org/soap/envelope/", "Body");

Stream<Element> allItems = root.descendantsByNamespace(
    "http://example.com/items", "item");
```

## Error Handling

DomTrip uses specific exception types for better error handling:

```java
try {
    Editor editor = new Editor(xmlString);
    // ... editing operations
} catch (ParseException e) {
    // XML parsing failed
    logger.error("Invalid XML: {}", e.getMessage());
} catch (InvalidXmlException e) {
    // Invalid editing operation
    logger.error("Invalid operation: {}", e.getMessage());
} catch (DomTripException e) {
    // General DomTrip error
    logger.error("DomTrip error: {}", e.getMessage());
}
```

## Builder Patterns

DomTrip provides fluent builders for creating complex structures:

### Element Builder

```java
Element dependency = Elements.builder("dependency")
    .withAttribute("scope", "test")
    .withChild(Elements.textElement("groupId", "junit"))
    .withChild(Elements.textElement("artifactId", "junit"))
    .withChild(Elements.textElement("version", "4.13.2"))
    .build();
```

### Document Builder

```java
Document doc = Documents.builder()
    .withVersion("1.0")
    .withEncoding("UTF-8")
    .withRootElement("project")
    .withXmlDeclaration()
    .build();
```

### Editor Builder

```java
editor.add()
    .element("dependency")
    .to(parent)
    .withAttribute("scope", "test")
    .withText("content")
    .build();
```

## Performance Characteristics

Understanding DomTrip's performance profile helps you use it effectively:

### Memory Usage
- **Base overhead**: ~30% more than traditional parsers
- **Scales linearly** with document size
- **Efficient for editing**: Only modified sections use extra memory

### Processing Speed
- **Parsing**: ~15% slower (due to metadata collection)
- **Navigation**: Same speed as traditional DOM
- **Serialization**: Faster for unmodified sections

### Best Use Cases
- ✅ **Configuration file editing**
- ✅ **Document transformation with formatting preservation**
- ✅ **XML editing tools and IDEs**
- ❌ **High-throughput data processing** (use Jackson XML)
- ❌ **Simple data extraction** (use traditional parsers)

## Next Steps

Now that you understand the core concepts, explore specific features:

- 🔄 [Lossless Parsing](../features/lossless-parsing) - Deep dive into preservation
- 📝 [Formatting Preservation](../features/formatting-preservation) - How formatting is maintained
- 🌐 [Namespace Support](../features/namespace-support) - Working with XML namespaces
- 🏗️ [Builder Patterns](../advanced/builder-patterns) - Creating complex XML structures
