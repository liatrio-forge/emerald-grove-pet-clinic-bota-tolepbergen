import type { Locator, Page } from '@playwright/test';

import { BasePage } from './base-page';

export class VetPage extends BasePage {
  constructor(page: Page) {
    super(page);
  }

  heading(): Locator {
    return this.page.getByRole('heading', { name: /Veterinarians/i });
  }

  vetsTable(): Locator {
    return this.page.locator('table#vets');
  }

  specialtyFilter(): Locator {
    return this.page.locator('select#specialty');
  }

  vetRows(): Locator {
    return this.vetsTable().locator('tbody tr');
  }

  async filterBySpecialty(value: string): Promise<void> {
    await this.specialtyFilter().selectOption(value);
    await this.page.waitForURL(new RegExp(`specialty=${value}`));
    await this.page.waitForLoadState('domcontentloaded');
  }

  async open(): Promise<void> {
    await this.goto('/vets.html');
    await this.heading().waitFor();
  }
}
