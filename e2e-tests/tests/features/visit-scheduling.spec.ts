import { test, expect } from '@fixtures/base-test';

import { VisitPage } from '@pages/visit-page';

test.describe('Visit Scheduling', () => {
  test('can schedule a visit for an existing pet', async ({ page }, testInfo) => {
    const visitPage = new VisitPage(page);
    // Note: searching by last name may redirect directly to owner details when there is a single match.
    // Use a stable direct URL to avoid depending on the owners list table.
    await page.goto('/owners/1');
    await expect(page.getByRole('heading', { name: /Owner Information/i })).toBeVisible();

    const addVisitLink = page.getByRole('link', { name: /^Add Visit$/i }).first();
    const addVisitHref = await addVisitLink.getAttribute('href');
    if (!addVisitHref) {
      throw new Error('Expected Add Visit link to have an href');
    }

    const petIdMatch = addVisitHref.match(/pets\/(\d+)\//);
    if (!petIdMatch) {
      throw new Error(`Expected Add Visit href to include pet id, got: ${addVisitHref}`);
    }

    const petId = petIdMatch[1];

    await addVisitLink.click();

    await expect(visitPage.heading()).toBeVisible();

    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const visitDate = tomorrow.toISOString().split('T')[0];
    const description = `E2E visit ${Date.now()}`;
    await visitPage.fillVisitDate(visitDate);
    await visitPage.fillDescription(description);

    await page.screenshot({ path: testInfo.outputPath('visit-scheduling-form.png'), fullPage: true });

    await visitPage.submit();

    await expect(page.getByRole('heading', { name: /Pets and Visits/i })).toBeVisible();

    const petVisitsTable = page
      .locator(`a[href*="pets/${petId}/visits/new"]`)
      .first()
      .locator('xpath=ancestor::table[1]');

    const visitRow = petVisitsTable.locator('tr').filter({ hasText: visitDate }).filter({ hasText: description });
    await expect(visitRow).toHaveCount(1);
  });

  test('validates visit description is required', async ({ page }) => {
    const visitPage = new VisitPage(page);
    await page.goto('/owners/1');
    await expect(page.getByRole('heading', { name: /Owner Information/i })).toBeVisible();

    await page.getByRole('link', { name: /Add Visit/i }).first().click();

    const futureDate = new Date();
    futureDate.setDate(futureDate.getDate() + 2);
    await visitPage.fillVisitDate(futureDate.toISOString().split('T')[0]);
    await visitPage.submit();

    await expect(page.getByText(/must not be blank/i)).toBeVisible();
  });
});
