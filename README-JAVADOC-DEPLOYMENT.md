# Enable Javadoc Deployment

## Issue
Javadocs are not appearing at https://maveniverse.github.io/domtrip/javadoc/ because the GitHub Actions workflow needs to be updated to generate them during deployment.

## Required Change

**File**: `.github/workflows/deploy-website.yml`  
**Line 34**: Update the build step to include Javadoc generation.

### Current (line 33-34):
```yaml
    - name: Build website
      run: QUARKUS_ROQ_GENERATOR_BATCH=true ./mvnw -f website clean package quarkus:run -DskipTests
```

### Updated (line 33-34):
```yaml
    - name: Build website with Javadocs
      run: QUARKUS_ROQ_GENERATOR_BATCH=true ./mvnw -f website clean package quarkus:run -DskipTests -Pinclude-javadocs -Dinclude.javadocs=true
```

## What This Does
1. Activates the `include-javadocs` Maven profile during CI/CD build
2. Generates fresh Javadocs for the current code
3. Includes them in the static site generation
4. Deploys complete documentation with API docs

## Result
After this change is applied and the workflow runs:
- ✅ https://maveniverse.github.io/domtrip/javadoc/ will work
- ✅ https://maveniverse.github.io/domtrip/javadoc/latest/ will show API docs
- ✅ https://maveniverse.github.io/domtrip/javadoc/snapshot/ will show API docs
- ✅ Navigation links will work properly

This completes the Javadoc integration implemented in PR #12.
