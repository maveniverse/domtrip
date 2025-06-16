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
 * Tests for {@link MavenExtensionsElements}.
 */
class MavenExtensionsElementsTest {

    @Test
    void testElements() {
        // Root element
        assertEquals("extensions", MavenExtensionsElements.Elements.EXTENSIONS);

        // Extension elements
        assertEquals("extension", MavenExtensionsElements.Elements.EXTENSION);
        assertEquals("groupId", MavenExtensionsElements.Elements.GROUP_ID);
        assertEquals("artifactId", MavenExtensionsElements.Elements.ARTIFACT_ID);
        assertEquals("version", MavenExtensionsElements.Elements.VERSION);
        assertEquals("classifier", MavenExtensionsElements.Elements.CLASSIFIER);
        assertEquals("type", MavenExtensionsElements.Elements.TYPE);

        // Configuration elements
        assertEquals("configuration", MavenExtensionsElements.Elements.CONFIGURATION);
    }

    @Test
    void testAttributes() {
        assertEquals("schemaLocation", MavenExtensionsElements.Attributes.SCHEMA_LOCATION);
        assertEquals("xsi", MavenExtensionsElements.Attributes.XSI_NAMESPACE_PREFIX);
        assertEquals("http://www.w3.org/2001/XMLSchema-instance", MavenExtensionsElements.Attributes.XSI_NAMESPACE_URI);
    }

    @Test
    void testNamespaces() {
        assertEquals(
                "http://maven.apache.org/EXTENSIONS/1.0.0",
                MavenExtensionsElements.Namespaces.EXTENSIONS_1_0_0_NAMESPACE);
        assertEquals(
                "http://maven.apache.org/EXTENSIONS/1.1.0",
                MavenExtensionsElements.Namespaces.EXTENSIONS_1_1_0_NAMESPACE);
        assertEquals(
                "http://maven.apache.org/EXTENSIONS/1.2.0",
                MavenExtensionsElements.Namespaces.EXTENSIONS_1_2_0_NAMESPACE);
    }

    @Test
    void testSchemaLocations() {
        assertEquals(
                "http://maven.apache.org/EXTENSIONS/1.0.0 https://maven.apache.org/xsd/core-extensions-1.0.0.xsd",
                MavenExtensionsElements.SchemaLocations.EXTENSIONS_1_0_0_SCHEMA_LOCATION);
        assertEquals(
                "http://maven.apache.org/EXTENSIONS/1.1.0 https://maven.apache.org/xsd/core-extensions-1.1.0.xsd",
                MavenExtensionsElements.SchemaLocations.EXTENSIONS_1_1_0_SCHEMA_LOCATION);
        assertEquals(
                "http://maven.apache.org/EXTENSIONS/1.2.0 https://maven.apache.org/xsd/core-extensions-1.2.0.xsd",
                MavenExtensionsElements.SchemaLocations.EXTENSIONS_1_2_0_SCHEMA_LOCATION);
    }

    @Test
    void testFiles() {
        assertEquals("extensions.xml", MavenExtensionsElements.Files.EXTENSIONS_XML);
        assertEquals(".mvn", MavenExtensionsElements.Files.MAVEN_EXTENSIONS_DIRECTORY);
        assertEquals(".mvn/extensions.xml", MavenExtensionsElements.Files.MAVEN_EXTENSIONS_PATH);
    }

    @Test
    void testValues() {
        assertEquals("jar", MavenExtensionsElements.Values.DEFAULT_TYPE);
        assertEquals("jar", MavenExtensionsElements.Values.TYPE_JAR);
        assertEquals("maven-plugin", MavenExtensionsElements.Values.TYPE_MAVEN_PLUGIN);
    }

    @Test
    void testConstantsAreNotNull() {
        // Verify all constants are properly initialized
        assertNotNull(MavenExtensionsElements.Elements.EXTENSIONS);
        assertNotNull(MavenExtensionsElements.Elements.EXTENSION);
        assertNotNull(MavenExtensionsElements.Elements.GROUP_ID);
        assertNotNull(MavenExtensionsElements.Elements.ARTIFACT_ID);
        assertNotNull(MavenExtensionsElements.Elements.VERSION);
        assertNotNull(MavenExtensionsElements.Elements.CLASSIFIER);
        assertNotNull(MavenExtensionsElements.Elements.TYPE);
        assertNotNull(MavenExtensionsElements.Elements.CONFIGURATION);

        assertNotNull(MavenExtensionsElements.Attributes.SCHEMA_LOCATION);
        assertNotNull(MavenExtensionsElements.Attributes.XSI_NAMESPACE_PREFIX);
        assertNotNull(MavenExtensionsElements.Attributes.XSI_NAMESPACE_URI);

        assertNotNull(MavenExtensionsElements.Namespaces.EXTENSIONS_1_0_0_NAMESPACE);
        assertNotNull(MavenExtensionsElements.Namespaces.EXTENSIONS_1_1_0_NAMESPACE);
        assertNotNull(MavenExtensionsElements.Namespaces.EXTENSIONS_1_2_0_NAMESPACE);

        assertNotNull(MavenExtensionsElements.SchemaLocations.EXTENSIONS_1_0_0_SCHEMA_LOCATION);
        assertNotNull(MavenExtensionsElements.SchemaLocations.EXTENSIONS_1_1_0_SCHEMA_LOCATION);
        assertNotNull(MavenExtensionsElements.SchemaLocations.EXTENSIONS_1_2_0_SCHEMA_LOCATION);

        assertNotNull(MavenExtensionsElements.Files.EXTENSIONS_XML);
        assertNotNull(MavenExtensionsElements.Files.MAVEN_EXTENSIONS_DIRECTORY);
        assertNotNull(MavenExtensionsElements.Files.MAVEN_EXTENSIONS_PATH);

        assertNotNull(MavenExtensionsElements.Values.DEFAULT_TYPE);
        assertNotNull(MavenExtensionsElements.Values.TYPE_JAR);
        assertNotNull(MavenExtensionsElements.Values.TYPE_MAVEN_PLUGIN);
    }

    @Test
    void testConstantsAreNotEmpty() {
        // Verify all constants have meaningful values
        assertFalse(MavenExtensionsElements.Elements.EXTENSIONS.isEmpty());
        assertFalse(MavenExtensionsElements.Elements.EXTENSION.isEmpty());
        assertFalse(MavenExtensionsElements.Elements.GROUP_ID.isEmpty());
        assertFalse(MavenExtensionsElements.Elements.ARTIFACT_ID.isEmpty());
        assertFalse(MavenExtensionsElements.Elements.VERSION.isEmpty());
        assertFalse(MavenExtensionsElements.Elements.CLASSIFIER.isEmpty());
        assertFalse(MavenExtensionsElements.Elements.TYPE.isEmpty());
        assertFalse(MavenExtensionsElements.Elements.CONFIGURATION.isEmpty());

        assertFalse(MavenExtensionsElements.Namespaces.EXTENSIONS_1_0_0_NAMESPACE.isEmpty());
        assertFalse(MavenExtensionsElements.Namespaces.EXTENSIONS_1_1_0_NAMESPACE.isEmpty());
        assertFalse(MavenExtensionsElements.Namespaces.EXTENSIONS_1_2_0_NAMESPACE.isEmpty());

        assertFalse(MavenExtensionsElements.Files.EXTENSIONS_XML.isEmpty());
        assertFalse(MavenExtensionsElements.Files.MAVEN_EXTENSIONS_DIRECTORY.isEmpty());
        assertFalse(MavenExtensionsElements.Files.MAVEN_EXTENSIONS_PATH.isEmpty());
    }
}
