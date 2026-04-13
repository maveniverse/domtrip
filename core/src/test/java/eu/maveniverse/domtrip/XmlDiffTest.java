package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * Tests for XML-aware structural diff.
 */
class XmlDiffTest {

    @Test
    void identicalDocumentsProduceEmptyDiff() {
        String xml = "<project><version>1.0</version></project>";
        Document before = Document.of(xml);
        Document after = Document.of(xml);

        DiffResult result = XmlDiff.diff(before, after);

        assertFalse(result.hasChanges());
        assertTrue(result.changes().isEmpty());
        assertEquals("No changes", result.toString());
    }

    @Test
    void detectsTextChange() {
        Document before = Document.of("<project><version>1.0</version></project>");
        Document after = Document.of("<project><version>1.1</version></project>");

        DiffResult result = XmlDiff.diff(before, after);

        assertTrue(result.hasChanges());
        assertTrue(result.hasSemanticChanges());
        assertChange(result, ChangeType.TEXT_CHANGED, "/project/version", "1.0", "1.1");
    }

    @Test
    void detectsAttributeChange() {
        Document before = Document.of("<dep scope=\"compile\">junit</dep>");
        Document after = Document.of("<dep scope=\"test\">junit</dep>");

        DiffResult result = XmlDiff.diff(before, after);

        assertChange(result, ChangeType.ATTRIBUTE_CHANGED, "/dep/@scope", "compile", "test");
    }

    @Test
    void detectsAttributeAdded() {
        Document before = Document.of("<dep>junit</dep>");
        Document after = Document.of("<dep scope=\"test\">junit</dep>");

        DiffResult result = XmlDiff.diff(before, after);

        assertChange(result, ChangeType.ATTRIBUTE_ADDED, "/dep/@scope", null, "test");
    }

    @Test
    void detectsAttributeRemoved() {
        Document before = Document.of("<dep scope=\"test\">junit</dep>");
        Document after = Document.of("<dep>junit</dep>");

        DiffResult result = XmlDiff.diff(before, after);

        assertChange(result, ChangeType.ATTRIBUTE_REMOVED, "/dep/@scope", "test", null);
    }

    @Test
    void detectsElementAdded() {
        Document before = Document.of("<project><version>1.0</version></project>");
        Document after = Document.of("<project><version>1.0</version><name>test</name></project>");

        DiffResult result = XmlDiff.diff(before, after);

        assertChange(result, ChangeType.ELEMENT_ADDED, "/project/name");
    }

    @Test
    void detectsElementRemoved() {
        Document before = Document.of("<project><version>1.0</version><name>test</name></project>");
        Document after = Document.of("<project><version>1.0</version></project>");

        DiffResult result = XmlDiff.diff(before, after);

        assertChange(result, ChangeType.ELEMENT_REMOVED, "/project/name");
    }

    @Test
    void detectsQuoteStyleChange() {
        Document before = Document.of("<dep scope='test'>junit</dep>");
        Document after = Document.of("<dep scope=\"test\">junit</dep>");

        DiffResult result = XmlDiff.diff(before, after);

        List<XmlChange> formatting = result.formattingChanges();
        boolean hasQuoteChange = formatting.stream()
                .anyMatch(c ->
                        c.type() == ChangeType.QUOTE_STYLE_CHANGED && c.path().equals("/dep/@scope"));
        assertTrue(hasQuoteChange, "Expected QUOTE_STYLE_CHANGED for /dep/@scope");
        assertFalse(result.hasSemanticChanges());
    }

    @Test
    void detectsEmptyElementStyleChange() {
        Document before = Document.of("<project><br/></project>");
        Document after = Document.of("<project><br></br></project>");

        DiffResult result = XmlDiff.diff(before, after);

        assertChange(result, ChangeType.EMPTY_ELEMENT_STYLE_CHANGED, "/project/br");
        assertFalse(result.hasSemanticChanges());
    }

