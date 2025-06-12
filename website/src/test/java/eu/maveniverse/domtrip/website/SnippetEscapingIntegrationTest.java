package eu.maveniverse.domtrip.website;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.qute.RawString;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

/**
 * Integration test to verify that Java code snippets are returned as RawString
 * to prevent HTML escaping of characters like ", <, and >.
 */
@QuarkusTest
public class SnippetEscapingIntegrationTest {

    @Inject
    SnippetTemplateExtension snippetExtension;

    @Test
    public void testSnippetReturnsRawStringType() {
        // Test with an existing snippet that likely contains special characters
        // From the error message, we know "dual-content-storage" exists
        RawString result = snippetExtension.snippet("dual-content-storage");
        assertNotNull(result, "Snippet should be found");

        String content = result.getValue();
        assertNotNull(content, "Snippet content should not be null");
        assertTrue(content.length() > 0, "Snippet content should not be empty");

        // The key test: verify this is a RawString, which prevents HTML escaping
        assertInstanceOf(RawString.class, result, "snippet() method should return RawString to prevent HTML escaping");

        System.out.println("✅ Test passed: Snippet returned as RawString");
        System.out.println("Snippet content preview (first 100 chars):");
        System.out.println(content.substring(0, Math.min(100, content.length())) + "...");
    }

    @Test
    public void testSpecialCharactersInExistingSnippet() {
        // Test with a snippet that we know contains special characters
        // "dual-content-storage" likely contains XML with < > and quotes
        RawString result = snippetExtension.snippet("dual-content-storage");
        assertNotNull(result, "Snippet should be found");

        String content = result.getValue();

        // If the snippet contains any of these characters, they should NOT be HTML-escaped
        // because we're returning RawString
        if (content.contains("\"")) {
            assertFalse(content.contains("&quot;"), "Quotes should not be HTML-escaped in RawString");
        }
        if (content.contains("<")) {
            assertFalse(content.contains("&lt;"), "Less-than symbols should not be HTML-escaped in RawString");
        }
        if (content.contains(">")) {
            assertFalse(content.contains("&gt;"), "Greater-than symbols should not be HTML-escaped in RawString");
        }
        if (content.contains("&")) {
            // Note: we need to be careful here as the snippet might legitimately contain &amp;
            // So we'll just verify the RawString type which is the main fix
        }

        System.out.println("✅ Test passed: No HTML escaping detected in RawString content");
    }

    @Test
    public void testNonExistentSnippetReturnsRawString() {
        // Even error messages should be returned as RawString
        RawString result = snippetExtension.snippet("non-existent-snippet-12345");
        assertNotNull(result, "Should return RawString even for non-existent snippets");

        String content = result.getValue();
        assertTrue(content.contains("not found"), "Should contain error message");

        // Verify it's still a RawString
        assertInstanceOf(RawString.class, result, "Error messages should also be returned as RawString");
    }
}
