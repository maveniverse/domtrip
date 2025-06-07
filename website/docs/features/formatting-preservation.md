---
sidebar_position: 2
---

# Formatting Preservation

One of DomTrip's core strengths is its ability to preserve the original formatting of your XML documents while allowing you to make targeted edits. This page explains how formatting preservation works and how to leverage it effectively.

## How It Works

DomTrip preserves formatting by storing whitespace and formatting information alongside the parsed content:

```java
// Original XML with specific formatting
String xml = """
    <project>
        <groupId>com.example</groupId>
        <artifactId>my-app</artifactId>
        <version>1.0.0</version>
    </project>
    """;

Editor editor = new Editor(xml);
// Make a targeted change
Element version = editor.findElement("version");
editor.setTextContent(version, "2.0.0");

String result = editor.toXml();
// Only the version element is reformatted, everything else stays the same
```

## Whitespace Tracking

DomTrip tracks whitespace at multiple levels to ensure perfect preservation:

### Node-Level Whitespace
Every node stores the whitespace that appears before and after it:

```java
// For this XML: "  <element>content</element>\n"
Element element = editor.findElement("element");
String before = element.getPrecedingWhitespace(); // "  "
String after = element.getFollowingWhitespace();  // "\n"
```

### Element-Level Whitespace
Elements also track whitespace within their tags:

```java
// For this XML: "<element >content</ element>"
Element element = editor.findElement("element");
String openTag = element.getOpenTagWhitespace();   // " "
String closeTag = element.getCloseTagWhitespace(); // " "
```

## Intelligent Formatting Inference

When you add new content, DomTrip automatically infers appropriate formatting from the surrounding context:

```java
// Existing XML structure:
String xml = """
    <dependencies>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
        </dependency>
    </dependencies>
    """;

Editor editor = new Editor(xml);
Element dependencies = editor.findElement("dependencies");

// Add new dependency - formatting is automatically inferred
Element newDep = editor.addElement(dependencies, "dependency");
editor.addElement(newDep, "groupId").setTextContent("org.mockito");
editor.addElement(newDep, "artifactId").setTextContent("mockito-core");

// Result maintains consistent indentation
```

## Configuration Options

You can control formatting behavior through `DomTripConfig`:

```java
// Strict preservation (default)
DomTripConfig strict = DomTripConfig.strict()
    .withPreserveWhitespace(true)
    .withPreserveComments(true);

// Pretty printing for new content
DomTripConfig pretty = DomTripConfig.prettyPrint()
    .withIndentation("    ")  // 4 spaces
    .withNewlineAfterElements(true);

// Custom configuration
DomTripConfig custom = DomTripConfig.defaults()
    .withIndentation("  ")    // 2 spaces
    .withPreserveWhitespace(true)
    .withQuoteStyle(QuoteStyle.DOUBLE);
```

## Best Practices

### 1. Minimal Changes
Make the smallest possible changes to preserve maximum formatting:

```java
// ✅ Good - targeted change
Element version = editor.findElement("version");
editor.setTextContent(version, "2.0.0");

// ❌ Avoid - rebuilding entire structure
Element parent = version.getParent();
parent.removeChild(version);
parent.addElement("version").setTextContent("2.0.0");
```

### 2. Batch Related Changes
Group related modifications to minimize formatting disruption:

```java
// ✅ Good - batch changes to same element
Element dependency = editor.findElement("dependency");
editor.setTextContent(dependency.findChild("groupId"), "new.group");
editor.setTextContent(dependency.findChild("artifactId"), "new-artifact");
editor.setTextContent(dependency.findChild("version"), "2.0.0");
```

### 3. Use Builder Patterns for New Content
When adding complex new structures, use builders for consistent formatting:

```java
Element newDependency = Elements.builder("dependency")
    .withChild(Elements.textElement("groupId", "junit"))
    .withChild(Elements.textElement("artifactId", "junit"))
    .withChild(Elements.textElement("version", "4.13.2"))
    .build();

editor.addChild(dependencies, newDependency);
```

## Common Scenarios

### Configuration File Updates
Perfect for updating configuration files while preserving comments and formatting:

```java
// Update Maven POM version
Editor editor = new Editor(pomXml);
Element version = editor.findElement("version");
editor.setTextContent(version, newVersion);
// Comments, formatting, and structure preserved
```

### Template Processing
Ideal for template-based XML generation:

```java
// Load template
Editor template = new Editor(templateXml);

// Fill in placeholders
template.findElements("placeholder")
    .forEach(placeholder -> {
        String key = placeholder.getAttribute("key");
        String value = properties.getProperty(key);
        editor.setTextContent(placeholder, value);
    });
```

### Incremental Updates
Great for making incremental changes to large documents:

```java
// Add new dependency without affecting existing ones
Element dependencies = editor.findElement("dependencies");
Element newDep = editor.addElement(dependencies, "dependency");
// Only the new dependency section is formatted
```

## Performance Considerations

Formatting preservation has minimal performance impact:

- **Memory**: ~30% overhead for whitespace storage
- **Speed**: Parsing is ~15% slower, serialization is often faster
- **Scalability**: Linear scaling with document size

## Next Steps

- 🔄 [Lossless Parsing](lossless-parsing) - Understanding the parsing process
- 🌐 [Namespace Support](namespace-support) - Working with XML namespaces
- 🏗️ [Builder Patterns](../advanced/builder-patterns) - Creating complex structures
