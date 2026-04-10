---
title: Maven Extension Overview
description: Overview of DomTrip's Maven-specific extensions for POM file editing
layout: page
---

# Maven Extension Overview

The DomTrip Maven extension (`domtrip-maven`) provides specialized functionality for working with Maven configuration files, extending the core DomTrip library with Maven-specific features and conventions.

## Key Features

### Maven-Aware Element Ordering
The `PomEditor` automatically orders elements according to Maven POM conventions:
- **Project elements**: `modelVersion`, `parent`, `groupId`, `artifactId`, `version`, `packaging`, `name`, `description`, etc.
- **Build elements**: `defaultGoal`, `directory`, `finalName`, `sourceDirectory`, etc.
- **Plugin elements**: `groupId`, `artifactId`, `version`, `extensions`, `executions`, etc.
- **Dependency elements**: `groupId`, `artifactId`, `version`, `classifier`, `type`, `scope`, etc.

### Intelligent Blank Line Management
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

### Sub-Object APIs
Domain-specific APIs accessed via the PomEditor:

| API | Access | Purpose |
|-----|--------|---------|
| **Dependencies** | `editor.dependencies()` | CRUD, exclusions, alignment, convention detection, profile scoping |
| **Plugins** | `editor.plugins()` | CRUD, pluginManagement |
| **Properties** | `editor.properties()` | CRUD |
| **Subprojects** | `editor.subprojects()` | Module management |
| **Parent** | `editor.parent()` | Parent POM management |
| **Profiles** | `editor.profiles()` | Profile lookup |

### Convention Detection & Alignment
Automatically detect how your project manages dependency versions (managed vs inline,
property vs literal, naming conventions) and add or transform dependencies to follow
those conventions consistently.

### Cross-POM Alignment
Move dependency versions from child POMs to a parent POM's `<dependencyManagement>`,
with automatic property migration.

### Specialized Editors
In addition to `PomEditor`, the Maven extension includes editors for other Maven files:
- **SettingsEditor**: `addServer()`, `addMirror()`, `addProxy()`, `addProfile()`
- **ExtensionsEditor**: `addExtension()` with proper coordinates
- **ToolchainsEditor**: `addJdkToolchain()`, `addToolchain()`

### Type-Safe Constants
Comprehensive constants classes for all Maven file types:
- **MavenPomElements**: POM elements, attributes, namespaces, and schema locations
- **MavenSettingsElements**: Settings elements, values, and configuration options
- **MavenExtensionsElements**: Extensions elements and common extension types
- **MavenToolchainsElements**: Toolchain elements, types, and common vendors

## Architecture

The Maven extension is built on top of the core DomTrip library:

```
┌─────────────────────────────────────────────────────────────────────┐
│                         domtrip-maven                               │
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────────┐│
│  │                       PomEditor                                 ││
│  │                                                                 ││
│  │  dependencies() ─── Dependencies                                ││
│  │    ├── CRUD (add, update, delete)                               ││
│  │    ├── Exclusions (add, delete, has)                            ││
│  │    ├── Convention Detection (style, source, naming)             ││
│  │    ├── Alignment (addAligned, alignDependency, alignAll)        ││
│  │    ├── Cross-POM (alignToParent, alignAllToParent)              ││
│  │    └── Profile Scoping (forProfile)                             ││
│  │                                                                 ││
│  │  plugins()      ─── Plugins (add, update, delete, management)   ││
│  │  properties()   ─── Properties (add, update, delete)            ││
│  │  subprojects()  ─── Subprojects (addModule, add/remove)         ││
│  │  parent()       ─── Parent (set, update, delete)                ││
│  │  profiles()     ─── Profiles (find, has)                        ││
│  └─────────────────────────────────────────────────────────────────┘│
│                                                                     │
│  ┌──────────────┐ ┌────────────────┐ ┌──────────────┐               │
│  │SettingsEditor│ │ExtensionsEditor│ │ToolchainsEd. │               │
│  └──────────────┘ └────────────────┘ └──────────────┘               │
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────────┐│
│  │  AlignOptions  │  Coordinates  │  Constants Classes             ││
│  └─────────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────────┘
                              │ extends
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                          domtrip-core                               │
│  ┌─────────────┐ ┌─────────────────┐ ┌─────────────────────┐        │
│  │ Editor      │ │ Document        │ │ Configuration       │        │
│  │ - Lossless  │ │ - Parsing       │ │ - Formatting        │        │
│  │   editing   │ │ - Serialization │ │ - Whitespace        │        │
│  │ - Formatting│ │ - Navigation    │ │ - Indentation       │        │
│  │   preserve  │ │ - Validation    │ │                     │        │
│  └─────────────┘ └─────────────────┘ └─────────────────────┘        │
└─────────────────────────────────────────────────────────────────────┘
```

## Use Cases

The Maven extension is perfect for:

### Maven Tooling Development
- Build tools that need to modify POM files
- IDE plugins for Maven project management
- Automated dependency management tools
- POM validation and cleanup utilities

### POM Transformation
- Upgrading Maven versions while preserving formatting
- Migrating between different Maven configurations
- Batch updates across multiple projects
- Template-based POM generation

### Multi-Module Management
- Moving dependency versions to parent POMs
- Aligning version conventions across modules
- Maintaining consistent dependency management

## Comparison with Standard XML Libraries

| Feature | Standard XML | DomTrip Core | DomTrip Maven |
|---------|-------------|--------------|---------------|
| Formatting Preservation | ❌ | ✅ | ✅ |
| Comment Preservation | ❌ | ✅ | ✅ |
| Whitespace Preservation | ❌ | ✅ | ✅ |
| Maven Element Ordering | ❌ | ❌ | ✅ |
| Maven Conventions | ❌ | ❌ | ✅ |
| Dependency Alignment | ❌ | ❌ | ✅ |
| Cross-POM Operations | ❌ | ❌ | ✅ |
| Maven Constants | ❌ | ❌ | ✅ |

## Getting Started

Ready to start using the Maven extension? Check out:

- [Installation Guide](../installation/) - Add the Maven extension to your project
- [Quick Start](../quick-start/) - Your first POM editing example
- [API Reference](../api/) - Complete PomEditor API documentation
- [Examples](../examples/) - Real-world usage examples
