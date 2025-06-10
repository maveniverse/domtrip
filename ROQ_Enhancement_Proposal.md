# ROQ Enhancement Proposal: Production-Ready Static Site Features

## Problem Statement
While implementing a documentation website for the DomTrip project, we identified critical ROQ limitations that prevent production deployments:

1. **No `site.basePath` support** - GitHub Pages requires `/repository-name` prefix, but ROQ has no configurable base path property
2. **Limited frontmatter access** - Custom frontmatter properties aren't accessible in templates (e.g., `{#if page.custom-property}` doesn't work)
3. **No environment-specific configuration** - Can't have different base paths for dev (`""`) vs prod (`"/project"`)
4. **Broken development mode** - `quarkus:dev` doesn't work when production requires base paths

## Current Workarounds & Pain Points
- Hardcoding `/domtrip` paths in all templates
- Creating separate template files instead of conditional logic
- Manual configuration switching for deployments
- Non-functional development mode for GitHub Pages projects

## Proposed Enhancements

### 1. Add `site.basePath` Property Support
**Implementation**: Add `basePath` property to Site model with configuration support:
```java
// Site model enhancement
public class Site {
    private String basePath = "";
    public String getBasePath() { return basePath; }
}

// Configuration support
@ConfigProperty(name = "roq.site.base-path", defaultValue = "")
String basePath;

// Environment-specific values
%dev.roq.site.base-path=
%prod.roq.site.base-path=/my-project
```

**Template Usage**:
```html
<link rel="stylesheet" href="{site.basePath}/css/style.css">
<a href="{site.basePath}/docs/">Documentation</a>
```

### 2. Enhanced Frontmatter Property Access
**Implementation**: Enable custom frontmatter property access in templates:
```java
// Add to page model
public Object getFrontmatterProperty(String key) {
    return frontmatterData.get(key);
}

// Template extension
@TemplateExtension
public static Object frontmatter(Page page, String property) {
    return page.getFrontmatterProperty(property);
}
```

**Template Usage**:
```html
{#if page.frontmatter('disable-sidebar')}
  <!-- No sidebar layout -->
{/if}
{#if page.frontmatter('special-layout')}
  <div class="special">Custom content</div>
{/if}
```

### 3. Asset Path Helper Function
**Implementation**: Automatic base path resolution for assets:
```java
@TemplateExtension
public static String assetPath(Site site, String path) {
    String basePath = site.getBasePath();
    return basePath.isEmpty() ? path : basePath + path;
}
```

**Template Usage**:
```html
<link rel="stylesheet" href="{site.assetPath('/css/style.css')}">
<script src="{site.assetPath('/js/app.js')}"></script>
```

## Implementation Priority
1. **Phase 1**: Core `site.basePath` property and configuration support
2. **Phase 2**: Frontmatter property access and asset path helpers
3. **Phase 3**: Environment-specific configuration and validation

## Success Criteria
- ✅ `{site.basePath}` works in all templates
- ✅ Environment-specific configuration (`%dev.roq.site.base-path=`)
- ✅ Custom frontmatter properties accessible (`{page.frontmatter('key')}`)
- ✅ `quarkus:dev` works with base path configurations
- ✅ GitHub Pages deployment works seamlessly
- ✅ Backward compatibility maintained

## Real-World Impact
**Before**: DomTrip project requires hardcoded `/domtrip` paths, broken dev mode, manual deployment configuration
**After**: Clean templates with `{site.basePath}`, working development mode, automatic environment-specific deployment

## Configuration Examples
```properties
# GitHub Pages deployment
%dev.roq.site.base-path=
%prod.roq.site.base-path=/repository-name

# Corporate intranet subdirectory
%prod.roq.site.base-path=/docs/project-name

# Multi-environment setup
%dev.roq.site.base-path=
%staging.roq.site.base-path=/staging/project
%prod.roq.site.base-path=/project
```

## Benefits
- **Production Readiness**: Makes ROQ suitable for real-world deployments
- **GitHub Pages Support**: Seamless documentation hosting
- **Developer Experience**: Working dev mode with proper asset loading
- **Template Flexibility**: Conditional logic based on page metadata
- **Deployment Simplicity**: No manual path configuration required

This enhancement would transform ROQ from a promising tool into a production-ready static site generator that can compete with Jekyll, Hugo, and Docusaurus while maintaining Quarkus ecosystem benefits.
