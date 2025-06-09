---
sidebar_position: 11
---

# Migration Guide

This guide helps you migrate from other XML libraries to DomTrip. We cover the most common migration scenarios and provide side-by-side examples.

## From DOM4J

DOM4J is one of the most popular XML libraries for Java. Here's how to migrate common patterns:

### Document Loading

```java
// DOM4J
SAXReader reader = new SAXReader();
Document document = reader.read(new StringReader(xml));

// DomTrip
Editor editor = new Editor(xml);
Document document = editor.getDocument();
```

### Element Navigation

```java
// DOM4J
Element root = document.getDocumentElement();
Element child = root.element("child");
List<Element> children = root.elements("item");

// DomTrip
Element root = editor.getDocumentElement();
Optional<Element> child = root.findChild("child");
Stream<Element> children = root.findChildren("item");
```

### Adding Elements

```java
// DOM4J
Element parent = root.element("dependencies");
Element dependency = parent.addElement("dependency");
dependency.addElement("groupId").setText("junit");
dependency.addElement("artifactId").setText("junit");

// DomTrip
Element parent = editor.findElement("dependencies");
Element dependency = editor.addElement(parent, "dependency");
editor.addElement(dependency, "groupId", "junit");
editor.addElement(dependency, "artifactId", "junit");
```

### Attribute Handling

```java
// DOM4J
element.addAttribute("scope", "test");
String scope = element.attributeValue("scope");

// DomTrip
editor.setAttribute(element, "scope", "test");
String scope = element.getAttribute("scope");
```

### Serialization

```java
// DOM4J
OutputFormat format = OutputFormat.createPrettyPrint();
XMLWriter writer = new XMLWriter(outputStream, format);
writer.write(document);

// DomTrip
String xml = editor.toXml(); // Preserves original formatting
String prettyXml = editor.toXml(DomTripConfig.prettyPrint());
```

## From JDOM

JDOM has a simpler API than DOM4J but similar concepts:

### Document Loading

```java
// JDOM
SAXBuilder builder = new SAXBuilder();
Document document = builder.build(new StringReader(xml));

// DomTrip
Editor editor = new Editor(xml);
```

### Element Operations

```java
// JDOM
Element root = document.getDocumentElement();
Element child = root.getChild("child");
List<Element> children = root.getChildren("item");

// Add new element
Element newElement = new Element("newChild");
newElement.setText("content");
root.addContent(newElement);

// DomTrip
Element root = editor.getDocumentElement();
Optional<Element> child = root.findChild("child");
Stream<Element> children = root.findChildren("item");

// Add new element
Element newElement = editor.addElement(root, "newChild", "content");
```

### Text Content

```java
// JDOM
element.setText("new content");
String content = element.getText();

// DomTrip
editor.setTextContent(element, "new content");
String content = element.getTextContent();
```

## From Java DOM

The built-in Java DOM API is verbose but powerful:

### Document Loading

```java
// Java DOM
DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
DocumentBuilder builder = factory.newDocumentBuilder();
Document document = builder.parse(new InputSource(new StringReader(xml)));

// DomTrip
Editor editor = new Editor(xml);
```

### Element Navigation

```java
// Java DOM
Element root = document.getDocumentElement();
NodeList children = root.getElementsByTagName("child");
Element child = (Element) children.item(0);

// DomTrip
Element root = editor.getDocumentElement();
Optional<Element> child = root.findChild("child");
```

### Creating Elements

```java
// Java DOM
Element newElement = document.createElement("newChild");
newElement.setTextContent("content");
parent.appendChild(newElement);

// DomTrip
Element newElement = editor.addElement(parent, "newChild", "content");
```

### Attributes

```java
// Java DOM
element.setAttribute("scope", "test");
String scope = element.getAttribute("scope");

// DomTrip
editor.setAttribute(element, "scope", "test");
String scope = element.getAttribute("scope");
```

## From Jackson XML

Jackson XML is primarily for object mapping, but here are equivalent operations:

### Simple Parsing

```java
// Jackson XML
XmlMapper mapper = new XmlMapper();
JsonNode root = mapper.readTree(xml);
JsonNode child = root.get("child");

// DomTrip
Editor editor = new Editor(xml);
Element root = editor.getDocumentElement();
Optional<Element> child = root.findChild("child");
```

### Object Mapping vs Manual Construction

