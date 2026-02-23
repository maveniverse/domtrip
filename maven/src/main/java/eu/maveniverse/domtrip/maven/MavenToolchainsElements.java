/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.maven;

/**
 * Constants for Maven toolchains.xml elements, attributes, and other XML constructs.
 *
 * <p>This class provides a comprehensive set of constants for working with Maven toolchains files,
 * organized into logical groups for better maintainability and ease of use.</p>
 *
 * <p>The constants are organized into nested classes:</p>
 * <ul>
 *   <li>{@link Elements} - XML element names used in Maven toolchains</li>
 *   <li>{@link Attributes} - XML attribute names and values</li>
 *   <li>{@link Namespaces} - XML namespace URIs</li>
 *   <li>{@link SchemaLocations} - Schema location strings</li>
 *   <li>{@link Files} - Common file and directory names</li>
 *   <li>{@link ToolchainTypes} - Common toolchain types</li>
 * </ul>
 *
 * @since 0.1
 */
public final class MavenToolchainsElements {

    private MavenToolchainsElements() {
        // Utility class
    }

    /**
     * Common XML element names used in Maven toolchains.xml files.
     *
     * <p>These constants represent the standard XML element names found in Maven toolchains files,
     * organized logically by their purpose and location within the toolchains structure.</p>
     */
    public static final class Elements {
        // Root element
        public static final String TOOLCHAINS = "toolchains";

        // Toolchain elements
        public static final String TOOLCHAIN = "toolchain";
        public static final String TYPE = "type";
        public static final String PROVIDES = "provides";
        public static final String CONFIGURATION = "configuration";

        // Common provides elements
        public static final String VERSION = "version";
        public static final String VENDOR = "vendor";
        public static final String ID = "id";

        // JDK toolchain configuration elements
        public static final String JDK_HOME = "jdkHome";
        public static final String JAVA_HOME = "javaHome";

        // NetBeans toolchain configuration elements
        public static final String INSTALL_DIR = "installDir";

        // Common configuration elements
        public static final String EXECUTABLE = "executable";
        public static final String TOOL_HOME = "toolHome";

        private Elements() {
            // Utility class
        }
    }

    /**
     * XML attribute constants used in Maven toolchains.
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
     * Maven toolchains namespace constants.
     */
    public static final class Namespaces {
        /** Maven toolchains 1.0.0 namespace URI */
        public static final String TOOLCHAINS_1_0_0_NAMESPACE = "http://maven.apache.org/TOOLCHAINS/1.0.0";
        /** Maven toolchains 1.1.0 namespace URI */
        public static final String TOOLCHAINS_1_1_0_NAMESPACE = "http://maven.apache.org/TOOLCHAINS/1.1.0";

        private Namespaces() {
            // Utility class
        }
    }

    /**
     * Schema location constants.
     */
    public static final class SchemaLocations {
        /** Schema location for 1.0.0 toolchains */
        public static final String TOOLCHAINS_1_0_0_SCHEMA_LOCATION =
                Namespaces.TOOLCHAINS_1_0_0_NAMESPACE + " https://maven.apache.org/xsd/toolchains-1.0.0.xsd";
        /** Schema location for 1.1.0 toolchains */
        public static final String TOOLCHAINS_1_1_0_SCHEMA_LOCATION =
                Namespaces.TOOLCHAINS_1_1_0_NAMESPACE + " https://maven.apache.org/xsd/toolchains-1.1.0.xsd";

        private SchemaLocations() {
            // Utility class
        }
    }

    /**
     * Common file and directory names.
     */
    public static final class Files {
        /** Standard Maven toolchains file name */
        public static final String TOOLCHAINS_XML = "toolchains.xml";
        /** Global Maven toolchains file name */
        public static final String GLOBAL_TOOLCHAINS_XML = "toolchains.xml";
        /** User Maven directory */
        public static final String USER_MAVEN_DIRECTORY = ".m2";

        private Files() {
            // Utility class
        }
    }

    /**
     * Common toolchain types.
     */
    public static final class ToolchainTypes {
        /** JDK toolchain type */
        public static final String JDK = "jdk";
        /** NetBeans toolchain type */
        public static final String NETBEANS = "netbeans";
        /** Protobuf toolchain type */
        public static final String PROTOBUF = "protobuf";

        private ToolchainTypes() {
            // Utility class
        }
    }

    /**
     * Common values for toolchain elements.
     */
    public static final class Values {
        // Common JDK vendors
        public static final String VENDOR_ORACLE = "oracle";
        public static final String VENDOR_OPENJDK = "openjdk";
        public static final String VENDOR_ADOPTIUM = "adoptium";
        public static final String VENDOR_AZUL = "azul";
        public static final String VENDOR_AMAZON = "amazon";
        public static final String VENDOR_IBM = "ibm";
        public static final String VENDOR_GRAALVM = "graalvm";

        // Common JDK versions
        public static final String VERSION_8 = "1.8";
        public static final String VERSION_11 = "11";
        public static final String VERSION_17 = "17";
        public static final String VERSION_21 = "21";

        private Values() {
            // Utility class
        }
    }
}
