/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.maven;

import static eu.maveniverse.domtrip.maven.MavenSettingsElements.Elements.*;
import static org.junit.jupiter.api.Assertions.*;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Element;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SettingsEditor}.
 */
class SettingsEditorTest {

    @Test
    void testCreateSettingsDocument() throws DomTripException {
        SettingsEditor editor = new SettingsEditor();
        editor.createSettingsDocument();

        Element root = editor.root();
        assertEquals(SETTINGS, root.name());
        assertTrue(root.hasAttribute("xmlns"));
        assertTrue(root.hasAttribute("xmlns:xsi"));
        assertTrue(root.hasAttribute("xsi:schemaLocation"));
    }

    @Test
    void testInsertSettingsElement() throws DomTripException {
        SettingsEditor editor = new SettingsEditor();
        editor.createSettingsDocument();
        Element root = editor.root();

        Element localRepo = editor.insertSettingsElement(root, LOCAL_REPOSITORY, "/custom/repo");
        assertEquals(LOCAL_REPOSITORY, localRepo.name());
        assertEquals("/custom/repo", localRepo.textContent());
    }

    @Test
    void testElementOrdering() throws DomTripException {
        SettingsEditor editor = new SettingsEditor();
        editor.createSettingsDocument();
        Element root = editor.root();

        // Add elements in reverse order
        editor.insertSettingsElement(root, OFFLINE, "false");
        editor.insertSettingsElement(root, LOCAL_REPOSITORY, "/custom/repo");

        // Verify they are ordered correctly
        var children = root.children().toList();
        assertEquals(LOCAL_REPOSITORY, children.get(0).name());
        assertEquals(OFFLINE, children.get(1).name());
    }

    @Test
    void testAddServer() throws DomTripException {
        SettingsEditor editor = new SettingsEditor();
        editor.createSettingsDocument();
        Element root = editor.root();

        Element servers = editor.insertSettingsElement(root, SERVERS);
        Element server = editor.addServer(servers, "my-server", "username", "password");

        assertEquals(SERVER, server.name());
        assertEquals("my-server", server.child(ID).orElseThrow().textContent());
        assertEquals("username", server.child(USERNAME).orElseThrow().textContent());
        assertEquals("password", server.child(PASSWORD).orElseThrow().textContent());
    }

    @Test
    void testAddServerWithNullCredentials() throws DomTripException {
        SettingsEditor editor = new SettingsEditor();
        editor.createSettingsDocument();
        Element root = editor.root();

        Element servers = editor.insertSettingsElement(root, SERVERS);
        Element server = editor.addServer(servers, "my-server", null, null);

        assertEquals(SERVER, server.name());
        assertEquals("my-server", server.child(ID).orElseThrow().textContent());
        assertTrue(server.child(USERNAME).isEmpty());
        assertTrue(server.child(PASSWORD).isEmpty());
    }

    @Test
    void testAddMirror() throws DomTripException {
        SettingsEditor editor = new SettingsEditor();
        editor.createSettingsDocument();
        Element root = editor.root();

        Element mirrors = editor.insertSettingsElement(root, MIRRORS);
        Element mirror = editor.addMirror(
                mirrors, "central-mirror", "Central Mirror", "https://repo1.maven.org/maven2", "central");

        assertEquals(MIRROR, mirror.name());
        assertEquals("central-mirror", mirror.child(ID).orElseThrow().textContent());
        assertEquals("Central Mirror", mirror.child(NAME).orElseThrow().textContent());
        assertEquals(
                "https://repo1.maven.org/maven2",
                mirror.child(URL).orElseThrow().textContent());
        assertEquals("central", mirror.child(MIRROR_OF).orElseThrow().textContent());
    }

    @Test
    void testAddProxy() throws DomTripException {
        SettingsEditor editor = new SettingsEditor();
        editor.createSettingsDocument();
        Element root = editor.root();

        Element proxies = editor.insertSettingsElement(root, PROXIES);
        Element proxy = editor.addProxy(proxies, "my-proxy", "http", "proxy.example.com", 8080);

        assertEquals(PROXY, proxy.name());
        assertEquals("my-proxy", proxy.child(ID).orElseThrow().textContent());
        assertEquals("http", proxy.child(PROTOCOL).orElseThrow().textContent());
        assertEquals("proxy.example.com", proxy.child(HOST).orElseThrow().textContent());
        assertEquals("8080", proxy.child(PORT).orElseThrow().textContent());
    }

