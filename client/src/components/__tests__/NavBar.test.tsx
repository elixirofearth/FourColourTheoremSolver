import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { Provider } from "react-redux";
import { BrowserRouter } from "react-router-dom";
import { configureStore } from "@reduxjs/toolkit";
import NavBar from "../NavBar";
import authSlice from "../../store/authSlice";

// Mock the notification hook
const mockShowNotification = vi.fn();
vi.mock("../../hooks/useNotification", () => ({
  useNotification: () => ({
    showNotification: mockShowNotification,
  }),
}));

// Mock the sketch handlers
vi.mock("../../utils/sketchHandlers", () => ({
  handleResetMap: vi.fn(),
}));

// Mock react-router-dom
const mockNavigate = vi.fn();
vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

const createMockStore = (isAuthenticated = false) =>
  configureStore({
    reducer: {
      auth: authSlice,
    },
    preloadedState: {
      auth: {
        isAuthenticated,
        user: isAuthenticated
          ? { id: 1, name: "Test User", email: "test@example.com" }
          : null,
        token: isAuthenticated ? "mock-token" : null,
        isLoading: false,
        error: null,
      },
    },
  });

const renderWithProviders = (isAuthenticated = false) => {
  const store = createMockStore(isAuthenticated);
  return render(
    <Provider store={store}>
      <BrowserRouter>
        <NavBar />
      </BrowserRouter>
    </Provider>
  );
};

describe("NavBar", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockNavigate.mockClear();
  });

  it("renders navigation bar with logo and title", () => {
    renderWithProviders();

    expect(screen.getByAltText("Map Coloring Logo")).toBeInTheDocument();
    expect(screen.getByText("ColorMap")).toBeInTheDocument();
    expect(
      screen.getByText("🎨 The Best Map Coloring App in the World! 🗺️")
    ).toBeInTheDocument();
  });

  it("shows sign in button when user is not authenticated", () => {
    renderWithProviders(false);

    expect(screen.getByText("Sign In")).toBeInTheDocument();
    expect(screen.queryByText("Profile")).not.toBeInTheDocument();
    expect(screen.queryByText("Sign Out")).not.toBeInTheDocument();
  });

  it("shows profile and sign out buttons when user is authenticated", () => {
    renderWithProviders(true);

    expect(screen.getByText("Profile")).toBeInTheDocument();
    expect(screen.getByText("Sign Out")).toBeInTheDocument();
    expect(screen.queryByText("Sign In")).not.toBeInTheDocument();
  });

  it("handles sign out when user clicks sign out button", async () => {
    renderWithProviders(true);

    const signOutButton = screen.getByText("Sign Out");
    fireEvent.click(signOutButton);

    // The sign out process should be initiated
    expect(mockNavigate).toHaveBeenCalledWith("/login");
  });

  it("shows error notification when sign out fails", async () => {
    // Mock the dispatch to throw an error
    const mockDispatch = vi
      .fn()
      .mockRejectedValue(new Error("Sign out failed"));

    const store = configureStore({
      reducer: { auth: authSlice },
      preloadedState: {
        auth: {
          isAuthenticated: true,
          user: { id: 1, name: "Test User", email: "test@example.com" },
          token: "mock-token",
          isLoading: false,
          error: null,
        },
      },
    });

    // Override the dispatch method
    store.dispatch = mockDispatch;

    render(
      <Provider store={store}>
        <BrowserRouter>
          <NavBar />
        </BrowserRouter>
      </Provider>
    );

    const signOutButton = screen.getByText("Sign Out");
    fireEvent.click(signOutButton);

    // Wait for the error to be handled
    await vi.waitFor(() => {
      expect(mockShowNotification).toHaveBeenCalledWith(
        "Error during logout",
        "error"
      );
    });
  });

  it("navigates to home when logo is clicked", () => {
    renderWithProviders();

    const logoLink = screen.getByAltText("Map Coloring Logo").closest("a");
    expect(logoLink).toHaveAttribute("href", "/");
  });

  it("applies hover effects to logo", () => {
    renderWithProviders();

    const logo = screen.getByAltText("Map Coloring Logo");
    expect(logo).toHaveClass(
      "transform",
      "group-hover:rotate-12",
      "transition-all",
      "duration-300"
    );
  });

  it("has responsive design for logo text", () => {
    renderWithProviders();

    const logoText = screen.getByText("ColorMap");
    expect(logoText).toHaveClass("hidden", "xs:block");
  });

  it("has responsive title text", () => {
    renderWithProviders();

    // Check for the full title (desktop version)
    const fullTitle = screen.getByText(
      "🎨 The Best Map Coloring App in the World! 🗺️"
    );
    expect(fullTitle).toHaveClass("hidden", "sm:inline");

    // Check for the mobile title
    const mobileTitle = screen.getByText("🎨 ColorMap 🗺️");
    expect(mobileTitle).toHaveClass("sm:hidden");
  });

  it("has proper navigation styling", () => {
    renderWithProviders();

    const nav = document.querySelector("nav");
    expect(nav).toHaveClass(
      "bg-gradient-to-r",
      "from-blue-600",
      "via-purple-600",
      "to-indigo-700",
      "shadow-xl",
      "border-b-4",
      "border-white/20"
    );
  });
});
