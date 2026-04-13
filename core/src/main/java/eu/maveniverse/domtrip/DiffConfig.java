/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration for XML diff operations.
 *
 * <p>Controls how elements are matched between two documents. By default, elements
 * are matched positionally among same-name siblings. Configuring match keys enables
 * identity-based matching for domain-specific element types.</p>
 *
 * <h3>Example:</h3>
 * <pre>{@code
 * DiffConfig config = DiffConfig.builder()
 *     .matchBy("dependency", "groupId", "artifactId")
 *     .matchBy("*", "id")
 *     .build();
 * }</pre>
 *
 * @see XmlDiff
 * @since 1.3.0
 */
public class DiffConfig {

    private final Map<String, List<String>> matchKeys;
    private final List<String> wildcardMatchKeys;

    private DiffConfig(Map<String, List<String>> matchKeys, List<String> wildcardMatchKeys) {
        this.matchKeys = Collections.unmodifiableMap(new LinkedHashMap<>(matchKeys));
        this.wildcardMatchKeys = wildcardMatchKeys.isEmpty()
                ? Collections.<String>emptyList()
                : Collections.unmodifiableList(new ArrayList<>(wildcardMatchKeys));
    }

    /**
     * Returns a default configuration with no match keys.
     *
     * @return the default configuration
     */
    public static DiffConfig defaults() {
        return new DiffConfig(Collections.<String, List<String>>emptyMap(), Collections.<String>emptyList());
    }

    /**
     * Returns a new builder for constructing a custom configuration.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the match key attribute or child element names for the given element name,
     * falling back to wildcard keys if no specific keys are configured.
     *
     * @param elementName the element name to look up
     * @return the list of key names, or an empty list if none configured
     */
    public List<String> matchKeysFor(String elementName) {
        List<String> keys = matchKeys.get(elementName);
        if (keys != null) {
            return keys;
        }
        return wildcardMatchKeys;
    }

    /**
     * Builder for {@link DiffConfig}.
     */
    public static class Builder {

        private final Map<String, List<String>> matchKeys = new LinkedHashMap<>();
        private final List<String> wildcardMatchKeys = new ArrayList<>();

        /**
         * Configures match keys for an element name. Use {@code "*"} as the element
         * name for a wildcard that applies to all elements without specific keys.
         *
         * <p>Keys can refer to child element names (e.g., {@code "groupId"} for Maven
         * dependencies) or attribute names (e.g., {@code "id"}).</p>
         *
         * @param elementName the element name, or {@code "*"} for wildcard
         * @param keyAttributes the attribute or child element names that form the identity
         * @return this builder
         */
        public Builder matchBy(String elementName, String... keyAttributes) {
            if ("*".equals(elementName)) {
                wildcardMatchKeys.addAll(Arrays.asList(keyAttributes));
            } else {
                matchKeys.put(elementName, Arrays.asList(keyAttributes));
            }
            return this;
        }

        /**
         * Builds the configuration.
         *
         * @return the built configuration
         */
        public DiffConfig build() {
            return new DiffConfig(matchKeys, wildcardMatchKeys);
        }
    }
}
