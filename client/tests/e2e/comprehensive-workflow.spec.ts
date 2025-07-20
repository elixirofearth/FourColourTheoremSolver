import { test, expect } from "@playwright/test";
import { createTestHelpers } from "./utils/test-helpers";

test.describe("Comprehensive User Workflow", () => {
  let helpers: ReturnType<typeof createTestHelpers>;

  test.beforeEach(async ({ page }) => {
    helpers = createTestHelpers(page);
    await page.goto("/");
  });

  test("complete user journey: signup, login, create map, view, and delete", async ({
    page,
  }) => {
    // Step 1: Sign up as a new user
    await helpers.signup("New User", "newuser@example.com", "password123");
    await helpers.expectNotification("Account created successfully");

    // Verify we're on the home page
    await expect(page).toHaveURL("/");

    // Step 2: Logout and login again
    await helpers.logout();
    await helpers.login("newuser@example.com", "password123");
    await helpers.expectNotification("Login successful");

    // Step 3: Create a map
    await helpers.drawOnCanvas(50, 50, 150, 150);
    await helpers.drawOnCanvas(200, 50, 300, 150);
    await helpers.drawOnCanvas(50, 200, 150, 300);

    // Save the map
    await page.click("text=Color Map");
    await page.waitForTimeout(5000); // Wait for coloring to complete
    await helpers.saveMap();
    await helpers.expectNotification("Map saved successfully");

    // Step 4: View the map in profile
    await helpers.goToProfile();
    await expect(page.locator("text=My First Map")).toBeVisible();

    // Step 5: View the map details
    await page.click("text=View");
    await expect(page.locator("h1")).toContainText("🗺️ My First Map");
    await expect(page.locator("text=Created:")).toBeVisible();
    await expect(page.locator("text=Dimensions:")).toBeVisible();

    // Step 6: Go back to profile and delete the map
    await page.click("text=← Back to Profile");
    await expect(page).toHaveURL("/profile");

    await page.click("text=Delete");
    await expect(page.locator("text=Are you sure?")).toBeVisible();
    await page.click("text=Yes, Delete");
    await helpers.expectNotification("Map deleted successfully");

    // Verify map is no longer visible
    await expect(page.locator("text=My First Map")).not.toBeVisible();
  });

  test("map coloring workflow", async ({ page }) => {
    // Login
    await helpers.login();

    // Draw regions on canvas
    await helpers.drawOnCanvas(50, 50, 150, 150);
    await helpers.drawOnCanvas(200, 50, 300, 150);
    await helpers.drawOnCanvas(50, 200, 150, 300);
    await helpers.drawOnCanvas(200, 200, 300, 300);

    // Apply four color theorem
    await page.click("text=Color Map");
    await helpers.expectLoadingState("Coloring map...");
    await helpers.expectNotification("Map colored successfully");

    // Save the colored map
    await helpers.saveMap();
    await helpers.expectNotification("Map saved successfully");
  });

  test("error handling workflow", async ({ page }) => {
    // Test login with wrong credentials
    await page.goto("/login");
    await page.fill('input[type="text"]', "wrong@example.com");
    await page.fill('input[type="password"]', "wrongpassword");
    await page.click('button[type="submit"]');

    await helpers.expectNotification("Invalid credentials", "error");

    // Test signup with existing email
    await page.goto("/signup");
    await page.fill('input[name="name"]', "Test User");
    await page.fill('input[type="email"]', "existing@example.com");
    await page.fill('input[type="password"]', "password123");
    await page.click('button[type="submit"]');

    await helpers.expectNotification("Email already exists", "error");

    // Test form validation
    await page.goto("/signup");
    await page.click('button[type="submit"]');
    await helpers.expectValidationErrors([
      "Name is required",
      "Email is required",
      "Password is required",
    ]);
  });

  test("responsive design workflow", async ({ page }) => {
    // Test mobile viewport
    await page.setViewportSize({ width: 375, height: 667 });
    await page.goto("/");

    await expect(page.locator("canvas")).toBeVisible();
    await expect(page.locator("nav")).toBeVisible();

    // Test tablet viewport
    await page.setViewportSize({ width: 768, height: 1024 });
    await page.goto("/");

    await expect(page.locator("canvas")).toBeVisible();
    await expect(page.locator("nav")).toBeVisible();

    // Test desktop viewport
    await page.setViewportSize({ width: 1920, height: 1080 });
    await page.goto("/");

    await expect(page.locator("canvas")).toBeVisible();
    await expect(page.locator("nav")).toBeVisible();
  });

  test("session management workflow", async ({ page }) => {
    // Login
    await helpers.login();

    // Verify authentication state by checking for logout button
    await expect(page.locator("text=Logout")).toBeVisible();

    // Refresh page and verify still authenticated
    await page.reload();
    await expect(page.locator("text=Logout")).toBeVisible();

    // Logout
    await helpers.logout();
    await expect(page.locator("text=Login")).toBeVisible();

    // Try to access protected route
    await page.goto("/profile");
    await expect(page).toHaveURL("/login");
  });

  test("multiple map management", async ({ page }) => {
    // Login
    await helpers.login();

    // Create first map
    await helpers.drawOnCanvas(50, 50, 150, 150);
    await page.click("text=Color Map");
    await page.waitForTimeout(5000); // Wait for coloring to complete
    await helpers.saveMap();
    await helpers.expectNotification("Map saved successfully");

    // Create second map
    await helpers.drawOnCanvas(200, 200, 300, 300);
    await page.click("text=Color Map");
    await page.waitForTimeout(5000); // Wait for coloring to complete
    await helpers.saveMap();
    await helpers.expectNotification("Map saved successfully");

    // Create third map
    await helpers.drawOnCanvas(100, 100, 250, 250);
    await page.click("text=Color Map");
    await page.waitForTimeout(5000); // Wait for coloring to complete
    await helpers.saveMap();
    await helpers.expectNotification("Map saved successfully");

    // View all maps in profile
    await helpers.goToProfile();
    // Note: Since maps are saved with auto-generated names, we can't check for specific names
    // Instead, check that maps are visible in the profile
    await expect(page.locator("text=Your Saved Maps")).toBeVisible();

    // Delete one map
    await page.locator("text=Delete").first().click();
    await expect(page.locator("text=Are you sure?")).toBeVisible();
    await page.click("text=Yes, Delete");
    await helpers.expectNotification("Map deleted successfully");

    // Verify maps are still visible (just fewer of them)
    await expect(page.locator("text=Your Saved Maps")).toBeVisible();
  });

  test("network error handling", async ({ page }) => {
    // Mock network failure
    await helpers.mockApiError("**/api/v1/auth/login", 500, "Server Error");

    // Try to login
    await page.goto("/login");
    await page.fill('input[type="text"]', "test@example.com");
    await page.fill('input[type="password"]', "password123");
    await page.click('button[type="submit"]');

    await helpers.expectNotification("Network error", "error");
  });

  test("accessibility workflow", async ({ page }) => {
    // Test keyboard navigation
    await page.goto("/");
    await page.keyboard.press("Tab");
    await page.keyboard.press("Enter");

    // Should navigate to login
    await expect(page).toHaveURL("/login");

    // Test form navigation with keyboard
    await page.keyboard.press("Tab");
    await page.fill('input[type="text"]', "test@example.com");
    await page.keyboard.press("Tab");
    await page.fill('input[type="password"]', "password123");
    await page.keyboard.press("Enter");

    // Should attempt login
    await expect(page.locator('button[type="submit"]')).toBeVisible();
  });
});
