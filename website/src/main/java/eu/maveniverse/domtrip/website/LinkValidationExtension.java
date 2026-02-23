/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.website;

import io.quarkus.logging.Log;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * ROQ extension that validates internal links in generated HTML files.
 * Prevents broken links from being deployed by failing the build when broken links are detected.
 */
@ApplicationScoped
public class LinkValidationExtension {

    @ConfigProperty(name = "domtrip.links.validate", defaultValue = "true")
    boolean validateLinks;

    @ConfigProperty(name = "domtrip.links.fail-on-broken", defaultValue = "true")
    boolean failOnBroken;

    @ConfigProperty(name = "domtrip.links.exclude-external", defaultValue = "true")
    boolean excludeExternal;

    // Site path will be determined from the config data
    private String sitePath = "/domtrip"; // Default based on config.yml

    // Pattern to match internal links in HTML: href="/..." or href="docs/..." but not href="http..."
    private static final Pattern INTERNAL_LINK_PATTERN =
            Pattern.compile("href=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE);

    // Pattern to match external links
    private static final Pattern EXTERNAL_LINK_PATTERN =
            Pattern.compile("^(https?://|mailto:|#)", Pattern.CASE_INSENSITIVE);

    /**
     * Event observer that triggers after application startup during static generation.
     */
    public void onStartup(@Observes StartupEvent event) {
        // Just log that we're ready to validate when shutdown happens
        if (validateLinks) {
            // Check both system property and environment variable
            String batchMode = System.getProperty("quarkus.roq.generator.batch");
            String batchModeEnv = System.getenv("QUARKUS_ROQ_GENERATOR_BATCH");

            boolean isBatchMode = "true".equals(batchMode) || "true".equals(batchModeEnv);

            if (isBatchMode) {
                Log.info("Link validation will run after ROQ generation completes");
            }
        }
    }

    /**
     * Event observer that triggers during application shutdown after static generation.
     */
    public void validateLinks(@Observes ShutdownEvent event) {
        if (!validateLinks) {
            Log.info("Link validation disabled");
            return;
        }

        // Only validate during static generation (batch mode)
        // Check both system property and environment variable
        String batchMode = System.getProperty("quarkus.roq.generator.batch");
        String batchModeEnv = System.getenv("QUARKUS_ROQ_GENERATOR_BATCH");

        Log.infof("Batch mode check - System property 'quarkus.roq.generator.batch': %s", batchMode);
        Log.infof("Batch mode check - Environment variable 'QUARKUS_ROQ_GENERATOR_BATCH': %s", batchModeEnv);

        boolean isBatchMode = "true".equals(batchMode) || "true".equals(batchModeEnv);

        if (!isBatchMode) {
            Log.debug("Not in batch mode, skipping link validation");
            return;
        }

        Log.info("Starting link validation after ROQ generation...");

        // Find the output directory
        Path outputDir = findOutputDirectory();
        if (outputDir == null || !Files.exists(outputDir)) {
            Log.warn("Output directory not found, skipping link validation");
            return;
        }

        try {
            List<BrokenLink> brokenLinks = validateInternalLinks(outputDir);

            if (!brokenLinks.isEmpty()) {
                Log.errorf("Found %d broken internal links:", brokenLinks.size());
                for (BrokenLink link : brokenLinks) {
                    Log.errorf("  %s (in file: %s)", link.targetUrl, link.sourceFile);
                }

                if (failOnBroken) {
                    throw new RuntimeException("Build failed due to broken internal links. "
                            + "Set domtrip.links.fail-on-broken=false to ignore.");
                }
            } else {
                Log.info("All internal links are valid!");
            }
        } catch (IOException e) {
            Log.error("Error during link validation", e);
            if (failOnBroken) {
                throw new RuntimeException("Link validation failed", e);
            }
        }
    }

    private Path findOutputDirectory() {
        // Try common output directories
        String[] possiblePaths = {"website/target/dist", "target/dist", "dist"};

        for (String pathStr : possiblePaths) {
            Path path = Path.of(pathStr);
            if (Files.exists(path)) {
                Log.infof("Found output directory: %s", path.toAbsolutePath());
                return path;
            }
        }
        return null;
    }

    private List<BrokenLink> validateInternalLinks(Path outputDir) throws IOException {
        List<BrokenLink> brokenLinks = new ArrayList<>();
        Set<String> existingFiles = collectExistingFiles(outputDir);

        Log.infof("Collected %d existing files/paths", existingFiles.size());
        Log.debugf("Existing files: %s", existingFiles);

        try (Stream<Path> paths = Files.walk(outputDir)) {
            List<Path> htmlFiles =
                    paths.filter(path -> path.toString().endsWith(".html")).toList();
            Log.infof("Validating links in %d HTML files...", htmlFiles.size());

            for (Path htmlFile : htmlFiles) {
                try {
                    brokenLinks.addAll(validateLinksInFile(htmlFile, outputDir, existingFiles));
                } catch (IOException e) {
                    Log.warn("Failed to validate links in file: " + htmlFile, e);
                }
            }
        }

        return brokenLinks;
    }

    private Set<String> collectExistingFiles(Path outputDir) throws IOException {
        Set<String> files = new HashSet<>();

        try (Stream<Path> paths = Files.walk(outputDir)) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                String relativePath = outputDir.relativize(path).toString();
                String normalizedPath = "/" + relativePath.replace('\\', '/');
                files.add(normalizedPath);

                // Also add directory paths for index.html files
                if (normalizedPath.endsWith("/index.html")) {
                    String dirPath = normalizedPath.substring(0, normalizedPath.length() - "/index.html".length());
                    if (!dirPath.equals("")) {
                        // Add directory with trailing slash
                        files.add(dirPath + "/");
                        // Add directory without trailing slash for links like "docs/api"
                        files.add(dirPath);
                    } else {
                        files.add("/");
                    }
                }
            });
        }

        return files;
    }

    private List<BrokenLink> validateLinksInFile(Path htmlFile, Path outputDir, Set<String> existingFiles)
            throws IOException {
        List<BrokenLink> brokenLinks = new ArrayList<>();
        String content = Files.readString(htmlFile);
        String relativeFilePath = outputDir.relativize(htmlFile).toString();

        Matcher matcher = INTERNAL_LINK_PATTERN.matcher(content);
        while (matcher.find()) {
            String url = matcher.group(1);

            // Skip external links if configured
            if (excludeExternal && EXTERNAL_LINK_PATTERN.matcher(url).find()) {
                continue;
            }

            // Skip anchors and fragments
            if (url.startsWith("#")) {
                continue;
            }

            // Skip dynamic resources (URLs with query parameters)
            if (isDynamicResource(url)) {
                continue;
            }

            // Remove fragment part for file existence check
            String urlWithoutFragment = url.split("#")[0];

            // Try multiple resolution strategies for relative URLs
            if (!isValidInternalLinkWithStrategies(urlWithoutFragment, relativeFilePath, existingFiles)) {
                brokenLinks.add(new BrokenLink(relativeFilePath, url));
                Log.debugf("Broken link found: %s -> %s", relativeFilePath, url);
            }
        }

        return brokenLinks;
    }

    /**
     * Validates a link using proper relative path resolution.
     */
    private boolean isValidInternalLinkWithStrategies(String url, String currentFilePath, Set<String> existingFiles) {
        // If URL starts with /, it's already absolute
        if (url.startsWith("/")) {
            // Strip site path prefix if present
            String normalizedUrl = stripSitePathPrefix(url);
            return isValidInternalLink(normalizedUrl, existingFiles);
        }

        // For relative URLs, try multiple resolution strategies for index.html files
        if (currentFilePath.endsWith("/index.html")) {
            // Strategy 1: Resolve relative to parent directory (e.g., javadoc/snapshot/index.html -> javadoc/snapshot/)
            String resolvedUrl = resolveRelativeUrlFromParent(url, currentFilePath);
            if (isValidInternalLink(resolvedUrl, existingFiles)) {
                return true;
            }

            // Strategy 2: Resolve relative to grandparent directory (e.g., examples/index.html -> /)
            String resolvedFromGrandparent = resolveRelativeUrlFromGrandparent(url, currentFilePath);
            if (isValidInternalLink(resolvedFromGrandparent, existingFiles)) {
                return true;
            }
        } else {
            // For non-index files, use standard resolution
            String resolvedUrl = resolveRelativeUrl(url, currentFilePath);
            if (isValidInternalLink(resolvedUrl, existingFiles)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Strips the site path prefix from an absolute URL if present.
     */
    private String stripSitePathPrefix(String url) {
        if (sitePath != null && !sitePath.isEmpty() && url.startsWith(sitePath)) {
            String stripped = url.substring(sitePath.length());
            // Ensure the result starts with /
            if (!stripped.startsWith("/")) {
                stripped = "/" + stripped;
            }
            return stripped;
        }
        return url;
    }

    /**
     * Checks if a URL is a dynamic resource that should be ignored during validation.
     */
    private boolean isDynamicResource(String url) {
        // Skip URLs with query parameters (cache busting, dynamic IDs)
        if (url.contains("?")) {
            return true;
        }

        // Skip RSS feeds and similar dynamic content
        if (url.equals("/rss.xml") || url.equals("/feed.xml") || url.equals("/atom.xml")) {
            return true;
        }

        // Skip known dynamic paths
        if (url.startsWith("/posts/tag/") || url.startsWith("/tags/")) {
            return true;
        }

        // Skip Javadoc external references
        if (url.equals("style.xsl")) {
            return true;
        }

        return false;
    }

    /**
     * Resolves a relative URL based on the current file's location.
     */
    private String resolveRelativeUrl(String url, String currentFilePath) {
        // If URL starts with /, it's already absolute
        if (url.startsWith("/")) {
            return url;
        }

        // For index.html files, resolve relative to the parent directory
        // e.g., docs/features/namespace-support/index.html should resolve links relative to docs/features/
        String baseDir = "/";
        if (currentFilePath.endsWith("/index.html")) {
            // Remove /index.html to get the directory containing the index.html
            String dirPath = currentFilePath.substring(0, currentFilePath.length() - "/index.html".length());
            // Then get the parent of that directory
            int lastSlash = dirPath.lastIndexOf('/');
            if (lastSlash >= 0) {
                baseDir = "/" + dirPath.substring(0, lastSlash + 1);
            } else {
                baseDir = "/";
            }
        } else {
            // For non-index files, get the directory of the current file (without the filename)
            int lastSlash = currentFilePath.lastIndexOf('/');
            if (lastSlash >= 0) {
                baseDir = "/" + currentFilePath.substring(0, lastSlash);
                if (!baseDir.endsWith("/")) {
                    baseDir += "/";
                }
            }
        }

        // Combine base directory with relative URL
        String combined = baseDir + url;

        // Normalize the result
        return normalizeUrl(combined);
    }

    /**
     * Resolves a relative URL from the parent directory of an index.html file.
     * e.g., javadoc/snapshot/index.html -> resolve from javadoc/snapshot/
     */
    private String resolveRelativeUrlFromParent(String url, String currentFilePath) {
        if (url.startsWith("/")) {
            return url;
        }

        // Remove /index.html to get the directory containing the index.html
        String dirPath = currentFilePath.substring(0, currentFilePath.length() - "/index.html".length());
        String baseDir = "/" + dirPath + "/";

        // Combine base directory with relative URL
        String combined = baseDir + url;

        // Normalize the result
        return normalizeUrl(combined);
    }

    /**
     * Resolves a relative URL from the grandparent directory of an index.html file.
     * e.g., examples/index.html -> resolve from /
     */
    private String resolveRelativeUrlFromGrandparent(String url, String currentFilePath) {
        if (url.startsWith("/")) {
            return url;
        }

        // Remove /index.html to get the directory containing the index.html
        String dirPath = currentFilePath.substring(0, currentFilePath.length() - "/index.html".length());

        // Get the parent of that directory (grandparent)
        int lastSlash = dirPath.lastIndexOf('/');
        String baseDir = "/";
        if (lastSlash >= 0) {
            baseDir = "/" + dirPath.substring(0, lastSlash + 1);
        }

        // Combine base directory with relative URL
        String combined = baseDir + url;

        // Normalize the result
        return normalizeUrl(combined);
    }

    private boolean isValidInternalLink(String url, Set<String> existingFiles) {
        // Skip empty URLs
        if (url.isEmpty()) {
            return true;
        }

        // Normalize the URL by resolving relative paths
        String normalizedUrl = normalizeUrl(url);

        // Check if the exact URL exists (for files)
        if (existingFiles.contains(normalizedUrl)) {
            return true;
        }

        // Check if it's a directory link that should have index.html
        if (normalizedUrl.endsWith("/")) {
            String indexPath = normalizedUrl + "index.html";
            if (existingFiles.contains(indexPath)) {
                return true;
            }
        } else {
            // For URLs without trailing slash, try both as file and as directory
            String indexPath = normalizedUrl + "/index.html";
            if (existingFiles.contains(indexPath)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Normalizes a URL by resolving relative path components like ../ and ./
     */
    private String normalizeUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }

        // Handle the case where URL starts with /
        boolean startsWithSlash = url.startsWith("/");

        // Split the URL into parts, filtering out empty parts from leading slash
        String[] parts = url.split("/");
        List<String> normalizedParts = new ArrayList<>();

        for (String part : parts) {
            if (part.equals("..")) {
                // Go up one level (remove last part if exists)
                if (!normalizedParts.isEmpty()) {
                    normalizedParts.remove(normalizedParts.size() - 1);
                }
                // If we're at the root and trying to go up, stay at root
            } else if (!part.equals(".") && !part.isEmpty()) {
                // Add non-empty, non-current-directory parts
                normalizedParts.add(part);
            }
        }

        // Reconstruct the URL
        String result;
        if (startsWithSlash || normalizedParts.isEmpty()) {
            result = "/" + String.join("/", normalizedParts);
        } else {
            result = String.join("/", normalizedParts);
        }

        // Preserve trailing slash if original had one and result is not just "/"
        if (url.endsWith("/") && !result.endsWith("/") && !result.equals("/")) {
            result += "/";
        }

        return result;
    }

    private static class BrokenLink {
        final String sourceFile;
        final String targetUrl;

        BrokenLink(String sourceFile, String targetUrl) {
            this.sourceFile = sourceFile;
            this.targetUrl = targetUrl;
        }
    }
}
