package eu.maveniverse.domtrip;

/**
 * Factory class for creating Document instances with common patterns.
 */
public class Documents {
    
    private Documents() {
        // Utility class
    }
    
    /**
     * Creates an empty document.
     */
    public static Document empty() {
        return new Document();
    }
    
    /**
     * Creates a document with XML declaration.
     */
    public static Document withXmlDeclaration(String version, String encoding) {
        Document document = new Document();
        document.setVersion(version != null ? version : "1.0");
        document.setEncoding(encoding != null ? encoding : "UTF-8");
        
        // Build XML declaration
        StringBuilder xmlDecl = new StringBuilder("<?xml version=\"");
        xmlDecl.append(document.getVersion()).append("\"");
        xmlDecl.append(" encoding=\"").append(document.getEncoding()).append("\"");
        xmlDecl.append("?>");
        
        document.setXmlDeclaration(xmlDecl.toString());
        return document;
    }
    
    /**
     * Creates a document with XML declaration and standalone attribute.
     */
    public static Document withXmlDeclaration(String version, String encoding, boolean standalone) {
        Document document = withXmlDeclaration(version, encoding);
        document.setStandalone(standalone);
        
        // Rebuild XML declaration with standalone
        StringBuilder xmlDecl = new StringBuilder("<?xml version=\"");
        xmlDecl.append(document.getVersion()).append("\"");
        xmlDecl.append(" encoding=\"").append(document.getEncoding()).append("\"");
        xmlDecl.append(" standalone=\"").append(standalone ? "yes" : "no").append("\"");
        xmlDecl.append("?>");
        
        document.setXmlDeclaration(xmlDecl.toString());
        return document;
    }
    
    /**
     * Creates a document with a root element.
     */
    public static Document withRootElement(String rootElementName) {
        Document document = withXmlDeclaration("1.0", "UTF-8");
        Element rootElement = new Element(rootElementName);
        document.setDocumentElement(rootElement);
        return document;
    }
    
    /**
     * Creates a document with XML declaration and DOCTYPE.
     */
    public static Document withDoctype(String version, String encoding, String doctype) {
        Document document = withXmlDeclaration(version, encoding);
        document.setDoctype(doctype);
        return document;
    }
    
    /**
     * Creates a minimal document with just a root element (no XML declaration).
     */
    public static Document minimal(String rootElementName) {
        Document document = new Document();
        Element rootElement = new Element(rootElementName);
        document.setDocumentElement(rootElement);
        return document;
    }
    
    /**
     * Builder for creating complex document structures.
     */
    public static class Builder {
        private final Document document;
        
        private Builder() {
            this.document = new Document();
        }
        
        public Builder withVersion(String version) {
            document.setVersion(version);
            return this;
        }
        
        public Builder withEncoding(String encoding) {
            document.setEncoding(encoding);
            return this;
        }
        
        public Builder withStandalone(boolean standalone) {
            document.setStandalone(standalone);
            return this;
        }
        
        public Builder withDoctype(String doctype) {
            document.setDoctype(doctype);
            return this;
        }
        
        public Builder withRootElement(Element rootElement) {
            document.setDocumentElement(rootElement);
            return this;
        }
        
        public Builder withRootElement(String rootElementName) {
            document.setDocumentElement(new Element(rootElementName));
            return this;
        }
        
        public Builder withXmlDeclaration() {
            StringBuilder xmlDecl = new StringBuilder("<?xml version=\"");
            xmlDecl.append(document.getVersion()).append("\"");
            xmlDecl.append(" encoding=\"").append(document.getEncoding()).append("\"");
            if (document.isStandalone()) {
                xmlDecl.append(" standalone=\"yes\"");
            }
            xmlDecl.append("?>");
            document.setXmlDeclaration(xmlDecl.toString());
            return this;
        }
        
        public Document build() {
            return document;
        }
    }
    
    /**
     * Creates a new document builder.
     */
    public static Builder builder() {
        return new Builder();
    }
}
