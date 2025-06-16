/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package org.maveniverse.domtrip.maven.snippets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.maveniverse.domtrip.maven.MavenSettingsElements.Elements.*;

import eu.maveniverse.domtrip.Element;
import org.junit.jupiter.api.Test;
import org.maveniverse.domtrip.maven.SettingsEditor;

/**
 * Code snippets for SettingsEditor documentation.
 */
public class SettingsEditorSnippets {

    @Test
    void testSnippets() {
        // All snippets should compile and run
        basicSettingsCreation();
        addingServers();
        addingMirrors();
        addingProfiles();
    }

    void basicSettingsCreation() {
        // START: basic-settings-creation
        // Create a new settings document
        SettingsEditor editor = new SettingsEditor();
        editor.createSettingsDocument();
        Element root = editor.root();

        // Add elements with automatic ordering
        editor.insertSettingsElement(root, LOCAL_REPOSITORY, "/custom/repo");
        editor.insertSettingsElement(root, OFFLINE, "false");

        String result = editor.toXml();
        // END: basic-settings-creation

        assertNotNull(result);
        assertTrue(result.contains("<settings"));
    }

    void addingServers() {
        // START: adding-servers
        SettingsEditor editor = new SettingsEditor();
        editor.createSettingsDocument();
        Element root = editor.root();

        // Add servers with convenience methods
        Element servers = editor.insertSettingsElement(root, SERVERS);
        editor.addServer(servers, "my-server", "username", "password");
        editor.addServer(servers, "nexus", "admin", "secret123");

        String result = editor.toXml();
        // END: adding-servers

        assertNotNull(result);
        assertTrue(result.contains("<server>"));
    }

    void addingMirrors() {
        // START: adding-mirrors
        SettingsEditor editor = new SettingsEditor();
        editor.createSettingsDocument();
        Element root = editor.root();

        // Add mirrors
        Element mirrors = editor.insertSettingsElement(root, MIRRORS);
        editor.addMirror(mirrors, "central-mirror", "Central Mirror", "https://repo1.maven.org/maven2", "central");
        editor.addMirror(
                mirrors, "nexus-mirror", "Nexus Mirror", "https://nexus.example.com/repository/maven-public/", "*");

        String result = editor.toXml();
        // END: adding-mirrors

        assertNotNull(result);
        assertTrue(result.contains("<mirror>"));
    }

    void addingProfiles() {
        // START: adding-profiles
        SettingsEditor editor = new SettingsEditor();
        editor.createSettingsDocument();
        Element root = editor.root();

        // Add profiles with properties
        Element profiles = editor.insertSettingsElement(root, PROFILES);
        Element profile = editor.addProfile(profiles, "development");

        Element properties = editor.insertSettingsElement(profile, PROPERTIES);
        editor.addProperty(properties, "maven.compiler.source", "17");
        editor.addProperty(properties, "maven.compiler.target", "17");
        editor.addProperty(properties, "env", "dev");

        String result = editor.toXml();
        // END: adding-profiles

        assertNotNull(result);
        assertTrue(result.contains("<profile>"));
    }
}
