---
title: Maven Quick Start
description: Get started with DomTrip Maven extension in 5 minutes
layout: page
---

# Maven Quick Start

Get up and running with the DomTrip Maven extension in just a few minutes. This guide shows you the most common use cases with practical examples.

## Your First POM Edit

Let's start by modifying an existing POM file:

```java
{cdi:snippets.snippet('editing-existing-pom')}
```

Notice how the `name` and `description` elements were automatically placed in the correct order with appropriate blank lines!

## Creating a POM from Scratch

```java
{cdi:snippets.snippet('basic-pom-creation')}
```

## Adding Dependencies

```java
{cdi:snippets.snippet('adding-dependencies')}
```

## Adding Plugins

```java
{cdi:snippets.snippet('adding-plugins')}
```

## Working with Multi-Module Projects

```java
{cdi:snippets.snippet('multi-module-project')}
```

## Common Patterns

### Finding and Modifying Existing Elements

```java
{cdi:snippets.snippet('find-and-modify')}
```

### Using Constants for Type Safety

```java
{cdi:snippets.snippet('using-constants')}
```

### Preserving Existing Formatting

```java
// The PomEditor preserves existing formatting and comments
String existingPom = """
    <?xml version="1.0" encoding="UTF-8"?>
    <!-- This is my project -->
    <project xmlns="http://maven.apache.org/POM/4.0.0">
      <modelVersion>4.0.0</modelVersion>

      <!-- Project coordinates -->
      <groupId>com.example</groupId>
      <artifactId>existing-project</artifactId>
      <version>1.0.0</version>
    </project>
    """;

Document doc = Document.of(existingPom);
PomEditor editor = new PomEditor(doc);

// Add new elements - comments and formatting are preserved
editor.insertMavenElement(editor.root(), "name", "Existing Project");

// The result maintains the original structure and comments
String result = editor.toXml();
```

## Next Steps

Now that you've seen the basics:

1. **Explore the API**: Check out the [PomEditor API Reference](../api/)
2. **Learn Element Ordering**: Understand how [Maven Element Ordering](../ordering/) works
3. **See More Examples**: Browse [Maven Examples](../examples/) for advanced use cases
4. **Dependency Alignment**: Learn about [auto-detecting conventions](../alignment/)
5. **Core Features**: Learn about [DomTrip Core Features](../../features/) that work with Maven extension
