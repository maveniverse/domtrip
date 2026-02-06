package eu.maveniverse.domtrip.snippets;

import eu.maveniverse.domtrip.Comment;
import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Snippet tests for commenting features documentation.
 */
public class CommentingSnippets extends BaseSnippetTest {

    @Test
    public void demonstrateCommentOutSingleElement() throws DomTripException {
        // START: comment-out-single-element
        Document doc = Document.of("""
            <project>
                <dependency>
                    <groupId>junit</groupId>
                    <artifactId>junit</artifactId>
                    <version>4.13.2</version>
                </dependency>
                <other>content</other>
            </project>
            """);

        Editor editor = new Editor(doc);
        Element dependency = doc.root().child("dependency").orElseThrow();

        // Comment out the dependency
        Comment comment = editor.commentOutElement(dependency);

        String result = editor.toXml();
        // END: comment-out-single-element

        Assertions.assertTrue(result.contains("<!-- <dependency>"));
        Assertions.assertTrue(result.contains("<other>content</other>"));
        Assertions.assertNotNull(comment);
    }

    @Test
    public void demonstrateCommentOutMultipleElements() throws DomTripException {
        // START: comment-out-multiple-elements
        Document doc = Document.of("""
            <project>
                <dependency>
                    <groupId>junit</groupId>
                    <artifactId>junit</artifactId>
                </dependency>
                <dependency>
                    <groupId>mockito</groupId>
                    <artifactId>mockito-core</artifactId>
                </dependency>
                <other>keep this</other>
            </project>
            """);

        Editor editor = new Editor(doc);
        Element junit = doc.root().children("dependency").findFirst().orElseThrow();
        Element mockito = doc.root().children("dependency").skip(1).findFirst().orElseThrow();

        // Comment out both dependencies as a block
        Comment comment = editor.commentOutElements(junit, mockito);

        String result = editor.toXml();
        // END: comment-out-multiple-elements

        Assertions.assertTrue(result.contains("<!-- <dependency>"));
        Assertions.assertTrue(result.contains("<other>keep this</other>"));
        Assertions.assertNotNull(comment);
    }

    @Test
    public void demonstrateUncommentElement() throws DomTripException {
        // START: uncomment-element
        Document doc = Document.of("""
            <project>
                <!-- <dependency><groupId>junit</groupId><artifactId>junit</artifactId></dependency> -->
                <other>content</other>
            </project>
            """);

        Editor editor = new Editor(doc);

        // Find the comment containing the dependency
        Comment comment = doc.root()
                .nodes()
                .filter(node -> node instanceof Comment)
                .map(node -> (Comment) node)
                .findFirst()
                .orElseThrow();

        // Restore the commented element
        Element restored = editor.uncommentElement(comment);

        String result = editor.toXml();
        // END: uncomment-element

        Assertions.assertTrue(result.contains("<dependency>"));
        Assertions.assertTrue(result.contains("<groupId>junit</groupId>"));
        Assertions.assertNotNull(restored);
    }

    @Test
    public void demonstrateWhitespacePreservation() throws DomTripException {
        Document doc = Document.of("""
            <project>
                <dependency>
                    <groupId>junit</groupId>
                </dependency>
            </project>
            """);

        Editor editor = new Editor(doc);
        Element dependency = doc.root().child("dependency").orElseThrow();

        // START: whitespace-preservation
        // Original element with specific indentation
        Element element = dependency; // Has specific preceding whitespace
        String originalWhitespace = element.precedingWhitespace();

        // Comment out - preserves the element's whitespace
        Comment comment = editor.commentOutElement(element);

        // The comment will have the same indentation as the original element
        Assertions.assertEquals(originalWhitespace, comment.precedingWhitespace());
        // END: whitespace-preservation
    }

    @Test
    public void demonstrateRoundTripOperations() throws DomTripException {
        Document doc = Document.of("""
            <project>
                <dependency>
                    <groupId>junit</groupId>
                    <artifactId>junit</artifactId>
                </dependency>
            </project>
            """);

        Editor editor = new Editor(doc);

        // START: round-trip-operations
        Element original = doc.root().child("dependency").orElseThrow();
        String originalGroupId = original.child("groupId").orElseThrow().textContent();

        // Comment out
        Comment comment = editor.commentOutElement(original);

        // Uncomment
        Element restored = editor.uncommentElement(comment);

        // Verify restoration
        Assertions.assertEquals(
                originalGroupId, restored.child("groupId").orElseThrow().textContent());
        // END: round-trip-operations
    }

    @Test
    public void demonstrateErrorHandling() throws DomTripException {
        Document doc = Document.of("<project><child1/></project>");
        Editor editor = new Editor(doc);

        // START: commenting-error-handling
        // Cannot comment out null element
        Assertions.assertThrows(DomTripException.class, () -> {
            editor.commentOutElement(null);
        });

        // Cannot comment out root element
        Assertions.assertThrows(DomTripException.class, () -> {
            editor.commentOutElement(doc.root());
        });

        // Comment must contain valid XML for uncommenting
        Comment invalidComment = new Comment("not valid xml");
        Assertions.assertThrows(DomTripException.class, () -> {
            editor.uncommentElement(invalidComment);
        });
        // END: commenting-error-handling
    }

    @Test
    public void demonstrateIntegrationWithOtherFeatures() throws DomTripException {
        Document doc = Document.of("""
            <project>
                <dependency>
                    <groupId>junit</groupId>
                </dependency>
            </project>
            """);

        Editor editor = new Editor(doc);
        Element dependency = doc.root().child("dependency").orElseThrow();
        Element root = doc.root();

        // START: commenting-integration
        // Comment out, modify other parts, then uncomment
        Comment comment = editor.commentOutElement(dependency);
        editor.addElement(root, "newElement", "content");
        Element restored = editor.uncommentElement(comment);

        // Use with positioning features
        Element newDep = editor.insertElementAfter(restored, "dependency");
        editor.addElement(newDep, "groupId", "new-group");
        // END: commenting-integration

        String result = editor.toXml();
        Assertions.assertTrue(result.contains("<newElement>content</newElement>"));
        Assertions.assertTrue(result.contains("new-group"));
    }
}
