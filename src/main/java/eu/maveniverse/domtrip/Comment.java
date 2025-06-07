package eu.maveniverse.domtrip;

/**
 * Represents an XML comment, preserving exact formatting and content.
 */
public class Comment extends Node {
    
    private String content;
    
    public Comment(String content) {
        super();
        this.content = content != null ? content : "";
    }
    
    @Override
    public NodeType getNodeType() {
        return NodeType.COMMENT;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content != null ? content : "";
        markModified();
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
        sb.append("<!--").append(content).append("-->");
        sb.append(followingWhitespace);
    }

    /**
     * Returns true if this comment contains only whitespace
     */
    public boolean isWhitespaceOnly() {
        return content.trim().isEmpty();
    }
    
    /**
     * Returns true if this comment is empty
     */
    public boolean isEmpty() {
        return content.isEmpty();
    }
    
    @Override
    public String toString() {
        String displayContent = content.length() > 50 ?
            content.substring(0, 47) + "..." : content;
        return "Comment{content='" + displayContent.replace("\n", "\\n") + "'}";
    }
}
