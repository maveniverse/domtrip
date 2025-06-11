---
title: Stream Support and Encoding
description: DomTrip provides comprehensive support for parsing XML from InputStreams and serializing to OutputStreams with automatic encoding detection and handling
layout: page
---

# Stream Support and Encoding

DomTrip provides comprehensive support for parsing XML from InputStreams and serializing to OutputStreams with automatic encoding detection and proper character encoding handling.

## InputStream Parsing

### Automatic Encoding Detection

DomTrip can automatically detect the character encoding of XML documents from InputStreams:

```java
// Parse with automatic encoding detection
InputStream inputStream = new FileInputStream("document.xml");
Document doc = Document.of(inputStream);

// Encoding is automatically detected and set
String detectedEncoding = doc.encoding(); // e.g., "UTF-8"
```

### Encoding Detection Process

The parser follows this detection process:

1. **BOM Detection**: Checks for Byte Order Marks (UTF-8, UTF-16BE, UTF-16LE, UTF-32BE, UTF-32LE)
2. **XML Declaration Reading**: Parses the encoding attribute from `<?xml encoding="..." ?>`
3. **Fallback**: Uses UTF-8 if no encoding is detected

```java
// With fallback encoding (String)
InputStream inputStream = new FileInputStream("document.xml");
Document doc = Document.of(inputStream, "ISO-8859-1");

// With fallback encoding (Charset - preferred)
Document doc2 = Document.of(inputStream, StandardCharsets.ISO_8859_1);
```

### Supported Encodings

DomTrip supports all Java-supported character encodings:

- **UTF-8** (default)
- **UTF-16** (with BOM detection)
- **UTF-32** (with BOM detection)
- **ISO-8859-1**
- Any encoding supported by Java's `Charset` class

### XML Declaration Parsing

The parser extracts and applies XML declaration attributes:

```java
String xml = "<?xml version=\"1.1\" encoding=\"UTF-8\" standalone=\"yes\"?><root/>";
InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

Document doc = Document.of(inputStream);

// All attributes are parsed and applied
assert doc.version().equals("1.1");
assert doc.encoding().equals("UTF-8");
assert doc.isStandalone() == true;
```

## OutputStream Serialization

### Document Serialization

Serialize documents to OutputStreams with proper encoding:

```java
Document doc = Document.of(xmlString);

// Use document's encoding
OutputStream outputStream = new FileOutputStream("output.xml");
doc.toXml(outputStream);

// Specify encoding explicitly (String)
doc.toXml(outputStream, "UTF-16");

// Specify encoding explicitly (Charset - preferred)
doc.toXml(outputStream, StandardCharsets.UTF_16);
```

### Serializer with Encoding

Use the Serializer class for more control:

```java
Serializer serializer = new Serializer();

// Use document's encoding
serializer.serialize(doc, outputStream);

// Specify encoding (String)
serializer.serialize(doc, outputStream, "ISO-8859-1");

// Specify encoding (Charset - preferred)
serializer.serialize(doc, outputStream, StandardCharsets.ISO_8859_1);
```

### Node Serialization

Individual nodes can also be serialized to OutputStreams:

```java
Element element = doc.root();

// Serialize node with UTF-8
serializer.serialize(element, outputStream);

// Serialize node with specific encoding (String)
serializer.serialize(element, outputStream, "UTF-16");

// Serialize node with specific encoding (Charset - preferred)
serializer.serialize(element, outputStream, StandardCharsets.UTF_16);
```

## Round-Trip Processing

DomTrip maintains perfect round-trip fidelity when processing streams:

```java
// Parse from InputStream
InputStream inputStream = new FileInputStream("input.xml");
Document doc = Document.of(inputStream);

// Make modifications
Editor editor = new Editor(doc);
editor.addElement(doc.root(), "newElement", "content");

// Serialize to OutputStream with same encoding
OutputStream outputStream = new FileOutputStream("output.xml");
doc.toXml(outputStream); // Uses document's detected encoding
```

## Encoding Consistency

### Automatic Encoding Preservation

When parsing from InputStream, the document's encoding property is automatically set:

```java
// Document with UTF-16 encoding
InputStream utf16Stream = new ByteArrayInputStream(
    xmlString.getBytes(StandardCharsets.UTF_16));

Document doc = Document.of(utf16Stream);
assert doc.encoding().equals("UTF-16");

// Serialization uses the same encoding
OutputStream outputStream = new ByteArrayOutputStream();
doc.toXml(outputStream); // Automatically uses UTF-16
```

### Encoding Override

You can override the encoding during serialization:

```java
// Parse with one encoding
Document doc = Document.of(inputStream); // UTF-8 detected

// Serialize with different encoding (String)
doc.toXml(outputStream, "UTF-16");

// Serialize with different encoding (Charset - preferred)
doc.toXml(outputStream, StandardCharsets.UTF_16);
```

## Special Characters and BOMs

### BOM Handling

