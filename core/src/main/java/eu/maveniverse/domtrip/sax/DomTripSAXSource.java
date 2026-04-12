/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.sax;

import eu.maveniverse.domtrip.Document;
import javax.xml.transform.sax.SAXSource;
import org.xml.sax.InputSource;

/**
 * A {@link SAXSource} backed by a domtrip {@link Document} for JAXP interoperability.
 *
 * <p>This class enables a domtrip document to be used directly with JAXP APIs such as
 * {@link javax.xml.transform.Transformer} and {@link javax.xml.validation.Validator}
 * without requiring intermediate serialization to a string or stream.</p>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // XSLT transformation
 * Document doc = Document.of(xml);
 * SAXSource source = DomTripSAXSource.of(doc);
 * transformer.transform(source, result);
 *
 * // Schema validation
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
     * Creates a new SAXSource backed by the given document.
     *
     * @param doc the document to use as the source
     * @return a new DomTripSAXSource
     * @throws IllegalArgumentException if doc is null
     */
    public static DomTripSAXSource of(Document doc) {
        if (doc == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }
        return new DomTripSAXSource(new DomTripXMLReader(doc), new InputSource());
    }

    private DomTripSAXSource(DomTripXMLReader reader, InputSource inputSource) {
        super(reader, inputSource);
    }
}
