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
// Simple elements
Element version = Element.of("version");
Element textElement = Element.text("version", "1.0.0");
Element selfClosing = Element.selfClosing("br");

// Element with attributes using fluent API
Element dependency = Element.of("dependency")
    .attribute("scope", "test")
    .attribute("optional", "true");

// Element with multiple attributes at once
Element div = Element.withAttributes("div", Map.of(
    "class", "container",
    "id", "main",
    "data-role", "content"
));
```

### Advanced Element Creation

```java
// Element with complex structure using fluent API
Element project = Element.of("project")
    .attribute("xmlns", "http://maven.apache.org/POM/4.0.0");

project.addNode(Element.text("modelVersion", "4.0.0"));
project.addNode(Element.text("groupId", "com.example"));
project.addNode(Element.text("artifactId", "my-project"));
project.addNode(Element.text("version", "1.0.0"));

// Element with CDATA content
Element script = Element.of("script")
    .attribute("type", "text/javascript");
script.addNode(Text.cdata("function test() { return x < y && z > 0; }"));

// Combined text and attributes
Element withTextAndAttrs = Element.withTextAndAttributes("dependency", 
    "junit", Map.of("scope", "test"));
```

### Element with Namespaces

```java
// Namespaced element using QName
QName soapEnvelope = QName.of("http://schemas.xmlsoap.org/soap/envelope/", "Envelope", "soap");
Element envelope = Element.of(soapEnvelope);

QName soapBody = QName.of("http://schemas.xmlsoap.org/soap/envelope/", "Body", "soap");
Element body = Element.text(soapBody, "Body content");
envelope.addNode(body);
```

## Document Factory Methods

DomTrip provides convenient factory methods for creating documents:

### Simple Document Creation

```java
// Basic document
Document doc = Document.of().root(Element.of("project"));

// Document with XML declaration
Document doc = Document.withXmlDeclaration("1.0", "UTF-8")
    .root(Element.of("project"));

// Complete document with root element
Document doc = Document.withRootElement("project");

// Parse existing XML
Document doc = Document.of(xmlString);
```

### Advanced Document Creation

```java
// Document with complex configuration
Document doc = Document.of()
    .version("1.1")
    .encoding("ISO-8859-1")
    .standalone(true)
    .doctype("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\">")
    .root(Element.of("html"))
    .withXmlDeclaration();

// Document with processing instructions
Document doc = Document.withXmlDeclaration("1.0", "UTF-8");
doc.addNode(ProcessingInstruction.of("xml-stylesheet", "type=\"text/xsl\" href=\"style.xsl\""));
doc.root(Element.of("project"));
```

## Text and Comment Factory Methods

### Text Node Creation

```java
// Simple text
Text text = Text.of("Hello World");

// CDATA text
Text cdata = Text.cdata("<script>alert('test');</script>");

// Text with explicit CDATA flag
Text explicitCdata = Text.of("content", true);

// Convert existing text to CDATA
Text converted = Text.of("Regular text").asCData();

// Control whitespace preservation
Text preserved = Text.of("  spaces  ").preserveWhitespace(false);
```

### Comment Creation

```java
// Simple comment
Comment comment = Comment.of("This is a comment");

// Modify comment content
Comment modified = Comment.of("Initial content")
    .content("Updated content");
```

### Processing Instruction Creation

```java
// Processing instruction with target and data
ProcessingInstruction pi = ProcessingInstruction.of("xml-stylesheet", 
    "type=\"text/css\" href=\"style.css\"");

// Processing instruction with target only
ProcessingInstruction simple = ProcessingInstruction.of("target");

// Modify processing instruction
ProcessingInstruction modified = ProcessingInstruction.of("target", "data")
    .target("new-target")
    .data("new data");
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
// Simple attribute
Attribute attr = Attribute.of("class", "important");

// Attribute with specific quote style
Attribute quoted = Attribute.of("id", "main", QuoteStyle.SINGLE);

// Attribute from QName (for namespaced attributes)
QName xmlLang = QName.of("http://www.w3.org/XML/1998/namespace", "lang", "xml");
Attribute nsAttr = Attribute.of(xmlLang, "en");
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
// ✅ Good - readable fluent chain
Element dependency = Element.of("dependency")
    .attribute("scope", "test")
    .attribute("optional", "true");

dependency.addNode(Element.text("groupId", "junit"));
dependency.addNode(Element.text("artifactId", "junit"));

// ❌ Avoid - breaking the flow
Element dependency = Element.of("dependency");
dependency.attribute("scope", "test");
dependency.attribute("optional", "true");
Element groupId = Element.text("groupId", "junit");
dependency.addNode(groupId);
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
// Efficient - direct object creation and modification
Element element = Element.of("dependency")
    .attribute("scope", "test")
    .attribute("optional", "true");

// All operations modify the same object instance
// No intermediate objects or copying involved
```
