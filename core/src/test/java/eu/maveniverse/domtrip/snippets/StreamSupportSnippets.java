package eu.maveniverse.domtrip.snippets;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Snippet tests for stream support features documentation.
 */
public class StreamSupportSnippets extends BaseSnippetTest {

    @Test
    public void demonstrateBasicStreamNavigation() {
        // START: basic-stream-navigation
        Document doc = Document.of(
                """
            <dependencies>
                <dependency>
                    <groupId>junit</groupId>
                    <artifactId>junit</artifactId>
                </dependency>
                <dependency>
                    <groupId>mockito</groupId>
                    <artifactId>mockito-core</artifactId>
                </dependency>
                <dependency>
                    <groupId>hamcrest</groupId>
                    <artifactId>hamcrest-core</artifactId>
                </dependency>
            </dependencies>
            """);

        Editor editor = new Editor(doc);
        Element dependencies = doc.root();

        // Stream over child elements
        List<String> groupIds = dependencies
                .children("dependency")
                .map(dep -> dep.child("groupId").orElseThrow().textContent())
                .collect(Collectors.toList());

        // Result: ["junit", "mockito", "hamcrest"]
        // END: basic-stream-navigation

        Assertions.assertEquals(3, groupIds.size());
        Assertions.assertTrue(groupIds.contains("junit"));
        Assertions.assertTrue(groupIds.contains("mockito"));
        Assertions.assertTrue(groupIds.contains("hamcrest"));
    }

    @Test
    public void demonstrateFilteringStreams() {
        // START: filtering-streams
        Document doc = Document.of(
                """
            <dependencies>
                <dependency>
                    <groupId>junit</groupId>
                    <artifactId>junit</artifactId>
                    <scope>test</scope>
                </dependency>
                <dependency>
                    <groupId>slf4j</groupId>
                    <artifactId>slf4j-api</artifactId>
                    <scope>compile</scope>
                </dependency>
                <dependency>
                    <groupId>mockito</groupId>
                    <artifactId>mockito-core</artifactId>
                    <scope>test</scope>
                </dependency>
            </dependencies>
            """);

        Editor editor = new Editor(doc);
        Element dependencies = doc.root();

        // Find all test dependencies
        List<String> testDependencies = dependencies
                .children("dependency")
                .filter(dep -> "test"
                        .equals(dep.child("scope").map(Element::textContent).orElse("")))
                .map(dep -> dep.child("artifactId").orElseThrow().textContent())
                .collect(Collectors.toList());

        // Result: ["junit", "mockito-core"]
        // END: filtering-streams

        Assertions.assertEquals(2, testDependencies.size());
        Assertions.assertTrue(testDependencies.contains("junit"));
        Assertions.assertTrue(testDependencies.contains("mockito-core"));
        Assertions.assertFalse(testDependencies.contains("slf4j-api"));
    }

    @Test
    public void demonstrateDescendantStreams() {
        // START: descendant-streams
        Document doc = Document.of(
                """
            <project>
                <dependencies>
                    <dependency>
                        <groupId>junit</groupId>
                        <artifactId>junit</artifactId>
                    </dependency>
                </dependencies>
                <build>
                    <plugins>
                        <plugin>
                            <groupId>org.apache.maven.plugins</groupId>
                            <artifactId>maven-compiler-plugin</artifactId>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """);

        Editor editor = new Editor(doc);
        Element root = doc.root();

        // Find all groupId elements anywhere in the document
        List<String> allGroupIds =
                root.descendants("groupId").map(Element::textContent).collect(Collectors.toList());

        // Result: ["junit", "org.apache.maven.plugins"]
        // END: descendant-streams

        Assertions.assertEquals(2, allGroupIds.size());
        Assertions.assertTrue(allGroupIds.contains("junit"));
        Assertions.assertTrue(allGroupIds.contains("org.apache.maven.plugins"));
    }

    @Test
    public void demonstrateStreamTransformations() {
        // START: stream-transformations
        Document doc = Document.of(
                """
            <dependencies>
                <dependency>
                    <groupId>junit</groupId>
                    <artifactId>junit</artifactId>
                    <version>4.13.2</version>
                </dependency>
                <dependency>
                    <groupId>mockito</groupId>
                    <artifactId>mockito-core</artifactId>
                    <version>3.12.4</version>
                </dependency>
            </dependencies>
            """);

        Editor editor = new Editor(doc);
        Element dependencies = doc.root();

        // Transform dependency information
        List<String> dependencyInfo = dependencies
                .children("dependency")
                .map(dep -> {
                    String groupId = dep.child("groupId").orElseThrow().textContent();
                    String artifactId = dep.child("artifactId").orElseThrow().textContent();
                    String version = dep.child("version").orElseThrow().textContent();
                    return groupId + ":" + artifactId + ":" + version;
                })
                .collect(Collectors.toList());

        // Result: ["junit:junit:4.13.2", "mockito:mockito-core:3.12.4"]
        // END: stream-transformations

        Assertions.assertEquals(2, dependencyInfo.size());
        Assertions.assertTrue(dependencyInfo.contains("junit:junit:4.13.2"));
        Assertions.assertTrue(dependencyInfo.contains("mockito:mockito-core:3.12.4"));
    }

