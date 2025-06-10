package eu.maveniverse.domtrip;

/**
 * Represents text content in XML documents, preserving exact whitespace,
 * entity encoding, and CDATA section formatting.
 *
 * <p>The Text class handles all forms of textual content in XML documents,
 * including regular text nodes, CDATA sections, and whitespace-only content.
 * It maintains both the decoded text content and the original raw content
 * with entities preserved, enabling lossless round-trip processing.</p>
 *
 * <h3>Text Handling:</h3>
 * <ul>
 *   <li><strong>Entity Preservation</strong> - Maintains original entity encoding</li>
 *   <li><strong>CDATA Support</strong> - Handles CDATA sections</li>
 *   <li><strong>Whitespace Preservation</strong> - Preserves significant whitespace</li>
 *   <li><strong>Content Normalization</strong> - Optional whitespace normalization</li>
 * </ul>
 *
 * <h3>Content Types:</h3>
 * <ul>
 *   <li><strong>Regular Text</strong> - Standard text content with entity escaping</li>
 *   <li><strong>CDATA Sections</strong> - Unescaped text within CDATA blocks</li>
 *   <li><strong>Whitespace</strong> - Formatting whitespace between elements</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Create regular text content
 * Text text = new Text("Hello & welcome!");
 *
 * // Create CDATA section
 * Text cdata = new Text("<script>alert('test');</script>", true);
 *
 * // Check content properties
 * if (text.isWhitespaceOnly()) {
 *     // Handle formatting whitespace
 * }
 *
 * // Access both decoded and raw content
 * String content = text.content();     // "Hello & welcome!"
 * String raw = text.getRawContent();      // "Hello &amp; welcome!" (if preserved)
 *
 * // Use builder pattern
 * Text builderText = Text.builder()
 *     .withContent("Hello World")
 *     .build();
 *
 * Text cdataText = Text.builder()
 *     .withContent("<script>alert('test');</script>")
 *     .asCData()
 *     .build();
 * }</pre>
 *
 * <h3>Entity Handling:</h3>
 * <p>Text nodes automatically handle XML entity encoding and decoding:</p>
 * <ul>
 *   <li>{@code &amp;} ↔ {@code &}</li>
 *   <li>{@code &lt;} ↔ {@code <}</li>
 *   <li>{@code &gt;} ↔ {@code >}</li>
 *   <li>{@code &quot;} ↔ {@code "}</li>
 *   <li>{@code &apos;} ↔ {@code '}</li>
 * </ul>
 *
 * @see Node
 * @see Element
 * @see Comment
 */
public class Text extends Node {

    private String content;
    private String rawContent; // Original content with entities preserved
    private boolean isCData;
    private boolean preserveWhitespace;

    public Text(String content) {
        super();
        this.content = content != null ? content : "";
        this.rawContent = null; // Will be set by parser if needed
        this.isCData = false;
        this.preserveWhitespace = true;
    }

    public Text(String content, boolean isCData) {
        this(content);
        this.isCData = isCData;
    }

    public Text(String content, String rawContent) {
        this(content);
        this.rawContent = rawContent;
    }

    @Override
    public NodeType type() {
        return NodeType.TEXT;
    }

    public String content() {
        return content;
    }

    public void content(String content) {
        this.content = content != null ? content : "";
        this.rawContent = null; // Clear raw content when content is modified
        markModified();
    }

    public String rawContent() {
        return rawContent;
    }

    public void rawContent(String rawContent) {
        this.rawContent = rawContent;
    }

    public boolean cdata() {
        return isCData;
    }

    public void cdata(boolean cData) {
        this.isCData = cData;
        markModified();
    }

    public boolean preserveWhitespace() {
        return preserveWhitespace;
    }

    public void preserveWhitespace(boolean preserveWhitespace) {
        this.preserveWhitespace = preserveWhitespace;
    }

    /**
     * Returns true if this text node contains only whitespace
     */
    public boolean isWhitespaceOnly() {
        return content.trim().isEmpty();
    }