```java
// Jackson XML (object mapping)
@JacksonXmlRootElement(localName = "dependency")
public class Dependency {
    public String groupId;
    public String artifactId;
    public String version;
}

Dependency dep = new Dependency();
dep.groupId = "junit";
dep.artifactId = "junit";
dep.version = "4.13.2";

String xml = mapper.writeValueAsString(dep);

// DomTrip (manual construction)
Element dependency = Element.builder("dependency")
    .withChild(Element.textElement("groupId", "junit"))
    .withChild(Element.textElement("artifactId", "junit"))
    .withChild(Element.textElement("version", "4.13.2"))
    .build();

String xml = dependency.toXml();
```

## Common Migration Patterns

### 1. Error Handling

```java
// Old libraries (various exceptions)
try {
    // DOM4J
    Document doc = reader.read(xml);
} catch (DocumentException e) {
    // Handle parsing error
}

try {
    // JDOM
    Document doc = builder.build(xml);
} catch (JDOMException | IOException e) {
    // Handle parsing error
}

// DomTrip (consistent exceptions)
try {
    Editor editor = new Editor(xml);
} catch (ParseException e) {
    // Handle parsing error
} catch (InvalidXmlException e) {
    // Handle invalid operations
}
```

### 2. Namespace Handling

```java
// DOM4J
Namespace ns = Namespace.get("soap", "http://schemas.xmlsoap.org/soap/envelope/");
Element envelope = root.element(QName.get("Envelope", ns));

// DomTrip
Optional<Element> envelope = root.findChildByNamespace(
    "http://schemas.xmlsoap.org/soap/envelope/", "Envelope");
```

### 3. XPath Queries

```java
// DOM4J (XPath support)
List<Element> nodes = document.selectNodes("//dependency[scope='test']");

// DomTrip (Stream-based filtering)
Stream<Element> nodes = root.descendants()
    .filter(el -> "dependency".equals(el.getName()))
    .filter(el -> "test".equals(el.getAttribute("scope")));
```

## Migration Checklist

### Before Migration

- [ ] Identify all XML processing code in your application
- [ ] Document current XML formatting requirements
- [ ] Create test cases for existing functionality
- [ ] Note any XPath usage (DomTrip doesn't support XPath)

### During Migration

- [ ] Replace library imports
- [ ] Update document loading code
- [ ] Convert element navigation to DomTrip patterns
- [ ] Update attribute handling
- [ ] Replace serialization code
- [ ] Handle namespace operations
- [ ] Update exception handling

### After Migration

- [ ] Run all existing tests
- [ ] Verify XML output formatting
- [ ] Check performance impact
- [ ] Update documentation
- [ ] Train team on new API patterns

## Performance Considerations

### Memory Usage

```java
// Old approach (minimal memory)
Document doc = parser.parse(xml);
// Memory: ~1x base size

// DomTrip (includes formatting metadata)
Editor editor = new Editor(xml);
// Memory: ~1.3x base size
```

### Processing Speed

- **Parsing**: DomTrip is ~15% slower due to metadata collection
- **Navigation**: Similar performance to other DOM libraries
- **Serialization**: Faster for unmodified content, slower for heavily modified content

## Gradual Migration Strategy

### Phase 1: New Code

Start using DomTrip for all new XML processing code:

```java
// New features use DomTrip
public void addDependency(String pomPath, Dependency dep) {
    String xml = Files.readString(Paths.get(pomPath));
    Editor editor = new Editor(xml);
    // ... DomTrip operations
}
```

### Phase 2: Critical Paths

Migrate code that requires formatting preservation:

```java
// Configuration file editing (formatting critical)
public void updateConfig(String configPath, Map<String, String> updates) {
    // Migrate to DomTrip for lossless editing
    Editor editor = new Editor(Files.readString(Paths.get(configPath)));
    // ...
}
```

### Phase 3: Complete Migration

Replace remaining XML processing code:

```java
// Data extraction (formatting less critical)
public List<String> extractValues(String xml) {
    // Can migrate to DomTrip or keep existing approach
    // based on requirements
}
```

## Getting Help

If you encounter issues during migration:

- 🐛 [Report Issues](https://github.com/maveniverse/domtrip/issues)
- 💬 [Ask Questions](https://github.com/maveniverse/domtrip/discussions)
- 📚 [Check Documentation](../intro)
- 📧 [Contact Support](mailto:support@maveniverse.eu)
