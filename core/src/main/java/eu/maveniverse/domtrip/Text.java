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
        return normalizeLineEndings(content);
    }

    /**
     * Returns the content without line ending normalization, for serialization.
     * Package-private so Serializer can preserve original line endings during output.
     */
    String serializationContent() {
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
     * {@inheritDoc}
     * @since 1.3.0
     */
    @Override
    public DomTripVisitor.Action accept(DomTripVisitor visitor) {
        if (visitor == null) {
            throw new IllegalArgumentException("Visitor cannot be null");
        }
        return visitor.visitText(this);
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

    /**
     * Appends this text node's XML representation to the provided StringBuilder.
     *
     * The method writes the node's preceding whitespace, then either:
     * - serializes the content as a CDATA section when this node is marked as CDATA, or
     * - appends the raw, unescaped content if available and the node has not been modified,
     *   otherwise appends the escaped text content.
     *
     * @param sb the StringBuilder to append XML output to
     */
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
     * Escape XML special characters in the given text for safe inclusion in element content.
     *
     * @param text the input text to escape; may be {@code null} (treated as an empty string)
     * @return the input with `&`, `<`, and `>` replaced by `&amp;`, `&lt;`, and `&gt;` respectively; empty string when input is {@code null}
     */
    static String escapeTextContent(String text) {
        if (text == null) return "";

        // Fast path: scan for any character that needs escaping
        int len = text.length();
        int firstSpecial = -1;
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if (c == '&' || c == '<' || c == '>') {
                firstSpecial = i;
                break;
            }
        }
        if (firstSpecial < 0) {
            return text; // No escaping needed — common case
        }

        // Single-pass escape from the first special character
        StringBuilder sb = new StringBuilder(len + 16);
        if (firstSpecial > 0) {
            sb.append(text, 0, firstSpecial);
        }
        for (int i = firstSpecial; i < len; i++) {
            char c = text.charAt(i);
            if (c == '&') {
                sb.append("&amp;");
            } else if (c == '<') {
                sb.append("&lt;");
            } else if (c == '>') {
                sb.append("&gt;");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Unescapes XML entities in text content, including numeric character references.
     *
     * @param text the text possibly containing XML entities or numeric references
     * @return the text with entities and numeric references decoded; empty string when {@code text} is {@code null}
     */
    @SuppressWarnings({"java:S135", "java:S3776"}) // Single-pass entity scanner has inherent branching complexity
    public static String unescapeTextContent(String text) {
        if (text == null) return "";

        // Fast path: if no '&' exists, no unescaping is needed.
        // This is the common case for most element text content.
        int ampIdx = text.indexOf('&');
        if (ampIdx < 0) {
            return text;
        }

        // Single-pass scanner: resolve numeric references and named entities in one pass
        // to avoid re-decoding (e.g., &#38;lt; must yield "&lt;", not "<")
        StringBuilder result = new StringBuilder(text.length());
        // Bulk-copy everything before the first '&'
        if (ampIdx > 0) {
            result.append(text, 0, ampIdx);
        }
        int i = ampIdx;
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
    @SuppressWarnings({"java:S2975", "java:S1133", "java:S1182"})
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
     * Normalizes line endings per XML 1.0 §2.11.
     *
     * <p>The XML specification requires parsers to normalize line endings on input:
     * {@code \r\n} sequences are replaced with {@code \n}, and standalone {@code \r}
     * characters are replaced with {@code \n}. This normalization is applied to
     * API-reported values while preserving original line endings for serialization.</p>
     *
     * @param text the text to normalize; may be {@code null}
     * @return the text with line endings normalized, or {@code null} if input was {@code null}
     */
    static String normalizeLineEndings(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        // Fast path: if no \r exists, no normalization needed (common case)
        if (text.indexOf('\r') < 0) {
            return text;
        }
        // Replace \r\n with \n first, then replace remaining standalone \r with \n
        return text.replace("\r\n", "\n").replace("\r", "\n");
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