    @Test
    void detectsCommentAdded() {
        Document before = Document.of("<project><version>1.0</version></project>");
        Document after = Document.of("<project><!-- note --><version>1.0</version></project>");

        DiffResult result = XmlDiff.diff(before, after);

        assertChange(result, ChangeType.COMMENT_ADDED, "/project/comment()");
    }

    @Test
    void detectsCommentRemoved() {
        Document before = Document.of("<project><!-- note --><version>1.0</version></project>");
        Document after = Document.of("<project><version>1.0</version></project>");

        DiffResult result = XmlDiff.diff(before, after);

        assertChange(result, ChangeType.COMMENT_REMOVED, "/project/comment()");
    }

    @Test
    void detectsCommentChanged() {
        Document before = Document.of("<project><!-- old --><version>1.0</version></project>");
        Document after = Document.of("<project><!-- new --><version>1.0</version></project>");

        DiffResult result = XmlDiff.diff(before, after);

        assertChange(result, ChangeType.COMMENT_CHANGED, "/project/comment()");
    }

    @Test
    void detectsNestedChanges() {
        Document before = Document.of(
                "<project><dependencies><dependency><version>1.0</version></dependency></dependencies></project>");
        Document after = Document.of(
                "<project><dependencies><dependency><version>2.0</version></dependency></dependencies></project>");

        DiffResult result = XmlDiff.diff(before, after);

        assertChange(result, ChangeType.TEXT_CHANGED, "/project/dependencies/dependency/version", "1.0", "2.0");
    }

    @Test
    void indexesMultipleSameNameSiblings() {
        Document before = Document.of("<deps><dep>A</dep><dep>B</dep></deps>");
        Document after = Document.of("<deps><dep>A</dep><dep>C</dep></deps>");

        DiffResult result = XmlDiff.diff(before, after);

        assertChange(result, ChangeType.TEXT_CHANGED, "/deps/dep[2]", "B", "C");
    }

    @Test
    void matchByChildElementKeys() {
        Document before = Document.of("<deps>"
                + "<dep><g>junit</g><a>junit</a><v>4</v></dep>"
                + "<dep><g>log4j</g><a>log4j</a><v>1</v></dep>"
                + "</deps>");
        Document after = Document.of("<deps>"
                + "<dep><g>log4j</g><a>log4j</a><v>2</v></dep>"
                + "<dep><g>junit</g><a>junit</a><v>5</v></dep>"
                + "</deps>");

        DiffConfig config = DiffConfig.builder().matchBy("dep", "g", "a").build();
        DiffResult result = XmlDiff.diff(before, after, config);

        // Should match by keys, not by position
        List<XmlChange> textChanges = result.semanticChanges().stream()
                .filter(c -> c.type() == ChangeType.TEXT_CHANGED)
                .collect(Collectors.toList());
        assertTrue(
                textChanges.stream().anyMatch(c -> "4".equals(c.beforeValue()) && "5".equals(c.afterValue())),
                "junit version should change from 4 to 5");
        assertTrue(
                textChanges.stream().anyMatch(c -> "1".equals(c.beforeValue()) && "2".equals(c.afterValue())),
                "log4j version should change from 1 to 2");
    }

    @Test
    void detectsElementMoved() {
        Document before = Document.of(
                "<deps>" + "<dep><g>junit</g><a>junit</a></dep>" + "<dep><g>log4j</g><a>log4j</a></dep>" + "</deps>");
        Document after = Document.of(
                "<deps>" + "<dep><g>log4j</g><a>log4j</a></dep>" + "<dep><g>junit</g><a>junit</a></dep>" + "</deps>");

        DiffConfig config = DiffConfig.builder().matchBy("dep", "g", "a").build();
        DiffResult result = XmlDiff.diff(before, after, config);

        assertTrue(
                result.changes().stream().anyMatch(c -> c.type() == ChangeType.ELEMENT_MOVED),
                "Expected ELEMENT_MOVED for reordered elements");
    }

