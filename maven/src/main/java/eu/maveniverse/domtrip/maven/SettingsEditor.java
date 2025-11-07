/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.maven;

import static eu.maveniverse.domtrip.maven.MavenSettingsElements.Elements.*;
import static eu.maveniverse.domtrip.maven.MavenSettingsElements.Namespaces.*;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripConfig;
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Specialized editor for Maven settings.xml files.
 *
 * <p>The SettingsEditor extends the base {@link Editor} class with Maven settings-specific functionality,
 * including automatic element ordering according to Maven conventions, intelligent blank line insertion,
 * and convenience methods for common settings operations.</p>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li><strong>Settings-aware element ordering</strong> - Automatically orders elements according to Maven settings conventions</li>
 *   <li><strong>Formatting preservation</strong> - Maintains original formatting, whitespace, and comments</li>
 *   <li><strong>Intelligent blank lines</strong> - Adds appropriate blank lines between element groups</li>
 *   <li><strong>Convenience methods</strong> - Easy-to-use methods for servers, mirrors, proxies, and profiles</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Create a new settings document
 * SettingsEditor editor = new SettingsEditor();
 * editor.createSettingsDocument();
 * Element root = editor.root();
 *
 * // Add elements with automatic ordering
 * editor.insertSettingsElement(root, LOCAL_REPOSITORY, "/custom/repo");
 * editor.insertSettingsElement(root, OFFLINE, "false");
 *
 * // Add servers with convenience methods
 * Element servers = editor.insertSettingsElement(root, SERVERS);
 * editor.addServer(servers, "my-server", "username", "password");
 *
 * String result = editor.toXml();
 * }</pre>
 *
 * @since 0.1
 */
public class SettingsEditor extends AbstractMavenEditor {

    /**
     * Element ordering for Maven settings.xml files.
     * This defines the conventional order of elements within the settings root.
     */
    private static final List<String> SETTINGS_ELEMENT_ORDER = Arrays.asList(
            // Core settings
            LOCAL_REPOSITORY,
            INTERACTIVE_MODE,
            USE_PLUGIN_REGISTRY,
            OFFLINE,

            // Infrastructure
            PROXIES,
            SERVERS,
            MIRRORS,

            // Profiles and activation
            PROFILES,
            ACTIVE_PROFILES,
            PLUGIN_GROUPS);

    /**
     * Element ordering for profile elements.
     */
    private static final List<String> PROFILE_ELEMENT_ORDER =
            Arrays.asList(ID, ACTIVATION, PROPERTIES, REPOSITORIES, PLUGIN_REPOSITORIES);

    /**
     * Element ordering for server elements.
     */
    private static final List<String> SERVER_ELEMENT_ORDER = Arrays.asList(
            ID, USERNAME, PASSWORD, PRIVATE_KEY, PASSPHRASE, FILE_PERMISSIONS, DIRECTORY_PERMISSIONS, CONFIGURATION);

    /**
     * Element ordering for mirror elements.
     */
    private static final List<String> MIRROR_ELEMENT_ORDER =
            Arrays.asList(ID, NAME, URL, MIRROR_OF, LAYOUT, MIRROR_OF_LAYOUTS, BLOCKED);

    /**
     * Element ordering for proxy elements.
     */
    private static final List<String> PROXY_ELEMENT_ORDER =
            Arrays.asList(ID, ACTIVE, PROTOCOL, HOST, PORT, USERNAME, PASSWORD, NON_PROXY_HOSTS);

    /**
     * Creates a new SettingsEditor with default configuration.
     */
    public SettingsEditor() {
        super();
    }

    /**
     * Creates a new SettingsEditor with the specified configuration.
     *
     * @param config the configuration to use
     */
    public SettingsEditor(DomTripConfig config) {
        super(config);
    }

    /**
     * Creates a new SettingsEditor for the specified document.
     *
     * @param document the document to edit
     */
    public SettingsEditor(Document document) {
        super(document);
    }

    /**
     * Creates a new SettingsEditor for the specified document with the given configuration.
     *
     * @param document the document to edit
     * @param config the configuration to use
     */
    public SettingsEditor(Document document, DomTripConfig config) {
        super(document, config);
    }

    /**
     * Creates a new Maven settings document with proper namespace.
     *
     * <p>This method creates a new document with the settings root element and sets up
     * the appropriate Maven settings namespace.</p>
     */
    public void createSettingsDocument() {
        createDocument(SETTINGS);
        Element root = root();
        root.attribute("xmlns", SETTINGS_1_2_0_NAMESPACE);
        root.attribute("xmlns:xsi", MavenSettingsElements.Attributes.XSI_NAMESPACE_URI);
        root.attribute("xsi:schemaLocation", MavenSettingsElements.SchemaLocations.SETTINGS_1_2_0_SCHEMA_LOCATION);
    }

