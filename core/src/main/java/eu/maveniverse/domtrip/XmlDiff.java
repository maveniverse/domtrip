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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * XML-aware structural diff engine that compares two documents and detects
 * both semantic and formatting-only changes.
 *
 * <p>Because DomTrip preserves all formatting metadata (whitespace, quote styles,
 * entity encoding, empty element style), this diff can uniquely distinguish
 * between changes that affect meaning and changes that are formatting-only.
 * This is a capability no other Java XML library offers.</p>
 *
 * <h3>Basic usage:</h3>
 * <pre>{@code
 * Document before = Document.of(oldXml);
 * Document after = Document.of(newXml);
 *
 * DiffResult diff = XmlDiff.diff(before, after);
 *
 * for (XmlChange change : diff.changes()) {
 *     System.out.println(change);
 * }
 * // → ELEMENT_ADDED: /project/dependencies/dependency[3]
 * // → TEXT_CHANGED: /project/version: "1.0" → "1.1"
 * // → ATTRIBUTE_CHANGED: /project/dependencies/dependency[2]/@scope: "compile" → "test"
 * }</pre>
 *
 * <h3>Configurable matching:</h3>
 * <pre>{@code
 * DiffConfig config = DiffConfig.builder()
 *     .matchBy("dependency", "groupId", "artifactId")
 *     .build();
 *
 * DiffResult diff = XmlDiff.diff(before, after, config);
 * }</pre>
 *
 * @see DiffResult
 * @see DiffConfig
 * @see XmlChange
 * @see ChangeType
 * @since 1.3.0
 */
public final class XmlDiff {

    private XmlDiff() {}

    /**
     * Compares two documents using default configuration (positional matching).
     *
     * @param before the original document
     * @param after the modified document
     * @return the diff result containing all detected changes
     */
    public static DiffResult diff(Document before, Document after) {
        return diff(before, after, DiffConfig.defaults());
    }

    /**
     * Compares two documents using the given configuration.
     *
     * @param before the original document
     * @param after the modified document
     * @param config the diff configuration controlling element matching
     * @return the diff result containing all detected changes
     */
    public static DiffResult diff(Document before, Document after, DiffConfig config) {
        Objects.requireNonNull(before, "before");
        Objects.requireNonNull(after, "after");
        Objects.requireNonNull(config, "config");

        List<XmlChange> changes = new ArrayList<>();
        Element beforeRoot = before.root();
        Element afterRoot = after.root();

        if (beforeRoot == null && afterRoot == null) {
            return new DiffResult(changes);
        }
        if (beforeRoot == null) {
            addElementAdded(changes, "/" + afterRoot.name(), afterRoot);
            return new DiffResult(changes);
        }
        if (afterRoot == null) {
            addElementRemoved(changes, "/" + beforeRoot.name(), beforeRoot);
            return new DiffResult(changes);
        }

        if (!beforeRoot.name().equals(afterRoot.name())) {
            addElementRemoved(changes, "/" + beforeRoot.name(), beforeRoot);
            addElementAdded(changes, "/" + afterRoot.name(), afterRoot);
        } else {
            compareElements(beforeRoot, afterRoot, "/" + beforeRoot.name(), config, changes);
        }

        return new DiffResult(changes);
    }

    // --- Element comparison ---

    private static void compareElements(
            Element before, Element after, String path, DiffConfig config, List<XmlChange> changes) {
        compareElementFormatting(before, after, path, changes);
        compareNamespace(before, after, path, changes);
        compareAttributes(before, after, path, changes);
        compareTextContent(before, after, path, changes);
        compareComments(before, after, path, changes);
        compareChildElements(before, after, path, config, changes);
    }

    private static void compareElementFormatting(Element before, Element after, String path, List<XmlChange> changes) {
        if (!safeEquals(before.precedingWhitespace(), after.precedingWhitespace())
                || !safeEquals(before.openTagWhitespace(), after.openTagWhitespace())
                || !safeEquals(before.innerPrecedingWhitespace(), after.innerPrecedingWhitespace())
                || !safeEquals(before.closeTagWhitespace(), after.closeTagWhitespace())) {
            changes.add(new XmlChange(ChangeType.WHITESPACE_CHANGED, path, null, null, before, after));
        }

        if (before.selfClosing() != after.selfClosing() && before.isEmpty() && after.isEmpty()) {
            changes.add(new XmlChange(
                    ChangeType.EMPTY_ELEMENT_STYLE_CHANGED,
                    path,
                    before.selfClosing() ? "self-closing" : "expanded",
                    after.selfClosing() ? "self-closing" : "expanded",
                    before,
                    after));
        }
    }

