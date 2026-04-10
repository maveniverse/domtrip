---
title: Profile-Scoped Operations
description: Managing dependencies within Maven profiles using DomTrip
layout: page
---

# Profile-Scoped Operations

DomTrip supports scoping dependency operations to specific Maven profiles. This allows you
to manage profile-specific dependencies independently from top-level project dependencies.

## Basic Usage

Use `forProfile()` to get a `Dependencies` instance scoped to a specific profile:

```java
{cdi:snippets.snippet('profile-scoped')}
```

## How It Works

The `forProfile()` method returns a new `Dependencies` instance where all operations resolve
relative to the `<profile>` element instead of the project root:

| Operation | Top-level | Profile-scoped |
|-----------|-----------|----------------|
| `addDependency()` | `project/dependencies` | `project/profiles/profile[id=X]/dependencies` |
| `updateDependency()` | `project/dependencies` | `project/profiles/profile[id=X]/dependencies` |
| `deleteDependency()` | `project/dependencies` | `project/profiles/profile[id=X]/dependencies` |
| `addAligned()` | `project/dependencies` | `project/profiles/profile[id=X]/dependencies` |
| `addExclusion()` | `project/dependencies` | `project/profiles/profile[id=X]/dependencies` |

## API Reference

### Scoping Methods

| Method | Description |
|--------|-------------|
| `dependencies().forProfile(String profileId)` | Scope to a profile by its `<id>` value |
| `dependencies().forProfile(Element profileElement)` | Scope to a pre-resolved `<profile>` element |

### Finding Profiles

```java
// Check if a profile exists
boolean exists = editor.profiles().hasProfile("integration-tests");

// Find a profile element
Element profile = editor.profiles().findProfile("integration-tests");
if (profile != null) {
    editor.dependencies().forProfile(profile).addAligned(coords);
}
```

## Notes

- `forProfile(String)` throws `DomTripException` if the profile is not found
- The profile-scoped `Dependencies` instance supports all the same operations as the top-level one: CRUD, exclusions, alignment, and convention detection
- Profile-scoped operations do not affect top-level dependencies and vice versa

## Next Steps

- [Dependency Alignment](../alignment/) - Auto-detect and align dependency conventions
- [Cross-POM Alignment](../cross-pom/) - Move versions to parent POMs
- [Exclusion Management](../exclusions/) - Add and remove dependency exclusions
