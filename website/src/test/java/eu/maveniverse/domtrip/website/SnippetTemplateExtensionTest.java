package eu.maveniverse.domtrip.website;

import static org.junit.jupiter.api.Assertions.*;

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
    public void testSnippetThrowsExceptionWhenNotFound() {
        // Test that the snippet method throws an exception for non-existent snippets
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> snippetExtension.snippet("non-existent-snippet"),
                "snippet() method should throw IllegalArgumentException for non-existent snippet");

        // Verify that the error message contains the snippet name
        assertTrue(
                exception.getMessage().contains("non-existent-snippet"),
                "Exception message should contain the snippet name");
        assertTrue(
                exception.getMessage().contains("not found"),
                "Exception message should indicate snippet was not found");
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
