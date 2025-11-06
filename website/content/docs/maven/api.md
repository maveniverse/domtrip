---
title: PomEditor API Reference
description: Complete API reference for the PomEditor class and Maven extension
layout: page
---

# PomEditor API Reference

The `PomEditor` class extends the core `Editor` class with Maven-specific functionality for working with POM files.

## Class Overview

```java
public class PomEditor extends Editor {
    // Constructors
    public PomEditor()
    public PomEditor(DomTripConfig config)
    public PomEditor(Document document)
    public PomEditor(Document document, DomTripConfig config)
    
    // Maven-specific methods
    public Element insertMavenElement(Element parent, String elementName)
    public Element insertMavenElement(Element parent, String elementName, String textContent)
    public Element findChildElement(Element parent, String elementName)
    public void createMavenDocument(String rootElementName)
    
    // Convenience methods
    public Element addDependency(Element dependenciesElement, String groupId, String artifactId, String version)
    public Element addPlugin(Element pluginsElement, String groupId, String artifactId, String version)
    public Element addModule(Element modulesElement, String moduleName)
    public Element addProperty(Element propertiesElement, String propertyName, String propertyValue)
}
```

## Constructors

### Default Constructor
```java
PomEditor editor = new PomEditor();
```
Creates a new PomEditor with default configuration.

### With Configuration
```java
DomTripConfig config = DomTripConfig.builder()
    .indentSize(4)
    .build();
PomEditor editor = new PomEditor(config);
```
Creates a PomEditor with custom configuration.

### With Existing Document
```java
Document doc = Document.of(pomXmlString);
PomEditor editor = new PomEditor(doc);
```
Creates a PomEditor for an existing Document.

### With Document and Configuration
```java
Document doc = Document.of(pomXmlString);
DomTripConfig config = DomTripConfig.builder()
    .preserveWhitespace(true)
    .build();
PomEditor editor = new PomEditor(doc, config);
```

## Core Methods

### insertMavenElement()

Inserts elements with Maven-aware ordering and formatting.

#### Basic Usage
```java
Element insertMavenElement(Element parent, String elementName)
```

**Example:**
```java
Element root = editor.root();
Element dependencies = editor.insertMavenElement(root, "dependencies");
```

#### With Text Content
```java
Element insertMavenElement(Element parent, String elementName, String textContent)
```

**Example:**
```java
editor.insertMavenElement(root, "groupId", "com.example");
editor.insertMavenElement(root, "artifactId", "my-project");
editor.insertMavenElement(root, "version", "1.0.0");
```

**Features:**
- Automatically orders elements according to Maven conventions
- Adds appropriate blank lines between element groups
- Preserves existing formatting and comments
- Handles nested element structures intelligently

### findChildElement()

Finds a direct child element by name.

```java
Element findChildElement(Element parent, String elementName)
```

**Example:**
```java
Element root = editor.root();
Element dependencies = editor.findChildElement(root, "dependencies");
if (dependencies == null) {
    dependencies = editor.insertMavenElement(root, "dependencies");
}
```

**Returns:** The child element if found, `null` otherwise.

### createMavenDocument()

Creates a new Maven POM document with proper namespace.

```java
void createMavenDocument(String rootElementName)
```

**Example:**
```java
PomEditor editor = new PomEditor();
editor.createMavenDocument("project");
Element root = editor.root();
// root now has xmlns="http://maven.apache.org/POM/4.0.0"
```

## Convenience Methods

### addDependency()

Adds a dependency with proper structure and ordering.

```java
Element addDependency(Element dependenciesElement, String groupId, String artifactId, String version)
```

**Example:**
```java
Element dependencies = editor.insertMavenElement(root, "dependencies");
Element dep = editor.addDependency(dependencies, "org.junit.jupiter", "junit-jupiter", "5.9.2");

// Add additional elements to the dependency
editor.insertMavenElement(dep, "scope", "test");
editor.insertMavenElement(dep, "optional", "true");
```

**Generated Structure:**
```xml
<dependency>
  <groupId>org.junit.jupiter</groupId>
  <artifactId>junit-jupiter</artifactId>
  <version>5.9.2</version>
  <scope>test</scope>
  <optional>true</optional>
</dependency>
```

### addPlugin()

Adds a plugin with proper structure and ordering.

```java
Element addPlugin(Element pluginsElement, String groupId, String artifactId, String version)
```