    private static void compareNamespace(Element before, Element after, String path, List<XmlChange> changes) {
        String beforeNs = before.namespaceURI();
        String afterNs = after.namespaceURI();
        if (!safeEquals(beforeNs, afterNs)) {
            changes.add(new XmlChange(ChangeType.NAMESPACE_CHANGED, path, beforeNs, afterNs, before, after));
        }
    }

    // --- Attribute comparison ---

    private static void compareAttributes(Element before, Element after, String path, List<XmlChange> changes) {
        Map<String, Attribute> beforeAttrs = before.attributeObjects();
        Map<String, Attribute> afterAttrs = after.attributeObjects();

        for (Map.Entry<String, Attribute> entry : beforeAttrs.entrySet()) {
            String name = entry.getKey();
            Attribute beforeAttr = entry.getValue();
            Attribute afterAttr = afterAttrs.get(name);
            String attrPath = path + "/@" + name;

            if (afterAttr == null) {
                changes.add(
                        new XmlChange(ChangeType.ATTRIBUTE_REMOVED, attrPath, beforeAttr.value(), null, before, after));
            } else {
                compareAttributeValues(beforeAttr, afterAttr, attrPath, before, after, changes);
            }
        }

        for (Map.Entry<String, Attribute> entry : afterAttrs.entrySet()) {
            if (!beforeAttrs.containsKey(entry.getKey())) {
                String attrPath = path + "/@" + entry.getKey();
                changes.add(new XmlChange(
                        ChangeType.ATTRIBUTE_ADDED,
                        attrPath,
                        null,
                        entry.getValue().value(),
                        before,
                        after));
            }
        }
    }

    private static void compareAttributeValues(
            Attribute before,
            Attribute after,
            String attrPath,
            Element beforeParent,
            Element afterParent,
            List<XmlChange> changes) {
        // Semantic: compare decoded values
        if (!Objects.equals(before.value(), after.value())) {
            changes.add(new XmlChange(
                    ChangeType.ATTRIBUTE_CHANGED, attrPath, before.value(), after.value(), beforeParent, afterParent));
            return;
        }

        // Formatting: compare quote style
        if (before.quoteStyle() != after.quoteStyle()) {
            changes.add(new XmlChange(
                    ChangeType.QUOTE_STYLE_CHANGED,
                    attrPath,
                    before.quoteStyle().name(),
                    after.quoteStyle().name(),
                    beforeParent,
                    afterParent));
        }

        // Formatting: compare raw value (entity encoding form)
        if (before.rawValue() != null && after.rawValue() != null && !safeEquals(before.rawValue(), after.rawValue())) {
            changes.add(new XmlChange(
                    ChangeType.ENTITY_FORM_CHANGED,
                    attrPath,
                    before.rawValue(),
                    after.rawValue(),
                    beforeParent,
                    afterParent));
        }

        // Formatting: compare attribute whitespace
        if (!safeEquals(before.precedingWhitespace(), after.precedingWhitespace())) {
            changes.add(new XmlChange(
                    ChangeType.WHITESPACE_CHANGED,
                    attrPath,
                    before.precedingWhitespace(),
                    after.precedingWhitespace(),
                    beforeParent,
                    afterParent));
        }
    }

    // --- Text comparison ---

    private static void compareTextContent(Element before, Element after, String path, List<XmlChange> changes) {
        List<Text> beforeTexts = getTextNodes(before);
        List<Text> afterTexts = getTextNodes(after);

        String beforeContent = joinTextContent(beforeTexts);
        String afterContent = joinTextContent(afterTexts);

        if (!beforeContent.equals(afterContent)) {
            changes.add(new XmlChange(ChangeType.TEXT_CHANGED, path, beforeContent, afterContent, before, after));
        } else if (!beforeContent.isEmpty()) {
            // Same decoded content — check raw formatting (entity encoding)
            String beforeRaw = joinRawContent(beforeTexts);
            String afterRaw = joinRawContent(afterTexts);
            if (!beforeRaw.equals(afterRaw)) {
                changes.add(new XmlChange(ChangeType.ENTITY_FORM_CHANGED, path, beforeRaw, afterRaw, before, after));
            }
        }
    }

