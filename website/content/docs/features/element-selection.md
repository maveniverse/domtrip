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

## Full XPath 1.0 via Jaxen Module

For applications that need full XPath 1.0 support -- including boolean operators (`and`, `or`), functions (`contains()`, `starts-with()`, `not()`), inequality (`!=`), union (`|`), and full axis navigation -- DomTrip provides an optional **Jaxen** module.

### Setup

Add the `domtrip-jaxen` dependency alongside `domtrip-core`:

```xml
<dependency>
  <groupId>eu.maveniverse.maven.domtrip</groupId>
  <artifactId>domtrip-jaxen</artifactId>
  <version>1.3.0</version>
</dependency>
```

### Quick Queries

Use the `XPath` utility class for one-shot queries:

```java
import eu.maveniverse.domtrip.jaxen.XPath;

// Boolean operators
List<Element> testJunit = XPath.select(root,
    "//dependency[scope='test' and groupId='junit']");

// String functions
List<Element> springDeps = XPath.select(root,
    "//dependency[contains(groupId, 'spring')]");
List<Element> orgDeps = XPath.select(root,
    "//dependency[starts-with(groupId, 'org.')]");

// Negation
List<Element> nonOptional = XPath.select(root,
    "//dependency[not(@optional)]");

// Inequality
List<Element> nonTest = XPath.select(root,
    "//dependency[@scope!='test']");

// Union (combine results from multiple paths)
List<Element> ids = XPath.select(root,
    "//groupId | //artifactId");

// First match
Optional<Element> first = XPath.selectFirst(root,
    "//dependency[scope='test']");
```

### Compiled Expressions

For repeated evaluation, compile the expression once:

```java
import eu.maveniverse.domtrip.jaxen.DomTripXPath;
import eu.maveniverse.domtrip.jaxen.XPath;

DomTripXPath expr = XPath.compile("//dependency[scope='test']");
List<Element> results1 = expr.selectElements(root1);
List<Element> results2 = expr.selectElements(root2);
```

### Full Axis Navigation

Jaxen supports all XPath axes that mini-XPath does not:

```java
// Following siblings
XPath.select(element, "following-sibling::dependency");

// Preceding siblings
XPath.select(element, "preceding-sibling::*");

// Ancestors
XPath.select(element, "ancestor::project");

// Descendants (explicit axis)
XPath.select(root, "descendant::groupId");
```

### Namespace-Aware Queries

Register namespace prefixes for namespace-aware queries:

```java
DomTripXPath xpath = XPath.compile("//soap:Body/soap:Fault");
xpath.addNamespace("soap", "http://schemas.xmlsoap.org/soap/envelope/");
List<Element> results = xpath.selectElements(root);
```

### Mini-XPath vs Jaxen

| Aspect | Mini-XPath (core) | Jaxen Module |
|---|---|---|
| **Dependencies** | None (built into core) | Requires `jaxen:jaxen` |
| **Boolean operators** | Not supported | `and`, `or` |
| **Functions** | `last()` only | `contains()`, `starts-with()`, `not()`, `string-length()`, etc. |
| **Inequality** | Not supported | `!=` |
| **Union** | Not supported | `\|` |
| **Full axes** | `..` only | `ancestor::`, `following-sibling::`, `preceding::`, etc. |
| **Use case** | Simple queries, zero-dep environments | Complex queries, full XPath 1.0 |

Use mini-XPath for simple queries where you want zero dependencies. Use the Jaxen module when you need the full power of XPath 1.0.
