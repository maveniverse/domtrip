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
 * // Create using factory method
 * Comment factoryComment = Comment.of("This is a factory comment");
 *
 * // Create and modify with fluent API
 * Comment fluentComment = Comment.of("Initial content")
 *     .content("Updated content");
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
 * }</pre>
 *
 * <h3>Best Practices:</h3>
 * <ul>
 *   <li>Avoid using "--" within comment content</li>
 *   <li>Use comments for documentation and metadata</li>
 *   <li>Consider comment placement for readability</li>
 * </ul>
 *
 * @see Node
 * @see ProcessingInstruction
 */
public class Comment extends Node {

    private String content;

    /**
     * Creates a new XML comment with the specified content.
     *
     * @param content the comment content (without the <!-- --> delimiters)
     */
    public Comment(String content) {
        super();
        this.content = content != null ? content : "";
    }

    /**
     * Returns the node type for this comment.
     *
     * @return {@link NodeType#COMMENT}
     */
    @Override
    public NodeType type() {
        return NodeType.COMMENT;
    }

    /**
     * Gets the content of this comment.
     *
     * <p>Returns the text content without the surrounding comment delimiters.</p>
     *
     * @return the comment content
     * @see #content(String)
     */
    public String content() {
        return content;
    }

    /**
     * Sets the content of this comment.
     *
     * <p>Setting the content marks this comment as modified. The content
     * should not include the comment delimiters (<!-- -->).</p>
     *
     * @param content the new comment content, or null for empty content
     * @see #content()
     */
    public Comment content(String content) {
        this.content = content != null ? content : "";
        markModified();
        return this;
    }

    /**
     * Serializes this comment to an XML string.
     *
     * <p>Creates the complete XML comment including the delimiters and
     * any surrounding whitespace.</p>
     *
     * @return the XML string representation of this comment
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
     */
    @Override
    public String toString() {
        String displayContent = content.length() > 50 ? content.substring(0, 47) + "..." : content;
        return "Comment{content='" + displayContent.replace("\n", "\\n") + "'}";
    }

    /**
     * Creates a comment with the specified content.
     *
     * <p>Factory method following modern Java naming conventions.</p>
     *
     * @param content the comment content
     * @return a new Comment
     */
    public static Comment of(String content) {
        return new Comment(content);
    }
}