    /**
     * Returns true if this text node is empty
     */
    public boolean isEmpty() {
        return content.isEmpty();
    }

    /**
     * Gets the text content with leading and trailing whitespace removed.
     *
     * <p>This is a convenience method that returns the trimmed content without
     * modifying the original content. The original content with whitespace
     * is preserved for lossless round-trip processing.</p>
     *
     * @return the content with leading and trailing whitespace removed
     * @see #content()
     * @see #leadingWhitespace()
     * @see #trailingWhitespace()
     */
    public String trimmedContent() {
        return content.trim();
    }

    /**
     * Gets the leading whitespace from the text content.
     *
     * <p>Extracts and returns any whitespace characters (spaces, tabs, newlines)
     * that appear at the beginning of the content. This is useful for understanding
     * the whitespace structure without modifying the original content.</p>
     *
     * @return the leading whitespace, or empty string if none
     * @see #trailingWhitespace()
     * @see #trimmedContent()
     */
    public String leadingWhitespace() {
        if (content.isEmpty()) {
            return "";
        }

        int start = 0;
        while (start < content.length() && Character.isWhitespace(content.charAt(start))) {
            start++;
        }

        return content.substring(0, start);
    }

    /**
     * Gets the trailing whitespace from the text content.
     *
     * <p>Extracts and returns any whitespace characters (spaces, tabs, newlines)
     * that appear at the end of the content. This is useful for understanding
     * the whitespace structure without modifying the original content.</p>
     *
     * @return the trailing whitespace, or empty string if none
     * @see #leadingWhitespace()
     * @see #trimmedContent()
     */
    public String trailingWhitespace() {
        if (content.isEmpty()) {
            return "";
        }

        int end = content.length();
        while (end > 0 && Character.isWhitespace(content.charAt(end - 1))) {
            end--;
        }

        return content.substring(end);
    }

    /**
     * Sets new content while preserving the existing whitespace pattern.
     *
     * <p>This method replaces the actual content but maintains the same leading
     * and trailing whitespace as the original content. This is useful when you
     * want to update the meaningful content without affecting the formatting.</p>
     *
     * <h3>Example:</h3>
     * <pre>{@code
     * // Original: "   Hello World   "
     * text.contentPreservingWhitespace("Goodbye");
     * // Result:   "   Goodbye   "
     * }</pre>
     *
     * @param newContent the new content to set (will be placed between existing whitespace)
     * @see #content(String)
     * @see #leadingWhitespace()
     * @see #trailingWhitespace()
     */
    public void contentPreservingWhitespace(String newContent) {
        if (newContent == null) {
            newContent = "";
        }

        String leading = leadingWhitespace();
        String trailing = trailingWhitespace();

        this.content = leading + newContent + trailing;
        this.rawContent = null; // Clear raw content when content is modified
        markModified();
    }

    /**
     * Checks if the content has leading whitespace.
     *
     * @return true if the content starts with whitespace characters
     * @see #hasTrailingWhitespace()
     * @see #leadingWhitespace()
     */
    public boolean hasLeadingWhitespace() {
        return !content.isEmpty() && Character.isWhitespace(content.charAt(0));
    }

    /**
     * Checks if the content has trailing whitespace.
     *
     * @return true if the content ends with whitespace characters
     * @see #hasLeadingWhitespace()
     * @see #trailingWhitespace()
     */
    public boolean hasTrailingWhitespace() {
        return !content.isEmpty() && Character.isWhitespace(content.charAt(content.length() - 1));
    }

    @Override
    public String toXml() {
        StringBuilder sb = new StringBuilder();
        toXml(sb);
        return sb.toString();
    }

    @Override
    public void toXml(StringBuilder sb) {
        sb.append(precedingWhitespace);

        if (isCData) {
            sb.append("<![CDATA[").append(content).append("]]>");
        } else {
            // Use raw content if available and not modified, otherwise escape current content
            if (rawContent != null && !isModified()) {
                sb.append(rawContent);
            } else {
                sb.append(escapeTextContent(content));
            }
        }

        sb.append(followingWhitespace);
    }

