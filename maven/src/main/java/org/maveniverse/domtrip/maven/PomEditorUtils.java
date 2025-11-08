/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package org.maveniverse.domtrip.maven;

import static org.maveniverse.domtrip.maven.MavenPomElements.Elements.*;

import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Element;

/**
 * Enhanced utility methods for PomEditor that provide convenient shortcuts for common operations.
 *
 * <p>This class contains static utility methods that complement the PomEditor class with
 * additional convenience methods for common Maven POM manipulation tasks.</p>
 *
 * @since 0.4.0
 */
public final class PomEditorUtils {

    private PomEditorUtils() {
        // Utility class
    }

    /**
     * Simple boolean check for child element existence.
     *
     * <p>This is a convenience method that provides a cleaner alternative to
     * {@code parent.child(childName).isPresent()} for simple existence checks.</p>
     *
     * @param parent the parent element to check
     * @param childName the name of the child element to look for
     * @return true if the child element exists, false otherwise
     */
    public static boolean hasChildElement(Element parent, String childName) {
        return parent.child(childName).isPresent();
    }

    /**
     * Get child element text content with null fallback.
     *
     * <p>This is a convenience method that provides a cleaner alternative to
     * {@code parent.child(childName).map(Element::textContent).orElse(null)}
     * for getting child element text content.</p>
     *
     * @param parent the parent element
     * @param childName the name of the child element
     * @return the text content of the child element, or null if the child doesn't exist
     */
    public static String getChildElementText(Element parent, String childName) {
        return parent.child(childName).map(Element::textContent).orElse(null);
    }

    /**
     * Update existing element or create new one with proper Maven ordering.
     *
     * <p>This method combines the common "find or create" pattern into a single operation.
     * If the child element exists, its content is updated. If it doesn't exist, it's created
     * with proper Maven element ordering.</p>
     *
     * @param editor the PomEditor instance to use
     * @param parent the parent element
     * @param childName the name of the child element
     * @param content the text content to set
     * @return the updated or created element
     * @throws DomTripException if an error occurs during editing
     */
    public static Element updateOrCreateChildElement(PomEditor editor, Element parent, String childName, String content)
            throws DomTripException {
        Element child = editor.findChildElement(parent, childName);
        if (child != null) {
            child.textContent(content);
            return child;
        } else {
            return editor.insertMavenElement(parent, childName, content);
        }
    }

    /**
     * Convenience method to add GAV (groupId, artifactId, version) elements to a parent.
     *
     * <p>This method is commonly used for dependencies and plugins, automatically adding
     * the three core Maven coordinates in the proper order.</p>
     *
     * @param editor the PomEditor instance to use
     * @param parent the parent element (e.g., dependency or plugin)
     * @param groupId the groupId value
     * @param artifactId the artifactId value
     * @param version the version value (can be null to skip)
     * @throws DomTripException if an error occurs during editing
     */
    public static void addGAVElements(
            PomEditor editor, Element parent, String groupId, String artifactId, String version)
            throws DomTripException {
        editor.insertMavenElement(parent, GROUP_ID, groupId);
        editor.insertMavenElement(parent, ARTIFACT_ID, artifactId);
        if (version != null && !version.isEmpty()) {
            editor.insertMavenElement(parent, VERSION, version);
        }
    }

    /**
     * Convenience method to create a dependency element with GAV coordinates.
     *
     * <p>This method creates a new dependency element under the given dependencies parent
     * and populates it with the provided GAV coordinates using proper Maven ordering.</p>
     *
     * @param editor the PomEditor instance to use
     * @param dependenciesElement the dependencies parent element
     * @param groupId the groupId value
     * @param artifactId the artifactId value
     * @param version the version value (can be null)
     * @return the created dependency element
     * @throws DomTripException if an error occurs during editing
     */
    public static Element createDependency(
            PomEditor editor, Element dependenciesElement, String groupId, String artifactId, String version)
            throws DomTripException {
        Element dependency = editor.insertMavenElement(dependenciesElement, DEPENDENCY);
        addGAVElements(editor, dependency, groupId, artifactId, version);
        return dependency;
    }

    /**
     * Convenience method to create a plugin element with GAV coordinates.
     *
     * <p>This method creates a new plugin element under the given plugins parent
     * and populates it with the provided GAV coordinates using proper Maven ordering.</p>
     *
     * @param editor the PomEditor instance to use
     * @param pluginsElement the plugins parent element
     * @param groupId the groupId value
     * @param artifactId the artifactId value
     * @param version the version value (can be null)
     * @return the created plugin element
     * @throws DomTripException if an error occurs during editing
     */
    public static Element createPlugin(
            PomEditor editor, Element pluginsElement, String groupId, String artifactId, String version)
            throws DomTripException {
        Element plugin = editor.insertMavenElement(pluginsElement, PLUGIN);
        addGAVElements(editor, plugin, groupId, artifactId, version);
        return plugin;
    }
}
