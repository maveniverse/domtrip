package eu.maveniverse.domtrip;

import java.util.regex.Pattern;

/**
 * Utility class for managing whitespace in XML documents.
 */
public class WhitespaceManager {

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern INDENT_PATTERN = Pattern.compile("\\n(\\s*)");

    private final DomTripConfig config;

    public WhitespaceManager(DomTripConfig config) {
        this.config = config;
    }

    /**
     * Infers the indentation pattern from the context of a node.
     */
    public String inferIndentation(Node context) {
        if (context == null) {
            return config.indentString();
        }

        // First, look for indentation patterns in the node's children
        if (context instanceof ContainerNode container) {
            for (Node child : container.nodes) {
                // Check preceding whitespace
                String precedingWs = child.precedingWhitespace();
                if (precedingWs != null && precedingWs.contains("\n")) {
                    // Extract indentation after the last newline
                    int lastNewline = precedingWs.lastIndexOf('\n');
                    if (lastNewline >= 0 && lastNewline < precedingWs.length() - 1) {
                        return precedingWs.substring(lastNewline + 1);
                    }
                }

                // Also check if this is a whitespace-only text node
                if (child instanceof Text textNode && isWhitespaceOnly(textNode.content())) {
                    String content = textNode.content();
                    if (content.contains("\n")) {
                        // Extract indentation after the last newline
                        int lastNewline = content.lastIndexOf('\n');
                        if (lastNewline >= 0 && lastNewline < content.length() - 1) {
                            return content.substring(lastNewline + 1);
                        }
                    }
                }
            }
        }

        // If no children, look at siblings (for when context is a child node)
        Node parent = context.parent();
        if (parent instanceof ContainerNode parentContainer) {
            for (Node sibling : parentContainer.nodes) {
                String precedingWs = sibling.precedingWhitespace();
                if (precedingWs != null && precedingWs.contains("\n")) {
                    // Extract indentation after the last newline
                    int lastNewline = precedingWs.lastIndexOf('\n');
                    if (lastNewline >= 0 && lastNewline < precedingWs.length() - 1) {
                        return precedingWs.substring(lastNewline + 1);
                    }
                }

                // Also check if this is a whitespace-only text node
                if (sibling instanceof Text textNode && isWhitespaceOnly(textNode.content())) {
                    String content = textNode.content();
                    if (content.contains("\n")) {
                        // Extract indentation after the last newline
                        int lastNewline = content.lastIndexOf('\n');
                        if (lastNewline >= 0 && lastNewline < content.length() - 1) {
                            return content.substring(lastNewline + 1);
                        }
                    }
                }
            }
        }

        // Fallback to configured indent string
        return config.indentString();
    }

    /**
     * Normalizes whitespace according to configuration.
     */
    public String normalizeWhitespace(String content) {
        if (content == null) {
            return "";
        }

        if (!config.isPreserveWhitespace()) {
            return WHITESPACE_PATTERN.matcher(content.trim()).replaceAll(" ");
        }

        return content;
    }

    /**
     * Checks if the given content contains only whitespace.
     */
    public boolean isWhitespaceOnly(String content) {
        return content == null || content.trim().isEmpty();
    }

    /**
     * Creates appropriate indentation for a given depth level.
     */
    public String createIndentation(int depth) {
        if (depth <= 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append(config.indentString());
        }
        return sb.toString();
    }

    /**
     * Calculates the depth level of indentation in the given whitespace.
     */
    public int calculateDepth(String whitespace) {
        if (whitespace == null || !whitespace.contains("\n")) {
            return 0;
        }

        int lastNewline = whitespace.lastIndexOf('\n');
        if (lastNewline >= 0 && lastNewline < whitespace.length() - 1) {
            String indent = whitespace.substring(lastNewline + 1);
            return indent.length() / config.indentString().length();
        }

        return 0;
    }

    /**
     * Adds appropriate preceding whitespace for a new node.
     */
    public String createPrecedingWhitespace(Node parent, int siblingIndex) {
        if (parent == null) {
            return "";
        }

        String baseIndent = inferIndentation(parent);

        // If this is the first child, add newline + indent
        if (siblingIndex == 0) {
            return "\n" + baseIndent + config.indentString();
        }

        // For subsequent children, use the same pattern as siblings
        return "\n" + baseIndent + config.indentString();
    }

    /**
     * Trims whitespace while preserving structure.
     */
    public String trimPreservingStructure(String content) {
        if (content == null) {
            return "";
        }

        if (config.isPreserveWhitespace()) {
            return content;
        }

        // Preserve leading/trailing newlines but trim spaces
        boolean startsWithNewline = content.startsWith("\n");
        boolean endsWithNewline = content.endsWith("\n");

        String trimmed = content.trim();

        StringBuilder result = new StringBuilder();
        if (startsWithNewline) {
            result.append("\n");
        }
        result.append(trimmed);
        if (endsWithNewline) {
            result.append("\n");
        }

        return result.toString();
    }
}
