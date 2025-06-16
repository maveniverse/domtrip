/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package org.maveniverse.domtrip.maven;

import static org.junit.jupiter.api.Assertions.*;
import static org.maveniverse.domtrip.maven.MavenToolchainsElements.Elements.*;
import static org.maveniverse.domtrip.maven.MavenToolchainsElements.ToolchainTypes.*;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.Element;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ToolchainsEditor}.
 */
class ToolchainsEditorTest {

    @Test
    void testCreateToolchainsDocument() {
        ToolchainsEditor editor = new ToolchainsEditor();
        editor.createToolchainsDocument();

        Element root = editor.root();
        assertEquals(TOOLCHAINS, root.name());
        assertTrue(root.hasAttribute("xmlns"));
        assertTrue(root.hasAttribute("xmlns:xsi"));
        assertTrue(root.hasAttribute("xsi:schemaLocation"));
    }

    @Test
    void testInsertToolchainsElement() {
        ToolchainsEditor editor = new ToolchainsEditor();
        editor.createToolchainsDocument();
        Element root = editor.root();

        Element toolchain = editor.insertToolchainsElement(root, TOOLCHAIN);
        assertEquals(TOOLCHAIN, toolchain.name());
    }

    @Test
    void testAddJdkToolchain() {
        ToolchainsEditor editor = new ToolchainsEditor();
        editor.createToolchainsDocument();
        Element root = editor.root();

        Element toolchain = editor.addJdkToolchain(root, "17", "openjdk", "/path/to/jdk/17");

        assertEquals(TOOLCHAIN, toolchain.name());
        assertEquals(JDK, toolchain.child(TYPE).get().textContent());

        Element provides = toolchain.child(PROVIDES).get();
        assertEquals("17", provides.child(VERSION).get().textContent());
        assertEquals("openjdk", provides.child(VENDOR).get().textContent());

        Element configuration = toolchain.child(CONFIGURATION).get();
        assertEquals("/path/to/jdk/17", configuration.child(JDK_HOME).get().textContent());
    }

    @Test
    void testAddGenericToolchain() {
        ToolchainsEditor editor = new ToolchainsEditor();
        editor.createToolchainsDocument();
        Element root = editor.root();

        Element toolchain = editor.addToolchain(root, PROTOBUF);

        assertEquals(TOOLCHAIN, toolchain.name());
        assertEquals(PROTOBUF, toolchain.child(TYPE).get().textContent());
        assertTrue(toolchain.child(PROVIDES).isEmpty());
        assertTrue(toolchain.child(CONFIGURATION).isEmpty());
    }

    @Test
    void testAddNetBeansToolchain() {
        ToolchainsEditor editor = new ToolchainsEditor();
        editor.createToolchainsDocument();
        Element root = editor.root();

        Element toolchain = editor.addNetBeansToolchain(root, "12.0", "/path/to/netbeans");

        assertEquals(TOOLCHAIN, toolchain.name());
        assertEquals(NETBEANS, toolchain.child(TYPE).get().textContent());

        Element provides = toolchain.child(PROVIDES).get();
        assertEquals("12.0", provides.child(VERSION).get().textContent());

        Element configuration = toolchain.child(CONFIGURATION).get();
        assertEquals("/path/to/netbeans", configuration.child(INSTALL_DIR).get().textContent());
    }

    @Test
    void testAddProvides() {
        ToolchainsEditor editor = new ToolchainsEditor();
        editor.createToolchainsDocument();
        Element root = editor.root();

        Element toolchain = editor.addToolchain(root, JDK);
        Element provides = editor.addProvides(toolchain, VERSION, "11");
        editor.addProvides(toolchain, VENDOR, "adoptium");

        assertEquals(PROVIDES, provides.name());
        assertEquals("11", provides.child(VERSION).get().textContent());
        assertEquals("adoptium", provides.child(VENDOR).get().textContent());
    }

    @Test
    void testAddConfiguration() {
        ToolchainsEditor editor = new ToolchainsEditor();
        editor.createToolchainsDocument();
        Element root = editor.root();

        Element toolchain = editor.addToolchain(root, JDK);
        Element configuration = editor.addConfiguration(toolchain, JDK_HOME, "/path/to/jdk");
        editor.addConfiguration(toolchain, "customProperty", "customValue");

        assertEquals(CONFIGURATION, configuration.name());
        assertEquals("/path/to/jdk", configuration.child(JDK_HOME).get().textContent());
        assertEquals("customValue", configuration.child("customProperty").get().textContent());
    }

    @Test
    void testElementOrdering() {
        ToolchainsEditor editor = new ToolchainsEditor();
        editor.createToolchainsDocument();
        Element root = editor.root();

        Element toolchain = editor.addToolchain(root, JDK);

        // Add elements in reverse order to test ordering
        editor.insertToolchainsElement(toolchain, CONFIGURATION);
        editor.insertToolchainsElement(toolchain, PROVIDES);

        // Verify they are ordered correctly: type, provides, configuration
        var children = toolchain.children().toList();
        assertEquals(TYPE, children.get(0).name());
        assertEquals(PROVIDES, children.get(1).name());
        assertEquals(CONFIGURATION, children.get(2).name());
    }

