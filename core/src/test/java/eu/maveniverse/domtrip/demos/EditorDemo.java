package eu.maveniverse.domtrip.demos;

import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;

/**
 * Demonstration of the lossless XML editor capabilities.
 * Shows how formatting is preserved during round-trip editing.
 */
public class EditorDemo {

    public static void main(String[] args) {
        // Sample XML with various formatting styles
        String originalXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<!-- Sample XML document -->\n"
                + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n"
                + "\n"
                + "    <groupId>com.example</groupId>\n"
                + "    <artifactId   >sample-project</artifactId>\n"
                + "    <version>1.0.0</version>\n"
                + "    \n"
                + "    <properties>\n"
                + "        <maven.compiler.source>11</maven.compiler.source>\n"
                + "        <maven.compiler.target>11</maven.compiler.target>\n"
                + "    </properties>\n"
                + "    \n"
                + "    <dependencies>\n"
                + "        <!-- Core dependency -->\n"
                + "        <dependency>\n"
                + "            <groupId>junit</groupId>\n"
                + "            <artifactId>junit</artifactId>\n"
                + "            <version>4.13.2</version>\n"
                + "            <scope>test</scope>\n"
                + "        </dependency>\n"
                + "    </dependencies>\n"
                + "    \n"
                + "</project>";

        System.out.println("=== XML Round-Trip Editor Demo ===\n");

        // Create editor and load XML
        Editor editor = new Editor(originalXml);

        System.out.println("1. Original XML:");
        System.out.println(originalXml);
        System.out.println("\n" + editor.documentStats());

        // Test 1: Round-trip without modifications
        System.out.println("\n2. Round-trip without modifications:");
        String roundTrip = editor.toXml();
        boolean identical = originalXml.equals(roundTrip);
        System.out.println("Identical to original: " + identical);
        if (!identical) {
            System.out.println("Round-trip result:");
            System.out.println(roundTrip);
        }

        // Test 2: Add a new dependency
        System.out.println("\n3. Adding new dependency...");
        Element dependencies = editor.element("dependencies").orElseThrow();
        Element newDep = editor.addElement(dependencies, "dependency");
        editor.addElement(newDep, "groupId", "org.mockito");
        editor.addElement(newDep, "artifactId", "mockito-core");
        editor.addElement(newDep, "version", "4.6.1");
        editor.addElement(newDep, "scope", "test");

        System.out.println("Result after adding dependency:");
        System.out.println(editor.toXml());

        // Test 3: Modify existing element
        System.out.println("\n4. Modifying version...");
        Element version = editor.element("version").orElseThrow();
        editor.setTextContent(version, "1.0.1-SNAPSHOT");

        System.out.println("Result after version change:");
        System.out.println(editor.toXml());

        // Test 4: Add comment
        System.out.println("\n5. Adding comment...");
        Element properties = editor.element("properties").orElseThrow();
        editor.addComment(properties, " Updated compiler settings ");

        System.out.println("Result after adding comment:");
        System.out.println(editor.toXml());

        // Test 5: Add attribute
        System.out.println("\n6. Adding attribute...");
        Element project = editor.documentElement().orElseThrow();
        editor.setAttribute(project, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");

        System.out.println("Final result:");
        System.out.println(editor.toXml());

        // Test 6: Pretty print comparison
        System.out.println("\n7. Pretty printed version:");
        System.out.println(editor.toXmlPretty());

        System.out.println("\n=== Demo Complete ===");

        // Demonstrate key features
        demonstrateKeyFeatures();
    }

    private static void demonstrateKeyFeatures() {
        System.out.println("\n=== Key Features Demonstration ===");

        // Feature 1: Whitespace preservation
        System.out.println("\n1. Whitespace Preservation:");
        String xmlWithSpaces = "<root>\n" + "    <element   attr=\"value\"   >\n"
                + "        <nested>  content  </nested>\n"
                + "    </element>\n"
                + "</root>";

        Editor editor = new Editor(xmlWithSpaces);
        System.out.println("Original: " + xmlWithSpaces.replace("\n", "\\n"));
        System.out.println("Round-trip: " + editor.toXml().replace("\n", "\\n"));
        System.out.println("Preserved: " + xmlWithSpaces.equals(editor.toXml()));

        // Feature 2: Comment preservation
        System.out.println("\n2. Comment Preservation:");
        String xmlWithComments = "<!-- Header -->\n<root>\n  <!-- Inline -->\n  <element/>\n</root>";
        editor = new Editor(xmlWithComments);
        System.out.println("Comments preserved: " + xmlWithComments.equals(editor.toXml()));

        // Feature 3: CDATA preservation
        System.out.println("\n3. CDATA Preservation:");
        String xmlWithCData = "<root><script><![CDATA[if (x < y) { alert(\"test\"); }]]></script></root>";
        editor = new Editor(xmlWithCData);
        System.out.println("CDATA preserved: " + xmlWithCData.equals(editor.toXml()));

        // Feature 4: Minimal change serialization
        System.out.println("\n4. Minimal Change Serialization:");
        String complexXml = "<root>\n  <unchanged>content</unchanged>\n  <toModify>old</toModify>\n</root>";
        editor = new Editor(complexXml);
        Element toModify = editor.element("toModify").orElseThrow();
        editor.setTextContent(toModify, "new");

        String result = editor.toXml();
        System.out.println("Only modified element changed:");
        System.out.println("- Unchanged element preserved: " + result.contains("  <unchanged>content</unchanged>"));
        System.out.println("- Modified element updated: " + result.contains("<toModify>new</toModify>"));
        System.out.println("- Overall structure preserved: " + result.startsWith("<root>\n"));
    }
}
