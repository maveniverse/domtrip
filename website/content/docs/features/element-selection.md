---
title: "Element Selection"
description: "Find elements using mini-XPath expressions or the programmatic ElementQuery API"
weight: 55
layout: page
---

# Element Selection

DomTrip provides two complementary APIs for finding elements in an XML document: **mini-XPath expressions** for concise string-based queries, and the **ElementQuery** API for programmatic, type-safe queries.

## Mini-XPath Expressions

The `select()` and `selectFirst()` methods accept mini-XPath expression strings, providing a familiar and concise way to locate elements.

### Basic Usage

```java
Editor editor = new Editor(Document.of(xml));
Element root = editor.root();

// Find all dependencies
List<Element> deps = root.select("dependencies/dependency");

// Find first matching element
Optional<Element> junit = root.selectFirst("//dependency[groupId='junit']");

// Or query from the editor (evaluates against root)
List<Element> allDeps = editor.select("//dependency");
Optional<Element> version = editor.selectFirst("project/version");
```

### Path Navigation

Navigate the document tree using path expressions:

```java
// Direct child path
root.select("dependencies/dependency/groupId");

// Descendant search (anywhere below)
root.select("//groupId");

// Mixed: child then descendant
root.select("dependencies//groupId");

// Wildcard: any element
root.select("dependencies/*/groupId");

// Self and parent
element.select(".");           // the element itself
element.select("dependency[1]/.."); // parent of first dependency
```

### Predicates

Filter elements using predicates inside square brackets:

```java
// Attribute presence
root.select("//item[@scope]");

// Attribute value (single or double quotes)
root.select("//dependency[@scope='test']");
root.select("//dependency[@scope=\"test\"]");

// Child text content
root.select("//dependency[groupId='org.junit']");

// Positional (1-based)
root.select("dependencies/dependency[1]");     // first
root.select("dependencies/dependency[last()]"); // last

// Multiple predicates (AND logic)
root.select("//dependency[scope='test'][artifactId='junit-api']");
```

### Namespace Support

Expressions match both prefixed and local names:

```java
// Given: <soap:Envelope xmlns:soap="..."><soap:Body>...
Element envelope = editor.root();

// Match by prefix
envelope.select("soap:Body/soap:Fault");

// Match by local name (also works)
envelope.select("Body");

// Descendant search with prefix
envelope.select("//soap:Fault");
```

### Compiled Expressions

For repeated evaluation, compile once and reuse:

```java
// Compile once
XPathExpression expr = XPathExpression.compile("//dependency[@scope='test']");

// Evaluate many times against different contexts
List<Element> results1 = expr.select(root1);
List<Element> results2 = expr.select(root2);
Optional<Element> first = expr.selectFirst(root1);
```

### Supported Expression Syntax

| Expression | Description |
|---|---|
| `foo/bar/baz` | Direct child path |
| `//foo` | Descendant search (anywhere below) |
| `foo//bar` | `bar` anywhere under `foo` children |
| `.` | Current element |
| `..` | Parent element |
| `*` | Any element (wildcard) |
| `foo[@attr]` | Element with attribute present |
| `foo[@attr='val']` | Element with attribute value |
| `foo[bar='text']` | Element with child text content |
| `foo[1]` | First element (1-based) |
| `foo[last()]` | Last element |

### What is NOT Supported

This is a practical subset of XPath, not a full implementation:

- Full axis specifiers (`preceding-sibling::`, `ancestor::`)
- XPath functions (`contains()`, `normalize-space()`)
- Boolean operators (`and`, `or`)
- Arithmetic operators
- Union operator (`|`)

## ElementQuery API

The `ElementQuery` API provides a programmatic, fluent interface for finding elements with compile-time type safety.

### Basic Usage

```java
// Start a query from any element
Optional<Element> result = root.query()
    .withName("dependency")
    .withAttribute("scope", "test")
    .first();

// Get all matches
List<Element> matches = root.query()
    .withName("dependency")
    .toList();

// Check existence
boolean hasTests = root.query()
    .withName("dependency")
    .withAttribute("scope", "test")
    .exists();
```

### Available Filters

```java
root.query()
    .withName("dependency")             // match by element name
    .withQName(qname)                   // match by qualified name
    .withNamespace("http://...")         // match by namespace URI
    .withAttribute("scope")             // has attribute
    .withAttribute("scope", "test")     // attribute equals value
    .withTextContent("4.0.0")           // exact text content
    .containingText("example")          // text contains substring
    .atDepth(2)                         // at specific nesting depth
    .withChildren()                     // has child elements
    .withoutChildren()                  // leaf elements only
    .where(e -> customCheck(e))         // custom predicate
    .first();                           // terminal: get first match
```

### Terminal Operations

```java
ElementQuery query = root.query().withName("dependency");

Optional<Element> first = query.first();   // first match
Stream<Element> stream = query.all();      // stream of matches
List<Element> list = query.toList();       // list of matches
long count = query.count();                // count matches
boolean found = query.exists();            // any match exists?
```

## Choosing Between XPath and ElementQuery

| Use Case | Recommended API |
|---|---|
| Quick one-off queries | `select("//dependency")` |
| Queries from configuration/user input | `select(userExpression)` |
| Complex multi-criteria filtering | `query().withName(...).withAttribute(...)` |
| Custom predicate logic | `query().where(e -> ...)` |
| Repeated evaluation of same query | `XPathExpression.compile(...)` |
| Depth-limited searches | `query().atDepth(2)` |

Both APIs can be used together:

```java
// Find dependencies with XPath, then refine with ElementQuery
List<Element> testDeps = root.select("//dependency[@scope='test']");

// Or use ElementQuery for complex conditions XPath can't express
Optional<Element> match = root.query()
    .withName("dependency")
    .where(dep -> {
        String groupId = dep.child("groupId").map(Element::textContentTrimmed).orElse("");
        return groupId.startsWith("org.junit");
    })
    .first();
```