    // --- Comment comparison ---

    private static void compareComments(Element before, Element after, String path, List<XmlChange> changes) {
        List<Comment> beforeComments = getComments(before);
        List<Comment> afterComments = getComments(after);

        int matchCount = Math.min(beforeComments.size(), afterComments.size());
        boolean needsIndex = beforeComments.size() > 1 || afterComments.size() > 1;

        for (int i = 0; i < matchCount; i++) {
            Comment bc = beforeComments.get(i);
            Comment ac = afterComments.get(i);
            if (!Objects.equals(bc.content(), ac.content())) {
                changes.add(new XmlChange(
                        ChangeType.COMMENT_CHANGED,
                        commentPath(path, i, needsIndex),
                        bc.content(),
                        ac.content(),
                        bc,
                        ac));
            }
        }

        for (int i = matchCount; i < beforeComments.size(); i++) {
            changes.add(new XmlChange(
                    ChangeType.COMMENT_REMOVED,
                    commentPath(path, i, needsIndex),
                    beforeComments.get(i).content(),
                    null,
                    beforeComments.get(i),
                    null));
        }

        for (int i = matchCount; i < afterComments.size(); i++) {
            changes.add(new XmlChange(
                    ChangeType.COMMENT_ADDED,
                    commentPath(path, i, needsIndex),
                    null,
                    afterComments.get(i).content(),
                    null,
                    afterComments.get(i)));
        }
    }

    /** Builds an XPath-like comment path, adding a positional index when multiple comments exist. */
    private static String commentPath(String path, int index, boolean needsIndex) {
        return needsIndex ? path + "/comment()[" + (index + 1) + "]" : path + "/comment()";
    }

    // --- Child element comparison ---

    private static void compareChildElements(
            Element before, Element after, String path, DiffConfig config, List<XmlChange> changes) {
        List<Element> beforeChildren = before.childElements().collect(Collectors.toList());
        List<Element> afterChildren = after.childElements().collect(Collectors.toList());

        if (beforeChildren.isEmpty() && afterChildren.isEmpty()) {
            return;
        }

        MatchResult match = matchChildren(beforeChildren, afterChildren, config);

        // Report removed elements
        for (int idx : match.removed) {
            Element elem = beforeChildren.get(idx);
            String childPath = computeChildPath(path, elem.name(), beforeChildren, idx);
            addElementRemoved(changes, childPath, elem);
        }

        // Report added elements
        for (int idx : match.added) {
            Element elem = afterChildren.get(idx);
            String childPath = computeChildPath(path, elem.name(), afterChildren, idx);
            addElementAdded(changes, childPath, elem);
        }

        // Process matched elements — detect moves and recurse
        for (int[] pair : match.matched) {
            Element beforeChild = beforeChildren.get(pair[0]);
            Element afterChild = afterChildren.get(pair[1]);
            String childPath = computeChildPath(path, beforeChild.name(), beforeChildren, pair[0]);

            // Detect moves (only for key-matched elements where position changed)
            if (pair[2] == 1) {
                int beforePos = sameNamePosition(beforeChildren, pair[0], match.matchedBeforeSet);
                int afterPos = sameNamePosition(afterChildren, pair[1], match.matchedAfterSet);
                if (beforePos != afterPos) {
                    changes.add(new XmlChange(
                            ChangeType.ELEMENT_MOVED,
                            childPath,
                            String.valueOf(beforePos + 1),
                            String.valueOf(afterPos + 1),
                            beforeChild,
                            afterChild));
                }
            }

            compareElements(beforeChild, afterChild, childPath, config, changes);
        }
    }

    // --- Child matching algorithm ---

