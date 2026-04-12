/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Lambda-friendly builder for configuring and executing tree traversals.
 *
 * <p>TreeWalker provides a fluent API alternative to implementing the
 * {@link DomTripVisitor} interface directly. It is particularly useful for
 * simple traversals where only a few visitor methods are needed.</p>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * element.walk()
 *     .onEnter(e -> {
 *         if ("secret".equals(e.localName())) {
 *             e.textContent("***");
 *             return DomTripVisitor.Action.SKIP;
 *         }
 *         return DomTripVisitor.Action.CONTINUE;
 *     })
 *     .onExit(e -> System.out.println("Exiting: " + e.name()))
 *     .onText(t -> {
 *         System.out.println("Text: " + t.content());
 *         return DomTripVisitor.Action.CONTINUE;
 *     })
 *     .execute();
 * }</pre>
 *
 * @see DomTripVisitor
 * @see Element#walk()
 * @since 1.3.0
 */
public class TreeWalker {

    private final Node root;
    private Function<Element, DomTripVisitor.Action> onEnter;
    private Consumer<Element> onExit;
    private Function<Text, DomTripVisitor.Action> onText;
    private Function<Comment, DomTripVisitor.Action> onComment;
    private Function<ProcessingInstruction, DomTripVisitor.Action> onPI;

    /**
     * Creates a new TreeWalker that will traverse from the given root node.
     *
     * @param root the node to start traversal from
     * @throws IllegalArgumentException if root is null
     */
    TreeWalker(Node root) {
        if (root == null) {
            throw new IllegalArgumentException("Root node cannot be null");
        }
        this.root = root;
    }

    /**
     * Sets the callback invoked when entering each element.
     *
     * <p>The function receives the element and returns an {@link DomTripVisitor.Action}
     * to control traversal flow.</p>
     *
     * @param handler the enter callback
     * @return this walker for method chaining
     */
    public TreeWalker onEnter(Function<Element, DomTripVisitor.Action> handler) {
        this.onEnter = handler;
        return this;
    }

    /**
     * Sets the callback invoked when exiting each element.
     *
     * @param handler the exit callback
     * @return this walker for method chaining
     */
    public TreeWalker onExit(Consumer<Element> handler) {
        this.onExit = handler;
        return this;
    }

    /**
     * Sets the callback invoked for each text node.
     *
     * @param handler the text callback
     * @return this walker for method chaining
     */
    public TreeWalker onText(Function<Text, DomTripVisitor.Action> handler) {
        this.onText = handler;
        return this;
    }

    /**
     * Sets the callback invoked for each comment node.
     *
     * @param handler the comment callback
     * @return this walker for method chaining
     */
    public TreeWalker onComment(Function<Comment, DomTripVisitor.Action> handler) {
        this.onComment = handler;
        return this;
    }

    /**
     * Sets the callback invoked for each processing instruction node.
     *
     * @param handler the processing instruction callback
     * @return this walker for method chaining
     */
    public TreeWalker onProcessingInstruction(Function<ProcessingInstruction, DomTripVisitor.Action> handler) {
        this.onPI = handler;
        return this;
    }

    /**
     * Executes the traversal with the configured callbacks.
     *
     * <p>Performs a depth-first walk of the tree starting from the root node,
     * invoking the configured callbacks for each node encountered.</p>
     */
    public void execute() {
        // Capture handlers into locals to ensure deterministic behavior during traversal
        Function<Element, DomTripVisitor.Action> enterHandler = onEnter;
        Consumer<Element> exitHandler = onExit;
        Function<Text, DomTripVisitor.Action> textHandler = onText;
        Function<Comment, DomTripVisitor.Action> commentHandler = onComment;
        Function<ProcessingInstruction, DomTripVisitor.Action> piHandler = onPI;

        root.accept(new DomTripVisitor() {
            @Override
            public Action enterElement(Element element) {
                return enterHandler != null ? enterHandler.apply(element) : Action.CONTINUE;
            }

            @Override
            public void exitElement(Element element) {
                if (exitHandler != null) {
                    exitHandler.accept(element);
                }
            }

            @Override
            public Action visitText(Text text) {
                return textHandler != null ? textHandler.apply(text) : Action.CONTINUE;
            }

            @Override
            public Action visitComment(Comment comment) {
                return commentHandler != null ? commentHandler.apply(comment) : Action.CONTINUE;
            }

            @Override
            public Action visitProcessingInstruction(ProcessingInstruction pi) {
                return piHandler != null ? piHandler.apply(pi) : Action.CONTINUE;
            }
        });
    }
}
