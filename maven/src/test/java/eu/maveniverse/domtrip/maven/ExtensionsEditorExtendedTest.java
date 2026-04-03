/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.maven;

import static eu.maveniverse.domtrip.maven.MavenExtensionsElements.Elements.*;
import static org.junit.jupiter.api.Assertions.*;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripConfig;
import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Element;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExtensionsEditorExtendedTest {

    private static final String EXISTING_EXTENSIONS = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<extensions xmlns=\"http://maven.apache.org/EXTENSIONS/1.2.0\">\n"
            + "  <extension>\n"
            + "    <groupId>org.apache.maven.wagon</groupId>\n"
            + "    <artifactId>wagon-ssh</artifactId>\n"
            + "    <version>3.5.1</version>\n"
            + "  </extension>\n"
            + "  <extension>\n"
            + "    <groupId>io.takari.maven</groupId>\n"
            + "    <artifactId>takari-smart-builder</artifactId>\n"
            + "    <version>0.6.1</version>\n"
            + "  </extension>\n"
            + "</extensions>\n";

    // ===== Constructor Tests =====

    @Test
    void testConstructorWithConfig() throws DomTripException {
        ExtensionsEditor editor = new ExtensionsEditor(DomTripConfig.defaults());
        editor.createExtensionsDocument();
        assertNotNull(editor.root());
    }

    @Test
    void testConstructorWithDocumentAndConfig() throws DomTripException {
        Document doc = Document.of(EXISTING_EXTENSIONS);
        ExtensionsEditor editor = new ExtensionsEditor(doc, DomTripConfig.defaults());
        assertNotNull(editor.root());
    }

    // ===== listExtensions =====

    @Test
    void testListExtensions() throws DomTripException {
        Document doc = Document.of(EXISTING_EXTENSIONS);
        ExtensionsEditor editor = new ExtensionsEditor(doc);

        List<Coordinates> extensions = editor.listExtensions();
        assertEquals(2, extensions.size());

        assertEquals("org.apache.maven.wagon", extensions.get(0).groupId());
        assertEquals("wagon-ssh", extensions.get(0).artifactId());
        assertEquals("3.5.1", extensions.get(0).version());
        assertEquals("jar", extensions.get(0).type());

        assertEquals("io.takari.maven", extensions.get(1).groupId());
        assertEquals("takari-smart-builder", extensions.get(1).artifactId());
        assertEquals("0.6.1", extensions.get(1).version());
    }

    @Test
    void testListExtensionsEmpty() throws DomTripException {
        ExtensionsEditor editor = new ExtensionsEditor();
        editor.createExtensionsDocument();

        List<Coordinates> extensions = editor.listExtensions();
        assertTrue(extensions.isEmpty());
    }

    // ===== updateExtension =====

    @Test
    void testUpdateExtensionExisting() throws DomTripException {
        Document doc = Document.of(EXISTING_EXTENSIONS);
        ExtensionsEditor editor = new ExtensionsEditor(doc);

        Coordinates updated = Coordinates.of("org.apache.maven.wagon", "wagon-ssh", "4.0.0");
        boolean result = editor.updateExtension(false, updated);

        assertTrue(result);
        Element root = editor.root();
        Element extension = root.childElements(EXTENSION)
                .filter(updated.predicateGA())
                .findFirst()
                .orElseThrow();
        assertEquals("4.0.0", extension.childElement(VERSION).orElseThrow().textContent());
    }

    @Test
    void testUpdateExtensionNonExistingNoUpsert() throws DomTripException {
        Document doc = Document.of(EXISTING_EXTENSIONS);
        ExtensionsEditor editor = new ExtensionsEditor(doc);

        Coordinates newExt = Coordinates.of("org.example", "new-extension", "1.0.0");
        boolean result = editor.updateExtension(false, newExt);

        assertFalse(result);
        assertEquals(2, editor.listExtensions().size());
    }

    @Test
    void testUpdateExtensionNonExistingWithUpsert() throws DomTripException {
        Document doc = Document.of(EXISTING_EXTENSIONS);
        ExtensionsEditor editor = new ExtensionsEditor(doc);

        Coordinates newExt = Coordinates.of("org.example", "new-extension", "1.0.0");
        boolean result = editor.updateExtension(true, newExt);

        assertTrue(result);
        assertEquals(3, editor.listExtensions().size());
    }

    // ===== deleteExtension =====

    @Test
    void testDeleteExtensionExisting() throws DomTripException {
        Document doc = Document.of(EXISTING_EXTENSIONS);
        ExtensionsEditor editor = new ExtensionsEditor(doc);

        Coordinates toDelete = Coordinates.of("org.apache.maven.wagon", "wagon-ssh", "3.5.1");
        boolean result = editor.deleteExtension(toDelete);

        assertTrue(result);
        assertEquals(1, editor.listExtensions().size());
    }

    @Test
    void testDeleteExtensionNonExisting() throws DomTripException {
        Document doc = Document.of(EXISTING_EXTENSIONS);
        ExtensionsEditor editor = new ExtensionsEditor(doc);

        Coordinates toDelete = Coordinates.of("org.nonexistent", "nonexistent", "1.0.0");
        boolean result = editor.deleteExtension(toDelete);

        assertFalse(result);
        assertEquals(2, editor.listExtensions().size());
    }

    @Test
    void testDeleteAllExtensions() throws DomTripException {
        Document doc = Document.of(EXISTING_EXTENSIONS);
        ExtensionsEditor editor = new ExtensionsEditor(doc);

        editor.deleteExtension(Coordinates.of("org.apache.maven.wagon", "wagon-ssh", "3.5.1"));
        editor.deleteExtension(Coordinates.of("io.takari.maven", "takari-smart-builder", "0.6.1"));

        assertTrue(editor.listExtensions().isEmpty());
    }

    // ===== insertExtensionsElement =====

    @Test
    void testInsertExtensionsElementWithTextContent() throws DomTripException {
        ExtensionsEditor editor = new ExtensionsEditor();
        editor.createExtensionsDocument();
        Element root = editor.root();

        Element extension = editor.addExtension(root, "org.example", "test", "1.0.0");
        editor.insertExtensionsElement(extension, CONFIGURATION);

        Element config = editor.findChildElement(extension, CONFIGURATION);
        assertNotNull(config);
    }

    // ===== getOrderListForParent =====

    @Test
    void testOrderListForUnknownParent() throws DomTripException {
        ExtensionsEditor editor = new ExtensionsEditor();
        editor.createExtensionsDocument();
        Element root = editor.root();

        // Adding element to root (which is "extensions", not "extension") should still work
        Element extension = editor.addExtension(root, "org.example", "test", "1.0.0");
        assertNotNull(extension);
    }

    // ===== Full roundtrip =====

    @Test
    void testFullRoundtrip() throws DomTripException {
        // Create document
        ExtensionsEditor editor = new ExtensionsEditor();
        editor.createExtensionsDocument();
        editor.addExtension(editor.root(), "org.example", "test-ext", "1.0.0");

        // Serialize
        String xml = editor.toXml();

        // Re-parse
        Document doc = Document.of(xml);
        ExtensionsEditor editor2 = new ExtensionsEditor(doc);

        // Verify
        List<Coordinates> extensions = editor2.listExtensions();
        assertEquals(1, extensions.size());
        assertEquals("org.example", extensions.get(0).groupId());
        assertEquals("test-ext", extensions.get(0).artifactId());
        assertEquals("1.0.0", extensions.get(0).version());

        // Update
        editor2.updateExtension(false, Coordinates.of("org.example", "test-ext", "2.0.0"));
        extensions = editor2.listExtensions();
        assertEquals("2.0.0", extensions.get(0).version());

        // Delete
        editor2.deleteExtension(Coordinates.of("org.example", "test-ext", "2.0.0"));
        assertTrue(editor2.listExtensions().isEmpty());
    }
}
