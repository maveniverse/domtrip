/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.maven;

import static org.junit.jupiter.api.Assertions.*;

import eu.maveniverse.domtrip.DomTripException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CoordinatesExtendedTest {

    // ===== Constructor Validation =====

    @Test
    void testNullArtifactIdThrows() {
        assertThrows(NullPointerException.class, () -> Coordinates.of(null, null, null));
    }

    @Test
    void testEmptyArtifactIdThrows() {
        assertThrows(DomTripException.class, () -> Coordinates.of("g", "", "1.0"));
    }

    @Test
    void testBlankArtifactIdThrows() {
        assertThrows(DomTripException.class, () -> Coordinates.of("g", "  ", "1.0"));
    }

    @Test
    void testEmptyGroupIdThrows() {
        assertThrows(DomTripException.class, () -> Coordinates.of("", "a", "1.0"));
    }

    @Test
    void testEmptyVersionThrows() {
        assertThrows(DomTripException.class, () -> Coordinates.of("g", "a", ""));
    }

    @Test
    void testNullGroupIdAllowed() {
        Coordinates c = Coordinates.of(null, "artifact", "1.0");
        assertNull(c.groupId());
    }

    @Test
    void testNullVersionAllowed() {
        Coordinates c = Coordinates.of("g", "artifact", null);
        assertNull(c.version());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void testNullEmptyOrBlankTypeDefaultsToJar(String type) {
        Coordinates c = new Coordinates("g", "a", "1.0", null, type);
        assertEquals("jar", c.type());
    }

    @Test
    void testEmptyClassifierNormalized() {
        Coordinates c = new Coordinates("g", "a", "1.0", "", "jar");
        assertNull(c.classifier());
    }

    @Test
    void testBlankClassifierNormalized() {
        Coordinates c = new Coordinates("g", "a", "1.0", "  ", "jar");
        assertNull(c.classifier());
    }

    // ===== Getters =====

    @Test
    void testGetters() {
        Coordinates c = Coordinates.of("org.example", "my-lib", "1.0.0", "sources", "jar");
        assertEquals("org.example", c.groupId());
        assertEquals("my-lib", c.artifactId());
        assertEquals("1.0.0", c.version());
        assertEquals("sources", c.classifier());
        assertEquals("jar", c.type());
    }

    // ===== String Representations =====

    @Test
    void testToGA() {
        Coordinates c = Coordinates.of("org.example", "my-lib", "1.0.0");
        assertEquals("org.example:my-lib", c.toGA());
    }

    @Test
    void testToGAV() {
        Coordinates c = Coordinates.of("org.example", "my-lib", "1.0.0");
        assertEquals("org.example:my-lib:1.0.0", c.toGAV());
    }

    @Test
    void testToGATCWithoutClassifier() {
        Coordinates c = Coordinates.of("org.example", "my-lib", "1.0.0");
        assertEquals("org.example:my-lib:jar", c.toGATC());
    }

    @Test
    void testToGATCWithClassifier() {
        Coordinates c = Coordinates.of("org.example", "my-lib", "1.0.0", "sources", "jar");
        assertEquals("org.example:my-lib:jar:sources", c.toGATC());
    }

    @Test
    void testToFullStringWithoutClassifier() {
        Coordinates c = Coordinates.of("org.example", "my-lib", "1.0.0");
        assertEquals("org.example:my-lib:jar:1.0.0", c.toFullString());
    }

    @Test
    void testToFullStringWithClassifier() {
        Coordinates c = Coordinates.of("org.example", "my-lib", "1.0.0", "sources", "jar");
        assertEquals("org.example:my-lib:jar:sources:1.0.0", c.toFullString());
    }

    @Test
    void testToFullStringWithPomType() {
        Coordinates c = Coordinates.of("org.example", "my-lib", "1.0.0", null, "pom");
        assertEquals("org.example:my-lib:pom:1.0.0", c.toFullString());
    }

    // ===== withVersion / withType =====

    @Test
    void testWithVersion() {
        Coordinates original = Coordinates.of("g", "a", "1.0.0");
        Coordinates updated = original.withVersion("2.0.0");
        assertEquals("2.0.0", updated.version());
        assertEquals("g", updated.groupId());
        assertEquals("a", updated.artifactId());
        assertEquals("jar", updated.type());
        assertNull(updated.classifier());
    }

    @Test
    void testWithVersionPreservesClassifier() {
        Coordinates original = Coordinates.of("g", "a", "1.0.0", "sources", "jar");
        Coordinates updated = original.withVersion("2.0.0");
        assertEquals("2.0.0", updated.version());
        assertEquals("sources", updated.classifier());
    }

    @Test
    void testWithType() {
        Coordinates original = Coordinates.of("g", "a", "1.0.0");
        Coordinates updated = original.withType("pom");
        assertEquals("pom", updated.type());
        assertEquals("g", updated.groupId());
        assertEquals("a", updated.artifactId());
        assertEquals("1.0.0", updated.version());
    }

    @Test
    void testWithTypePreservesClassifier() {
        Coordinates original = Coordinates.of("g", "a", "1.0.0", "sources", "jar");
        Coordinates updated = original.withType("war");
        assertEquals("war", updated.type());
        assertEquals("sources", updated.classifier());
    }

    // ===== Factory Methods =====

    @Test
    void testOfThreeArgs() {
        Coordinates c = Coordinates.of("g", "a", "1.0");
        assertEquals("g", c.groupId());
        assertEquals("a", c.artifactId());
        assertEquals("1.0", c.version());
        assertNull(c.classifier());
        assertEquals("jar", c.type());
    }

    @Test
    void testOfFiveArgs() {
        Coordinates c = Coordinates.of("g", "a", "1.0", "tests", "test-jar");
        assertEquals("g", c.groupId());
        assertEquals("a", c.artifactId());
        assertEquals("1.0", c.version());
        assertEquals("tests", c.classifier());
        assertEquals("test-jar", c.type());
    }
}
