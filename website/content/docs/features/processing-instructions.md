---
title: "Processing Instructions"
description: "Working with XML processing instructions in DomTrip"
weight: 60
---

# Processing Instructions

DomTrip provides comprehensive support for XML processing instructions (PIs), preserving their exact formatting and content during parsing and serialization.

## Overview

Processing instructions are special XML constructs that provide instructions to applications processing the XML document. They have the form `<?target data?>` and are commonly used for:

- **Stylesheet declarations** - `<?xml-stylesheet type="text/xsl" href="style.xsl"?>`
- **Application directives** - `<?php echo "Hello World"; ?>`
- **Processing hints** - `<?sort alpha-ascending?>`

## Key Features

- **Perfect preservation** - Original formatting and content maintained
- **Target and data access** - Separate access to PI components
- **Modification support** - Change target and data while preserving structure
- **Position awareness** - Maintain PI placement in document structure

## Basic Usage

### Creating Processing Instructions

```java
// Create a new processing instruction
ProcessingInstruction pi = new ProcessingInstruction("<?xml-stylesheet type=\"text/xsl\" href=\"style.xsl\"?>");

// Access components
String target = pi.target();  // "xml-stylesheet"
String data = pi.data();      // "type=\"text/xsl\" href=\"style.xsl\""
```

### Parsing Documents with Processing Instructions

```java
String xml = """
    <?xml version="1.0" encoding="UTF-8"?>
    <?xml-stylesheet type="text/xsl" href="transform.xsl"?>
    <?sort-order alpha-ascending?>
    <root>
        <data>content</data>
    </root>
    """;

Document doc = Document.of(xml);
Editor editor = new Editor(doc);

// Processing instructions are preserved exactly
String result = editor.toXml();
// Result maintains all PIs in their original positions
```

## Working with Processing Instructions

### Finding Processing Instructions

```java
Document doc = Document.of(xmlWithPIs);

// Find all processing instructions in document
List<ProcessingInstruction> pis = doc.nodes()
    .filter(node -> node instanceof ProcessingInstruction)
    .map(node -> (ProcessingInstruction) node)
    .collect(Collectors.toList());

// Find specific PI by target
Optional<ProcessingInstruction> stylesheet = doc.nodes()
    .filter(node -> node instanceof ProcessingInstruction)
    .map(node -> (ProcessingInstruction) node)
    .filter(pi -> "xml-stylesheet".equals(pi.target()))
    .findFirst();
```

### Modifying Processing Instructions

```java
ProcessingInstruction pi = new ProcessingInstruction("<?target old-data?>");

// Modify target and data
pi.target("new-target");
pi.data("new-data with parameters");

// Check if modified
boolean isModified = pi.isModified(); // true

// Get updated content
String content = pi.originalContent(); // "<?new-target new-data with parameters?>"
```

## Advanced Features

### Processing Instructions with Special Characters

```java
String xml = """
    <?target data with <special> &amp; characters?>
    <root/>
    """;

Document doc = Document.of(xml);
Editor editor = new Editor(doc);

// Special characters are preserved exactly
String result = editor.toXml();
assertEquals(xml, result);
```

### Position and Whitespace Preservation

```java
String xml = """
    <?xml version="1.0"?>
    
    <?xml-stylesheet href="style.css"?>
    
    <root/>
    """;

Document doc = Document.of(xml);

// Whitespace around PIs is preserved
ProcessingInstruction stylesheet = /* find stylesheet PI */;
String precedingWhitespace = stylesheet.precedingWhitespace(); // "\n\n"
String followingWhitespace = stylesheet.followingWhitespace(); // "\n\n"
```

## Common Use Cases

### XML Stylesheet Declaration

```java
// Add stylesheet PI to document
Editor editor = new Editor();
editor.createDocument("html");

ProcessingInstruction stylesheet = new ProcessingInstruction(
    "<?xml-stylesheet type=\"text/xsl\" href=\"transform.xsl\"?>"
);

// Insert PI before root element
Document doc = editor.document();
doc.insertBefore(doc.root(), stylesheet);
```

### PHP Processing Instructions

```java
String phpXml = """
    <?xml version="1.0"?>
    <?php
        $title = "Dynamic Title";
        echo "<title>$title</title>";
    ?>
    <html>
        <head></head>
        <body>Content</body>
    </html>
    """;

Document doc = Document.of(phpXml);
// PHP PI content is preserved exactly, including newlines and formatting
```

### Application-Specific Instructions

```java
// Custom processing instructions for application logic
ProcessingInstruction sortOrder = new ProcessingInstruction("<?sort-order alpha-ascending?>");
ProcessingInstruction cacheHint = new ProcessingInstruction("<?cache-duration 3600?>");

// Add to document
Element root = editor.root();
root.insertBefore(root.firstChild(), sortOrder);
root.insertBefore(root.firstChild(), cacheHint);
```

## Best Practices

### ✅ **Do:**
- Use meaningful target names that identify the processing application
- Include necessary data in a structured format
- Preserve original formatting when possible
- Use PIs for application-specific metadata

### ❌ **Avoid:**
- Using PIs for data that belongs in elements or attributes
- Creating PIs with malformed syntax
- Assuming all parsers will preserve PI content exactly
- Using reserved target names like "xml"

## Integration with Editor

Processing instructions work seamlessly with DomTrip's Editor API:

```java
Editor editor = new Editor(Document.of(xmlWithPIs));

// PIs are automatically preserved during editing
editor.addElement(editor.root(), "newElement", "content");

// Original PIs remain in their positions with exact formatting
String result = editor.toXml();
```

## Performance Considerations

- **Memory efficient** - PIs are stored as lightweight objects
- **Lazy parsing** - PI content is parsed only when accessed
- **Minimal overhead** - No performance impact when PIs are not used
- **Streaming friendly** - Compatible with large document processing

Processing instructions in DomTrip provide the perfect balance of preservation and functionality, making them ideal for applications that need to maintain exact XML formatting while providing programmatic access to PI content.
