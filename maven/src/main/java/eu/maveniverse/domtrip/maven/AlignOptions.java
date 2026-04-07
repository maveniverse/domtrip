/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.maven;

import java.util.function.Function;

/**
 * Options for controlling how dependencies are aligned with project conventions.
 *
 * <p>When adding or aligning dependencies, these options specify the version management style,
 * version source, and property naming convention. Null values indicate auto-detection from
 * existing POM conventions.</p>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Auto-detect all conventions
 * AlignOptions options = AlignOptions.defaults();
 *
 * // Force managed + property style
 * AlignOptions options = AlignOptions.builder()
 *     .versionStyle(AlignOptions.VersionStyle.MANAGED)
 *     .versionSource(AlignOptions.VersionSource.PROPERTY)
 *     .build();
 *
 * // Force specific property name
 * AlignOptions options = AlignOptions.builder()
 *     .versionSource(AlignOptions.VersionSource.PROPERTY)
 *     .propertyName("junit.version")
 *     .build();
 *
 * // Custom property naming pattern
 * AlignOptions options = AlignOptions.builder()
 *     .versionSource(AlignOptions.VersionSource.PROPERTY)
 *     .propertyNameGenerator(coords -> coords.groupId() + "." + coords.artifactId() + ".version")
 *     .build();
 *
 * // Add with scope
 * AlignOptions options = AlignOptions.builder()
 *     .scope("test")
 *     .build();
 * }</pre>
 *
 * @since 1.1.0
 */
public final class AlignOptions {

    /**
     * Whether the dependency version is inline or delegated to dependency management.
     *
     * @since 1.1.0
     */
    public enum VersionStyle {
        /** Version specified directly in the {@code <dependency>} element. */
        INLINE,
        /** Version delegated to {@code <dependencyManagement>}, dependency is version-less. */
        MANAGED
    }

    /**
     * Whether the version value is a literal or a property reference.
     *
     * @since 1.1.0
     */
    public enum VersionSource {
        /** Literal version value (e.g., {@code 5.9.2}). */
        LITERAL,
        /** Property reference (e.g., {@code ${junit.version}}). */
        PROPERTY
    }

    /**
     * Naming convention for version properties.
     *
     * @since 1.1.0
     */
    public enum PropertyNamingConvention {
        /** Dot suffix: {@code artifactId.version} (e.g., {@code guava.version}). */
        DOT_SUFFIX,
        /** Dash suffix: {@code artifactId-version} (e.g., {@code guava-version}). */
        DASH_SUFFIX,
        /** CamelCase: {@code artifactIdVersion} (e.g., {@code guavaVersion}). */
        CAMEL_CASE,
        /** Dot prefix: {@code version.artifactId} (e.g., {@code version.guava}). */
        DOT_PREFIX
    }

    private final VersionStyle versionStyle;
    private final VersionSource versionSource;
    private final PropertyNamingConvention namingConvention;
    private final Function<Coordinates, String> propertyNameGenerator;
    private final String propertyName;
    private final String scope;

    /**
     * Create an AlignOptions instance using the provided option values.
     *
     * <p>Each parameter may be {@code null} to indicate “auto-detect from existing POM conventions.”
     * This constructor initializes the instance state directly without validation.
     *
     * @param versionStyle         placement of dependency versions (inline vs managed); {@code null} means auto-detect
     * @param versionSource        whether versions are literal or property references; {@code null} means auto-detect
     * @param namingConvention     property naming convention to use; {@code null} means auto-detect
     * @param propertyNameGenerator optional custom generator for property names; {@code null} to use convention-based generation
     * @param propertyName         explicit property name override; {@code null} to allow auto-generation
     * @param scope                Maven dependency scope override; {@code null} to use the default scope
     */
    private AlignOptions(
            VersionStyle versionStyle,
            VersionSource versionSource,
            PropertyNamingConvention namingConvention,
            Function<Coordinates, String> propertyNameGenerator,
            String propertyName,
            String scope) {
        this.versionStyle = versionStyle;
        this.versionSource = versionSource;
        this.namingConvention = namingConvention;
        this.propertyNameGenerator = propertyNameGenerator;
        this.propertyName = propertyName;
        this.scope = scope;
    }

    /**
     * Create an AlignOptions instance with all conventions set to auto-detect.
     *
     * @return an AlignOptions whose fields are all {@code null}, indicating auto-detection of conventions
     * @since 1.1.0
     */
    public static AlignOptions defaults() {
        return new AlignOptions(null, null, null, null, null, null);
    }

    /**
     * Create a new Builder for constructing AlignOptions.
     *
     * @return a new Builder instance for configuring and building an AlignOptions object
     * @since 1.1.0
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Gets the configured version placement style or defers to auto-detection when unspecified.
     *
     * @return the configured version style, or {@code null} to indicate auto-detection
     * @since 1.1.0
     */
    public VersionStyle versionStyle() {
        return versionStyle;
    }

    /**
     * The configured version source, or null to indicate auto-detection.
     *
     * @return the version source override, or {@code null} if auto-detection is used
     * @since 1.1.0
     */
    public VersionSource versionSource() {
        return versionSource;
    }

    /**
     * The property naming convention to use, or null to indicate auto-detection.
     *
     * @return the naming convention override, or {@code null} if it should be auto-detected
     * @since 1.1.0
     */
    public PropertyNamingConvention namingConvention() {
        return namingConvention;
    }