**Example:**
```java
Element build = editor.insertMavenElement(root, "build");
Element plugins = editor.insertMavenElement(build, "plugins");

Element plugin = editor.addPlugin(plugins, 
    "org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");

// Add configuration
Element config = editor.insertMavenElement(plugin, "configuration");
editor.addElement(config, "source", "17");
editor.addElement(config, "target", "17");
```

**Notes:**
- `groupId` can be `null` for plugins with default groupId
- `version` can be `null` for plugins managed by parent or pluginManagement

### addModule()

Adds a module to a multi-module project.

```java
Element addModule(Element modulesElement, String moduleName)
```

**Example:**
```java
Element modules = editor.insertMavenElement(root, "modules");
editor.addModule(modules, "core");
editor.addModule(modules, "web");
editor.addModule(modules, "cli");
```

**Generated Structure:**
```xml
<modules>
  <module>core</module>
  <module>web</module>
  <module>cli</module>
</modules>
```

### addProperty()

Adds a property to the properties section.

```java
Element addProperty(Element propertiesElement, String propertyName, String propertyValue)
```

**Example:**
```java
Element properties = editor.insertMavenElement(root, "properties");
editor.addProperty(properties, "maven.compiler.source", "17");
editor.addProperty(properties, "maven.compiler.target", "17");
editor.addProperty(properties, "project.build.sourceEncoding", "UTF-8");
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
11. `mailingLists`
12. *blank line*
13. `prerequisites`
14. *blank line*
15. `modules`
16. *blank line*
17. `scm`, `issueManagement`, `ciManagement`, `distributionManagement`
18. *blank line*
19. `properties`
20. *blank line*
21. `dependencyManagement`, `dependencies`
22. *blank line*
23. `repositories`, `pluginRepositories`
24. *blank line*
25. `build`
26. *blank line*
27. `reporting`
28. *blank line*
29. `profiles`

### Build Elements
1. `defaultGoal`, `directory`, `finalName`
2. `sourceDirectory`, `scriptSourceDirectory`, `testSourceDirectory`
3. `outputDirectory`, `testOutputDirectory`
4. `extensions`
5. *blank line*
6. `pluginManagement`, `plugins`

### Plugin Elements
1. `groupId`, `artifactId`, `version`
2. `extensions`
3. `executions`
4. `dependencies`
5. `goals`
6. `inherited`
7. `configuration`

### Dependency Elements
1. `groupId`, `artifactId`, `version`
2. `classifier`, `type`
3. `scope`
4. `systemPath`
5. `optional`
6. `exclusions`

## Error Handling

The PomEditor throws `DomTripException` for various error conditions:

```java
try {
    editor.insertMavenElement(root, "invalidElement", "value");
} catch (DomTripException e) {
    System.err.println("Error: " + e.getMessage());
}
```

Common error scenarios:
- Invalid XML structure
- Null parent elements
- Invalid element names
- Document parsing errors

## Integration with Core Editor

PomEditor inherits all methods from the core `Editor` class:

```java
// Core Editor methods are available
Element element = editor.addElement(parent, "customElement");
editor.removeElement(element);
editor.insertElementBefore(referenceElement, "newElement");
editor.insertElementAfter(referenceElement, "newElement");

// Comment methods
editor.addComment(element, "This is a comment");

// Serialization
String xml = editor.toXml();
byte[] bytes = editor.toBytes();
```

## Best Practices

### Use Constants
```java
import static org.maveniverse.domtrip.maven.MavenPomElements.Elements.*;

// Good
editor.insertMavenElement(root, GROUP_ID, "com.example");

// Avoid
editor.insertMavenElement(root, "groupId", "com.example");
```

### Check for Existing Elements
```java
Element dependencies = editor.findChildElement(root, DEPENDENCIES);
if (dependencies == null) {
    dependencies = editor.insertMavenElement(root, DEPENDENCIES);
}
```

### Handle Null Versions
```java
// For managed dependencies/plugins
editor.addDependency(dependencies, "org.junit.jupiter", "junit-jupiter", null);
editor.addPlugin(plugins, "org.apache.maven.plugins", "maven-surefire-plugin", null);
```

### Preserve Existing Structure
```java
// Work with existing POMs
Document doc = Document.of(existingPomContent);
PomEditor editor = new PomEditor(doc);
// Modifications preserve original formatting and comments
```
