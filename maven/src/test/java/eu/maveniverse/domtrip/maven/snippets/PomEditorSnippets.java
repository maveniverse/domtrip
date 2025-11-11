/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.maven.snippets;

import static eu.maveniverse.domtrip.maven.MavenPomElements.Elements.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Element;
import eu.maveniverse.domtrip.maven.PomEditor;
import org.junit.jupiter.api.Test;

/**
 * Code snippets for PomEditor documentation.
 */
public class PomEditorSnippets {

    @Test
    void testSnippets() throws DomTripException {
        // All snippets should compile and run
        basicPomCreation();
        addingDependencies();
        addingPlugins();
        multiModuleProject();
        editingExistingPom();
    }

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
        editor.addDependency(dependencies, "org.junit.jupiter", "junit-jupiter", "5.9.2");

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
                editor.addPlugin(plugins, "org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");
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
        parentEditor.addModule(modules, "core");
        parentEditor.addModule(modules, "web");
        parentEditor.addModule(modules, "cli");

        String parentPom = parentEditor.toXml();
        // END: multi-module-project

        assertNotNull(parentPom);
        assertTrue(parentPom.contains("<modules>"));
    }

    void editingExistingPom() throws DomTripException {
        // START: editing-existing-pom
        String existingPom =
                """
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
}
