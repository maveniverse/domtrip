---
title: XML-Aware Structural Diff
description: Compare two XML documents and detect both semantic and formatting-only changes using DomTrip's lossless formatting metadata
layout: page
---

# XML-Aware Structural Diff

DomTrip can compare two XML documents and report structural, content, and formatting differences. Because DomTrip preserves all formatting metadata (whitespace, quote styles, entity encoding, empty element style), the diff engine uniquely distinguishes **semantic changes** that affect meaning from **formatting-only changes** that are cosmetic.

## Basic Usage

```java
Document before = Document.of(oldXml);
Document after = Document.of(newXml);

DiffResult diff = XmlDiff.diff(before, after);

for (XmlChange change : diff.changes()) {
    System.out.println(change);
}
// → TEXT_CHANGED: /project/version: "1.0" → "1.1"
// → ATTRIBUTE_CHANGED: /project/dependencies/dependency[2]/@scope: "compile" → "test"
// → ELEMENT_ADDED: /project/dependencies/dependency[3]
```

## Semantic vs. Formatting Changes

The key differentiator is the ability to separate meaningful changes from noise:

```java
DiffResult diff = XmlDiff.diff(before, after);

// Only changes that affect the XML's meaning
List<XmlChange> meaningful = diff.semanticChanges();

// Only cosmetic changes (whitespace, quotes, entity encoding)
List<XmlChange> cosmetic = diff.formattingChanges();

// Quick checks
if (diff.hasSemanticChanges()) {
    // Something meaningful changed
}
if (!diff.hasSemanticChanges() && diff.hasFormattingChanges()) {
    // Documents are semantically identical, only formatting differs
}
```

### Semantic Change Types

| Change Type | Description |
|---|---|
| `ELEMENT_ADDED` | A new element was inserted |
| `ELEMENT_REMOVED` | An element was deleted |
| `ELEMENT_MOVED` | An element was reordered among siblings |
| `TEXT_CHANGED` | Text content was modified |
| `ATTRIBUTE_ADDED` | A new attribute was added |
| `ATTRIBUTE_REMOVED` | An attribute was removed |
| `ATTRIBUTE_CHANGED` | An attribute value was modified |
| `COMMENT_ADDED` | A comment was inserted |
| `COMMENT_REMOVED` | A comment was deleted |
| `COMMENT_CHANGED` | Comment content was modified |
| `NAMESPACE_CHANGED` | A namespace declaration was modified |

### Formatting-Only Change Types

| Change Type | Description |
|---|---|
| `WHITESPACE_CHANGED` | Indentation or spacing changed |
| `QUOTE_STYLE_CHANGED` | Attribute quotes changed (single ↔ double) |
| `ENTITY_FORM_CHANGED` | Entity encoding changed (e.g., `&lt;` vs `&#60;`) |
| `EMPTY_ELEMENT_STYLE_CHANGED` | Self-closing style changed (`<br/>` ↔ `<br></br>`) |

## Configurable Element Matching

By default, same-name sibling elements are matched positionally. For domain-specific matching (e.g., Maven dependencies), configure identity keys:

```java
DiffConfig config = DiffConfig.builder()
    .matchBy("dependency", "groupId", "artifactId")
    .matchBy("plugin", "groupId", "artifactId")
    .matchBy("*", "id")  // wildcard: match any element by "id"
    .build();

DiffResult diff = XmlDiff.diff(before, after, config);
```

Keys can refer to **child element names** (Maven-style, e.g., `groupId`) or **attribute names** (e.g., `id`). When keys are configured, the diff engine uses a two-phase matching algorithm:

1. **Key-based matching** — elements with the same identity keys are paired regardless of position
2. **Positional matching** — remaining unmatched elements are paired by position among same-name siblings

This enables accurate **move detection** when elements are reordered:

```java
// Before: junit, then log4j
// After: log4j, then junit (reordered)
DiffConfig config = DiffConfig.builder()
    .matchBy("dependency", "groupId", "artifactId")
    .build();

DiffResult diff = XmlDiff.diff(before, after, config);
// → ELEMENT_MOVED: /dependencies/dependency (position 1 → 2)
```

## Filtering Changes

### By Path Prefix

Focus on changes in a specific part of the document:

```java
DiffResult diff = XmlDiff.diff(before, after);

// Only changes under /project/dependencies
List<XmlChange> depChanges = diff.changesUnder("/project/dependencies");
```

### By XPath Expression

Use XPath expressions for richer filtering against the actual DOM tree:

```java
DiffResult diff = XmlDiff.diff(before, after);

// Changes affecting test-scoped dependencies only
List<XmlChange> testChanges = diff.changesFor("//dependency[scope='test']", after);

// Changes affecting a specific plugin
List<XmlChange> pluginChanges = diff.changesFor(
    "//plugin[groupId='org.apache.maven.plugins']", after);
```

The `changesFor` method compiles the XPath expression, selects matching elements from the
provided document, and returns all changes at or under those elements' paths.

## Inspecting Changes

Each `XmlChange` provides:

```java
for (XmlChange change : diff.changes()) {
    ChangeType type = change.type();         // What kind of change
    String path = change.path();             // XPath-like location
    String before = change.beforeValue();    // Value before (null for additions)
    String after = change.afterValue();      // Value after (null for removals)
    boolean semantic = change.isSemantic();  // Does it affect meaning?

    // Access the actual DOM nodes for deeper inspection
    Node beforeNode = change.beforeNode();
    Node afterNode = change.afterNode();
}
```

## Use Cases

### Configuration File Auditing

Detect meaningful changes while ignoring reformatting:

```java
Document baseline = Document.of(Files.readString(baselinePath));
Document current = Document.of(Files.readString(currentPath));

DiffResult diff = XmlDiff.diff(baseline, current);
if (diff.hasSemanticChanges()) {
    System.out.println("Configuration has changed:");
    diff.semanticChanges().forEach(System.out::println);
} else if (diff.hasFormattingChanges()) {
    System.out.println("Only formatting differences (no functional impact)");
}
```

### Maven POM Comparison

Compare POM files with identity-aware dependency matching:

```java
DiffConfig mavenConfig = DiffConfig.builder()
    .matchBy("dependency", "groupId", "artifactId")
    .matchBy("plugin", "groupId", "artifactId")
    .matchBy("exclusion", "groupId", "artifactId")
    .build();

DiffResult diff = XmlDiff.diff(oldPom, newPom, mavenConfig);

// Find version changes
diff.semanticChanges().stream()
    .filter(c -> c.type() == ChangeType.TEXT_CHANGED
              && c.path().endsWith("/version"))
    .forEach(c -> System.out.println(
        c.path() + ": " + c.beforeValue() + " → " + c.afterValue()));
```

### Pre-Commit Validation

Verify that only expected changes were made:

```java
DiffResult diff = XmlDiff.diff(original, modified);

// Ensure no unexpected structural changes
List<XmlChange> structural = diff.semanticChanges().stream()
    .filter(c -> c.type() == ChangeType.ELEMENT_ADDED
              || c.type() == ChangeType.ELEMENT_REMOVED)
    .collect(Collectors.toList());

if (!structural.isEmpty()) {
    throw new ValidationException("Unexpected structural changes: " + structural);
}
```

## Next Steps

- [Formatting Preservation](../formatting-preservation/) — How DomTrip preserves formatting metadata
- [Lossless Parsing](../lossless-parsing/) — Understanding the parsing process
- [API Reference](../../api/) — Complete API documentation
