# XML Round-Trip Editor POC

A proof-of-concept Java implementation for a lossless XML editor that preserves formatting during round-trip operations.

## Overview

This POC demonstrates a lossless XML editor that can:
- Parse XML while preserving ALL formatting information (comments, whitespace, DTDs, processing instructions)
- Maintain a DOM-like tree structure with formatting metadata
- Perform basic editing operations while preserving original formatting
- Serialize back to XML with minimal changes to unmodified sections

## Key Features

### ✅ Implemented Features

1. **Lossless Parsing**
   - Preserves comments (including multi-line comments)
   - Preserves CDATA sections
   - Preserves XML declarations and processing instructions
   - Maintains text content with exact whitespace
   - **Preserves entity encoding** (e.g., `&lt;`, `&amp;`, `&quot;`)
   - **Preserves attribute quote styles** (single vs double quotes)
   - Tracks original element formatting

2. **DOM-like Tree Structure**
   - `XmlDocument` - Root container with XML declaration and DOCTYPE support
   - `XmlElement` - Elements with attributes and children
   - `XmlText` - Text nodes with CDATA support
   - `XmlComment` - Comment nodes
   - All nodes track modification state and position information

3. **Basic Editing Operations**
   - Add new elements with automatic indentation inference
   - Remove elements while preserving surrounding formatting
   - Modify element text content
   - Add/remove/modify attributes
   - Add comments

4. **Formatting Preservation**
   - Unmodified sections retain exact original formatting
   - Modified sections use inferred formatting patterns
   - Minimal change serialization - only changed parts are reformatted

### 📊 **Test Coverage: 100% (59/59 tests passing)** 🎉

- **Core Functionality**: 100% ✅ (12/12 original tests)
- **Indentation**: 100% ✅ (7/7 tests)
- **Performance**: 100% ✅ (6/6 tests)
- **Attribute Handling**: 100% ✅ (16/16 tests)
- **XML Conformance**: 100% ✅ (11/11 tests)
- **Entity Preservation**: 100% ✅ (7/7 tests)

### 🚀 **Recent Achievements**

1. **✅ Processing Instructions** - Full support for parsing and preserving PIs
2. **✅ Self-Closing Elements** - Automatic handling when children are added
3. **✅ Quote Preservation** - Perfect preservation of single vs double quotes
4. **✅ Entity Escaping** - Proper escaping of new content with special characters
5. **✅ Serializer Integration** - Full Attribute class integration in custom serialization

### 🔮 **Future Enhancements**

1. **DTD Support** - Document Type Definition parsing and validation
2. **Schema Validation** - XSD schema validation support
3. **Streaming Parser** - Large document streaming support
4. **XPath Support** - XPath query language support

### 🆕 Recent Enhancements

- **Attribute Class Refactoring**: Replaced 4 separate maps with a clean `Attribute` class for better maintainability
- **Quote Style Preservation**: Attribute quotes (single `'` vs double `"`) are maintained during round-trip ✅
- **Performance Optimization**: Added `toXml(StringBuilder)` methods to all nodes for better memory efficiency ✅
- **Comprehensive Test Suite**: Added 47 new tests covering indentation, performance, XML conformance, and attribute handling
- **Removed Position Tracking**: Cleaned up unused `int position` fields from Node class

### 🚀 **NEW: Enhanced API Features**

- **Builder Patterns**: Fluent APIs for creating complex XML structures
- **Factory Methods**: Convenient factory classes (`Elements`, `Documents`) for common patterns
- **Enhanced Navigation**: Stream-based navigation with `findChild()`, `descendants()`, `childElements()`
- **Namespace Support**: Comprehensive namespace handling with resolution and context management
- **Type-Safe Enums**: `QuoteStyle` and `WhitespaceStyle` enums replace magic values
- **Configuration Management**: `DomTripConfig` with presets (strict, lenient, pretty print)
- **Exception Hierarchy**: Specific exceptions (`ParseException`, `InvalidXmlException`)
- **Immutable Operations**: Safe attribute modifications with `withValue()`, `withQuoteStyle()`
- **Comprehensive Demos**: 6 demo classes showcasing all new features

## 🏷️ **Namespace Support**

DomTrip provides comprehensive XML namespace handling with full resolution and context management:

### Basic Namespace Operations

```java
// Create elements with namespaces
Element defaultNs = Elements.elementInNamespace("http://example.com/ns", "root");
Element prefixed = Elements.namespacedElement("ex", "element", "http://example.com/ns");
Element withText = Elements.textElementInNamespace("http://example.com/ns", "title", "My Title");

// Namespace-aware element methods
String localName = element.getLocalName();        // "element"
String prefix = element.getPrefix();              // "ex" or null
String namespaceURI = element.getNamespaceURI();  // "http://example.com/ns"
boolean inNamespace = element.isInNamespace("http://example.com/ns");
```

### Namespace-Aware Navigation

```java
// Find elements by namespace URI and local name
Optional<Element> child = root.findChildByNamespace("http://example.com/ns", "element");
Stream<Element> children = root.findChildrenByNamespace("http://example.com/ns", "item");
Stream<Element> descendants = root.descendantsByNamespace("http://example.com/ns", "data");

// Traditional navigation still works
Optional<Element> byName = root.findChild("ex:element");
```

