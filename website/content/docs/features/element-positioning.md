---
title: "Element Positioning"
description: "Insert XML elements at specific positions with precise control"
weight: 60
---

# Element Positioning

DomTrip's Editor provides precise control over element positioning, allowing you to insert elements at specific locations within the XML document structure.

## Overview

The positioning features allow you to:

- **Insert at specific index** - Place elements at exact positions within a parent
- **Insert before elements** - Add elements immediately before existing elements
- **Insert after elements** - Add elements immediately after existing elements
- **Maintain formatting** - Preserve indentation and whitespace patterns

## Basic Usage

### Insert at Specific Position

Use `insertElementAt()` to insert an element at a specific index:

```java
{cdi:snippets.snippet('insert-element-at')}
```

### Insert Before Element

Use `insertElementBefore()` to insert an element before an existing element:

```java
{cdi:snippets.snippet('insert-element-before')}
```

### Insert After Element

Use `insertElementAfter()` to insert an element after an existing element:

```java
{cdi:snippets.snippet('insert-element-after')}
```

## Advanced Usage

### Insert with Text Content

All positioning methods support optional text content:

```java
// Insert at position with text content
Element element = editor.insertElementAt(parent, 2, "description", "Project description");

// Insert before with text content
Element name = editor.insertElementBefore(existing, "name", "My Project");

// Insert after with text content
Element url = editor.insertElementAfter(existing, "url", "https://example.com");
```

### Complex Positioning Scenarios

Handle complex document structures with multiple insertions:

```java
Document doc = Document.of("""
    <project>
        <modelVersion>4.0.0</modelVersion>
        <dependencies>
            <dependency>
                <groupId>junit</groupId>
                <artifactId>junit</artifactId>
            </dependency>
        </dependencies>
    </project>
    """);

Editor editor = new Editor(doc);
Element modelVersion = doc.root().child("modelVersion").orElseThrow();
Element dependencies = doc.root().child("dependencies").orElseThrow();
Element junit = dependencies.child("dependency").orElseThrow();

// Insert project coordinates after modelVersion
editor.insertElementAfter(modelVersion, "groupId", "com.example");
editor.insertElementAfter(doc.root().child("groupId").orElseThrow(), "artifactId", "my-app");
editor.insertElementAfter(doc.root().child("artifactId").orElseThrow(), "version", "1.0.0");

// Insert additional dependency before junit
Element mockito = editor.insertElementBefore(junit, "dependency");
editor.addElement(mockito, "groupId", "org.mockito");
editor.addElement(mockito, "artifactId", "mockito-core");

// Insert build section after dependencies
Element build = editor.insertElementAfter(dependencies, "build");
Element plugins = editor.addElement(build, "plugins");
```

### Working with Indices

Understand how indices work with XML nodes:

```java
Element parent = doc.root();

// Index 0: insert at the beginning
Element first = editor.insertElementAt(parent, 0, "first");

// Index parent.nodeCount(): append at the end
Element last = editor.insertElementAt(parent, parent.nodeCount(), "last");

// Indices account for all nodes (elements, text, comments)
// Use carefully when whitespace nodes are present
```

## Method Reference

### `insertElementAt(Element parent, int index, String elementName)`

Inserts a new element at the specified position within the parent.

**Parameters:**
- `parent` - The parent element to insert into
- `index` - The position to insert at (0-based)
- `elementName` - The name of the new element

**Returns:**
- `Element` - The newly created element

**Throws:**
- `DomTripException` - If the insertion fails or index is out of bounds

### `insertElementAt(Element parent, int index, String elementName, String textContent)`

Inserts a new element with text content at the specified position.

**Parameters:**
- `parent` - The parent element to insert into
- `index` - The position to insert at (0-based)
- `elementName` - The name of the new element
- `textContent` - The text content for the element

**Returns:**
- `Element` - The newly created element

### `insertElementBefore(Element referenceElement, String elementName)`

Inserts a new element before the specified reference element.

**Parameters:**
- `referenceElement` - The element to insert before
- `elementName` - The name of the new element

**Returns:**
- `Element` - The newly created element

**Throws:**
- `DomTripException` - If the reference element has no parent

### `insertElementBefore(Element referenceElement, String elementName, String textContent)`

Inserts a new element with text content before the reference element.

### `insertElementAfter(Element referenceElement, String elementName)`

Inserts a new element after the specified reference element.

**Parameters:**
- `referenceElement` - The element to insert after
- `elementName` - The name of the new element

**Returns:**
- `Element` - The newly created element

### `insertElementAfter(Element referenceElement, String elementName, String textContent)`

Inserts a new element with text content after the reference element.

## Error Handling

The positioning methods include comprehensive validation:

```java
// Invalid index bounds
assertThrows(DomTripException.class, () -> {
    editor.insertElementAt(parent, -1, "invalid");  // Negative index
});

assertThrows(DomTripException.class, () -> {
    editor.insertElementAt(parent, parent.nodeCount() + 1, "invalid");  // Too large
});

// Null or empty element names
assertThrows(DomTripException.class, () -> {
    editor.insertElementAt(parent, 0, null);
});

assertThrows(DomTripException.class, () -> {
    editor.insertElementBefore(reference, "");
});

// Reference element without parent
Element orphan = new Element("orphan");
assertThrows(DomTripException.class, () -> {
    editor.insertElementBefore(orphan, "newElement");
});
```

## Whitespace and Formatting

Positioning methods automatically handle whitespace and indentation:

```java
// The editor infers appropriate indentation from the parent
Element newElement = editor.insertElementAt(parent, 1, "child");

// Whitespace is automatically applied based on surrounding elements
String indentation = editor.whitespaceManager().inferIndentation(parent);
// newElement will have appropriate preceding whitespace
```

## Best Practices

1. **Use semantic positioning** - Choose the method that best expresses your intent
2. **Consider document structure** - Understand the logical flow of your XML
3. **Handle edge cases** - Always validate indices and reference elements
4. **Combine with other features** - Use positioning with element creation and modification
5. **Test thoroughly** - Verify element order in complex scenarios

## Integration Examples

### Building a Maven POM

```java
Editor editor = new Editor();
editor.createDocument("project");
Element project = editor.root();

// Add basic coordinates
editor.addElement(project, "modelVersion", "4.0.0");
editor.addElement(project, "groupId", "com.example");
editor.addElement(project, "artifactId", "my-app");
editor.addElement(project, "version", "1.0.0");

// Insert packaging after version
Element version = project.child("version").orElseThrow();
editor.insertElementAfter(version, "packaging", "jar");

// Add dependencies section
Element dependencies = editor.addElement(project, "dependencies");
Element junit = editor.addElement(dependencies, "dependency");
editor.addElement(junit, "groupId", "junit");
editor.addElement(junit, "artifactId", "junit");
editor.addElement(junit, "scope", "test");

// Insert mockito before junit
Element mockito = editor.insertElementBefore(junit, "dependency");
editor.addElement(mockito, "groupId", "org.mockito");
editor.addElement(mockito, "artifactId", "mockito-core");
editor.addElement(mockito, "scope", "test");
```

### Reordering Elements

```java
// Move an element to a different position
Element elementToMove = parent.child("target").orElseThrow();
Element referenceElement = parent.child("reference").orElseThrow();

// Remove from current position
parent.removeNode(elementToMove);

// Insert at new position
editor.insertElementBefore(referenceElement, elementToMove.name(), elementToMove.textContent());
```
