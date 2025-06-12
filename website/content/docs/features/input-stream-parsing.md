---
title: "Input Stream Parsing"
description: "Parse XML from InputStreams with automatic encoding detection"
weight: 70
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
// Parse XML file with automatic encoding detection
Path xmlFile = Path.of("config.xml");
Document doc = Document.of(xmlFile);

Editor editor = new Editor(doc);
// File encoding is automatically detected and preserved
```

### Parsing from InputStream

```java
// Parse from any InputStream
try (InputStream inputStream = new FileInputStream("data.xml")) {
    Document doc = Document.of(inputStream);
    Editor editor = new Editor(doc);
    
    // Process the document
    Element root = editor.root();
    // ... edit operations
}
```

### Parsing from URL/Network

```java
// Parse XML from network resource
URL xmlUrl = new URL("https://example.com/api/data.xml");
try (InputStream stream = xmlUrl.openStream()) {
    Document doc = Document.of(stream);
    Editor editor = new Editor(doc);
    
    // Network XML is parsed with encoding detection
}
```

## Encoding Detection

### Automatic Detection

DomTrip automatically detects encoding from multiple sources:

```java
// Encoding detected from:
// 1. Byte Order Mark (BOM)
// 2. XML declaration
// 3. Content analysis
// 4. Default fallback (UTF-8)

Document doc = Document.of(inputStream);
String detectedEncoding = doc.encoding(); // "UTF-8", "UTF-16", etc.
```

### Supported Encodings

```java
// UTF-8 (most common)
Document utf8Doc = Document.of(utf8Stream);

// UTF-16 with BOM
Document utf16Doc = Document.of(utf16Stream);

// ISO-8859-1 (Latin-1)
Document isoDoc = Document.of(isoStream);

// Windows-1252
Document winDoc = Document.of(windowsStream);

// All Java-supported encodings work
```

### BOM Handling

```java
// BOM is automatically detected and handled
byte[] utf8WithBom = {(byte)0xEF, (byte)0xBB, (byte)0xBF, /* XML content */};
ByteArrayInputStream stream = new ByteArrayInputStream(utf8WithBom);

Document doc = Document.of(stream);
// BOM is processed transparently, encoding correctly detected
```

## Advanced Features

### Large File Processing

```java
// Memory-efficient processing of large XML files
Path largeXmlFile = Path.of("large-dataset.xml");

try {
    Document doc = Document.of(largeXmlFile);
    Editor editor = new Editor(doc);
    
    // Process in chunks or specific elements
    Element root = editor.root();
    
    // Modify only what's needed
    editor.setAttribute(root, "processed", "true");
    
    // Save back to file
    Files.writeString(largeXmlFile, editor.toXml());
    
} catch (DomTripException e) {
    System.err.println("Failed to process large file: " + e.getMessage());
}
```

### Custom Stream Sources

```java
// Parse from compressed streams
try (InputStream gzipStream = new GZIPInputStream(
        new FileInputStream("data.xml.gz"))) {
    Document doc = Document.of(gzipStream);
    // Compressed XML is automatically decompressed and parsed
}

// Parse from database BLOB
try (InputStream blobStream = resultSet.getBinaryStream("xml_data")) {
    Document doc = Document.of(blobStream);
    // Database XML content is parsed with encoding detection
}
```

### Error Handling

```java
try {
    Document doc = Document.of(inputStream);
    Editor editor = new Editor(doc);
    
} catch (DomTripException e) {
    if (e.getMessage().contains("encoding")) {
        // Handle encoding-related errors
        System.err.println("Encoding issue: " + e.getMessage());
    } else if (e.getMessage().contains("malformed")) {
        // Handle XML syntax errors
        System.err.println("XML syntax error: " + e.getMessage());
    } else {
        // Handle other parsing errors
        System.err.println("Parsing failed: " + e.getMessage());
    }
}
```

## Performance Optimization

### Buffered Streams

```java
// Use BufferedInputStream for better performance
try (InputStream buffered = new BufferedInputStream(
        new FileInputStream("large.xml"), 8192)) {
    Document doc = Document.of(buffered);
    // Buffering improves read performance
}
```

### Memory Management

```java
// For very large files, consider processing in sections
Path hugefile = Path.of("huge-dataset.xml");

// Check file size first
long fileSize = Files.size(hugefile);
if (fileSize > 100_000_000) { // 100MB
    System.out.println("Large file detected, using optimized processing");
}

Document doc = Document.of(hugefile);
// DomTrip handles memory efficiently even for large files
```

## Common Use Cases

### Configuration Files

```java
// Load application configuration
Path configPath = Path.of("app-config.xml");
if (Files.exists(configPath)) {
    Document config = Document.of(configPath);
    Editor editor = new Editor(config);
    
    // Read configuration values
    String dbUrl = editor.findElement("database")
        .flatMap(db -> db.child("url"))
        .map(Element::textContent)
        .orElse("default-url");
}
```

### Web Service Responses

```java
// Parse XML response from web service
HttpURLConnection connection = (HttpURLConnection) url.openConnection();
try (InputStream response = connection.getInputStream()) {
    Document doc = Document.of(response);
    Editor editor = new Editor(doc);
    
    // Process response data
    Element result = editor.findElement("result");
    // ... extract data
}
```

### Batch Processing

```java
// Process multiple XML files
List<Path> xmlFiles = Files.list(Path.of("xml-data"))
    .filter(path -> path.toString().endsWith(".xml"))
    .collect(Collectors.toList());

for (Path xmlFile : xmlFiles) {
    try {
        Document doc = Document.of(xmlFile);
        Editor editor = new Editor(doc);
        
        // Process each file
        processXmlDocument(editor);
        
        // Save processed result
        String outputName = xmlFile.getFileName().toString()
            .replace(".xml", "-processed.xml");
        Path outputPath = xmlFile.getParent().resolve(outputName);
        Files.writeString(outputPath, editor.toXml());
        
    } catch (Exception e) {
        System.err.println("Failed to process " + xmlFile + ": " + e.getMessage());
    }
}
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
// Parse from stream and edit
try (InputStream stream = source.getInputStream()) {
    Document doc = Document.of(stream);
    Editor editor = new Editor(doc);
    
    // All Editor features work normally
    editor.addElement(editor.root(), "timestamp", 
        Instant.now().toString());
    
    // Encoding is preserved in output
    String result = editor.toXml();
}
```

Input stream parsing in DomTrip provides a robust, efficient way to work with XML from any source while maintaining the library's core principles of lossless processing and formatting preservation.
