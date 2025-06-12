---
title: "Error Handling"
description: "Comprehensive error handling and recovery strategies in DomTrip"
weight: 80
---

# Error Handling

DomTrip provides comprehensive error handling with detailed error messages, recovery strategies, and graceful degradation for robust XML processing applications.

## Overview

DomTrip's error handling system includes:

- **Detailed error messages** with context and suggestions
- **Exception hierarchy** for specific error types
- **Recovery strategies** for common issues
- **Validation errors** with precise location information
- **Graceful degradation** for malformed content

## Exception Hierarchy

### DomTripException

The base exception for all DomTrip-related errors:

```java
try {
    Document doc = Document.of(malformedXml);
} catch (DomTripException e) {
    System.err.println("DomTrip error: " + e.getMessage());
    System.err.println("Cause: " + e.getCause());
}
```

### Parsing Exceptions

```java
try {
    Document doc = Document.of("<<invalid xml>>");
} catch (DomTripParseException e) {
    System.err.println("Parse error at line " + e.getLineNumber());
    System.err.println("Column: " + e.getColumnNumber());
    System.err.println("Error: " + e.getMessage());
}
```

### Validation Exceptions

```java
try {
    Editor editor = new Editor();
    editor.addElement(null, "invalid", "content"); // null parent
} catch (DomTripValidationException e) {
    System.err.println("Validation error: " + e.getMessage());
    System.err.println("Invalid operation: " + e.getOperation());
}
```

## Common Error Scenarios

### Malformed XML

```java
String malformedXml = """
    <root>
        <unclosed-tag>
        <another>content</another>
    </root>
    """;

try {
    Document doc = Document.of(malformedXml);
} catch (DomTripParseException e) {
    System.err.println("XML syntax error:");
    System.err.println("  Line: " + e.getLineNumber());
    System.err.println("  Column: " + e.getColumnNumber());
    System.err.println("  Message: " + e.getMessage());
    
    // Suggested fix
    System.err.println("  Suggestion: Check for unclosed tags");
}
```

### Encoding Issues

```java
try {
    // File with incorrect encoding declaration
    Document doc = Document.of(Path.of("wrong-encoding.xml"));
} catch (DomTripEncodingException e) {
    System.err.println("Encoding error: " + e.getMessage());
    System.err.println("Declared encoding: " + e.getDeclaredEncoding());
    System.err.println("Detected encoding: " + e.getDetectedEncoding());
    
    // Recovery strategy
    try {
        // Try with explicit encoding
        Document doc = Document.of(Files.readString(
            Path.of("wrong-encoding.xml"), 
            Charset.forName(e.getDetectedEncoding())
        ));
    } catch (Exception recovery) {
        System.err.println("Recovery failed: " + recovery.getMessage());
    }
}
```

### Namespace Conflicts

```java
try {
    Editor editor = new Editor();
    Element root = editor.createDocument("root");
    
    // Try to add conflicting namespace
    editor.addNamespace(root, "ns", "http://example.com/ns1");
    editor.addNamespace(root, "ns", "http://example.com/ns2"); // Conflict!
    
} catch (DomTripNamespaceException e) {
    System.err.println("Namespace conflict: " + e.getMessage());
    System.err.println("Existing prefix: " + e.getExistingPrefix());
    System.err.println("Conflicting URI: " + e.getConflictingUri());
    
    // Resolution strategy
    String alternativePrefix = "ns2";
    editor.addNamespace(root, alternativePrefix, "http://example.com/ns2");
}
```

## Error Recovery Strategies

### Graceful Parsing

```java
public Document parseWithRecovery(String xml) {
    try {
        return Document.of(xml);
    } catch (DomTripParseException e) {
        System.err.println("Parse failed, attempting recovery...");
        
        // Strategy 1: Try to fix common issues
        String fixedXml = xml
            .replaceAll("&(?![a-zA-Z]+;)", "&amp;") // Fix unescaped ampersands
            .replaceAll("<([^>]+)>\\s*</\\1>", "<$1/>"); // Convert empty elements
        
        try {
            return Document.of(fixedXml);
        } catch (DomTripParseException e2) {
            // Strategy 2: Extract valid fragments
            return extractValidFragments(xml);
        }
    }
}

private Document extractValidFragments(String xml) {
    // Implementation to extract valid XML fragments
    // and create a document with available content
    Document doc = Document.withRootElement("recovered");
    Editor editor = new Editor(doc);
    
    // Add error information
    editor.addElement(editor.root(), "error", "Original XML was malformed");
    editor.addElement(editor.root(), "partial-content", extractTextContent(xml));
    
    return doc;
}
```

