import { test, expect } from '@playwright/test';

/**
 * Reactive contracts (audit closure): wire-level proofs of the Inertia v3
 * contract on Quarkus Reactive Routes (@Route) for the Vue client, plus the
 * instant-visit proof on the JAX-RS twin (E2E-06 vue/quarkus-rest).
 *
 * Runs against the Quarkus kitchen-sink (E2E_BASE_URL, default
 * http://localhost:8081). Uses the browser context (page.request) so session
 * cookies and the XSRF token behave exactly like the official client.
 * Closes E2E-01..05 + E2E-08 + E2E-10 on vue/quarkus-reactive and E2E-06 on
 * vue/quarkus-rest. Fixtures: @Route props-wire/validate-wire/upload-wire in
 * NavigationController + JAX-RS /features/rest-instant/* (InstantRestResource).
 */

const RX = '/features/navigation/props-wire';
const RX_VALIDATE = '/features/navigation/validate-wire';
const RX_UPLOAD = '/features/navigation/upload-wire';
const REST_VISITS = '/features/rest-instant/visits';

interface PageObject {
  component: string;
  props: Record<string, unknown>;
  url: string;
  version: string;
}

function parsePage(html: string): PageObject {
  const match = html.match(/<script[^>]*data-page="app"[^>]*>([\s\S]*?)<\/script>/);
  expect(match, 'data-page JSON script must be present').not.toBeNull();
  return JSON.parse(match![1]) as PageObject;
}

async function loginAsTestUser(page: import('@playwright/test').Page): Promise<void> {
  await page.goto('/login');
  await expect(page.locator('#email')).toBeVisible({ timeout: 30000 });
  await page.locator('#password').fill('password');
  await page.getByRole('button', { name: 'Log in' }).click();
  await expect(page).not.toHaveURL(/\/login$/, { timeout: 30000 });
}

async function xsrfToken(page: import('@playwright/test').Page): Promise<string> {
  const cookies = await page.context().cookies();
  return cookies.find((c) => c.name === 'XSRF-TOKEN')?.value ?? '';
}

