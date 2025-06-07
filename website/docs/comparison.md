---
sidebar_position: 10
---

# Library Comparison

DomTrip offers unique advantages over traditional XML processing libraries. Here's how it compares to popular alternatives:

## Feature Comparison

<table className="comparison-table">
<thead>
<tr>
<th>Feature</th>
<th>DomTrip</th>
<th>DOM4J</th>
<th>JDOM</th>
<th>Java DOM</th>
<th>Jackson XML</th>
</tr>
</thead>
<tbody>
<tr>
<td><strong>Lossless Round-Trip</strong></td>
<td className="feature-yes">✅ Perfect</td>
<td className="feature-no">❌ No</td>
<td className="feature-no">❌ No</td>
<td className="feature-no">❌ No</td>
<td className="feature-no">❌ No</td>
</tr>
<tr>
<td><strong>Comment Preservation</strong></td>
<td className="feature-yes">✅ Full</td>
<td className="feature-yes">✅ Yes</td>
<td className="feature-yes">✅ Yes</td>
<td className="feature-yes">✅ Yes</td>
<td className="feature-no">❌ No</td>
</tr>
<tr>
<td><strong>Between-Element Whitespace</strong></td>
<td className="feature-yes">✅ Exact</td>
<td className="feature-partial">⚠️ Partial</td>
<td className="feature-yes">✅ Yes*</td>
<td className="feature-partial">⚠️ Limited</td>
<td className="feature-no">❌ No</td>
</tr>
<tr>
<td><strong>In-Element Whitespace</strong></td>
<td className="feature-yes">✅ Exact</td>
<td className="feature-no">❌ Lost</td>
<td className="feature-partial">⚠️ Config**</td>
<td className="feature-partial">⚠️ Limited</td>
<td className="feature-no">❌ No</td>
</tr>
<tr>
<td><strong>Entity Preservation</strong></td>
<td className="feature-yes">✅ Perfect</td>
<td className="feature-no">❌ No</td>
<td className="feature-no">❌ No</td>
<td className="feature-no">❌ No</td>
<td className="feature-no">❌ No</td>
</tr>
<tr>
<td><strong>Attribute Quote Style</strong></td>
<td className="feature-yes">✅ Preserved</td>
<td className="feature-no">❌ No</td>
<td className="feature-no">❌ No</td>
<td className="feature-no">❌ No</td>
<td className="feature-no">❌ No</td>
</tr>
<tr>
<td><strong>Attribute Order</strong></td>
<td className="feature-yes">✅ Preserved</td>
<td className="feature-no">❌ Lost</td>
<td className="feature-no">❌ Lost</td>
<td className="feature-no">❌ Lost</td>
<td className="feature-no">❌ No</td>
</tr>
<tr>
<td><strong>Modern Java API</strong></td>
<td className="feature-yes">✅ Java 17+</td>
<td className="feature-no">❌ Legacy</td>
<td className="feature-no">❌ Legacy</td>
<td className="feature-no">❌ Legacy</td>
<td className="feature-yes">✅ Modern</td>
</tr>
<tr>
<td><strong>Fluent Builders</strong></td>
<td className="feature-yes">✅ Full</td>
<td className="feature-no">❌ No</td>
<td className="feature-no">❌ No</td>
<td className="feature-no">❌ No</td>
<td className="feature-partial">⚠️ Limited</td>
</tr>
<tr>
<td><strong>Stream Navigation</strong></td>
<td className="feature-yes">✅ Native</td>
<td className="feature-no">❌ No</td>
<td className="feature-no">❌ No</td>
<td className="feature-no">❌ No</td>
<td className="feature-no">❌ No</td>
</tr>
<tr>
<td><strong>Namespace Support</strong></td>
<td className="feature-yes">✅ Comprehensive</td>
<td className="feature-yes">✅ Good</td>
<td className="feature-yes">✅ Good</td>
<td className="feature-yes">✅ Good</td>
<td className="feature-partial">⚠️ Basic</td>
</tr>
<tr>
<td><strong>Performance</strong></td>
<td className="feature-yes">✅ Optimized</td>
<td className="feature-yes">✅ Good</td>
<td className="feature-yes">✅ Good</td>
<td className="feature-partial">⚠️ Slow</td>
<td className="feature-yes">✅ Fast</td>
</tr>
</tbody>
</table>

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

Migrating from other libraries to DomTrip is straightforward. Check out our [Migration Guide](migration) for specific examples and patterns for each library.
