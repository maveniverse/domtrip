/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.maven;

import static eu.maveniverse.domtrip.maven.MavenPomElements.Elements.*;
import static org.junit.jupiter.api.Assertions.*;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Element;
import org.junit.jupiter.api.Test;

/**
 * Tests for the PomEditor class.
 */
class PomEditorTest {

    @Test
    void testCreateMavenDocument() throws DomTripException {
        PomEditor editor = new PomEditor();
        editor.createMavenDocument("project");

        Element root = editor.root();
        assertNotNull(root);
        assertEquals("project", root.name());
        assertEquals(MavenPomElements.Namespaces.MAVEN_4_0_0_NAMESPACE, root.attribute("xmlns"));
        assertEquals(MavenPomElements.Attributes.XSI_NAMESPACE_URI, root.attribute("xmlns:xsi"));
        assertEquals(
                MavenPomElements.SchemaLocations.MAVEN_4_0_0_SCHEMA_LOCATION, root.attribute("xsi:schemaLocation"));
    }

    @Test
    void testInsertMavenElementWithOrdering() throws Exception {
        String pomXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.example</groupId>
              <artifactId>test-project</artifactId>
              <version>1.0.0</version>
            </project>
            """;

        Document doc = Document.of(pomXml);
        PomEditor editor = new PomEditor(doc);
        Element root = editor.root();

        // Add description - should come after version
        editor.insertMavenElement(root, DESCRIPTION, "Test project description");

        // Add name - should come before description
        editor.insertMavenElement(root, NAME, "Test Project");

        String result = editor.toXml();

        // Verify that name comes before description
        int nameIndex = result.indexOf("<name>");
        int descIndex = result.indexOf("<description>");
        assertTrue(nameIndex < descIndex, "Name should come before description");

        // Verify content
        assertTrue(result.contains("<name>Test Project</name>"));
        assertTrue(result.contains("<description>Test project description</description>"));
    }

    @Test
    void testAddDependency() throws DomTripException {
        PomEditor editor = new PomEditor();
        editor.createMavenDocument("project");
        Element root = editor.root();

        // Add basic project info
        editor.insertMavenElement(root, MODEL_VERSION, "4.0.0");
        editor.insertMavenElement(root, GROUP_ID, "com.example");
        editor.insertMavenElement(root, ARTIFACT_ID, "test-project");
        editor.insertMavenElement(root, VERSION, "1.0.0");

        // Add dependencies section
        Element dependencies = editor.insertMavenElement(root, DEPENDENCIES);

        // Add a dependency
        Element dependency =
                editor.dependencies().addDependency(dependencies, "org.junit.jupiter", "junit-jupiter", "5.9.2");

        assertNotNull(dependency);
        assertEquals("dependency", dependency.name());

        // Verify dependency structure
        assertEquals(
                "org.junit.jupiter",
                dependency.childElement(GROUP_ID).orElseThrow().textContent());
        assertEquals(
                "junit-jupiter",
                dependency.childElement(ARTIFACT_ID).orElseThrow().textContent());
        assertEquals("5.9.2", dependency.childElement(VERSION).orElseThrow().textContent());

        String result = editor.toXml();
        assertTrue(result.contains("<groupId>org.junit.jupiter</groupId>"));
        assertTrue(result.contains("<artifactId>junit-jupiter</artifactId>"));
        assertTrue(result.contains("<version>5.9.2</version>"));
    }

    @Test
    void testAddPlugin() throws DomTripException {
        PomEditor editor = new PomEditor();
        editor.createMavenDocument("project");
        Element root = editor.root();

        // Add basic project info
        editor.insertMavenElement(root, MODEL_VERSION, "4.0.0");
        editor.insertMavenElement(root, GROUP_ID, "com.example");
        editor.insertMavenElement(root, ARTIFACT_ID, "test-project");
        editor.insertMavenElement(root, VERSION, "1.0.0");

        // Add build section
        Element build = editor.insertMavenElement(root, BUILD);
        Element plugins = editor.insertMavenElement(build, PLUGINS);

        // Add a plugin
        Element plugin =
                editor.plugins().addPlugin(plugins, "org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");

        assertNotNull(plugin);
        assertEquals("plugin", plugin.name());

        // Verify plugin structure
        assertEquals(
                "org.apache.maven.plugins",
                plugin.childElement(GROUP_ID).orElseThrow().textContent());
        assertEquals(
                "maven-compiler-plugin",
                plugin.childElement(ARTIFACT_ID).orElseThrow().textContent());
        assertEquals("3.11.0", plugin.childElement(VERSION).orElseThrow().textContent());
    }

    @Test
    void testAddModule() throws DomTripException {
        PomEditor editor = new PomEditor();
        editor.createMavenDocument("project");
        Element root = editor.root();

        // Add basic project info
        editor.insertMavenElement(root, MODEL_VERSION, "4.0.0");
        editor.insertMavenElement(root, GROUP_ID, "com.example");
        editor.insertMavenElement(root, ARTIFACT_ID, "parent-project");
        editor.insertMavenElement(root, VERSION, "1.0.0");
        editor.insertMavenElement(root, PACKAGING, "pom");

        // Add modules section
        Element modules = editor.insertMavenElement(root, MODULES);

        // Add modules
        editor.subprojects().addModule(modules, "module1");
        editor.subprojects().addModule(modules, "module2");

        String result = editor.toXml();
        assertTrue(result.contains("<module>module1</module>"));
        assertTrue(result.contains("<module>module2</module>"));
    }

    @Test
    void testFindChildElement() throws DomTripException {
        String pomXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.example</groupId>
              <artifactId>test-project</artifactId>
              <version>1.0.0</version>
              <dependencies>
                <dependency>
                  <groupId>junit</groupId>
                  <artifactId>junit</artifactId>
                  <version>4.13.2</version>
                </dependency>
              </dependencies>
            </project>
            """;

        Document doc = Document.of(pomXml);
        PomEditor editor = new PomEditor(doc);
        Element root = editor.root();

        // Find existing elements
        Element dependencies = editor.findChildElement(root, DEPENDENCIES);
        assertNotNull(dependencies);
        assertEquals("dependencies", dependencies.name());

        Element dependency = editor.findChildElement(dependencies, DEPENDENCY);
        assertNotNull(dependency);
        assertEquals("dependency", dependency.name());

        // Try to find non-existent element
        Element nonExistent = editor.findChildElement(root, "nonexistent");
        assertNull(nonExistent);
    }

