/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for resolving namespace information in XML elements.
 * Provides methods to resolve namespace URIs, prefixes, and build namespace contexts.
 */
public class NamespaceResolver {

    /**
     * Resolves the namespace URI for a given prefix in the context of an element.
     * Walks up the element tree to find namespace declarations.
     */
    public static String resolveNamespaceURI(Element element, String prefix) {
        if (element == null) {
            return getBuiltInNamespaceURI(prefix);
        }

        // Check for built-in namespaces first
        String builtInUri = getBuiltInNamespaceURI(prefix);
        if (builtInUri != null) {
            return builtInUri;
        }

        // Look for namespace declaration in current element and ancestors
        Element current = element;
        while (current != null) {
            String uri = findNamespaceDeclaration(current, prefix);
            if (uri != null) {
                return uri;
            }

            Node parent = current.parent();
            current = (parent instanceof Element) ? (Element) parent : null;
        }

        return null;
    }

    /**
     * Resolves a prefix for a given namespace URI in the context of an element.
     * Returns the first prefix found that maps to the URI.
     */
    public static String resolvePrefix(Element element, String namespaceURI) {
        if (element == null || namespaceURI == null) {
            return null;
        }

        // Check for built-in namespaces
        if ("http://www.w3.org/XML/1998/namespace".equals(namespaceURI)) {
            return "xml";
        }
        if ("http://www.w3.org/2000/xmlns/".equals(namespaceURI)) {
            return "xmlns";
        }

        // Look for prefix declaration in current element and ancestors
        Element current = element;
        while (current != null) {
            String prefix = findPrefixDeclaration(current, namespaceURI);
            if (prefix != null) {
                return prefix;
            }

            Node parent = current.parent();
            current = (parent instanceof Element) ? (Element) parent : null;
        }

        return null;
    }

    /**
     * Checks if a namespace URI is in scope for the given element.
     */
    public static boolean isNamespaceInScope(Element element, String namespaceURI) {
        if (namespaceURI == null) {
            return false;
        }

        // Built-in namespaces are always in scope
        if ("http://www.w3.org/XML/1998/namespace".equals(namespaceURI)
                || "http://www.w3.org/2000/xmlns/".equals(namespaceURI)) {
            return true;
        }

        return resolvePrefix(element, namespaceURI) != null
                || namespaceURI.equals(resolveNamespaceURI(element, null)); // Check default namespace
    }

    /**
     * Builds a complete namespace context for the given element.
     * Includes all namespace declarations from the element and its ancestors.
     */
    public static NamespaceContext buildNamespaceContext(Element element) {
        if (element == null) {
            return new NamespaceContext();
        }

        Map<String, String> prefixToUri = new HashMap<>();
        String defaultNamespaceURI = null;

        // Collect namespace declarations from element and ancestors
        Element current = element;
        while (current != null) {
            Map<String, String> attributes = current.attributes();

            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                String attrName = entry.getKey();
                String attrValue = entry.getValue();

                if ("xmlns".equals(attrName)) {
                    // Default namespace declaration
                    if (defaultNamespaceURI == null) { // Don't override closer declarations
                        defaultNamespaceURI = attrValue;
                    }
                } else if (attrName.startsWith("xmlns:")) {
                    // Prefixed namespace declaration
                    String prefix = attrName.substring(6);
                    if (!prefixToUri.containsKey(prefix)) { // Don't override closer declarations
                        prefixToUri.put(prefix, attrValue);
                    }
                }
            }

            Node parent = current.parent();
            current = (parent instanceof Element) ? (Element) parent : null;
        }

        return new NamespaceContext(prefixToUri, defaultNamespaceURI);
    }

    /**
     * Splits a qualified name into prefix and local name parts.
     * Returns an array where [0] is prefix (or null) and [1] is local name.
     */
    public static String[] splitQualifiedName(String qualifiedName) {
        if (qualifiedName == null || qualifiedName.isEmpty()) {
            return new String[] {null, ""};
        }

        int colonIndex = qualifiedName.indexOf(':');
        if (colonIndex == -1) {
            return new String[] {null, qualifiedName};
        }

        String prefix = qualifiedName.substring(0, colonIndex);
        String localName = qualifiedName.substring(colonIndex + 1);

        return new String[] {prefix.isEmpty() ? null : prefix, localName};
    }

    /**
     * Creates a qualified name from prefix and local name.
     */
    public static String createQualifiedName(String prefix, String localName) {
        if (prefix == null || prefix.isEmpty()) {
            return localName != null ? localName : "";
        }
        if (localName == null || localName.isEmpty()) {
            return prefix;
        }
        return prefix + ":" + localName;
    }

    // Private helper methods

    private static String getBuiltInNamespaceURI(String prefix) {
        if ("xml".equals(prefix)) {
            return "http://www.w3.org/XML/1998/namespace";
        }
        if ("xmlns".equals(prefix)) {
            return "http://www.w3.org/2000/xmlns/";
        }
        return null;
    }

    private static String findNamespaceDeclaration(Element element, String prefix) {
        Map<String, String> attributes = element.attributes();

        if (prefix == null) {
            // Looking for default namespace
            return attributes.get("xmlns");
        } else {
            // Looking for prefixed namespace
            return attributes.get("xmlns:" + prefix);
        }
    }

    private static String findPrefixDeclaration(Element element, String namespaceURI) {
        Map<String, String> attributes = element.attributes();

        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            String attrName = entry.getKey();
            String attrValue = entry.getValue();

            if (namespaceURI.equals(attrValue)) {
                if ("xmlns".equals(attrName)) {
                    return null; // Default namespace
                } else if (attrName.startsWith("xmlns:")) {
                    return attrName.substring(6);
                }
            }
        }

        return null;
    }
}
