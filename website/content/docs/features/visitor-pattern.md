---
title: Visitor and Walker Patterns
description: DomTrip provides a visitor/walker API for structured depth-first tree traversal with enter/exit lifecycle callbacks
layout: page
---

# Visitor and Walker Patterns

DomTrip provides a visitor/walker API for structured depth-first tree traversal with enter/exit
lifecycle callbacks. This complements the existing stream-based navigation
([`descendants()`](../stream-support/), [`query()`](../../api/element/)) with a pattern better
suited for transformations needing context tracking, selective subtree skipping, or accumulated
state.

## When to Use Visitors vs Streams

| Use Case | Streams | Visitor |
|----------|---------|---------|
| Find elements matching a condition | `descendants().filter(...)` | Overkill |
| Transform with context (depth, ancestors) | Awkward | Natural fit |
| Skip entire subtrees | Not supported | `Action.SKIP` |
| Enter/exit lifecycle (stack tracking) | Not possible | Built-in |
| Accumulate state across the tree | Possible but verbose | Clean pattern |

## Visitor Interface

The `DomTripVisitor` interface provides callbacks for each node type with flow control:

```java
public interface DomTripVisitor {

    enum Action {
        CONTINUE,   // Continue normal traversal
        SKIP,       // Skip children of current element (enterElement only)
        STOP        // Abort traversal entirely
    }

    default Action enterElement(Element element) { return Action.CONTINUE; }
    default void exitElement(Element element) {}
    default Action visitText(Text text) { return Action.CONTINUE; }
    default Action visitComment(Comment comment) { return Action.CONTINUE; }
    default Action visitProcessingInstruction(ProcessingInstruction pi) { return Action.CONTINUE; }
}
```

All methods have default implementations, so you only need to override the ones you care about.

## Basic Usage

### Walking from an Element

Call `accept()` on any element to start a depth-first traversal of its subtree:

```java
element.accept(new DomTripVisitor() {
    @Override
    public Action enterElement(Element e) {
        System.out.println("Entering: " + e.name());
        return Action.CONTINUE;
    }

    @Override
    public void exitElement(Element e) {
        System.out.println("Exiting: " + e.name());
    }
});
```

### Walking an Entire Document

Use `Editor.walk()` to traverse the full document including top-level comments and
processing instructions:

```java
Editor editor = new Editor(Document.of(xml));
editor.walk(new DomTripVisitor() {
    @Override
    public Action enterElement(Element e) {
        System.out.println(e.name());
        return Action.CONTINUE;
    }
});
```

## Flow Control

### Skipping Subtrees

Return `Action.SKIP` from `enterElement` to skip all children of an element.
The `exitElement` callback is still called:

```java
element.accept(new DomTripVisitor() {
    @Override
    public Action enterElement(Element e) {
        if ("metadata".equals(e.localName())) {
            return Action.SKIP; // Don't descend into metadata
        }
        return Action.CONTINUE;
    }

    @Override
    public void exitElement(Element e) {
        // Still called for skipped elements
    }
});
```

### Stopping Traversal

Return `Action.STOP` from any visit method to abort the entire traversal immediately.
When stopped from `enterElement`, `exitElement` is **not** called for the stopped element
or any ancestors:

```java
element.accept(new DomTripVisitor() {
    @Override
    public Action enterElement(Element e) {
        if ("target".equals(e.name())) {
            // Found what we need, stop walking
            return Action.STOP;
        }
        return Action.CONTINUE;
    }

    @Override
    public Action visitText(Text t) {
        if (t.content().contains("error")) {
            return Action.STOP; // Can also stop from leaf nodes
        }
        return Action.CONTINUE;
    }
});
```

## Stateful Visitors

Visitors naturally support accumulated state, which is difficult to achieve with streams:

### Depth Tracking

```java
element.accept(new DomTripVisitor() {
    private int depth = 0;

    @Override
    public Action enterElement(Element e) {
        System.out.println("  ".repeat(depth) + e.name());
        depth++;
        return Action.CONTINUE;
    }

    @Override
    public void exitElement(Element e) {
        depth--;
    }
});
```

### Namespace Collection

```java
class NamespaceCollector implements DomTripVisitor {
    private final Map<String, String> namespaces = new LinkedHashMap<>();
    private final Deque<String> path = new ArrayDeque<>();

    @Override
    public Action enterElement(Element e) {
        path.push(e.localName());
        String ns = e.namespaceURI();
        if (ns != null && !namespaces.containsKey(ns)) {
            namespaces.put(ns, String.join("/", path));
        }
        return Action.CONTINUE;
    }

    @Override
    public void exitElement(Element e) {
        path.pop();
    }

    public Map<String, String> result() { return namespaces; }
}

NamespaceCollector collector = new NamespaceCollector();
editor.root().accept(collector);
Map<String, String> nsMap = collector.result();
```