### Validation with Fallbacks

```java
public void safeElementOperation(Element parent, String name, String content) {
    try {
        // Primary operation
        Editor editor = new Editor(parent.document());
        editor.addElement(parent, name, content);
        
    } catch (DomTripValidationException e) {
        System.err.println("Validation failed: " + e.getMessage());
        
        // Fallback: Add as comment
        try {
            Comment fallback = new Comment("<!-- Failed to add element: " + 
                name + "=" + content + " -->");
            parent.addNode(fallback);
        } catch (Exception fallbackError) {
            System.err.println("Fallback also failed: " + fallbackError.getMessage());
        }
    }
}
```

### Resource Cleanup

```java
public Document parseWithCleanup(InputStream inputStream) {
    try {
        return Document.of(inputStream);
    } catch (DomTripException e) {
        System.err.println("Parse failed: " + e.getMessage());
        throw e;
    } finally {
        // Ensure resources are cleaned up
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException closeError) {
            System.err.println("Failed to close stream: " + closeError.getMessage());
        }
    }
}
```

## Error Prevention

### Input Validation

```java
public Document safeParse(String xml) {
    // Pre-validation
    if (xml == null || xml.trim().isEmpty()) {
        throw new IllegalArgumentException("XML content cannot be null or empty");
    }
    
    // Basic structure check
    if (!xml.trim().startsWith("<")) {
        throw new IllegalArgumentException("Content does not appear to be XML");
    }
    
    // Check for obvious issues
    long openTags = xml.chars().filter(ch -> ch == '<').count();
    long closeTags = xml.chars().filter(ch -> ch == '>').count();
    
    if (openTags != closeTags) {
        System.err.println("Warning: Unbalanced angle brackets detected");
    }
    
    return Document.of(xml);
}
```

### Safe Element Access

```java
public String safeGetElementText(Element parent, String childName) {
    try {
        return parent.child(childName)
            .map(Element::textContent)
            .orElse("");
    } catch (Exception e) {
        System.err.println("Failed to access child element '" + childName + "': " + e.getMessage());
        return "";
    }
}

public void safeSetAttribute(Element element, String name, String value) {
    try {
        // Validate attribute name
        if (!isValidXmlName(name)) {
            throw new IllegalArgumentException("Invalid attribute name: " + name);
        }
        
        element.attribute(name, value);
    } catch (Exception e) {
        System.err.println("Failed to set attribute '" + name + "': " + e.getMessage());
    }
}
```

## Debugging Support

### Error Context

```java
try {
    Editor editor = new Editor(document);
    // ... complex operations
} catch (DomTripException e) {
    // Get detailed context
    String context = e.getContext();
    System.err.println("Error context: " + context);
    
    // Get operation stack
    List<String> operations = e.getOperationStack();
    System.err.println("Operation stack:");
    operations.forEach(op -> System.err.println("  " + op));
}
```

### Validation Mode

```java
// Enable strict validation for debugging
DomTripConfig config = DomTripConfig.strict()
    .withValidation(true)
    .withDetailedErrors(true);

try {
    Editor editor = new Editor(document, config);
    // Operations will provide detailed validation
} catch (DomTripValidationException e) {
    // Detailed validation errors
    System.err.println("Validation details: " + e.getValidationDetails());
}
```

## Best Practices

### ✅ **Do:**
- Always catch specific exception types when possible
- Provide meaningful error messages to users
- Implement graceful degradation for non-critical errors
- Log errors with sufficient context for debugging
- Clean up resources in finally blocks or try-with-resources
- Validate inputs before processing
- Use recovery strategies for common issues

### ❌ **Avoid:**
- Catching generic Exception unless necessary
- Ignoring errors silently
- Exposing internal error details to end users
- Continuing processing after critical errors
- Assuming all XML will be well-formed
- Forgetting to close streams and resources

## Integration with Logging

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlProcessor {
    private static final Logger logger = LoggerFactory.getLogger(XmlProcessor.class);
    
    public Document processXml(String xml) {
        try {
            logger.debug("Parsing XML document, length: {}", xml.length());
            Document doc = Document.of(xml);
            logger.info("Successfully parsed XML document");
            return doc;
            
        } catch (DomTripParseException e) {
            logger.error("XML parse error at line {}, column {}: {}", 
                e.getLineNumber(), e.getColumnNumber(), e.getMessage());
            throw e;
            
        } catch (DomTripException e) {
            logger.error("DomTrip processing error: {}", e.getMessage(), e);
            throw e;
        }
    }
}
```

DomTrip's comprehensive error handling ensures that your applications can gracefully handle XML processing issues while providing detailed information for debugging and recovery.
