/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.website;

import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Dynamically loads and caches code snippets from test files.
 * Automatically reloads snippets when source files change, making it perfect
 * for quarkus:dev auto-reload mode.
 *
 * <p>This processor scans Java test files for snippet markers in either format:</p>
 * <pre>
 * // START: snippet-name
 * // code here
 * // END: snippet-name
 * </pre>
 * <p>or:</p>
 * <pre>
 * // snippet:snippet-name
 * // code here
 * // end-snippet:snippet-name
 * </pre>
 *
 * <p>The extracted snippets can then be referenced in markdown files using
 * the Qute extension function: {snippet('snippet-name')}</p>
 */
@ApplicationScoped
@RegisterForReflection
public class SnippetProcessor {

    /** Log message suffix for snippet count messages. */
    private static final String SNIPPETS_SUFFIX = " snippets";

    private static final Pattern SNIPPET_START_PATTERN =
            Pattern.compile("\\s*//\\s*(?:START:|snippet:)\\s*([a-zA-Z0-9-_]+)\\s*");
    private static final Pattern SNIPPET_END_PATTERN =
            Pattern.compile("\\s*//\\s*(?:END:|end-snippet:)\\s*([a-zA-Z0-9-_]+)\\s*");

    private final Map<String, String> snippetCache = new HashMap<>();
    private volatile long lastModified = 0;
    private volatile Path[] snippetsDirectories = null;

    /**
     * Initialize snippets on startup for better performance.
     */
    void onStart(@Observes StartupEvent ev) {
        Log.info("Starting dynamic snippet processor...");
        loadSnippetsIfNeeded();
        Log.infof("Dynamic snippet processor ready with %d%s", snippetCache.size(), SNIPPETS_SUFFIX);
    }

    /**
     * Loads snippets if needed (first time or if files have changed).
     * This method checks file modification times and reloads automatically.
     */
    private synchronized void loadSnippetsIfNeeded() {
        try {
            if (snippetsDirectories == null) {
                Path[] dirs = findSnippetsDirectories();
                if (dirs.length == 0) {
                    Log.warn("Could not find any snippets directories");
                    return;
                }
                snippetsDirectories = dirs;
            }

            long currentModified = getDirectoriesLastModified(snippetsDirectories);
            if (currentModified > lastModified) {
                Log.info("Reloading snippets (files changed)...");
                Map<String, String> newCache = loadSnippetsIntoNewMap();
                snippetCache.clear();
                snippetCache.putAll(newCache);
                lastModified = currentModified;
                Log.infof("Reloaded %d%s", snippetCache.size(), SNIPPETS_SUFFIX);
            }
        } catch (IOException e) {
            Log.error("Failed to load code snippets", e);
        }
    }

    /**
     * Gets a code snippet by name.
     * Automatically reloads snippets if source files have changed.
     *
     * @param snippetName the name of the snippet to retrieve
     * @return the code snippet content, or null if not found
     */
    public String getSnippet(String snippetName) {
        loadSnippetsIfNeeded();
        return snippetCache.get(snippetName);
    }

    /**
     * Gets all available snippet names.
     * Automatically reloads snippets if source files have changed.
     *
     * @return a set of all snippet names
     */
    public Set<String> getAvailableSnippets() {
        loadSnippetsIfNeeded();
        return snippetCache.keySet();
    }

    /**
     * Checks if a snippet with the given name exists.
     * Automatically reloads snippets if source files have changed.
     *
     * @param snippetName the name to check
     * @return true if the snippet exists, false otherwise
     */
    public boolean hasSnippet(String snippetName) {
        loadSnippetsIfNeeded();
        return snippetCache.containsKey(snippetName);
    }

    /**
     * Gets the last modification time of all files in multiple directories.
     */
    private long getDirectoriesLastModified(Path[] directories) throws IOException {
        long maxModified = 0;
        for (Path directory : directories) {
            if (Files.exists(directory)) {
                try (Stream<Path> files = Files.walk(directory)) {
                    long dirModified = files.filter(path -> path.toString().endsWith(".java"))
                            .mapToLong(path -> {
                                try {
                                    return Files.getLastModifiedTime(path).toMillis();
                                } catch (IOException e) {
                                    return 0;
                                }
                            })
                            .max()
                            .orElse(0);
                    maxModified = Math.max(maxModified, dirModified);
                }
            }
        }
        return maxModified;
    }

    private void loadSnippets() throws IOException {
        if (snippetsDirectories == null || snippetsDirectories.length == 0) {
            Log.warn("No snippets directories available");
            return;
        }

        for (Path snippetsDirectory : snippetsDirectories) {
            if (Files.exists(snippetsDirectory)) {
                try (Stream<Path> files = Files.walk(snippetsDirectory)) {
                    files.filter(path -> path.toString().endsWith(".java"))
                            .forEach(path -> processFile(path, snippetCache));
                }
                Log.infof("Loaded snippets from %s", snippetsDirectory);
            }
        }

        Log.infof("Total loaded %d code snippets from %d directories", snippetCache.size(), snippetsDirectories.length);
    }

    private Map<String, String> loadSnippetsIntoNewMap() throws IOException {
        Map<String, String> tempMap = new HashMap<>();
        if (snippetsDirectories == null || snippetsDirectories.length == 0) {
            return tempMap;
        }

        for (Path snippetsDirectory : snippetsDirectories) {
            if (Files.exists(snippetsDirectory)) {
                try (Stream<Path> files = Files.walk(snippetsDirectory)) {
                    files.filter(path -> path.toString().endsWith(".java")).forEach(path -> processFile(path, tempMap));
                }
            }
        }

        Log.infof("Total loaded %d code snippets from %d directories", tempMap.size(), snippetsDirectories.length);
        return tempMap;
    }

