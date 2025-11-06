---
title: Library Comparison
description: DomTrip offers unique advantages over traditional XML processing libraries. Here's how it compares to popular alternatives.
layout: page
---

# Library Comparison

DomTrip offers unique advantages over traditional XML processing libraries. Here's how it compares to popular alternatives:

## Feature Comparison

| Feature | DomTrip | DOM4J | JDOM | Java DOM | Jackson XML |
|---------|---------|-------|------|----------|-------------|
| **Lossless Round-Trip** | ✅ Perfect | ❌ No | ❌ No | ❌ No | ❌ No |
| **Comment Preservation** | ✅ Full | ✅ Yes | ✅ Yes | ✅ Yes | ❌ No |
| **Between-Element Whitespace** | ✅ Exact | ⚠️ Partial | ✅ Yes* | ⚠️ Limited | ❌ No |
| **In-Element Whitespace** | ✅ Exact | ❌ Lost | ⚠️ Config** | ⚠️ Limited | ❌ No |
| **Entity Preservation** | ✅ Perfect | ❌ No | ❌ No | ❌ No | ❌ No |
| **Attribute Quote Style** | ✅ Preserved | ❌ No | ❌ No | ❌ No | ❌ No |
| **Attribute Order** | ✅ Preserved | ❌ Lost | ❌ Lost | ❌ Lost | ❌ No |
| **Modern Java API** | ✅ Java 17+ | ❌ Legacy | ❌ Legacy | ❌ Legacy | ✅ Modern |
| **Fluent Builders** | ✅ Full | ❌ No | ❌ No | ❌ No | ⚠️ Limited |
| **Stream Navigation** | ✅ Native | ❌ No | ❌ No | ❌ No | ❌ No |
| **Namespace Support** | ✅ Comprehensive | ✅ Good | ✅ Good | ✅ Good | ⚠️ Basic |
| **XML Conformance** | ⚠️ Partial | ✅ Full | ✅ Full | ✅ Full | ✅ Full |

**\* JDOM**: Use `Format.getRawFormat()` to preserve original whitespace between elements  
**\*\* JDOM**: Configure with `TextMode.PRESERVE` to maintain text content whitespace

## Detailed Comparison

### DomTrip vs DOM4J

**DomTrip Advantages:**
- **Perfect formatting preservation** - DOM4J loses in-element whitespace and many formatting details
- **Modern API** - Fluent builders, Stream navigation, Optional returns
- **Entity preservation** - Maintains `&lt;`, `&amp;`, etc. exactly as written
- **Quote style preservation** - Keeps single vs double quotes in attributes
- **Attribute order preservation** - Maintains original attribute ordering

**DOM4J Advantages:**
- **Mature ecosystem** - Longer history, more third-party integrations
- **XPath support** - Built-in XPath query capabilities
- **Larger community** - More Stack Overflow answers and tutorials
- **Comment preservation** - Can preserve XML comments during processing

### DomTrip vs JDOM

**DomTrip Advantages:**
- **Perfect lossless round-trip** - JDOM with `Format.getPrettyFormat()` reformats everything
- **No configuration needed** - Works losslessly out of the box
- **Entity and quote preservation** - JDOM normalizes these during serialization
- **Better API design** - Type-safe, fluent, modern Java patterns
- **Comprehensive namespace handling** - Built-in namespace context and resolution

**JDOM Advantages:**
- **Configurable whitespace handling** - `Format.getRawFormat()` can preserve between-element whitespace
- **Simplicity** - Easier learning curve for basic XML processing
- **Lightweight** - Smaller memory footprint for simple use cases
- **Wide adoption** - Used in many existing projects
- **Comment preservation** - Full support for XML comments

### DomTrip vs Java DOM

**DomTrip Advantages:**
- **Much simpler API** - No verbose factory patterns or checked exceptions
- **Perfect preservation** - Java DOM loses many formatting details
- **Modern design** - Built for Java 17+ with contemporary patterns
- **Better error handling** - Specific exception types, clear error messages

**Java DOM Advantages:**
- **Built-in** - Part of the JDK, no external dependencies
- **Standards compliant** - Follows W3C DOM specification exactly
- **Universal support** - Available everywhere Java runs

### DomTrip vs Jackson XML

**DomTrip Advantages:**
- **Document-oriented** - Designed for XML document editing, not object mapping
- **Formatting preservation** - Jackson focuses on data binding, not formatting
- **Comment support** - Jackson typically ignores comments
- **Manual control** - Fine-grained control over XML structure

