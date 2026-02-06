/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.maven;

import static eu.maveniverse.domtrip.maven.MavenPomElements.Elements.*;
import static org.junit.jupiter.api.Assertions.*;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Element;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Test for "lock plugin versions" functionality - adding pluginManagement section
 * with current plugin versions to existing POMs.
 */
class LockPluginVersionsTest {

    @Test
    void testLockPluginVersionsBasic() throws DomTripException {
        // Starting POM - simple project with some plugins but no pluginManagement
        String startingPom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                         https://maven.apache.org/xsd/maven-4.0.0.xsd">
                  <modelVersion>4.0.0</modelVersion>

                  <groupId>com.example</groupId>
                  <artifactId>test-project</artifactId>
                  <version>1.0.0</version>
                  <packaging>jar</packaging>

                  <properties>
                    <maven.compiler.source>17</maven.compiler.source>
                    <maven.compiler.target>17</maven.compiler.target>
                    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                  </properties>

                  <build>
                    <plugins>
                      <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-compiler-plugin</artifactId>
                        <version>3.11.0</version>
                        <configuration>
                          <source>17</source>
                          <target>17</target>
                        </configuration>
                      </plugin>
                      <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-surefire-plugin</artifactId>
                        <version>3.0.0</version>
                      </plugin>
                    </plugins>
                  </build>
                </project>
                """;

        Document doc = Document.of(startingPom);
        PomEditor editor = new PomEditor(doc);
        Element root = editor.root();

        // Find the build element
        Element build = editor.findChildElement(root, BUILD);
        assertNotNull(build, "Build element should exist");

        // Add pluginManagement section with current plugin versions
        lockPluginVersions(editor, build);

        String result = editor.toXml();

        // Expected result with proper formatting
        String expected = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                         https://maven.apache.org/xsd/maven-4.0.0.xsd">
                  <modelVersion>4.0.0</modelVersion>

                  <groupId>com.example</groupId>
                  <artifactId>test-project</artifactId>
                  <version>1.0.0</version>
                  <packaging>jar</packaging>

                  <properties>
                    <maven.compiler.source>17</maven.compiler.source>
                    <maven.compiler.target>17</maven.compiler.target>
                    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                  </properties>

                  <build>
                    <pluginManagement>
                      <plugins>
                        <plugin>
                          <groupId>org.apache.maven.plugins</groupId>
                          <artifactId>maven-compiler-plugin</artifactId>
                          <version>3.11.0</version>
                        </plugin>
                        <plugin>
                          <groupId>org.apache.maven.plugins</groupId>
                          <artifactId>maven-surefire-plugin</artifactId>
                          <version>3.0.0</version>
                        </plugin>
                      </plugins>
                    </pluginManagement>

                    <plugins>
                      <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-compiler-plugin</artifactId>
                        <version>3.11.0</version>
                        <configuration>
                          <source>17</source>
                          <target>17</target>
                        </configuration>
                      </plugin>
                      <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-surefire-plugin</artifactId>
                        <version>3.0.0</version>
                      </plugin>
                    </plugins>
                  </build>
                </project>
                """;

        System.out.println("=== ACTUAL RESULT ===");
        System.out.println(result);
        System.out.println("=== EXPECTED RESULT ===");
        System.out.println(expected);
        System.out.println("=== END COMPARISON ===");

        // Assert the full formatted XML
        assertEquals(expected.trim(), result.trim(), "The formatted XML should match expected output");
    }

    @Test
    void testLockPluginVersionsWithExistingPluginManagement() throws DomTripException {
        // Starting POM with existing pluginManagement
        String startingPom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test-project</artifactId>
                  <version>1.0.0</version>

                  <build>
                    <pluginManagement>
                      <plugins>
                        <plugin>
                          <groupId>org.apache.maven.plugins</groupId>
                          <artifactId>maven-clean-plugin</artifactId>
                          <version>3.2.0</version>
                        </plugin>
                      </plugins>
                    </pluginManagement>

                    <plugins>
                      <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-compiler-plugin</artifactId>
                        <version>3.11.0</version>
                      </plugin>
                      <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-surefire-plugin</artifactId>
                        <version>3.0.0</version>
                      </plugin>
                    </plugins>
                  </build>
                </project>
                """;

        Document doc = Document.of(startingPom);
        PomEditor editor = new PomEditor(doc);
        Element root = editor.root();

        Element build = editor.findChildElement(root, BUILD);
        assertNotNull(build);

        // Add current plugin versions to existing pluginManagement
        lockPluginVersions(editor, build);

        String result = editor.toXml();

        // Expected result with existing pluginManagement preserved and new plugins added
        String expected = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test-project</artifactId>
                  <version>1.0.0</version>

                  <build>
                    <pluginManagement>
                      <plugins>
                        <plugin>
                          <groupId>org.apache.maven.plugins</groupId>
                          <artifactId>maven-clean-plugin</artifactId>
                          <version>3.2.0</version>
                        </plugin>
                        <plugin>
                          <groupId>org.apache.maven.plugins</groupId>
                          <artifactId>maven-compiler-plugin</artifactId>
                          <version>3.11.0</version>
                        </plugin>
                        <plugin>
                          <groupId>org.apache.maven.plugins</groupId>
                          <artifactId>maven-surefire-plugin</artifactId>
                          <version>3.0.0</version>
                        </plugin>
                      </plugins>
                    </pluginManagement>

                    <plugins>
                      <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-compiler-plugin</artifactId>
                        <version>3.11.0</version>
                      </plugin>
                      <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-surefire-plugin</artifactId>
                        <version>3.0.0</version>
                      </plugin>
                    </plugins>
                  </build>
                </project>
                """;

        System.out.println("=== ACTUAL RESULT WITH EXISTING PLUGIN MANAGEMENT ===");
        System.out.println(result);
        System.out.println("=== EXPECTED RESULT ===");
        System.out.println(expected);
        System.out.println("=== END COMPARISON ===");

        // Assert the full formatted XML
        assertEquals(
                expected.trim(),
                result.trim(),
                "The formatted XML should match expected output with existing pluginManagement preserved");
    }

