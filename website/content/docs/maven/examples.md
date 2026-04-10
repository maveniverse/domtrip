---
title: Maven Examples
description: Real-world examples of using the DomTrip Maven extension
layout: page
---

# Maven Examples

Practical examples of using the DomTrip Maven extension for common POM editing tasks.

## Creating a POM from Scratch

Create a complete Maven project POM with Maven-aware element ordering:

```java
{cdi:snippets.snippet('basic-pom-creation')}
```

## Adding Dependencies

Add dependencies with proper structure and ordering:

```java
{cdi:snippets.snippet('adding-dependencies')}
```

## Adding Plugins

Configure build plugins with nested configuration:

```java
{cdi:snippets.snippet('adding-plugins')}
```

## Multi-Module Projects

Create a parent POM with module declarations:

```java
{cdi:snippets.snippet('multi-module-project')}
```

## Spring Boot Project

Create a Spring Boot project POM with parent, starters, and plugins:

```java
{cdi:snippets.snippet('spring-boot-project')}
```

## POM Transformation

Transform an existing POM by adding metadata, properties, dependencies, and plugins:

```java
{cdi:snippets.snippet('pom-transformation')}
```

## Editing Existing POMs

Load and modify existing POM files while preserving formatting:

```java
{cdi:snippets.snippet('editing-existing-pom')}
```

## Dependency Management Operations

Update, upsert, and delete dependencies and managed dependencies:

```java
{cdi:snippets.snippet('dependency-management-ops')}
```

## Managing Properties

Add, update, and delete POM properties:

```java
{cdi:snippets.snippet('property-management')}
```

## Exclusion Management

Add and remove dependency exclusions:

```java
{cdi:snippets.snippet('exclusion-add')}
```

## Convention-Aligned Dependencies

Add dependencies that match your project's existing conventions:

```java
{cdi:snippets.snippet('add-aligned')}
```

## Cross-POM Alignment

Move dependency versions from child POMs to a parent POM:

```java
{cdi:snippets.snippet('align-to-parent')}
```

## Next Steps

- [PomEditor API Reference](../api/) - Complete method reference
- [Element Ordering](../ordering/) - How Maven element ordering works
- [Dependency Alignment](../alignment/) - Auto-detect and align conventions
- [Cross-POM Alignment](../cross-pom/) - Multi-module version management
