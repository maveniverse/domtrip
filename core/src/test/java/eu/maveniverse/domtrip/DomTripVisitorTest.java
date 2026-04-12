/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for the visitor/walker pattern.
 */
class DomTripVisitorTest {

    private Editor editor;
    private Element root;

    @BeforeEach
    void setUp() {
        String xml = """
            <project>
                <groupId>com.example</groupId>
                <artifactId>test-project</artifactId>
                <dependencies>
                    <dependency>
                        <groupId>junit</groupId>
                        <artifactId>junit</artifactId>
                    </dependency>
                </dependencies>
            </project>
            """;
        editor = new Editor(Document.of(xml));
        root = editor.root();
    }

    @Test
    void testEnterExitOrder() {
        List<String> events = new ArrayList<>();

        root.accept(new DomTripVisitor() {
            @Override
            public Action enterElement(Element element) {
                events.add("enter:" + element.name());
                return Action.CONTINUE;
            }

            @Override
            public void exitElement(Element element) {
                events.add("exit:" + element.name());
            }
        });

        assertEquals("enter:project", events.get(0));
        assertTrue(events.contains("enter:groupId"));
        assertTrue(events.contains("exit:groupId"));
        assertTrue(events.contains("enter:dependencies"));
        assertTrue(events.contains("enter:dependency"));
        assertTrue(events.contains("exit:dependency"));
        assertTrue(events.contains("exit:dependencies"));
        assertEquals("exit:project", events.get(events.size() - 1));

        // Verify enter always comes before exit for same element
        int enterDeps = events.indexOf("enter:dependencies");
        int exitDeps = events.indexOf("exit:dependencies");
        assertTrue(enterDeps < exitDeps);

        // Verify child is between parent enter/exit
        int enterDep = events.indexOf("enter:dependency");
        assertTrue(enterDep > enterDeps && enterDep < exitDeps);
    }

    @Test
    void testSkipAction() {
        List<String> visited = new ArrayList<>();

        root.accept(new DomTripVisitor() {
            @Override
            public Action enterElement(Element element) {
                visited.add("enter:" + element.name());
                if ("dependencies".equals(element.name())) {
                    return Action.SKIP;
                }
                return Action.CONTINUE;
            }

            @Override
            public void exitElement(Element element) {
                visited.add("exit:" + element.name());
            }
        });

        // dependencies should be entered and exited
        assertTrue(visited.contains("enter:dependencies"));
        assertTrue(visited.contains("exit:dependencies"));

        // But dependency (child of dependencies) should NOT be visited
        assertFalse(visited.contains("enter:dependency"));
        assertFalse(visited.contains("exit:dependency"));
    }

    @Test
    void testStopAction() {
        List<String> visited = new ArrayList<>();

        root.accept(new DomTripVisitor() {
            @Override
            public Action enterElement(Element element) {
                visited.add(element.name());
                if ("artifactId".equals(element.name())) {
                    return Action.STOP;
                }
                return Action.CONTINUE;
            }
        });

        // Should have visited project, groupId, then artifactId where it stopped
        assertTrue(visited.contains("project"));
        assertTrue(visited.contains("groupId"));
        assertTrue(visited.contains("artifactId"));
        // dependencies comes after artifactId, should NOT be visited
        assertFalse(visited.contains("dependencies"));
    }

    @Test
    void testTextNodesVisited() {
        List<String> textContents = new ArrayList<>();

        root.accept(new DomTripVisitor() {
            @Override
            public Action visitText(Text text) {
                if (!text.isWhitespaceOnly()) {
                    textContents.add(text.content().trim());
                }
                return Action.CONTINUE;
            }
        });

        assertTrue(textContents.contains("com.example"));
        assertTrue(textContents.contains("test-project"));
        assertTrue(textContents.contains("junit"));
    }

