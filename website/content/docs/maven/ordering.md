---
title: Maven Element Ordering
description: Understanding how PomEditor orders elements according to Maven conventions
---

# Maven Element Ordering

The PomEditor automatically orders elements according to established Maven conventions, ensuring your POM files follow best practices and remain readable and maintainable.

## Why Element Ordering Matters

### Consistency
- **Standardized Structure**: All team members see POMs organized the same way
- **Easier Reviews**: Predictable element placement makes code reviews faster
- **Tool Compatibility**: Many Maven tools expect certain element ordering

### Readability
- **Logical Flow**: Related elements are grouped together
- **Clear Separation**: Blank lines separate different sections
- **Intuitive Navigation**: Developers can quickly find what they need

### Maintainability
- **Reduced Conflicts**: Consistent ordering minimizes merge conflicts
- **Easier Updates**: Automated tools can reliably modify POMs
- **Better Diffs**: Changes are easier to spot in version control

## Project Level Ordering

The PomEditor follows this ordering for project-level elements:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <!-- 1. Model Version -->
  <modelVersion>4.0.0</modelVersion>
  
  <!-- 2. Parent Information -->
  <parent>
    <groupId>org.example</groupId>
    <artifactId>parent</artifactId>
    <version>1.0.0</version>
  </parent>
  
  <!-- 3. Project Coordinates -->
  <groupId>com.example</groupId>
  <artifactId>my-project</artifactId>
  <version>1.0.0</version>
  <packaging>jar</packaging>
  
  <!-- 4. Project Information -->
  <name>My Project</name>
  <description>A sample project</description>
  <url>https://github.com/example/my-project</url>
  <inceptionYear>2024</inceptionYear>
  <organization>
    <name>Example Corp</name>
    <url>https://example.com</url>
  </organization>
  <licenses>
    <license>
      <name>Apache License 2.0</name>
      <url>https://www.apache.org/licenses/LICENSE-2.0</url>
    </license>
  </licenses>
  
  <!-- 5. People Information -->
  <developers>
    <developer>
      <name>John Doe</name>
      <email>john@example.com</email>
    </developer>
  </developers>
  <contributors>
    <contributor>
      <name>Jane Smith</name>
    </contributor>
  </contributors>
  
  <!-- 6. Communication -->
  <mailingLists>
    <mailingList>
      <name>dev</name>
      <post>dev@example.com</post>
    </mailingList>
  </mailingLists>
  
  <!-- 7. Prerequisites -->
  <prerequisites>
    <maven>3.6.0</maven>
  </prerequisites>
  
  <!-- 8. Modules (for multi-module projects) -->
  <modules>
    <module>core</module>
    <module>web</module>
  </modules>
  
  <!-- 9. Source Control and Issue Management -->
  <scm>
    <connection>scm:git:git://github.com/example/my-project.git</connection>
    <url>https://github.com/example/my-project</url>
  </scm>
  <issueManagement>
    <system>GitHub</system>
    <url>https://github.com/example/my-project/issues</url>
  </issueManagement>
  <ciManagement>
    <system>GitHub Actions</system>
    <url>https://github.com/example/my-project/actions</url>
  </ciManagement>
  <distributionManagement>
    <repository>
      <id>releases</id>
      <url>https://repo.example.com/releases</url>
    </repository>
  </distributionManagement>
  
  <!-- 10. Properties -->
  <properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>
  
  <!-- 11. Dependency Management -->
  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.junit</groupId>
        <artifactId>junit-bom</artifactId>
        <version>5.9.2</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>
  
  <!-- 12. Repositories -->
  <repositories>
    <repository>
      <id>central</id>
      <url>https://repo1.maven.org/maven2</url>
    </repository>
  </repositories>
  <pluginRepositories>
    <pluginRepository>
      <id>central</id>
      <url>https://repo1.maven.org/maven2</url>
    </pluginRepository>
  </pluginRepositories>
  
  <!-- 13. Build Configuration -->
  <build>
    <!-- Build elements ordered separately -->
  </build>
  
  <!-- 14. Reporting -->
  <reporting>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-javadoc-plugin</artifactId>
      </plugin>
    </plugins>
  </reporting>
  
  <!-- 15. Profiles -->
  <profiles>
    <profile>
      <id>development</id>
      <properties>
        <env>dev</env>
      </properties>
    </profile>
  </profiles>