    @Test
    void testElementOrdering() throws DomTripException {
        PomEditor editor = new PomEditor();
        editor.createMavenDocument("project");
        Element root = editor.root();

        // Add elements in random order
        editor.insertMavenElement(root, VERSION, "1.0.0");
        editor.insertMavenElement(root, MODEL_VERSION, "4.0.0");
        editor.insertMavenElement(root, ARTIFACT_ID, "test-project");
        editor.insertMavenElement(root, GROUP_ID, "com.example");
        editor.insertMavenElement(root, DESCRIPTION, "A test project");
        editor.insertMavenElement(root, NAME, "Test Project");

        String result = editor.toXml();

        // Verify ordering: modelVersion, groupId, artifactId, version, name, description
        int modelVersionIndex = result.indexOf("<modelVersion>");
        int groupIdIndex = result.indexOf("<groupId>");
        int artifactIdIndex = result.indexOf("<artifactId>");
        int versionIndex = result.indexOf("<version>");
        int nameIndex = result.indexOf("<name>");
        int descriptionIndex = result.indexOf("<description>");

        assertTrue(modelVersionIndex < groupIdIndex);
        assertTrue(groupIdIndex < artifactIdIndex);
        assertTrue(artifactIdIndex < versionIndex);
        assertTrue(versionIndex < nameIndex);
        assertTrue(nameIndex < descriptionIndex);
    }

    @Test
    void testHasChildElement() throws DomTripException {
        PomEditor editor = new PomEditor();
        editor.createMavenDocument("project");
        Element root = editor.root();

        // Initially no children
        assertFalse(editor.hasChildElement(root, "name"));
        assertFalse(editor.hasChildElement(root, "description"));

        // Add a child
        editor.insertMavenElement(root, "name", "Test Project");
        assertTrue(editor.hasChildElement(root, "name"));
        assertFalse(editor.hasChildElement(root, "description"));

        // Add another child
        editor.insertMavenElement(root, "description", "A test project");
        assertTrue(editor.hasChildElement(root, "name"));
        assertTrue(editor.hasChildElement(root, "description"));
    }

    @Test
    void testGetChildElementText() throws DomTripException {
        PomEditor editor = new PomEditor();
        editor.createMavenDocument("project");
        Element root = editor.root();

        // Non-existent child returns null
        assertNull(editor.getChildElementText(root, "name"));
        assertNull(editor.getChildElementText(root, "description"));

        // Add children with content
        editor.insertMavenElement(root, "name", "Test Project");
        editor.insertMavenElement(root, "description", "A test project");

        // Verify content retrieval
        assertEquals("Test Project", editor.getChildElementText(root, "name"));
        assertEquals("A test project", editor.getChildElementText(root, "description"));
        assertNull(editor.getChildElementText(root, "nonexistent"));
    }

    @Test
    void testUpdateOrCreateChildElement() throws DomTripException {
        PomEditor editor = new PomEditor();
        editor.createMavenDocument("project");
        Element root = editor.root();

        // Create new element
        Element name = editor.updateOrCreateChildElement(root, "name", "Test Project");
        assertNotNull(name);
        assertEquals("Test Project", name.textContent());
        assertTrue(editor.hasChildElement(root, "name"));

        // Update existing element
        Element updatedName = editor.updateOrCreateChildElement(root, "name", "Updated Project");
        assertNotNull(updatedName);
        assertEquals("Updated Project", updatedName.textContent());
        assertEquals("Updated Project", editor.getChildElementText(root, "name"));

        // Verify it is the same element (updated, not recreated)
        assertEquals(name, updatedName);
    }