    @Test
    void testCommentNodesVisited() {
        String xml = """
            <root>
                <!-- first comment -->
                <child>text</child>
                <!-- second comment -->
            </root>
            """;
        Element commentRoot = Document.of(xml).root();
        List<String> comments = new ArrayList<>();

        commentRoot.accept(new DomTripVisitor() {
            @Override
            public Action visitComment(Comment comment) {
                comments.add(comment.content().trim());
                return Action.CONTINUE;
            }
        });

        assertEquals(2, comments.size());
        assertEquals("first comment", comments.get(0));
        assertEquals("second comment", comments.get(1));
    }

    @Test
    void testProcessingInstructionVisited() {
        String xml = """
            <root>
                <?xml-stylesheet type="text/xsl" href="style.xsl"?>
                <child>text</child>
            </root>
            """;
        Element piRoot = Document.of(xml).root();
        List<String> piTargets = new ArrayList<>();

        piRoot.accept(new DomTripVisitor() {
            @Override
            public Action visitProcessingInstruction(ProcessingInstruction pi) {
                piTargets.add(pi.target());
                return Action.CONTINUE;
            }
        });

        assertEquals(1, piTargets.size());
        assertEquals("xml-stylesheet", piTargets.get(0));
    }

    @Test
    void testMutationDuringTraversal() {
        root.accept(new DomTripVisitor() {
            @Override
            public Action enterElement(Element element) {
                if ("groupId".equals(element.name())) {
                    element.findTextNode().ifPresent(t -> t.content("mutated"));
                }
                return Action.CONTINUE;
            }
        });

        assertEquals("mutated", root.childElement("groupId").orElseThrow().textContent());
    }

    @Test
    void testEmptyElement() {
        String xml = "<root><empty/></root>";
        Element emptyRoot = Document.of(xml).root();
        List<String> events = new ArrayList<>();

        emptyRoot.accept(new DomTripVisitor() {
            @Override
            public Action enterElement(Element element) {
                events.add("enter:" + element.name());
                return Action.CONTINUE;
            }

            @Override
            public void exitElement(Element element) {
                events.add("exit:" + element.name());
            }
        });

        assertTrue(events.contains("enter:empty"));
        assertTrue(events.contains("exit:empty"));
        int enterIdx = events.indexOf("enter:empty");
        int exitIdx = events.indexOf("exit:empty");
        assertEquals(enterIdx + 1, exitIdx);
    }

    @Test
    void testWalkFromSubElement() {
        Element deps = root.childElement("dependencies").orElseThrow();
        List<String> visited = new ArrayList<>();

        deps.accept(new DomTripVisitor() {
            @Override
            public Action enterElement(Element element) {
                visited.add(element.name());
                return Action.CONTINUE;
            }
        });

        // Should only visit dependencies subtree
        assertTrue(visited.contains("dependencies"));
        assertTrue(visited.contains("dependency"));
        assertTrue(visited.contains("groupId"));
        assertFalse(visited.contains("project"));
    }

    @Test
    void testDeeplyNestedStructure() {
        String xml = "<a><b><c><d><e>deep</e></d></c></b></a>";
        Element deepRoot = Document.of(xml).root();
        List<String> enterOrder = new ArrayList<>();
        List<String> exitOrder = new ArrayList<>();

        deepRoot.accept(new DomTripVisitor() {
            @Override
            public Action enterElement(Element element) {
                enterOrder.add(element.name());
                return Action.CONTINUE;
            }

            @Override
            public void exitElement(Element element) {
                exitOrder.add(element.name());
            }
        });

        assertEquals(List.of("a", "b", "c", "d", "e"), enterOrder);
        assertEquals(List.of("e", "d", "c", "b", "a"), exitOrder);
    }

