/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package org.maveniverse.domtrip.maven;

import static org.junit.jupiter.api.Assertions.*;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for the enhanced utility methods in PomEditorUtils.
 */
@DisplayName("PomEditorUtils")
class PomEditorUtilsTest {

    private PomEditor editor;
    private Document document;
    private Element root;

    @BeforeEach
    void setUp() throws Exception {
        String pomXml =
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>com.example</groupId>
                    <artifactId>test-project</artifactId>
                    <version>1.0.0</version>
                    <properties>
                        <maven.compiler.source>11</maven.compiler.source>
                    </properties>
                </project>
                """;
        document = Document.of(pomXml);
        editor = new PomEditor(document);
        root = editor.root();
    }

    @Test
    @DisplayName("hasChildElement should return true for existing elements")
    void hasChildElementShouldReturnTrueForExistingElements() {
        assertTrue(PomEditorUtils.hasChildElement(root, "modelVersion"));
        assertTrue(PomEditorUtils.hasChildElement(root, "groupId"));
        assertTrue(PomEditorUtils.hasChildElement(root, "properties"));
    }

    @Test
    @DisplayName("hasChildElement should return false for non-existing elements")
    void hasChildElementShouldReturnFalseForNonExistingElements() {
        assertFalse(PomEditorUtils.hasChildElement(root, "description"));
        assertFalse(PomEditorUtils.hasChildElement(root, "dependencies"));
        assertFalse(PomEditorUtils.hasChildElement(root, "nonexistent"));
    }

    @Test
    @DisplayName("getChildElementText should return text content for existing elements")
    void getChildElementTextShouldReturnTextContentForExistingElements() {
        assertEquals("4.0.0", PomEditorUtils.getChildElementText(root, "modelVersion"));
        assertEquals("com.example", PomEditorUtils.getChildElementText(root, "groupId"));
        assertEquals("test-project", PomEditorUtils.getChildElementText(root, "artifactId"));
        assertEquals("1.0.0", PomEditorUtils.getChildElementText(root, "version"));
    }

    @Test
    @DisplayName("getChildElementText should return null for non-existing elements")
    void getChildElementTextShouldReturnNullForNonExistingElements() {
        assertNull(PomEditorUtils.getChildElementText(root, "description"));
        assertNull(PomEditorUtils.getChildElementText(root, "dependencies"));
        assertNull(PomEditorUtils.getChildElementText(root, "nonexistent"));
    }

    @Test
    @DisplayName("updateOrCreateChildElement should update existing elements")
    void updateOrCreateChildElementShouldUpdateExistingElements() throws Exception {
        // Update existing version
        Element versionElement = PomEditorUtils.updateOrCreateChildElement(editor, root, "version", "2.0.0");

        assertNotNull(versionElement);
        assertEquals("2.0.0", versionElement.textContent());
        assertEquals("2.0.0", PomEditorUtils.getChildElementText(root, "version"));
    }

    @Test
    @DisplayName("updateOrCreateChildElement should create new elements with proper ordering")
    void updateOrCreateChildElementShouldCreateNewElementsWithProperOrdering() throws Exception {
        // Create new description element
        Element descriptionElement =
                PomEditorUtils.updateOrCreateChildElement(editor, root, "description", "Test project description");

        assertNotNull(descriptionElement);
        assertEquals("Test project description", descriptionElement.textContent());
        assertTrue(PomEditorUtils.hasChildElement(root, "description"));

        // Verify it's in the right position (after packaging, before name)
        String xml = editor.toXml();
        assertTrue(xml.contains("<description>Test project description</description>"));
    }

    @Test
    @DisplayName("addGAVElements should add groupId, artifactId, and version in correct order")
    void addGAVElementsShouldAddElementsInCorrectOrder() throws Exception {
        Element dependency = editor.insertMavenElement(root, "dependency");
        PomEditorUtils.addGAVElements(editor, dependency, "org.junit.jupiter", "junit-jupiter", "5.10.0");

        assertEquals("org.junit.jupiter", PomEditorUtils.getChildElementText(dependency, "groupId"));
        assertEquals("junit-jupiter", PomEditorUtils.getChildElementText(dependency, "artifactId"));
        assertEquals("5.10.0", PomEditorUtils.getChildElementText(dependency, "version"));

        // Verify order in XML
        String xml = editor.toXml();
        int groupIdPos = xml.indexOf("<groupId>org.junit.jupiter</groupId>");
        int artifactIdPos = xml.indexOf("<artifactId>junit-jupiter</artifactId>");
        int versionPos = xml.indexOf("<version>5.10.0</version>");

        assertTrue(groupIdPos < artifactIdPos);
        assertTrue(artifactIdPos < versionPos);
    }

    @Test
    @DisplayName("addGAVElements should skip version when null")
    void addGAVElementsShouldSkipVersionWhenNull() throws Exception {
        Element dependency = editor.insertMavenElement(root, "dependency");
        PomEditorUtils.addGAVElements(editor, dependency, "org.junit.jupiter", "junit-jupiter", null);

        assertEquals("org.junit.jupiter", PomEditorUtils.getChildElementText(dependency, "groupId"));
        assertEquals("junit-jupiter", PomEditorUtils.getChildElementText(dependency, "artifactId"));
        assertNull(PomEditorUtils.getChildElementText(dependency, "version"));
    }

    @Test
    @DisplayName("createDependency should create properly structured dependency")
    void createDependencyShouldCreateProperlyStructuredDependency() throws Exception {
        Element dependencies = editor.insertMavenElement(root, "dependencies");
        Element junit =
                PomEditorUtils.createDependency(editor, dependencies, "org.junit.jupiter", "junit-jupiter", "5.10.0");

        assertNotNull(junit);
        assertEquals("dependency", junit.name());
        assertEquals("org.junit.jupiter", PomEditorUtils.getChildElementText(junit, "groupId"));
        assertEquals("junit-jupiter", PomEditorUtils.getChildElementText(junit, "artifactId"));
        assertEquals("5.10.0", PomEditorUtils.getChildElementText(junit, "version"));
    }

    @Test
    @DisplayName("createPlugin should create properly structured plugin")
    void createPluginShouldCreateProperlyStructuredPlugin() throws Exception {
        Element build = editor.insertMavenElement(root, "build");
        Element plugins = editor.insertMavenElement(build, "plugins");
        Element compiler = PomEditorUtils.createPlugin(
                editor, plugins, "org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");

        assertNotNull(compiler);
        assertEquals("plugin", compiler.name());
        assertEquals("org.apache.maven.plugins", PomEditorUtils.getChildElementText(compiler, "groupId"));
        assertEquals("maven-compiler-plugin", PomEditorUtils.getChildElementText(compiler, "artifactId"));
        assertEquals("3.11.0", PomEditorUtils.getChildElementText(compiler, "version"));
    }
}
