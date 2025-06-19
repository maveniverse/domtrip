---
title: Maven Extension
description: DomTrip Maven extension for specialized POM file editing
---

# Maven Extension

The DomTrip Maven extension provides specialized functionality for working with Maven POM files, extending the core DomTrip library with Maven-specific features and conventions.

## Quick Overview

```java
import org.maveniverse.domtrip.maven.PomEditor;
import eu.maveniverse.domtrip.Element;
import static org.maveniverse.domtrip.maven.MavenPomElements.Elements.*;

// Create or edit POM files with Maven-aware ordering
PomEditor editor = new PomEditor();
editor.createMavenDocument("project");
Element root = editor.root();

// Elements are automatically ordered according to Maven conventions
editor.insertMavenElement(root, VERSION, "1.0.0");        // Will be ordered correctly
editor.insertMavenElement(root, MODEL_VERSION, "4.0.0");  // Will come first
editor.insertMavenElement(root, GROUP_ID, "com.example"); // Will be ordered correctly
editor.insertMavenElement(root, ARTIFACT_ID, "my-app");   // Will be ordered correctly

// Add dependencies with convenience methods
Element dependencies = editor.insertMavenElement(root, DEPENDENCIES);
editor.addDependency(dependencies, "org.junit.jupiter", "junit-jupiter", "5.9.2");

String result = editor.toXml(); // Properly formatted with blank lines
```

## Key Features

### 🏗️ **Maven-Aware Element Ordering**
Automatically orders elements according to Maven POM conventions with intelligent blank line insertion.

### 🎯 **Convenience Methods**
Specialized methods for common Maven operations like adding dependencies, plugins, modules, and properties.

### 🔧 **Type-Safe Constants**
Comprehensive constants for Maven elements, attributes, namespaces, and schema locations.

### 📐 **Formatting Preservation**
Maintains original formatting, whitespace, and comments while making changes.

## Getting Started

1. **[Installation](/docs/maven/installation/)** - Add the Maven extension to your project
2. **[Quick Start](/docs/maven/quick-start/)** - Your first POM editing example in 5 minutes
3. **[API Reference](/docs/maven/api/)** - Complete PomEditor API documentation

## Documentation

### Core Concepts
- **[Overview](/docs/maven/overview/)** - Understanding the Maven extension architecture
- **[Element Ordering](/docs/maven/ordering/)** - How Maven element ordering works
- **[Examples](/docs/maven/examples/)** - Real-world usage examples

### API Reference
- **[PomEditor API](/docs/maven/api/)** - Complete method reference
- **[MavenPomElements](/docs/maven/api/#mavenpomelementsclass)** - Constants reference

## Use Cases

### 🛠️ **Maven Tooling Development**
Perfect for building tools that need to modify POM files:
- Build automation tools
- IDE plugins for Maven
- Dependency management utilities
- POM validation tools

### 🔄 **POM Transformation**
Ideal for transforming existing POMs:
- Maven version upgrades
- Dependency updates
- Plugin configuration changes
- Template-based generation

### 📊 **POM Analysis**
Great for analyzing and modifying POMs:
- Security scanning tools
- License compliance checking
- Build optimization
- Dependency analysis

## Architecture

```
┌─────────────────────────────────────┐
│           domtrip-maven             │
│                                     │
│  ┌─────────────┐ ┌─────────────────┐│
│  │ PomEditor   │ │ MavenPomElements││
│  │             │ │                 ││
│  │ - Maven     │ │ - Element names ││
│  │   ordering  │ │ - Attributes    ││
│  │ - Blank     │ │ - Namespaces    ││
│  │   lines     │ │ - Schema URIs   ││
│  │ - Helpers   │ │ - Constants     ││
│  └─────────────┘ └─────────────────┘│
└─────────────────────────────────────┘
              │ extends
              ▼
┌─────────────────────────────────────┐
│            domtrip-core             │
│                                     │
│  ┌─────────────┐ ┌─────────────────┐│
│  │ Editor      │ │ Document        ││
│  │             │ │                 ││
│  │ - Lossless  │ │ - Parsing       ││
│  │   editing   │ │ - Serialization ││
│  │ - Formatting│ │ - Navigation    ││
│  │   preserve  │ │ - Whitespace    ││
│  └─────────────┘ └─────────────────┘│
└─────────────────────────────────────┘
```

## Maven Coordinates

```xml
<dependency>
    <groupId>eu.maveniverse</groupId>
    <artifactId>domtrip-maven</artifactId>
    <version>0.2.0</version>
</dependency>
```

## Requirements

- **Java 17+** - Built for modern Java
- **Maven 3.6+** - Compatible with current Maven versions
- **domtrip-core** - Automatically included as dependency

## Next Steps

Choose your path:

### 🚀 **Get Started Quickly**
- [Install the extension](/docs/maven/installation/)
- [Try the 5-minute quick start](/docs/maven/quick-start/)

### 📖 **Learn the Concepts**
- [Understand the overview](/docs/maven/overview/)
- [Learn about element ordering](/docs/maven/ordering/)

### 🔍 **Explore Examples**
- [Browse real-world examples](/docs/maven/examples/)
- [Check the API reference](/docs/maven/api/)

### 🏗️ **Build Something**
Start with the [Quick Start Guide](/docs/maven/quick-start/) and begin editing your first POM file with the Maven extension!
