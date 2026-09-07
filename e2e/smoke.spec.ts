import { test, expect } from '@playwright/test';

test('e2e harness smoke check', async ({ page }) => {
  await page.goto('/');
  await expect(page.locator('body')).toBeVisible();
});
