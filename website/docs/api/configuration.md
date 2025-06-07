---
sidebar_position: 2
---

# Configuration

DomTrip's behavior can be customized through the `DomTripConfig` class. This page covers all available configuration options and how to use them effectively.

## Configuration Overview

`DomTripConfig` controls how DomTrip parses, processes, and serializes XML:

```java
// Use preset configurations
DomTripConfig strict = DomTripConfig.strict();
DomTripConfig lenient = DomTripConfig.lenient();
DomTripConfig pretty = DomTripConfig.prettyPrint();

// Create custom configuration
DomTripConfig custom = DomTripConfig.defaults()
    .withIndentation("  ")
    .withPreserveWhitespace(true)
    .withQuoteStyle(QuoteStyle.DOUBLE);

// Use with Editor
Editor editor = new Editor(xml, custom);
```

## Preset Configurations

### Strict Configuration

Maximum preservation of original formatting:

```java
DomTripConfig strict = DomTripConfig.strict();
// - Preserves all whitespace
// - Preserves all comments
// - Preserves original quote styles
// - Preserves entity encoding
// - Minimal reformatting
```

### Lenient Configuration

Balanced approach between preservation and flexibility:

```java
DomTripConfig lenient = DomTripConfig.lenient();
// - Preserves significant whitespace
// - Preserves comments
// - Normalizes quote styles
// - Flexible entity handling
// - Smart reformatting
```

### Pretty Print Configuration

Clean, readable output:

```java
DomTripConfig pretty = DomTripConfig.prettyPrint();
// - Consistent indentation
// - Clean whitespace
// - Uniform quote styles
// - Readable formatting
// - Full reformatting
```

## Whitespace Configuration

Control how whitespace is handled:

```java
DomTripConfig config = DomTripConfig.defaults()
    .withPreserveWhitespace(true)           // Keep original whitespace
    .withIndentation("    ")                // 4 spaces for new content
    .withNewlineAfterElements(true)         // Add newlines after elements
    .withTrimTextContent(false)             // Don't trim text content
    .withCollapseWhitespace(false);         // Don't collapse multiple spaces
```

### Indentation Options

```java
// Different indentation styles
DomTripConfig spaces = DomTripConfig.defaults()
    .withIndentation("  ");                 // 2 spaces

DomTripConfig tabs = DomTripConfig.defaults()
    .withIndentation("\t");                 // Tab characters

DomTripConfig mixed = DomTripConfig.defaults()
    .withIndentation("    ");               // 4 spaces
```

## Quote Style Configuration

Control attribute quote styles:

```java
// Force double quotes
DomTripConfig doubleQuotes = DomTripConfig.defaults()
    .withQuoteStyle(QuoteStyle.DOUBLE);

// Force single quotes  
DomTripConfig singleQuotes = DomTripConfig.defaults()
    .withQuoteStyle(QuoteStyle.SINGLE);

// Preserve original quotes
DomTripConfig preserveQuotes = DomTripConfig.defaults()
    .withQuoteStyle(QuoteStyle.PRESERVE);
```

## Comment Handling

Configure comment preservation:

```java
DomTripConfig config = DomTripConfig.defaults()
    .withPreserveComments(true)             // Keep all comments
    .withPreserveDocumentComments(true)     // Keep document-level comments
    .withPreserveInlineComments(true)       // Keep inline comments
    .withCommentIndentation("  ");          // Indent comments
```

## Entity Handling

Control entity encoding and decoding:

```java
DomTripConfig config = DomTripConfig.defaults()
    .withPreserveEntities(true)             // Keep original entities
    .withEncodeEntities(true)               // Encode special characters
    .withEntityReferences(Map.of(           // Custom entity references
        "copy", "©",
        "reg", "®"
    ));
```

## Namespace Configuration

Configure namespace handling:

```java
DomTripConfig config = DomTripConfig.defaults()
    .withPreserveNamespaceDeclarations(true)    // Keep all declarations
    .withValidateNamespaces(true)               // Validate namespace usage
    .withRequireNamespaceDeclarations(false)    // Allow undeclared prefixes
    .withDefaultNamespacePrefix("ns")           // Prefix for default namespace
    .withNamespaceAware(true);                  // Enable namespace processing
```

## Validation Configuration

Configure validation behavior:

```java
DomTripConfig config = DomTripConfig.defaults()
    .withValidateStructure(true)            // Validate XML structure
    .withValidateNamespaces(true)           // Validate namespaces
    .withValidateAttributes(true)           // Validate attribute names
    .withStrictMode(false)                  // Allow some flexibility
    .withFailOnError(true);                 // Fail on validation errors
```

## Performance Configuration

Optimize for different use cases:

