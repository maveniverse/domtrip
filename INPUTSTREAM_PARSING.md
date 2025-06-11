# InputStream Parsing with Encoding Detection

This document describes the enhanced Parser functionality that supports parsing XML from InputStream with automatic encoding detection.

## Overview

The Parser class has been enhanced to support parsing XML from InputStream sources while automatically detecting the character encoding. This improvement addresses the limitation where the Parser previously only supported parsing from String objects.

## Features

### 1. Automatic Encoding Detection

The parser now automatically detects character encoding through multiple methods:

- **Byte Order Mark (BOM) Detection**: Recognizes UTF-8, UTF-16BE, UTF-16LE, UTF-32BE, and UTF-32LE BOMs
- **XML Declaration Parsing**: Extracts encoding information from the XML declaration
- **Fallback Encoding**: Uses a configurable default encoding when detection fails

### 2. Supported Encodings

The parser supports all standard Java character encodings, with special handling for:

- UTF-8 (default)
- UTF-16 (with BOM detection)
- UTF-32 (with BOM detection)
- ISO-8859-1
- Any encoding supported by Java's Charset class

### 3. XML Declaration Attribute Parsing

The parser now extracts and applies XML declaration attributes to the Document:

- **version**: Sets the document XML version (e.g., "1.0", "1.1")
- **encoding**: Sets the document encoding property
- **standalone**: Sets the document standalone flag

## API Usage

### Basic InputStream Parsing

```java
// Parse with automatic encoding detection
InputStream inputStream = new FileInputStream("document.xml");
Document doc = Document.of(inputStream);

// Access detected encoding
String encoding = doc.encoding(); // e.g., "UTF-8"
String version = doc.version();   // e.g., "1.0"
boolean standalone = doc.isStandalone();
```

### InputStream Parsing with Fallback Encoding

```java
// Parse with custom fallback encoding (String)
InputStream inputStream = new FileInputStream("document.xml");
Document doc = Document.of(inputStream, "ISO-8859-1");

// Parse with custom fallback encoding (Charset - preferred)
Document doc2 = Document.of(inputStream, StandardCharsets.ISO_8859_1);
```

### Using Parser Directly

```java
Parser parser = new Parser();

// Parse with automatic encoding detection
Document doc1 = parser.parse(inputStream);

// Parse with fallback encoding (String)
Document doc2 = parser.parse(inputStream, "UTF-8");

// Parse with fallback encoding (Charset - preferred)
Document doc3 = parser.parse(inputStream, StandardCharsets.UTF_8);
```

## Encoding Detection Process

The parser follows this detection process:

1. **BOM Detection**: Check for byte order marks at the beginning of the stream
2. **XML Declaration Reading**: Attempt to read XML declaration with common encodings
3. **Encoding Extraction**: Parse the encoding attribute from the XML declaration
4. **Validation**: Verify the declared encoding is supported by Java
5. **Fallback**: Use the default encoding if detection fails

## Examples

### UTF-8 with BOM

```java
// XML file with UTF-8 BOM
InputStream inputStream = new FileInputStream("utf8-with-bom.xml");
Document doc = Document.of(inputStream);
// Automatically detects UTF-8 from BOM
```

### UTF-16 Document

```java
// XML with UTF-16 encoding
String xml = "<?xml version=\"1.0\" encoding=\"UTF-16\"?><root>Content</root>";
InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_16));
Document doc = Document.of(inputStream);
// Detects UTF-16 from XML declaration
```

### Complex XML Declaration

```java
// XML with full declaration
String xml = "<?xml version=\"1.1\" encoding=\"UTF-8\" standalone=\"yes\"?><root/>";
InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
Document doc = Document.of(inputStream);

// All attributes are parsed and applied
assert doc.version().equals("1.1");
assert doc.encoding().equals("UTF-8");
assert doc.isStandalone() == true;
```

## Error Handling

The parser throws `DomTripException` for various error conditions:

- **Null InputStream**: When the input stream is null
- **Empty InputStream**: When the input stream contains no data
- **I/O Errors**: When reading from the input stream fails
- **Malformed XML**: When the XML content is invalid
- **Encoding Errors**: When character encoding conversion fails

```java
try {
    Document doc = Document.of(inputStream);
} catch (DomTripException e) {
    System.err.println("Parsing failed: " + e.getMessage());
    if (e.getCause() instanceof IOException) {
        System.err.println("I/O error occurred");
    }
}
```

## Performance Considerations

- The entire InputStream is read into memory for encoding detection
- For large files, consider the memory implications
- The parser tries multiple encodings during detection, which may impact performance for edge cases
- BOM detection is fast and occurs first to minimize encoding attempts

## Backward Compatibility

All existing String-based parsing methods remain unchanged and fully compatible. The new InputStream methods are additive enhancements.

## Testing

Comprehensive tests are provided in `InputStreamParsingTest.java` covering:

- Various character encodings
- BOM detection scenarios
- XML declaration parsing
- Error conditions
- Formatting preservation
- Complex document structures

Run the tests with:
```bash
./mvnw test -Dtest=InputStreamParsingTest
```

## Implementation Details

The implementation adds several private utility methods to the Parser class:

- `readAllBytes()`: Reads the entire InputStream into a byte array
- `detectEncoding()`: Performs encoding detection logic
- `detectBOM()`: Identifies byte order marks
- `extractEncodingFromXmlDeclaration()`: Parses encoding from XML declaration
- `updateDocumentFromXmlDeclaration()`: Updates Document properties from XML declaration

The XML declaration parsing uses a compiled regex pattern for efficient attribute extraction:
```java
Pattern.compile("\\s*<\\?xml\\s+version\\s*=\\s*[\"']([^\"']+)[\"'](?:\\s+encoding\\s*=\\s*[\"']([^\"']+)[\"'])?(?:\\s+standalone\\s*=\\s*[\"']([^\"']+)[\"'])?\\s*\\?>");
```
