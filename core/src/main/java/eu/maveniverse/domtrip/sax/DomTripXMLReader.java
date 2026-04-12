/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.sax;

import eu.maveniverse.domtrip.Document;
import java.io.IOException;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

/**
 * An {@link XMLReader} implementation that reads from a domtrip {@link Document}.
 *
 * <p>This class allows a domtrip document to be used as a {@link javax.xml.transform.sax.SAXSource}
 * for JAXP interoperability. When {@link #parse(InputSource)} is called, it walks the
 * domtrip document tree and emits SAX events to the registered {@link ContentHandler}.</p>
 *
 * <h3>Supported Features:</h3>
 * <ul>
 *   <li>{@code http://xml.org/sax/features/namespaces} - always {@code true}</li>
 *   <li>{@code http://xml.org/sax/features/namespace-prefixes} - controls whether namespace
 *       declarations are reported as attributes (default {@code false})</li>
 * </ul>
 *
 * <h3>Supported Properties:</h3>
 * <ul>
 *   <li>{@code http://xml.org/sax/properties/lexical-handler} - {@link LexicalHandler}
 *       for comment and CDATA events</li>
 * </ul>
 *
 * @see DomTripSAXSource
 * @see SAXOutputter
 * @since 1.3.0
 */
public class DomTripXMLReader implements XMLReader {

    /** SAX feature URI for namespace processing. */
    private static final String FEATURE_NAMESPACES = "http://xml.org/sax/features/namespaces";

    /** SAX feature URI for reporting namespace declarations as attributes. */
    private static final String FEATURE_NAMESPACE_PREFIXES = "http://xml.org/sax/features/namespace-prefixes";

    /** SAX property URI for the lexical handler. */
    private static final String PROPERTY_LEXICAL_HANDLER = "http://xml.org/sax/properties/lexical-handler";

    private final Document document;
    private ContentHandler contentHandler;
    private DTDHandler dtdHandler;
    private EntityResolver entityResolver;
    private ErrorHandler errorHandler;
    private LexicalHandler lexicalHandler;
    private boolean namespacePrefixes;

    /**
     * Creates a new XMLReader that reads from the given document.
     *
     * @param document the document to read
     * @throws IllegalArgumentException if document is null
     */
    public DomTripXMLReader(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }
        this.document = document;
    }

    /**
     * Returns the document backing this reader.
     *
     * @return the domtrip document
     */
    public Document getDocument() {
        return document;
    }

    @Override
    public boolean getFeature(String name) throws SAXNotRecognizedException {
        if (FEATURE_NAMESPACES.equals(name)) {
            return true;
        }
        if (FEATURE_NAMESPACE_PREFIXES.equals(name)) {
            return namespacePrefixes;
        }
        throw new SAXNotRecognizedException("Feature: " + name);
    }

    @Override
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (FEATURE_NAMESPACES.equals(name)) {
            if (!value) {
                throw new SAXNotSupportedException("Namespace processing cannot be disabled");
            }
            return;
        }
        if (FEATURE_NAMESPACE_PREFIXES.equals(name)) {
            namespacePrefixes = value;
            return;
        }
        throw new SAXNotRecognizedException("Feature: " + name);
    }

    @Override
    public Object getProperty(String name) throws SAXNotRecognizedException {
        if (PROPERTY_LEXICAL_HANDLER.equals(name)) {
            return lexicalHandler;
        }
        throw new SAXNotRecognizedException("Property: " + name);
    }

    @Override
    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (PROPERTY_LEXICAL_HANDLER.equals(name)) {
            if (value != null && !(value instanceof LexicalHandler)) {
                throw new SAXNotSupportedException("Property value must be a LexicalHandler");
            }
            lexicalHandler = (LexicalHandler) value;
            return;
        }
        throw new SAXNotRecognizedException("Property: " + name);
    }

    @Override
    public void setEntityResolver(EntityResolver resolver) {
        this.entityResolver = resolver;
    }

    @Override
    public EntityResolver getEntityResolver() {
        return entityResolver;
    }

    @Override
    public void setDTDHandler(DTDHandler handler) {
        this.dtdHandler = handler;
    }

    @Override
    public DTDHandler getDTDHandler() {
        return dtdHandler;
    }

    @Override
    public void setContentHandler(ContentHandler handler) {
        this.contentHandler = handler;
    }

    @Override
    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    @Override
    public void setErrorHandler(ErrorHandler handler) {
        this.errorHandler = handler;
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    /**
     * Parses the domtrip document, emitting SAX events to the registered handlers.
     *
     * <p>The {@code input} parameter is ignored since the document is provided
     * at construction time. A {@link ContentHandler} must be set before calling
     * this method.</p>
     *
     * @param input ignored (document was provided at construction)
     * @throws SAXException if the content handler reports an error
     * @throws IllegalStateException if no content handler has been set
     */
    @Override
    public void parse(InputSource input) throws IOException, SAXException {
        if (contentHandler == null) {
            throw new IllegalStateException("ContentHandler must be set before parsing");
        }
        SAXOutputter outputter = new SAXOutputter();
        outputter.setReportNamespaceDeclarations(namespacePrefixes);
        outputter.output(document, contentHandler, lexicalHandler);
    }

    /**
     * Parses the domtrip document, emitting SAX events to the registered handlers.
     *
     * <p>The {@code systemId} parameter is ignored since the document is provided
     * at construction time.</p>
     *
     * @param systemId ignored
     * @throws SAXException if the content handler reports an error
     */
    @Override
    public void parse(String systemId) throws IOException, SAXException {
        parse(new InputSource(systemId));
    }
}