## Mutation During Traversal

Visitors can safely mutate elements during traversal. DomTrip uses a snapshot of the
children list internally, so structural modifications will not cause errors:

### Text Redaction

```java
element.accept(new DomTripVisitor() {
    @Override
    public Action enterElement(Element e) {
        if ("password".equals(e.name())) {
            e.findTextNode().ifPresent(t -> t.content("***"));
            return Action.SKIP; // No need to visit children
        }
        return Action.CONTINUE;
    }
});
```

### Conditional Transformation

```java
element.accept(new DomTripVisitor() {
    @Override
    public Action enterElement(Element e) {
        // Only redact passwords inside <credentials> blocks
        if ("password".equals(e.localName())
                && e.parentElement() != null
                && "credentials".equals(e.parentElement().localName())) {
            e.findTextNode().ifPresent(t -> t.content("REDACTED"));
        }
        return Action.CONTINUE;
    }
});
```

## Lambda-Friendly Walker

For simple traversals, the `TreeWalker` fluent API avoids the need to create a full
`DomTripVisitor` implementation:

```java
element.walk()
    .onEnter(e -> {
        if ("secret".equals(e.localName())) {
            e.findTextNode().ifPresent(t -> t.content("***"));
            return DomTripVisitor.Action.SKIP;
        }
        return DomTripVisitor.Action.CONTINUE;
    })
    .onExit(e -> { /* cleanup */ })
    .onText(t -> {
        System.out.println("Text: " + t.content());
        return DomTripVisitor.Action.CONTINUE;
    })
    .onComment(c -> {
        System.out.println("Comment: " + c.content());
        return DomTripVisitor.Action.CONTINUE;
    })
    .onProcessingInstruction(pi -> {
        System.out.println("PI: " + pi.target());
        return DomTripVisitor.Action.CONTINUE;
    })
    .execute();
```

You only need to set the callbacks you care about. Unconfigured callbacks default to
`Action.CONTINUE`.

## Visiting All Node Types

The visitor dispatches to different methods based on node type:

| Node Type | Callback | Returns |
|-----------|----------|---------|
| `Element` | `enterElement()` + children + `exitElement()` | `Action` / `void` |
| `Text` | `visitText()` | `Action` |
| `Comment` | `visitComment()` | `Action` |
| `ProcessingInstruction` | `visitProcessingInstruction()` | `Action` |

```java
element.accept(new DomTripVisitor() {
    @Override
    public Action enterElement(Element e) {
        System.out.println("Element: " + e.name());
        return Action.CONTINUE;
    }

    @Override
    public Action visitText(Text t) {
        if (!t.isWhitespaceOnly()) {
            System.out.println("Text: " + t.content().trim());
        }
        return Action.CONTINUE;
    }

    @Override
    public Action visitComment(Comment c) {
        System.out.println("Comment: " + c.content().trim());
        return Action.CONTINUE;
    }

    @Override
    public Action visitProcessingInstruction(ProcessingInstruction pi) {
        System.out.println("PI: " + pi.target() + " " + pi.data());
        return Action.CONTINUE;
    }
});
```

## Best Practices

### 1. Prefer the Visitor Interface for Complex Traversals

```java
// Good - reusable, testable, stateful
class DependencyCollector implements DomTripVisitor {
    private final List<String> dependencies = new ArrayList<>();

    @Override
    public Action enterElement(Element e) {
        if ("dependency".equals(e.name())) {
            String groupId = e.childElement("groupId")
                .map(Element::textContent).orElse("?");
            String artifactId = e.childElement("artifactId")
                .map(Element::textContent).orElse("?");
            dependencies.add(groupId + ":" + artifactId);
            return Action.SKIP; // No need to visit children
        }
        return Action.CONTINUE;
    }

    public List<String> result() { return dependencies; }
}
```

### 2. Use the TreeWalker for Simple One-Off Traversals

```java
// Good - concise for simple cases
List<String> names = new ArrayList<>();
element.walk()
    .onEnter(e -> {
        names.add(e.name());
        return DomTripVisitor.Action.CONTINUE;
    })
    .execute();
```

### 3. Use SKIP to Avoid Unnecessary Work

```java
// Good - skip subtrees you don't care about
element.accept(new DomTripVisitor() {
    @Override
    public Action enterElement(Element e) {
        if ("metadata".equals(e.name()) || "plugins".equals(e.name())) {
            return Action.SKIP;
        }
        // Process only relevant subtrees
        return Action.CONTINUE;
    }
});
```

### 4. Use STOP for Early Exit

```java
// Good - stop as soon as you find what you need
element.accept(new DomTripVisitor() {
    private Element found;

    @Override
    public Action enterElement(Element e) {
        if ("target".equals(e.name())) {
            found = e;
            return Action.STOP;
        }
        return Action.CONTINUE;
    }
});
```
