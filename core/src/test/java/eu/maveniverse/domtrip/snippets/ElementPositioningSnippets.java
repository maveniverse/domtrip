package eu.maveniverse.domtrip.snippets;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Snippet tests for element positioning features documentation.
 */
public class ElementPositioningSnippets extends BaseSnippetTest {

    @Test
    public void demonstrateInsertElementAt() throws DomTripException {
        // START: insert-element-at
        Document doc = Document.of("""
            <dependencies>
                <dependency>
                    <groupId>junit</groupId>
                    <artifactId>junit</artifactId>
                </dependency>
                <dependency>
                    <groupId>hamcrest</groupId>
                    <artifactId>hamcrest-core</artifactId>
                </dependency>
            </dependencies>
            """);

        Editor editor = new Editor(doc);
        Element dependencies = doc.root();

        // Insert a new dependency at index 1 (between existing dependencies)
        Element newDep = editor.insertElementAt(dependencies, 1, "dependency");
        editor.addElement(newDep, "groupId", "mockito");
        editor.addElement(newDep, "artifactId", "mockito-core");

        String result = editor.toXml();
        // END: insert-element-at

        Assertions.assertTrue(result.contains("mockito"));
        Assertions.assertTrue(result.indexOf("mockito") < result.indexOf("hamcrest"));
    }

    @Test
    public void demonstrateInsertElementBefore() throws DomTripException {
        // START: insert-element-before
        Document doc = Document.of("""
            <project>
                <dependencies>
                    <dependency>
                        <groupId>junit</groupId>
                        <artifactId>junit</artifactId>
                    </dependency>
                </dependencies>
                <build>
                    <plugins/>
                </build>
            </project>
            """);

        Editor editor = new Editor(doc);
        Element build = doc.root().childElement("build").orElseThrow();

        // Insert properties section before build
        Element properties = editor.insertElementBefore(build, "properties");
        editor.addElement(properties, "maven.compiler.source", "17");
        editor.addElement(properties, "maven.compiler.target", "17");

        String result = editor.toXml();
        // END: insert-element-before

        Assertions.assertTrue(result.contains("<properties>"));
        Assertions.assertTrue(result.indexOf("properties") < result.indexOf("build"));
    }

    @Test
    public void demonstrateInsertElementAfter() throws DomTripException {
        // START: insert-element-after
        Document doc = Document.of("""
            <project>
                <groupId>com.example</groupId>
                <artifactId>my-app</artifactId>
                <version>1.0.0</version>
            </project>
            """);

        Editor editor = new Editor(doc);
        Element version = doc.root().childElement("version").orElseThrow();

        // Insert packaging after version
        Element packaging = editor.insertElementAfter(version, "packaging");
        editor.setTextContent(packaging, "jar");

        String result = editor.toXml();
        // END: insert-element-after

        Assertions.assertTrue(result.contains("<packaging>jar</packaging>"));
        Assertions.assertTrue(result.indexOf("version") < result.indexOf("packaging"));
    }

    @Test
    public void demonstrateElementReordering() throws DomTripException {
        // START: element-reordering
        Document doc = Document.of("""
            <project>
                <version>1.0.0</version>
                <groupId>com.example</groupId>
                <artifactId>my-app</artifactId>
            </project>
            """);

        Editor editor = new Editor(doc);
        Element root = doc.root();
        Element version = root.childElement("version").orElseThrow();

        // Remove and re-add at the end to reorder
        root.removeChild(version);
        root.addChild(version);

        String result = editor.toXml();
        // END: element-reordering

        Assertions.assertTrue(result.indexOf("artifactId") < result.indexOf("version"));
    }

    @Test
    public void demonstrateElementReorderingBefore() throws DomTripException {
        // START: element-reordering-before
        Document doc = Document.of("""
            <project>
                <artifactId>my-app</artifactId>
                <version>1.0.0</version>
                <groupId>com.example</groupId>
            </project>
            """);

        Editor editor = new Editor(doc);
        Element root = doc.root();
        Element groupId = root.childElement("groupId").orElseThrow();
        Element artifactId = root.childElement("artifactId").orElseThrow();

        // Remove groupId and insert it before artifactId
        root.removeChild(groupId);
        int artifactIdIndex = root.children().toList().indexOf(artifactId);
        root.insertChild(artifactIdIndex, groupId);

        String result = editor.toXml();
        // END: element-reordering-before

        Assertions.assertTrue(result.indexOf("groupId") < result.indexOf("artifactId"));
    }

    @Test
    public void demonstrateElementReorderingAfter() throws DomTripException {
        // START: element-reordering-after
        Document doc = Document.of("""
            <project>
                <groupId>com.example</groupId>
                <version>1.0.0</version>
                <artifactId>my-app</artifactId>
            </project>
            """);

        Editor editor = new Editor(doc);
        Element root = doc.root();
        Element artifactId = root.childElement("artifactId").orElseThrow();
        Element groupId = root.childElement("groupId").orElseThrow();

        // Remove artifactId and insert it after groupId
        root.removeChild(artifactId);
        int groupIdIndex = root.children().toList().indexOf(groupId);
        root.insertChild(groupIdIndex + 1, artifactId);

        String result = editor.toXml();
        // END: element-reordering-after

        Assertions.assertTrue(result.indexOf("groupId") < result.indexOf("artifactId"));
    }

    @Test
    public void demonstrateWhitespaceInference() throws DomTripException {
        // START: whitespace-inference
        Document doc = Document.of("""
            <project>
                <groupId>com.example</groupId>
                <artifactId>my-app</artifactId>
            </project>
            """);

        Editor editor = new Editor(doc);
        Element artifactId = doc.root().childElement("artifactId").orElseThrow();

        // Insert version between groupId and artifactId
        // Whitespace is automatically inferred from surrounding elements
        Element version = editor.insertElementBefore(artifactId, "version");
        editor.setTextContent(version, "1.0.0");

        String result = editor.toXml();
        // END: whitespace-inference

        Assertions.assertTrue(result.contains("<version>1.0.0</version>"));
        // Verify proper indentation is maintained
        Assertions.assertTrue(result.contains("    <version>1.0.0</version>"));
    }

    @Test
    public void demonstrateComplexReordering() throws DomTripException {
        // START: complex-reordering
        Document doc = Document.of("""
            <project>
                <build/>
                <dependencies/>
                <groupId>com.example</groupId>
                <artifactId>my-app</artifactId>
                <version>1.0.0</version>
            </project>
            """);

        Editor editor = new Editor(doc);
        Element root = doc.root();

        // Move project coordinates to the beginning
        Element groupId = root.childElement("groupId").orElseThrow();
        Element artifactId = root.childElement("artifactId").orElseThrow();
        Element version = root.childElement("version").orElseThrow();

        // Remove all three elements
        root.removeChild(groupId);
        root.removeChild(artifactId);
        root.removeChild(version);

        // Insert them at the beginning in the correct order
        root.insertChild(0, groupId);
        root.insertChild(1, artifactId);
        root.insertChild(2, version);

        String result = editor.toXml();
        // END: complex-reordering

        String[] lines = result.split("\n");
        int groupIdIndex = -1, artifactIdIndex = -1, versionIndex = -1;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("groupId")) groupIdIndex = i;
            if (lines[i].contains("artifactId")) artifactIdIndex = i;
            if (lines[i].contains("version")) versionIndex = i;
        }

        Assertions.assertTrue(groupIdIndex < artifactIdIndex);
        Assertions.assertTrue(artifactIdIndex < versionIndex);
    }
}
