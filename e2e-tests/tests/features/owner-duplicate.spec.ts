import { test, expect } from '@fixtures/base-test';

import { OwnerPage } from '@pages/owner-page';
import { createOwner } from '@utils/data-factory';

test.describe('Duplicate Owner Prevention', () => {
  test('should show error when creating an owner with same name and telephone', async ({
    page,
  }) => {
    const ownerPage = new OwnerPage(page);
    const owner = createOwner();

    // Create the first owner
    await ownerPage.openFindOwners();
    await ownerPage.clickAddOwner();
    await ownerPage.fillOwnerForm(owner);
    await ownerPage.submitOwnerForm();
    await expect(page.getByRole('heading', { name: /Owner Information/i })).toBeVisible();

    // Attempt to create a duplicate owner with same firstName, lastName, telephone
    await ownerPage.openFindOwners();
    await ownerPage.clickAddOwner();
    await ownerPage.fillOwnerForm(owner);
    await ownerPage.submitOwnerForm();

    // Should stay on the form and show the duplicate error
    await expect(page.locator('.form-group .text-danger, .help-inline')).toBeVisible();
  });
});
