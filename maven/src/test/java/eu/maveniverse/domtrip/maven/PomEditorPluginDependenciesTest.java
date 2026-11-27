/*
 * Copyright (c) 2023-2026 Maveniverse Org.
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
import org.junit.jupiter.api.Test;

/**
 * Tests for plugin dependency management via {@link PomEditor.Plugins.PluginDependencies}.
 */
class PomEditorPluginDependenciesTest {

    private PomEditor editorOf(String xml) {
        return new PomEditor(Document.of(xml));
    }

    // -------------------------------------------------------------------------
    // pluginDependencies(boolean upsert, Coordinates plugin)
    // -------------------------------------------------------------------------

    @Test
    void testPluginDependencies_add() throws DomTripException {
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                  <build>
                    <plugins>
                      <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-compiler-plugin</artifactId>
                        <version>3.11.0</version>
                      </plugin>
                    </plugins>
                  </build>
                </project>
                """;
        PomEditor editor = editorOf(pom);
        Coordinates plugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", null);

        PomEditor.Plugins.PluginDependencies pd = editor.plugins().pluginDependencies(false, plugin);
        assertNotNull(pd, "PluginDependencies should not be null for existing plugin");

        Element added = pd.addDependency("asm", "asm", "3.3.1");
        assertNotNull(added);
        assertEquals("dependency", added.name());

        String xml = editor.toXml();
        assertTrue(xml.contains("<groupId>asm</groupId>"));
        assertTrue(xml.contains("<artifactId>asm</artifactId>"));
        assertTrue(xml.contains("<version>3.3.1</version>"));
    }

    @Test
    void testPluginDependencies_upsertCreatesBuildStructure() throws DomTripException {
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                  <groupId>com.example</groupId>
                  <artifactId>my-project</artifactId>
                  <version>1.0.0</version>
                </project>
                """;
        PomEditor editor = editorOf(pom);
        Coordinates plugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");
        Coordinates dep = Coordinates.of("asm", "asm", "3.3.1");

        PomEditor.Plugins.PluginDependencies pd = editor.plugins().pluginDependencies(true, plugin);
        assertNotNull(pd, "upsert=true must always return a non-null PluginDependencies");

        boolean result = pd.updateDependency(true, dep);
        assertTrue(result);

        String xml = editor.toXml();
        assertTrue(xml.contains("<build>"));
        assertTrue(xml.contains("<plugins>"));
        assertTrue(xml.contains("<plugin>"));
        assertTrue(xml.contains("<artifactId>maven-compiler-plugin</artifactId>"));
        assertTrue(xml.contains("<dependencies>"));
        assertTrue(xml.contains("<groupId>asm</groupId>"));
        assertTrue(xml.contains("<version>3.3.1</version>"));
    }

    @Test
    void testPluginDependencies_noUpsertMissingPluginReturnsNull() throws DomTripException {
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                  <groupId>com.example</groupId>
                  <artifactId>my-project</artifactId>
                  <version>1.0.0</version>
                </project>
                """;
        PomEditor editor = editorOf(pom);
        Coordinates plugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", null);

        PomEditor.Plugins.PluginDependencies pd = editor.plugins().pluginDependencies(false, plugin);
        assertNull(pd, "upsert=false on missing plugin must return null");
    }

    @Test
    void testPluginDependencies_updateExistingDependencyVersion() throws DomTripException {
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                  <build>
                    <plugins>
                      <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-compiler-plugin</artifactId>
                        <version>3.11.0</version>
                        <dependencies>
                          <dependency>
                            <groupId>asm</groupId>
                            <artifactId>asm</artifactId>
                            <version>3.3.1</version>
                          </dependency>
                        </dependencies>
                      </plugin>
                    </plugins>
                  </build>
                </project>
                """;
        PomEditor editor = editorOf(pom);
        Coordinates plugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", null);
        Coordinates dep = Coordinates.of("asm", "asm", "3.3.2");

        boolean updated = editor.plugins().pluginDependencies(false, plugin).updateDependency(false, dep);
        assertTrue(updated);

        String xml = editor.toXml();
        assertTrue(xml.contains("<version>3.3.2</version>"));
        assertFalse(xml.contains("3.3.1"));
    }

