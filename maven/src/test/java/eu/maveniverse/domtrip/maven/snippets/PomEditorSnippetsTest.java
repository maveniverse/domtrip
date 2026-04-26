/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.maven.snippets;

import static eu.maveniverse.domtrip.maven.MavenPomElements.Elements.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Element;
import eu.maveniverse.domtrip.maven.AlignOptions;
import eu.maveniverse.domtrip.maven.Coordinates;
import eu.maveniverse.domtrip.maven.PomEditor;
import org.junit.jupiter.api.Test;

/**
 * Code snippets for PomEditor documentation.
 * Each snippet is marked with START/END comments and referenced from website docs.
 */
class PomEditorSnippetsTest {

    @Test
    void testBasicSnippets() throws DomTripException {
        basicPomCreation();
        addingDependencies();
        addingPlugins();
        multiModuleProject();
        editingExistingPom();
    }

    @Test
    void testApiSnippets() throws DomTripException {
        pomEditorClassOverview();
        dependencyManagementOperations();
        pluginManagementOperations();
        propertyManagement();
        parentManagement();
        findAndModifyElements();
        usingConstants();
        coordinatesUsage();
    }

    @Test
    void testExclusionSnippets() throws DomTripException {
        exclusionManagement();
        managedExclusionManagement();
    }

    @Test
    void testAlignmentSnippets() throws DomTripException {
        conventionDetection();
        addAligned();
        alignExistingDependency();
        alignAllDependencies();
        updateManagedDependencyAligned();
    }

    @Test
    void testProfileSnippets() throws DomTripException {
        profileScopedDependencies();
    }

    @Test
    void testCrossPomSnippets() throws DomTripException {
        alignToParent();
        alignAllToParent();
    }

    @Test
    void testExampleSnippets() throws DomTripException {
        springBootProject();
        pomTransformation();
    }

    // ========== BASIC SNIPPETS (quick-start.md) ==========

    void basicPomCreation() throws DomTripException {
        // START: basic-pom-creation
        // Create a new POM with Maven-aware ordering
        PomEditor editor = new PomEditor();
        editor.createMavenDocument("project");
        Element root = editor.root();

        // Add elements - they'll be automatically ordered
        editor.insertMavenElement(root, MODEL_VERSION, "4.0.0");
        editor.insertMavenElement(root, GROUP_ID, "com.example");
        editor.insertMavenElement(root, ARTIFACT_ID, "my-project");
        editor.insertMavenElement(root, VERSION, "1.0.0");
        editor.insertMavenElement(root, NAME, "My Project");

        String result = editor.toXml();
        // END: basic-pom-creation

        assertNotNull(result);
        assertTrue(result.contains("<project"));
    }

    void addingDependencies() throws DomTripException {
        // START: adding-dependencies
        PomEditor editor = new PomEditor();
        editor.createMavenDocument("project");
        Element root = editor.root();

        // Add dependencies with proper structure
        Element dependencies = editor.insertMavenElement(root, DEPENDENCIES);
        editor.dependencies().addDependency(dependencies, "org.junit.jupiter", "junit-jupiter", "5.9.2");

        // Add scope to the dependency
        Element junitDep = editor.findChildElement(dependencies, DEPENDENCY);
        editor.insertMavenElement(junitDep, SCOPE, "test");
        // END: adding-dependencies

        String result = editor.toXml();
        assertNotNull(result);
        assertTrue(result.contains("<dependency>"));
    }

    void addingPlugins() throws DomTripException {
        // START: adding-plugins
        PomEditor editor = new PomEditor();
        editor.createMavenDocument("project");
        Element root = editor.root();

        // Add build plugins with configuration
        Element build = editor.insertMavenElement(root, BUILD);
        Element plugins = editor.insertMavenElement(build, PLUGINS);

        Element compilerPlugin =
                editor.plugins().addPlugin(plugins, "org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");
        Element config = editor.insertMavenElement(compilerPlugin, CONFIGURATION);
        editor.addElement(config, "source", "17");
        editor.addElement(config, "target", "17");
        // END: adding-plugins

        String result = editor.toXml();
        assertNotNull(result);
        assertTrue(result.contains("<plugin>"));
    }

