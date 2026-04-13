/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Mini-XPath expression support for string-based element queries.
 *
 * <p>Compiles a subset of XPath expressions into efficient element queries
 * that can be evaluated against any element in a DomTrip document tree.
 * This provides a convenient string-based alternative to the programmatic
 * {@link ElementQuery} API.</p>
 *
 * <h3>Supported Expressions:</h3>
 * <table>
 *   <caption>Path navigation</caption>
 *   <tr><td>{@code foo/bar/baz}</td><td>Direct child path</td></tr>
 *   <tr><td>{@code //foo}</td><td>Descendant-or-self (search anywhere below)</td></tr>
 *   <tr><td>{@code foo//bar}</td><td>{@code bar} anywhere under {@code foo} children</td></tr>
 *   <tr><td>{@code .}</td><td>Current element</td></tr>
 *   <tr><td>{@code ..}</td><td>Parent element</td></tr>
 *   <tr><td>{@code *}</td><td>Any element (wildcard)</td></tr>
 * </table>
 *
 * <table>
 *   <caption>Predicates</caption>
 *   <tr><td>{@code foo[@attr]}</td><td>Element with attribute present</td></tr>
 *   <tr><td>{@code foo[@attr='value']}</td><td>Element with attribute equal to value</td></tr>
 *   <tr><td>{@code foo[bar='text']}</td><td>Element with child text content</td></tr>
 *   <tr><td>{@code foo[1]}</td><td>First element (1-based)</td></tr>
 *   <tr><td>{@code foo[last()]}</td><td>Last element</td></tr>
 * </table>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Compile once, reuse many times
 * XPathExpression expr = XPathExpression.compile("//dependency[@scope='test']");
 * List<Element> results = expr.select(root);
 * Optional<Element> first = expr.selectFirst(root);
 *
 * // Or use convenience methods on Element
 * List<Element> deps = element.select("dependencies/dependency");
 * Optional<Element> match = element.selectFirst("dependency[groupId='junit']");
 *
 * // Or on Editor
 * List<Element> allDeps = editor.select("//dependency");
 * }</pre>
 *
 * <h3>What is NOT Supported:</h3>
 * <ul>
 *   <li>Full axis specifiers ({@code preceding-sibling::}, {@code ancestor::})</li>
 *   <li>XPath functions ({@code contains()}, {@code normalize-space()})</li>
 *   <li>Boolean operators ({@code and}, {@code or})</li>
 *   <li>Arithmetic operators</li>
 *   <li>Union operator ({@code |})</li>
 * </ul>
 *
 * @see Element#select(String)
 * @see Element#selectFirst(String)
 * @see Editor#select(String)
 * @see Editor#selectFirst(String)
 * @implNote Compiled expressions are immutable and thread-safe. They can be safely shared
 *           across threads and evaluated concurrently against different context elements.
 *
 * @see ElementQuery
 * @since 1.3.0
 */
public class XPathExpression {

    private final String expression;
    private final List<Step> steps;

    private XPathExpression(String expression, List<Step> steps) {
        this.expression = expression;
        this.steps = steps;
    }

    /**
     * Compiles an XPath expression string into a reusable {@code XPathExpression}.
     *
     * <p>The compiled expression can be evaluated multiple times against different
     * context elements without re-parsing.</p>
     *
     * @param expression the XPath expression to compile
     * @return a compiled XPathExpression
     * @throws DomTripException if the expression is null, empty, or syntactically invalid
     */
    public static XPathExpression compile(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            throw new DomTripException("XPath expression cannot be null or empty");
        }
        List<Step> steps = parse(expression.trim());
        if (steps.isEmpty()) {
            throw new DomTripException("XPath expression produced no path steps: " + expression);
        }
        return new XPathExpression(expression, steps);
    }

    /**
     * Evaluates this expression against the given context element and returns all matching elements.
     *
     * @param context the element to evaluate the expression against
     * @return a list of matching elements, never null
     */
    public List<Element> select(Element context) {
        if (context == null) {
            return Collections.emptyList();
        }
        List<Element> current = Collections.singletonList(context);
        for (Step step : steps) {
            current = step.evaluate(current);
            if (current.isEmpty()) {
                return current;
            }
        }
        return current;
    }

    /**
     * Evaluates this expression against the given context element and returns the first match.
     *
     * @param context the element to evaluate the expression against
     * @return an Optional containing the first matching element, or empty if none found
     */
    public Optional<Element> selectFirst(Element context) {
        List<Element> results = select(context);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Returns the original expression string.
     *
     * @return the expression string
     */
    public String expression() {
        return expression;
    }

    @Override
    public String toString() {
        return "XPathExpression{" + expression + "}";
    }

    // ========== PARSING ==========

    /**
     * Parses an expression string into a list of compiled steps.
     */
    private static List<Step> parse(String expression) {
        List<Step> steps = new ArrayList<>();
        List<RawStep> rawSteps = splitIntoRawSteps(expression);
        for (RawStep raw : rawSteps) {
            steps.add(parseStep(raw));
        }
        return steps;
    }

    /**
     * Splits the expression string into raw step tokens, tracking the axis for each.
     */
    private static List<RawStep> splitIntoRawSteps(String expression) {
        List<RawStep> result = new ArrayList<>();
        int pos = 0;
        boolean descendant = false;

        // Handle leading // or /
        if (expression.startsWith("//")) {
            descendant = true;
            pos = 2;
        } else if (expression.startsWith("/")) {
            pos = 1;
        }

        boolean[] isDoubleSlash = {false};
        int sepIdx;
        while ((sepIdx = findNextSeparator(expression, pos, isDoubleSlash)) >= 0) {
            addStepIfNotEmpty(result, expression.substring(pos, sepIdx), descendant);
            descendant = isDoubleSlash[0];
            pos = sepIdx + (descendant ? 2 : 1);
        }
        addStepIfNotEmpty(result, expression.substring(pos), descendant);

        return result;
    }

    /**
     * Finds the next path separator {@code /} that is outside brackets and quotes.
     *
     * @param expression the expression string to scan
     * @param start the position to start scanning from
     * @param isDoubleSlash output parameter; set to {@code true} if the separator is {@code //}
     * @return the index of the separator, or {@code -1} if not found
     */
    private static int findNextSeparator(String expression, int start, boolean[] isDoubleSlash) {
        int bracketDepth = 0;
        boolean inSQ = false;
        boolean inDQ = false;
        for (int i = start; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (inSQ) {
                inSQ = c != '\''; // stay in single-quote mode unless this char closes it
            } else if (inDQ) {
                inDQ = c != '"'; // stay in double-quote mode unless this char closes it
            } else if (c == '\'') {
                inSQ = true;
            } else if (c == '"') {
                inDQ = true;
            } else if (c == '[') {
                bracketDepth++;
            } else if (c == ']') {
                bracketDepth--;
            } else if (c == '/' && bracketDepth == 0) {
                isDoubleSlash[0] = (i + 1 < expression.length() && expression.charAt(i + 1) == '/');
                return i;
            }
        }
        return -1;
    }

    /**
     * Adds a step to the result list if the step text is not empty.
     */
    private static void addStepIfNotEmpty(List<RawStep> result, String text, boolean descendant) {
        if (!text.isEmpty()) {
            result.add(new RawStep(descendant ? Axis.DESCENDANT : Axis.CHILD, text));
        }
    }

    /**
     * Parses a single raw step token into a compiled {@link Step} with axis, name test, and predicates.
     */
    private static Step parseStep(RawStep raw) {
        String text = raw.text;
        Axis axis = raw.axis;

        // Handle special navigation steps
        if (".".equals(text)) {
            return new Step(Axis.SELF, null, Collections.<Predicate>emptyList());
        }
        if ("..".equals(text)) {
            return new Step(Axis.PARENT, null, Collections.<Predicate>emptyList());
        }

        // Parse name test and predicates
        int bracketStart = text.indexOf('[');
        String nameTest;
        List<Predicate> predicates;

        if (bracketStart < 0) {
            nameTest = text;
            predicates = Collections.emptyList();
        } else {
            nameTest = text.substring(0, bracketStart);
            predicates = parsePredicates(text.substring(bracketStart));
        }

        // Wildcard: * matches any element
        if ("*".equals(nameTest)) {
            nameTest = null;
        }

        return new Step(axis, nameTest, predicates);
    }

    /**
     * Parses all predicate expressions from a string like {@code [pred1][pred2]}.
     */
    private static List<Predicate> parsePredicates(String text) {
        List<Predicate> predicates = new ArrayList<>();
        int pos = 0;

        while (pos < text.length()) {
            if (text.charAt(pos) != '[') {
                pos++;
                continue;
            }
            int closePos = findClosingBracket(text, pos);
            String content = text.substring(pos + 1, closePos - 1).trim();
            predicates.add(parseSinglePredicate(content));
            pos = closePos;
        }

        return predicates;
    }

    /**
     * Finds the position just past the closing bracket that matches
     * the opening bracket at {@code openPos}, respecting nested brackets and quotes.
     *
     * @param text the text to scan
     * @param openPos the index of the opening {@code [}
     * @return the index just past the matching {@code ]}
     */
    private static int findClosingBracket(String text, int openPos) {
        int depth = 1;
        int pos = openPos + 1;
        boolean inSQ = false;
        boolean inDQ = false;

        while (pos < text.length() && depth > 0) {
            char c = text.charAt(pos);
            if (inSQ) {
                inSQ = c != '\'';
            } else if (inDQ) {
                inDQ = c != '"';
            } else if (c == '\'') {
                inSQ = true;
            } else if (c == '"') {
                inDQ = true;
            } else if (c == '[') {
                depth++;
            } else if (c == ']') {
                depth--;
            }
            pos++;
        }

        return pos;
    }

    /**
     * Parses the content inside a single predicate bracket pair into a {@link Predicate}.
     *
     * @param content the trimmed text between {@code [} and {@code ]}
     * @return the parsed predicate
     * @throws DomTripException if the predicate syntax is not recognized
     */
    private static Predicate parseSinglePredicate(String content) {
        if (content.isEmpty()) {
            throw new DomTripException("Empty predicate: []");
        }

        // last()
        if ("last()".equals(content)) {
            return Predicate.last();
        }

        // Positional: a plain integer
        try {
            int position = Integer.parseInt(content);
            if (position < 1) {
                throw new DomTripException("Positional predicate must be >= 1, got: " + position);
            }
            return Predicate.ofPosition(position);
        } catch (NumberFormatException ignored) {
            // Not a number — continue
        }

        // Attribute predicates: @attr or @attr='value'
        if (content.startsWith("@")) {
            String attrPart = content.substring(1);
            int eqPos = indexOfEqualsOutsideQuotes(attrPart);
            if (eqPos < 0) {
                return Predicate.attributePresence(attrPart.trim());
            }
            String attrName = attrPart.substring(0, eqPos).trim();
            String attrValue = unquote(attrPart.substring(eqPos + 1).trim());
            return Predicate.attributeValue(attrName, attrValue);
        }

        // Child text predicate: childName='text'
        int eqPos = indexOfEqualsOutsideQuotes(content);
        if (eqPos > 0) {
            String childName = content.substring(0, eqPos).trim();
            String textValue = unquote(content.substring(eqPos + 1).trim());
            return Predicate.childText(childName, textValue);
        }

        throw new DomTripException("Unsupported predicate syntax: [" + content + "]");
    }

    /**
     * Finds the index of the first {@code =} that is not inside quotes.
     *
     * @return the index, or -1 if not found
     */
    private static int indexOfEqualsOutsideQuotes(String text) {
        boolean inSQ = false;
        boolean inDQ = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (inSQ) {
                if (c == '\'') {
                    inSQ = false;
                }
            } else if (inDQ) {
                if (c == '"') {
                    inDQ = false;
                }
            } else if (c == '\'') {
                inSQ = true;
            } else if (c == '"') {
                inDQ = true;
            } else if (c == '=') {
                return i;
            }
        }
        return -1;
    }

    /**
     * Strips surrounding single or double quotes from a string.
     */
    private static String unquote(String text) {
        if (text.length() >= 2) {
            char first = text.charAt(0);
            char last = text.charAt(text.length() - 1);
            if ((first == '\'' && last == '\'') || (first == '"' && last == '"')) {
                return text.substring(1, text.length() - 1);
            }
        }
        return text;
    }

    // ========== AST NODES ==========

    /**
     * The navigation axis for a path step.
     */
    enum Axis {
        /** Direct child elements. */
        CHILD,
        /** All descendant elements (recursive). */
        DESCENDANT,
        /** The context element itself. */
        SELF,
        /** The parent element. */
        PARENT
    }

    /**
     * Intermediate representation produced by the path splitter.
     */
    private static class RawStep {
        final Axis axis;
        final String text;

        RawStep(Axis axis, String text) {
            this.axis = axis;
            this.text = text;
        }
    }

    /**
     * A single step in a compiled XPath expression (axis + name test + predicates).
     */
    static class Step {
        final Axis axis;
        final String nameTest; // null = wildcard (matches any element)
        final List<Predicate> predicates;

        Step(Axis axis, String nameTest, List<Predicate> predicates) {
            this.axis = axis;
            this.nameTest = nameTest;
            this.predicates = predicates;
        }

        /**
         * Evaluates this step against the given context nodes, returning all matching elements.
         */
        List<Element> evaluate(List<Element> contextNodes) {
            List<Element> result = new ArrayList<>();
            for (Element context : contextNodes) {
                Stream<Element> candidates;
                switch (axis) {
                    case SELF:
                        candidates = Stream.of(context);
                        break;
                    case PARENT:
                        if (context.parent() instanceof Element) {
                            candidates = Stream.of((Element) context.parent());
                        } else {
                            candidates = Stream.empty();
                        }
                        break;
                    case DESCENDANT:
                        candidates = context.descendants();
                        break;
                    default: // CHILD
                        candidates = context.childElements();
                        break;
                }

                // Apply name test
                if (nameTest != null) {
                    candidates = candidates.filter(e -> nameMatches(e, nameTest));
                }

                // Collect before applying predicates (positional predicates need the full list)
                List<Element> candidateList = candidates.collect(Collectors.toList());

                // Apply each predicate in order
                for (Predicate predicate : predicates) {
                    candidateList = predicate.apply(candidateList);
                }

                result.addAll(candidateList);
            }
            return result;
        }
    }

    /**
     * Checks whether an element matches a name test.
     * Matches against the qualified name first, then the local name,
     * enabling both prefixed ({@code soap:Envelope}) and unprefixed ({@code dependency}) matching.
     */
    static boolean nameMatches(Element element, String name) {
        return name.equals(element.name()) || name.equals(element.localName());
    }

    /**
     * A predicate filter within a path step (e.g., {@code [@scope='test']}).
     */
    static class Predicate {
        /**
         * The type of predicate filter.
         */
        enum Type {
            /** Matches elements that have a given attribute. */
            ATTRIBUTE_PRESENCE,
            /** Matches elements whose attribute equals a given value. */
            ATTRIBUTE_VALUE,
            /** Matches elements with a child whose text content equals a given value. */
            CHILD_TEXT,
            /** Selects the element at a specific 1-based position. */
            POSITION,
            /** Selects the last element. */
            LAST
        }

        final Type type;
        final String name;
        final String value;
        final int position;

        private Predicate(Type type, String name, String value, int position) {
            this.type = type;
            this.name = name;
            this.value = value;
            this.position = position;
        }

        /** Creates a predicate that matches elements having the given attribute. */
        static Predicate attributePresence(String attrName) {
            return new Predicate(Type.ATTRIBUTE_PRESENCE, attrName, null, -1);
        }

        /** Creates a predicate that matches elements whose attribute equals the given value. */
        static Predicate attributeValue(String attrName, String attrValue) {
            return new Predicate(Type.ATTRIBUTE_VALUE, attrName, attrValue, -1);
        }

        /** Creates a predicate that matches elements with a child whose text equals the given value. */
        static Predicate childText(String childName, String textValue) {
            return new Predicate(Type.CHILD_TEXT, childName, textValue, -1);
        }

        /** Creates a predicate that selects the element at the given 1-based position. */
        static Predicate ofPosition(int pos) {
            return new Predicate(Type.POSITION, null, null, pos);
        }

        /** Creates a predicate that selects the last element. */
        static Predicate last() {
            return new Predicate(Type.LAST, null, null, -1);
        }

        /**
         * Applies this predicate to filter or select from the given element list.
         */
        List<Element> apply(List<Element> elements) {
            if (elements.isEmpty()) {
                return elements;
            }
            switch (type) {
                case ATTRIBUTE_PRESENCE:
                    return elements.stream().filter(e -> e.hasAttribute(name)).collect(Collectors.toList());
                case ATTRIBUTE_VALUE:
                    return elements.stream()
                            .filter(e -> value.equals(e.attribute(name)))
                            .collect(Collectors.toList());
                case CHILD_TEXT:
                    return elements.stream()
                            .filter(e -> childTextMatches(e, name, value))
                            .collect(Collectors.toList());
                case POSITION:
                    if (position >= 1 && position <= elements.size()) {
                        return Collections.singletonList(elements.get(position - 1));
                    }
                    return Collections.emptyList();
                case LAST:
                    return Collections.singletonList(elements.get(elements.size() - 1));
                default:
                    return elements;
            }
        }

        /**
         * Checks if an element has a child element with the given name whose trimmed
         * text content equals the expected value.
         */
        private static boolean childTextMatches(Element parent, String childName, String expectedText) {
            // Search children matching by qualified name or local name
            return parent.childElements()
                    .filter(child -> nameMatches(child, childName))
                    .findFirst()
                    .map(child -> expectedText.equals(child.textContentTrimmed()))
                    .orElse(false);
        }
    }
}
