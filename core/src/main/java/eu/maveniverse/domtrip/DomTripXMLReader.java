/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip;

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
 * An {@link XMLReader} implementation that reads from a domtrip {@link Document}
 * instead of parsing XML text.
 *
 * <p>This class is used internally by {@link DomTripSAXSource} to provide a
 * {@link javax.xml.transform.sax.SAXSource} for JAXP interop. It can also be
 * used directly when an {@code XMLReader} is needed.</p>
 *
 * <h3>Supported Features:</h3>
 * <ul>
 *   <li>{@code http://xml.org/sax/features/namespaces} &mdash; always {@code true}</li>
 *   <li>{@code http://xml.org/sax/features/namespace-prefixes} &mdash; configurable,
 *       controls whether namespace declarations appear as attributes</li>
 * </ul>
 *
 * <h3>Supported Properties:</h3>
 * <ul>
 *   <li>{@code http://xml.org/sax/properties/lexical-handler} &mdash; set a
 *       {@link LexicalHandler} for comment and CDATA events</li>
 * </ul>
 *
 * @see DomTripSAXSource
 * @see SAXOutputter
 * @since 1.3.0
 */
public class DomTripXMLReader implements XMLReader {

    /** SAX feature: namespace processing. */
    private static final String FEATURE_NAMESPACES = "http://xml.org/sax/features/namespaces";

    /** SAX feature: namespace-prefixes (report xmlns attributes). */
    private static final String FEATURE_NAMESPACE_PREFIXES = "http://xml.org/sax/features/namespace-prefixes";

    /** SAX property: lexical handler. */
    private static final String PROPERTY_LEXICAL_HANDLER = "http://xml.org/sax/properties/lexical-handler";

    private final Document document;

    private ContentHandler contentHandler;
    private DTDHandler dtdHandler;
    private EntityResolver entityResolver;
    private ErrorHandler errorHandler;
    private LexicalHandler lexicalHandler;
    private boolean namespacePrefixes;

    /**
     * Creates a new XMLReader that will read from the given document.
     *
     * @param document the domtrip document to read from
     * @throws IllegalArgumentException if document is null
     */
    public DomTripXMLReader(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }
        this.document = document;
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
            this.namespacePrefixes = value;
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
            this.lexicalHandler = (LexicalHandler) value;
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
     * Parses the domtrip document and emits SAX events.
     *
     * <p>The {@code input} parameter is ignored &mdash; the document provided
     * at construction time is always used. This method exists to satisfy the
     * {@link XMLReader} contract.</p>
     *
     * @param input ignored
     * @throws SAXException if the content handler reports an error
     */
    @Override
    public void parse(InputSource input) throws SAXException, IOException {
        if (contentHandler == null) {
            throw new SAXException("ContentHandler not set");
        }

        SAXOutputter outputter = new SAXOutputter();
        outputter.setReportNamespaceDeclarations(namespacePrefixes);
        outputter.output(document, contentHandler, lexicalHandler);
    }

    /**
     * Parses the domtrip document and emits SAX events.
     *
     * <p>The {@code systemId} parameter is ignored &mdash; the document provided
     * at construction time is always used.</p>
     *
     * @param systemId ignored
     * @throws SAXException if the content handler reports an error
     */
    @Override
    public void parse(String systemId) throws SAXException, IOException {
        parse(new InputSource(systemId));
    }
}
