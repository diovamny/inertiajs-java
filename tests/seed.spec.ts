import { test, expect } from '@playwright/test';

test.describe('Test group', () => {
  test('seed - Login and verify dashboard loads', async ({ page }) => {
    await page.goto('/login');
    await expect(page.locator('text=Welcome Back!')).toBeVisible();

    await page.fill('input[type="email"]', 'johndoe@example.com');
    await page.fill('input[type="password"]', 'secret');
    await page.getByRole('button', { name: 'Login' }).click();

    await expect(page).toHaveURL('/', { timeout: 15000 });
    await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible({ timeout: 15000 });
  });
});
