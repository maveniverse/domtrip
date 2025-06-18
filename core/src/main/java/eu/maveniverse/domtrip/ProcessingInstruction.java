package eu.maveniverse.domtrip;

/**
 * Represents an XML processing instruction, preserving exact formatting and content.
 *
 * <p>Processing instructions (PIs) provide a way to include application-specific
 * instructions in XML documents. They follow the syntax {@code <?target data?>}
 * where the target identifies the application and the data contains the instruction.</p>
 *
 * <h3>Processing Instruction Handling:</h3>
 * <ul>
 *   <li><strong>Target Preservation</strong> - Maintains the PI target exactly as written</li>
 *   <li><strong>Data Preservation</strong> - Preserves the instruction data</li>
 *   <li><strong>Format Preservation</strong> - Keeps original formatting when unmodified</li>
 * </ul>
 *
 * <h3>Common Processing Instructions:</h3>
 * <ul>
 *   <li>{@code <?xml version="1.0" encoding="UTF-8"?>} - XML declaration</li>
 *   <li>{@code <?xml-stylesheet type="text/xsl" href="style.xsl"?>} - Stylesheet reference</li>
 *   <li>{@code <?php echo "Hello World"; ?>} - PHP code</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Create processing instruction
 * ProcessingInstruction pi = new ProcessingInstruction("xml-stylesheet",
 *     "type=\"text/xsl\" href=\"style.xsl\"");
 *
 * // Create using factory methods
 * ProcessingInstruction factoryPI = ProcessingInstruction.of("xml-stylesheet",
 *     "type=\"text/css\" href=\"style.css\"");
 * ProcessingInstruction simplePI = ProcessingInstruction.of("target");
 *
 * // Create and modify with fluent API
 * ProcessingInstruction fluentPI = ProcessingInstruction.of("target", "data")
 *     .target("new-target")
 *     .data("new data");
 *
 * // Add to document
 * document.addChild(pi);
 * }</pre>
 *
 * @see Node
 * @see Document
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

    public String target() {
        return target;
    }

    public ProcessingInstruction target(String target) {
        this.target = target != null ? target : "";
        this.originalContent = ""; // Clear original when modified
        markModified();
        return this;
    }

    public String data() {
        return data;
    }

    public ProcessingInstruction data(String data) {
        this.data = data != null ? data : "";
        this.originalContent = ""; // Clear original when modified
        markModified();
        return this;
    }

    public String originalContent() {
        return originalContent;
    }

    public void originalContent(String originalContent) {
        this.originalContent = originalContent != null ? originalContent : "";
    }

    @Override
    public NodeType type() {
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

    /**
     * Creates a processing instruction with the specified target and data.
     *
     * <p>Factory method following modern Java naming conventions.</p>
     *
     * @param target the processing instruction target
     * @param data the processing instruction data
     * @return a new ProcessingInstruction
     */
    public static ProcessingInstruction of(String target, String data) {
        return new ProcessingInstruction(target, data);
    }

    /**
     * Creates a processing instruction with only a target (no data).
     *
     * <p>Factory method for simple processing instructions.</p>
     *
     * @param target the processing instruction target
     * @return a new ProcessingInstruction with empty data
     */
    public static ProcessingInstruction of(String target) {
        return new ProcessingInstruction(target, "");
    }
}
