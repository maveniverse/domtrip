/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */

/**
 * DomTrip Maven module for Maven-specific XML editing extensions.
 */
module eu.maveniverse.domtrip.maven {
    exports eu.maveniverse.domtrip.maven;

    requires transitive eu.maveniverse.domtrip;
    requires java.logging;
}
