import { test, expect } from '@fixtures/base-test';

import { VetPage } from '@pages/vet-page';

test.describe('Vet Specialty Filter', () => {
  test('can filter veterinarians by surgery specialty', async ({ page }) => {
    const vetPage = new VetPage(page);
    await vetPage.open();

    await vetPage.filterBySpecialty('surgery');

    // Seed data: Linda Douglas and Rafael Ortega have surgery
    const rows = vetPage.vetRows();
    await expect(rows).toHaveCount(2);

    const names = await rows.locator('td:first-child').allTextContents();
    expect(names.sort()).toEqual(['Linda Douglas', 'Rafael Ortega'].sort());
  });

  test('can filter veterinarians with no specialty', async ({ page }) => {
    const vetPage = new VetPage(page);
    await vetPage.open();

    await vetPage.filterBySpecialty('none');

    // Seed data: James Carter and Sharon Jenkins have no specialties
    const rows = vetPage.vetRows();
    await expect(rows).toHaveCount(2);

    const names = await rows.locator('td:first-child').allTextContents();
    expect(names.sort()).toEqual(['James Carter', 'Sharon Jenkins'].sort());
  });

  test('filter URL is shareable via query parameter', async ({ page }) => {
    const vetPage = new VetPage(page);

    // Navigate directly to a filtered URL
    await page.goto('/vets.html?specialty=radiology');
    await vetPage.heading().waitFor();

    // Should show only radiology vets: Helen Leary, Henry Stevens
    const rows = vetPage.vetRows();
    await expect(rows).toHaveCount(2);

    // The select dropdown should reflect the active filter
    await expect(vetPage.specialtyFilter()).toHaveValue('radiology');
  });

  test('selecting All Specialties shows all vets', async ({ page }) => {
    const vetPage = new VetPage(page);
    await vetPage.open();

    // First filter by surgery
    await vetPage.filterBySpecialty('surgery');
    await expect(vetPage.vetRows()).toHaveCount(2);

    // Then go back to all — should show more vets than the filtered view
    // (page size is 5, so first page shows 5 of the 6 total vets)
    await vetPage.filterBySpecialty('all');
    const allCount = await vetPage.vetRows().count();
    expect(allCount).toBeGreaterThan(2);
  });
});
