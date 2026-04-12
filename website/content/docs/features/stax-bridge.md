---
title: StAX Stream Reader
description: DomTrip can expose a Document as a StAX XMLStreamReader for integration with pull-based XML processing pipelines
layout: page
---

# StAX Stream Reader

DomTrip can expose a `Document` tree as a StAX `XMLStreamReader`, enabling direct
integration with pull-based XML processing pipelines. This avoids the overhead of
serializing to a string and re-parsing when feeding domtrip documents into tools that
consume StAX events.

**Note:** Formatting preservation is intentionally lost at the StAX boundary since StAX
events do not carry formatting metadata. The value is interoperability, not round-tripping
through StAX.

## When to Use StAX vs SAX

| | StAX (pull) | SAX (push) |
|---|---|---|
| **Control flow** | Caller drives with `next()` | Parser drives via callbacks |
| **Best for** | Selective reading, filtering | Full-document processing |
| **API style** | Cursor / iterator | Event handler |
| **DomTrip class** | `DomTripStreamReader` | `SAXOutputter` |

Both bridges are available; choose the one that matches your pipeline.

## Basic Usage

### Creating a StAX Reader

```java
Document doc = Document.of(xml);

DomTripStreamReader reader = new DomTripStreamReader(doc);
while (reader.hasNext()) {
    int event = reader.next();
    switch (event) {
        case XMLStreamConstants.START_ELEMENT:
            System.out.println("Element: " + reader.getLocalName());
            break;
        case XMLStreamConstants.CHARACTERS:
            System.out.println("Text: " + reader.getText());
            break;
        // ... handle other events
    }
}
reader.close();
```

### Using getElementText()

For elements that contain only text, use the convenience method:

```java
reader.next(); // advance to START_ELEMENT
String text = reader.getElementText(); // reads text, leaves cursor at END_ELEMENT
```

### Using nextTag()

Skip whitespace and comments to reach the next element boundary:

```java
reader.next(); // advance to START_ELEMENT of parent
int event = reader.nextTag(); // skips whitespace/comments, returns START_ELEMENT or END_ELEMENT
```

## JAXP Integration with StAXSource

The `DomTripStAXSource` class provides a `StAXSource` adapter for seamless use with
JAXP APIs like `Transformer` and `Validator`.

### XSLT Transformation

```java
Document doc = Document.of(xml);
StAXSource source = DomTripStAXSource.of(doc);

TransformerFactory tf = TransformerFactory.newInstance();
Transformer transformer = tf.newTransformer(new StreamSource(xsltFile));

StringWriter writer = new StringWriter();
transformer.transform(source, new StreamResult(writer));
String result = writer.toString();
```

### Building a DOM Tree

```java
Document doc = Document.of(xml);
StAXSource source = DomTripStAXSource.of(doc);

DOMResult result = new DOMResult();
TransformerFactory.newInstance().newTransformer().transform(source, result);

org.w3c.dom.Document domDoc = (org.w3c.dom.Document) result.getNode();
```

## StAX Events Emitted

The reader walks the domtrip tree and exposes the following StAX event types:

| domtrip Node | StAX Event Constant |
|---|---|
| `Document` start | `START_DOCUMENT` |
| `Document` end | `END_DOCUMENT` |
| `Element` open | `START_ELEMENT` |
| `Element` close | `END_ELEMENT` |
| `Text` (regular) | `CHARACTERS` |
| `Text` (CDATA) | `CDATA` |
| `Comment` | `COMMENT` |
| `ProcessingInstruction` | `PROCESSING_INSTRUCTION` |

## Namespace Handling

Namespace declarations are separated from regular attributes and exposed via the
namespace accessor methods. The element's namespace URI is resolved and available
through `getNamespaceURI()`.

```java
// Given: <root xmlns="http://example.com" xmlns:ns="http://ns.example.com">
//          <ns:child/>
//        </root>

reader.next(); // START_ELEMENT "root"
reader.getNamespaceCount();           // 2
reader.getNamespacePrefix(0);         // ""
reader.getNamespaceURI(0);            // "http://example.com"
reader.getNamespacePrefix(1);         // "ns"
reader.getNamespaceURI(1);            // "http://ns.example.com"
reader.getNamespaceURI();             // "http://example.com" (element's own URI)

reader.next(); // START_ELEMENT "ns:child"
reader.getLocalName();                // "child"
reader.getPrefix();                   // "ns"
reader.getNamespaceURI();             // "http://ns.example.com"
```

### NamespaceContext

The reader provides a full `javax.xml.namespace.NamespaceContext` at any point
during traversal:

```java
NamespaceContext ctx = reader.getNamespaceContext();
String uri = ctx.getNamespaceURI("ns");       // resolve prefix to URI
String prefix = ctx.getPrefix("http://ns.example.com"); // resolve URI to prefix
```

## Attribute Access

Attributes (excluding namespace declarations) are accessible at `START_ELEMENT` events:

```java
reader.next(); // START_ELEMENT
int count = reader.getAttributeCount();
for (int i = 0; i < count; i++) {
    String name = reader.getAttributeLocalName(i);
    String value = reader.getAttributeValue(i);
    String ns = reader.getAttributeNamespace(i);
}

// Look up by namespace URI and local name
String value = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type");
```

## Document Properties

Document-level properties are available throughout the reader's lifecycle:

```java
reader.getVersion();                    // "1.0"
reader.getEncoding();                   // "UTF-8"
reader.getCharacterEncodingScheme();    // "UTF-8"
reader.isStandalone();                  // false
reader.standaloneSet();                 // true if explicitly declared
```

## Supported Properties

| Property Name | Value | Description |
|---|---|---|
| `javax.xml.stream.isValidating` | `false` | No validation is performed |
| `javax.xml.stream.isNamespaceAware` | `true` | Namespace-aware processing |

## Classes

| Class | Purpose |
|---|---|
| `DomTripStreamReader` | `XMLStreamReader` backed by a domtrip `Document` |
| `DomTripStAXSource` | `StAXSource` adapter wrapping a domtrip `Document` |

All classes are in the `eu.maveniverse.domtrip.stax` package.

## See Also

- [SAX Event Output](../sax-output/) -- push-based SAX bridge for the same interoperability scenarios