    /**
     * Two-phase child matching: first by configured identity keys, then positionally by name.
     * Each matched entry is {@code int[]{beforeIdx, afterIdx, matchType}} where matchType
     * 1 = key-matched, 0 = positional.
     */
    private static MatchResult matchChildren(
            List<Element> beforeChildren, List<Element> afterChildren, DiffConfig config) {
        Set<Integer> matchedBefore = new LinkedHashSet<>();
        Set<Integer> matchedAfter = new LinkedHashSet<>();
        List<int[]> matched = new ArrayList<>();

        matchByKeys(beforeChildren, afterChildren, config, matchedBefore, matchedAfter, matched);
        matchByPosition(beforeChildren, afterChildren, matchedBefore, matchedAfter, matched);

        List<Integer> removed = collectUnmatched(beforeChildren.size(), matchedBefore);
        List<Integer> added = collectUnmatched(afterChildren.size(), matchedAfter);

        return new MatchResult(matched, removed, added, matchedBefore, matchedAfter);
    }

    /** Phase 1: matches children by configured identity keys (e.g., groupId+artifactId for dependencies). */
    private static void matchByKeys(
            List<Element> beforeChildren,
            List<Element> afterChildren,
            DiffConfig config,
            Set<Integer> matchedBefore,
            Set<Integer> matchedAfter,
            List<int[]> matched) {
        for (int i = 0; i < beforeChildren.size(); i++) {
            if (matchedBefore.contains(i)) {
                // already matched — skip
            } else {
                tryKeyMatch(beforeChildren.get(i), i, afterChildren, config, matchedBefore, matchedAfter, matched);
            }
        }
    }

    /** Attempts to find a key-based match for a single before-child in the after-children list. */
    private static void tryKeyMatch(
            Element bc,
            int beforeIndex,
            List<Element> afterChildren,
            DiffConfig config,
            Set<Integer> matchedBefore,
            Set<Integer> matchedAfter,
            List<int[]> matched) {
        List<String> keys = config.matchKeysFor(bc.name());
        if (keys.isEmpty()) {
            return;
        }
        String keySignature = computeKeySignature(bc, keys);
        if (keySignature == null) {
            return;
        }
        int match = findKeyMatch(afterChildren, bc.name(), keySignature, keys, matchedAfter);
        if (match >= 0) {
            matched.add(new int[] {beforeIndex, match, 1}); // 1 = key-matched
            matchedBefore.add(beforeIndex);
            matchedAfter.add(match);
        }
    }

    /** Returns the index of the first unmatched after-child with the same name and key signature, or -1. */
    private static int findKeyMatch(
            List<Element> afterChildren,
            String name,
            String keySignature,
            List<String> keys,
            Set<Integer> matchedAfter) {
        for (int j = 0; j < afterChildren.size(); j++) {
            if (!matchedAfter.contains(j) && name.equals(afterChildren.get(j).name())) {
                String afterSig = computeKeySignature(afterChildren.get(j), keys);
                if (keySignature.equals(afterSig)) {
                    return j;
                }
            }
        }
        return -1;
    }

    /** Phase 2: matches remaining unmatched children positionally among same-name siblings. */
    private static void matchByPosition(
            List<Element> beforeChildren,
            List<Element> afterChildren,
            Set<Integer> matchedBefore,
            Set<Integer> matchedAfter,
            List<int[]> matched) {
        Map<String, List<Integer>> remainingBefore = groupByName(beforeChildren, matchedBefore);
        Map<String, List<Integer>> remainingAfter = groupByName(afterChildren, matchedAfter);

        for (Map.Entry<String, List<Integer>> entry : remainingBefore.entrySet()) {
            List<Integer> beforeIndices = entry.getValue();
            List<Integer> afterIndices = remainingAfter.getOrDefault(entry.getKey(), Collections.<Integer>emptyList());

            int matchCount = Math.min(beforeIndices.size(), afterIndices.size());
            for (int k = 0; k < matchCount; k++) {
                matched.add(new int[] {beforeIndices.get(k), afterIndices.get(k), 0}); // 0 = positional
                matchedBefore.add(beforeIndices.get(k));
                matchedAfter.add(afterIndices.get(k));
            }
        }
    }

