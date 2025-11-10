/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.maven;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link MavenToolchainsElements}.
 */
class MavenToolchainsElementsTest {

    @Test
    void testElements() {
        // Root element
        assertEquals("toolchains", MavenToolchainsElements.Elements.TOOLCHAINS);

        // Toolchain elements
        assertEquals("toolchain", MavenToolchainsElements.Elements.TOOLCHAIN);
        assertEquals("type", MavenToolchainsElements.Elements.TYPE);
        assertEquals("provides", MavenToolchainsElements.Elements.PROVIDES);
        assertEquals("configuration", MavenToolchainsElements.Elements.CONFIGURATION);

        // Common provides elements
        assertEquals("version", MavenToolchainsElements.Elements.VERSION);
        assertEquals("vendor", MavenToolchainsElements.Elements.VENDOR);
        assertEquals("id", MavenToolchainsElements.Elements.ID);

        // JDK toolchain configuration elements
        assertEquals("jdkHome", MavenToolchainsElements.Elements.JDK_HOME);
        assertEquals("javaHome", MavenToolchainsElements.Elements.JAVA_HOME);

        // NetBeans toolchain configuration elements
        assertEquals("installDir", MavenToolchainsElements.Elements.INSTALL_DIR);

        // Common configuration elements
        assertEquals("executable", MavenToolchainsElements.Elements.EXECUTABLE);
        assertEquals("toolHome", MavenToolchainsElements.Elements.TOOL_HOME);
    }

    @Test
    void testAttributes() {
        assertEquals("schemaLocation", MavenToolchainsElements.Attributes.SCHEMA_LOCATION);
        assertEquals("xsi", MavenToolchainsElements.Attributes.XSI_NAMESPACE_PREFIX);
        assertEquals("http://www.w3.org/2001/XMLSchema-instance", MavenToolchainsElements.Attributes.XSI_NAMESPACE_URI);
    }

    @Test
    void testNamespaces() {
        assertEquals(
                "http://maven.apache.org/TOOLCHAINS/1.0.0",
                MavenToolchainsElements.Namespaces.TOOLCHAINS_1_0_0_NAMESPACE);
        assertEquals(
                "http://maven.apache.org/TOOLCHAINS/1.1.0",
                MavenToolchainsElements.Namespaces.TOOLCHAINS_1_1_0_NAMESPACE);
    }

    @Test
    void testSchemaLocations() {
        assertEquals(
                "http://maven.apache.org/TOOLCHAINS/1.0.0 https://maven.apache.org/xsd/toolchains-1.0.0.xsd",
                MavenToolchainsElements.SchemaLocations.TOOLCHAINS_1_0_0_SCHEMA_LOCATION);
        assertEquals(
                "http://maven.apache.org/TOOLCHAINS/1.1.0 https://maven.apache.org/xsd/toolchains-1.1.0.xsd",
                MavenToolchainsElements.SchemaLocations.TOOLCHAINS_1_1_0_SCHEMA_LOCATION);
    }

    @Test
    void testFiles() {
        assertEquals("toolchains.xml", MavenToolchainsElements.Files.TOOLCHAINS_XML);
        assertEquals("toolchains.xml", MavenToolchainsElements.Files.GLOBAL_TOOLCHAINS_XML);
        assertEquals(".m2", MavenToolchainsElements.Files.USER_MAVEN_DIRECTORY);
    }

    @Test
    void testToolchainTypes() {
        assertEquals("jdk", MavenToolchainsElements.ToolchainTypes.JDK);
        assertEquals("netbeans", MavenToolchainsElements.ToolchainTypes.NETBEANS);
        assertEquals("protobuf", MavenToolchainsElements.ToolchainTypes.PROTOBUF);
    }

    @Test
    void testValues() {
        // Common JDK vendors
        assertEquals("oracle", MavenToolchainsElements.Values.VENDOR_ORACLE);
        assertEquals("openjdk", MavenToolchainsElements.Values.VENDOR_OPENJDK);
        assertEquals("adoptium", MavenToolchainsElements.Values.VENDOR_ADOPTIUM);
        assertEquals("azul", MavenToolchainsElements.Values.VENDOR_AZUL);
        assertEquals("amazon", MavenToolchainsElements.Values.VENDOR_AMAZON);
        assertEquals("ibm", MavenToolchainsElements.Values.VENDOR_IBM);
        assertEquals("graalvm", MavenToolchainsElements.Values.VENDOR_GRAALVM);

        // Common JDK versions
        assertEquals("1.8", MavenToolchainsElements.Values.VERSION_8);
        assertEquals("11", MavenToolchainsElements.Values.VERSION_11);
        assertEquals("17", MavenToolchainsElements.Values.VERSION_17);
        assertEquals("21", MavenToolchainsElements.Values.VERSION_21);
    }

