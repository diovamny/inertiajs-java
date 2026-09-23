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

  async function collectBrowserErrors(page: import('@playwright/test').Page): string[] {
    const errors: string[] = [];
    page.on('pageerror', (error) => errors.push(String(error)));
    page.on('console', (message) => {
      if (message.type() === 'error') errors.push(message.text());
    });
    return errors;
  }

  async function loginAsTestUser(page: import('@playwright/test').Page): Promise<void> {
    await page.goto('/login');
    await expect(page.locator('#email')).toBeVisible({ timeout: 30000 });
    await page.locator('#password').fill('password');
    await page.getByRole('button', { name: 'Log in' }).click();
    // Successful login leaves /login (dashboard or first authenticated page).
    await expect(page).not.toHaveURL(/\/login$/, { timeout: 30000 });
  }

  test('server validation errors render in the Vue client (M2 proof)', async ({ page }) => {
    const errors = await collectBrowserErrors(page);
    await loginAsTestUser(page);
    await page.goto('/features/forms/validation');
    await expect(page.getByRole('heading', { name: 'Validation' }).first()).toBeVisible({ timeout: 30000 });
    // Submit the primary form empty: the server answers 303 + flash, the
    // official client re-renders with the error bag (M2 wire shape).
    await page.locator('#v-name').fill('');
    await page.locator('#v-email').fill('');
    await page.getByRole('button', { name: 'Submit', exact: true }).first().click();
    await expect(page.getByText('Please enter your full name.').first()).toBeVisible({ timeout: 15000 });
    await expect(page.getByText('We need your email address.').first()).toBeVisible({ timeout: 15000 });
    await expect(page.getByRole('heading', { name: 'form.errors' })).toBeVisible({ timeout: 15000 });
    expect(errors).toEqual([]);
  });

  test('named error bag isolates the secondary form (M2 proof)', async ({ page }) => {
    const errors = await collectBrowserErrors(page);
    await loginAsTestUser(page);
    await page.goto('/features/forms/validation');
    await expect(page.getByRole('heading', { name: 'Validation' }).first()).toBeVisible({ timeout: 30000 });
    await page.locator('#v2-title').fill('');
    await page.getByRole('button', { name: 'Submit (Error Bag)' }).click();
    await expect(page.getByRole('heading', { name: 'secondaryForm.errors' })).toBeVisible({ timeout: 15000 });
    // The primary form stays clean: bags do not bleed across forms.
    await expect(page.getByText('Please enter your full name.')).toHaveCount(0);
    expect(errors).toEqual([]);
  });

  test('client appends notifications on partial reload (M3 proof)', async ({ page }) => {
    const errors = await collectBrowserErrors(page);
    await loginAsTestUser(page);
    await page.goto('/features/data-loading/prop-merging');
    await expect(page.getByRole('button', { name: 'Add Notification' })).toBeVisible({ timeout: 30000 });
    const badge = page.getByText(/^\d+ total$/).first();
    await expect(badge).toBeVisible();
    const before = Number.parseInt((await badge.textContent()) ?? '0', 10);
    expect(before).toBeGreaterThanOrEqual(1);
    // The server returns exactly one fresh notification; the official client
    // appends it (mergeProps) instead of replacing: total grows by one.
    await page.getByRole('button', { name: 'Add Notification' }).click();
    await expect(page.getByText(`${before + 1} total`).first()).toBeVisible({ timeout: 15000 });
    expect(errors).toEqual([]);
  });

  test('instant visit renders shared and current props before the server responds (PROTO-057B proof)', async ({ page }) => {
    const errors = await collectBrowserErrors(page);
    await loginAsTestUser(page);
    await page.goto('/features/navigation/instant-visits');
    await expect(page.getByRole('button', { name: 'Visit with Callback' })).toBeVisible({ timeout: 30000 });
    await page.getByRole('button', { name: 'Visit with Callback' }).click();
    // The official client swaps instantly with current + shared props while
    // the sidecar sleeps ~2s: this text can only come from the client.
    await expect(
      page.getByText('Navigating from source (was: "This is the source page.")'),
    ).toBeVisible({ timeout: 1500 });
    // ...and the server response replaces the placeholder afterwards.
    await expect(page.getByText('Hello from the server!')).toBeVisible({ timeout: 15000 });
    expect(errors).toEqual([]);
  });

  test('multipart upload round-trips through the Vue client', async ({ page }) => {
    const errors = await collectBrowserErrors(page);
    await loginAsTestUser(page);
    await page.goto('/features/forms/file-uploads');
    await expect(page.getByRole('button', { name: 'Upload' })).toBeVisible({ timeout: 30000 });
    await page.locator('#photo').setInputFiles({
      name: 'avatar.png',
      mimeType: 'image/png',
      buffer: Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]),
    });
    await expect(page.getByText('avatar.png', { exact: true })).toBeVisible({ timeout: 15000 });
    await page.getByRole('button', { name: 'Upload' }).click();
    // onSuccess resets the form: the placeholder returns, errors stay empty.
    await expect(page.getByText('Choose image...')).toBeVisible({ timeout: 15000 });
    expect(errors).toEqual([]);
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
    // Decisive checks: is the bundle served, does it throw on load, which browser?
    const assetStatus = await page.request.get('/assets/app.js').then(
      (r) => r.status(),
      (e) => `fetch-error:${String(e).slice(0, 80)}`,
    );
    console.log(`[e2e-diag] asset-appjs=${assetStatus} ua=${await page.evaluate(() => navigator.userAgent)}`);
    await page.waitForTimeout(2000);
    console.log(`[e2e-diag] console-errors-so-far=${JSON.stringify(errors)} #app-children-after=${await page.locator('#app').evaluate((el) => el.childElementCount)}`);
    // Generous timeout: cold CI runners need seconds to parse the bundle
    // and mount the app on first paint.
    await expect(page.locator('#email')).toBeVisible({ timeout: 30000 });
    await expect(page.getByRole('button', { name: 'Log in' })).toBeVisible({ timeout: 15000 });
    await expect(page).toHaveTitle(/Log in/);
    expect(errors).toEqual([]);
  });
});
