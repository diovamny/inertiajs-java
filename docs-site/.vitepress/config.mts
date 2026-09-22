import { defineConfig } from 'vitepress';

export default defineConfig({
  title: 'Inertia.js Java',
  description: 'Server-side Inertia.js v3 adapters for Spring Boot and Quarkus',
  base: '/inertiajs-java/',
  themeConfig: {
    nav: [
      { text: 'Guide', link: '/guide/spring-boot' },
      { text: 'Features', link: '/features/props' },
      { text: 'Compatibility', link: '/compatibility' },
      { text: 'Changelog', link: '/changelog' },
    ],
    sidebar: [
      {
        text: 'Guide',
        items: [
          { text: 'Quickstart', link: '/' },
          { text: 'Spring Boot', link: '/guide/spring-boot' },
          { text: 'Quarkus', link: '/guide/quarkus' },
          { text: 'Quarkus Reactive', link: '/guide/quarkus-reactive' },
        ],
      },
      {
        text: 'Features',
        items: [
          { text: 'Shared data', link: '/features/shared-data' },
          { text: 'Partial reloads', link: '/features/partial-reloads' },
          { text: 'Props', link: '/features/props' },
          { text: 'Validation', link: '/features/validation' },
          { text: 'Uploads', link: '/features/uploads' },
          { text: 'SSR', link: '/features/ssr' },
          { text: 'History', link: '/features/history' },
          { text: 'Asset versioning', link: '/features/asset-versioning' },
        ],
      },
      {
        text: 'Reference',
        items: [
          { text: 'Testing', link: '/testing' },
          { text: 'Security', link: '/security' },
          { text: 'Native Image', link: '/native' },
          { text: 'Performance', link: '/performance' },
          { text: 'Compatibility', link: '/compatibility' },
          { text: 'Not supported', link: '/not-supported' },
          { text: 'Migration', link: '/migration' },
          { text: 'Changelog', link: '/changelog' },
        ],
      },
    ],
    socialLinks: [
      { icon: 'github', link: 'https://github.com/diovamny/inertiajs-java' },
    ],
  },
});