    /**
     * Escapes special characters in text content
     */
    private String escapeTextContent(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /**
     * Unescapes XML entities in text content
     */
    public static String unescapeTextContent(String text) {
        if (text == null) return "";
        return text.replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .replace("&amp;", "&"); // This must be last
    }

    /**
     * Trims whitespace from the content while preserving internal structure
     */
    public void trim() {
        if (!preserveWhitespace) {
            content = content.trim();
            markModified();
        }
    }

    /**
     * Normalizes whitespace in the content (collapses multiple spaces to single space)
     */
    public void normalizeWhitespace() {
        if (!preserveWhitespace) {
            content = content.replaceAll("\\s+", " ").trim();
            markModified();
        }
    }

    @Override
    public String toString() {
        String displayContent = content.length() > 50 ? content.substring(0, 47) + "..." : content;
        return "Text{content='" + displayContent.replace("\n", "\\n") + "', isCData=" + isCData + "}";
    }

    /**
     * Builder for creating Text instances with fluent API.
     *
     * <p>The Text.Builder provides a convenient way to construct XML text nodes
     * with proper content handling and CDATA support.</p>
     *
     * <h3>Usage Examples:</h3>
     * <pre>{@code
     * // Simple text content
     * Text text = Text.builder()
     *     .withContent("Hello World")
     *     .build();
     *
     * // CDATA section
     * Text cdata = Text.builder()
     *     .withContent("<script>alert('test');</script>")
     *     .asCData()
     *     .build();
     *
     * // Text with whitespace preservation disabled
     * Text normalized = Text.builder()
     *     .withContent("  Multiple   spaces  ")
     *     .withPreserveWhitespace(false)
     *     .build();
     * }</pre>
     *
     */
    public static class Builder {
        private String content = "";
        private boolean isCData = false;
        private boolean preserveWhitespace = true;

        private Builder() {}

        /**
         * Sets the text content.
         *
         * @param content the text content
         * @return this builder for method chaining
         */
        public Builder withContent(String content) {
            this.content = content != null ? content : "";
            return this;
        }

        /**
         * Makes this text node a CDATA section.
         *
         * @return this builder for method chaining
         */
        public Builder asCData() {
            this.isCData = true;
            return this;
        }

        /**
         * Sets whether to preserve whitespace in the content.
         *
         * @param preserveWhitespace true to preserve whitespace, false otherwise
         * @return this builder for method chaining
         */
        public Builder withPreserveWhitespace(boolean preserveWhitespace) {
            this.preserveWhitespace = preserveWhitespace;
            return this;
        }

        /**
         * Builds and returns the configured Text instance.
         *
         * @return the constructed Text
         */
        public Text build() {
            Text text = new Text(content, isCData);
            text.preserveWhitespace(preserveWhitespace);
            return text;
        }

        /**
         * Builds the text node and adds it to the specified parent.
         *
         * <p>This method creates the text node and adds it directly to the parent
         * container. Text nodes typically don't need special whitespace management
         * as they are content nodes.</p>
         *
         * @param editor the Editor instance (for consistency, though not used for text)
         * @param parent the parent container to add this text to
         * @return the constructed and added Text
         * @throws IllegalArgumentException if editor or parent is null
         */
        public Text buildAndAddTo(Editor editor, ContainerNode parent) {
            if (editor == null) {
                throw new IllegalArgumentException("Editor cannot be null");
            }
            if (parent == null) {
                throw new IllegalArgumentException("Parent cannot be null");
            }

            Text builtText = build();
            parent.addChild(builtText);
            return builtText;
        }
    }

    /**
     * Creates a new Text builder instance.
     *
     * @return a new Text.Builder for fluent text construction
     */
    public static Builder builder() {
        return new Builder();
    }
}
