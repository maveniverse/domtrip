---
title: Quick Start
description: Get up and running with DomTrip in 5 minutes! This guide covers the essential operations you'll use most often.
layout: page
---

# Quick Start

Get up and running with DomTrip in 5 minutes! This guide covers the essential operations you'll use most often.

## Your First DomTrip Program

Let's start with a simple example that demonstrates DomTrip's core strength: lossless round-trip editing.

```java
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;

public class QuickStart {
    public static void main(String[] args) throws Exception {
        // 1. Parse XML while preserving all formatting
        String originalXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!-- Configuration file -->
            <config>
                <database>
                    <host>localhost</host>
                    <port>5432</port>
                </database>
            </config>
            """;
        
        Editor editor = new Editor(Document.of(originalXml));
        
        // 2. Make some changes
        Element database = editor.findElement("database");
        editor.addElement(database, "username", "admin");
        editor.addElement(database, "password", "secret");
        
        // 3. Serialize back to XML
        String result = editor.toXml();
        System.out.println(result);
    }
}
```

**Output:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- Configuration file -->
<config>
    <database>
        <host>localhost</host>
        <port>5432</port>
        <username>admin</username>
        <password>secret</password>
    </database>
</config>
```

Notice how:
- The XML declaration and comments are preserved
- Original indentation is maintained
- New elements follow the existing formatting pattern

## Core Operations

### 1. Loading XML

```java
// From string
Editor editor = new Editor(Document.of(xmlString));

// From file (recommended - handles encoding automatically)
Document doc = Document.of(Path.of("config.xml"));
Editor editor = new Editor(doc);

// From InputStream with automatic encoding detection
try (InputStream inputStream = Files.newInputStream(Path.of("config.xml"))) {
    Document doc = Document.of(inputStream);
    Editor editor = new Editor(doc);
}

// With custom configuration
Editor editor = new Editor(Document.of(Path.of("config.xml")), DomTripConfig.prettyPrint());
```

### 2. Finding Elements

```java
// Find by name
Element root = editor.getDocumentElement();
Element database = editor.findElement("database");

// Modern navigation with Optional
Optional<Element> host = root.findChild("host");
if (host.isPresent()) {
    System.out.println("Host: " + host.get().getTextContent());
}

// Stream-based navigation
root.findChildren("item")
    .filter(item -> "active".equals(item.getAttribute("status")))
    .forEach(item -> System.out.println(item.getTextContent()));
```

### 3. Adding Elements

```java
// Simple element with text
editor.addElement(parent, "name", "value");

// Element with attributes
Element element = editor.addElement(parent, "dependency");
editor.setAttribute(element, "scope", "test");
editor.addElement(element, "groupId", "junit");

// Using fluent builders
Element dependency = Element.builder("dependency")
    .withAttribute("scope", "test")
    .withChild(Element.textElement("groupId", "junit"))
    .withChild(Element.textElement("artifactId", "junit"))
    .build();
```

### 4. Modifying Content

```java
// Change text content
Element version = editor.findElement("version");
editor.setTextContent(version, "2.0.0");

// Modify attributes
editor.setAttribute(element, "id", "new-id");
editor.removeAttribute(element, "old-attr");

// Add comments
editor.addComment(parent, " This is a comment ");
```

### 5. Removing Elements

```java
// Remove element
Element toRemove = editor.findElement("deprecated");
editor.removeElement(toRemove);

// Remove by condition
root.findChildren("item")
    .filter(item -> "inactive".equals(item.getAttribute("status")))
    .forEach(editor::removeElement);
```

## Working with Namespaces

DomTrip provides excellent namespace support:

```java
// Create elements with namespaces
Element soapEnvelope = Element.namespacedElement(
    "soap", "Envelope", "http://schemas.xmlsoap.org/soap/envelope/");

// Namespace-aware navigation
Optional<Element> body = root.findChildByNamespace(
    "http://schemas.xmlsoap.org/soap/envelope/", "Body");