    @Test
    void testFindChildElement() {
        ToolchainsEditor editor = new ToolchainsEditor();
        editor.createToolchainsDocument();
        Element root = editor.root();

        Element toolchain = editor.addJdkToolchain(root, "17", "openjdk", "/path/to/jdk/17");

        Element found = editor.findChildElement(toolchain, TYPE);
        assertNotNull(found);
        assertEquals(TYPE, found.name());
        assertEquals(JDK, found.textContent());

        Element provides = editor.findChildElement(toolchain, PROVIDES);
        assertNotNull(provides);
        assertEquals(PROVIDES, provides.name());

        Element configuration = editor.findChildElement(toolchain, CONFIGURATION);
        assertNotNull(configuration);
        assertEquals(CONFIGURATION, configuration.name());
    }

    @Test
    void testMultipleToolchains() {
        ToolchainsEditor editor = new ToolchainsEditor();
        editor.createToolchainsDocument();
        Element root = editor.root();

        // Add multiple toolchains
        editor.addJdkToolchain(root, "17", "openjdk", "/path/to/jdk/17");
        editor.addJdkToolchain(root, "11", "adoptium", "/path/to/jdk/11");
        editor.addNetBeansToolchain(root, "12.0", "/path/to/netbeans");

        var toolchains = root.children(TOOLCHAIN).toList();
        assertEquals(3, toolchains.size());

        // Verify first toolchain (JDK 17)
        Element first = toolchains.get(0);
        assertEquals(JDK, first.child(TYPE).get().textContent());
        assertEquals("17", first.child(PROVIDES).get().child(VERSION).get().textContent());

        // Verify second toolchain (JDK 11)
        Element second = toolchains.get(1);
        assertEquals(JDK, second.child(TYPE).get().textContent());
        assertEquals("11", second.child(PROVIDES).get().child(VERSION).get().textContent());

        // Verify third toolchain (NetBeans)
        Element third = toolchains.get(2);
        assertEquals(NETBEANS, third.child(TYPE).get().textContent());
        assertEquals("12.0", third.child(PROVIDES).get().child(VERSION).get().textContent());
    }

    @Test
    void testCompleteToolchainsExample() {
        ToolchainsEditor editor = new ToolchainsEditor();
        editor.createToolchainsDocument();
        Element root = editor.root();

        // Add various toolchains
        editor.addJdkToolchain(root, "17", "openjdk", "/path/to/jdk/17");
        editor.addJdkToolchain(root, "11", "adoptium", "/path/to/jdk/11");
        editor.addJdkToolchain(root, "1.8", "oracle", "/path/to/jdk/8");
        editor.addNetBeansToolchain(root, "12.0", "/path/to/netbeans");

        String xml = editor.toXml();
        assertNotNull(xml);
        assertTrue(xml.contains("<toolchains"));
        assertTrue(xml.contains("<toolchain>"));
        assertTrue(xml.contains("<type>jdk</type>"));
        assertTrue(xml.contains("<type>netbeans</type>"));
        assertTrue(xml.contains("<provides>"));
        assertTrue(xml.contains("<configuration>"));
        assertTrue(xml.contains("<version>17</version>"));
        assertTrue(xml.contains("<vendor>openjdk</vendor>"));
        assertTrue(xml.contains("<jdkHome>/path/to/jdk/17</jdkHome>"));
    }

    @Test
    void testEditExistingToolchains() {
        String existingToolchains =
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <toolchains xmlns="http://maven.apache.org/TOOLCHAINS/1.1.0">
                  <toolchain>
                    <type>jdk</type>
                    <provides>
                      <version>8</version>
                      <vendor>oracle</vendor>
                    </provides>
                    <configuration>
                      <jdkHome>/existing/jdk/8</jdkHome>
                    </configuration>
                  </toolchain>
                </toolchains>
                """;

        Document doc = Document.of(existingToolchains);
        ToolchainsEditor editor = new ToolchainsEditor(doc);
        Element root = editor.root();

        // Add new toolchain
        editor.addJdkToolchain(root, "17", "openjdk", "/path/to/jdk/17");

        String result = editor.toXml();
        assertTrue(result.contains("/existing/jdk/8"));
        assertTrue(result.contains("oracle"));
        assertTrue(result.contains("/path/to/jdk/17"));
        assertTrue(result.contains("openjdk"));
    }

    @Test
    void testEmptyToolchainsDocument() {
        ToolchainsEditor editor = new ToolchainsEditor();
        editor.createToolchainsDocument();

        String xml = editor.toXml();
        assertNotNull(xml);
        assertTrue(xml.contains("<toolchains"));
        assertTrue(xml.contains("xmlns=\"http://maven.apache.org/TOOLCHAINS/1.1.0\""));
        assertTrue(xml.contains("</toolchains>"));
    }
}