    @Test
    void testPluginDependencies_updateUpsertInsertsVersionWhenMissing() throws DomTripException {
        // dep exists with no <version> element; upsert=true should insert <version>
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                  <build>
                    <plugins>
                      <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-compiler-plugin</artifactId>
                        <version>3.11.0</version>
                        <dependencies>
                          <dependency>
                            <groupId>asm</groupId>
                            <artifactId>asm</artifactId>
                          </dependency>
                        </dependencies>
                      </plugin>
                    </plugins>
                  </build>
                </project>
                """;
        PomEditor editor = editorOf(pom);
        Coordinates plugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", null);
        Coordinates dep = Coordinates.of("asm", "asm", "3.3.1");

        boolean updated = editor.plugins().pluginDependencies(false, plugin).updateDependency(true, dep);
        assertTrue(updated, "upsert=true should insert <version> when dep exists but has none");
        assertTrue(editor.toXml().contains("<version>3.3.1</version>"));
    }

    @Test
    void testPluginDependencies_updateNoUpsertVersionlessDepsReturnsFalse() throws DomTripException {
        // dep exists with no <version> element; upsert=false should return false
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                  <build>
                    <plugins>
                      <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-compiler-plugin</artifactId>
                        <version>3.11.0</version>
                        <dependencies>
                          <dependency>
                            <groupId>asm</groupId>
                            <artifactId>asm</artifactId>
                          </dependency>
                        </dependencies>
                      </plugin>
                    </plugins>
                  </build>
                </project>
                """;
        PomEditor editor = editorOf(pom);
        Coordinates plugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", null);
        Coordinates dep = Coordinates.of("asm", "asm", "3.3.1");

        boolean updated = editor.plugins().pluginDependencies(false, plugin).updateDependency(false, dep);
        assertFalse(updated, "upsert=false on a version-less dep should return false");
        assertFalse(editor.toXml().contains("<version>3.3.1</version>"));
    }

    @Test
    void testPluginDependencies_updateNoUpsertMissingDepReturnsFalse() throws DomTripException {
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                  <build>
                    <plugins>
                      <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-compiler-plugin</artifactId>
                        <version>3.11.0</version>
                      </plugin>
                    </plugins>
                  </build>
                </project>
                """;
        PomEditor editor = editorOf(pom);
        Coordinates plugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", null);
        Coordinates dep = Coordinates.of("asm", "asm", "3.3.1");

        boolean updated = editor.plugins().pluginDependencies(false, plugin).updateDependency(false, dep);
        assertFalse(updated);
        assertFalse(editor.toXml().contains("<dependencies>"));
    }

    @Test
    void testPluginDependencies_updateUpsertPreservesTypeAndClassifier() throws DomTripException {
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                  <build>
                    <plugins>
                      <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-compiler-plugin</artifactId>
                        <version>3.11.0</version>
                      </plugin>
                    </plugins>
                  </build>
                </project>
                """;
        PomEditor editor = editorOf(pom);
        Coordinates plugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", null);
        Coordinates dep = Coordinates.of("org.example", "my-lib", "1.0.0", "tests", "test-jar");

        boolean updated = editor.plugins().pluginDependencies(true, plugin).updateDependency(true, dep);
        assertTrue(updated);

        String xml = editor.toXml();
        assertTrue(xml.contains("<classifier>tests</classifier>"), "classifier must be preserved");
        assertTrue(xml.contains("<type>test-jar</type>"), "type must be preserved");
    }

