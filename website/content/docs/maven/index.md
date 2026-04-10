---
title: Maven Extension
description: DomTrip Maven extension for specialized POM file editing
layout: page
---

# Maven Extension

The DomTrip Maven extension provides specialized functionality for working with Maven POM files, extending the core DomTrip library with Maven-specific features and conventions.

## Quick Overview

```java
{cdi:snippets.snippet('pom-editor-overview')}
```

## Key Features

### Maven-Aware Element Ordering
Automatically orders elements according to Maven POM conventions with intelligent blank line insertion.

### Sub-Object APIs
Domain-specific APIs for dependencies, plugins, properties, subprojects, and parent management via `editor.dependencies()`, `editor.plugins()`, etc.

### Convention Detection & Alignment
Auto-detect how your project manages dependency versions and add or transform dependencies to follow those conventions consistently.

### Exclusion Management
Full CRUD for dependency exclusions on both regular and managed dependencies.

### Profile-Scoped Operations
Scope dependency operations to specific Maven profiles.

### Cross-POM Alignment
Move dependency versions from child POMs to parent POM's `<dependencyManagement>` with automatic property migration.

### Type-Safe Constants
Comprehensive constants for Maven elements, attributes, namespaces, and schema locations.

### Formatting Preservation
Maintains original formatting, whitespace, and comments while making changes.

## Getting Started

1. **[Installation](../maven/installation/)** - Add the Maven extension to your project
2. **[Quick Start](../maven/quick-start/)** - Your first POM editing example in 5 minutes
3. **[API Reference](../maven/api/)** - Complete PomEditor API documentation

## Documentation

### Core Concepts
- **[Overview](../maven/overview/)** - Understanding the Maven extension architecture
- **[Element Ordering](../maven/ordering/)** - How Maven element ordering works
- **[Examples](../maven/examples/)** - Real-world usage examples

### Dependency Management
- **[Exclusion Management](../maven/exclusions/)** - Add and remove dependency exclusions
- **[Dependency Alignment](../maven/alignment/)** - Auto-detect and align conventions
- **[Profile-Scoped Operations](../maven/profiles/)** - Manage profile-specific dependencies
- **[Cross-POM Alignment](../maven/cross-pom/)** - Multi-module version management

### API Reference
- **[PomEditor API](../maven/api/)** - Complete method reference
- **[Coordinates](../maven/api/#coordinates)** - Maven artifact coordinates

## Maven Coordinates

```xml
<dependency>
    <groupId>eu.maveniverse.maven.domtrip</groupId>
    <artifactId>domtrip-maven</artifactId>
    <version>1.2.0</version>
</dependency>
```

## Requirements

- **Java 8+** - Compatible with a wide range of Java versions
- **Maven 3.6+** - Compatible with current Maven versions
- **domtrip-core** - Automatically included as dependency