// Get namespace information
String localName = element.getLocalName();
String namespaceURI = element.getNamespaceURI();
String prefix = element.getPrefix();
```

## Configuration Options

Customize DomTrip's behavior with configuration:

```java
// Pretty printing
DomTripConfig config = DomTripConfig.prettyPrint();
String prettyXml = editor.toXml(config);

// Minimal output
String minimalXml = editor.toXml(DomTripConfig.minimal());

// Custom configuration
DomTripConfig custom = DomTripConfig.defaults()
    .withIndentation("  ")  // 2 spaces
    .withPreserveWhitespace(true)
    .withPreserveComments(true);
```

## Real-World Example: Maven POM Editing

Here's a practical example of editing a Maven POM file:

```java
public class MavenPomEditor {
    public static void addDependency(String pomPath,
                                   String groupId,
                                   String artifactId,
                                   String version) throws Exception {
        // Load POM with automatic encoding detection
        Document doc = Document.of(Path.of(pomPath));
        Editor editor = new Editor(doc);

        // Find or create dependencies section
        Element project = editor.getDocumentElement();
        Element dependencies = editor.findElement("dependencies");
        if (dependencies == null) {
            dependencies = editor.addElement(project, "dependencies");
        }

        // Add new dependency
        Element dependency = editor.addElement(dependencies, "dependency");
        editor.addElement(dependency, "groupId", groupId);
        editor.addElement(dependency, "artifactId", artifactId);
        editor.addElement(dependency, "version", version);

        // Save back to file (String-based)
        Files.writeString(Path.of(pomPath), editor.toXml());

        // Or save to OutputStream with proper encoding
        try (OutputStream outputStream = Files.newOutputStream(Path.of(pomPath))) {
            editor.document().toXml(outputStream);
        }

        System.out.println("✅ Added dependency: " + groupId + ":" + artifactId);
    }
}
```

## Working with Existing Documents

If you already have a parsed Document object, you can create an Editor from it:

```java
// Parse with Parser directly
Parser parser = new Parser();
Document document = parser.parse(xmlString);

// Create Editor from existing Document
Editor editor = new Editor(document);

// Now use the convenient Editor API
Element root = editor.getDocumentElement();
editor.addElement(root, "newChild", "value");
editor.setAttribute(root, "version", "2.0");

// Serialize with preserved formatting
String result = editor.toXml();
```

You can also use custom configuration with existing documents:

```java
// Create document programmatically
Document doc = Document.builder()
    .withRootElement("project")
    .withXmlDeclaration()
    .build();

// Create Editor with custom config
DomTripConfig config = DomTripConfig.prettyPrint()
    .withIndentString("  ");
Editor editor = new Editor(doc, config);

// Build document structure
Element root = editor.getDocumentElement();
editor.addElement(root, "groupId", "com.example");
editor.addElement(root, "artifactId", "my-project");
```

## Error Handling

DomTrip provides specific exception types for better error handling:

```java
try {
    Editor editor = new Editor(Document.of(malformedXml));
} catch (ParseException e) {
    System.err.println("XML parsing failed: " + e.getMessage());
} catch (InvalidXmlException e) {
    System.err.println("Invalid XML operation: " + e.getMessage());
}
```

## Next Steps

Now that you've mastered the basics, explore more advanced features:

- 🏗️ [Builder Patterns](/docs/advanced/builder-patterns/) - Fluent APIs for complex XML
- 🌐 [Namespace Support](/docs/features/namespace-support/) - Working with XML namespaces
- ⚙️ [Configuration](/docs/api/configuration/) - Customizing DomTrip's behavior
- 📖 [Examples](/docs/examples/) - Real-world use cases

## Tips for Success

1. **Always use try-with-resources** for file operations
2. **Check for null** when finding elements that might not exist
3. **Use Optional** for safer navigation
4. **Leverage Stream API** for filtering and processing collections
5. **Configure appropriately** for your use case (preserve vs. pretty print)
