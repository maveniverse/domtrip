---
title: Maven Extension Installation
description: How to install and configure the DomTrip Maven extension
layout: page
---

# Maven Extension Installation

The DomTrip Maven extension provides specialized functionality for working with Maven POM files. This guide shows you how to add it to your project.

## Maven Dependency

Add the Maven extension to your project's `pom.xml`:

```xml
<dependency>
    <groupId>eu.maveniverse.maven.domtrip</groupId>
    <artifactId>domtrip-maven</artifactId>
    <version>0.2.0</version>
</dependency>
```

The Maven extension automatically includes the core DomTrip library as a transitive dependency, so you don't need to add `domtrip-core` separately.

## Gradle Dependency

For Gradle projects, add to your `build.gradle`:

```gradle
dependencies {
    implementation 'eu.maveniverse.maven.domtrip:domtrip-maven:0.2.0'
}
```

Or for Gradle Kotlin DSL (`build.gradle.kts`):

```kotlin
dependencies {
    implementation("eu.maveniverse.maven.domtrip:domtrip-maven:0.2.0")
}
```

## Requirements

### Java Version
- **Java 17 or higher** is required
- The library is built and tested with Java 17+
- Uses modern Java features like records, pattern matching, and enhanced switch expressions

### Maven Version Compatibility
The Maven extension works with POM files from:
- **Maven 2.x** (legacy support)
- **Maven 3.x** (full support)
- **Maven 4.x** (full support with 4.1.0 model version)

## Verification

To verify the installation, create a simple test:

```java
import org.maveniverse.domtrip.maven.PomEditor;
import org.maveniverse.domtrip.maven.MavenPomElements;

public class InstallationTest {
    public static void main(String[] args) {
        // Create a new POM editor
        PomEditor editor = new PomEditor();
        
        // Verify constants are available
        System.out.println("Maven namespace: " + 
            MavenPomElements.Namespaces.MAVEN_4_0_0_NAMESPACE);
        
        System.out.println("DomTrip Maven extension installed successfully!");
    }
}
```

## Module Structure

When you add the Maven extension, you get access to:

### Core Classes
- `PomEditor` - Specialized editor for Maven POM files
- `MavenPomElements` - Constants for Maven elements and attributes

### Package Structure
```
org.maveniverse.domtrip.maven
├── PomEditor.class
├── MavenPomElements.class
├── MavenPomElements$Elements.class
├── MavenPomElements$Attributes.class
├── MavenPomElements$Namespaces.class
├── MavenPomElements$SchemaLocations.class
├── MavenPomElements$ModelVersions.class
├── MavenPomElements$Files.class
├── MavenPomElements$Plugins.class
└── MavenPomElements$Indentation.class
```

## IDE Setup

### IntelliJ IDEA
1. Add the dependency to your `pom.xml` or `build.gradle`
2. Refresh the Maven/Gradle project
3. The classes should be available with full autocomplete

### Eclipse
1. Add the dependency to your build file
2. Right-click project → Maven → Reload Projects (for Maven)
3. Or Gradle → Refresh Gradle Project (for Gradle)

### VS Code
1. Add the dependency to your build file
2. Reload the Java workspace
3. The Extension Pack for Java should automatically detect the new dependency

## Common Issues

### ClassNotFoundException
If you get a `ClassNotFoundException` for Maven extension classes:

1. **Check Java version**: Ensure you're using Java 17+
2. **Verify dependency**: Make sure the Maven extension is in your classpath
3. **Clean build**: Run `mvn clean compile` or `gradle clean build`

### Version Conflicts
If you have version conflicts with transitive dependencies:

```xml
<dependency>
    <groupId>eu.maveniverse</groupId>
    <artifactId>domtrip-maven</artifactId>
    <version>0.2.0</version>
    <exclusions>
        <exclusion>
            <groupId>*</groupId>
            <artifactId>*</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<!-- Add specific versions of transitive dependencies -->
<dependency>
    <groupId>eu.maveniverse</groupId>
    <artifactId>domtrip-core</artifactId>
    <version>0.2.0</version>
</dependency>
```

## Next Steps

Now that you have the Maven extension installed:

1. **Try the Quick Start**: Follow the [Maven Quick Start Guide](/docs/maven/quick-start/)
2. **Explore the API**: Check out the [PomEditor API Reference](/docs/maven/api/)
3. **See Examples**: Browse [Maven Examples](/docs/maven/examples/)
4. **Learn Element Ordering**: Understand [Maven Element Ordering](/docs/maven/ordering/)

## Alternative Installation Methods

### Local Build
To build and install from source:

```bash
git clone https://github.com/maveniverse/domtrip.git
cd domtrip
./mvnw clean install
```

Then use the SNAPSHOT version in your project.

### Development Setup
For development work on the Maven extension itself:

```bash
git clone https://github.com/maveniverse/domtrip.git
cd domtrip
./mvnw clean compile
# Import into your IDE as a Maven project
```