```java
// Memory-optimized configuration
DomTripConfig memoryOptimized = DomTripConfig.defaults()
    .withLazyLoading(true)                  // Load content on demand
    .withCompactStorage(true)               // Use compact storage
    .withPooledObjects(true);               // Reuse objects

// Speed-optimized configuration
DomTripConfig speedOptimized = DomTripConfig.defaults()
    .withPrecomputeHashes(true)             // Cache hash codes
    .withFastParsing(true)                  // Skip some validations
    .withBufferedIO(true);                  // Use buffered I/O
```

## Custom Configuration Builder

Build complex configurations step by step:

```java
DomTripConfig config = DomTripConfig.builder()
    // Whitespace settings
    .withIndentation("  ")
    .withPreserveWhitespace(true)
    .withNewlineAfterElements(true)
    
    // Quote and entity settings
    .withQuoteStyle(QuoteStyle.DOUBLE)
    .withPreserveEntities(true)
    
    // Comment settings
    .withPreserveComments(true)
    .withCommentIndentation("  ")
    
    // Namespace settings
    .withNamespaceAware(true)
    .withValidateNamespaces(true)
    
    // Performance settings
    .withLazyLoading(false)
    .withBufferedIO(true)
    
    .build();
```

## Configuration Inheritance

Configurations can be based on existing ones:

```java
// Start with strict configuration
DomTripConfig base = DomTripConfig.strict();

// Customize specific aspects
DomTripConfig custom = base.toBuilder()
    .withIndentation("    ")                // Change indentation
    .withQuoteStyle(QuoteStyle.DOUBLE)      // Force double quotes
    .build();

// Create variant for different use case
DomTripConfig variant = custom.toBuilder()
    .withPreserveComments(false)            // Remove comments
    .withPrettyPrint(true)                  // Enable pretty printing
    .build();
```

## Environment-Specific Configurations

Different configurations for different environments:

```java
public class ConfigurationFactory {
    
    public static DomTripConfig forDevelopment() {
        return DomTripConfig.prettyPrint()
            .withValidateStructure(true)
            .withFailOnError(true);
    }
    
    public static DomTripConfig forProduction() {
        return DomTripConfig.strict()
            .withPreserveWhitespace(true)
            .withPreserveComments(true);
    }
    
    public static DomTripConfig forTesting() {
        return DomTripConfig.lenient()
            .withNormalizeWhitespace(true)
            .withIgnoreComments(true);
    }
}

// Usage
DomTripConfig config = ConfigurationFactory.forProduction();
Editor editor = new Editor(xml, config);
```

## Configuration Validation

Validate configuration settings:

```java
try {
    DomTripConfig config = DomTripConfig.defaults()
        .withIndentation("")                // Invalid: empty indentation
        .withQuoteStyle(null)              // Invalid: null quote style
        .validate();                       // Throws exception
} catch (InvalidConfigurationException e) {
    System.err.println("Configuration error: " + e.getMessage());
}
```

## Common Configuration Patterns

### Configuration File Editing

```java
// Preserve everything for configuration files
DomTripConfig configFile = DomTripConfig.strict()
    .withPreserveWhitespace(true)
    .withPreserveComments(true)
    .withPreserveEntities(true);
```

### Template Processing

```java
// Clean output for templates
DomTripConfig template = DomTripConfig.prettyPrint()
    .withIndentation("  ")
    .withQuoteStyle(QuoteStyle.DOUBLE)
    .withNewlineAfterElements(true);
```

### Data Exchange

```java
// Consistent format for data exchange
DomTripConfig dataExchange = DomTripConfig.defaults()
    .withQuoteStyle(QuoteStyle.DOUBLE)
    .withNormalizeWhitespace(true)
    .withEncodeEntities(true);
```

## Best Practices

### 1. Choose the Right Preset

```java
// ✅ Good - start with appropriate preset
DomTripConfig config = DomTripConfig.strict()  // For config files
    .withIndentation("  ");                    // Customize as needed

// ❌ Avoid - building from scratch unnecessarily
DomTripConfig config = DomTripConfig.builder()
    .withPreserveWhitespace(true)
    .withPreserveComments(true)
    // ... many more settings
    .build();
```

### 2. Document Configuration Choices

```java
// ✅ Good - document why you chose specific settings
DomTripConfig config = DomTripConfig.strict()
    .withIndentation("  ")                     // Match project style
    .withQuoteStyle(QuoteStyle.DOUBLE)         // Consistent with JSON
    .withPreserveComments(true);               // Keep documentation
```

### 3. Use Environment-Specific Configurations

```java
// ✅ Good - different configs for different needs
DomTripConfig config = isProduction() 
    ? DomTripConfig.strict()                   // Preserve everything
    : DomTripConfig.prettyPrint();             // Readable for debugging
```

## Next Steps

- 📝 [Editor API](editor) - Using the Editor class
- 🏗️ [Builder Patterns](../advanced/builder-patterns) - Creating XML structures
- 📖 [Examples](../examples/basic-editing) - Real-world usage examples
