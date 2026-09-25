import { test, expect, chromium } from '@playwright/test';

/**
 * SSR contracts (E2E-09, Fase D): sidecar render + hydration + CSR fallback.
 * Target: generated starter with ssr-enabled (E2E_SSR_URL selects it;
 * defaults to the Spring starter smoke URL). E2E_SSR_PATH selects the page
 * path (default '/'; '/ssr-rx' exercises the same contract through a
 * Reactive Routes endpoint on Quarkus starters).
 */
const SSR_URL = process.env.E2E_SSR_URL ?? 'http://localhost:8080';
const SSR_PATH = process.env.E2E_SSR_PATH ?? '/';

test.describe('SSR contracts', () => {
  test('sidecar HTML is server-rendered (visible without JS)', async () => {
    const browser = await chromium.launch();
    const context = await browser.newContext({ javaScriptEnabled: false });
    const page = await context.newPage();
    await page.goto(`${SSR_URL}${SSR_PATH}`);
    // Without JS, only SSR HTML can show content: definitive server proof.
    await expect(page.getByText(/Hello Inertia powered by/)).toBeVisible({
      timeout: 15000,
    });
    await expect(
      page.getByText(/Powered by (Spring Boot|Quarkus)/).first(),
    ).toBeVisible({ timeout: 15000 });
    await browser.close();
  });

  test('client hydrates without errors or console noise', async ({ page }) => {
    const errors: string[] = [];
    page.on('pageerror', (e) => errors.push(String(e)));
    page.on('console', (m) => {
      if (m.type() === 'error') errors.push(m.text());
    });
    await page.goto(`${SSR_URL}${SSR_PATH}`);
    await expect(page.getByText(/Hello Inertia powered by/)).toBeVisible({
      timeout: 30000,
    });
    expect(errors).toEqual([]);
  });

  test('CSR fallback renders when the sidecar is down', async ({ page }) => {
    // This case runs with the sidecar stopped (CI matrix): the shell must
    // arrive without SSR content and the client must still mount.
    const errors: string[] = [];
    page.on('pageerror', (e) => errors.push(String(e)));
    page.on('console', (m) => {
      if (m.type() === 'error') errors.push(m.text());
    });
    const html = await (await page.request.get(`${SSR_URL}${SSR_PATH}`)).text();
    expect(html).toContain('id="app"');
    await page.goto(`${SSR_URL}${SSR_PATH}`);
    await expect(page.getByText(/Hello Inertia powered by/)).toBeVisible({
      timeout: 30000,
    });
    expect(errors).toEqual([]);
  });
});
