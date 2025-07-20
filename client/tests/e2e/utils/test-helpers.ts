import { Page, expect } from "@playwright/test";

export class TestHelpers {
  constructor(private page: Page) {}

  /**
   * Login with provided credentials
   */
  async login(
    email: string = "test@example.com",
    password: string = "password123"
  ) {
    await this.page.goto("/login");
    await this.page.fill('input[type="email"]', email);
    await this.page.fill('input[type="password"]', password);
    await this.page.click('button[type="submit"]');

    // Wait for successful login (redirect to home or show success message)
    await this.page.waitForURL("**/", { timeout: 10000 });
  }

  /**
   * Signup with provided credentials
   */
  async signup(
    name: string = "Test User",
    email: string = "newuser@example.com",
    password: string = "password123"
  ) {
    await this.page.goto("/signup");
    await this.page.fill('input[name="name"]', name);
    await this.page.fill('input[type="email"]', email);
    await this.page.fill('input[type="password"]', password);
    await this.page.click('button[type="submit"]');

    // Wait for successful signup (redirect to home or show success message)
    await this.page.waitForURL("**/", { timeout: 10000 });
  }

  /**
   * Logout from the application
   */
  async logout() {
    await this.page.click("text=Logout");
    await this.page.waitForURL("**/", { timeout: 10000 });
  }

  /**
   * Navigate to profile page (requires authentication)
   */
  async goToProfile() {
    await this.page.click("text=Profile");
    await this.page.waitForURL("**/profile", { timeout: 10000 });
  }

  /**
   * Draw on the canvas
   */
  async drawOnCanvas(
    startX: number = 100,
    startY: number = 100,
    endX: number = 200,
    endY: number = 200
  ) {
    const canvas = this.page.locator("canvas").first();
    const canvasBox = await canvas.boundingBox();

    if (!canvasBox) {
      throw new Error("Canvas not found");
    }

    await this.page.mouse.move(canvasBox.x + startX, canvasBox.y + startY);
    await this.page.mouse.down();
    await this.page.mouse.move(canvasBox.x + endX, canvasBox.y + endY);
    await this.page.mouse.up();
  }

  /**
   * Save a map with a name
   */
  async saveMap() {
    // Note: The current implementation doesn't ask for a name
    // It generates the name automatically
    await this.page.click("text=Save Map");

    // Wait for save operation to complete
    await this.page.waitForTimeout(2000);
  }

  /**
   * Wait for and verify a notification
   */
  async expectNotification(
    message: string,
    type: "success" | "error" = "success"
  ) {
    const notification = this.page.locator('[data-testid="notification"]');
    await expect(notification).toBeVisible();
    await expect(notification).toContainText(message);

    if (type === "error") {
      await expect(notification).toHaveClass(/error/);
    } else {
      await expect(notification).toHaveClass(/success/);
    }
  }

  /**
   * Wait for and verify a loading state
   */
  async expectLoadingState(message?: string) {
    const spinner = this.page.locator('[data-testid="loading-spinner"]');
    await expect(spinner).toBeVisible();

    if (message) {
      await expect(this.page.locator(`text=${message}`)).toBeVisible();
    }
  }

  /**
   * Wait for and verify form validation errors
   */
  async expectValidationErrors(errors: string[]) {
    for (const error of errors) {
      await expect(this.page.locator(`text=${error}`)).toBeVisible();
    }
  }

  /**
   * Clear all form fields
   */
  async clearForm() {
    await this.page.evaluate(() => {
      const inputs = document.querySelectorAll("input");
      inputs.forEach((input) => {
        if (input.type !== "hidden") {
          (input as HTMLInputElement).value = "";
        }
      });
    });
  }

  /**
   * Wait for page to be fully loaded
   */
  async waitForPageLoad() {
    await this.page.waitForLoadState("networkidle");
  }

  /**
   * Take a screenshot for debugging
   */
  async takeScreenshot(name: string) {
    await this.page.screenshot({ path: `screenshots/${name}.png` });
  }

  /**
   * Mock API responses for testing
   */
  async mockApiResponse(url: string, response: Record<string, unknown>) {
    await this.page.route(url, (route) => {
      route.fulfill({
        status: 200,
        contentType: "application/json",
        body: JSON.stringify(response),
      });
    });
  }

  /**
   * Mock API error responses
   */
  async mockApiError(
    url: string,
    status: number = 500,
    message: string = "Internal Server Error"
  ) {
    await this.page.route(url, (route) => {
      route.fulfill({
        status,
        contentType: "application/json",
        body: JSON.stringify({ error: message }),
      });
    });
  }

  /**
   * Check if user is authenticated
   */
  async isAuthenticated(): Promise<boolean> {
    const logoutButton = this.page.locator("text=Logout");
    return await logoutButton.isVisible();
  }

  /**
   * Get current URL
   */
  async getCurrentUrl(): Promise<string> {
    return this.page.url();
  }

  /**
   * Wait for element to be visible
   */
  async waitForElement(selector: string, timeout: number = 10000) {
    await this.page.waitForSelector(selector, { state: "visible", timeout });
  }

  /**
   * Wait for element to not be visible
   */
  async waitForElementToDisappear(selector: string, timeout: number = 10000) {
    await this.page.waitForSelector(selector, { state: "hidden", timeout });
  }
}

/**
 * Create a test helper instance
 */
export function createTestHelpers(page: Page): TestHelpers {
  return new TestHelpers(page);
}
