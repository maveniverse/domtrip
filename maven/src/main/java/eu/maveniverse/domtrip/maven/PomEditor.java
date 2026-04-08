/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.maven;

import static eu.maveniverse.domtrip.maven.MavenPomElements.Elements.*;
import static java.util.Arrays.asList;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripConfig;
import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Specialized editor for Maven POM files that extends the base {@link Editor} class
 * with Maven-specific functionality and element ordering.
 *
 * <p>The PomEditor provides lossless XML editing capabilities specifically tailored for Maven POM files,
 * including:</p>
 * <ul>
 *   <li><strong>Maven Element Ordering</strong> - Automatically orders elements according to Maven conventions</li>
 *   <li><strong>Formatting Preservation</strong> - Maintains original formatting, whitespace, and comments</li>
 *   <li><strong>Intelligent Blank Lines</strong> - Adds appropriate blank lines between element groups</li>
 *   <li><strong>Maven-specific Methods</strong> - Convenience methods for common POM operations</li>
 * </ul>
 *
 * <h3>Basic Usage:</h3>
 * <pre>{@code
 * // Parse existing POM
 * Document doc = Document.of(pomXmlString);
 * PomEditor editor = new PomEditor(doc);
 *
 * // Add elements with proper ordering
 * Element root = editor.root();
 * editor.insertMavenElement(root, "description", "My project description");
 * editor.insertMavenElement(root, "name", "My Project");  // Will be ordered before description
 *
 * // Serialize with preserved formatting
 * String result = editor.toXml();
 * }</pre>
 *
 * <h3>Element Ordering:</h3>
 * <p>The PomEditor automatically orders elements according to Maven POM conventions:</p>
 * <ul>
 *   <li>Project elements: modelVersion, parent, groupId, artifactId, version, packaging, name, description, etc.</li>
 *   <li>Build elements: defaultGoal, directory, finalName, sourceDirectory, etc.</li>
 *   <li>Plugin elements: groupId, artifactId, version, extensions, executions, etc.</li>
 *   <li>Dependency elements: groupId, artifactId, version, classifier, type, scope, etc.</li>
 * </ul>
 *
 * @see Editor
 * @see MavenPomElements
 * @since 0.1
 */
public class PomEditor extends AbstractMavenEditor {

    /** CamelCase version suffix used in property naming convention detection. */
    private static final String VERSION_SUFFIX = "Version";

    /** Label for dependency coordinate validation in {@code requireGA} calls. */
    private static final String DEPENDENCY_LABEL = "Dependency";

    // Element ordering configuration for Maven POM elements
    private static final Map<String, List<String>> ELEMENT_ORDER = new HashMap<>();

    static {
        // Project element order - follows Maven POM reference order
        ELEMENT_ORDER.put(
                "project",
                asList(
                        MODEL_VERSION,
                        "", // blank line
                        PARENT,
                        "", // blank line
                        GROUP_ID,
                        ARTIFACT_ID,
                        VERSION,
                        PACKAGING,
                        "", // blank line
                        NAME,
                        DESCRIPTION,
                        URL,
                        INCEPTION_YEAR,
                        ORGANIZATION,
                        LICENSES,
                        "", // blank line
                        DEVELOPERS,
                        CONTRIBUTORS,
                        "", // blank line
                        MAILING_LISTS,
                        "", // blank line
                        PREREQUISITES,
                        "", // blank line
                        MODULES,
                        "", // blank line
                        SCM,
                        ISSUE_MANAGEMENT,
                        CI_MANAGEMENT,
                        DISTRIBUTION_MANAGEMENT,
                        "", // blank line
                        PROPERTIES,
                        "", // blank line
                        DEPENDENCY_MANAGEMENT,
                        DEPENDENCIES,
                        "", // blank line
                        REPOSITORIES,
                        PLUGIN_REPOSITORIES,
                        "", // blank line
                        BUILD,
                        "", // blank line
                        REPORTING,
                        "", // blank line
                        PROFILES));

        // Build element order
        ELEMENT_ORDER.put(
                BUILD,
                asList(
                        DEFAULT_GOAL,
                        DIRECTORY,
                        FINAL_NAME,
                        SOURCE_DIRECTORY,
                        SCRIPT_SOURCE_DIRECTORY,
                        TEST_SOURCE_DIRECTORY,
                        OUTPUT_DIRECTORY,
                        TEST_OUTPUT_DIRECTORY,
                        EXTENSIONS,
                        "", // blank line
                        PLUGIN_MANAGEMENT,
                        "", // blank line
                        PLUGINS));

        // Plugin element order
        ELEMENT_ORDER.put(
                PLUGIN,
                asList(
                        GROUP_ID,
                        ARTIFACT_ID,
                        VERSION,
                        EXTENSIONS,
                        EXECUTIONS,
                        DEPENDENCIES,
                        GOALS,
                        INHERITED,
                        CONFIGURATION));

        // Dependency element order
        ELEMENT_ORDER.put(
                DEPENDENCY,
                asList(GROUP_ID, ARTIFACT_ID, VERSION, CLASSIFIER, TYPE, SCOPE, SYSTEM_PATH, OPTIONAL, EXCLUSIONS));

        // Exclusion element order
        ELEMENT_ORDER.put(EXCLUSION, asList(GROUP_ID, ARTIFACT_ID));
    }

    /**
     * Creates a new PomEditor with default configuration.
     */
    public PomEditor() {
        super();
    }

    /**
     * Creates a new PomEditor with custom configuration.
     *
     * @param config the configuration to use
     */
    public PomEditor(DomTripConfig config) {
        super(config);
    }

    /**
     * Creates a new PomEditor with an existing Document.
     *
     * @param document the existing Document to edit
     */
    public PomEditor(Document document) {
        super(document);
    }

    /**
     * Creates a new PomEditor with an existing Document and custom configuration.
     *
     * @param document the existing Document to edit
     * @param config the configuration to use
     */
    public PomEditor(Document document, DomTripConfig config) {
        super(document, config);
    }

    /**
     * Gets the appropriate element order list for the given parent element.
     * This implementation provides POM-specific ordering with blank line markers.
     */
    @Override
    protected List<String> getOrderListForParent(Element parent) {
        return ELEMENT_ORDER.get(parent.name());
    }

    /**
     * Determines whether an element name should be skipped during ordering analysis.
     * For POM files, empty strings represent blank line markers.
     */
    @Override
    protected boolean shouldSkipInOrdering(String elementName) {
        return elementName.isEmpty(); // Skip blank line markers
    }

    /**
     * Enhanced element insertion with POM-specific blank line handling.
     */
    @Override
    protected Element insertElementAtPosition(
            Element parent,
            String elementName,
            Element insertBefore,
            Element insertAfter,
            List<String> order,
            int elementIndex)
            throws DomTripException {
        // Determine if we need blank lines before/after based on ordering
        boolean needsBlankLineBefore = needsBlankLineBefore(parent, order, elementIndex);
        boolean needsBlankLineAfter = needsBlankLineAfter(parent, order, elementIndex);

        // Insert the element at the correct position
        Element newElement;
        if (insertBefore != null) {
            newElement = insertElementBefore(insertBefore, elementName);
        } else if (insertAfter != null) {
            newElement = insertElementAfter(insertAfter, elementName);
        } else {
            newElement = addElement(parent, elementName);
        }
        if (needsBlankLineBefore) {
            addBlankLineBefore(newElement);
        }
        if (needsBlankLineAfter) {
            addBlankLineAfter(newElement);
        }
        return newElement;
    }

    /**
     * Inserts a new Maven element with proper ordering and formatting.
     *
     * <p>This method automatically determines the correct position for the new element
     * based on Maven POM conventions and adds appropriate blank lines.</p>
     *
     * @param parent the parent element
     * @param elementName the name of the new element
     * @return the newly created element
     * @throws DomTripException if the element cannot be added
     */
    public Element insertMavenElement(Element parent, String elementName) throws DomTripException {
        return insertElementAtCorrectPosition(parent, elementName, null);
    }

    /**
     * Inserts a new Maven element with text content and proper ordering.
     *
     * @param parent the parent element
     * @param elementName the name of the new element
     * @param textContent the text content for the element
     * @return the newly created element
     * @throws DomTripException if the element cannot be added
     */
    public Element insertMavenElement(Element parent, String elementName, String textContent) throws DomTripException {
        return insertElementAtCorrectPosition(parent, elementName, textContent);
    }

    /**
     * Finds a child element by name under the specified parent.
     *
     * @param parent the parent element
     * @param elementName the child element name to find
     * @return the child element if found, null otherwise
     */
    public Element findChildElement(Element parent, String elementName) {
        return parent.childElement(elementName).orElse(null);
    }

    /**
     * Creates a new Maven POM document with the specified root element name.
     *
     * @param rootElementName the name of the root element (typically "project")
     * @throws DomTripException if the document cannot be created
     */
    public void createMavenDocument(String rootElementName) throws DomTripException {
        createDocument(rootElementName);

        // Set the Maven namespace on the root element if it's a project
        if ("project".equals(rootElementName)) {
            Element root = root();
            root.attribute("xmlns", MavenPomElements.Namespaces.MAVEN_4_0_0_NAMESPACE);
            root.attribute("xmlns:xsi", MavenPomElements.Attributes.XSI_NAMESPACE_URI);
            root.attribute("xsi:schemaLocation", MavenPomElements.SchemaLocations.MAVEN_4_0_0_SCHEMA_LOCATION);
        }
    }

    /**
     * Helper for managing regular and managed Maven dependencies within a POM.
     *
     * <p>Provides high-level operations for adding, updating, deleting, aligning, and inspecting
     * dependencies, including support for exclusions, dependencyManagement, and convention detection.</p>
     *
     * <p>By default, operations resolve relative to the project root ({@code <project>}).
     * Use {@link #forProfile(String)} or {@link #forProfile(Element)} to obtain a profile-scoped
     * instance whose operations resolve relative to a specific {@code <profile>} element.</p>
     *
     * @since 0.3.0
     */
    public class Dependencies {

        private final Element contextElement;

        Dependencies() {
            this.contextElement = null;
        }

        private Dependencies(Element contextElement) {
            this.contextElement = contextElement;
        }

        /**
         * Returns the context element for dependency resolution.
         *
         * <p>Shadows {@link PomEditor#root()} so that all existing methods in this class
         * automatically resolve relative to the correct element — the project root for
         * top-level operations, or a {@code <profile>} element for profile-scoped operations.</p>
         */
        private Element root() {
            return contextElement != null ? contextElement : PomEditor.this.root();
        }

