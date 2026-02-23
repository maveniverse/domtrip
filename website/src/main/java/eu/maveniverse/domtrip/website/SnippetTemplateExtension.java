/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.website;

import io.quarkus.qute.RawString;
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
 * <p><strong>Important:</strong> If a snippet is not found, an {@link IllegalArgumentException}
 * will be thrown, causing the build to fail. This ensures that all referenced snippets
 * exist and prevents broken documentation.</p>
 *
 * <h3>Usage Examples:</h3>
 * <p>In a markdown file or Qute template:</p>
 * <pre>
 * ```java
 * {snippet('quick-start-basic')}
 * ```
 * </pre>
 *
 * <p>Optional: Check if a snippet exists before using it:</p>
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
     * Returns a RawString to prevent HTML escaping of code content.
     *
     * @param snippetName the name of the snippet to retrieve
     * @return the code snippet content as RawString
     * @throws IllegalArgumentException if the snippet is not found
     */
    public RawString snippet(String snippetName) {
        String snippet = snippetProcessor.getSnippet(snippetName);
        if (snippet != null) {
            return new RawString(snippet);
        } else {
            String errorMessage = "❌ ERROR: Snippet '" + snippetName + "' not found. Available snippets: "
                    + String.join(", ", snippetProcessor.getAvailableSnippets());
            System.err.println(errorMessage);
            throw new IllegalArgumentException(errorMessage);
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
