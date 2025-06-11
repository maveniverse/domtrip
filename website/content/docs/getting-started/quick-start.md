---
title: Quick Start
description: Get up and running with DomTrip in 5 minutes! This guide covers the essential operations you'll use most often.
layout: page
---

# Quick Start

Get up and running with DomTrip in 5 minutes! This guide covers the essential operations you'll use most often.

## Your First DomTrip Program

Let's start with a simple example that demonstrates DomTrip's core strength: lossless round-trip editing.

```java
{cdi:snippets.snippet('quick-start-basic')}
```

**Output:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- Configuration file -->
<config>
    <database>
        <host>localhost</host>
        <port>5432</port>
        <username>admin</username>
        <password>secret</password>
    </database>
</config>
```

Notice how:
- The XML declaration and comments are preserved
- Original indentation is maintained
- New elements follow the existing formatting pattern

## Core Operations

### 1. Loading XML

```java
{cdi:snippets.snippet('loading-xml-string')}
```

```java
{cdi:snippets.snippet('loading-xml-from-file')}
```

```java
{cdi:snippets.snippet('loading-xml-from-inputstream')}
```

```java
{cdi:snippets.snippet('loading-xml-config')}
```

### 2. Finding Elements

```java
{cdi:snippets.snippet('finding-elements-basic')}
```

```java
{cdi:snippets.snippet('stream-based-navigation')}
```

### 3. Adding Elements

```java
{cdi:snippets.snippet('adding-elements-simple')}
```

```java
{cdi:snippets.snippet('adding-elements-attributes')}
```

```java
{cdi:snippets.snippet('element-builders')}
```

### 4. Modifying Content

```java
{cdi:snippets.snippet('modifying-content')}
```

### 5. Removing Elements

```java
{cdi:snippets.snippet('removing-elements')}
```

## Working with Namespaces

DomTrip provides excellent namespace support:

```java
{cdi:snippets.snippet('namespace-support')}
```

## Configuration Options

Customize DomTrip's behavior with configuration:

```java
{cdi:snippets.snippet('configuration-options')}
```

## Real-World Example: Maven POM Editing

Here's a practical example of editing a Maven POM file:

```java
{cdi:snippets.snippet('real-world-maven-example')}
```

## Working with Existing Documents

If you already have a parsed Document object, you can create an Editor from it:

```java
{cdi:snippets.snippet('working-with-existing-documents')}
```

You can also use custom configuration with existing documents:

```java
{cdi:snippets.snippet('programmatic-document-creation')}
```

## Error Handling

DomTrip provides robust error handling:

```java
{cdi:snippets.snippet('error-handling')}
```

## Next Steps

Now that you've mastered the basics, explore more advanced features:

- 🏗️ [Builder Patterns](../../docs/advanced/factory-methods/) - Fluent APIs for complex XML
- 🌐 [Namespace Support](../../docs/features/namespace-support/) - Working with XML namespaces
- ⚙️ [Configuration](../../docs/api/configuration/) - Customizing DomTrip's behavior
- 📖 [Examples](../../examples/) - Real-world use cases

## Tips for Success

1. **Always use try-with-resources** for file operations
2. **Check for null** when finding elements that might not exist
3. **Use Optional** for safer navigation
4. **Leverage Stream API** for filtering and processing collections
5. **Configure appropriately** for your use case (preserve vs. pretty print)
