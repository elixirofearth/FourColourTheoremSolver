import { test, expect } from "@playwright/test";

test.describe("Authentication Tests", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/");
  });

  test.describe("Login Flow", () => {
    test("should display login form", async ({ page }) => {
      await page.goto("/login");

      await expect(page.locator('input[type="text"]')).toBeVisible();
      await expect(page.locator('input[type="password"]')).toBeVisible();
      await expect(page.locator('button[type="submit"]')).toBeVisible();
    });

    test("should show validation errors for empty fields", async ({ page }) => {
      await page.goto("/login");

      // Try to submit empty form
      await page.click('button[type="submit"]');

      // Should show validation errors - check for the actual error message
      await expect(
        page.locator("text=Please fill in all fields")
      ).toBeVisible();
    });

    test("should handle successful login", async ({ page }) => {
      await page.goto("/login");

      await page.fill('input[type="text"]', "test@example.com");
      await page.fill('input[type="password"]', "password123");
      await page.click('button[type="submit"]');

      // Should redirect to home page after successful login
      await expect(page).toHaveURL("/");
    });

    test("should handle login with incorrect credentials", async ({ page }) => {
      await page.goto("/login");

      await page.fill('input[type="text"]', "wrong@example.com");
      await page.fill('input[type="password"]', "wrongpassword");
      await page.click('button[type="submit"]');

      // Should show error message
      await expect(
        page.locator("text=Invalid email or password")
      ).toBeVisible();
    });
  });

  test.describe("Signup Flow", () => {
    test("should display signup form", async ({ page }) => {
      await page.goto("/signup");

      await expect(page.locator('input[type="text"]')).toBeVisible();
      await expect(page.locator('input[type="email"]')).toBeVisible();
      await expect(page.locator('input[type="password"]')).toBeVisible();
      await expect(page.locator('button[type="submit"]')).toBeVisible();
    });

    test("should show validation errors for empty fields", async ({ page }) => {
      await page.goto("/signup");

      // Try to submit empty form
      await page.click('button[type="submit"]');

      // Should show validation errors
      await expect(
        page.locator("text=Please fill in all fields")
      ).toBeVisible();
    });

    test("should handle successful signup", async ({ page }) => {
      await page.goto("/signup");

      await page.fill('input[type="text"]', "Test User");
      await page.fill('input[type="email"]', "newuser@example.com");
      await page.fill('input[type="password"]', "password123");
      await page.click('button[type="submit"]');

      // Should redirect to home page after successful signup
      await expect(page).toHaveURL("/");
    });

    test("should handle signup with existing email", async ({ page }) => {
      await page.goto("/signup");

      await page.fill('input[type="text"]', "Test User");
      await page.fill('input[type="email"]', "existing@example.com");
      await page.fill('input[type="password"]', "password123");
      await page.click('button[type="submit"]');

      // Should show error message
      await expect(
        page.locator("text=User with this email already exists")
      ).toBeVisible();
    });
  });

  test.describe("Logout Flow", () => {
    test("should show logout option when authenticated", async ({ page }) => {
      // First login
      await page.goto("/login");
      await page.fill('input[type="text"]', "test@example.com");
      await page.fill('input[type="password"]', "password123");
      await page.click('button[type="submit"]');

      // Should see logout option in navigation
      await expect(page.locator("text=Sign Out")).toBeVisible();
    });

    test("should logout successfully", async ({ page }) => {
      // First login
      await page.goto("/login");
      await page.fill('input[type="text"]', "test@example.com");
      await page.fill('input[type="password"]', "password123");
      await page.click('button[type="submit"]');

      // Click logout
      await page.click("text=Sign Out");

      // Should redirect to login page and show login option
      await expect(page).toHaveURL("/login");
      await expect(page.locator("text=Sign In")).toBeVisible();
    });
  });

  test.describe("Protected Routes", () => {
    test("should redirect to login when accessing profile without authentication", async ({
      page,
    }) => {
      await page.goto("/profile");
      await expect(page).toHaveURL("/login");
    });

    test("should allow access to profile when authenticated", async ({
      page,
    }) => {
      // First login
      await page.goto("/login");
      await page.fill('input[type="text"]', "test@example.com");
      await page.fill('input[type="password"]', "password123");
      await page.click('button[type="submit"]');

      // Navigate to profile
      await page.click("text=Profile");
      await expect(page).toHaveURL("/profile");

      // Check for the specific welcome message in the profile page
      await expect(
        page.locator("h1").filter({ hasText: "Welcome" })
      ).toBeVisible();
    });
  });
});
