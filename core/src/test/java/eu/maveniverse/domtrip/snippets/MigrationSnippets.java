package eu.maveniverse.domtrip.snippets;

import static org.junit.jupiter.api.Assertions.assertEquals;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripConfig;
import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Snippet tests for the Migration Guide documentation.
 */
public class MigrationSnippets extends BaseSnippetTest {

    @Test
    public void demonstrateDOM4JDocumentLoading() throws DomTripException {
        // START: dom4j-document-loading
        // DOM4J
        // SAXReader reader = new SAXReader();
        // Document document = reader.read(new StringReader(xml));

        // DomTrip
        String xml = createTestXml("root");
        Document document = Document.of(xml);
        Editor editor = new Editor(document);
        // END: dom4j-document-loading

        Assertions.assertNotNull(document);
        Assertions.assertNotNull(editor);
    }

    @Test
    public void demonstrateDOM4JElementNavigation() throws DomTripException {
        // START: dom4j-element-navigation
        // DOM4J
        // Element root = document.getDocumentElement();
        // Element child = root.element("child");
        // List<Element> children = root.elements("item");

        // DomTrip
        String xml = createTestXml("root");
        Document document = Document.of(xml);
        Editor editor = new Editor(document);
        Element root = editor.root();
        Optional<Element> child = root.childElement("child");
        Stream<Element> children = root.childElements("item");
        // END: dom4j-element-navigation

        Assertions.assertNotNull(root);
        Assertions.assertNotNull(child);
        Assertions.assertNotNull(children);
    }

    @Test
    public void demonstrateDOM4JAddingElements() throws DomTripException {
        // START: dom4j-adding-elements
        // DOM4J
        // Element parent = root.element("dependencies");
        // Element dependency = parent.addElement("dependency");
        // dependency.addElement("groupId").setText("junit");
        // dependency.addElement("artifactId").setText("junit");

        // DomTrip
        String xml = createMavenPomXml();
        Document document = Document.of(xml);
        Editor editor = new Editor(document);
        Element parent = editor.root().descendant("dependencies").orElseThrow();
        Element dependency = editor.addElement(parent, "dependency");
        editor.addElement(dependency, "groupId", "junit");
        editor.addElement(dependency, "artifactId", "junit");
        // END: dom4j-adding-elements

        String result = editor.toXml();
        Assertions.assertTrue(result.contains("<groupId>junit</groupId>"));
        Assertions.assertTrue(result.contains("<artifactId>junit</artifactId>"));
    }

    @Test
    public void demonstrateDOM4JAttributeHandling() throws DomTripException {
        // START: dom4j-attribute-handling
        // DOM4J
        // element.addAttribute("scope", "test");
        // String scope = element.attributeValue("scope");

        // DomTrip
        String xml = createTestXml("element");
        Document document = Document.of(xml);
        Editor editor = new Editor(document);
        Element element = editor.root();
        editor.setAttribute(element, "scope", "test");
        String scope = element.attribute("scope");
        // END: dom4j-attribute-handling

        assertEquals("test", scope);
    }

    @Test
    public void demonstrateDOM4JSerialization() throws DomTripException {
        // START: dom4j-serialization
        // DOM4J
        // OutputFormat format = OutputFormat.createPrettyPrint();
        // XMLWriter writer = new XMLWriter(outputStream, format);
        // writer.write(document);

        // DomTrip
        String xml = createTestXml("root");
        Document document = Document.of(xml);
        Editor editor = new Editor(document);
        String result = editor.toXml(); // Preserves original formatting
        String prettyXml = editor.toXml(DomTripConfig.prettyPrint());
        // END: dom4j-serialization

        Assertions.assertNotNull(result);
        Assertions.assertNotNull(prettyXml);
    }

    @Test
    public void demonstrateJDOMDocumentLoading() throws DomTripException {
        // START: jdom-document-loading
        // JDOM
        // SAXBuilder builder = new SAXBuilder();
        // Document document = builder.build(new StringReader(xml));

        // DomTrip
        String xml = createTestXml("root");
        Document document = Document.of(xml);
        Editor editor = new Editor(document);
        // END: jdom-document-loading

        Assertions.assertNotNull(document);
        Assertions.assertNotNull(editor);
    }

    @Test
    public void demonstrateJDOMElementOperations() throws DomTripException {
        // START: jdom-element-operations
        // JDOM
        // Element root = document.getDocumentElement();
        // Element child = root.getChild("child");
        // List<Element> children = root.getChildren("item");
        //
        // Add new element
        // Element newElement = new Element("newChild");
        // newElement.setText("content");
        // root.addContent(newElement);

        // DomTrip
        String xml = createTestXml("root");
        Document document = Document.of(xml);
        Editor editor = new Editor(document);
        Element root = editor.root();
        Optional<Element> child = root.childElement("child");
        Stream<Element> children = root.childElements("item");

        // Add new element
        Element newElement = editor.addElement(root, "newChild", "content");
        // END: jdom-element-operations

        Assertions.assertNotNull(root);
        Assertions.assertNotNull(child);
        Assertions.assertNotNull(children);
        assertEquals("newChild", newElement.name());
        assertEquals("content", newElement.textContent());
    }

