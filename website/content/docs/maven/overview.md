---
title: Maven Extension Overview
description: Overview of DomTrip's Maven-specific extensions for POM file editing
---

# Maven Extension Overview

The DomTrip Maven extension (`domtrip-maven`) provides specialized functionality for working with Maven POM files, extending the core DomTrip library with Maven-specific features and conventions.

## Key Features

### 🏗️ **Maven-Aware Element Ordering**
The `PomEditor` automatically orders elements according to Maven POM conventions:
- **Project elements**: `modelVersion`, `parent`, `groupId`, `artifactId`, `version`, `packaging`, `name`, `description`, etc.
- **Build elements**: `defaultGoal`, `directory`, `finalName`, `sourceDirectory`, etc.
- **Plugin elements**: `groupId`, `artifactId`, `version`, `extensions`, `executions`, etc.
- **Dependency elements**: `groupId`, `artifactId`, `version`, `classifier`, `type`, `scope`, etc.

### 📐 **Intelligent Blank Line Management**
Automatically adds appropriate blank lines between element groups to maintain readable POM structure:
```xml
<project>
  <modelVersion>4.0.0</modelVersion>
  
  <parent>
    <groupId>org.example</groupId>
    <artifactId>parent</artifactId>
    <version>1.0.0</version>
  </parent>
  
  <groupId>com.example</groupId>
  <artifactId>my-project</artifactId>
  <version>1.0.0</version>
  <packaging>jar</packaging>
  
  <name>My Project</name>
  <description>An example project</description>
</project>
```

### 🎯 **Convenience Methods**
Specialized methods for common Maven operations:
- `addDependency()` - Add dependencies with proper structure
- `addPlugin()` - Add plugins with configuration
- `addModule()` - Add modules to multi-module projects
- `addProperty()` - Add properties with proper placement
- `findChildElement()` - Navigate POM structure easily

### 🔧 **Type-Safe Constants**
The `MavenPomElements` class provides comprehensive constants for:
- **Element names**: All standard Maven POM elements
- **Attribute names**: Common XML attributes used in POMs
- **Namespace URIs**: Maven 4.0.0 and 4.1.0 namespaces
- **Schema locations**: XSD schema references
- **File names**: Standard Maven file and directory names

## Architecture

The Maven extension is built on top of the core DomTrip library:

```
┌─────────────────────────────────────┐
│           domtrip-maven             │
│  ┌─────────────┐ ┌─────────────────┐│
│  │ PomEditor   │ │ MavenPomElements││
│  │             │ │                 ││
│  │ - Maven     │ │ - Constants     ││
│  │   ordering  │ │ - Namespaces    ││
│  │ - Blank     │ │ - Schema URIs   ││
│  │   lines     │ │                 ││
│  │ - Helpers   │ │                 ││
│  └─────────────┘ └─────────────────┘│
└─────────────────────────────────────┘
┌─────────────────────────────────────┐
│            domtrip-core             │
│  ┌─────────────┐ ┌─────────────────┐│
│  │ Editor      │ │ Document        ││
│  │             │ │                 ││
│  │ - Lossless  │ │ - Parsing       ││
│  │   editing   │ │ - Serialization ││
│  │ - Formatting│ │ - Navigation    ││
│  │   preserve  │ │                 ││
│  └─────────────┘ └─────────────────┘│
└─────────────────────────────────────┘
```

## Use Cases

The Maven extension is perfect for:

### 🛠️ **Maven Tooling Development**
- Build tools that need to modify POM files
- IDE plugins for Maven project management
- Automated dependency management tools
- POM validation and cleanup utilities

### 🔄 **POM Transformation**
- Upgrading Maven versions while preserving formatting
- Migrating between different Maven configurations
- Batch updates across multiple projects
- Template-based POM generation

### 📊 **POM Analysis**
- Dependency analysis tools
- Security scanning with POM modification
- License compliance checking
- Build optimization tools

## Comparison with Standard XML Libraries

| Feature | Standard XML | DomTrip Core | DomTrip Maven |
|---------|-------------|--------------|---------------|
| Formatting Preservation | ❌ | ✅ | ✅ |
| Comment Preservation | ❌ | ✅ | ✅ |
| Whitespace Preservation | ❌ | ✅ | ✅ |
| Maven Element Ordering | ❌ | ❌ | ✅ |
| Maven Conventions | ❌ | ❌ | ✅ |
| POM-specific Methods | ❌ | ❌ | ✅ |
| Maven Constants | ❌ | ❌ | ✅ |

## Getting Started

Ready to start using the Maven extension? Check out:

- [Installation Guide](/docs/maven/installation/) - Add the Maven extension to your project
- [Quick Start](/docs/maven/quick-start/) - Your first POM editing example
- [API Reference](/docs/maven/api/) - Complete PomEditor API documentation
- [Examples](/docs/maven/examples/) - Real-world usage examples

## Next Steps

- Learn about [Maven-specific installation](/docs/maven/installation/)
- Try the [Maven quick start guide](/docs/maven/quick-start/)
- Explore [element ordering rules](/docs/maven/ordering/)
- Browse [Maven examples](/docs/maven/examples/)