    /**
     * Helper method to implement "lock plugin versions" functionality.
     * This adds a pluginManagement section with current plugin versions.
     */
    private void lockPluginVersions(PomEditor editor, Element buildElement) throws DomTripException {
        // Find existing plugins
        Element pluginsElement = editor.findChildElement(buildElement, PLUGINS);
        if (pluginsElement == null) {
            return; // No plugins to lock
        }

        // Find or create pluginManagement
        Element pluginManagement = editor.findChildElement(buildElement, PLUGIN_MANAGEMENT);
        if (pluginManagement == null) {
            pluginManagement = editor.insertMavenElement(buildElement, PLUGIN_MANAGEMENT);
        }

        // Find or create plugins under pluginManagement
        Element managedPlugins = editor.findChildElement(pluginManagement, PLUGINS);
        if (managedPlugins == null) {
            managedPlugins = editor.insertMavenElement(pluginManagement, PLUGINS);
        }

        // Iterate through existing plugins and add them to pluginManagement
        for (Element plugin : pluginsElement.children(PLUGIN).toList()) {
            String groupId = getPluginCoordinate(plugin, GROUP_ID);
            String artifactId = getPluginCoordinate(plugin, ARTIFACT_ID);
            String version = getPluginCoordinate(plugin, VERSION);

            if (artifactId != null && version != null) {
                // Check if this plugin is already managed
                if (!isPluginAlreadyManaged(managedPlugins, groupId, artifactId)) {
                    // Add to pluginManagement
                    editor.plugins().addPlugin(managedPlugins, groupId, artifactId, version);
                }
            }
        }
    }

    private String getPluginCoordinate(Element plugin, String coordinateName) {
        return plugin.child(coordinateName).map(Element::textContent).orElse(null);
    }

    private boolean isPluginAlreadyManaged(Element managedPlugins, String groupId, String artifactId) {
        return managedPlugins.children(PLUGIN).anyMatch(plugin -> {
            String managedGroupId = getPluginCoordinate(plugin, GROUP_ID);
            String managedArtifactId = getPluginCoordinate(plugin, ARTIFACT_ID);

            // Handle default groupId for Maven plugins
            String effectiveGroupId = groupId != null ? groupId : "org.apache.maven.plugins";
            String effectiveManagedGroupId = managedGroupId != null ? managedGroupId : "org.apache.maven.plugins";

            return effectiveGroupId.equals(effectiveManagedGroupId) && artifactId.equals(managedArtifactId);
        });
    }

    @Test
    void testSimpleFormattingIssue() throws DomTripException {
        // Simple test to isolate the formatting issue
        String simpleXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                  <build>
                    <plugins>
                      <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-compiler-plugin</artifactId>
                      </plugin>
                    </plugins>
                  </build>
                </project>
                """;

        Document doc = Document.of(simpleXml);
        PomEditor editor = new PomEditor(doc);
        Element root = editor.root();
        Element build = editor.findChildElement(root, BUILD);

        // Find or create pluginManagement
        Element pluginManagement = editor.insertMavenElement(build, PLUGIN_MANAGEMENT);
        Element managedPlugins = editor.insertMavenElement(pluginManagement, PLUGINS);

        // Add a simple plugin
        editor.plugins().addPlugin(managedPlugins, "org.apache.maven.plugins", "maven-surefire-plugin", "3.0.0");

        String result = editor.toXml();

        // Write to file for examination
        try {
            Files.writeString(Path.of("target/simple-test-output.xml"), result);
        } catch (Exception e) {
            // ignore
        }

        // Just verify it contains the expected elements
        assertTrue(result.contains("<pluginManagement>"));
        assertTrue(result.contains("maven-surefire-plugin"));
    }
}
