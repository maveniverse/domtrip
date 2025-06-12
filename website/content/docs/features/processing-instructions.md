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
{cdi:snippets.snippet('creating-processing-instructions')}
```

### Parsing Documents with Processing Instructions

```java
{cdi:snippets.snippet('parsing-documents-with-pis')}
```

## Working with Processing Instructions

### Finding Processing Instructions

```java
{cdi:snippets.snippet('finding-processing-instructions')}
```

### Modifying Processing Instructions

```java
{cdi:snippets.snippet('modifying-processing-instructions')}
```

## Advanced Features

### Processing Instructions with Special Characters

```java
{cdi:snippets.snippet('special-characters')}
```

### Position and Whitespace Preservation

```java
{cdi:snippets.snippet('position-whitespace-preservation')}
```

## Common Use Cases

### XML Stylesheet Declaration

```java
{cdi:snippets.snippet('xml-stylesheet-declaration')}
```

### PHP Processing Instructions

```java
{cdi:snippets.snippet('php-processing-instructions')}
```

### Application-Specific Instructions

```java
{cdi:snippets.snippet('application-specific-instructions')}
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
{cdi:snippets.snippet('editor-integration')}
```

## Performance Considerations

- **Memory efficient** - PIs are stored as lightweight objects
- **Lazy parsing** - PI content is parsed only when accessed
- **Minimal overhead** - No performance impact when PIs are not used
- **Streaming friendly** - Compatible with large document processing

Processing instructions in DomTrip provide the perfect balance of preservation and functionality, making them ideal for applications that need to maintain exact XML formatting while providing programmatic access to PI content.
