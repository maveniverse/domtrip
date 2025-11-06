---
title: "Element Commenting"
description: "Comment out and uncomment XML elements while preserving formatting"
weight: 50
layout: page
---

# Element Commenting

DomTrip's Editor provides powerful features for commenting out XML elements, allowing you to temporarily disable parts of your XML document while preserving their structure and formatting.

## Overview

The commenting features allow you to:

- **Comment out individual elements** - Wrap single elements in XML comments
- **Comment out multiple elements** - Group multiple elements in a single comment block
- **Uncomment elements** - Restore previously commented elements back to active XML
- **Preserve formatting** - Maintain original whitespace and indentation

## Basic Usage

### Comment Out Single Element

Use `commentOutElement()` to comment out an individual element:

```java
{cdi:snippets.snippet('comment-out-single-element')}
```

### Comment Out Multiple Elements

Use `commentOutElements()` to comment out multiple elements as a single block:

```java
{cdi:snippets.snippet('comment-out-multiple-elements')}
```

### Uncomment Elements

Use `uncommentElement()` to restore previously commented elements:

```java
{cdi:snippets.snippet('uncomment-element')}
```

## Advanced Features

### Whitespace Preservation

The commenting features preserve the original whitespace and indentation:

```java
{cdi:snippets.snippet('whitespace-preservation')}
```

### Round-trip Operations

You can comment and uncomment elements multiple times:

```java
{cdi:snippets.snippet('round-trip-operations')}
```

## Method Reference

### `commentOutElement(Element element)`

Comments out a single element by wrapping it in an XML comment.

**Parameters:**
- `element` - The element to comment out

**Returns:**
- `Comment` - The comment that replaced the element

**Throws:**
- `DomTripException` - If the element cannot be commented out (e.g., root element)

### `commentOutElements(Element... elements)`

Comments out multiple elements as a single comment block.

**Parameters:**
- `elements` - The elements to comment out (must have the same parent)

**Returns:**
- `Comment` - The comment that replaced the elements

**Throws:**
- `DomTripException` - If the elements cannot be commented out

### `uncommentElement(Comment comment)`

Restores elements from a comment by parsing the comment content as XML.

**Parameters:**
- `comment` - The comment containing XML to restore

**Returns:**
- `Element` - The first element that was restored

**Throws:**
- `DomTripException` - If the comment content cannot be parsed as XML

## Error Handling

The commenting methods include comprehensive error handling:

```java
{cdi:snippets.snippet('commenting-error-handling')}
```

## Best Practices

1. **Use for temporary disabling** - Commenting is ideal for temporarily disabling configuration sections
2. **Group related elements** - Use block commenting for logically related elements
3. **Preserve structure** - The round-trip capability makes it safe to comment/uncomment repeatedly
4. **Handle errors** - Always handle `DomTripException` when using commenting methods
5. **Validate before uncommenting** - Ensure comments contain valid XML before attempting to uncomment

## Integration with Other Features

Commenting works seamlessly with other Editor features:

```java
{cdi:snippets.snippet('commenting-integration')}
```
