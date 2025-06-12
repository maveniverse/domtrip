---
title: Installation
description: Get started with DomTrip in your Java project
layout: page
---

# Installation

Get started with DomTrip in your Java project using your preferred build tool.

## Requirements

- **Java 17** or higher
- **Maven 3.6+** or **Gradle 7.0+** (for build tools)

## Maven

Add DomTrip to your `pom.xml`:

```xml
<dependency>
    <groupId>eu.maveniverse</groupId>
    <artifactId>domtrip-core</artifactId>
    <version>0.1-SNAPSHOT</version>
</dependency>
```

## Gradle

Add DomTrip to your `build.gradle`:

```groovy
dependencies {
    implementation 'eu.maveniverse:domtrip-core:0.1-SNAPSHOT'
}
```

Or for Kotlin DSL (`build.gradle.kts`):

```kotlin
dependencies {
    implementation("eu.maveniverse:domtrip-core:0.1-SNAPSHOT")
}
```

## SBT

Add DomTrip to your `build.sbt`:

```scala
libraryDependencies += "eu.maveniverse" % "domtrip-core" % "0.1-SNAPSHOT"
```

## Verify Installation

Create a simple test to verify DomTrip is working:

```java
import eu.maveniverse.domtrip.Editor;

public class DomTripTest {
    public static void main(String[] args) {
        try {
            String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <root>
                    <message>Hello DomTrip!</message>
                </root>
                """;
            
            Editor editor = new Editor(xml);
            String result = editor.toXml();
            
            System.out.println("✅ DomTrip is working!");
            System.out.println("Round-trip successful: " + xml.equals(result));
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }
}
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

DomTrip is currently in development. To use snapshot versions, add the snapshot repository:

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

Now that DomTrip is installed, let's explore the basics:

- 📚 [Quick Start Guide](getting-started/quick-start/) - Your first DomTrip program
- 🧠 [Basic Concepts](getting-started/basic-concepts/) - Understanding DomTrip's approach
- 🚀 [API Reference](api/editor/) - Detailed API documentation

## Troubleshooting

### Common Issues

**"Package eu.maveniverse.domtrip does not exist"**
- Verify the dependency is correctly added to your build file
- Check that you're using Java 17 or higher
- Refresh/reload your project dependencies

**"ClassNotFoundException: eu.maveniverse.domtrip.Editor"**
- Ensure the JAR is in your classpath
- For snapshot versions, verify the snapshot repository is configured

**Build fails with "unsupported class file version"**
- DomTrip requires Java 17+. Update your Java version or use a compatible library version

### Getting Help

- 🐛 [Report Issues](https://github.com/maveniverse/domtrip/issues)
- 💬 [Discussions](https://github.com/maveniverse/domtrip/discussions)
- 📧 [Contact](mailto:support@maveniverse.eu)
