---
sidebar_position: 1
---

# API Design

DomTrip follows a consistent and intuitive API design that makes XML processing natural and discoverable.

## Design Principles

### 1. Logical Grouping
All functionality is grouped with the classes it relates to:
- **Document** class contains document creation methods
- **Element** class contains element creation methods  
- **Comment** class contains comment creation methods
- **Text** class contains text creation methods

### 2. Consistent Patterns
Every class follows the same pattern:
- **Factory methods** for common use cases
- **Builder** for complex scenarios
- **Instance methods** for manipulation

### 3. Intuitive Discovery
Developers can find what they need exactly where they expect it:
```java
// Naturally discoverable
Document doc = Document.withRootElement("project");
Element element = Element.textElement("name", "value");
Comment comment = Comment.builder().withContent("text").build();
```

## API Structure

### Main Classes (Primary API)

#### Document
```java
// Factory methods
Document.empty()
Document.withXmlDeclaration("1.0", "UTF-8")
Document.withRootElement("project")
Document.minimal("root")

// Builder
Document.builder()
    .withVersion("1.0")
    .withEncoding("UTF-8")
    .withRootElement("project")
    .build()

// Instance methods
doc.setDocumentElement(element)
doc.findElement("name")
```

#### Element
```java
// Factory methods
Element.textElement("name", "value")
Element.emptyElement("properties")
Element.selfClosingElement("br")
Element.namespacedElement("soap", "Envelope", "http://...")

// Builder
Element.builder("dependency")
    .withAttribute("scope", "test")
    .withChild(Element.textElement("groupId", "junit"))
    .build()

// Instance methods
element.setAttribute("name", "value")
element.findChild("child")
element.addChild(childElement)
```

#### Comment
```java
// Factory method via Element (for convenience)
Comment comment = Element.comment("This is a comment");

// Builder
Comment.builder()
    .withContent("This is a comment")
    .build()

// Instance methods
comment.setContent("new content")
comment.getContent()
```

#### Text
```java
// Builder
Text.builder()
    .withContent("Hello World")
    .asCData()
    .build()

// Instance methods
text.setContent("new content")
text.getContent()
text.isCData()
```

### Utility Classes (Backward Compatibility)

For backward compatibility, utility classes delegate to main classes:

```java
// These still work but delegate to main classes
Documents.withRootElement("project")  // → Document.withRootElement("project")
Element.textElement("name", "value")  // → Element.textElement("name", "value")
```

## When to Use What

### Factory Methods vs Builders

**Use Factory Methods for:**
- Simple, common patterns
- Single-purpose creation
- Quick prototyping

```java
// Simple cases
Element version = Element.textElement("version", "1.0.0");
Document doc = Document.withRootElement("project");
```

**Use Builders for:**
- Complex structures
- Multiple properties
- Conditional building

```java
// Complex cases
Element dependency = Element.builder("dependency")
    .withAttribute("scope", "test")
    .withChild(Element.builder("groupId").withText("junit").build())
    .withChild(Element.builder("artifactId").withText("junit").build())
    .build();
```

### Direct Creation vs Editor

**Use Direct Creation for:**
- Building new documents from scratch
- Creating standalone elements
- Template generation

```java
Document doc = Document.builder()
    .withRootElement("project")
    .build();
```

**Use Editor for:**
- Modifying existing documents
- Preserving formatting
- Complex document editing

```java
Editor editor = new Editor(existingXml);
editor.addElement(parent, "dependency");
```

## Migration Guide

If you're using the old utility classes, migration is straightforward:

### Before (Old API)
```java
Document doc = Documents.withRootElement("project");
Element element = Element.textElement("name", "value");
Element complex = Element.builder("dependency")
    .withAttribute("scope", "test")
    .build();
```

### After (New API)
```java
Document doc = Document.withRootElement("project");
Element element = Element.textElement("name", "value");
Element complex = Element.builder("dependency")
    .withAttribute("scope", "test")
    .build();
```

**Note:** The old utility classes still work and delegate to the new API, so existing code continues to function without changes.

## Best Practices

### 1. Use Main Classes Directly
```java
// ✅ Preferred - discoverable and consistent
Element element = Element.textElement("name", "value");

// ⚠️ Still works but less discoverable
Element element = Element.textElement("name", "value");
```

### 2. Choose the Right Tool
```java
// ✅ Factory method for simple cases
Element simple = Element.textElement("version", "1.0.0");

// ✅ Builder for complex cases
Element complex = Element.builder("dependency")
    .withAttribute("scope", "test")
    .withChild(Element.textElement("groupId", "junit"))
    .build();
```

### 3. Leverage IDE Auto-completion
Start typing the class name and let your IDE show you all available options:
- `Document.` → shows all document creation methods
- `Element.` → shows all element creation methods
- `Comment.` → shows comment creation methods

This design makes the API self-documenting and highly discoverable!
