/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.jaxen;

import eu.maveniverse.domtrip.Element;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;

/**
 * XPath implementation for DomTrip's XML object model.
 *
 * <p>This class extends Jaxen's {@link BaseXPath} to provide full XPath 1.0
 * evaluation against DomTrip documents. It supports all standard XPath features
 * including boolean operators, functions, full axis navigation, and union expressions.</p>
 *
 * <h3>Usage:</h3>
 * <pre>{@code
 * DomTripXPath xpath = new DomTripXPath("//dependency[scope='test' and groupId='junit']");
 * List results = xpath.selectNodes(root);
 *
 * // Type-safe element selection
 * List<Element> elements = xpath.selectElements(root);
 * Optional<Element> first = xpath.selectFirstElement(root);
 * }</pre>
 *
 * <h3>Supported XPath Features (beyond mini-XPath):</h3>
 * <ul>
 *   <li>Boolean operators: {@code and}, {@code or}</li>
 *   <li>Negation: {@code not()}</li>
 *   <li>Inequality: {@code !=}</li>
 *   <li>String functions: {@code contains()}, {@code starts-with()}, {@code string-length()}</li>
 *   <li>Union operator: {@code |}</li>
 *   <li>Full axis navigation: {@code ancestor::}, {@code following-sibling::}, etc.</li>
 * </ul>
 *
 * @implNote This class is NOT thread-safe due to mutable namespace and variable contexts
 *           inherited from Jaxen's {@link BaseXPath}. Create separate instances per thread
 *           or synchronize access externally.
 *
 * @since 1.3.0
 * @see XPath
 * @see DomTripNavigator
 */
public class DomTripXPath extends BaseXPath {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new XPath expression for DomTrip documents.
     *
     * @param xpathExpr the XPath 1.0 expression string
     * @throws JaxenException if the expression is syntactically invalid
     */
    public DomTripXPath(String xpathExpr) throws JaxenException {
        super(xpathExpr, DomTripNavigator.getInstance());
    }

    /**
     * Evaluates this expression and returns only {@link Element} results.
     *
     * @param context the context node (Element, Document, or other DomTrip node)
     * @return list of matching elements
     * @throws JaxenException if evaluation fails
     */
    public List<Element> selectElements(Object context) throws JaxenException {
        List<?> results = selectNodes(context);
        List<Element> elements = new ArrayList<>();
        for (Object result : results) {
            if (result instanceof Element) {
                elements.add((Element) result);
            }
        }
        return elements;
    }

    /**
     * Evaluates this expression and returns the first {@link Element} result.
     *
     * @param context the context node (Element, Document, or other DomTrip node)
     * @return the first matching element, or empty if none match
     * @throws JaxenException if evaluation fails
     */
    public Optional<Element> selectFirstElement(Object context) throws JaxenException {
        List<Element> elements = selectElements(context);
        return elements.isEmpty() ? Optional.empty() : Optional.of(elements.get(0));
    }
}
