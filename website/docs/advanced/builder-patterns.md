---
sidebar_position: 1
---

# Builder Patterns

DomTrip provides fluent builder APIs that make creating and modifying XML structures intuitive and type-safe. This page covers all the builder patterns available in DomTrip.

## Element Builder

The `Elements.builder()` provides a fluent API for creating elements:

### Basic Element Creation

```java
// Simple element with text content
Element version = Elements.builder("version")
    .withText("1.0.0")
    .build();

// Element with attributes
Element dependency = Elements.builder("dependency")
    .withAttribute("scope", "test")
    .withAttribute("optional", "true")
    .build();
```

### Complex Element Structures

```java
// Nested element structure
Element dependency = Elements.builder("dependency")
    .withAttribute("scope", "test")
    .withChild(Elements.textElement("groupId", "junit"))
    .withChild(Elements.textElement("artifactId", "junit"))
    .withChild(Elements.textElement("version", "4.13.2"))
    .withChild(Elements.builder("exclusions")
        .withChild(Elements.builder("exclusion")
            .withChild(Elements.textElement("groupId", "org.hamcrest"))
            .withChild(Elements.textElement("artifactId", "*"))
            .build())
        .build())
    .build();
```

### Namespace Support

```java
// Element with namespace
Element soapEnvelope = Elements.builder("soap:Envelope")
    .withNamespace("http://schemas.xmlsoap.org/soap/envelope/")
    .withNamespaceDeclaration("soap", "http://schemas.xmlsoap.org/soap/envelope/")
    .withChild(Elements.builder("soap:Body")
        .withNamespace("http://schemas.xmlsoap.org/soap/envelope/")
        .build())
    .build();
```

## Document Builder

The `Documents.builder()` creates complete XML documents:

### Basic Document

```java
Document doc = Documents.builder()
    .withVersion("1.0")
    .withEncoding("UTF-8")
    .withRootElement("project")
    .build();
```

### Document with Declaration and Processing Instructions

```java
Document doc = Documents.builder()
    .withVersion("1.0")
    .withEncoding("UTF-8")
    .withStandalone(true)
    .withXmlDeclaration()
    .withProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"style.xsl\"")
    .withRootElement(Elements.builder("project")
        .withAttribute("xmlns", "http://maven.apache.org/POM/4.0.0")
        .build())
    .build();
```

## Editor Builder

The `Editor.add()` method provides a fluent API for adding content to existing documents:

### Adding Elements

```java
Editor editor = new Editor(existingXml);
Element parent = editor.findElement("dependencies");

// Fluent element addition
editor.add()
    .element("dependency")
    .to(parent)
    .withAttribute("scope", "test")
    .withChild(editor.add()
        .element("groupId")
        .withText("junit")
        .build())
    .withChild(editor.add()
        .element("artifactId") 
        .withText("junit")
        .build())
    .build();
```

### Conditional Building

```java
Elements.Builder builder = Elements.builder("dependency")
    .withChild(Elements.textElement("groupId", groupId))
    .withChild(Elements.textElement("artifactId", artifactId))
    .withChild(Elements.textElement("version", version));

// Conditionally add scope
if (scope != null) {
    builder.withAttribute("scope", scope);
}

// Conditionally add classifier
if (classifier != null) {
    builder.withChild(Elements.textElement("classifier", classifier));
}

Element dependency = builder.build();
```

## Attribute Builder

For complex attribute scenarios:

```java
Attribute scopeAttr = Attributes.builder("scope")
    .withValue("test")
    .withQuoteStyle(QuoteStyle.SINGLE)
    .withWhitespace(" ")
    .build();

Element dependency = Elements.builder("dependency")
    .withAttributeObject(scopeAttr)
    .build();
```

## Configuration Builder

Build custom configurations fluently:

```java
DomTripConfig config = DomTripConfig.builder()
    .withIndentation("    ")  // 4 spaces
    .withPreserveWhitespace(true)
    .withPreserveComments(true)
    .withQuoteStyle(QuoteStyle.DOUBLE)
    .withNewlineAfterElements(true)
    .withValidateNamespaces(true)
    .build();

Editor editor = new Editor(xml, config);
```

## Stream-Based Building

Combine builders with Java Streams for powerful XML generation:

