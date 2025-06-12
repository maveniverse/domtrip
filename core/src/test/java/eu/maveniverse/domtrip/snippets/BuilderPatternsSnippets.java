/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.snippets;

import eu.maveniverse.domtrip.*;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Code snippets demonstrating DomTrip's builder patterns and fluent APIs.
 * These snippets are extracted and used in the documentation.
 */
public class BuilderPatternsSnippets extends BaseSnippetTest {

    @Test
    public void fluentInterfaceDesign() {
        // snippet:fluent-interface-design
        Editor editor = new Editor();
        editor.createDocument("project");
        Element root = editor.root();

        // Add namespace attribute
        editor.setAttribute(root, "xmlns", "http://maven.apache.org/POM/4.0.0");

        // Add child elements using fluent API
        editor.addElement(root, "groupId", "com.example");
        editor.addElement(root, "artifactId", "my-project");
        editor.addElement(root, "version", "1.0.0");

        Document doc = editor.document();
        // end-snippet:fluent-interface-design

        Assertions.assertNotNull(doc);
        Assertions.assertEquals("project", root.name());
    }

    @Test
    public void documentCreation() {
        // snippet:document-creation
        // Create empty document
        Editor editor = new Editor();

        // Create from existing XML
        Editor editor2 = new Editor(Document.of("<root></root>"));

        // Create from file (example)
        // Editor editor3 = new Editor(Document.of(Files.readString(Paths.get("document.xml"))));
        // end-snippet:document-creation

        Assertions.assertNotNull(editor);
        Assertions.assertNotNull(editor2);
    }

    @Test
    public void documentConfiguration() {
        // snippet:document-configuration
        DomTripConfig config = DomTripConfig.prettyPrint();
        Editor editor = new Editor(config);
        editor.createDocument("root");

        Document doc = editor.document();
        // end-snippet:document-configuration

        Assertions.assertNotNull(doc);
        Assertions.assertEquals("root", editor.root().name());
    }

    @Test
    public void basicElementCreation() {
        // snippet:basic-element-creation
        Editor editor = new Editor();
        editor.createDocument("root");
        Element root = editor.root();

        // Simple element with text
        editor.addElement(root, "name", "John Doe");

        // Element with attributes
        Element person = editor.addElement(root, "person");
        editor.setAttribute(person, "id", "123");
        editor.setAttribute(person, "active", "true");
        editor.setTextContent(person, "John Doe");

        // Nested elements
        Element personNested = editor.addElement(root, "person");
        editor.addElement(personNested, "firstName", "John");
        editor.addElement(personNested, "lastName", "Doe");
        editor.addElement(personNested, "email", "john@example.com");
        // end-snippet:basic-element-creation

        Document doc = editor.document();
        Assertions.assertNotNull(doc);
    }

    @Test
    public void advancedElementOperations() {
        // snippet:advanced-element-operations
        Editor editor = new Editor();
        editor.createDocument("root");
        Element root = editor.root();

        Person person = new Person("123", "john@example.com", true);

        // Conditional element addition
        Element personElement = editor.addElement(root, "person");
        editor.setAttribute(personElement, "id", person.getId());

        if (person.getEmail() != null) {
            editor.addElement(personElement, "email", person.getEmail());
        }

        if (person.isActive()) {
            editor.addElement(personElement, "status", "active");
        }

        // Element positioning using batch operations
        Element dependencies = editor.addElement(root, "dependencies");
        Map<String, String> dependencyInfo = Map.of(
                "groupId", "junit",
                "artifactId", "junit");
        Element dependency = editor.addElement(dependencies, "dependency");
        editor.addElements(dependency, dependencyInfo);
        editor.addElement(dependency, "scope", "test");
        // end-snippet:advanced-element-operations

        Document doc = editor.document();
        Assertions.assertNotNull(doc);
    }

    @Test
    public void attributeManagement() {
        // snippet:attribute-management
        Editor editor = new Editor();
        editor.createDocument("root");
        Element element = editor.addElement(editor.root(), "test");
        boolean condition = true;

        // Basic attributes
        editor.setAttribute(element, "name", "value");

        // Attribute with specific quote style
        Attribute attr = Attribute.of("id", "123", QuoteStyle.SINGLE);
        element.attributeObject("id", attr);

        // Conditional attributes
        if (condition) {
            editor.setAttribute(element, "optional", "value");
        }

        // Namespace-aware attributes (using string form)
        editor.setAttribute(element, "xml:lang", "en");
        // end-snippet:attribute-management

        Assertions.assertNotNull(element);
        Assertions.assertEquals("value", element.attribute("name"));
    }

    @Test
    public void attributeModification() {
        // snippet:attribute-modification
        Editor editor = new Editor();
        editor.createDocument("root");
        Element element = editor.addElement(editor.root(), "test");

        // Set initial attributes
        editor.setAttribute(element, "id", "old-value");
        editor.setAttribute(element, "class", "old-class");
        editor.setAttribute(element, "deprecated", "true");
        editor.setAttribute(element, "version", "1.0");

        // Update existing attributes
        editor.setAttribute(element, "id", "new-value");

        // Change quote style (create new attribute with different quotes)
        Attribute classAttr = Attribute.of("class", "old-class", QuoteStyle.SINGLE);
        element.attributeObject("class", classAttr);

        // Remove attributes
        editor.removeAttribute(element, "deprecated");

        // Attribute transformation (manual approach)
        String version = element.attribute("version");
        if (version != null) {
            editor.setAttribute(element, "version", version + "-SNAPSHOT");
        }
        // end-snippet:attribute-modification

        Assertions.assertEquals("new-value", element.attribute("id"));
        Assertions.assertEquals("1.0-SNAPSHOT", element.attribute("version"));
        Assertions.assertNull(element.attribute("deprecated"));
    }

