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
import eu.maveniverse.domtrip.Element;
import org.junit.jupiter.api.Test;

/**
 * Tests for the PomEditor class.
 */
class PomEditorTest {

    @Test
    void testCreateMavenDocument() throws Exception {
        PomEditor editor = new PomEditor();
        editor.createMavenDocument("project");

        Element root = editor.root();
        assertNotNull(root);
        assertEquals("project", root.name());
        assertEquals(MavenPomElements.Namespaces.MAVEN_4_0_0_NAMESPACE, root.attribute("xmlns"));
    }

    @Test
    void testInsertMavenElementWithOrdering() throws Exception {
        String pomXml =
                """
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
    void testAddDependency() throws Exception {
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
        Element dependency = editor.addDependency(dependencies, "org.junit.jupiter", "junit-jupiter", "5.9.2");

        assertNotNull(dependency);
        assertEquals("dependency", dependency.name());

        // Verify dependency structure
        assertEquals(
                "org.junit.jupiter", dependency.child(GROUP_ID).orElseThrow().textContent());
        assertEquals(
                "junit-jupiter", dependency.child(ARTIFACT_ID).orElseThrow().textContent());
        assertEquals("5.9.2", dependency.child(VERSION).orElseThrow().textContent());

        String result = editor.toXml();
        assertTrue(result.contains("<groupId>org.junit.jupiter</groupId>"));
        assertTrue(result.contains("<artifactId>junit-jupiter</artifactId>"));
        assertTrue(result.contains("<version>5.9.2</version>"));
    }

    @Test
    void testAddPlugin() throws Exception {
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
        Element plugin = editor.addPlugin(plugins, "org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");

        assertNotNull(plugin);
        assertEquals("plugin", plugin.name());

        // Verify plugin structure
        assertEquals(
                "org.apache.maven.plugins", plugin.child(GROUP_ID).orElseThrow().textContent());
        assertEquals(
                "maven-compiler-plugin", plugin.child(ARTIFACT_ID).orElseThrow().textContent());
        assertEquals("3.11.0", plugin.child(VERSION).orElseThrow().textContent());
    }

    @Test
    void testAddModule() throws Exception {
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
        editor.addModule(modules, "module1");
        editor.addModule(modules, "module2");

        String result = editor.toXml();
        assertTrue(result.contains("<module>module1</module>"));
        assertTrue(result.contains("<module>module2</module>"));
    }

    @Test
    void testFindChildElement() throws Exception {
        String pomXml =
                """
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
    void testElementOrdering() throws Exception {
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
}