    @Test
    void testPluginDependencies_delete() throws DomTripException {
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                  <build>
                    <plugins>
                      <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-compiler-plugin</artifactId>
                        <version>3.11.0</version>
                        <dependencies>
                          <dependency>
                            <groupId>asm</groupId>
                            <artifactId>asm</artifactId>
                            <version>3.3.1</version>
                          </dependency>
                        </dependencies>
                      </plugin>
                    </plugins>
                  </build>
                </project>
                """;
        PomEditor editor = editorOf(pom);
        Coordinates plugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", null);
        Coordinates dep = Coordinates.of("asm", "asm", null);

        boolean deleted = editor.plugins().pluginDependencies(false, plugin).deleteDependency(dep);
        assertTrue(deleted);

        String xml = editor.toXml();
        assertFalse(xml.contains("<artifactId>asm</artifactId>"));
        assertFalse(xml.contains("<dependencies/>"), "empty <dependencies> container should be removed");
    }

    @Test
    void testPluginDependencies_deleteLastDependencyRemovesContainer() throws DomTripException {
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                  <build>
                    <plugins>
                      <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-compiler-plugin</artifactId>
                        <version>3.11.0</version>
                        <dependencies>
                          <dependency>
                            <groupId>asm</groupId>
                            <artifactId>asm</artifactId>
                            <version>3.3.1</version>
                          </dependency>
                        </dependencies>
                      </plugin>
                    </plugins>
                  </build>
                </project>
                """;
        PomEditor editor = editorOf(pom);
        Coordinates plugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", null);
        Coordinates dep = Coordinates.of("asm", "asm", null);

        boolean deleted = editor.plugins().pluginDependencies(false, plugin).deleteDependency(dep);
        assertTrue(deleted);

        String xml = editor.toXml();
        assertFalse(xml.contains("<dependencies"), "empty <dependencies> container must be removed");
    }

    @Test
    void testPluginDependencies_deleteMissingReturnsFalse() throws DomTripException {
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                  <build>
                    <plugins>
                      <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-compiler-plugin</artifactId>
                        <version>3.11.0</version>
                      </plugin>
                    </plugins>
                  </build>
                </project>
                """;
        PomEditor editor = editorOf(pom);
        Coordinates plugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", null);
        Coordinates dep = Coordinates.of("asm", "asm", null);

        boolean deleted = editor.plugins().pluginDependencies(false, plugin).deleteDependency(dep);
        assertFalse(deleted);
    }

    @Test
    void testPluginDependencies_deleteDependencyVersion() throws DomTripException {
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                  <build>
                    <plugins>
                      <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-compiler-plugin</artifactId>
                        <version>3.11.0</version>
                        <dependencies>
                          <dependency>
                            <groupId>asm</groupId>
                            <artifactId>asm</artifactId>
                            <version>3.3.1</version>
                          </dependency>
                        </dependencies>
                      </plugin>
                    </plugins>
                  </build>
                </project>
                """;
        PomEditor editor = editorOf(pom);
        Coordinates plugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", null);
        Coordinates dep = Coordinates.of("asm", "asm", null);

        boolean deleted = editor.plugins().pluginDependencies(false, plugin).deleteDependencyVersion(dep);
        assertTrue(deleted);

        String xml = editor.toXml();
        // dependency element itself must still be there
        assertTrue(xml.contains("<artifactId>asm</artifactId>"));
        // but version must be gone
        assertFalse(xml.contains("<version>3.3.1</version>"));
    }

    // -------------------------------------------------------------------------
    // managedPluginDependencies(boolean upsert, Coordinates plugin)
    // -------------------------------------------------------------------------

