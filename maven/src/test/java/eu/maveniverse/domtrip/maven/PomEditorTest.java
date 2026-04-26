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

    // ========== ALIGNED DEPENDENCY TESTS ==========

    private static final String POM_MANAGED_PROPERTY = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.example</groupId>
              <artifactId>test-project</artifactId>
              <version>1.0.0</version>
              <properties>
                <guava.version>32.1.2-jre</guava.version>
                <slf4j.version>2.0.9</slf4j.version>
              </properties>
              <dependencyManagement>
                <dependencies>
                  <dependency>
                    <groupId>com.google.guava</groupId>
                    <artifactId>guava</artifactId>
                    <version>${guava.version}</version>
                  </dependency>
                  <dependency>
                    <groupId>org.slf4j</groupId>
                    <artifactId>slf4j-api</artifactId>
                    <version>${slf4j.version}</version>
                  </dependency>
                </dependencies>
              </dependencyManagement>
              <dependencies>
                <dependency>
                  <groupId>com.google.guava</groupId>
                  <artifactId>guava</artifactId>
                </dependency>
                <dependency>
                  <groupId>org.slf4j</groupId>
                  <artifactId>slf4j-api</artifactId>
                </dependency>
              </dependencies>
            </project>
            """;

    private static final String POM_INLINE_LITERAL = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.example</groupId>
              <artifactId>test-project</artifactId>
              <version>1.0.0</version>
              <dependencies>
                <dependency>
                  <groupId>com.google.guava</groupId>
                  <artifactId>guava</artifactId>
                  <version>32.1.2-jre</version>
                </dependency>
                <dependency>
                  <groupId>org.slf4j</groupId>
                  <artifactId>slf4j-api</artifactId>
                  <version>2.0.9</version>
                </dependency>
              </dependencies>
            </project>
            """;

    @Test
    void testDetectConventionsManagedProperty() {
        PomEditor editor = editorOf(POM_MANAGED_PROPERTY);
        AlignOptions options = editor.dependencies().detectConventions();

        assertEquals(AlignOptions.VersionStyle.MANAGED, options.versionStyle());
        assertEquals(AlignOptions.VersionSource.PROPERTY, options.versionSource());
        assertEquals(AlignOptions.PropertyNamingConvention.DOT_SUFFIX, options.namingConvention());
    }

    @Test
    void testDetectConventionsInlineLiteral() {
        PomEditor editor = editorOf(POM_INLINE_LITERAL);
        AlignOptions options = editor.dependencies().detectConventions();

        assertEquals(AlignOptions.VersionStyle.INLINE, options.versionStyle());
        assertEquals(AlignOptions.VersionSource.LITERAL, options.versionSource());
    }

    @Test
    void testAddAlignedFollowsManagedPropertyConvention() {
        PomEditor editor = editorOf(POM_MANAGED_PROPERTY);
        Coordinates jackson = Coordinates.of("com.fasterxml.jackson.core", "jackson-databind", "2.15.0");

        boolean added = editor.dependencies().addAligned(jackson);
        assertTrue(added);

        String xml = editor.toXml();
        // Should create property
        assertTrue(xml.contains("<jackson-databind.version>2.15.0</jackson-databind.version>"));
        // Dependency should be version-less
        assertTrue(xml.contains("<artifactId>jackson-databind</artifactId>"));
        // Managed dependency should reference property
        assertTrue(xml.contains("${jackson-databind.version}"));
    }

    @Test
    void testAddAlignedFollowsInlineLiteralConvention() {
        PomEditor editor = editorOf(POM_INLINE_LITERAL);
        Coordinates jackson = Coordinates.of("com.fasterxml.jackson.core", "jackson-databind", "2.15.0");

        boolean added = editor.dependencies().addAligned(jackson);
        assertTrue(added);

        String xml = editor.toXml();
        // Should have inline version, no property, no managed dep
        assertTrue(xml.contains("<version>2.15.0</version>"));
        assertFalse(xml.contains("<dependencyManagement>"));
        assertFalse(xml.contains("jackson-databind.version"));
    }

    @Test
    void testAddAlignedReturnsFalseForExisting() {
        PomEditor editor = editorOf(POM_INLINE_LITERAL);
        Coordinates guava = Coordinates.of("com.google.guava", "guava", "33.0.0-jre");

        assertFalse(editor.dependencies().addAligned(guava));
    }

    @Test
    void testAddAlignedWithScope() {
        PomEditor editor = editorOf(POM_INLINE_LITERAL);
        Coordinates junit = Coordinates.of("org.junit.jupiter", "junit-jupiter", "5.10.0");

        boolean added = editor.dependencies()
                .addAligned(junit, AlignOptions.builder().scope("test").build());
        assertTrue(added);

        String xml = editor.toXml();
        assertTrue(xml.contains("<scope>test</scope>"));
    }

    @Test
    void testAlignDependencyLiteralToProperty() {
        PomEditor editor = editorOf(POM_INLINE_LITERAL);

        Coordinates guava = Coordinates.of("com.google.guava", "guava", null);
        boolean changed = editor.dependencies()
                .alignDependency(
                        guava,
                        AlignOptions.builder()
                                .versionSource(AlignOptions.VersionSource.PROPERTY)
                                .build());
        assertTrue(changed);

        String xml = editor.toXml();
        assertTrue(xml.contains("<guava.version>32.1.2-jre</guava.version>"));
        assertTrue(xml.contains("<version>${guava.version}</version>"));
    }

    @Test
    void testAlignAllDependencies() {
        PomEditor editor = editorOf(POM_INLINE_LITERAL);

        int count = editor.dependencies()
                .alignAllDependencies(AlignOptions.builder()
                        .versionStyle(AlignOptions.VersionStyle.MANAGED)
                        .versionSource(AlignOptions.VersionSource.PROPERTY)
                        .build());
        assertEquals(2, count);

        String xml = editor.toXml();
        // Both dependencies should now be managed with properties
        assertTrue(xml.contains("<dependencyManagement>"));
        assertTrue(xml.contains("<guava.version>32.1.2-jre</guava.version>"));
        assertTrue(xml.contains("<slf4j-api.version>2.0.9</slf4j-api.version>"));
    }

    @Test
    void testAlignDependencyNotFoundReturnsFalse() {
        PomEditor editor = editorOf(POM_INLINE_LITERAL);
        Coordinates nonExistent = Coordinates.of("com.nonexistent", "nonexistent", null);

        assertFalse(editor.dependencies().alignDependency(nonExistent));
    }

    @Test
    void testAddAlignedWithPropertyNameGenerator() {
        PomEditor editor = editorOf(POM_INLINE_LITERAL);
        Coordinates jackson = Coordinates.of("com.fasterxml.jackson.core", "jackson-databind", "2.15.0");

        boolean added = editor.dependencies()
                .addAligned(
                        jackson,
                        AlignOptions.builder()
                                .versionSource(AlignOptions.VersionSource.PROPERTY)
                                .propertyNameGenerator(coords -> coords.groupId() + "." + coords.artifactId())
                                .build());
        assertTrue(added);

        String xml = editor.toXml();
        assertTrue(xml.contains(
                "<com.fasterxml.jackson.core.jackson-databind>2.15.0</com.fasterxml.jackson.core.jackson-databind>"));
        assertTrue(xml.contains("${com.fasterxml.jackson.core.jackson-databind}"));
    }

    @Test
    void testAlignAllWithPropertyNameGenerator() {
        PomEditor editor = editorOf(POM_INLINE_LITERAL);

        int count = editor.dependencies()
                .alignAllDependencies(AlignOptions.builder()
                        .versionSource(AlignOptions.VersionSource.PROPERTY)
                        .propertyNameGenerator(coords -> "v." + coords.artifactId())
                        .build());
        assertEquals(2, count);

        String xml = editor.toXml();
        assertTrue(xml.contains("<v.guava>32.1.2-jre</v.guava>"));
        assertTrue(xml.contains("<v.slf4j-api>2.0.9</v.slf4j-api>"));
    }

    @Test
    void testPropertyNameOverridesGenerator() {
        PomEditor editor = editorOf(POM_INLINE_LITERAL);
        Coordinates jackson = Coordinates.of("com.fasterxml.jackson.core", "jackson-databind", "2.15.0");

        boolean added = editor.dependencies()
                .addAligned(
                        jackson,
                        AlignOptions.builder()
                                .versionSource(AlignOptions.VersionSource.PROPERTY)
                                .propertyNameGenerator(coords -> "generated." + coords.artifactId())
                                .propertyName("jackson.version")
                                .build());
        assertTrue(added);

        String xml = editor.toXml();
        // Explicit propertyName should win over generator
        assertTrue(xml.contains("<jackson.version>2.15.0</jackson.version>"));
        assertFalse(xml.contains("generated."));
    }

    @Test
    void testAlignedVersionPropertiesInsertedAlphabetically() {
        PomEditor editor = editorOf(POM_MANAGED_PROPERTY);

        // Existing version properties: guava.version, slf4j.version
        // Add dependencies that create new version properties via addAligned
        editor.dependencies().addAligned(Coordinates.of("com.aaa", "aaa-lib", "1.0"));
        editor.dependencies().addAligned(Coordinates.of("com.zzz", "zzz-lib", "2.0"));
        editor.dependencies().addAligned(Coordinates.of("com.fasterxml.jackson.core", "jackson-databind", "2.15.0"));

        String xml = editor.toXml();
        int aaa = xml.indexOf("<aaa-lib.version>");
        int guava = xml.indexOf("<guava.version>");
        int jackson = xml.indexOf("<jackson-databind.version>");
        int slf4j = xml.indexOf("<slf4j.version>");
        int zzz = xml.indexOf("<zzz-lib.version>");

        assertTrue(aaa < guava, "aaa-lib should come before guava");
        assertTrue(guava < jackson, "guava should come before jackson-databind");
        assertTrue(jackson < slf4j, "jackson-databind should come before slf4j");
        assertTrue(slf4j < zzz, "slf4j should come before zzz-lib");
    }

    @Test
    void testAlignedPropertyPreservesBuildProperties() {
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <properties>
                    <maven.compiler.source>17</maven.compiler.source>
                    <maven.compiler.target>17</maven.compiler.target>
                    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                    <guava.version>32.1.2-jre</guava.version>
                  </properties>
                  <dependencyManagement>
                    <dependencies>
                      <dependency>
                        <groupId>com.google.guava</groupId>
                        <artifactId>guava</artifactId>
                        <version>${guava.version}</version>
                      </dependency>
                    </dependencies>
                  </dependencyManagement>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                    </dependency>
                  </dependencies>
                </project>
                """;
        PomEditor editor = editorOf(pom);

        // Add a dependency — new version property should be placed near guava.version, not among build props
        editor.dependencies().addAligned(Coordinates.of("org.slf4j", "slf4j-api", "2.0.9"));

        String xml = editor.toXml();
        int buildProp = xml.indexOf("<project.build.sourceEncoding>");
        int guavaProp = xml.indexOf("<guava.version>");
        int slf4jProp = xml.indexOf("<slf4j-api.version>");

        // Build properties stay before version properties
        assertTrue(buildProp < guavaProp, "build properties should remain before version properties");
        // New version property inserted after existing version properties (alphabetically after guava)
        assertTrue(guavaProp < slf4jProp, "guava.version should come before slf4j-api.version");
    }

    @Test
    void testAlignDependencyRealignsExistingPropertyReference() {
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <properties>
                    <old-guava-prop>32.1.2-jre</old-guava-prop>
                  </properties>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                      <version>${old-guava-prop}</version>
                    </dependency>
                  </dependencies>
                </project>
                """;
        PomEditor editor = editorOf(pom);

        // Align to DOT_SUFFIX convention — should rename old-guava-prop → guava.version
        boolean changed = editor.dependencies()
                .alignDependency(
                        Coordinates.of("com.google.guava", "guava", null),
                        AlignOptions.builder()
                                .versionSource(AlignOptions.VersionSource.PROPERTY)
                                .namingConvention(AlignOptions.PropertyNamingConvention.DOT_SUFFIX)
                                .build());
        assertTrue(changed);

        String xml = editor.toXml();
        assertTrue(xml.contains("<guava.version>32.1.2-jre</guava.version>"));
        assertTrue(xml.contains("<version>${guava.version}</version>"));
        // Old property is intentionally preserved — other dependencies may still reference it
        assertTrue(xml.contains("<old-guava-prop>32.1.2-jre</old-guava-prop>"));
    }

    @Test
    void testAlignDependencySkipsParentInheritedProperty() {
        // Property is NOT defined locally — simulates inheritance from parent POM
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                      <version>${parent.guava.version}</version>
                    </dependency>
                  </dependencies>
                </project>
                """;
        PomEditor editor = editorOf(pom);

        // Re-alignment should be skipped since the property can't be resolved locally
        boolean changed = editor.dependencies()
                .alignDependency(
                        Coordinates.of("com.google.guava", "guava", null),
                        AlignOptions.builder()
                                .versionSource(AlignOptions.VersionSource.PROPERTY)
                                .namingConvention(AlignOptions.PropertyNamingConvention.DOT_SUFFIX)
                                .build());
        assertFalse(changed);

        // Original reference should be preserved
        String xml = editor.toXml();
        assertTrue(xml.contains("<version>${parent.guava.version}</version>"));
    }

    @Test
    void testAddAlignedWithClassifierAndType() {
        PomEditor editor = editorOf(POM_MANAGED_PROPERTY);

        Coordinates coords = Coordinates.of("org.example", "my-lib", "2.0.0", "sources", "jar");
        boolean added = editor.dependencies().addAligned(coords);
        assertTrue(added);

        String xml = editor.toXml();
        assertTrue(xml.contains("<artifactId>my-lib</artifactId>"));
        assertTrue(xml.contains("<classifier>sources</classifier>"));
    }

    @Test
    void testAddAlignedWithNonJarType() {
        PomEditor editor = editorOf(POM_MANAGED_PROPERTY);

        Coordinates coords = Coordinates.of("org.example", "my-bom", "1.0.0", null, "pom");
        boolean added = editor.dependencies().addAligned(coords);
        assertTrue(added);

        String xml = editor.toXml();
        assertTrue(xml.contains("<artifactId>my-bom</artifactId>"));
        assertTrue(xml.contains("<type>pom</type>"));
    }

    @Test
    void testIsPropertyReferenceRejectsCompoundExpressions() {
        // Compound expression like ${a}${b} should NOT be treated as a single property reference
        // Verify that a dep with compound version is treated as literal, not property
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>org.example</groupId>
                      <artifactId>compound</artifactId>
                      <version>${major}.${minor}</version>
                    </dependency>
                  </dependencies>
                </project>
                """;
        PomEditor editor = editorOf(pom);

        // Aligning should treat the compound expression as a literal — create a property for it
        boolean changed = editor.dependencies()
                .alignDependency(
                        Coordinates.of("org.example", "compound", null),
                        AlignOptions.builder()
                                .versionSource(AlignOptions.VersionSource.PROPERTY)
                                .namingConvention(AlignOptions.PropertyNamingConvention.DOT_SUFFIX)
                                .build());
        assertTrue(changed);

        String xml = editor.toXml();
        // The compound expression is treated as a literal value stored in a property
        assertTrue(xml.contains("<compound.version>${major}.${minor}</compound.version>"));
        assertTrue(xml.contains("<version>${compound.version}</version>"));
    }

    @Test
    void testAlignDependencySkipsMatchingPropertyReference() {
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <properties>
                    <guava.version>32.1.2-jre</guava.version>
                  </properties>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                      <version>${guava.version}</version>
                    </dependency>
                  </dependencies>
                </project>
                """;
        PomEditor editor = editorOf(pom);

        // Align guava which already uses ${guava.version} with DOT_SUFFIX — should be a no-op
        boolean changed = editor.dependencies()
                .alignDependency(
                        Coordinates.of("com.google.guava", "guava", null),
                        AlignOptions.builder()
                                .versionSource(AlignOptions.VersionSource.PROPERTY)
                                .namingConvention(AlignOptions.PropertyNamingConvention.DOT_SUFFIX)
                                .build());
        assertFalse(changed);
    }

    @Test
    void testAddAlignedWithoutVersionThrows() {
        PomEditor editor = editorOf(POM_INLINE_LITERAL);
        Coordinates noVersion = Coordinates.of("com.example", "no-version", null);
        PomEditor.Dependencies deps = editor.dependencies();

        assertThrows(DomTripException.class, () -> deps.addAligned(noVersion));
    }

    // ========== PROFILE-SCOPED DEPENDENCY TESTS ==========

    private static final String POM_WITH_PROFILE = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.example</groupId>
              <artifactId>test-project</artifactId>
              <version>1.0.0</version>
              <dependencies>
                <dependency>
                  <groupId>com.google.guava</groupId>
                  <artifactId>guava</artifactId>
                  <version>32.1.2-jre</version>
                </dependency>
              </dependencies>
              <profiles>
                <profile>
                  <id>my-profile</id>
                  <dependencies>
                    <dependency>
                      <groupId>org.slf4j</groupId>
                      <artifactId>slf4j-api</artifactId>
                      <version>2.0.9</version>
                    </dependency>
                  </dependencies>
                </profile>
              </profiles>
            </project>
            """;

    private static final String POM_WITH_PROFILE_MANAGED = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.example</groupId>
              <artifactId>test-project</artifactId>
              <version>1.0.0</version>
              <profiles>
                <profile>
                  <id>test-profile</id>
                  <properties>
                    <junit.version>5.10.0</junit.version>
                  </properties>
                  <dependencyManagement>
                    <dependencies>
                      <dependency>
                        <groupId>org.junit.jupiter</groupId>
                        <artifactId>junit-jupiter</artifactId>
                        <version>${junit.version}</version>
                      </dependency>
                    </dependencies>
                  </dependencyManagement>
                  <dependencies>
                    <dependency>
                      <groupId>org.junit.jupiter</groupId>
                      <artifactId>junit-jupiter</artifactId>
                    </dependency>
                  </dependencies>
                </profile>
              </profiles>
            </project>
            """;

    @Test
    void testProfilesHasProfile() {
        PomEditor editor = editorOf(POM_WITH_PROFILE);
        assertTrue(editor.profiles().hasProfile("my-profile"));
        assertFalse(editor.profiles().hasProfile("nonexistent"));
    }

    @Test
    void testProfilesFindProfile() {
        PomEditor editor = editorOf(POM_WITH_PROFILE);
        assertNotNull(editor.profiles().findProfile("my-profile"));
        assertNull(editor.profiles().findProfile("nonexistent"));
    }

    @Test
    void testForProfileWithNonExistentProfileThrows() {
        PomEditor editor = editorOf(POM_WITH_PROFILE);
        PomEditor.Dependencies deps = editor.dependencies();

        assertThrows(DomTripException.class, () -> deps.forProfile("nonexistent"));
    }

    @Test
    void testAddAlignedWithinProfile() {
        PomEditor editor = editorOf(POM_WITH_PROFILE);
        Coordinates jackson = Coordinates.of("com.fasterxml.jackson.core", "jackson-databind", "2.15.0");

        boolean added = editor.dependencies().forProfile("my-profile").addAligned(jackson);
        assertTrue(added);

        String xml = editor.toXml();
        // The new dependency should be inside the profile
        int profileStart = xml.indexOf("<id>my-profile</id>");
        int profileDeps = xml.indexOf("<artifactId>jackson-databind</artifactId>");
        assertTrue(profileDeps > profileStart, "jackson-databind should be inside the profile");
        // The top-level dependencies should not contain jackson-databind
        int topLevelDeps = xml.indexOf("<artifactId>guava</artifactId>");
        assertTrue(topLevelDeps < profileStart, "guava should remain at top level");
    }

    @Test
    void testDeleteDependencyWithinProfile() {
        PomEditor editor = editorOf(POM_WITH_PROFILE);
        Coordinates slf4j = Coordinates.of("org.slf4j", "slf4j-api", "2.0.9");

        boolean deleted = editor.dependencies().forProfile("my-profile").deleteDependency(slf4j);
        assertTrue(deleted);

        String xml = editor.toXml();
        // slf4j should be gone from the profile
        assertFalse(xml.contains("<artifactId>slf4j-api</artifactId>"));
        // guava should still be at top level
        assertTrue(xml.contains("<artifactId>guava</artifactId>"));
    }

    @Test
    void testUpdateManagedDependencyWithinProfile() {
        PomEditor editor = editorOf(POM_WITH_PROFILE_MANAGED);
        Coordinates mockito = Coordinates.of("org.mockito", "mockito-core", "5.5.0");

        boolean added = editor.dependencies().forProfile("test-profile").updateManagedDependency(true, mockito);
        assertTrue(added);

        String xml = editor.toXml();
        int profileStart = xml.indexOf("<id>test-profile</id>");
        int mockitoPos = xml.indexOf("<artifactId>mockito-core</artifactId>");
        assertTrue(mockitoPos > profileStart, "mockito should be inside the profile's dependencyManagement");
        // Top level should not have dependencyManagement
        String beforeProfile = xml.substring(0, profileStart);
        assertFalse(beforeProfile.contains("<dependencyManagement>"), "Top-level should not have dependencyManagement");
    }

    @Test
    void testDetectConventionsWithinProfile() {
        PomEditor editor = editorOf(POM_WITH_PROFILE_MANAGED);

        AlignOptions options = editor.dependencies().forProfile("test-profile").detectConventions();

        assertEquals(AlignOptions.VersionStyle.MANAGED, options.versionStyle());
        assertEquals(AlignOptions.VersionSource.PROPERTY, options.versionSource());
    }

    @Test
    void testUpdateManagedDependencyPropertyWithinProfile() {
        PomEditor editor = editorOf(POM_WITH_PROFILE_MANAGED);

        // Update junit version via managed dependency — should update the profile's property, not the project root
        Coordinates junit = Coordinates.of("org.junit.jupiter", "junit-jupiter", "5.11.0");
        boolean updated = editor.dependencies().forProfile("test-profile").updateManagedDependency(false, junit);
        assertTrue(updated);

        String xml = editor.toXml();
        // The profile's property should be updated
        assertTrue(xml.contains("<junit.version>5.11.0</junit.version>"));
        // No properties section should appear at the project root
        int profileStart = xml.indexOf("<profiles>");
        String beforeProfiles = xml.substring(0, profileStart);
        assertFalse(beforeProfiles.contains("<properties>"), "Project root should not have a properties section");
    }

    @Test
    void testForProfileWithNullProfileIdThrows() {
        PomEditor editor = editorOf(POM_WITH_PROFILE);
        PomEditor.Dependencies deps = editor.dependencies();

        assertThrows(DomTripException.class, () -> deps.forProfile((String) null));
    }

    @Test
    void testForProfileWithNonProfileElementThrows() {
        PomEditor editor = editorOf(POM_WITH_PROFILE);
        Element root = editor.root();
        PomEditor.Dependencies deps = editor.dependencies();

        assertThrows(DomTripException.class, () -> deps.forProfile(root));
    }

    @Test
    void testForProfileWithElement() {
        PomEditor editor = editorOf(POM_WITH_PROFILE);
        Element profile = editor.profiles().findProfile("my-profile");
        assertNotNull(profile);

        Coordinates jackson = Coordinates.of("com.fasterxml.jackson.core", "jackson-databind", "2.15.0");
        boolean added = editor.dependencies().forProfile(profile).addAligned(jackson);
        assertTrue(added);

        String xml = editor.toXml();
        int profileStart = xml.indexOf("<id>my-profile</id>");
        int jacksonPos = xml.indexOf("<artifactId>jackson-databind</artifactId>");
        assertTrue(jacksonPos > profileStart);
    }

    @Test
    void testDeleteManagedDependencyWithinProfile() {
        PomEditor editor = editorOf(POM_WITH_PROFILE_MANAGED);
        Coordinates junit = Coordinates.of("org.junit.jupiter", "junit-jupiter", "5.10.0");

        boolean deleted = editor.dependencies().forProfile("test-profile").deleteManagedDependency(junit);
        assertTrue(deleted);

        String xml = editor.toXml();
        // Managed dependency version should be gone (was in dependencyManagement)
        assertFalse(xml.contains("${junit.version}"));
        // Regular dependency should still exist (version-less)
        assertTrue(xml.contains("<artifactId>junit-jupiter</artifactId>"));
    }

    @Test
    void testUpdateDependencyWithinProfile() {
        PomEditor editor = editorOf(POM_WITH_PROFILE);
        Coordinates slf4j = Coordinates.of("org.slf4j", "slf4j-api", "2.1.0");

        boolean updated = editor.dependencies().forProfile("my-profile").updateDependency(false, slf4j);
        assertTrue(updated);

        String xml = editor.toXml();
        assertTrue(xml.contains("<version>2.1.0</version>"));
        assertFalse(xml.contains("<version>2.0.9</version>"));
    }

    @Test
    void testDeleteDependencyVersionWithinProfile() {
        PomEditor editor = editorOf(POM_WITH_PROFILE);
        Coordinates slf4j = Coordinates.of("org.slf4j", "slf4j-api", "2.0.9");

        boolean deleted = editor.dependencies().forProfile("my-profile").deleteDependencyVersion(slf4j);
        assertTrue(deleted);

        String xml = editor.toXml();
        // Profile dep should still exist but without version
        int profileStart = xml.indexOf("<id>my-profile</id>");
        String afterProfile = xml.substring(profileStart);
        assertTrue(afterProfile.contains("<artifactId>slf4j-api</artifactId>"));
        assertFalse(afterProfile.contains("<version>2.0.9</version>"));
    }

    @Test
    void testAlignDependencyWithinProfile() {
        PomEditor editor = editorOf(POM_WITH_PROFILE);
        Coordinates slf4j = Coordinates.of("org.slf4j", "slf4j-api", null);

        boolean changed = editor.dependencies()
                .forProfile("my-profile")
                .alignDependency(
                        slf4j,
                        AlignOptions.builder()
                                .versionSource(AlignOptions.VersionSource.PROPERTY)
                                .build());
        assertTrue(changed);

        String xml = editor.toXml();
        int profileStart = xml.indexOf("<id>my-profile</id>");
        String afterProfile = xml.substring(profileStart);
        // Property should be created in profile
        assertTrue(afterProfile.contains("<slf4j-api.version>2.0.9</slf4j-api.version>"));
        assertTrue(afterProfile.contains("${slf4j-api.version}"));
        // Project root should not have the property
        String beforeProfile = xml.substring(0, profileStart);
        assertFalse(beforeProfile.contains("slf4j-api.version"));
    }

    @Test
    void testAlignAllDependenciesWithinProfile() {
        PomEditor editor = editorOf(POM_WITH_PROFILE);

        int count = editor.dependencies()
                .forProfile("my-profile")
                .alignAllDependencies(AlignOptions.builder()
                        .versionStyle(AlignOptions.VersionStyle.MANAGED)
                        .versionSource(AlignOptions.VersionSource.PROPERTY)
                        .build());
        assertEquals(1, count);

        String xml = editor.toXml();
        int profileStart = xml.indexOf("<id>my-profile</id>");
        String afterProfile = xml.substring(profileStart);
        // Profile should now have dependencyManagement
        assertTrue(afterProfile.contains("<dependencyManagement>"));
        // Project root should not have dependencyManagement
        String beforeProfile = xml.substring(0, profileStart);
        assertFalse(beforeProfile.contains("<dependencyManagement>"));
    }

    @Test
    void testAddAlignedManagedPropertyWithinProfile() {
        PomEditor editor = editorOf(POM_WITH_PROFILE_MANAGED);
        Coordinates mockito = Coordinates.of("org.mockito", "mockito-core", "5.5.0");

        boolean added = editor.dependencies().forProfile("test-profile").addAligned(mockito);
        assertTrue(added);

        String xml = editor.toXml();
        int profileStart = xml.indexOf("<id>test-profile</id>");
        String afterProfile = xml.substring(profileStart);
        // Should follow managed+property convention detected from the profile
        assertTrue(afterProfile.contains("<mockito-core.version>5.5.0</mockito-core.version>"));
        assertTrue(afterProfile.contains("${mockito-core.version}"));
        // Dependency should be version-less in the profile's dependencies
        assertTrue(afterProfile.contains("<artifactId>mockito-core</artifactId>"));
    }

    private static final String POM_WITH_PROFILE_AND_EXCLUSION = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.example</groupId>
              <artifactId>test-project</artifactId>
              <version>1.0.0</version>
              <profiles>
                <profile>
                  <id>extras</id>
                  <dependencies>
                    <dependency>
                      <groupId>org.example</groupId>
                      <artifactId>my-lib</artifactId>
                      <version>1.0.0</version>
                    </dependency>
                  </dependencies>
                </profile>
              </profiles>
            </project>
            """;

    @Test
    void testExclusionOperationsWithinProfile() {
        PomEditor editor = editorOf(POM_WITH_PROFILE_AND_EXCLUSION);
        Coordinates dep = Coordinates.of("org.example", "my-lib", null);
        Coordinates excl = Coordinates.of("commons-logging", "commons-logging", null);

        PomEditor.Dependencies profileDeps = editor.dependencies().forProfile("extras");

        // Add exclusion
        Element exclEl = profileDeps.addExclusion(dep, excl);
        assertNotNull(exclEl);

        // Verify it exists
        assertTrue(profileDeps.hasExclusion(dep, excl));
        // Top-level should not be affected
        assertFalse(editor.dependencies().hasExclusion(dep, excl));

        // Remove exclusion
        assertTrue(profileDeps.deleteExclusion(dep, excl));
        assertFalse(profileDeps.hasExclusion(dep, excl));
    }

    private static final String POM_WITH_EMPTY_PROFILE = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.example</groupId>
              <artifactId>test-project</artifactId>
              <version>1.0.0</version>
              <profiles>
                <profile>
                  <id>empty</id>
                </profile>
              </profiles>
            </project>
            """;

    @Test
    void testAddAlignedToEmptyProfile() {
        PomEditor editor = editorOf(POM_WITH_EMPTY_PROFILE);
        Coordinates guava = Coordinates.of("com.google.guava", "guava", "32.1.2-jre");

        boolean added = editor.dependencies().forProfile("empty").addAligned(guava);
        assertTrue(added);

        String xml = editor.toXml();
        int profileStart = xml.indexOf("<id>empty</id>");
        String afterProfile = xml.substring(profileStart);
        assertTrue(afterProfile.contains("<artifactId>guava</artifactId>"));
        assertTrue(afterProfile.contains("<version>32.1.2-jre</version>"));
    }

    // ========== updateDependency tests ==========

    @Test
    void testUpdateDependencyUpsertCreatesNew() throws DomTripException {
        String pom = """
                <project>
                    <dependencies>
                        <dependency>
                            <groupId>org.junit</groupId>
                            <artifactId>junit</artifactId>
                            <version>4.13</version>
                        </dependency>
                    </dependencies>
                </project>""";
        PomEditor editor = editorOf(pom);
        Coordinates newDep = Coordinates.of("com.example", "new-lib", "1.0.0");
        boolean result = editor.dependencies().updateDependency(true, newDep);
        assertTrue(result);
        String xml = editor.toXml();
        assertTrue(xml.contains("<groupId>com.example</groupId>"));
        assertTrue(xml.contains("<artifactId>new-lib</artifactId>"));
    }

    @Test
    void testUpdateDependencyNoUpsertReturnsFalse() throws DomTripException {
        String pom = """
                <project>
                    <dependencies>
                        <dependency>
                            <groupId>org.junit</groupId>
                            <artifactId>junit</artifactId>
                            <version>4.13</version>
                        </dependency>
                    </dependencies>
                </project>""";
        PomEditor editor = editorOf(pom);
        Coordinates missing = Coordinates.of("com.example", "missing", "1.0.0");
        boolean result = editor.dependencies().updateDependency(false, missing);
        assertFalse(result);
        String xml = editor.toXml();
        assertFalse(xml.contains("com.example"), "No new dependency should be added");
        assertFalse(xml.contains("missing"), "No new dependency should be added");
    }

    @Test
    void testUpdateDependencyNoDependenciesNoUpsert() throws DomTripException {
        String pom = "<project></project>";
        PomEditor editor = editorOf(pom);
        Coordinates dep = Coordinates.of("com.example", "lib", "1.0.0");
        boolean result = editor.dependencies().updateDependency(false, dep);
        assertFalse(result);
        String xml = editor.toXml();
        assertFalse(xml.contains("<dependencies>"), "No dependencies element should be created");
    }

    @Test
    void testUpdateDependencyWithTypeAndClassifier() throws DomTripException {
        String pom = """
                <project>
                    <dependencies>
                    </dependencies>
                </project>""";
        PomEditor editor = editorOf(pom);
        Coordinates dep = Coordinates.of("com.example", "lib", "1.0.0", "sources", "war");
        boolean result = editor.dependencies().updateDependency(true, dep);
        assertTrue(result);
        String xml = editor.toXml();
        assertTrue(xml.contains("<type>war</type>"));
        assertTrue(xml.contains("<classifier>sources</classifier>"));
    }

    @Test
    void testUpdateManagedDependencyNoUpsert() throws DomTripException {
        String pom = "<project></project>";
        PomEditor editor = editorOf(pom);
        Coordinates dep = Coordinates.of("com.example", "lib", "1.0.0");
        boolean result = editor.dependencies().updateManagedDependency(false, dep);
        assertFalse(result);
        String xml = editor.toXml();
        assertFalse(xml.contains("<dependencyManagement>"), "No dependencyManagement should be created");
    }

    @Test
    void testUpdateManagedDependencyAlignedCreatesProperty() {
        PomEditor editor = editorOf(POM_MANAGED_PROPERTY);
        Coordinates jackson = Coordinates.of("com.fasterxml.jackson.core", "jackson-databind", "2.15.0");

        boolean added = editor.dependencies().updateManagedDependencyAligned(true, jackson);
        assertTrue(added);

        String xml = editor.toXml();
        assertTrue(xml.contains("<jackson-databind.version>2.15.0</jackson-databind.version>"));
        assertTrue(xml.contains("${jackson-databind.version}"));
    }

    @Test
    void testUpdateManagedDependencyAlignedLiteralConvention() {
        PomEditor editor = editorOf(POM_INLINE_LITERAL);
        Coordinates jackson = Coordinates.of("com.fasterxml.jackson.core", "jackson-databind", "2.15.0");

        boolean added = editor.dependencies().updateManagedDependencyAligned(true, jackson);
        assertTrue(added);

        String xml = editor.toXml();
        assertTrue(xml.contains("<version>2.15.0</version>"));
        assertFalse(xml.contains("jackson-databind.version"));
    }

    @Test
    void testUpdateManagedDependencyAlignedUpdatesExistingProperty() {
        PomEditor editor = editorOf(POM_MANAGED_PROPERTY);
        Coordinates guava = Coordinates.of("com.google.guava", "guava", "33.0.0-jre");

        boolean updated = editor.dependencies().updateManagedDependencyAligned(false, guava);
        assertTrue(updated);

        String xml = editor.toXml();
        assertTrue(xml.contains("<guava.version>33.0.0-jre</guava.version>"));
        assertTrue(xml.contains("${guava.version}"));
    }

    @Test
    void testUpdateManagedDependencyAlignedWithExplicitOptions() {
        PomEditor editor = editorOf(POM_INLINE_LITERAL);
        Coordinates jackson = Coordinates.of("com.fasterxml.jackson.core", "jackson-databind", "2.15.0");

        boolean added = editor.dependencies()
                .updateManagedDependencyAligned(
                        true,
                        jackson,
                        AlignOptions.builder()
                                .versionSource(AlignOptions.VersionSource.PROPERTY)
                                .namingConvention(AlignOptions.PropertyNamingConvention.DOT_SUFFIX)
                                .build());
        assertTrue(added);

        String xml = editor.toXml();
        assertTrue(xml.contains("<jackson-databind.version>2.15.0</jackson-databind.version>"));
        assertTrue(xml.contains("${jackson-databind.version}"));
    }

    @Test
    void testUpdateManagedDependencyAlignedNoUpsert() {
        String pom = "<project></project>";
        PomEditor editor = editorOf(pom);
        Coordinates dep = Coordinates.of("com.example", "lib", "1.0.0");

        boolean result = editor.dependencies().updateManagedDependencyAligned(false, dep);
        assertFalse(result);

        String xml = editor.toXml();
        assertFalse(xml.contains("<dependencyManagement>"));
    }

    @Test
    void testUpdateManagedDependencyAlignedNoUpsertPropertyConvention() {
        PomEditor editor = editorOf(POM_MANAGED_PROPERTY);
        Coordinates dep = Coordinates.of("com.example", "nonexistent-lib", "1.0.0");

        boolean result = editor.dependencies().updateManagedDependencyAligned(false, dep);
        assertFalse(result);

        String xml = editor.toXml();
        assertFalse(xml.contains("nonexistent-lib"), "No dependency element should be created");
        assertFalse(xml.contains("nonexistent-lib.version"), "No property should be created");
    }

    @Test
    void testUpdateManagedDependencyAlignedWithoutVersionThrows() {
        PomEditor editor = editorOf(POM_INLINE_LITERAL);
        Coordinates noVersion = Coordinates.of("com.example", "lib", null);
        PomEditor.Dependencies deps = editor.dependencies();

        assertThrows(DomTripException.class, () -> deps.updateManagedDependencyAligned(true, noVersion));
    }

    @Test
    void testUpdateManagedDependencyAlignedWithPropertyNameGenerator() {
        PomEditor editor = editorOf(POM_INLINE_LITERAL);
        Coordinates jackson = Coordinates.of("com.fasterxml.jackson.core", "jackson-databind", "2.15.0");

        boolean added = editor.dependencies()
                .updateManagedDependencyAligned(
                        true,
                        jackson,
                        AlignOptions.builder()
                                .versionSource(AlignOptions.VersionSource.PROPERTY)
                                .propertyNameGenerator(coords -> coords.groupId() + ".version")
                                .build());
        assertTrue(added);

        String xml = editor.toXml();
        assertTrue(xml.contains("<com.fasterxml.jackson.core.version>2.15.0</com.fasterxml.jackson.core.version>"));
        assertTrue(xml.contains("${com.fasterxml.jackson.core.version}"));
    }
}