```java
List<Dependency> dependencies = getDependencies();

Element dependenciesElement = Elements.builder("dependencies")
    .withChildren(dependencies.stream()
        .map(dep -> Elements.builder("dependency")
            .withChild(Elements.textElement("groupId", dep.getGroupId()))
            .withChild(Elements.textElement("artifactId", dep.getArtifactId()))
            .withChild(Elements.textElement("version", dep.getVersion()))
            .build())
        .collect(Collectors.toList()))
    .build();
```

## Template-Based Building

Use builders for template-style XML generation:

```java
public Element createMavenDependency(String groupId, String artifactId, String version, String scope) {
    Elements.Builder builder = Elements.builder("dependency")
        .withChild(Elements.textElement("groupId", groupId))
        .withChild(Elements.textElement("artifactId", artifactId))
        .withChild(Elements.textElement("version", version));
    
    if (scope != null && !scope.equals("compile")) {
        builder.withChild(Elements.textElement("scope", scope));
    }
    
    return builder.build();
}

// Usage
Element junitDep = createMavenDependency("junit", "junit", "4.13.2", "test");
Element springDep = createMavenDependency("org.springframework", "spring-core", "5.3.21", null);
```

## Error Handling in Builders

Builders provide validation and helpful error messages:

```java
try {
    Element element = Elements.builder("dependency")
        .withAttribute("", "invalid")  // Empty attribute name
        .build();
} catch (InvalidXmlException e) {
    System.err.println("Builder validation failed: " + e.getMessage());
}
```

## Performance Considerations

Builders are optimized for both convenience and performance:

- **Memory efficient**: No intermediate objects created unnecessarily
- **Validation**: Early validation prevents invalid XML structures
- **Reusable**: Builder instances can be reused for similar structures

```java
// Reusable builder pattern
Elements.Builder dependencyTemplate = Elements.builder("dependency")
    .withAttribute("scope", "test");

Element junit = dependencyTemplate.copy()
    .withChild(Elements.textElement("groupId", "junit"))
    .withChild(Elements.textElement("artifactId", "junit"))
    .build();

Element mockito = dependencyTemplate.copy()
    .withChild(Elements.textElement("groupId", "org.mockito"))
    .withChild(Elements.textElement("artifactId", "mockito-core"))
    .build();
```

## Best Practices

### 1. Use Method Chaining Consistently

```java
// ✅ Good - consistent chaining
Element element = Elements.builder("dependency")
    .withAttribute("scope", "test")
    .withChild(Elements.textElement("groupId", "junit"))
    .withChild(Elements.textElement("artifactId", "junit"))
    .build();

// ❌ Inconsistent - breaks the flow
Elements.Builder builder = Elements.builder("dependency");
builder.withAttribute("scope", "test");
Element child = Elements.textElement("groupId", "junit");
builder.withChild(child);
Element element = builder.build();
```

### 2. Extract Complex Builders to Methods

```java
// ✅ Good - readable and reusable
private Element createDependency(String groupId, String artifactId, String version) {
    return Elements.builder("dependency")
        .withChild(Elements.textElement("groupId", groupId))
        .withChild(Elements.textElement("artifactId", artifactId))
        .withChild(Elements.textElement("version", version))
        .build();
}

// ❌ Hard to read - inline complexity
Element deps = Elements.builder("dependencies")
    .withChild(Elements.builder("dependency")
        .withChild(Elements.textElement("groupId", "junit"))
        .withChild(Elements.textElement("artifactId", "junit"))
        .withChild(Elements.textElement("version", "4.13.2"))
        .build())
    .withChild(Elements.builder("dependency")
        .withChild(Elements.textElement("groupId", "org.mockito"))
        .withChild(Elements.textElement("artifactId", "mockito-core"))
        .withChild(Elements.textElement("version", "4.6.1"))
        .build())
    .build();
```

### 3. Validate Early

```java
// ✅ Good - validate inputs before building
public Element createDependency(String groupId, String artifactId, String version) {
    if (groupId == null || groupId.trim().isEmpty()) {
        throw new IllegalArgumentException("groupId cannot be null or empty");
    }
    
    return Elements.builder("dependency")
        .withChild(Elements.textElement("groupId", groupId))
        .withChild(Elements.textElement("artifactId", artifactId))
        .withChild(Elements.textElement("version", version))
        .build();
}
```

## Next Steps

- 📝 [Formatting Preservation](../features/formatting-preservation) - How formatting is maintained
- 🌐 [Namespace Support](../features/namespace-support) - Working with namespaces
- ⚙️ [Configuration](../api/configuration) - Customizing DomTrip behavior
