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
 * // Add to document
 * document.addChild(pi);
 *
 * // Use builder pattern
 * ProcessingInstruction builderPI = ProcessingInstruction.builder()
 *     .withTarget("xml-stylesheet")
 *     .withData("type=\"text/css\" href=\"style.css\"")
 *     .build();
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

    public void target(String target) {
        this.target = target != null ? target : "";
        this.originalContent = ""; // Clear original when modified
        markModified();
    }

    public String data() {
        return data;
    }

    public void data(String data) {
        this.data = data != null ? data : "";
        this.originalContent = ""; // Clear original when modified
        markModified();
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

    /**
     * Builder for creating ProcessingInstruction instances with fluent API.
     *
     * <p>The ProcessingInstruction.Builder provides a convenient way to construct
     * XML processing instructions with proper target and data handling.</p>
     *
     * <h3>Usage Examples:</h3>
     * <pre>{@code
     * // Stylesheet processing instruction
     * ProcessingInstruction stylesheet = ProcessingInstruction.builder()
     *     .withTarget("xml-stylesheet")
     *     .withData("type=\"text/xsl\" href=\"transform.xsl\"")
     *     .build();
     *
     * // Simple processing instruction
     * ProcessingInstruction simple = ProcessingInstruction.builder()
     *     .withTarget("custom-app")
     *     .withData("config=debug")
     *     .build();
     * }</pre>
     *
     */
    public static class Builder {
        private String target = "";
        private String data = "";

        private Builder() {}

        /**
         * Sets the target of the processing instruction.
         *
         * @param target the PI target
         * @return this builder for method chaining
         */
        public Builder withTarget(String target) {
            this.target = target != null ? target : "";
            return this;
        }

        /**
         * Sets the data of the processing instruction.
         *
         * @param data the PI data
         * @return this builder for method chaining
         */
        public Builder withData(String data) {
            this.data = data != null ? data : "";
            return this;
        }

        /**
         * Builds and returns the configured ProcessingInstruction instance.
         *
         * @return the constructed ProcessingInstruction
         */
        public ProcessingInstruction build() {
            return new ProcessingInstruction(target, data);
        }
    }

    /**
     * Creates a new ProcessingInstruction builder instance.
     *
     * @return a new ProcessingInstruction.Builder for fluent PI construction
     */
    public static Builder builder() {
        return new Builder();
    }
}
