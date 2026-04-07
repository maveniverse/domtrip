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
     */
    public enum VersionStyle {
        /** Version specified directly in the {@code <dependency>} element. */
        INLINE,
        /** Version delegated to {@code <dependencyManagement>}, dependency is version-less. */
        MANAGED
    }

    /**
     * Whether the version value is a literal or a property reference.
     */
    public enum VersionSource {
        /** Literal version value (e.g., {@code 5.9.2}). */
        LITERAL,
        /** Property reference (e.g., {@code ${junit.version}}). */
        PROPERTY
    }

    /**
     * Naming convention for version properties.
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
     * Returns default options where all conventions are auto-detected.
     *
     * @return default align options
     */
    public static AlignOptions defaults() {
        return new AlignOptions(null, null, null, null, null, null);
    }

    /**
     * Returns a new builder for constructing align options.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the version style, or null for auto-detection.
     *
     * @return the version style override, or null
     */
    public VersionStyle versionStyle() {
        return versionStyle;
    }

    /**
     * Returns the version source, or null for auto-detection.
     *
     * @return the version source override, or null
     */
    public VersionSource versionSource() {
        return versionSource;
    }

    /**
     * Returns the property naming convention, or null for auto-detection.
     *
     * @return the naming convention override, or null
     */
    public PropertyNamingConvention namingConvention() {
        return namingConvention;
    }

    /**
     * Returns the custom property name generator function, or null for convention-based generation.
     *
     * <p>When set, this function is used to generate property names from dependency coordinates,
     * allowing arbitrary naming patterns beyond the built-in {@link PropertyNamingConvention} options.</p>
     *
     * <p>Precedence: {@link #propertyName()} &gt; {@link #propertyNameGenerator()} &gt;
     * {@link #namingConvention()} &gt; auto-detected convention.</p>
     *
     * @return the property name generator, or null
     * @since 1.1.0
     */
    public Function<Coordinates, String> propertyNameGenerator() {
        return propertyNameGenerator;
    }

    /**
     * Returns the explicit property name override, or null for auto-generation.
     *
     * @return the property name override, or null
     */
    public String propertyName() {
        return propertyName;
    }

    /**
     * Returns the dependency scope (e.g., "test", "provided"), or null for default scope.
     *
     * @return the scope, or null
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
     * Converts a hyphenated or dotted string to camelCase.
     *
     * @param input the input string (e.g., "junit-jupiter")
     * @return the camelCase version (e.g., "junitJupiter")
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
     */
    public static final class Builder {
        private VersionStyle versionStyle;
        private VersionSource versionSource;
        private PropertyNamingConvention namingConvention;
        private Function<Coordinates, String> propertyNameGenerator;
        private String propertyName;
        private String scope;

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
         * Sets a custom function to generate property names from dependency coordinates.
         *
         * <p>This allows arbitrary naming patterns beyond the built-in conventions.
         * Overridden by {@link #propertyName(String)} if both are set.</p>
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
         * Builds the {@link AlignOptions} instance.
         *
         * @return a new AlignOptions instance
         */
        public AlignOptions build() {
            return new AlignOptions(
                    versionStyle, versionSource, namingConvention, propertyNameGenerator, propertyName, scope);
        }
    }
}