        /**
         * Returns a Dependencies instance scoped to the specified Maven profile.
         *
         * <p>All dependency operations on the returned instance will resolve paths relative to the
         * profile element instead of the project root. For example, {@code addAligned} will target
         * {@code project/profiles/profile[id=X]/dependencies} instead of {@code project/dependencies}.</p>
         *
         * @param profileId the {@code <id>} of the profile to scope to
         * @return a Dependencies instance scoped to the profile
         * @throws DomTripException if the profile is not found
         * @since 1.1.0
         */
        public Dependencies forProfile(String profileId) {
            if (profileId == null || profileId.trim().isEmpty()) {
                throw new DomTripException("Profile id cannot be null or empty");
            }
            Element profile = findProfileElement(profileId);
            if (profile == null) {
                throw new DomTripException("Profile '" + profileId + "' not found");
            }
            return forProfile(profile);
        }

        /**
         * Returns a Dependencies instance scoped to the given profile element.
         *
         * <p>This overload accepts a pre-resolved {@code <profile>} element, which can be
         * obtained via {@link Profiles#findProfile(String)}. This is useful when the caller
         * has already located the profile or wants to check its existence before scoping.</p>
         *
         * <h4>Example:</h4>
         * <pre>{@code
         * Element profile = editor.profiles().findProfile("my-profile");
         * if (profile != null) {
         *     editor.dependencies().forProfile(profile).addAligned(coords);
         * }
         * }</pre>
         *
         * @param profileElement the {@code <profile>} element to scope to
         * @return a Dependencies instance scoped to the profile
         * @since 1.1.0
         */
        public Dependencies forProfile(Element profileElement) {
            if (profileElement == null) {
                throw new DomTripException("Profile element cannot be null");
            }
            if (!PROFILE.equals(profileElement.name())) {
                throw new DomTripException("Expected a <profile> element but got <" + profileElement.name() + ">");
            }
            return new Dependencies(profileElement);
        }

        private void requireGA(String label, Coordinates coordinates) {
            if (coordinates.groupId() == null || coordinates.groupId().trim().isEmpty()) {
                throw new DomTripException(label + " groupId cannot be null or empty");
            }
            if (coordinates.artifactId() == null
                    || coordinates.artifactId().trim().isEmpty()) {
                throw new DomTripException(label + " artifactId cannot be null or empty");
            }
        }

        private Element findDependencyElement(Element dependencies, Coordinates dependency) {
            return dependencies
                    .childElements(DEPENDENCY)
                    .filter(dependency.predicateGA())
                    .findFirst()
                    .orElse(null);
        }

        private Element addExclusionToElement(Element dep, Coordinates exclusion) {
            Element exclusions = findChildElement(dep, EXCLUSIONS);
            if (exclusions == null) {
                exclusions = insertMavenElement(dep, EXCLUSIONS);
            }
            Element exclElement = insertMavenElement(exclusions, EXCLUSION);
            insertMavenElement(exclElement, GROUP_ID, exclusion.groupId());
            insertMavenElement(exclElement, ARTIFACT_ID, exclusion.artifactId());
            return exclElement;
        }

        private boolean deleteExclusionFromElement(Element dep, Coordinates exclusion) {
            Element exclusions = findChildElement(dep, EXCLUSIONS);
            if (exclusions == null) {
                return false;
            }
            Element excl = exclusions
                    .childElements(EXCLUSION)
                    .filter(exclusion.predicateGA())
                    .findFirst()
                    .orElse(null);
            if (excl != null) {
                removeElement(excl);
                if (!exclusions.childElements(EXCLUSION).findFirst().isPresent()) {
                    removeElement(exclusions);
                }
                return true;
            }
            return false;
        }

        private boolean hasExclusionInElement(Element dep, Coordinates exclusion) {
            Element exclusions = findChildElement(dep, EXCLUSIONS);
            if (exclusions == null) {
                return false;
            }
            return exclusions.childElements(EXCLUSION).anyMatch(exclusion.predicateGA());
        }

        /**
         * Adds a dependency element with the specified coordinates.
         *
         * @param dependenciesElement the dependencies container element
         * @param groupId             the dependency groupId
         * @param artifactId          the dependency artifactId
         * @param version             the dependency version (can be null)
         * @return the newly created dependency element
         * @throws DomTripException if the dependency cannot be added
         */
        public Element addDependency(Element dependenciesElement, String groupId, String artifactId, String version)
                throws DomTripException {
            Element dependency = insertMavenElement(dependenciesElement, DEPENDENCY);
            insertMavenElement(dependency, GROUP_ID, groupId);
            insertMavenElement(dependency, ARTIFACT_ID, artifactId);
            if (version != null && !version.trim().isEmpty()) {
                insertMavenElement(dependency, VERSION, version);
            }
            return dependency;
        }

        /**
         * Updates or inserts a managed dependency in {@code project/dependencyManagement/dependencies/dependency[]}.
         *
         * <p>If the dependency exists (matched by GATC), its version is updated. If the version is a property
         * reference (${...}), the property value is updated instead. If {@code upsert} is true and the dependency
         * doesn't exist, it will be created.</p>
         *
         * <h4>Example:</h4>
         * <pre>{@code
         * PomEditor editor = new PomEditor(document);
         * Coordinates junit = Coordinates.of("org.junit.jupiter", "junit-jupiter", "5.10.0");
         * editor.updateManagedDependency(true, junit);
         * }</pre>
         *
         * @param upsert whether to create the dependency if it doesn't exist
         * @param coordinates the artifact coordinates
         * @return true if the dependency was updated or created, false otherwise
         * @throws DomTripException if an error occurs during editing
         * @since 0.3.0
         */
        public boolean updateManagedDependency(boolean upsert, Coordinates coordinates) throws DomTripException {
            Element dependencyManagement = findOrCreateElement(root(), DEPENDENCY_MANAGEMENT, upsert);
            if (dependencyManagement == null) {
                return false;
            }
            Element dependencies = findOrCreateElement(dependencyManagement, DEPENDENCIES, upsert);
            if (dependencies == null) {
                return false;
            }
            return updateOrCreateDependencyInContainer(dependencies, upsert, coordinates);
        }

        private Element findOrCreateElement(Element parent, String childName, boolean create) throws DomTripException {
            Element child = findChildElement(parent, childName);
            if (child == null && create) {
                child = insertMavenElement(parent, childName);
            }
            return child;
        }

        private boolean updateOrCreateDependencyInContainer(
                Element dependencies, boolean upsert, Coordinates coordinates) throws DomTripException {
            Element dependency = dependencies
                    .childElements(DEPENDENCY)
                    .filter(coordinates.predicateGATC())
                    .findFirst()
                    .orElse(null);
            if (dependency == null && upsert) {
                createNewDependency(dependencies, coordinates);
                return true;
            }
            if (dependency != null) {
                return updateVersionElement(dependency, coordinates.version());
            }
            return false;
        }

        private void createNewDependency(Element dependencies, Coordinates coordinates) throws DomTripException {
            Element dependency =
                    addDependency(dependencies, coordinates.groupId(), coordinates.artifactId(), coordinates.version());
            if (coordinates.type() != null && !"jar".equals(coordinates.type())) {
                insertMavenElement(dependency, TYPE, coordinates.type());
            }
            if (coordinates.classifier() != null) {
                insertMavenElement(dependency, CLASSIFIER, coordinates.classifier());
            }
        }

        /**
         * Removes a managed dependency from {@code project/dependencyManagement/dependencies/dependency[]}.
         *
         * <h4>Example:</h4>
         * <pre>{@code
         * PomEditor editor = new PomEditor(document);
         * Coordinates junit = Coordinates.of("org.junit.jupiter", "junit-jupiter", "5.10.0");
         * editor.deleteManagedDependency(junit);
         * }</pre>
         *
         * @param coordinates the artifact to remove (matched by GATC)
         * @return true if the dependency was removed, false if it didn't exist
         * @since 0.3.0
         */
        public boolean deleteManagedDependency(Coordinates coordinates) throws DomTripException {
            Element dependencyManagement = findChildElement(root(), DEPENDENCY_MANAGEMENT);
            if (dependencyManagement != null) {
                Element dependencies = findChildElement(dependencyManagement, DEPENDENCIES);
                if (dependencies != null) {
                    Element dependency = dependencies
                            .childElements(DEPENDENCY)
                            .filter(coordinates.predicateGATC())
                            .findFirst()
                            .orElse(null);
                    if (dependency != null) {
                        return removeElement(dependency);
                    }
                }
            }
            return false;
        }

        /**
         * Updates or inserts a dependency in {@code project/dependencies/dependency[]}.
         *
         * <p>If the dependency exists (matched by GATC), its version is updated. If the version is a property
         * reference (${...}), the property value is updated instead. If the dependency has no version element,
         * the managed dependency is updated instead. If {@code upsert} is true and the dependency doesn't exist,
         * it will be created.</p>
         *
         * <h4>Example:</h4>
         * <pre>{@code
         * PomEditor editor = new PomEditor(document);
         * Coordinates junit = Coordinates.of("org.junit.jupiter", "junit-jupiter", "5.10.0");
         * editor.updateDependency(true, junit);
         * }</pre>
         *
         * @param upsert whether to create the dependency if it doesn't exist
         * @param coordinates the artifact coordinates
         * @return true if the dependency was updated or created, false otherwise
         * @throws DomTripException if an error occurs during editing
         * @since 0.3.0
         */
        public boolean updateDependency(boolean upsert, Coordinates coordinates) throws DomTripException {
            Element dependencies = findOrCreateElement(root(), DEPENDENCIES, upsert);
            if (dependencies == null) {
                return false;
            }

            Element dependency = dependencies
                    .childElements(DEPENDENCY)
                    .filter(coordinates.predicateGATC())
                    .findFirst()
                    .orElse(null);
            if (dependency == null && upsert) {
                createNewDependency(dependencies, coordinates);
                return true;
            }
            if (dependency != null) {
                return updateExistingDependency(dependency, coordinates);
            }
            return false;
        }

        private boolean updateExistingDependency(Element dependency, Coordinates coordinates) throws DomTripException {
            java.util.Optional<Element> version = dependency.childElement(VERSION);
            if (version.isPresent()) {
                return updateVersionElement(dependency, coordinates.version());
            }
            return updateManagedDependency(false, coordinates);
        }

        /**
         * Removes a dependency from {@code project/dependencies/dependency[]}.
         *
         * <h4>Example:</h4>
         * <pre>{@code
         * PomEditor editor = new PomEditor(document);
         * Coordinates junit = Coordinates.of("org.junit.jupiter", "junit-jupiter", "5.10.0");
         * editor.deleteDependency(junit);
         * }</pre>
         *
         * @param coordinates the Coordinates to remove (matched by GATC)
         * @return true if the dependency was removed, false if it didn't exist
         * @since 0.3.0
         */
        public boolean deleteDependency(Coordinates coordinates) throws DomTripException {
            Element dependencies = findChildElement(root(), DEPENDENCIES);
            if (dependencies != null) {
                Element dependency = dependencies
                        .childElements(DEPENDENCY)
                        .filter(coordinates.predicateGATC())
                        .findFirst()
                        .orElse(null);
                if (dependency != null) {
                    return removeElement(dependency);
                }
            }
            return false;
        }

