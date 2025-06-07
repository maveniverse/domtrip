# DomTrip Website

This directory contains the source code for the DomTrip documentation website, built with [Docusaurus](https://docusaurus.io/).

## Development

### Prerequisites

- Node.js 18 or higher
- npm or yarn

### Installation

```bash
npm install
```

### Local Development

```bash
npm start
```

This command starts a local development server and opens up a browser window. Most changes are reflected live without having to restart the server.

### Build

```bash
npm run build
```

This command generates static content into the `target/build` directory and can be served using any static contents hosting service.

### Code Snippets

The website automatically extracts code snippets from the demo classes in the main project:

```bash
# Extract snippets from demo classes
npm run extract-snippets

# Validate that all referenced snippets exist
npm run validate-snippets

# Start development server with fresh snippets
npm run start-with-snippets
```

### Deployment

The website is automatically deployed to GitHub Pages when changes are pushed to the main branch. The deployment process:

1. Extracts code snippets from demo classes
2. Validates all snippet references
3. Builds the static site
4. Deploys to GitHub Pages

## Project Structure

```
website/
├── docs/                    # Documentation markdown files
│   ├── getting-started/     # Getting started guides
│   ├── features/           # Feature documentation
│   ├── api/                # API reference
│   ├── advanced/           # Advanced usage guides
│   └── examples/           # Example use cases
├── src/                    # React components and pages
│   ├── components/         # Reusable components
│   ├── css/               # Custom CSS
│   └── pages/             # Custom pages
├── static/                 # Static assets
│   └── img/               # Images and icons
├── scripts/               # Build and utility scripts
│   ├── extract-snippets.js # Extract code from demos
│   ├── validate-snippets.js # Validate snippet references
│   └── replace-version.js  # Version placeholder replacement
└── target/                # Build output
    ├── build/             # Generated static site
    └── static/            # Generated static assets
        └── snippets/      # Extracted code snippets
```

## Writing Documentation

### Adding New Pages

1. Create a new markdown file in the appropriate directory under `docs/`
2. Add frontmatter with `sidebar_position` to control ordering
3. Update `sidebars.ts` if creating new categories

### Using Code Snippets

Reference extracted code snippets in your documentation:

```markdown
import CodeBlock from '@theme/CodeBlock';
import MySnippet from '!!raw-loader!../target/static/snippets/MySnippet.java';

<CodeBlock language="java">{MySnippet}</CodeBlock>
```

### Styling

- Use the existing CSS classes for consistency
- Add custom styles to `src/css/custom.css`
- Use Docusaurus theme variables when possible

## Configuration

The main configuration is in `docusaurus.config.ts`. Key settings:

- **Site metadata**: Title, tagline, URL
- **Navigation**: Navbar and footer links
- **Theme**: Colors, syntax highlighting
- **Plugins**: Additional functionality

## Troubleshooting

### Build Fails

1. Check that all snippet references are valid: `npm run validate-snippets`
2. Ensure Node.js version is 18 or higher
3. Clear cache: `npm run clear`

### Missing Snippets

1. Run `npm run extract-snippets` to regenerate snippets
2. Check that demo classes exist in the expected location
3. Verify snippet references in markdown files

### Deployment Issues

1. Check GitHub Actions logs for detailed error messages
2. Ensure GitHub Pages is enabled in repository settings
3. Verify that the `deploy-website.yml` workflow has proper permissions

## Contributing

1. Make changes to documentation or website code
2. Test locally with `npm start`
3. Ensure all snippets are valid with `npm run validate-snippets`
4. Submit a pull request

The website will be automatically deployed when your changes are merged to the main branch.
