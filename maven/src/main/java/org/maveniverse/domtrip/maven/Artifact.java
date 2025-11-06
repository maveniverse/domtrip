/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package org.maveniverse.domtrip.maven;

import static java.util.Objects.requireNonNull;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.Element;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Represents a Maven artifact with its coordinates (groupId, artifactId, version, classifier, and type).
 *
 * <p>This is a simple immutable record for representing Maven artifact coordinates. It provides
 * convenient factory methods and string representations commonly used in Maven.</p>
 *
 * <p><strong>Maven 4 Inference Support:</strong> With Maven 4's inference mechanism, groupId and version
 * may be inferred from the reactor or parent POM and might not be present in the build POM. This record
 * allows null values for groupId and version to support such scenarios. Only artifactId is strictly required.</p>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Create artifacts
 * Artifact jar = Artifact.of("org.junit.jupiter", "junit-jupiter", "5.9.2");
 * Artifact pom = Artifact.of("org.example", "my-project", "1.0.0", null, "pom");
 * Artifact classified = Artifact.of("org.example", "my-lib", "1.0.0", "sources", "jar");
 *
 * // Maven 4 inference - groupId/version may be null
 * Artifact inferred = Artifact.of(null, "my-module", null, null, "jar");
 *
 * // Get string representations
 * String ga = jar.toGA();           // "org.junit.jupiter:junit-jupiter"
 * String gav = jar.toGAV();         // "org.junit.jupiter:junit-jupiter:5.9.2"
 * String gatc = jar.toGATC();       // "org.junit.jupiter:junit-jupiter:jar"
 * String full = jar.toFullString(); // "org.junit.jupiter:junit-jupiter:jar:5.9.2"
 * }</pre>
 *
 * @param groupId the Maven groupId (can be null for Maven 4 inference)
 * @param artifactId the Maven artifactId (required)
 * @param version the artifact version (can be null for dependency management or Maven 4 inference)
 * @param classifier the artifact classifier (can be null)
 * @param type the artifact type/extension (defaults to "jar" if null)
 * @since 0.3.0
 */
public record Artifact(String groupId, String artifactId, String version, String classifier, String type) {

    /**
     * Compact constructor with validation.
     *
     * <p>Note: groupId and version can be null to support Maven 4's inference mechanism.
     * Only artifactId is strictly required.</p>
     */
    public Artifact {
        requireNonNull(artifactId, "artifactId cannot be null");
        if (artifactId.trim().isEmpty()) {
            throw new IllegalArgumentException("artifactId cannot be empty");
        }
        // Validate groupId if present
        if (groupId != null && groupId.trim().isEmpty()) {
            throw new IllegalArgumentException("groupId cannot be empty (but can be null)");
        }
        // Validate version if present
        if (version != null && version.trim().isEmpty()) {
            throw new IllegalArgumentException("version cannot be empty (but can be null)");
        }
        // Normalize type to "jar" if null or empty
        if (type == null || type.trim().isEmpty()) {
            type = "jar";
        }
        // Normalize classifier to null if empty
        if (classifier != null && classifier.trim().isEmpty()) {
            classifier = null;
        }
    }

    /**
     * Creates an artifact with groupId, artifactId, and version (JAR type, no classifier).
     *
     * @param groupId the Maven groupId
     * @param artifactId the Maven artifactId
     * @param version the artifact version
     * @return a new Artifact instance
     */
    public static Artifact of(String groupId, String artifactId, String version) {
        return new Artifact(groupId, artifactId, version, null, "jar");
    }

    /**
     * Creates an artifact with groupId, artifactId, version, classifier, and type.
     *
     * @param groupId the Maven groupId
     * @param artifactId the Maven artifactId
     * @param version the artifact version
     * @param classifier the artifact classifier (can be null)
     * @param type the artifact type (can be null, defaults to "jar")
     * @return a new Artifact instance
     */
    public static Artifact of(String groupId, String artifactId, String version, String classifier, String type) {
        return new Artifact(groupId, artifactId, version, classifier, type);
    }

    /**
     * Returns the groupId:artifactId string representation.
     *
     * @return GA string (e.g., "org.junit.jupiter:junit-jupiter")
     */
    public String toGA() {
        return groupId + ":" + artifactId;
    }

    /**
     * Returns the groupId:artifactId:version string representation.
     *
     * @return GAV string (e.g., "org.junit.jupiter:junit-jupiter:5.9.2")
     */
    public String toGAV() {
        return groupId + ":" + artifactId + ":" + version;
    }

    /**
     * Returns the groupId:artifactId:type[:classifier] string representation.
     *
     * @return GATC string (e.g., "org.junit.jupiter:junit-jupiter:jar" or "org.example:lib:jar:sources")
     */
    public String toGATC() {
        if (classifier != null) {
            return groupId + ":" + artifactId + ":" + type + ":" + classifier;
        } else {
            return groupId + ":" + artifactId + ":" + type;
        }
    }