    @Test
    void changesUnderFiltersByPath() {
        Document before = Document.of("<project><version>1.0</version><name>old</name></project>");
        Document after = Document.of("<project><version>2.0</version><name>new</name></project>");

        DiffResult result = XmlDiff.diff(before, after);

        List<XmlChange> versionChanges = result.changesUnder("/project/version");
        assertEquals(1, versionChanges.size());
        assertEquals(ChangeType.TEXT_CHANGED, versionChanges.get(0).type());
    }

    @Test
    void semanticAndFormattingInSameDocument() {
        // scope value changes (semantic) + type quote-only changes (formatting)
        Document before = Document.of("<dep scope='compile' type='jar'>junit</dep>");
        Document after = Document.of("<dep scope=\"test\" type=\"jar\">junit</dep>");

        DiffResult result = XmlDiff.diff(before, after);

        assertTrue(result.hasSemanticChanges(), "Expected semantic changes");
        assertTrue(result.hasFormattingChanges(), "Expected formatting changes");
        assertChange(result, ChangeType.ATTRIBUTE_CHANGED, "/dep/@scope", "compile", "test");
        assertTrue(
                result.formattingChanges().stream()
                        .anyMatch(c -> c.type() == ChangeType.QUOTE_STYLE_CHANGED
                                && c.path().equals("/dep/@type")),
                "Expected QUOTE_STYLE_CHANGED for /dep/@type");
    }

    @Test
    void differentRootElements() {
        Document before = Document.of("<old>content</old>");
        Document after = Document.of("<new>content</new>");

        DiffResult result = XmlDiff.diff(before, after);

        assertChange(result, ChangeType.ELEMENT_REMOVED, "/old");
        assertChange(result, ChangeType.ELEMENT_ADDED, "/new");
    }

    @Test
    void emptyDocuments() {
        Document before = Document.of();
        Document after = Document.of();

        DiffResult result = XmlDiff.diff(before, after);

        assertFalse(result.hasChanges());
    }

    @Test
    void rootAddedToEmptyDocument() {
        Document before = Document.of();
        Document after = Document.of("<root/>");

        DiffResult result = XmlDiff.diff(before, after);

        assertChange(result, ChangeType.ELEMENT_ADDED, "/root");
    }

    @Test
    void rootRemovedFromDocument() {
        Document before = Document.of("<root/>");
        Document after = Document.of();

        DiffResult result = XmlDiff.diff(before, after);

        assertChange(result, ChangeType.ELEMENT_REMOVED, "/root");
    }

    @Test
    void multipleChangesInSameElement() {
        Document before = Document.of("<e attr1=\"a\" attr2=\"b\">text</e>");
        Document after = Document.of("<e attr1=\"x\" attr3=\"c\">changed</e>");

        DiffResult result = XmlDiff.diff(before, after);

        assertChange(result, ChangeType.ATTRIBUTE_CHANGED, "/e/@attr1", "a", "x");
        assertChange(result, ChangeType.ATTRIBUTE_REMOVED, "/e/@attr2", "b", null);
        assertChange(result, ChangeType.ATTRIBUTE_ADDED, "/e/@attr3", null, "c");
        assertChange(result, ChangeType.TEXT_CHANGED, "/e", "text", "changed");
    }

    @Test
    void xmlChangeToString() {
        XmlChange change = new XmlChange(ChangeType.TEXT_CHANGED, "/project/version", "1.0", "1.1", null, null);
        assertEquals("TEXT_CHANGED: /project/version: \"1.0\" \u2192 \"1.1\"", change.toString());
    }

