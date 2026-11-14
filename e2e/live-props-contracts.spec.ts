import { test, expect, type Page } from '@playwright/test';

/**
 * Live Props contracts (LP-01..LP-04) on the Vue kitchen-sink demos.
 *
 * Runs unmodified against Spring MVC and Quarkus (JAX-RS) kitchen sinks;
 * the target is selected with E2E_BASE_URL. The page requires a session, so
 * every test signs in through the real login form first and then inspects
 * the wire page object with the browser request context (same cookies and
 * XSRF token the official client uses).
 */

interface LivePropListener {
  channel: { name: string; type: string };
  events: string[];
}

interface LivePropMetadata {
  listeners: LivePropListener[];
  throttle?: number;
}

interface PageObject {
  component: string;
  props: Record<string, unknown>;
  url: string;
  version: string;
  liveProps?: Record<string, LivePropMetadata>;
}

const LIVE_PATH = '/features/live-props';
const COMPONENT = 'Features/LiveProps/Index';

function collectBrowserErrors(page: Page): string[] {
  const errors: string[] = [];
  page.on('pageerror', (error) => errors.push(String(error)));
  page.on('console', (message) => {
    if (message.type() === 'error') errors.push(message.text());
  });
  return errors;
}

async function loginAsTestUser(page: Page): Promise<void> {
  await page.goto('/login');
  await expect(page.locator('#email')).toBeVisible({ timeout: 30000 });
  await page.locator('#password').fill('password');
  await page.getByRole('button', { name: 'Log in' }).click();
  await expect(page).not.toHaveURL(/\/login$/, { timeout: 30000 });
}

function parsePage(html: string): PageObject {
  const match = html.match(/<script[^>]*data-page="app"[^>]*>([\s\S]*?)<\/script>/);
  expect(match, 'data-page JSON script must be present').not.toBeNull();
  return JSON.parse(match![1]) as PageObject;
}

async function readLivePage(page: Page): Promise<PageObject> {
  const response = await page.request.get(LIVE_PATH);
  expect(response.status()).toBe(200);
  expect(response.headers()['content-type'] ?? '').toContain('text/html');
  const parsed = parsePage(await response.text());
  expect(parsed.component).toBe(COMPONENT);
  expect(parsed.url).toBe(LIVE_PATH);
  expect(parsed.liveProps, 'liveProps must be present on the page').toBeTruthy();
  return parsed;
}

async function xsrfToken(page: Page): Promise<string> {
  const cookies = await page.context().cookies();
  return cookies.find((cookie) => cookie.name === 'XSRF-TOKEN')?.value ?? '';
}

function origin(page: Page): string {
  return new URL(page.url()).origin;
}

test.describe('Live Props contracts (/features/live-props)', () => {
  test('LP-01: initial page emits liveProps.stockCount on the inventory channel', async ({
    page,
  }) => {
    await loginAsTestUser(page);
    const parsed = await readLivePage(page);
    const entry = parsed.liveProps?.stockCount;
    expect(entry, 'liveProps.stockCount must be emitted').toBeTruthy();
    expect(entry!.listeners[0].channel.name).toBe('inventory');
    expect(Number(parsed.props.stockCount)).toBeGreaterThan(0);
  });

  test('LP-02: channel is private and reacts to StockUpdated', async ({ page }) => {
    await loginAsTestUser(page);
    const parsed = await readLivePage(page);
    const listener = parsed.liveProps!.stockCount.listeners[0];
    expect(listener.channel.type).toBe('private');
    expect(listener.events).toContain('StockUpdated');
  });

  test('LP-03: simulate then partial reload returns the fresh stockCount', async ({
    page,
  }) => {
    await loginAsTestUser(page);
    const before = await readLivePage(page);
    const initial = Number(before.props.stockCount);
    const token = await xsrfToken(page);
    expect(token.length).toBeGreaterThan(0);

    const post = await page.request.post('/api/simulate-stock-update', {
      headers: {
        'X-Inertia': 'true',
        'X-Inertia-Version': before.version,
        'X-XSRF-TOKEN': token,
        Referer: `${origin(page)}${LIVE_PATH}`,
      },
      maxRedirects: 0,
    });
    expect([302, 303]).toContain(post.status());

    const partial = await page.request.get(LIVE_PATH, {
      headers: {
        'X-Inertia': 'true',
        'X-Inertia-Version': before.version,
        'X-Inertia-Partial-Component': COMPONENT,
        'X-Inertia-Partial-Data': 'stockCount',
      },
    });
    expect(partial.status()).toBe(200);
    const body = (await partial.json()) as PageObject;
    expect(body.component).toBe(COMPONENT);
    expect(Number(body.props.stockCount)).toBeGreaterThan(initial);
    // Partial reload scope: only the selected live prop travels.
    expect('channelName' in body.props).toBe(false);
    expect(body.liveProps?.stockCount.listeners[0].channel.name).toBe('inventory');
  });

  test('LP-04: configured throttle is emitted in the wire metadata', async ({ page }) => {
    await loginAsTestUser(page);
    const parsed = await readLivePage(page);
    expect(parsed.liveProps!.stockCount.throttle).toBe(2000);
  });

  test('browser simulation updates the rendered stockCount', async ({ page }) => {
    const errors = collectBrowserErrors(page);
    await loginAsTestUser(page);
    await page.goto(LIVE_PATH);
    const badge = page.getByTestId('live-stock-count');
    await expect(badge).toBeVisible({ timeout: 30000 });
    const before = (await badge.textContent())?.trim() ?? '';
    await expect(page.getByTestId('live-channel-name')).toHaveText('inventory');
    await expect(page.getByTestId('live-channel-type')).toHaveText('private');
    await expect(page.getByTestId('live-throttle')).toHaveText('2000 ms');
    await page.getByTestId('simulate-stock-update').click();
    await expect(badge).not.toHaveText(before, { timeout: 15000 });
    expect(errors).toEqual([]);
  });
});