    @Test
    void testMixedContent() {
        String xml = "<root>text1<child/>text2<!-- comment -->text3</root>";
        Element mixedRoot = Document.of(xml).root();
        List<String> events = new ArrayList<>();

        mixedRoot.accept(new DomTripVisitor() {
            @Override
            public Action enterElement(Element element) {
                events.add("enter:" + element.name());
                return Action.CONTINUE;
            }

            @Override
            public void exitElement(Element element) {
                events.add("exit:" + element.name());
            }

            @Override
            public Action visitText(Text text) {
                events.add("text:" + text.content());
                return Action.CONTINUE;
            }

            @Override
            public Action visitComment(Comment comment) {
                events.add("comment:" + comment.content().trim());
                return Action.CONTINUE;
            }
        });

        assertEquals("enter:root", events.get(0));
        assertEquals("text:text1", events.get(1));
        assertEquals("enter:child", events.get(2));
        assertEquals("exit:child", events.get(3));
        assertEquals("text:text2", events.get(4));
        assertEquals("comment:comment", events.get(5));
        assertEquals("text:text3", events.get(6));
        assertEquals("exit:root", events.get(7));
    }

    @Test
    void testStopFromTextNode() {
        List<String> events = new ArrayList<>();

        root.accept(new DomTripVisitor() {
            @Override
            public Action enterElement(Element element) {
                events.add("enter:" + element.name());
                return Action.CONTINUE;
            }

            @Override
            public Action visitText(Text text) {
                if (!text.isWhitespaceOnly() && text.content().contains("com.example")) {
                    events.add("stop-at-text");
                    return Action.STOP;
                }
                return Action.CONTINUE;
            }
        });

        assertTrue(events.contains("stop-at-text"));
        // Should not have visited elements after the stop
        assertFalse(events.contains("enter:artifactId"));
    }

    @Test
    void testStopFromCommentNode() {
        String xml = """
            <root>
                <!-- stop here -->
                <child>should not visit</child>
            </root>
            """;
        Element commentRoot = Document.of(xml).root();
        List<String> events = new ArrayList<>();

        commentRoot.accept(new DomTripVisitor() {
            @Override
            public Action enterElement(Element element) {
                events.add("enter:" + element.name());
                return Action.CONTINUE;
            }

            @Override
            public Action visitComment(Comment comment) {
                events.add("comment");
                return Action.STOP;
            }
        });

        assertTrue(events.contains("enter:root"));
        assertTrue(events.contains("comment"));
        assertFalse(events.contains("enter:child"));
    }

    @Test
    void testEditorWalk() {
        List<String> elements = new ArrayList<>();

        editor.walk(new DomTripVisitor() {
            @Override
            public Action enterElement(Element element) {
                elements.add(element.name());
                return Action.CONTINUE;
            }
        });

        assertTrue(elements.contains("project"));
        assertTrue(elements.contains("groupId"));
        assertTrue(elements.contains("dependencies"));
    }

    @Test
    void testTreeWalkerLambdaApi() {
        List<String> entered = new ArrayList<>();
        List<String> exited = new ArrayList<>();
        List<String> texts = new ArrayList<>();

        root.walk()
                .onEnter(e -> {
                    entered.add(e.name());
                    return DomTripVisitor.Action.CONTINUE;
                })
                .onExit(e -> exited.add(e.name()))
                .onText(t -> {
                    if (!t.isWhitespaceOnly()) {
                        texts.add(t.content().trim());
                    }
                    return DomTripVisitor.Action.CONTINUE;
                })
                .execute();

        assertTrue(entered.contains("project"));
        assertTrue(entered.contains("groupId"));
        assertTrue(exited.contains("project"));
        assertTrue(texts.contains("com.example"));
    }

    @Test
    void testTreeWalkerSkip() {
        List<String> visited = new ArrayList<>();

        root.walk()
                .onEnter(e -> {
                    visited.add(e.name());
                    if ("dependencies".equals(e.name())) {
                        return DomTripVisitor.Action.SKIP;
                    }
                    return DomTripVisitor.Action.CONTINUE;
                })
                .execute();

        assertTrue(visited.contains("dependencies"));
        assertFalse(visited.contains("dependency"));
    }

    @Test
    void testTreeWalkerStop() {
        List<String> visited = new ArrayList<>();

        root.walk()
                .onEnter(e -> {
                    visited.add(e.name());
                    if ("artifactId".equals(e.name())) {
                        return DomTripVisitor.Action.STOP;
                    }
                    return DomTripVisitor.Action.CONTINUE;
                })
                .execute();

        assertTrue(visited.contains("artifactId"));
        assertFalse(visited.contains("dependencies"));
    }