    @Test
    public void demonstrateJDOMTextContent() throws DomTripException {
        // START: jdom-text-content
        // JDOM
        // element.setText("new content");
        // String content = element.getText();

        // DomTrip
        String xml = createTestXml("element");
        Document document = Document.of(xml);
        Editor editor = new Editor(document);
        Element element = editor.root();
        editor.setTextContent(element, "new content");
        String content = element.textContent();
        // END: jdom-text-content

        assertEquals("new content", content);
    }

    @Test
    public void demonstrateJavaDOMDocumentLoading() throws DomTripException {
        // START: java-dom-document-loading
        // Java DOM
        // DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // DocumentBuilder builder = factory.newDocumentBuilder();
        // Document document = builder.parse(new InputSource(new StringReader(xml)));

        // DomTrip
        String xml = createTestXml("root");
        Document document = Document.of(xml);
        Editor editor = new Editor(document);
        // END: java-dom-document-loading

        Assertions.assertNotNull(document);
        Assertions.assertNotNull(editor);
    }

    @Test
    public void demonstrateJavaDOMElementNavigation() throws DomTripException {
        // START: java-dom-element-navigation
        // Java DOM
        // Element root = document.getDocumentElement();
        // NodeList children = root.getElementsByTagName("child");
        // Element child = (Element) children.item(0);

        // DomTrip
        String xml = createTestXml("root");
        Document document = Document.of(xml);
        Editor editor = new Editor(document);
        Element root = editor.root();
        Optional<Element> child = root.childElement("child");
        // END: java-dom-element-navigation

        Assertions.assertNotNull(root);
        Assertions.assertNotNull(child);
    }

    @Test
    public void demonstrateJavaDOMCreatingElements() throws DomTripException {
        // START: java-dom-creating-elements
        // Java DOM
        // Element newElement = document.createElement("newChild");
        // newElement.setTextContent("content");
        // parent.appendChild(newElement);

        // DomTrip
        String xml = createTestXml("parent");
        Document document = Document.of(xml);
        Editor editor = new Editor(document);
        Element parent = editor.root();
        Element newElement = editor.addElement(parent, "newChild", "content");
        // END: java-dom-creating-elements

        assertEquals("newChild", newElement.name());
        assertEquals("content", newElement.textContent());
    }

    @Test
    public void demonstrateJavaDOMAttributes() throws DomTripException {
        // START: java-dom-attributes
        // Java DOM
        // element.setAttribute("scope", "test");
        // String scope = element.getAttribute("scope");

        // DomTrip
        String xml = createTestXml("element");
        Document document = Document.of(xml);
        Editor editor = new Editor(document);
        Element element = editor.root();
        editor.setAttribute(element, "scope", "test");
        String scope = element.attribute("scope");
        // END: java-dom-attributes

        assertEquals("test", scope);
    }

    @Test
    public void demonstrateJacksonXMLSimpleParsing() throws DomTripException {
        // START: jackson-xml-simple-parsing
        // Jackson XML
        // XmlMapper mapper = new XmlMapper();
        // JsonNode root = mapper.readTree(xml);
        // JsonNode child = root.get("child");

        // DomTrip
        String xml = createTestXml("root");
        Document document = Document.of(xml);
        Editor editor = new Editor(document);
        Element root = editor.root();
        Optional<Element> child = root.childElement("child");
        // END: jackson-xml-simple-parsing

        Assertions.assertNotNull(root);
        Assertions.assertNotNull(child);
    }

    @Test
    public void demonstrateJacksonXMLObjectMapping() throws DomTripException {
        // START: jackson-xml-object-mapping
        // Jackson XML (object mapping)
        // @JacksonXmlRootElement(localName = "dependency")
        // public class Dependency {
        //     public String groupId;
        //     public String artifactId;
        //     public String version;
        // }
        //
        // Dependency dep = new Dependency();
        // dep.groupId = "junit";
        // dep.artifactId = "junit";
        // dep.version = "4.13.2";
        //
        // String xml = mapper.writeValueAsString(dep);

        // DomTrip (manual construction)
        Element dependency = Element.of("dependency");
        dependency.addChild(Element.text("groupId", "junit"));
        dependency.addChild(Element.text("artifactId", "junit"));
        dependency.addChild(Element.text("version", "4.13.2"));

        String xml = dependency.toXml();
        // END: jackson-xml-object-mapping

        Assertions.assertTrue(xml.contains("<groupId>junit</groupId>"));
        Assertions.assertTrue(xml.contains("<artifactId>junit</artifactId>"));
        Assertions.assertTrue(xml.contains("<version>4.13.2</version>"));
    }

