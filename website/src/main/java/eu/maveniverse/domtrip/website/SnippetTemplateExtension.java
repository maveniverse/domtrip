package eu.maveniverse.domtrip.website;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Qute template extension that provides access to code snippets in templates.
 *
 * <p>This extension allows templates to include code snippets using the following syntax:</p>
 * <pre>
 * {snippet('snippet-name')}
 * </pre>
 *
 * <p>The snippets are automatically extracted from test files and made available
 * during the template rendering process.</p>
 *
 * <h3>Usage Examples:</h3>
 * <p>In a markdown file or Qute template:</p>
 * <pre>
 * ```java
 * {snippet('quick-start-basic')}
 * ```
 * </pre>
 *
 * <p>Or with error handling:</p>
 * <pre>
 * {#if snippetExists('quick-start-basic')}
 * ```java
 * {snippet('quick-start-basic')}
 * ```
 * {#else}
 * Code snippet not available.
 * {/if}
 * </pre>
 *
 * <p>List all available snippets:</p>
 * <pre>
 * Available snippets: {availableSnippets()}
 * </pre>
 */
@ApplicationScoped
@Named("snippets")
public class SnippetTemplateExtension {

    @Inject
    SnippetProcessor snippetProcessor;

    /**
     * Gets a code snippet by name for use in templates.
     *
     * @param snippetName the name of the snippet to retrieve
     * @return the code snippet content, or a placeholder message if not found
     */
    public String snippet(String snippetName) {
        String snippet = snippetProcessor.getSnippet(snippetName);
        if (snippet != null) {
            return snippet;
        } else {
            System.err.println("⚠️  Warning: Snippet '" + snippetName + "' not found. Available snippets: "
                    + String.join(", ", snippetProcessor.getAvailableSnippets()));
            return "// ❌ Snippet '" + snippetName + "' not found\n// Available snippets: "
                    + String.join(", ", snippetProcessor.getAvailableSnippets());
        }
    }

    /**
     * Checks if a snippet exists.
     *
     * @param snippetName the name of the snippet to check
     * @return true if the snippet exists, false otherwise
     */
    public boolean snippetExists(String snippetName) {
        return snippetProcessor.hasSnippet(snippetName);
    }

    /**
     * Gets all available snippet names for debugging purposes.
     *
     * @return a string containing all available snippet names
     */
    public String availableSnippets() {
        return String.join(", ", snippetProcessor.getAvailableSnippets());
    }

    /**
     * Gets the count of available snippets.
     *
     * @return the number of available snippets
     */
    public int snippetCount() {
        return snippetProcessor.getAvailableSnippets().size();
    }
}
