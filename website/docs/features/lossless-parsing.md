---
sidebar_position: 1
---

# Lossless Parsing

DomTrip's core strength is its ability to parse XML documents while preserving **every single detail** of the original formatting. This enables true round-trip editing where unmodified sections remain completely unchanged.

## What Gets Preserved

### 1. Comments (Including Multi-line)

```java
String xml = """
    <?xml version="1.0"?>
    <!-- This is a single-line comment -->
    <root>
        <!--
            This is a multi-line comment
            with multiple lines of text
        -->
        <element>value</element>
    </root>
    """;

Editor editor = new Editor(xml);
String result = editor.toXml();
// Comments are preserved exactly as written
```

### 2. Whitespace and Indentation

```java
String xml = """
    <root>
        <child1>    value with spaces    </child1>
            <child2>differently indented</child2>
    	<child3>tab indented</child3>
    </root>
    """;

Editor editor = new Editor(xml);
// All whitespace, including tabs and spaces, is preserved
```

### 3. Entity Encoding

```java
String xml = """
    <message>
        <text>This &amp; that &lt; other &gt; thing</text>
        <html>&lt;b&gt;Bold&lt;/b&gt; text</html>
    </message>
    """;

Editor editor = new Editor(xml);
// Entities remain as &amp;, &lt;, &gt; - not converted to &, <, >
```

### 4. Attribute Quote Styles

```java
String xml = """
    <element 
        single='quoted' 
        double="quoted"
        mixed='single "inside" double'>
        Content
    </element>
    """;

Editor editor = new Editor(xml);
// Quote styles (single vs double) are preserved exactly
```

### 5. CDATA Sections

```java
String xml = """
    <script>
        <![CDATA[
            function example() {
                if (x < y && y > z) {
                    return "complex content";
                }
            }
        ]]>
    </script>
    """;

Editor editor = new Editor(xml);
// CDATA sections are preserved with their exact content
```

### 6. Processing Instructions

```java
String xml = """
    <?xml version="1.0" encoding="UTF-8"?>
    <?xml-stylesheet type="text/xsl" href="style.xsl"?>
    <document>
        <?custom-instruction data="value"?>
        <content>text</content>
    </document>
    """;

Editor editor = new Editor(xml);
// All processing instructions are preserved
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
@Test
void testLosslessRoundTrip() {
    String original = Files.readString(Paths.get("complex.xml"));
    
    Editor editor = new Editor(original);
    String result = editor.toXml();
    
    assertEquals(original, result, "Round-trip should be identical");
}
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
// ✅ Perfect for editing existing files
Editor editor = new Editor(existingConfigFile);
editor.addElement(root, "newSetting", "value");
Files.writeString(configPath, editor.toXml());
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
// ✅ For large files, consider streaming or chunking
if (fileSize > 10_000_000) { // 10MB
    // Consider alternative approaches for very large files
    logger.warn("Large file detected, consider streaming approach");
}
```

## Comparison with Other Libraries

| Feature | DomTrip | DOM4J | JDOM | Java DOM |
|---------|---------|-------|------|----------|
| Comment preservation | ✅ Perfect | ⚠️ Basic | ⚠️ Basic | ⚠️ Basic |
| Whitespace preservation | ✅ Exact | ❌ Lost | ❌ Lost | ⚠️ Limited |
| Entity preservation | ✅ Perfect | ❌ Decoded | ❌ Decoded | ❌ Decoded |
| Quote style preservation | ✅ Perfect | ❌ Normalized | ❌ Normalized | ❌ Normalized |
| Round-trip fidelity | ✅ 100% | ❌ ~60% | ❌ ~60% | ❌ ~70% |

DomTrip is the only library that achieves true lossless round-trip editing, making it ideal for scenarios where preserving the original document structure is critical.
