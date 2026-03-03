import { test, expect } from '../fixtures/base-test';

import { OwnerPage } from '../pages/owner-page';

test.describe('Owner Search Filters', () => {
  test('search by telephone finds matching owner', async ({ page }) => {
    const ownerPage = new OwnerPage(page);

    await ownerPage.openFindOwners();
    // George Franklin has telephone 6085551023
    await ownerPage.searchByTelephone('6085551023');

    // Single result should redirect to owner details
    await expect(ownerPage.heading().filter({ hasText: /Owner Information/i })).toBeVisible();
  });

  test('search by city finds matching owners', async ({ page }) => {
    const ownerPage = new OwnerPage(page);

    await ownerPage.openFindOwners();
    // Madison has 4 owners: Franklin, McTavish, Escobito, Schroeder
    await ownerPage.searchByCity('Madison');

    const ownersTable = ownerPage.ownersTable();
    await expect(ownersTable).toBeVisible();
    await expect(ownersTable.locator('tbody tr')).toHaveCount(4);
  });

  test('invalid telephone input shows validation error', async ({ page }) => {
    const ownerPage = new OwnerPage(page);

    await ownerPage.openFindOwners();
    await ownerPage.searchByTelephone('abc');

    // Should stay on find owners page with error message
    await expect(ownerPage.heading().filter({ hasText: /Find Owners/i })).toBeVisible();
    await expect(page.locator('#telephoneGroup .help-inline')).toContainText(/digits/i);
  });
});
