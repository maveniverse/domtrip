---
title: Examples
description: Practical examples of using DomTrip for XML editing
layout: page
---

# DomTrip Examples

Explore practical examples of using DomTrip for various XML editing scenarios.

## Basic XML Editing

### Simple Element Modification

```java
{cdi:snippets.snippet('simple-element-modification')}}
```

### Adding New Elements

```java
{cdi:snippets.snippet('adding-new-elements')}}
```

## Maven POM Editing

The DomTrip Maven extension provides specialized functionality for working with Maven POM files.

### Basic POM Creation with Maven Extension

```java
import org.maveniverse.domtrip.maven.PomEditor;
import eu.maveniverse.domtrip.Element;
import static org.maveniverse.domtrip.maven.MavenPomElements.Elements.*;

// Create a new POM with Maven-aware ordering
PomEditor editor = new PomEditor();
editor.createMavenDocument("project");
Element root = editor.root();

// Add elements - they'll be automatically ordered
editor.insertMavenElement(root, MODEL_VERSION, "4.0.0");
editor.insertMavenElement(root, GROUP_ID, "com.example");
editor.insertMavenElement(root, ARTIFACT_ID, "my-project");
editor.insertMavenElement(root, VERSION, "1.0.0");
editor.insertMavenElement(root, NAME, "My Project");

String result = editor.toXml();
```

### Adding Dependencies with Maven Extension

```java
// Add dependencies with proper structure
Element dependencies = editor.insertMavenElement(root, DEPENDENCIES);
editor.addDependency(dependencies, "org.junit.jupiter", "junit-jupiter", "5.9.2");

// Add scope to the dependency
Element junitDep = editor.findChildElement(dependencies, DEPENDENCY);
editor.insertMavenElement(junitDep, SCOPE, "test");
```

### Adding Plugins with Maven Extension

```java
// Add build plugins with configuration
Element build = editor.insertMavenElement(root, BUILD);
Element plugins = editor.insertMavenElement(build, PLUGINS);

Element compilerPlugin = editor.addPlugin(plugins,
    "org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");
Element config = editor.insertMavenElement(compilerPlugin, CONFIGURATION);
editor.addElement(config, "source", "17");
editor.addElement(config, "target", "17");
```

### Core Library Examples

For comparison, here are examples using the core DomTrip library:

#### Adding Dependencies (Core Library)

```java
{cdi:snippets.snippet('maven-pom-adding-dependencies')}}
```

#### Updating Version (Core Library)

```java
{cdi:snippets.snippet('maven-pom-updating-version')}}
```

## Configuration File Editing

### Spring Configuration

```java
{cdi:snippets.snippet('spring-configuration')}
```

## Advanced Features

### Working with Namespaces

```java
{cdi:snippets.snippet('working-with-namespaces')}
```

### Using Builder Patterns

```java
{cdi:snippets.snippet('using-builder-patterns')}
```

### Attribute Manipulation

```java
{cdi:snippets.snippet('attribute-manipulation')}
```

## Error Handling

```java
{cdi:snippets.snippet('safe-element-handling')}
```

## Best Practices

### 1. Always Use Optional for Safe Navigation

```java
{cdi:snippets.snippet('best-practices-optional')}
```

### 2. Preserve Original Formatting

```java
{cdi:snippets.snippet('best-practices-preserve-formatting')}
```

## Next Steps

### Core Library
- [API Reference](../docs/api/) - Complete API documentation
- [Features](../docs/features/) - Core library features
- [Advanced Features](../docs/advanced/) - Builder patterns and advanced usage

### Maven Extension
- [Maven Extension Overview](../docs/maven/overview/) - Maven-specific features
- [Maven Quick Start](../docs/maven/quick-start/) - Get started in 5 minutes
- [Maven Examples](../docs/maven/examples/) - More Maven examples
- [Maven API Reference](../docs/maven/api/) - Complete Maven API documentation