    /**
     * Returns the full string representation: groupId:artifactId:type[:classifier]:version.
     *
     * @return full artifact string
     */
    public String toFullString() {
        if (classifier != null) {
            return groupId + ":" + artifactId + ":" + type + ":" + classifier + ":" + version;
        } else {
            return groupId + ":" + artifactId + ":" + type + ":" + version;
        }
    }

    /**
     * Returns a new Artifact with the same coordinates but different version.
     *
     * @param newVersion the new version
     * @return a new Artifact instance with updated version
     */
    public Artifact withVersion(String newVersion) {
        return new Artifact(groupId, artifactId, newVersion, classifier, type);
    }

    /**
     * Returns a new Artifact with the same coordinates but different type.
     *
     * @param newType the new type
     * @return a new Artifact instance with updated type
     */
    public Artifact withType(String newType) {
        return new Artifact(groupId, artifactId, version, classifier, newType);
    }

    // ========== PREDICATE FACTORY METHODS ==========

    /**
     * Creates a predicate that matches elements by GA (groupId:artifactId).
     *
     * <p>This is useful for filtering streams of elements to find matching artifacts:</p>
     * <pre>{@code
     * Artifact junit = Artifact.of("junit", "junit", "4.13.2");
     * dependencies.children("dependency")
     *     .filter(junit.predicateGA())
     *     .findFirst();
     * }</pre>
     *
     * @return a predicate for matching elements by GA
     * @since 0.3.0
     */
    public Predicate<Element> predicateGA() {
        return element -> {
            String elementGA = AbstractMavenEditor.toGA(element);
            return Objects.equals(elementGA, toGA());
        };
    }

    /**
     * Creates a predicate that matches plugin elements by GA (with default groupId handling).
     *
     * <p>Maven plugins default to groupId "org.apache.maven.plugins" if not specified.
     * This predicate handles that convention:</p>
     * <pre>{@code
     * Artifact compiler = Artifact.of("org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");
     * plugins.children("plugin")
     *     .filter(compiler.predicatePluginGA())
     *     .findFirst();
     * }</pre>
     *
     * @return a predicate for matching plugin elements by GA
     * @since 0.3.0
     */
    public Predicate<Element> predicatePluginGA() {
        return element -> {
            String elementGA = AbstractMavenEditor.toPluginGA(element);
            return Objects.equals(elementGA, toGA());
        };
    }

    /**
     * Creates a predicate that matches elements by GATC (groupId:artifactId:type[:classifier]).
     *
     * <p>This is useful for filtering dependencies or artifacts with specific types and classifiers:</p>
     * <pre>{@code
     * Artifact sources = Artifact.of("org.example", "my-lib", "1.0.0", "sources", "jar");
     * dependencies.children("dependency")
     *     .filter(sources.predicateGATC())
     *     .findFirst();
     * }</pre>
     *
     * @return a predicate for matching elements by GATC
     * @since 0.3.0
     */
    public Predicate<Element> predicateGATC() {
        return element -> {
            String elementGATC = AbstractMavenEditor.toGATC(element);
            return Objects.equals(elementGATC, toGATC());
        };
    }

    // ========== FACTORY METHODS ==========

    /**
     * Creates a POM Artifact from a POM file by reading its GAV coordinates.
     *
     * <p>This method reads the groupId, artifactId, and version from the POM file.
     * If groupId or version are not present in the project element, it looks for them
     * in the parent element. With Maven 4's inference mechanism, groupId and version
     * may be inferred from the reactor and not present in the POM - in such cases,
     * the returned Artifact will have null values for these fields.</p>
     *
     * <h4>Example:</h4>
     * <pre>{@code
     * Path pomFile = Paths.get("pom.xml");
     * Artifact project = Artifact.fromPom(pomFile);
     * System.out.println("Project: " + project.toGAV());
     * }</pre>
     *
     * @param pomPath the path to the POM file
     * @return a new Artifact instance representing the POM (groupId and version may be null)
     * @throws IllegalArgumentException if artifactId is missing
     * @since 0.3.0
     */
    public static Artifact fromPom(Path pomPath) {
        requireNonNull(pomPath);
        PomEditor pomEditor = new PomEditor(Document.of(pomPath));
        Element root = pomEditor.root();

        String groupId = root.childTextOr(MavenPomElements.Elements.GROUP_ID, null);
        String artifactId = root.childTextOr(MavenPomElements.Elements.ARTIFACT_ID, null);
        String version = root.childTextOr(MavenPomElements.Elements.VERSION, null);

        // ArtifactId is always required
        if (artifactId == null) {
            throw new IllegalArgumentException("Malformed POM: artifactId is required but not found");
        }

        // Try to get groupId and version from parent if not present
        if (groupId == null || version == null) {
            Element parent = pomEditor.findChildElement(root, MavenPomElements.Elements.PARENT);
            if (parent != null) {
                if (groupId == null) {
                    groupId = parent.childTextOr(MavenPomElements.Elements.GROUP_ID, null);
                }
                if (version == null) {
                    version = parent.childTextOr(MavenPomElements.Elements.VERSION, null);
                }
            }
            // Note: We don't throw if groupId or version is still null - Maven 4 can infer these
        }

        return Artifact.of(groupId, artifactId, version, null, "pom");
    }
}
