package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for element commenting functionality in Editor.
 */
class EditorCommentingTest {

    private Editor editor;

    @BeforeEach
    void setUp() {
        editor = new Editor();
    }

    @Test
    void testCommentOutSingleElement() throws DomTripException {
        String xml = """
            <root>
                <dependency>
                    <groupId>junit</groupId>
                    <artifactId>junit</artifactId>
                </dependency>
                <other>content</other>
            </root>""";
        String expected = """
            <root>
                <!-- <dependency>
                    <groupId>junit</groupId>
                    <artifactId>junit</artifactId>
                </dependency> -->
                <other>content</other>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element dependency = doc.root().childElement("dependency").orElseThrow();

        editor.commentOutElement(dependency);

        String result = editor.toXml();
        assertEquals(expected, result);
    }

    @Test
    void testCommentOutMultipleElements() throws DomTripException {
        String xml = """
            <root>
                <first>content1</first>
                <second>content2</second>
                <third>content3</third>
                <keep>keep this</keep>
            </root>""";
        String expected = """
            <root>
                <!-- <first>content1</first>
                <second>content2</second> -->
                <third>content3</third>
                <keep>keep this</keep>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element first = doc.root().childElement("first").orElseThrow();
        Element second = doc.root().childElement("second").orElseThrow();

        editor.commentOutElements(first, second);

        String result = editor.toXml();
        assertEquals(expected, result);
    }

    @Test
    void testUncommentElement() throws DomTripException {
        String xml = """
            <root>
                <!-- <dependency><groupId>junit</groupId><artifactId>junit</artifactId></dependency> -->
                <other>content</other>
            </root>""";
        String expected = """
            <root>
                <dependency><groupId>junit</groupId><artifactId>junit</artifactId></dependency>
                <other>content</other>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);

        // Find the comment
        Comment comment = doc.root()
                .children()
                .filter(Comment.class::isInstance)
                .map(Comment.class::cast)
                .findFirst()
                .orElseThrow();

        editor.uncommentElement(comment);

        String result = editor.toXml();
        assertEquals(expected, result);
    }

    @Test
    void testCommentOutNullElement() {
        assertThrows(DomTripException.class, () -> editor.commentOutElement(null));
    }

    @Test
    @SuppressWarnings("java:S5778")
    void testCommentOutRootElement() throws DomTripException {
        String xml = "<root><child/></root>";
        Document doc = Document.of(xml);
        editor = new Editor(doc);

        assertThrows(DomTripException.class, () -> editor.commentOutElement(doc.root()));
    }

    @Test
    void testCommentOutElementsWithDifferentParents() throws DomTripException {
        String xml = """
            <root>
                <parent1><child1/></parent1>
                <parent2><child2/></parent2>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element child1 = doc.root()
                .childElement("parent1")
                .orElseThrow()
                .childElement("child1")
                .orElseThrow();
        Element child2 = doc.root()
                .childElement("parent2")
                .orElseThrow()
                .childElement("child2")
                .orElseThrow();

        assertThrows(DomTripException.class, () -> editor.commentOutElements(child1, child2));
    }

    @Test
    void testUncommentEmptyComment() throws DomTripException {
        String xml = "<root><!-- --><other/></root>";
        Document doc = Document.of(xml);
        editor = new Editor(doc);

        Comment comment = doc.root()
                .children()
                .filter(Comment.class::isInstance)
                .map(Comment.class::cast)
                .findFirst()
                .orElseThrow();

        assertThrows(DomTripException.class, () -> editor.uncommentElement(comment));
    }

    @Test
    void testUncommentInvalidXml() throws DomTripException {
        String xml = "<root><!-- <element attr=\"unclosed --><other/></root>";
        Document doc = Document.of(xml);
        editor = new Editor(doc);

        Comment comment = doc.root()
                .children()
                .filter(Comment.class::isInstance)
                .map(Comment.class::cast)
                .findFirst()
                .orElseThrow();

        assertThrows(DomTripException.class, () -> editor.uncommentElement(comment));
    }

