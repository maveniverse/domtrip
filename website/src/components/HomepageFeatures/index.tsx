import clsx from 'clsx';
import Heading from '@theme/Heading';
import styles from './styles.module.css';

type FeatureItem = {
  title: string;
  Svg: React.ComponentType<React.ComponentProps<'svg'>>;
  description: JSX.Element;
};

const FeatureList: FeatureItem[] = [
  {
    title: 'Lossless Round-Trip',
    Svg: require('@site/static/img/undraw_docusaurus_mountain.svg').default,
    description: (
      <>
        DomTrip preserves <strong>every detail</strong> of your XML: comments, whitespace,
        entity encoding, attribute quote styles, and formatting. Parse and serialize
        with zero information loss.
      </>
    ),
  },
  {
    title: 'Easy Editing with Formatting Preservation',
    Svg: require('@site/static/img/undraw_docusaurus_tree.svg').default,
    description: (
      <>
        Make changes to your XML while keeping the original formatting intact.
        Only modified sections are reformatted, using intelligent indentation
        inference for new content.
      </>
    ),
  },
  {
    title: 'Modern Java API',
    Svg: require('@site/static/img/undraw_docusaurus_react.svg').default,
    description: (
      <>
        Built for Java 17+ with fluent builders, Stream-based navigation,
        comprehensive namespace support, and type-safe configuration.
        Clean, intuitive API that feels natural.
      </>
    ),
  },
];

function Feature({title, Svg, description}: FeatureItem) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        <Svg className={styles.featureSvg} role="img" />
      </div>
      <div className="text--center padding-horiz--md">
        <Heading as="h3">{title}</Heading>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures(): JSX.Element {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
