/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.stax;

import eu.maveniverse.domtrip.Document;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stax.StAXSource;

/**
 * A {@link StAXSource} backed by a domtrip {@link Document} for JAXP interoperability.
 *
 * <p>This class enables a domtrip document to be used directly with JAXP APIs such as
 * {@link javax.xml.transform.Transformer} and {@link javax.xml.validation.Validator}
 * without requiring intermediate serialization to a string or stream.</p>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // XSLT transformation
 * Document doc = Document.of(xml);
 * StAXSource source = DomTripStAXSource.of(doc);
 * transformer.transform(source, result);
 *
 * // Schema validation
 * StAXSource source = DomTripStAXSource.of(doc);
 * validator.validate(source);
 * }</pre>
 *
 * @see DomTripStreamReader
 * @since 1.3.0
 */
public class DomTripStAXSource extends StAXSource {

    /**
     * Creates a new StAXSource backed by the given document.
     *
     * @param doc the document to use as the source
     * @return a new DomTripStAXSource
     * @throws IllegalArgumentException if doc is null
     */
    public static DomTripStAXSource of(Document doc) {
        if (doc == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }
        try {
            return new DomTripStAXSource(new DomTripStreamReader(doc));
        } catch (XMLStreamException e) {
            // Should not happen: DomTripStreamReader always starts at START_DOCUMENT
            throw new IllegalStateException("Failed to create StAX source", e);
        }
    }

    private DomTripStAXSource(DomTripStreamReader reader) throws XMLStreamException {
        super(reader);
    }
}