    @Test
    public void namespaceDeclaration() {
        // snippet:namespace-declaration
        Editor editor = new Editor();
        editor.createDocument("project");
        Element root = editor.root();

        // Declare namespaces using namespace declaration methods
        root.namespaceDeclaration("", "http://maven.apache.org/POM/4.0.0");
        root.namespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        editor.setAttribute(root, "xsi:schemaLocation", "...");

        // Namespace-aware element creation
        Element nsElement = editor.addElement(root, "ns:element");
        editor.addElement(nsElement, "ns:child", "content");
        // end-snippet:namespace-declaration

        Document doc = editor.document();
        Assertions.assertNotNull(doc);
    }

    @Test
    public void addingComments() {
        // snippet:adding-comments
        Editor editor = new Editor();
        editor.createDocument("root");
        Element root = editor.root();

        // Simple comments
        editor.addComment(root, " This is a comment ");

        // Positioned comments using fluent API
        Element section = editor.addElement(root, "section");
        editor.add()
                .comment()
                .to(section)
                .withContent(" Start of configuration ")
                .build();
        editor.addElement(section, "config", "value");
        editor.add().comment().to(section).withContent(" End of configuration ").build();

        // Multi-line comments
        editor.addComment(
                root,
                """
                This is a multi-line comment
                that spans several lines
                and provides detailed information
                """);
        // end-snippet:adding-comments

        Document doc = editor.document();
        Assertions.assertNotNull(doc);
    }

    @Test
    public void processingInstructions() {
        // snippet:processing-instructions
        Editor editor = new Editor();
        editor.createDocument("root");

        // Processing instructions using direct creation
        ProcessingInstruction stylesheet =
                ProcessingInstruction.of("xml-stylesheet", "type=\"text/xsl\" href=\"transform.xsl\"");
        editor.document().addNode(stylesheet);
        // end-snippet:processing-instructions

        Document doc = editor.document();
        Assertions.assertNotNull(doc);
    }

    @Test
    public void customBuilderExtensions() {
        // snippet:custom-builder-extensions
        MavenPomBuilder pomBuilder = new MavenPomBuilder();
        Document pom = pomBuilder
                .groupId("com.example")
                .artifactId("my-project")
                .version("1.0.0")
                .build();
        // end-snippet:custom-builder-extensions

        Assertions.assertNotNull(pom);
        Assertions.assertEquals("project", pom.root().name());
    }

    @Test
    public void builderComposition() {
        // snippet:builder-composition
        Document pom = new MavenPomBuilder()
                .groupId("com.example")
                .artifactId("my-app")
                .version("1.0.0")
                .addDependency("junit", "junit", "4.13.2", "test")
                .addDependency("org.slf4j", "slf4j-api", "1.7.32", null)
                .build();
        // end-snippet:builder-composition

        Assertions.assertNotNull(pom);
        Assertions.assertEquals("project", pom.root().name());
    }

    @Test
    public void errorHandling() {
        // snippet:error-handling
        String value = "test-value";
        String content = "test-content";

        try {
            Editor editor = new Editor();
            editor.createDocument("root");
            Element root = editor.root();
            Element child = editor.addElement(root, "child");
            editor.setAttribute(child, "required", value);
            editor.setTextContent(child, content);

            Document doc = editor.document();
            Assertions.assertNotNull(doc);
        } catch (DomTripException e) {
            // Handle validation errors
            System.err.println("Failed to build document: " + e.getMessage());
            // Provide fallback or recovery logic
        }
        // end-snippet:error-handling
    }

    // Helper class for examples
    private static class Person {
        private final String id;
        private final String email;
        private final boolean active;

        public Person(String id, String email, boolean active) {
            this.id = id;
            this.email = email;
            this.active = active;
        }

        public String getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public boolean isActive() {
            return active;
        }
    }

    // Custom builder example
    public static class MavenPomBuilder {
        private final Editor editor;
        private Element root;

        public MavenPomBuilder() {
            this.editor = new Editor();
            editor.createDocument("project");
            this.root = editor.root();
            root.namespaceDeclaration("", "http://maven.apache.org/POM/4.0.0");
        }

        public MavenPomBuilder groupId(String groupId) {
            editor.addElement(root, "groupId", groupId);
            return this;
        }

        public MavenPomBuilder artifactId(String artifactId) {
            editor.addElement(root, "artifactId", artifactId);
            return this;
        }

        public MavenPomBuilder version(String version) {
            editor.addElement(root, "version", version);
            return this;
        }

        public MavenPomBuilder addDependency(String groupId, String artifactId, String version, String scope) {
            Element dependencies = root.child("dependencies").orElse(null);
            if (dependencies == null) {
                dependencies = editor.addElement(root, "dependencies");
            }

            Element dependency = editor.addElement(dependencies, "dependency");
            editor.addElement(dependency, "groupId", groupId);
            editor.addElement(dependency, "artifactId", artifactId);
            editor.addElement(dependency, "version", version);
            if (scope != null) {
                editor.addElement(dependency, "scope", scope);
            }
            return this;
        }

        public Document build() {
            return editor.document();
        }
    }
}
