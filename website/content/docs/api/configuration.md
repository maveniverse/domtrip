---
title: Configuration
description: DomTrip's behavior can be customized through the DomTripConfig class. This page covers all available configuration options and how to use them effectively.
layout: page
---

# Configuration

DomTrip's behavior can be customized through the `DomTripConfig` class. This page covers all available configuration options and how to use them effectively.

## Configuration Overview

`DomTripConfig` controls how DomTrip parses, processes, and serializes XML:

```java
{cdi:snippets.snippet('basic-configuration')}
```

## Preset Configurations

### Default Configuration

Maximum preservation of original formatting:

```java
{cdi:snippets.snippet('preset-configurations')}
```

### Pretty Print Configuration

Clean, readable output:

```java
DomTripConfig pretty = DomTripConfig.prettyPrint();
// - Consistent indentation
// - Clean whitespace formatting
// - Preserves comments and processing instructions
// - Readable structure
```

### Minimal Configuration

Compact output for size optimization:

```java
DomTripConfig minimal = DomTripConfig.minimal();
// - No whitespace preservation
// - No comments
// - No processing instructions
// - Omits XML declaration
// - Compact output
```

### Raw Configuration

Completely unformatted output (single line, no indentation):

```java
DomTripConfig raw = DomTripConfig.raw();
// - No line breaks
// - No indentation
// - Single continuous line
// - Minimal file size
// - Useful for APIs or storage optimization

// Example output: <root><child>content</child></root>
```

## Whitespace Configuration

Control how whitespace is handled:

```java
{cdi:snippets.snippet('whitespace-configuration')}
```

### Indentation Options

```java
{cdi:snippets.snippet('indentation-options')}
```

## Quote Style Configuration

Control default attribute quote styles for new attributes:

```java
{cdi:snippets.snippet('quote-style-configuration')}
```

Note: Existing attributes preserve their original quote styles. The default quote style only applies to newly created attributes.

## Comment and Processing Instruction Handling

Configure comment and processing instruction preservation:

```java
{cdi:snippets.snippet('comment-pi-handling')}
```

## Line Ending Configuration

Control line endings in output:

```java
// Use Unix line endings
DomTripConfig unix = DomTripConfig.defaults()
    .withLineEnding("\n");

// Use Windows line endings
DomTripConfig windows = DomTripConfig.defaults()
    .withLineEnding("\r\n");

// Use Mac line endings
DomTripConfig mac = DomTripConfig.defaults()
    .withLineEnding("\r");
```

## XML Declaration Handling

Control whether to include XML declarations:

```java
// Include XML declaration (default)
DomTripConfig withDeclaration = DomTripConfig.defaults()
    .withXmlDeclaration(true);

// Omit XML declaration for minimal output
DomTripConfig withoutDeclaration = DomTripConfig.defaults()
    .withXmlDeclaration(false);
```

## Complete Configuration Example

Build a comprehensive configuration:

```java
DomTripConfig config = DomTripConfig.defaults()
    // Formatting settings
    .withIndentString("  ")                         // 2-space indentation
    .withPrettyPrint(false)                         // No reformatting

    // Content preservation
    .withCommentPreservation(true)                  // Keep comments
    .withProcessingInstructionPreservation(true)    // Keep PIs

    // Quote and declaration settings
    .withDefaultQuoteStyle(QuoteStyle.DOUBLE)       // Double quotes for new attrs
    .withXmlDeclaration(true)                       // Include XML declaration

    // Line ending settings
    .withLineEnding("\n");                          // Unix line endings
```

## Configuration Patterns

Common configuration patterns for different use cases:

```java
// Development configuration - readable output
DomTripConfig development = DomTripConfig.prettyPrint()
    .withIndentString("  ")
    .withCommentPreservation(true);

// Production configuration - preserve everything
DomTripConfig production = DomTripConfig.defaults()
    .withWhitespacePreservation(true)
    .withCommentPreservation(true);

// API response configuration - minimal output
DomTripConfig api = DomTripConfig.minimal()
    .withXmlDeclaration(false);

// Raw configuration - completely unformatted
DomTripConfig raw = DomTripConfig.raw();
// Perfect for storage optimization or when formatting is not needed
```

## Environment-Specific Configurations

Different configurations for different environments:

```java
public class ConfigurationFactory {

    public static DomTripConfig forDevelopment() {
        return DomTripConfig.prettyPrint()
            .withIndentString("  ")
            .withCommentPreservation(true);
    }

    public static DomTripConfig forProduction() {
        return DomTripConfig.defaults()
            .withWhitespacePreservation(true)
            .withCommentPreservation(true);
    }

    public static DomTripConfig forTesting() {
        return DomTripConfig.minimal()
            .withCommentPreservation(false)
            .withXmlDeclaration(false);
    }
}

// Usage
DomTripConfig config = ConfigurationFactory.forProduction();
Editor editor = new Editor(xml, config);
```

## Best Practices

### 1. Choose the Right Preset

```java
// ✅ Good - start with appropriate preset
DomTripConfig config = DomTripConfig.defaults()    // For config files
    .withIndentString("  ");                       // Customize as needed

// ✅ Good - use minimal for APIs
DomTripConfig apiConfig = DomTripConfig.minimal(); // Compact output
```

### 2. Document Configuration Choices

```java
// ✅ Good - document why you chose specific settings
DomTripConfig config = DomTripConfig.defaults()
    .withIndentString("  ")                        // Match project style
    .withDefaultQuoteStyle(QuoteStyle.DOUBLE)      // Consistent with JSON
    .withCommentPreservation(true);                // Keep documentation
```

### 3. Use Environment-Specific Configurations

```java
// ✅ Good - different configs for different needs
DomTripConfig config = isProduction()
    ? DomTripConfig.defaults()                     // Preserve everything
    : DomTripConfig.prettyPrint();                 // Readable for debugging
```

## Available Configuration Methods

Here's a complete list of available configuration methods:

```java
DomTripConfig config = DomTripConfig.defaults()
    // Formatting control
    .withPrettyPrint(boolean)                      // Enable pretty printing
    .withIndentString(String)                      // Set indentation string
    .withLineEnding(String)                        // Set line ending style

    // Content preservation
    .withCommentPreservation(boolean)              // Preserve comments
    .withProcessingInstructionPreservation(boolean) // Preserve PIs
    .withXmlDeclaration(boolean)                   // Include XML declaration

    // Attribute formatting
    .withDefaultQuoteStyle(QuoteStyle);            // Default quote style for new attributes
```

## Next Steps

- 📝 [Editor API](../../docs/api/editor/) - Using the Editor class
- 🏗️ [Builder Patterns](../../docs/advanced/factory-methods/) - Creating XML structures
- 📖 [Examples](../../examples/) - Real-world usage examples