    /**
     * Custom function that generates property names from dependency coordinates, or null to use convention-based generation.
     *
     * <p>When provided, this function is used to derive property names allowing arbitrary naming patterns beyond the
     * built-in {@link PropertyNamingConvention} options.</p>
     *
     * <p>Precedence: an explicit property name overrides this generator, which overrides an explicit naming convention,
     * which in turn falls back to auto-detection.</p>
     *
     * @return the property name generator, or null
     * @since 1.1.0
     */
    public Function<Coordinates, String> propertyNameGenerator() {
        return propertyNameGenerator;
    }

    /**
     * Provides the explicit property name override.
     *
     * @return the explicit property name override, or {@code null} to indicate auto-generation
     * @since 1.1.0
     */
    public String propertyName() {
        return propertyName;
    }

    /**
     * Get the Maven dependency scope configured for this instance.
     *
     * <p>May be {@code null} to indicate the default scope.</p>
     *
     * @return the dependency scope (e.g., "test", "provided"), or {@code null} to use the default
     * @since 1.1.0
     */
    public String scope() {
        return scope;
    }

    /**
     * Generates a version property name for the given coordinates and naming convention.
     *
     * <h4>Examples:</h4>
     * <ul>
     *   <li>{@code DOT_SUFFIX}: {@code junit-jupiter.version}</li>
     *   <li>{@code DASH_SUFFIX}: {@code junit-jupiter-version}</li>
     *   <li>{@code CAMEL_CASE}: {@code junitJupiterVersion}</li>
     *   <li>{@code DOT_PREFIX}: {@code version.junit-jupiter}</li>
     * </ul>
     *
     * @param coords the dependency coordinates (artifactId is used as the base name)
     * @param convention the naming convention to apply
     * @return the generated property name
     * @since 1.1.0
     */
    public static String generatePropertyName(Coordinates coords, PropertyNamingConvention convention) {
        String base = coords.artifactId();
        switch (convention) {
            case DASH_SUFFIX:
                return base + "-version";
            case CAMEL_CASE:
                return toCamelCase(base) + "Version";
            case DOT_PREFIX:
                return "version." + base;
            case DOT_SUFFIX:
            default:
                return base + ".version";
        }
    }

    /**
     * Convert a hyphenated or dotted identifier to camelCase.
     *
     * <p>Removes '-' and '.' characters and capitalizes the character immediately following each removed delimiter.
     *
     * @param input the input string (for example, "junit-jupiter")
     * @return the input converted to camelCase (for example, "junitJupiter")
     * @since 1.1.0
     */
    static String toCamelCase(String input) {
        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '-' || c == '.') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                sb.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Builder for {@link AlignOptions}.
     *
     * @since 1.1.0
     */
    public static final class Builder {
        private VersionStyle versionStyle;
        private VersionSource versionSource;
        private PropertyNamingConvention namingConvention;
        private Function<Coordinates, String> propertyNameGenerator;
        private String propertyName;
        private String scope;

        /**
         * Creates an empty Builder with all option fields unset (null).
         */
        private Builder() {}

        /**
         * Sets the version style override.
         *
         * @param versionStyle the version style (null for auto-detection)
         * @return this builder
         */
        public Builder versionStyle(VersionStyle versionStyle) {
            this.versionStyle = versionStyle;
            return this;
        }

        /**
         * Sets the version source override.
         *
         * @param versionSource the version source (null for auto-detection)
         * @return this builder
         */
        public Builder versionSource(VersionSource versionSource) {
            this.versionSource = versionSource;
            return this;
        }

        /**
         * Sets the property naming convention override.
         *
         * @param namingConvention the naming convention (null for auto-detection)
         * @return this builder
         */
        public Builder namingConvention(PropertyNamingConvention namingConvention) {
            this.namingConvention = namingConvention;
            return this;
        }

        /**
         * Set a custom function that generates property names from dependency coordinates.
         *
         * <p>Allows arbitrary naming patterns beyond the built-in conventions.</p>
         *
         * <p>Example:</p>
         * <pre>{@code
         * .propertyNameGenerator(coords ->
         *     coords.groupId().replace(".", "-") + "." + coords.artifactId() + ".version")
         * }</pre>
         *
         * @param generator the function to generate property names (null to clear)
         * @return this builder
         * @since 1.1.0
         */
        public Builder propertyNameGenerator(Function<Coordinates, String> generator) {
            this.propertyNameGenerator = generator;
            return this;
        }

        /**
         * Sets an explicit property name, overriding auto-generation.
         *
         * @param propertyName the property name to use
         * @return this builder
         */
        public Builder propertyName(String propertyName) {
            this.propertyName = propertyName;
            return this;
        }

        /**
         * Sets the dependency scope.
         *
         * @param scope the scope (e.g., "test", "provided")
         * @return this builder
         */
        public Builder scope(String scope) {
            this.scope = scope;
            return this;
        }

        /**
         * Create an AlignOptions configured with the builder's current settings.
         *
         * <p>No validation or additional processing is performed; the builder's field
         * values (which may be {@code null}) are copied directly into the created instance.</p>
         *
         * @return the constructed AlignOptions
         */
        public AlignOptions build() {
            return new AlignOptions(
                    versionStyle, versionSource, namingConvention, propertyNameGenerator, propertyName, scope);
        }
    }
}
