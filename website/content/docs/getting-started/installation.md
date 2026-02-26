---
title: Installation
description: Get started with DomTrip in your Java project using your preferred build tool
layout: page
---

# Installation

Get started with DomTrip in your Java project using your preferred build tool.

## Requirements

- **Java 8** or higher
- **Maven 3.6+** or **Gradle 7.0+** (for build tools)

## Maven

### Core Library

Add DomTrip core to your `pom.xml`:

```xml
<dependency>
    <groupId>eu.maveniverse.maven.domtrip</groupId>
    <artifactId>domtrip-core</artifactId>
    <version>0.2.0</version>
</dependency>
```

### Maven Extension (Recommended for POM editing)

For Maven POM file editing, use the Maven extension which includes the core library:

```xml
<dependency>
    <groupId>eu.maveniverse.maven.domtrip</groupId>
    <artifactId>domtrip-maven</artifactId>
    <version>0.2.0</version>
</dependency>
```

The Maven extension provides:
- **PomEditor** class with Maven-aware element ordering
- **MavenPomElements** constants for type-safe element names
- Convenience methods for dependencies, plugins, and modules
- Automatic blank line insertion between element groups

## Gradle

### Core Library

Add DomTrip core to your `build.gradle`:

```groovy
dependencies {
    implementation 'eu.maveniverse.maven.domtrip:domtrip-core:0.2.0'
}
```

### Maven Extension

For Maven POM editing, use the Maven extension:

```groovy
dependencies {
    implementation 'eu.maveniverse.maven.domtrip:domtrip-maven:0.2.0'
}
```

Or for Kotlin DSL (`build.gradle.kts`):

```kotlin
dependencies {
    // Core library
    implementation("eu.maveniverse.maven.domtrip:domtrip-core:0.2.0")

    // Or Maven extension (includes core)
    implementation("eu.maveniverse.maven.domtrip:domtrip-maven:0.2.0")
}
```

## SBT

Add DomTrip to your `build.sbt`:

```scala
libraryDependencies += "eu.maveniverse.maven.domtrip" % "domtrip-core" % "0.2.0"
```

## Verify Installation

Create a simple test to verify DomTrip is working:

```java
{cdi:snippets.snippet('installation-test')}
```

If you see "✅ DomTrip is working!" and "Round-trip successful: true", you're all set!

## IDE Setup

### IntelliJ IDEA

1. **Import Project**: Open your project in IntelliJ IDEA
2. **Refresh Dependencies**: Click the Maven/Gradle refresh button
3. **Enable Auto-Import**: Go to Settings → Build Tools → Maven/Gradle → Enable auto-import

### Eclipse

1. **Import Project**: Import as Maven/Gradle project
2. **Refresh Dependencies**: Right-click project → Maven → Reload Projects
3. **Build Path**: Verify DomTrip appears in Referenced Libraries

### VS Code

1. **Install Extensions**: Java Extension Pack, Maven for Java
2. **Open Project**: Open the folder containing your project
3. **Reload Window**: Ctrl+Shift+P → "Java: Reload Projects"

## Snapshot Versions

To use the latest development snapshot versions, add the snapshot repository:

### Maven

```xml
<repositories>
    <repository>
        <id>sonatype-snapshots</id>
        <url>https://oss.sonatype.org/content/repositories/snapshots</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

### Gradle

```groovy
repositories {
    maven {
        url 'https://oss.sonatype.org/content/repositories/snapshots'
    }
}
```

## Next Steps

Now that DomTrip is installed, choose your path:

### Core Library
- 📚 [Quick Start Guide](../../docs/getting-started/quick-start/) - Your first DomTrip program
- 🧠 [Basic Concepts](../../docs/getting-started/basic-concepts/) - Understanding DomTrip's approach
- 🚀 [API Reference](../../docs/api/editor/) - Detailed API documentation

### Maven Extension
- 🏗️ [Maven Quick Start](../../docs/maven/quick-start/) - Maven POM editing in 5 minutes
- 📖 [Maven Overview](../../docs/maven/overview/) - Understanding Maven-specific features
- 🔧 [Maven API Reference](../../docs/maven/api/) - Complete PomEditor documentation

## Troubleshooting

### Common Issues

**"Package eu.maveniverse.domtrip does not exist"**
- Verify the dependency is correctly added to your build file
- Check that you're using Java 8 or higher
- Refresh/reload your project dependencies

**"ClassNotFoundException: eu.maveniverse.domtrip.Editor"**
- Ensure the JAR is in your classpath
- For snapshot versions, verify the snapshot repository is configured

### Getting Help

- 🐛 [Report Issues](https://github.com/maveniverse/domtrip/issues)
- 💬 [Discussions](https://github.com/maveniverse/domtrip/discussions)
- 📧 [Contact](mailto:support@maveniverse.eu)
