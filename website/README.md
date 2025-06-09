# DomTrip Website

This module contains the DomTrip documentation website built with [ROQ](https://iamroq.com/), a static site generator powered by Quarkus.

## Building the Website

From the project root, build the static website:

```shell script
QUARKUS_ROQ_GENERATOR_BATCH=true ./mvnw -f website clean package quarkus:run -DskipTests
```

The generated static site will be available in `website/target/dist/`.

## Development Mode

For live development with hot reload:

```shell script
cd website
../mvnw quarkus:dev
```

The development server will be available at <http://localhost:8080>.

## Content Structure

- `content/` - Markdown documentation files
- `templates/` - Qute templates for page layouts
- `public/` - Static assets (CSS, JS, images)
- `data/` - YAML data files (menu, authors)

## Related Links

- [ROQ Documentation](https://iamroq.com/docs/)
- [DomTrip Main Project](../README.md)
