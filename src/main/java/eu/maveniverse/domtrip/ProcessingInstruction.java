package eu.maveniverse.domtrip;

/**
 * Represents an XML processing instruction.
 */
public class ProcessingInstruction extends Node {
    private String target;
    private String data;
    private String originalContent; // Full original PI content for preservation

    public ProcessingInstruction(String target, String data) {
        super();
        this.target = target != null ? target : "";
        this.data = data != null ? data : "";
        this.originalContent = "";
    }

    public ProcessingInstruction(String originalContent) {
        super();
        this.originalContent = originalContent != null ? originalContent : "";
        parseContent();
    }

    private void parseContent() {
        if (originalContent.startsWith("<?") && originalContent.endsWith("?>")) {
            String content =
                    originalContent.substring(2, originalContent.length() - 2).trim();
            int spaceIndex = content.indexOf(' ');
            if (spaceIndex > 0) {
                this.target = content.substring(0, spaceIndex);
                this.data = content.substring(spaceIndex + 1).trim();
            } else {
                this.target = content;
                this.data = "";
            }
        } else {
            this.target = "";
            this.data = "";
        }
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target != null ? target : "";
        this.originalContent = ""; // Clear original when modified
        markModified();
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data != null ? data : "";
        this.originalContent = ""; // Clear original when modified
        markModified();
    }

    public String getOriginalContent() {
        return originalContent;
    }

    public void setOriginalContent(String originalContent) {
        this.originalContent = originalContent != null ? originalContent : "";
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.PROCESSING_INSTRUCTION;
    }

    @Override
    public String toXml() {
        StringBuilder sb = new StringBuilder();
        toXml(sb);
        return sb.toString();
    }

    @Override
    public void toXml(StringBuilder sb) {
        // Add preceding whitespace
        sb.append(precedingWhitespace);

        // Use original content if not modified, otherwise build from scratch
        if (!isModified() && !originalContent.isEmpty()) {
            sb.append(originalContent);
        } else {
            sb.append("<?").append(target);
            if (!data.isEmpty()) {
                sb.append(" ").append(data);
            }
            sb.append("?>");
        }

        // Add following whitespace
        sb.append(followingWhitespace);
    }

    @Override
    public String toString() {
        return "ProcessingInstruction{target='" + target + "', data='" + data + "'}";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ProcessingInstruction that = (ProcessingInstruction) obj;
        return target.equals(that.target) && data.equals(that.data);
    }

    @Override
    public int hashCode() {
        return target.hashCode() * 31 + data.hashCode();
    }
}
