---
title: Introduction
description: Welcome to DomTrip - the lossless XML editing library for Java
layout: page
---

# Introduction

Welcome to **DomTrip** - the lossless XML editing library for Java that preserves every detail of your XML documents during round-trip operations.

## What is DomTrip?

DomTrip is a Java library designed to solve a fundamental problem with XML processing: **information loss during parsing and serialization**. Unlike traditional XML libraries that focus on data extraction, DomTrip preserves the complete original formatting, making it perfect for XML editing scenarios where you need to maintain the exact structure and style of the original document.

## Why DomTrip?

### 🎯 **Perfect Round-Trip Preservation**

```java
{cdi:snippets.snippet('round-trip-preservation')}
```

### 🔧 **Intelligent Editing**

```java
{cdi:snippets.snippet('intelligent-editing')}
```

### 🚀 **Modern Java API**

```java
{cdi:snippets.snippet('modern-java-api')}
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
{cdi:snippets.snippet('quick-example')}
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

Ready to try DomTrip? Head over to the [Installation Guide](../docs/getting-started/installation/) to get started in minutes!
