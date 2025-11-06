---
title: "Document API"
description: "Complete reference for the Document class and XML document management"
weight: 30
layout: page
---

# Document API

The Document class represents the root of an XML document, containing the document element and preserving document-level formatting like XML declarations and DTDs.

## Overview

The Document class serves as the top-level container for an XML document, maintaining:

- **Document element** (root element)
- **XML declaration** with version, encoding, and standalone flag
- **DOCTYPE declarations** 
- **Processing instructions** at document level
- **Comments** before and after the root element
- **Whitespace** preservation between top-level nodes

## Creating Documents

### Factory Methods

```java
{cdi:snippets.snippet('document-creation')}
```

### Fluent API

```java
{cdi:snippets.snippet('fluent-api')}
```

## Document Properties

### XML Declaration

```java
{cdi:snippets.snippet('xml-declaration')}
```

### Encoding Management

```java
{cdi:snippets.snippet('encoding-management')}
```

### Version Control

```java
{cdi:snippets.snippet('version-control')}
```

## Root Element Management

### Setting Root Element

```java
{cdi:snippets.snippet('root-element-management')}
```

### Root Element with Namespaces

```java
{cdi:snippets.snippet('root-element-namespaces')}
```

## Document Structure

### Adding Top-Level Nodes

```java
{cdi:snippets.snippet('adding-top-level-nodes')}
```

### Document Traversal

```java
{cdi:snippets.snippet('document-traversal')}
```

## DOCTYPE Support

### Setting DOCTYPE

```java
{cdi:snippets.snippet('doctype-support')}
```

### DOCTYPE Preservation

```java
{cdi:snippets.snippet('doctype-preservation')}
```

## Document Statistics

### Node Counting

```java
{cdi:snippets.snippet('node-counting')}
```

## Serialization

### Basic Serialization

```java
{cdi:snippets.snippet('basic-serialization')}
```

### Custom Serialization

```java
{cdi:snippets.snippet('custom-serialization')}
```

## Advanced Features

### Document Cloning

```java
{cdi:snippets.snippet('document-cloning')}
```

### Document Validation

```java
{cdi:snippets.snippet('document-validation')}
```

### Memory Management

```java
// For large documents, consider memory usage
Document largeDoc = Document.of(largeXmlFile);

// Process in sections if needed
Element root = largeDoc.root();
// ... process specific elements

// Clear references when done
largeDoc = null; // Allow garbage collection
```

## Integration with Editor

The Document class works seamlessly with the Editor API:

```java
{cdi:snippets.snippet('editor-integration')}
```

## Best Practices

### ✅ **Do:**
- Use factory methods for document creation
- Set encoding explicitly for non-UTF-8 documents
- Preserve XML declarations when round-tripping
- Use fluent API for complex document setup
- Handle null checks for optional elements

### ❌ **Avoid:**
- Creating documents without root elements
- Modifying document structure directly (use Editor instead)
- Ignoring encoding when parsing from streams
- Setting invalid XML version numbers
- Creating malformed DOCTYPE declarations

## Error Handling

```java
{cdi:snippets.snippet('document-error-handling')}
```

## Performance Considerations

- **Lazy loading** - Document content is parsed on demand
- **Memory efficient** - Only modified nodes are tracked
- **Streaming friendly** - Large documents can be processed efficiently
- **Minimal overhead** - Document metadata has negligible memory impact

The Document API provides the foundation for all XML processing in DomTrip, offering both simplicity for basic use cases and power for complex document manipulation scenarios.
