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

        Comment comment = editor.commentOutElement(dependency);

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

        Comment comment = editor.commentOutElements(first, second);

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
                .filter(node -> node instanceof Comment)
                .map(node -> (Comment) node)
                .findFirst()
                .orElseThrow();

        Element restored = editor.uncommentElement(comment);

        String result = editor.toXml();
        assertEquals(expected, result);
    }

    @Test
    void testCommentOutNullElement() {
        assertThrows(DomTripException.class, () -> {
            editor.commentOutElement(null);
        });
    }

    @Test
    void testCommentOutRootElement() throws DomTripException {
        String xml = "<root><child/></root>";
        Document doc = Document.of(xml);
        editor = new Editor(doc);

        assertThrows(DomTripException.class, () -> {
            editor.commentOutElement(doc.root());
        });
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

        assertThrows(DomTripException.class, () -> {
            editor.commentOutElements(child1, child2);
        });
    }

    @Test
    void testUncommentEmptyComment() throws DomTripException {
        String xml = "<root><!-- --><other/></root>";
        Document doc = Document.of(xml);
        editor = new Editor(doc);

        Comment comment = doc.root()
                .children()
                .filter(node -> node instanceof Comment)
                .map(node -> (Comment) node)
                .findFirst()
                .orElseThrow();

        assertThrows(DomTripException.class, () -> {
            editor.uncommentElement(comment);
        });
    }

    @Test
    void testUncommentInvalidXml() throws DomTripException {
        String xml = "<root><!-- <element attr=\"unclosed --><other/></root>";
        Document doc = Document.of(xml);
        editor = new Editor(doc);

        Comment comment = doc.root()
                .children()
                .filter(node -> node instanceof Comment)
                .map(node -> (Comment) node)
                .findFirst()
                .orElseThrow();

        assertThrows(DomTripException.class, () -> {
            editor.uncommentElement(comment);
        });
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
        Element restored = editor.uncommentElement(comment);

        // Verify the XML is identical to the original
        String result = editor.toXml();
        assertEquals(xml, result);
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
                .filter(node -> node instanceof Comment)
                .map(node -> (Comment) node)
                .findFirst()
                .orElseThrow();

        Element restored = editor.uncommentElement(comment);

        String result = editor.toXml();
        assertEquals(expected, result);
    }
}
