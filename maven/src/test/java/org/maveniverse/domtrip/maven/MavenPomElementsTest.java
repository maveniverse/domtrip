/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package org.maveniverse.domtrip.maven;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for the MavenPomElements constants class.
 */
class MavenPomElementsTest {

    @Test
    void testModelVersionConstants() {
        assertEquals("4.0.0", MavenPomElements.ModelVersions.MODEL_VERSION_4_0_0);
        assertEquals("4.1.0", MavenPomElements.ModelVersions.MODEL_VERSION_4_1_0);
    }

    @Test
    void testElementConstants() {
        // Test core POM elements
        assertEquals("modelVersion", MavenPomElements.Elements.MODEL_VERSION);
        assertEquals("groupId", MavenPomElements.Elements.GROUP_ID);
        assertEquals("artifactId", MavenPomElements.Elements.ARTIFACT_ID);
        assertEquals("version", MavenPomElements.Elements.VERSION);
        assertEquals("parent", MavenPomElements.Elements.PARENT);
        assertEquals("packaging", MavenPomElements.Elements.PACKAGING);
        assertEquals("name", MavenPomElements.Elements.NAME);
        assertEquals("description", MavenPomElements.Elements.DESCRIPTION);
        assertEquals("url", MavenPomElements.Elements.URL);

        // Test build elements
        assertEquals("build", MavenPomElements.Elements.BUILD);
        assertEquals("plugins", MavenPomElements.Elements.PLUGINS);
        assertEquals("plugin", MavenPomElements.Elements.PLUGIN);
        assertEquals("pluginManagement", MavenPomElements.Elements.PLUGIN_MANAGEMENT);
        assertEquals("configuration", MavenPomElements.Elements.CONFIGURATION);

        // Test dependency elements
        assertEquals("dependencies", MavenPomElements.Elements.DEPENDENCIES);
        assertEquals("dependency", MavenPomElements.Elements.DEPENDENCY);
        assertEquals("dependencyManagement", MavenPomElements.Elements.DEPENDENCY_MANAGEMENT);
        assertEquals("scope", MavenPomElements.Elements.SCOPE);
        assertEquals("optional", MavenPomElements.Elements.OPTIONAL);

        // Test module elements
        assertEquals("modules", MavenPomElements.Elements.MODULES);
        assertEquals("module", MavenPomElements.Elements.MODULE);

        // Test project information elements
        assertEquals("properties", MavenPomElements.Elements.PROPERTIES);
        assertEquals("licenses", MavenPomElements.Elements.LICENSES);
        assertEquals("developers", MavenPomElements.Elements.DEVELOPERS);
        assertEquals("scm", MavenPomElements.Elements.SCM);
        assertEquals("repositories", MavenPomElements.Elements.REPOSITORIES);
    }

    @Test
    void testAttributeConstants() {
        assertEquals("schemaLocation", MavenPomElements.Attributes.SCHEMA_LOCATION);
        assertEquals("xsi", MavenPomElements.Attributes.XSI_NAMESPACE_PREFIX);
        assertEquals("http://www.w3.org/2001/XMLSchema-instance", MavenPomElements.Attributes.XSI_NAMESPACE_URI);

        assertEquals("combine.children", MavenPomElements.Attributes.COMBINE_CHILDREN);
        assertEquals("combine.self", MavenPomElements.Attributes.COMBINE_SELF);

        assertEquals("override", MavenPomElements.Attributes.COMBINE_OVERRIDE);
        assertEquals("merge", MavenPomElements.Attributes.COMBINE_MERGE);
        assertEquals("append", MavenPomElements.Attributes.COMBINE_APPEND);
    }

    @Test
    void testNamespaceConstants() {
        assertEquals("http://maven.apache.org/POM/4.0.0", MavenPomElements.Namespaces.MAVEN_4_0_0_NAMESPACE);
        assertEquals("http://maven.apache.org/POM/4.1.0", MavenPomElements.Namespaces.MAVEN_4_1_0_NAMESPACE);
    }