    @Test
    public void demonstrateStreamAggregation() {
        // START: stream-aggregation
        Document doc = Document.of(
                """
            <project>
                <dependencies>
                    <dependency><scope>test</scope></dependency>
                    <dependency><scope>compile</scope></dependency>
                    <dependency><scope>test</scope></dependency>
                    <dependency><scope>runtime</scope></dependency>
                </dependencies>
            </project>
            """);

        Editor editor = new Editor(doc);
        Element dependencies = doc.root().child("dependencies").orElseThrow();

        // Count dependencies by scope
        long testCount = dependencies
                .children("dependency")
                .filter(dep -> "test"
                        .equals(dep.child("scope").map(Element::textContent).orElse("")))
                .count();

        // Find first compile dependency
        Optional<Element> firstCompile = dependencies
                .children("dependency")
                .filter(dep -> "compile"
                        .equals(dep.child("scope").map(Element::textContent).orElse("")))
                .findFirst();

        // Check if any runtime dependencies exist
        boolean hasRuntime = dependencies.children("dependency").anyMatch(dep -> "runtime"
                .equals(dep.child("scope").map(Element::textContent).orElse("")));
        // END: stream-aggregation

        Assertions.assertEquals(2, testCount);
        Assertions.assertTrue(firstCompile.isPresent());
        Assertions.assertTrue(hasRuntime);
    }

    @Test
    public void demonstrateStreamModification() {
        // START: stream-modification
        Document doc = Document.of(
                """
            <dependencies>
                <dependency>
                    <groupId>junit</groupId>
                    <artifactId>junit</artifactId>
                    <version>4.12</version>
                </dependency>
                <dependency>
                    <groupId>mockito</groupId>
                    <artifactId>mockito-core</artifactId>
                    <version>3.0.0</version>
                </dependency>
            </dependencies>
            """);

        Editor editor = new Editor(doc);
        Element dependencies = doc.root();

        // Update all versions to latest
        dependencies.children("dependency").forEach(dep -> {
            Element version = dep.child("version").orElseThrow();
            String groupId = dep.child("groupId").orElseThrow().textContent();

            if ("junit".equals(groupId)) {
                editor.setTextContent(version, "4.13.2");
            } else if ("mockito".equals(groupId)) {
                editor.setTextContent(version, "4.6.1");
            }
        });

        String result = editor.toXml();
        // END: stream-modification

        Assertions.assertTrue(result.contains("4.13.2"));
        Assertions.assertTrue(result.contains("4.6.1"));
        Assertions.assertFalse(result.contains("4.12"));
        Assertions.assertFalse(result.contains("3.0.0"));
    }

    @Test
    public void demonstrateComplexStreamQueries() {
        // START: complex-stream-queries
        Document doc = Document.of(
                """
            <project>
                <dependencies>
                    <dependency>
                        <groupId>junit</groupId>
                        <artifactId>junit</artifactId>
                        <version>4.13.2</version>
                        <scope>test</scope>
                    </dependency>
                    <dependency>
                        <groupId>org.slf4j</groupId>
                        <artifactId>slf4j-api</artifactId>
                        <version>1.7.32</version>
                        <scope>compile</scope>
                    </dependency>
                </dependencies>
                <build>
                    <plugins>
                        <plugin>
                            <groupId>org.apache.maven.plugins</groupId>
                            <artifactId>maven-compiler-plugin</artifactId>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """);

        Editor editor = new Editor(doc);
        Element root = doc.root();

        // Complex query: Find all Apache Maven plugins
        List<String> mavenPlugins = root.descendants("plugin")
                .filter(plugin -> plugin.child("groupId")
                        .map(Element::textContent)
                        .orElse("")
                        .startsWith("org.apache.maven.plugins"))
                .map(plugin ->
                        plugin.child("artifactId").map(Element::textContent).orElse("unknown"))
                .collect(Collectors.toList());

        // Find dependencies with specific patterns
        boolean hasTestFramework = root.descendants("dependency").anyMatch(dep -> {
            String artifactId =
                    dep.child("artifactId").map(Element::textContent).orElse("");
            String scope = dep.child("scope").map(Element::textContent).orElse("compile");
            return (artifactId.contains("junit") || artifactId.contains("testng")) && "test".equals(scope);
        });
        // END: complex-stream-queries

        Assertions.assertEquals(1, mavenPlugins.size());
        Assertions.assertTrue(mavenPlugins.contains("maven-compiler-plugin"));
        Assertions.assertTrue(hasTestFramework);
    }