    @Test
    void testCommentOutPreservesWhitespace() throws DomTripException {
        String xml = """
            <root>
                <dependency>
                    <groupId>junit</groupId>
                </dependency>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element dependency = doc.root().childElement("dependency").orElseThrow();

        String originalPrecedingWhitespace = dependency.precedingWhitespace();

        Comment comment = editor.commentOutElement(dependency);

        assertEquals(originalPrecedingWhitespace, comment.precedingWhitespace());
    }

    @Test
    void testRoundTripCommentUncomment() throws DomTripException {
        String xml = """
            <root>
                <dependency scope="test">
                    <groupId>junit</groupId>
                    <artifactId>junit</artifactId>
                    <version>4.13.2</version>
                </dependency>
                <other>content</other>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element dependency = doc.root().childElement("dependency").orElseThrow();

        // Comment out and then uncomment
        Comment comment = editor.commentOutElement(dependency);
        editor.uncommentElement(comment);

        // Verify the XML is identical to the original
        String result = editor.toXml();
        assertEquals(xml, result);
    }

    // ========== insertCommentBefore / insertCommentAfter tests ==========

    @Test
    void testInsertCommentBeforeElement() throws DomTripException {
        String xml = """
            <root>
                <first>content1</first>
                <second>content2</second>
            </root>""";
        String expected = """
            <root>
                <first>content1</first>
                <!-- inserted comment -->
                <second>content2</second>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element second = doc.root().childElement("second").orElseThrow();

        Comment comment = editor.insertCommentBefore(second, " inserted comment ");

        assertNotNull(comment);
        assertEquals(" inserted comment ", comment.content());
        assertEquals(doc.root(), comment.parent());

        String result = editor.toXml();
        assertEquals(expected, result);
    }

    @Test
    void testInsertCommentAfterElement() throws DomTripException {
        String xml = """
            <root>
                <first>content1</first>
                <second>content2</second>
            </root>""";
        String expected = """
            <root>
                <first>content1</first>
                <!-- inserted comment -->
                <second>content2</second>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element first = doc.root().childElement("first").orElseThrow();

        Comment comment = editor.insertCommentAfter(first, " inserted comment ");

        assertNotNull(comment);
        assertEquals(" inserted comment ", comment.content());
        assertEquals(doc.root(), comment.parent());

        String result = editor.toXml();
        assertEquals(expected, result);
    }

    @Test
    void testInsertCommentBeforeFirstElement() throws DomTripException {
        String xml = """
            <root>
                <first>content1</first>
                <second>content2</second>
            </root>""";
        String expected = """
            <root>
                <!-- before first -->
                <first>content1</first>
                <second>content2</second>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element first = doc.root().childElement("first").orElseThrow();

        editor.insertCommentBefore(first, " before first ");

        String result = editor.toXml();
        assertEquals(expected, result);
    }

    @Test
    void testInsertCommentAfterLastElement() throws DomTripException {
        String xml = """
            <root>
                <first>content1</first>
                <second>content2</second>
            </root>""";
        String expected = """
            <root>
                <first>content1</first>
                <second>content2</second>
                <!-- after last -->
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element second = doc.root().childElement("second").orElseThrow();

        editor.insertCommentAfter(second, " after last ");

        String result = editor.toXml();
        assertEquals(expected, result);
    }

