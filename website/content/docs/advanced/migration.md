---
title: Migration Guide
description: This guide helps you migrate from other XML libraries to DomTrip. We cover the most common migration scenarios and provide side-by-side examples.
layout: page
---

# Migration Guide

This guide helps you migrate from other XML libraries to DomTrip. We cover the most common migration scenarios and provide side-by-side examples.

## From DOM4J

DOM4J is one of the most popular XML libraries for Java. Here's how to migrate common patterns:

### Document Loading

```java
{cdi:snippets.snippet('dom4j-document-loading')}
```

### Element Navigation

```java
{cdi:snippets.snippet('dom4j-element-navigation')}
```

### Adding Elements

```java
{cdi:snippets.snippet('dom4j-adding-elements')}
```

### Attribute Handling

```java
{cdi:snippets.snippet('dom4j-attribute-handling')}
```

### Serialization

```java
{cdi:snippets.snippet('dom4j-serialization')}
```

## From JDOM

JDOM has a simpler API than DOM4J but similar concepts:

### Document Loading

```java
{cdi:snippets.snippet('jdom-document-loading')}
```

### Element Operations

```java
{cdi:snippets.snippet('jdom-element-operations')}
```

### Text Content

```java
{cdi:snippets.snippet('jdom-text-content')}
```

## From Java DOM

The built-in Java DOM API is verbose but powerful:

### Document Loading

```java
{cdi:snippets.snippet('java-dom-document-loading')}
```

### Element Navigation

```java
{cdi:snippets.snippet('java-dom-element-navigation')}
```

### Creating Elements

```java
{cdi:snippets.snippet('java-dom-creating-elements')}
```

### Attributes

```java
{cdi:snippets.snippet('java-dom-attributes')}
```

## From Jackson XML

Jackson XML is primarily for object mapping, but here are equivalent operations:

### Simple Parsing

```java
{cdi:snippets.snippet('jackson-xml-simple-parsing')}
```

### Object Mapping vs Manual Construction

```java
{cdi:snippets.snippet('jackson-xml-object-mapping')}
```

## Common Migration Patterns

### 1. Error Handling

```java
{cdi:snippets.snippet('migration-error-handling')}
```

### 2. Namespace Handling

```java
{cdi:snippets.snippet('migration-namespace-handling')}
```

### 3. XPath Queries

```java
{cdi:snippets.snippet('migration-xpath-queries')}
```

## Breaking Changes in Recent Versions

### Whitespace API Simplification (v0.1.1+)

The whitespace handling API has been simplified for better maintainability:

**Removed Methods:**
- `Node.followingWhitespace()` and `Node.followingWhitespace(String)`
- `Element.innerFollowingWhitespace()` and `Element.innerFollowingWhitespace(String)`

**Migration Strategy:**
```java
// OLD: Setting whitespace after a node
node.followingWhitespace("\n  ");

// NEW: Set whitespace before the next node instead
nextNode.precedingWhitespace("\n  ");

// OLD: Setting whitespace after opening tag
element.innerFollowingWhitespace("\n    ");

// NEW: Set whitespace before first child instead
firstChild.precedingWhitespace("\n    ");
```

**Rationale:**
The simplified model eliminates redundant whitespace storage where the same whitespace was stored in multiple places. This reduces memory usage and eliminates synchronization issues.

## Migration Checklist

### Before Migration

- [ ] Identify all XML processing code in your application
- [ ] Document current XML formatting requirements
- [ ] Create test cases for existing functionality
- [ ] Note any XPath usage (DomTrip doesn't support XPath)

### During Migration

- [ ] Replace library imports
- [ ] Update document loading code
- [ ] Convert element navigation to DomTrip patterns
- [ ] Update attribute handling
- [ ] Replace serialization code
- [ ] Handle namespace operations
- [ ] Update exception handling

### After Migration

- [ ] Run all existing tests
- [ ] Verify XML output formatting
- [ ] Check performance impact
- [ ] Update documentation
- [ ] Train team on new API patterns

## Performance Considerations

### Memory Usage

```java
{cdi:snippets.snippet('migration-memory-usage')}
```

### Processing Speed

- **Parsing**: DomTrip is ~15% slower due to metadata collection
- **Navigation**: Similar performance to other DOM libraries
- **Serialization**: Faster for unmodified content, slower for heavily modified content

## Gradual Migration Strategy

### Phase 1: New Code

Start using DomTrip for all new XML processing code:

```java
{cdi:snippets.snippet('gradual-migration-phase1')}
```

### Phase 2: Critical Paths

Migrate code that requires formatting preservation:

```java
{cdi:snippets.snippet('gradual-migration-phase2')}
```

### Phase 3: Complete Migration

Replace remaining XML processing code:

```java
{cdi:snippets.snippet('gradual-migration-phase3')}
```

## Getting Help

If you encounter issues during migration:

- 🐛 [Report Issues](https://github.com/maveniverse/domtrip/issues)
- 💬 [Ask Questions](https://github.com/maveniverse/domtrip/discussions)
- 📚 [Check Documentation](../../docs/introduction/)
- 📧 [Contact Support](mailto:support@maveniverse.eu)
