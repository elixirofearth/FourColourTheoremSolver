import { test, expect } from "@playwright/test";

test.describe("Notification and Error Handling Tests", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/");
  });

  test.describe("Success Notifications", () => {
    test("should show success notification on map save", async ({ page }) => {
      // First login
      await page.goto("/login");
      await page.fill('input[type="text"]', "test@example.com");
      await page.fill('input[type="password"]', "password123");
      await page.click('button[type="submit"]');

      // Check if the user is in the home page
      await expect(page).toHaveURL("/");

      // Click on the Color Map button, wait a bit, then click the Save Map button
      await expect(page.locator('button:has-text("Color Map")')).toBeVisible();
      await page.click('button:has-text("Color Map")');
      await page.waitForTimeout(5000); // Wait for coloring to complete
      await expect(page.locator('button:has-text("Save Map")')).toBeVisible();
      await page.click('button:has-text("Save Map")');
      // Should show success notification
      await expect(page.locator('[data-testid="notification"]')).toBeVisible();
      await expect(page.locator("text=Map saved successfully!")).toBeVisible();
    });

    test("should show success notification on map deletion", async ({
      page,
    }) => {
      // First login
      await page.goto("/login");
      await page.fill('input[type="text"]', "test@example.com");
      await page.fill('input[type="password"]', "password123");
      await page.click('button[type="submit"]');

      // Navigate to profile
      await page.click("text=Profile");

      // Delete a map
      await page.click('button:has-text("Delete")');
      await page.click('button:has-text("Confirm")');

      // Should show success notification
      await expect(page.locator('[data-testid="notification"]')).toBeVisible();
      await expect(
        page.locator("text=Map deleted successfully!")
      ).toBeVisible();
    });

    test("show show warning notification when trying to save map without coloring the map", async ({
      page,
    }) => {
      // First login
      await page.goto("/login");
      await page.fill('input[type="text"]', "test@example.com");
      await page.fill('input[type="password"]', "password123");
      await page.click('button[type="submit"]');

      // Check if the user is in the home page
      await expect(page).toHaveURL("/");

      // Click on the Save Map button
      await page.click('button:has-text("Save Map")');
      // Should show warning notification
      await expect(page.locator('[data-testid="notification"]')).toBeVisible();
      await expect(
        page.locator("text=Please color your map before saving!")
      ).toBeVisible();
    });
  });
});