    @Test
    void testSchemaLocationConstants() {
        String expected400 = "http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd";
        assertEquals(expected400, MavenPomElements.SchemaLocations.MAVEN_4_0_0_SCHEMA_LOCATION);

        String expected410 = "http://maven.apache.org/POM/4.1.0 https://maven.apache.org/xsd/maven-4.1.0.xsd";
        assertEquals(expected410, MavenPomElements.SchemaLocations.MAVEN_4_1_0_SCHEMA_LOCATION);
    }

    @Test
    void testFileConstants() {
        assertEquals("pom.xml", MavenPomElements.Files.POM_XML);
        assertEquals(".mvn", MavenPomElements.Files.MVN_DIRECTORY);
        assertEquals("../pom.xml", MavenPomElements.Files.DEFAULT_PARENT_RELATIVE_PATH);
    }

    @Test
    void testPluginConstants() {
        assertEquals("org.apache.maven.plugins", MavenPomElements.Plugins.DEFAULT_MAVEN_PLUGIN_GROUP_ID);
        assertEquals("maven-", MavenPomElements.Plugins.MAVEN_PLUGIN_PREFIX);
        assertEquals("Maven 4 compatibility", MavenPomElements.Plugins.MAVEN_4_COMPATIBILITY_REASON);
    }

    @Test
    void testIndentationConstants() {
        assertEquals("  ", MavenPomElements.Indentation.TWO_SPACES);
        assertEquals("    ", MavenPomElements.Indentation.FOUR_SPACES);
        assertEquals("\t", MavenPomElements.Indentation.TAB);
        assertEquals("  ", MavenPomElements.Indentation.DEFAULT);
    }

    @Test
    void testConstantsAreNotNull() {
        // Verify that all major constants are not null
        assertNotNull(MavenPomElements.Elements.MODEL_VERSION);
        assertNotNull(MavenPomElements.Elements.GROUP_ID);
        assertNotNull(MavenPomElements.Elements.ARTIFACT_ID);
        assertNotNull(MavenPomElements.Elements.VERSION);
        assertNotNull(MavenPomElements.Elements.DEPENDENCIES);
        assertNotNull(MavenPomElements.Elements.BUILD);
        assertNotNull(MavenPomElements.Elements.PLUGINS);

        assertNotNull(MavenPomElements.Namespaces.MAVEN_4_0_0_NAMESPACE);
        assertNotNull(MavenPomElements.SchemaLocations.MAVEN_4_0_0_SCHEMA_LOCATION);
        assertNotNull(MavenPomElements.Files.POM_XML);
        assertNotNull(MavenPomElements.Plugins.DEFAULT_MAVEN_PLUGIN_GROUP_ID);
    }

    @Test
    void testConstantsAreNotEmpty() {
        // Verify that all major constants are not empty
        assertFalse(MavenPomElements.Elements.MODEL_VERSION.isEmpty());
        assertFalse(MavenPomElements.Elements.GROUP_ID.isEmpty());
        assertFalse(MavenPomElements.Elements.ARTIFACT_ID.isEmpty());
        assertFalse(MavenPomElements.Elements.VERSION.isEmpty());
        assertFalse(MavenPomElements.Elements.DEPENDENCIES.isEmpty());
        assertFalse(MavenPomElements.Elements.BUILD.isEmpty());
        assertFalse(MavenPomElements.Elements.PLUGINS.isEmpty());

        assertFalse(MavenPomElements.Namespaces.MAVEN_4_0_0_NAMESPACE.isEmpty());
        assertFalse(MavenPomElements.SchemaLocations.MAVEN_4_0_0_SCHEMA_LOCATION.isEmpty());
        assertFalse(MavenPomElements.Files.POM_XML.isEmpty());
        assertFalse(MavenPomElements.Plugins.DEFAULT_MAVEN_PLUGIN_GROUP_ID.isEmpty());
    }
}