test.describe('Reactive wire contracts (Vue @Route)', () => {
  test('bootstrap serves the HTML shell with safe embedded JSON (E2E-01/10)', async ({
    page,
  }) => {
    await loginAsTestUser(page);
    const response = await page.request.get(RX);
    expect(response.status()).toBe(200);
    expect(response.headers()['content-type'] ?? '').toContain('text/html');
    expect(response.headers()['x-inertia']).toBeUndefined();
    const html = await response.text();
    expect(html).toContain('id="app"');
    const pageObject = parsePage(html);
    expect(pageObject.component).toBe('Features/Navigation/PropsWire');
    expect(pageObject.url).toBe(RX);
    expect(typeof pageObject.version).toBe('string');
    // SafeJsonEncoder escapes every forward slash: a </script> sequence can
    // never appear inside the JSON payload itself (only the real closing
    // tag that follows it).
    const scriptMatch = html.match(/<script[^>]*data-page="app"[^>]*>([\s\S]*?)<\/script>/);
    expect(scriptMatch).not.toBeNull();
    expect(scriptMatch![1].includes('</script')).toBe(false);
  });

  test('Inertia visit resolves deferred/once/merge/scroll metadata (E2E-05)', async ({
    page,
  }) => {
    await loginAsTestUser(page);
    const version = parsePage(await (await page.request.get(RX)).text()).version;
    const response = await page.request.get(RX, {
      headers: { 'X-Inertia': 'true', 'X-Inertia-Version': version },
    });
    expect(response.status()).toBe(200);
    expect(response.headers()['x-inertia']).toBe('true');
    const body = (await response.json()) as PageObject & {
      deferredProps: Record<string, string[]>;
      mergeProps: string[];
      matchPropsOn: string[];
      onceProps: Record<string, { prop: string }>;
      scrollProps: Record<string, { pageName: string; currentPage: number }>;
    };
    expect(body.props.greeting).toBe('Wire sampler.');
    expect(Array.isArray(body.props.entries)).toBe(true);
    expect('slow' in body.props).toBe(false);
    expect(body.deferredProps.probe).toContain('slow');
    expect(body.mergeProps).toContain('entries');
    expect(body.matchPropsOn).toContain('entries.id');
    expect(body.onceProps.notice.prop).toBe('notice');
    expect(body.props.notice).toBe('Probe notice');
    expect(body.scrollProps.items.pageName).toBe('page');
    expect(body.scrollProps.items.currentPage).toBe(1);
  });

  test('stale asset version forces a full reload with 409 (E2E-02)', async ({ page }) => {
    await loginAsTestUser(page);
    const response = await page.request.get(RX, {
      headers: { 'X-Inertia': 'true', 'X-Inertia-Version': 'stale-version-for-e2e' },
    });
    expect(response.status()).toBe(409);
    expect(response.headers()['x-inertia-location']).toBe(RX);
  });

  test('partial reload filters props on reactive routes (E2E-03)', async ({ page }) => {
    await loginAsTestUser(page);
    const version = parsePage(await (await page.request.get(RX)).text()).version;
    const only = await page.request.get(RX, {
      headers: {
        'X-Inertia': 'true',
        'X-Inertia-Version': version,
        'X-Inertia-Partial-Data': 'entries',
        'X-Inertia-Partial-Component': 'Features/Navigation/PropsWire',
      },
    });
    expect(only.status()).toBe(200);
    const onlyBody = (await only.json()) as PageObject;
    expect('entries' in onlyBody.props).toBe(true);
    expect('greeting' in onlyBody.props).toBe(false);
    expect('slow' in onlyBody.props).toBe(false);
    const except = await page.request.get(RX, {
      headers: {
        'X-Inertia': 'true',
        'X-Inertia-Version': version,
        'X-Inertia-Partial-Except': 'entries',
        'X-Inertia-Partial-Component': 'Features/Navigation/PropsWire',
      },
    });
    expect(except.status()).toBe(200);
    const exceptBody = (await except.json()) as PageObject;
    expect('entries' in exceptBody.props).toBe(false);
    expect(exceptBody.props.greeting).toBe('Wire sampler.');
  });

  test('except-once suppresses the once prop server-side (E2E-05)', async ({ page }) => {
    await loginAsTestUser(page);
    const version = parsePage(await (await page.request.get(RX)).text()).version;
    const response = await page.request.get(RX, {
      headers: {
        'X-Inertia': 'true',
        'X-Inertia-Version': version,
        'X-Inertia-Except-Once-Props': 'notice',
      },
    });
    expect(response.status()).toBe(200);
    const body = (await response.json()) as PageObject;
    expect('notice' in body.props).toBe(false);
    expect(body.props.greeting).toBe('Wire sampler.');
  });

  test('reactive validation redirects with errors and flash (E2E-04)', async ({ page }) => {
    await loginAsTestUser(page);
    const token = await xsrfToken(page);
    expect(token.length).toBeGreaterThan(0);
    const invalid = await page.request.post(RX_VALIDATE, {
      data: { name: '', email: '' },
      headers: {
        'X-Inertia': 'true',
        'X-XSRF-TOKEN': token,
        Referer: RX,
      },
      maxRedirects: 0,
    });
    expect([302, 303]).toContain(invalid.status());
    const location = invalid.headers()['location'];
    expect(location).toBeTruthy();
    const followup = await page.request.get(location!, {
      headers: { 'X-Inertia': 'true' },
    });
    const errors = ((await followup.json()) as PageObject).props.errors as Record<
      string,
      string
    >;
    expect(errors.name).toBe('Please enter your full name.');
    expect(errors.email).toBe('We need your email address.');
    const valid = await page.request.post(RX_VALIDATE, {
      data: { name: 'Ada', email: 'ada@example.com' },
      headers: {
        'X-Inertia': 'true',
        'X-XSRF-TOKEN': token,
        Referer: RX,
      },
      maxRedirects: 0,
    });
    expect([302, 303]).toContain(valid.status());
    const done = await page.request.get(valid.headers()['location']!, {
      headers: { 'X-Inertia': 'true' },
    });
    expect(((await done.json()) as PageObject).props.success).toBe('Wire form submitted!');
  });

  test('reactive named error bag namespaces errors (E2E-04)', async ({ page }) => {
    await loginAsTestUser(page);
    const token = await xsrfToken(page);
    const invalid = await page.request.post(RX_VALIDATE, {
      data: { name: '', email: '' },
      headers: {
        'X-Inertia': 'true',
        'X-Inertia-Error-Bag': 'wireBag',
        'X-XSRF-TOKEN': token,
        Referer: RX,
      },
      maxRedirects: 0,
    });
    expect([302, 303]).toContain(invalid.status());
    const followup = await page.request.get(invalid.headers()['location']!, {
      headers: { 'X-Inertia': 'true' },
    });
    const errors = ((await followup.json()) as PageObject).props.errors as Record<
      string,
      Record<string, string>
    >;
    expect(errors.wireBag.name).toBe('Please enter your full name.');
  });

  test('reactive multipart upload flashes success (E2E-08)', async ({ page }) => {
    await loginAsTestUser(page);
    const token = await xsrfToken(page);
    const uploaded = await page.request.post(RX_UPLOAD, {
      multipart: {
        photo: { name: 'avatar.png', mimeType: 'image/png', buffer: Buffer.from([137, 80, 78, 71]) },
      },
      headers: {
        'X-Inertia': 'true',
        'X-XSRF-TOKEN': token,
        Referer: RX,
      },
      maxRedirects: 0,
    });
    expect([302, 303]).toContain(uploaded.status());
    const done = await page.request.get(uploaded.headers()['location']!, {
      headers: { 'X-Inertia': 'true' },
    });
    expect(((await done.json()) as PageObject).props.success).toBe(
      'Uploaded 1 file(s) successfully!',
    );
  });

  test('reactive CSRF failure is denied as documented (E2E-10)', async ({ page }) => {
    await loginAsTestUser(page);
    // Inertia visit without a token: 303 + flash recovery (never a page).
    const denied = await page.request.post(RX_VALIDATE, {
      data: { name: 'Ada', email: 'ada@example.com' },
      headers: { 'X-Inertia': 'true', Referer: RX },
      maxRedirects: 0,
    });
    expect(denied.status()).toBe(303);
    expect(denied.headers()['location']).toBeTruthy();
    // Plain (non-Inertia) request without a token: 419, no redirect.
    const plain = await page.request.post(RX_VALIDATE, {
      data: { name: 'Ada', email: 'ada@example.com' },
      headers: { 'Content-Type': 'application/json' },
      maxRedirects: 0,
    });
    expect(plain.status()).toBe(419);
  });

  test('instant visit renders on the JAX-RS twin (E2E-06 vue/quarkus-rest)', async ({
    page,
  }) => {
    const errors: string[] = [];
    page.on('pageerror', (e) => errors.push(String(e)));
    page.on('console', (m) => {
      if (m.type() === 'error') errors.push(m.text());
    });
    await loginAsTestUser(page);
    await page.goto(REST_VISITS);
    await expect(page.getByRole('button', { name: 'Visit with Callback' })).toBeVisible({
      timeout: 30000,
    });
    await page.getByRole('button', { name: 'Visit with Callback' }).click();
    await expect(
      page.getByText('Navigating from source (was: "This is the source page.")'),
    ).toBeVisible({ timeout: 1500 });
    await expect(page.getByText('Hello from the server!')).toBeVisible({ timeout: 15000 });
    expect(errors).toEqual([]);
  });
});
