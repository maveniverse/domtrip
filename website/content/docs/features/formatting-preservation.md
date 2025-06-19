---
title: Formatting Preservation
description: One of DomTrip's core strengths is its ability to preserve the original formatting of your XML documents while allowing you to make targeted edits
layout: page
---

# Formatting Preservation

One of DomTrip's core strengths is its ability to preserve the original formatting of your XML documents while allowing you to make targeted edits. This page explains how formatting preservation works and how to leverage it effectively.

## How It Works

DomTrip preserves formatting by storing whitespace and formatting information alongside the parsed content:

```java
{cdi:snippets.snippet('basic-format-preservation')}
```

## Whitespace Tracking

DomTrip tracks whitespace at multiple levels to ensure perfect preservation:

### Node-Level Whitespace
Every node stores the whitespace that appears before and after it:

```java
{cdi:snippets.snippet('whitespace-tracking')}
```

### Element-Level Whitespace
Elements track whitespace at multiple levels within their structure:

```java
{cdi:snippets.snippet('element-whitespace')}
```

### Inner Element Whitespace
For elements that contain only whitespace (no child elements), DomTrip provides special handling:

```java
{cdi:snippets.snippet('inner-element-whitespace')}
```

### Attribute Formatting
Attributes preserve their quote style, whitespace, and alignment patterns:

```java
// Original XML with mixed formatting:
String xml = """
    <element attr1='single'
             attr2="double"
             attr3='aligned'/>
    """;

Editor editor = new Editor(xml);
Element element = editor.getDocumentElement();

// Update existing attributes - formatting preserved
element.setAttribute("attr1", "updated");  // Still uses single quotes
editor.setAttribute(element, "attr2", "modified");  // Still uses double quotes

// Add new attribute - formatting inferred from existing patterns
editor.setAttribute(element, "attr4", "new");  // Uses inferred alignment and quotes
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

## Automatic Formatting Detection

DomTrip automatically detects the formatting style of existing XML documents and preserves it when adding new content:

```java
// Raw XML (no formatting) - automatically detected
String rawXml = "<root><child>content</child></root>";
Editor editor = new Editor(rawXml);
editor.addElement(editor.root(), "new", "element");
// Result: <root><child>content</child><new>element</new></root>

// Pretty XML - formatting preserved
String prettyXml = """
    <root>
        <child>content</child>
    </root>
    """;
Editor prettyEditor = new Editor(prettyXml);
prettyEditor.addElement(prettyEditor.root(), "new", "element");
// Result maintains indentation and line breaks

// Custom spacing - patterns preserved
String customXml = "<root  attr1=\"value1\"   attr2=\"value2\"/>";
Editor customEditor = new Editor(customXml);
customEditor.setAttribute(customEditor.root(), "attr3", "value3");
// Result maintains the custom spacing pattern
```

## Serialization Modes

DomTrip provides flexible serialization modes to control output formatting:

### Preserve Formatting Mode (Default)
Maintains original formatting for unmodified content and automatically detects formatting patterns:

```java
Serializer serializer = new Serializer(); // prettyPrint = false (default)
String result = serializer.serialize(document);
// Preserves original formatting exactly
```

### Pretty Print Mode
Applies consistent formatting with configurable indentation and line endings:

```java
Serializer prettySerializer = new Serializer();
prettySerializer.setPrettyPrint(true);
prettySerializer.setIndentString("    "); // 4 spaces
prettySerializer.setLineEnding("\n");
String prettyResult = prettySerializer.serialize(document);
```

### Raw Mode
Produces completely unformatted output with no line breaks or indentation:

```java
// Using convenience method
Serializer rawSerializer = new Serializer(DomTripConfig.raw());
String rawResult = rawSerializer.serialize(document);
// Result: <root><child>content</child></root>

// Manual configuration
Serializer manualRaw = new Serializer();
manualRaw.setPrettyPrint(true);
manualRaw.setIndentString(""); // No indentation
manualRaw.setLineEnding("");   // No line endings
```

## Configuration Options

You can control formatting behavior through `DomTripConfig`:

```java
// Default preservation mode
DomTripConfig preserve = DomTripConfig.defaults();

// Pretty printing for new content
DomTripConfig pretty = DomTripConfig.prettyPrint()
    .withIndentString("    ")  // 4 spaces
    .withLineEnding("\n");

// Raw mode (no formatting)
DomTripConfig raw = DomTripConfig.raw();

// Custom configuration
DomTripConfig custom = DomTripConfig.prettyPrint()
    .withIndentString("\t")    // Tabs
    .withLineEnding("\r\n")    // Windows line endings
    .withPreserveComments(true);
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
Element newDependency = Element.builder("dependency")
    .withChild(Element.textElement("groupId", "junit"))
    .withChild(Element.textElement("artifactId", "junit"))
    .withChild(Element.textElement("version", "4.13.2"))
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

- 🔄 [Lossless Parsing](../../docs/features/lossless-parsing/) - Understanding the parsing process
- 🌐 [Namespace Support](../../docs/features/namespace-support/) - Working with XML namespaces
- 🏗️ [Builder Patterns](../../docs/advanced/factory-methods/) - Creating complex structures
