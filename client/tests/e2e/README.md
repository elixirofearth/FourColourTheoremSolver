# End-to-End Testing with Playwright

This directory contains comprehensive end-to-end tests for the Four Color Theorem Solver application using Playwright.

## Test Structure

### Test Files

- **`navigation.spec.ts`** - Tests navigation between pages and URL handling
- **`authentication.spec.ts`** - Tests login, signup, logout, and protected routes
- **`map-functionality.spec.ts`** - Tests canvas interaction, map creation, and map management
- **`notifications.spec.ts`** - Tests notification system and error handling
- **`comprehensive-workflow.spec.ts`** - Tests complete user workflows and scenarios

### Test Utilities

- **`utils/test-helpers.ts`** - Helper functions for common test operations

## Running Tests

### Basic Commands

```bash
# Run all E2E tests
npm run test:e2e

# Run tests with UI (interactive mode)
npm run test:e2e:ui

# Run tests in headed mode (see browser)
npm run test:e2e:headed

# Run tests in debug mode
npm run test:e2e:debug

# Show test report
npm run test:e2e:report
```

### Running Specific Tests

```bash
# Run a specific test file
npx playwright test navigation.spec.ts

# Run tests matching a pattern
npx playwright test --grep "login"

# Run tests in a specific browser
npx playwright test --project=chromium
```

### Running Tests in Different Environments

```bash
# Run tests against different browsers
npx playwright test --project=firefox
npx playwright test --project=webkit

# Run tests on mobile viewports
npx playwright test --project="Mobile Chrome"
npx playwright test --project="Mobile Safari"
```

## Test Coverage

### Navigation Tests

- Page navigation and routing
- URL handling and direct access
- Navigation state persistence
- Responsive navigation

### Authentication Tests

- User registration and login
- Form validation and error handling
- Protected route access
- Session management
- Logout functionality

### Map Functionality Tests

- Canvas drawing and interaction
- Map creation and saving
- Map viewing and management
- Map deletion with confirmation
- Four color theorem application
- Responsive design testing

### Notification Tests

- Success and error notifications
- Notification auto-dismissal
- Manual notification dismissal
- Multiple notification stacking
- Loading states and error boundaries

### Comprehensive Workflow Tests

- Complete user journeys
- Multi-step processes
- Error handling scenarios
- Network failure handling
- Accessibility testing

## Test Helpers

The `TestHelpers` class provides common operations:

```typescript
// Login with default credentials
await helpers.login();

// Login with custom credentials
await helpers.login("user@example.com", "password");

// Signup new user
await helpers.signup("New User", "newuser@example.com", "password123");

// Draw on canvas
await helpers.drawOnCanvas(100, 100, 200, 200);

// Save map with name
await helpers.saveMap("My Map");

// Check for notifications
await helpers.expectNotification("Success message");

// Check for loading states
await helpers.expectLoadingState("Loading...");

// Mock API responses
await helpers.mockApiResponse("/api/test", { data: "test" });

// Mock API errors
await helpers.mockApiError("/api/test", 500, "Server Error");
```

## Configuration

The Playwright configuration is in `playwright.config.ts` and includes:

- **Multiple browsers**: Chrome, Firefox, Safari, Mobile Chrome, Mobile Safari
- **Auto-start dev server**: Runs `npm run dev` before tests
- **Screenshots and videos**: Captured on test failures
- **Trace collection**: For debugging failed tests
- **Parallel execution**: Tests run in parallel for speed

## Best Practices

### Writing Tests

1. **Use descriptive test names** that explain what is being tested
2. **Group related tests** using `test.describe()`
3. **Use test helpers** for common operations
4. **Test both success and failure scenarios**
5. **Test responsive design** on different viewports
6. **Mock external dependencies** when appropriate

### Test Structure

```typescript
test.describe("Feature Name", () => {
  test.beforeEach(async ({ page }) => {
    // Setup code
  });

  test("should do something specific", async ({ page }) => {
    // Arrange
    await page.goto("/");

    // Act
    await page.click("button");

    // Assert
    await expect(page.locator("result")).toBeVisible();
  });
});
```

### Debugging Tests

1. **Use debug mode**: `npm run test:e2e:debug`
2. **Add screenshots**: `await page.screenshot({ path: 'debug.png' })`
3. **Use trace viewer**: `npx playwright show-trace trace.zip`
4. **Add logging**: `console.log('Debug info')`

## Continuous Integration

For CI/CD, the tests are configured to:

- Run in headless mode
- Retry failed tests on CI
- Generate HTML reports
- Capture screenshots and videos on failure
- Use a single worker on CI for stability

## Troubleshooting

### Common Issues

1. **Tests failing due to timing**: Add explicit waits or use `waitForSelector`
2. **Element not found**: Check if the element exists and is visible
3. **Network errors**: Mock API calls or handle offline scenarios
4. **Browser compatibility**: Test on multiple browsers

### Debugging Commands

```bash
# Show test report
npx playwright show-report

# Show trace
npx playwright show-trace trace.zip

# Install browsers
npx playwright install

# Update browsers
npx playwright install --with-deps
```

## Contributing

When adding new tests:

1. Follow the existing naming conventions
2. Use the test helpers for common operations
3. Add appropriate error handling
4. Test both positive and negative scenarios
5. Update this README if adding new test categories