        /**
         * Removes a dependency version from {@code project/dependencies/dependency[]}. This call is usually combined
         * with adding dependency management for same dependency.
         *
         * <h4>Example:</h4>
         * <pre>{@code
         * PomEditor editor = new PomEditor(document);
         * Coordinates junit = Coordinates.of("org.junit.jupiter", "junit-jupiter", "5.10.0");
         * editor.deleteDependencyVersion(junit);
         * editor.updateManagedDependency(true, junit);
         * }</pre>
         *
         * @param coordinates the Coordinates to remove (matched by GATC)
         * @return true if the dependency was removed, false if it didn't exist
         * @since 0.3.1
         */
        public boolean deleteDependencyVersion(Coordinates coordinates) throws DomTripException {
            Element dependencies = findChildElement(root(), DEPENDENCIES);
            if (dependencies != null) {
                Element dependency = dependencies
                        .childElements(DEPENDENCY)
                        .filter(coordinates.predicateGATC())
                        .findFirst()
                        .orElse(null);
                if (dependency != null) {
                    return dependency
                            .childElement(VERSION)
                            .map(PomEditor.this::removeElement)
                            .isPresent();
                }
            }
            return false;
        }

        /**
         * Adds an exclusion to a dependency. Creates the {@code <exclusions>} wrapper if absent.
         * The dependency is found by matching on groupId:artifactId (GA).
         *
         * <h4>Example:</h4>
         * <pre>{@code
         * PomEditor editor = new PomEditor(document);
         * Coordinates dep = Coordinates.of("org.example", "my-lib", "1.0.0");
         * Coordinates excl = Coordinates.of("commons-logging", "commons-logging", null);
         * editor.dependencies().addExclusion(dep, excl);
         * }</pre>
         *
         * @param dependency the dependency coordinates (matched by GA)
         * @param exclusion the exclusion coordinates (groupId and artifactId)
         * @return the newly created exclusion element
         * @throws DomTripException if the dependency is not found or an error occurs
         * @since 1.0.0
         */
        public Element addExclusion(Coordinates dependency, Coordinates exclusion) throws DomTripException {
            requireGA(DEPENDENCY_LABEL, dependency);
            requireGA("Exclusion", exclusion);
            Element dependencies = findChildElement(root(), DEPENDENCIES);
            if (dependencies == null) {
                throw new DomTripException("No dependencies element found");
            }
            Element dep = findDependencyElement(dependencies, dependency);
            if (dep == null) {
                throw new DomTripException("Dependency not found: " + dependency.toGA());
            }
            return addExclusionToElement(dep, exclusion);
        }

        /**
         * Removes an exclusion from a dependency. Removes the {@code <exclusions>} wrapper if it becomes empty.
         *
         * <h4>Example:</h4>
         * <pre>{@code
         * PomEditor editor = new PomEditor(document);
         * Coordinates dep = Coordinates.of("org.example", "my-lib", "1.0.0");
         * Coordinates excl = Coordinates.of("commons-logging", "commons-logging", null);
         * editor.dependencies().deleteExclusion(dep, excl);
         * }</pre>
         *
         * @param dependency the dependency coordinates (matched by GA)
         * @param exclusion the exclusion coordinates (matched by GA)
         * @return true if the exclusion was removed, false if it didn't exist
         * @since 1.0.0
         */
        public boolean deleteExclusion(Coordinates dependency, Coordinates exclusion) throws DomTripException {
            Element dependencies = findChildElement(root(), DEPENDENCIES);
            if (dependencies == null) {
                return false;
            }
            Element dep = findDependencyElement(dependencies, dependency);
            if (dep == null) {
                return false;
            }
            return deleteExclusionFromElement(dep, exclusion);
        }

        /**
         * Checks whether a dependency has a specific exclusion.
         *
         * <h4>Example:</h4>
         * <pre>{@code
         * PomEditor editor = new PomEditor(document);
         * Coordinates dep = Coordinates.of("org.example", "my-lib", "1.0.0");
         * Coordinates excl = Coordinates.of("commons-logging", "commons-logging", null);
         * boolean has = editor.dependencies().hasExclusion(dep, excl);
         * }</pre>
         *
         * @param dependency the dependency coordinates (matched by GA)
         * @param exclusion the exclusion coordinates (matched by GA)
         * @return true if the dependency has the specified exclusion
         * @since 1.0.0
         */
        public boolean hasExclusion(Coordinates dependency, Coordinates exclusion) {
            Element dependencies = findChildElement(root(), DEPENDENCIES);
            if (dependencies == null) {
                return false;
            }
            Element dep = findDependencyElement(dependencies, dependency);
            if (dep == null) {
                return false;
            }
            return hasExclusionInElement(dep, exclusion);
        }

        /**
         * Adds an exclusion to a managed dependency. Creates the {@code <exclusions>} wrapper if absent.
         * The dependency is found by matching on groupId:artifactId (GA).
         *
         * <h4>Example:</h4>
         * <pre>{@code
         * PomEditor editor = new PomEditor(document);
         * Coordinates dep = Coordinates.of("org.example", "my-lib", "1.0.0");
         * Coordinates excl = Coordinates.of("commons-logging", "commons-logging", null);
         * editor.dependencies().addManagedExclusion(dep, excl);
         * }</pre>
         *
         * @param dependency the dependency coordinates (matched by GA)
         * @param exclusion the exclusion coordinates (groupId and artifactId)
         * @return the newly created exclusion element
         * @throws DomTripException if the dependency is not found or an error occurs
         * @since 1.0.0
         */
        public Element addManagedExclusion(Coordinates dependency, Coordinates exclusion) throws DomTripException {
            requireGA("Managed dependency", dependency);
            requireGA("Exclusion", exclusion);
            Element dependencyManagement = findChildElement(root(), DEPENDENCY_MANAGEMENT);
            if (dependencyManagement == null) {
                throw new DomTripException("No dependencyManagement element found");
            }
            Element dependencies = findChildElement(dependencyManagement, DEPENDENCIES);
            if (dependencies == null) {
                throw new DomTripException("No dependencies element found in dependencyManagement");
            }
            Element dep = findDependencyElement(dependencies, dependency);
            if (dep == null) {
                throw new DomTripException("Managed dependency not found: " + dependency.toGA());
            }
            return addExclusionToElement(dep, exclusion);
        }

        /**
         * Removes an exclusion from a managed dependency. Removes the {@code <exclusions>} wrapper if it becomes empty.
         *
         * <h4>Example:</h4>
         * <pre>{@code
         * PomEditor editor = new PomEditor(document);
         * Coordinates dep = Coordinates.of("org.example", "my-lib", "1.0.0");
         * Coordinates excl = Coordinates.of("commons-logging", "commons-logging", null);
         * editor.dependencies().deleteManagedExclusion(dep, excl);
         * }</pre>
         *
         * @param dependency the dependency coordinates (matched by GA)
         * @param exclusion the exclusion coordinates (matched by GA)
         * @return true if the exclusion was removed, false if it didn't exist
         * @since 1.0.0
         */
        public boolean deleteManagedExclusion(Coordinates dependency, Coordinates exclusion) throws DomTripException {
            Element dependencyManagement = findChildElement(root(), DEPENDENCY_MANAGEMENT);
            if (dependencyManagement == null) {
                return false;
            }
            Element dependencies = findChildElement(dependencyManagement, DEPENDENCIES);
            if (dependencies == null) {
                return false;
            }
            Element dep = findDependencyElement(dependencies, dependency);
            if (dep == null) {
                return false;
            }
            return deleteExclusionFromElement(dep, exclusion);
        }

        /**
         * Checks whether a managed dependency has a specific exclusion.
         *
         * <h4>Example:</h4>
         * <pre>{@code
         * PomEditor editor = new PomEditor(document);
         * Coordinates dep = Coordinates.of("org.example", "my-lib", "1.0.0");
         * Coordinates excl = Coordinates.of("commons-logging", "commons-logging", null);
         * boolean has = editor.dependencies().hasManagedExclusion(dep, excl);
         * }</pre>
         *
         * @param dependency the dependency coordinates (matched by GA)
         * @param exclusion the exclusion coordinates (matched by GA)
         * @return true if the managed dependency has the specified exclusion
         * @since 1.0.0
         */
        public boolean hasManagedExclusion(Coordinates dependency, Coordinates exclusion) {
            Element dependencyManagement = findChildElement(root(), DEPENDENCY_MANAGEMENT);
            if (dependencyManagement == null) {
                return false;
            }
            Element dependencies = findChildElement(dependencyManagement, DEPENDENCIES);
            if (dependencies == null) {
                return false;
            }
            Element dep = findDependencyElement(dependencies, dependency);
            if (dep == null) {
                return false;
            }
            return hasExclusionInElement(dep, exclusion);
        }

        // ========== CONVENTION DETECTION ==========

        /**
         * Detects whether most dependencies use managed versions (version-less) or inline versions.
         *
         * <p>Analyzes {@code project/dependencies/dependency[]} to count how many have inline versions
         * versus how many delegate to {@code dependencyManagement}. Returns
         * {@link AlignOptions.VersionStyle#MANAGED} if the strict majority of dependencies are version-less.</p>
         *
         * @return the detected version style
         * @since 1.1.0
         */
        public AlignOptions.VersionStyle detectVersionStyle() {
            Element dependencies = findChildElement(root(), DEPENDENCIES);
            if (dependencies == null) {
                Element depMgmt = findChildElement(root(), DEPENDENCY_MANAGEMENT);
                return depMgmt != null ? AlignOptions.VersionStyle.MANAGED : AlignOptions.VersionStyle.INLINE;
            }

            long total = dependencies.childElements(DEPENDENCY).count();
            if (total == 0) {
                Element depMgmt = findChildElement(root(), DEPENDENCY_MANAGEMENT);
                return depMgmt != null ? AlignOptions.VersionStyle.MANAGED : AlignOptions.VersionStyle.INLINE;
            }

            long withVersion = countVersionedDependencies(dependencies);
            long withoutVersion = total - withVersion;

            return withoutVersion > withVersion ? AlignOptions.VersionStyle.MANAGED : AlignOptions.VersionStyle.INLINE;
        }

