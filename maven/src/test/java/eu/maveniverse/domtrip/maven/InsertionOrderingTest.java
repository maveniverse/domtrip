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
import eu.maveniverse.domtrip.Element;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * Tests for auto-detected and explicit dependency insertion ordering in PomEditor.
 */
class InsertionOrderingTest {

    private PomEditor editorOf(String pomXml) {
        return new PomEditor(Document.of(pomXml));
    }

    /** Returns the groupId:artifactId of all direct <dependency> children in order. */
    private List<String> depGAs(PomEditor editor) {
        Element deps = editor.findChildElement(editor.root(), DEPENDENCIES);
        assertNotNull(deps, "<dependencies> must exist");
        return deps.childElements(DEPENDENCY)
                .map(dep -> dep.childTextOr("groupId", "") + ":" + dep.childTextOr("artifactId", ""))
                .collect(Collectors.toList());
    }

    /** Returns groupId:artifactId of all <dependencyManagement>/<dependency> children. */
    private List<String> managedDepGAs(PomEditor editor) {
        Element depMgmt = editor.findChildElement(editor.root(), DEPENDENCY_MANAGEMENT);
        assertNotNull(depMgmt, "<dependencyManagement> must exist");
        Element managedDeps = editor.findChildElement(depMgmt, DEPENDENCIES);
        assertNotNull(managedDeps);
        return managedDeps
                .childElements(DEPENDENCY)
                .map(dep -> dep.childTextOr("groupId", "") + ":" + dep.childTextOr("artifactId", ""))
                .collect(Collectors.toList());
    }

    // ========== DETECTION TESTS ==========

