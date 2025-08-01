import { test, expect } from "@playwright/test";

test.describe("Map Functionality Tests", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/");
  });

  test.describe("Canvas Interaction", () => {
    test("should display canvas on home page", async ({ page }) => {
      // login first
      await page.goto("/login");
      await page.fill('input[type="text"]', "test@example.com");
      await page.fill('input[type="password"]', "password123");
      await page.click('button[type="submit"]');

      // Check if the user is in the home page
      await expect(page).toHaveURL("/");
      await expect(page.locator("canvas")).toBeVisible();
    });

    test("should allow drawing on canvas", async ({ page }) => {
      // login first
      await page.goto("/login");
      await page.fill('input[type="text"]', "test@example.com");
      await page.fill('input[type="password"]', "password123");
      await page.click('button[type="submit"]');

      // Check if the user is in the home page
      const canvas = page.locator("canvas").first();

      // Get canvas position and size
      const canvasBox = await canvas.boundingBox();
      if (!canvasBox) throw new Error("Canvas not found");

      // Draw a simple line
      await page.mouse.move(canvasBox.x + 100, canvasBox.y + 100);
      await page.mouse.down();
      await page.mouse.move(canvasBox.x + 200, canvasBox.y + 200);
      await page.mouse.up();

      // Canvas should be interactive
      await expect(canvas).toBeVisible();
    });
  });

  test.describe("Map Creation", () => {
    test("should show color map button", async ({ page }) => {
      await expect(page.locator("text=Color Map")).toBeVisible();
    });

    test("should allow map coloring and saving", async ({ page }) => {
      // First login
      await page.goto("/login");
      await page.fill('input[type="text"]', "test@example.com");
      await page.fill('input[type="password"]', "password123");
      await page.click('button[type="submit"]');

      // Check if the user is in the home page
      await expect(page).toHaveURL("/");

      // Draw something on canvas
      const canvas = page.locator("canvas").first();
      const canvasBox = await canvas.boundingBox();
      if (!canvasBox) throw new Error("Canvas not found");
      await page.mouse.move(canvasBox.x + 100, canvasBox.y + 100);
      await page.mouse.down();
      await page.mouse.move(canvasBox.x + 200, canvasBox.y + 200);

      // Should be able to create map
      await page.click("text=Color Map");
      await page.waitForTimeout(5000); // Wait for coloring to complete
      await expect(page.locator('button:has-text("Save Map")')).toBeVisible();
      await page.click('button:has-text("Save Map")');
      // Should show success message
      await expect(page.locator('[data-testid="notification"]')).toBeVisible();
      await expect(page.locator("text=Map saved successfully!")).toBeVisible();
    });

    test.describe("Map Viewing", () => {
      test("should show saved maps in profile", async ({ page }) => {
        // First login
        await page.goto("/login");
        await page.fill('input[type="text"]', "test@example.com");
        await page.fill('input[type="password"]', "password123");
        await page.click('button[type="submit"]');

        // Navigate to profile
        await page.click("text=Profile");

        // Should show maps section
        await expect(page.locator("text=Your Saved Maps")).toBeVisible();
      });

      test("should display map details", async ({ page }) => {
        // First login
        await page.goto("/login");
        await page.fill('input[type="text"]', "test@example.com");
        await page.fill('input[type="password"]', "password123");
        await page.click('button[type="submit"]');

        // Navigate to profile
        await page.click("text=Profile");

        // Click on a map to view it
        await page.click("text=View");

        // Should show map details on the map details page
        await expect(
          page.locator('button:has-text("Back to Profile")')
        ).toBeVisible();
        await expect(
          page.locator('button:has-text("Create New Map")')
        ).toBeVisible();
      });

      test("should allow map deletion", async ({ page }) => {
        // First login
        await page.goto("/login");
        await page.fill('input[type="text"]', "test@example.com");
        await page.fill('input[type="password"]', "password123");
        await page.click('button[type="submit"]');

        // Create a map first by drawing and coloring
        await expect(
          page.locator('button:has-text("Color Map")')
        ).toBeVisible();
        await page.click('button:has-text("Color Map")');
        await page.waitForTimeout(5000); // Wait for coloring to complete
        await expect(page.locator('button:has-text("Save Map")')).toBeVisible();
        await page.click('button:has-text("Save Map")');
        await expect(
          page.locator('[data-testid="notification"]')
        ).toBeVisible();
        await page.waitForTimeout(2000); // Wait for save to complete

        // Navigate to profile
        await page.click("text=Profile");

        // Delete a map
        await page.click('button:has-text("Delete")');
        await expect(page.locator("text=Are you sure")).toBeVisible();
        await page.locator('[data-testid="confirm-button"]').click();

        // Should show success notification
        await expect(
          page.locator('[data-testid="notification"]')
        ).toBeVisible();
        await expect(
          page.locator("text=Map deleted successfully!")
        ).toBeVisible();
      });

      test("should cancel map deletion", async ({ page }) => {
        // First login
        await page.goto("/login");
        await page.fill('input[type="text"]', "test@example.com");
        await page.fill('input[type="password"]', "password123");
        await page.click('button[type="submit"]');

        // Create a map first by drawing and coloring
        await expect(
          page.locator('button:has-text("Color Map")')
        ).toBeVisible();
        await page.click('button:has-text("Color Map")');
        await page.waitForTimeout(5000); // Wait for coloring to complete
        await expect(page.locator('button:has-text("Save Map")')).toBeVisible();
        await page.click('button:has-text("Save Map")');
        await expect(
          page.locator('[data-testid="notification"]')
        ).toBeVisible();
        await page.waitForTimeout(2000); // Wait for save to complete

        // Navigate to profile
        await page.click("text=Profile");

        // Click delete button
        await page.click('button:has-text("Delete")');

        // Should show confirmation modal
        await expect(page.locator("text=Are you sure")).toBeVisible();

        // Cancel deletion
        await page.click("text=Cancel");

        // Modal should close
        await expect(page.locator("text=Are you sure")).not.toBeVisible();
      });
    });

    test.describe("Map Coloring", () => {
      test("should apply four color theorem algorithm", async ({ page }) => {
        // First login
        await page.goto("/login");
        await page.fill('input[type="text"]', "test@example.com");
        await page.fill('input[type="password"]', "password123");
        await page.click('button[type="submit"]');

        // Draw regions on canvas
        const canvas = page.locator("canvas").first();
        const canvasBox = await canvas.boundingBox();
        if (canvasBox) {
          // Draw multiple regions
          await page.mouse.move(canvasBox.x + 50, canvasBox.y + 50);
          await page.mouse.down();
          await page.mouse.move(canvasBox.x + 150, canvasBox.y + 150);
          await page.mouse.up();

          await page.mouse.move(canvasBox.x + 200, canvasBox.y + 50);
          await page.mouse.down();
          await page.mouse.move(canvasBox.x + 300, canvasBox.y + 150);
          await page.mouse.up();
        }

        // Click color map button
        await page.click("text=Color Map");

        // Check notification
        await expect(
          page.locator("text=Map colored successfully!")
        ).toBeVisible();
      });
    });
  });
});