    @Test
    public void demonstrateStreamWithOptionals() {
        // START: stream-with-optionals
        Document doc = Document.of(
                """
            <dependencies>
                <dependency>
                    <groupId>junit</groupId>
                    <artifactId>junit</artifactId>
                    <!-- No version specified -->
                </dependency>
                <dependency>
                    <groupId>mockito</groupId>
                    <artifactId>mockito-core</artifactId>
                    <version>4.6.1</version>
                </dependency>
            </dependencies>
            """);

        Editor editor = new Editor(doc);
        Element dependencies = doc.root();

        // Handle optional elements gracefully
        List<String> dependenciesWithVersions = dependencies
                .children("dependency")
                .filter(dep -> dep.child("version").isPresent())
                .map(dep -> {
                    String artifactId = dep.child("artifactId").orElseThrow().textContent();
                    String version = dep.child("version").orElseThrow().textContent();
                    return artifactId + ":" + version;
                })
                .collect(Collectors.toList());

        // Find dependencies missing versions
        List<String> dependenciesMissingVersions = dependencies
                .children("dependency")
                .filter(dep -> dep.child("version").isEmpty())
                .map(dep -> dep.child("artifactId").orElseThrow().textContent())
                .collect(Collectors.toList());
        // END: stream-with-optionals

        Assertions.assertEquals(1, dependenciesWithVersions.size());
        Assertions.assertTrue(dependenciesWithVersions.contains("mockito-core:4.6.1"));

        Assertions.assertEquals(1, dependenciesMissingVersions.size());
        Assertions.assertTrue(dependenciesMissingVersions.contains("junit"));
    }

    @Test
    public void demonstrateParallelStreams() {
        // START: parallel-streams
        Document doc = Document.of(
                """
            <project>
                <dependencies>
                    <dependency><groupId>group1</groupId><artifactId>artifact1</artifactId></dependency>
                    <dependency><groupId>group2</groupId><artifactId>artifact2</artifactId></dependency>
                    <dependency><groupId>group3</groupId><artifactId>artifact3</artifactId></dependency>
                    <dependency><groupId>group4</groupId><artifactId>artifact4</artifactId></dependency>
                </dependencies>
            </project>
            """);

        Editor editor = new Editor(doc);
        Element dependencies = doc.root().child("dependencies").orElseThrow();

        // Process dependencies in parallel for performance
        List<String> processedDependencies = dependencies
                .children("dependency")
                .parallel()
                .map(dep -> {
                    String groupId = dep.child("groupId").orElseThrow().textContent();
                    String artifactId = dep.child("artifactId").orElseThrow().textContent();
                    // Simulate expensive processing
                    return groupId.toUpperCase() + ":" + artifactId.toUpperCase();
                })
                .collect(Collectors.toList());

        // Note: Order may vary with parallel streams
        // END: parallel-streams

        Assertions.assertEquals(4, processedDependencies.size());
        Assertions.assertTrue(processedDependencies.contains("GROUP1:ARTIFACT1"));
        Assertions.assertTrue(processedDependencies.contains("GROUP2:ARTIFACT2"));
    }

    @Test
    public void demonstrateStreamChaining() {
        // START: stream-chaining
        Document doc = Document.of(
                """
            <project>
                <profiles>
                    <profile>
                        <id>dev</id>
                        <dependencies>
                            <dependency>
                                <groupId>junit</groupId>
                                <artifactId>junit</artifactId>
                                <scope>test</scope>
                            </dependency>
                        </dependencies>
                    </profile>
                    <profile>
                        <id>prod</id>
                        <dependencies>
                            <dependency>
                                <groupId>slf4j</groupId>
                                <artifactId>slf4j-api</artifactId>
                                <scope>compile</scope>
                            </dependency>
                        </dependencies>
                    </profile>
                </profiles>
            </project>
            """);

        Editor editor = new Editor(doc);
        Element root = doc.root();

        // Chain multiple stream operations
        List<String> testDependenciesInProfiles = root.child("profiles")
                .orElseThrow()
                .children("profile")
                .flatMap(profile -> profile.child("dependencies")
                        .map(deps -> deps.children("dependency"))
                        .orElse(java.util.stream.Stream.empty()))
                .filter(dep -> "test"
                        .equals(dep.child("scope").map(Element::textContent).orElse("")))
                .map(dep -> dep.child("artifactId").orElseThrow().textContent())
                .collect(Collectors.toList());
        // END: stream-chaining

        Assertions.assertEquals(1, testDependenciesInProfiles.size());
        Assertions.assertTrue(testDependenciesInProfiles.contains("junit"));
    }
}
