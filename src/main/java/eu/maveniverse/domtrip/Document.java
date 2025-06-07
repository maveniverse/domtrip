package eu.maveniverse.domtrip;

/**
 * Represents the root of an XML document, containing the document element
 * and preserving document-level formatting like XML declarations and DTDs.
 */
public class Document extends Node {
    
    private String xmlDeclaration;
    private String doctype;
    private Element documentElement;
    private String encoding;
    private String version;
    private boolean standalone;
    
    public Document() {
        super();
        this.xmlDeclaration = "";
        this.doctype = "";
        this.encoding = "UTF-8";
        this.version = "1.0";
        this.standalone = false;
    }
    
    @Override
    public NodeType getNodeType() {
        return NodeType.DOCUMENT;
    }
    
    public String getXmlDeclaration() {
        return xmlDeclaration;
    }
    
    public void setXmlDeclaration(String xmlDeclaration) {
        this.xmlDeclaration = xmlDeclaration != null ? xmlDeclaration : "";
        markModified();
    }
    
    public String getDoctype() {
        return doctype;
    }
    
    public void setDoctype(String doctype) {
        this.doctype = doctype != null ? doctype : "";
        markModified();
    }
    
    public Element getDocumentElement() {
        return documentElement;
    }

    public void setDocumentElement(Element documentElement) {
        this.documentElement = documentElement;
        if (documentElement != null) {
            documentElement.setParent(this);
        }
        markModified();
    }

    /**
     * Sets document element without marking as modified (for use during parsing)
     */
    void setDocumentElementInternal(Element documentElement) {
        this.documentElement = documentElement;
        if (documentElement != null) {
            documentElement.setParent(this);
        }
        // Don't call markModified() here
    }
    
    public String getEncoding() {
        return encoding;
    }
    
    public void setEncoding(String encoding) {
        this.encoding = encoding != null ? encoding : "UTF-8";
        markModified();
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version != null ? version : "1.0";
        markModified();
    }
    
    public boolean isStandalone() {
        return standalone;
    }
    
    public void setStandalone(boolean standalone) {
        this.standalone = standalone;
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
        // Add XML declaration only if it was present in original
        if (!xmlDeclaration.isEmpty()) {
            sb.append(xmlDeclaration);
        }

        // Add DOCTYPE if present
        if (!doctype.isEmpty()) {
            sb.append("\n").append(doctype);
        }

        // Add preceding whitespace
        sb.append(precedingWhitespace);

        // Add all children (comments, processing instructions, document element)
        for (Node child : children) {
            child.toXml(sb);
        }

        // Add document element if set and not already in children
        if (documentElement != null && !children.contains(documentElement)) {
            documentElement.toXml(sb);
        }

        // Add following whitespace
        sb.append(followingWhitespace);
    }

    /**
     * Finds the first element with the given name in the document
     */
    public Element findElement(String name) {
        // First check document element
        if (documentElement != null && name.equals(documentElement.getName())) {
            return documentElement;
        }

        // Then search in children
        for (Node child : children) {
            if (child instanceof Element) {
                Element element = (Element) child;
                if (name.equals(element.getName())) {
                    return element;
                }
            }
        }

        // Finally search recursively in document element
        return findElementRecursive(documentElement, name);
    }

    private Element findElementRecursive(Node node, String name) {
        if (node == null) return null;

        for (Node child : node.getChildren()) {
            if (child instanceof Element) {
                Element element = (Element) child;
                if (name.equals(element.getName())) {
                    return element;
                }
                Element found = findElementRecursive(element, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
    
    /**
     * Creates a minimal XML declaration based on current settings
     */
    public String generateXmlDeclaration() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"").append(version).append("\"");
        sb.append(" encoding=\"").append(encoding).append("\"");
        if (standalone) {
            sb.append(" standalone=\"yes\"");
        }
        sb.append("?>");
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return "Document{version='" + version + "', encoding='" + encoding +
               "', documentElement=" + (documentElement != null ? documentElement.getName() : "null") + "}";
    }
}
