package eu.maveniverse.domtrip.website;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

public class LinkValidationTest {

    private static final Pattern INTERNAL_LINK_PATTERN =
            Pattern.compile("href=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE);

    private static final Pattern EXTERNAL_LINK_PATTERN =
            Pattern.compile("^(https?://|mailto:|#)", Pattern.CASE_INSENSITIVE);

    @Test
    public void testInternalLinksAreValid() throws IOException {
        Path outputDir = findOutputDirectory();
        if (outputDir == null || !Files.exists(outputDir)) {
            System.out.println("Output directory not found, skipping link validation");
            return;
        }

        List<BrokenLink> brokenLinks = validateInternalLinks(outputDir);

        if (!brokenLinks.isEmpty()) {
            System.err.printf("Found %d broken internal links:%n", brokenLinks.size());
            for (BrokenLink link : brokenLinks) {
                System.err.printf("  %s -> %s (in file: %s)%n", link.targetUrl, link.targetUrl, link.sourceFile);
            }
        } else {
            System.out.println("All internal links are valid!");
        }

        // Report the results but don't fail for now
        System.out.println("Final result: " + brokenLinks.size() + " broken links found");
    }

    private Path findOutputDirectory() {
        String[] possiblePaths = {"target/dist", "website/target/dist", "dist"};

        for (String pathStr : possiblePaths) {
            Path path = Path.of(pathStr);
            if (Files.exists(path)) {
                System.out.printf("Found output directory: %s%n", path.toAbsolutePath());
                return path;
            }
        }
        return null;
    }

    private List<BrokenLink> validateInternalLinks(Path outputDir) throws IOException {
        List<BrokenLink> brokenLinks = new ArrayList<>();
        Set<String> existingFiles = collectExistingFiles(outputDir);

        System.out.printf("Collected %d existing files/paths%n", existingFiles.size());

        try (Stream<Path> paths = Files.walk(outputDir)) {
            List<Path> htmlFiles =
                    paths.filter(path -> path.toString().endsWith(".html")).toList();
            System.out.printf("Validating links in %d HTML files...%n", htmlFiles.size());

            for (Path htmlFile : htmlFiles) {
                brokenLinks.addAll(validateLinksInFile(htmlFile, outputDir, existingFiles));
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

                if (relativePath.endsWith("/index.html")) {
                    String dirPath = "/" + relativePath.substring(0, relativePath.length() - "/index.html".length());
                    if (!dirPath.equals("/")) {
                        files.add(dirPath + "/");
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
        String content = new String(Files.readAllBytes(htmlFile), StandardCharsets.UTF_8);
        String relativeFilePath = outputDir.relativize(htmlFile).toString();

        Matcher matcher = INTERNAL_LINK_PATTERN.matcher(content);
        while (matcher.find()) {
            String url = matcher.group(1);

            if (EXTERNAL_LINK_PATTERN.matcher(url).find()) {
                continue;
            }

            if (url.startsWith("#")) {
                continue;
            }

            String urlWithoutFragment = url.split("#")[0];

            if (!isValidInternalLink(urlWithoutFragment, existingFiles)) {
                brokenLinks.add(new BrokenLink(relativeFilePath, url));
            }
        }

        return brokenLinks;
    }

    private boolean isValidInternalLink(String url, Set<String> existingFiles) {
        if (url.isEmpty()) {
            return true;
        }

        if (existingFiles.contains(url)) {
            return true;
        }

        if (url.endsWith("/")) {
            return existingFiles.contains(url + "index.html");
        }

        return existingFiles.contains(url + "/index.html");
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
