---
title: Namespace Support
description: DomTrip provides comprehensive support for XML namespaces, allowing you to work with complex XML documents that use multiple namespaces
layout: page
---

# Namespace Support

DomTrip provides comprehensive support for XML namespaces, allowing you to work with complex XML documents that use multiple namespaces while preserving all namespace declarations and prefixes.

## Understanding XML Namespaces

XML namespaces allow you to avoid naming conflicts when combining XML vocabularies:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <groupId>com.example</groupId>
    <artifactId>my-app</artifactId>
</project>
```

## Namespace-Aware Navigation

DomTrip provides several methods for namespace-aware navigation:

### Finding Elements by Namespace

```java
Editor editor = new Editor(xmlWithNamespaces);

// Find by namespace URI and local name
Optional<Element> project = editor.getDocumentElement()
    .findChildByNamespace("http://maven.apache.org/POM/4.0.0", "project");

// Find all elements in a namespace
Stream<Element> pomElements = editor.getDocumentElement()
    .descendantsByNamespace("http://maven.apache.org/POM/4.0.0");

// Find with prefix (if you know it)
Optional<Element> schemaLocation = editor.getDocumentElement()
    .findChild("xsi:schemaLocation");
```

### Working with Default Namespaces

```java
// XML with default namespace
String xml = """
    <project xmlns="http://maven.apache.org/POM/4.0.0">
        <groupId>com.example</groupId>
        <artifactId>my-app</artifactId>
    </project>
    """;

Editor editor = new Editor(xml);

// These elements are in the default namespace
Element groupId = editor.findElementByNamespace(
    "http://maven.apache.org/POM/4.0.0", "groupId");
```

## Namespace Declaration Management

DomTrip preserves all namespace declarations and allows you to manage them:

### Reading Namespace Information

```java
Element element = editor.findElement("project");

// Get namespace URI
String namespaceURI = element.getNamespaceURI();

// Get prefix (null for default namespace)
String prefix = element.getPrefix();

// Get local name (without prefix)
String localName = element.getLocalName();

// Get qualified name (with prefix if present)
String qualifiedName = element.getQualifiedName();
```

### Managing Namespace Declarations

```java
Element root = editor.getDocumentElement();

// Add namespace declaration
root.addNamespaceDeclaration("custom", "http://example.com/custom");

// Remove namespace declaration
root.removeNamespaceDeclaration("unused");

// Get all namespace declarations
Map<String, String> namespaces = root.getNamespaceDeclarations();
```

## Creating Namespaced Elements

When creating new elements, you can specify their namespace:

### Using Namespace URIs

```java
// Create element in specific namespace
Element customElement = Element.builder("item")
    .withNamespace("http://example.com/custom")
    .withAttribute("id", "123")
    .build();

// Add to document
editor.addChild(parent, customElement);
```

### Using Prefixes

```java
// Create element with prefix
Element prefixedElement = Element.builder("custom:item")
    .withAttribute("id", "123")
    .build();

// DomTrip will resolve the prefix using existing declarations
editor.addChild(parent, prefixedElement);
```

## Namespace Context

DomTrip maintains namespace context throughout the document tree:

```java
Element element = editor.findElement("someElement");

// Get namespace context at this element
NamespaceContext context = element.getNamespaceContext();

// Resolve prefix to URI
String uri = context.getNamespaceURI("custom");

// Get prefix for URI
String prefix = context.getPrefix("http://example.com/custom");

// Get all prefixes for a URI
Iterator<String> prefixes = context.getPrefixes("http://example.com/custom");
```

## Working with Schema Locations

DomTrip handles schema location attributes specially:

```java
Element root = editor.getDocumentElement();

// Get schema locations
String schemaLocation = root.getAttribute("xsi:schemaLocation");

// Update schema location
root.setAttribute("xsi:schemaLocation", 
    "http://maven.apache.org/POM/4.0.0 " +
    "http://maven.apache.org/xsd/maven-4.0.0.xsd");
```

## Namespace Validation

DomTrip can validate namespace usage:

```java
DomTripConfig config = DomTripConfig.defaults()
    .withValidateNamespaces(true)
    .withRequireNamespaceDeclarations(true);

try {
    Editor editor = new Editor(xml, config);
    // Will throw exception if namespaces are invalid
} catch (InvalidNamespaceException e) {
    System.err.println("Namespace error: " + e.getMessage());
}
```

## Common Patterns

### SOAP Documents

```java
// Working with SOAP envelopes
String soapNamespace = "http://schemas.xmlsoap.org/soap/envelope/";

Optional<Element> soapBody = editor.getDocumentElement()
    .findChildByNamespace(soapNamespace, "Body");

if (soapBody.isPresent()) {
    // Process SOAP body content
    Element body = soapBody.get();
    // Add content to body...
}
```

### Maven POMs

```java
// Working with Maven POM files
String pomNamespace = "http://maven.apache.org/POM/4.0.0";

Element dependencies = editor.findElementByNamespace(pomNamespace, "dependencies");
if (dependencies == null) {
    // Create dependencies section
    dependencies = Element.builder("dependencies")
        .withNamespace(pomNamespace)
        .build();
    editor.addChild(editor.getDocumentElement(), dependencies);
}
```

### Configuration Files

```java
// Working with Spring configuration
String springNamespace = "http://www.springframework.org/schema/beans";
String contextNamespace = "http://www.springframework.org/schema/context";

// Find Spring beans
Stream<Element> beans = editor.getDocumentElement()
    .descendantsByNamespace(springNamespace, "bean");

// Find context annotations
Optional<Element> componentScan = editor.getDocumentElement()
    .findChildByNamespace(contextNamespace, "component-scan");
```

## Best Practices

### 1. Use Namespace URIs for Reliability

```java
// ✅ Good - uses namespace URI
Element element = editor.findElementByNamespace(
    "http://maven.apache.org/POM/4.0.0", "dependency");

// ❌ Fragile - depends on prefix
Element element = editor.findElement("pom:dependency");
```

### 2. Preserve Existing Namespace Declarations

```java
// ✅ Good - reuse existing declarations
Element root = editor.getDocumentElement();
if (!root.hasNamespaceDeclaration("custom")) {
    root.addNamespaceDeclaration("custom", "http://example.com/custom");
}

// ❌ Wasteful - always adds new declaration
root.addNamespaceDeclaration("custom", "http://example.com/custom");
```

### 3. Use Qualified Names When Creating Elements

```java
// ✅ Good - explicit namespace
Element element = Element.builder("custom:item")
    .withNamespace("http://example.com/custom")
    .build();

// ❌ Unclear - namespace might be wrong
Element element = Element.builder("item").build();
```

## Configuration Options

Control namespace handling through configuration:

```java
DomTripConfig config = DomTripConfig.defaults()
    .withPreserveNamespaceDeclarations(true)  // Keep all declarations
    .withValidateNamespaces(true)             // Validate namespace usage
    .withRequireNamespaceDeclarations(false)  // Allow undeclared prefixes
    .withDefaultNamespacePrefix("ns");        // Prefix for default namespace
```

## Next Steps

- 🔄 [Lossless Parsing](/docs/features/lossless-parsing/) - Understanding preservation
- 📝 [Formatting Preservation](/docs/features/formatting-preservation/) - Maintaining formatting
- 🏗️ [Builder Patterns](/docs/advanced/builder-patterns/) - Creating complex structures
