/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package org.maveniverse.domtrip.maven;

/**
 * Constants for Maven extensions.xml elements, attributes, and other XML constructs.
 *
 * <p>This class provides a comprehensive set of constants for working with Maven core extensions files,
 * organized into logical groups for better maintainability and ease of use.</p>
 *
 * <p>The constants are organized into nested classes:</p>
 * <ul>
 *   <li>{@link Elements} - XML element names used in Maven extensions</li>
 *   <li>{@link Attributes} - XML attribute names and values</li>
 *   <li>{@link Namespaces} - XML namespace URIs</li>
 *   <li>{@link SchemaLocations} - Schema location strings</li>
 *   <li>{@link Files} - Common file and directory names</li>
 * </ul>
 *
 * @since 0.1
 */
public final class MavenExtensionsElements {

    private MavenExtensionsElements() {
        // Utility class
    }

    /**
     * Common XML element names used in Maven extensions.xml files.
     *
     * <p>These constants represent the standard XML element names found in Maven core extensions files,
     * organized logically by their purpose and location within the extensions structure.</p>
     */
    public static final class Elements {
        // Root element
        public static final String EXTENSIONS = "extensions";

        // Extension elements
        public static final String EXTENSION = "extension";
        public static final String GROUP_ID = "groupId";
        public static final String ARTIFACT_ID = "artifactId";
        public static final String VERSION = "version";
        public static final String CLASSIFIER = "classifier";
        public static final String TYPE = "type";

        // Configuration elements
        public static final String CONFIGURATION = "configuration";

        private Elements() {
            // Utility class
        }
    }

    /**
     * XML attribute constants used in Maven extensions.
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
     * Maven extensions namespace constants.
     */
    public static final class Namespaces {
        /** Maven core extensions 1.0.0 namespace URI */
        public static final String EXTENSIONS_1_0_0_NAMESPACE = "http://maven.apache.org/EXTENSIONS/1.0.0";
        /** Maven core extensions 1.1.0 namespace URI */
        public static final String EXTENSIONS_1_1_0_NAMESPACE = "http://maven.apache.org/EXTENSIONS/1.1.0";
        /** Maven core extensions 1.2.0 namespace URI */
        public static final String EXTENSIONS_1_2_0_NAMESPACE = "http://maven.apache.org/EXTENSIONS/1.2.0";

        private Namespaces() {
            // Utility class
        }
    }

    /**
     * Schema location constants.
     */
    public static final class SchemaLocations {
        /** Schema location for 1.0.0 extensions */
        public static final String EXTENSIONS_1_0_0_SCHEMA_LOCATION =
                Namespaces.EXTENSIONS_1_0_0_NAMESPACE + " https://maven.apache.org/xsd/core-extensions-1.0.0.xsd";
        /** Schema location for 1.1.0 extensions */
        public static final String EXTENSIONS_1_1_0_SCHEMA_LOCATION =
                Namespaces.EXTENSIONS_1_1_0_NAMESPACE + " https://maven.apache.org/xsd/core-extensions-1.1.0.xsd";
        /** Schema location for 1.2.0 extensions */
        public static final String EXTENSIONS_1_2_0_SCHEMA_LOCATION =
                Namespaces.EXTENSIONS_1_2_0_NAMESPACE + " https://maven.apache.org/xsd/core-extensions-1.2.0.xsd";

        private SchemaLocations() {
            // Utility class
        }
    }

    /**
     * Common file and directory names.
     */
    public static final class Files {
        /** Standard Maven extensions file name */
        public static final String EXTENSIONS_XML = "extensions.xml";
        /** Maven extensions directory */
        public static final String MAVEN_EXTENSIONS_DIRECTORY = ".mvn";
        /** Full path to extensions file */
        public static final String MAVEN_EXTENSIONS_PATH = ".mvn/extensions.xml";

        private Files() {
            // Utility class
        }
    }

    /**
     * Common values and defaults for extensions elements.
     */
    public static final class Values {
        // Default type value
        public static final String DEFAULT_TYPE = "jar";

        // Common extension types
        public static final String TYPE_JAR = "jar";
        public static final String TYPE_MAVEN_PLUGIN = "maven-plugin";

        private Values() {
            // Utility class
        }
    }
}
