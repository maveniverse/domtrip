/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package org.maveniverse.domtrip.maven;

/**
 * Constants for Maven POM elements, attributes, and other XML constructs.
 *
 * <p>This class provides a comprehensive set of constants for working with Maven POM files,
 * organized into logical groups for better maintainability and ease of use.</p>
 *
 * <p>The constants are organized into nested classes:</p>
 * <ul>
 *   <li>{@link Elements} - XML element names used in Maven POMs</li>
 *   <li>{@link Attributes} - XML attribute names and values</li>
 *   <li>{@link Namespaces} - XML namespace URIs</li>
 *   <li>{@link SchemaLocations} - Schema location strings</li>
 *   <li>{@link ModelVersions} - Maven model version constants</li>
 *   <li>{@link Files} - Common file and directory names</li>
 *   <li>{@link Plugins} - Maven plugin related constants</li>
 * </ul>
 *
 * @since 0.1
 */
public final class MavenPomElements {

    private MavenPomElements() {
        // Utility class
    }

    /**
     * Maven model version constants.
     */
    public static final class ModelVersions {
        /** Maven 4.0.0 model version */
        public static final String MODEL_VERSION_4_0_0 = "4.0.0";
        /** Maven 4.1.0 model version */
        public static final String MODEL_VERSION_4_1_0 = "4.1.0";

        private ModelVersions() {
            // Utility class
        }
    }

    /**
     * Common XML element names used in Maven POMs.
     *
     * <p>These constants represent the standard XML element names found in Maven POM files,
     * organized logically by their purpose and location within the POM structure.</p>
     */
    public static final class Elements {
        // Core POM elements
        public static final String MODEL_VERSION = "modelVersion";
        public static final String GROUP_ID = "groupId";
        public static final String ARTIFACT_ID = "artifactId";
        public static final String VERSION = "version";
        public static final String PARENT = "parent";
        public static final String RELATIVE_PATH = "relativePath";
        public static final String PACKAGING = "packaging";
        public static final String NAME = "name";
        public static final String DESCRIPTION = "description";
        public static final String URL = "url";

        // Build elements
        public static final String BUILD = "build";
        public static final String PLUGINS = "plugins";
        public static final String PLUGIN = "plugin";
        public static final String PLUGIN_MANAGEMENT = "pluginManagement";
        public static final String DEFAULT_GOAL = "defaultGoal";
        public static final String DIRECTORY = "directory";
        public static final String FINAL_NAME = "finalName";
        public static final String SOURCE_DIRECTORY = "sourceDirectory";
        public static final String SCRIPT_SOURCE_DIRECTORY = "scriptSourceDirectory";
        public static final String TEST_SOURCE_DIRECTORY = "testSourceDirectory";
        public static final String OUTPUT_DIRECTORY = "outputDirectory";
        public static final String TEST_OUTPUT_DIRECTORY = "testOutputDirectory";
        public static final String EXTENSIONS = "extensions";
        public static final String EXECUTIONS = "executions";
        public static final String GOALS = "goals";
        public static final String INHERITED = "inherited";
        public static final String CONFIGURATION = "configuration";

        // Module elements
        public static final String MODULES = "modules";
        public static final String MODULE = "module";
        public static final String SUBPROJECTS = "subprojects";
        public static final String SUBPROJECT = "subproject";

        // Dependency elements
        public static final String DEPENDENCIES = "dependencies";
        public static final String DEPENDENCY = "dependency";
        public static final String DEPENDENCY_MANAGEMENT = "dependencyManagement";
        public static final String CLASSIFIER = "classifier";
        public static final String TYPE = "type";
        public static final String SCOPE = "scope";
        public static final String SYSTEM_PATH = "systemPath";
        public static final String OPTIONAL = "optional";
        public static final String EXCLUSIONS = "exclusions";

        // Profile elements
        public static final String PROFILES = "profiles";
        public static final String PROFILE = "profile";