    @Test
    void xmlChangeEquality() {
        XmlChange a = new XmlChange(ChangeType.TEXT_CHANGED, "/project/version", "1.0", "1.1", null, null);
        XmlChange b = new XmlChange(ChangeType.TEXT_CHANGED, "/project/version", "1.0", "1.1", null, null);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void diffConfigDefaults() {
        DiffConfig config = DiffConfig.defaults();
        assertTrue(config.matchKeysFor("anything").isEmpty());
    }

    @Test
    void diffConfigBuilder() {
        DiffConfig config = DiffConfig.builder()
                .matchBy("dependency", "groupId", "artifactId")
                .matchBy("*", "id")
                .build();

        assertEquals(2, config.matchKeysFor("dependency").size());
        assertEquals(1, config.matchKeysFor("other").size()); // wildcard
    }

    @Test
    void changeTypeClassification() {
        assertTrue(ChangeType.ELEMENT_ADDED.isSemantic());
        assertTrue(ChangeType.ELEMENT_REMOVED.isSemantic());
        assertTrue(ChangeType.ELEMENT_MOVED.isSemantic());
        assertTrue(ChangeType.TEXT_CHANGED.isSemantic());
        assertTrue(ChangeType.ATTRIBUTE_ADDED.isSemantic());
        assertTrue(ChangeType.ATTRIBUTE_REMOVED.isSemantic());
        assertTrue(ChangeType.ATTRIBUTE_CHANGED.isSemantic());
        assertTrue(ChangeType.COMMENT_ADDED.isSemantic());
        assertTrue(ChangeType.COMMENT_REMOVED.isSemantic());
        assertTrue(ChangeType.COMMENT_CHANGED.isSemantic());
        assertTrue(ChangeType.PI_ADDED.isSemantic());
        assertTrue(ChangeType.PI_REMOVED.isSemantic());
        assertTrue(ChangeType.PI_CHANGED.isSemantic());
        assertTrue(ChangeType.NAMESPACE_CHANGED.isSemantic());

        assertTrue(ChangeType.WHITESPACE_CHANGED.isFormattingOnly());
        assertTrue(ChangeType.QUOTE_STYLE_CHANGED.isFormattingOnly());
        assertTrue(ChangeType.ENTITY_FORM_CHANGED.isFormattingOnly());
        assertTrue(ChangeType.EMPTY_ELEMENT_STYLE_CHANGED.isFormattingOnly());
    }

    @Test
    void matchByAttributeKeys() {
        Document before = Document.of("<items><item id=\"1\">A</item><item id=\"2\">B</item></items>");
        Document after = Document.of("<items><item id=\"2\">B2</item><item id=\"1\">A</item></items>");

        DiffConfig config = DiffConfig.builder().matchBy("item", "id").build();
        DiffResult result = XmlDiff.diff(before, after, config);

        assertTrue(
                result.semanticChanges().stream()
                        .anyMatch(c -> c.type() == ChangeType.TEXT_CHANGED
                                && "B".equals(c.beforeValue())
                                && "B2".equals(c.afterValue())),
                "Expected text change B->B2 for item id=2");
    }

    @Test
    void wildcardMatchKeys() {
        Document before = Document.of("<root><thing id=\"x\">old</thing></root>");
        Document after = Document.of("<root><thing id=\"x\">new</thing></root>");

        DiffConfig config = DiffConfig.builder().matchBy("*", "id").build();
        DiffResult result = XmlDiff.diff(before, after, config);

        assertChange(result, ChangeType.TEXT_CHANGED, "/root/thing", "old", "new");
    }

    @Test
    void diffResultToStringWithChanges() {
        Document before = Document.of("<r><a>1</a><b>2</b></r>");
        Document after = Document.of("<r><a>X</a><b>Y</b></r>");

        DiffResult result = XmlDiff.diff(before, after);

        String str = result.toString();
        assertTrue(str.contains("TEXT_CHANGED"));
        assertTrue(str.contains("/r/a"));
        assertTrue(str.contains("/r/b"));
    }

    @Test
    void noSemanticChangesWhenOnlyFormattingDiffers() {
        Document before = Document.of("<dep scope='test'>junit</dep>");
        Document after = Document.of("<dep scope=\"test\">junit</dep>");

        DiffResult result = XmlDiff.diff(before, after);

        assertTrue(result.hasChanges(), "Should detect formatting changes");
        assertFalse(result.hasSemanticChanges(), "Should not have semantic changes");
        assertTrue(result.hasFormattingChanges(), "Should have formatting changes");
    }

    @Test
    void deeplyNestedPathGeneration() {
        Document before = Document.of("<a><b><c><d>old</d></c></b></a>");
        Document after = Document.of("<a><b><c><d>new</d></c></b></a>");

        DiffResult result = XmlDiff.diff(before, after);

        assertChange(result, ChangeType.TEXT_CHANGED, "/a/b/c/d", "old", "new");
    }

    @Test
    void detectsWhitespaceChange() {
        Document before = Document.of("<project>\n  <version>1.0</version>\n</project>");
        Document after = Document.of("<project>\n    <version>1.0</version>\n</project>");

        DiffResult result = XmlDiff.diff(before, after);

        assertChange(result, ChangeType.WHITESPACE_CHANGED, "/project/version");
        assertFalse(result.hasSemanticChanges(), "Whitespace-only change should not be semantic");
        assertTrue(result.hasFormattingChanges(), "Should detect formatting changes");
    }

    @Test
    void whitespaceOnlyDiffHasNoSemanticChanges() {
        Document before =
                Document.of("<project>\n  <groupId>org.example</groupId>\n  <version>1.0</version>\n</project>");
        Document after =
                Document.of("<project>\n    <groupId>org.example</groupId>\n    <version>1.0</version>\n</project>");

        DiffResult result = XmlDiff.diff(before, after);

        assertTrue(result.hasChanges(), "Should detect whitespace changes");
        assertTrue(result.semanticChanges().isEmpty(), "semanticChanges() should be empty for whitespace-only diff");
        assertEquals(2, result.formattingChanges().size(), "Should have formatting changes for both elements");
    }

    @Test
    void mixedSemanticAndWhitespaceChanges() {
        Document before = Document.of("<project>\n  <version>1.0</version>\n  <name>old</name>\n</project>");
        Document after = Document.of("<project>\n    <version>2.0</version>\n    <name>old</name>\n</project>");

        DiffResult result = XmlDiff.diff(before, after);

        // Semantic: version text changed
        List<XmlChange> semantic = result.semanticChanges();
        assertEquals(1, semantic.size());
        assertEquals(ChangeType.TEXT_CHANGED, semantic.get(0).type());
        assertEquals("1.0", semantic.get(0).beforeValue());
        assertEquals("2.0", semantic.get(0).afterValue());

        // Formatting: whitespace changed on both elements
        assertTrue(result.formattingChanges().size() >= 2, "Should have whitespace changes");
    }

    @Test
    void changesForWithXPathExpression() {
        String beforeXml = "<project>"
                + "<dependencies>"
                + "<dependency><groupId>junit</groupId><scope>test</scope><version>4</version></dependency>"
                + "<dependency><groupId>log4j</groupId><scope>compile</scope><version>1</version></dependency>"
                + "</dependencies>"
                + "<version>1.0</version>"
                + "</project>";
        String afterXml = "<project>"
                + "<dependencies>"
                + "<dependency><groupId>junit</groupId><scope>test</scope><version>5</version></dependency>"
                + "<dependency><groupId>log4j</groupId><scope>compile</scope><version>2</version></dependency>"
                + "</dependencies>"
                + "<version>2.0</version>"
                + "</project>";

        Document before = Document.of(beforeXml);
        Document after = Document.of(afterXml);
        DiffResult result = XmlDiff.diff(before, after);

        // All changes (version + 2 dependency versions)
        assertEquals(3, result.semanticChanges().size());

        // Filter to test-scoped dependencies only
        List<XmlChange> testChanges = result.changesFor("//dependency[scope='test']", after);
        assertTrue(
                testChanges.stream()
                        .anyMatch(c -> c.type() == ChangeType.TEXT_CHANGED
                                && "4".equals(c.beforeValue())
                                && "5".equals(c.afterValue())),
                "Should find junit version change");
        assertFalse(
                testChanges.stream().anyMatch(c -> "1".equals(c.beforeValue()) && "2".equals(c.afterValue())),
                "Should not include log4j version change");

        // Filter to project version only
        List<XmlChange> versionChanges = result.changesFor("version", after);
        assertEquals(1, versionChanges.size());
        assertEquals("1.0", versionChanges.get(0).beforeValue());
        assertEquals("2.0", versionChanges.get(0).afterValue());
    }

    @Test
    void changesForWithNoMatches() {
        Document before = Document.of("<project><version>1.0</version></project>");
        Document after = Document.of("<project><version>2.0</version></project>");

        DiffResult result = XmlDiff.diff(before, after);

        List<XmlChange> noChanges = result.changesFor("//nonexistent", after);
        assertTrue(noChanges.isEmpty());
    }

    @Test
    void detectsProcessingInstructionAdded() {
        Document before = Document.of("<root>text</root>");
        Document after = Document.of("<root><?target data?>text</root>");
        DiffResult result = XmlDiff.diff(before, after);
        assertChange(result, ChangeType.PI_ADDED, "/root/processing-instruction()");
    }

    @Test
    void detectsProcessingInstructionRemoved() {
        Document before = Document.of("<root><?target data?>text</root>");
        Document after = Document.of("<root>text</root>");
        DiffResult result = XmlDiff.diff(before, after);
        assertChange(result, ChangeType.PI_REMOVED, "/root/processing-instruction()");
    }

    @Test
    void detectsProcessingInstructionChanged() {
        Document before = Document.of("<root><?target old?></root>");
        Document after = Document.of("<root><?target new?></root>");
        DiffResult result = XmlDiff.diff(before, after);
        assertChange(result, ChangeType.PI_CHANGED, "/root/processing-instruction()");
    }

    @Test
    void detectsNamespaceChange() {
        Document before = Document.of("<root xmlns=\"http://old.ns\"/>");
        Document after = Document.of("<root xmlns=\"http://new.ns\"/>");
        DiffResult result = XmlDiff.diff(before, after);
        assertChange(result, ChangeType.NAMESPACE_CHANGED, "/root");
    }

    @Test
    void changesUnderUsesBoundaryAwareMatching() {
        Document before = Document.of("<project><name>old</name><namespace>ns</namespace></project>");
        Document after = Document.of("<project><name>new</name><namespace>ns2</namespace></project>");
        DiffResult result = XmlDiff.diff(before, after);

        // /project/name should only match changes at /project/name, not /project/namespace
        List<XmlChange> nameChanges = result.changesUnder("/project/name");
        assertFalse(nameChanges.isEmpty(), "changesUnder('/project/name') should return at least one change");
        assertTrue(nameChanges.stream()
                .allMatch(c -> c.path().equals("/project/name")
                        || c.path().startsWith("/project/name/")
                        || c.path().startsWith("/project/name/@")));
        assertFalse(nameChanges.stream().anyMatch(c -> c.path().startsWith("/project/namespace")));
    }

    // --- Assertion helpers ---

    private static void assertChange(DiffResult result, ChangeType expectedType, String expectedPath) {
        assertTrue(
                result.changes().stream()
                        .anyMatch(c -> c.type() == expectedType && c.path().equals(expectedPath)),
                "Expected " + expectedType + " at " + expectedPath + " but got: " + result);
    }

    private static void assertChange(
            DiffResult result, ChangeType expectedType, String expectedPath, String beforeValue, String afterValue) {
        assertTrue(
                result.changes().stream()
                        .anyMatch(c -> c.type() == expectedType
                                && c.path().equals(expectedPath)
                                && Objects.equals(beforeValue, c.beforeValue())
                                && Objects.equals(afterValue, c.afterValue())),
                "Expected " + expectedType + " at " + expectedPath + " with before=" + beforeValue + " after="
                        + afterValue + " but got: " + result);
    }
}
