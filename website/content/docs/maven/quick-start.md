---
title: Maven Quick Start
description: Get started with DomTrip Maven extension in 5 minutes
---

# Maven Quick Start

Get up and running with the DomTrip Maven extension in just a few minutes. This guide shows you the most common use cases with practical examples.

## Your First POM Edit

Let's start by modifying an existing POM file:

```java
import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.Element;
import org.maveniverse.domtrip.maven.PomEditor;
import static org.maveniverse.domtrip.maven.MavenPomElements.Elements.*;

// Parse an existing POM
String pomXml = """
    <?xml version="1.0" encoding="UTF-8"?>
    <project xmlns="http://maven.apache.org/POM/4.0.0">
      <modelVersion>4.0.0</modelVersion>
      <groupId>com.example</groupId>
      <artifactId>my-project</artifactId>
      <version>1.0.0</version>
    </project>
    """;

Document doc = Document.of(pomXml);
PomEditor editor = new PomEditor(doc);
Element root = editor.root();

// Add elements with automatic ordering
editor.insertMavenElement(root, NAME, "My Example Project");
editor.insertMavenElement(root, DESCRIPTION, "A sample project");

// Generate the result
String result = editor.toXml();
System.out.println(result);
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
// Create a new POM document
PomEditor editor = new PomEditor();
editor.createMavenDocument("project");
Element root = editor.root();

// Add basic project information
editor.insertMavenElement(root, MODEL_VERSION, "4.0.0");
editor.insertMavenElement(root, GROUP_ID, "com.example");
editor.insertMavenElement(root, ARTIFACT_ID, "new-project");
editor.insertMavenElement(root, VERSION, "1.0.0");
editor.insertMavenElement(root, PACKAGING, "jar");

// Add project metadata
editor.insertMavenElement(root, NAME, "New Project");
editor.insertMavenElement(root, DESCRIPTION, "A brand new project");

// Add properties
Element properties = editor.insertMavenElement(root, PROPERTIES);
editor.addProperty(properties, "maven.compiler.source", "17");
editor.addProperty(properties, "maven.compiler.target", "17");
editor.addProperty(properties, "project.build.sourceEncoding", "UTF-8");

String result = editor.toXml();
```

## Adding Dependencies

```java
// Add dependencies section
Element dependencies = editor.insertMavenElement(root, DEPENDENCIES);

// Add JUnit dependency
editor.addDependency(dependencies, "org.junit.jupiter", "junit-jupiter", "5.9.2");

// Find the dependency we just added and set its scope
Element junitDep = editor.findChildElement(dependencies, DEPENDENCY);
editor.insertMavenElement(junitDep, SCOPE, "test");

// Add another dependency
editor.addDependency(dependencies, "org.slf4j", "slf4j-api", "2.0.7");
```

## Adding Plugins

```java
// Add build section
Element build = editor.insertMavenElement(root, BUILD);
Element plugins = editor.insertMavenElement(build, PLUGINS);

// Add Maven Compiler Plugin
Element compilerPlugin = editor.addPlugin(plugins, 
    "org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");

// Add configuration to the plugin
Element config = editor.insertMavenElement(compilerPlugin, CONFIGURATION);
editor.addElement(config, "source", "17");
editor.addElement(config, "target", "17");

// Add Surefire Plugin (no version needed - will be managed)
editor.addPlugin(plugins, "org.apache.maven.plugins", "maven-surefire-plugin", null);
```

## Working with Multi-Module Projects

```java
// Create parent POM
PomEditor parentEditor = new PomEditor();
parentEditor.createMavenDocument("project");
Element parentRoot = parentEditor.root();

// Set up parent project
parentEditor.insertMavenElement(parentRoot, MODEL_VERSION, "4.0.0");
parentEditor.insertMavenElement(parentRoot, GROUP_ID, "com.example");
parentEditor.insertMavenElement(parentRoot, ARTIFACT_ID, "parent-project");
parentEditor.insertMavenElement(parentRoot, VERSION, "1.0.0");
parentEditor.insertMavenElement(parentRoot, PACKAGING, "pom");

// Add modules
Element modules = parentEditor.insertMavenElement(parentRoot, MODULES);
parentEditor.addModule(modules, "core");
parentEditor.addModule(modules, "web");
parentEditor.addModule(modules, "cli");
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
            "org.junit.jupiter", "junit-jupiter", "${junit.version}");
        editor.insertMavenElement(junitDep, SCOPE, "test");

        // Build configuration
        Element build = editor.insertMavenElement(root, BUILD);
        Element plugins = editor.insertMavenElement(build, PLUGINS);
        
        // Compiler plugin
        Element compilerPlugin = editor.addPlugin(plugins, 
            "org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");
        Element compilerConfig = editor.insertMavenElement(compilerPlugin, CONFIGURATION);
        editor.addElement(compilerConfig, "source", "${maven.compiler.source}");
        editor.addElement(compilerConfig, "target", "${maven.compiler.target}");

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
