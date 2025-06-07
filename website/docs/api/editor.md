---
sidebar_position: 1
---

# Editor API

The `Editor` class is the main entry point for DomTrip. It provides high-level operations for loading, editing, and serializing XML documents while preserving formatting.

## Constructor

### `Editor()`

Creates a new editor with default configuration.

```java
Editor editor = new Editor();
```

### `Editor(DomTripConfig config)`

Creates a new editor with custom configuration.

```java
DomTripConfig config = DomTripConfig.prettyPrint();
Editor editor = new Editor(config);
```

### `Editor(String xml)`

Creates a new editor and loads XML content.

```java
String xml = """
    <?xml version="1.0"?>
    <root>
        <child>value</child>
    </root>
    """;
Editor editor = new Editor(xml);
```

### `Editor(String xml, DomTripConfig config)`

Creates a new editor with custom configuration and loads XML content.

```java
Editor editor = new Editor(xml, DomTripConfig.strict());
```

## Document Management

### `loadXml(String xml)`

Loads XML content into the editor.

```java
editor.loadXml(xmlString);
```

**Throws:** `ParseException` if the XML is malformed.

### `getDocument()`

Gets the current XML document.

```java
Document document = editor.getDocument();
```

### `getRootElement()`

Gets the root element of the document.

```java
Element root = editor.getRootElement();
```

### `createDocument(String rootElementName)`

Creates a new document with the specified root element.

```java
editor.createDocument("project");
Element root = editor.getRootElement(); // <project></project>
```

## Serialization

### `toXml()`

Serializes the document to XML string with preserved formatting.

```java
String xml = editor.toXml();
```

### `toXml(DomTripConfig config)`

Serializes with custom configuration.

```java
String prettyXml = editor.toXml(DomTripConfig.prettyPrint());
String minimalXml = editor.toXml(DomTripConfig.minimal());
```

### `toXmlPretty()`

Serializes with pretty printing enabled.

```java
String prettyXml = editor.toXmlPretty();
```

## Element Operations

### `findElement(String name)`

Finds the first element with the specified name in the document.

```java
Element version = editor.findElement("version");
```

**Returns:** The element, or `null` if not found.

### `findElements(String name)`

Finds all elements with the specified name.

```java
List<Element> dependencies = editor.findElements("dependency");
```

### `addElement(Element parent, String name)`

Adds a new child element to the parent.

```java
Element parent = editor.getRootElement();
Element child = editor.addElement(parent, "newChild");
```

**Returns:** The newly created element.

### `addElement(Element parent, String name, String textContent)`

Adds a new child element with text content.

```java
Element version = editor.addElement(parent, "version", "1.0.0");
```

### `addElements(Element parent, Map<String, String> nameValuePairs)`

Batch operation to add multiple child elements.

```java
Map<String, String> properties = Map.of(
    "groupId", "com.example",
    "artifactId", "my-app",
    "version", "1.0.0"
);
editor.addElements(parent, properties);
```

### `removeElement(Element element)`

Removes an element from its parent.

```java
Element toRemove = editor.findElement("deprecated");
editor.removeElement(toRemove);
```

## Text Content Operations

### `setTextContent(Element element, String content)`

Sets the text content of an element.

```java
Element version = editor.findElement("version");
editor.setTextContent(version, "2.0.0");
```

### `getTextContent(Element element)`

Gets the text content of an element.

```java
String content = editor.getTextContent(element);
```

## Attribute Operations

### `setAttribute(Element element, String name, String value)`

Sets an attribute value.

```java
editor.setAttribute(element, "scope", "test");
```

### `getAttribute(Element element, String name)`

Gets an attribute value.

```java
String scope = editor.getAttribute(element, "scope");
```

### `removeAttribute(Element element, String name)`

Removes an attribute.

```java
editor.removeAttribute(element, "deprecated");
```

### `setAttributes(Element element, Map<String, String> attributes)`

Sets multiple attributes at once.

```java
Map<String, String> attrs = Map.of(
    "scope", "test",
    "optional", "true"
);
editor.setAttributes(element, attrs);
```

## Comment Operations

### `addComment(Element parent, String content)`

Adds a comment as a child of the parent element.

```java
editor.addComment(parent, " This is a comment ");
```

### `addCommentBefore(Element element, String content)`

Adds a comment before the specified element.

```java
editor.addCommentBefore(element, " Configuration section ");
```

### `addCommentAfter(Element element, String content)`

Adds a comment after the specified element.

```java
editor.addCommentAfter(element, " End of configuration ");
```

## Fluent Builder API

### `add()`

Creates a fluent builder for adding nodes.

```java
editor.add()
    .element("dependency")
    .to(parent)
    .withAttribute("scope", "test")
    .withText("content")
    .build();

editor.add()
    .comment()
    .to(parent)
    .withContent(" This is a comment ")
    .build();
```

## Configuration

### `getConfig()`

Gets the configuration used by this editor.

```java
DomTripConfig config = editor.getConfig();
```

## Exception Handling

The Editor class throws specific exceptions for different error conditions:

- **`ParseException`**: Thrown when XML parsing fails
- **`InvalidXmlException`**: Thrown for invalid editing operations
- **`DomTripException`**: Base exception for other DomTrip errors

```java
try {
    Editor editor = new Editor(malformedXml);
    // ... editing operations
} catch (ParseException e) {
    logger.error("XML parsing failed: {}", e.getMessage());
} catch (InvalidXmlException e) {
    logger.error("Invalid operation: {}", e.getMessage());
}
```

## Best Practices

### 1. Check for Null Returns

```java
// ✅ Safe navigation
Element element = editor.findElement("optional");
if (element != null) {
    editor.setTextContent(element, "value");
}

// ✅ Or use Optional-based navigation on Element
Optional<Element> optional = root.findChild("optional");
optional.ifPresent(el -> editor.setTextContent(el, "value"));
```

### 2. Use Batch Operations

```java
// ✅ Efficient batch operation
Map<String, String> properties = Map.of(
    "groupId", "com.example",
    "artifactId", "my-app"
);
editor.addElements(parent, properties);

// ❌ Less efficient individual operations
editor.addElement(parent, "groupId", "com.example");
editor.addElement(parent, "artifactId", "my-app");
```

### 3. Handle Exceptions Appropriately

```java
// ✅ Specific exception handling
try {
    editor.loadXml(xmlContent);
} catch (ParseException e) {
    // Handle parsing errors specifically
    showUserFriendlyError("Invalid XML format: " + e.getMessage());
}
```

## Thread Safety

The `Editor` class is **not thread-safe**. If you need to use an editor instance across multiple threads, you must provide external synchronization.

```java
// ✅ Thread-safe usage
synchronized (editor) {
    editor.addElement(parent, "child", "value");
    String result = editor.toXml();
}

// ✅ Or use separate editor instances per thread
Editor editorForThread = new Editor(xml);
```