    @Test
    void testManagedPluginDependencies_add() throws DomTripException {
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                  <build>
                    <pluginManagement>
                      <plugins>
                        <plugin>
                          <groupId>org.apache.maven.plugins</groupId>
                          <artifactId>maven-compiler-plugin</artifactId>
                          <version>3.11.0</version>
                        </plugin>
                      </plugins>
                    </pluginManagement>
                  </build>
                </project>
                """;
        PomEditor editor = editorOf(pom);
        Coordinates plugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", null);

        PomEditor.Plugins.PluginDependencies pd = editor.plugins().managedPluginDependencies(false, plugin);
        assertNotNull(pd);

        pd.addDependency("asm", "asm", "3.3.1");

        String xml = editor.toXml();
        assertTrue(xml.contains("<pluginManagement>"));
        assertTrue(xml.contains("<groupId>asm</groupId>"));
        assertTrue(xml.contains("<version>3.3.1</version>"));
    }

    @Test
    void testManagedPluginDependencies_upsertCreatesStructure() throws DomTripException {
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                  <groupId>com.example</groupId>
                  <artifactId>my-project</artifactId>
                  <version>1.0.0</version>
                </project>
                """;
        PomEditor editor = editorOf(pom);
        Coordinates plugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");
        Coordinates dep = Coordinates.of("asm", "asm", "3.3.1");

        PomEditor.Plugins.PluginDependencies pd = editor.plugins().managedPluginDependencies(true, plugin);
        assertNotNull(pd);
        pd.updateDependency(true, dep);

        String xml = editor.toXml();
        assertTrue(xml.contains("<pluginManagement>"));
        assertTrue(xml.contains("<artifactId>maven-compiler-plugin</artifactId>"));
        assertTrue(xml.contains("<groupId>asm</groupId>"));
    }

    @Test
    void testManagedPluginDependencies_noUpsertMissingReturnsNull() throws DomTripException {
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                  <groupId>com.example</groupId>
                  <artifactId>my-project</artifactId>
                  <version>1.0.0</version>
                </project>
                """;
        PomEditor editor = editorOf(pom);
        Coordinates plugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", null);

        PomEditor.Plugins.PluginDependencies pd = editor.plugins().managedPluginDependencies(false, plugin);
        assertNull(pd);
    }

    @Test
    void testManagedPluginDependencies_delete() throws DomTripException {
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                  <build>
                    <pluginManagement>
                      <plugins>
                        <plugin>
                          <groupId>org.apache.maven.plugins</groupId>
                          <artifactId>maven-compiler-plugin</artifactId>
                          <version>3.11.0</version>
                          <dependencies>
                            <dependency>
                              <groupId>asm</groupId>
                              <artifactId>asm</artifactId>
                              <version>3.3.1</version>
                            </dependency>
                          </dependencies>
                        </plugin>
                      </plugins>
                    </pluginManagement>
                  </build>
                </project>
                """;
        PomEditor editor = editorOf(pom);
        Coordinates plugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", null);

        boolean deleted = editor.plugins()
                .managedPluginDependencies(false, plugin)
                .deleteDependency(Coordinates.of("asm", "asm", null));
        assertTrue(deleted);
        assertFalse(editor.toXml().contains("<artifactId>asm</artifactId>"));
    }

    // -------------------------------------------------------------------------
    // pluginDependencies(Element pluginElement)
    // -------------------------------------------------------------------------

