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

      // Navigate back to home
      await page.goto("/");

      // Click on the Color Map button, wait a bit, then click the Save Map button
      await page.click("text=Color Map");
      await page.click("text=Save Map");
      // Should show success notification
      await expect(page.locator('[data-testid="notification"]')).toBeVisible();
      await expect(page.locator("text=Map saved successfully")).toBeVisible();
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
      await page.click("text=Delete");
      await page.click("text=Yes, Delete");

      // Should show success notification
      await expect(page.locator('[data-testid="notification"]')).toBeVisible();
      await expect(page.locator("text=Map deleted successfully")).toBeVisible();
    });

    test("show show warning notification when trying to save map without coloring the map", async ({
      page,
    }) => {
      // First login
      await page.goto("/login");
      await page.fill('input[type="text"]', "test@example.com");
      await page.fill('input[type="password"]', "password123");
      await page.click('button[type="submit"]');

      // Navigate back to home
      await page.goto("/");

      // Click on the Color Map button, wait a bit, then click the Save Map button
      await page.click("text=Save Map");
      // Should show warning notification
      await expect(page.locator('[data-testid="notification"]')).toBeVisible();
      await expect(
        page.locator("text=Please color the map before saving")
      ).toBeVisible();
    });
  });
});
