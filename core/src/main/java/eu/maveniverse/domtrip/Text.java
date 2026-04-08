/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
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
 * // Create using factory methods
 * Text factoryText = Text.of("Hello World");
 * Text cdataText = Text.cdata("<script>alert('test');</script>");
 * Text explicitCdata = Text.of("content", true);
 *
 * // Create and modify with fluent API
 * Text fluentText = Text.of("Regular text").asCData();
 * Text preservedText = Text.of("  spaces  ").preserveWhitespace(false);
 *
 * // Check content properties
 * if (text.isWhitespaceOnly()) {
 *     // Handle formatting whitespace
 * }
 *
 * // Access both decoded and raw content
 * String content = text.content();     // "Hello & welcome!"
 * String raw = text.getRawContent();      // "Hello &amp; welcome!" (if preserved)
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

    /**
     * Private copy constructor for cloning.
     *
     * @param original the text node to copy from
     */
    private Text(Text original) {
        super();
        this.content = original.content;
        this.rawContent = original.rawContent;
        this.isCData = original.isCData;
        this.preserveWhitespace = original.preserveWhitespace;

        // Copy inherited Node properties
        this.precedingWhitespace = original.precedingWhitespace;

        // Note: parent is intentionally not copied - clone has no parent
        // Note: modified flag is not copied - clone starts as unmodified
    }

    @Override
    public NodeType type() {
        return NodeType.TEXT;
    }

    public String content() {
        return content;
    }
    /**
     * {@inheritDoc}
     *
     * @return this text node for method chaining
     */
    @Override
    public Text parent(ContainerNode parent) {
        this.parent = parent;
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return this text node for method chaining
     */
    @Override
    public Text precedingWhitespace(String whitespace) {
        this.precedingWhitespace = whitespace != null ? whitespace : "";
        markModified();
        return this;
    }

    public Text content(String content) {
        this.content = content != null ? content : "";
        this.rawContent = null; // Clear raw content when content is modified
        markModified();
        return this;
    }

    public String rawContent() {
        return rawContent;
    }

    public Text rawContent(String rawContent) {
        this.rawContent = rawContent;
        return this;
    }

    public boolean cdata() {
        return isCData;
    }

    public Text cdata(boolean cData) {
        this.isCData = cData;
        markModified();
        return this;
    }

    public boolean preserveWhitespace() {
        return preserveWhitespace;
    }

    public Text preserveWhitespace(boolean preserveWhitespace) {
        this.preserveWhitespace = preserveWhitespace;
        return this;
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
    public Text contentPreservingWhitespace(String newContent) {
        if (newContent == null) {
            newContent = "";
        }

        String leading = leadingWhitespace();
        String trailing = trailingWhitespace();

        this.content = leading + newContent + trailing;
        this.rawContent = null; // Clear raw content when content is modified
        markModified();
        return this;
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
    }

    /**
     * Escapes special characters in text content
     */
    private String escapeTextContent(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /**
     * Unescapes XML entities in text content, including numeric character references
     */
    public static String unescapeTextContent(String text) {
        if (text == null) return "";

        // Single-pass scanner: resolve numeric references and named entities in one pass
        // to avoid re-decoding (e.g., &#38;lt; must yield "&lt;", not "<")
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            if (text.charAt(i) == '&') {
                // Try numeric reference first
                if (i + 2 < text.length() && text.charAt(i + 1) == '#') {
                    int consumed = tryResolveNumericReference(text, i, result);
                    if (consumed > 0) {
                        i += consumed;
                        continue;
                    }
                }
                // Try named entities
                int consumed = tryResolveNamedEntity(text, i, result);
                if (consumed > 0) {
                    i += consumed;
                    continue;
                }
            }
            result.append(text.charAt(i));
            i++;
        }
        return result.toString();
    }

    /**
     * Tries to resolve a named XML entity (&amp;lt;, &amp;gt;, &amp;quot;, &amp;apos;, &amp;amp;) at the given position.
     * Returns the number of characters consumed, or 0 if no entity matched.
     */
    private static int tryResolveNamedEntity(String text, int start, StringBuilder result) {
        if (text.startsWith("&lt;", start)) {
            result.append('<');
            return 4;
        }
        if (text.startsWith("&gt;", start)) {
            result.append('>');
            return 4;
        }
        if (text.startsWith("&quot;", start)) {
            result.append('"');
            return 6;
        }
        if (text.startsWith("&apos;", start)) {
            result.append('\'');
            return 6;
        }
        if (text.startsWith("&amp;", start)) {
            result.append('&');
            return 5;
        }
        return 0;
    }

    /**
     * Tries to resolve a single numeric character reference starting at the given position.
     * Returns the number of characters consumed, or 0 if it was not a valid reference.
     */
    private static int tryResolveNumericReference(String text, int start, StringBuilder result) {
        int end = text.indexOf(';', start + 2);
        if (end == -1) {
            return 0;
        }
        String numericPart = text.substring(start + 2, end);
        try {
            int codePoint = parseNumericReference(numericPart);
            if (isValidXmlChar(codePoint)) {
                result.appendCodePoint(codePoint);
                return end - start + 1;
            }
        } catch (NumberFormatException e) {
            // Not a valid numeric reference, treat as regular text
        }
        return 0;
    }

    /**
     * Parses a numeric character reference value (decimal or hexadecimal).
     */
    private static int parseNumericReference(String numericPart) {
        if (!numericPart.isEmpty() && (numericPart.charAt(0) == 'x' || numericPart.charAt(0) == 'X')) {
            return Integer.parseInt(numericPart.substring(1), 16);
        }
        return Integer.parseInt(numericPart, 10);
    }

    /**
     * Checks whether a code point is a valid XML character per the XML 1.0 spec:
     * #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
     */
    private static boolean isValidXmlChar(int cp) {
        return cp == 0x9
                || cp == 0xA
                || cp == 0xD
                || (cp >= 0x20 && cp <= 0xD7FF)
                || (cp >= 0xE000 && cp <= 0xFFFD)
                || (cp >= 0x10000 && cp <= 0x10FFFF);
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

    /**
     * {@inheritDoc}
     * @since 1.1.0
     */
    @Override
    public Text copy() {
        return new Text(this);
    }

    /**
     * Creates a deep copy of this text node.
     *
     * @return a new text node that is a copy of this text node
     * @deprecated Use {@link #copy()} instead.
     */
    @Deprecated
    @SuppressWarnings({"java:S2975", "java:S1133"})
    @Override
    public Text clone() {
        return copy();
    }

    @Override
    public String toString() {
        String displayContent = content.length() > 50 ? content.substring(0, 47) + "..." : content;
        return "Text{content='" + displayContent.replace("\n", "\\n") + "', isCData=" + isCData + "}";
    }

    /**
     * Creates a text node with the specified content.
     *
     * <p>Factory method following modern Java naming conventions.</p>
     *
     * @param content the text content
     * @return a new Text node
     */
    public static Text of(String content) {
        return new Text(content);
    }

    /**
     * Creates a CDATA text node with the specified content.
     *
     * <p>Factory method for creating CDATA sections.</p>
     *
     * @param content the CDATA content
     * @return a new Text node with CDATA flag set
     */
    public static Text cdata(String content) {
        return new Text(content, true);
    }

    /**
     * Creates a text node with the specified content and CDATA flag.
     *
     * <p>Factory method for creating text nodes with explicit CDATA control.</p>
     *
     * @param content the text content
     * @param isCData true to create a CDATA section, false for regular text
     * @return a new Text node
     */
    public static Text of(String content, boolean isCData) {
        return new Text(content, isCData);
    }

    /**
     * Converts this text node to a CDATA section.
     *
     * <p>Fluent setter for converting existing text to CDATA format.</p>
     *
     * @return this text node for method chaining
     */
    public Text asCData() {
        this.isCData = true;
        markModified();
        return this;
    }
}
