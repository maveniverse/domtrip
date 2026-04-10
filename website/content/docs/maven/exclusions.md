---
title: Exclusion Management
description: Managing dependency exclusions with DomTrip's PomEditor
layout: page
---

# Exclusion Management

DomTrip provides a complete API for managing dependency exclusions in Maven POM files.
Exclusions can be added, checked, and removed from both regular and managed dependencies.

## Adding Exclusions

Add exclusions to dependencies using `Coordinates` to identify both the dependency and the exclusion:

```java
{cdi:snippets.snippet('exclusion-add')}
```

## Managed Dependency Exclusions

The same operations are available for dependencies in `<dependencyManagement>`:

```java
{cdi:snippets.snippet('exclusion-managed')}
```

## API Reference

### Regular Dependencies

| Method | Description |
|--------|-------------|
| `dependencies().addExclusion(dep, excl)` | Adds an exclusion; creates `<exclusions>` wrapper if absent |
| `dependencies().deleteExclusion(dep, excl)` | Removes an exclusion; removes empty `<exclusions>` wrapper |
| `dependencies().hasExclusion(dep, excl)` | Checks if a dependency has a specific exclusion |

### Managed Dependencies

| Method | Description |
|--------|-------------|
| `dependencies().addManagedExclusion(dep, excl)` | Adds an exclusion to a managed dependency |
| `dependencies().deleteManagedExclusion(dep, excl)` | Removes an exclusion from a managed dependency |
| `dependencies().hasManagedExclusion(dep, excl)` | Checks if a managed dependency has a specific exclusion |

## Notes

- Dependencies are matched by **groupId:artifactId** (GA coordinates)
- The version in the `Coordinates` is not used for matching exclusions
- When the last exclusion is deleted, the `<exclusions>` wrapper element is automatically removed
- Exclusion elements are ordered according to Maven conventions: `<groupId>` then `<artifactId>`

## Next Steps

- [Dependency Alignment](../alignment/) - Auto-detect and align dependency conventions
- [PomEditor API Reference](../api/) - Complete API documentation
- [Maven Examples](../examples/) - More real-world examples
