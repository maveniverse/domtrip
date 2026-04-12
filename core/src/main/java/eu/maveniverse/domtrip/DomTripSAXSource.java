/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip;

import javax.xml.transform.sax.SAXSource;
import org.xml.sax.InputSource;

/**
 * A {@link SAXSource} adapter that wraps a domtrip {@link Document} for use
 * with JAXP APIs such as {@link javax.xml.transform.Transformer} and
 * {@link javax.xml.validation.Validator}.
 *
 * <h3>Usage with Transformer:</h3>
 * <pre>{@code
 * Document doc = Document.of(xml);
 * SAXSource source = DomTripSAXSource.of(doc);
 * transformer.transform(source, result);
 * }</pre>
 *
 * <h3>Usage with Validator:</h3>
 * <pre>{@code
 * Document doc = Document.of(xml);
 * SAXSource source = DomTripSAXSource.of(doc);
 * validator.validate(source);
 * }</pre>
 *
 * @see SAXOutputter
 * @see DomTripXMLReader
 * @since 1.3.0
 */
public class DomTripSAXSource extends SAXSource {

    /**
     * Creates a new DomTripSAXSource wrapping the given document.
     *
     * @param document the domtrip document to wrap
     * @throws IllegalArgumentException if document is null
     */
    public DomTripSAXSource(Document document) {
        super(new DomTripXMLReader(document), new InputSource());
    }

    /**
     * Creates a new DomTripSAXSource wrapping the given document.
     *
     * <p>This is a convenience factory method equivalent to calling the constructor.</p>
     *
     * @param document the domtrip document to wrap
     * @return a new DomTripSAXSource
     * @throws IllegalArgumentException if document is null
     */
    public static DomTripSAXSource of(Document document) {
        return new DomTripSAXSource(document);
    }
}