        // Project information elements
        public static final String PROPERTIES = "properties";
        public static final String INCEPTION_YEAR = "inceptionYear";
        public static final String ORGANIZATION = "organization";
        public static final String LICENSES = "licenses";
        public static final String DEVELOPERS = "developers";
        public static final String CONTRIBUTORS = "contributors";
        public static final String MAILING_LISTS = "mailingLists";
        public static final String PREREQUISITES = "prerequisites";
        public static final String SCM = "scm";
        public static final String ISSUE_MANAGEMENT = "issueManagement";
        public static final String CI_MANAGEMENT = "ciManagement";
        public static final String DISTRIBUTION_MANAGEMENT = "distributionManagement";
        public static final String REPOSITORIES = "repositories";
        public static final String PLUGIN_REPOSITORIES = "pluginRepositories";
        public static final String REPOSITORY = "repository";
        public static final String PLUGIN_REPOSITORY = "pluginRepository";
        public static final String REPORTING = "reporting";

        private Elements() {
            // Utility class
        }
    }

    /**
     * XML attribute constants used in Maven POMs.
     */
    public static final class Attributes {
        /** Schema location attribute name */
        public static final String SCHEMA_LOCATION = "schemaLocation";
        /** XSI namespace prefix */
        public static final String XSI_NAMESPACE_PREFIX = "xsi";
        /** XSI namespace URI */
        public static final String XSI_NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema-instance";

        // Combine attributes
        public static final String COMBINE_CHILDREN = "combine.children";
        public static final String COMBINE_SELF = "combine.self";

        // Combine attribute values
        public static final String COMBINE_OVERRIDE = "override";
        public static final String COMBINE_MERGE = "merge";
        public static final String COMBINE_APPEND = "append";

        private Attributes() {
            // Utility class
        }
    }

    /**
     * Maven namespace constants.
     */
    public static final class Namespaces {
        /** Maven 4.0.0 namespace URI */
        public static final String MAVEN_4_0_0_NAMESPACE = "http://maven.apache.org/POM/4.0.0";
        /** Maven 4.1.0 namespace URI */
        public static final String MAVEN_4_1_0_NAMESPACE = "http://maven.apache.org/POM/4.1.0";

        private Namespaces() {
            // Utility class
        }
    }

    /**
     * Schema location constants.
     */
    public static final class SchemaLocations {
        /** Schema location for 4.0.0 models */
        public static final String MAVEN_4_0_0_SCHEMA_LOCATION =
                Namespaces.MAVEN_4_0_0_NAMESPACE + " https://maven.apache.org/xsd/maven-4.0.0.xsd";
        /** Schema location for 4.1.0 models */
        public static final String MAVEN_4_1_0_SCHEMA_LOCATION =
                Namespaces.MAVEN_4_1_0_NAMESPACE + " https://maven.apache.org/xsd/maven-4.1.0.xsd";

        private SchemaLocations() {
            // Utility class
        }
    }

    /**
     * Common file and directory names.
     */
    public static final class Files {
        /** Standard Maven POM file name */
        public static final String POM_XML = "pom.xml";
        /** Maven configuration directory (alternative name) */
        public static final String MVN_DIRECTORY = ".mvn";
        /** Default parent POM relative path */
        public static final String DEFAULT_PARENT_RELATIVE_PATH = "../pom.xml";

        private Files() {
            // Utility class
        }
    }

    /**
     * Common Maven plugin constants.
     */
    public static final class Plugins {
        /** Default Maven plugin groupId */
        public static final String DEFAULT_MAVEN_PLUGIN_GROUP_ID = "org.apache.maven.plugins";
        /** Maven plugin artifact prefix */
        public static final String MAVEN_PLUGIN_PREFIX = "maven-";
        /** Standard reason for Maven 4 compatibility upgrades */
        public static final String MAVEN_4_COMPATIBILITY_REASON = "Maven 4 compatibility";

        private Plugins() {
            // Utility class
        }
    }

    /**
     * Common indentation patterns for XML formatting.
     */
    public static final class Indentation {
        public static final String TWO_SPACES = "  ";
        public static final String FOUR_SPACES = "    ";
        public static final String TAB = "\t";
        public static final String DEFAULT = TWO_SPACES;

        private Indentation() {
            // Utility class
        }
    }
}
