import { test, expect } from '@fixtures/base-test';

import { HomePage } from '@pages/home-page';

test.describe('Language Selector', () => {
  test('should switch to Spanish and persist across navigation', async ({ page }) => {
    const homePage = new HomePage(page);
    await homePage.open();

    // Verify default is English
    await expect(homePage.currentLanguage()).toContainText('English');

    // Switch to Spanish
    await homePage.selectLanguage('Español');

    // Assert nav labels changed to Spanish
    await expect(page.locator('nav.navbar')).toContainText('Inicio');
    await expect(page.locator('nav.navbar')).toContainText('Buscar propietarios');

    // Assert page heading changed to Spanish
    await expect(page.locator('h1')).toContainText('Cuidado moderno');

    // Navigate to Veterinarians page
    await page.locator('nav.navbar').getByRole('link', { name: 'Veterinarios' }).click();

    // Assert language persists - nav still in Spanish
    await expect(page.locator('nav.navbar')).toContainText('Inicio');
    await expect(page.locator('nav.navbar')).toContainText('Veterinarios');

    // Assert dropdown still shows Spanish
    await expect(homePage.currentLanguage()).toContainText('Español');
  });

  test('should switch to German and persist across navigation', async ({ page }) => {
    const homePage = new HomePage(page);
    await homePage.open();

    // Switch to German
    await homePage.selectLanguage('Deutsch');

    // Assert nav labels changed to German
    await expect(page.locator('nav.navbar')).toContainText('Startseite');
    await expect(page.locator('nav.navbar')).toContainText('Besitzer suchen');

    // Assert page heading changed to German
    await expect(page.locator('h1')).toContainText('Moderne Tierpflege');

    // Navigate to Find Owners page
    await page.locator('nav.navbar').getByRole('link', { name: 'Besitzer suchen' }).click();

    // Assert language persists - nav still in German
    await expect(page.locator('nav.navbar')).toContainText('Startseite');
    await expect(page.locator('nav.navbar')).toContainText('Tierärzte');

    // Assert dropdown still shows German
    await expect(homePage.currentLanguage()).toContainText('Deutsch');
  });
});