    @Test
    void testInsertCommentBeforeWithDeepNesting() throws DomTripException {
        String xml = """
            <root>
                <dependencies>
                    <dependency>
                        <groupId>junit</groupId>
                    </dependency>
                </dependencies>
            </root>""";
        String expected = """
            <root>
                <dependencies>
                    <!-- Override version inherited from parent -->
                    <dependency>
                        <groupId>junit</groupId>
                    </dependency>
                </dependencies>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element dependency = doc.root()
                .childElement("dependencies")
                .orElseThrow()
                .childElement("dependency")
                .orElseThrow();

        editor.insertCommentBefore(dependency, " Override version inherited from parent ");

        String result = editor.toXml();
        assertEquals(expected, result);
    }

    @Test
    void testInsertCommentBeforeNullElement() {
        assertThrows(DomTripException.class, () -> editor.insertCommentBefore(null, "text"));
    }

    @Test
    void testInsertCommentAfterNullElement() {
        assertThrows(DomTripException.class, () -> editor.insertCommentAfter(null, "text"));
    }

    @Test
    void testInsertCommentBeforeOrphanElement() {
        Element orphan = new Element("orphan");
        assertThrows(DomTripException.class, () -> editor.insertCommentBefore(orphan, "text"));
    }

    @Test
    void testInsertCommentAfterOrphanElement() {
        Element orphan = new Element("orphan");
        assertThrows(DomTripException.class, () -> editor.insertCommentAfter(orphan, "text"));
    }

    @Test
    void testInsertCommentWithNullText() throws DomTripException {
        String xml = """
            <root>
                <child>content</child>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element child = doc.root().childElement("child").orElseThrow();

        Comment comment = editor.insertCommentBefore(child, null);

        assertNotNull(comment);
        assertEquals("", comment.content());
    }

    @Test
    void testInsertCommentBeforeInMavenPluginScenario() throws DomTripException {
        String xml = """
            <build>
                <plugins>
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-compiler-plugin</artifactId>
                    </plugin>
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-surefire-plugin</artifactId>
                    </plugin>
                </plugins>
            </build>""";
        String expected = """
            <build>
                <plugins>
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-compiler-plugin</artifactId>
                    </plugin>
                    <!-- Override version inherited from parent -->
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-surefire-plugin</artifactId>
                    </plugin>
                </plugins>
            </build>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element plugins = doc.root().childElement("plugins").orElseThrow();
        Element secondPlugin = plugins.childElements()
                .filter(e -> "plugin".equals(e.name()))
                .skip(1)
                .findFirst()
                .orElseThrow();

        editor.insertCommentBefore(secondPlugin, " Override version inherited from parent ");

        String result = editor.toXml();
        assertEquals(expected, result);
    }

    @Test
    void testInsertCommentAfterInMavenPluginScenario() throws DomTripException {
        String xml = """
            <build>
                <plugins>
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-compiler-plugin</artifactId>
                    </plugin>
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-surefire-plugin</artifactId>
                    </plugin>
                </plugins>
            </build>""";
        String expected = """
            <build>
                <plugins>
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-compiler-plugin</artifactId>
                    </plugin>
                    <!-- Next section -->
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-surefire-plugin</artifactId>
                    </plugin>
                </plugins>
            </build>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc);
        Element plugins = doc.root().childElement("plugins").orElseThrow();
        Element firstPlugin = plugins.childElement("plugin").orElseThrow();

        editor.insertCommentAfter(firstPlugin, " Next section ");

        String result = editor.toXml();
        assertEquals(expected, result);
    }

    @Test
    void testUncommentElementWithPrettyPrint() throws DomTripException {
        String xml = """
            <root>
                <!-- <dependency><groupId>junit</groupId><artifactId>junit</artifactId></dependency> -->
                <other>content</other>
            </root>""";
        String expected = """
            <root>
                <dependency><groupId>junit</groupId><artifactId>junit</artifactId></dependency>
                <other>content</other>
            </root>""";

        Document doc = Document.of(xml);
        editor = new Editor(doc, DomTripConfig.prettyPrint());

        // Find the comment
        Comment comment = doc.root()
                .children()
                .filter(Comment.class::isInstance)
                .map(Comment.class::cast)
                .findFirst()
                .orElseThrow();

        editor.uncommentElement(comment);

        String result = editor.toXml();
        assertEquals(expected, result);
    }
}
