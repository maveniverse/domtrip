/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.maven.snippets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Element;
import eu.maveniverse.domtrip.maven.ToolchainsEditor;
import org.junit.jupiter.api.Test;

/**
 * Code snippets for ToolchainsEditor documentation.
 */
public class ToolchainsEditorSnippets {

    @Test
    void testSnippets() throws DomTripException {
        // All snippets should compile and run
        basicToolchainsCreation();
        addingJdkToolchains();
        addingVariousToolchains();
    }

    void basicToolchainsCreation() throws DomTripException {
        // START: basic-toolchains-creation
        // Create a new toolchains document
        ToolchainsEditor editor = new ToolchainsEditor();
        editor.createToolchainsDocument();
        Element root = editor.root();

        // Add JDK toolchains with convenience methods
        editor.addJdkToolchain(root, "17", "openjdk", "/path/to/jdk/17");
        editor.addJdkToolchain(root, "11", "adoptium", "/path/to/jdk/11");

        String result = editor.toXml();
        // END: basic-toolchains-creation

        assertNotNull(result);
        assertTrue(result.contains("<toolchains"));
    }

    void addingJdkToolchains() throws DomTripException {
        // START: adding-jdk-toolchains
        ToolchainsEditor editor = new ToolchainsEditor();
        editor.createToolchainsDocument();
        Element root = editor.root();

        // Add multiple JDK toolchains
        editor.addJdkToolchain(root, "17", "openjdk", "/path/to/jdk/17");
        editor.addJdkToolchain(root, "11", "adoptium", "/path/to/jdk/11");
        editor.addJdkToolchain(root, "1.8", "oracle", "/path/to/jdk/8");

        String result = editor.toXml();
        // END: adding-jdk-toolchains

        assertNotNull(result);
        assertTrue(result.contains("<toolchain>"));
    }

    void addingVariousToolchains() throws DomTripException {
        // START: adding-various-toolchains
        ToolchainsEditor editor = new ToolchainsEditor();
        editor.createToolchainsDocument();
        Element root = editor.root();

        // Add JDK toolchains
        editor.addJdkToolchain(root, "17", "openjdk", "/path/to/jdk/17");

        // Add NetBeans toolchain
        editor.addNetBeansToolchain(root, "12.0", "/path/to/netbeans");

        // Add custom toolchain
        Element customToolchain = editor.addToolchain(root, "protobuf");
        editor.addProvides(customToolchain, "version", "3.21.0");
        editor.addConfiguration(customToolchain, "protocExecutable", "/usr/local/bin/protoc");

        String result = editor.toXml();
        // END: adding-various-toolchains

        assertNotNull(result);
        assertTrue(result.contains("protobuf"));
    }
}
