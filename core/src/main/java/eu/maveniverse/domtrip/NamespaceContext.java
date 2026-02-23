/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Represents a namespace context for XML elements, providing namespace URI resolution
 * and prefix management functionality.
 */
public class NamespaceContext {

    private final Map<String, String> prefixToUri;
    private final Map<String, Set<String>> uriToPrefixes;
    private final String defaultNamespaceURI;

    /**
     * Creates an empty namespace context.
     */
    public NamespaceContext() {
        this(new HashMap<>(), null);
    }

    /**
     * Creates a namespace context with the given prefix-to-URI mappings.
     */
    public NamespaceContext(Map<String, String> prefixToUri, String defaultNamespaceURI) {
        this.prefixToUri = new HashMap<>(prefixToUri);
        this.uriToPrefixes = new HashMap<>();
        this.defaultNamespaceURI = defaultNamespaceURI;

        // Build reverse mapping
        for (Map.Entry<String, String> entry : this.prefixToUri.entrySet()) {
            String prefix = entry.getKey();
            String uri = entry.getValue();
            uriToPrefixes.computeIfAbsent(uri, k -> new LinkedHashSet<>()).add(prefix);
        }
    }

    /**
     * Gets the namespace URI for the given prefix.
     * Returns null if the prefix is not bound to any namespace.
     */
    public String namespaceURI(String prefix) {
        if (prefix == null) {
            return defaultNamespaceURI;
        }
        if ("xml".equals(prefix)) {
            return "http://www.w3.org/XML/1998/namespace";
        }
        if ("xmlns".equals(prefix)) {
            return "http://www.w3.org/2000/xmlns/";
        }
        return prefixToUri.get(prefix);
    }

    /**
     * Gets the first prefix bound to the given namespace URI.
     * Returns null if no prefix is bound to the URI.
     */
    public String prefix(String namespaceURI) {
        if (namespaceURI == null) {
            return null;
        }
        if ("http://www.w3.org/XML/1998/namespace".equals(namespaceURI)) {
            return "xml";
        }
        if ("http://www.w3.org/2000/xmlns/".equals(namespaceURI)) {
            return "xmlns";
        }
        if (namespaceURI.equals(defaultNamespaceURI)) {
            return null; // Default namespace has no prefix
        }

        Set<String> prefixes = uriToPrefixes.get(namespaceURI);
        return prefixes != null && !prefixes.isEmpty() ? prefixes.iterator().next() : null;
    }

    /**
     * Gets all prefixes bound to the given namespace URI.
     */
    public Stream<String> prefixes(String namespaceURI) {
        if (namespaceURI == null) {
            return Stream.of();
        }
        if ("http://www.w3.org/XML/1998/namespace".equals(namespaceURI)) {
            return Stream.of("xml");
        }
        if ("http://www.w3.org/2000/xmlns/".equals(namespaceURI)) {
            return Stream.of("xmlns");
        }

        Set<String> prefixes = uriToPrefixes.get(namespaceURI);
        if (prefixes != null) {
            return prefixes.stream();
        }
        return Stream.of();
    }

    /**
     * Gets the default namespace URI.
     */
    public String defaultNamespaceURI() {
        return defaultNamespaceURI;
    }

    /**
     * Checks if the given prefix is declared in this context.
     */
    public boolean isPrefixDeclared(String prefix) {
        if (prefix == null) {
            return defaultNamespaceURI != null;
        }
        return "xml".equals(prefix) || "xmlns".equals(prefix) || prefixToUri.containsKey(prefix);
    }

    /**
     * Checks if the given namespace URI is declared in this context.
     */
    public boolean isNamespaceUriDeclared(String namespaceURI) {
        if (namespaceURI == null) {
            return false;
        }
        return "http://www.w3.org/XML/1998/namespace".equals(namespaceURI)
                || "http://www.w3.org/2000/xmlns/".equals(namespaceURI)
                || namespaceURI.equals(defaultNamespaceURI)
                || uriToPrefixes.containsKey(namespaceURI);
    }

    /**
     * Gets all declared prefixes (excluding xml and xmlns).
     */
    public Set<String> declaredPrefixes() {
        return new HashSet<>(prefixToUri.keySet());
    }

    /**
     * Gets all declared namespace URIs.
     */
    public Set<String> declaredNamespaceURIs() {
        Set<String> uris = new HashSet<>(uriToPrefixes.keySet());
        if (defaultNamespaceURI != null) {
            uris.add(defaultNamespaceURI);
        }
        return uris;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("NamespaceContext{");
        if (defaultNamespaceURI != null) {
            sb.append("default=").append(defaultNamespaceURI);
            if (!prefixToUri.isEmpty()) {
                sb.append(", ");
            }
        }
        sb.append("prefixes=").append(prefixToUri);
        sb.append("}");
        return sb.toString();
    }
}