        /**
         * Determine whether dependency versions predominantly use property references or literal values.
         *
         * <p>Analyzes `<project>/dependencies` and `<project>/dependencyManagement/dependencies`. If a strict
         * majority of observed `<version>` elements are property references of the form `${...}`, the method
         * returns {@link AlignOptions.VersionSource#PROPERTY}; otherwise it returns
         * {@link AlignOptions.VersionSource#LITERAL}.</p>
         *
         * @return {@link AlignOptions.VersionSource#PROPERTY} if strictly more versioned dependencies use `${...}` property
         *         references, {@link AlignOptions.VersionSource#LITERAL} otherwise
         * @since 1.1.0
         */
        public AlignOptions.VersionSource detectVersionSource() {
            long propertyCount = 0;
            long totalVersioned = 0;

            Element dependencies = findChildElement(root(), DEPENDENCIES);
            if (dependencies != null) {
                propertyCount += countPropertyVersionedDependencies(dependencies);
                totalVersioned += countVersionedDependencies(dependencies);
            }

            Element depMgmt = findChildElement(root(), DEPENDENCY_MANAGEMENT);
            if (depMgmt != null) {
                Element managedDeps = findChildElement(depMgmt, DEPENDENCIES);
                if (managedDeps != null) {
                    propertyCount += countPropertyVersionedDependencies(managedDeps);
                    totalVersioned += countVersionedDependencies(managedDeps);
                }
            }

            if (totalVersioned == 0) {
                return AlignOptions.VersionSource.LITERAL;
            }

            return propertyCount > totalVersioned - propertyCount
                    ? AlignOptions.VersionSource.PROPERTY
                    : AlignOptions.VersionSource.LITERAL;
        }

        /**
         * Detects the dominant property naming convention from existing version property references.
         *
         * <p>Analyzes {@code ${...}} version references in both regular and managed dependencies
         * to determine the naming pattern. Falls back to
         * {@link AlignOptions.PropertyNamingConvention#DOT_SUFFIX} if no pattern can be detected.</p>
         *
         * @return the detected naming convention
         * @since 1.1.0
         */
        public AlignOptions.PropertyNamingConvention detectPropertyNamingConvention() {
            Map<AlignOptions.PropertyNamingConvention, Integer> votes =
                    new EnumMap<>(AlignOptions.PropertyNamingConvention.class);

            collectPropertyConventionVotes(findChildElement(root(), DEPENDENCIES), votes);

            Element depMgmt = findChildElement(root(), DEPENDENCY_MANAGEMENT);
            if (depMgmt != null) {
                collectPropertyConventionVotes(findChildElement(depMgmt, DEPENDENCIES), votes);
            }

            AlignOptions.PropertyNamingConvention best = AlignOptions.PropertyNamingConvention.DOT_SUFFIX;
            int bestCount = 0;
            for (Map.Entry<AlignOptions.PropertyNamingConvention, Integer> entry : votes.entrySet()) {
                if (entry.getValue() > bestCount) {
                    bestCount = entry.getValue();
                    best = entry.getKey();
                }
            }
            return best;
        }

        /**
         * Detects the project's dominant dependency version style, version source, and property naming convention
         * and returns an AlignOptions instance with all corresponding fields populated.
         *
         * @return an AlignOptions populated with the detected version style, version source, and naming convention
         * @since 1.1.0
         */
        public AlignOptions detectConventions() {
            return AlignOptions.builder()
                    .versionStyle(detectVersionStyle())
                    .versionSource(detectVersionSource())
                    .namingConvention(detectPropertyNamingConvention())
                    .build();
        }

        // ========== ALIGNED OPERATIONS ==========

        /**
         * Adds a dependency aligned with the project's auto-detected conventions.
         *
         * <p>Detects the project's dependency management style (managed vs inline, property vs literal,
         * property naming convention) and adds the dependency accordingly. If the dependency already
         * exists, returns false.</p>
         *
         * @param coords the dependency coordinates (version is required)
         * @return true if the dependency was added, false if it already existed
         * @throws DomTripException if the coordinates are invalid or version is null
         * @since 1.1.0
         */
        public boolean addAligned(Coordinates coords) {
            return addAligned(coords, AlignOptions.defaults());
        }

        /**
         * Adds a dependency aligned with the specified options, auto-detecting any unspecified conventions.
         *
         * <p>Example usage:</p>
         * <pre>{@code
         * PomEditor editor = new PomEditor(document);
         * Coordinates guava = Coordinates.of("com.google.guava", "guava", "32.1.2-jre");
         *
         * // Auto-detect all conventions
         * editor.dependencies().addAligned(guava);
         *
         * // Force managed + property with explicit property name
         * editor.dependencies().addAligned(guava, AlignOptions.builder()
         *     .versionStyle(AlignOptions.VersionStyle.MANAGED)
         *     .versionSource(AlignOptions.VersionSource.PROPERTY)
         *     .propertyName("guava.version")
         *     .build());
         *
         * // Add as test dependency
         * editor.dependencies().addAligned(junit, AlignOptions.builder()
         *     .scope("test")
         *     .build());
         * }</pre>
         *
         * @param coords the dependency coordinates (version is required)
         * @param options alignment options (null fields are auto-detected)
         * @return true if the dependency was added, false if it already existed
         * @throws DomTripException if the coordinates are invalid or version is null
         * @since 1.1.0
         */
        public boolean addAligned(Coordinates coords, AlignOptions options) {
            requireGA(DEPENDENCY_LABEL, coords);
            if (coords.version() == null) {
                throw new DomTripException("Version is required for addAligned");
            }

            // Check if dependency already exists
            Element deps = findChildElement(root(), DEPENDENCIES);
            if (deps != null) {
                Element existing = deps.childElements(DEPENDENCY)
                        .filter(coords.predicateGATC())
                        .findFirst()
                        .orElse(null);
                if (existing != null) {
                    return false;
                }
            }

            // Resolve conventions
            Object[] conventions = resolveConventions(options);
            AlignOptions.VersionStyle versionStyle = (AlignOptions.VersionStyle) conventions[0];
            AlignOptions.VersionSource versionSource = (AlignOptions.VersionSource) conventions[1];
            AlignOptions.PropertyNamingConvention naming = (AlignOptions.PropertyNamingConvention) conventions[2];

            String actualVersion = coords.version();
            String versionForElement = actualVersion;

            // Create property if needed
            if (versionSource == AlignOptions.VersionSource.PROPERTY) {
                String propName = resolvePropertyName(coords, naming, options);
                upsertVersionProperty(propName, actualVersion);
                versionForElement = "${" + propName + "}";
            }

            // Add to dependencyManagement if managed style
            if (versionStyle == AlignOptions.VersionStyle.MANAGED) {
                ensureManagedDependency(
                        coords.groupId(), coords.artifactId(), versionForElement, coords.classifier(), coords.type());
                // Add version-less dependency
                if (deps == null) {
                    deps = insertMavenElement(root(), DEPENDENCIES);
                }
                Element dep = addDependency(deps, coords.groupId(), coords.artifactId(), null);
                addOptionalDependencyElements(dep, coords, options);
            } else {
                // Add with inline version
                if (deps == null) {
                    deps = insertMavenElement(root(), DEPENDENCIES);
                }
                Element dep = addDependency(deps, coords.groupId(), coords.artifactId(), versionForElement);
                addOptionalDependencyElements(dep, coords, options);
            }

            return true;
        }

        /**
         * Aligns the specified dependency to the project's detected dependency/version conventions.
         *
         * <p>Applies the project's inferred version style and source (property vs literal) to the
         * dependency's version handling. Only dependencies that currently have a `<version>` element
         * may be modified; dependencies without a `<version>` are left unchanged.</p>
         *
         * @param coords the dependency coordinates matched by groupId and artifactId
         * @return `true` if the dependency was modified; `false` if the dependency was not found or no change was necessary
         * @since 1.1.0
         */
        public boolean alignDependency(Coordinates coords) {
            return alignDependency(coords, AlignOptions.defaults());
        }

        /**
         * Aligns an existing dependency to match the specified options, auto-detecting any
         * unspecified conventions.
         *
         * <p>This method transforms the version management of an existing dependency to match
         * the target style. The following transformations may be applied:</p>
         * <ul>
         *   <li><b>Literal → Property</b>: creates a version property and replaces the literal
         *       version with a {@code ${property}} reference</li>
         *   <li><b>Inline → Managed</b>: moves the version to {@code dependencyManagement} and
         *       removes the version element from the dependency</li>
         * </ul>
         *
         * <p>Dependencies that are already version-less (managed) are not modified.</p>
         *
         * <p>Example usage:</p>
         * <pre>{@code
         * // Align a specific dependency to use managed + property style
         * editor.dependencies().alignDependency(
         *     Coordinates.of("com.google.guava", "guava"),
         *     AlignOptions.builder()
         *         .versionStyle(AlignOptions.VersionStyle.MANAGED)
         *         .versionSource(AlignOptions.VersionSource.PROPERTY)
         *         .build());
         * }</pre>
         *
         * @param coords the dependency coordinates (matched by groupId and artifactId)
         * @param options alignment options (null fields are auto-detected from the POM)
         * @return true if the dependency was modified, false if not found or no change was needed
         * @see #alignDependency(Coordinates)
         * @see #alignAllDependencies(AlignOptions)
         * @since 1.1.0
         */
        public boolean alignDependency(Coordinates coords, AlignOptions options) {
            requireGA(DEPENDENCY_LABEL, coords);
            Element deps = findChildElement(root(), DEPENDENCIES);
            if (deps == null) {
                return false;
            }

            Element dep = findDependencyElement(deps, coords);
            if (dep == null) {
                return false;
            }

            Object[] conventions = resolveConventions(options);
            AlignOptions.VersionStyle versionStyle = (AlignOptions.VersionStyle) conventions[0];
            AlignOptions.VersionSource versionSource = (AlignOptions.VersionSource) conventions[1];
            AlignOptions.PropertyNamingConvention naming = (AlignOptions.PropertyNamingConvention) conventions[2];

            return alignDependencyElement(dep, coords, versionStyle, versionSource, naming, options);
        }

        /**
         * Aligns all existing dependencies to match the project's auto-detected conventions.
         *
         * <p>Equivalent to calling {@code alignAllDependencies(AlignOptions.defaults())}.</p>
         *
         * @return the number of dependencies that were modified
         * @see #alignAllDependencies(AlignOptions)
         * @see #alignDependency(Coordinates)
         * @since 1.1.0
         */
        public int alignAllDependencies() {
            return alignAllDependencies(AlignOptions.defaults());
        }

