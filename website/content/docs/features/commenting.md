---
title: "Element Commenting"
description: "Comment out and uncomment XML elements while preserving formatting"
weight: 50
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
Document doc = Document.of("""
    <project>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.13.2</version>
        </dependency>
        <other>content</other>
    </project>
    """);

Editor editor = new Editor(doc);
Element dependency = doc.root().child("dependency").orElseThrow();

// Comment out the dependency
Comment comment = editor.commentOutElement(dependency);

String result = editor.toXml();
// Result:
// <project>
//     <!-- <dependency><groupId>junit</groupId><artifactId>junit</artifactId><version>4.13.2</version></dependency> -->
//     <other>content</other>
// </project>
```

### Comment Out Multiple Elements

Use `commentOutElements()` to comment out multiple elements as a single block:

```java
Document doc = Document.of("""
    <project>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
        </dependency>
        <dependency>
            <groupId>mockito</groupId>
            <artifactId>mockito-core</artifactId>
        </dependency>
        <other>keep this</other>
    </project>
    """);

Editor editor = new Editor(doc);
Element junit = doc.root().children("dependency").findFirst().orElseThrow();
Element mockito = doc.root().children("dependency").skip(1).findFirst().orElseThrow();

// Comment out both dependencies as a block
Comment comment = editor.commentOutElements(junit, mockito);

String result = editor.toXml();
// Result contains: <!-- <dependency>...</dependency><dependency>...</dependency> -->
```

### Uncomment Elements

Use `uncommentElement()` to restore previously commented elements:

```java
Document doc = Document.of("""
    <project>
        <!-- <dependency><groupId>junit</groupId><artifactId>junit</artifactId></dependency> -->
        <other>content</other>
    </project>
    """);

Editor editor = new Editor(doc);

// Find the comment containing the dependency
Comment comment = doc.root().nodes()
    .filter(node -> node instanceof Comment)
    .map(node -> (Comment) node)
    .findFirst()
    .orElseThrow();

// Restore the commented element
Element restored = editor.uncommentElement(comment);

String result = editor.toXml();
// Result:
// <project>
//     <dependency>
//         <groupId>junit</groupId>
//         <artifactId>junit</artifactId>
//     </dependency>
//     <other>content</other>
// </project>
```

## Advanced Features

### Whitespace Preservation

The commenting features preserve the original whitespace and indentation:

```java
// Original element with specific indentation
Element element = ...; // Has specific preceding whitespace

// Comment out - preserves the element's whitespace
Comment comment = editor.commentOutElement(element);

// The comment will have the same indentation as the original element
assertEquals(element.precedingWhitespace(), comment.precedingWhitespace());
```

### Round-trip Operations

You can comment and uncomment elements multiple times:

```java
Element original = doc.root().child("dependency").orElseThrow();
String originalGroupId = original.child("groupId").orElseThrow().textContent();

// Comment out
Comment comment = editor.commentOutElement(original);

// Uncomment
Element restored = editor.uncommentElement(comment);

// Verify restoration
assertEquals(originalGroupId, restored.child("groupId").orElseThrow().textContent());
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
// Cannot comment out null element
assertThrows(DomTripException.class, () -> {
    editor.commentOutElement(null);
});

// Cannot comment out root element
assertThrows(DomTripException.class, () -> {
    editor.commentOutElement(doc.root());
});

// Elements must have same parent for block commenting
Element child1 = parent1.child("child").orElseThrow();
Element child2 = parent2.child("child").orElseThrow();
assertThrows(DomTripException.class, () -> {
    editor.commentOutElements(child1, child2);
});

// Comment must contain valid XML for uncommenting
Comment invalidComment = new Comment("not valid xml");
assertThrows(DomTripException.class, () -> {
    editor.uncommentElement(invalidComment);
});
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
// Comment out, modify other parts, then uncomment
Comment comment = editor.commentOutElement(dependency);
editor.addElement(root, "newElement", "content");
Element restored = editor.uncommentElement(comment);

// Use with positioning features
Element newDep = editor.insertElementAfter(restored, "dependency");
editor.addElement(newDep, "groupId", "new-group");
```
