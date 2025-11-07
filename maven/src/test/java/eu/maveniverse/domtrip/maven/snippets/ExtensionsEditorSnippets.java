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

import eu.maveniverse.domtrip.Element;
import eu.maveniverse.domtrip.maven.ExtensionsEditor;
import org.junit.jupiter.api.Test;

/**
 * Code snippets for ExtensionsEditor documentation.
 */
public class ExtensionsEditorSnippets {

    @Test
    void testSnippets() {
        // All snippets should compile and run
        basicExtensionsCreation();
        addingExtensions();
    }

    void basicExtensionsCreation() {
        // START: basic-extensions-creation
        // Create a new extensions document
        ExtensionsEditor editor = new ExtensionsEditor();
        editor.createExtensionsDocument();
        Element root = editor.root();

        // Add extensions with convenience methods
        editor.addExtension(root, "org.apache.maven.wagon", "wagon-ssh", "3.5.1");
        editor.addExtension(root, "io.takari.maven", "takari-smart-builder", "0.6.1");

        String result = editor.toXml();
        // END: basic-extensions-creation

        assertNotNull(result);
        assertTrue(result.contains("<extensions"));
    }

    void addingExtensions() {
        // START: adding-extensions
        ExtensionsEditor editor = new ExtensionsEditor();
        editor.createExtensionsDocument();
        Element root = editor.root();

        // Add various types of extensions
        editor.addExtension(root, "org.apache.maven.wagon", "wagon-ssh", "3.5.1");
        editor.addExtension(root, "io.takari.maven", "takari-smart-builder", "0.6.1");
        editor.addExtension(root, "org.eclipse.tycho", "tycho-maven-plugin", "3.0.4", null, "maven-plugin");

        String result = editor.toXml();
        // END: adding-extensions

        assertNotNull(result);
        assertTrue(result.contains("<extension>"));
    }
}
