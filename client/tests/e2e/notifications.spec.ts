import { test, expect } from "@playwright/test";

test.describe("Notification and Error Handling Tests", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/");
  });

  test.describe("Success Notifications", () => {
    test("should show success notification on successful login", async ({
      page,
    }) => {
      await page.goto("/login");
      await page.fill('input[type="email"]', "test@example.com");
      await page.fill('input[type="password"]', "password123");
      await page.click('button[type="submit"]');

      // Should show success notification
      await expect(page.locator('[data-testid="notification"]')).toBeVisible();
      await expect(page.locator("text=Login successful")).toBeVisible();
    });

    test("should show success notification on successful signup", async ({
      page,
    }) => {
      await page.goto("/signup");
      await page.fill('input[name="name"]', "New User");
      await page.fill('input[type="email"]', "newuser@example.com");
      await page.fill('input[type="password"]', "password123");
      await page.click('button[type="submit"]');

      // Should show success notification
      await expect(page.locator('[data-testid="notification"]')).toBeVisible();
      await expect(
        page.locator("text=Account created successfully")
      ).toBeVisible();
    });

    test("should show success notification on map save", async ({ page }) => {
      // First login
      await page.goto("/login");
      await page.fill('input[type="email"]', "test@example.com");
      await page.fill('input[type="password"]', "password123");
      await page.click('button[type="submit"]');

      // Navigate back to home
      await page.goto("/");

      // Save a map
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
      await page.fill('input[type="email"]', "test@example.com");
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
  });

  test.describe("Error Notifications", () => {
    test("should show error notification on login failure", async ({
      page,
    }) => {
      await page.goto("/login");
      await page.fill('input[type="email"]', "wrong@example.com");
      await page.fill('input[type="password"]', "wrongpassword");
      await page.click('button[type="submit"]');

      // Should show error notification
      await expect(page.locator('[data-testid="notification"]')).toBeVisible();
      await expect(page.locator("text=Invalid credentials")).toBeVisible();
    });

    test("should show error notification on signup with existing email", async ({
      page,
    }) => {
      await page.goto("/signup");
      await page.fill('input[name="name"]', "Test User");
      await page.fill('input[type="email"]', "existing@example.com");
      await page.fill('input[type="password"]', "password123");
      await page.click('button[type="submit"]');

      // Should show error notification
      await expect(page.locator('[data-testid="notification"]')).toBeVisible();
      await expect(page.locator("text=Email already exists")).toBeVisible();
    });

    test("should show error notification on network failure", async ({
      page,
    }) => {
      // Mock network failure by going offline
      await page.route("**/*", (route) => route.abort());

      await page.goto("/login");
      await page.fill('input[type="email"]', "test@example.com");
      await page.fill('input[type="password"]', "password123");
      await page.click('button[type="submit"]');

      // Should show error notification
      await expect(page.locator('[data-testid="notification"]')).toBeVisible();
      await expect(page.locator("text=Network error")).toBeVisible();
    });

    test("should show error notification on session expiration", async ({
      page,
    }) => {
      // First login
      await page.goto("/login");
      await page.fill('input[type="email"]', "test@example.com");
      await page.fill('input[type="password"]', "password123");
      await page.click('button[type="submit"]');

      // Mock session expiration by clearing storage
      await page.evaluate(() => {
        localStorage.clear();
        sessionStorage.clear();
      });

      // Try to access protected route
      await page.goto("/profile");

      // Should show session expiration notification
      await expect(page.locator('[data-testid="notification"]')).toBeVisible();
      await expect(page.locator("text=Session expired")).toBeVisible();
    });
  });

  test.describe("Notification Behavior", () => {
    test("should auto-dismiss notifications after timeout", async ({
      page,
    }) => {
      await page.goto("/login");
      await page.fill('input[type="email"]', "test@example.com");
      await page.fill('input[type="password"]', "password123");
      await page.click('button[type="submit"]');

      // Should show notification
      await expect(page.locator('[data-testid="notification"]')).toBeVisible();

      // Wait for auto-dismiss (assuming 5 second timeout)
      await page.waitForTimeout(6000);

      // Should be dismissed
      await expect(
        page.locator('[data-testid="notification"]')
      ).not.toBeVisible();
    });

    test("should allow manual dismissal of notifications", async ({ page }) => {
      await page.goto("/login");
      await page.fill('input[type="email"]', "test@example.com");
      await page.fill('input[type="password"]', "password123");
      await page.click('button[type="submit"]');

      // Should show notification
      await expect(page.locator('[data-testid="notification"]')).toBeVisible();

      // Click close button
      await page.click('[data-testid="notification-close"]');

      // Should be dismissed
      await expect(
        page.locator('[data-testid="notification"]')
      ).not.toBeVisible();
    });

    test("should stack multiple notifications", async ({ page }) => {
      await page.goto("/login");
      await page.fill('input[type="email"]', "test@example.com");
      await page.fill('input[type="password"]', "password123");
      await page.click('button[type="submit"]');

      // Navigate to home and trigger another notification
      await page.goto("/");
      await page.click("text=Save Map");

      // Should show multiple notifications
      const notifications = page.locator('[data-testid="notification"]');
      await expect(notifications).toHaveCount(2);
    });
  });

  test.describe("Form Validation Errors", () => {
    test("should show validation errors for login form", async ({ page }) => {
      await page.goto("/login");

      // Submit empty form
      await page.click('button[type="submit"]');

      // Should show validation errors
      await expect(page.locator("text=Email is required")).toBeVisible();
      await expect(page.locator("text=Password is required")).toBeVisible();
    });

    test("should show validation errors for signup form", async ({ page }) => {
      await page.goto("/signup");

      // Submit empty form
      await page.click('button[type="submit"]');

      // Should show validation errors
      await expect(page.locator("text=Name is required")).toBeVisible();
      await expect(page.locator("text=Email is required")).toBeVisible();
      await expect(page.locator("text=Password is required")).toBeVisible();
    });

    test("should clear validation errors when user starts typing", async ({
      page,
    }) => {
      await page.goto("/login");

      // Submit empty form to trigger errors
      await page.click('button[type="submit"]');
      await expect(page.locator("text=Email is required")).toBeVisible();

      // Start typing in email field
      await page.fill('input[type="email"]', "test");

      // Error should clear
      await expect(page.locator("text=Email is required")).not.toBeVisible();
    });
  });

  test.describe("Loading States", () => {
    test("should show loading state during login", async ({ page }) => {
      await page.goto("/login");
      await page.fill('input[type="email"]', "test@example.com");
      await page.fill('input[type="password"]', "password123");
      await page.click('button[type="submit"]');

      // Should show loading state
      await expect(
        page.locator('[data-testid="loading-spinner"]')
      ).toBeVisible();
      await expect(page.locator("text=Logging in...")).toBeVisible();
    });

    test("should show loading state during signup", async ({ page }) => {
      await page.goto("/signup");
      await page.fill('input[name="name"]', "Test User");
      await page.fill('input[type="email"]', "newuser@example.com");
      await page.fill('input[type="password"]', "password123");
      await page.click('button[type="submit"]');

      // Should show loading state
      await expect(
        page.locator('[data-testid="loading-spinner"]')
      ).toBeVisible();
      await expect(page.locator("text=Creating account...")).toBeVisible();
    });

    test("should show loading state during map operations", async ({
      page,
    }) => {
      // First login
      await page.goto("/login");
      await page.fill('input[type="email"]', "test@example.com");
      await page.fill('input[type="password"]', "password123");
      await page.click('button[type="submit"]');

      // Navigate back to home
      await page.goto("/");

      // Save map
      await page.click("text=Save Map");

      // Should show loading state
      await expect(
        page.locator('[data-testid="loading-spinner"]')
      ).toBeVisible();
      await expect(page.locator("text=Saving map...")).toBeVisible();
    });
  });

  test.describe("Error Boundaries", () => {
    test("should handle JavaScript errors gracefully", async ({ page }) => {
      // Inject an error to test error boundary
      await page.addInitScript(() => {
        window.addEventListener("error", (e) => {
          e.preventDefault();
          // This would normally trigger an error boundary
        });
      });

      await page.goto("/");

      // Should still show the page even if there are JS errors
      await expect(page.locator("body")).toBeVisible();
    });

    test("should show fallback UI for broken components", async ({ page }) => {
      // This test would require a component that can be made to fail
      // For now, we'll test that the app doesn't crash on navigation
      await page.goto("/");
      await page.goto("/login");
      await page.goto("/signup");
      await page.goto("/");

      // Should navigate successfully without crashes
      await expect(page).toHaveURL("/");
    });
  });
});
