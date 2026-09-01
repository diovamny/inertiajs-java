import { test, expect } from '@playwright/test';

const BASE = 'http://localhost:8081';

test('debug login', async ({ page }) => {
  page.on('console', msg => console.log('BROWSER:', msg.type(), msg.text()));
  page.on('response', resp => console.log('RESPONSE:', resp.status(), resp.url()));
  
  await page.goto(`${BASE}/login`);
  console.log('Page loaded, URL:', page.url());
  
  // Fill and click
  await page.fill('input[type="email"]', 'johndoe@example.com');
  await page.fill('input[type="password"]', 'secret');
  
  // Listen for navigation
  const navPromise = page.waitForURL(`${BASE}/`, { timeout: 10000 }).catch(e => {
    console.log('Navigation failed:', e.message);
    return null;
  });
  
  await page.getByRole('button', { name: 'Login' }).click();
  console.log('Button clicked, current URL:', page.url());
  
  await navPromise;
  console.log('Final URL:', page.url());
  
  // Check if we're on dashboard
  const content = await page.content();
  console.log('Page contains Dashboard:', content.includes('Dashboard'));
  console.log('Page contains Login:', content.includes('Login'));
});