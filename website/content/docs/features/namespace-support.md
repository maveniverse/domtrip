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
{cdi:snippets.snippet('finding-elements-by-namespace')}
```

### Working with Default Namespaces

```java
{cdi:snippets.snippet('basic-namespace-handling')}
```

## Namespace Declaration Management

DomTrip preserves all namespace declarations and allows you to manage them:

### Reading Namespace Information

```java
{cdi:snippets.snippet('prefixed-namespaces')}
```

### Managing Namespace Declarations

```java
{cdi:snippets.snippet('managing-namespace-declarations')}
```

## Creating Namespaced Elements

When creating new elements, you can specify their namespace:

### Using Namespace URIs

```java
{cdi:snippets.snippet('creating-namespaced-elements')}
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
{cdi:snippets.snippet('soap-document-handling')}
```

### Maven POMs

```java
{cdi:snippets.snippet('maven-pom-handling')}
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
{cdi:snippets.snippet('namespace-best-practices')}
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

- 🔄 [Lossless Parsing](../../docs/features/lossless-parsing/) - Understanding preservation
- 📝 [Formatting Preservation](../../docs/features/formatting-preservation/) - Maintaining formatting
- 🏗️ [Builder Patterns](../../docs/advanced/factory-methods/) - Creating complex structures
