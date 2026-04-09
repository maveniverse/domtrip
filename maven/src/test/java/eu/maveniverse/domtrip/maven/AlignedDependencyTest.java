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
import org.junit.jupiter.api.Test;

/**
 * Tests for convention-aligned dependency addition and alignment in PomEditor.
 */
class AlignedDependencyTest {

    private PomEditor editorOf(String pomXml) {
        return new PomEditor(Document.of(pomXml));
    }

    // ========== CONVENTION DETECTION TESTS ==========

    @Test
    void detectsInlineVersionStyle() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                      <version>32.1.2-jre</version>
                    </dependency>
                    <dependency>
                      <groupId>org.slf4j</groupId>
                      <artifactId>slf4j-api</artifactId>
                      <version>2.0.9</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        assertEquals(AlignOptions.VersionStyle.INLINE, editor.dependencies().detectVersionStyle());
    }

    @Test
    void detectsManagedVersionStyle() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencyManagement>
                    <dependencies>
                      <dependency>
                        <groupId>com.google.guava</groupId>
                        <artifactId>guava</artifactId>
                        <version>32.1.2-jre</version>
                      </dependency>
                    </dependencies>
                  </dependencyManagement>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                    </dependency>
                    <dependency>
                      <groupId>org.slf4j</groupId>
                      <artifactId>slf4j-api</artifactId>
                    </dependency>
                    <dependency>
                      <groupId>org.junit.jupiter</groupId>
                      <artifactId>junit-jupiter</artifactId>
                      <version>5.9.2</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        // 2 without version > 1 with version → MANAGED
        assertEquals(AlignOptions.VersionStyle.MANAGED, editor.dependencies().detectVersionStyle());
    }

    @Test
    void detectsManagedStyleFromEmptyDependenciesWithDepMgmt() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencyManagement>
                    <dependencies>
                      <dependency>
                        <groupId>com.google.guava</groupId>
                        <artifactId>guava</artifactId>
                        <version>32.1.2-jre</version>
                      </dependency>
                    </dependencies>
                  </dependencyManagement>
                </project>
                """);

        assertEquals(AlignOptions.VersionStyle.MANAGED, editor.dependencies().detectVersionStyle());
    }

    @Test
    void detectsInlineStyleFromBarePom() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                </project>
                """);

        assertEquals(AlignOptions.VersionStyle.INLINE, editor.dependencies().detectVersionStyle());
    }

    @Test
    void detectsLiteralVersionSource() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                      <version>32.1.2-jre</version>
                    </dependency>
                    <dependency>
                      <groupId>org.slf4j</groupId>
                      <artifactId>slf4j-api</artifactId>
                      <version>2.0.9</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        assertEquals(AlignOptions.VersionSource.LITERAL, editor.dependencies().detectVersionSource());
    }

    @Test
    void detectsPropertyVersionSource() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <properties>
                    <guava.version>32.1.2-jre</guava.version>
                    <slf4j.version>2.0.9</slf4j.version>
                  </properties>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                      <version>${guava.version}</version>
                    </dependency>
                    <dependency>
                      <groupId>org.slf4j</groupId>
                      <artifactId>slf4j-api</artifactId>
                      <version>${slf4j.version}</version>
                    </dependency>
                    <dependency>
                      <groupId>org.example</groupId>
                      <artifactId>some-lib</artifactId>
                      <version>1.0.0</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        // 2 property > 1 literal → PROPERTY
        assertEquals(AlignOptions.VersionSource.PROPERTY, editor.dependencies().detectVersionSource());
    }

    @Test
    void detectsPropertyVersionSourceFromManagedDeps() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <properties>
                    <guava.version>32.1.2-jre</guava.version>
                    <slf4j.version>2.0.9</slf4j.version>
                  </properties>
                  <dependencyManagement>
                    <dependencies>
                      <dependency>
                        <groupId>com.google.guava</groupId>
                        <artifactId>guava</artifactId>
                        <version>${guava.version}</version>
                      </dependency>
                      <dependency>
                        <groupId>org.slf4j</groupId>
                        <artifactId>slf4j-api</artifactId>
                        <version>${slf4j.version}</version>
                      </dependency>
                    </dependencies>
                  </dependencyManagement>
                </project>
                """);

        assertEquals(AlignOptions.VersionSource.PROPERTY, editor.dependencies().detectVersionSource());
    }

    @Test
    void detectsDotSuffixConvention() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                      <version>${guava.version}</version>
                    </dependency>
                    <dependency>
                      <groupId>org.slf4j</groupId>
                      <artifactId>slf4j-api</artifactId>
                      <version>${slf4j.version}</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        assertEquals(
                AlignOptions.PropertyNamingConvention.DOT_SUFFIX,
                editor.dependencies().detectPropertyNamingConvention());
    }

    @Test
    void detectsDashSuffixConvention() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                      <version>${guava-version}</version>
                    </dependency>
                    <dependency>
                      <groupId>org.slf4j</groupId>
                      <artifactId>slf4j-api</artifactId>
                      <version>${slf4j-version}</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        assertEquals(
                AlignOptions.PropertyNamingConvention.DASH_SUFFIX,
                editor.dependencies().detectPropertyNamingConvention());
    }

    @Test
    void detectsCamelCaseConvention() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                      <version>${guavaVersion}</version>
                    </dependency>
                    <dependency>
                      <groupId>org.slf4j</groupId>
                      <artifactId>slf4j-api</artifactId>
                      <version>${slf4jVersion}</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        assertEquals(
                AlignOptions.PropertyNamingConvention.CAMEL_CASE,
                editor.dependencies().detectPropertyNamingConvention());
    }

    @Test
    void detectsDotPrefixConvention() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                      <version>${version.guava}</version>
                    </dependency>
                    <dependency>
                      <groupId>org.slf4j</groupId>
                      <artifactId>slf4j-api</artifactId>
                      <version>${version.slf4j}</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        assertEquals(
                AlignOptions.PropertyNamingConvention.DOT_PREFIX,
                editor.dependencies().detectPropertyNamingConvention());
    }

    @Test
    void detectsConventionFromMixedPatterns() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>a</groupId>
                      <artifactId>a1</artifactId>
                      <version>${a1.version}</version>
                    </dependency>
                    <dependency>
                      <groupId>a</groupId>
                      <artifactId>a2</artifactId>
                      <version>${a2.version}</version>
                    </dependency>
                    <dependency>
                      <groupId>a</groupId>
                      <artifactId>a3</artifactId>
                      <version>${a3.version}</version>
                    </dependency>
                    <dependency>
                      <groupId>b</groupId>
                      <artifactId>b1</artifactId>
                      <version>${b1-version}</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        // 3 DOT_SUFFIX > 1 DASH_SUFFIX → DOT_SUFFIX wins
        assertEquals(
                AlignOptions.PropertyNamingConvention.DOT_SUFFIX,
                editor.dependencies().detectPropertyNamingConvention());
    }

    @Test
    void detectConventionsReturnsCombined() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <properties>
                    <guava.version>32.1.2-jre</guava.version>
                  </properties>
                  <dependencyManagement>
                    <dependencies>
                      <dependency>
                        <groupId>com.google.guava</groupId>
                        <artifactId>guava</artifactId>
                        <version>${guava.version}</version>
                      </dependency>
                    </dependencies>
                  </dependencyManagement>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                    </dependency>
                  </dependencies>
                </project>
                """);

        AlignOptions detected = editor.dependencies().detectConventions();
        assertEquals(AlignOptions.VersionStyle.MANAGED, detected.versionStyle());
        assertEquals(AlignOptions.VersionSource.PROPERTY, detected.versionSource());
        assertEquals(AlignOptions.PropertyNamingConvention.DOT_SUFFIX, detected.namingConvention());
    }

    // ========== ADD ALIGNED TESTS ==========

    @Test
    void addAlignedInlineLiteral() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>org.slf4j</groupId>
                      <artifactId>slf4j-api</artifactId>
                      <version>2.0.9</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        Coordinates guava = Coordinates.of("com.google.guava", "guava", "32.1.2-jre");
        assertTrue(editor.dependencies().addAligned(guava));

        String xml = editor.toXml();
        assertTrue(xml.contains("<groupId>com.google.guava</groupId>"));
        assertTrue(xml.contains("<artifactId>guava</artifactId>"));
        assertTrue(xml.contains("<version>32.1.2-jre</version>"));
        // Should not create dependencyManagement
        assertFalse(xml.contains("<dependencyManagement>"));
        // Should not create properties
        assertFalse(xml.contains("<properties>"));
    }

    @Test
    void addAlignedInlineProperty() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <properties>
                    <slf4j.version>2.0.9</slf4j.version>
                  </properties>
                  <dependencies>
                    <dependency>
                      <groupId>org.slf4j</groupId>
                      <artifactId>slf4j-api</artifactId>
                      <version>${slf4j.version}</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        Coordinates guava = Coordinates.of("com.google.guava", "guava", "32.1.2-jre");
        assertTrue(editor.dependencies().addAligned(guava));

        String xml = editor.toXml();
        // Should create a property
        assertTrue(xml.contains("<guava.version>32.1.2-jre</guava.version>"));
        // Should use property reference inline
        assertTrue(xml.contains("<version>${guava.version}</version>"));
        // Should not create dependencyManagement
        assertFalse(xml.contains("<dependencyManagement>"));
    }

    @Test
    void addAlignedManagedLiteral() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencyManagement>
                    <dependencies>
                      <dependency>
                        <groupId>org.slf4j</groupId>
                        <artifactId>slf4j-api</artifactId>
                        <version>2.0.9</version>
                      </dependency>
                    </dependencies>
                  </dependencyManagement>
                  <dependencies>
                    <dependency>
                      <groupId>org.slf4j</groupId>
                      <artifactId>slf4j-api</artifactId>
                    </dependency>
                  </dependencies>
                </project>
                """);

        Coordinates guava = Coordinates.of("com.google.guava", "guava", "32.1.2-jre");
        assertTrue(editor.dependencies().addAligned(guava));

        String xml = editor.toXml();
        // Should add versioned entry to dependencyManagement
        int depMgmtStart = xml.indexOf("<dependencyManagement>");
        int depMgmtEnd = xml.indexOf("</dependencyManagement>");
        String depMgmtSection = xml.substring(depMgmtStart, depMgmtEnd);
        assertTrue(depMgmtSection.contains("<artifactId>guava</artifactId>"));
        assertTrue(depMgmtSection.contains("<version>32.1.2-jre</version>"));

        // Should add version-less dependency in dependencies section
        int depsStart = xml.indexOf("<dependencies>", depMgmtEnd);
        String depsSection = xml.substring(depsStart);
        assertTrue(depsSection.contains("<artifactId>guava</artifactId>"));
        // The dependency in dependencies section should NOT have a version
        // Count version elements in the guava dependency section
        int guavaIdx = depsSection.indexOf("<artifactId>guava</artifactId>");
        int nextDepIdx = depsSection.indexOf("<dependency>", guavaIdx);
        if (nextDepIdx == -1) nextDepIdx = depsSection.length();
        String guavaSection = depsSection.substring(guavaIdx, nextDepIdx);
        assertFalse(guavaSection.contains("<version>"));
    }

    @Test
    void addAlignedManagedProperty() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <properties>
                    <slf4j.version>2.0.9</slf4j.version>
                  </properties>
                  <dependencyManagement>
                    <dependencies>
                      <dependency>
                        <groupId>org.slf4j</groupId>
                        <artifactId>slf4j-api</artifactId>
                        <version>${slf4j.version}</version>
                      </dependency>
                    </dependencies>
                  </dependencyManagement>
                  <dependencies>
                    <dependency>
                      <groupId>org.slf4j</groupId>
                      <artifactId>slf4j-api</artifactId>
                    </dependency>
                  </dependencies>
                </project>
                """);

        Coordinates guava = Coordinates.of("com.google.guava", "guava", "32.1.2-jre");
        assertTrue(editor.dependencies().addAligned(guava));

        String xml = editor.toXml();
        // Should create property
        assertTrue(xml.contains("<guava.version>32.1.2-jre</guava.version>"));
        // Should have managed dep with property reference
        assertTrue(xml.contains("${guava.version}"));
        // Should have version-less regular dependency
        int lastDepsStart = xml.lastIndexOf("<dependencies>");
        String lastDeps = xml.substring(lastDepsStart);
        int guavaIdx = lastDeps.indexOf("<artifactId>guava</artifactId>");
        assertTrue(guavaIdx > 0);
    }

    @Test
    void addAlignedWithScope() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>org.slf4j</groupId>
                      <artifactId>slf4j-api</artifactId>
                      <version>2.0.9</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        Coordinates junit = Coordinates.of("org.junit.jupiter", "junit-jupiter", "5.10.0");
        AlignOptions options = AlignOptions.builder().scope("test").build();
        assertTrue(editor.dependencies().addAligned(junit, options));

        String xml = editor.toXml();
        assertTrue(xml.contains("<scope>test</scope>"));
    }

    @Test
    void addAlignedWithExplicitPropertyName() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                </project>
                """);

        Coordinates guava = Coordinates.of("com.google.guava", "guava", "32.1.2-jre");
        AlignOptions options = AlignOptions.builder()
                .versionStyle(AlignOptions.VersionStyle.INLINE)
                .versionSource(AlignOptions.VersionSource.PROPERTY)
                .propertyName("google.guava.version")
                .build();
        assertTrue(editor.dependencies().addAligned(guava, options));

        String xml = editor.toXml();
        assertTrue(xml.contains("<google.guava.version>32.1.2-jre</google.guava.version>"));
        assertTrue(xml.contains("<version>${google.guava.version}</version>"));
    }

    @Test
    void addAlignedWithExplicitOverrides() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>org.slf4j</groupId>
                      <artifactId>slf4j-api</artifactId>
                      <version>2.0.9</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        // POM has inline+literal style, but we force managed+property
        Coordinates guava = Coordinates.of("com.google.guava", "guava", "32.1.2-jre");
        AlignOptions options = AlignOptions.builder()
                .versionStyle(AlignOptions.VersionStyle.MANAGED)
                .versionSource(AlignOptions.VersionSource.PROPERTY)
                .namingConvention(AlignOptions.PropertyNamingConvention.DASH_SUFFIX)
                .build();
        assertTrue(editor.dependencies().addAligned(guava, options));

        String xml = editor.toXml();
        // Should use dash-suffix naming
        assertTrue(xml.contains("<guava-version>32.1.2-jre</guava-version>"));
        assertTrue(xml.contains("${guava-version}"));
        // Should use managed style
        assertTrue(xml.contains("<dependencyManagement>"));
    }

    @Test
    void addAlignedReturnsFalseIfExists() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                      <version>31.0-jre</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        Coordinates guava = Coordinates.of("com.google.guava", "guava", "32.1.2-jre");
        assertFalse(editor.dependencies().addAligned(guava));
    }

    @Test
    void addAlignedRequiresVersion() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                </project>
                """);

        Coordinates noVersion = Coordinates.of("com.google.guava", "guava", null);
        assertThrows(Exception.class, () -> editor.dependencies().addAligned(noVersion));
    }

    @Test
    void addAlignedWithDashSuffixConvention() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <properties>
                    <slf4j-version>2.0.9</slf4j-version>
                  </properties>
                  <dependencies>
                    <dependency>
                      <groupId>org.slf4j</groupId>
                      <artifactId>slf4j-api</artifactId>
                      <version>${slf4j-version}</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        Coordinates guava = Coordinates.of("com.google.guava", "guava", "32.1.2-jre");
        assertTrue(editor.dependencies().addAligned(guava));

        String xml = editor.toXml();
        // Should detect dash-suffix convention and use it
        assertTrue(xml.contains("<guava-version>32.1.2-jre</guava-version>"));
        assertTrue(xml.contains("<version>${guava-version}</version>"));
    }

    @Test
    void addAlignedWithCamelCaseConvention() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <properties>
                    <slf4jVersion>2.0.9</slf4jVersion>
                  </properties>
                  <dependencies>
                    <dependency>
                      <groupId>org.slf4j</groupId>
                      <artifactId>slf4j-api</artifactId>
                      <version>${slf4jVersion}</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        Coordinates junitJupiter = Coordinates.of("org.junit.jupiter", "junit-jupiter", "5.10.0");
        assertTrue(editor.dependencies().addAligned(junitJupiter));

        String xml = editor.toXml();
        // Should detect camelCase convention and use it
        assertTrue(xml.contains("<junitJupiterVersion>5.10.0</junitJupiterVersion>"));
        assertTrue(xml.contains("<version>${junitJupiterVersion}</version>"));
    }

    @Test
    void addAlignedWithDotPrefixConvention() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <properties>
                    <version.slf4j>2.0.9</version.slf4j>
                  </properties>
                  <dependencies>
                    <dependency>
                      <groupId>org.slf4j</groupId>
                      <artifactId>slf4j-api</artifactId>
                      <version>${version.slf4j}</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        Coordinates guava = Coordinates.of("com.google.guava", "guava", "32.1.2-jre");
        assertTrue(editor.dependencies().addAligned(guava));

        String xml = editor.toXml();
        // Should detect dot-prefix convention and use it
        assertTrue(xml.contains("<version.guava>32.1.2-jre</version.guava>"));
        assertTrue(xml.contains("<version>${version.guava}</version>"));
    }

    // ========== ALIGN DEPENDENCY TESTS ==========

    @Test
    void alignDependencyLiteralToManaged() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                      <version>32.1.2-jre</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        Coordinates guava = Coordinates.of("com.google.guava", "guava", null);
        AlignOptions options = AlignOptions.builder()
                .versionStyle(AlignOptions.VersionStyle.MANAGED)
                .versionSource(AlignOptions.VersionSource.LITERAL)
                .build();
        assertTrue(editor.dependencies().alignDependency(guava, options));

        String xml = editor.toXml();
        // Should have created dependencyManagement with version
        assertTrue(xml.contains("<dependencyManagement>"));
        int depMgmtStart = xml.indexOf("<dependencyManagement>");
        int depMgmtEnd = xml.indexOf("</dependencyManagement>");
        String depMgmt = xml.substring(depMgmtStart, depMgmtEnd);
        assertTrue(depMgmt.contains("<version>32.1.2-jre</version>"));

        // Regular dependency should be version-less
        int depsStart = xml.indexOf("<dependencies>", depMgmtEnd);
        String deps = xml.substring(depsStart);
        int guavaIdx = deps.indexOf("<artifactId>guava</artifactId>");
        int nextDep = deps.indexOf("</dependency>", guavaIdx);
        String guavaDep = deps.substring(guavaIdx, nextDep);
        assertFalse(guavaDep.contains("<version>"));
    }

    @Test
    void alignDependencyLiteralToProperty() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                      <version>32.1.2-jre</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        Coordinates guava = Coordinates.of("com.google.guava", "guava", null);
        AlignOptions options = AlignOptions.builder()
                .versionStyle(AlignOptions.VersionStyle.INLINE)
                .versionSource(AlignOptions.VersionSource.PROPERTY)
                .build();
        assertTrue(editor.dependencies().alignDependency(guava, options));

        String xml = editor.toXml();
        // Should have created property
        assertTrue(xml.contains("<guava.version>32.1.2-jre</guava.version>"));
        // Should have updated version reference
        assertTrue(xml.contains("<version>${guava.version}</version>"));
        // Should NOT create dependencyManagement
        assertFalse(xml.contains("<dependencyManagement>"));
    }

    @Test
    void alignDependencyToManagedAndProperty() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                      <version>32.1.2-jre</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        Coordinates guava = Coordinates.of("com.google.guava", "guava", null);
        AlignOptions options = AlignOptions.builder()
                .versionStyle(AlignOptions.VersionStyle.MANAGED)
                .versionSource(AlignOptions.VersionSource.PROPERTY)
                .build();
        assertTrue(editor.dependencies().alignDependency(guava, options));

        String xml = editor.toXml();
        // Should have created property
        assertTrue(xml.contains("<guava.version>32.1.2-jre</guava.version>"));
        // Managed dep should use property reference
        int depMgmtStart = xml.indexOf("<dependencyManagement>");
        int depMgmtEnd = xml.indexOf("</dependencyManagement>");
        String depMgmt = xml.substring(depMgmtStart, depMgmtEnd);
        assertTrue(depMgmt.contains("${guava.version}"));
        // Regular dependency should be version-less
        int depsStart = xml.indexOf("<dependencies>", depMgmtEnd);
        String deps = xml.substring(depsStart);
        int guavaIdx = deps.indexOf("<artifactId>guava</artifactId>");
        int nextDep = deps.indexOf("</dependency>", guavaIdx);
        String guavaDep = deps.substring(guavaIdx, nextDep);
        assertFalse(guavaDep.contains("<version>"));
    }

    @Test
    void alignDependencyAlreadyManagedReturnsNoChange() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                    </dependency>
                  </dependencies>
                </project>
                """);

        Coordinates guava = Coordinates.of("com.google.guava", "guava", null);
        AlignOptions options = AlignOptions.builder()
                .versionStyle(AlignOptions.VersionStyle.MANAGED)
                .build();
        assertFalse(editor.dependencies().alignDependency(guava, options));
    }

    @Test
    void alignDependencyNotFoundReturnsFalse() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>org.slf4j</groupId>
                      <artifactId>slf4j-api</artifactId>
                      <version>2.0.9</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        Coordinates guava = Coordinates.of("com.google.guava", "guava", null);
        assertFalse(editor.dependencies().alignDependency(guava));
    }

    @Test
    void alignDependencyWithExplicitPropertyName() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                      <version>32.1.2-jre</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        Coordinates guava = Coordinates.of("com.google.guava", "guava", null);
        AlignOptions options = AlignOptions.builder()
                .versionSource(AlignOptions.VersionSource.PROPERTY)
                .propertyName("google.guava.version")
                .build();
        assertTrue(editor.dependencies().alignDependency(guava, options));

        String xml = editor.toXml();
        assertTrue(xml.contains("<google.guava.version>32.1.2-jre</google.guava.version>"));
        assertTrue(xml.contains("<version>${google.guava.version}</version>"));
    }

    // ========== ALIGN ALL DEPENDENCIES TESTS ==========

    @Test
    void alignAllDependenciesToManagedProperty() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                      <version>32.1.2-jre</version>
                    </dependency>
                    <dependency>
                      <groupId>org.slf4j</groupId>
                      <artifactId>slf4j-api</artifactId>
                      <version>2.0.9</version>
                    </dependency>
                    <dependency>
                      <groupId>org.junit.jupiter</groupId>
                      <artifactId>junit-jupiter</artifactId>
                      <version>5.10.0</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        AlignOptions options = AlignOptions.builder()
                .versionStyle(AlignOptions.VersionStyle.MANAGED)
                .versionSource(AlignOptions.VersionSource.PROPERTY)
                .namingConvention(AlignOptions.PropertyNamingConvention.DOT_SUFFIX)
                .build();
        int count = editor.dependencies().alignAllDependencies(options);
        assertEquals(3, count);

        String xml = editor.toXml();
        // All three should have properties
        assertTrue(xml.contains("<guava.version>32.1.2-jre</guava.version>"));
        assertTrue(xml.contains("<slf4j-api.version>2.0.9</slf4j-api.version>"));
        assertTrue(xml.contains("<junit-jupiter.version>5.10.0</junit-jupiter.version>"));
        // All should be in dependencyManagement
        assertTrue(xml.contains("<dependencyManagement>"));
    }

    @Test
    void alignAllDependenciesSkipsAlreadyAligned() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                      <version>32.1.2-jre</version>
                    </dependency>
                    <dependency>
                      <groupId>org.slf4j</groupId>
                      <artifactId>slf4j-api</artifactId>
                    </dependency>
                  </dependencies>
                </project>
                """);

        AlignOptions options = AlignOptions.builder()
                .versionStyle(AlignOptions.VersionStyle.MANAGED)
                .versionSource(AlignOptions.VersionSource.LITERAL)
                .build();
        int count = editor.dependencies().alignAllDependencies(options);
        // Only guava should be aligned (slf4j is already version-less)
        assertEquals(1, count);
    }

    @Test
    void alignAllDependenciesNoDependenciesReturnsZero() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                </project>
                """);

        assertEquals(0, editor.dependencies().alignAllDependencies());
    }

    // ========== PROPERTY → LITERAL CONVERSION TESTS ==========

    @Test
    void alignDependencyPropertyToLiteral() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <properties>
                    <guava.version>32.1.2-jre</guava.version>
                  </properties>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                      <version>${guava.version}</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        Coordinates guava = Coordinates.of("com.google.guava", "guava", null);
        AlignOptions options = AlignOptions.builder()
                .versionSource(AlignOptions.VersionSource.LITERAL)
                .build();
        assertTrue(editor.dependencies().alignDependency(guava, options));

        String xml = editor.toXml();
        assertTrue(xml.contains("<version>32.1.2-jre</version>"));
        assertFalse(xml.contains("${guava.version}"));
    }

    @Test
    void alignDependencyPropertyToLiteralAlreadyLiteral() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                      <version>32.1.2-jre</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        Coordinates guava = Coordinates.of("com.google.guava", "guava", null);
        AlignOptions options = AlignOptions.builder()
                .versionSource(AlignOptions.VersionSource.LITERAL)
                .build();
        assertFalse(editor.dependencies().alignDependency(guava, options));
    }

    @Test
    void alignAllDependenciesPropertyToLiteral() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <properties>
                    <guava.version>32.1.2-jre</guava.version>
                    <slf4j-api.version>2.0.9</slf4j-api.version>
                    <junit-jupiter.version>5.10.0</junit-jupiter.version>
                  </properties>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                      <version>${guava.version}</version>
                    </dependency>
                    <dependency>
                      <groupId>org.slf4j</groupId>
                      <artifactId>slf4j-api</artifactId>
                      <version>${slf4j-api.version}</version>
                    </dependency>
                    <dependency>
                      <groupId>org.junit.jupiter</groupId>
                      <artifactId>junit-jupiter</artifactId>
                      <version>${junit-jupiter.version}</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        AlignOptions options = AlignOptions.builder()
                .versionStyle(AlignOptions.VersionStyle.INLINE)
                .versionSource(AlignOptions.VersionSource.LITERAL)
                .build();
        int count = editor.dependencies().alignAllDependencies(options);
        assertEquals(3, count);

        String xml = editor.toXml();
        assertTrue(xml.contains("<version>32.1.2-jre</version>"));
        assertTrue(xml.contains("<version>2.0.9</version>"));
        assertTrue(xml.contains("<version>5.10.0</version>"));
        assertFalse(xml.contains("${guava.version}"));
        assertFalse(xml.contains("${slf4j-api.version}"));
        assertFalse(xml.contains("${junit-jupiter.version}"));
    }

    @Test
    void alignAllDependenciesLiteralToProperty() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                      <version>32.1.2-jre</version>
                    </dependency>
                    <dependency>
                      <groupId>org.slf4j</groupId>
                      <artifactId>slf4j-api</artifactId>
                      <version>2.0.9</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        AlignOptions options = AlignOptions.builder()
                .versionStyle(AlignOptions.VersionStyle.INLINE)
                .versionSource(AlignOptions.VersionSource.PROPERTY)
                .namingConvention(AlignOptions.PropertyNamingConvention.DOT_SUFFIX)
                .build();
        int count = editor.dependencies().alignAllDependencies(options);
        assertEquals(2, count);

        String xml = editor.toXml();
        assertTrue(xml.contains("<guava.version>32.1.2-jre</guava.version>"));
        assertTrue(xml.contains("<version>${guava.version}</version>"));
        assertTrue(xml.contains("<slf4j-api.version>2.0.9</slf4j-api.version>"));
        assertTrue(xml.contains("<version>${slf4j-api.version}</version>"));
    }

    // ========== MANAGED ↔ INLINE CONVERSION TESTS ==========

    @Test
    void alignAllDependenciesManagedToInlineLiteral() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencyManagement>
                    <dependencies>
                      <dependency>
                        <groupId>com.google.guava</groupId>
                        <artifactId>guava</artifactId>
                        <version>32.1.2-jre</version>
                      </dependency>
                      <dependency>
                        <groupId>org.slf4j</groupId>
                        <artifactId>slf4j-api</artifactId>
                        <version>2.0.9</version>
                      </dependency>
                    </dependencies>
                  </dependencyManagement>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                    </dependency>
                    <dependency>
                      <groupId>org.slf4j</groupId>
                      <artifactId>slf4j-api</artifactId>
                    </dependency>
                  </dependencies>
                </project>
                """);

        AlignOptions options = AlignOptions.builder()
                .versionStyle(AlignOptions.VersionStyle.INLINE)
                .versionSource(AlignOptions.VersionSource.LITERAL)
                .build();
        int count = editor.dependencies().alignAllDependencies(options);
        assertEquals(2, count);

        String xml = editor.toXml();
        // Versions should now appear inline in the dependencies
        assertTrue(xml.contains("<version>32.1.2-jre</version>"));
        assertTrue(xml.contains("<version>2.0.9</version>"));
    }

    @Test
    void alignDependencyManagedToInlineProperty() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencyManagement>
                    <dependencies>
                      <dependency>
                        <groupId>com.google.guava</groupId>
                        <artifactId>guava</artifactId>
                        <version>32.1.2-jre</version>
                      </dependency>
                    </dependencies>
                  </dependencyManagement>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                    </dependency>
                  </dependencies>
                </project>
                """);

        Coordinates guava = Coordinates.of("com.google.guava", "guava", null);
        AlignOptions options = AlignOptions.builder()
                .versionStyle(AlignOptions.VersionStyle.INLINE)
                .versionSource(AlignOptions.VersionSource.PROPERTY)
                .propertyName("guava.version")
                .build();
        assertTrue(editor.dependencies().alignDependency(guava, options));

        String xml = editor.toXml();
        assertTrue(xml.contains("<guava.version>32.1.2-jre</guava.version>"));
        assertTrue(xml.contains("<version>${guava.version}</version>"));
    }

    // ========== GATC-AWARE MANAGED LOOKUP TESTS ==========

    @Test
    void alignDependencyManagedToInlineWithClassifier() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencyManagement>
                    <dependencies>
                      <dependency>
                        <groupId>io.netty</groupId>
                        <artifactId>netty-transport</artifactId>
                        <version>4.1.100</version>
                      </dependency>
                      <dependency>
                        <groupId>io.netty</groupId>
                        <artifactId>netty-transport</artifactId>
                        <version>4.1.100</version>
                        <classifier>linux-x86_64</classifier>
                      </dependency>
                    </dependencies>
                  </dependencyManagement>
                  <dependencies>
                    <dependency>
                      <groupId>io.netty</groupId>
                      <artifactId>netty-transport</artifactId>
                      <classifier>linux-x86_64</classifier>
                    </dependency>
                  </dependencies>
                </project>
                """);

        Coordinates coords = Coordinates.of("io.netty", "netty-transport", null, "linux-x86_64", "jar");
        AlignOptions options = AlignOptions.builder()
                .versionStyle(AlignOptions.VersionStyle.INLINE)
                .versionSource(AlignOptions.VersionSource.LITERAL)
                .build();
        assertTrue(editor.dependencies().alignDependency(coords, options));

        String xml = editor.toXml();
        assertTrue(xml.contains("<version>4.1.100</version>"));
    }

    @Test
    void alignDependencyManagedToInlineNoManagedEntry() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                    </dependency>
                  </dependencies>
                </project>
                """);

        Coordinates guava = Coordinates.of("com.google.guava", "guava", null);
        AlignOptions options = AlignOptions.builder()
                .versionStyle(AlignOptions.VersionStyle.INLINE)
                .versionSource(AlignOptions.VersionSource.LITERAL)
                .build();
        // No dependencyManagement exists, so nothing can be done
        assertFalse(editor.dependencies().alignDependency(guava, options));
    }

    @Test
    void alignDependencyManagedPropertyToInlineLiteral() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <properties>
                    <guava.version>32.1.2-jre</guava.version>
                  </properties>
                  <dependencyManagement>
                    <dependencies>
                      <dependency>
                        <groupId>com.google.guava</groupId>
                        <artifactId>guava</artifactId>
                        <version>${guava.version}</version>
                      </dependency>
                    </dependencies>
                  </dependencyManagement>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                    </dependency>
                  </dependencies>
                </project>
                """);

        Coordinates guava = Coordinates.of("com.google.guava", "guava", null);
        AlignOptions options = AlignOptions.builder()
                .versionStyle(AlignOptions.VersionStyle.INLINE)
                .versionSource(AlignOptions.VersionSource.LITERAL)
                .build();
        assertTrue(editor.dependencies().alignDependency(guava, options));

        String xml = editor.toXml();
        // The inline dependency should have the resolved literal version
        assertTrue(xml.contains("<version>32.1.2-jre</version>"));
        // The dependencyManagement entry still uses the property reference
        assertTrue(xml.contains("<version>${guava.version}</version>"));
    }

    @Test
    void alignVersionToLiteralUnresolvableProperty() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.google.guava</groupId>
                      <artifactId>guava</artifactId>
                      <version>${inherited.version}</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        Coordinates guava = Coordinates.of("com.google.guava", "guava", null);
        AlignOptions options = AlignOptions.builder()
                .versionSource(AlignOptions.VersionSource.LITERAL)
                .build();
        // Property not defined locally — can't resolve, no change
        assertFalse(editor.dependencies().alignDependency(guava, options));

        String xml = editor.toXml();
        assertTrue(xml.contains("${inherited.version}"));
    }

    // ========== PROPERTY NAME GENERATION TESTS ==========

    @Test
    void generatePropertyNameDotSuffix() {
        Coordinates coords = Coordinates.of("org.junit.jupiter", "junit-jupiter", "5.10.0");
        assertEquals(
                "junit-jupiter.version",
                AlignOptions.generatePropertyName(coords, AlignOptions.PropertyNamingConvention.DOT_SUFFIX));
    }

    @Test
    void generatePropertyNameDashSuffix() {
        Coordinates coords = Coordinates.of("org.junit.jupiter", "junit-jupiter", "5.10.0");
        assertEquals(
                "junit-jupiter-version",
                AlignOptions.generatePropertyName(coords, AlignOptions.PropertyNamingConvention.DASH_SUFFIX));
    }

    @Test
    void generatePropertyNameCamelCase() {
        Coordinates coords = Coordinates.of("org.junit.jupiter", "junit-jupiter", "5.10.0");
        assertEquals(
                "junitJupiterVersion",
                AlignOptions.generatePropertyName(coords, AlignOptions.PropertyNamingConvention.CAMEL_CASE));
    }

    @Test
    void generatePropertyNameDotPrefix() {
        Coordinates coords = Coordinates.of("org.junit.jupiter", "junit-jupiter", "5.10.0");
        assertEquals(
                "version.junit-jupiter",
                AlignOptions.generatePropertyName(coords, AlignOptions.PropertyNamingConvention.DOT_PREFIX));
    }

    @Test
    void toCamelCaseSimple() {
        assertEquals("guava", AlignOptions.toCamelCase("guava"));
    }

    @Test
    void toCamelCaseWithHyphens() {
        assertEquals("junitJupiter", AlignOptions.toCamelCase("junit-jupiter"));
    }

    @Test
    void toCamelCaseWithDots() {
        assertEquals("slf4jApi", AlignOptions.toCamelCase("slf4j.api"));
    }

    @Test
    void toCamelCaseWithMultipleSeparators() {
        assertEquals("myLongArtifactName", AlignOptions.toCamelCase("my-long-artifact-name"));
    }
}
