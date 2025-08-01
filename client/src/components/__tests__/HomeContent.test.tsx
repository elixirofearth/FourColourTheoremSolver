import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { vi, beforeEach, afterEach, describe, it, expect } from "vitest";
import { BrowserRouter } from "react-router-dom";
import { Provider } from "react-redux";
import { configureStore } from "@reduxjs/toolkit";
import NotificationProvider from "../../contexts/NotificationContext";
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

// Mock react-router-dom
const mockNavigate = vi.fn();
vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

// Mock window.innerWidth
const mockWindowWidth = (width: number) => {
  Object.defineProperty(window, "innerWidth", {
    writable: true,
    configurable: true,
    value: width,
  });
  // Trigger resize event to update the component
  window.dispatchEvent(new Event("resize"));
};

const createMockStore = (
  isAuthenticated: boolean,
  user: User | null = null
) => {
  return configureStore({
    reducer: { auth: authSlice },
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
};

const renderWithProviders = (
  isAuthenticated: boolean = true,
  user: User | null = null
) => {
  const store = createMockStore(isAuthenticated, user);
  return render(
    <Provider store={store}>
      <NotificationProvider>
        <BrowserRouter>
          <HomeContent />
        </BrowserRouter>
      </NotificationProvider>
    </Provider>
  );
};

describe("HomeContent", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // Set default desktop width
    mockWindowWidth(1200);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe("Authentication and Loading", () => {
    it("redirects to login when user is not authenticated", () => {
      renderWithProviders(false);
      expect(mockNavigate).toHaveBeenCalledWith("/login");
    });

    it("shows loading state initially", async () => {
      // Create a store with loading state
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
          <NotificationProvider>
            <BrowserRouter>
              <HomeContent />
            </BrowserRouter>
          </NotificationProvider>
        </Provider>
      );

      expect(screen.getByText("Loading...")).toBeInTheDocument();
    });

    it("shows welcome message with user name", async () => {
      const user: User = { id: 1, name: "John Doe", email: "john@example.com" };
      renderWithProviders(true, user);

      await waitFor(() => {
        expect(screen.getByText(/Welcome back, John Doe!/)).toBeInTheDocument();
      });
    });

    it("shows generic user name when user name is not available", async () => {
      renderWithProviders(true, null);

      await waitFor(() => {
        expect(screen.getByText(/Welcome back, User!/)).toBeInTheDocument();
      });
    });
  });

  describe("Main Content Structure", () => {
    it("renders the main content when authenticated", async () => {
      renderWithProviders();
      await waitFor(() => {
        expect(
          screen.getByText(/Ready to create some amazing maps/)
        ).toBeInTheDocument();
      });
    });

    it("renders canvas section with proper styling", async () => {
      renderWithProviders();
      await waitFor(() => {
        const canvasSection = screen.getByText("🖌️ Drawing Canvas");
        expect(canvasSection).toBeInTheDocument();
      });
    });

    it("renders canvas instructions", async () => {
      renderWithProviders();
      await waitFor(() => {
        expect(
          screen.getByText("Click and drag to draw your map regions")
        ).toBeInTheDocument();
      });
    });

    it("renders the Canvas component", async () => {
      renderWithProviders();
      await waitFor(() => {
        expect(screen.getByText("🖌️ Drawing Canvas")).toBeInTheDocument();
      });
    });
  });

  describe("Mobile/Desktop Responsive Behavior", () => {
    it("shows canvas on desktop screens", async () => {
      mockWindowWidth(1200); // Desktop width
      renderWithProviders();

      await waitFor(() => {
        expect(screen.getByText("🖌️ Drawing Canvas")).toBeInTheDocument();
      });
    });

    it("shows mobile message on smaller screens", async () => {
      mockWindowWidth(800); // Mobile width
      renderWithProviders();

      await waitFor(() => {
        expect(
          screen.getByText("Desktop Required for Drawing")
        ).toBeInTheDocument();
      });
    });
  });

  describe("Action Buttons", () => {
    it("renders all four action buttons", async () => {
      renderWithProviders();
      await waitFor(() => {
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
    });

    it("buttons are disabled on mobile/tablet screens", async () => {
      mockWindowWidth(800); // Tablet width
      renderWithProviders();

      await waitFor(() => {
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
    });

    it("buttons are enabled on desktop screens", async () => {
      mockWindowWidth(1200); // Desktop width
      renderWithProviders();

      await waitFor(() => {
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

        expect(colorMapButton).not.toBeDisabled();
        expect(resetButton).not.toBeDisabled();
        expect(downloadButton).not.toBeDisabled();
        expect(saveButton).not.toBeDisabled();
      });
    });

    it("calls handlers when buttons are clicked on desktop", async () => {
      const {
        handleColorMap,
        handleResetMap,
        handleDownloadMap,
        handleSaveMap,
      } = await import("../../utils/sketchHandlers");

      mockWindowWidth(1200); // Desktop width
      renderWithProviders();

      await waitFor(() => {
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

        fireEvent.click(colorMapButton);
        fireEvent.click(resetButton);
        fireEvent.click(downloadButton);
        fireEvent.click(saveButton);

        expect(handleColorMap).toHaveBeenCalledTimes(1);
        expect(handleResetMap).toHaveBeenCalledTimes(1);
        expect(handleDownloadMap).toHaveBeenCalledTimes(1);
        expect(handleSaveMap).toHaveBeenCalledTimes(1);
      });
    });

    it("does not call handlers when buttons are clicked on mobile", async () => {
      const { handleColorMap } = await import("../../utils/sketchHandlers");

      mockWindowWidth(800); // Mobile width
      renderWithProviders();

      await waitFor(() => {
        const colorMapButton = screen.getByRole("button", {
          name: "🎨 Color Map",
        });
        fireEvent.click(colorMapButton);
        expect(handleColorMap).not.toHaveBeenCalled();
      });
    });
  });

  describe("Button Styling and Icons", () => {
    it("Color Map button has disabled styling on mobile", async () => {
      mockWindowWidth(800); // Mobile width
      renderWithProviders();

      await waitFor(() => {
        const colorMapButton = screen.getByRole("button", {
          name: "🎨 Color Map",
        });
        expect(colorMapButton).toHaveClass(
          "from-gray-400",
          "to-gray-500",
          "cursor-not-allowed",
          "opacity-60"
        );
      });
    });

    it("Reset Canvas button has disabled styling on mobile", async () => {
      mockWindowWidth(800); // Mobile width
      renderWithProviders();

      await waitFor(() => {
        const resetButton = screen.getByRole("button", {
          name: "🔄 Reset Canvas",
        });
        expect(resetButton).toHaveClass(
          "from-gray-400",
          "to-gray-500",
          "cursor-not-allowed",
          "opacity-60"
        );
      });
    });

    it("Download button has disabled styling on mobile", async () => {
      mockWindowWidth(800); // Mobile width
      renderWithProviders();

      await waitFor(() => {
        const downloadButton = screen.getByRole("button", {
          name: "⬇️ Download",
        });
        expect(downloadButton).toHaveClass(
          "from-gray-400",
          "to-gray-500",
          "cursor-not-allowed",
          "opacity-60"
        );
      });
    });

    it("Save Map button has disabled styling on mobile", async () => {
      mockWindowWidth(800); // Mobile width
      renderWithProviders();

      await waitFor(() => {
        const saveButton = screen.getByRole("button", { name: "💾 Save Map" });
        expect(saveButton).toHaveClass(
          "from-gray-400",
          "to-gray-500",
          "cursor-not-allowed",
          "opacity-60"
        );
      });
    });

    it("buttons have enabled styling on desktop", async () => {
      mockWindowWidth(1200); // Desktop width
      renderWithProviders();

      await waitFor(() => {
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

        expect(colorMapButton).toHaveClass(
          "from-emerald-500",
          "to-teal-600",
          "cursor-pointer"
        );
        expect(resetButton).toHaveClass(
          "from-red-500",
          "to-pink-600",
          "cursor-pointer"
        );
        expect(downloadButton).toHaveClass(
          "from-blue-500",
          "to-indigo-600",
          "cursor-pointer"
        );
        expect(saveButton).toHaveClass(
          "from-purple-500",
          "to-violet-600",
          "cursor-pointer"
        );
      });
    });
  });

  describe("Instructions Section", () => {
    it("renders instructions section", async () => {
      renderWithProviders();
      await waitFor(() => {
        expect(screen.getByText("📝 How to use:")).toBeInTheDocument();
      });
    });

    it("renders all four instruction steps", async () => {
      renderWithProviders();
      await waitFor(() => {
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
    });

    it("has proper instruction styling with responsive classes", async () => {
      renderWithProviders();
      await waitFor(() => {
        const instructionsSection = screen
          .getByText("📝 How to use:")
          .closest("div");
        expect(instructionsSection).toHaveClass(
          "from-blue-50",
          "to-purple-50",
          "rounded-xl",
          "sm:rounded-2xl"
        );
      });
    });
  });

  describe("Layout and Responsive Design", () => {
    it("has proper main container styling with responsive classes", async () => {
      renderWithProviders();
      await waitFor(() => {
        const mainContainer = screen.getByText(/Welcome back/).closest("div");
        expect(mainContainer).toHaveClass(
          "min-h-screen",
          "bg-gradient-to-br",
          "from-blue-50",
          "via-indigo-50",
          "to-purple-50"
        );
      });
    });

    it("has proper content card styling with responsive classes", async () => {
      renderWithProviders();
      await waitFor(() => {
        const contentCard = screen
          .getByText(/Ready to create some amazing maps/)
          .closest("div");
        expect(contentCard).toHaveClass(
          "bg-white/80",
          "backdrop-blur-sm",
          "rounded-2xl",
          "sm:rounded-3xl"
        );
      });
    });

    it("has responsive grid layout for action buttons", async () => {
      renderWithProviders();
      await waitFor(() => {
        const buttonGrid = screen
          .getByRole("button", { name: "🎨 Color Map" })
          .closest("div");
        expect(buttonGrid).toHaveClass("grid", "grid-cols-2", "lg:grid-cols-4");
      });
    });
  });

  describe("Accessibility", () => {
    it("has proper button roles", async () => {
      renderWithProviders();
      await waitFor(() => {
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
    });

    it("has proper heading structure", async () => {
      renderWithProviders();
      await waitFor(() => {
        const headings = screen.getAllByRole("heading");
        expect(headings).toHaveLength(3); // Welcome header, Canvas title, Instructions title
      });
    });
  });

  describe("Edge Cases", () => {
    it("handles user with no name property", async () => {
      const user: User = { id: 1, name: "", email: "test@example.com" };
      renderWithProviders(true, user);

      await waitFor(() => {
        expect(screen.getByText(/Welcome back, User!/)).toBeInTheDocument();
      });
    });

    it("handles user with empty name", async () => {
      const user: User = { id: 1, name: "", email: "test@example.com" };
      renderWithProviders(true, user);

      await waitFor(() => {
        expect(screen.getByText(/Welcome back, User!/)).toBeInTheDocument();
      });
    });

    it("handles multiple button clicks on desktop", async () => {
      const { handleColorMap, handleResetMap } = await import(
        "../../utils/sketchHandlers"
      );

      mockWindowWidth(1200); // Desktop width
      renderWithProviders();

      await waitFor(() => {
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

        expect(handleColorMap).toHaveBeenCalledTimes(2);
        expect(handleResetMap).toHaveBeenCalledTimes(2);
      });
    });
  });

  describe("Loading State", () => {
    it("shows loading spinner and text", async () => {
      // Create a store with loading state
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
          <NotificationProvider>
            <BrowserRouter>
              <HomeContent />
            </BrowserRouter>
          </NotificationProvider>
        </Provider>
      );

      expect(screen.getByText("Loading...")).toBeInTheDocument();
    });

    it("has proper loading container styling with responsive classes", async () => {
      // Create a store with loading state
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
          <NotificationProvider>
            <BrowserRouter>
              <HomeContent />
            </BrowserRouter>
          </NotificationProvider>
        </Provider>
      );

      const loadingContainer = screen.getByText("Loading...").closest("div");
      expect(loadingContainer).toHaveClass(
        "bg-white/80",
        "backdrop-blur-sm",
        "rounded-2xl"
      );
    });
  });
});
