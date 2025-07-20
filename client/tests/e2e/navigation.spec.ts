import { test, expect } from "@playwright/test";

test.describe("Navigation Tests", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/");
  });

  test("should navigate to all pages from home", async ({ page }) => {
    // Test navigation to login
    await page.click("text=Login");
    await expect(page).toHaveURL("/login");
    await expect(page.locator("h1")).toContainText("Welcome Back");

    // Test navigation to signup
    await page.click("text=Sign Up");
    await expect(page).toHaveURL("/signup");
    await expect(page.locator("h1")).toContainText("Join Us");

    // Test navigation back to home
    await page.click("text=Home");
    await expect(page).toHaveURL("/");
    await expect(page.locator("h1")).toContainText("Four Color Theorem");
  });

  test("should show navigation bar on all pages", async ({ page }) => {
    const pages = ["/", "/login", "/signup"];

    for (const route of pages) {
      await page.goto(route);
      await expect(page.locator("nav")).toBeVisible();
    }
  });

  test("should handle direct URL navigation", async ({ page }) => {
    // Test direct navigation to login
    await page.goto("/login");
    await expect(page).toHaveURL("/login");
    await expect(page.locator("h1")).toContainText("Welcome Back");

    // Test direct navigation to signup
    await page.goto("/signup");
    await expect(page).toHaveURL("/signup");
    await expect(page.locator("h1")).toContainText("Join Us");

    // Test direct navigation to profile (should redirect to login if not authenticated)
    await page.goto("/profile");
    await expect(page).toHaveURL("/login");
  });

  test("should maintain navigation state after page refresh", async ({
    page,
  }) => {
    await page.goto("/login");
    await page.reload();
    await expect(page).toHaveURL("/login");
    await expect(page.locator("h1")).toContainText("Welcome Back");
  });
});
