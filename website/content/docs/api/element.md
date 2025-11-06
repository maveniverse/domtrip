---
title: "Element API"
description: "Complete reference for the Element class and XML element manipulation"
weight: 20
layout: page
---

# Element API

The Element class represents XML elements and provides comprehensive methods for manipulating element content, attributes, and whitespace while preserving formatting.

## Overview

The Element class is the core building block for XML documents in DomTrip, providing:

- **Element creation and manipulation**
- **Attribute management with formatting preservation**
- **Text content handling with whitespace preservation**
- **Child element navigation and modification**
- **Comprehensive whitespace control**
- **Namespace-aware operations**

## Element Creation

### Factory Methods

```java
{cdi:snippets.snippet('element-creation')}
```

### Builder Pattern

```java
{cdi:snippets.snippet('element-builder')}
```

## Attribute Management

### Basic Attribute Operations

```java
{cdi:snippets.snippet('basic-attributes')}
```

### Attribute Formatting

```java
{cdi:snippets.snippet('attribute-formatting')}
```

## Text Content

### Basic Text Operations

```java
{cdi:snippets.snippet('text-content')}
```

### Whitespace-Preserving Text

```java
{cdi:snippets.snippet('whitespace-preserving-text')}
```

## Whitespace Management

DomTrip provides fine-grained control over whitespace at multiple levels:

### Node-Level Whitespace

```java
{cdi:snippets.snippet('node-whitespace')}
```

### Element Tag Whitespace

```java
{cdi:snippets.snippet('element-tag-whitespace')}
```

### Inner Element Whitespace

For elements containing only whitespace (no child elements), DomTrip provides specialized handling:

```java
{cdi:snippets.snippet('inner-element-whitespace')}
```

## Child Element Navigation

### Finding Child Elements

```java
{cdi:snippets.snippet('child-navigation')}
```

### Element Streams

```java
{cdi:snippets.snippet('element-streams')}
```

## Namespace Support

### Namespace-Aware Operations

```java
{cdi:snippets.snippet('namespace-operations')}
```

### QName Support

```java
{cdi:snippets.snippet('qname-support')}
```

## Element Modification

### Adding Child Elements

```java
{cdi:snippets.snippet('adding-children')}
```

### Removing Elements

```java
{cdi:snippets.snippet('removing-elements')}
```

## Advanced Features

### Element Cloning

```java
{cdi:snippets.snippet('element-cloning')}
```

### Modification Tracking

```java
{cdi:snippets.snippet('modification-tracking')}
```

## Best Practices

### ✅ **Do:**
- Use `textPreservingWhitespace()` when updating text content to maintain formatting
- Leverage the fluent API for method chaining
- Use Optional-returning methods for safe navigation
- Set inner whitespace fields for elements with only whitespace content
- Use QName-based methods for namespace-aware operations

### ❌ **Avoid:**
- Directly manipulating the nodes list (use provided methods instead)
- Ignoring namespace context when working with namespaced elements
- Using `textContent()` when you need to preserve whitespace patterns
- Creating elements without proper namespace declarations

## Integration with Editor

Elements work seamlessly with the Editor API for document-level operations:

```java
{cdi:snippets.snippet('element-editor-integration')}
```

The Element API provides the foundation for precise XML element manipulation while maintaining formatting integrity and supporting advanced features like namespaces and whitespace preservation.
