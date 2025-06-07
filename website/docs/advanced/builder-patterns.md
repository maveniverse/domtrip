---
sidebar_position: 1
---

# Builder Patterns

DomTrip provides fluent builder APIs that make creating and modifying XML structures intuitive and type-safe. This page covers all the builder patterns available in DomTrip.

## Element Builder

The `Element.builder()` provides a fluent API for creating elements:

### Basic Element Creation

```java
// Simple element with text content
Element version = Element.builder("version")
    .withText("1.0.0")
    .build();

// Element with attributes
Element dependency = Element.builder("dependency")
    .withAttribute("scope", "test")
    .withAttribute("optional", "true")
    .build();
```

### Complex Element Structures

```java
// Nested element structure
Element dependency = Element.builder("dependency")
    .withAttribute("scope", "test")
    .withChild(Element.textElement("groupId", "junit"))
    .withChild(Element.textElement("artifactId", "junit"))
    .withChild(Element.textElement("version", "4.13.2"))
    .withChild(Element.builder("exclusions")
        .withChild(Element.builder("exclusion")
            .withChild(Element.textElement("groupId", "org.hamcrest"))
            .withChild(Element.textElement("artifactId", "*"))
            .build())
        .build())
    .build();
```

### Namespace Support

```java
// Element with namespace
Element soapEnvelope = Element.builder("soap:Envelope")
    .withNamespace("soap", "http://schemas.xmlsoap.org/soap/envelope/")
    .withChild(Element.builder("soap:Body")
        .withNamespace("soap", "http://schemas.xmlsoap.org/soap/envelope/")
        .build())
    .build();
```

## Document Builder

The `Document.builder()` creates complete XML documents:

### Basic Document

```java
Document doc = Document.builder()
    .withVersion("1.0")
    .withEncoding("UTF-8")
    .withRootElement("project")
    .build();
```

### Document with Declaration and Processing Instructions

```java
Document doc = Document.builder()
    .withVersion("1.0")
    .withEncoding("UTF-8")
    .withStandalone(true)
    .withXmlDeclaration()
    .withProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"style.xsl\"")
    .withRootElement(Element.builder("project")
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
Element.Builder builder = Element.builder("dependency")
    .withChild(Element.builder("groupId").withText(groupId).build())
    .withChild(Element.builder("artifactId").withText(artifactId).build())
    .withChild(Element.builder("version").withText(version).build());

// Conditionally add scope
if (scope != null) {
    builder.withAttribute("scope", scope);
}

// Conditionally add classifier
if (classifier != null) {
    builder.withChild(Element.textElement("classifier", classifier));
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

Element dependency = Element.builder("dependency")
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

Element dependenciesElement = Element.builder("dependencies")
    .withChildren(dependencies.stream()
        .map(dep -> Element.builder("dependency")
            .withChild(Element.textElement("groupId", dep.getGroupId()))
            .withChild(Element.textElement("artifactId", dep.getArtifactId()))
            .withChild(Element.textElement("version", dep.getVersion()))
            .build())
        .collect(Collectors.toList()))
    .build();
```

## Template-Based Building

Use builders for template-style XML generation:

```java
public Element createMavenDependency(String groupId, String artifactId, String version, String scope) {
    Element.Builder builder = Element.builder("dependency")
        .withChild(Element.textElement("groupId", groupId))
        .withChild(Element.textElement("artifactId", artifactId))
        .withChild(Element.textElement("version", version));
    
    if (scope != null && !scope.equals("compile")) {
        builder.withChild(Element.textElement("scope", scope));
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
    Element element = Element.builder("dependency")
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
Element.Builder dependencyTemplate = Element.builder("dependency")
    .withAttribute("scope", "test");

Element junit = dependencyTemplate.copy()
    .withChild(Element.textElement("groupId", "junit"))
    .withChild(Element.textElement("artifactId", "junit"))
    .build();

Element mockito = dependencyTemplate.copy()
    .withChild(Element.textElement("groupId", "org.mockito"))
    .withChild(Element.textElement("artifactId", "mockito-core"))
    .build();
```

## Best Practices

### 1. Use Method Chaining Consistently

```java
// ✅ Good - consistent chaining
Element element = Element.builder("dependency")
    .withAttribute("scope", "test")
    .withChild(Element.textElement("groupId", "junit"))
    .withChild(Element.textElement("artifactId", "junit"))
    .build();

// ❌ Inconsistent - breaks the flow
Element.Builder builder = Element.builder("dependency");
builder.withAttribute("scope", "test");
Element child = Element.textElement("groupId", "junit");
builder.withChild(child);
Element element = builder.build();
```

### 2. Extract Complex Builders to Methods

```java
// ✅ Good - readable and reusable
private Element createDependency(String groupId, String artifactId, String version) {
    return Element.builder("dependency")
        .withChild(Element.textElement("groupId", groupId))
        .withChild(Element.textElement("artifactId", artifactId))
        .withChild(Element.textElement("version", version))
        .build();
}

// ❌ Hard to read - inline complexity
Element deps = Element.builder("dependencies")
    .withChild(Element.builder("dependency")
        .withChild(Element.textElement("groupId", "junit"))
        .withChild(Element.textElement("artifactId", "junit"))
        .withChild(Element.textElement("version", "4.13.2"))
        .build())
    .withChild(Element.builder("dependency")
        .withChild(Element.textElement("groupId", "org.mockito"))
        .withChild(Element.textElement("artifactId", "mockito-core"))
        .withChild(Element.textElement("version", "4.6.1"))
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
    
    return Element.builder("dependency")
        .withChild(Element.textElement("groupId", groupId))
        .withChild(Element.textElement("artifactId", artifactId))
        .withChild(Element.textElement("version", version))
        .build();
}
```

## Next Steps

- 📝 [Formatting Preservation](../features/formatting-preservation) - How formatting is maintained
- 🌐 [Namespace Support](../features/namespace-support) - Working with namespaces
- ⚙️ [Configuration](../api/configuration) - Customizing DomTrip behavior