    @Test
    void testConstantsAreNotNull() {
        // Verify all constants are properly initialized
        assertNotNull(MavenToolchainsElements.Elements.TOOLCHAINS);
        assertNotNull(MavenToolchainsElements.Elements.TOOLCHAIN);
        assertNotNull(MavenToolchainsElements.Elements.TYPE);
        assertNotNull(MavenToolchainsElements.Elements.PROVIDES);
        assertNotNull(MavenToolchainsElements.Elements.CONFIGURATION);
        assertNotNull(MavenToolchainsElements.Elements.VERSION);
        assertNotNull(MavenToolchainsElements.Elements.VENDOR);
        assertNotNull(MavenToolchainsElements.Elements.ID);
        assertNotNull(MavenToolchainsElements.Elements.JDK_HOME);
        assertNotNull(MavenToolchainsElements.Elements.JAVA_HOME);
        assertNotNull(MavenToolchainsElements.Elements.INSTALL_DIR);
        assertNotNull(MavenToolchainsElements.Elements.EXECUTABLE);
        assertNotNull(MavenToolchainsElements.Elements.TOOL_HOME);

        assertNotNull(MavenToolchainsElements.Attributes.SCHEMA_LOCATION);
        assertNotNull(MavenToolchainsElements.Attributes.XSI_NAMESPACE_PREFIX);
        assertNotNull(MavenToolchainsElements.Attributes.XSI_NAMESPACE_URI);

        assertNotNull(MavenToolchainsElements.Namespaces.TOOLCHAINS_1_0_0_NAMESPACE);
        assertNotNull(MavenToolchainsElements.Namespaces.TOOLCHAINS_1_1_0_NAMESPACE);

        assertNotNull(MavenToolchainsElements.SchemaLocations.TOOLCHAINS_1_0_0_SCHEMA_LOCATION);
        assertNotNull(MavenToolchainsElements.SchemaLocations.TOOLCHAINS_1_1_0_SCHEMA_LOCATION);

        assertNotNull(MavenToolchainsElements.Files.TOOLCHAINS_XML);
        assertNotNull(MavenToolchainsElements.Files.GLOBAL_TOOLCHAINS_XML);
        assertNotNull(MavenToolchainsElements.Files.USER_MAVEN_DIRECTORY);

        assertNotNull(MavenToolchainsElements.ToolchainTypes.JDK);
        assertNotNull(MavenToolchainsElements.ToolchainTypes.NETBEANS);
        assertNotNull(MavenToolchainsElements.ToolchainTypes.PROTOBUF);
    }

    @Test
    void testConstantsAreNotEmpty() {
        // Verify all constants have meaningful values
        assertFalse(MavenToolchainsElements.Elements.TOOLCHAINS.isEmpty());
        assertFalse(MavenToolchainsElements.Elements.TOOLCHAIN.isEmpty());
        assertFalse(MavenToolchainsElements.Elements.TYPE.isEmpty());
        assertFalse(MavenToolchainsElements.Elements.PROVIDES.isEmpty());
        assertFalse(MavenToolchainsElements.Elements.CONFIGURATION.isEmpty());

        assertFalse(MavenToolchainsElements.Namespaces.TOOLCHAINS_1_0_0_NAMESPACE.isEmpty());
        assertFalse(MavenToolchainsElements.Namespaces.TOOLCHAINS_1_1_0_NAMESPACE.isEmpty());

        assertFalse(MavenToolchainsElements.Files.TOOLCHAINS_XML.isEmpty());
        assertFalse(MavenToolchainsElements.Files.GLOBAL_TOOLCHAINS_XML.isEmpty());
        assertFalse(MavenToolchainsElements.Files.USER_MAVEN_DIRECTORY.isEmpty());

        assertFalse(MavenToolchainsElements.ToolchainTypes.JDK.isEmpty());
        assertFalse(MavenToolchainsElements.ToolchainTypes.NETBEANS.isEmpty());
        assertFalse(MavenToolchainsElements.ToolchainTypes.PROTOBUF.isEmpty());
    }
}
