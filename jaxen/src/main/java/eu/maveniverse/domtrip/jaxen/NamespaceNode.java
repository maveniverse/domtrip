/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.jaxen;

import eu.maveniverse.domtrip.Element;

/**
 * Represents a namespace node for XPath namespace axis traversal.
 *
 * <p>In the XPath data model, namespace declarations are exposed as namespace nodes
 * on the namespace axis, separate from regular attributes. This class wraps a
 * namespace prefix-URI binding along with the element where it is in scope.</p>
 *
 * @since 1.3.0
 */
class NamespaceNode {

    private final Element element;
    private final String prefix;
    private final String uri;

    NamespaceNode(Element element, String prefix, String uri) {
        this.element = element;
        this.prefix = prefix;
        this.uri = uri;
    }

    /**
     * Returns the element where this namespace is in scope.
     */
    Element element() {
        return element;
    }

    /**
     * Returns the namespace prefix, or empty string for the default namespace.
     */
    String prefix() {
        return prefix;
    }

    /**
     * Returns the namespace URI.
     */
    String uri() {
        return uri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NamespaceNode that = (NamespaceNode) o;
        return element == that.element && prefix.equals(that.prefix) && uri.equals(that.uri);
    }

    @Override
    public int hashCode() {
        int result = System.identityHashCode(element);
        result = 31 * result + prefix.hashCode();
        result = 31 * result + uri.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "NamespaceNode{prefix='" + prefix + "', uri='" + uri + "'}";
    }
}
