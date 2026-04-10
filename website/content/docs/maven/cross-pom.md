---
title: Cross-POM Alignment
description: Move dependency versions from child POMs to parent POMs with DomTrip
layout: page
---

# Cross-POM Alignment

DomTrip supports moving dependency versions from child POMs to a parent POM's
`<dependencyManagement>` section, making the child dependencies version-less.
Property definitions are automatically migrated or created in the parent as needed.

## Aligning a Single Dependency

Move one dependency's version to the parent:

```java
{cdi:snippets.snippet('align-to-parent')}
```

## Aligning All Dependencies

Move all dependency versions to the parent in one call:

```java
{cdi:snippets.snippet('align-all-to-parent')}
```

## What Happens During Alignment

When a dependency is aligned to a parent POM:

1. **Version resolution** - The child's version is resolved (including property lookup if `$\{...}`)
2. **Property migration** - If the child uses a property reference, the property definition is migrated to the parent
3. **Managed dependency creation** - A `<dependency>` entry is created in the parent's `<dependencyManagement>`
4. **Child version removal** - The `<version>` element is removed from the child dependency
5. **Property cleanup** - If the child property is no longer referenced anywhere, it is removed

### Property Handling

| Child version | Target source | Result in parent |
|--------------|---------------|------------------|
| `2.0.7` (literal) | `LITERAL` | `<version>2.0.7</version>` |
| `2.0.7` (literal) | `PROPERTY` | Property created + `<version>$\{slf4j-api.version}</version>` |
| `$\{slf4j.version}` (property) | `PROPERTY` | Property migrated + `<version>$\{slf4j.version}</version>` |
| `$\{slf4j.version}` (property) | `LITERAL` | `<version>2.0.7</version>` (resolved) |

## AlignOptions for Cross-POM

The `AlignOptions` control how versions are stored in the parent POM. Key options:

```java
AlignOptions options = AlignOptions.builder()
    // How versions are stored in the parent's dependencyManagement
    .versionSource(AlignOptions.VersionSource.PROPERTY)
    // How property names are generated
    .namingConvention(AlignOptions.PropertyNamingConvention.DOT_SUFFIX)
    // Or use an explicit property name
    .propertyName("guava.version")
    .build();
```

## API Reference

| Method | Description |
|--------|-------------|
| `dependencies().alignToParent(coords, parentEditor, options)` | Move a single dependency's version to the parent |
| `dependencies().alignAllToParent(parentEditor, options)` | Move all dependency versions to the parent |

Both methods return a count or boolean indicating how many/whether dependencies were moved.
Dependencies that are already version-less (managed) are skipped.

## Next Steps

- [Dependency Alignment](../alignment/) - Align dependencies within a single POM
- [Profile-Scoped Operations](../profiles/) - Manage profile-specific dependencies
- [Maven Examples](../examples/) - More real-world examples
