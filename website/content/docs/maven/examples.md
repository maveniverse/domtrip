---
title: Maven Examples
description: Real-world examples of using the DomTrip Maven extension
---

# Maven Examples

This page provides practical examples of using the DomTrip Maven extension for common POM editing tasks.

## Basic POM Creation

Create a complete Maven project POM from scratch:

```java
import org.maveniverse.domtrip.maven.PomEditor;
import eu.maveniverse.domtrip.Element;
import static org.maveniverse.domtrip.maven.MavenPomElements.Elements.*;

public class BasicPomCreation {
    public static void main(String[] args) {
        PomEditor editor = new PomEditor();
        editor.createMavenDocument("project");
        Element root = editor.root();

        // Basic project information
        editor.insertMavenElement(root, MODEL_VERSION, "4.0.0");
        editor.insertMavenElement(root, GROUP_ID, "com.example");
        editor.insertMavenElement(root, ARTIFACT_ID, "basic-project");
        editor.insertMavenElement(root, VERSION, "1.0.0");
        editor.insertMavenElement(root, PACKAGING, "jar");
        
        // Project metadata
        editor.insertMavenElement(root, NAME, "Basic Project");
        editor.insertMavenElement(root, DESCRIPTION, "A basic Maven project example");
        editor.insertMavenElement(root, URL, "https://github.com/example/basic-project");

        // Properties
        Element properties = editor.insertMavenElement(root, PROPERTIES);
        editor.addProperty(properties, "maven.compiler.source", "17");
        editor.addProperty(properties, "maven.compiler.target", "17");
        editor.addProperty(properties, "project.build.sourceEncoding", "UTF-8");

        System.out.println(editor.toXml());
    }
}
```

## Adding Dependencies

Add various types of dependencies to a POM:

```java
public class DependencyManagement {
    public static void addDependencies(PomEditor editor, Element root) {
        Element dependencies = editor.insertMavenElement(root, DEPENDENCIES);

        // Production dependency
        editor.addDependency(dependencies, "org.slf4j", "slf4j-api", "2.0.7");
        
        // Test dependency with scope
        Element junitDep = editor.addDependency(dependencies, 
            "org.junit.jupiter", "junit-jupiter", "5.9.2");
        editor.insertMavenElement(junitDep, SCOPE, "test");

        // Optional dependency
        Element optionalDep = editor.addDependency(dependencies, 
            "com.fasterxml.jackson.core", "jackson-core", "2.15.2");
        editor.insertMavenElement(optionalDep, OPTIONAL, "true");

        // Dependency with classifier
        Element testJarDep = editor.addDependency(dependencies, 
            "com.example", "test-utils", "1.0.0");
        editor.insertMavenElement(testJarDep, CLASSIFIER, "tests");
        editor.insertMavenElement(testJarDep, TYPE, "test-jar");
        editor.insertMavenElement(testJarDep, SCOPE, "test");

        // Dependency with exclusions
        Element springDep = editor.addDependency(dependencies, 
            "org.springframework", "spring-core", "6.0.9");
        Element exclusions = editor.insertMavenElement(springDep, EXCLUSIONS);
        Element exclusion = editor.addElement(exclusions, "exclusion");
        editor.addElement(exclusion, GROUP_ID, "commons-logging");
        editor.addElement(exclusion, ARTIFACT_ID, "commons-logging");
    }
}
```

## Plugin Configuration

Configure build plugins with various settings:

```java
public class PluginConfiguration {
    public static void configureBuild(PomEditor editor, Element root) {
        Element build = editor.insertMavenElement(root, BUILD);
        Element plugins = editor.insertMavenElement(build, PLUGINS);

        // Maven Compiler Plugin with configuration
        Element compilerPlugin = editor.addPlugin(plugins, 
            "org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");
        Element compilerConfig = editor.insertMavenElement(compilerPlugin, CONFIGURATION);
        editor.addElement(compilerConfig, "source", "17");
        editor.addElement(compilerConfig, "target", "17");
        editor.addElement(compilerConfig, "encoding", "UTF-8");

        // Maven Surefire Plugin with test configuration
        Element surefirePlugin = editor.addPlugin(plugins, 
            "org.apache.maven.plugins", "maven-surefire-plugin", "3.0.0");
        Element surefireConfig = editor.insertMavenElement(surefirePlugin, CONFIGURATION);
        Element includes = editor.addElement(surefireConfig, "includes");
        editor.addElement(includes, "include", "**/*Test.java");
        editor.addElement(includes, "include", "**/*Tests.java");

        // Maven JAR Plugin with manifest configuration
        Element jarPlugin = editor.addPlugin(plugins, 
            "org.apache.maven.plugins", "maven-jar-plugin", "3.3.0");
        Element jarConfig = editor.insertMavenElement(jarPlugin, CONFIGURATION);
        Element archive = editor.addElement(jarConfig, "archive");
        Element manifest = editor.addElement(archive, "manifest");
        editor.addElement(manifest, "mainClass", "com.example.Main");
        editor.addElement(manifest, "addClasspath", "true");

        // Plugin with execution
        Element execPlugin = editor.addPlugin(plugins, 
            "org.codehaus.mojo", "exec-maven-plugin", "3.1.0");
        Element executions = editor.insertMavenElement(execPlugin, EXECUTIONS);
        Element execution = editor.addElement(executions, "execution");
        editor.addElement(execution, "id", "run-app");
        editor.addElement(execution, "phase", "verify");
        Element goals = editor.addElement(execution, GOALS);
        editor.addElement(goals, "goal", "java");
        Element execConfig = editor.addElement(execution, CONFIGURATION);
        editor.addElement(execConfig, "mainClass", "com.example.App");
    }
}
```

## Multi-Module Project

Create a parent POM for a multi-module project:

```java
public class MultiModuleProject {
    public static String createParentPom() {
        PomEditor editor = new PomEditor();
        editor.createMavenDocument("project");
        Element root = editor.root();

        // Parent project setup
        editor.insertMavenElement(root, MODEL_VERSION, "4.0.0");
        editor.insertMavenElement(root, GROUP_ID, "com.example");
        editor.insertMavenElement(root, ARTIFACT_ID, "multi-module-parent");
        editor.insertMavenElement(root, VERSION, "1.0.0");
        editor.insertMavenElement(root, PACKAGING, "pom");
        
        editor.insertMavenElement(root, NAME, "Multi-Module Parent");
        editor.insertMavenElement(root, DESCRIPTION, "Parent POM for multi-module project");

        // Modules
        Element modules = editor.insertMavenElement(root, MODULES);
        editor.addModule(modules, "common");
        editor.addModule(modules, "core");
        editor.addModule(modules, "web");
        editor.addModule(modules, "cli");

        // Properties for version management
        Element properties = editor.insertMavenElement(root, PROPERTIES);
        editor.addProperty(properties, "maven.compiler.source", "17");
        editor.addProperty(properties, "maven.compiler.target", "17");
        editor.addProperty(properties, "project.build.sourceEncoding", "UTF-8");
        editor.addProperty(properties, "junit.version", "5.9.2");
        editor.addProperty(properties, "slf4j.version", "2.0.7");

        // Dependency management
        Element depMgmt = editor.insertMavenElement(root, DEPENDENCY_MANAGEMENT);
        Element depMgmtDeps = editor.insertMavenElement(depMgmt, DEPENDENCIES);
        
        // Manage internal module versions
        editor.addDependency(depMgmtDeps, "${project.groupId}", "common", "${project.version}");
        editor.addDependency(depMgmtDeps, "${project.groupId}", "core", "${project.version}");
        
        // Manage external dependencies
        editor.addDependency(depMgmtDeps, "org.slf4j", "slf4j-api", "${slf4j.version}");
        editor.addDependency(depMgmtDeps, "org.junit.jupiter", "junit-jupiter", "${junit.version}");

        // Plugin management
        Element build = editor.insertMavenElement(root, BUILD);
        Element pluginMgmt = editor.insertMavenElement(build, PLUGIN_MANAGEMENT);
        Element pluginMgmtPlugins = editor.insertMavenElement(pluginMgmt, PLUGINS);
        
        editor.addPlugin(pluginMgmtPlugins, "org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");
        editor.addPlugin(pluginMgmtPlugins, "org.apache.maven.plugins", "maven-surefire-plugin", "3.0.0");
        editor.addPlugin(pluginMgmtPlugins, "org.apache.maven.plugins", "maven-jar-plugin", "3.3.0");

        return editor.toXml();
    }

    public static String createChildPom(String artifactId, String name) {
        PomEditor editor = new PomEditor();
        editor.createMavenDocument("project");
        Element root = editor.root();

        // Child project setup
        editor.insertMavenElement(root, MODEL_VERSION, "4.0.0");
        
        // Parent reference
        Element parent = editor.insertMavenElement(root, PARENT);
        editor.insertMavenElement(parent, GROUP_ID, "com.example");
        editor.insertMavenElement(parent, ARTIFACT_ID, "multi-module-parent");
        editor.insertMavenElement(parent, VERSION, "1.0.0");
        
        editor.insertMavenElement(root, ARTIFACT_ID, artifactId);
        editor.insertMavenElement(root, NAME, name);

        // Dependencies (versions managed by parent)
        Element dependencies = editor.insertMavenElement(root, DEPENDENCIES);
        editor.addDependency(dependencies, "org.slf4j", "slf4j-api", null); // Version from parent
        
        Element testDep = editor.addDependency(dependencies, "org.junit.jupiter", "junit-jupiter", null);
        editor.insertMavenElement(testDep, SCOPE, "test");

        return editor.toXml();
    }
}
```

