import { test, expect } from '@playwright/test';

// Temporary FD diagnostic: React login network anatomy.
test('diag react login', async ({ page }) => {
  const logs: string[] = [];
  page.on('pageerror', (e) => logs.push('PAGEERROR: ' + String(e).slice(0, 300)));
  page.on('console', (m) => {
    if (m.type() === 'error') logs.push('CONSOLE: ' + m.text().slice(0, 300));
  });
  page.on('response', (r) => {
    const u = new URL(r.url());
    if (!u.pathname.startsWith('/assets')) {
      logs.push(`RESP ${r.status()} ${r.request().method()} ${u.pathname} loc=${r.headers()['location'] ?? '-'}`);
    }
  });
  await page.goto('/login');
  await expect(page.getByLabel('Email')).toBeVisible({ timeout: 30000 });
  await page.getByRole('button', { name: 'Login' }).click();
  await page.waitForTimeout(8000);
  logs.push('FINAL-URL=' + page.url());
  console.log('RDDIAG\n' + logs.join('\n'));
});
