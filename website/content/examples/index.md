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
{cdi:snippets.snippet('simple-element-modification')}}
```

### Adding New Elements

```java
{cdi:snippets.snippet('adding-new-elements')}}
```

## Maven POM Editing

### Adding Dependencies

```java
{cdi:snippets.snippet('maven-pom-adding-dependencies')}}
```

### Updating Version

```java
{cdi:snippets.snippet('maven-pom-updating-version')}}
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
{cdi:snippets.snippet('attribute-manipulation')}}
```

## Error Handling

```java
{cdi:snippets.snippet('error-handling')}}
```

## Best Practices

### 1. Always Use Optional for Safe Navigation

```java
{cdi:snippets.snippet('best-practices-optional')}}
```

### 2. Preserve Original Formatting

```java
{cdi:snippets.snippet('best-practices-preserve-formatting')}}
```

## Next Steps

- [API Reference](/docs/api/) - Complete API documentation
- [Advanced Features](/docs/advanced/) - Builder patterns and advanced usage
- [Migration Guide](/docs/migration/) - Moving from other XML libraries
