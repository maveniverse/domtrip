import type {SidebarsConfig} from '@docusaurus/plugin-content-docs';

/**
 * Creating a sidebar enables you to:
 - create an ordered group of docs
 - render a sidebar for each doc of that group
 - provide next/previous navigation

 The sidebars can be generated from the filesystem, or explicitly defined here.

 Create as many sidebars as you want.
 */
const sidebars: SidebarsConfig = {
  // By default, Docusaurus generates a sidebar from the docs folder structure
  tutorialSidebar: [
    'intro',
    {
      type: 'category',
      label: 'Getting Started',
      items: [
        'getting-started/installation',
        'getting-started/quick-start',
        'getting-started/basic-concepts',
      ],
    },
    {
      type: 'category',
      label: 'Core Features',
      items: [
        'features/lossless-parsing',
        'features/formatting-preservation',
        'features/namespace-support',
      ],
    },
    {
      type: 'category',
      label: 'Advanced Topics',
      items: [
        'advanced/builder-patterns',
      ],
    },
    {
      type: 'category',
      label: 'API Reference',
      items: [
        'api/editor',
        'api/configuration',
      ],
    },
    {
      type: 'category',
      label: 'Examples',
      items: [
        'examples/basic-editing',
      ],
    },
    'comparison',
    'migration',
  ],
};

export default sidebars;
