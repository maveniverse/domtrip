package eu.maveniverse.domtrip.snippets;

import eu.maveniverse.domtrip.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Snippet tests for configuration features documentation.
 */
public class ConfigurationSnippets extends BaseSnippetTest {

    @Test
    public void demonstrateBasicConfiguration() {
        // START: basic-configuration
        // Use preset configurations
        DomTripConfig defaults = DomTripConfig.defaults();
        DomTripConfig pretty = DomTripConfig.prettyPrint();
        DomTripConfig minimal = DomTripConfig.minimal();

        // Create custom configuration
        DomTripConfig custom = DomTripConfig.defaults()
                .withIndentString("  ")
                .withWhitespacePreservation(true)
                .withDefaultQuoteStyle(QuoteStyle.DOUBLE);

        // Use with Editor
        String xml = "<root><child>value</child></root>";
        Editor editor = new Editor(Document.of(xml), custom);
        // END: basic-configuration

        Assertions.assertNotNull(defaults);
        Assertions.assertNotNull(pretty);
        Assertions.assertNotNull(minimal);
        Assertions.assertNotNull(custom);
        Assertions.assertNotNull(editor);
    }

    @Test
    public void demonstratePresetConfigurations() {
        // START: preset-configurations
        DomTripConfig defaults = DomTripConfig.defaults();
        // - Preserves all whitespace
        // - Preserves all comments
        // - Preserves processing instructions
        // - Uses double quotes by default
        // - No pretty printing

        DomTripConfig pretty = DomTripConfig.prettyPrint();
        // - Consistent indentation
        // - Clean whitespace formatting
        // - Preserves comments and processing instructions
        // - Readable structure

        DomTripConfig minimal = DomTripConfig.minimal();
        // - No whitespace preservation
        // - No comments
        // - No processing instructions
        // - Omits XML declaration
        // - Compact output
        // END: preset-configurations

        Assertions.assertNotNull(defaults);
        Assertions.assertNotNull(pretty);
        Assertions.assertNotNull(minimal);
    }

    @Test
    public void demonstrateWhitespaceConfiguration() {
        // START: whitespace-configuration
        DomTripConfig config = DomTripConfig.defaults()
                .withWhitespacePreservation(true) // Keep original whitespace
                .withIndentString("    ") // 4 spaces for new content
                .withPrettyPrint(false); // Disable pretty printing
        // END: whitespace-configuration

        Assertions.assertNotNull(config);
    }

    @Test
    public void demonstrateIndentationOptions() {
        // START: indentation-options
        // Different indentation styles
        DomTripConfig spaces = DomTripConfig.defaults().withIndentString("  "); // 2 spaces

        DomTripConfig tabs = DomTripConfig.defaults().withIndentString("\t"); // Tab characters

        DomTripConfig mixed = DomTripConfig.defaults().withIndentString("    "); // 4 spaces
        // END: indentation-options

        Assertions.assertNotNull(spaces);
        Assertions.assertNotNull(tabs);
        Assertions.assertNotNull(mixed);
    }

    @Test
    public void demonstrateQuoteStyleConfiguration() {
        // START: quote-style-configuration
        // Use double quotes for new attributes
        DomTripConfig doubleQuotes = DomTripConfig.defaults().withDefaultQuoteStyle(QuoteStyle.DOUBLE);

        // Use single quotes for new attributes
        DomTripConfig singleQuotes = DomTripConfig.defaults().withDefaultQuoteStyle(QuoteStyle.SINGLE);
        // END: quote-style-configuration

        Assertions.assertNotNull(doubleQuotes);
        Assertions.assertNotNull(singleQuotes);
    }

    @Test
    public void demonstrateCommentAndPIHandling() {
        // START: comment-pi-handling
        DomTripConfig config = DomTripConfig.defaults()
                .withCommentPreservation(true) // Keep all comments
                .withProcessingInstructionPreservation(true); // Keep processing instructions

        // For minimal output, exclude comments and PIs
        DomTripConfig minimal =
                DomTripConfig.defaults().withCommentPreservation(false).withProcessingInstructionPreservation(false);
        // END: comment-pi-handling

        Assertions.assertNotNull(config);
        Assertions.assertNotNull(minimal);
    }

    @Test
    public void demonstrateLineEndingConfiguration() {
        // START: line-ending-configuration
        // Use Unix line endings
        DomTripConfig unix = DomTripConfig.defaults().withLineEnding("\n");

        // Use Windows line endings
        DomTripConfig windows = DomTripConfig.defaults().withLineEnding("\r\n");

        // Use Mac line endings
        DomTripConfig mac = DomTripConfig.defaults().withLineEnding("\r");
        // END: line-ending-configuration

        Assertions.assertNotNull(unix);
        Assertions.assertNotNull(windows);
        Assertions.assertNotNull(mac);
    }

