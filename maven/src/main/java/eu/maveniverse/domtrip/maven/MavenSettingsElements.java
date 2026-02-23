/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.maven;

/**
 * Constants for Maven settings.xml elements, attributes, and other XML constructs.
 *
 * <p>This class provides a comprehensive set of constants for working with Maven settings files,
 * organized into logical groups for better maintainability and ease of use.</p>
 *
 * <p>The constants are organized into nested classes:</p>
 * <ul>
 *   <li>{@link Elements} - XML element names used in Maven settings</li>
 *   <li>{@link Attributes} - XML attribute names and values</li>
 *   <li>{@link Namespaces} - XML namespace URIs</li>
 *   <li>{@link SchemaLocations} - Schema location strings</li>
 *   <li>{@link Files} - Common file and directory names</li>
 * </ul>
 *
 * @since 0.1
 */
public final class MavenSettingsElements {

    private MavenSettingsElements() {
        // Utility class
    }

    /**
     * Common XML element names used in Maven settings.xml files.
     *
     * <p>These constants represent the standard XML element names found in Maven settings files,
     * organized logically by their purpose and location within the settings structure.</p>
     */
    public static final class Elements {
        // Root element
        public static final String SETTINGS = "settings";

        // Core settings elements
        public static final String LOCAL_REPOSITORY = "localRepository";
        public static final String INTERACTIVE_MODE = "interactiveMode";
        public static final String USE_PLUGIN_REGISTRY = "usePluginRegistry";
        public static final String OFFLINE = "offline";

        // Proxy elements
        public static final String PROXIES = "proxies";
        public static final String PROXY = "proxy";
        public static final String ACTIVE = "active";
        public static final String PROTOCOL = "protocol";
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
        public static final String PORT = "port";
        public static final String HOST = "host";
        public static final String NON_PROXY_HOSTS = "nonProxyHosts";

        // Server elements
        public static final String SERVERS = "servers";
        public static final String SERVER = "server";
        public static final String PRIVATE_KEY = "privateKey";
        public static final String PASSPHRASE = "passphrase";
        public static final String FILE_PERMISSIONS = "filePermissions";
        public static final String DIRECTORY_PERMISSIONS = "directoryPermissions";
        public static final String CONFIGURATION = "configuration";

        // Mirror elements
        public static final String MIRRORS = "mirrors";
        public static final String MIRROR = "mirror";
        public static final String MIRROR_OF = "mirrorOf";
        public static final String NAME = "name";
        public static final String URL = "url";
        public static final String LAYOUT = "layout";
        public static final String MIRROR_OF_LAYOUTS = "mirrorOfLayouts";
        public static final String BLOCKED = "blocked";

        // Repository elements
        public static final String REPOSITORIES = "repositories";
        public static final String REPOSITORY = "repository";
        public static final String PLUGIN_REPOSITORIES = "pluginRepositories";
        public static final String PLUGIN_REPOSITORY = "pluginRepository";
        public static final String RELEASES = "releases";
        public static final String SNAPSHOTS = "snapshots";
        public static final String ENABLED = "enabled";
        public static final String UPDATE_POLICY = "updatePolicy";
        public static final String CHECKSUM_POLICY = "checksumPolicy";

        // Profile elements
        public static final String PROFILES = "profiles";
        public static final String PROFILE = "profile";
        public static final String ACTIVATION = "activation";
        public static final String ACTIVE_BY_DEFAULT = "activeByDefault";
        public static final String JDK = "jdk";
        public static final String OS = "os";
        public static final String PROPERTY = "property";
        public static final String FILE = "file";
        public static final String PACKAGING = "packaging";
        public static final String CONDITION = "condition";
        public static final String PROPERTIES = "properties";

        // Activation elements
        public static final String ARCH = "arch";
        public static final String FAMILY = "family";
        public static final String VERSION = "version";
        public static final String VALUE = "value";
        public static final String EXISTS = "exists";
        public static final String MISSING = "missing";

        // Profile settings
        public static final String ACTIVE_PROFILES = "activeProfiles";
        public static final String ACTIVE_PROFILE = "activeProfile";
        public static final String PLUGIN_GROUPS = "pluginGroups";
        public static final String PLUGIN_GROUP = "pluginGroup";

        // Common elements
        public static final String ID = "id";

        private Elements() {
            // Utility class
        }
    }

