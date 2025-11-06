---
title: "Input Stream Parsing"
description: "Parse XML from InputStreams with automatic encoding detection"
weight: 70
layout: page
---

# Input Stream Parsing

DomTrip provides robust support for parsing XML from various input sources including InputStreams, files, and network resources with automatic encoding detection and BOM handling.

## Overview

Input stream parsing allows you to process XML from:

- **File systems** - Local and network files
- **Network streams** - HTTP responses, web services
- **Memory streams** - ByteArrayInputStream, in-memory data
- **Compressed streams** - ZIP, GZIP archives
- **Any InputStream** - Database BLOBs, custom sources

## Key Features

- **Automatic encoding detection** - UTF-8, UTF-16, ISO-8859-1, and more
- **BOM handling** - Byte Order Mark detection and processing
- **Large file support** - Memory-efficient streaming for big documents
- **Error recovery** - Graceful handling of encoding issues
- **Resource management** - Automatic stream cleanup

## Basic Usage

### Parsing from File

```java
{cdi:snippets.snippet('parsing-from-file')}
```

### Parsing from InputStream

```java
{cdi:snippets.snippet('parsing-from-inputstream')}
```

### Parsing from URL/Network

```java
{cdi:snippets.snippet('parsing-from-network')}
```

## Encoding Detection

### Automatic Detection

DomTrip automatically detects encoding from multiple sources:

```java
{cdi:snippets.snippet('automatic-encoding-detection')}
```

### Supported Encodings

```java
{cdi:snippets.snippet('supported-encodings')}
```

### BOM Handling

```java
{cdi:snippets.snippet('bom-handling')}
```

## Advanced Features

### Large File Processing

```java
{cdi:snippets.snippet('large-file-processing')}
```

### Custom Stream Sources

```java
{cdi:snippets.snippet('custom-stream-sources')}
```

### Error Handling

```java
{cdi:snippets.snippet('inputstream-error-handling')}
```

## Performance Optimization

### Buffered Streams

```java
{cdi:snippets.snippet('buffered-streams')}
```

### Memory Management

```java
{cdi:snippets.snippet('memory-management')}
```

## Common Use Cases

### Configuration Files

```java
{cdi:snippets.snippet('configuration-files')}
```

### Web Service Responses

```java
{cdi:snippets.snippet('web-service-responses')}
```

### Batch Processing

```java
{cdi:snippets.snippet('batch-processing')}
```

## Best Practices

### ✅ **Do:**
- Always use try-with-resources for proper stream cleanup
- Let DomTrip handle encoding detection automatically
- Use buffered streams for large files
- Handle encoding exceptions gracefully
- Check file existence before parsing

### ❌ **Avoid:**
- Manually specifying encoding unless absolutely necessary
- Keeping streams open longer than needed
- Ignoring encoding-related exceptions
- Processing extremely large files without memory considerations
- Assuming all InputStreams support mark/reset

## Integration with Editor

Input stream parsing integrates seamlessly with the Editor API:

```java
{cdi:snippets.snippet('editor-integration')}
```

Input stream parsing in DomTrip provides a robust, efficient way to work with XML from any source while maintaining the library's core principles of lossless processing and formatting preservation.
