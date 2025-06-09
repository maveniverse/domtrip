package eu.maveniverse.domtrip;

/**
 * Represents an XML comment node, preserving exact formatting and content.
 *
 * <p>XML comments are used to include human-readable notes and documentation
 * within XML documents. DomTrip preserves comments exactly as they appear in
 * the source XML, including any internal formatting, whitespace, and content.</p>
 *
 * <h3>Comment Handling:</h3>
 * <ul>
 *   <li><strong>Content Preservation</strong> - Maintains exact comment text</li>
 *   <li><strong>Position Preservation</strong> - Keeps comments in their original locations</li>
 *   <li><strong>Whitespace Preservation</strong> - Preserves surrounding whitespace</li>
 * </ul>
 *
 * <h3>XML Comment Syntax:</h3>
 * <p>XML comments follow the syntax: {@code <!-- comment content -->}</p>
 * <p>Comments cannot contain the string "--" and cannot end with "-".</p>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Create a comment
 * Comment comment = new Comment("This is a comment");
 *
 * // Add to document
 * document.addChild(comment);
 *
 * // Check comment properties
 * if (comment.isEmpty()) {
 *     // Handle empty comment
 * }
 *
 * if (comment.isWhitespaceOnly()) {
 *     // Handle whitespace-only comment
 * }
 *
 * // Use builder pattern
 * Comment builderComment = Comment.builder()
 *     .withContent("This is a builder comment")
 *     .build();
 * }</pre>
 *
 * <h3>Best Practices:</h3>
 * <ul>
 *   <li>Avoid using "--" within comment content</li>
 *   <li>Use comments for documentation and metadata</li>
 *   <li>Consider comment placement for readability</li>
 * </ul>
 *
 * @author DomTrip Development Team
 * @since 1.0
 * @see Node
 * @see ProcessingInstruction
 */
public class Comment extends Node {

    private String content;

    /**
     * Creates a new XML comment with the specified content.
     *
     * @param content the comment content (without the <!-- --> delimiters)
     * @since 1.0
     */
    public Comment(String content) {
        super();
        this.content = content != null ? content : "";
    }

    /**
     * Returns the node type for this comment.
     *
     * @return {@link NodeType#COMMENT}
     * @since 1.0
     */
    @Override
    public NodeType getNodeType() {
        return NodeType.COMMENT;
    }

    /**
     * Gets the content of this comment.
     *
     * <p>Returns the text content without the surrounding comment delimiters.</p>
     *
     * @return the comment content
     * @since 1.0
     * @see #setContent(String)
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the content of this comment.
     *
     * <p>Setting the content marks this comment as modified. The content
     * should not include the comment delimiters (<!-- -->).</p>
     *
     * @param content the new comment content, or null for empty content
     * @since 1.0
     * @see #getContent()
     */
    public void setContent(String content) {
        this.content = content != null ? content : "";
        markModified();
    }

    /**
     * Serializes this comment to an XML string.
     *
     * <p>Creates the complete XML comment including the delimiters and
     * any surrounding whitespace.</p>
     *
     * @return the XML string representation of this comment
     * @since 1.0
     * @see #toXml(StringBuilder)
     */
    @Override
    public String toXml() {
        StringBuilder sb = new StringBuilder();
        toXml(sb);
        return sb.toString();
    }

    /**
     * Serializes this comment to XML, appending to the provided StringBuilder.
     *
     * <p>Appends the complete comment including preceding whitespace,
     * comment delimiters, content, and following whitespace.</p>
     *
     * @param sb the StringBuilder to append the XML content to
     * @since 1.0
     * @see #toXml()
     */
    @Override
    public void toXml(StringBuilder sb) {
        sb.append(precedingWhitespace);
        sb.append("<!--").append(content).append("-->");
        sb.append(followingWhitespace);
    }

    /**
     * Checks if this comment contains only whitespace characters.
     *
     * <p>Returns true if the comment content consists entirely of
     * whitespace characters (spaces, tabs, newlines, etc.).</p>
     *
     * @return true if the comment contains only whitespace, false otherwise
     * @since 1.0
     * @see #isEmpty()
     */
    public boolean isWhitespaceOnly() {
        return content.trim().isEmpty();
    }

    /**
     * Checks if this comment is completely empty.
     *
     * <p>Returns true if the comment has no content at all.</p>
     *
     * @return true if the comment is empty, false otherwise
     * @since 1.0
     * @see #isWhitespaceOnly()
     */
    public boolean isEmpty() {
        return content.isEmpty();
    }

    /**
     * Returns a string representation of this comment for debugging purposes.
     *
     * <p>The string includes the comment content, truncated if longer than
     * 50 characters, with newlines escaped for readability.</p>
     *
     * @return a string representation of this comment
     * @since 1.0
     */
    @Override
    public String toString() {
        String displayContent = content.length() > 50 ? content.substring(0, 47) + "..." : content;
        return "Comment{content='" + displayContent.replace("\n", "\\n") + "'}";
    }

    /**
     * Builder for creating Comment instances with fluent API.
     *
     * <p>The Comment.Builder provides a convenient way to construct XML comments
     * with proper content handling.</p>
     *
     * <h3>Usage Examples:</h3>
     * <pre>{@code
     * // Simple comment
     * Comment comment = Comment.builder()
     *     .withContent("This is a comment")
     *     .build();
     *
     * // Multi-line comment
     * Comment multiLine = Comment.builder()
     *     .withContent("Line 1\nLine 2\nLine 3")
     *     .build();
     * }</pre>
     *
     * @since 1.0
     */
    public static class Builder {
        private String content = "";

        private Builder() {}

        /**
         * Sets the content of the comment.
         *
         * @param content the comment content
         * @return this builder for method chaining
         * @since 1.0
         */
        public Builder withContent(String content) {
            this.content = content != null ? content : "";
            return this;
        }

        /**
         * Builds and returns the configured Comment instance.
         *
         * @return the constructed Comment
         * @since 1.0
         */
        public Comment build() {
            return new Comment(content);
        }

        /**
         * Builds the comment and adds it to the specified parent using Editor's whitespace management.
         *
         * <p>This method integrates with the Editor's whitespace management to properly
         * format the comment when adding it to the document tree.</p>
         *
         * @param editor the Editor instance for whitespace management
         * @param parent the parent container to add this comment to
         * @return the constructed and added Comment
         * @throws IllegalArgumentException if editor or parent is null
         * @since 1.0
         */
        public Comment buildAndAddTo(Editor editor, ContainerNode parent) {
            if (editor == null) {
                throw new IllegalArgumentException("Editor cannot be null");
            }
            if (parent == null) {
                throw new IllegalArgumentException("Parent cannot be null");
            }

            Comment builtComment = build();

            // Use Editor's whitespace management
            String indentation = editor.getWhitespaceManager().inferIndentation(parent);
            if (!indentation.isEmpty()) {
                builtComment.setPrecedingWhitespace("\n" + indentation);
            }

            parent.addChild(builtComment);
            return builtComment;
        }
    }

    /**
     * Creates a new Comment builder instance.
     *
     * @return a new Comment.Builder for fluent comment construction
     * @since 1.0
     */
    public static Builder builder() {
        return new Builder();
    }
}