**Jackson XML Advantages:**
- **Object mapping** - Automatic conversion between Java objects and XML
- **Performance** - Optimized for high-throughput data processing
- **Annotation-driven** - Declarative configuration via annotations
- **JSON compatibility** - Can switch between XML and JSON easily

## Use Case Recommendations

### Choose DomTrip When:
- ✅ **Editing existing XML files** (config files, POMs, etc.)
- ✅ **Building XML transformation tools**
- ✅ **Creating XML editors or IDEs**
- ✅ **Processing SOAP messages** with formatting requirements
- ✅ **Maintaining XML document integrity**

### Choose DOM4J When:
- ✅ **Working with legacy codebases** already using DOM4J
- ✅ **Need extensive XPath support**
- ✅ **Processing XML for data extraction** (formatting not important)

### Choose JDOM When:
- ✅ **Simple XML processing tasks**
- ✅ **Learning XML processing** (easier API)
- ✅ **Memory-constrained environments**

### Choose Java DOM When:
- ✅ **No external dependencies allowed**
- ✅ **Strict W3C DOM compliance required**
- ✅ **Integration with other DOM-based tools**

### Choose Jackson XML When:
- ✅ **Object-to-XML mapping**
- ✅ **High-performance data processing**
- ✅ **REST API development**
- ✅ **Need both JSON and XML support**

## Migration Guide

Migrating from other libraries to DomTrip is straightforward. Check out our [Migration Guide](migration/) for specific examples and patterns for each library.

## XML Conformance

DomTrip prioritizes **formatting preservation** over strict XML specification conformance. Based on comprehensive testing, here's what you need to know:

### Critical Limitation

**Numeric Character References in Attributes** ❌

Numeric character references (like `&#10;` for newline or `&#x3C;` for `<`) in attribute values are currently double-escaped, causing data loss:

```xml
<!-- Input -->
<root attr="line1&#10;line2"/>

<!-- Output (INCORRECT) -->
<root attr="line1&amp;#10;line2"/>
```

**Impact**: The newline character is lost. This is a known bug that will be fixed in a future release.

**Workaround**: Use CDATA sections or standard entities instead of numeric character references in attributes.

### Minor Limitations

**DOCTYPE Formatting:**
- DOCTYPE declarations are preserved but may have an extra newline added
- No data loss, just minor formatting difference

**XML Declaration Attributes:**
- XML declaration is preserved as-is, but `version` and `standalone` attributes are not parsed into the Document object
- No data loss, declaration round-trips correctly

**Attribute Quote Normalization:**
- `&quot;` in single-quoted attributes becomes literal `"` (semantically equivalent)

### What Works Perfectly ✅

DomTrip provides **perfect round-tripping** for:

- ✅ **Standard XML Entities** - `&lt;`, `&gt;`, `&amp;`, `&quot;`, `&apos;`
- ✅ **CDATA Sections** - Including CDATA with XML-like content
- ✅ **Comments** - Single-line and multi-line comments
- ✅ **Whitespace** - Exact preservation of spaces, tabs, newlines
- ✅ **Namespaces** - Default and prefixed namespaces, including overriding
- ✅ **Attribute Order** - Maintains exact attribute order
- ✅ **Attribute Quote Style** - Preserves single vs. double quotes
- ✅ **Empty Attributes** - Preserves empty attribute values
- ✅ **Processing Instructions** - Including xml-stylesheet and custom PIs
- ✅ **DOCTYPE Declarations** - System, public, and internal subsets (with minor formatting)
- ✅ **Encoding Declarations** - UTF-8, UTF-16, ISO-8859-1, etc.

### Recommendation

**Use DomTrip when:**
- ✅ Editing configuration files (Maven POMs, Spring configs, etc.)
- ✅ Transforming documents while preserving formatting
- ✅ Working with human-edited XML that needs to stay readable
- ✅ You need perfect whitespace and comment preservation
- ✅ You need to maintain attribute order and quote styles

**Avoid DomTrip when:**
- ❌ You need numeric character references in attributes (until fixed)
- ❌ You need strict XML 1.1 specification compliance
- ❌ You need DTD validation or entity expansion
- ❌ You need programmatic access to XML declaration attributes

**For strict XML conformance**, consider:
- **Java DOM** - Full W3C DOM specification compliance
- **DOM4J** or **JDOM** - Mature libraries with comprehensive XML support

## Next Steps

- 📚 [Migration Guide](migration/) - Moving from other XML libraries
- 🚀 [Quick Start](../../docs/getting-started/quick-start/) - Get started with DomTrip
- 📖 [Examples](../../examples/) - Real-world usage examples
