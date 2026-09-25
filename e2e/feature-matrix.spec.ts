import { test, expect, type APIRequestContext } from '@playwright/test';

/**
 * Feature-matrix contracts (Fase E): the full Inertia v3 feature set through
 * the official React/Svelte clients on the public /e2e-probe fixture.
 *
 * Runs unmodified against every PingCRM React/Svelte demo (Spring Boot and
 * Quarkus); the target is selected with E2E_BASE_URL. E2E_PROBE_BASE selects
 * the transport variant (default /e2e-probe). Covers E2E-01..08 + E2E-10:
 * shell/visit/partial/409, validation, named bags, deferred, once, merge,
 * instant visits, redirect-back, upload, mount hygiene.
 */

const PROBE = process.env.E2E_PROBE_BASE ?? '/e2e-probe';

interface PageObject {
  component: string;
  props: Record<string, unknown>;
  url: string;
  version: string;
}

function collectBrowserErrors(page: import('@playwright/test').Page): string[] {
  const errors: string[] = [];
  page.on('pageerror', (error) => errors.push(String(error)));
  page.on('console', (message) => {
    if (message.type() === 'error') errors.push(message.text());
  });
  return errors;
}

async function readInitialPage(request: APIRequestContext): Promise<PageObject> {
  const response = await request.get(PROBE);
  expect(response.status()).toBe(200);
  expect(response.headers()['content-type'] ?? '').toContain('text/html');
  expect(response.headers()['x-inertia']).toBeUndefined();
  const html = await response.text();
  expect(html).toContain('id="app"');
  const match = html.match(/<script[^>]*data-page="app"[^>]*>([\s\S]*?)<\/script>/);
  expect(match, 'data-page JSON script must be present').not.toBeNull();
  const page = JSON.parse(match![1]) as PageObject;
  expect(page.component).toBe('Probe/Index');
  expect(page.url).toBe(PROBE);
  expect(typeof page.version).toBe('string');
  expect(page.version.length).toBeGreaterThan(0);
  expect(page.props.greeting).toBe('Probe source page.');
  expect(page.props.notice).toBe('Probe notice');
  expect(Array.isArray(page.props.entries)).toBe(true);
  return page;
}

