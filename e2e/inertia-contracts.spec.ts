import { test, expect, type APIRequestContext } from '@playwright/test';

/**
 * Common Inertia.js v3 browser/API contract suite.
 *
 * Runs unmodified against the Spring and the Quarkus kitchen-sink demos;
 * the target is selected with E2E_BASE_URL (see e2e CI job). Keeping one
 * identical list of cases for both adapters prevents Spring/Quarkus drift.
 */

interface PageObject {
  component: string;
  props: Record<string, unknown>;
  url: string;
  version: string;
}

async function readInitialPage(request: APIRequestContext): Promise<{ page: PageObject; headers: Record<string, string> }> {
  const response = await request.get('/login');
  expect(response.status()).toBe(200);
  expect(response.headers()['content-type'] ?? '').toContain('text/html');
  // An initial (non-Inertia) visit must NOT carry the X-Inertia response header.
  expect(response.headers()['x-inertia']).toBeUndefined();
  const html = await response.text();
  expect(html).toContain('id="app"');
  const match = html.match(/<script[^>]*data-page="app"[^>]*>([\s\S]*?)<\/script>/);
  expect(match, 'data-page JSON script must be present').not.toBeNull();
  const page = JSON.parse(match![1]) as PageObject;
  expect(page.component).toBe('Auth/Login');
  expect(page.url).toBe('/login');
  expect(typeof page.version).toBe('string');
  expect(page.version.length).toBeGreaterThan(0);
  expect(page.props).toBeDefined();
  expect('status' in page.props).toBe(true);
  return { page, headers: response.headers() };
}

test.describe('Inertia v3 contracts (/login)', () => {
  test('initial visit returns the HTML shell with the page object', async ({ request }) => {
    await readInitialPage(request);
  });

  test('Inertia visit returns the JSON page object', async ({ request }) => {
    const { page } = await readInitialPage(request);
    const response = await request.get('/login', {
      headers: { 'X-Inertia': 'true', 'X-Inertia-Version': page.version },
    });
    expect(response.status()).toBe(200);
    expect(response.headers()['x-inertia']).toBe('true');
    const body = (await response.json()) as PageObject;
    expect(body.component).toBe('Auth/Login');
    expect(body.url).toBe('/login');
    expect('status' in body.props).toBe(true);
  });

  test('partial reload returns only the requested props', async ({ request }) => {
    const { page } = await readInitialPage(request);
    const response = await request.get('/login', {
      headers: {
        'X-Inertia': 'true',
        'X-Inertia-Version': page.version,
        'X-Inertia-Partial-Data': 'status',
        'X-Inertia-Partial-Component': 'Auth/Login',
      },
    });
    expect(response.status()).toBe(200);
    const body = (await response.json()) as PageObject;
    expect(body.component).toBe('Auth/Login');
    expect('status' in body.props).toBe(true);
  });

  test('stale asset version forces a full reload with 409', async ({ request }) => {
    const response = await request.get('/login', {
      headers: { 'X-Inertia': 'true', 'X-Inertia-Version': 'stale-version-for-e2e' },
    });
    expect(response.status()).toBe(409);
    expect(response.headers()['x-inertia-location']).toBe('/login');
  });

  test('browser renders the login page without console errors', async ({ page }) => {
    const errors: string[] = [];
    page.on('pageerror', (error) => errors.push(String(error)));
    page.on('console', (message) => {
      if (message.type() === 'error') errors.push(message.text());
    });
    // Fail fast with diagnostics if the server does not serve the shell.
    const response = await page.goto('/login');
    expect(response?.status(), 'GET /login must return 200 HTML').toBe(200);
    await expect(page.locator('#app')).toBeAttached();
    // CI diagnostics: log what the browser actually received before asserting.
    // These lines stay in the CI stdout and explain mount failures.
    const appChildren = await page.locator('#app').evaluate((el) => el.childElementCount);
    const bodySnippet = (await page.content()).slice(0, 300).replace(/\s+/g, ' ');
    console.log(`[e2e-diag] url=${page.url()} #app-children=${appChildren} body-start=${bodySnippet}`);
    // Generous timeout: cold CI runners need seconds to parse the bundle
    // and mount the app on first paint.
    await expect(page.locator('#email')).toBeVisible({ timeout: 30000 });
    await expect(page.getByRole('button', { name: 'Log in' })).toBeVisible({ timeout: 15000 });
    await expect(page).toHaveTitle(/Log in/);
    expect(errors).toEqual([]);
  });
});