    private static List<Integer> collectUnmatched(int size, Set<Integer> matched) {
        List<Integer> unmatched = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            if (!matched.contains(i)) {
                unmatched.add(i);
            }
        }
        return unmatched;
    }

    /** Builds an identity signature from child elements or attributes named by the keys, or null if any key is missing. */
    private static String computeKeySignature(Element element, List<String> keys) {
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            // Try child element first (Maven-style keys like groupId)
            Optional<Element> keyChild = element.childElement(key);
            if (keyChild.isPresent()) {
                sb.append(key).append('=').append(keyChild.get().textContent()).append(';');
            } else {
                // Fall back to attribute
                String attrValue = element.attribute(key);
                if (attrValue != null) {
                    sb.append(key).append('=').append(attrValue).append(';');
                } else {
                    return null; // Key not found
                }
            }
        }
        return sb.toString();
    }

    private static Map<String, List<Integer>> groupByName(List<Element> elements, Set<Integer> excluded) {
        Map<String, List<Integer>> grouped = new LinkedHashMap<>();
        for (int i = 0; i < elements.size(); i++) {
            if (excluded.contains(i)) {
                continue;
            }
            grouped.computeIfAbsent(elements.get(i).name(), k -> new ArrayList<>())
                    .add(i);
        }
        return grouped;
    }

    /**
     * Computes the 0-based position of an element among matched same-name siblings.
     */
    private static int sameNamePosition(List<Element> children, int index, Set<Integer> matchedIndices) {
        String name = children.get(index).name();
        int pos = 0;
        for (int i = 0; i < index; i++) {
            if (matchedIndices.contains(i) && children.get(i).name().equals(name)) {
                pos++;
            }
        }
        return pos;
    }

    // --- Path helpers ---

    private static String computeChildPath(String parentPath, String name, List<Element> siblings, int index) {
        long sameNameCount =
                siblings.stream().filter(e -> e.name().equals(name)).count();
        if (sameNameCount > 1) {
            int pos = 1;
            for (int i = 0; i < index; i++) {
                if (siblings.get(i).name().equals(name)) {
                    pos++;
                }
            }
            return parentPath + "/" + name + "[" + pos + "]";
        }
        return parentPath + "/" + name;
    }

    // --- Node collection helpers ---

    private static List<Text> getTextNodes(Element element) {
        return element.children()
                .filter(Text.class::isInstance)
                .map(Text.class::cast)
                .collect(Collectors.toList());
    }

    private static List<Comment> getComments(Element element) {
        return element.children()
                .filter(Comment.class::isInstance)
                .map(Comment.class::cast)
                .collect(Collectors.toList());
    }

    private static String joinTextContent(List<Text> texts) {
        StringBuilder sb = new StringBuilder();
        for (Text t : texts) {
            sb.append(t.content());
        }
        return sb.toString();
    }

    private static String joinRawContent(List<Text> texts) {
        StringBuilder sb = new StringBuilder();
        for (Text t : texts) {
            sb.append(t.rawContent() != null ? t.rawContent() : t.content());
        }
        return sb.toString();
    }

    // --- Change creation helpers ---

    private static void addElementAdded(List<XmlChange> changes, String path, Element element) {
        changes.add(new XmlChange(ChangeType.ELEMENT_ADDED, path, null, elementSummary(element), null, element));
    }

    private static void addElementRemoved(List<XmlChange> changes, String path, Element element) {
        changes.add(new XmlChange(ChangeType.ELEMENT_REMOVED, path, elementSummary(element), null, element, null));
    }

    private static String elementSummary(Element element) {
        String text = element.textContent();
        if (text != null && !text.trim().isEmpty()) {
            return text.trim();
        }
        return "<" + element.name() + ">";
    }

    // --- Utility ---

    private static boolean safeEquals(String a, String b) {
        String sa = a != null ? a : "";
        String sb = b != null ? b : "";
        return sa.equals(sb);
    }

    /**
     * Internal result of the child matching algorithm.
     */
    static class MatchResult {
        final List<int[]> matched; // Each entry: [beforeIdx, afterIdx, keyMatched (1=key, 0=positional)]
        final List<Integer> removed;
        final List<Integer> added;
        final Set<Integer> matchedBeforeSet;
        final Set<Integer> matchedAfterSet;

        MatchResult(
                List<int[]> matched,
                List<Integer> removed,
                List<Integer> added,
                Set<Integer> matchedBeforeSet,
                Set<Integer> matchedAfterSet) {
            this.matched = matched;
            this.removed = removed;
            this.added = added;
            this.matchedBeforeSet = matchedBeforeSet;
            this.matchedAfterSet = matchedAfterSet;
        }
    }
}
