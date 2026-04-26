---
title: PomEditor API Reference
description: Complete API reference for the PomEditor class and Maven extension
layout: page
---

# PomEditor API Reference

The `PomEditor` class extends the core `Editor` class with Maven-specific functionality
for working with POM files. It provides sub-object APIs for dependencies, plugins,
properties, subprojects, and parent management.

## Overview

```java
{cdi:snippets.snippet('pom-editor-overview')}
```

## Sub-Object APIs

The PomEditor organizes operations into domain-specific sub-objects:

| API | Access | Purpose |
|-----|--------|---------|
| `Dependencies` | `editor.dependencies()` | Dependency CRUD, exclusions, alignment, convention detection |
| `Plugins` | `editor.plugins()` | Plugin CRUD, pluginManagement |
| `Properties` | `editor.properties()` | Property CRUD |
| `Subprojects` | `editor.subprojects()` | Module management |
| `Parent` | `editor.parent()` | Parent POM management |
| `Profiles` | `editor.profiles()` | Profile lookup |

## Constructors

```java
// Default configuration
PomEditor editor = new PomEditor();

// Custom configuration
DomTripConfig config = DomTripConfig.builder().indentSize(4).build();
PomEditor editor = new PomEditor(config);

// From existing document
Document doc = Document.of(pomXmlString);
PomEditor editor = new PomEditor(doc);

// Document + configuration
PomEditor editor = new PomEditor(doc, config);
```

## Core Methods

### insertMavenElement()

Inserts elements with Maven-aware ordering and formatting:

```java
Element root = editor.root();
Element dependencies = editor.insertMavenElement(root, "dependencies");
editor.insertMavenElement(root, "groupId", "com.example");
```

Features:
- Automatically orders elements according to Maven conventions
- Adds appropriate blank lines between element groups
- Preserves existing formatting and comments

### findChildElement()

Finds a direct child element by name:

```java
Element dependencies = editor.findChildElement(root, "dependencies");
if (dependencies == null) {
    dependencies = editor.insertMavenElement(root, "dependencies");
}
```

### createMavenDocument()

Creates a new Maven POM document with proper namespace:

```java
PomEditor editor = new PomEditor();
editor.createMavenDocument("project");
```

## Coordinates

The `Coordinates` class represents Maven artifact coordinates (GAV):

```java
{cdi:snippets.snippet('coordinates-usage')}
```

## Dependencies API

Access via `editor.dependencies()`.

### CRUD Operations

```java
{cdi:snippets.snippet('dependency-management-ops')}
```

For convention-aware managed dependency updates (creating version properties automatically),
see `updateManagedDependencyAligned()` in [Dependency Alignment](../alignment/).

### Exclusion Management

See [Exclusion Management](../exclusions/) for full documentation.

### Convention Detection & Alignment

See [Dependency Alignment](../alignment/) for full documentation.

### Profile-Scoped Operations

See [Profile-Scoped Operations](../profiles/) for full documentation.

### Cross-POM Alignment

See [Cross-POM Alignment](../cross-pom/) for full documentation.

## Plugins API

Access via `editor.plugins()`.

```java
{cdi:snippets.snippet('plugin-management-ops')}
```

## Properties API

Access via `editor.properties()`.

```java
{cdi:snippets.snippet('property-management')}
```

## Parent API

Access via `editor.parent()`.

```java
{cdi:snippets.snippet('parent-management')}
```

## Utility Methods

### Finding and Modifying Elements

```java
{cdi:snippets.snippet('find-and-modify')}
```

### Using Constants

```java
{cdi:snippets.snippet('using-constants')}
```

## Element Ordering

The PomEditor automatically orders elements according to Maven conventions:

### Project Level Elements
1. `modelVersion`
2. *blank line*
3. `parent`
4. *blank line*
5. `groupId`, `artifactId`, `version`, `packaging`
6. *blank line*
7. `name`, `description`, `url`, `inceptionYear`, `organization`, `licenses`
8. *blank line*
9. `developers`, `contributors`
10. *blank line*
11. `modules`
12. *blank line*
13. `properties`
14. *blank line*
15. `dependencyManagement`, `dependencies`
16. *blank line*
17. `build`
18. *blank line*
19. `profiles`

### Dependency Elements
1. `groupId`, `artifactId`, `version`
2. `classifier`, `type`
3. `scope`
4. `systemPath`
5. `optional`
6. `exclusions`

### Plugin Elements
1. `groupId`, `artifactId`, `version`
2. `extensions`
3. `executions`
4. `dependencies`
5. `goals`
6. `inherited`
7. `configuration`

## Error Handling

The PomEditor throws `DomTripException` for error conditions:
- Invalid XML structure
- Null parent elements
- Missing required elements (e.g., profile not found)
- Invalid coordinates

## Integration with Core Editor

PomEditor inherits all methods from the core `Editor` class:

```java
// Core Editor methods are available
Element element = editor.addElement(parent, "customElement");
editor.removeElement(element);

// Serialization
String xml = editor.toXml();
byte[] bytes = editor.toBytes();
```

## Next Steps

- [Maven Quick Start](../quick-start/) - Get started in 5 minutes
- [Maven Examples](../examples/) - Real-world usage examples
- [Element Ordering](../ordering/) - How ordering works in detail
