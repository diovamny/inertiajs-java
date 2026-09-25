import { test, expect } from '@playwright/test';

/**
 * PingCRM browser contracts (official clients only).
 *
 * Runs unmodified against every PingCRM demo (Vue/React/Svelte on Spring
 * Boot and Quarkus); the target is selected with E2E_BASE_URL. All apps
 * share /login (Auth/Login, pre-filled johndoe@example.com/secret) and a
 * dashboard after login. Each test mounts the real client, observes DOM
 * state and fails on any console/page error.
 */

function collectBrowserErrors(page: import('@playwright/test').Page): string[] {
  const errors: string[] = [];
  page.on('pageerror', (error) => errors.push(String(error)));
  page.on('console', (message) => {
    if (message.type() === 'error') errors.push(message.text());
  });
  return errors;
}

test.describe('PingCRM contracts (/login)', () => {
  test('initial visit returns the HTML shell with the page object', async ({ request }) => {
    const response = await request.get('/login');
    expect(response.status()).toBe(200);
    expect(response.headers()['content-type'] ?? '').toContain('text/html');
    expect(response.headers()['x-inertia']).toBeUndefined();
    const html = await response.text();
    expect(html).toContain('id="app"');
    const match = html.match(/<script[^>]*data-page="app"[^>]*>([\s\S]*?)<\/script>/);
    expect(match, 'data-page JSON script must be present').not.toBeNull();
    const page = JSON.parse(match![1]) as { component: string };
    expect(page.component).toBe('Auth/Login');
  });

  test('Inertia visit returns the JSON page object', async ({ request }) => {
    const response = await request.get('/login', {
      headers: { 'X-Inertia': 'true' },
    });
    expect(response.status()).toBe(200);
    expect(response.headers()['x-inertia']).toBe('true');
    const body = (await response.json()) as { component: string; url: string };
    expect(body.component).toBe('Auth/Login');
    expect(body.url).toBe('/login');
  });

  test('browser mounts the login page without console errors', async ({ page }) => {
    const errors = collectBrowserErrors(page);
    await page.goto('/login');
    await expect(page.locator('#app')).toBeAttached({ timeout: 30000 });
    await expect(page.getByLabel('Email')).toBeVisible({ timeout: 30000 });
    await expect(page.getByLabel('Password')).toBeVisible({ timeout: 15000 });
    expect(errors).toEqual([]);
  });

  test('wrong credentials render server errors in the client', async ({ page }) => {
    const errors = collectBrowserErrors(page);
    await page.goto('/login');
    await expect(page.getByLabel('Email')).toBeVisible({ timeout: 30000 });
    await page.getByLabel('Password').fill('definitely-wrong');
    await page.getByRole('button', { name: 'Login' }).click();
    await expect(
      page.getByText('These credentials do not match our records.'),
    ).toBeVisible({ timeout: 15000 });
    expect(errors).toEqual([]);
  });

  test('login navigates to the dashboard without console errors', async ({ page }) => {
    const errors = collectBrowserErrors(page);
    await page.goto('/login');
    await expect(page.getByLabel('Email')).toBeVisible({ timeout: 30000 });
    await page.getByRole('button', { name: 'Login' }).click();
    await expect(page).not.toHaveURL(/\/login$/, { timeout: 30000 });
    expect(errors).toEqual([]);
  });
});