    private Path[] findSnippetsDirectories() {
        // Try different possible locations relative to the website module
        String[][] possiblePaths = {
            // Core module snippets
            {
                "../core/src/test/java/eu/maveniverse/domtrip/snippets", // From website directory
                "../../core/src/test/java/eu/maveniverse/domtrip/snippets", // From website/target
                "core/src/test/java/eu/maveniverse/domtrip/snippets", // From project root
                "./core/src/test/java/eu/maveniverse/domtrip/snippets" // Alternative
            },
            // Maven module snippets
            {
                "../maven/src/test/java/eu/maveniverse/domtrip/maven/snippets", // From website directory
                "../../maven/src/test/java/eu/maveniverse/domtrip/maven/snippets", // From website/target
                "maven/src/test/java/eu/maveniverse/domtrip/maven/snippets", // From project root
                "./maven/src/test/java/eu/maveniverse/domtrip/maven/snippets" // Alternative
            }
        };

        java.util.List<Path> foundDirectories = new java.util.ArrayList<>();

        for (String[] modulePathOptions : possiblePaths) {
            for (String pathStr : modulePathOptions) {
                Path path = Paths.get(pathStr);
                if (Files.exists(path) && Files.isDirectory(path)) {
                    Log.infof("Found snippets directory: %s", path.toAbsolutePath());
                    foundDirectories.add(path);
                    break; // Found this module's directory, move to next module
                }
            }
        }

        if (foundDirectories.isEmpty()) {
            Log.error("Could not find any snippets directories.");
        }

        return foundDirectories.toArray(new Path[0]);
    }

    private void processFile(Path filePath, Map<String, String> targetCache) {
        try {
            String content = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
            int snippetsBefore = targetCache.size();
            extractSnippetsFromContent(content, filePath.getFileName().toString(), targetCache);
            int snippetsAfter = targetCache.size();
            if (snippetsAfter > snippetsBefore) {
                Log.infof(
                        "Processed %s - found %d%s",
                        filePath.getFileName(), snippetsAfter - snippetsBefore, SNIPPETS_SUFFIX);
            }
        } catch (IOException e) {
            Log.error("Error processing file " + filePath, e);
        }
    }

    private void extractSnippetsFromContent(String content, String fileName, Map<String, String> targetCache) {
        String[] lines = content.split("\n");
        String currentSnippetName = null;
        StringBuilder currentSnippet = new StringBuilder();

        for (String line : lines) {
            Matcher startMatcher = SNIPPET_START_PATTERN.matcher(line);
            Matcher endMatcher = SNIPPET_END_PATTERN.matcher(line);

            if (startMatcher.matches()) {
                // Start of a new snippet
                currentSnippetName = startMatcher.group(1);
                currentSnippet = new StringBuilder();
            } else if (endMatcher.matches() && currentSnippetName != null) {
                // End of current snippet
                String snippetName = endMatcher.group(1);
                if (snippetName.equals(currentSnippetName)) {
                    // Store the snippet with normalized indentation
                    String snippetContent = normalizeIndentation(currentSnippet.toString());
                    targetCache.put(currentSnippetName, snippetContent);
                    Log.infof("Extracted snippet: %s from %s", currentSnippetName, fileName);
                } else {
                    Log.warnf(
                            "Mismatched snippet markers in %s: START %s but END %s",
                            fileName, currentSnippetName, snippetName);
                }
                currentSnippetName = null;
                currentSnippet = new StringBuilder();
            } else if (currentSnippetName != null) {
                // Inside a snippet - collect the line
                if (currentSnippet.length() > 0) {
                    currentSnippet.append("\n");
                }
                currentSnippet.append(line);
            }
        }

        // Check for unclosed snippets
        if (currentSnippetName != null) {
            Log.warnf("Unclosed snippet '%s' in %s", currentSnippetName, fileName);
        }
    }

    /**
     * Normalizes indentation by finding the minimal indentation and removing it from all lines.
     * Also removes leading and trailing empty lines.
     */
    private String normalizeIndentation(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        String[] lines = content.split("\n");
        int[] bounds = findContentBounds(lines);
        int start = bounds[0];
        int end = bounds[1];

        if (start > end) {
            return ""; // All lines were empty
        }

        int minIndent = findMinimalIndentation(lines, start, end);
        return buildNormalizedContent(lines, start, end, minIndent);
    }

    /**
     * Finds the first and last non-empty line indices.
     */
    private int[] findContentBounds(String[] lines) {
        int start = 0;
        int end = lines.length - 1;

        while (start <= end && lines[start].trim().isEmpty()) {
            start++;
        }
        while (end >= start && lines[end].trim().isEmpty()) {
            end--;
        }
        return new int[] {start, end};
    }

    /**
     * Finds the minimal indentation level across non-empty lines.
     */
    private int findMinimalIndentation(String[] lines, int start, int end) {
        int minIndent = Integer.MAX_VALUE;
        for (int i = start; i <= end; i++) {
            String line = lines[i];
            if (!line.trim().isEmpty()) {
                int indent = 0;
                while (indent < line.length() && line.charAt(indent) == ' ') {
                    indent++;
                }
                minIndent = Math.min(minIndent, indent);
            }
        }
        return minIndent == Integer.MAX_VALUE ? 0 : minIndent;
    }

    /**
     * Builds the normalized content by removing minimal indentation from all lines.
     */
    private String buildNormalizedContent(String[] lines, int start, int end, int minIndent) {
        StringBuilder result = new StringBuilder();
        for (int i = start; i <= end; i++) {
            if (i > start) {
                result.append("\n");
            }
            String line = lines[i];
            if (!line.trim().isEmpty()) {
                result.append(line.length() >= minIndent ? line.substring(minIndent) : line);
            }
        }
        return result.toString();
    }
}