## POM Transformation

Transform an existing POM by adding new elements:

```java
public class PomTransformation {
    public static String upgradePom(String existingPomXml) {
        Document doc = Document.of(existingPomXml);
        PomEditor editor = new PomEditor(doc);
        Element root = editor.root();

        // Add missing project information
        if (editor.findChildElement(root, NAME) == null) {
            editor.insertMavenElement(root, NAME, "Upgraded Project");
        }
        
        if (editor.findChildElement(root, DESCRIPTION) == null) {
            editor.insertMavenElement(root, DESCRIPTION, "Project upgraded with DomTrip");
        }

        // Update or add properties
        Element properties = editor.findChildElement(root, PROPERTIES);
        if (properties == null) {
            properties = editor.insertMavenElement(root, PROPERTIES);
        }
        
        // Upgrade Java version
        editor.addProperty(properties, "maven.compiler.source", "17");
        editor.addProperty(properties, "maven.compiler.target", "17");
        
        // Add encoding if missing
        if (properties.child("project.build.sourceEncoding").isEmpty()) {
            editor.addProperty(properties, "project.build.sourceEncoding", "UTF-8");
        }

        // Add or update dependencies
        Element dependencies = editor.findChildElement(root, DEPENDENCIES);
        if (dependencies == null) {
            dependencies = editor.insertMavenElement(root, DEPENDENCIES);
        }

        // Add JUnit 5 if not present
        boolean hasJunit5 = dependencies.children(DEPENDENCY)
            .anyMatch(dep -> dep.child(ARTIFACT_ID)
                .map(aid -> aid.textContent().contains("junit-jupiter"))
                .orElse(false));
        
        if (!hasJunit5) {
            Element junitDep = editor.addDependency(dependencies, 
                "org.junit.jupiter", "junit-jupiter", "5.9.2");
            editor.insertMavenElement(junitDep, SCOPE, "test");
        }

        // Update build plugins
        Element build = editor.findChildElement(root, BUILD);
        if (build == null) {
            build = editor.insertMavenElement(root, BUILD);
        }
        
        Element plugins = editor.findChildElement(build, PLUGINS);
        if (plugins == null) {
            plugins = editor.insertMavenElement(build, PLUGINS);
        }

        // Add or update compiler plugin
        boolean hasCompilerPlugin = plugins.children(PLUGIN)
            .anyMatch(plugin -> plugin.child(ARTIFACT_ID)
                .map(aid -> aid.textContent().equals("maven-compiler-plugin"))
                .orElse(false));
        
        if (!hasCompilerPlugin) {
            Element compilerPlugin = editor.addPlugin(plugins, 
                "org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");
            Element config = editor.insertMavenElement(compilerPlugin, CONFIGURATION);
            editor.addElement(config, "source", "17");
            editor.addElement(config, "target", "17");
        }

        return editor.toXml();
    }
}
```

