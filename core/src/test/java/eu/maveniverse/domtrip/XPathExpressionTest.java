/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class XPathExpressionTest {

    private Editor editor;
    private Element root;

    @BeforeEach
    void setUp() {
        String xml = """
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>org.example</groupId>
                  <artifactId>my-project</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>org.junit</groupId>
                      <artifactId>junit</artifactId>
                      <version>5.9.2</version>
                      <scope>test</scope>
                    </dependency>
                    <dependency>
                      <groupId>org.example</groupId>
                      <artifactId>example-lib</artifactId>
                      <version>1.0.0</version>
                    </dependency>
                    <dependency>
                      <groupId>org.junit</groupId>
                      <artifactId>junit-api</artifactId>
                      <version>5.9.2</version>
                      <scope>test</scope>
                    </dependency>
                  </dependencies>
                  <build>
                    <plugins>
                      <plugin>
                        <artifactId>maven-compiler-plugin</artifactId>
                        <version>3.11.0</version>
                      </plugin>
                    </plugins>
                  </build>
                </project>""";
        editor = new Editor(Document.of(xml));
        root = editor.root();
    }

    // ========== CHILD PATH NAVIGATION ==========

    @Nested
    class ChildPathTests {

        @Test
        void simpleChildName() {
            List<Element> results = root.select("dependencies");
            assertEquals(1, results.size());
            assertEquals("dependencies", results.get(0).localName());
        }

        @Test
        void multiLevelPath() {
            List<Element> results = root.select("dependencies/dependency");
            assertEquals(3, results.size());
        }

        @Test
        void deepPath() {
            List<Element> results = root.select("dependencies/dependency/groupId");
            assertEquals(3, results.size());
            assertEquals("org.junit", results.get(0).textContentTrimmed());
        }

        @Test
        void noMatch() {
            List<Element> results = root.select("nonexistent");
            assertTrue(results.isEmpty());
        }

        @Test
        void partialPathNoMatch() {
            List<Element> results = root.select("dependencies/nonexistent");
            assertTrue(results.isEmpty());
        }
    }

    // ========== DESCENDANT SEARCH ==========

    @Nested
    class DescendantTests {

        @Test
        void descendantSearch() {
            List<Element> results = root.select("//dependency");
            assertEquals(3, results.size());
        }

        @Test
        void descendantSearchDeep() {
            List<Element> results = root.select("//groupId");
            // 1 at project level + 3 in dependencies = 4
            assertEquals(4, results.size());
        }

        @Test
        void descendantAfterChild() {
            List<Element> results = root.select("dependencies//groupId");
            assertEquals(3, results.size());
        }

        @Test
        void descendantNoMatch() {
            List<Element> results = root.select("//nonexistent");
            assertTrue(results.isEmpty());
        }

        @Test
        void descendantArtifactId() {
            List<Element> results = root.select("//artifactId");
            // 1 at project level + 3 in dependencies + 1 in plugin = 5
            assertEquals(5, results.size());
        }
    }

    // ========== ATTRIBUTE PREDICATES ==========

    @Nested
    @SuppressWarnings("java:S5976")
    class AttributePredicateTests {

        @Test
        void attributePresence() {
            String xml = """
                    <root><item type="a"/><item/><item type="b"/></root>""";
            Editor ed = new Editor(Document.of(xml));
            List<Element> results = ed.root().select("item[@type]");
            assertEquals(2, results.size());
        }

        @Test
        void attributeValue() {
            String xml = """
                    <root><item type="a"/><item type="b"/><item type="a"/></root>""";
            Editor ed = new Editor(Document.of(xml));
            List<Element> results = ed.root().select("item[@type='a']");
            assertEquals(2, results.size());
        }

        @Test
        void attributeValueDoubleQuotes() {
            String xml = """
                    <root><item type="a"/><item type="b"/></root>""";
            Editor ed = new Editor(Document.of(xml));
            List<Element> results = ed.root().select("item[@type=\"a\"]");
            assertEquals(1, results.size());
        }

        @Test
        void attributeValueNoMatch() {
            String xml = """
                    <root><item type="a"/><item type="b"/></root>""";
            Editor ed = new Editor(Document.of(xml));
            List<Element> results = ed.root().select("item[@type='c']");
            assertTrue(results.isEmpty());
        }

        @Test
        void descendantWithAttributePredicate() {
            String xml = """
                    <root><a><item scope="test"/></a><item scope="compile"/></root>""";
            Editor ed = new Editor(Document.of(xml));
            List<Element> results = ed.root().select("//item[@scope='test']");
            assertEquals(1, results.size());
        }
    }

    // ========== CHILD TEXT PREDICATES ==========

    @Nested
    class ChildTextPredicateTests {

        @Test
        void childTextMatch() {
            List<Element> results = root.select("//dependency[groupId='org.junit']");
            assertEquals(2, results.size());
        }

        @Test
        void childTextExactMatch() {
            List<Element> results = root.select("//dependency[artifactId='junit']");
            assertEquals(1, results.size());
        }

        @Test
        void childTextNoMatch() {
            List<Element> results = root.select("//dependency[groupId='nonexistent']");
            assertTrue(results.isEmpty());
        }

        @Test
        void childTextWithPath() {
            List<Element> results = root.select("dependencies/dependency[scope='test']");
            assertEquals(2, results.size());
        }
    }

    // ========== POSITIONAL PREDICATES ==========

    @Nested
    class PositionalPredicateTests {

        @Test
        void firstElement() {
            List<Element> results = root.select("dependencies/dependency[1]");
            assertEquals(1, results.size());
            assertEquals(
                    "org.junit", results.get(0).selectFirst("groupId").get().textContentTrimmed());
        }

        @Test
        void secondElement() {
            List<Element> results = root.select("dependencies/dependency[2]");
            assertEquals(1, results.size());
            assertEquals(
                    "org.example", results.get(0).selectFirst("groupId").get().textContentTrimmed());
        }

        @Test
        void lastElement() {
            List<Element> results = root.select("dependencies/dependency[last()]");
            assertEquals(1, results.size());
            assertEquals(
                    "junit-api", results.get(0).selectFirst("artifactId").get().textContentTrimmed());
        }

        @Test
        void outOfBoundsPosition() {
            List<Element> results = root.select("dependencies/dependency[99]");
            assertTrue(results.isEmpty());
        }
    }

    // ========== WILDCARD ==========

    @Nested
    class WildcardTests {

        @Test
        void wildcardChildren() {
            String xml = """
                    <root><a/><b/><c/></root>""";
            Editor ed = new Editor(Document.of(xml));
            List<Element> results = ed.root().select("*");
            assertEquals(3, results.size());
        }

        @Test
        void wildcardInPath() {
            List<Element> results = root.select("dependencies/*/groupId");
            assertEquals(3, results.size());
        }

        @Test
        void wildcardDescendant() {
            String xml = """
                    <root><a><x/></a><b><x/></b></root>""";
            Editor ed = new Editor(Document.of(xml));
            List<Element> results = ed.root().select("*/x");
            assertEquals(2, results.size());
        }
    }

    // ========== SELF AND PARENT ==========

    @Nested
    class SelfAndParentTests {

        @Test
        void selfNavigation() {
            List<Element> results = root.select(".");
            assertEquals(1, results.size());
            assertSame(root, results.get(0));
        }

        @Test
        void selfInPath() {
            List<Element> results = root.select("./dependencies");
            assertEquals(1, results.size());
        }

        @Test
        void parentNavigation() {
            Element deps = root.select("dependencies").get(0);
            List<Element> results = deps.select("dependency[1]/..");
            assertEquals(1, results.size());
            assertEquals("dependencies", results.get(0).localName());
        }
    }

    // ========== MIXED / COMBINED EXPRESSIONS ==========

    @Nested
    class CombinedTests {

        @Test
        void multiplePredicates() {
            List<Element> results = root.select("//dependency[scope='test'][artifactId='junit-api']");
            assertEquals(1, results.size());
            assertEquals(
                    "org.junit", results.get(0).selectFirst("groupId").get().textContentTrimmed());
        }

        @Test
        void descendantWithChildText() {
            List<Element> results = root.select("//dependency[groupId='org.example']");
            assertEquals(1, results.size());
            assertEquals(
                    "example-lib",
                    results.get(0).selectFirst("artifactId").get().textContentTrimmed());
        }

        @Test
        void pathWithPositional() {
            List<Element> results = root.select("dependencies/dependency[1]/groupId");
            assertEquals(1, results.size());
            assertEquals("org.junit", results.get(0).textContentTrimmed());
        }
    }

    // ========== NAMESPACE SUPPORT ==========

    @Nested
    class NamespaceTests {

        @Test
        void prefixedElementName() {
            String xml = """
                    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                      <soap:Body>
                        <soap:Fault/>
                      </soap:Body>
                    </soap:Envelope>""";
            Editor ed = new Editor(Document.of(xml));
            Element envelope = ed.root();

            List<Element> results = envelope.select("soap:Body");
            assertEquals(1, results.size());
        }

        @Test
        void prefixedDescendant() {
            String xml = """
                    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                      <soap:Body>
                        <soap:Fault/>
                      </soap:Body>
                    </soap:Envelope>""";
            Editor ed = new Editor(Document.of(xml));
            Element envelope = ed.root();

            List<Element> results = envelope.select("//soap:Fault");
            assertEquals(1, results.size());
        }

        @Test
        void localNameMatchesNamespacedElement() {
            String xml = """
                    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                      <soap:Body/>
                    </soap:Envelope>""";
            Editor ed = new Editor(Document.of(xml));

            // Matching by local name should also work
            List<Element> results = ed.root().select("Body");
            assertEquals(1, results.size());
        }

        @Test
        void prefixedPath() {
            String xml = """
                    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                      <soap:Body>
                        <soap:Fault/>
                      </soap:Body>
                    </soap:Envelope>""";
            Editor ed = new Editor(Document.of(xml));

            List<Element> results = ed.root().select("soap:Body/soap:Fault");
            assertEquals(1, results.size());
        }
    }

    // ========== EDITOR CONVENIENCE METHODS ==========

    @Nested
    class EditorTests {

        @Test
        void editorSelect() {
            List<Element> results = editor.select("//dependency");
            assertEquals(3, results.size());
        }

        @Test
        void editorSelectFirst() {
            Optional<Element> result = editor.selectFirst("//dependency[groupId='org.junit']");
            assertTrue(result.isPresent());
            assertEquals("junit", result.get().selectFirst("artifactId").get().textContentTrimmed());
        }

        @Test
        void editorSelectNoMatch() {
            List<Element> results = editor.select("//nonexistent");
            assertTrue(results.isEmpty());
        }

        @Test
        void editorSelectFirstNoMatch() {
            Optional<Element> result = editor.selectFirst("//nonexistent");
            assertFalse(result.isPresent());
        }
    }

    // ========== COMPILED EXPRESSION REUSE ==========

    @Nested
    class CompiledExpressionTests {

        @Test
        void compileAndReuse() {
            XPathExpression expr = XPathExpression.compile("//dependency");
            List<Element> results1 = expr.select(root);
            List<Element> results2 = expr.select(root);
            assertEquals(3, results1.size());
            assertEquals(3, results2.size());
        }

        @Test
        void expressionToString() {
            XPathExpression expr = XPathExpression.compile("//dependency[@scope='test']");
            assertTrue(expr.toString().contains("//dependency[@scope='test']"));
        }

        @Test
        void expressionAccessor() {
            XPathExpression expr = XPathExpression.compile("//dependency");
            assertEquals("//dependency", expr.expression());
        }
    }

    // ========== ERROR HANDLING ==========

    @Nested
    class ErrorHandlingTests {

        @Test
        void nullExpression() {
            assertThrows(DomTripException.class, () -> XPathExpression.compile(null));
        }

        @Test
        void emptyExpression() {
            assertThrows(DomTripException.class, () -> XPathExpression.compile(""));
        }

        @Test
        void blankExpression() {
            assertThrows(DomTripException.class, () -> XPathExpression.compile("   "));
        }

        @Test
        void invalidPredicate() {
            assertThrows(DomTripException.class, () -> XPathExpression.compile("foo[!!!]"));
        }

        @Test
        void emptyPredicate() {
            assertThrows(DomTripException.class, () -> XPathExpression.compile("foo[]"));
        }

        @Test
        void negativePosition() {
            assertThrows(DomTripException.class, () -> XPathExpression.compile("foo[-1]"));
        }

        @Test
        void zeroPosition() {
            assertThrows(DomTripException.class, () -> XPathExpression.compile("foo[0]"));
        }

        @Test
        void selectWithNullContext() {
            XPathExpression expr = XPathExpression.compile("//foo");
            List<Element> results = expr.select(null);
            assertTrue(results.isEmpty());
        }

        @Test
        void selectFirstWithNullContext() {
            XPathExpression expr = XPathExpression.compile("//foo");
            Optional<Element> result = expr.selectFirst(null);
            assertFalse(result.isPresent());
        }
    }

    // ========== EDGE CASES ==========

    @Nested
    @SuppressWarnings("java:S5976")
    class EdgeCaseTests {

        @Test
        void singleElementDocument() {
            Editor ed = new Editor(Document.of("<root/>"));
            List<Element> results = ed.root().select(".");
            assertEquals(1, results.size());
        }

        @Test
        void deepNesting() {
            String xml = """
                    <a><b><c><d><e>deep</e></d></c></b></a>""";
            Editor ed = new Editor(Document.of(xml));
            List<Element> results = ed.root().select("b/c/d/e");
            assertEquals(1, results.size());
            assertEquals("deep", results.get(0).textContentTrimmed());
        }

        @Test
        void multipleMatchesAtSameLevel() {
            String xml = """
                    <root><item>a</item><item>b</item><item>c</item></root>""";
            Editor ed = new Editor(Document.of(xml));
            List<Element> results = ed.root().select("item");
            assertEquals(3, results.size());
        }

        @Test
        void attributeValueWithSlash() {
            String xml = """
                    <root><item path="a/b/c"/><item path="x/y"/></root>""";
            Editor ed = new Editor(Document.of(xml));
            List<Element> results = ed.root().select("item[@path='a/b/c']");
            assertEquals(1, results.size());
        }

        @Test
        void emptyResults() {
            List<Element> results = root.select("a/b/c/d/e/f");
            assertTrue(results.isEmpty());
        }

        @Test
        void rootElementMatch() {
            List<Element> results = root.select(".");
            assertEquals(1, results.size());
            assertEquals("project", results.get(0).localName());
        }

        @Test
        void leadingSingleSlash() {
            // Leading / is treated as relative to context (same as no leading /)
            List<Element> results = root.select("/dependencies");
            assertEquals(1, results.size());
        }

        @Test
        void descendantAllWithWildcard() {
            String xml = """
                    <root><a/><b><c/></b></root>""";
            Editor ed = new Editor(Document.of(xml));
            List<Element> results = ed.root().select("//*");
            assertEquals(3, results.size());
        }
    }
}