    @Test
    void testTreeWalkerWithComments() {
        String xml = """
            <root>
                <!-- a comment -->
                <child>text</child>
            </root>
            """;
        Element commentRoot = Document.of(xml).root();
        List<String> comments = new ArrayList<>();

        commentRoot
                .walk()
                .onComment(c -> {
                    comments.add(c.content().trim());
                    return DomTripVisitor.Action.CONTINUE;
                })
                .execute();

        assertEquals(1, comments.size());
        assertEquals("a comment", comments.get(0));
    }

    @Test
    void testTreeWalkerWithProcessingInstructions() {
        String xml = """
            <root>
                <?target data?>
                <child>text</child>
            </root>
            """;
        Element piRoot = Document.of(xml).root();
        List<String> targets = new ArrayList<>();

        piRoot.walk()
                .onProcessingInstruction(pi -> {
                    targets.add(pi.target());
                    return DomTripVisitor.Action.CONTINUE;
                })
                .execute();

        assertEquals(1, targets.size());
        assertEquals("target", targets.get(0));
    }

    @Test
    void testDefaultVisitorMethodsReturnContinue() {
        DomTripVisitor defaultVisitor = new DomTripVisitor() {};
        List<String> events = new ArrayList<>();

        // Should traverse entire tree without issues
        root.accept(defaultVisitor);

        // Verify no exceptions were thrown and traversal completed
        root.accept(new DomTripVisitor() {
            @Override
            public Action enterElement(Element element) {
                events.add(element.name());
                return Action.CONTINUE;
            }
        });

        // If we get here, the default visitor completed successfully
        assertFalse(events.isEmpty());
    }

    @Test
    void testNullVisitorThrows() {
        Document doc = Document.of("<root/>");
        assertThrows(IllegalArgumentException.class, () -> doc.accept(null));
        assertThrows(IllegalArgumentException.class, () -> root.accept(null));
        Text text = Text.of("test");
        assertThrows(IllegalArgumentException.class, () -> text.accept(null));
        Comment comment = Comment.of("test");
        assertThrows(IllegalArgumentException.class, () -> comment.accept(null));
        ProcessingInstruction pi = ProcessingInstruction.of("target");
        assertThrows(IllegalArgumentException.class, () -> pi.accept(null));
    }

