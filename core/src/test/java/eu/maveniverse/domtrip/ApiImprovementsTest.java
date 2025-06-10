package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test cases for the API improvements including QName support,
 * standardized return types, and fluent navigation.
 */
public class ApiImprovementsTest {

    private Editor editor;

    @BeforeEach
    void setUp() {
        editor = new Editor(Document.of());
    }

    @Test
    void testQNameCreation() {
        // Test basic QName creation
        QName simple = QName.of("version");
        assertEquals("version", simple.localName());
        assertFalse(simple.hasNamespace());
        assertFalse(simple.hasPrefix());
        assertEquals("version", simple.qualifiedName());

        // Test namespaced QName
        QName namespaced = QName.of("http://maven.apache.org/POM/4.0.0", "dependency");
        assertEquals("dependency", namespaced.localName());
        assertEquals("http://maven.apache.org/POM/4.0.0", namespaced.namespaceURI());
        assertTrue(namespaced.hasNamespace());
        assertFalse(namespaced.hasPrefix());

        // Test QName with prefix
        QName prefixed = QName.of("http://schemas.xmlsoap.org/soap/envelope/", "Envelope", "soap");
        assertEquals("Envelope", prefixed.localName());
        assertEquals("http://schemas.xmlsoap.org/soap/envelope/", prefixed.namespaceURI());
        assertEquals("soap", prefixed.prefix());
        assertTrue(prefixed.hasNamespace());
        assertTrue(prefixed.hasPrefix());
        assertEquals("soap:Envelope", prefixed.qualifiedName());
    }

    @Test
    void testQNameParsing() {
        // Test parsing simple name
        QName simple = QName.parse("version");
        assertEquals("version", simple.localName());
        assertNull(simple.prefix());

        // Test parsing prefixed name
        QName prefixed = QName.parse("soap:Envelope");
        assertEquals("Envelope", prefixed.localName());
        assertEquals("soap", prefixed.prefix());
        assertEquals("soap:Envelope", prefixed.qualifiedName());

        // Test invalid names
        assertThrows(IllegalArgumentException.class, () -> QName.parse(null));
        assertThrows(IllegalArgumentException.class, () -> QName.parse(""));
        assertThrows(IllegalArgumentException.class, () -> QName.parse(":invalid"));
        assertThrows(IllegalArgumentException.class, () -> QName.parse("invalid:"));
    }

    @Test
    void testQNameMatching() {
        QName qname1 = QName.of("http://example.com", "test");
        QName qname2 = QName.of("http://example.com", "test", "ex");
        QName qname3 = QName.of("http://other.com", "test");

        // Same namespace and local name should match regardless of prefix
        assertTrue(qname1.matches(qname2));
        assertTrue(qname2.matches(qname1));

        // Different namespace should not match
        assertFalse(qname1.matches(qname3));

        // Test string matching
        assertTrue(qname1.matches("http://example.com", "test"));
        assertFalse(qname1.matches("http://other.com", "test"));
    }

    @Test
    void testStandardizedReturnTypes() throws Exception {
        // Create a test document
        editor.createDocument("root");
        Document doc = editor.document();
        Element root = editor.root().orElseThrow();

        // Add some elements
        Element child1 = editor.addElement(root, "child1", "content1");
        Element child2 = editor.addElement(root, "child2", "content2");

        // Test Optional return types
        Optional<Element> foundChild1 = doc.root().descendant("child1");
        assertTrue(foundChild1.isPresent());
        assertEquals("content1", foundChild1.orElseThrow().textContent());

        Optional<Element> notFound = doc.root().descendant("nonexistent");
        assertFalse(notFound.isPresent());

        // Test Stream return types
        List<Element> allChildren = doc.root().descendants("child1").collect(Collectors.toList());
        assertEquals(1, allChildren.size());
        assertEquals("child1", allChildren.get(0).name());
    }

    @Test
    void testQNameElementCreation() throws Exception {
        editor.createDocument("root");
        Document doc = editor.document();
        Element root = editor.root().orElseThrow();

        // Create element with QName
        QName dependencyQName = QName.of("http://maven.apache.org/POM/4.0.0", "dependency");
        Element dependency = editor.addElement(root, dependencyQName);

        assertEquals("dependency", dependency.localName());
        assertEquals("http://maven.apache.org/POM/4.0.0", dependency.namespaceURI());

        // Find element by QName
        Optional<Element> found = doc.root().descendant(dependencyQName);
        assertTrue(found.isPresent());
        assertEquals(dependency, found.orElseThrow());
    }