        /**
         * Aligns all existing dependencies to match the specified options.
         *
         * <p>Conventions are detected once before aligning, then applied consistently
         * to all dependencies. This avoids convention drift that could occur if conventions
         * were re-detected after each individual alignment.</p>
         *
         * <p>Example usage:</p>
         * <pre>{@code
         * // Align all dependencies to use managed + property style
         * int changed = editor.dependencies().alignAllDependencies(
         *     AlignOptions.builder()
         *         .versionStyle(AlignOptions.VersionStyle.MANAGED)
         *         .versionSource(AlignOptions.VersionSource.PROPERTY)
         *         .build());
         * System.out.println(changed + " dependencies aligned");
         * }</pre>
         *
         * @param options alignment options (null fields are auto-detected from the POM)
         * @return the number of dependencies that were modified
         * @see #alignAllDependencies()
         * @see #alignDependency(Coordinates, AlignOptions)
         * @since 1.1.0
         */
        public int alignAllDependencies(AlignOptions options) {
            Element deps = findChildElement(root(), DEPENDENCIES);
            if (deps == null) {
                return 0;
            }

            Object[] conventions = resolveConventions(options);
            AlignOptions.VersionStyle versionStyle = (AlignOptions.VersionStyle) conventions[0];
            AlignOptions.VersionSource versionSource = (AlignOptions.VersionSource) conventions[1];
            AlignOptions.PropertyNamingConvention naming = (AlignOptions.PropertyNamingConvention) conventions[2];

            List<Element> depList = deps.childElements(DEPENDENCY).collect(Collectors.toList());
            int count = 0;
            for (Element dep : depList) {
                String groupId = dep.childTextOr(GROUP_ID, null);
                String artifactId = dep.childTextOr(ARTIFACT_ID, null);
                if (artifactId == null) {
                    continue;
                }
                String type = dep.childTextOr(TYPE, "jar");
                String classifier = dep.childTextOr(CLASSIFIER, null);
                Coordinates depCoords = Coordinates.of(groupId, artifactId, null, classifier, type);
                if (alignDependencyElement(dep, depCoords, versionStyle, versionSource, naming, options)) {
                    count++;
                }
            }
            return count;
        }

        /**
         * Aligns a single dependency element's version representation to the specified style and source.
         *
         * Updates the dependency element or project state when converting a literal version into a property
         * reference (creates or updates the corresponding property) and/or moving an inline version into
         * dependencyManagement (ensures the managed entry exists and removes the dependency's `<version>`).
         *
         * @param dep the `<dependency>` element to align
         * @param coords coordinates identifying the dependency (groupId/artifactId/type/classifier as used)
         * @param targetStyle desired version placement strategy (`INLINE` or `MANAGED`)
         * @param targetSource desired version value source (`LITERAL` or `PROPERTY`)
         * @param naming property naming convention to use when creating a version property
         * @param options alignment options that may supply explicit property names or generators
         * @return `true` if any change was made to the dependency or project (property upserted, version replaced, or version element removed), `false` if the dependency was already aligned (no `<version>` present or no conversion needed)
         */
        private boolean alignDependencyElement(
                Element dep,
                Coordinates coords,
                AlignOptions.VersionStyle targetStyle,
                AlignOptions.VersionSource targetSource,
                AlignOptions.PropertyNamingConvention naming,
                AlignOptions options) {
            java.util.Optional<Element> versionEl = dep.childElement(VERSION);
            if (!versionEl.isPresent()) {
                return false; // Already version-less (managed)
            }

            String versionText = versionEl.get().textContent();
            boolean changed = false;

            // Convert literal → property or re-align existing property reference if needed
            if (targetSource == AlignOptions.VersionSource.PROPERTY) {
                String desiredPropName = resolvePropertyName(coords, naming, options);
                String aligned = alignVersionToProperty(versionEl.get(), versionText, desiredPropName);
                if (aligned != null) {
                    versionText = aligned;
                    changed = true;
                }
            }

            // Convert inline → managed if needed
            if (targetStyle == AlignOptions.VersionStyle.MANAGED) {
                ensureManagedDependency(
                        coords.groupId(), coords.artifactId(), versionText, coords.classifier(), coords.type());
                removeElement(versionEl.get());
                changed = true;
            }

            return changed;
        }

        /**
         * Aligns a version element to use the desired property reference.
         *
         * <p>If the version is a literal value, creates the property and updates the element
         * to reference it. If it already references a different property, copies the value
         * to the desired property name and updates the reference.</p>
         *
         * @param versionEl the {@code <version>} element to update
         * @param versionText the current text content of the version element
         * @param desiredPropName the property name that should be referenced
         * @return the new version text (e.g. {@code "${prop.version}"}) if a change was made, or {@code null} if already aligned
         * @since 1.1.0
         */
        private String alignVersionToProperty(Element versionEl, String versionText, String desiredPropName) {
            if (!isPropertyReference(versionText)) {
                // Literal → property
                upsertVersionProperty(desiredPropName, versionText);
                String ref = "${" + desiredPropName + "}";
                versionEl.textContent(ref);
                return ref;
            }
            // Already a property reference — re-align if the name doesn't match
            String currentPropName = versionText.substring(2, versionText.length() - 1);
            if (currentPropName.equals(desiredPropName)) {
                return null; // Already aligned
            }
            // Only re-align if the property is defined locally — if it's inherited from a
            // parent POM we can't resolve the value, so leave the reference unchanged.
            Element props = root().childElement(PROPERTIES).orElse(null);
            if (props == null) {
                return null;
            }
            String currentValue = props.childTextOr(currentPropName, null);
            if (currentValue == null) {
                return null;
            }
            upsertVersionProperty(desiredPropName, currentValue);
            String ref = "${" + desiredPropName + "}";
            versionEl.textContent(ref);
            return ref;
        }

        /**
         * Ensure a managed dependency with the given coordinates exists under
         * `dependencyManagement/dependencies`, creating or updating the entry as needed.
         *
         * If the managed dependency is missing this inserts it (including `version` when non-null)
         * and adds `type` when non-`jar` and `classifier` when non-null. If the managed dependency
         * already exists its `<version>` child is overwritten or added when `version` is non-null.
         *
         * @param groupId    the dependency groupId
         * @param artifactId the dependency artifactId
         * @param version    the dependency version (may be null)
         * @param classifier the dependency classifier (may be null)
         * @param type       the dependency type/packaging (may be null; `"jar"` is treated as default)
         */
        private void ensureManagedDependency(
                String groupId, String artifactId, String version, String classifier, String type) {
            Element root = root();
            Element depMgmt = findChildElement(root, DEPENDENCY_MANAGEMENT);
            if (depMgmt == null) {
                depMgmt = insertMavenElement(root, DEPENDENCY_MANAGEMENT);
            }
            Element managedDeps = findChildElement(depMgmt, DEPENDENCIES);
            if (managedDeps == null) {
                managedDeps = insertMavenElement(depMgmt, DEPENDENCIES);
            }
            Coordinates lookupCoords = Coordinates.of(groupId, artifactId, version, classifier, type);
            Element managedDep = managedDeps
                    .childElements(DEPENDENCY)
                    .filter(lookupCoords.predicateGATC())
                    .findFirst()
                    .orElse(null);
            if (managedDep == null) {
                managedDep = addDependency(managedDeps, groupId, artifactId, version);
                if (type != null && !"jar".equals(type)) {
                    insertMavenElement(managedDep, TYPE, type);
                }
                if (classifier != null) {
                    insertMavenElement(managedDep, CLASSIFIER, classifier);
                }
            } else {
                java.util.Optional<Element> versionEl = managedDep.childElement(VERSION);
                if (versionEl.isPresent()) {
                    versionEl.get().textContent(version);
                } else if (version != null) {
                    insertMavenElement(managedDep, VERSION, version);
                }
            }
        }

        /**
         * Insert optional dependency sub-elements into the given dependency element when specified.
         *
         * Inserts a `<type>` element if the coordinate's type is non-null and not "jar",
         * a `<classifier>` element if the coordinate's classifier is non-null, and
         * a `<scope>` element if the provided align options include a non-null scope.
         *
         * @param dep the `<dependency>` element to modify
         * @param coords the dependency coordinates providing `type` and `classifier`
         * @param options alignment options providing an optional `scope`
         */
        private void addOptionalDependencyElements(Element dep, Coordinates coords, AlignOptions options) {
            if (coords.type() != null && !"jar".equals(coords.type())) {
                insertMavenElement(dep, TYPE, coords.type());
            }
            if (coords.classifier() != null) {
                insertMavenElement(dep, CLASSIFIER, coords.classifier());
            }
            if (options.scope() != null) {
                insertMavenElement(dep, SCOPE, options.scope());
            }
        }

        /**
         * Count dependency elements that contain a `version` child.
         *
         * @param dependencies the `<dependencies>` container element to inspect
         * @return the number of `<dependency>` children that have a `<version>` child element
         */
        private long countVersionedDependencies(Element dependencies) {
            return dependencies
                    .childElements(DEPENDENCY)
                    .filter(dep -> dep.childElement(VERSION).isPresent())
                    .count();
        }

        /**
         * Counts dependency entries whose <version> value is a property reference (for example `${...}`).
         *
         * @param dependencies the `<dependencies>` container element to inspect
         * @return the number of dependency elements whose `<version>` text is a property reference
         */
        private long countPropertyVersionedDependencies(Element dependencies) {
            return dependencies
                    .childElements(DEPENDENCY)
                    .map(dep -> dep.childElement(VERSION).orElse(null))
                    .filter(v -> v != null && isPropertyReference(v.textContent()))
                    .count();
        }

        /**
         * Tally property-naming convention votes from dependency version property references into the provided map.
         *
         * Scans each `<dependency>` child of the given `dependencies` element; for any `<version>` whose text is a
         * property reference of the form `${...}`, extracts the property name, classifies its naming convention, and
         * increments the corresponding count in `votes` when a convention is recognized. If `dependencies` is null,
         * the method returns without modifying `votes`.
         *
         * @param dependencies the `<dependencies>` element containing `<dependency>` children (may be null)
         * @param votes        map that will be updated: keys are detected conventions and values are their vote counts
         */
        private void collectPropertyConventionVotes(
                Element dependencies, Map<AlignOptions.PropertyNamingConvention, Integer> votes) {
            if (dependencies == null) {
                return;
            }
            dependencies.childElements(DEPENDENCY).forEach(dep -> dep.childElement(VERSION)
                    .ifPresent(version -> {
                        String v = version.textContent();
                        if (isPropertyReference(v)) {
                            String propName = v.substring(2, v.length() - 1);
                            AlignOptions.PropertyNamingConvention conv = classifyPropertyName(propName);
                            if (conv != null) {
                                votes.merge(conv, 1, Integer::sum);
                            }
                        }
                    }));
        }

        /**
         * Determines the property-naming convention of a version property name.
         *
         * @param propName the property name to classify (e.g. "project.version", "artifact-version", "myVersion")
         * @return the matching PropertyNamingConvention, or `null` if the name does not match any known convention
         */
        private AlignOptions.PropertyNamingConvention classifyPropertyName(String propName) {
            if (propName.endsWith(".version")) {
                return AlignOptions.PropertyNamingConvention.DOT_SUFFIX;
            } else if (propName.endsWith("-version")) {
                return AlignOptions.PropertyNamingConvention.DASH_SUFFIX;
            } else if (propName.endsWith(VERSION_SUFFIX)
                    && propName.length() > VERSION_SUFFIX.length()
                    && Character.isLowerCase(propName.charAt(0))) {
                return AlignOptions.PropertyNamingConvention.CAMEL_CASE;
            } else if (propName.startsWith("version.")) {
                return AlignOptions.PropertyNamingConvention.DOT_PREFIX;
            }
            return null;
        }