    void multiModuleProject() throws DomTripException {
        // START: multi-module-project
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
        parentEditor.subprojects().addModule(modules, "core");
        parentEditor.subprojects().addModule(modules, "web");
        parentEditor.subprojects().addModule(modules, "cli");

        String parentPom = parentEditor.toXml();
        // END: multi-module-project

        assertNotNull(parentPom);
        assertTrue(parentPom.contains("<modules>"));
    }

    void editingExistingPom() throws DomTripException {
        // START: editing-existing-pom
        String existingPom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>existing-project</artifactId>
                  <version>1.0.0</version>
                </project>
                """;

        Document doc = Document.of(existingPom);
        PomEditor editor = new PomEditor(doc);
        Element root = editor.root();

        // Add new elements - formatting and comments are preserved
        editor.insertMavenElement(root, NAME, "Existing Project");
        editor.insertMavenElement(root, DESCRIPTION, "Updated with DomTrip");

        String result = editor.toXml();
        // END: editing-existing-pom

        assertNotNull(result);
        assertTrue(result.contains("Existing Project"));
    }

    // ========== API REFERENCE SNIPPETS (api.md) ==========

    void pomEditorClassOverview() throws DomTripException {
        // START: pom-editor-overview
        // PomEditor uses sub-object APIs for domain-specific operations
        PomEditor editor = new PomEditor();
        editor.createMavenDocument("project");
        Element root = editor.root();

        editor.insertMavenElement(root, MODEL_VERSION, "4.0.0");
        editor.insertMavenElement(root, GROUP_ID, "com.example");
        editor.insertMavenElement(root, ARTIFACT_ID, "my-project");
        editor.insertMavenElement(root, VERSION, "1.0.0");

        // Dependencies API
        Element deps = editor.insertMavenElement(root, DEPENDENCIES);
        editor.dependencies().addDependency(deps, "org.slf4j", "slf4j-api", "2.0.7");

        // Plugins API
        Element build = editor.insertMavenElement(root, BUILD);
        Element plugins = editor.insertMavenElement(build, PLUGINS);
        editor.plugins().addPlugin(plugins, "org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");

        // Properties API
        Element properties = editor.insertMavenElement(root, PROPERTIES);
        editor.properties().addProperty(properties, "maven.compiler.source", "17");

        // Subprojects API (for modules)
        // editor.subprojects().addModule(modules, "core");

        String result = editor.toXml();
        // END: pom-editor-overview

        assertNotNull(result);
    }

    void dependencyManagementOperations() throws DomTripException {
        // START: dependency-management-ops
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>my-project</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>org.slf4j</groupId>
                      <artifactId>slf4j-api</artifactId>
                      <version>2.0.7</version>
                    </dependency>
                  </dependencies>
                </project>
                """;

        PomEditor editor = new PomEditor(Document.of(pom));
        Coordinates slf4j = Coordinates.of("org.slf4j", "slf4j-api", "2.0.9");

        // Update an existing dependency version
        editor.dependencies().updateDependency(false, slf4j);

        // Upsert: update if exists, create if not
        Coordinates guava = Coordinates.of("com.google.guava", "guava", "32.1.2-jre");
        editor.dependencies().updateDependency(true, guava);

        // Add to dependencyManagement
        Coordinates junit = Coordinates.of("org.junit.jupiter", "junit-jupiter", "5.10.0");
        editor.dependencies().updateManagedDependency(true, junit);

        // Delete a dependency
        editor.dependencies().deleteDependency(guava);

        // Delete a managed dependency
        editor.dependencies().deleteManagedDependency(junit);
        // END: dependency-management-ops

        assertNotNull(editor.toXml());
    }

