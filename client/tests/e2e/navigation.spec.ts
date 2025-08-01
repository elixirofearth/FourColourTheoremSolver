import { test, expect } from "@playwright/test";

test.describe("Navigation Tests", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/");
  });

  test("should show navigation bar on home page", async ({ page }) => {
    // go to login page
    await page.goto("/login");
    // login first
    await page.fill('input[type="text"]', "test@example.com");
    await page.fill('input[type="password"]', "password123");
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL("/");
    // check if the navigation bar is visible
    await expect(page.locator("nav")).toBeVisible();
  });

  test("should not show navigation bar on login and signup pages", async ({
    page,
  }) => {
    // check if the navigation bar is not visible
    await page.goto("/login");
    await expect(page.locator("nav")).not.toBeVisible();
    await page.goto("/signup");
    await expect(page.locator("nav")).not.toBeVisible();
  });

  test("should handle direct URL navigation", async ({ page }) => {
    // Test direct navigation to login
    await page.goto("/login");
    await expect(page).toHaveURL("/login");
    await expect(page.locator("h2")).toContainText("Welcome Back");

    // Test direct navigation to signup
    await page.goto("/signup");
    await expect(page).toHaveURL("/signup");
    await expect(page.locator("h2")).toContainText("Join Us");

    // Test direct navigation to profile (should redirect to login if not authenticated)
    await page.goto("/profile");
    await expect(page).toHaveURL("/login");

    // Test direct navigation to home (should redirect to login if not authenticated)
    await page.goto("/");
    await expect(page).toHaveURL("/login");
  });

  test("should maintain navigation state after page refresh", async ({
    page,
  }) => {
    await page.goto("/login");
    await page.reload();
    await expect(page).toHaveURL("/login");
    await expect(page.locator("h2")).toContainText("Welcome Back");
  });

  test("should go to login page by clicking the sign out button on the navigation bar", async ({
    page,
  }) => {
    await page.goto("/login");
    await page.fill('input[type="text"]', "test@example.com");
    await page.fill('input[type="password"]', "password123");
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL("/");
    // click the sign out button on the navigation bar
    await expect(page.locator("nav")).toBeVisible();
    await page.click("text= Sign Out");
    await expect(page).toHaveURL("/login");
  });

  test("should go to profile page by clicking the profile button on the navigation bar", async ({
    page,
  }) => {
    await page.goto("/login");
    await page.fill('input[type="text"]', "test@example.com");
    await page.fill('input[type="password"]', "password123");
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL("/");
    await expect(page.locator("nav")).toBeVisible();
    await page.click("text= Profile");
    await expect(page).toHaveURL("/profile");
  });

  test("should go to home page by clicking the ColorMap logo on the navigation bar", async ({
    page,
  }) => {
    await page.goto("/login");
    await page.fill('input[type="text"]', "test@example.com");
    await page.fill('input[type="password"]', "password123");
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL("/");
    await expect(page.locator("nav")).toBeVisible();
    await page.click("text= Profile");
    await expect(page).toHaveURL("/profile");
    await page.click('img[alt="Map Coloring Logo"]');
    await expect(page).toHaveURL("/");
  });
});
