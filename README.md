# DomTrip

**Lossless XML Editing for Java**

[![Maven Central](https://img.shields.io/maven-central/v/eu.maveniverse.maven.domtrip/domtrip-core.svg)](https://central.sonatype.com/artifact/eu.maveniverse.maven.domtrip/domtrip-core)
[![GitHub](https://img.shields.io/github/license/maveniverse/domtrip.svg)](https://github.com/maveniverse/domtrip/blob/master/LICENSE)
[![Documentation](https://img.shields.io/badge/docs-website-blue.svg)](https://maveniverse.github.io/domtrip/)

DomTrip is a Java library for lossless XML editing that preserves every detail of your XML documents during round-trip operations. Perfect for configuration file editing, document transformation, and any scenario where maintaining original formatting is crucial.

## Why DomTrip?

- **Perfect Round-Trip**: Preserves comments, whitespace, entity encoding, attribute quote styles, and formatting
- **Easy Editing**: Make changes while keeping original formatting intact
- **Modern API**: Built for Java 8+ with fluent builders, Stream-based navigation, and type-safe configuration
- **Namespace Aware**: Comprehensive XML namespace support with resolution and context management
- **XPath Queries**: Built-in mini-XPath for common queries, plus full XPath 1.0 via the Jaxen module
- **Structural Diff**: XML-aware diff that distinguishes semantic changes from formatting-only changes
- **SAX Interop**: Emit SAX events from DomTrip documents for JAXP pipeline integration
- **JPMS Ready**: Ships with `module-info.java` descriptors
- **Well Tested**: 1900+ tests covering XML conformance, round-trip fidelity, and edge cases

## Quick Start

### Installation

Add DomTrip to your Maven project:

```xml
<dependency>
    <groupId>eu.maveniverse.maven.domtrip</groupId>
    <artifactId>domtrip-core</artifactId>
    <version>1.2.0</version>
</dependency>
```

For Maven POM editing (includes core):

```xml
<dependency>
    <groupId>eu.maveniverse.maven.domtrip</groupId>
    <artifactId>domtrip-maven</artifactId>
    <version>1.2.0</version>
</dependency>
```

For full XPath 1.0 support (includes core):

```xml
<dependency>
    <groupId>eu.maveniverse.maven.domtrip</groupId>
    <artifactId>domtrip-jaxen</artifactId>
    <version>1.2.0</version>
</dependency>
```

### Basic Usage

```java
// Load and edit XML while preserving formatting
Editor editor = new Editor(Document.of(xmlString));

// Make targeted changes
Element version = editor.root().descendant("version").orElseThrow();
editor.setTextContent(version, "2.0.0");

// Add new elements with automatic formatting
Element dependencies = editor.root().descendant("dependencies").orElseThrow();
Element newDep = editor.addElement(dependencies, "dependency");
editor.addElement(newDep, "groupId", "junit");
editor.addElement(newDep, "artifactId", "junit");

// Get result with preserved formatting
String result = editor.toXml();
```

## Modules

| Module | Artifact | Description |
|--------|----------|-------------|
| **core** | `domtrip-core` | Parser, serializer, editor, diff, visitor, XPath mini-language, SAX output |
| **maven** | `domtrip-maven` | Maven POM-specific editing utilities |
| **jaxen** | `domtrip-jaxen` | Full XPath 1.0 support via the Jaxen engine |

## Key Features

### Lossless Preservation
- **Comments**: Multi-line comments, inline comments, document comments
- **Whitespace**: Exact spacing, indentation, and line breaks
- **Entities**: Custom entity encoding (`&lt;`, `&amp;`, `&quot;`)
- **Attributes**: Quote styles (single vs double quotes), ordering, spacing
- **Declarations**: XML declarations, processing instructions, DTDs

### Modern Java API
- **Fluent Builders**: `Element.builder("dependency").withAttribute("scope", "test").build()`
- **Stream Navigation**: `root.descendants().filter(e -> "dependency".equals(e.getName()))`
- **Optional Safety**: `element.findChild("version").ifPresent(v -> ...)`
- **Type Safety**: Compile-time prevention of invalid operations
- **Configuration**: Preset configurations for different use cases

### XPath Queries

Built-in mini-XPath handles the most common query patterns without any extra dependencies:

```java
// Path navigation with predicates
root.xpath("dependencies/dependency[@scope='test']");
root.xpath("//version");
root.xpath("dependency[groupId='junit']/version");
```

For full XPath 1.0 support, add the `domtrip-jaxen` module:

```java
DomTripXPath xpath = new DomTripXPath("//dependency[starts-with(groupId, 'org.')]");
List<?> results = xpath.selectNodes(root);
```

### Visitor / Walker

Structured depth-first tree traversal with enter/exit callbacks:

```java
// Visitor interface
element.accept(new DomTripVisitor() {
    @Override
    public Action enterElement(Element e) {
        System.out.println("Entering: " + e.name());
        return Action.CONTINUE;
    }
});

// Lambda-friendly TreeWalker
element.walk()
    .onEnter(e -> {
        System.out.println(e.name());
        return DomTripVisitor.Action.CONTINUE;
    })
    .execute();
```

### XML Structural Diff

Compare documents with XML-aware diff that distinguishes semantic changes from formatting-only changes:

```java
Document before = Document.of(oldXml);
Document after = Document.of(newXml);

DiffResult diff = XmlDiff.diff(before, after);

for (XmlChange change : diff.changes()) {
    System.out.println(change);
}
// ELEMENT_ADDED: /project/dependencies/dependency[3]
// TEXT_CHANGED: /project/version: "1.0" -> "1.1"
```

### SAX Event Output

Bridge DomTrip documents into SAX-based pipelines and JAXP APIs:

```java
// Emit SAX events
SAXOutputter outputter = new SAXOutputter();
outputter.output(doc, contentHandler);

// Use as a JAXP SAXSource (e.g. with Transformer or Validator)
SAXSource source = DomTripSAXSource.of(doc);
transformer.transform(source, result);
```

### Namespace Support
- **Namespace-Aware Navigation**: Find elements by namespace URI and local name
- **Context Management**: Automatic namespace resolution and prefix handling
- **Builder Integration**: Create namespaced elements with fluent builders
- **Preservation**: Maintains namespace declarations and prefixes

## Documentation

**[Complete Documentation](https://maveniverse.github.io/domtrip/)**

- **[Getting Started](https://maveniverse.github.io/domtrip/docs/getting-started/installation)** - Installation and quick start guide
- **[Core Features](https://maveniverse.github.io/domtrip/docs/features/lossless-parsing)** - Lossless parsing, formatting preservation, namespaces
- **[API Reference](https://maveniverse.github.io/domtrip/docs/api/editor)** - Complete API documentation
- **[Examples](https://maveniverse.github.io/domtrip/docs/examples/basic-editing)** - Real-world usage examples
- **[Library Comparison](https://maveniverse.github.io/domtrip/docs/comparison)** - How DomTrip compares to other XML libraries

## Architecture

DomTrip uses a clean, type-safe architecture that enforces XML structure rules:

### Node Hierarchy
```
Node (abstract base)
├── ContainerNode (abstract)
│   ├── Document (root container)
│   └── Element (XML elements)
└── Leaf Nodes
    ├── Text (text content, CDATA)
    ├── Comment (XML comments)
    └── ProcessingInstruction (PIs)
```

### Core Components
- **`Editor`** - High-level editing interface with formatting preservation
- **`Parser`** - XML parsing engine that captures all formatting metadata
- **`Serializer`** - Minimal-change XML output generation
- **`DomTripConfig`** - Configuration management with presets
- **`XPathExpression`** - Mini-XPath query engine for element lookup
- **`XmlDiff`** - XML-aware structural diff engine
- **`DomTripVisitor` / `TreeWalker`** - Depth-first tree traversal
- **`SAXOutputter`** - SAX event emission for JAXP interop

### Design Principles
1. **Preservation First** - Original formatting preserved unless explicitly modified
2. **Metadata Tracking** - Each node tracks modification state and formatting
3. **Minimal Changes** - Only modified sections are reformatted
4. **Type Safety** - Compile-time prevention of invalid operations

## Use Cases

DomTrip excels in scenarios where preserving original formatting is crucial:

- **Configuration Files** - Update Maven POMs, Spring configs, web.xml while preserving comments and formatting
- **Document Transformation** - Transform XML documents while maintaining original structure and style
- **XML Editing Tools** - Build XML editors and IDEs that respect user formatting preferences
- **Template Processing** - Process XML templates while preserving formatting for human readability
- **Build Tools** - Modify build files programmatically without disrupting team formatting standards

## Comparison with Other Libraries

| Feature | DomTrip | DOM4J | JDOM | Jackson XML |
|---------|---------|-------|------|-------------|
| **Lossless Round-Trip** | Yes | No | No | No |
| **Comment Preservation** | Yes | Yes | Yes | No |
| **Between-Element Whitespace** | Exact | Partial | Yes* | No |
| **In-Element Whitespace** | Exact | No | Configurable | No |
| **Quote Style Preservation** | Yes | No | No | No |
| **Attribute Order Preservation** | Yes | No | No | No |
| **Entity Preservation** | Yes | No | No | No |
| **Modern Java API** | Java 8+ | Legacy | Legacy | Modern |
| **Stream Navigation** | Yes | No | No | No |
| **XPath** | Built-in + Jaxen | Jaxen | Built-in | No |
| **Structural Diff** | Built-in | No | No | No |
| **SAX Output** | Yes | Yes | Yes | No |
| **Type Safety** | Compile-time | Runtime | Runtime | Compile-time |

**\* JDOM Notes**:
- `Format.getRawFormat()` preserves original whitespace between elements
- `Format.getPrettyFormat()` reformats with consistent indentation
- Text content whitespace configurable via `TextMode.PRESERVE/TRIM/NORMALIZE`

**Choose DomTrip when**: You need perfect formatting preservation for config files, documentation, or human-edited XML

**Choose others when**: You only need data extraction, transformation, or high-throughput processing

## Development

### Running Tests
```bash
mvn test
```

### Building Documentation
```bash
cd website
npm install
npm start  # Development server
npm run build  # Production build
```

## Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

## License

DomTrip is licensed under the [Eclipse Public License 2.0](LICENSE).

## Links

- **[Documentation](https://maveniverse.github.io/domtrip/)**
- **[Maven Central](https://central.sonatype.com/artifact/eu.maveniverse.maven.domtrip/domtrip-core)**
- **[Issues](https://github.com/maveniverse/domtrip/issues)**
- **[Discussions](https://github.com/maveniverse/domtrip/discussions)**

---

**DomTrip** - *Perfect XML round-trips, every time*