    void pluginManagementOperations() throws DomTripException {
        // START: plugin-management-ops
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>my-project</artifactId>
                  <version>1.0.0</version>
                  <build>
                    <plugins>
                      <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-compiler-plugin</artifactId>
                        <version>3.11.0</version>
                      </plugin>
                    </plugins>
                  </build>
                </project>
                """;

        PomEditor editor = new PomEditor(Document.of(pom));

        // Update a plugin version
        Coordinates compiler = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", "3.12.0");
        editor.plugins().updatePlugin(false, compiler);

        // Upsert a plugin
        Coordinates surefire = Coordinates.of("org.apache.maven.plugins", "maven-surefire-plugin", "3.2.0");
        editor.plugins().updatePlugin(true, surefire);

        // Add to pluginManagement
        editor.plugins().updateManagedPlugin(true, surefire);

        // Delete a plugin
        editor.plugins().deletePlugin(surefire);
        // END: plugin-management-ops

        assertNotNull(editor.toXml());
    }

    void propertyManagement() throws DomTripException {
        // START: property-management
        PomEditor editor = new PomEditor();
        editor.createMavenDocument("project");
        Element root = editor.root();
        editor.insertMavenElement(root, MODEL_VERSION, "4.0.0");

        // Add properties section and properties
        Element properties = editor.insertMavenElement(root, PROPERTIES);
        editor.properties().addProperty(properties, "maven.compiler.source", "17");
        editor.properties().addProperty(properties, "maven.compiler.target", "17");
        editor.properties().addProperty(properties, "project.build.sourceEncoding", "UTF-8");
        editor.properties().addProperty(properties, "junit.version", "5.9.2");

        // Update an existing property
        editor.properties().updateProperty(false, "junit.version", "5.10.0");

        // Upsert: update or create
        editor.properties().updateProperty(true, "slf4j.version", "2.0.9");

        // Delete a property
        editor.properties().deleteProperty("slf4j.version");
        // END: property-management

        assertNotNull(editor.toXml());
    }

    void parentManagement() throws DomTripException {
        // START: parent-management
        PomEditor editor = new PomEditor();
        editor.createMavenDocument("project");
        Element root = editor.root();
        editor.insertMavenElement(root, MODEL_VERSION, "4.0.0");
        editor.insertMavenElement(root, ARTIFACT_ID, "my-project");

        // Set a parent POM
        Coordinates parent = Coordinates.of("org.springframework.boot", "spring-boot-starter-parent", "3.2.0");
        editor.parent().setParent(parent);

        // Update parent version
        Coordinates updatedParent = Coordinates.of("org.springframework.boot", "spring-boot-starter-parent", "3.3.0");
        editor.parent().updateParent(false, updatedParent);

        // Delete parent
        // editor.parent().deleteParent();
        // END: parent-management

        assertNotNull(editor.toXml());
    }

    void findAndModifyElements() throws DomTripException {
        // START: find-and-modify
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>my-project</artifactId>
                  <version>1.0.0</version>
                  <properties>
                    <junit.version>5.9.2</junit.version>
                  </properties>
                  <dependencies>
                    <dependency>
                      <groupId>org.junit.jupiter</groupId>
                      <artifactId>junit-jupiter</artifactId>
                      <version>${junit.version}</version>
                      <scope>test</scope>
                    </dependency>
                  </dependencies>
                </project>
                """;

        PomEditor editor = new PomEditor(Document.of(pom));
        Element root = editor.root();

        // Find a child element
        Element dependencies = editor.findChildElement(root, DEPENDENCIES);
        assertNotNull(dependencies);

        // Check if element exists
        boolean hasProps = editor.hasChildElement(root, PROPERTIES);

        // Get child element text
        String version = editor.getChildElementText(root, VERSION);

        // Update or create child element
        editor.updateOrCreateChildElement(root, DESCRIPTION, "My project description");

        // Set project version
        editor.setVersion("2.0.0");
        // END: find-and-modify

        assertTrue(hasProps);
        assertEquals("1.0.0", version);
    }

