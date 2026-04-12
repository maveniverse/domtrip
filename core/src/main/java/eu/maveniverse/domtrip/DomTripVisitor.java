/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip;

/**
 * Visitor interface for structured depth-first tree traversal with enter/exit
 * lifecycle callbacks.
 *
 * <p>This complements the existing stream-based navigation ({@link Element#descendants()},
 * {@link ElementQuery}) with a pattern better suited for transformations needing context
 * tracking (depth, ancestors, accumulated state).</p>
 *
 * <h3>Traversal Order:</h3>
 * <p>The visitor performs a depth-first traversal: for each element,
 * {@link #enterElement(Element)} is called first, then all children are visited
 * recursively, and finally {@link #exitElement(Element)} is called.</p>
 *
 * <h3>Flow Control:</h3>
 * <p>Visit methods return an {@link Action} to control traversal:</p>
 * <ul>
 *   <li>{@link Action#CONTINUE} - Continue normal traversal</li>
 *   <li>{@link Action#SKIP} - Skip children of current element (only meaningful from {@code enterElement})</li>
 *   <li>{@link Action#STOP} - Abort traversal entirely</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * element.accept(new DomTripVisitor() {
 *     @Override
 *     public Action enterElement(Element e) {
 *         if ("metadata".equals(e.localName())) {
 *             return Action.SKIP; // don't descend into metadata
 *         }
 *         System.out.println("Entering: " + e.name());
 *         return Action.CONTINUE;
 *     }
 *
 *     @Override
 *     public void exitElement(Element e) {
 *         System.out.println("Exiting: " + e.name());
 *     }
 *
 *     @Override
 *     public Action visitComment(Comment c) {
 *         System.out.println("Comment: " + c.content());
 *         return Action.CONTINUE;
 *     }
 * });
 * }</pre>
 *
 * <h3>Stateful Visitor Example:</h3>
 * <pre>{@code
 * class NamespaceCollector implements DomTripVisitor {
 *     private final Map<String, String> namespaces = new LinkedHashMap<>();
 *     private final Deque<String> path = new ArrayDeque<>();
 *
 *     @Override
 *     public Action enterElement(Element e) {
 *         path.push(e.localName());
 *         String ns = e.namespaceURI();
 *         if (ns != null && !namespaces.containsKey(ns)) {
 *             namespaces.put(ns, String.join("/", path));
 *         }
 *         return Action.CONTINUE;
 *     }
 *
 *     @Override
 *     public void exitElement(Element e) {
 *         path.pop();
 *     }
 *
 *     public Map<String, String> result() { return namespaces; }
 * }
 * }</pre>
 *
 * @see Node#accept(DomTripVisitor)
 * @see TreeWalker
 * @since 1.3.0
 */
public interface DomTripVisitor {

    /**
     * Controls traversal flow during a visitor walk.
     */
    enum Action {
        /** Continue normal depth-first traversal. */
        CONTINUE,
        /** Skip children of the current element (only meaningful from {@link #enterElement}). */
        SKIP,
        /** Abort the entire traversal immediately. */
        STOP
    }

    /**
     * Called when entering an element during depth-first traversal.
     *
     * <p>This is called before visiting any of the element's children.
     * Return {@link Action#SKIP} to skip the element's children, or
     * {@link Action#STOP} to abort the entire traversal.</p>
     *
     * @param element the element being entered
     * @return the action to take: {@link Action#CONTINUE}, {@link Action#SKIP}, or {@link Action#STOP}
     */
    default Action enterElement(Element element) {
        return Action.CONTINUE;
    }

    /**
     * Called after all children of an element have been visited.
     *
     * <p>This is always called for elements that were entered (even if
     * children were skipped via {@link Action#SKIP}), unless the traversal
     * was stopped via {@link Action#STOP}.</p>
     *
     * @param element the element being exited
     */
    default void exitElement(Element element) {}

    /**
     * Called for text nodes during traversal.
     *
     * @param text the text node being visited
     * @return the action to take: {@link Action#CONTINUE} or {@link Action#STOP}
     */
    default Action visitText(Text text) {
        return Action.CONTINUE;
    }

    /**
     * Called for comment nodes during traversal.
     *
     * @param comment the comment node being visited
     * @return the action to take: {@link Action#CONTINUE} or {@link Action#STOP}
     */
    default Action visitComment(Comment comment) {
        return Action.CONTINUE;
    }

    /**
     * Called for processing instruction nodes during traversal.
     *
     * @param pi the processing instruction being visited
     * @return the action to take: {@link Action#CONTINUE} or {@link Action#STOP}
     */
    default Action visitProcessingInstruction(ProcessingInstruction pi) {
        return Action.CONTINUE;
    }
}