    @Test
    void detectAlpha3Deps() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.aaa</groupId>
                      <artifactId>aaa</artifactId>
                      <version>1.0</version>
                    </dependency>
                    <dependency>
                      <groupId>com.bbb</groupId>
                      <artifactId>bbb</artifactId>
                      <version>1.0</version>
                    </dependency>
                    <dependency>
                      <groupId>com.zzz</groupId>
                      <artifactId>zzz</artifactId>
                      <version>1.0</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        // All deps have no explicit scope → treated as compile → single scope rank (hasMultipleScopes=false).
        // SCOPE_THEN_ALPHA/SCOPE branches require hasMultipleScopes; only ALPHA is evaluated.
        // Alpha score is 100% → ALPHA is returned.
        Element deps = editor.findChildElement(editor.root(), DEPENDENCIES);
        assertEquals(AlignOptions.InsertionOrdering.ALPHA, editor.dependencies().detectInsertionOrdering(deps));
    }

    @Test
    void detectScopeOrdering() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.zzz</groupId>
                      <artifactId>zzz-compile</artifactId>
                      <version>1.0</version>
                    </dependency>
                    <dependency>
                      <groupId>com.aaa</groupId>
                      <artifactId>aaa-provided</artifactId>
                      <version>1.0</version>
                      <scope>provided</scope>
                    </dependency>
                    <dependency>
                      <groupId>com.bbb</groupId>
                      <artifactId>bbb-test</artifactId>
                      <version>1.0</version>
                      <scope>test</scope>
                    </dependency>
                  </dependencies>
                </project>
                """);

        Element deps = editor.findChildElement(editor.root(), DEPENDENCIES);
        // When all consecutive pairs have different scopes (compile→provided→test), scopeAlpha
        // degenerates to scope (rankA < rankB ⇒ scopeAlpha condition is trivially true).
        // Both SCOPE and SCOPE_THEN_ALPHA reach 100%; SCOPE_THEN_ALPHA is checked first, so it fires.
        AlignOptions.InsertionOrdering detected = editor.dependencies().detectInsertionOrdering(deps);
        assertEquals(AlignOptions.InsertionOrdering.SCOPE_THEN_ALPHA, detected);
    }

    @Test
    void detectAlphaWithBrokenScopeOrder() {
        // A list that is alphabetically ordered but whose scope order is inconsistent:
        // test-dep comes before compile-dep (scope order violated on one of two pairs).
        // scopeScore = 1/2 = 50% < 75%, so SCOPE/SCOPE_THEN_ALPHA are not triggered.
        // alphaScore = 2/2 = 100% ≥ 75% → ALPHA is returned.
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.aaa</groupId>
                      <artifactId>aaa</artifactId>
                      <version>1.0</version>
                      <scope>test</scope>
                    </dependency>
                    <dependency>
                      <groupId>com.bbb</groupId>
                      <artifactId>bbb</artifactId>
                      <version>1.0</version>
                    </dependency>
                    <dependency>
                      <groupId>com.zzz</groupId>
                      <artifactId>zzz</artifactId>
                      <version>1.0</version>
                      <scope>test</scope>
                    </dependency>
                  </dependencies>
                </project>
                """);

        Element deps = editor.findChildElement(editor.root(), DEPENDENCIES);
        assertEquals(AlignOptions.InsertionOrdering.ALPHA, editor.dependencies().detectInsertionOrdering(deps));
    }

    @Test
    void detectScopeThenAlpha() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.aaa</groupId>
                      <artifactId>aaa</artifactId>
                      <version>1.0</version>
                    </dependency>
                    <dependency>
                      <groupId>com.bbb</groupId>
                      <artifactId>bbb</artifactId>
                      <version>1.0</version>
                    </dependency>
                    <dependency>
                      <groupId>com.aaa</groupId>
                      <artifactId>aaa-test</artifactId>
                      <version>1.0</version>
                      <scope>test</scope>
                    </dependency>
                    <dependency>
                      <groupId>com.bbb</groupId>
                      <artifactId>bbb-test</artifactId>
                      <version>1.0</version>
                      <scope>test</scope>
                    </dependency>
                  </dependencies>
                </project>
                """);

        Element deps = editor.findChildElement(editor.root(), DEPENDENCIES);
        assertEquals(
                AlignOptions.InsertionOrdering.SCOPE_THEN_ALPHA,
                editor.dependencies().detectInsertionOrdering(deps));
    }

    @Test
    void detectNoneWhenFewer3Deps() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.aaa</groupId>
                      <artifactId>aaa</artifactId>
                      <version>1.0</version>
                    </dependency>
                    <dependency>
                      <groupId>com.bbb</groupId>
                      <artifactId>bbb</artifactId>
                      <version>1.0</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        Element deps = editor.findChildElement(editor.root(), DEPENDENCIES);
        assertEquals(AlignOptions.InsertionOrdering.NONE, editor.dependencies().detectInsertionOrdering(deps));
    }

    @Test
    void detectNoneWhenNoDeps() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                </project>
                """);

        assertEquals(AlignOptions.InsertionOrdering.NONE, editor.dependencies().detectInsertionOrdering(null));
    }

    @Test
    void detectNoneWhenInconsistent() {
        // Scope order violated on every pair: test→provided→compile (reverse scope order).
        // Alpha also violated. Both alpha and scope scores will be 0/2 → NONE.
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.zzz</groupId>
                      <artifactId>zzz</artifactId>
                      <version>1.0</version>
                      <scope>test</scope>
                    </dependency>
                    <dependency>
                      <groupId>com.bbb</groupId>
                      <artifactId>bbb</artifactId>
                      <version>1.0</version>
                      <scope>provided</scope>
                    </dependency>
                    <dependency>
                      <groupId>com.aaa</groupId>
                      <artifactId>aaa</artifactId>
                      <version>1.0</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        Element deps = editor.findChildElement(editor.root(), DEPENDENCIES);
        assertEquals(AlignOptions.InsertionOrdering.NONE, editor.dependencies().detectInsertionOrdering(deps));
    }

    // ========== INSERTION BEHAVIOUR TESTS ==========

    @Test
    void addAlignedAlphaOrderInsertedAtCorrectRank() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.aaa</groupId>
                      <artifactId>aaa</artifactId>
                      <version>1.0</version>
                    </dependency>
                    <dependency>
                      <groupId>com.bbb</groupId>
                      <artifactId>bbb</artifactId>
                      <version>1.0</version>
                    </dependency>
                    <dependency>
                      <groupId>com.zzz</groupId>
                      <artifactId>zzz</artifactId>
                      <version>1.0</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        // Insert mmm — should land between bbb and zzz
        assertTrue(editor.dependencies().addAligned(Coordinates.of("com.mmm", "mmm", "1.0")));

        List<String> gas = depGAs(editor);
        assertEquals(4, gas.size());
        assertEquals("com.aaa:aaa", gas.get(0));
        assertEquals("com.bbb:bbb", gas.get(1));
        assertEquals("com.mmm:mmm", gas.get(2));
        assertEquals("com.zzz:zzz", gas.get(3));
    }

    @Test
    void addAlignedAlphaOrderInsertedAtBeginning() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.bbb</groupId>
                      <artifactId>bbb</artifactId>
                      <version>1.0</version>
                    </dependency>
                    <dependency>
                      <groupId>com.mmm</groupId>
                      <artifactId>mmm</artifactId>
                      <version>1.0</version>
                    </dependency>
                    <dependency>
                      <groupId>com.zzz</groupId>
                      <artifactId>zzz</artifactId>
                      <version>1.0</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        // Insert aaa — should be first
        assertTrue(editor.dependencies().addAligned(Coordinates.of("com.aaa", "aaa", "1.0")));

        List<String> gas = depGAs(editor);
        assertEquals(4, gas.size());
        assertEquals("com.aaa:aaa", gas.get(0));
    }

    @Test
    void addAlignedAlphaOrderInsertedAtEnd() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.aaa</groupId>
                      <artifactId>aaa</artifactId>
                      <version>1.0</version>
                    </dependency>
                    <dependency>
                      <groupId>com.bbb</groupId>
                      <artifactId>bbb</artifactId>
                      <version>1.0</version>
                    </dependency>
                    <dependency>
                      <groupId>com.mmm</groupId>
                      <artifactId>mmm</artifactId>
                      <version>1.0</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        // Insert zzz — should be last
        assertTrue(editor.dependencies().addAligned(Coordinates.of("com.zzz", "zzz", "1.0")));

        List<String> gas = depGAs(editor);
        assertEquals(4, gas.size());
        assertEquals("com.zzz:zzz", gas.get(3));
    }

    @Test
    void addAlignedScopeOrderInsertedInCorrectScopeGroup() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.aaa</groupId>
                      <artifactId>compile1</artifactId>
                      <version>1.0</version>
                    </dependency>
                    <dependency>
                      <groupId>com.aaa</groupId>
                      <artifactId>compile2</artifactId>
                      <version>1.0</version>
                    </dependency>
                    <dependency>
                      <groupId>com.aaa</groupId>
                      <artifactId>test1</artifactId>
                      <version>1.0</version>
                      <scope>test</scope>
                    </dependency>
                  </dependencies>
                </project>
                """);

        // Add a 'provided' dep — should be inserted between compile and test groups
        AlignOptions opts = AlignOptions.builder().scope("provided").build();
        assertTrue(editor.dependencies().addAligned(Coordinates.of("com.aaa", "provided1", "1.0"), opts));

        List<String> gas = depGAs(editor);
        assertEquals(4, gas.size());
        // compile1, compile2 come first; provided1 before test1
        assertTrue(gas.indexOf("com.aaa:provided1") > gas.indexOf("com.aaa:compile2"));
        assertTrue(gas.indexOf("com.aaa:provided1") < gas.indexOf("com.aaa:test1"));
    }

    @Test
    void addAlignedScopeThenAlphaCorrectGroupAndRank() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.aaa</groupId>
                      <artifactId>aaa</artifactId>
                      <version>1.0</version>
                    </dependency>
                    <dependency>
                      <groupId>com.zzz</groupId>
                      <artifactId>zzz</artifactId>
                      <version>1.0</version>
                    </dependency>
                    <dependency>
                      <groupId>com.aaa</groupId>
                      <artifactId>aaa-test</artifactId>
                      <version>1.0</version>
                      <scope>test</scope>
                    </dependency>
                    <dependency>
                      <groupId>com.zzz</groupId>
                      <artifactId>zzz-test</artifactId>
                      <version>1.0</version>
                      <scope>test</scope>
                    </dependency>
                  </dependencies>
                </project>
                """);

        // Add mmm (compile) — should land between aaa and zzz in compile group
        assertTrue(editor.dependencies().addAligned(Coordinates.of("com.mmm", "mmm", "1.0")));

        List<String> gas = depGAs(editor);
        assertEquals(5, gas.size());
        assertEquals("com.aaa:aaa", gas.get(0));
        assertEquals("com.mmm:mmm", gas.get(1));
        assertEquals("com.zzz:zzz", gas.get(2));
        // test group untouched
        assertEquals("com.aaa:aaa-test", gas.get(3));
        assertEquals("com.zzz:zzz-test", gas.get(4));
    }

    @Test
    void addAlignedWith2DepsAppendsAtEnd() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.aaa</groupId>
                      <artifactId>aaa</artifactId>
                      <version>1.0</version>
                    </dependency>
                    <dependency>
                      <groupId>com.zzz</groupId>
                      <artifactId>zzz</artifactId>
                      <version>1.0</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        // Only 2 deps → NONE → appended at end, even though "bbb" is lexicographically between
        assertTrue(editor.dependencies().addAligned(Coordinates.of("com.bbb", "bbb", "1.0")));

        List<String> gas = depGAs(editor);
        assertEquals(3, gas.size());
        assertEquals("com.bbb:bbb", gas.get(2)); // appended, not inserted at alpha position
    }

    @Test
    void addAlignedWith0DepsAppendsAtEnd() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                </project>
                """);

        assertTrue(editor.dependencies().addAligned(Coordinates.of("com.bbb", "bbb", "1.0")));

        List<String> gas = depGAs(editor);
        assertEquals(1, gas.size());
        assertEquals("com.bbb:bbb", gas.get(0));
    }

    @Test
    void addAlignedExplicitNoneAlwaysAppends() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.aaa</groupId>
                      <artifactId>aaa</artifactId>
                      <version>1.0</version>
                    </dependency>
                    <dependency>
                      <groupId>com.bbb</groupId>
                      <artifactId>bbb</artifactId>
                      <version>1.0</version>
                    </dependency>
                    <dependency>
                      <groupId>com.zzz</groupId>
                      <artifactId>zzz</artifactId>
                      <version>1.0</version>
                    </dependency>
                  </dependencies>
                </project>
                """);

        // Alpha would be detected, but we explicitly force NONE
        AlignOptions opts = AlignOptions.builder()
                .insertionOrdering(AlignOptions.InsertionOrdering.NONE)
                .build();
        assertTrue(editor.dependencies().addAligned(Coordinates.of("com.mmm", "mmm", "1.0"), opts));

        List<String> gas = depGAs(editor);
        assertEquals(4, gas.size());
        assertEquals("com.mmm:mmm", gas.get(3)); // appended, not alpha-inserted
    }

    @Test
    void addAlignedManagedDepsAlphaOrdering() {
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
                        <groupId>com.aaa</groupId>
                        <artifactId>aaa</artifactId>
                        <version>1.0</version>
                      </dependency>
                      <dependency>
                        <groupId>com.bbb</groupId>
                        <artifactId>bbb</artifactId>
                        <version>1.0</version>
                      </dependency>
                      <dependency>
                        <groupId>com.zzz</groupId>
                        <artifactId>zzz</artifactId>
                        <version>1.0</version>
                      </dependency>
                    </dependencies>
                  </dependencyManagement>
                  <dependencies>
                    <dependency>
                      <groupId>com.aaa</groupId>
                      <artifactId>aaa</artifactId>
                    </dependency>
                    <dependency>
                      <groupId>com.bbb</groupId>
                      <artifactId>bbb</artifactId>
                    </dependency>
                    <dependency>
                      <groupId>com.zzz</groupId>
                      <artifactId>zzz</artifactId>
                    </dependency>
                  </dependencies>
                </project>
                """);

        // MANAGED style: both the dependencyManagement entry and the dep entry should respect alpha order
        AlignOptions opts = AlignOptions.builder()
                .versionStyle(AlignOptions.VersionStyle.MANAGED)
                .versionSource(AlignOptions.VersionSource.LITERAL)
                .build();
        assertTrue(editor.dependencies().addAligned(Coordinates.of("com.mmm", "mmm", "1.0"), opts));

        // Check managed entries
        List<String> managedGAs = managedDepGAs(editor);
        assertEquals(4, managedGAs.size());
        assertEquals("com.aaa:aaa", managedGAs.get(0));
        assertEquals("com.bbb:bbb", managedGAs.get(1));
        assertEquals("com.mmm:mmm", managedGAs.get(2));
        assertEquals("com.zzz:zzz", managedGAs.get(3));

        // Check direct deps also in alpha order
        List<String> gas = depGAs(editor);
        assertEquals(4, gas.size());
        assertEquals("com.mmm:mmm", gas.get(2));
    }

    @Test
    void nullOrEmptyScopeTreatedAsCompile() {
        PomEditor editor = editorOf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example</groupId>
                  <artifactId>test</artifactId>
                  <version>1.0.0</version>
                  <dependencies>
                    <dependency>
                      <groupId>com.aaa</groupId>
                      <artifactId>no-scope</artifactId>
                      <version>1.0</version>
                    </dependency>
                    <dependency>
                      <groupId>com.bbb</groupId>
                      <artifactId>no-scope</artifactId>
                      <version>1.0</version>
                    </dependency>
                    <dependency>
                      <groupId>com.aaa</groupId>
                      <artifactId>test-dep</artifactId>
                      <version>1.0</version>
                      <scope>test</scope>
                    </dependency>
                  </dependencies>
                </project>
                """);

        // Deps with no scope + test scope → SCOPE_THEN_ALPHA expected
        Element deps = editor.findChildElement(editor.root(), DEPENDENCIES);
        AlignOptions.InsertionOrdering detected = editor.dependencies().detectInsertionOrdering(deps);
        assertEquals(AlignOptions.InsertionOrdering.SCOPE_THEN_ALPHA, detected);

        // Add a compile dep (no scope) — should land in compile group before test
        assertTrue(editor.dependencies().addAligned(Coordinates.of("com.aaa", "aaa-mid", "1.0")));

        List<String> gas = depGAs(editor);
        // aaa-mid is compile scope, should appear before test group
        assertTrue(gas.indexOf("com.aaa:aaa-mid") < gas.indexOf("com.aaa:test-dep"));
    }

    @Test
    void managedOnlyPomAutoDetectsAlphaFromManagedContainer() {
        // A POM with no <dependencies>, only <dependencyManagement> with 3 alpha-ordered entries.
        // detectConventions() returns null insertionOrdering (NONE from direct-deps → stored as null).
        // addAligned with VersionStyle.MANAGED should auto-detect ALPHA from the managed container.
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
                        <groupId>com.aaa</groupId>
                        <artifactId>aaa</artifactId>
                        <version>1.0</version>
                      </dependency>
                      <dependency>
                        <groupId>com.bbb</groupId>
                        <artifactId>bbb</artifactId>
                        <version>1.0</version>
                      </dependency>
                      <dependency>
                        <groupId>com.zzz</groupId>
                        <artifactId>zzz</artifactId>
                        <version>1.0</version>
                      </dependency>
                    </dependencies>
                  </dependencyManagement>
                </project>
                """);

        // detectConventions returns null for insertionOrdering (no direct-deps → below threshold)
        AlignOptions conventions = editor.dependencies().detectConventions();
        assertNull(conventions.insertionOrdering());

        // addAligned with MANAGED style: com.mmm:mmm should be inserted alpha-ordered in managed container
        AlignOptions opts = AlignOptions.builder()
                .versionStyle(AlignOptions.VersionStyle.MANAGED)
                .versionSource(AlignOptions.VersionSource.LITERAL)
                .build();
        assertTrue(editor.dependencies().addAligned(Coordinates.of("com.mmm", "mmm", "1.0"), opts));

        // Managed container should now be aaa, bbb, mmm, zzz (alpha-ordered)
        List<String> managed = managedDepGAs(editor);
        assertEquals(java.util.Arrays.asList("com.aaa:aaa", "com.bbb:bbb", "com.mmm:mmm", "com.zzz:zzz"), managed);
    }
}
