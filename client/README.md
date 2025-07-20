# Four Color Theorem Solver - React Client

A modern React application for solving the Four Color Theorem using interactive map coloring. Built with React 19, TypeScript, Vite, Redux Toolkit, and Tailwind CSS.

## Features

- 🎨 Interactive map coloring interface
- 🔐 User authentication with JWT tokens
- 📱 Responsive design with Tailwind CSS
- 🔄 Real-time state management with Redux Toolkit
- 🎯 TypeScript for type safety
- 🔔 Toast notifications system
- 🧪 Comprehensive test suite with Vitest and React Testing Library

## Tech Stack

- **Frontend Framework**: React 19
- **Build Tool**: Vite
- **Language**: TypeScript
- **State Management**: Redux Toolkit with Redux Persist
- **Styling**: Tailwind CSS
- **Routing**: React Router DOM
- **Testing**: Vitest + React Testing Library
- **HTTP Client**: Fetch API
- **Authentication**: JWT tokens

## Project Structure

```
client/
├── src/
│   ├── components/          # Reusable UI components
│   │   ├── __tests__/      # Component tests
│   │   ├── Buttons.tsx     # Button components
│   │   ├── Canvas.tsx      # Drawing canvas
│   │   ├── LoginForm.tsx   # Login form
│   │   ├── NavBar.tsx      # Navigation bar
│   │   └── Notification.tsx # Toast notifications
│   ├── contexts/           # React contexts
│   │   ├── NotificationContext.tsx
│   │   └── NotificationContextDef.ts
│   ├── hooks/              # Custom React hooks
│   │   ├── __tests__/      # Hook tests
│   │   └── useNotification.ts
│   ├── pages/              # Page components
│   │   ├── HomePage.tsx
│   │   ├── LoginPage.tsx
│   │   ├── MapPage.tsx
│   │   ├── ProfilePage.tsx
│   │   └── SignUpPage.tsx
│   ├── store/              # Redux store
│   │   ├── __tests__/      # Store tests
│   │   ├── authSlice.ts    # Authentication slice
│   │   ├── hooks.ts        # Redux hooks
│   │   ├── persistConfig.ts # Persistence config
│   │   └── store.ts        # Store configuration
│   ├── test/               # Test utilities
│   │   ├── setup.ts        # Test setup
│   │   └── test-utils.tsx  # Custom render function
│   ├── utils/              # Utility functions
│   │   ├── __tests__/      # Utility tests
│   │   ├── authInterceptor.ts
│   │   ├── sketchHandlers.ts
│   │   └── tokenUtils.ts   # JWT token utilities
│   ├── App.tsx             # Main app component
│   └── main.tsx            # App entry point
├── public/                 # Static assets
├── package.json            # Dependencies and scripts
├── vite.config.ts          # Vite configuration
├── vitest.config.ts        # Vitest configuration
└── tailwind.config.js      # Tailwind configuration
```

## Getting Started

### Prerequisites

- Node.js 18+
- npm or yarn

### Installation

1. Clone the repository
2. Navigate to the client directory:

   ```bash
   cd client
   ```

3. Install dependencies:

   ```bash
   npm install
   ```

4. Create a `.env` file in the client directory:

   ```env
   VITE_API_GATEWAY_URL=http://localhost:8080
   ```

5. Start the development server:
   ```bash
   npm run dev
   ```

The application will be available at `http://localhost:5173`

## Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint
- `npm test` - Run tests in watch mode
- `npm run test:ui` - Run tests with UI
- `npm run test:coverage` - Run tests with coverage
- `npm run test:run` - Run tests once

## Testing

This project includes a comprehensive test suite using Vitest and React Testing Library.

### Test Structure

- **Unit Tests**: Test individual functions and utilities
- **Component Tests**: Test React components in isolation
- **Integration Tests**: Test component interactions and Redux state
- **Hook Tests**: Test custom React hooks

### Running Tests

```bash
# Run all tests
npm test

# Run tests with UI
npm run test:ui

# Run tests with coverage
npm run test:coverage

# Run tests once
npm run test:run
```

### Test Files

- `src/utils/__tests__/tokenUtils.test.ts` - JWT token utility tests
- `src/store/__tests__/authSlice.test.ts` - Redux auth slice tests
- `src/components/__tests__/LoginForm.test.tsx` - Login form component tests
- `src/components/__tests__/NavBar.test.tsx` - Navigation bar tests
- `src/components/__tests__/Notification.test.tsx` - Notification component tests
- `src/hooks/__tests__/useNotification.test.ts` - Notification hook tests

### Test Utilities

- `src/test/setup.ts` - Global test setup with mocks
- `src/test/test-utils.tsx` - Custom render function with providers

### Testing Best Practices

1. **Mock External Dependencies**: All external APIs and browser APIs are mocked
2. **Test User Interactions**: Use `fireEvent` and `userEvent` for user interactions
3. **Test Accessibility**: Use semantic queries like `getByRole` and `getByLabelText`
4. **Test Error States**: Ensure error handling is properly tested
5. **Test Loading States**: Verify loading states are displayed correctly
6. **Test Responsive Design**: Test component behavior across different screen sizes

### Example Test

```typescript
import { describe, it, expect, vi } from "vitest";
import { screen, fireEvent } from "@testing-library/react";
import { render } from "../test/test-utils";
import LoginForm from "../LoginForm";

describe("LoginForm", () => {
  it("renders login form with all elements", () => {
    render(<LoginForm />);

    expect(screen.getByText("Welcome Back!")).toBeInTheDocument();
    expect(screen.getByLabelText("Email Address")).toBeInTheDocument();
    expect(screen.getByLabelText("Password")).toBeInTheDocument();
  });
});
```

## Key Components

### Authentication

The app uses JWT tokens for authentication with the following features:

- **Login/Register**: User registration and login forms
- **Token Management**: Automatic token refresh and expiration handling
- **Protected Routes**: Route protection based on authentication state
- **Persistent Sessions**: User sessions persist across browser restarts

### State Management

Redux Toolkit is used for state management with the following slices:

- **Auth Slice**: Handles user authentication state
- **Persistence**: Redux Persist for state persistence

### UI Components

- **LoginForm**: User authentication form with validation
- **NavBar**: Responsive navigation with authentication state
- **Notification**: Toast notification system
- **Canvas**: Interactive drawing canvas for map coloring

### Utilities

- **tokenUtils.ts**: JWT token decoding and validation
- **authInterceptor.ts**: HTTP request interception for authentication
- **sketchHandlers.ts**: Canvas drawing and interaction handlers

## Development Guidelines

### Code Style

- Use TypeScript for all new code
- Follow ESLint configuration
- Use functional components with hooks
- Implement proper error boundaries
- Write comprehensive tests for new features

### Component Structure

- Keep components small and focused
- Use custom hooks for complex logic
- Implement proper prop types
- Use React.memo for performance optimization when needed

### State Management

- Use Redux for global state
- Use local state for component-specific state
- Implement proper loading and error states
- Use Redux Toolkit for simplified Redux code

## Deployment

### Build for Production

```bash
npm run build
```

The built files will be in the `dist` directory.

### Environment Variables

Required environment variables:

- `VITE_API_GATEWAY_URL`: Backend API gateway URL

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## License

This project is licensed under the MIT License.
