---
title: Factory Methods and Fluent APIs
description: DomTrip provides clean factory methods and fluent APIs that make creating and modifying XML structures intuitive and type-safe
layout: page
---

# Factory Methods and Fluent APIs

DomTrip provides clean factory methods and fluent APIs that make creating and modifying XML structures intuitive and type-safe. This page covers all the creation patterns available in DomTrip.

## Element Factory Methods

DomTrip provides convenient factory methods for creating elements:

### Basic Element Creation

```java
{cdi:snippets.snippet('basic-element-creation')}
```

### Advanced Element Creation

```java
{cdi:snippets.snippet('advanced-element-creation')}
```

### Element with Namespaces

```java
{cdi:snippets.snippet('namespaced-elements')}
```

## Document Factory Methods

DomTrip provides convenient factory methods for creating documents:

### Simple Document Creation

```java
{cdi:snippets.snippet('simple-document-creation')}
```

### File-Based Document Loading

The `Document.of(Path)` method is the recommended way to load XML files as it handles encoding detection automatically:

```java
{cdi:snippets.snippet('file-based-document-loading')}
```

**Benefits of `Document.of(Path)`:**

- **Automatic encoding detection**: Properly handles UTF-8, UTF-16, ISO-8859-1, and other encodings
- **BOM handling**: Correctly processes Byte Order Marks
- **Memory efficient**: Streams the file content rather than loading entire string into memory first
- **Error handling**: Provides better error messages for encoding and parsing issues
- **Convenience**: Single method call for file-based XML parsing

### Advanced Document Creation

```java
{cdi:snippets.snippet('advanced-document-creation')}
```

## Text and Comment Factory Methods

### Text Node Creation

```java
{cdi:snippets.snippet('text-node-creation')}
```

### Comment Creation

```java
{cdi:snippets.snippet('comment-creation')}
```

### Processing Instruction Creation

```java
{cdi:snippets.snippet('processing-instruction-creation')}
```

## Editor Fluent API

The Editor provides a fluent API for adding content to existing documents:

### Adding Elements

```java
Editor editor = new Editor(Document.of(xmlString));
Element root = editor.root();

// Fluent element addition
editor.add()
    .element("dependency")
    .to(root)
    .withAttribute("scope", "test")
    .withText("junit")
    .build();

// Add comments and processing instructions
editor.add()
    .comment()
    .to(root)
    .withContent(" Configuration section ")
    .build();

editor.add()
    .text()
    .to(element)
    .withContent("text content")
    .asCData()
    .build();
```

## Attribute Factory Methods

### Attribute Creation

```java
{cdi:snippets.snippet('attribute-creation')}
```

## Best Practices

### 1. Use Factory Methods for Simple Cases

```java
// ✅ Good - clean and direct
Element version = Element.text("version", "1.0.0");
Comment comment = Comment.of("Configuration");
Text cdata = Text.cdata("script content");

// ❌ Avoid - unnecessary complexity
Element version = new Element("version");
version.addNode(new Text("1.0.0"));
```

### 2. Chain Fluent Methods for Complex Structures

```java
{cdi:snippets.snippet('fluent-chaining')}
```

### 3. Extract Complex Structures to Methods

```java
// ✅ Good - readable and reusable
private Element createDependency(String groupId, String artifactId, String version) {
    Element dependency = Element.of("dependency");
    dependency.addNode(Element.text("groupId", groupId));
    dependency.addNode(Element.text("artifactId", artifactId));
    dependency.addNode(Element.text("version", version));
    return dependency;
}

// Usage
Element junitDep = createDependency("junit", "junit", "4.13.2");
```

### 4. Use Appropriate Factory Methods

```java
// ✅ Good - use the right tool for the job
Element selfClosing = Element.selfClosing("br");
Element withAttrs = Element.withAttributes("div", attributes);
Text cdata = Text.cdata("script content");
Document withDecl = Document.withXmlDeclaration("1.0", "UTF-8");

// ❌ Avoid - manual setup when factory exists
Element br = Element.of("br").selfClosing(true);
Document doc = Document.of().version("1.0").encoding("UTF-8").withXmlDeclaration();
```

## Performance Considerations

Factory methods and fluent APIs are optimized for both convenience and performance:

- **Memory efficient**: No intermediate builder objects created
- **Validation**: Early validation prevents invalid XML structures  
- **Direct creation**: Objects created immediately, not deferred
- **Method chaining**: Returns `this` for zero-cost fluent chaining

```java
{cdi:snippets.snippet('performance-optimizations')}
```