    @Test
    void testUpdateOrCreateChildElementWithOrdering() throws DomTripException {
        PomEditor editor = new PomEditor();
        editor.createMavenDocument("project");
        Element root = editor.root();

        // Add elements in reverse order - they should be properly ordered
        editor.updateOrCreateChildElement(root, "description", "A test project");
        editor.updateOrCreateChildElement(root, "name", "Test Project");
        editor.updateOrCreateChildElement(root, "groupId", "com.example");

        String result = editor.toXml();

        // Verify proper ordering: groupId comes before name, name comes before description
        int groupIdIndex = result.indexOf("<groupId>");
        int nameIndex = result.indexOf("<name>");
        int descriptionIndex = result.indexOf("<description>");

        assertTrue(groupIdIndex < nameIndex);
        assertTrue(nameIndex < descriptionIndex);
    }

    @Test
    void testUpdateManagedPluginCreate() throws DomTripException {
        PomEditor editor = new PomEditor();
        editor.createMavenDocument("project");

        Coordinates compilerPlugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");

        // Create new managed plugin with upsert=true
        boolean result = editor.plugins().updateManagedPlugin(true, compilerPlugin);
        assertTrue(result);

        // Verify structure was created
        Element root = editor.root();
        Element build = editor.findChildElement(root, BUILD);
        assertNotNull(build);
        Element pluginManagement = editor.findChildElement(build, PLUGIN_MANAGEMENT);
        assertNotNull(pluginManagement);
        Element plugins = editor.findChildElement(pluginManagement, PLUGINS);
        assertNotNull(plugins);

        // Verify plugin was added
        Element plugin = plugins.childElements(PLUGIN)
                .filter(compilerPlugin.predicateGA())
                .findFirst()
                .orElse(null);
        assertNotNull(plugin);
        assertEquals("3.11.0", plugin.childElement(VERSION).orElseThrow().textContent());
    }

    @Test
    void testUpdateManagedPluginUpdate() throws DomTripException {
        String pomXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.example</groupId>
              <artifactId>test-project</artifactId>
              <version>1.0.0</version>
              <build>
                <pluginManagement>
                  <plugins>
                    <plugin>
                      <groupId>org.apache.maven.plugins</groupId>
                      <artifactId>maven-compiler-plugin</artifactId>
                      <version>3.10.0</version>
                    </plugin>
                  </plugins>
                </pluginManagement>
              </build>
            </project>
            """;

        Document doc = Document.of(pomXml);
        PomEditor editor = new PomEditor(doc);

        Coordinates compilerPlugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");

        // Update existing plugin
        boolean result = editor.plugins().updateManagedPlugin(false, compilerPlugin);
        assertTrue(result);

        // Verify version was updated
        Element root = editor.root();
        Element build = editor.findChildElement(root, BUILD);
        Element pluginManagement = editor.findChildElement(build, PLUGIN_MANAGEMENT);
        Element plugins = editor.findChildElement(pluginManagement, PLUGINS);
        Element plugin = plugins.childElements(PLUGIN)
                .filter(compilerPlugin.predicateGA())
                .findFirst()
                .orElse(null);
        assertNotNull(plugin);
        assertEquals("3.11.0", plugin.childElement(VERSION).orElseThrow().textContent());
    }

    @Test
    void testUpdateManagedPluginWithProperty() throws DomTripException {
        String pomXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.example</groupId>
              <artifactId>test-project</artifactId>
              <version>1.0.0</version>
              <properties>
                <compiler.version>3.10.0</compiler.version>
              </properties>
              <build>
                <pluginManagement>
                  <plugins>
                    <plugin>
                      <groupId>org.apache.maven.plugins</groupId>
                      <artifactId>maven-compiler-plugin</artifactId>
                      <version>${compiler.version}</version>
                    </plugin>
                  </plugins>
                </pluginManagement>
              </build>
            </project>
            """;

        Document doc = Document.of(pomXml);
        PomEditor editor = new PomEditor(doc);

        Coordinates compilerPlugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");

        // Update plugin with property reference - should update property value
        boolean result = editor.plugins().updateManagedPlugin(false, compilerPlugin);
        assertTrue(result);

        // Verify property was updated, not the version element
        Element root = editor.root();
        Element properties = editor.findChildElement(root, PROPERTIES);
        Element compilerVersion = editor.findChildElement(properties, "compiler.version");
        assertEquals("3.11.0", compilerVersion.textContent());

        // Version element should still reference property
        Element build = editor.findChildElement(root, BUILD);
        Element pluginManagement = editor.findChildElement(build, PLUGIN_MANAGEMENT);
        Element plugins = editor.findChildElement(pluginManagement, PLUGINS);
        Element plugin = plugins.childElements(PLUGIN)
                .filter(compilerPlugin.predicateGA())
                .findFirst()
                .orElse(null);
        assertEquals(
                "${compiler.version}",
                plugin.childElement(VERSION).orElseThrow().textContent());
    }

