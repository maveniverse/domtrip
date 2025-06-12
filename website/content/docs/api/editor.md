---
title: Editor API
description: The Editor class is the main entry point for DomTrip. It provides high-level operations for loading, editing, and serializing XML documents while preserving formatting.
layout: page
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

### `Editor(Document document)`

Creates a new editor with an existing Document object.

```java
{cdi:snippets.snippet('loading-xml-string')}
```

### `Editor(Document document, DomTripConfig config)`

Creates a new editor with an existing Document and custom configuration.

```java
Document doc = Document.of(xml);
Editor editor = new Editor(doc, DomTripConfig.strict());
```

**Throws:** `IllegalArgumentException` if document is null.

## Advanced Constructor Examples

```java
// Working with an existing document
Document existingDoc = Document.of(xmlString);
Editor editor = new Editor(existingDoc);

// Working with a programmatically created document
Document doc = Document.withRootElement("project");
Editor editor = new Editor(doc);

// Working with existing document and custom config
Document existingDoc = Document.of(xmlString);
DomTripConfig config = DomTripConfig.prettyPrint()
    .withIndentString("  ")
    .withPreserveComments(true);
Editor editor = new Editor(existingDoc, config);

// Working with builder-created document
Document doc = Document.withRootElement("maven");
Editor editor = new Editor(doc, DomTripConfig.minimal());
```

## Document Management

### `document()`

Gets the current XML document.

```java
Document document = editor.document();
```

### `root()`

Gets the root element of the document.

```java
Element root = editor.root();
```

### `createDocument(String rootElementName)`

Creates a new document with the specified root element.

```java
editor.createDocument("project");
Element root = editor.root(); // <project></project>
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
{cdi:snippets.snippet('serialization-options')}
```

### Pretty Printing

For pretty printing, use the configuration approach:

```java
String prettyXml = editor.toXml(DomTripConfig.prettyPrint());
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
Element parent = editor.root();
Element child = editor.addElement(parent, "newChild");
```

**Returns:** The newly created element.

### `addElement(Element parent, String name, String textContent)`

Adds a new child element with text content.

```java
{cdi:snippets.snippet('element-addition')}
```

### `addElements(Element parent, Map<String, String> nameValuePairs)`

Batch operation to add multiple child elements.

```java
{cdi:snippets.snippet('batch-element-creation')}
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
{cdi:snippets.snippet('text-content-operations')}
```

### `getTextContent(Element element)`

Gets the text content of an element.

```java
String content = editor.getTextContent(element);
```

## Attribute Operations

### `setAttribute(Element element, String name, String value)`

Sets an attribute value with intelligent formatting preservation and inference.

**Formatting Behavior:**
- **Existing attributes**: Preserves original quote style and whitespace
- **New attributes**: Infers formatting from existing attributes on the element

```java
// For XML: <element attr1='existing' attr2="another"/>
editor.setAttribute(element, "attr1", "updated");  // Preserves single quotes
editor.setAttribute(element, "attr3", "new");      // Infers quote style from existing

// For multi-line attributes:
// <element attr1="value1"
//          attr2="value2"/>
editor.setAttribute(element, "attr3", "value3");   // Maintains alignment
```

### `getAttribute(Element element, String name)`

Gets an attribute value.

```java
{cdi:snippets.snippet('attribute-operations')}
```

### `removeAttribute(Element element, String name)`

Removes an attribute.

```java
editor.removeAttribute(element, "deprecated");
```

### `setAttributes(Element element, Map<String, String> attributes)`

Sets multiple attributes at once with intelligent formatting.

```java
Map<String, String> attrs = Map.of(
    "scope", "test",
    "optional", "true"
);
editor.setAttributes(element, attrs);
// Each attribute uses inferred formatting based on existing patterns
```

**Advanced Attribute Formatting:**

For fine-grained control over attribute formatting, you can work with `Attribute` objects directly:

```java
// Get attribute object for advanced manipulation
Attribute attr = element.getAttributeObject("combine.children");
if (attr != null) {
    attr.setValue("merge");  // Preserves all formatting
}

// Create custom formatted attribute
Attribute customAttr = Attribute.builder()
    .name("newAttr")
    .value("value")
    .quoteStyle(QuoteStyle.SINGLE)
    .precedingWhitespace("\n         ")  // For alignment
    .build();
element.setAttributeObject("newAttr", customAttr);
```

## Comment Operations

### `addComment(Element parent, String content)`

Adds a comment as a child of the parent element.

```java
{cdi:snippets.snippet('comment-operations')}
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
{cdi:snippets.snippet('fluent-builder-api')}
```

## Configuration

### `config()`

Gets the configuration used by this editor.

```java
{cdi:snippets.snippet('configuration-access')}
```

## Exception Handling

The Editor class throws specific exceptions for different error conditions:

- **`ParseException`**: Thrown when XML parsing fails
- **`InvalidXmlException`**: Thrown for invalid editing operations
- **`DomTripException`**: Base exception for other DomTrip errors

```java
{cdi:snippets.snippet('exception-handling')}
```

## Best Practices

### 1. Check for Null Returns

```java
{cdi:snippets.snippet('safe-navigation')}
```

### 2. Use Batch Operations

```java
{cdi:snippets.snippet('best-practices')}
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
{cdi:snippets.snippet('thread-safety-pattern')}
```