### Namespace Context and Resolution

```java
// Get namespace context for an element
NamespaceContext context = element.getNamespaceContext();
String uri = context.getNamespaceURI("ex");           // Resolve prefix to URI
String prefix = context.getPrefix("http://example.com/ns"); // Resolve URI to prefix
Set<String> prefixes = context.getDeclaredPrefixes();

// Manual namespace resolution
String resolvedURI = NamespaceResolver.resolveNamespaceURI(element, "ex");
boolean inScope = NamespaceResolver.isNamespaceInScope(element, "http://example.com/ns");
```

### Builder Pattern with Namespaces

```java
Element soapEnvelope = Elements.builder("Envelope")
    .withNamespace("soap", "http://schemas.xmlsoap.org/soap/envelope/")
    .withNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance")
    .withDefaultNamespace("http://example.com/default")
    .withChild(Elements.namespacedElement("soap", "Header", "http://schemas.xmlsoap.org/soap/envelope/"))
    .build();
```

## Architecture

### Core Classes

- **`XmlNode`** - Base class for all XML nodes with formatting metadata
- **`XmlElement`** - Element implementation with attribute and child management
- **`XmlText`** - Text content with whitespace preservation
- **`XmlComment`** - Comment preservation
- **`XmlDocument`** - Document root with XML declaration handling
- **`XmlParser`** - Simple parser that preserves formatting information
- **`XmlSerializer`** - Serializer with minimal change output
- **`XmlEditor`** - High-level editing operations

### Key Design Principles

1. **Preservation First** - Original formatting is preserved unless explicitly modified
2. **Metadata Tracking** - Each node tracks its modification state and original formatting
3. **Minimal Changes** - Only modified sections are reformatted during serialization
4. **Simple API** - Easy-to-use editing operations that handle formatting automatically

## Usage Examples

### Basic Round-Trip
```java
XmlEditor editor = new XmlEditor(xmlString);
String result = editor.toXml(); // Identical to original if unmodified
```

### Adding Elements
```java
XmlElement parent = editor.findElement("dependencies");
XmlElement newDep = editor.addElement(parent, "dependency");
editor.addElement(newDep, "groupId", "org.example");
editor.addElement(newDep, "artifactId", "example-lib");
```

### Modifying Content
```java
XmlElement version = editor.findElement("version");
editor.setTextContent(version, "2.0.0");
```

### Adding Attributes
```java
XmlElement root = editor.getRootElement();
editor.setAttribute(root, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
```

## Testing

Run the comprehensive test suite:
```bash
mvn test
```

Run the interactive demos:
```bash
# Original lossless editing demo
mvn test-compile exec:java -Dexec.mainClass="eu.maveniverse.domtrip.demos.EditorDemo" -Dexec.classpathScope=test

# New API features overview
mvn test-compile exec:java -Dexec.mainClass="eu.maveniverse.domtrip.demos.ImprovedApiDemo" -Dexec.classpathScope=test

# Builder patterns demonstration
mvn test-compile exec:java -Dexec.mainClass="eu.maveniverse.domtrip.demos.BuilderPatternsDemo" -Dexec.classpathScope=test

# Enhanced navigation features
mvn test-compile exec:java -Dexec.mainClass="eu.maveniverse.domtrip.demos.NavigationDemo" -Dexec.classpathScope=test

# Configuration and serialization options
mvn test-compile exec:java -Dexec.mainClass="eu.maveniverse.domtrip.demos.ConfigurationDemo" -Dexec.classpathScope=test

# Comprehensive namespace handling
mvn test-compile exec:java -Dexec.mainClass="eu.maveniverse.domtrip.demos.NamespaceDemo" -Dexec.classpathScope=test
```

## Test Results

The POC successfully demonstrates:

- ✅ **Perfect round-trip** for unmodified XML
- ✅ **Comment preservation** including multi-line comments
- ✅ **CDATA preservation** with complex content
- ✅ **Whitespace preservation** in text content
- ✅ **Minimal change serialization** - only modified elements change
- ✅ **Automatic formatting** for new elements with indentation inference

## Example Output

**Original XML:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- Sample document -->
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <groupId>com.example</groupId>
    <version>1.0.0</version>
</project>
```

**After adding dependency and modifying version:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- Sample document -->
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <groupId>com.example</groupId>
    <version>1.0.1</version>
    <dependencies>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.13.2</version>
        </dependency>
    </dependencies>
</project>
```

Notice how:
- Original formatting is preserved (XML declaration, comments, indentation)
- Only modified elements (`version`) and new elements (`dependencies`) are changed
- New elements follow inferred indentation patterns

## Conclusion

This POC successfully demonstrates the core concepts of a lossless XML editor:

1. **Parsing with complete formatting preservation**
2. **Tree-based editing with modification tracking**
3. **Minimal-change serialization**
4. **Automatic formatting inference for new content**

The implementation provides a solid foundation that could be extended into a full-featured XML editing library with enhanced whitespace handling, better error recovery, and additional editing operations.