    @Test
    void testUpdateManagedPluginNoUpsert() throws DomTripException {
        PomEditor editor = new PomEditor();
        editor.createMavenDocument("project");

        Coordinates compilerPlugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");

        // Try to update non-existent plugin with upsert=false
        boolean result = editor.plugins().updateManagedPlugin(false, compilerPlugin);
        assertFalse(result);

        // Verify nothing was created
        Element root = editor.root();
        Element build = editor.findChildElement(root, BUILD);
        assertNull(build);
    }

    @Test
    void testDeleteManagedPlugin() throws DomTripException {
        String pomXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.example</groupId>
              <artifactId>test-project</artifactId>
              <version>1.0.0</version>
              <build>
                <pluginManagement>
                  <plugins>
                    <plugin>
                      <groupId>org.apache.maven.plugins</groupId>
                      <artifactId>maven-compiler-plugin</artifactId>
                      <version>3.10.0</version>
                    </plugin>
                    <plugin>
                      <groupId>org.apache.maven.plugins</groupId>
                      <artifactId>maven-surefire-plugin</artifactId>
                      <version>3.0.0</version>
                    </plugin>
                  </plugins>
                </pluginManagement>
              </build>
            </project>
            """;

        Document doc = Document.of(pomXml);
        PomEditor editor = new PomEditor(doc);

        Coordinates compilerPlugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");

        // Delete the plugin
        boolean result = editor.plugins().deleteManagedPlugin(compilerPlugin);
        assertTrue(result);

        // Verify plugin was removed
        Element root = editor.root();
        Element build = editor.findChildElement(root, BUILD);
        Element pluginManagement = editor.findChildElement(build, PLUGIN_MANAGEMENT);
        Element plugins = editor.findChildElement(pluginManagement, PLUGINS);
        Element plugin = plugins.childElements(PLUGIN)
                .filter(compilerPlugin.predicateGA())
                .findFirst()
                .orElse(null);
        assertNull(plugin);

        // Verify other plugin still exists
        Coordinates surefirePlugin = Coordinates.of("org.apache.maven.plugins", "maven-surefire-plugin", "3.0.0");
        Element surefire = plugins.childElements(PLUGIN)
                .filter(surefirePlugin.predicateGA())
                .findFirst()
                .orElse(null);
        assertNotNull(surefire);
    }

    @Test
    void testDeleteManagedPluginNotFound() throws DomTripException {
        PomEditor editor = new PomEditor();
        editor.createMavenDocument("project");

        Coordinates compilerPlugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");

        // Try to delete non-existent plugin
        boolean result = editor.plugins().deleteManagedPlugin(compilerPlugin);
        assertFalse(result);
    }

    @Test
    void testUpdatePluginCreate() throws DomTripException {
        PomEditor editor = new PomEditor();
        editor.createMavenDocument("project");

        Coordinates compilerPlugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");

        // Create new plugin with upsert=true
        boolean result = editor.plugins().updatePlugin(true, compilerPlugin);
        assertTrue(result);

        // Verify structure was created
        Element root = editor.root();
        Element build = editor.findChildElement(root, BUILD);
        assertNotNull(build);
        Element plugins = editor.findChildElement(build, PLUGINS);
        assertNotNull(plugins);

        // Verify plugin was added
        Element plugin = plugins.childElements(PLUGIN)
                .filter(compilerPlugin.predicateGA())
                .findFirst()
                .orElse(null);
        assertNotNull(plugin);
        assertEquals("3.11.0", plugin.childElement(VERSION).orElseThrow().textContent());
    }

    @Test
    void testUpdatePluginUpdate() throws DomTripException {
        String pomXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.example</groupId>
              <artifactId>test-project</artifactId>
              <version>1.0.0</version>
              <build>
                <plugins>
                  <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>3.10.0</version>
                  </plugin>
                </plugins>
              </build>
            </project>
            """;

        Document doc = Document.of(pomXml);
        PomEditor editor = new PomEditor(doc);

        Coordinates compilerPlugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");

        // Update existing plugin
        boolean result = editor.plugins().updatePlugin(false, compilerPlugin);
        assertTrue(result);

        // Verify version was updated
        Element root = editor.root();
        Element build = editor.findChildElement(root, BUILD);
        Element plugins = editor.findChildElement(build, PLUGINS);
        Element plugin = plugins.childElements(PLUGIN)
                .filter(compilerPlugin.predicateGA())
                .findFirst()
                .orElse(null);
        assertNotNull(plugin);
        assertEquals("3.11.0", plugin.childElement(VERSION).orElseThrow().textContent());
    }