DomTrip automatically detects and handles Byte Order Marks:

```java
// UTF-8 with BOM
byte[] bomBytes = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
byte[] xmlBytes = xmlString.getBytes(StandardCharsets.UTF_8);
byte[] xmlWithBom = new byte[bomBytes.length + xmlBytes.length];
System.arraycopy(bomBytes, 0, xmlWithBom, 0, bomBytes.length);
System.arraycopy(xmlBytes, 0, xmlWithBom, bomBytes.length, xmlBytes.length);

InputStream inputStream = new ByteArrayInputStream(xmlWithBom);
Document doc = Document.of(inputStream);
// BOM is detected and UTF-8 encoding is used
```

### Special Characters

DomTrip properly handles special characters across different encodings:

```java
String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
             "<root><text>Special: àáâãäå èéêë</text></root>";

// Round-trip preserves special characters
InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
Document doc = Document.of(inputStream);

OutputStream outputStream = new ByteArrayOutputStream();
doc.toXml(outputStream);
// Special characters are preserved
```

## Error Handling

### Common Exceptions

Stream operations can throw `DomTripException` for various conditions:

```java
try {
    Document doc = Document.of(inputStream);
    doc.toXml(outputStream);
} catch (DomTripException e) {
    if (e.getCause() instanceof IOException) {
        // Handle I/O errors
        System.err.println("I/O error: " + e.getMessage());
    } else {
        // Handle parsing/encoding errors
        System.err.println("XML error: " + e.getMessage());
    }
}
```

### Invalid Encoding

```java
try {
    serializer.serialize(doc, outputStream, "INVALID-ENCODING");
} catch (DomTripException e) {
    System.err.println("Unsupported encoding: " + e.getMessage());
}
```

## Charset vs String Encoding

### Preferred: Charset Objects

DomTrip supports both String-based encoding names and Charset objects, but Charset objects are preferred:

```java
// ✅ Preferred - Type-safe, no invalid encoding names
Document doc = Document.of(inputStream, StandardCharsets.UTF_8);
doc.toXml(outputStream, StandardCharsets.UTF_16);

// ❌ Acceptable but less safe - String can be invalid
Document doc2 = Document.of(inputStream, "UTF-8");
doc2.toXml(outputStream, "UTF-16");
```

### Benefits of Charset Objects

- **Type Safety**: Compile-time validation of encoding names
- **Performance**: No string parsing overhead
- **Clarity**: Clear intent and better IDE support
- **Error Prevention**: Eliminates typos in encoding names

## Best Practices

### 1. Use Try-With-Resources

```java
// ✅ Proper resource management
try (InputStream inputStream = new FileInputStream("input.xml");
     OutputStream outputStream = new FileOutputStream("output.xml")) {
    
    Document doc = Document.of(inputStream);
    doc.toXml(outputStream);
}
```

### 2. Prefer Charset Objects

```java
// ✅ Type-safe Charset objects
Document doc = Document.of(inputStream, StandardCharsets.UTF_8);
doc.toXml(outputStream, StandardCharsets.UTF_16);

// ❌ String encoding names (error-prone)
Document doc2 = Document.of(inputStream, "UTF-8");
doc2.toXml(outputStream, "UTF-16");
```

### 3. Let DomTrip Detect Encoding

```java
// ✅ Automatic detection
Document doc = Document.of(inputStream);

// ❌ Unnecessary manual specification
Document doc = Document.of(inputStream, StandardCharsets.UTF_8); // Only if needed
```

### 4. Preserve Original Encoding

```java
// ✅ Maintain consistency
Document doc = Document.of(inputStream);
doc.toXml(outputStream); // Uses detected encoding

// ❌ Unnecessary encoding changes
doc.toXml(outputStream, StandardCharsets.UTF_16); // Only if intentional
```

### 5. Handle Large Files Efficiently

```java
// ✅ Stream processing for large files
try (InputStream inputStream = Files.newInputStream(largePath);
     OutputStream outputStream = Files.newOutputStream(outputPath)) {
    
    Document doc = Document.of(inputStream);
    // Process document...
    doc.toXml(outputStream);
}
```

## Performance Considerations

- **Memory Usage**: The entire InputStream is read into memory for encoding detection
- **Encoding Detection**: Multiple encoding attempts may impact performance for edge cases
- **BOM Detection**: Fast and occurs first to minimize encoding attempts
- **Large Files**: Consider memory implications when processing very large XML files

## Migration from String-Based APIs

### Before (String-based)

```java
String xml = Files.readString(path);
Document doc = Document.of(xml);
String result = doc.toXml();
Files.writeString(outputPath, result);
```

### After (Stream-based)

```java
try (InputStream inputStream = Files.newInputStream(path);
     OutputStream outputStream = Files.newOutputStream(outputPath)) {
    
    Document doc = Document.of(inputStream);
    doc.toXml(outputStream);
}
```

The stream-based approach provides better encoding handling and is more memory-efficient for large files.
