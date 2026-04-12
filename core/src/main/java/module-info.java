/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */

/**
 * DomTrip core module for lossless XML editing.
 */
module eu.maveniverse.domtrip {
    requires transitive java.xml;

    exports eu.maveniverse.domtrip;
    exports eu.maveniverse.domtrip.sax;
    exports eu.maveniverse.domtrip.stax;
}
