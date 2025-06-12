---
title: Basic Concepts
description: Understanding DomTrip's core concepts will help you use the library effectively
layout: page
---

# Basic Concepts

Understanding DomTrip's core concepts will help you use the library effectively. This guide covers the fundamental ideas behind DomTrip's design and how they differ from traditional XML libraries.

## The Lossless Philosophy

Traditional XML libraries focus on **data extraction** - they parse XML to get the information you need, often discarding formatting details in the process. DomTrip takes a different approach: **preservation first**.

```java
{cdi:snippets.snippet('lossless-philosophy')}
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
{cdi:snippets.snippet('node-hierarchy')}
```

## Modification Tracking

Every node tracks whether it has been modified since parsing. This enables **minimal-change serialization**:

```java
{cdi:snippets.snippet('modification-tracking')}
```

## Dual Content Storage

Text nodes store content in two forms:

1. **Decoded Content**: For your application logic
2. **Raw Content**: For preservation during serialization

```java
{cdi:snippets.snippet('dual-content-storage')}
```

This allows you to work with normal strings while preserving entity encoding.

## Attribute Handling

Attributes are first-class objects that preserve formatting details:

```java
{cdi:snippets.snippet('attribute-handling')}
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
{cdi:snippets.snippet('whitespace-inference')}
```

## Configuration System

DomTrip behavior is controlled through `DomTripConfig`:

```java
{cdi:snippets.snippet('configuration-system')}
```

## Navigation Patterns

DomTrip provides multiple ways to navigate XML structures:

### 1. Traditional Navigation

```java
Element root = editor.getDocumentElement();
Element child = root.getChild("child-name");
List<Element> children = root.getChildren("item");
```

### 2. Optional-Based Navigation

```java
{cdi:snippets.snippet('optional-based-navigation')}
```

### 3. Stream-Based Navigation

```java
{cdi:snippets.snippet('stream-based-navigation')}
```

### 4. Namespace-Aware Navigation

```java
{cdi:snippets.snippet('namespace-aware-navigation')}
```

## Error Handling

DomTrip uses specific exception types for better error handling:

```java
{cdi:snippets.snippet('error-handling')}
```

## Next Steps

Now that you understand the core concepts, explore specific features:

- 🔄 [Lossless Parsing](../../docs/features/lossless-parsing/) - Deep dive into preservation
- 📝 [Formatting Preservation](../../docs/features/formatting-preservation/) - How formatting is maintained
- 🌐 [Namespace Support](../../docs/features/namespace-support/) - Working with XML namespaces
- 🏗️ [Builder Patterns](../../docs/advanced/factory-methods/) - Creating complex XML structures
