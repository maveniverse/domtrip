package eu.maveniverse.domtrip.website;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.qute.RawString;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

/**
 * Test for SnippetTemplateExtension to verify that code snippets are returned
 * as RawString to prevent HTML escaping of characters like ", <, and >.
 */
@QuarkusTest
public class SnippetTemplateExtensionTest {

    @Inject
    SnippetTemplateExtension snippetExtension;

    @Test
    public void testSnippetReturnsRawString() {
        // Test that the snippet method returns a RawString
        Object result = snippetExtension.snippet("non-existent-snippet");

        // Verify that the result is a RawString instance
        assertInstanceOf(RawString.class, result, "snippet() method should return RawString to prevent HTML escaping");

        RawString rawString = (RawString) result;
        String content = rawString.getValue();

        // Verify that the error message is returned when snippet doesn't exist
        assertTrue(
                content.contains("Snippet 'non-existent-snippet' not found"),
                "Should return error message for non-existent snippet");
    }

    @Test
    public void testSnippetExistsMethod() {
        // Test the snippetExists method
        boolean exists = snippetExtension.snippetExists("non-existent-snippet");
        assertFalse(exists, "Non-existent snippet should return false");
    }

    @Test
    public void testAvailableSnippetsMethod() {
        // Test that availableSnippets returns a string
        String snippets = snippetExtension.availableSnippets();
        assertNotNull(snippets, "availableSnippets() should not return null");
    }

    @Test
    public void testSnippetCountMethod() {
        // Test that snippetCount returns a non-negative number
        int count = snippetExtension.snippetCount();
        assertTrue(count >= 0, "snippetCount() should return non-negative number");
    }
}
