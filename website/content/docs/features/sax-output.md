---
title: SAX Event Output
description: DomTrip can emit SAX ContentHandler events from a Document tree for integration with SAX-based XML processing pipelines
layout: page
---

# SAX Event Output

DomTrip can emit SAX `ContentHandler` events from a `Document` or `Element` tree, enabling
direct integration with SAX-based XML processing pipelines. This avoids the overhead of
serializing to a string and re-parsing when feeding domtrip documents into tools that consume
SAX events.

**Note:** Formatting preservation is intentionally lost at the SAX boundary since SAX events
do not carry formatting metadata. The value is interoperability, not round-tripping through SAX.

## Use Cases

| Scenario | How |
|----------|-----|
| **Schema validation** | Feed a domtrip document through a `javax.xml.validation.Validator` via `SAXSource` |
| **XSLT transformation** | Use a domtrip document as input to a `javax.xml.transform.Transformer` |
| **Content pipelines** | Integrate with SAX-based filters, indexers, or content extractors |
| **Library interop** | Bridge domtrip with libraries that only accept SAX input |

## Basic Usage

### Emitting Events to a ContentHandler

```java
Document doc = Document.of(xml);

SAXOutputter outputter = new SAXOutputter();
outputter.output(doc, myContentHandler);
```

### With a LexicalHandler

Pass a `LexicalHandler` to receive events for comments and CDATA sections:

```java
SAXOutputter outputter = new SAXOutputter();
outputter.output(doc, contentHandler, lexicalHandler);
```

### Outputting a Single Element

You can also emit events for just an element subtree. This does **not** emit
`startDocument`/`endDocument` events:

```java
Element element = doc.root().childElement("child").orElseThrow();
outputter.output(element, contentHandler);
```

## JAXP Integration with SAXSource

The `DomTripSAXSource` class provides a `SAXSource` adapter for seamless use with
JAXP APIs like `Transformer` and `Validator`.

### XSLT Transformation

```java
Document doc = Document.of(xml);
SAXSource source = DomTripSAXSource.of(doc);

TransformerFactory tf = TransformerFactory.newInstance();
Transformer transformer = tf.newTransformer(new StreamSource(xsltFile));

StringWriter writer = new StringWriter();
transformer.transform(source, new StreamResult(writer));
String result = writer.toString();
```

### Schema Validation

```java
Document doc = Document.of(xml);

SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
Schema schema = sf.newSchema(schemaFile);
Validator validator = schema.newValidator();

SAXSource source = DomTripSAXSource.of(doc);
validator.validate(source); // throws SAXException if invalid
```

### Building a DOM Tree

```java
Document doc = Document.of(xml);
SAXSource source = DomTripSAXSource.of(doc);

DOMResult result = new DOMResult();
TransformerFactory.newInstance().newTransformer().transform(source, result);

org.w3c.dom.Document domDoc = (org.w3c.dom.Document) result.getNode();
```

## SAX Events Emitted

The outputter walks the domtrip tree and emits the following SAX events:

| domtrip Node | SAX Event(s) |
|---|---|
| `Document` start | `startDocument()` |
| `Document` end | `endDocument()` |
| `Element` open | `startPrefixMapping()` (for ns decls), `startElement()` |
| `Element` close | `endElement()`, `endPrefixMapping()` |
| `Text` (regular) | `characters()` |
| `Text` (CDATA) | `startCDATA()`, `characters()`, `endCDATA()` (via `LexicalHandler`) |
| `Comment` | `comment()` (via `LexicalHandler`) |
| `ProcessingInstruction` | `processingInstruction()` |

## Namespace Handling

Namespace declarations on elements are emitted as `startPrefixMapping`/`endPrefixMapping`
pairs. The element's namespace URI is resolved and passed to `startElement`/`endElement`.

```java
// Given: <root xmlns="http://example.com" xmlns:ns="http://ns.example.com">
//          <ns:child/>
//        </root>
//
// Events emitted:
//   startPrefixMapping("", "http://example.com")
//   startPrefixMapping("ns", "http://ns.example.com")
//   startElement("http://example.com", "root", "root", ...)
//     startElement("http://ns.example.com", "child", "ns:child", ...)
//     endElement("http://ns.example.com", "child", "ns:child")
//   endElement("http://example.com", "root", "root")
//   endPrefixMapping("ns")
//   endPrefixMapping("")
```

### Reporting Namespace Declarations as Attributes

By default, `xmlns` and `xmlns:prefix` attributes are **not** included in the `Attributes`
parameter of `startElement`. Enable this to match the SAX `namespace-prefixes` feature:

```java
SAXOutputter outputter = new SAXOutputter();
outputter.setReportNamespaceDeclarations(true);
outputter.output(doc, contentHandler);
```

This is also configurable via the `DomTripXMLReader` feature API:

```java
DomTripXMLReader reader = new DomTripXMLReader(doc);
reader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
```

## DomTripXMLReader

The `DomTripXMLReader` implements `org.xml.sax.XMLReader` and is used internally by
`DomTripSAXSource`. You can also use it directly when you need fine-grained control
over features and properties.

### Supported Features

| Feature URI | Default | Description |
|---|---|---|
| `http://xml.org/sax/features/namespaces` | `true` | Always enabled, cannot be disabled |
| `http://xml.org/sax/features/namespace-prefixes` | `false` | Report namespace declarations as attributes |

### Supported Properties

| Property URI | Type | Description |
|---|---|---|
| `http://xml.org/sax/properties/lexical-handler` | `LexicalHandler` | Handler for comments and CDATA events |

```java
DomTripXMLReader reader = new DomTripXMLReader(doc);
reader.setContentHandler(myHandler);
reader.setProperty("http://xml.org/sax/properties/lexical-handler", myLexicalHandler);
reader.parse(new InputSource()); // InputSource is ignored; document was provided at construction
```

## Classes

| Class | Purpose |
|---|---|
| `SAXOutputter` | Walks the tree and emits SAX events to a `ContentHandler` |
| `DomTripXMLReader` | `XMLReader` implementation for JAXP interop |
| `DomTripSAXSource` | `SAXSource` adapter wrapping a domtrip `Document` |

All classes are in the `eu.maveniverse.domtrip.sax` package.