## Spring Boot Project

Create a Spring Boot project POM:

```java
public class SpringBootProject {
    public static String createSpringBootPom() {
        PomEditor editor = new PomEditor();
        editor.createMavenDocument("project");
        Element root = editor.root();

        // Basic project info
        editor.insertMavenElement(root, MODEL_VERSION, "4.0.0");
        
        // Spring Boot parent
        Element parent = editor.insertMavenElement(root, PARENT);
        editor.insertMavenElement(parent, GROUP_ID, "org.springframework.boot");
        editor.insertMavenElement(parent, ARTIFACT_ID, "spring-boot-starter-parent");
        editor.insertMavenElement(parent, VERSION, "3.1.0");
        Element relativePath = editor.insertMavenElement(parent, RELATIVE_PATH);
        // Empty relativePath for Spring Boot parent
        
        editor.insertMavenElement(root, GROUP_ID, "com.example");
        editor.insertMavenElement(root, ARTIFACT_ID, "spring-boot-app");
        editor.insertMavenElement(root, VERSION, "1.0.0");
        editor.insertMavenElement(root, PACKAGING, "jar");
        
        editor.insertMavenElement(root, NAME, "Spring Boot Application");
        editor.insertMavenElement(root, DESCRIPTION, "Demo project for Spring Boot");

        // Properties
        Element properties = editor.insertMavenElement(root, PROPERTIES);
        editor.addProperty(properties, "java.version", "17");

        // Dependencies
        Element dependencies = editor.insertMavenElement(root, DEPENDENCIES);
        
        // Spring Boot starters
        editor.addDependency(dependencies, "org.springframework.boot", "spring-boot-starter-web", null);
        editor.addDependency(dependencies, "org.springframework.boot", "spring-boot-starter-data-jpa", null);
        editor.addDependency(dependencies, "org.springframework.boot", "spring-boot-starter-security", null);
        
        // Database
        editor.addDependency(dependencies, "com.h2database", "h2", null);
        
        // Test dependencies
        Element testDep = editor.addDependency(dependencies, 
            "org.springframework.boot", "spring-boot-starter-test", null);
        editor.insertMavenElement(testDep, SCOPE, "test");

        // Build configuration
        Element build = editor.insertMavenElement(root, BUILD);
        Element plugins = editor.insertMavenElement(build, PLUGINS);
        
        // Spring Boot Maven plugin
        editor.addPlugin(plugins, "org.springframework.boot", "spring-boot-maven-plugin", null);

        return editor.toXml();
    }
}
```

## Complete Example Application

Here's a complete example that demonstrates multiple features:

```java
public class CompleteExample {
    public static void main(String[] args) {
        // Create enterprise Java project POM
        PomEditor editor = new PomEditor();
        editor.createMavenDocument("project");
        Element root = editor.root();

        // Project coordinates
        editor.insertMavenElement(root, MODEL_VERSION, "4.0.0");
        editor.insertMavenElement(root, GROUP_ID, "com.enterprise");
        editor.insertMavenElement(root, ARTIFACT_ID, "enterprise-app");
        editor.insertMavenElement(root, VERSION, "2.0.0");
        editor.insertMavenElement(root, PACKAGING, "jar");
        
        // Project information
        editor.insertMavenElement(root, NAME, "Enterprise Application");
        editor.insertMavenElement(root, DESCRIPTION, "A comprehensive enterprise Java application");
        editor.insertMavenElement(root, URL, "https://github.com/enterprise/app");
        editor.insertMavenElement(root, INCEPTION_YEAR, "2024");

        // Organization
        Element organization = editor.insertMavenElement(root, ORGANIZATION);
        editor.addElement(organization, NAME, "Enterprise Corp");
        editor.addElement(organization, URL, "https://enterprise.com");

        // Licenses
        Element licenses = editor.insertMavenElement(root, LICENSES);
        Element license = editor.addElement(licenses, "license");
        editor.addElement(license, NAME, "Apache License 2.0");
        editor.addElement(license, URL, "https://www.apache.org/licenses/LICENSE-2.0");

        // Properties with version management
        Element properties = editor.insertMavenElement(root, PROPERTIES);
        editor.addProperty(properties, "maven.compiler.source", "17");
        editor.addProperty(properties, "maven.compiler.target", "17");
        editor.addProperty(properties, "project.build.sourceEncoding", "UTF-8");
        editor.addProperty(properties, "spring.version", "6.0.9");
        editor.addProperty(properties, "junit.version", "5.9.2");
        editor.addProperty(properties, "slf4j.version", "2.0.7");

        // Dependencies
        Element dependencies = editor.insertMavenElement(root, DEPENDENCIES);
        
        // Production dependencies
        editor.addDependency(dependencies, "org.springframework", "spring-context", "${spring.version}");
        editor.addDependency(dependencies, "org.slf4j", "slf4j-api", "${slf4j.version}");
        editor.addDependency(dependencies, "ch.qos.logback", "logback-classic", "1.4.7");
        
        // Test dependencies
        Element junitDep = editor.addDependency(dependencies, 
            "org.junit.jupiter", "junit-jupiter", "${junit.version}");
        editor.insertMavenElement(junitDep, SCOPE, "test");
        
        Element mockitoDep = editor.addDependency(dependencies, 
            "org.mockito", "mockito-core", "4.6.1");
        editor.insertMavenElement(mockitoDep, SCOPE, "test");

        // Build configuration
        Element build = editor.insertMavenElement(root, BUILD);
        editor.insertMavenElement(build, FINAL_NAME, "enterprise-app");
        
        Element plugins = editor.insertMavenElement(build, PLUGINS);
        
        // Compiler plugin
        Element compilerPlugin = editor.addPlugin(plugins, 
            "org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");
        Element compilerConfig = editor.insertMavenElement(compilerPlugin, CONFIGURATION);
        editor.addElement(compilerConfig, "source", "${maven.compiler.source}");
        editor.addElement(compilerConfig, "target", "${maven.compiler.target}");
        
        // Surefire plugin
        Element surefirePlugin = editor.addPlugin(plugins, 
            "org.apache.maven.plugins", "maven-surefire-plugin", "3.0.0");
        
        // JAR plugin with manifest
        Element jarPlugin = editor.addPlugin(plugins, 
            "org.apache.maven.plugins", "maven-jar-plugin", "3.3.0");
        Element jarConfig = editor.insertMavenElement(jarPlugin, CONFIGURATION);
        Element archive = editor.addElement(jarConfig, "archive");
        Element manifest = editor.addElement(archive, "manifest");
        editor.addElement(manifest, "mainClass", "com.enterprise.Application");
        editor.addElement(manifest, "addClasspath", "true");

        // Output the complete POM
        System.out.println(editor.toXml());
    }
}
```

These examples demonstrate the power and flexibility of the DomTrip Maven extension for creating, modifying, and transforming Maven POM files while maintaining proper formatting and element ordering.

## Next Steps

- Try these examples in your own projects
- Explore the [PomEditor API](/docs/maven/api/) for more methods
- Learn about [Element Ordering](/docs/maven/ordering/) rules
- Check out the [Maven Quick Start](/docs/maven/quick-start/) guide