</project>
```

## Build Section Ordering

Within the `<build>` section, elements are ordered as follows:

```xml
<build>
  <!-- 1. Build Configuration -->
  <defaultGoal>compile</defaultGoal>
  <directory>target</directory>
  <finalName>my-project</finalName>
  
  <!-- 2. Source Directories -->
  <sourceDirectory>src/main/java</sourceDirectory>
  <scriptSourceDirectory>src/main/scripts</scriptSourceDirectory>
  <testSourceDirectory>src/test/java</testSourceDirectory>
  
  <!-- 3. Output Directories -->
  <outputDirectory>target/classes</outputDirectory>
  <testOutputDirectory>target/test-classes</testOutputDirectory>
  
  <!-- 4. Extensions -->
  <extensions>
    <extension>
      <groupId>org.apache.maven.wagon</groupId>
      <artifactId>wagon-ssh</artifactId>
      <version>3.5.1</version>
    </extension>
  </extensions>
  
  <!-- 5. Plugin Management -->
  <pluginManagement>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.11.0</version>
      </plugin>
    </plugins>
  </pluginManagement>
  
  <!-- 6. Plugins -->
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <configuration>
        <source>17</source>
        <target>17</target>
      </configuration>
    </plugin>
  </plugins>
</build>
```

## Plugin Element Ordering

Within each `<plugin>` element:

```xml
<plugin>
  <!-- 1. Coordinates -->
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <version>3.11.0</version>
  
  <!-- 2. Extensions -->
  <extensions>true</extensions>
  
  <!-- 3. Executions -->
  <executions>
    <execution>
      <id>compile</id>
      <phase>compile</phase>
      <goals>
        <goal>compile</goal>
      </goals>
    </execution>
  </executions>
  
  <!-- 4. Dependencies -->
  <dependencies>
    <dependency>
      <groupId>org.ow2.asm</groupId>
      <artifactId>asm</artifactId>
      <version>9.4</version>
    </dependency>
  </dependencies>
  
  <!-- 5. Goals -->
  <goals>
    <goal>compile</goal>
  </goals>
  
  <!-- 6. Inherited -->
  <inherited>false</inherited>
  
  <!-- 7. Configuration -->
  <configuration>
    <source>17</source>
    <target>17</target>
  </configuration>
</plugin>
```

## Dependency Element Ordering

Within each `<dependency>` element:

```xml
<dependency>
  <!-- 1. Coordinates -->
  <groupId>org.junit.jupiter</groupId>
  <artifactId>junit-jupiter</artifactId>
  <version>5.9.2</version>
  
  <!-- 2. Classifier and Type -->
  <classifier>tests</classifier>
  <type>test-jar</type>
  
  <!-- 3. Scope -->
  <scope>test</scope>
  
  <!-- 4. System Path (for system scope) -->
  <systemPath>${java.home}/lib/rt.jar</systemPath>
  
  <!-- 5. Optional -->
  <optional>true</optional>
  
  <!-- 6. Exclusions -->
  <exclusions>
    <exclusion>
      <groupId>org.hamcrest</groupId>
      <artifactId>hamcrest-core</artifactId>
    </exclusion>
  </exclusions>
</dependency>
```

## Automatic Ordering in Action

When you use PomEditor, elements are automatically placed in the correct order:

```java
PomEditor editor = new PomEditor();
editor.createMavenDocument("project");
Element root = editor.root();

// Add elements in any order
editor.insertMavenElement(root, "version", "1.0.0");           // Will be ordered correctly
editor.insertMavenElement(root, "modelVersion", "4.0.0");     // Will come first
editor.insertMavenElement(root, "artifactId", "my-project");  // Will be ordered correctly
editor.insertMavenElement(root, "groupId", "com.example");    // Will be ordered correctly
editor.insertMavenElement(root, "description", "My project"); // Will come after name
editor.insertMavenElement(root, "name", "My Project");        // Will come before description

// Result is properly ordered with blank lines
String xml = editor.toXml();
```

## Blank Line Rules

The PomEditor automatically adds blank lines:

### Between Major Sections
- After `modelVersion`
- After `parent` block
- After project coordinates
- After project information
- Between different functional groups

### Within Sections
- Between individual plugins
- Between individual dependencies (when there are many)
- Between different types of configuration

### Customization
The blank line insertion follows Maven community conventions and cannot be customized. This ensures consistency across all projects using PomEditor.

## Benefits of Automatic Ordering

### For Developers
- **No Mental Overhead**: Don't think about where to place elements
- **Consistent Results**: Same ordering regardless of who edits the POM
- **Focus on Content**: Concentrate on what to add, not where to put it

### For Teams
- **Reduced Conflicts**: Consistent ordering minimizes merge conflicts
- **Easier Reviews**: Reviewers know where to look for changes
- **Better Collaboration**: Everyone follows the same conventions

### For Tools
- **Predictable Structure**: Tools can rely on consistent element placement
- **Easier Parsing**: Automated tools can navigate POMs more easily
- **Better Integration**: Works well with IDEs and build tools

## Next Steps

- Learn about [PomEditor API](/docs/maven/api/) methods that use this ordering
- See [Maven Examples](/docs/maven/examples/) showing ordering in practice
- Try the [Maven Quick Start](/docs/maven/quick-start/) to see ordering in action
