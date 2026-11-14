import { defineConfig } from 'vitepress';
import { withMermaid } from 'vitepress-plugin-mermaid';

export default withMermaid(
  defineConfig({
    title: 'Inertia.js Java',
    description:
      'Server-side Inertia.js v3 adapters for Spring Boot and Quarkus — build modern single-page apps without a REST API.',
    base: '/inertiajs-java/',
    lang: 'en-US',
    lastUpdated: true,
    cleanUrls: false,
    head: [
      ['meta', { name: 'theme-color', content: '#9553e9' }],
      ['meta', { name: 'author', content: 'diovamny' }],
      [
        'meta',
        {
          name: 'keywords',
          content:
            'Inertia.js, Java, Spring Boot, Quarkus, Vue, React, Svelte, SSR, GraalVM',
        },
      ],
      ['meta', { property: 'og:type', content: 'website' }],
      ['meta', { property: 'og:site_name', content: 'Inertia.js Java' }],
    ],
    markdown: {
      lineNumbers: true,
      theme: { light: 'github-light', dark: 'github-dark' },
    },
    themeConfig: {
      nav: [
        { text: 'Guide', link: '/guide/introduction', activeMatch: '/guide/' },
        { text: 'Frameworks', link: '/guide/spring-boot', activeMatch: '/guide/(spring|quarkus)' },
        { text: 'Props', link: '/guide/props', activeMatch: '/guide/(props|live-props|shared-data)' },
        { text: 'Reference', link: '/reference/configuration', activeMatch: '/reference/' },
        { text: 'Changelog', link: '/reference/changelog' },
      ],
      sidebar: [
        {
          text: 'Getting Started',
          collapsed: false,
          items: [
            { text: 'Introduction', link: '/guide/introduction' },
            { text: 'How It Works', link: '/guide/how-it-works' },
            { text: 'Starter Kits', link: '/guide/starter-kits' },
            { text: 'Server Setup', link: '/guide/server-setup' },
            { text: 'Client Setup', link: '/guide/client-setup' },
            { text: 'Root Template', link: '/guide/root-template' },
          ],
        },
        {
          text: 'Core Concepts',
          collapsed: true,
          items: [
            { text: 'Pages', link: '/guide/pages' },
            { text: 'Responses & BigInt', link: '/guide/responses' },
            { text: 'Routing', link: '/guide/routing' },
            { text: 'Redirects', link: '/guide/redirects' },
            { text: 'Title & Meta', link: '/guide/title-and-meta' },
            { text: 'View Data', link: '/guide/viewdata' },
          ],
        },
        {
          text: 'Data & Props',
          collapsed: true,
          items: [
            { text: 'Shared Data', link: '/guide/shared-data' },
            { text: 'Props', link: '/guide/props' },
            {
              text: 'Live Props',
              link: '/guide/live-props',
              badge: { text: '0.0.6', type: 'tip' },
            },
            { text: 'Partial Reloads', link: '/guide/partial-reloads' },
          ],
        },
        {
          text: 'Forms & UX',
          collapsed: true,
          items: [
            { text: 'Forms', link: '/guide/forms' },
            { text: 'Validation', link: '/guide/validation' },
            { text: 'Precognition', link: '/guide/precognition' },
            { text: 'File Uploads', link: '/guide/file-uploads' },
            { text: 'Scroll Management', link: '/guide/scroll-management' },
            { text: 'History', link: '/guide/history' },
            { text: 'Asset Versioning', link: '/guide/asset-versioning' },
          ],
        },
        {
          text: 'Frameworks',
          collapsed: true,
          items: [
            { text: 'Spring Boot', link: '/guide/spring-boot' },
            { text: 'Quarkus', link: '/guide/quarkus' },
            { text: 'Quarkus Reactive', link: '/guide/quarkus-reactive' },
          ],
        },
        {
          text: 'Advanced',
          collapsed: true,
          items: [
            { text: 'SSR', link: '/guide/ssr' },
            { text: 'Testing', link: '/guide/testing' },
            { text: 'Security', link: '/guide/security' },
            { text: 'Native Image', link: '/guide/native-image' },
            { text: 'Performance', link: '/guide/performance' },
          ],
        },
        {
          text: 'Reference',
          collapsed: true,
          items: [
            { text: 'Configuration', link: '/reference/configuration' },
            { text: 'Protocol & TCK', link: '/reference/protocol' },
            { text: 'Not Supported (ADR-007)', link: '/reference/not-supported' },
            { text: 'Migration', link: '/reference/migration' },
            { text: 'Changelog', link: '/reference/changelog' },
          ],
        },
      ],
      search: {
        provider: 'local',
        options: {
          detailedView: true,
          miniSearch: {
            searchOptions: {
              fuzzy: 0.2,
              prefix: true,
              boost: { title: 4, text: 2, titles: 1 },
            },
          },
        },
      },
      editLink: {
        pattern:
          'https://github.com/diovamny/inertiajs-java/edit/main/docs-site/:path',
        text: 'Edit this page on GitHub',
      },
      socialLinks: [
        {
          icon: 'github',
          link: 'https://github.com/diovamny/inertiajs-java',
        },
      ],
      outline: { level: [2, 3], label: 'On this page' },
      docFooter: { prev: 'Previous page', next: 'Next page' },
      footer: {
        message:
          'Released under the Apache 2.0 License. Inertia.js v3 protocol · 11 TCK suites.',
        copyright: 'Copyright © 2026 diovamny',
      },
      lastUpdatedText: 'Last updated',
      darkModeSwitchLabel: 'Appearance',
      sidebarMenuLabel: 'Menu',
      returnToTopLabel: 'Return to top',
    },
    vite: {
      optimizeDeps: {
        include: ['mermaid'],
      },
    },
  }),
);