    @Test
    void testAddProfile() throws DomTripException {
        SettingsEditor editor = new SettingsEditor();
        editor.createSettingsDocument();
        Element root = editor.root();

        Element profiles = editor.insertSettingsElement(root, PROFILES);
        Element profile = editor.addProfile(profiles, "dev-profile");

        assertEquals(PROFILE, profile.name());
        assertEquals("dev-profile", profile.child(ID).orElseThrow().textContent());
    }

    @Test
    void testAddProperty() throws DomTripException {
        SettingsEditor editor = new SettingsEditor();
        editor.createSettingsDocument();
        Element root = editor.root();

        Element profiles = editor.insertSettingsElement(root, PROFILES);
        Element profile = editor.addProfile(profiles, "dev-profile");
        Element properties = editor.insertSettingsElement(profile, PROPERTIES);

        Element property = editor.addProperty(properties, "maven.compiler.source", "17");
        assertEquals("maven.compiler.source", property.name());
        assertEquals("17", property.textContent());
    }

    @Test
    void testFindChildElement() throws DomTripException {
        SettingsEditor editor = new SettingsEditor();
        editor.createSettingsDocument();
        Element root = editor.root();

        editor.insertSettingsElement(root, LOCAL_REPOSITORY, "/custom/repo");

        Element found = editor.findChildElement(root, LOCAL_REPOSITORY);
        assertNotNull(found);
        assertEquals(LOCAL_REPOSITORY, found.name());
        assertEquals("/custom/repo", found.textContent());

        Element notFound = editor.findChildElement(root, SERVERS);
        assertNull(notFound);
    }

    @Test
    void testCompleteSettingsExample() throws DomTripException {
        SettingsEditor editor = new SettingsEditor();
        editor.createSettingsDocument();
        Element root = editor.root();

        // Add core settings
        editor.insertSettingsElement(root, LOCAL_REPOSITORY, "/custom/repository");
        editor.insertSettingsElement(root, OFFLINE, "false");

        // Add servers
        Element servers = editor.insertSettingsElement(root, SERVERS);
        editor.addServer(servers, "nexus", "admin", "password123");

        // Add mirrors
        Element mirrors = editor.insertSettingsElement(root, MIRRORS);
        editor.addMirror(
                mirrors, "nexus-mirror", "Nexus Mirror", "https://nexus.example.com/repository/maven-public/", "*");

        // Add profiles
        Element profiles = editor.insertSettingsElement(root, PROFILES);
        Element profile = editor.addProfile(profiles, "development");
        Element properties = editor.insertSettingsElement(profile, PROPERTIES);
        editor.addProperty(properties, "maven.compiler.source", "17");
        editor.addProperty(properties, "maven.compiler.target", "17");

        String xml = editor.toXml();
        assertNotNull(xml);
        assertTrue(xml.contains("<settings"));
        assertTrue(xml.contains("<localRepository>/custom/repository</localRepository>"));
        assertTrue(xml.contains("<server>"));
        assertTrue(xml.contains("<mirror>"));
        assertTrue(xml.contains("<profile>"));
        assertTrue(xml.contains("<maven.compiler.source>17</maven.compiler.source>"));
    }

    @Test
    void testEditExistingSettings() throws DomTripException {
        String existingSettings =
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <settings xmlns="http://maven.apache.org/SETTINGS/1.2.0">
                  <localRepository>/existing/repo</localRepository>
                </settings>
                """;

        Document doc = Document.of(existingSettings);
        SettingsEditor editor = new SettingsEditor(doc);
        Element root = editor.root();

        // Add new elements
        editor.insertSettingsElement(root, OFFLINE, "true");
        Element servers = editor.insertSettingsElement(root, SERVERS);
        editor.addServer(servers, "new-server", "user", "pass");

        String result = editor.toXml();
        assertTrue(result.contains("/existing/repo"));
        assertTrue(result.contains("<offline>true</offline>"));
        assertTrue(result.contains("<server>"));
    }
}
