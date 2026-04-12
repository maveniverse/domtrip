/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.jaxen;

import eu.maveniverse.domtrip.Attribute;
import eu.maveniverse.domtrip.Element;

/**
 * Wrapper that pairs a DomTrip {@link Attribute} with its owning {@link Element},
 * enabling Jaxen to navigate from an attribute back to its parent element.
 *
 * <p>DomTrip's {@link Attribute} class does not store a reference to its owning element,
 * but XPath requires parent navigation from attributes. This wrapper provides that link.</p>
 *
 * @since 1.3.0
 */
class AttributeNode {

    private final Element element;
    private final String name;
    private final Attribute attribute;

    AttributeNode(Element element, String name, Attribute attribute) {
        this.element = element;
        this.name = name;
        this.attribute = attribute;
    }

    /**
     * Returns the element that owns this attribute.
     */
    Element element() {
        return element;
    }

    /**
     * Returns the qualified attribute name (may include prefix).
     */
    String name() {
        return name;
    }

    /**
     * Returns the decoded attribute value.
     */
    String value() {
        return attribute.value();
    }

    /**
     * Returns the underlying DomTrip attribute object.
     */
    Attribute attribute() {
        return attribute;
    }
}