    @Test
    void testUpdatePluginWithoutVersionUpdatesManagedPlugin() throws DomTripException {
        String pomXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.example</groupId>
              <artifactId>test-project</artifactId>
              <version>1.0.0</version>
              <build>
                <pluginManagement>
                  <plugins>
                    <plugin>
                      <groupId>org.apache.maven.plugins</groupId>
                      <artifactId>maven-compiler-plugin</artifactId>
                      <version>3.10.0</version>
                    </plugin>
                  </plugins>
                </pluginManagement>
                <plugins>
                  <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                  </plugin>
                </plugins>
              </build>
            </project>
            """;

        Document doc = Document.of(pomXml);
        PomEditor editor = new PomEditor(doc);

        Coordinates compilerPlugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");

        // Update plugin without version - should update managed plugin instead
        boolean result = editor.plugins().updatePlugin(false, compilerPlugin);
        assertTrue(result);

        // Verify managed plugin version was updated
        Element root = editor.root();
        Element build = editor.findChildElement(root, BUILD);
        Element pluginManagement = editor.findChildElement(build, PLUGIN_MANAGEMENT);
        Element managedPlugins = editor.findChildElement(pluginManagement, PLUGINS);
        Element managedPlugin = managedPlugins
                .childElements(PLUGIN)
                .filter(compilerPlugin.predicateGA())
                .findFirst()
                .orElse(null);
        assertNotNull(managedPlugin);
        assertEquals("3.11.0", managedPlugin.childElement(VERSION).orElseThrow().textContent());

        // Verify plugin still has no version
        Element plugins = editor.findChildElement(build, PLUGINS);
        Element plugin = plugins.childElements(PLUGIN)
                .filter(compilerPlugin.predicateGA())
                .findFirst()
                .orElse(null);
        assertNotNull(plugin);
        assertFalse(plugin.childElement(VERSION).isPresent());
    }

    @Test
    void testDeletePlugin() throws DomTripException {
        String pomXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.example</groupId>
              <artifactId>test-project</artifactId>
              <version>1.0.0</version>
              <build>
                <plugins>
                  <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>3.10.0</version>
                  </plugin>
                  <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <version>3.0.0</version>
                  </plugin>
                </plugins>
              </build>
            </project>
            """;

        Document doc = Document.of(pomXml);
        PomEditor editor = new PomEditor(doc);

        Coordinates compilerPlugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");

        // Delete the plugin
        boolean result = editor.plugins().deletePlugin(compilerPlugin);
        assertTrue(result);

        // Verify plugin was removed
        Element root = editor.root();
        Element build = editor.findChildElement(root, BUILD);
        Element plugins = editor.findChildElement(build, PLUGINS);
        Element plugin = plugins.childElements(PLUGIN)
                .filter(compilerPlugin.predicateGA())
                .findFirst()
                .orElse(null);
        assertNull(plugin);

        // Verify other plugin still exists
        Coordinates surefirePlugin = Coordinates.of("org.apache.maven.plugins", "maven-surefire-plugin", "3.0.0");
        Element surefire = plugins.childElements(PLUGIN)
                .filter(surefirePlugin.predicateGA())
                .findFirst()
                .orElse(null);
        assertNotNull(surefire);
    }

    @Test
    void testDeletePluginNotFound() throws DomTripException {
        PomEditor editor = new PomEditor();
        editor.createMavenDocument("project");

        Coordinates compilerPlugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");

        // Try to delete non-existent plugin
        boolean result = editor.plugins().deletePlugin(compilerPlugin);
        assertFalse(result);
    }

    // ========== EXCLUSION TESTS ==========

    // ========== Exclusion test fixtures ==========

    private static final Coordinates SPRING_CORE = Coordinates.of("org.springframework", "spring-core", "5.3.20");
    private static final Coordinates COMMONS_LOGGING_EXCL = Coordinates.of("commons-logging", "commons-logging", null);
    private static final Coordinates LOG4J_EXCL = Coordinates.of("log4j", "log4j", null);
    private static final Coordinates NONEXISTENT_DEP = Coordinates.of("com.nonexistent", "nonexistent", "1.0");
    private static final Coordinates NULL_GROUP_EXCL = Coordinates.of(null, "commons-logging", null);

    private static final String POM_BARE = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.example</groupId>
              <artifactId>test-project</artifactId>
              <version>1.0.0</version>
            </project>
            """;

    private static final String POM_WITH_DEPENDENCY = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.example</groupId>
              <artifactId>test-project</artifactId>
              <version>1.0.0</version>
              <dependencies>
                <dependency>
                  <groupId>org.springframework</groupId>
                  <artifactId>spring-core</artifactId>
                  <version>5.3.20</version>
                </dependency>
              </dependencies>
            </project>
            """;

