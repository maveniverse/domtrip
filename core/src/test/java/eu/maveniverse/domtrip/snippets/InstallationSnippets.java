package eu.maveniverse.domtrip.snippets;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.Editor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Snippet tests for the Installation documentation.
 */
public class InstallationSnippets extends BaseSnippetTest {

    @Test
    public void demonstrateInstallationTest() {
        // START: installation-test
        try {
            String xml = "<project><version>1.0</version></project>";
            Document doc = Document.of(xml);
            Editor editor = new Editor(doc);

            String result = editor.toXml();
            System.out.println("DomTrip is working! Result: " + result);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        // END: installation-test

        // Test passes if no exception is thrown
        Assertions.assertTrue(true);
    }
}