    void usingConstants() throws DomTripException {
        // START: using-constants
        // Use MavenPomElements constants for type-safe element names
        // import static eu.maveniverse.domtrip.maven.MavenPomElements.Elements.*;

        PomEditor editor = new PomEditor();
        editor.createMavenDocument("project");
        Element root = editor.root();

        // Constants prevent typos and enable IDE autocompletion
        editor.insertMavenElement(root, MODEL_VERSION, "4.0.0");
        editor.insertMavenElement(root, GROUP_ID, "com.example");
        editor.insertMavenElement(root, ARTIFACT_ID, "my-project");
        editor.insertMavenElement(root, VERSION, "1.0.0");
        editor.insertMavenElement(root, PACKAGING, "jar");
        editor.insertMavenElement(root, NAME, "My Project");
        editor.insertMavenElement(root, DESCRIPTION, "A sample project");
        // END: using-constants

        assertNotNull(editor.toXml());
    }

    void coordinatesUsage() throws DomTripException {
        // START: coordinates-usage
        // Create coordinates for a dependency
        Coordinates junit = Coordinates.of("org.junit.jupiter", "junit-jupiter", "5.10.0");

        // Access coordinate components
        String groupId = junit.groupId(); // "org.junit.jupiter"
        String artifactId = junit.artifactId(); // "junit-jupiter"
        String version = junit.version(); // "5.10.0"

        // String representations
        String ga = junit.toGA(); // "org.junit.jupiter:junit-jupiter"
        String gav = junit.toGAV(); // "org.junit.jupiter:junit-jupiter:5.10.0"

        // Coordinates with classifier and type
        Coordinates sources = Coordinates.of("org.example", "my-lib", "1.0.0", "sources", "jar");

        // Create new coordinates with different version
        Coordinates updated = junit.withVersion("5.11.0");

        // Use Coordinates with PomEditor operations
        PomEditor editor = new PomEditor(Document.of("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>my-project</artifactId>
                  <version>1.0.0</version>
                </project>
                """));
        editor.dependencies().updateDependency(true, junit);
        editor.dependencies().updateManagedDependency(true, updated);
        // END: coordinates-usage

        assertEquals("org.junit.jupiter", groupId);
        assertNotNull(editor.toXml());
    }

    // ========== EXCLUSION SNIPPETS (exclusions.md) ==========

    void exclusionManagement() throws DomTripException {
        // START: exclusion-add
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>my-project</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>org.springframework</groupId>
                      <artifactId>spring-core</artifactId>
                      <version>6.0.9</version>
                    </dependency>
                  </dependencies>
                </project>
                """;

        PomEditor editor = new PomEditor(Document.of(pom));
        Coordinates springCore = Coordinates.of("org.springframework", "spring-core", "6.0.9");
        Coordinates commonsLogging = Coordinates.of("commons-logging", "commons-logging", null);

        // Add an exclusion to a dependency
        editor.dependencies().addExclusion(springCore, commonsLogging);

        // Check if an exclusion exists
        boolean hasExcl = editor.dependencies().hasExclusion(springCore, commonsLogging);
        assertTrue(hasExcl);

        // Delete an exclusion (removes <exclusions> wrapper if empty)
        editor.dependencies().deleteExclusion(springCore, commonsLogging);
        assertFalse(editor.dependencies().hasExclusion(springCore, commonsLogging));
        // END: exclusion-add

        assertNotNull(editor.toXml());
    }

    void managedExclusionManagement() throws DomTripException {
        // START: exclusion-managed
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>my-project</artifactId>
                  <version>1.0.0</version>
                  <dependencyManagement>
                    <dependencies>
                      <dependency>
                        <groupId>org.springframework</groupId>
                        <artifactId>spring-core</artifactId>
                        <version>6.0.9</version>
                      </dependency>
                    </dependencies>
                  </dependencyManagement>
                </project>
                """;

        PomEditor editor = new PomEditor(Document.of(pom));
        Coordinates springCore = Coordinates.of("org.springframework", "spring-core", "6.0.9");
        Coordinates commonsLogging = Coordinates.of("commons-logging", "commons-logging", null);

        // Add exclusion to a managed dependency
        editor.dependencies().addManagedExclusion(springCore, commonsLogging);

        // Check if managed dependency has exclusion
        boolean has = editor.dependencies().hasManagedExclusion(springCore, commonsLogging);
        assertTrue(has);

        // Delete exclusion from managed dependency
        editor.dependencies().deleteManagedExclusion(springCore, commonsLogging);
        // END: exclusion-managed

        assertNotNull(editor.toXml());
    }

    // ========== ALIGNMENT SNIPPETS (alignment.md) ==========

    void conventionDetection() throws DomTripException {
        // START: convention-detection
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>my-project</artifactId>
                  <version>1.0.0</version>
                  <properties>
                    <slf4j.version>2.0.7</slf4j.version>
                    <guava.version>32.1.2-jre</guava.version>
                  </properties>
                  <dependencyManagement>
                    <dependencies>
                      <dependency>
                        <groupId>org.slf4j</groupId>
                        <artifactId>slf4j-api</artifactId>
                        <version>${slf4j.version}</version>
                      </dependency>
                      <dependency>
                        <groupId>com.google.guava</groupId>
                        <artifactId>guava</artifactId>
                        <version>${guava.version}</version>
                      </dependency>
                    </dependencies>
                  </dependencyManagement>
                  <dependencies>
                    <dependency>
                      <groupId>org.slf4j</groupId>
                      <artifactId>slf4j-api</artifactId>
                    </dependency>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                    </dependency>
                  </dependencies>
                </project>
                """;

        PomEditor editor = new PomEditor(Document.of(pom));

        // Detect individual conventions
        AlignOptions.VersionStyle style = editor.dependencies().detectVersionStyle();
        // MANAGED (most dependencies are version-less)

        AlignOptions.VersionSource source = editor.dependencies().detectVersionSource();
        // PROPERTY (versions use ${...} references)

        AlignOptions.PropertyNamingConvention naming = editor.dependencies().detectPropertyNamingConvention();
        // DOT_SUFFIX (e.g., "slf4j.version")

        // Detect all conventions at once
        AlignOptions detected = editor.dependencies().detectConventions();
        // END: convention-detection

        assertEquals(AlignOptions.VersionStyle.MANAGED, style);
        assertEquals(AlignOptions.VersionSource.PROPERTY, source);
        assertEquals(AlignOptions.PropertyNamingConvention.DOT_SUFFIX, naming);
        assertNotNull(detected);
    }

    void addAligned() throws DomTripException {
        // START: add-aligned
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>my-project</artifactId>
                  <version>1.0.0</version>
                  <properties>
                    <slf4j.version>2.0.7</slf4j.version>
                  </properties>
                  <dependencyManagement>
                    <dependencies>
                      <dependency>
                        <groupId>org.slf4j</groupId>
                        <artifactId>slf4j-api</artifactId>
                        <version>${slf4j.version}</version>
                      </dependency>
                    </dependencies>
                  </dependencyManagement>
                  <dependencies>
                    <dependency>
                      <groupId>org.slf4j</groupId>
                      <artifactId>slf4j-api</artifactId>
                    </dependency>
                  </dependencies>
                </project>
                """;

        PomEditor editor = new PomEditor(Document.of(pom));

        // Add a dependency aligned with auto-detected conventions
        // Detects: MANAGED + PROPERTY + DOT_SUFFIX → creates property and managed entry
        Coordinates jackson = Coordinates.of("com.fasterxml.jackson.core", "jackson-core", "2.15.2");
        editor.dependencies().addAligned(jackson);

        // Add with explicit options
        Coordinates junit = Coordinates.of("org.junit.jupiter", "junit-jupiter", "5.10.0");
        editor.dependencies()
                .addAligned(junit, AlignOptions.builder().scope("test").build());

        // Force a specific style regardless of detected conventions
        Coordinates mockito = Coordinates.of("org.mockito", "mockito-core", "5.5.0");
        editor.dependencies()
                .addAligned(
                        mockito,
                        AlignOptions.builder()
                                .versionStyle(AlignOptions.VersionStyle.INLINE)
                                .versionSource(AlignOptions.VersionSource.LITERAL)
                                .scope("test")
                                .build());
        // END: add-aligned

        assertNotNull(editor.toXml());
    }

    void alignExistingDependency() throws DomTripException {
        // START: align-dependency
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>my-project</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                      <version>32.1.2-jre</version>
                    </dependency>
                  </dependencies>
                </project>
                """;

        PomEditor editor = new PomEditor(Document.of(pom));

        // Align an existing dependency to use managed + property style
        Coordinates guava = Coordinates.of("com.google.guava", "guava", null);
        editor.dependencies()
                .alignDependency(
                        guava,
                        AlignOptions.builder()
                                .versionStyle(AlignOptions.VersionStyle.MANAGED)
                                .versionSource(AlignOptions.VersionSource.PROPERTY)
                                .namingConvention(AlignOptions.PropertyNamingConvention.DOT_SUFFIX)
                                .build());
        // Result: version moved to dependencyManagement with ${guava.version} property
        // END: align-dependency

        String result = editor.toXml();
        assertTrue(result.contains("guava.version"));
    }

    void alignAllDependencies() throws DomTripException {
        // START: align-all
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>my-project</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>org.slf4j</groupId>
                      <artifactId>slf4j-api</artifactId>
                      <version>2.0.7</version>
                    </dependency>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                      <version>32.1.2-jre</version>
                    </dependency>
                  </dependencies>
                </project>
                """;

        PomEditor editor = new PomEditor(Document.of(pom));

        // Align all dependencies to use managed + property style
        int changed = editor.dependencies()
                .alignAllDependencies(AlignOptions.builder()
                        .versionStyle(AlignOptions.VersionStyle.MANAGED)
                        .versionSource(AlignOptions.VersionSource.PROPERTY)
                        .build());
        // END: align-all

        assertEquals(2, changed);
    }

    void updateManagedDependencyAligned() throws DomTripException {
        // START: update-managed-aligned
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>my-project</artifactId>
                  <version>1.0.0</version>
                  <properties>
                    <slf4j.version>2.0.7</slf4j.version>
                  </properties>
                  <dependencyManagement>
                    <dependencies>
                      <dependency>
                        <groupId>org.slf4j</groupId>
                        <artifactId>slf4j-api</artifactId>
                        <version>${slf4j.version}</version>
                      </dependency>
                    </dependencies>
                  </dependencyManagement>
                  <dependencies>
                    <dependency>
                      <groupId>org.slf4j</groupId>
                      <artifactId>slf4j-api</artifactId>
                    </dependency>
                  </dependencies>
                </project>
                """;

        PomEditor editor = new PomEditor(Document.of(pom));

        // Add a new managed dependency following auto-detected conventions
        // Detects PROPERTY + DOT_SUFFIX → creates property and uses ${...} reference
        Coordinates jackson = Coordinates.of("com.fasterxml.jackson.core", "jackson-core", "2.15.2");
        editor.dependencies().updateManagedDependencyAligned(true, jackson);

        // Use explicit options to force property-backed version
        Coordinates guava = Coordinates.of("com.google.guava", "guava", "33.0.0-jre");
        editor.dependencies()
                .updateManagedDependencyAligned(
                        true,
                        guava,
                        AlignOptions.builder()
                                .versionSource(AlignOptions.VersionSource.PROPERTY)
                                .namingConvention(AlignOptions.PropertyNamingConvention.DOT_SUFFIX)
                                .build());

        // Use explicit property name to update an existing managed dependency
        Coordinates slf4j = Coordinates.of("org.slf4j", "slf4j-api", "2.0.13");
        editor.dependencies()
                .updateManagedDependencyAligned(
                        false,
                        slf4j,
                        AlignOptions.builder().propertyName("slf4j.version").build());
        // END: update-managed-aligned

        String result = editor.toXml();
        // New dependency got an auto-generated property
        assertTrue(result.contains("<jackson-core.version>2.15.2</jackson-core.version>"));
        assertTrue(result.contains("${jackson-core.version}"));
        // Explicit options created property
        assertTrue(result.contains("<guava.version>33.0.0-jre</guava.version>"));
        assertTrue(result.contains("${guava.version}"));
        // Explicit property name preserved existing convention
        assertTrue(result.contains("<slf4j.version>2.0.13</slf4j.version>"));
    }

    // ========== PROFILE SNIPPETS (profiles.md) ==========

    void profileScopedDependencies() throws DomTripException {
        // START: profile-scoped
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>my-project</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>org.slf4j</groupId>
                      <artifactId>slf4j-api</artifactId>
                      <version>2.0.7</version>
                    </dependency>
                  </dependencies>
                  <profiles>
                    <profile>
                      <id>integration-tests</id>
                      <dependencies>
                        <dependency>
                          <groupId>org.testcontainers</groupId>
                          <artifactId>testcontainers</artifactId>
                          <version>1.19.0</version>
                          <scope>test</scope>
                        </dependency>
                      </dependencies>
                    </profile>
                  </profiles>
                </project>
                """;

        PomEditor editor = new PomEditor(Document.of(pom));

        // Scope operations to a specific profile
        PomEditor.Dependencies profileDeps = editor.dependencies().forProfile("integration-tests");

        // Update a dependency within the profile
        Coordinates testcontainers = Coordinates.of("org.testcontainers", "testcontainers", "1.20.0");
        profileDeps.updateDependency(false, testcontainers);

        // Add a new dependency to the profile
        Coordinates wiremock = Coordinates.of("com.github.tomakehurst", "wiremock-jre8", "2.35.0");
        profileDeps.updateDependency(true, wiremock);

        // Profile-scoped operations don't affect top-level dependencies
        // editor.dependencies() still targets project/dependencies
        // END: profile-scoped

        assertNotNull(editor.toXml());
    }

    // ========== CROSS-POM SNIPPETS (cross-pom.md) ==========

    void alignToParent() throws DomTripException {
        // START: align-to-parent
        String childPom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <parent>
                    <groupId>com.example</groupId>
                    <artifactId>parent-project</artifactId>
                    <version>1.0.0</version>
                  </parent>
                  <artifactId>child-module</artifactId>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                      <version>32.1.2-jre</version>
                    </dependency>
                  </dependencies>
                </project>
                """;

        String parentPom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>parent-project</artifactId>
                  <version>1.0.0</version>
                  <packaging>pom</packaging>
                </project>
                """;

        PomEditor child = new PomEditor(Document.of(childPom));
        PomEditor parent = new PomEditor(Document.of(parentPom));

        // Move a single dependency's version to the parent's dependencyManagement
        Coordinates guava = Coordinates.of("com.google.guava", "guava", null);
        child.dependencies()
                .alignToParent(
                        guava,
                        parent,
                        AlignOptions.builder()
                                .versionSource(AlignOptions.VersionSource.PROPERTY)
                                .namingConvention(AlignOptions.PropertyNamingConvention.DOT_SUFFIX)
                                .build());

        // Child POM: dependency is now version-less
        // Parent POM: has dependencyManagement entry + guava.version property
        // END: align-to-parent

        String childResult = child.toXml();
        String parentResult = parent.toXml();
        assertFalse(childResult.contains("<version>32.1.2-jre</version>"));
        assertTrue(parentResult.contains("guava.version"));
    }

    void alignAllToParent() throws DomTripException {
        // START: align-all-to-parent
        String childPom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <parent>
                    <groupId>com.example</groupId>
                    <artifactId>parent-project</artifactId>
                    <version>1.0.0</version>
                  </parent>
                  <artifactId>child-module</artifactId>
                  <dependencies>
                    <dependency>
                      <groupId>org.slf4j</groupId>
                      <artifactId>slf4j-api</artifactId>
                      <version>2.0.7</version>
                    </dependency>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                      <version>32.1.2-jre</version>
                    </dependency>
                  </dependencies>
                </project>
                """;

        String parentPom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>parent-project</artifactId>
                  <version>1.0.0</version>
                  <packaging>pom</packaging>
                </project>
                """;

        PomEditor child = new PomEditor(Document.of(childPom));
        PomEditor parent = new PomEditor(Document.of(parentPom));

        // Move ALL dependency versions to the parent
        int moved = child.dependencies()
                .alignAllToParent(
                        parent,
                        AlignOptions.builder()
                                .versionSource(AlignOptions.VersionSource.PROPERTY)
                                .namingConvention(AlignOptions.PropertyNamingConvention.DOT_SUFFIX)
                                .build());
        // Both dependencies are now managed by the parent
        // END: align-all-to-parent

        assertEquals(2, moved);
    }

    // ========== EXAMPLE SNIPPETS (examples.md) ==========

    void springBootProject() throws DomTripException {
        // START: spring-boot-project
        PomEditor editor = new PomEditor();
        editor.createMavenDocument("project");
        Element root = editor.root();

        // Basic project info
        editor.insertMavenElement(root, MODEL_VERSION, "4.0.0");

        // Spring Boot parent
        Coordinates bootParent = Coordinates.of("org.springframework.boot", "spring-boot-starter-parent", "3.2.0");
        editor.parent().setParent(bootParent);

        editor.insertMavenElement(root, GROUP_ID, "com.example");
        editor.insertMavenElement(root, ARTIFACT_ID, "spring-boot-app");
        editor.insertMavenElement(root, VERSION, "1.0.0");

        editor.insertMavenElement(root, NAME, "Spring Boot Application");

        // Properties
        Element properties = editor.insertMavenElement(root, PROPERTIES);
        editor.properties().addProperty(properties, "java.version", "17");

        // Dependencies (versions managed by parent)
        Element deps = editor.insertMavenElement(root, DEPENDENCIES);
        editor.dependencies().addDependency(deps, "org.springframework.boot", "spring-boot-starter-web", null);
        editor.dependencies().addDependency(deps, "org.springframework.boot", "spring-boot-starter-data-jpa", null);
        Element testDep =
                editor.dependencies().addDependency(deps, "org.springframework.boot", "spring-boot-starter-test", null);
        editor.insertMavenElement(testDep, SCOPE, "test");

        // Build
        Element build = editor.insertMavenElement(root, BUILD);
        Element plugins = editor.insertMavenElement(build, PLUGINS);
        editor.plugins().addPlugin(plugins, "org.springframework.boot", "spring-boot-maven-plugin", null);

        String result = editor.toXml();
        // END: spring-boot-project

        assertNotNull(result);
        assertTrue(result.contains("spring-boot-starter-web"));
    }

    void pomTransformation() throws DomTripException {
        // START: pom-transformation
        String existingPom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>legacy-project</artifactId>
                  <version>1.0.0</version>
                </project>
                """;

        PomEditor editor = new PomEditor(Document.of(existingPom));
        Element root = editor.root();

        // Add missing metadata
        if (!editor.hasChildElement(root, NAME)) {
            editor.insertMavenElement(root, NAME, "Upgraded Project");
        }

        // Add or update properties
        editor.properties().updateProperty(true, "maven.compiler.source", "17");
        editor.properties().updateProperty(true, "maven.compiler.target", "17");
        editor.properties().updateProperty(true, "project.build.sourceEncoding", "UTF-8");

        // Add dependencies using Coordinates
        Coordinates junit = Coordinates.of("org.junit.jupiter", "junit-jupiter", "5.10.0");
        editor.dependencies().updateDependency(true, junit);

        // Add build plugins
        Coordinates compiler = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", "3.12.0");
        editor.plugins().updatePlugin(true, compiler);

        // Set project version
        editor.setVersion("2.0.0");

        String result = editor.toXml();
        // END: pom-transformation

        assertNotNull(result);
        assertTrue(result.contains("2.0.0"));
    }
}