    private static final String POM_WITH_DEPENDENCY_AND_EXCLUSION = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.example</groupId>
              <artifactId>test-project</artifactId>
              <version>1.0.0</version>
              <dependencies>
                <dependency>
                  <groupId>org.springframework</groupId>
                  <artifactId>spring-core</artifactId>
                  <version>5.3.20</version>
                  <exclusions>
                    <exclusion>
                      <groupId>commons-logging</groupId>
                      <artifactId>commons-logging</artifactId>
                    </exclusion>
                  </exclusions>
                </dependency>
              </dependencies>
            </project>
            """;

    private static final String POM_WITH_DEPENDENCY_AND_TWO_EXCLUSIONS = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.example</groupId>
              <artifactId>test-project</artifactId>
              <version>1.0.0</version>
              <dependencies>
                <dependency>
                  <groupId>org.springframework</groupId>
                  <artifactId>spring-core</artifactId>
                  <version>5.3.20</version>
                  <exclusions>
                    <exclusion>
                      <groupId>commons-logging</groupId>
                      <artifactId>commons-logging</artifactId>
                    </exclusion>
                    <exclusion>
                      <groupId>log4j</groupId>
                      <artifactId>log4j</artifactId>
                    </exclusion>
                  </exclusions>
                </dependency>
              </dependencies>
            </project>
            """;

    private static final String POM_WITH_MANAGED_DEPENDENCY = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.example</groupId>
              <artifactId>test-project</artifactId>
              <version>1.0.0</version>
              <dependencyManagement>
                <dependencies>
                  <dependency>
                    <groupId>org.springframework</groupId>
                    <artifactId>spring-core</artifactId>
                    <version>5.3.20</version>
                  </dependency>
                </dependencies>
              </dependencyManagement>
            </project>
            """;

    private static final String POM_WITH_MANAGED_DEPENDENCY_AND_EXCLUSION = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.example</groupId>
              <artifactId>test-project</artifactId>
              <version>1.0.0</version>
              <dependencyManagement>
                <dependencies>
                  <dependency>
                    <groupId>org.springframework</groupId>
                    <artifactId>spring-core</artifactId>
                    <version>5.3.20</version>
                    <exclusions>
                      <exclusion>
                        <groupId>commons-logging</groupId>
                        <artifactId>commons-logging</artifactId>
                      </exclusion>
                    </exclusions>
                  </dependency>
                </dependencies>
              </dependencyManagement>
            </project>
            """;

