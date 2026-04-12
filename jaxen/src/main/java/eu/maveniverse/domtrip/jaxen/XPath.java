/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.jaxen;

import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Element;
import java.util.List;
import java.util.Optional;
import org.jaxen.JaxenException;

/**
 * Static utility for evaluating full XPath 1.0 expressions against DomTrip documents.
 *
 * <p>This class provides convenient one-shot methods that wrap {@link JaxenException}
 * into {@link DomTripException} for consistency with the core DomTrip API. For repeated
 * evaluation of the same expression, use {@link #compile(String)} to avoid re-parsing.</p>
 *
 * <h3>Quick Queries:</h3>
 * <pre>{@code
 * // Find all test dependencies
 * List<Element> testDeps = XPath.select(root, "//dependency[scope='test']");
 *
 * // Find first matching element
 * Optional<Element> junit = XPath.selectFirst(root,
 *     "//dependency[contains(groupId, 'junit')]");
 * }</pre>
 *
 * <h3>Compiled Expressions:</h3>
 * <pre>{@code
 * // Compile once, evaluate many times
 * DomTripXPath expr = XPath.compile("//dependency[scope='test']");
 * List<Element> results1 = expr.selectElements(root1);
 * List<Element> results2 = expr.selectElements(root2);
 * }</pre>
 *
 * <h3>Advanced Queries (not available in mini-XPath):</h3>
 * <pre>{@code
 * // Boolean operators
 * XPath.select(root, "//dependency[scope='test' and groupId='junit']");
 *
 * // String functions
 * XPath.select(root, "//dependency[contains(groupId, 'spring')]");
 * XPath.select(root, "//dependency[starts-with(groupId, 'org.')]");
 *
 * // Negation
 * XPath.select(root, "//dependency[not(@optional)]");
 *
 * // Union
 * XPath.select(root, "//groupId | //artifactId");
 *
 * // Full axis navigation
 * XPath.select(root, "//dependency/following-sibling::dependency");
 * }</pre>
 *
 * @since 1.3.0
 * @see DomTripXPath
 */
public final class XPath {

    private XPath() {
        // utility class
    }

    /**
     * Evaluates an XPath expression and returns matching elements.
     *
     * @param context the context node to evaluate against
     * @param expression the XPath 1.0 expression
     * @return list of matching elements
     * @throws DomTripException if the expression is invalid or evaluation fails
     */
    public static List<Element> select(Object context, String expression) {
        try {
            return new DomTripXPath(expression).selectElements(context);
        } catch (JaxenException e) {
            throw new DomTripException("XPath evaluation failed: " + expression, e);
        }
    }

    /**
     * Evaluates an XPath expression and returns the first matching element.
     *
     * @param context the context node to evaluate against
     * @param expression the XPath 1.0 expression
     * @return the first matching element, or empty if none match
     * @throws DomTripException if the expression is invalid or evaluation fails
     */
    public static Optional<Element> selectFirst(Object context, String expression) {
        try {
            return new DomTripXPath(expression).selectFirstElement(context);
        } catch (JaxenException e) {
            throw new DomTripException("XPath evaluation failed: " + expression, e);
        }
    }

    /**
     * Compiles an XPath expression for repeated evaluation.
     *
     * <p>Use this when the same expression will be evaluated against multiple
     * context nodes to avoid re-parsing the expression each time.</p>
     *
     * @param expression the XPath 1.0 expression to compile
     * @return the compiled expression
     * @throws DomTripException if the expression is syntactically invalid
     */
    public static DomTripXPath compile(String expression) {
        try {
            return new DomTripXPath(expression);
        } catch (JaxenException e) {
            throw new DomTripException("Invalid XPath expression: " + expression, e);
        }
    }
}
