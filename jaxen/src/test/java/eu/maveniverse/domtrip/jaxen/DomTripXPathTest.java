/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.jaxen;

import static org.junit.jupiter.api.Assertions.*;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Element;
import java.util.List;
import java.util.Optional;
import org.jaxen.JaxenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for the Jaxen XPath integration with DomTrip.
 */
class DomTripXPathTest {

    private static final String POM_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project>
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.example</groupId>
              <artifactId>my-app</artifactId>
              <version>1.0.0</version>
              <dependencies>
                <dependency>
                  <groupId>org.junit</groupId>
                  <artifactId>junit-api</artifactId>
                  <version>5.10.0</version>
                  <scope>test</scope>
                </dependency>
                <dependency>
                  <groupId>org.springframework</groupId>
                  <artifactId>spring-core</artifactId>
                  <version>6.0.0</version>
                </dependency>
                <dependency>
                  <groupId>com.google.guava</groupId>
                  <artifactId>guava</artifactId>
                  <version>32.0.0</version>
                  <scope>compile</scope>
                </dependency>
              </dependencies>
            </project>""";

    private static final String SIMPLE_XML = """
            <root>
              <items>
                <item id="1" type="a">First</item>
                <item id="2" type="b">Second</item>
                <item id="3" type="a">Third</item>
              </items>
              <metadata>
                <author>Alice</author>
                <tags>
                  <tag>xml</tag>
                  <tag>xpath</tag>
                </tags>
              </metadata>
            </root>""";

    private static final String NS_XML = """
            <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Header/>
              <soap:Body>
                <m:GetPrice xmlns:m="http://www.example.org/stock">
                  <m:StockName>IBM</m:StockName>
                </m:GetPrice>
              </soap:Body>
            </soap:Envelope>""";

    private Element pomRoot;
    private Element simpleRoot;
    private Element nsRoot;

    @BeforeEach
    void setUp() {
        pomRoot = Document.of(POM_XML).root();
        simpleRoot = Document.of(SIMPLE_XML).root();
        nsRoot = Document.of(NS_XML).root();
    }

    @Nested
    class BasicPathTests {

        @Test
        void directChildPath() {
            List<Element> results = XPath.select(simpleRoot, "items");
            assertEquals(1, results.size());
            assertEquals("items", results.get(0).name());
        }

        @Test
        void multiLevelPath() {
            List<Element> results = XPath.select(simpleRoot, "items/item");
            assertEquals(3, results.size());
        }

        @Test
        void descendantSearch() {
            List<Element> results = XPath.select(simpleRoot, "//item");
            assertEquals(3, results.size());
        }

        @Test
        void descendantSearchDeep() {
            List<Element> results = XPath.select(simpleRoot, "//tag");
            assertEquals(2, results.size());
        }

        @Test
        void mixedChildAndDescendant() {
            List<Element> results = XPath.select(simpleRoot, "metadata//tag");
            assertEquals(2, results.size());
        }

        @Test
        void wildcardMatch() {
            List<Element> results = XPath.select(simpleRoot, "items/*");
            assertEquals(3, results.size());
        }

        @Test
        void selfAxis() {
            List<Element> results = XPath.select(simpleRoot, ".");
            assertEquals(1, results.size());
            assertEquals("root", results.get(0).name());
        }

        @Test
        void parentAxis() {
            Element items = XPath.select(simpleRoot, "items").get(0);
            List<Element> results = XPath.select(items, "..");
            assertEquals(1, results.size());
            assertEquals("root", results.get(0).name());
        }
    }

    @Nested
    class AttributePredicateTests {

        @Test
        void attributePresence() {
            List<Element> results = XPath.select(simpleRoot, "//item[@id]");
            assertEquals(3, results.size());
        }

        @Test
        void attributeValue() {
            List<Element> results = XPath.select(simpleRoot, "//item[@type='a']");
            assertEquals(2, results.size());
        }

        @Test
        void attributeValueDoubleQuotes() {
            List<Element> results = XPath.select(simpleRoot, "//item[@type=\"b\"]");
            assertEquals(1, results.size());
            assertEquals("Second", results.get(0).textContentTrimmed());
        }

        @Test
        void attributeInequality() {
            List<Element> results = XPath.select(simpleRoot, "//item[@type!='a']");
            assertEquals(1, results.size());
            assertEquals("Second", results.get(0).textContentTrimmed());
        }
    }

    @Nested
    class ChildTextPredicateTests {

        @Test
        void childTextEquals() {
            List<Element> results = XPath.select(simpleRoot, "//tag[text()='xml']");
            assertEquals(1, results.size());
        }

        @Test
        void childTextNotEquals() {
            List<Element> results = XPath.select(simpleRoot, "//tag[text()!='xml']");
            assertEquals(1, results.size());
            assertEquals("xpath", results.get(0).textContentTrimmed());
        }
    }

    @Nested
    @SuppressWarnings("java:S5976") // Positional tests are clearer as separate methods
    class PositionalPredicateTests {

        @Test
        void firstPosition() {
            List<Element> results = XPath.select(simpleRoot, "items/item[1]");
            assertEquals(1, results.size());
            assertEquals("First", results.get(0).textContentTrimmed());
        }

        @Test
        void lastPosition() {
            List<Element> results = XPath.select(simpleRoot, "items/item[last()]");
            assertEquals(1, results.size());
            assertEquals("Third", results.get(0).textContentTrimmed());
        }

        @Test
        void specificPosition() {
            List<Element> results = XPath.select(simpleRoot, "items/item[2]");
            assertEquals(1, results.size());
            assertEquals("Second", results.get(0).textContentTrimmed());
        }
    }

    @Nested
    class BooleanOperatorTests {

        @Test
        void andOperator() {
            List<Element> results = XPath.select(simpleRoot, "//item[@id='1' and @type='a']");
            assertEquals(1, results.size());
            assertEquals("First", results.get(0).textContentTrimmed());
        }

        @Test
        void andOperatorNoMatch() {
            List<Element> results = XPath.select(simpleRoot, "//item[@id='1' and @type='b']");
            assertTrue(results.isEmpty());
        }

        @Test
        void orOperator() {
            List<Element> results = XPath.select(simpleRoot, "//item[@id='1' or @id='2']");
            assertEquals(2, results.size());
        }

        @Test
        void orOperatorPartialMatch() {
            List<Element> results = XPath.select(simpleRoot, "//item[@id='1' or @id='99']");
            assertEquals(1, results.size());
        }

        @Test
        void combinedAndOr() {
            List<Element> results = XPath.select(simpleRoot, "//item[(@type='a' and @id='1') or @id='2']");
            assertEquals(2, results.size());
        }
    }

    @Nested
    class NotFunctionTests {

        @Test
        void notAttributePresence() {
            List<Element> results = XPath.select(pomRoot, "//dependency[not(scope)]");
            assertEquals(1, results.size());
            assertEquals(
                    "org.springframework",
                    results.get(0).childElement("groupId").get().textContentTrimmed());
        }

        @Test
        void notAttributeValue() {
            List<Element> results = XPath.select(simpleRoot, "//item[not(@type='a')]");
            assertEquals(1, results.size());
            assertEquals("Second", results.get(0).textContentTrimmed());
        }
    }

    @Nested
    class StringFunctionTests {

        @Test
        void containsFunction() {
            List<Element> results = XPath.select(pomRoot, "//dependency[contains(groupId, 'junit')]");
            assertEquals(1, results.size());
        }

        @Test
        void containsFunctionNoMatch() {
            List<Element> results = XPath.select(pomRoot, "//dependency[contains(groupId, 'nonexistent')]");
            assertTrue(results.isEmpty());
        }

        @Test
        void startsWithFunction() {
            List<Element> results = XPath.select(pomRoot, "//dependency[starts-with(groupId, 'org.')]");
            assertEquals(2, results.size());
        }

        @Test
        void startsWithFunctionNoMatch() {
            List<Element> results = XPath.select(pomRoot, "//dependency[starts-with(groupId, 'net.')]");
            assertTrue(results.isEmpty());
        }

        @Test
        void stringLengthFunction() {
            List<Element> results = XPath.select(simpleRoot, "//item[string-length(@id) = 1]");
            assertEquals(3, results.size());
        }
    }

    @Nested
    class UnionOperatorTests {

        @Test
        void unionTwoNodeSets() {
            List<Element> results = XPath.select(simpleRoot, "//author | //tag");
            assertEquals(3, results.size());
        }

        @Test
        void unionSameNodeSet() {
            List<Element> results = XPath.select(simpleRoot, "//item | //item");
            // Union removes duplicates
            assertEquals(3, results.size());
        }
    }

    @Nested
    class FullAxisTests {

        @Test
        void followingSiblingAxis() {
            Element firstItem = XPath.select(simpleRoot, "items/item[1]").get(0);
            List<Element> results = XPath.select(firstItem, "following-sibling::item");
            assertEquals(2, results.size());
        }

        @Test
        void precedingSiblingAxis() {
            Element lastItem = XPath.select(simpleRoot, "items/item[last()]").get(0);
            List<Element> results = XPath.select(lastItem, "preceding-sibling::item");
            assertEquals(2, results.size());
        }

        @Test
        void ancestorAxis() {
            Element tag = XPath.select(simpleRoot, "//tag[1]").get(0);
            List<Element> results = XPath.select(tag, "ancestor::root");
            assertEquals(1, results.size());
            assertEquals("root", results.get(0).name());
        }

        @Test
        void descendantAxis() {
            List<Element> results = XPath.select(simpleRoot, "descendant::item");
            assertEquals(3, results.size());
        }

        @Test
        void descendantOrSelfAxis() {
            List<Element> results = XPath.select(simpleRoot, "descendant-or-self::root");
            assertEquals(1, results.size());
        }
    }

    @Nested
    class NamespaceTests {

        @Test
        void namespacePrefixMatch() throws JaxenException {
            DomTripXPath xpath = new DomTripXPath("//soap:Body");
            xpath.addNamespace("soap", "http://schemas.xmlsoap.org/soap/envelope/");
            List<Element> results = xpath.selectElements(nsRoot);
            assertEquals(1, results.size());
            assertEquals("soap:Body", results.get(0).name());
        }

        @Test
        void nestedNamespace() throws JaxenException {
            DomTripXPath xpath = new DomTripXPath("//m:StockName");
            xpath.addNamespace("m", "http://www.example.org/stock");
            List<Element> results = xpath.selectElements(nsRoot);
            assertEquals(1, results.size());
            assertEquals("IBM", results.get(0).textContentTrimmed());
        }
    }

    @Nested
    class ConvenienceApiTests {

        @Test
        void selectReturnsElements() {
            List<Element> results = XPath.select(simpleRoot, "//item");
            assertEquals(3, results.size());
        }

        @Test
        void selectFirstReturnsFirst() {
            Optional<Element> result = XPath.selectFirst(simpleRoot, "//item");
            assertTrue(result.isPresent());
            assertEquals("First", result.get().textContentTrimmed());
        }

        @Test
        void selectFirstReturnsEmptyWhenNoMatch() {
            Optional<Element> result = XPath.selectFirst(simpleRoot, "//nonexistent");
            assertFalse(result.isPresent());
        }

        @Test
        void compileAndReuse() throws JaxenException {
            DomTripXPath compiled = XPath.compile("//item");
            List<Element> results1 = compiled.selectElements(simpleRoot);
            assertEquals(3, results1.size());
            // Verify reuse with a second evaluation
            List<Element> results2 = compiled.selectElements(simpleRoot);
            assertEquals(3, results2.size());
        }

        @Test
        void invalidExpressionThrowsDomTripException() {
            assertThrows(DomTripException.class, () -> XPath.compile("[invalid[["));
        }
    }

    @Nested
    class DocumentContextTests {

        @Test
        void queryFromDocument() {
            Document doc = Document.of(SIMPLE_XML);
            List<Element> results = XPath.select(doc, "//item");
            assertEquals(3, results.size());
        }

        @Test
        void queryFromDocumentRoot() {
            Document doc = Document.of(SIMPLE_XML);
            List<Element> results = XPath.select(doc.root(), "//item");
            assertEquals(3, results.size());
        }
    }

    @Nested
    class PomXmlTests {

        @Test
        void findTestDependencies() {
            List<Element> results = XPath.select(pomRoot, "//dependency[scope='test']");
            assertEquals(1, results.size());
        }

        @Test
        void findDependenciesWithoutScope() {
            List<Element> results = XPath.select(pomRoot, "//dependency[not(scope)]");
            assertEquals(1, results.size());
        }

        @Test
        void findByGroupIdContains() {
            List<Element> results = XPath.select(pomRoot, "//dependency[contains(groupId, 'spring')]");
            assertEquals(1, results.size());
        }

        @Test
        void findByGroupIdStartsWith() {
            List<Element> results = XPath.select(pomRoot, "//dependency[starts-with(groupId, 'com.')]");
            assertEquals(1, results.size());
            assertEquals(
                    "com.google.guava",
                    results.get(0).childElement("groupId").get().textContentTrimmed());
        }

        @Test
        void countDependencies() {
            List<Element> results = XPath.select(pomRoot, "//dependency");
            assertEquals(3, results.size());
        }

        @Test
        void findTestOrCompileDeps() {
            List<Element> results = XPath.select(pomRoot, "//dependency[scope='test' or scope='compile']");
            assertEquals(2, results.size());
        }

        @Test
        void findNonTestDeps() {
            List<Element> results = XPath.select(pomRoot, "//dependency[not(scope='test')]");
            assertEquals(2, results.size());
        }
    }

    @Nested
    class CommentAndPiTests {

        private static final String XML_WITH_COMMENTS_AND_PI = """
                <?xml version="1.0"?>
                <?app-config debug="true"?>
                <root>
                  <!-- A comment -->
                  <child>text</child>
                  <!-- Another comment -->
                </root>""";

        @Test
        void selectComments() throws JaxenException {
            Document doc = Document.of(XML_WITH_COMMENTS_AND_PI);
            DomTripXPath xpath = new DomTripXPath("//comment()");
            List<?> results = xpath.selectNodes(doc.root());
            assertEquals(2, results.size());
        }

        @Test
        void selectProcessingInstructions() throws JaxenException {
            Document doc = Document.of(XML_WITH_COMMENTS_AND_PI);
            DomTripXPath xpath = new DomTripXPath("//processing-instruction('app-config')");
            List<?> results = xpath.selectNodes(doc);
            assertEquals(1, results.size());
        }

        @Test
        void selectTextNodes() throws JaxenException {
            Element root = Document.of("<root><a>hello</a><b>world</b></root>").root();
            DomTripXPath xpath = new DomTripXPath("//a/text()");
            List<?> results = xpath.selectNodes(root);
            assertFalse(results.isEmpty());
        }

        @Test
        void deepTextContent() {
            Element root = Document.of("<root><a>hello <b>world</b></a></root>").root();
            List<Element> results = XPath.select(root, "//a[contains(., 'hello world')]");
            assertEquals(1, results.size());
        }
    }

    @Nested
    class NavigatorCoverageTests {

        @Test
        void queryFromDocumentNode() throws JaxenException {
            Document doc = Document.of(SIMPLE_XML);
            DomTripXPath xpath = new DomTripXPath("/root");
            List<?> results = xpath.selectNodes(doc);
            assertEquals(1, results.size());
        }

        @Test
        void attributeParentNavigation() {
            List<Element> results = XPath.select(simpleRoot, "//item[@id='1']/..");
            assertEquals(1, results.size());
            assertEquals("items", results.get(0).name());
        }

        @Test
        void namespacedAttributeQuery() throws JaxenException {
            Element root = Document.of("""
                    <root xmlns:custom="http://example.com" custom:attr="value">
                      <child/>
                    </root>""").root();
            DomTripXPath xpath = new DomTripXPath("/root/@custom:attr");
            xpath.addNamespace("custom", "http://example.com");
            Object result = xpath.evaluate(root);
            assertNotNull(result);
        }

        @Test
        void selectFirstErrorHandling() {
            Optional<Element> result = XPath.selectFirst(simpleRoot, "//nonexistent");
            assertFalse(result.isPresent());
        }

        @Test
        void selectErrorHandling() {
            List<Element> results = XPath.select(simpleRoot, "//nonexistent");
            assertTrue(results.isEmpty());
        }

        @Test
        void selectInvalidExpressionThrows() {
            assertThrows(DomTripException.class, () -> XPath.select(simpleRoot, "[invalid[["));
        }

        @Test
        void selectFirstInvalidExpressionThrows() {
            assertThrows(DomTripException.class, () -> XPath.selectFirst(simpleRoot, "[invalid[["));
        }

        @Test
        void namespaceAxisIterator() throws JaxenException {
            DomTripXPath xpath = new DomTripXPath("namespace::*");
            List<?> results = xpath.selectNodes(nsRoot);
            // Should include at least the xml namespace and soap namespace
            assertTrue(results.size() >= 2);
        }

        @Test
        void elementNamespaceUri() throws JaxenException {
            DomTripXPath xpath = new DomTripXPath("//soap:Body");
            xpath.addNamespace("soap", "http://schemas.xmlsoap.org/soap/envelope/");
            List<Element> results = xpath.selectElements(nsRoot);
            assertEquals(1, results.size());
            // This exercises getElementNamespaceUri in the navigator
            String uri = results.get(0).namespaceURI();
            assertEquals("http://schemas.xmlsoap.org/soap/envelope/", uri);
        }

        @Test
        void processingInstructionData() throws JaxenException {
            Document doc = Document.of("<?xml version=\"1.0\"?><?pi-target pi-data?><root/>");
            DomTripXPath xpath = new DomTripXPath("//processing-instruction('pi-target')");
            List<?> results = xpath.selectNodes(doc);
            assertEquals(1, results.size());
        }

        @Test
        void documentNodeFromNamespaceContext() throws JaxenException {
            // Query that navigates from namespace node back to document
            DomTripXPath xpath = new DomTripXPath("/root");
            List<?> results = xpath.selectNodes(Document.of(NS_XML));
            // Envelope is the root, not "root" — just verify no error
            assertTrue(results.isEmpty());
        }

        @Test
        void precedingSiblingFromNonNodeReturnsEmpty() throws JaxenException {
            // Preceding sibling on a non-node context (attribute)
            DomTripXPath xpath = new DomTripXPath("//item[1]/@id/preceding-sibling::*");
            List<?> results = xpath.selectNodes(simpleRoot);
            assertTrue(results.isEmpty());
        }
    }

    @Nested
    class EdgeCaseTests {

        @Test
        void emptyDocument() {
            Element root = Document.of("<root/>").root();
            List<Element> results = XPath.select(root, "//child");
            assertTrue(results.isEmpty());
        }

        @Test
        void deeplyNestedElements() {
            Element root =
                    Document.of("<a><b><c><d><e>deep</e></d></c></b></a>").root();
            List<Element> results = XPath.select(root, "//e");
            assertEquals(1, results.size());
            assertEquals("deep", results.get(0).textContentTrimmed());
        }

        @Test
        void selectFromMiddleOfTree() {
            Element items = XPath.select(simpleRoot, "items").get(0);
            List<Element> results = XPath.select(items, "item[@type='a']");
            assertEquals(2, results.size());
        }

        @Test
        void multiplePredicates() {
            List<Element> results = XPath.select(simpleRoot, "//item[@id][@type='a']");
            assertEquals(2, results.size());
        }
    }
}
