---
title: Builder Patterns
description: Advanced builder patterns and fluent APIs in DomTrip
layout: page
---

# Builder Patterns

DomTrip provides sophisticated builder patterns that enable fluent, type-safe XML document construction and modification. These patterns make it easy to create complex XML structures while maintaining readability and compile-time safety.

## Core Builder Concepts

### Fluent Interface Design
DomTrip's builders follow the fluent interface pattern, allowing method chaining for natural, readable code:

```java
{cdi:snippets.snippet('fluent-interface-design')}
```

### Type-Safe Construction
Builders provide compile-time safety and IDE support with method completion and type checking.

## Document Builder

### Creating New Documents
The `Editor` class serves as the main entry point for document creation:

```java
{cdi:snippets.snippet('document-creation')}
```

### Document Configuration
Configure document-level settings during creation:

```java
{cdi:snippets.snippet('document-configuration')}
```

## Element Builder

### Basic Element Creation
Create elements with various content types:

```java
{cdi:snippets.snippet('basic-element-creation')}
```

### Advanced Element Operations

```java
{cdi:snippets.snippet('advanced-element-operations')}
```

## Attribute Builder

### Attribute Management
DomTrip provides flexible attribute handling with quote style preservation:

```java
{cdi:snippets.snippet('attribute-management')}
```

### Attribute Modification
```java
{cdi:snippets.snippet('attribute-modification')}
```

## Namespace Builder

### Namespace Declaration
Handle XML namespaces with builder support:

```java
{cdi:snippets.snippet('namespace-declaration')}
```

## Comment and Processing Instruction Builders

### Adding Comments
```java
{cdi:snippets.snippet('adding-comments')}
```

### Processing Instructions
```java
{cdi:snippets.snippet('processing-instructions')}
```

## Advanced Builder Patterns

### Custom Builder Extensions
Create domain-specific builders for common patterns:

```java
{cdi:snippets.snippet('custom-builder-extensions')}
```

### Builder Composition
Combine multiple builders for complex document structures:

```java
{cdi:snippets.snippet('builder-composition')}
```

## Error Handling in Builders

### Validation and Error Recovery
```java
{cdi:snippets.snippet('error-handling')}
```

### Builder State Validation
```java
{cdi:snippets.snippet('builder-state-validation')}
```

## Best Practices

### 1. **Use Method Chaining Judiciously**
```java
{cdi:snippets.snippet('method-chaining-best-practices')}
```

### 2. **Leverage Type Safety**
```java
{cdi:snippets.snippet('type-safety-best-practices')}
```

### 3. **Handle Namespaces Consistently**
```java
{cdi:snippets.snippet('namespace-consistency')}
```

### 4. **Use Builders for Complex Structures**
For repetitive or complex XML patterns, create custom builders that encapsulate the logic and provide a clean API.

## Integration with DomTrip Features

Builder patterns work seamlessly with all DomTrip features:

- **Formatting Preservation**: Builders respect existing formatting when modifying documents
- **Namespace Support**: Full namespace-aware building capabilities
- **Configuration**: Builders honor DomTripConfig settings
- **Error Handling**: Comprehensive error reporting and recovery

The builder patterns in DomTrip provide a powerful, flexible foundation for XML document construction while maintaining the library's core principles of formatting preservation and ease of use.