    @Test
    void testPluginDependencies_elementOverload() throws DomTripException {
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                  <build>
                    <plugins>
                      <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-compiler-plugin</artifactId>
                        <version>3.11.0</version>
                      </plugin>
                    </plugins>
                  </build>
                </project>
                """;
        PomEditor editor = editorOf(pom);

        // Locate the plugin element manually
        Element build = editor.findChildElement(editor.root(), BUILD);
        Element plugins = editor.findChildElement(build, PLUGINS);
        Element pluginEl = plugins.childElements(PLUGIN).findFirst().orElseThrow();

        PomEditor.Plugins.PluginDependencies pd = editor.plugins().pluginDependencies(pluginEl);
        pd.addDependency("asm", "asm", "3.3.1");

        String xml = editor.toXml();
        assertTrue(xml.contains("<groupId>asm</groupId>"));
    }

    @Test
    void testPluginDependencies_elementOverload_nullThrows() {
        PomEditor editor = new PomEditor(Document.of("<project/>"));
        PomEditor.Plugins plugins = editor.plugins();
        assertThrows(DomTripException.class, () -> plugins.pluginDependencies((Element) null));
    }

    @Test
    void testPluginDependencies_elementOverload_wrongElementThrows() throws DomTripException {
        PomEditor editor = new PomEditor(Document.of("<project/>"));
        Element root = editor.root();
        PomEditor.Plugins plugins = editor.plugins();
        assertThrows(DomTripException.class, () -> plugins.pluginDependencies(root));
    }

    // -------------------------------------------------------------------------
    // Exclusion helpers
    // -------------------------------------------------------------------------

    @Test
    void testPluginDependencies_addExclusion() throws DomTripException {
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                  <build>
                    <plugins>
                      <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-compiler-plugin</artifactId>
                        <version>3.11.0</version>
                        <dependencies>
                          <dependency>
                            <groupId>asm</groupId>
                            <artifactId>asm</artifactId>
                            <version>3.3.1</version>
                          </dependency>
                        </dependencies>
                      </plugin>
                    </plugins>
                  </build>
                </project>
                """;
        PomEditor editor = editorOf(pom);
        Coordinates plugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", null);
        Coordinates dep = Coordinates.of("asm", "asm", null);
        Coordinates excl = Coordinates.of("commons-logging", "commons-logging", null);

        Element added = editor.plugins().pluginDependencies(false, plugin).addExclusion(dep, excl);
        assertNotNull(added);
        assertEquals("exclusion", added.name());

        String xml = editor.toXml();
        assertTrue(xml.contains("<exclusions>"));
        assertTrue(xml.contains("<groupId>commons-logging</groupId>"));
        assertTrue(xml.contains("<artifactId>commons-logging</artifactId>"));
    }

    @Test
    void testPluginDependencies_addExclusion_missingDepThrows() throws DomTripException {
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                  <build>
                    <plugins>
                      <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-compiler-plugin</artifactId>
                        <version>3.11.0</version>
                        <dependencies>
                          <dependency>
                            <groupId>asm</groupId>
                            <artifactId>asm</artifactId>
                            <version>3.3.1</version>
                          </dependency>
                        </dependencies>
                      </plugin>
                    </plugins>
                  </build>
                </project>
                """;
        PomEditor editor = editorOf(pom);
        Coordinates plugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", null);
        Coordinates nonexistent = Coordinates.of("org.example", "nonexistent", null);
        Coordinates excl = Coordinates.of("commons-logging", "commons-logging", null);

        assertThrows(
                DomTripException.class,
                () -> {
                    PomEditor.Plugins.PluginDependencies pd = editor.plugins().pluginDependencies(false, plugin);
                    pd.addExclusion(nonexistent, excl);
                },
                "addExclusion should throw when the dependency is not found");
    }

    @Test
    void testPluginDependencies_addExclusion_noDepsContainerThrows() throws DomTripException {
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                  <build>
                    <plugins>
                      <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-compiler-plugin</artifactId>
                        <version>3.11.0</version>
                      </plugin>
                    </plugins>
                  </build>
                </project>
                """;
        PomEditor editor = editorOf(pom);
        Coordinates plugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", null);
        Coordinates dep = Coordinates.of("asm", "asm", null);
        Coordinates excl = Coordinates.of("commons-logging", "commons-logging", null);

        assertThrows(
                DomTripException.class,
                () -> {
                    PomEditor.Plugins.PluginDependencies pd = editor.plugins().pluginDependencies(false, plugin);
                    pd.addExclusion(dep, excl);
                },
                "addExclusion should throw when there is no <dependencies> container");
    }

