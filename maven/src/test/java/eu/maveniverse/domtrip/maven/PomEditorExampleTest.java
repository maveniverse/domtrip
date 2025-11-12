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
 * Example test demonstrating PomEditor functionality.
 */
class PomEditorExampleTest {

    @Test
    void testCompleteExampleFromScratch() throws Exception {
        // Create a new POM from scratch
        PomEditor editor = new PomEditor();
        editor.createMavenDocument("project");
        Element root = editor.root();

        // Add basic project information
        editor.insertMavenElement(root, MODEL_VERSION, "4.0.0");
        editor.insertMavenElement(root, GROUP_ID, "com.example");
        editor.insertMavenElement(root, ARTIFACT_ID, "my-project");
        editor.insertMavenElement(root, VERSION, "1.0.0");
        editor.insertMavenElement(root, PACKAGING, "jar");

        // Add project metadata
        editor.insertMavenElement(root, NAME, "My Example Project");
        editor.insertMavenElement(root, DESCRIPTION, "An example project demonstrating PomEditor");
        editor.insertMavenElement(root, URL, "https://github.com/example/my-project");

        // Add properties
        Element properties = editor.insertMavenElement(root, PROPERTIES);
        editor.properties().addProperty(properties, "maven.compiler.source", "17");
        editor.properties().addProperty(properties, "maven.compiler.target", "17");
        editor.properties().addProperty(properties, "project.build.sourceEncoding", "UTF-8");

        // Add dependencies
        Element dependencies = editor.insertMavenElement(root, DEPENDENCIES);
        editor.dependencies().addDependency(dependencies, "org.junit.jupiter", "junit-jupiter", "5.9.2");

        Element junitDep = editor.findChildElement(dependencies, DEPENDENCY);
        editor.insertMavenElement(junitDep, SCOPE, "test");

        // Add build section with plugins
        Element build = editor.insertMavenElement(root, BUILD);
        Element plugins = editor.insertMavenElement(build, PLUGINS);

        Element compilerPlugin =
                editor.plugins().addPlugin(plugins, "org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");
        Element compilerConfig = editor.insertMavenElement(compilerPlugin, CONFIGURATION);
        editor.addElement(compilerConfig, "source", "17");
        editor.addElement(compilerConfig, "target", "17");

        Element surefirePlugin =
                editor.plugins().addPlugin(plugins, "org.apache.maven.plugins", "maven-surefire-plugin", "3.0.0");

        // Generate the XML
        String result = editor.toXml();

        // Verify the structure
        assertTrue(result.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(result.contains("<project xmlns=\"http://maven.apache.org/POM/4.0.0\">"));
        assertTrue(result.contains("<modelVersion>4.0.0</modelVersion>"));
        assertTrue(result.contains("<groupId>com.example</groupId>"));
        assertTrue(result.contains("<artifactId>my-project</artifactId>"));
        assertTrue(result.contains("<version>1.0.0</version>"));
        assertTrue(result.contains("<packaging>jar</packaging>"));
        assertTrue(result.contains("<name>My Example Project</name>"));
        assertTrue(result.contains("<description>An example project demonstrating PomEditor</description>"));
        assertTrue(result.contains("<url>https://github.com/example/my-project</url>"));

        // Verify properties
        assertTrue(result.contains("<properties>"));
        assertTrue(result.contains("<maven.compiler.source>17</maven.compiler.source>"));
        assertTrue(result.contains("<maven.compiler.target>17</maven.compiler.target>"));
        assertTrue(result.contains("<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>"));

        // Verify dependencies
        assertTrue(result.contains("<dependencies>"));
        assertTrue(result.contains("<groupId>org.junit.jupiter</groupId>"));
        assertTrue(result.contains("<artifactId>junit-jupiter</artifactId>"));
        assertTrue(result.contains("<version>5.9.2</version>"));
        assertTrue(result.contains("<scope>test</scope>"));

        // Verify build section
        assertTrue(result.contains("<build>"));
        assertTrue(result.contains("<plugins>"));
        assertTrue(result.contains("maven-compiler-plugin"));
        assertTrue(result.contains("maven-surefire-plugin"));

        // Verify element ordering - modelVersion should come first
        int modelVersionIndex = result.indexOf("<modelVersion>");
        int groupIdIndex = result.indexOf("<groupId>");
        int nameIndex = result.indexOf("<name>");
        int propertiesIndex = result.indexOf("<properties>");
        int dependenciesIndex = result.indexOf("<dependencies>");
        int buildIndex = result.indexOf("<build>");

        assertTrue(modelVersionIndex < groupIdIndex);
        assertTrue(groupIdIndex < nameIndex);
        assertTrue(nameIndex < propertiesIndex);
        assertTrue(propertiesIndex < dependenciesIndex);
        assertTrue(dependenciesIndex < buildIndex);

        System.out.println("Generated POM:");
        System.out.println(result);
    }

    @Test
    void testModifyingExistingPom() throws Exception {
        String existingPom =
                """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.example</groupId>
              <artifactId>existing-project</artifactId>
              <version>1.0.0</version>

              <dependencies>
                <dependency>
                  <groupId>junit</groupId>
                  <artifactId>junit</artifactId>
                  <version>4.13.2</version>
                  <scope>test</scope>
                </dependency>
              </dependencies>
            </project>
            """;

        Document doc = Document.of(existingPom);
        PomEditor editor = new PomEditor(doc);
        Element root = editor.root();

        // Add missing elements with proper ordering
        editor.insertMavenElement(root, NAME, "Existing Project");
        editor.insertMavenElement(root, DESCRIPTION, "A project that already exists");

        // Add properties
        Element properties = editor.insertMavenElement(root, PROPERTIES);
        editor.properties().addProperty(properties, "maven.compiler.source", "11");
        editor.properties().addProperty(properties, "maven.compiler.target", "11");

        // Add another dependency
        Element dependencies = editor.findChildElement(root, DEPENDENCIES);
        editor.dependencies().addDependency(dependencies, "org.mockito", "mockito-core", "4.6.1");

        Element mockitoDep =
                dependencies.children(DEPENDENCY).skip(1).findFirst().orElseThrow(); // Second dependency
        editor.insertMavenElement(mockitoDep, SCOPE, "test");

        String result = editor.toXml();

        // Verify the modifications
        assertTrue(result.contains("<name>Existing Project</name>"));
        assertTrue(result.contains("<description>A project that already exists</description>"));
        assertTrue(result.contains("<properties>"));
        assertTrue(result.contains("<maven.compiler.source>11</maven.compiler.source>"));
        assertTrue(result.contains("mockito-core"));

        // Verify ordering - name and description should be inserted in correct positions
        int nameIndex = result.indexOf("<name>");
        int descIndex = result.indexOf("<description>");
        int propertiesIndex = result.indexOf("<properties>");
        int dependenciesIndex = result.indexOf("<dependencies>");

        assertTrue(nameIndex < descIndex);
        assertTrue(descIndex < propertiesIndex);
        assertTrue(propertiesIndex < dependenciesIndex);

        System.out.println("Modified POM:");
        System.out.println(result);
    }
}