    /**
     * Inserts an element with settings-aware ordering and formatting.
     *
     * <p>This method automatically determines the correct position for the element
     * based on Maven settings conventions and adds appropriate blank lines.</p>
     *
     * @param parent the parent element
     * @param elementName the name of the element to insert
     * @return the newly created element
     */
    public Element insertSettingsElement(Element parent, String elementName) {
        return insertSettingsElement(parent, elementName, null);
    }

    /**
     * Inserts an element with settings-aware ordering, formatting, and text content.
     *
     * @param parent the parent element
     * @param elementName the name of the element to insert
     * @param textContent the text content for the element (can be null)
     * @return the newly created element
     */
    public Element insertSettingsElement(Element parent, String elementName, String textContent) {
        return insertElementAtCorrectPosition(parent, elementName, textContent);
    }

    /**
     * Adds a server configuration.
     *
     * @param serversElement the servers parent element
     * @param id the server ID
     * @param username the username (can be null)
     * @param password the password (can be null)
     * @return the newly created server element
     */
    public Element addServer(Element serversElement, String id, String username, String password) {
        Element server = addElement(serversElement, SERVER);
        insertElementAtCorrectPosition(server, ID, id);
        if (username != null) {
            insertElementAtCorrectPosition(server, USERNAME, username);
        }
        if (password != null) {
            insertElementAtCorrectPosition(server, PASSWORD, password);
        }
        return server;
    }

    /**
     * Adds a mirror configuration.
     *
     * @param mirrorsElement the mirrors parent element
     * @param id the mirror ID
     * @param name the mirror name
     * @param url the mirror URL
     * @param mirrorOf the repositories this mirror serves
     * @return the newly created mirror element
     */
    public Element addMirror(Element mirrorsElement, String id, String name, String url, String mirrorOf) {
        Element mirror = addElement(mirrorsElement, MIRROR);
        insertElementAtCorrectPosition(mirror, ID, id);
        insertElementAtCorrectPosition(mirror, NAME, name);
        insertElementAtCorrectPosition(mirror, URL, url);
        insertElementAtCorrectPosition(mirror, MIRROR_OF, mirrorOf);
        return mirror;
    }

    /**
     * Adds a proxy configuration.
     *
     * @param proxiesElement the proxies parent element
     * @param id the proxy ID
     * @param protocol the proxy protocol
     * @param host the proxy host
     * @param port the proxy port
     * @return the newly created proxy element
     */
    public Element addProxy(Element proxiesElement, String id, String protocol, String host, int port) {
        Element proxy = addElement(proxiesElement, PROXY);
        insertElementAtCorrectPosition(proxy, ID, id);
        insertElementAtCorrectPosition(proxy, PROTOCOL, protocol);
        insertElementAtCorrectPosition(proxy, HOST, host);
        insertElementAtCorrectPosition(proxy, PORT, String.valueOf(port));
        return proxy;
    }

    /**
     * Adds a profile configuration.
     *
     * @param profilesElement the profiles parent element
     * @param id the profile ID
     * @return the newly created profile element
     */
    public Element addProfile(Element profilesElement, String id) {
        Element profile = addElement(profilesElement, PROFILE);
        insertElementAtCorrectPosition(profile, ID, id);
        return profile;
    }

    /**
     * Adds a property to a properties element.
     *
     * @param propertiesElement the properties parent element
     * @param propertyName the property name
     * @param propertyValue the property value
     * @return the newly created property element
     */
    public Element addProperty(Element propertiesElement, String propertyName, String propertyValue) {
        return addElement(propertiesElement, propertyName, propertyValue);
    }

    /**
     * Finds a direct child element by name.
     *
     * @param parent the parent element
     * @param elementName the name of the child element to find
     * @return the child element if found, null otherwise
     */
    public Element findChildElement(Element parent, String elementName) {
        Optional<Element> child = parent.child(elementName);
        return child.orElse(null);
    }

    /**
     * Gets the appropriate element order list for the given parent element.
     */
    @Override
    protected List<String> getOrderListForParent(Element parent) {
        String parentName = parent.name();
        return switch (parentName) {
            case SETTINGS -> SETTINGS_ELEMENT_ORDER;
            case PROFILE -> PROFILE_ELEMENT_ORDER;
            case SERVER -> SERVER_ELEMENT_ORDER;
            case MIRROR -> MIRROR_ELEMENT_ORDER;
            case PROXY -> PROXY_ELEMENT_ORDER;
            default -> null;
        };
    }
}