    @Test
    void testDocumentAcceptVisitsAllTopLevelNodes() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!-- document comment -->
            <root>
                <child>text</child>
            </root>
            """;
        Document doc = Document.of(xml);
        List<String> events = new ArrayList<>();

        doc.accept(new DomTripVisitor() {
            @Override
            public Action enterElement(Element element) {
                events.add("enter:" + element.name());
                return Action.CONTINUE;
            }

            @Override
            public void exitElement(Element element) {
                events.add("exit:" + element.name());
            }

            @Override
            public Action visitComment(Comment comment) {
                events.add("comment:" + comment.content().trim());
                return Action.CONTINUE;
            }
        });

        assertTrue(events.contains("comment:document comment"));
        assertTrue(events.contains("enter:root"));
        assertTrue(events.contains("enter:child"));
        assertTrue(events.contains("exit:root"));
    }

    @Test
    void testStatefulVisitorDepthTracking() {
        List<String> depthEntries = new ArrayList<>();

        root.accept(new DomTripVisitor() {
            private int depth = 0;

            @Override
            public Action enterElement(Element element) {
                depthEntries.add(depth + ":" + element.name());
                depth++;
                return Action.CONTINUE;
            }

            @Override
            public void exitElement(Element element) {
                depth--;
            }
        });

        assertTrue(depthEntries.contains("0:project"));
        assertTrue(depthEntries.contains("1:groupId"));
        assertTrue(depthEntries.contains("1:dependencies"));
        assertTrue(depthEntries.contains("2:dependency"));
        assertTrue(depthEntries.contains("3:groupId"));
    }

    @Test
    void testSelectiveSubtreeSkipping() {
        String xml = """
            <root>
                <metadata>
                    <deep>should not visit</deep>
                </metadata>
                <content>
                    <important>should visit</important>
                </content>
            </root>
            """;
        Element selectiveRoot = Document.of(xml).root();
        List<String> visited = new ArrayList<>();

        selectiveRoot.accept(new DomTripVisitor() {
            @Override
            public Action enterElement(Element element) {
                visited.add(element.name());
                if ("metadata".equals(element.name())) {
                    return Action.SKIP;
                }
                return Action.CONTINUE;
            }
        });

        assertTrue(visited.contains("root"));
        assertTrue(visited.contains("metadata"));
        assertFalse(visited.contains("deep"));
        assertTrue(visited.contains("content"));
        assertTrue(visited.contains("important"));
    }

    @Test
    void testTextRedaction() {
        String xml = """
            <config>
                <username>admin</username>
                <password>secret123</password>
                <host>localhost</host>
            </config>
            """;
        Element configRoot = Document.of(xml).root();

        configRoot.accept(new DomTripVisitor() {
            @Override
            public Action enterElement(Element element) {
                if ("password".equals(element.name())) {
                    element.findTextNode().ifPresent(t -> t.content("***"));
                    return Action.SKIP;
                }
                return Action.CONTINUE;
            }
        });

        assertEquals("***", configRoot.childElement("password").orElseThrow().textContent());
        assertEquals("admin", configRoot.childElement("username").orElseThrow().textContent());
    }

    @Test
    void testExitCalledEvenWithSkip() {
        List<String> events = new ArrayList<>();

        root.accept(new DomTripVisitor() {
            @Override
            public Action enterElement(Element element) {
                events.add("enter:" + element.name());
                if ("dependencies".equals(element.name())) {
                    return Action.SKIP;
                }
                return Action.CONTINUE;
            }

            @Override
            public void exitElement(Element element) {
                events.add("exit:" + element.name());
            }
        });

        // exitElement should be called even when children were skipped
        int enterIdx = events.indexOf("enter:dependencies");
        int exitIdx = events.indexOf("exit:dependencies");
        assertTrue(enterIdx >= 0);
        assertTrue(exitIdx >= 0);
        assertTrue(exitIdx > enterIdx);
    }

    @Test
    void testExitNotCalledOnStop() {
        List<String> events = new ArrayList<>();

        root.accept(new DomTripVisitor() {
            @Override
            public Action enterElement(Element element) {
                events.add("enter:" + element.name());
                if ("groupId".equals(element.name())) {
                    return Action.STOP;
                }
                return Action.CONTINUE;
            }

            @Override
            public void exitElement(Element element) {
                events.add("exit:" + element.name());
            }
        });

        // When STOP is returned from enterElement, exitElement should NOT be called
        // for the stopped element or any ancestor
        assertTrue(events.contains("enter:project"));
        assertTrue(events.contains("enter:groupId"));
        assertFalse(events.contains("exit:groupId"));
        assertFalse(events.contains("exit:project"));
    }

    @Test
    void testTreeWalkerMatchesVisitorBehavior() {
        List<String> visitorEvents = new ArrayList<>();
        List<String> walkerEvents = new ArrayList<>();

        // Collect events using visitor
        root.accept(new DomTripVisitor() {
            @Override
            public Action enterElement(Element element) {
                visitorEvents.add("enter:" + element.name());
                return Action.CONTINUE;
            }

            @Override
            public void exitElement(Element element) {
                visitorEvents.add("exit:" + element.name());
            }
        });

        // Collect events using walker
        root.walk()
                .onEnter(e -> {
                    walkerEvents.add("enter:" + e.name());
                    return DomTripVisitor.Action.CONTINUE;
                })
                .onExit(e -> walkerEvents.add("exit:" + e.name()))
                .execute();

        assertEquals(visitorEvents, walkerEvents);
    }
}