    private static final String POM_WITH_EMPTY_DEPENDENCY_MANAGEMENT = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.example</groupId>
              <artifactId>test-project</artifactId>
              <version>1.0.0</version>
              <dependencyManagement>
              </dependencyManagement>
            </project>
            """;

    private PomEditor editorOf(String pomXml) {
        return new PomEditor(Document.of(pomXml));
    }

    // ========== Exclusion tests ==========

    @Test
    void testAddExclusion() throws DomTripException {
        PomEditor editor = editorOf(POM_WITH_DEPENDENCY);

        Element exclElement = editor.dependencies().addExclusion(SPRING_CORE, COMMONS_LOGGING_EXCL);

        assertNotNull(exclElement);
        assertEquals("exclusion", exclElement.name());

        String result = editor.toXml();
        assertTrue(result.contains("<exclusions>"));
        assertTrue(result.contains("<exclusion>"));
        assertTrue(result.contains("<groupId>commons-logging</groupId>"));
        assertTrue(result.contains("<artifactId>commons-logging</artifactId>"));
    }

    @Test
    void testAddExclusionCreatesExclusionsWrapper() throws DomTripException {
        PomEditor editor = editorOf(POM_WITH_DEPENDENCY);

        // Verify no exclusions exist yet
        Element root = editor.root();
        Element dependencies = editor.findChildElement(root, DEPENDENCIES);
        Element dependency = editor.findChildElement(dependencies, DEPENDENCY);
        assertNull(editor.findChildElement(dependency, EXCLUSIONS));

        editor.dependencies().addExclusion(SPRING_CORE, COMMONS_LOGGING_EXCL);

        // Verify exclusions wrapper was created
        Element exclusions = editor.findChildElement(dependency, EXCLUSIONS);
        assertNotNull(exclusions);
        assertEquals("exclusions", exclusions.name());
    }

    @Test
    void testDeleteExclusion() throws DomTripException {
        PomEditor editor = editorOf(POM_WITH_DEPENDENCY_AND_TWO_EXCLUSIONS);

        boolean result = editor.dependencies().deleteExclusion(SPRING_CORE, COMMONS_LOGGING_EXCL);
        assertTrue(result);

        // Verify the exclusion was removed
        assertFalse(editor.dependencies().hasExclusion(SPRING_CORE, COMMONS_LOGGING_EXCL));

        // Verify the other exclusion still exists
        assertTrue(editor.dependencies().hasExclusion(SPRING_CORE, LOG4J_EXCL));

        // Verify exclusions wrapper still exists (since there's still one exclusion)
        assertTrue(editor.toXml().contains("<exclusions>"));
    }

    @Test
    void testDeleteLastExclusionRemovesWrapper() throws DomTripException {
        PomEditor editor = editorOf(POM_WITH_DEPENDENCY_AND_EXCLUSION);

        boolean result = editor.dependencies().deleteExclusion(SPRING_CORE, COMMONS_LOGGING_EXCL);
        assertTrue(result);

        // Verify exclusions wrapper was removed since it's now empty
        String xml = editor.toXml();
        assertFalse(xml.contains("<exclusions>"));
        assertFalse(xml.contains("<exclusion>"));
    }

    @Test
    void testHasExclusion() throws DomTripException {
        PomEditor editor = editorOf(POM_WITH_DEPENDENCY_AND_EXCLUSION);

        assertTrue(editor.dependencies().hasExclusion(SPRING_CORE, COMMONS_LOGGING_EXCL));
        assertFalse(editor.dependencies().hasExclusion(SPRING_CORE, LOG4J_EXCL));

        // Non-existing dependency
        Coordinates nonExistingDep = Coordinates.of("org.example", "nonexistent", null);
        assertFalse(editor.dependencies().hasExclusion(nonExistingDep, COMMONS_LOGGING_EXCL));
    }

    @Test
    void testAddManagedExclusion() throws DomTripException {
        PomEditor editor = editorOf(POM_WITH_MANAGED_DEPENDENCY);

        Element exclElement = editor.dependencies().addManagedExclusion(SPRING_CORE, COMMONS_LOGGING_EXCL);

        assertNotNull(exclElement);
        assertEquals("exclusion", exclElement.name());
        assertTrue(editor.dependencies().hasManagedExclusion(SPRING_CORE, COMMONS_LOGGING_EXCL));

        String result = editor.toXml();
        assertTrue(result.contains("<exclusions>"));
        assertTrue(result.contains("<exclusion>"));
    }

    @Test
    void testMultipleExclusionsOnSameDependency() throws DomTripException {
        PomEditor editor = editorOf(POM_WITH_DEPENDENCY);

        Coordinates excl3 = Coordinates.of("org.slf4j", "slf4j-api", null);

        editor.dependencies().addExclusion(SPRING_CORE, COMMONS_LOGGING_EXCL);
        editor.dependencies().addExclusion(SPRING_CORE, LOG4J_EXCL);
        editor.dependencies().addExclusion(SPRING_CORE, excl3);

        assertTrue(editor.dependencies().hasExclusion(SPRING_CORE, COMMONS_LOGGING_EXCL));
        assertTrue(editor.dependencies().hasExclusion(SPRING_CORE, LOG4J_EXCL));
        assertTrue(editor.dependencies().hasExclusion(SPRING_CORE, excl3));

        // Delete one and verify others remain
        assertTrue(editor.dependencies().deleteExclusion(SPRING_CORE, LOG4J_EXCL));
        assertTrue(editor.dependencies().hasExclusion(SPRING_CORE, COMMONS_LOGGING_EXCL));
        assertFalse(editor.dependencies().hasExclusion(SPRING_CORE, LOG4J_EXCL));
        assertTrue(editor.dependencies().hasExclusion(SPRING_CORE, excl3));
    }

    @Test
    void testDeleteManagedExclusion() throws DomTripException {
        PomEditor editor = editorOf(POM_WITH_MANAGED_DEPENDENCY_AND_EXCLUSION);

        assertTrue(editor.dependencies().hasManagedExclusion(SPRING_CORE, COMMONS_LOGGING_EXCL));

        boolean result = editor.dependencies().deleteManagedExclusion(SPRING_CORE, COMMONS_LOGGING_EXCL);
        assertTrue(result);
        assertFalse(editor.dependencies().hasManagedExclusion(SPRING_CORE, COMMONS_LOGGING_EXCL));
        assertFalse(editor.toXml().contains("<exclusions>"));
    }

    @Test
    void testDeleteExclusionNotFound() throws DomTripException {
        PomEditor editor = editorOf(POM_WITH_DEPENDENCY);
        assertFalse(editor.dependencies().deleteExclusion(SPRING_CORE, COMMONS_LOGGING_EXCL));
    }

    @Test
    void testAddExclusionWithNullGroupIdThrows() throws DomTripException {
        PomEditor.Dependencies deps = editorOf(POM_WITH_DEPENDENCY).dependencies();
        assertThrows(DomTripException.class, () -> deps.addExclusion(SPRING_CORE, NULL_GROUP_EXCL));
    }

    @Test
    void testAddExclusionDependencyNotFoundThrows() throws DomTripException {
        PomEditor.Dependencies deps = editorOf(POM_WITH_DEPENDENCY).dependencies();
        assertThrows(DomTripException.class, () -> deps.addExclusion(NONEXISTENT_DEP, COMMONS_LOGGING_EXCL));
    }

    @Test
    void testAddExclusionNoDependenciesThrows() throws DomTripException {
        PomEditor.Dependencies deps = editorOf(POM_BARE).dependencies();
        assertThrows(DomTripException.class, () -> deps.addExclusion(SPRING_CORE, COMMONS_LOGGING_EXCL));
    }

    @Test
    void testDeleteExclusionNoDependenciesReturnsFalse() throws DomTripException {
        assertFalse(editorOf(POM_BARE).dependencies().deleteExclusion(SPRING_CORE, COMMONS_LOGGING_EXCL));
    }

    @Test
    void testDeleteExclusionDependencyNotFoundReturnsFalse() throws DomTripException {
        assertFalse(
                editorOf(POM_WITH_DEPENDENCY).dependencies().deleteExclusion(NONEXISTENT_DEP, COMMONS_LOGGING_EXCL));
    }

    @Test
    void testHasExclusionNoDependenciesReturnsFalse() throws DomTripException {
        assertFalse(editorOf(POM_BARE).dependencies().hasExclusion(SPRING_CORE, COMMONS_LOGGING_EXCL));
    }

    @Test
    void testAddManagedExclusionNoDependencyManagementThrows() throws DomTripException {
        PomEditor.Dependencies deps = editorOf(POM_BARE).dependencies();
        assertThrows(DomTripException.class, () -> deps.addManagedExclusion(SPRING_CORE, COMMONS_LOGGING_EXCL));
    }

    @Test
    void testAddManagedExclusionWithNullGroupIdThrows() throws DomTripException {
        PomEditor.Dependencies deps = editorOf(POM_WITH_MANAGED_DEPENDENCY).dependencies();
        assertThrows(DomTripException.class, () -> deps.addManagedExclusion(SPRING_CORE, NULL_GROUP_EXCL));
    }

    @Test
    void testDeleteManagedExclusionNoDependencyManagementReturnsFalse() throws DomTripException {
        assertFalse(editorOf(POM_BARE).dependencies().deleteManagedExclusion(SPRING_CORE, COMMONS_LOGGING_EXCL));
    }

    @Test
    void testDeleteManagedExclusionNoDependenciesReturnsFalse() throws DomTripException {
        assertFalse(editorOf(POM_WITH_EMPTY_DEPENDENCY_MANAGEMENT)
                .dependencies()
                .deleteManagedExclusion(SPRING_CORE, COMMONS_LOGGING_EXCL));
    }

    @Test
    void testDeleteManagedExclusionDependencyNotFoundReturnsFalse() throws DomTripException {
        assertFalse(editorOf(POM_WITH_MANAGED_DEPENDENCY)
                .dependencies()
                .deleteManagedExclusion(NONEXISTENT_DEP, COMMONS_LOGGING_EXCL));
    }

    @Test
    void testHasManagedExclusionNoDependencyManagementReturnsFalse() throws DomTripException {
        assertFalse(editorOf(POM_BARE).dependencies().hasManagedExclusion(SPRING_CORE, COMMONS_LOGGING_EXCL));
    }

    @Test
    void testHasManagedExclusionNoDependenciesReturnsFalse() throws DomTripException {
        assertFalse(editorOf(POM_WITH_EMPTY_DEPENDENCY_MANAGEMENT)
                .dependencies()
                .hasManagedExclusion(SPRING_CORE, COMMONS_LOGGING_EXCL));
    }

    @Test
    void testHasManagedExclusionDependencyNotFoundReturnsFalse() throws DomTripException {
        assertFalse(editorOf(POM_WITH_MANAGED_DEPENDENCY)
                .dependencies()
                .hasManagedExclusion(NONEXISTENT_DEP, COMMONS_LOGGING_EXCL));
    }

    @Test
    void testAddManagedExclusionDependencyNotFoundThrows() throws DomTripException {
        PomEditor.Dependencies deps = editorOf(POM_WITH_MANAGED_DEPENDENCY).dependencies();
        assertThrows(DomTripException.class, () -> deps.addManagedExclusion(NONEXISTENT_DEP, COMMONS_LOGGING_EXCL));
    }

    @Test
    void testAddManagedExclusionNoDependenciesInManagementThrows() throws DomTripException {
        PomEditor.Dependencies deps =
                editorOf(POM_WITH_EMPTY_DEPENDENCY_MANAGEMENT).dependencies();
        assertThrows(DomTripException.class, () -> deps.addManagedExclusion(SPRING_CORE, COMMONS_LOGGING_EXCL));
    }
}
