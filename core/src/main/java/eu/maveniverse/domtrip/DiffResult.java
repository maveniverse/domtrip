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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The result of comparing two XML documents, containing all detected changes.
 *
 * <p>Provides methods to filter changes by type (semantic vs. formatting-only)
 * or by path, and to check whether any changes were detected.</p>
 *
 * <h3>Example:</h3>
 * <pre>{@code
 * DiffResult diff = XmlDiff.diff(before, after);
 *
 * // Semantic changes only (ignore whitespace/formatting)
 * List<XmlChange> semantic = diff.semanticChanges();
 *
 * // Formatting-only changes
 * List<XmlChange> formatting = diff.formattingChanges();
 *
 * // Changes at a specific path
 * List<XmlChange> depChanges = diff.changesUnder("/project/dependencies");
 * }</pre>
 *
 * @see XmlDiff
 * @see XmlChange
 * @since 1.3.0
 */
public class DiffResult {

    private final List<XmlChange> changes;

    /**
     * Creates a new DiffResult with the given changes.
     *
     * @param changes the list of changes
     */
    public DiffResult(List<XmlChange> changes) {
        this.changes = Collections.unmodifiableList(new ArrayList<>(changes));
    }

    /**
     * Returns all changes.
     *
     * @return an unmodifiable list of all changes
     */
    public List<XmlChange> changes() {
        return changes;
    }

    /**
     * Returns only semantic changes that affect the meaning of the XML.
     *
     * @return the list of semantic changes
     */
    public List<XmlChange> semanticChanges() {
        return changes.stream().filter(XmlChange::isSemantic).collect(Collectors.toList());
    }

    /**
     * Returns only formatting changes that do not affect meaning.
     *
     * @return the list of formatting-only changes
     */
    public List<XmlChange> formattingChanges() {
        return changes.stream().filter(XmlChange::isFormattingOnly).collect(Collectors.toList());
    }

    /**
     * Returns changes under the specified path prefix.
     *
     * <p>This method uses boundary-aware matching: a change path must either equal
     * the given path exactly, or start with the path followed by {@code /} or {@code /@}.
     * For example, path {@code /project/name} will NOT match a change at
     * {@code /project/namespace}.</p>
     *
     * @param path the path prefix to filter by
     * @return the list of changes under the given path
     */
    public List<XmlChange> changesUnder(String path) {
        return changes.stream().filter(c -> isAtOrUnder(c.path(), path)).collect(Collectors.toList());
    }

    /**
     * Returns {@code true} if any changes were detected.
     *
     * @return {@code true} if there are changes
     */
    public boolean hasChanges() {
        return !changes.isEmpty();
    }

    /**
     * Returns changes affecting elements that match the given XPath expression
     * in the specified document. This enables rich filtering like
     * {@code diff.changesFor("//dependency[scope='test']", afterDoc)}.
     *
     * @param xpath the XPath expression to match elements
     * @param doc the document to evaluate the expression against
     * @return the list of changes at or under matching elements
     * @since 1.3.0
     */
    public List<XmlChange> changesFor(String xpath, Document doc) {
        return changesFor(XPathExpression.compile(xpath), doc);
    }

    /**
     * Returns changes affecting elements that match the given compiled XPath expression
     * in the specified document.
     *
     * @param xpath the compiled XPath expression to match elements
     * @param doc the document to evaluate the expression against
     * @return the list of changes at or under matching elements
     * @since 1.3.0
     */
    public List<XmlChange> changesFor(XPathExpression xpath, Document doc) {
        Element root = doc.root();
        if (root == null) {
            return Collections.emptyList();
        }
        List<Element> matched = xpath.select(root);
        if (matched.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> matchedPaths =
                matched.stream().map(DiffResult::computeElementPath).collect(Collectors.toSet());
        return changes.stream()
                .filter(c -> matchedPaths.stream().anyMatch(p -> isAtOrUnder(c.path(), p)))
                .collect(Collectors.toList());
    }

    /**
     * Returns {@code true} if any semantic changes were detected.
     *
     * @return {@code true} if there are semantic changes
     */
    public boolean hasSemanticChanges() {
        return changes.stream().anyMatch(XmlChange::isSemantic);
    }

    /**
     * Returns {@code true} if any formatting changes were detected.
     *
     * @return {@code true} if there are formatting-only changes
     */
    public boolean hasFormattingChanges() {
        return changes.stream().anyMatch(XmlChange::isFormattingOnly);
    }

    @Override
    public String toString() {
        if (changes.isEmpty()) {
            return "No changes";
        }
        return changes.stream().map(XmlChange::toString).collect(Collectors.joining("\n"));
    }

    /**
     * Returns {@code true} if {@code changePath} is at or under {@code basePath},
     * using boundary-aware matching to avoid false positives (e.g., {@code /project/name}
     * must not match {@code /project/namespace}).
     */
    private static boolean isAtOrUnder(String changePath, String basePath) {
        if ("/".equals(basePath)) {
            return changePath.startsWith("/");
        }
        // Strip trailing slash so "/project/" matches the same as "/project"
        String normalized = basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath;
        return changePath.equals(normalized)
                || changePath.startsWith(normalized + "/")
                || changePath.startsWith(normalized + "/@");
    }

    /** Computes the XPath-like path for an element by walking up the parent chain. */
    static String computeElementPath(Element element) {
        List<String> parts = new ArrayList<>();
        Element current = element;
        while (current != null) {
            parts.add(computePathStep(current));
            ContainerNode parent = current.parent();
            current = (parent instanceof Element) ? (Element) parent : null;
        }
        Collections.reverse(parts);
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            sb.append('/').append(part);
        }
        return sb.toString();
    }

    /** Computes a single path step, adding a positional index when multiple same-name siblings exist. */
    private static String computePathStep(Element element) {
        String name = element.name();
        ContainerNode parent = element.parent();
        if (!(parent instanceof Element)) {
            return name;
        }
        List<Element> siblings = ((Element) parent).childElements().collect(Collectors.toList());
        long sameNameCount =
                siblings.stream().filter(e -> e.name().equals(name)).count();
        if (sameNameCount <= 1) {
            return name;
        }
        int pos = 1;
        for (Element sibling : siblings) {
            if (sibling == element) {
                break;
            }
            if (sibling.name().equals(name)) {
                pos++;
            }
        }
        return name + "[" + pos + "]";
    }
}