    @Test
    void testPluginDependencies_deleteExclusion() throws DomTripException {
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                  <build>
                    <plugins>
                      <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-compiler-plugin</artifactId>
                        <version>3.11.0</version>
                        <dependencies>
                          <dependency>
                            <groupId>asm</groupId>
                            <artifactId>asm</artifactId>
                            <version>3.3.1</version>
                            <exclusions>
                              <exclusion>
                                <groupId>commons-logging</groupId>
                                <artifactId>commons-logging</artifactId>
                              </exclusion>
                            </exclusions>
                          </dependency>
                        </dependencies>
                      </plugin>
                    </plugins>
                  </build>
                </project>
                """;
        PomEditor editor = editorOf(pom);
        Coordinates plugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", null);
        Coordinates dep = Coordinates.of("asm", "asm", null);
        Coordinates excl = Coordinates.of("commons-logging", "commons-logging", null);

        boolean deleted = editor.plugins().pluginDependencies(false, plugin).deleteExclusion(dep, excl);
        assertTrue(deleted);

        String xml = editor.toXml();
        assertFalse(xml.contains("<exclusions>"));
        assertFalse(xml.contains("commons-logging"));
    }

    @Test
    void testPluginDependencies_hasExclusion() throws DomTripException {
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                  <build>
                    <plugins>
                      <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-compiler-plugin</artifactId>
                        <version>3.11.0</version>
                        <dependencies>
                          <dependency>
                            <groupId>asm</groupId>
                            <artifactId>asm</artifactId>
                            <version>3.3.1</version>
                            <exclusions>
                              <exclusion>
                                <groupId>commons-logging</groupId>
                                <artifactId>commons-logging</artifactId>
                              </exclusion>
                            </exclusions>
                          </dependency>
                        </dependencies>
                      </plugin>
                    </plugins>
                  </build>
                </project>
                """;
        PomEditor editor = editorOf(pom);
        Coordinates plugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", null);
        Coordinates dep = Coordinates.of("asm", "asm", null);
        Coordinates excl = Coordinates.of("commons-logging", "commons-logging", null);
        Coordinates absent = Coordinates.of("log4j", "log4j", null);

        PomEditor.Plugins.PluginDependencies pd = editor.plugins().pluginDependencies(false, plugin);
        assertTrue(pd.hasExclusion(dep, excl), "should find the existing exclusion");
        assertFalse(pd.hasExclusion(dep, absent), "should not find a non-existent exclusion");
    }

    // -------------------------------------------------------------------------
    // Profile-scoped plugin dependencies
    // -------------------------------------------------------------------------

    @Test
    void testPluginDependencies_profileScoped() throws DomTripException {
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                  <profiles>
                    <profile>
                      <id>release</id>
                      <build>
                        <plugins>
                          <plugin>
                            <groupId>org.apache.maven.plugins</groupId>
                            <artifactId>maven-compiler-plugin</artifactId>
                            <version>3.11.0</version>
                          </plugin>
                        </plugins>
                      </build>
                    </profile>
                  </profiles>
                </project>
                """;
        PomEditor editor = editorOf(pom);
        Coordinates plugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", null);
        Coordinates dep = Coordinates.of("asm", "asm", "3.3.1");

        PomEditor.Plugins.PluginDependencies pd =
                editor.plugins().forProfile("release").pluginDependencies(false, plugin);
        assertNotNull(pd, "PluginDependencies should resolve under profile");

        pd.addDependency(dep.groupId(), dep.artifactId(), dep.version());

        String xml = editor.toXml();
        // dependency must be inside the profile, not at project root build
        assertTrue(xml.contains("<profiles>"));
        assertTrue(xml.contains("<groupId>asm</groupId>"));
    }

    @Test
    void testPluginDependencies_implicitGroupId() throws DomTripException {
        // Plugin declared without <groupId> — relies on default org.apache.maven.plugins
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                  <build>
                    <plugins>
                      <plugin>
                        <artifactId>maven-compiler-plugin</artifactId>
                        <version>3.11.0</version>
                      </plugin>
                    </plugins>
                  </build>
                </project>
                """;
        PomEditor editor = editorOf(pom);
        // Lookup by full GA must still find the plugin (predicatePluginGA handles missing <groupId>)
        Coordinates plugin = Coordinates.of("org.apache.maven.plugins", "maven-compiler-plugin", null);
        PomEditor.Plugins.PluginDependencies pd = editor.plugins().pluginDependencies(false, plugin);
        assertNotNull(pd, "Plugin without <groupId> should be found via predicatePluginGA()");
    }
}
