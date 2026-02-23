/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.maven;

import static eu.maveniverse.domtrip.maven.MavenExtensionsElements.Elements.*;
import static org.junit.jupiter.api.Assertions.*;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Element;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ExtensionsEditor}.
 */
class ExtensionsEditorTest {

    @Test
    void testCreateExtensionsDocument() throws DomTripException {
        ExtensionsEditor editor = new ExtensionsEditor();
        editor.createExtensionsDocument();

        Element root = editor.root();
        assertEquals(EXTENSIONS, root.name());
        assertTrue(root.hasAttribute("xmlns"));
        assertTrue(root.hasAttribute("xmlns:xsi"));
        assertTrue(root.hasAttribute("xsi:schemaLocation"));
    }

    @Test
    void testInsertExtensionsElement() throws DomTripException {
        ExtensionsEditor editor = new ExtensionsEditor();
        editor.createExtensionsDocument();
        Element root = editor.root();

        Element extension = editor.insertExtensionsElement(root, EXTENSION);
        assertEquals(EXTENSION, extension.name());
    }

    @Test
    void testAddExtensionBasic() throws DomTripException {
        ExtensionsEditor editor = new ExtensionsEditor();
        editor.createExtensionsDocument();
        Element root = editor.root();

        Element extension = editor.addExtension(root, "org.apache.maven.wagon", "wagon-ssh", "3.5.1");

        assertEquals(EXTENSION, extension.name());
        assertEquals(
                "org.apache.maven.wagon",
                extension.childElement(GROUP_ID).orElseThrow().textContent());
        assertEquals(
                "wagon-ssh", extension.childElement(ARTIFACT_ID).orElseThrow().textContent());
        assertEquals("3.5.1", extension.childElement(VERSION).orElseThrow().textContent());
        assertTrue(extension.childElement(CLASSIFIER).isEmpty());
        assertTrue(extension.childElement(TYPE).isEmpty());
    }

    @Test
    void testAddExtensionWithClassifierAndType() throws DomTripException {
        ExtensionsEditor editor = new ExtensionsEditor();
        editor.createExtensionsDocument();
        Element root = editor.root();

        Element extension =
                editor.addExtension(root, "io.takari.maven", "takari-smart-builder", "0.6.1", "tests", "test-jar");

        assertEquals(EXTENSION, extension.name());
        assertEquals(
                "io.takari.maven",
                extension.childElement(GROUP_ID).orElseThrow().textContent());
        assertEquals(
                "takari-smart-builder",
                extension.childElement(ARTIFACT_ID).orElseThrow().textContent());
        assertEquals("0.6.1", extension.childElement(VERSION).orElseThrow().textContent());
        assertEquals("tests", extension.childElement(CLASSIFIER).orElseThrow().textContent());
        assertEquals("test-jar", extension.childElement(TYPE).orElseThrow().textContent());
    }

    @Test
    void testAddExtensionWithNullClassifierAndType() throws DomTripException {
        ExtensionsEditor editor = new ExtensionsEditor();
        editor.createExtensionsDocument();
        Element root = editor.root();

        Element extension = editor.addExtension(root, "org.example", "example-extension", "1.0.0", null, null);

        assertEquals(EXTENSION, extension.name());
        assertEquals(
                "org.example", extension.childElement(GROUP_ID).orElseThrow().textContent());
        assertEquals(
                "example-extension",
                extension.childElement(ARTIFACT_ID).orElseThrow().textContent());
        assertEquals("1.0.0", extension.childElement(VERSION).orElseThrow().textContent());
        assertTrue(extension.childElement(CLASSIFIER).isEmpty());
        assertTrue(extension.childElement(TYPE).isEmpty());
    }

    @Test
    void testElementOrdering() throws DomTripException {
        ExtensionsEditor editor = new ExtensionsEditor();
        editor.createExtensionsDocument();
        Element root = editor.root();

        Element extension = editor.addExtension(root, "org.example", "test", "1.0.0");

        // Add elements in reverse order to test ordering
        editor.insertExtensionsElement(extension, TYPE, "jar");
        editor.insertExtensionsElement(extension, CLASSIFIER, "sources");

        // Verify they are ordered correctly: groupId, artifactId, version, classifier, type
        var children = extension.childElements().toList();
        assertEquals(GROUP_ID, children.get(0).name());
        assertEquals(ARTIFACT_ID, children.get(1).name());
        assertEquals(VERSION, children.get(2).name());
        assertEquals(CLASSIFIER, children.get(3).name());
        assertEquals(TYPE, children.get(4).name());
    }

