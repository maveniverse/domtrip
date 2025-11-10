/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */

/**
 * Maven-specific extensions for the DomTrip XML editing library.
 *
 * <p>This package provides specialized classes for working with Maven POM files,
 * extending the core DomTrip functionality with Maven-specific features:</p>
 *
 * <ul>
 *   <li>{@link eu.maveniverse.domtrip.maven.PomEditor} - Specialized editor for Maven POM files</li>
 *   <li>{@link eu.maveniverse.domtrip.maven.MavenPomElements} - Constants for Maven POM elements and attributes</li>
 * </ul>
 *
 * <h2>Key Features</h2>
 * <ul>
 *   <li><strong>Maven Element Ordering</strong> - Automatically orders elements according to Maven conventions</li>
 *   <li><strong>Formatting Preservation</strong> - Maintains original formatting, whitespace, and comments</li>
 *   <li><strong>Intelligent Blank Lines</strong> - Adds appropriate blank lines between element groups</li>
 *   <li><strong>Maven-specific Methods</strong> - Convenience methods for common POM operations</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Parse existing POM
 * Document doc = Document.of(pomXmlString);
 * PomEditor editor = new PomEditor(doc);
 *
 * // Add elements with proper ordering
 * Element root = editor.root();
 * editor.insertMavenElement(root, "description", "My project description");
 * editor.insertMavenElement(root, "name", "My Project");  // Will be ordered before description
 *
 * // Add dependencies
 * Element dependencies = editor.findChildElement(root, "dependencies");
 * if (dependencies == null) {
 *     dependencies = editor.insertMavenElement(root, "dependencies");
 * }
 * editor.addDependency(dependencies, "org.junit.jupiter", "junit-jupiter", "5.9.2");
 *
 * // Serialize with preserved formatting
 * String result = editor.toXml();
 * }</pre>
 *
 * @since 0.1
 */
package eu.maveniverse.domtrip.maven;