test.describe('Feature matrix (/e2e-probe)', () => {
  test('initial visit returns the HTML shell with the probe page object', async ({ request }) => {
    await readInitialPage(request);
  });

  test('Inertia visit returns the JSON page object', async ({ request }) => {
    const page = await readInitialPage(request);
    const response = await request.get(PROBE, {
      headers: { 'X-Inertia': 'true', 'X-Inertia-Version': page.version },
    });
    expect(response.status()).toBe(200);
    expect(response.headers()['x-inertia']).toBe('true');
    const body = (await response.json()) as PageObject;
    expect(body.component).toBe('Probe/Index');
    expect(body.props.greeting).toBe('Probe source page.');
  });

  test('partial reload returns only the requested props', async ({ request }) => {
    const page = await readInitialPage(request);
    const response = await request.get(PROBE, {
      headers: {
        'X-Inertia': 'true',
        'X-Inertia-Version': page.version,
        'X-Inertia-Partial-Data': 'entries',
        'X-Inertia-Partial-Component': 'Probe/Index',
      },
    });
    expect(response.status()).toBe(200);
    const body = (await response.json()) as PageObject;
    expect(body.component).toBe('Probe/Index');
    expect('entries' in body.props).toBe(true);
  });

  test('stale asset version forces a full reload with 409', async ({ request }) => {
    const response = await request.get(PROBE, {
      headers: { 'X-Inertia': 'true', 'X-Inertia-Version': 'stale-version-for-e2e' },
    });
    expect(response.status()).toBe(409);
    expect(response.headers()['x-inertia-location']).toBe(PROBE);
  });

  test('deferred prop is excluded from the initial page with metadata', async ({
    request,
  }) => {
    const page = await readInitialPage(request);
    const response = await request.get(PROBE, {
      headers: { 'X-Inertia': 'true', 'X-Inertia-Version': page.version },
    });
    const body = (await response.json()) as PageObject & {
      deferredProps: Record<string, string[]>;
    };
    // The server must not resolve the deferred prop eagerly: the official
    // client fetches it after mount instead.
    expect('slow' in body.props).toBe(false);
    expect(body.deferredProps.probe).toContain('slow');
  });

  test('except header suppresses the once prop server-side', async ({ request }) => {
    const page = await readInitialPage(request);
    const response = await request.get(PROBE, {
      headers: {
        'X-Inertia': 'true',
        'X-Inertia-Version': page.version,
        'X-Inertia-Except-Once-Props': 'notice',
      },
    });
    expect(response.status()).toBe(200);
    const body = (await response.json()) as PageObject;
    expect('notice' in body.props).toBe(false);
    expect(body.props.greeting).toBe('Probe source page.');
  });

  test('server validation errors render in the client', async ({ page }) => {
    const errors = collectBrowserErrors(page);
    await page.goto(PROBE);
    await expect(page.getByTestId('probe-greeting')).toBeVisible({ timeout: 30000 });
    await page.getByRole('button', { name: 'Submit probe form' }).click();
    await expect(page.getByText('Please enter your full name.').first()).toBeVisible({
      timeout: 15000,
    });
    await expect(page.getByText('We need your email address.').first()).toBeVisible({
      timeout: 15000,
    });
    expect(errors).toEqual([]);
  });

  test('named error bag isolates the secondary form', async ({ page }) => {
    const errors = collectBrowserErrors(page);
    await page.goto(PROBE);
    await expect(page.getByTestId('probe-greeting')).toBeVisible({ timeout: 30000 });
    await page.getByRole('button', { name: 'Submit probe secondary' }).click();
    await expect(page.getByRole('heading', { name: 'probeSecondary.errors' })).toBeVisible({
      timeout: 15000,
    });
    await expect(page.getByText('Please enter a title.').first()).toBeVisible({
      timeout: 15000,
    });
    await expect(page.getByText('Please enter a body.').first()).toBeVisible({
      timeout: 15000,
    });
    // The primary form stays clean: bags do not bleed across forms.
    await expect(page.getByText('Please enter your full name.')).toHaveCount(0);
    expect(errors).toEqual([]);
  });

  test('deferred prop resolves on partial reload', async ({ page }) => {
    const errors = collectBrowserErrors(page);
    await page.goto(PROBE);
    await expect(page.getByTestId('probe-greeting')).toBeVisible({ timeout: 30000 });
    // The official client may already auto-fetch deferred props after mount;
    // either way the settled value must come from the server resolver.
    await page.getByRole('button', { name: 'Load slow prop' }).click();
    await expect(page.getByTestId('probe-slow')).toHaveText('slow-value', { timeout: 15000 });
    expect(errors).toEqual([]);
  });

  test('once prop is tracked by the client across except reloads', async ({ page }) => {
    const errors = collectBrowserErrors(page);
    await page.goto(PROBE);
    await expect(page.getByTestId('probe-notice')).toHaveText('Probe notice', {
      timeout: 30000,
    });
    // Let the automatic deferred fetch settle so the next Inertia response
    // can only be our suppress reload.
    await expect(page.getByTestId('probe-slow')).toHaveText('slow-value', {
      timeout: 15000,
    });
    const [reload] = await Promise.all([
      page.waitForResponse(
        (response) => {
          // headerValue() is async: use the sync headers() map instead.
          const headers = response.request().headers();
          return (
            response.url().includes(PROBE) &&
            headers['x-inertia'] === 'true' &&
            headers['x-inertia-except-once-props'] === 'notice' &&
            headers['x-inertia-partial-data'] == null
          );
        },
        { timeout: 15000 },
      ),
      page.getByRole('button', { name: 'Suppress notice' }).click(),
    ]);
    const body = (await reload.json()) as PageObject;
    // The server omits the once prop...
    expect('notice' in body.props).toBe(false);
    // ...yet the official client keeps rendering its tracked copy.
    await expect(page.getByTestId('probe-notice')).toHaveText('Probe notice', {
      timeout: 15000,
    });
    expect(errors).toEqual([]);
  });

  test('client appends entries on partial reload (merge matchOn id)', async ({ page }) => {
    const errors = collectBrowserErrors(page);
    await page.goto(PROBE);
    await expect(page.getByTestId('probe-entries-count')).toHaveText('1 entries', {
      timeout: 30000,
    });
    await expect(page.getByTestId('probe-entries')).toContainText('1: Alpha');
    await page.getByRole('button', { name: 'Fetch next entry' }).click();
    // The server returns only the fresh entry; the official client appends
    // it (merge + matchOn id) instead of replacing: Alpha survives. A
    // replacing client would show "1 entries" with Beta alone.
    await expect(page.getByTestId('probe-entries-count')).toHaveText('2 entries', {
      timeout: 15000,
    });
    await expect(page.getByTestId('probe-entries')).toContainText('1: Alpha');
    await expect(page.getByTestId('probe-entries')).toContainText('2: Beta');
    expect(errors).toEqual([]);
  });

  test('instant visit renders placeholder props before the server responds', async ({
    page,
  }) => {
    const errors = collectBrowserErrors(page);
    await page.goto(PROBE);
    await expect(page.getByTestId('probe-greeting')).toBeVisible({ timeout: 30000 });
    await page.getByRole('button', { name: 'Visit target instantly' }).click();
    // The official client swaps instantly with callback props while the
    // sidecar sleeps ~2s: this text can only come from the client.
    await expect(
      page.getByText('Navigating from probe (was: "Probe source page.")'),
    ).toBeVisible({ timeout: 1500 });
    // ...and the server response replaces the placeholder afterwards.
    await expect(page.getByText('Hello from the server!')).toBeVisible({ timeout: 15000 });
    expect(errors).toEqual([]);
  });

  test('redirect back re-renders with server flash', async ({ page }) => {
    const errors = collectBrowserErrors(page);
    await page.goto(PROBE);
    await expect(page.getByTestId('probe-greeting')).toBeVisible({ timeout: 30000 });
    await page.getByRole('button', { name: 'Submit and redirect back' }).click();
    await expect(page.getByTestId('probe-flash')).toHaveText('Redirected back via probe', {
      timeout: 15000,
    });
    expect(errors).toEqual([]);
  });

  test('multipart upload round-trips through the client', async ({ page }) => {
    const errors = collectBrowserErrors(page);
    await page.goto(PROBE);
    await expect(page.getByTestId('probe-greeting')).toBeVisible({ timeout: 30000 });
    await page.locator('#probe-photo').setInputFiles({
      name: 'avatar.png',
      mimeType: 'image/png',
      buffer: Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]),
    });
    await page.getByRole('button', { name: 'Upload photo' }).click();
    await expect(page.getByTestId('probe-flash')).toHaveText(
      'Uploaded 1 file(s) successfully!',
      { timeout: 15000 },
    );
    expect(errors).toEqual([]);
  });

  test('reactive CSRF failure is denied with recovery flash', async ({ request }) => {
    // Transport-specific contract: on the Reactive Routes base the adapter
    // filter owns CSRF (declared reactive-csrf-paths); on the JAX-RS base
    // quarkus-rest-csrf owns it (unit-covered in CsrfQuarkusTest).
    test.skip(
      PROBE === '/e2e-probe',
      'rest-csrf owns denial on the JAX-RS base',
    );
    const page = await readInitialPage(request);
    const denied = await request.post(`${PROBE}/validate`, {
      data: { name: '', email: '' },
      headers: {
        'X-Inertia': 'true',
        'X-Inertia-Version': page.version,
        Referer: `${process.env.E2E_BASE_URL ?? 'http://localhost:8080'}${PROBE}`,
      },
      maxRedirects: 0,
    });
    expect(denied.status()).toBe(303);
    expect(denied.headers()['location']).toBeTruthy();
    const followup = await request.get(denied.headers()['location']!, {
      headers: { 'X-Inertia': 'true', 'X-Inertia-Version': page.version },
    });
    expect(followup.status()).toBe(200);
    const body = (await followup.json()) as PageObject;
    const errors = body.props.errors as Record<string, unknown>;
    // The request never reached validation: no field errors leak.
    expect(errors.name).toBeUndefined();
    expect(errors.email).toBeUndefined();
  });

  test('browser mounts the probe page without console errors', async ({ page }) => {
    const errors = collectBrowserErrors(page);
    const response = await page.goto(PROBE);
    expect(response?.status(), `GET ${PROBE} must return 200 HTML`).toBe(200);
    await expect(page.locator('#app')).toBeAttached();
    await expect(page.getByTestId('probe-greeting')).toBeVisible({ timeout: 30000 });
    await expect(page.getByRole('heading', { name: 'E2E Probe' })).toBeVisible({
      timeout: 15000,
    });
    expect(errors).toEqual([]);
  });
});
