import type { Locator, Page } from '@playwright/test';

export abstract class BasePage {
  protected readonly page: Page;

  protected constructor(page: Page) {
    this.page = page;
  }

  async goto(path: string): Promise<void> {
    await this.page.goto(path);
  }

  navLink(name: string | RegExp): Locator {
    return this.page.locator('nav.navbar').getByRole('link', { name });
  }

  async goHome(): Promise<void> {
    await this.navLink(/Home/i).click();
  }

  async goFindOwners(): Promise<void> {
    await this.navLink(/Find Owners/i).click();
  }

  async goVeterinarians(): Promise<void> {
    await this.navLink(/Veterinarians/i).click();
  }

  async screenshot(path: string): Promise<void> {
    await this.page.screenshot({ path, fullPage: true });
  }

  async selectLanguage(language: string): Promise<void> {
    const selector = this.page.locator('#languageSelector');
    await selector.locator('.dropdown-toggle').click();
    await selector.getByRole('link', { name: language }).click();
  }

  currentLanguage(): Locator {
    return this.page.locator('#languageSelector .dropdown-toggle');
  }
}
