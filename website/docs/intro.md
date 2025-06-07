---
sidebar_position: 1
---

# Introduction

Welcome to **DomTrip** - the lossless XML editing library for Java that preserves every detail of your XML documents during round-trip operations.

## What is DomTrip?

DomTrip is a Java library designed to solve a fundamental problem with XML processing: **information loss during parsing and serialization**. Unlike traditional XML libraries that focus on data extraction, DomTrip preserves the complete original formatting, making it perfect for XML editing scenarios where you need to maintain the exact structure and style of the original document.

## Why DomTrip?

### 🎯 **Perfect Round-Trip Preservation**

```java
Editor editor = new Editor(originalXml);
String result = editor.toXml();
// result is IDENTICAL to originalXml if no modifications were made
```

### 🔧 **Intelligent Editing**

```java
// Add new elements while preserving original formatting
Element parent = editor.findElement("dependencies");
Element newDep = editor.addElement(parent, "dependency");
editor.addElement(newDep, "groupId", "org.example");
```

### 🚀 **Modern Java API**

```java
// Fluent builders and Stream-based navigation
Element element = Elements.builder("dependency")
    .withAttribute("scope", "test")
    .withChild(Elements.textElement("groupId", "junit"))
    .build();

Optional<Element> child = root.findChild("dependency");
Stream<Element> descendants = root.descendants();
```

## Key Features

- **🔄 Lossless Round-Trip**: Preserves comments, whitespace, entity encoding, attribute quotes
- **📝 Format Preservation**: Only modified sections are reformatted
- **🏗️ Builder Patterns**: Fluent APIs for creating complex XML structures  
- **🌐 Namespace Support**: Comprehensive namespace handling and resolution
- **⚙️ Configurable**: Multiple serialization options and presets
- **🧪 Well Tested**: 100% test coverage with 59 passing tests

## Quick Example

```java
// Parse XML while preserving all formatting
Editor editor = new Editor("""
    <?xml version="1.0" encoding="UTF-8"?>
    <!-- Project configuration -->
    <project xmlns="http://maven.apache.org/POM/4.0.0">
        <groupId>com.example</groupId>
        <version>1.0.0</version>
    </project>
    """);

// Make targeted changes
Element version = editor.findElement("version");
editor.setTextContent(version, "1.0.1");

// Add new dependency with automatic formatting
Element project = editor.getRootElement();
Element dependencies = editor.addElement(project, "dependencies");
Element dependency = editor.addElement(dependencies, "dependency");
editor.addElement(dependency, "groupId", "junit");
editor.addElement(dependency, "artifactId", "junit");
editor.addElement(dependency, "version", "4.13.2");

// Result preserves original formatting and comments
String result = editor.toXml();
```

**Output:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- Project configuration -->
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <groupId>com.example</groupId>
    <version>1.0.1</version>
    <dependencies>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.13.2</version>
        </dependency>
    </dependencies>
</project>
```

Notice how:
- Original XML declaration and comments are preserved
- Existing formatting remains unchanged
- New elements follow the inferred indentation pattern
- Only the modified version element changed

## When to Use DomTrip

DomTrip is perfect for scenarios where you need to:

- **Edit configuration files** (Maven POMs, Spring configs, etc.)
- **Modify XML documents** while preserving their original structure
- **Build XML transformation tools** that maintain formatting
- **Create XML editors** with lossless round-trip capabilities
- **Process SOAP/XML messages** without losing formatting details

## Getting Started

Ready to try DomTrip? Head over to the [Installation Guide](getting-started/installation) to get started in minutes!