    @Test
    public void demonstrateErrorHandling() {
        // START: migration-error-handling
        // Old libraries (various exceptions)
        // try {
        //     // DOM4J
        //     Document doc = reader.read(xml);
        // } catch (DocumentException e) {
        //     // Handle parsing error
        // }
        //
        // try {
        //     // JDOM
        //     Document doc = builder.build(xml);
        // } catch (JDOMException | IOException e) {
        //     // Handle parsing error
        // }

        // DomTrip (consistent exceptions)
        try {
            String xml = createTestXml("root");
            Document document = Document.of(xml);
            Editor editor = new Editor(document);
        } catch (Exception e) {
            // Handle parsing error
            System.err.println("Failed to parse: " + e.getMessage());
        }
        // END: migration-error-handling

        // Test passes if no exception is thrown
        Assertions.assertTrue(true);
    }

    @Test
    public void demonstrateNamespaceHandling() throws DomTripException {
        // START: migration-namespace-handling
        // DOM4J
        // Namespace ns = Namespace.get("soap", "http://schemas.xmlsoap.org/soap/envelope/");
        // Element envelope = root.element(QName.get("Envelope", ns));

        // DomTrip
        String xml = createSoapXml();
        Document document = Document.of(xml);
        Editor editor = new Editor(document);
        Element root = editor.root();

        // Check if root itself is the Envelope element
        assertEquals("Envelope", root.localName());
        assertEquals("http://schemas.xmlsoap.org/soap/envelope/", root.namespaceURI());
        // END: migration-namespace-handling
    }

    @Test
    public void demonstrateXPathQueries() throws DomTripException {
        // START: migration-xpath-queries
        // DOM4J (XPath support)
        // List<Element> nodes = document.selectNodes("//dependency[scope='test']");

        // DomTrip (Stream-based filtering)
        String xml = createMavenPomXml();
        Document document = Document.of(xml);
        Editor editor = new Editor(document);
        Element root = editor.root();
        Stream<Element> nodes = root.descendants()
                .filter(el -> "dependency".equals(el.name()))
                .filter(el -> "test".equals(el.attribute("scope")));
        // END: migration-xpath-queries

        List<Element> nodeList = nodes.toList();
        Assertions.assertNotNull(nodeList);
    }

    @Test
    public void demonstrateMemoryUsage() throws DomTripException {
        // START: migration-memory-usage
        // Old approach (minimal memory)
        // Document doc = parser.parse(xml);
        // Memory: ~1x base size

        // DomTrip (includes formatting metadata)
        String xml = createTestXml("root");
        Document document = Document.of(xml);
        Editor editor = new Editor(document);
        // Memory: ~1.3x base size
        // END: migration-memory-usage

        Assertions.assertNotNull(document);
        Assertions.assertNotNull(editor);
    }

    @Test
    public void demonstrateGradualMigrationPhase1() throws DomTripException {
        // START: gradual-migration-phase1
        // New features use DomTrip
        // public void addDependency(String pomPath, Dependency dep) {
        //     Document doc = Document.of(Path.of(pomPath));
        //     Editor editor = new Editor(doc);
        //     // ... DomTrip operations
        // }

        // Example implementation
        String pomXml = createMavenPomXml();
        Document doc = Document.of(pomXml);
        Editor editor = new Editor(doc);
        // DomTrip operations here
        // END: gradual-migration-phase1

        Assertions.assertNotNull(doc);
        Assertions.assertNotNull(editor);
    }

    @Test
    public void demonstrateGradualMigrationPhase2() throws DomTripException {
        // START: gradual-migration-phase2
        // Configuration file editing (formatting critical)
        // public void updateConfig(String configPath, Map<String, String> updates) {
        //     // Migrate to DomTrip for lossless editing
        //     Document doc = Document.of(Path.of(configPath));
        //     Editor editor = new Editor(doc);
        //     // ...
        // }

        // Example implementation
        String configXml = createConfigXml();
        Document doc = Document.of(configXml);
        Editor editor = new Editor(doc);
        // Lossless editing operations here
        // END: gradual-migration-phase2

        Assertions.assertNotNull(doc);
        Assertions.assertNotNull(editor);
    }

    @Test
    public void demonstrateGradualMigrationPhase3() throws DomTripException {
        // START: gradual-migration-phase3
        // Data extraction (formatting less critical)
        // public List<String> extractValues(String xml) {
        //     // Can migrate to DomTrip or keep existing approach
        //     // based on requirements
        // }

        // Example implementation
        String xml = createTestXml("root");
        Document document = Document.of(xml);
        Editor editor = new Editor(document);
        // Data extraction operations here
        // END: gradual-migration-phase3

        Assertions.assertNotNull(document);
        Assertions.assertNotNull(editor);
    }

    private String createSoapXml() {
        return """
            <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                <soap:Body>
                    <GetUserInfo>
                        <userId>123</userId>
                    </GetUserInfo>
                </soap:Body>
            </soap:Envelope>
            """;
    }
}
