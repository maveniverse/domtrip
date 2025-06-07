package eu.maveniverse.domtrip;

/**
 * Represents text content in XML, preserving exact whitespace and formatting.
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
    public NodeType getNodeType() {
        return NodeType.TEXT;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content != null ? content : "";
        this.rawContent = null; // Clear raw content when content is modified
        markModified();
    }

    public String getRawContent() {
        return rawContent;
    }

    public void setRawContent(String rawContent) {
        this.rawContent = rawContent;
    }
    
    public boolean isCData() {
        return isCData;
    }
    
    public void setCData(boolean cData) {
        this.isCData = cData;
        markModified();
    }
    
    public boolean isPreserveWhitespace() {
        return preserveWhitespace;
    }
    
    public void setPreserveWhitespace(boolean preserveWhitespace) {
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
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
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
        String displayContent = content.length() > 50 ?
            content.substring(0, 47) + "..." : content;
        return "Text{content='" + displayContent.replace("\n", "\\n") +
               "', isCData=" + isCData + "}";
    }
}
