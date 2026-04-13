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
| **Between-Element Whitespace** | ✅ Exact | ✅ Yes* | ✅ Yes* | ⚠️ Limited | ❌ No |
| **In-Element Whitespace** | ✅ Exact | ✅ Yes* | ⚠️ Config** | ⚠️ Limited | ❌ No |
| **Entity Preservation** | ✅ Perfect | ❌ No | ❌ No | ❌ No | ❌ No |
| **Numeric Char Refs** | ✅ Perfect | ❌ No | ❌ No | ❌ No | ❌ No |
| **Attribute Quote Style** | ✅ Preserved | ❌ No | ❌ No | ❌ No | ❌ No |
| **Attribute Order** | ✅ Preserved | ✅ Yes | ✅ Yes | ❌ Lost | ❌ No |
| **Modern Java API** | ✅ Java 8+ | ❌ Legacy | ❌ Legacy | ❌ Legacy | ✅ Modern |
| **Fluent Builders** | ✅ Full | ❌ No | ❌ No | ❌ No | ⚠️ Limited |
| **Stream Navigation** | ✅ Native | ❌ No | ❌ No | ❌ No | ❌ No |
| **XPath Queries** | ✅ Full XPath 1.0† | ✅ Full XPath | ✅ XPath via JAXP | ✅ Full XPath | ❌ No |
| **Namespace Support** | ✅ Comprehensive | ✅ Good | ✅ Good | ✅ Good | ⚠️ Basic |
| **XML Spec Compliance** | ✅ Full | ✅ Full | ✅ Full | ✅ Full | ✅ Full |

**\* DOM4J/JDOM**: Use compact/raw format with no trimming to preserve whitespace  
**\*\* JDOM**: Configure with `TextMode.PRESERVE` to maintain text content whitespace  
**† DomTrip**: Built-in mini-XPath for common queries; full XPath 1.0 via optional `domtrip-jaxen` module

## Detailed Comparison

### DomTrip vs DOM4J

**DomTrip Advantages:**
- **Perfect formatting preservation** - DOM4J with best-effort settings still loses prolog whitespace, quote styles, entity representation, and empty element styles
- **Modern API** - Fluent builders, Stream navigation, Optional returns
- **Entity preservation** - Maintains `&lt;`, `&amp;`, etc. exactly as written
- **Quote style preservation** - Keeps single vs double quotes in attributes
- **Numeric character reference preservation** - DOM4J decodes these through SAX
- **No configuration needed** - Works losslessly out of the box

**DOM4J Advantages:**
- **Good whitespace handling** - With compact format and no trimming, preserves inner and between-element whitespace, CDATA sections, and namespace declarations
- **Mature ecosystem** - Longer history, more third-party integrations
- **Built-in XPath** - XPath 1.0 is built into DOM4J with no extra module (DomTrip offers built-in mini-XPath plus full XPath 1.0 via the optional Jaxen module)
- **Larger community** - More Stack Overflow answers and tutorials

### DomTrip vs JDOM

**DomTrip Advantages:**
- **Perfect lossless round-trip** - Even JDOM's best settings (`getRawFormat()` + `TextMode.PRESERVE` + `omitDeclaration`) still lose formatting details like quote styles, inter-attribute whitespace, entity representation, and numeric character references
- **No configuration needed** - Works losslessly out of the box
- **Quote style preservation** - JDOM always normalizes to double quotes
- **Entity and numeric char ref preservation** - JDOM decodes these through SAX and re-encodes differently
- **Better API design** - Type-safe, fluent, modern Java patterns

**JDOM Advantages:**
- **Configurable whitespace handling** - `Format.getRawFormat()` with `TextMode.PRESERVE` can preserve between-element whitespace and mixed content
- **Simplicity** - Easier learning curve for basic XML processing
- **Lightweight** - Smaller memory footprint for simple use cases
- **Wide adoption** - Used in many existing projects

Note: JDOM 2.0.6.1 is the latest and final release (Dec 2021). The project is no longer actively developed.

### DomTrip vs Java DOM

**DomTrip Advantages:**
- **Much simpler API** - No verbose factory patterns or checked exceptions
- **Perfect preservation** - Java DOM loses many formatting details
- **Modern design** - Built for Java 8+ with contemporary patterns
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

Migrating from other libraries to DomTrip is straightforward. Check out our [Migration Guide](../migration/) for specific examples and patterns for each library.

## XML Conformance and Round-Tripping

DomTrip achieves **both** full XML 1.0 spec compliance **and** perfect round-tripping. API-reported values (e.g., `textContent()`, `attribute()`) conform to the XML specification, while serialization preserves the original formatting for lossless round-tripping.

Specifically, DomTrip implements:
- **Line ending normalization (§2.11)** — `textContent()` normalizes `\r\n` and `\r` to `\n`, while serialization preserves original line endings
- **Attribute value normalization (§3.3.3)** — `attribute()` normalizes tab, CR, and LF to space, while serialization preserves original character references

Based on comprehensive testing with 1300+ passing tests, here's what you need to know:

### Perfect Round-Tripping ✅

DomTrip achieves **zero data loss** and perfect round-tripping for:

- ✅ **Standard XML Entities** - `&lt;`, `&gt;`, `&amp;`, `&quot;`, `&apos;`
- ✅ **Numeric Character References** - Both decimal (`&#10;`) and hexadecimal (`&#x3C;`) formats
- ✅ **CDATA Sections** - Including CDATA with XML-like content
- ✅ **Comments** - Single-line and multi-line comments
- ✅ **Whitespace** - Exact preservation of spaces, tabs, newlines
- ✅ **Namespaces** - Default and prefixed namespaces, including overriding
- ✅ **Attribute Order** - Maintains exact attribute order
- ✅ **Attribute Quote Style** - Preserves single vs. double quotes
- ✅ **Empty Attributes** - Preserves empty attribute values
- ✅ **Processing Instructions** - Including xml-stylesheet and custom PIs
- ✅ **DOCTYPE Declarations** - System, public, and internal subsets with perfect formatting
- ✅ **Encoding Declarations** - UTF-8, UTF-16, ISO-8859-1, etc.

### Example: Perfect Round-Tripping

```xml
<!-- Input -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE root SYSTEM "example.dtd">
<root attr="line1&#10;line2" style='color: &quot;red&quot;'>
  <![CDATA[<special> & content]]>
  <!-- comment -->
</root>

<!-- Output (IDENTICAL) -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE root SYSTEM "example.dtd">
<root attr="line1&#10;line2" style='color: &quot;red&quot;'>
  <![CDATA[<special> & content]]>
  <!-- comment -->
</root>
```

### Recommendation

**Use DomTrip when:**
- ✅ Editing configuration files (Maven POMs, Spring configs, etc.)
- ✅ Transforming documents while preserving formatting
- ✅ Working with human-edited XML that needs to stay readable
- ✅ You need perfect whitespace and comment preservation
- ✅ You need to maintain attribute order and quote styles
- ✅ You need numeric character references preserved exactly

**Consider other libraries when:**
- ⚠️ You need DTD validation or entity expansion
- ⚠️ You need a validating parser

## Next Steps

- 📚 [Migration Guide](../migration/) - Moving from other XML libraries
- 🚀 [Quick Start](../../getting-started/quick-start/) - Get started with DomTrip
- 📖 [Examples](../../../examples/) - Real-world usage examples
