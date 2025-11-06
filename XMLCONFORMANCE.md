# XML Conformance and Round-Tripping Analysis

This document provides a detailed analysis of DomTrip's XML conformance and round-tripping capabilities based on comprehensive testing.

## Summary

DomTrip is designed for **perfect formatting preservation** rather than strict XML specification conformance. It excels at round-tripping common XML documents while maintaining exact formatting, but has specific limitations with advanced XML features.

## Round-Tripping Issues (Data Loss)

**ALL FIXED!** ✅

As of the latest version, DomTrip has **NO data loss issues**. All previously identified round-tripping problems have been resolved:

### 1. Numeric Character References in Attributes ✅ FIXED

**Was**: Numeric character references in attribute values were double-escaped, causing data loss.

**Now**: Numeric character references are properly decoded AND preserved for perfect round-tripping.

```xml
<!-- Input -->
<root attr="line1&#10;line2"/>

<!-- Output (PERFECT) -->
<root attr="line1&#10;line2"/>

<!-- Decoded value -->
root.attribute("attr") // Returns "line1\nline2" (with actual newline)
```

**Fix**: Enhanced `Text.unescapeTextContent()` to handle numeric character references (both decimal `&#DDDD;` and hexadecimal `&#xHHHH;`). Also fixed element modification tracking to preserve raw attribute values.

### 2. DOCTYPE Extra Newline ✅ FIXED

**Was**: DOCTYPE declarations had an extra newline added after them.

**Now**: DOCTYPE declarations round-trip perfectly with exact whitespace preservation.

```xml
<!-- Input -->
<?xml version="1.0"?>
<!DOCTYPE root SYSTEM "example.dtd">
<root/>

<!-- Output (PERFECT) -->
<?xml version="1.0"?>
<!DOCTYPE root SYSTEM "example.dtd">
<root/>
```

**Fix**: Added `doctypePrecedingWhitespace` field to Document to store whitespace before DOCTYPE separately, eliminating the hardcoded newline.

### 3. Attribute Quote Entity Normalization ✅ FIXED

**Was**: `&quot;` in single-quoted attributes became literal `"`.

**Now**: All entities in attributes are preserved exactly as written.

```xml
<!-- Input -->
<root attr='value with &quot;quotes&quot;'/>

<!-- Output (PERFECT) -->
<root attr='value with &quot;quotes&quot;'/>
```

**Fix**: Same fix as #1 - proper raw value preservation.

## Minor Limitations (Acceptable)

These are minor limitations that don't affect round-tripping but may affect programmatic access:

### 1. XML Declaration Attributes Not Parsed ⚠️ MINOR

**Issue**: XML declaration attributes (version, standalone) are not parsed when using `Document.of(String)`.

```xml
<!-- Input -->
<?xml version="1.1" encoding="UTF-8" standalone="yes"?>
<root/>

<!-- Parsed values -->
doc.version()      // Returns "1.0" (default)
doc.isStandalone() // Returns false (default)
doc.encoding()     // Returns "UTF-8" (correctly parsed)

<!-- Output (PERFECT round-trip) -->
<?xml version="1.1" encoding="UTF-8" standalone="yes"?>
<root/>
```

**Impact**: The XML declaration is preserved perfectly in the output, but the Document object doesn't reflect the actual version or standalone values from the input. This only affects programmatic access to these values, not round-tripping.

**Status**: **ACCEPTABLE** - No data loss, perfect round-tripping, only programmatic access affected.

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

## Summary

DomTrip now provides **perfect round-tripping** for all common XML features with **zero data loss**. The only remaining limitation is that XML declaration attributes (version, standalone) are not parsed into the Document object, but they are still preserved perfectly in the output.

## Action Items

1. ✅ **DONE**: Add numeric character reference support to `Text.unescapeTextContent()`
2. ✅ **DONE**: Fix element modification tracking to preserve raw attribute values
3. ✅ **DONE**: Fix DOCTYPE extra newline issue
4. **CONSIDER**: Parse XML declaration attributes when using `Document.of(String)` (low priority - no data loss)
5. ✅ **DONE**: Update website with accurate limitations

