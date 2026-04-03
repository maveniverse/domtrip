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
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ElementQueryTest {

    private Editor editor;
    private Element root;

    @BeforeEach
    void setUp() {
        String xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n"
                + "  <dependencies>\n"
                + "    <dependency>\n"
                + "      <groupId>org.junit</groupId>\n"
                + "      <artifactId>junit</artifactId>\n"
                + "      <version>5.9.2</version>\n"
                + "      <scope>test</scope>\n"
                + "    </dependency>\n"
                + "    <dependency>\n"
                + "      <groupId>org.example</groupId>\n"
                + "      <artifactId>example-lib</artifactId>\n"
                + "      <version>1.0.0</version>\n"
                + "    </dependency>\n"
                + "    <dependency>\n"
                + "      <groupId>org.junit</groupId>\n"
                + "      <artifactId>junit-api</artifactId>\n"
                + "      <version>5.9.2</version>\n"
                + "      <scope>test</scope>\n"
                + "    </dependency>\n"
                + "  </dependencies>\n"
                + "  <build>\n"
                + "    <plugins>\n"
                + "      <plugin>\n"
                + "        <artifactId>maven-compiler-plugin</artifactId>\n"
                + "      </plugin>\n"
                + "    </plugins>\n"
                + "  </build>\n"
                + "</project>";
        editor = new Editor(Document.of(xml));
        root = editor.root();
    }

    @Test
    void testWithName() {
        List<Element> deps = root.query().withName("dependency").toList();
        assertEquals(3, deps.size());
    }

    @Test
    void testWithNameNull() {
        // Passing null name should return all descendants (no filtering)
        long allCount = root.query().withName(null).count();
        long totalCount = root.query().count();
        assertEquals(totalCount, allCount);
    }

    @Test
    void testWithNameNoMatch() {
        List<Element> results = root.query().withName("nonexistent").toList();
        assertTrue(results.isEmpty());
    }

    @Test
    void testWithQName() {
        QName depQName = QName.of("http://maven.apache.org/POM/4.0.0", "dependency");
        List<Element> deps = root.query().withQName(depQName).toList();
        assertEquals(3, deps.size());
    }

    @Test
    void testWithQNameNull() {
        long allCount = root.query().withQName(null).count();
        long totalCount = root.query().count();
        assertEquals(totalCount, allCount);
    }

    @Test
    void testWithQNameNoMatch() {
        QName qname = QName.of("http://nonexistent.namespace", "dependency");
        List<Element> results = root.query().withQName(qname).toList();
        assertTrue(results.isEmpty());
    }

    @Test
    void testWithNamespace() {
        List<Element> mavenElements =
                root.query().withNamespace("http://maven.apache.org/POM/4.0.0").toList();
        // All elements should be in the Maven namespace
        assertFalse(mavenElements.isEmpty());
    }

    @Test
    void testWithNamespaceNull() {
        long allCount = root.query().withNamespace(null).count();
        long totalCount = root.query().count();
        assertEquals(totalCount, allCount);
    }

    @Test
    void testWithNamespaceNoMatch() {
        List<Element> results = root.query().withNamespace("http://nonexistent").toList();
        assertTrue(results.isEmpty());
    }

    @Test
    void testWithAttributePresence() {
        // Build XML with attributes
        String xml = "<root><item type=\"a\"/><item/><item type=\"b\"/></root>";
        Editor ed = new Editor(Document.of(xml));
        List<Element> withType = ed.root().query().withAttribute("type").toList();
        assertEquals(2, withType.size());
    }

    @Test
    void testWithAttributePresenceNull() {
        long allCount = root.query().withAttribute((String) null).count();
        long totalCount = root.query().count();
        assertEquals(totalCount, allCount);
    }

    @Test
    void testWithAttributeNameAndValue() {
        List<Element> testScoped = root.query().withAttribute("scope", "test").toList();
        // Should match elements with scope="test" attribute
        // Note: in this XML, scope is an element, not an attribute
        assertTrue(testScoped.isEmpty());
    }

    @Test
    void testWithAttributeNameAndNullValue() {
        // When value is null, should behave like attribute presence check
        String xml = "<root><item type=\"a\"/><item/><item type=\"b\"/></root>";
        Editor ed = new Editor(Document.of(xml));
        List<Element> results = ed.root().query().withAttribute("type", null).toList();
        assertEquals(2, results.size());
    }

    @Test
    void testWithAttributeQName() {
        String xml = "<root xmlns:x=\"http://example.com\"><item x:id=\"1\"/><item/></root>";
        Editor ed = new Editor(Document.of(xml));
        QName attrQName = QName.of("http://example.com", "id", "x");
        List<Element> results = ed.root().query().withAttribute(attrQName).toList();
        assertEquals(1, results.size());
    }

    @Test
    void testWithAttributeQNameNull() {
        long allCount = root.query().withAttribute((QName) null).count();
        long totalCount = root.query().count();
        assertEquals(totalCount, allCount);
    }

    @Test
    void testWithAttributeQNameAndValue() {
        String xml = "<root xmlns:x=\"http://example.com\"><item x:id=\"1\"/><item x:id=\"2\"/></root>";
        Editor ed = new Editor(Document.of(xml));
        QName attrQName = QName.of("http://example.com", "id", "x");
        List<Element> results = ed.root().query().withAttribute(attrQName, "1").toList();
        assertEquals(1, results.size());
    }

    @Test
    void testWithAttributeQNameAndNullValue() {
        String xml = "<root xmlns:x=\"http://example.com\"><item x:id=\"1\"/><item/></root>";
        Editor ed = new Editor(Document.of(xml));
        QName attrQName = QName.of("http://example.com", "id", "x");
        List<Element> results = ed.root().query().withAttribute(attrQName, null).toList();
        assertEquals(1, results.size());
    }

    @Test
    void testWithAttributeQNameNull2() {
        long allCount = root.query().withAttribute((QName) null, "value").count();
        long totalCount = root.query().count();
        assertEquals(totalCount, allCount);
    }

    @Test
    void testWithTextContent() {
        List<Element> results = root.query().withTextContent("org.junit").toList();
        assertEquals(2, results.size());
    }

    @Test
    void testWithTextContentNull() {
        long allCount = root.query().withTextContent(null).count();
        long totalCount = root.query().count();
        assertEquals(totalCount, allCount);
    }

    @Test
    void testWithTextContentNoMatch() {
        List<Element> results = root.query().withTextContent("nonexistent-text").toList();
        assertTrue(results.isEmpty());
    }

    @Test
    void testContainingText() {
        List<Element> results = root.query().containingText("junit").toList();
        // Should match groupId with "org.junit" and artifactId with "junit" and "junit-api"
        assertTrue(results.size() >= 2);
    }

    @Test
    void testContainingTextNull() {
        long allCount = root.query().containingText(null).count();
        long totalCount = root.query().count();
        assertEquals(totalCount, allCount);
    }

    @Test
    void testContainingTextNoMatch() {
        List<Element> results = root.query().containingText("zzzzz").toList();
        assertTrue(results.isEmpty());
    }

    @Test
    void testAtDepth() {
        // Depth 0 = direct children of root (dependencies, build)
        List<Element> depth0 = root.query().atDepth(0).toList();
        assertEquals(2, depth0.size());

        // Depth 1 = children of direct children (dependency, plugins)
        List<Element> depth1 = root.query().atDepth(1).toList();
        assertFalse(depth1.isEmpty());
    }

    @Test
    void testAtDepthNegative() {
        // Negative depth should return all (no filtering)
        long allCount = root.query().atDepth(-1).count();
        long totalCount = root.query().count();
        assertEquals(totalCount, allCount);
    }

    @Test
    void testWithChildren() {
        List<Element> withKids = root.query().withChildren().toList();
        // Elements like dependencies, dependency, build, plugins should have children
        assertFalse(withKids.isEmpty());
        for (Element el : withKids) {
            assertTrue(el.hasChildElements());
        }
    }

    @Test
    void testWithoutChildren() {
        List<Element> leaves = root.query().withoutChildren().toList();
        // Leaf elements like groupId, artifactId, version, scope, artifactId (plugin)
        assertFalse(leaves.isEmpty());
        for (Element el : leaves) {
            assertFalse(el.hasChildElements());
        }
    }

    @Test
    void testWhere() {
        List<Element> results = root.query()
                .where(el -> el.name().endsWith("Id") || el.localName().endsWith("Id"))
                .toList();
        assertFalse(results.isEmpty());
    }

    @Test
    void testWhereNull() {
        long allCount = root.query().where(null).count();
        long totalCount = root.query().count();
        assertEquals(totalCount, allCount);
    }

    @Test
    void testFirst() {
        Optional<Element> first = root.query().withName("dependency").first();
        assertTrue(first.isPresent());
        assertEquals("dependency", first.get().localName());
    }

    @Test
    void testFirstNoMatch() {
        Optional<Element> first = root.query().withName("nonexistent").first();
        assertFalse(first.isPresent());
    }

    @Test
    void testAll() {
        List<Element> all = root.query().withName("dependency").all().collect(Collectors.toList());
        assertEquals(3, all.size());
    }

    @Test
    void testToList() {
        List<Element> list = root.query().withName("groupId").toList();
        assertEquals(3, list.size());
    }

    @Test
    void testCount() {
        long count = root.query().withName("dependency").count();
        assertEquals(3, count);
    }

    @Test
    void testExists() {
        assertTrue(root.query().withName("dependency").exists());
        assertFalse(root.query().withName("nonexistent").exists());
    }

    @Test
    void testChainingMultipleFilters() {
        List<Element> results =
                root.query().withName("groupId").withTextContent("org.junit").toList();
        assertEquals(2, results.size());
    }

    @Test
    void testChainingWithNameAndWithChildren() {
        List<Element> results =
                root.query().withName("dependency").withChildren().toList();
        assertEquals(3, results.size());
    }

    @Test
    void testChainingWithNameAndWithoutChildren() {
        List<Element> results =
                root.query().withName("dependency").withoutChildren().toList();
        assertTrue(results.isEmpty()); // all dependencies have children
    }

    @Test
    void testChainingDepthAndName() {
        // depth 2 = grandchildren of direct children
        List<Element> results = root.query().atDepth(2).withName("groupId").toList();
        assertFalse(results.isEmpty());
    }
}
