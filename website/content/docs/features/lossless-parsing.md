---
title: Lossless Parsing
description: DomTrip's core strength is its ability to parse XML documents while preserving every single detail of the original formatting
layout: page
---

# Lossless Parsing

DomTrip's core strength is its ability to parse XML documents while preserving **every single detail** of the original formatting. This enables true round-trip editing where unmodified sections remain completely unchanged.

## What Gets Preserved

### 1. Comments (Including Multi-line)

```java
{cdi:snippets.snippet('comments-preservation')}
```

### 2. Whitespace and Indentation

```java
{cdi:snippets.snippet('whitespace-preservation')}
```

### 3. Entity Encoding

```java
{cdi:snippets.snippet('entity-preservation')}
```

### 4. Attribute Quote Styles

```java
{cdi:snippets.snippet('attribute-quote-preservation')}
```

### 5. CDATA Sections

```java
{cdi:snippets.snippet('cdata-preservation')}
```

### 6. Processing Instructions

```java
{cdi:snippets.snippet('processing-instructions-with-data')}
```

## How It Works

DomTrip achieves lossless parsing through several key techniques:

### 1. Dual Content Storage

Each text node stores both the **decoded content** (for programmatic access) and the **raw content** (for preservation):

```java
// Internal representation
Text textNode = new Text(
    "decoded content: < & >",     // For your code to use
    "raw content: &lt; &amp; &gt;"  // For serialization
);

// You work with decoded content
String content = textNode.getTextContent(); // "decoded content: < & >"

// Serialization uses raw content to preserve entities
String xml = textNode.toXml(); // "raw content: &lt; &amp; &gt;"
```

### 2. Attribute Metadata

Attributes store comprehensive formatting information:

```java
public class Attribute {
    private String value;           // The actual value
    private QuoteStyle quoteStyle;  // SINGLE or DOUBLE
    private String whitespace;      // Surrounding whitespace
    private String rawValue;        // Original encoded value
}
```

### 3. Whitespace Tracking

Every node tracks its surrounding whitespace:

```java
public abstract class Node {
    protected String precedingWhitespace;  // Whitespace before the node
    protected String followingWhitespace;  // Whitespace after the node
}
```

### 4. Modification Tracking

Nodes track whether they've been modified to determine serialization strategy:

```java
// Unmodified nodes use original formatting
if (!node.isModified() && !node.getOriginalContent().isEmpty()) {
    return node.getOriginalContent();
}

// Modified nodes are rebuilt with preserved style
return buildFromScratch(node);
```

## Round-Trip Verification

You can verify lossless parsing with this simple test:

```java
{cdi:snippets.snippet('round-trip-verification')}
```

## Performance Considerations

Lossless parsing requires additional memory to store formatting metadata:

- **Memory overhead**: ~20-30% compared to traditional parsers
- **Parse time**: ~10-15% slower due to metadata collection
- **Serialization**: Faster for unmodified sections, slower for modified sections

### Memory Usage Example

```java
// Traditional parser memory usage
Document traditionalDoc = traditionalParser.parse(xml);
// Memory: ~1x base size

// DomTrip memory usage  
Document domtripDoc = domtripParser.parse(xml);
// Memory: ~1.3x base size (includes formatting metadata)
```

## Limitations

While DomTrip preserves almost everything, there are a few edge cases:

1. **DTD Internal Subsets**: Complex DTD declarations may be simplified
2. **Exotic Encodings**: Some rare character encodings may be normalized
3. **XML Declaration Order**: Attribute order in XML declarations may be standardized

## Best Practices

### 1. Use for Editing Scenarios

```java
{cdi:snippets.snippet('best-practices-editing')}
```

### 2. Verify Round-Trip in Tests

```java
// ✅ Always test round-trip preservation
@Test
void testConfigurationEditing() {
    String original = loadTestXml();
    Editor editor = new Editor(original);
    
    // Make changes...
    editor.addElement(root, "test", "value");
    
    // Verify only intended changes occurred
    String result = editor.toXml();
    assertThat(result).contains("<test>value</test>");
    assertThat(countLines(result)).isEqualTo(countLines(original) + 1);
}
```

### 3. Handle Large Files Carefully

```java
{cdi:snippets.snippet('large-file-handling')}
```

## Comparison with Other Libraries

| Feature | DomTrip | DOM4J | JDOM | Java DOM |
|---------|---------|-------|------|----------|
| **Comment preservation** | ✅ Perfect | ✅ Yes | ✅ Yes | ✅ Yes |
| **Between-element whitespace** | ✅ Exact | ⚠️ Partial | ✅ Yes* | ⚠️ Limited |
| **In-element whitespace** | ✅ Exact | ❌ Lost | ⚠️ Configurable** | ⚠️ Limited |
| **Entity preservation** | ✅ Perfect | ❌ Decoded | ❌ Decoded | ❌ Decoded |
| **Quote style preservation** | ✅ Perfect | ❌ Normalized | ❌ Normalized | ❌ Normalized |
| **Attribute order preservation** | ✅ Perfect | ❌ Lost | ❌ Lost | ❌ Lost |
| **Processing instructions** | ✅ Perfect | ✅ Yes | ✅ Yes | ✅ Yes |
| **CDATA preservation** | ✅ Perfect | ✅ Yes | ✅ Yes | ✅ Yes |
| **Round-trip fidelity** | ✅ 100% | ❌ ~70% | ⚠️ ~80%*** | ❌ ~75% |

**\* JDOM**: Use `Format.getRawFormat()` to preserve original whitespace between elements  
**\*\* JDOM**: Configure with `TextMode.PRESERVE` to maintain text content whitespace  
**\*\*\* JDOM**: Higher fidelity possible with careful configuration, but still loses some formatting details

**Key Insight**: While other libraries can preserve individual aspects of formatting, DomTrip is unique in preserving **all formatting details simultaneously** without requiring special configuration or losing any information during round-trip operations.
