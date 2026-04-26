---
title: Dependency Alignment
description: Auto-detect and align dependency version conventions with DomTrip
layout: page
---

# Dependency Alignment

DomTrip can detect how your project manages dependency versions and add or transform dependencies
to follow those conventions consistently. This is powered by the `AlignOptions` configuration
and the convention detection API.

## Convention Detection

DomTrip analyzes your POM to detect three aspects of your dependency versioning strategy:

```java
{cdi:snippets.snippet('convention-detection')}
```

### What Gets Detected

| Convention | Values | Description |
|-----------|--------|-------------|
| **VersionStyle** | `INLINE`, `MANAGED` | Whether versions are on dependencies directly or in `<dependencyManagement>` |
| **VersionSource** | `LITERAL`, `PROPERTY` | Whether versions are literal values (`5.9.2`) or property references (`$\{junit.version}`) |
| **PropertyNamingConvention** | `DOT_SUFFIX`, `DASH_SUFFIX`, `CAMEL_CASE`, `DOT_PREFIX` | How version properties are named |

### Property Naming Examples

| Convention | Example |
|-----------|---------|
| `DOT_SUFFIX` | `junit-jupiter.version` |
| `DASH_SUFFIX` | `junit-jupiter-version` |
| `CAMEL_CASE` | `junitJupiterVersion` |
| `DOT_PREFIX` | `version.junit-jupiter` |

## Adding Aligned Dependencies

Use `addAligned()` to add dependencies that automatically follow your project's conventions:

```java
{cdi:snippets.snippet('add-aligned')}
```

## Aligning Existing Dependencies

Transform existing dependencies to match a target convention:

```java
{cdi:snippets.snippet('align-dependency')}
```

## Aligning All Dependencies

Apply conventions consistently across all dependencies in a single call:

```java
{cdi:snippets.snippet('align-all')}
```

## Updating Managed Dependencies (Aligned)

Use `updateManagedDependencyAligned()` to add or update entries in `<dependencyManagement>`
while following the project's detected conventions. Unlike `updateManagedDependency()` which
always inlines the raw version string, this method creates version properties when the project
convention calls for them:

```java
{cdi:snippets.snippet('update-managed-aligned')}
```

## AlignOptions

`AlignOptions` controls how alignment operations behave. Use the builder for explicit control,
or `AlignOptions.defaults()` to auto-detect everything.

### Builder API

```java
// Auto-detect all conventions
AlignOptions options = AlignOptions.defaults();

// Force managed + property style
AlignOptions options = AlignOptions.builder()
    .versionStyle(AlignOptions.VersionStyle.MANAGED)
    .versionSource(AlignOptions.VersionSource.PROPERTY)
    .build();

// Force specific property name
AlignOptions options = AlignOptions.builder()
    .versionSource(AlignOptions.VersionSource.PROPERTY)
    .propertyName("junit.version")
    .build();

// Custom property naming pattern
AlignOptions options = AlignOptions.builder()
    .versionSource(AlignOptions.VersionSource.PROPERTY)
    .propertyNameGenerator(coords ->
        coords.groupId() + "." + coords.artifactId() + ".version")
    .build();

// Add as test dependency
AlignOptions options = AlignOptions.builder()
    .scope("test")
    .build();
```

### Option Fields

| Field | Type | Description |
|-------|------|-------------|
| `versionStyle` | `VersionStyle` | `INLINE` or `MANAGED`; `null` = auto-detect |
| `versionSource` | `VersionSource` | `LITERAL` or `PROPERTY`; `null` = auto-detect |
| `namingConvention` | `PropertyNamingConvention` | Property naming pattern; `null` = auto-detect |
| `propertyName` | `String` | Explicit property name override |
| `propertyNameGenerator` | `Function<Coordinates, String>` | Custom property name generator |
| `scope` | `String` | Maven dependency scope (e.g., `"test"`) |

### Precedence

When resolving property names, the precedence is:
1. Explicit `propertyName` (highest)
2. Custom `propertyNameGenerator`
3. `namingConvention`
4. Auto-detected convention (lowest)

## Next Steps

- [Cross-POM Alignment](../cross-pom/) - Move versions to parent POMs
- [Profile-Scoped Operations](../profiles/) - Manage profile-specific dependencies
- [Exclusion Management](../exclusions/) - Add and remove dependency exclusions
