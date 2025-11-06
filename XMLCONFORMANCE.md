# XML Conformance and Round-Tripping Analysis

This document provides a detailed analysis of DomTrip's XML conformance and round-tripping capabilities based on comprehensive testing.

## Summary

DomTrip is designed for **perfect formatting preservation** rather than strict XML specification conformance. It excels at round-tripping common XML documents while maintaining exact formatting, but has specific limitations with advanced XML features.

## Round-Tripping Issues (Data Loss)

These are issues where data is lost or corrupted during parsing and serialization:

### 1. Numeric Character References in Attributes ❌ CRITICAL

**Issue**: Numeric character references in attribute values are double-escaped, causing data loss.

```xml
<!-- Input -->
<root attr="line1&#10;line2"/>

<!-- Output -->
<root attr="line1&amp;#10;line2"/>
```

**Impact**: The newline character (represented by `&#10;`) is lost. The attribute value becomes the literal string `"line1&#10;line2"` instead of `"line1\nline2"`.

**Root Cause**: The `Text.unescapeTextContent()` method only handles the five standard XML entities (`&lt;`, `&gt;`, `&amp;`, `&quot;`, `&apos;`) and doesn't decode numeric character references.

**Status**: **NEEDS FIX** - This is a data loss issue.

### 2. XML Declaration Attributes Not Parsed ⚠️ MINOR

**Issue**: XML declaration attributes (version, standalone) are not parsed when using `Document.of(String)`.

```xml
<!-- Input -->
<?xml version="1.1" encoding="UTF-8" standalone="yes"?>
<root/>

<!-- Parsed values -->
doc.version()      // Returns "1.0" (default)
doc.isStandalone() // Returns false (default)
doc.encoding()     // Returns "UTF-8" (correctly parsed)
```

**Impact**: The XML declaration is preserved as-is in the output, but the Document object doesn't reflect the actual version or standalone values from the input.

**Workaround**: The declaration is preserved verbatim, so round-tripping works. Only programmatic access to these values is affected.

**Status**: **ACCEPTABLE** - No data loss, declaration is preserved.

## Minor Formatting Differences (Acceptable)

These are minor formatting changes that don't affect data integrity:

### 1. DOCTYPE Extra Newline ✅ ACCEPTABLE

**Issue**: An extra newline is added after DOCTYPE declarations.

```xml
<!-- Input -->
<?xml version="1.0"?>
<!DOCTYPE root SYSTEM "example.dtd">
<root/>

<!-- Output -->
<?xml version="1.0"?>
<!DOCTYPE root SYSTEM "example.dtd">

<root/>
```

**Impact**: Minimal - just an extra blank line.

**Status**: **ACCEPTABLE** - No data loss, minor formatting difference.

### 2. Attribute Quote Normalization ✅ ACCEPTABLE

**Issue**: `&quot;` in single-quoted attributes becomes literal `"`.

```xml
<!-- Input -->
<root attr='value with &quot;quotes&quot;'/>

<!-- Output -->
<root attr='value with "quotes"'/>
```

**Impact**: None - semantically equivalent, and actually more readable.

**Status**: **ACCEPTABLE** - Semantically equivalent.

## What Works Perfectly ✅

The following features round-trip with **perfect fidelity**:

### Standard XML Entities
```xml
<root>&lt;&gt;&amp;&quot;&apos;</root>
```
✅ Preserved exactly

### CDATA Sections
```xml
<root><![CDATA[<tag> & special]]></root>
```
✅ Preserved exactly, including CDATA with XML-like content

### Comments
```xml
<!-- comment -->
<root/>
```
✅ Preserved exactly, including multi-line comments

### Whitespace
```xml
<root>  text with   spaces  </root>
```
✅ Preserved exactly, including tabs, newlines, and multiple spaces

### Namespaces
```xml
<root xmlns="http://example.com" xmlns:ns="http://ns.com">
  <ns:element/>
</root>
```
✅ Preserved exactly, including default namespace overriding

### Attribute Order
```xml
<root z="3" a="1" m="2"/>
```
✅ Preserved exactly

### Attribute Quote Style
```xml
<root attr1='single' attr2="double"/>
```
✅ Preserved exactly

### Empty Attributes
```xml
<root attr=""/>
```
✅ Preserved exactly

### Processing Instructions
```xml
<?xml-stylesheet type="text/xsl" href="style.xsl"?>
<root/>
```
✅ Preserved exactly

### DOCTYPE Declarations
```xml
<!DOCTYPE root SYSTEM "example.dtd">
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!DOCTYPE root [
  <!ENTITY custom "Custom Value">
]>
```
✅ Preserved (with minor extra newline)

## Recommendations

### Use DomTrip When:
- ✅ Editing configuration files (Maven POMs, Spring configs, etc.)
- ✅ Transforming documents while preserving formatting
- ✅ Working with human-edited XML that needs to stay readable
- ✅ You need perfect whitespace and comment preservation
- ✅ You need to maintain attribute order and quote styles

### Avoid DomTrip When:
- ❌ You need to parse attributes with numeric character references (until fixed)
- ❌ You need strict XML 1.1 specification compliance
- ❌ You need DTD validation or entity expansion
- ❌ You need programmatic access to XML declaration attributes

## Action Items

1. **FIX**: Add numeric character reference support to `Text.unescapeTextContent()`
2. **CONSIDER**: Parse XML declaration attributes when using `Document.of(String)`
3. **DOCUMENT**: Update website with accurate limitations