    @Test
    void testElementQueryAPI() throws Exception {
        // Create a test document with multiple elements
        editor.createDocument("project");
        Element root = editor.root().orElseThrow();

        // Add dependencies
        Element dependencies = editor.addElement(root, "dependencies");
        Element dep1 = editor.addElement(dependencies, "dependency");
        dep1.attribute("scope", "test");
        editor.addElement(dep1, "groupId", "junit");
        editor.addElement(dep1, "artifactId", "junit");

        Element dep2 = editor.addElement(dependencies, "dependency");
        dep2.attribute("scope", "compile");
        editor.addElement(dep2, "groupId", "commons-lang");
        editor.addElement(dep2, "artifactId", "commons-lang");

        // Test fluent query API
        List<Element> testDeps = root.query()
                .withName("dependency")
                .withAttribute("scope", "test")
                .toList();
        assertEquals(1, testDeps.size());
        assertEquals("test", testDeps.get(0).attribute("scope"));

        // Test query with text content
        Optional<Element> junitGroupId =
                root.query().withName("groupId").withTextContent("junit").first();
        assertTrue(junitGroupId.isPresent());
        assertEquals("junit", junitGroupId.orElseThrow().textContent());

        // Test query count
        long depCount = root.query().withName("dependency").count();
        assertEquals(2, depCount);
    }

    @Test
    void testPathBasedNavigation() throws Exception {
        // Create a nested document structure
        editor.createDocument("project");
        Element root = editor.root().orElseThrow();

        Element dependencies = editor.addElement(root, "dependencies");
        Element dependency = editor.addElement(dependencies, "dependency");
        Element groupId = editor.addElement(dependency, "groupId", "junit");

        // Test path-based navigation
        Optional<Element> foundGroupId = root.path("dependencies", "dependency", "groupId");
        assertTrue(foundGroupId.isPresent());
        assertEquals("junit", foundGroupId.orElseThrow().textContent());

        // Test path that doesn't exist
        Optional<Element> notFound = root.path("dependencies", "nonexistent");
        assertFalse(notFound.isPresent());

        // Test partial path
        Optional<Element> foundDependencies = root.path("dependencies");
        assertTrue(foundDependencies.isPresent());
        assertEquals("dependencies", foundDependencies.orElseThrow().name());
    }

    @Test
    void testEnhancedElementNavigation() throws Exception {
        editor.createDocument("root");
        Element root = editor.root().orElseThrow();

        // Add child elements
        Element child1 = editor.addElement(root, "child", "content1");
        Element child2 = editor.addElement(root, "child", "content2");
        Element other = editor.addElement(root, "other", "content3");

        // Test enhanced navigation methods
        Optional<Element> firstChild = root.child("child");
        assertTrue(firstChild.isPresent());
        assertEquals("content1", firstChild.orElseThrow().textContent());

        List<Element> allChildren = root.children("child").collect(Collectors.toList());
        assertEquals(2, allChildren.size());

        List<Element> allChildElements = root.children().collect(Collectors.toList());
        assertEquals(3, allChildElements.size());

        // Test descendant navigation
        Element nested = editor.addElement(child1, "nested", "nested-content");
        Optional<Element> foundNested = root.descendant("nested");
        assertTrue(foundNested.isPresent());
        assertEquals("nested-content", foundNested.orElseThrow().textContent());
    }

    @Test
    void testQNameElementBuilder() {
        // Test QName-based element creation
        QName soapEnvelope = QName.of("http://schemas.xmlsoap.org/soap/envelope/", "Envelope", "soap");
        Element envelope = Element.of(soapEnvelope);
        // Add namespace declaration for the test to work
        envelope.attribute("xmlns:soap", "http://schemas.xmlsoap.org/soap/envelope/");

        assertEquals("soap:Envelope", envelope.name());
        assertEquals("Envelope", envelope.localName());
        assertEquals("soap", envelope.prefix());
        assertEquals("http://schemas.xmlsoap.org/soap/envelope/", envelope.namespaceURI());

        // Test QName element creation
        Element body = Element.of(QName.of("http://schemas.xmlsoap.org/soap/envelope/", "Body", "soap"));
        body.addNode(new Text("body content"));

        assertEquals("soap:Body", body.name());
        assertEquals("body content", body.textContent());
    }

    /*
    @Test
    void testImprovedConvenienceMethods() throws Exception {
        editor.createDocument("root");
        Document doc = editor.document();
        Element root = editor.root().orElseThrow();

        // Test findOrCreateElement
        Element version = // FIXME: editor.findOrCreateElement("version");
                assertNotNull(version);
        assertEquals("version", version.name());

        // Should find existing element on second call
        Element sameVersion = // FIXME: editor.findOrCreateElement("version");
                assertEquals(version, sameVersion);

        // Test setElementText with return value
        boolean updated = // FIXME: editor.setElementText("version", "1.0.0");
                assertTrue(updated);
        assertEquals("1.0.0", version.textContent());

        boolean notUpdated = // FIXME: editor.setElementText("nonexistent", "value");
                assertFalse(notUpdated);

        // Test setElementAttribute with return value
        boolean attrSet = // FIXME: editor.setElementAttribute("version", "type", "release");
                assertTrue(attrSet);
        assertEquals("release", version.attribute("type"));

        boolean attrNotSet = // FIXME: editor.setElementAttribute("nonexistent", "attr", "value");
                assertFalse(attrNotSet);
    }
     */
}
