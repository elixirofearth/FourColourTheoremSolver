import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { Provider } from "react-redux";
import { BrowserRouter } from "react-router-dom";
import { configureStore } from "@reduxjs/toolkit";
import HomeContent from "../HomeContent";
import authSlice from "../../store/authSlice";
import type { User } from "../../store/authSlice";

// Mock the sketch handlers
vi.mock("../../utils/sketchHandlers", () => ({
  handleColorMap: vi.fn(),
  handleResetMap: vi.fn(),
  handleDownloadMap: vi.fn(),
  handleSaveMap: vi.fn(),
}));

// Mock the Canvas component
vi.mock("../Canvas", () => ({
  default: () => <div data-testid="canvas">Canvas Component</div>,
}));

// Mock useNavigate
const mockNavigate = vi.fn();
vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

// Import mocked functions for assertions
import {
  handleColorMap as mockHandleColorMap,
  handleResetMap as mockHandleResetMap,
} from "../../utils/sketchHandlers";

const createMockStore = (
  isAuthenticated = true,
  user: User | null = { id: 1, name: "Test User", email: "test@example.com" }
) =>
  configureStore({
    reducer: {
      auth: authSlice,
    },
    preloadedState: {
      auth: {
        isAuthenticated,
        user,
        token: isAuthenticated ? "mock-token" : null,
        isLoading: false,
        error: null,
      },
    },
  });

const renderWithProviders = (
  component: React.ReactElement,
  isAuthenticated = true,
  user: User | null = { id: 1, name: "Test User", email: "test@example.com" }
) => {
  const store = createMockStore(isAuthenticated, user);
  return render(
    <Provider store={store}>
      <BrowserRouter>{component}</BrowserRouter>
    </Provider>
  );
};

// Mock window.innerWidth for responsive tests
const mockWindowWidth = (width: number) => {
  Object.defineProperty(window, "innerWidth", {
    writable: true,
    configurable: true,
    value: width,
  });
  window.dispatchEvent(new Event("resize"));
};