    @Test
    void testFindChildElement() throws DomTripException {
        ExtensionsEditor editor = new ExtensionsEditor();
        editor.createExtensionsDocument();
        Element root = editor.root();

        Element extension = editor.addExtension(root, "org.example", "test", "1.0.0");

        Element found = editor.findChildElement(extension, GROUP_ID);
        assertNotNull(found);
        assertEquals(GROUP_ID, found.name());
        assertEquals("org.example", found.textContent());

        Element notFound = editor.findChildElement(extension, CONFIGURATION);
        assertNull(notFound);
    }

    @Test
    void testMultipleExtensions() throws DomTripException {
        ExtensionsEditor editor = new ExtensionsEditor();
        editor.createExtensionsDocument();
        Element root = editor.root();

        // Add multiple extensions
        editor.addExtension(root, "org.apache.maven.wagon", "wagon-ssh", "3.5.1");
        editor.addExtension(root, "io.takari.maven", "takari-smart-builder", "0.6.1");
        editor.addExtension(root, "org.eclipse.tycho", "tycho-maven-plugin", "3.0.4");

        var extensions = root.childElements(EXTENSION).toList();
        assertEquals(3, extensions.size());

        // Verify first extension
        Element first = extensions.get(0);
        assertEquals(
                "org.apache.maven.wagon",
                first.childElement(GROUP_ID).orElseThrow().textContent());
        assertEquals("wagon-ssh", first.childElement(ARTIFACT_ID).orElseThrow().textContent());

        // Verify second extension
        Element second = extensions.get(1);
        assertEquals(
                "io.takari.maven", second.childElement(GROUP_ID).orElseThrow().textContent());
        assertEquals(
                "takari-smart-builder",
                second.childElement(ARTIFACT_ID).orElseThrow().textContent());

        // Verify third extension
        Element third = extensions.get(2);
        assertEquals(
                "org.eclipse.tycho", third.childElement(GROUP_ID).orElseThrow().textContent());
        assertEquals(
                "tycho-maven-plugin",
                third.childElement(ARTIFACT_ID).orElseThrow().textContent());
    }

    @Test
    void testCompleteExtensionsExample() throws DomTripException {
        ExtensionsEditor editor = new ExtensionsEditor();
        editor.createExtensionsDocument();
        Element root = editor.root();

        // Add various extensions
        editor.addExtension(root, "org.apache.maven.wagon", "wagon-ssh", "3.5.1");
        editor.addExtension(root, "io.takari.maven", "takari-smart-builder", "0.6.1");
        editor.addExtension(root, "org.eclipse.tycho", "tycho-maven-plugin", "3.0.4", null, "maven-plugin");

        String xml = editor.toXml();
        assertNotNull(xml);
        assertTrue(xml.contains("<extensions"));
        assertTrue(xml.contains("<extension>"));
        assertTrue(xml.contains("<groupId>org.apache.maven.wagon</groupId>"));
        assertTrue(xml.contains("<artifactId>wagon-ssh</artifactId>"));
        assertTrue(xml.contains("<version>3.5.1</version>"));
        assertTrue(xml.contains("<type>maven-plugin</type>"));
    }

    @Test
    void testEditExistingExtensions() throws DomTripException {
        String existingExtensions = """
                <?xml version="1.0" encoding="UTF-8"?>
                <extensions xmlns="http://maven.apache.org/EXTENSIONS/1.2.0">
                  <extension>
                    <groupId>org.existing</groupId>
                    <artifactId>existing-extension</artifactId>
                    <version>1.0.0</version>
                  </extension>
                </extensions>
                """;

        Document doc = Document.of(existingExtensions);
        ExtensionsEditor editor = new ExtensionsEditor(doc);
        Element root = editor.root();

        // Add new extension
        editor.addExtension(root, "org.apache.maven.wagon", "wagon-ssh", "3.5.1");

        String result = editor.toXml();
        assertTrue(result.contains("org.existing"));
        assertTrue(result.contains("existing-extension"));
        assertTrue(result.contains("org.apache.maven.wagon"));
        assertTrue(result.contains("wagon-ssh"));
    }

    @Test
    void testEmptyExtensionsDocument() throws DomTripException {
        ExtensionsEditor editor = new ExtensionsEditor();
        editor.createExtensionsDocument();

        String xml = editor.toXml();
        assertNotNull(xml);
        assertTrue(xml.contains("<extensions"));
        assertTrue(xml.contains("xmlns=\"http://maven.apache.org/EXTENSIONS/1.2.0\""));
        assertTrue(xml.contains("</extensions>"));
    }
}
