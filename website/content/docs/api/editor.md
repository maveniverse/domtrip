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
{cdi:snippets.snippet('basic-constructors')}
```

### `Editor(DomTripConfig config)`

Creates a new editor with custom configuration.

```java
{cdi:snippets.snippet('basic-constructors')}
```

### `Editor(Document document)`

Creates a new editor with an existing Document object.

```java
{cdi:snippets.snippet('loading-xml-string')}
```

### `Editor(Document document, DomTripConfig config)`

Creates a new editor with an existing Document and custom configuration.

```java
{cdi:snippets.snippet('basic-constructors')}
```

**Throws:** `IllegalArgumentException` if document is null.

## Advanced Constructor Examples

```java
{cdi:snippets.snippet('advanced-constructor-examples')}
```

## Document Management

### `document()`

Gets the current XML document.

```java
{cdi:snippets.snippet('basic-operations')}
```

### `root()`

Gets the root element of the document.

```java
{cdi:snippets.snippet('basic-operations')}
```

### `createDocument(String rootElementName)`

Creates a new document with the specified root element.

```java
{cdi:snippets.snippet('basic-operations')}
```

## Serialization

### `toXml()`

Serializes the document to XML string with preserved formatting.

```java
{cdi:snippets.snippet('basic-operations')}
```

### `toXml(DomTripConfig config)`

Serializes with custom configuration.

```java
{cdi:snippets.snippet('serialization-options')}
```

### Pretty Printing

For pretty printing, use the configuration approach:

```java
{cdi:snippets.snippet('basic-operations')}
```

## Element Operations

### `findElement(String name)`

Finds the first element with the specified name in the document.

```java
{cdi:snippets.snippet('element-operations')}
```

**Returns:** The element, or `null` if not found.

### `findElements(String name)`

Finds all elements with the specified name.

```java
{cdi:snippets.snippet('element-operations')}
```

### `addElement(Element parent, String name)`

Adds a new child element to the parent.

```java
{cdi:snippets.snippet('element-operations')}
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
{cdi:snippets.snippet('element-operations')}
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
{cdi:snippets.snippet('element-operations')}
```

## Attribute Operations

### `setAttribute(Element element, String name, String value)`

Sets an attribute value with intelligent formatting preservation and inference.

**Formatting Behavior:**
- **Existing attributes**: Preserves original quote style and whitespace
- **New attributes**: Infers formatting from existing attributes on the element

```java
{cdi:snippets.snippet('attribute-management')}
```

### `getAttribute(Element element, String name)`

Gets an attribute value.

```java
{cdi:snippets.snippet('attribute-operations')}
```

### `removeAttribute(Element element, String name)`

Removes an attribute.

```java
{cdi:snippets.snippet('attribute-management')}
```

### `setAttributes(Element element, Map<String, String> attributes)`

Sets multiple attributes at once with intelligent formatting.

```java
{cdi:snippets.snippet('attribute-management')}
```

**Advanced Attribute Formatting:**

For fine-grained control over attribute formatting, you can work with `Attribute` objects directly:

```java
{cdi:snippets.snippet('attribute-management')}
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
{cdi:snippets.snippet('comment-management')}
```

### `addCommentAfter(Element element, String content)`

Adds a comment after the specified element.

```java
{cdi:snippets.snippet('comment-management')}
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
{cdi:snippets.snippet('specific-exception-handling')}
```

## Thread Safety

The `Editor` class is **not thread-safe**. If you need to use an editor instance across multiple threads, you must provide external synchronization.

```java
{cdi:snippets.snippet('thread-safety-pattern')}
```
