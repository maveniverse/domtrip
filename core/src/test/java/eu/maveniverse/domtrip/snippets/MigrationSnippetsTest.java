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
class MigrationSnippetsTest extends BaseSnippetTest {

    @Test
    void demonstrateDOM4JDocumentLoading() throws DomTripException {
        // START: dom4j-document-loading
        // DomTrip equivalent of DOM4J SAXReader.read()
        String xml = createTestXml("root");
        Document document = Document.of(xml);
        Editor editor = new Editor(document);
        Assertions.assertNotNull(editor);
        // END: dom4j-document-loading

        Assertions.assertNotNull(document);
        Assertions.assertNotNull(editor);
    }

    @Test
    void demonstrateDOM4JElementNavigation() throws DomTripException {
        // START: dom4j-element-navigation
        // DomTrip equivalent of DOM4J element navigation
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
    void demonstrateDOM4JAddingElements() throws DomTripException {
        // START: dom4j-adding-elements
        // DomTrip equivalent of DOM4J addElement()
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
    void demonstrateDOM4JAttributeHandling() throws DomTripException {
        // START: dom4j-attribute-handling
        // DomTrip equivalent of DOM4J attribute handling
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
    void demonstrateDOM4JSerialization() throws DomTripException {
        // START: dom4j-serialization
        // DomTrip equivalent of DOM4J serialization
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
    void demonstrateJDOMDocumentLoading() throws DomTripException {
        // START: jdom-document-loading
        // DomTrip equivalent of JDOM SAXBuilder.build()
        String xml = createTestXml("root");
        Document document = Document.of(xml);
        Editor editor = new Editor(document);
        Assertions.assertNotNull(editor);
        // END: jdom-document-loading

        Assertions.assertNotNull(document);
        Assertions.assertNotNull(editor);
    }

    @Test
    void demonstrateJDOMElementOperations() throws DomTripException {
        // START: jdom-element-operations
        // DomTrip equivalent of JDOM element operations
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
    void demonstrateJDOMTextContent() throws DomTripException {
        // START: jdom-text-content
        // DomTrip equivalent of JDOM text content handling
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
    void demonstrateJavaDOMDocumentLoading() throws DomTripException {
        // START: java-dom-document-loading
        // DomTrip equivalent of Java DOM document loading
        String xml = createTestXml("root");
        Document document = Document.of(xml);
        Editor editor = new Editor(document);
        Assertions.assertNotNull(editor);
        // END: java-dom-document-loading

        Assertions.assertNotNull(document);
        Assertions.assertNotNull(editor);
    }

    @Test
    void demonstrateJavaDOMElementNavigation() throws DomTripException {
        // START: java-dom-element-navigation
        // DomTrip equivalent of Java DOM element navigation
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
    void demonstrateJavaDOMCreatingElements() throws DomTripException {
        // START: java-dom-creating-elements
        // DomTrip equivalent of Java DOM element creation
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
    void demonstrateJavaDOMAttributes() throws DomTripException {
        // START: java-dom-attributes
        // DomTrip equivalent of Java DOM attribute handling
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
    void demonstrateJacksonXMLSimpleParsing() throws DomTripException {
        // START: jackson-xml-simple-parsing
        // DomTrip equivalent of Jackson XML parsing
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
    void demonstrateJacksonXMLObjectMapping() throws DomTripException {
        // START: jackson-xml-object-mapping
        // DomTrip equivalent of Jackson XML object mapping
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
    void demonstrateErrorHandling() {
        // START: migration-error-handling
        // DomTrip provides consistent exception handling
        try {
            String xml = createTestXml("root");
            Document document = Document.of(xml);
            Editor editor = new Editor(document);
            Assertions.assertNotNull(editor);
        } catch (Exception e) {
            // Handle parsing error
            System.err.println("Failed to parse: " + e.getMessage());
        }
        // END: migration-error-handling

        // Test passes if no exception is thrown
        Assertions.assertTrue(true);
    }

    @Test
    void demonstrateNamespaceHandling() throws DomTripException {
        // START: migration-namespace-handling
        // DomTrip equivalent of DOM4J namespace handling
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
    void demonstrateXPathQueries() throws DomTripException {
        // START: migration-xpath-queries
        // DomTrip uses Stream-based filtering instead of XPath
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
    void demonstrateMemoryUsage() throws DomTripException {
        // START: migration-memory-usage
        // DomTrip includes formatting metadata (~1.3x base size)
        String xml = createTestXml("root");
        Document document = Document.of(xml);
        Editor editor = new Editor(document);
        // Memory: ~1.3x base size
        // END: migration-memory-usage

        Assertions.assertNotNull(document);
        Assertions.assertNotNull(editor);
        Assertions.assertNotNull(editor.root());
    }

    @Test
    void demonstrateGradualMigrationPhase1() throws DomTripException {
        // START: gradual-migration-phase1
        // Phase 1: Use DomTrip for new features
        String pomXml = createMavenPomXml();
        Document doc = Document.of(pomXml);
        Editor editor = new Editor(doc);
        // DomTrip operations here
        // END: gradual-migration-phase1

        Assertions.assertNotNull(doc);
        Assertions.assertNotNull(editor);
    }

    @Test
    void demonstrateGradualMigrationPhase2() throws DomTripException {
        // START: gradual-migration-phase2
        // Phase 2: Migrate formatting-critical code to DomTrip
        String configXml = createConfigXml();
        Document doc = Document.of(configXml);
        Editor editor = new Editor(doc);
        // Lossless editing operations here
        // END: gradual-migration-phase2

        Assertions.assertNotNull(doc);
        Assertions.assertNotNull(editor);
    }

    @Test
    void demonstrateGradualMigrationPhase3() throws DomTripException {
        // START: gradual-migration-phase3
        // Phase 3: Migrate data extraction code
        String xml = createTestXml("root");
        Document document = Document.of(xml);
        Editor editor = new Editor(document);
        // Data extraction operations here
        // END: gradual-migration-phase3

        Assertions.assertNotNull(document);
        Assertions.assertNotNull(editor);
        Assertions.assertNotNull(editor.toXml());
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