        /**
         * Resolves the effective conventions by filling in {@code null} fields from the given options
         * with auto-detected values from the current POM.
         *
         * @param options alignment options (fields may be {@code null} to indicate auto-detection)
         * @return a three-element array: {@code [VersionStyle, VersionSource, PropertyNamingConvention]}
         * @since 1.1.0
         */
        private Object[] resolveConventions(AlignOptions options) {
            AlignOptions.VersionStyle versionStyle =
                    options.versionStyle() != null ? options.versionStyle() : detectVersionStyle();
            AlignOptions.VersionSource versionSource =
                    options.versionSource() != null ? options.versionSource() : detectVersionSource();
            AlignOptions.PropertyNamingConvention naming =
                    options.namingConvention() != null ? options.namingConvention() : detectPropertyNamingConvention();
            return new Object[] {versionStyle, versionSource, naming};
        }

        /**
         * Resolve the effective property name to use for the given dependency coordinates.
         *
         * @param coords  dependency coordinates used when generating a name if none is supplied
         * @param naming  naming convention to apply when a name is generated
         * @param options alignment options that may supply an explicit property name or a generator
         * @return        the resolved property name; preferring an explicit name from {@code options}, then a generated name from {@code options.propertyNameGenerator()}, and finally a name produced via {@link AlignOptions#generatePropertyName(Coordinates, AlignOptions.PropertyNamingConvention)}
         * @since 1.1.0
         */
        private String resolvePropertyName(
                Coordinates coords, AlignOptions.PropertyNamingConvention naming, AlignOptions options) {
            if (options.propertyName() != null) {
                return options.propertyName();
            }
            if (options.propertyNameGenerator() != null) {
                return options.propertyNameGenerator().apply(coords);
            }
            return AlignOptions.generatePropertyName(coords, naming);
        }

        /**
         * Ensure a version-like property with the given key exists and set its value.
         *
         * <p>If the project's {@code <properties>} container is missing it will be created. If a property
         * element with the given key already exists its text will be overwritten; otherwise a new property
         * element will be inserted. New properties are placed alphabetically (case-insensitive) among
         * existing version-like properties when any are present; if none are present the property is
         * appended.</p>
         *
         * @param key   the property name to create or update (e.g. {@code "my.artifact.version"})
         * @param value the value to assign to the property
         * @since 1.1.0
         */
        private void upsertVersionProperty(String key, String value) {
            Element properties = root().childElement(PROPERTIES).orElse(null);
            if (properties == null) {
                properties = insertMavenElement(root(), PROPERTIES);
            }
            Element existing = properties.childElement(key).orElse(null);
            if (existing != null) {
                existing.textContent(value);
                return;
            }
            // Find existing version properties and insert alphabetically among them
            Element lastVersionProp = null;
            Element insertBefore = null;
            for (java.util.Iterator<Element> it = properties.childElements().iterator(); it.hasNext(); ) {
                Element el = it.next();
                if (isVersionProperty(el.name())) {
                    if (el.name().compareToIgnoreCase(key) > 0 && insertBefore == null) {
                        insertBefore = el;
                    }
                    lastVersionProp = el;
                }
            }
            Element prop;
            if (insertBefore != null) {
                prop = insertElementBefore(insertBefore, key);
            } else if (lastVersionProp != null) {
                prop = insertElementAfter(lastVersionProp, key);
            } else {
                prop = addElement(properties, key);
            }
            prop.textContent(value);
        }

        /**
         * Detects whether a property name follows common "version-like" naming patterns.
         *
         * <p>Patterns considered: ends with ".version" or "-version", starts with "version.",
         * or ends with "Version" while the first character is lowercase (e.g., "artifactVersion").</p>
         *
         * @param name the property name to test
         * @return {@code true} if the name matches a version-like pattern, {@code false} otherwise
         * @since 1.1.0
         */
        private boolean isVersionProperty(String name) {
            return name.endsWith(".version")
                    || name.endsWith("-version")
                    || name.startsWith("version.")
                    || (name.endsWith(VERSION_SUFFIX)
                            && name.length() > VERSION_SUFFIX.length()
                            && Character.isLowerCase(name.charAt(0)));
        }

        /**
         * Checks whether the given string is a single Maven-style property reference of the form {@code ${name}}.
         *
         * <p>Returns {@code false} for compound expressions (e.g. {@code ${a}-${b}}),
         * interpolated strings (e.g. {@code ${version}-SNAPSHOT}), or null/empty values.</p>
         *
         * @param value the string to inspect
         * @return {@code true} if {@code value} is a single property reference, {@code false} otherwise
         * @since 1.1.0
         */
        private boolean isPropertyReference(String value) {
            return value != null
                    && value.startsWith("${")
                    && value.endsWith("}")
                    && value.indexOf('}') == value.length() - 1;
        }