    @Test
    public void demonstrateXmlDeclarationHandling() {
        // START: xml-declaration-handling
        // Include XML declaration (default)
        DomTripConfig withDeclaration = DomTripConfig.defaults().withXmlDeclaration(true);

        // Omit XML declaration for minimal output
        DomTripConfig withoutDeclaration = DomTripConfig.defaults().withXmlDeclaration(false);
        // END: xml-declaration-handling

        Assertions.assertNotNull(withDeclaration);
        Assertions.assertNotNull(withoutDeclaration);
    }

    @Test
    public void demonstrateCompleteConfiguration() {
        // START: complete-configuration
        DomTripConfig config = DomTripConfig.defaults()
                // Whitespace settings
                .withIndentString("  ") // 2-space indentation
                .withWhitespacePreservation(true) // Keep original whitespace
                .withPrettyPrint(false) // No reformatting

                // Content preservation
                .withCommentPreservation(true) // Keep comments
                .withProcessingInstructionPreservation(true) // Keep PIs

                // Quote and declaration settings
                .withDefaultQuoteStyle(QuoteStyle.DOUBLE) // Double quotes for new attrs
                .withXmlDeclaration(true) // Include XML declaration

                // Line ending settings
                .withLineEnding("\n"); // Unix line endings
        // END: complete-configuration

        Assertions.assertNotNull(config);
    }

    @Test
    public void demonstrateConfigurationPatterns() {
        // START: configuration-patterns
        // Development configuration - readable output
        DomTripConfig development =
                DomTripConfig.prettyPrint().withIndentString("  ").withCommentPreservation(true);

        // Production configuration - preserve everything
        DomTripConfig production =
                DomTripConfig.defaults().withWhitespacePreservation(true).withCommentPreservation(true);

        // API response configuration - minimal output
        DomTripConfig api = DomTripConfig.minimal().withXmlDeclaration(false);
        // END: configuration-patterns

        Assertions.assertNotNull(development);
        Assertions.assertNotNull(production);
        Assertions.assertNotNull(api);
    }

    @Test
    public void demonstrateEnvironmentSpecificConfigurations() {
        // START: environment-specific-configurations
        // Different configurations for different environments
        DomTripConfig devConfig = forDevelopment();
        DomTripConfig prodConfig = forProduction();
        DomTripConfig testConfig = forTesting();

        // Usage
        String xml = "<root><child>value</child></root>";
        Editor editor = new Editor(Document.of(xml), prodConfig);
        // END: environment-specific-configurations

        Assertions.assertNotNull(devConfig);
        Assertions.assertNotNull(prodConfig);
        Assertions.assertNotNull(testConfig);
        Assertions.assertNotNull(editor);
    }

    @Test
    public void demonstrateConfigurationBestPractices() {
        // START: configuration-best-practices
        // ✅ Good - start with appropriate preset
        DomTripConfig config = DomTripConfig.defaults() // For config files
                .withIndentString("  "); // Customize as needed

        // ✅ Good - use minimal for APIs
        DomTripConfig apiConfig = DomTripConfig.minimal(); // Compact output

        // ✅ Good - document why you chose specific settings
        DomTripConfig documented = DomTripConfig.defaults()
                .withIndentString("  ") // Match project style
                .withDefaultQuoteStyle(QuoteStyle.DOUBLE) // Consistent with JSON
                .withCommentPreservation(true); // Keep documentation

        // ✅ Good - different configs for different needs
        boolean isProduction = false; // Example condition
        DomTripConfig conditional = isProduction
                ? DomTripConfig.defaults() // Preserve everything
                : DomTripConfig.prettyPrint(); // Readable for debugging
        // END: configuration-best-practices

        Assertions.assertNotNull(config);
        Assertions.assertNotNull(apiConfig);
        Assertions.assertNotNull(documented);
        Assertions.assertNotNull(conditional);
    }

    @Test
    public void demonstrateAvailableConfigurationMethods() {
        // START: available-configuration-methods
        DomTripConfig config = DomTripConfig.defaults()
                // Whitespace control
                .withWhitespacePreservation(true) // Preserve original whitespace
                .withPrettyPrint(false) // Enable pretty printing
                .withIndentString("  ") // Set indentation string
                .withLineEnding("\n") // Set line ending style

                // Content preservation
                .withCommentPreservation(true) // Preserve comments
                .withProcessingInstructionPreservation(true) // Preserve PIs
                .withXmlDeclaration(true) // Include XML declaration

                // Attribute formatting
                .withDefaultQuoteStyle(QuoteStyle.DOUBLE); // Default quote style for new attributes
        // END: available-configuration-methods

        Assertions.assertNotNull(config);
    }

    // Helper methods for environment-specific configurations example
    private static DomTripConfig forDevelopment() {
        return DomTripConfig.prettyPrint().withIndentString("  ").withCommentPreservation(true);
    }

    private static DomTripConfig forProduction() {
        return DomTripConfig.defaults().withWhitespacePreservation(true).withCommentPreservation(true);
    }

    private static DomTripConfig forTesting() {
        return DomTripConfig.minimal().withCommentPreservation(false).withXmlDeclaration(false);
    }
}
