---
title: Examples
description: Practical examples of using DomTrip for XML editing
layout: page
---

# DomTrip Examples

Explore practical examples of using DomTrip for various XML editing scenarios.

## Basic XML Editing

### Simple Element Modification

```java
import eu.maveniverse.domtrip.*;

String xml = """
    <config>
        <database>
            <host>localhost</host>
            <port>5432</port>
        </database>
    </config>
    """;

Editor editor = new Editor(xml);

// Find and update the host
Element host = editor.findElement("host");
editor.setTextContent(host, "production-db.example.com");

// Find and update the port
Element port = editor.findElement("port");
editor.setTextContent(port, "5433");

String result = editor.toXml();
```

### Adding New Elements

```java
Editor editor = new Editor("<project></project>");

Element root = editor.getDocumentElement();

// Add dependencies section
Element dependencies = editor.addElement(root, "dependencies");

// Add a dependency with multiple children
Element dependency = editor.addElement(dependencies, "dependency");
editor.addElement(dependency, "groupId", "junit");
editor.addElement(dependency, "artifactId", "junit");
editor.addElement(dependency, "version", "4.13.2");
editor.addElement(dependency, "scope", "test");

String result = editor.toXml();
```

## Maven POM Editing

### Adding Dependencies

```java
String pomXml = """
    <?xml version="1.0" encoding="UTF-8"?>
    <project xmlns="http://maven.apache.org/POM/4.0.0">
        <modelVersion>4.0.0</modelVersion>
        <groupId>com.example</groupId>
        <artifactId>my-app</artifactId>
        <version>1.0.0</version>
    </project>
    """;

Editor editor = new Editor(pomXml);
Element project = editor.getDocumentElement();

// Add dependencies section if it doesn't exist
Element dependencies = editor.findElement("dependencies");
if (dependencies == null) {
    dependencies = editor.addElement(project, "dependencies");
}

// Add Spring Boot starter
Element springDep = editor.addElement(dependencies, "dependency");
editor.addElement(springDep, "groupId", "org.springframework.boot");
editor.addElement(springDep, "artifactId", "spring-boot-starter-web");
editor.addElement(springDep, "version", "3.2.0");

String result = editor.toXml();
```

### Updating Version

```java
Editor editor = new Editor(pomContent);

// Update project version
Element version = editor.findElement("version");
if (version != null) {
    editor.setTextContent(version, "2.0.0");
}

// Update parent version if exists
Element parent = editor.findElement("parent");
if (parent != null) {
    Element parentVersion = editor.findChild(parent, "version");
    if (parentVersion != null) {
        editor.setTextContent(parentVersion, "2.1.0");
    }
}
```

## Configuration File Editing

### Spring Configuration

```java
String springConfig = """
    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans">
        <bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource">
            <property name="driverClassName" value="org.postgresql.Driver"/>
            <property name="url" value="jdbc:postgresql://localhost:5432/mydb"/>
        </bean>
    </beans>
    """;

Editor editor = new Editor(springConfig);

// Find the dataSource bean
Element dataSource = editor.findElementByAttribute("id", "dataSource");

// Update the URL property
Element urlProperty = editor.findChildByAttribute(dataSource, "name", "url");
if (urlProperty != null) {
    editor.setAttribute(urlProperty, "value", "jdbc:postgresql://prod-db:5432/mydb");
}

// Add new property
Element newProperty = editor.addElement(dataSource, "property");
editor.setAttribute(newProperty, "name", "maxActive");
editor.setAttribute(newProperty, "value", "100");
```

## Advanced Features

### Working with Namespaces

```java
String xmlWithNamespaces = """
    <?xml version="1.0" encoding="UTF-8"?>
    <project xmlns="http://maven.apache.org/POM/4.0.0"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <modelVersion>4.0.0</modelVersion>
    </project>
    """;

Editor editor = new Editor(xmlWithNamespaces);

// Add element with namespace
Element project = editor.getDocumentElement();
Element build = editor.addElement(project, "build");
Element plugins = editor.addElement(build, "plugins");

// Elements inherit the default namespace automatically
Element plugin = editor.addElement(plugins, "plugin");
editor.addElement(plugin, "groupId", "org.apache.maven.plugins");
editor.addElement(plugin, "artifactId", "maven-compiler-plugin");
```

### Using Builder Patterns

```java
// Create elements using builders
Element dependency = Element.builder("dependency")
    .withChild(Element.textElement("groupId", "org.junit.jupiter"))
    .withChild(Element.textElement("artifactId", "junit-jupiter"))
    .withChild(Element.textElement("version", "5.9.2"))
    .withChild(Element.textElement("scope", "test"))
    .build();

// Add to existing document
Editor editor = new Editor(pomXml);
Element dependencies = editor.findElement("dependencies");
editor.addElement(dependencies, dependency);
```

### Attribute Manipulation

```java
Editor editor = new Editor(xmlContent);

// Set attributes
Element element = editor.findElement("dependency");
editor.setAttribute(element, "scope", "test");
editor.setAttribute(element, "optional", "true");

// Remove attributes
editor.removeAttribute(element, "scope");

// Check if attribute exists
if (editor.hasAttribute(element, "optional")) {
    String value = editor.getAttribute(element, "optional");
    System.out.println("Optional: " + value);
}
```

## Error Handling

```java
try {
    Editor editor = new Editor(xmlContent);
    
    // Safe element finding
    Element element = editor.findElement("nonexistent");
    if (element == null) {
        System.out.println("Element not found");
        return;
    }
    
    // Modify element
    editor.setTextContent(element, "new value");
    
} catch (DomTripException e) {
    System.err.println("XML processing error: " + e.getMessage());
} catch (Exception e) {
    System.err.println("Unexpected error: " + e.getMessage());
}
```

## Best Practices

### 1. Always Check for Null

```java
Element element = editor.findElement("optional-element");
if (element != null) {
    editor.setTextContent(element, "value");
}
```

### 2. Use Fluent API for Complex Operations

```java
editor.findElement("dependencies")
      .map(deps -> editor.addElement(deps, "dependency"))
      .ifPresent(dep -> {
          editor.addElement(dep, "groupId", "org.example");
          editor.addElement(dep, "artifactId", "example-lib");
      });
```

### 3. Preserve Original Formatting

```java
// DomTrip automatically preserves formatting
// No special configuration needed
String result = editor.toXml(); // Maintains original indentation and style
```

## Next Steps

- [API Reference](/docs/api/) - Complete API documentation
- [Advanced Features](/docs/advanced/) - Builder patterns and advanced usage
- [Migration Guide](/docs/migration/) - Moving from other XML libraries
