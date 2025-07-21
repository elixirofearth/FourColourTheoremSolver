import { test, expect } from "@playwright/test";

test.describe("Map Functionality Tests", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/");
  });

  test.describe("Canvas Interaction", () => {
    test("should display canvas on home page", async ({ page }) => {
      await expect(page.locator("canvas")).toBeVisible();
    });

    test("should allow drawing on canvas", async ({ page }) => {
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

    test("should show color palette", async ({ page }) => {
      await expect(page.locator("text=🎨")).toBeVisible();
      // Should have color options available
      await expect(page.locator('[data-testid="color-palette"]')).toBeVisible();
    });

    test("should allow color selection", async ({ page }) => {
      // Click on a color option
      await page.click('[data-testid="color-option"]');

      // Should show selected color
      await expect(
        page.locator('[data-testid="selected-color"]')
      ).toBeVisible();
    });
  });

  test.describe("Map Creation", () => {
    test("should show create map button", async ({ page }) => {
      await expect(page.locator("text=Create Map")).toBeVisible();
    });

    test("should handle map creation when not authenticated", async ({
      page,
    }) => {
      // Try to create a map without being logged in
      await page.click("text=Create Map");

      // Should redirect to login
      await expect(page).toHaveURL("/login");
    });

    test("should allow map creation when authenticated", async ({ page }) => {
      // First login
      await page.goto("/login");
      await page.fill('input[type="text"]', "test@example.com");
      await page.fill('input[type="password"]', "password123");
      await page.click('button[type="submit"]');

      // Navigate back to home
      await page.goto("/");

      // Should be able to create map
      await page.click("text=Create Map");

      // Should show success message or redirect appropriately
      await expect(page.locator("text=Map created successfully")).toBeVisible();
    });

    test("should save map with name", async ({ page }) => {
      // First login
      await page.goto("/login");
      await page.fill('input[type="text"]', "test@example.com");
      await page.fill('input[type="password"]', "password123");
      await page.click('button[type="submit"]');

      // Navigate back to home
      await page.goto("/");

      // Draw something on canvas
      const canvas = page.locator("canvas").first();
      const canvasBox = await canvas.boundingBox();
      if (canvasBox) {
        await page.mouse.move(canvasBox.x + 100, canvasBox.y + 100);
        await page.mouse.down();
        await page.mouse.move(canvasBox.x + 150, canvasBox.y + 150);
        await page.mouse.up();
      }

      // Color the map first
      await page.click("text=Color Map");
      await page.waitForTimeout(5000); // Wait for coloring to complete

      // Save map
      await page.click("text=Save Map");

      // Should show success message
      await expect(page.locator("text=Map saved successfully!")).toBeVisible();
    });
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

      // Should show map details
      await expect(page.locator("h1")).toContainText("🗺️");
      await expect(page.locator("text=Created:")).toBeVisible();
      await expect(page.locator("text=Dimensions:")).toBeVisible();
    });

    test("should allow map deletion", async ({ page }) => {
      // First login
      await page.goto("/login");
      await page.fill('input[type="text"]', "test@example.com");
      await page.fill('input[type="password"]', "password123");
      await page.click('button[type="submit"]');

      // Navigate to profile
      await page.click("text=Profile");

      // Click delete button
      await page.click("text=Delete");

      // Should show confirmation modal
      await expect(page.locator("text=Are you sure?")).toBeVisible();

      // Confirm deletion
      await page.click("text=Yes, Delete");

      // Should show success message
      await expect(page.locator("text=Map deleted successfully")).toBeVisible();
    });

    test("should cancel map deletion", async ({ page }) => {
      // First login
      await page.goto("/login");
      await page.fill('input[type="text"]', "test@example.com");
      await page.fill('input[type="password"]', "password123");
      await page.click('button[type="submit"]');

      // Navigate to profile
      await page.click("text=Profile");

      // Click delete button
      await page.click("text=Delete");

      // Should show confirmation modal
      await expect(page.locator("text=Are you sure?")).toBeVisible();

      // Cancel deletion
      await page.click("text=Cancel");

      // Modal should close
      await expect(page.locator("text=Are you sure?")).not.toBeVisible();
    });
  });

  test.describe("Map Coloring", () => {
    test("should apply four color theorem algorithm", async ({ page }) => {
      // First login
      await page.goto("/login");
      await page.fill('input[type="text"]', "test@example.com");
      await page.fill('input[type="password"]', "password123");
      await page.click('button[type="submit"]');

      // Navigate back to home
      await page.goto("/");

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

      // Should show loading state
      await expect(page.locator("text=Coloring map...")).toBeVisible();

      // Should show colored result
      await expect(page.locator("text=Map colored successfully")).toBeVisible();
    });

    test("should handle coloring errors", async ({ page }) => {
      // First login
      await page.goto("/login");
      await page.fill('input[type="text"]', "test@example.com");
      await page.fill('input[type="password"]', "password123");
      await page.click('button[type="submit"]');

      // Navigate back to home
      await page.goto("/");

      // Try to color empty canvas
      await page.click("text=Color Map");

      // Should show error message
      await expect(
        page.locator("text=Please draw some regions first")
      ).toBeVisible();
    });
  });

  test.describe("Responsive Design", () => {
    test("should work on mobile viewport", async ({ page }) => {
      // Set mobile viewport
      await page.setViewportSize({ width: 375, height: 667 });

      await page.goto("/");

      // Should show mobile-friendly layout
      await expect(page.locator("canvas")).toBeVisible();
      await expect(page.locator("nav")).toBeVisible();
    });

    test("should work on tablet viewport", async ({ page }) => {
      // Set tablet viewport
      await page.setViewportSize({ width: 768, height: 1024 });

      await page.goto("/");

      // Should show tablet-friendly layout
      await expect(page.locator("canvas")).toBeVisible();
      await expect(page.locator("nav")).toBeVisible();
    });
  });
});
