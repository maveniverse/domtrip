# DomTrip Maven

Maven-specific extensions for the DomTrip XML editing library.

## Overview

The `domtrip-maven` module provides specialized classes for working with Maven POM files, extending the core DomTrip functionality with Maven-specific features:

- **Maven Element Ordering** - Automatically orders elements according to Maven conventions
- **Formatting Preservation** - Maintains original formatting, whitespace, and comments
- **Intelligent Blank Lines** - Adds appropriate blank lines between element groups
- **Maven-specific Methods** - Convenience methods for common POM operations

## Key Classes

### PomEditor

The `PomEditor` class extends the base `Editor` class with Maven-specific functionality:

```java
// Parse existing POM
Document doc = Document.of(pomXmlString);
PomEditor editor = new PomEditor(doc);

// Add elements with proper ordering
Element root = editor.root();
editor.insertMavenElement(root, "description", "My project description");
editor.insertMavenElement(root, "name", "My Project");  // Will be ordered before description

// Serialize with preserved formatting
String result = editor.toXml();
```

### MavenPomElements

The `MavenPomElements` class provides constants for Maven POM elements, attributes, and other XML constructs:

```java
// Use constants for element names
editor.insertMavenElement(root, MavenPomElements.Elements.GROUP_ID, "com.example");
editor.insertMavenElement(root, MavenPomElements.Elements.ARTIFACT_ID, "my-project");

// Access namespace constants
String namespace = MavenPomElements.Namespaces.MAVEN_4_0_0_NAMESPACE;
```

## Features

### Element Ordering

The PomEditor automatically orders elements according to Maven POM conventions:

- **Project elements**: modelVersion, parent, groupId, artifactId, version, packaging, name, description, etc.
- **Build elements**: defaultGoal, directory, finalName, sourceDirectory, etc.
- **Plugin elements**: groupId, artifactId, version, extensions, executions, etc.
- **Dependency elements**: groupId, artifactId, version, classifier, type, scope, etc.

### Convenience Methods

The PomEditor provides convenience methods for common Maven operations:

```java
// Add dependencies
Element dependencies = editor.insertMavenElement(root, DEPENDENCIES);
editor.addDependency(dependencies, "org.junit.jupiter", "junit-jupiter", "5.9.2");

// Add plugins
Element build = editor.insertMavenElement(root, BUILD);
Element plugins = editor.insertMavenElement(build, PLUGINS);
editor.addPlugin(plugins, "org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");

// Add modules
Element modules = editor.insertMavenElement(root, MODULES);
editor.addModule(modules, "module1");

// Add properties
Element properties = editor.insertMavenElement(root, PROPERTIES);
editor.addProperty(properties, "maven.compiler.source", "17");
```

## Complete Example

```java
// Create a new POM from scratch
PomEditor editor = new PomEditor();
editor.createMavenDocument("project");
Element root = editor.root();

// Add basic project information
editor.insertMavenElement(root, MODEL_VERSION, "4.0.0");
editor.insertMavenElement(root, GROUP_ID, "com.example");
editor.insertMavenElement(root, ARTIFACT_ID, "my-project");
editor.insertMavenElement(root, VERSION, "1.0.0");
editor.insertMavenElement(root, PACKAGING, "jar");

// Add project metadata
editor.insertMavenElement(root, NAME, "My Example Project");
editor.insertMavenElement(root, DESCRIPTION, "An example project");

// Add properties
Element properties = editor.insertMavenElement(root, PROPERTIES);
editor.addProperty(properties, "maven.compiler.source", "17");
editor.addProperty(properties, "maven.compiler.target", "17");

// Add dependencies
Element dependencies = editor.insertMavenElement(root, DEPENDENCIES);
editor.addDependency(dependencies, "org.junit.jupiter", "junit-jupiter", "5.9.2");

// Add build section with plugins
Element build = editor.insertMavenElement(root, BUILD);
Element plugins = editor.insertMavenElement(build, PLUGINS);
editor.addPlugin(plugins, "org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");

// Generate the XML
String result = editor.toXml();
```

This will generate a properly formatted Maven POM with correct element ordering and appropriate blank lines between sections.

## Dependencies

This module depends on:
- `domtrip-core` - The core DomTrip XML editing library
- `junit-jupiter` (test scope) - For unit testing

## Maven Coordinates

```xml
<dependency>
    <groupId>eu.maveniverse</groupId>
    <artifactId>domtrip-maven</artifactId>
    <version>0.1-SNAPSHOT</version>
</dependency>
```

## License

This project is licensed under the Eclipse Public License v2.0.
