---
title: Features
description: Comprehensive overview of DomTrip's XML processing features
layout: page
---

# DomTrip Features

DomTrip provides a comprehensive set of features for XML processing with a focus on **lossless parsing** and **formatting preservation**. This section covers all the key capabilities that make DomTrip unique.

## Core Features

### [Lossless Parsing](lossless-parsing/)
DomTrip preserves all formatting information during XML parsing, ensuring perfect round-trip editing.

- **Whitespace preservation** (spaces, tabs, newlines)
- **Comment positioning** maintained
- **Processing instruction** handling
- **Document structure** integrity

### [Formatting Preservation](formatting-preservation/)
Maintain the original look and feel of your XML documents.

- **Indentation styles** preserved
- **Line ending consistency**
- **Attribute formatting** maintained
- **Element spacing** retained

### [Namespace Support](namespace-support/)
Comprehensive XML namespace handling with prefix preservation.

- **Namespace declarations** preserved
- **Prefix consistency** maintained
- **Namespace-aware parsing**
- **Qualified name handling**

### [Element Positioning](element-positioning/)
Advanced element positioning and manipulation capabilities.

- **Precise element placement**
- **Sibling ordering** control
- **Parent-child relationships**
- **Document structure** management

### [Element Selection](element-selection/)
Powerful element selection and querying using mini-XPath and programmatic APIs.

- **Mini-XPath expressions** for concise string-based queries
- **ElementQuery API** for type-safe programmatic queries
- **Attribute-based filtering** with predicate support
- **Descendant search** with `//` syntax

### [Commenting](commenting/)
Rich support for XML comments with positioning preservation.

- **Comment placement** preserved
- **Multi-line comments** supported
- **Comment formatting** maintained
- **Inline and block** comment styles

### [Stream Support](stream-support/)
Efficient processing of large XML documents with streaming capabilities.

- **Memory-efficient** processing
- **Large document** handling
- **Streaming API** support
- **Performance optimization**

### [XML-Aware Structural Diff](xml-diff/)
Compare two XML documents and detect both semantic and formatting-only changes.

- **Semantic vs. formatting** change classification
- **Configurable element matching** by identity keys
- **Move detection** for reordered elements
- **Path-based filtering** of changes

### [Visitor and Walker Patterns](visitor-pattern/)
Structured depth-first tree traversal with enter/exit lifecycle callbacks.

- **Depth-first traversal** with flow control
- **Enter/exit callbacks** for context tracking
- **Subtree skipping** and early termination
- **Lambda-friendly** TreeWalker API

### [SAX Event Output](sax-output/)
Emit SAX events from a domtrip document for integration with SAX-based XML processing pipelines.

- **XSLT transformation** via SAXSource
- **Schema validation** without re-parsing
- **Content pipeline** integration
- **Namespace-aware** event emission

### [StAX Stream Reader](stax-bridge/)
Expose a domtrip document as a StAX XMLStreamReader for pull-based XML processing pipelines.

- **Pull-based** cursor API
- **JAXP integration** via StAXSource
- **Namespace-aware** with full NamespaceContext
- **Selective reading** with getElementText() and nextTag()

## Why These Features Matter

### ✅ **Perfect Round-Trip Editing**
Edit XML files without losing any formatting information, making DomTrip ideal for configuration files and documents where formatting matters.

### ✅ **Developer-Friendly**
Clean, intuitive APIs that make XML processing straightforward and enjoyable.

### ✅ **Production-Ready**
Robust, well-tested features suitable for enterprise applications and critical systems.

### ✅ **Standards Compliant**
Full XML specification compliance with modern Java best practices.

## Getting Started

Ready to explore these features? Check out:

- **[Getting Started Guide](../getting-started/)** - Basic usage examples
- **[API Reference](../api/)** - Complete API documentation
- **[Examples](../../examples/)** - Practical code examples

Each feature page provides detailed explanations, code examples, and best practices for using that specific capability in your applications.
