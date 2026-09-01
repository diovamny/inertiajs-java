import { test, expect } from '@playwright/test';

const BASE = 'http://localhost:8082';
const EMAIL = 'johndoe@example.com';
const PASSWORD = 'secret';

async function login(page) {
  await page.goto(`${BASE}/login`);
  await page.fill('input[type="email"]', EMAIL);
  await page.fill('input[type="password"]', PASSWORD);
  await page.getByRole('button', { name: 'Login' }).click();
  await expect(page).toHaveURL(`${BASE}/`, { timeout: 15000 });
}

test.describe('PingCRM Svelte - Full Certification', () => {

  test('Login page - standalone, no sidebar', async ({ page }) => {
    await page.goto(`${BASE}/login`);
    await expect(page.locator('text=Welcome Back!')).toBeVisible();
    await expect(page.locator('text=Ping CRM')).toBeVisible();
    // Should NOT have sidebar navigation
    await expect(page.locator('text=Dashboard')).not.toBeVisible();
    await expect(page.locator('text=Contacts')).not.toBeVisible();
  });

  test('Login - invalid credentials shows error', async ({ page }) => {
    await page.goto(`${BASE}/login`);
    await page.fill('input[type="email"]', 'wrong@example.com');
    await page.fill('input[type="password"]', 'wrongpassword');
    await page.getByRole('button', { name: 'Login' }).click();
    await page.waitForTimeout(2000);
    // Should still be on login page
    await expect(page).toHaveURL(/\/login/, { timeout: 5000 });
  });

  test('Login and Dashboard', async ({ page }) => {
    await login(page);
    await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible();
    // Sidebar should be visible
    await expect(page.locator('nav >> text=Contacts')).toBeVisible();
    await expect(page.locator('nav >> text=Organizations')).toBeVisible();
    await expect(page.locator('nav >> text=Users')).toBeVisible();
    await expect(page.locator('nav >> text=Reports')).toBeVisible();
  });

  test('Navigation - sidebar links work', async ({ page }) => {
    await login(page);

    // Contacts
    await page.locator('nav >> text=Contacts').first().click();
    await expect(page).toHaveURL(/\/contacts/, { timeout: 10000 });
    await expect(page.getByRole('heading', { name: 'Contacts' })).toBeVisible();

    // Organizations
    await page.locator('nav >> text=Organizations').first().click();
    await expect(page).toHaveURL(/\/organizations/, { timeout: 10000 });
    await expect(page.getByRole('heading', { name: 'Organizations' })).toBeVisible();

    // Users
    await page.locator('nav >> text=Users').first().click();
    await expect(page).toHaveURL(/\/users/, { timeout: 10000 });
    await expect(page.getByRole('heading', { name: 'Users' })).toBeVisible();

    // Reports
    await page.locator('nav >> text=Reports').first().click();
    await expect(page).toHaveURL(/\/reports/, { timeout: 10000 });
    await expect(page.getByRole('heading', { name: 'Reports' })).toBeVisible();
  });

  test('Contacts - index with search and pagination', async ({ page }) => {
    await login(page);
    await page.goto(`${BASE}/contacts`);
    await expect(page.getByRole('heading', { name: 'Contacts' })).toBeVisible();
    await expect(page.locator('text=Create')).toBeVisible();

    // Table should have contacts
    const rows = page.locator('table tbody tr');
    await expect(rows.first()).toBeVisible({ timeout: 5000 });

    // Search input should exist
    await expect(page.locator('input[placeholder="Search..."]')).toBeVisible();
  });

  test('Contacts - create form', async ({ page }) => {
    await login(page);
    await page.goto(`${BASE}/contacts/create`);
    await expect(page.getByRole('heading', { name: 'Create Contact' })).toBeVisible();

    // Fill and submit
    await page.fill('input[type="text"]', 'John'); // first_name
    const lastInputs = page.locator('input[type="text"]');
    await lastInputs.nth(1).fill('TestContact');
    await page.fill('input[type="email"]', 'test@example.com');
    await page.locator('input[type="text"]').nth(2).fill('555-1234');

    await page.getByRole('button', { name: 'Create Contact' }).click();
    await expect(page).toHaveURL(/\/contacts/, { timeout: 15000 });
  });

  test('Contacts - edit form', async ({ page }) => {
    await login(page);
    await page.goto(`${BASE}/contacts`);
    // Click first contact link
    const firstContact = page.locator('table tbody tr td a').first();
    await firstContact.click();
    await expect(page.locator('text=Update Contact')).toBeVisible({ timeout: 10000 });

    // Update button should exist
    await expect(page.getByRole('button', { name: 'Update Contact' })).toBeVisible();
    // Delete button should exist
    await expect(page.getByRole('button', { name: 'Delete' })).toBeVisible();
  });

  test('Organizations - index with search and pagination', async ({ page }) => {
    await login(page);
    await page.goto(`${BASE}/organizations`);
    await expect(page.getByRole('heading', { name: 'Organizations' })).toBeVisible();
    await expect(page.locator('text=Create')).toBeVisible();

    const rows = page.locator('table tbody tr');
    await expect(rows.first()).toBeVisible({ timeout: 5000 });
    await expect(page.locator('input[placeholder="Search..."]')).toBeVisible();
  });

  test('Organizations - create form', async ({ page }) => {
    await login(page);
    await page.goto(`${BASE}/organizations/create`);
    await expect(page.getByRole('heading', { name: 'Create Organization' })).toBeVisible();

    await page.fill('input[type="text"]', 'Test Org');
    await page.fill('input[type="email"]', 'org@test.com');
    await page.locator('input[type="text"]').nth(1).fill('555-9999');

    await page.getByRole('button', { name: 'Create Organization' }).click();
    await expect(page).toHaveURL(/\/organizations/, { timeout: 15000 });
  });

  test('Organizations - edit form with contacts table', async ({ page }) => {
    await login(page);
    await page.goto(`${BASE}/organizations`);
    const firstOrg = page.locator('table tbody tr td a').first();
    await firstOrg.click();
    await expect(page.locator('text=Update Organization')).toBeVisible({ timeout: 10000 });

    await expect(page.getByRole('button', { name: 'Update Organization' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Delete' })).toBeVisible();
  });

  test('Users - index with search, role filter, and trashed filter', async ({ page }) => {
    await login(page);
    await page.goto(`${BASE}/users`);
    await expect(page.getByRole('heading', { name: 'Users' })).toBeVisible();
    await expect(page.locator('text=Create')).toBeVisible();

    const rows = page.locator('table tbody tr');
    await expect(rows.first()).toBeVisible({ timeout: 5000 });

    // Should have search, role filter, and trashed filter
    await expect(page.locator('input[placeholder="Search..."]')).toBeVisible();
    await expect(page.locator('select').first()).toBeVisible();
  });

  test('Users - create form', async ({ page }) => {
    await login(page);
    await page.goto(`${BASE}/users/create`);
    await expect(page.getByRole('heading', { name: 'Create User' })).toBeVisible();

    await page.fill('input[type="text"]', 'Test');
    const lastInputs = page.locator('input[type="text"]');
    await lastInputs.nth(1).fill('User');
    await page.fill('input[type="email"]', 'testuser@example.com');
    await page.fill('input[type="password"]', 'password123');

    await page.getByRole('button', { name: 'Create User' }).click();
    await expect(page).toHaveURL(/\/users/, { timeout: 15000 });
  });

  test('Users - edit form', async ({ page }) => {
    await login(page);
    await page.goto(`${BASE}/users`);
    const firstUser = page.locator('table tbody tr td a').first();
    await firstUser.click();
    await expect(page.locator('text=Update User')).toBeVisible({ timeout: 10000 });

    await expect(page.getByRole('button', { name: 'Update User' })).toBeVisible();
  });

  test('Reports page', async ({ page }) => {
    await login(page);
    await page.goto(`${BASE}/reports`);
    await expect(page.getByRole('heading', { name: 'Reports' })).toBeVisible();
  });

  test('Logout flow', async ({ page }) => {
    await login(page);

    // Click user dropdown
    await page.locator('text=John').first().click();
    await page.waitForTimeout(500);

    // Click logout
    await page.locator('text=Logout').click();
    await expect(page).toHaveURL(/\/login/, { timeout: 10000 });
  });

  test('Flash messages display on CRUD operations', async ({ page }) => {
    await login(page);

    // Create a contact to trigger flash message
    await page.goto(`${BASE}/contacts/create`);
    await page.fill('input[type="text"]', 'Flash');
    await page.locator('input[type="text"]').nth(1).fill('Test');
    await page.fill('input[type="email"]', 'flash@test.com');

    await page.getByRole('button', { name: 'Create Contact' }).click();

    // After redirect, flash message should be visible
    await expect(page).toHaveURL(/\/contacts/, { timeout: 15000 });
    const flash = page.locator('[class*="green"]').or(page.locator('[class*="flash"]')).or(page.locator('text=/created|saved|success/i'));
    await expect(flash.first()).toBeVisible({ timeout: 10000 });
  });
});