        /**
         * Updates a version element, resolving property references relative to this Dependencies' context.
         *
         * <p>Shadows {@link PomEditor#updateVersionElement(Element, String)} so that profile-scoped
         * instances update properties under the profile's {@code <properties>} rather than the
         * project root's.</p>
         */
        private boolean updateVersionElement(Element parent, String newVersion) throws DomTripException {
            java.util.Optional<Element> version = parent.childElement(VERSION);
            if (version.isPresent()) {
                String versionValue = version.get().textContent();
                if (versionValue != null && versionValue.startsWith("${") && versionValue.endsWith("}")) {
                    String propertyKey = versionValue.substring(2, versionValue.length() - 1);
                    Element properties = root().childElement(PROPERTIES).orElse(null);
                    if (properties != null) {
                        Element property = properties.childElement(propertyKey).orElse(null);
                        if (property != null) {
                            property.textContent(newVersion);
                            return true;
                        }
                    }
                    return false;
                } else {
                    version.get().textContent(newVersion);
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Create a helper for managing regular and managed Maven dependencies within this POM.
     *
     * Provides high-level operations for adding, updating, deleting, aligning, and inspecting dependencies,
     * including support for exclusions, dependencyManagement, and convention detection.
     *
     * @return a Dependencies helper bound to this PomEditor instance
     */
    public Dependencies dependencies() {
        return new Dependencies();
    }

    public class Plugins {
        /**
         * Adds a plugin element with the specified coordinates.
         *
         * @param pluginsElement the plugins container element
         * @param groupId        the plugin groupId
         * @param artifactId     the plugin artifactId
         * @param version        the plugin version (can be null)
         * @return the newly created plugin element
         * @throws DomTripException if the plugin cannot be added
         */
        public Element addPlugin(Element pluginsElement, String groupId, String artifactId, String version)
                throws DomTripException {
            Element plugin = insertMavenElement(pluginsElement, PLUGIN);
            if (groupId != null && !groupId.trim().isEmpty()) {
                insertMavenElement(plugin, GROUP_ID, groupId);
            }
            insertMavenElement(plugin, ARTIFACT_ID, artifactId);
            if (version != null && !version.trim().isEmpty()) {
                insertMavenElement(plugin, VERSION, version);
            }
            return plugin;
        }

        /**
         * Finds or creates the managed plugins container element.
         *
         * @param upsert whether to create the structure if it doesn't exist
         * @return the plugins element, or null if not found and upsert is false
         * @throws DomTripException if an error occurs during creation
         */
        private Element findOrCreateManagedPlugins(boolean upsert) throws DomTripException {
            Element root = root();
            Element build = findChildElement(root, BUILD);
            if (build == null && upsert) {
                build = insertMavenElement(root, BUILD);
            }
            if (build != null) {
                Element pluginManagement = findChildElement(build, PLUGIN_MANAGEMENT);
                if (pluginManagement == null && upsert) {
                    pluginManagement = insertMavenElement(build, PLUGIN_MANAGEMENT);
                }
                if (pluginManagement != null) {
                    Element plugins = findChildElement(pluginManagement, PLUGINS);
                    if (plugins == null && upsert) {
                        plugins = insertMavenElement(pluginManagement, PLUGINS);
                    }
                    return plugins;
                }
            }
            return null;
        }

        /**
         * Finds or creates the plugins container element.
         *
         * @param upsert whether to create the structure if it doesn't exist
         * @return the plugins element, or null if not found and upsert is false
         * @throws DomTripException if an error occurs during creation
         */
        private Element findOrCreatePlugins(boolean upsert) throws DomTripException {
            Element build = findChildElement(root(), BUILD);
            if (build == null && upsert) {
                build = insertMavenElement(root(), BUILD);
            }
            if (build != null) {
                Element plugins = findChildElement(build, PLUGINS);
                if (plugins == null && upsert) {
                    plugins = insertMavenElement(build, PLUGINS);
                }
                return plugins;
            }
            return null;
        }

        /**
         * Finds a plugin element by artifact coordinates.
         *
         * @param plugins the plugins container element
         * @param coordinates the artifact to find
         * @return the plugin element, or null if not found
         */
        private Element findPlugin(Element plugins, Coordinates coordinates) {
            if (plugins == null) {
                return null;
            }
            return plugins.childElements(PLUGIN)
                    .filter(coordinates.predicateGA())
                    .findFirst()
                    .orElse(null);
        }

        /**
         * Removes a managed plugin from {@code project/build/pluginManagement/plugins/plugin[]}.
         *
         * <h4>Example:</h4>
         * <pre>{@code
         * PomEditor editor = new PomEditor(document);
         * Coordinates compilerPlugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");
         * editor.deleteManagedPlugin(compilerPlugin);
         * }</pre>
         *
         * @param coordinates the artifact to remove (matched by GA)
         * @return true if the plugin was removed, false if it didn't exist
         * @since 0.3.0
         */
        public boolean deleteManagedPlugin(Coordinates coordinates) throws DomTripException {
            Element plugins = findOrCreateManagedPlugins(false); // upsert=false; will not throw
            Element plugin = findPlugin(plugins, coordinates);
            if (plugin != null) {
                return removeElement(plugin);
            }
            return false;
        }

        /**
         * Updates or inserts a plugin in {@code project/build/plugins/plugin[]}.
         *
         * <p>If the plugin exists (matched by GA), its version is updated. If the version is a property
         * reference (${...}), the property value is updated instead. If the plugin has no version element,
         * the managed plugin is updated instead. If {@code upsert} is true and the plugin doesn't exist,
         * it will be created.</p>
         *
         * <h4>Example:</h4>
         * <pre>{@code
         * PomEditor editor = new PomEditor(document);
         * Coordinates compilerPlugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");
         * editor.updatePlugin(true, compilerPlugin);
         * }</pre>
         *
         * @param upsert whether to create the plugin if it doesn't exist
         * @param coordinates the artifact coordinates
         * @return true if the plugin was updated or created, false otherwise
         * @throws DomTripException if an error occurs during editing
         * @since 0.3.0
         */
        public boolean updatePlugin(boolean upsert, Coordinates coordinates) throws DomTripException {
            Element plugins = findOrCreatePlugins(upsert);
            if (plugins != null) {
                Element plugin = findPlugin(plugins, coordinates);
                if (plugin == null && upsert) {
                    addPlugin(plugins, coordinates.groupId(), coordinates.artifactId(), coordinates.version());
                    return true;
                }
                if (plugin != null) {
                    java.util.Optional<Element> version = plugin.childElement(VERSION);
                    if (version.isPresent()) {
                        return updateVersionElement(plugin, coordinates.version());
                    } else {
                        return updateManagedPlugin(false, coordinates);
                    }
                }
            }
            return false;
        }

        /**
         * Removes a plugin from {@code project/build/plugins/plugin[]}.
         *
         * <h4>Example:</h4>
         * <pre>{@code
         * PomEditor editor = new PomEditor(document);
         * Coordinates compilerPlugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");
         * editor.deletePlugin(compilerPlugin);
         * }</pre>
         *
         * @param coordinates the artifact to remove (matched by GA)
         * @return true if the plugin was removed, false if it didn't exist
         * @since 0.3.0
         */
        public boolean deletePlugin(Coordinates coordinates) throws DomTripException {
            Element plugins = findOrCreatePlugins(false); // upsert=false; will not throw
            Element plugin = findPlugin(plugins, coordinates);
            if (plugin != null) {
                return removeElement(plugin);
            }
            return false;
        }

        /**
         * Removes a plugin version element from {@code project/build/plugins/plugin[]}. This is usually combined with
         * adding plugin management for same plugin.
         *
         * <h4>Example:</h4>
         * <pre>{@code
         * PomEditor editor = new PomEditor(document);
         * Coordinates compilerPlugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");
         * editor.deletePluginVersion(compilerPlugin);
         * editor.updateManagedPlugin(true, compilerPlugin);
         * }</pre>
         *
         * @param coordinates the artifact to remove (matched by GA)
         * @return true if the plugin version was removed, false if it didn't exist
         * @since 0.3.1
         */
        public boolean deletePluginVersion(Coordinates coordinates) throws DomTripException {
            Element plugins = findOrCreatePlugins(false); // upsert=false; will not throw
            Element plugin = findPlugin(plugins, coordinates);
            if (plugin != null) {
                return plugin.childElement(VERSION).filter(plugin::removeChild).isPresent();
            }
            return false;
        }

        /**
         * Updates or inserts a managed plugin in {@code project/build/pluginManagement/plugins/plugin[]}.
         *
         * <p>If the plugin exists (matched by GA), its version is updated. If the version is a property
         * reference (${...}), the property value is updated instead. If {@code upsert} is true and the plugin
         * doesn't exist, it will be created.</p>
         *
         * <h4>Example:</h4>
         * <pre>{@code
         * PomEditor editor = new PomEditor(document);
         * Coordinates compilerPlugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");
         * editor.updateManagedPlugin(true, compilerPlugin);
         * }</pre>
         *
         * @param upsert whether to create the plugin if it doesn't exist
         * @param coordinates the artifact coordinates
         * @return true if the plugin was updated or created, false otherwise
         * @throws DomTripException if an error occurs during editing
         * @since 0.3.0
         */
        public boolean updateManagedPlugin(boolean upsert, Coordinates coordinates) throws DomTripException {
            Element plugins = findOrCreateManagedPlugins(upsert);
            if (plugins != null) {
                Element plugin = findPlugin(plugins, coordinates);
                if (plugin == null && upsert) {
                    addPlugin(plugins, coordinates.groupId(), coordinates.artifactId(), coordinates.version());
                    return true;
                }
                if (plugin != null) {
                    return updateVersionElement(plugin, coordinates.version());
                }
            }
            return false;
        }
    }

    public Plugins plugins() {
        return new Plugins();
    }

    public class Extensions {
        /**
         * Adds a extension element with the specified coordinates.
         *
         * @param extensionsElement the extensions container element
         * @param groupId           the extension groupId
         * @param artifactId        the extension artifactId
         * @param version           the extension version
         * @return the newly created extension element
         * @throws DomTripException if the plugin cannot be added
         */
        public Element addExtension(Element extensionsElement, String groupId, String artifactId, String version)
                throws DomTripException {
            Element extension = insertMavenElement(extensionsElement, EXTENSION);
            insertMavenElement(extension, GROUP_ID, groupId);
            insertMavenElement(extension, ARTIFACT_ID, artifactId);
            insertMavenElement(extension, VERSION, version);
            return extension;
        }

        /**
         * Finds or creates the extensions container element.
         *
         * @param upsert whether to create the structure if it doesn't exist
         * @return the extensions element, or null if not found and upsert is false
         * @throws DomTripException if an error occurs during creation
         */
        private Element findOrCreateExtensions(boolean upsert) throws DomTripException {
            Element build = findChildElement(root(), BUILD);
            if (build == null && upsert) {
                build = insertMavenElement(root(), BUILD);
            }
            if (build != null) {
                Element extensions = findChildElement(build, EXTENSIONS);
                if (extensions == null && upsert) {
                    extensions = insertMavenElement(build, EXTENSIONS);
                }
                return extensions;
            }
            return null;
        }

        /**
         * Finds an extension element by artifact coordinates.
         *
         * @param extensions the extensions container element
         * @param coordinates the artifact to find
         * @return the extension element, or null if not found
         */
        private Element findExtension(Element extensions, Coordinates coordinates) {
            if (extensions == null) {
                return null;
            }
            return extensions
                    .childElements(EXTENSION)
                    .filter(coordinates.predicateGA())
                    .findFirst()
                    .orElse(null);
        }

        /**
         * Updates or inserts an extension in {@code project/build/extensions/extension[]}.
         *
         * <p>If the extension exists (matched by GA), its version is updated. If the version is a property
         * reference (${...}), the property value is updated instead. If {@code upsert} is true and the plugin doesn't exist,
         * it will be created.</p>
         *
         * <h4>Example:</h4>
         * <pre>{@code
         * PomEditor editor = new PomEditor(document);
         * Coordinates myExtension = Coordinates.of("org.apache.maven.extensions", "some-extension", "1.11.0");
         * editor.updateExtension(true, myExtension);
         * }</pre>
         *
         * @param upsert whether to create the extension if it doesn't exist
         * @param coordinates the artifact coordinates
         * @return true if the extension was updated or created, false otherwise
         * @throws DomTripException if an error occurs during editing
         * @since 0.3.1
         */
        public boolean updateExtension(boolean upsert, Coordinates coordinates) throws DomTripException {
            Element extensions = findOrCreateExtensions(upsert);
            if (extensions != null) {
                Element extension = findExtension(extensions, coordinates);
                if (extension == null && upsert) {
                    addExtension(extensions, coordinates.groupId(), coordinates.artifactId(), coordinates.version());
                    return true;
                }
                if (extension != null) {
                    java.util.Optional<Element> version = extension.childElement(VERSION);
                    if (version.isPresent()) {
                        return updateVersionElement(extension, coordinates.version());
                    }
                }
            }
            return false;
        }

        /**
         * Removes an extension from {@code project/build/extensions/extension[]}.
         *
         * <h4>Example:</h4>
         * <pre>{@code
         * PomEditor editor = new PomEditor(document);
         * Coordinates myExtension = Coordinates.of("org.apache.maven.extension", "my-extension", "1.11.0");
         * editor.deleteExtension(myExtension);
         * }</pre>
         *
         * @param coordinates the artifact to remove (matched by GA)
         * @return true if the extension was removed, false if it didn't exist
         * @since 0.3.1
         */
        public boolean deleteExtension(Coordinates coordinates) throws DomTripException {
            Element extensions = findOrCreateExtensions(false); // upsert=false; will not throw
            Element extension = findExtension(extensions, coordinates);
            if (extension != null) {
                return removeElement(extension);
            }
            return false;
        }
    }

    public Extensions extensions() {
        return new Extensions();
    }

    public class Subprojects {

        /**
         * Adds a module to the modules section.
         *
         * @param modulesElement the modules container element
         * @param moduleName     the name of the module
         * @return the newly created module element
         * @throws DomTripException if the module cannot be added
         */
        public Element addModule(Element modulesElement, String moduleName) throws DomTripException {
            return insertMavenElement(modulesElement, MODULE, moduleName);
        }

        /**
         * Adds a module entry to {@code project/modules/module[]}, if not already present.
         *
         * @param moduleName the module name/path
         * @return true if the module was added, false if it already existed
         * @throws DomTripException if an error occurs during editing
         */
        public boolean addSubProject(String moduleName) throws DomTripException {
            Element modules = findChildElement(root(), MODULES);
            if (modules == null) {
                modules = insertMavenElement(root(), MODULES);
            }
            List<String> existing =
                    modules.childElements(MODULE).map(Element::textContent).collect(Collectors.toList());
            if (!existing.contains(moduleName)) {
                insertMavenElement(modules, MODULE, moduleName);
                return true;
            }
            return false;
        }

        /**
         * Removes a module entry from {@code project/modules/module[]}, if present.
         *
         * @param moduleName the module name/path
         * @return true if the module was removed, false if it didn't exist
         */
        public boolean removeSubProject(String moduleName) throws DomTripException {
            Element modules = findChildElement(root(), MODULES);
            if (modules == null) {
                return false;
            }
            AtomicBoolean removed = new AtomicBoolean(false);
            modules.childElements(MODULE)
                    .filter(e -> Objects.equals(moduleName, e.textContent()))
                    .peek(e -> removed.set(true))
                    .forEach(PomEditor.this::removeElement);
            return removed.get();
        }
    }

    public Subprojects subprojects() {
        return new Subprojects();
    }

    public class Properties {

        /**
         * Adds a property to the properties section.
         *
         * @param propertiesElement the properties container element
         * @param propertyName      the name of the property
         * @param propertyValue     the value of the property
         * @return the newly created property element
         * @throws DomTripException if the property cannot be added
         */
        public Element addProperty(Element propertiesElement, String propertyName, String propertyValue)
                throws DomTripException {
            return addElement(propertiesElement, propertyName, propertyValue);
        }

        /**
         * Updates or inserts a property value in {@code project/properties/key}.
         *
         * <p>If the property already exists, its value is updated. If {@code upsert} is true
         * and the property doesn't exist, it will be created (along with the properties element if needed).</p>
         *
         * @param upsert whether to create the property if it doesn't exist
         * @param key    the property name
         * @param value  the property value
         * @return true if the property was updated or created, false otherwise
         * @throws DomTripException if an error occurs during editing
         */
        public boolean updateProperty(boolean upsert, String key, String value) throws DomTripException {
            Element properties = root().childElement(PROPERTIES).orElse(null);
            if (properties == null && upsert) {
                properties = insertMavenElement(root(), PROPERTIES);
            }
            if (properties != null) {
                Element property = properties.childElement(key).orElse(null);
                if (property == null && upsert) {
                    property = addElement(properties, key);
                }
                if (property != null) {
                    property.textContent(value);
                    return true;
                }
            }
            return false;
        }

        /**
         * Removes a property from {@code project/properties/key}.
         *
         * @param key the property name
         * @return true if the property was removed, false if it didn't exist
         */
        public boolean deleteProperty(String key) throws DomTripException {
            Element properties = root().childElement(PROPERTIES).orElse(null);
            if (properties != null) {
                Element property = properties.childElement(key).orElse(null);
                if (property != null) {
                    removeElement(property);
                    return true;
                }
            }
            return false;
        }
    }

    public Properties properties() {
        return new Properties();
    }

    public class Parent {
        /**
         * Sets {@code project/parent} to the given artifact coordinates.
         *
         * @param coordinates the parent artifact (groupId, artifactId, version)
         * @throws DomTripException if an error occurs during editing
         */
        public void setParent(Coordinates coordinates) throws DomTripException {
            Element parent = findChildElement(root(), PARENT);
            if (parent == null) {
                parent = insertMavenElement(root(), PARENT);
            }
            insertMavenElement(parent, GROUP_ID, coordinates.groupId());
            insertMavenElement(parent, ARTIFACT_ID, coordinates.artifactId());
            insertMavenElement(parent, VERSION, coordinates.version());
        }

        /**
         * Updates/insert parent. It goes for {@code project/parent} and rewrites it according to passed in coordinates.
         *
         * @param upsert whether to create the parent if it doesn't exist
         * @param coordinates the parent coordinates
         * @return true if the parent was updated or created, false otherwise
         * @throws DomTripException if an error occurs during editing
         * @since 0.3.1
         */
        public boolean updateParent(boolean upsert, Coordinates coordinates) throws DomTripException {
            Element parent = findChildElement(root(), MavenPomElements.Elements.PARENT);
            if (parent == null && upsert) {
                parent = insertMavenElement(root(), MavenPomElements.Elements.PARENT);
            }
            if (parent != null) {
                if (coordinates.groupId() != null
                        && !coordinates.groupId().trim().isEmpty()) {
                    insertMavenElement(parent, MavenPomElements.Elements.GROUP_ID, coordinates.groupId());
                }
                insertMavenElement(parent, MavenPomElements.Elements.ARTIFACT_ID, coordinates.artifactId());
                if (coordinates.version() != null
                        && !coordinates.version().trim().isEmpty()) {
                    insertMavenElement(parent, MavenPomElements.Elements.VERSION, coordinates.version());
                }
                return true;
            }
            return false;
        }

        /**
         * Removes parent.  It goes for {@code project/parent} and removes entry if present.
         *
         * @return true if the parent was updated or created, false otherwise
         * @since 0.3.1
         */
        public boolean deleteParent() throws DomTripException {
            Element parent = findChildElement(root(), MavenPomElements.Elements.PARENT);
            return parent != null && removeElement(parent);
        }
    }

    public Parent parent() {
        return new Parent();
    }

    /**
     * Helper for inspecting Maven profiles within a POM.
     *
     * <p>Provides methods to find and check the existence of {@code <profile>} elements
     * by their {@code <id>}. The returned elements can be passed to
     * {@link Dependencies#forProfile(Element)} for profile-scoped dependency operations.</p>
     *
     * @since 1.1.0
     */
    public class Profiles {

        /**
         * Finds a profile element by its {@code <id>}.
         *
         * @param id the profile id to look for
         * @return the {@code <profile>} element, or {@code null} if not found
         * @since 1.1.0
         */
        public Element findProfile(String id) {
            return findProfileElement(id);
        }

        /**
         * Checks whether a profile with the given {@code <id>} exists.
         *
         * @param id the profile id to check
         * @return {@code true} if a matching profile exists, {@code false} otherwise
         * @since 1.1.0
         */
        public boolean hasProfile(String id) {
            return findProfile(id) != null;
        }
    }

    /**
     * Create a helper for inspecting Maven profiles within this POM.
     *
     * @return a Profiles helper bound to this PomEditor instance
     * @since 1.1.0
     */
    public Profiles profiles() {
        return new Profiles();
    }

    /**
     * Sets {@code project/version} or {@code project/parent/version} (if project version doesn't exist).
     *
     * @param value the version value
     * @throws DomTripException if no version element can be found
     */
    public void setVersion(String value) throws DomTripException {
        Element version = findChildElement(root(), VERSION);
        if (version == null) {
            Element parent = findChildElement(root(), PARENT);
            if (parent != null) {
                version = findChildElement(parent, VERSION);
            }
        }
        if (version != null) {
            version.textContent(value);
            return;
        }
        throw new DomTripException("Could not set version");
    }

    /**
     * Sets {@code project/packaging} to the given value.
     *
     * @param value the packaging value (e.g., "jar", "pom", "war")
     * @throws DomTripException if an error occurs during editing
     */
    public void setPackaging(String value) throws DomTripException {
        Element packaging = findChildElement(root(), PACKAGING);
        if (packaging == null) {
            insertMavenElement(root(), PACKAGING, value);
        } else {
            packaging.textContent(value);
        }
    }

    /**
     * Determines if a blank line should be added before the element based on ordering.
     * Only adds blank lines if there are elements before the insertion point.
     */
    private boolean needsBlankLineBefore(Element parent, List<String> order, int elementIndex) {
        // Don't add blank lines if this is the first element being inserted
        // (even if parent has children, if they come after this element in the order)

        // Check if there are any existing elements that come before this element in the order
        boolean hasElementsBefore = false;
        for (int i = 0; i < elementIndex; i++) {
            String orderElement = order.get(i);
            if (!orderElement.isEmpty() && parent.childElement(orderElement).isPresent()) {
                hasElementsBefore = true;
                break;
            }
        }

        return hasElementsBefore
                && elementIndex > 0
                && order.get(elementIndex - 1).isEmpty();
    }

    /**
     * Determines if a blank line should be added after the element based on ordering.
     * Only adds blank lines if there are elements after the insertion point.
     */
    private boolean needsBlankLineAfter(Element parent, List<String> order, int elementIndex) {
        // Don't add blank lines if there are no elements that come after this element

        // Check if there are any existing elements that come after this element in the order
        boolean hasElementsAfter = false;
        for (int i = elementIndex + 2; i < order.size(); i++) { // Skip the blank line marker at elementIndex + 1
            String orderElement = order.get(i);
            if (!orderElement.isEmpty() && parent.childElement(orderElement).isPresent()) {
                hasElementsAfter = true;
                break;
            }
        }

        return hasElementsAfter
                && elementIndex < order.size() - 1
                && order.get(elementIndex + 1).isEmpty();
    }

    // ========== CONVENIENCE UTILITY METHODS ==========

    /**
     * Checks if an element exists as a child of the given parent.
     *
     * <p>This is a convenience method that provides a simple boolean check
     * for child element existence without needing to handle Optional.</p>
     *
     * <h4>Example:</h4>
     * <pre>{@code
     * PomEditor editor = new PomEditor(document);
     * Element root = editor.root();
     * if (editor.hasChildElement(root, "properties")) {
     *     // Properties section exists
     * }
     * }</pre>
     *
     * @param parent the parent element
     * @param childName the child element name to check
     * @return true if the child element exists, false otherwise
     * @see #findChildElement(Element, String)
     * @see #getChildElementText(Element, String)
     * @since 0.3.0
     */
    public boolean hasChildElement(Element parent, String childName) {
        return parent.childElement(childName).isPresent();
    }

    /**
     * Gets the text content of a child element, or returns null if not found.
     *
     * <p>This is a convenience method that provides a simple way to get child
     * element text content with null fallback instead of handling Optional.</p>
     *
     * <h4>Example:</h4>
     * <pre>{@code
     * PomEditor editor = new PomEditor(document);
     * Element dependency = // ... get dependency element
     * String version = editor.getChildElementText(dependency, "version");
     * if (version != null) {
     *     // Process version
     * }
     * }</pre>
     *
     * @param parent the parent element
     * @param childName the child element name
     * @return the text content of the child element, or null if not found
     * @see #findChildElement(Element, String)
     * @see #hasChildElement(Element, String)
     * @see #updateOrCreateChildElement(Element, String, String)
     * @since 0.3.0
     */
    public String getChildElementText(Element parent, String childName) {
        return parent.childElement(childName).map(Element::textContent).orElse(null);
    }

    /**
     * Updates or creates a child element with the given content.
     *
     * <p>If the child element exists, updates its text content. If it doesn't exist,
     * creates it with the specified content using proper Maven element ordering.</p>
     *
     * <h4>Example:</h4>
     * <pre>{@code
     * PomEditor editor = new PomEditor(document);
     * Element root = editor.root();
     * // This will update existing description or create new one
     * editor.updateOrCreateChildElement(root, "description", "My project description");
     * }</pre>
     *
     * @param parent the parent element
     * @param childName the child element name
     * @param content the content to set
     * @return the updated or created element
     * @throws DomTripException if an error occurs during editing
     * @see #findChildElement(Element, String)
     * @see #insertMavenElement(Element, String, String)
     * @see #getChildElementText(Element, String)
     * @since 0.3.0
     */
    public Element updateOrCreateChildElement(Element parent, String childName, String content)
            throws DomTripException {
        Element child = findChildElement(parent, childName);
        if (child != null) {
            child.textContent(content);
            return child;
        } else {
            return insertMavenElement(parent, childName, content);
        }
    }

    /**
     * Finds a {@code <profile>} element by its {@code <id>}.
     *
     * @param id the profile id to look for (may be null)
     * @return the matching profile element, or null if not found or id is null
     */
    private Element findProfileElement(String id) {
        if (id == null) {
            return null;
        }
        return root().childElement(PROFILES)
                .flatMap(profiles -> profiles.childElements(PROFILE)
                        .filter(p -> p.childElement(ID)
                                .map(idEl -> id.equals(idEl.textContent()))
                                .orElse(false))
                        .findFirst())
                .orElse(null);
    }

    /**
     * Updates a version element, handling property references intelligently.
     *
     * <p>If the version element contains a property reference (${property.name}), this method
     * updates the property value instead of the version element itself.</p>
     *
     * @param parent the parent element containing the version child
     * @param newVersion the new version value
     * @return true if the version was updated, false otherwise
     * @throws DomTripException if an error occurs during editing
     */
    private boolean updateVersionElement(Element parent, String newVersion) throws DomTripException {
        java.util.Optional<Element> version = parent.childElement(VERSION);
        if (version.isPresent()) {
            String versionValue = version.orElseThrow(() -> new NoSuchElementException("No value present"))
                    .textContent();
            if (versionValue != null && versionValue.startsWith("${") && versionValue.endsWith("}")) {
                String propertyKey = versionValue.substring(2, versionValue.length() - 1);
                return properties().updateProperty(false, propertyKey, newVersion);
            } else {
                version.orElseThrow(() -> new NoSuchElementException("No value present"))
                        .textContent(newVersion);
                return true;
            }
        }
        return false;
    }
}