describe("HomeContent", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockNavigate.mockClear();
    // Default to desktop width for most tests
    mockWindowWidth(1200);
  });

  describe("Authentication and Loading", () => {
    it("redirects to login when user is not authenticated", () => {
      renderWithProviders(<HomeContent />, false, null);
      expect(mockNavigate).toHaveBeenCalledWith("/login");
    });

    it("shows loading state initially", () => {
      // Mock the store to show loading state
      const store = configureStore({
        reducer: { auth: authSlice },
        preloadedState: {
          auth: {
            isAuthenticated: false,
            user: null,
            token: null,
            isLoading: true,
            error: null,
          },
        },
      });

      render(
        <Provider store={store}>
          <BrowserRouter>
            <HomeContent />
          </BrowserRouter>
        </Provider>
      );

      expect(screen.getByText("Loading...")).toBeInTheDocument();
    });

    it("shows welcome message with user name", () => {
      renderWithProviders(<HomeContent />);
      expect(screen.getByText(/Welcome back, Test User!/)).toBeInTheDocument();
    });

    it("shows generic user name when user name is not available", () => {
      renderWithProviders(<HomeContent />, true, null);
      expect(screen.getByText(/Welcome back, User!/)).toBeInTheDocument();
    });
  });

  describe("Main Content Structure", () => {
    it("renders the main content when authenticated", () => {
      renderWithProviders(<HomeContent />);
      expect(
        screen.getByText(/Ready to create some amazing maps/)
      ).toBeInTheDocument();
    });

    it("renders canvas section with proper styling", () => {
      renderWithProviders(<HomeContent />);
      expect(screen.getByText("🖌️ Drawing Canvas")).toBeInTheDocument();
    });

    it("renders canvas instructions", () => {
      renderWithProviders(<HomeContent />);
      expect(
        screen.getByText("Click and drag to draw your map regions")
      ).toBeInTheDocument();
    });

    it("renders the Canvas component", () => {
      renderWithProviders(<HomeContent />);
      expect(screen.getByTestId("canvas")).toBeInTheDocument();
    });
  });

  describe("Mobile/Desktop Responsive Behavior", () => {
    it("shows canvas on desktop screens", () => {
      mockWindowWidth(1200); // Desktop width
      renderWithProviders(<HomeContent />);
      expect(screen.getByText("🖌️ Drawing Canvas")).toBeInTheDocument();
      expect(screen.getByTestId("canvas")).toBeInTheDocument();
    });

    it("shows mobile message on smaller screens", () => {
      mockWindowWidth(800); // Tablet width
      renderWithProviders(<HomeContent />);
      expect(
        screen.getByText("Desktop Required for Drawing")
      ).toBeInTheDocument();
      expect(
        screen.getByText(/The drawing canvas is optimized for desktop use/)
      ).toBeInTheDocument();
    });
  });

  describe("Action Buttons", () => {
    it("renders all four action buttons", () => {
      renderWithProviders(<HomeContent />);

      expect(
        screen.getByRole("button", { name: "🎨 Color Map" })
      ).toBeInTheDocument();
      expect(
        screen.getByRole("button", { name: "🔄 Reset Canvas" })
      ).toBeInTheDocument();
      expect(
        screen.getByRole("button", { name: "⬇️ Download" })
      ).toBeInTheDocument();
      expect(
        screen.getByRole("button", { name: "💾 Save Map" })
      ).toBeInTheDocument();
    });

    it("buttons are disabled on mobile/tablet screens", () => {
      mockWindowWidth(800); // Tablet width
      renderWithProviders(<HomeContent />);

      const colorMapButton = screen.getByRole("button", {
        name: "🎨 Color Map",
      });
      const resetButton = screen.getByRole("button", {
        name: "🔄 Reset Canvas",
      });
      const downloadButton = screen.getByRole("button", {
        name: "⬇️ Download",
      });
      const saveButton = screen.getByRole("button", { name: "💾 Save Map" });

      expect(colorMapButton).toBeDisabled();
      expect(resetButton).toBeDisabled();
      expect(downloadButton).toBeDisabled();
      expect(saveButton).toBeDisabled();
    });

    it("buttons are disabled by default (responsive design)", () => {
      mockWindowWidth(1200); // Desktop width
      renderWithProviders(<HomeContent />);

      const colorMapButton = screen.getByRole("button", {
        name: "🎨 Color Map",
      });
      const resetButton = screen.getByRole("button", {
        name: "🔄 Reset Canvas",
      });
      const downloadButton = screen.getByRole("button", {
        name: "⬇️ Download",
      });
      const saveButton = screen.getByRole("button", { name: "💾 Save Map" });

      // Buttons are disabled by design in the current implementation
      // The responsive behavior is handled via CSS classes, not JavaScript
      expect(colorMapButton).toBeDisabled();
      expect(resetButton).toBeDisabled();
      expect(downloadButton).toBeDisabled();
      expect(saveButton).toBeDisabled();
    });

    it("does not call handlers when buttons are clicked (buttons are disabled)", () => {
      mockWindowWidth(1200); // Desktop width
      renderWithProviders(<HomeContent />);

      const colorMapButton = screen.getByRole("button", {
        name: "🎨 Color Map",
      });
      fireEvent.click(colorMapButton);

      // Since buttons are disabled, handlers should not be called
      expect(mockHandleColorMap).not.toHaveBeenCalled();
    });

    it("does not call handlers when buttons are clicked on mobile", () => {
      mockWindowWidth(800); // Mobile width
      renderWithProviders(<HomeContent />);

      const colorMapButton = screen.getByRole("button", {
        name: "🎨 Color Map",
      });
      fireEvent.click(colorMapButton);

      expect(mockHandleColorMap).not.toHaveBeenCalled();
    });
  });

  describe("Button Styling and Icons", () => {
    it("Color Map button has disabled styling on mobile", () => {
      mockWindowWidth(800); // Mobile width
      renderWithProviders(<HomeContent />);

      const colorMapButton = screen.getByRole("button", {
        name: "🎨 Color Map",
      });
      expect(colorMapButton).toHaveClass(
        "bg-gradient-to-r",
        "from-gray-400",
        "to-gray-500",
        "cursor-not-allowed",
        "opacity-60"
      );
    });

    it("Reset Canvas button has disabled styling on mobile", () => {
      mockWindowWidth(800); // Mobile width
      renderWithProviders(<HomeContent />);

      const resetButton = screen.getByRole("button", {
        name: "🔄 Reset Canvas",
      });
      expect(resetButton).toHaveClass(
        "bg-gradient-to-r",
        "from-gray-400",
        "to-gray-500",
        "cursor-not-allowed",
        "opacity-60"
      );
    });

    it("Download button has disabled styling on mobile", () => {
      mockWindowWidth(800); // Mobile width
      renderWithProviders(<HomeContent />);

      const downloadButton = screen.getByRole("button", {
        name: "⬇️ Download",
      });
      expect(downloadButton).toHaveClass(
        "bg-gradient-to-r",
        "from-gray-400",
        "to-gray-500",
        "cursor-not-allowed",
        "opacity-60"
      );
    });

    it("Save Map button has disabled styling on mobile", () => {
      mockWindowWidth(800); // Mobile width
      renderWithProviders(<HomeContent />);

      const saveButton = screen.getByRole("button", { name: "💾 Save Map" });
      expect(saveButton).toHaveClass(
        "bg-gradient-to-r",
        "from-gray-400",
        "to-gray-500",
        "cursor-not-allowed",
        "opacity-60"
      );
    });

    it("buttons have responsive classes for large screens", () => {
      mockWindowWidth(1200); // Desktop width
      renderWithProviders(<HomeContent />);

      const colorMapButton = screen.getByRole("button", {
        name: "🎨 Color Map",
      });
      expect(colorMapButton).toHaveClass(
        "lg:bg-gradient-to-r",
        "lg:from-emerald-500",
        "lg:to-teal-600",
        "lg:opacity-100",
        "lg:cursor-pointer"
      );
    });
  });

  describe("Instructions Section", () => {
    it("renders instructions section", () => {
      renderWithProviders(<HomeContent />);
      expect(screen.getByText("📝 How to use:")).toBeInTheDocument();
    });

    it("renders all four instruction steps", () => {
      renderWithProviders(<HomeContent />);
      expect(
        screen.getByText(/Draw regions on the canvas using your mouse/)
      ).toBeInTheDocument();
      expect(
        screen.getByText(/Click "Color Map" to apply the four-color theorem/)
      ).toBeInTheDocument();
      expect(
        screen.getByText(/Use "Reset" to clear and start over/)
      ).toBeInTheDocument();
      expect(
        screen.getByText(/"Save" your masterpiece or "Download" as image/)
      ).toBeInTheDocument();
    });

    it("has proper instruction styling with responsive classes", () => {
      renderWithProviders(<HomeContent />);
      const instructionsSection = screen
        .getByText("📝 How to use:")
        .closest("div");
      expect(instructionsSection).toHaveClass(
        "bg-gradient-to-r",
        "from-blue-50",
        "to-purple-50",
        "rounded-xl",
        "sm:rounded-2xl",
        "p-4",
        "sm:p-6",
        "border",
        "border-blue-200"
      );
    });
  });

  describe("Layout and Responsive Design", () => {
    it("has proper main container styling with responsive classes", () => {
      renderWithProviders(<HomeContent />);
      const mainContainer = document.querySelector(
        ".min-h-screen.bg-gradient-to-br.from-blue-50.via-indigo-50.to-purple-50.py-4.sm\\:py-8.px-4"
      );
      expect(mainContainer).toBeInTheDocument();
    });

    it("has proper content card styling with responsive classes", () => {
      renderWithProviders(<HomeContent />);
      const contentCard = document.querySelector(
        ".bg-white\\/80.backdrop-blur-sm.rounded-2xl.sm\\:rounded-3xl.shadow-2xl.p-4.sm\\:p-6.lg\\:p-8.border.border-white\\/20"
      );
      expect(contentCard).toBeInTheDocument();
    });

    it("has responsive grid layout for action buttons", () => {
      renderWithProviders(<HomeContent />);
      const buttonGrid = document.querySelector(
        ".grid.grid-cols-2.lg\\:grid-cols-4.gap-3.sm\\:gap-4"
      );
      expect(buttonGrid).toBeInTheDocument();
    });
  });

  describe("Accessibility", () => {
    it("has proper button roles", () => {
      renderWithProviders(<HomeContent />);
      const buttons = screen.getAllByRole("button");
      expect(buttons).toHaveLength(4);
    });

    it("has proper heading structure", () => {
      renderWithProviders(<HomeContent />);

      const headings = screen.getAllByRole("heading");
      expect(headings).toHaveLength(4); // Welcome heading, Canvas heading, Mobile message heading, Instructions heading

      expect(screen.getByRole("heading", { level: 1 })).toBeInTheDocument();
      expect(screen.getAllByRole("heading", { level: 2 })).toHaveLength(2); // Canvas and Mobile message headings
      expect(screen.getByRole("heading", { level: 3 })).toBeInTheDocument(); // Instructions heading
    });
  });

  describe("Edge Cases", () => {
    it("handles user with no name property", () => {
      renderWithProviders(<HomeContent />, true, {
        id: 1,
        name: "",
        email: "test@example.com",
      });
      expect(screen.getByText(/Welcome back, User!/)).toBeInTheDocument();
    });

    it("handles user with null name", () => {
      renderWithProviders(<HomeContent />, true, null);
      expect(screen.getByText(/Welcome back, User!/)).toBeInTheDocument();
    });

    it("handles user with empty name", () => {
      renderWithProviders(<HomeContent />, true, {
        id: 1,
        name: "",
        email: "test@example.com",
      });
      expect(screen.getByText(/Welcome back, User!/)).toBeInTheDocument();
    });

    it("handles multiple button clicks on desktop", () => {
      mockWindowWidth(1200); // Desktop width
      renderWithProviders(<HomeContent />);

      const colorMapButton = screen.getByRole("button", {
        name: "🎨 Color Map",
      });
      const resetButton = screen.getByRole("button", {
        name: "🔄 Reset Canvas",
      });

      fireEvent.click(colorMapButton);
      fireEvent.click(colorMapButton);
      fireEvent.click(resetButton);
      fireEvent.click(resetButton);

      // Since buttons are disabled, handlers should not be called
      expect(mockHandleColorMap).not.toHaveBeenCalled();
      expect(mockHandleResetMap).not.toHaveBeenCalled();
    });
  });

  describe("Loading State", () => {
    it("shows loading spinner and text", () => {
      const store = configureStore({
        reducer: { auth: authSlice },
        preloadedState: {
          auth: {
            isAuthenticated: false,
            user: null,
            token: null,
            isLoading: true,
            error: null,
          },
        },
      });

      render(
        <Provider store={store}>
          <BrowserRouter>
            <HomeContent />
          </BrowserRouter>
        </Provider>
      );

      expect(screen.getByText("Loading...")).toBeInTheDocument();
      expect(document.querySelector(".animate-spin")).toBeInTheDocument();
    });

    it("has proper loading container styling with responsive classes", () => {
      const store = configureStore({
        reducer: { auth: authSlice },
        preloadedState: {
          auth: {
            isAuthenticated: false,
            user: null,
            token: null,
            isLoading: true,
            error: null,
          },
        },
      });

      render(
        <Provider store={store}>
          <BrowserRouter>
            <HomeContent />
          </BrowserRouter>
        </Provider>
      );

      const mainContainer = document.querySelector(
        ".min-h-screen.bg-gradient-to-br.from-blue-50.to-purple-100.flex.items-center.justify-center.px-4"
      );
      expect(mainContainer).toBeInTheDocument();
    });
  });
});