    /**
     * XML attribute constants used in Maven settings.
     */
    public static final class Attributes {
        /** Schema location attribute name */
        public static final String SCHEMA_LOCATION = "schemaLocation";
        /** XSI namespace prefix */
        public static final String XSI_NAMESPACE_PREFIX = "xsi";
        /** XSI namespace URI */
        public static final String XSI_NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema-instance";

        private Attributes() {
            // Utility class
        }
    }

    /**
     * Maven settings namespace constants.
     */
    public static final class Namespaces {
        /** Maven settings 1.0.0 namespace URI */
        public static final String SETTINGS_1_0_0_NAMESPACE = "http://maven.apache.org/SETTINGS/1.0.0";
        /** Maven settings 1.1.0 namespace URI */
        public static final String SETTINGS_1_1_0_NAMESPACE = "http://maven.apache.org/SETTINGS/1.1.0";
        /** Maven settings 1.2.0 namespace URI */
        public static final String SETTINGS_1_2_0_NAMESPACE = "http://maven.apache.org/SETTINGS/1.2.0";
        /** Maven settings 2.0.0 namespace URI */
        public static final String SETTINGS_2_0_0_NAMESPACE = "http://maven.apache.org/SETTINGS/2.0.0";

        private Namespaces() {
            // Utility class
        }
    }

    /**
     * Schema location constants.
     */
    public static final class SchemaLocations {
        /** Schema location for 1.0.0 settings */
        public static final String SETTINGS_1_0_0_SCHEMA_LOCATION =
                Namespaces.SETTINGS_1_0_0_NAMESPACE + " https://maven.apache.org/xsd/settings-1.0.0.xsd";
        /** Schema location for 1.1.0 settings */
        public static final String SETTINGS_1_1_0_SCHEMA_LOCATION =
                Namespaces.SETTINGS_1_1_0_NAMESPACE + " https://maven.apache.org/xsd/settings-1.1.0.xsd";
        /** Schema location for 1.2.0 settings */
        public static final String SETTINGS_1_2_0_SCHEMA_LOCATION =
                Namespaces.SETTINGS_1_2_0_NAMESPACE + " https://maven.apache.org/xsd/settings-1.2.0.xsd";
        /** Schema location for 2.0.0 settings */
        public static final String SETTINGS_2_0_0_SCHEMA_LOCATION =
                Namespaces.SETTINGS_2_0_0_NAMESPACE + " https://maven.apache.org/xsd/settings-2.0.0.xsd";

        private SchemaLocations() {
            // Utility class
        }
    }

    /**
     * Common file and directory names.
     */
    public static final class Files {
        /** Standard Maven settings file name */
        public static final String SETTINGS_XML = "settings.xml";
        /** Global Maven settings file name */
        public static final String GLOBAL_SETTINGS_XML = "settings.xml";
        /** User Maven directory */
        public static final String USER_MAVEN_DIRECTORY = ".m2";
        /** Default local repository directory */
        public static final String DEFAULT_LOCAL_REPOSITORY = "${user.home}/.m2/repository";

        private Files() {
            // Utility class
        }
    }

    /**
     * Common values and defaults for settings elements.
     */
    public static final class Values {
        // Update policy values
        public static final String UPDATE_POLICY_ALWAYS = "always";
        public static final String UPDATE_POLICY_DAILY = "daily";
        public static final String UPDATE_POLICY_NEVER = "never";

        // Checksum policy values
        public static final String CHECKSUM_POLICY_FAIL = "fail";
        public static final String CHECKSUM_POLICY_WARN = "warn";
        public static final String CHECKSUM_POLICY_IGNORE = "ignore";

        // Layout values
        public static final String LAYOUT_DEFAULT = "default";
        public static final String LAYOUT_LEGACY = "legacy";

        // Protocol values
        public static final String PROTOCOL_HTTP = "http";
        public static final String PROTOCOL_HTTPS = "https";
        public static final String PROTOCOL_SOCKS4 = "socks4";
        public static final String PROTOCOL_SOCKS5 = "socks5";

        // OS family values
        public static final String OS_FAMILY_WINDOWS = "windows";
        public static final String OS_FAMILY_UNIX = "unix";
        public static final String OS_FAMILY_MAC = "mac";

        // Boolean values
        public static final String TRUE = "true";
        public static final String FALSE = "false";

        private Values() {
            // Utility class
        }
    }
}
