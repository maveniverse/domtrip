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

**Output:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.example</groupId>
  <artifactId>my-project</artifactId>
  <version>1.0.0</version>
  
  <name>My Example Project</name>
  <description>A sample project</description>
</project>
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
// Find dependencies section
Element dependencies = editor.findChildElement(root, DEPENDENCIES);
if (dependencies == null) {
    dependencies = editor.insertMavenElement(root, DEPENDENCIES);
}

// Add a new dependency
editor.addDependency(dependencies, "com.fasterxml.jackson.core", "jackson-core", "2.15.2");
```

### Using Constants for Type Safety

```java
import static org.maveniverse.domtrip.maven.MavenPomElements.Elements.*;
import static org.maveniverse.domtrip.maven.MavenPomElements.Namespaces.*;

// Use constants instead of string literals
editor.insertMavenElement(root, GROUP_ID, "com.example");
editor.insertMavenElement(root, ARTIFACT_ID, "my-project");

// Access namespace constants
root.attribute("xmlns", MAVEN_4_0_0_NAMESPACE);
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
editor.insertMavenElement(editor.root(), NAME, "Existing Project");

// The result maintains the original structure and comments
String result = editor.toXml();
```

## Complete Example

Here's a complete example that creates a full Maven project POM:

```java
public class CompletePomExample {
    public static void main(String[] args) {
        PomEditor editor = new PomEditor();
        editor.createMavenDocument("project");
        Element root = editor.root();

        // Basic project info
        editor.insertMavenElement(root, MODEL_VERSION, "4.0.0");
        editor.insertMavenElement(root, GROUP_ID, "com.example");
        editor.insertMavenElement(root, ARTIFACT_ID, "complete-example");
        editor.insertMavenElement(root, VERSION, "1.0.0");
        editor.insertMavenElement(root, PACKAGING, "jar");
        
        // Metadata
        editor.insertMavenElement(root, NAME, "Complete Example");
        editor.insertMavenElement(root, DESCRIPTION, "A complete Maven project example");
        editor.insertMavenElement(root, URL, "https://github.com/example/complete-example");

        // Properties
        Element properties = editor.insertMavenElement(root, PROPERTIES);
        editor.addProperty(properties, "maven.compiler.source", "17");
        editor.addProperty(properties, "maven.compiler.target", "17");
        editor.addProperty(properties, "project.build.sourceEncoding", "UTF-8");
        editor.addProperty(properties, "junit.version", "5.9.2");

        // Dependencies
        Element dependencies = editor.insertMavenElement(root, DEPENDENCIES);
        
        // Production dependencies
        editor.addDependency(dependencies, "org.slf4j", "slf4j-api", "2.0.7");
        editor.addDependency(dependencies, "ch.qos.logback", "logback-classic", "1.4.7");
        
        // Test dependencies
        Element junitDep = editor.addDependency(dependencies, 
            "org.junit.jupiter", "junit-jupiter", "$\{junit.version\}");
        editor.insertMavenElement(junitDep, SCOPE, "test");

        // Build configuration
        Element build = editor.insertMavenElement(root, BUILD);
        Element plugins = editor.insertMavenElement(build, PLUGINS);
        
        // Compiler plugin
        Element compilerPlugin = editor.addPlugin(plugins, 
            "org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");
        Element compilerConfig = editor.insertMavenElement(compilerPlugin, CONFIGURATION);
        editor.addElement(compilerConfig, "source", "$\{maven.compiler.source\}");
        editor.addElement(compilerConfig, "target", "$\{maven.compiler.target\}");

        // Surefire plugin
        editor.addPlugin(plugins, "org.apache.maven.plugins", "maven-surefire-plugin", "3.0.0");

        System.out.println(editor.toXml());
    }
}
```

## Next Steps

Now that you've seen the basics:

1. **Explore the API**: Check out the [PomEditor API Reference](/docs/maven/api/)
2. **Learn Element Ordering**: Understand how [Maven Element Ordering](/docs/maven/ordering/) works
3. **See More Examples**: Browse [Maven Examples](/docs/maven/examples/) for advanced use cases
4. **Core Features**: Learn about [DomTrip Core Features](/docs/features/) that work with Maven extension
