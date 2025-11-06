---
title: Maven Extension Overview
description: Overview of DomTrip's Maven-specific extensions for POM file editing
layout: page
---

# Maven Extension Overview

The DomTrip Maven extension (`domtrip-maven`) provides specialized functionality for working with Maven configuration files, extending the core DomTrip library with Maven-specific features and conventions.

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
- **PomEditor**: `addDependency()`, `addPlugin()`, `addModule()`, `addProperty()`
- **SettingsEditor**: `addServer()`, `addMirror()`, `addProxy()`, `addProfile()`
- **ExtensionsEditor**: `addExtension()` with proper coordinates
- **ToolchainsEditor**: `addJdkToolchain()`, `addNetBeansToolchain()`, `addToolchain()`

### 🔧 **Type-Safe Constants**
Comprehensive constants classes for all Maven file types:
- **MavenPomElements**: POM elements, attributes, namespaces, and schema locations
- **MavenSettingsElements**: Settings elements, values, and configuration options
- **MavenExtensionsElements**: Extensions elements and common extension types
- **MavenToolchainsElements**: Toolchain elements, types, and common vendors

## Architecture

The Maven extension is built on top of the core DomTrip library:

```
┌─────────────────────────────────────────────────────────────┐
│                      domtrip-maven                          │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────┐│
│  │ PomEditor   │ │SettingsEditor│ │ExtensionsEd.│ │Toolchain││
│  │             │ │             │ │             │ │Editor   ││
│  │ - POM       │ │ - Settings  │ │ - Extensions│ │ - Tool  ││
│  │   ordering  │ │   ordering  │ │   ordering  │ │   chains││
│  │ - Deps/     │ │ - Servers/  │ │ - Extension │ │ - JDK   ││
│  │   Plugins   │ │   Mirrors   │ │   coords    │ │   setup ││
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────┘│
│  ┌─────────────────────────────────────────────────────────┐│
│  │                   Constants Classes                     ││
│  │ MavenPomElements | MavenSettingsElements |              ││
│  │ MavenExtensionsElements | MavenToolchainsElements       ││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                        domtrip-core                         │
│  ┌─────────────┐ ┌─────────────────┐ ┌─────────────────────┐│
│  │ Editor      │ │ Document        │ │ Configuration       ││
│  │             │ │                 │ │                     ││
│  │ - Lossless  │ │ - Parsing       │ │ - Formatting        ││
│  │   editing   │ │ - Serialization │ │ - Whitespace        ││
│  │ - Formatting│ │ - Navigation    │ │ - Indentation       ││
│  │   preserve  │ │ - Validation    │ │                     ││
│  └─────────────┘ └─────────────────┘ └─────────────────────┘│
└─────────────────────────────────────────────────────────────┘
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
