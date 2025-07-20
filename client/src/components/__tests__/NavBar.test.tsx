import { describe, it, expect, vi, beforeEach } from "vitest";
import { screen, fireEvent } from "@testing-library/react";
import { render } from "../../test/test-utils";
import NavBar from "../NavBar";
import { logoutUser } from "../../store/authSlice";

// Mock react-router-dom
vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => vi.fn(),
    Link: ({ children, to }: { children: React.ReactNode; to: string }) => (
      <a href={to}>{children}</a>
    ),
  };
});

// Mock the store hooks
const mockUseAppSelector = vi.fn();
const mockUseAppDispatch = vi.fn();

vi.mock("../../store/hooks", () => ({
  useAppDispatch: () => mockUseAppDispatch,
  useAppSelector: mockUseAppSelector,
}));

// Mock the notification hook
vi.mock("../../hooks/useNotification", () => ({
  useNotification: () => ({
    showNotification: vi.fn(),
  }),
}));

// Mock sketch handlers
vi.mock("../../utils/sketchHandlers", () => ({
  handleResetMap: vi.fn(),
}));

describe("NavBar", () => {
  const mockDispatch = vi.fn();
  const mockNavigate = vi.fn();
  const mockShowNotification = vi.fn();
  const mockHandleResetMap = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    mockUseAppDispatch.mockReturnValue(mockDispatch);
  });

  it("renders navigation bar with logo and title", async () => {
    mockUseAppSelector.mockReturnValue({
      isAuthenticated: false,
    });

    render(<NavBar />);

    expect(screen.getByAltText("Map Coloring Logo")).toBeInTheDocument();
    expect(screen.getByText("ColorMap")).toBeInTheDocument();
    expect(
      screen.getByText("🎨 The Best Map Coloring App in the World! 🗺️")
    ).toBeInTheDocument();
  });

  it("shows sign in button when user is not authenticated", async () => {
    mockUseAppSelector.mockReturnValue({
      isAuthenticated: false,
    });

    render(<NavBar />);

    expect(screen.getByText("Sign In")).toBeInTheDocument();
    expect(screen.queryByText("Profile")).not.toBeInTheDocument();
    expect(screen.queryByText("Sign Out")).not.toBeInTheDocument();
  });

  it("shows profile and sign out buttons when user is authenticated", async () => {
    mockUseAppSelector.mockReturnValue({
      isAuthenticated: true,
    });

    render(<NavBar />);

    expect(screen.getByText("Profile")).toBeInTheDocument();
    expect(screen.getByText("Sign Out")).toBeInTheDocument();
    expect(screen.queryByText("Sign In")).not.toBeInTheDocument();
  });

  it("handles sign out when user clicks sign out button", async () => {
    mockUseAppSelector.mockReturnValue({
      isAuthenticated: true,
    });

    mockDispatch.mockResolvedValue({
      type: logoutUser.fulfilled.type,
    });

    render(<NavBar />);

    const signOutButton = screen.getByText("Sign Out");
    fireEvent.click(signOutButton);

    expect(mockDispatch).toHaveBeenCalledWith(logoutUser());
    expect(mockHandleResetMap).toHaveBeenCalled();
    expect(mockNavigate).toHaveBeenCalledWith("/login");
  });

  it("shows error notification when sign out fails", async () => {
    mockUseAppSelector.mockReturnValue({
      isAuthenticated: true,
    });

    mockDispatch.mockRejectedValue(new Error("Sign out failed"));

    render(<NavBar />);

    const signOutButton = screen.getByText("Sign Out");
    fireEvent.click(signOutButton);

    expect(mockShowNotification).toHaveBeenCalledWith(
      "Error during logout",
      "error"
    );
  });

  it("navigates to home when logo is clicked", async () => {
    mockUseAppSelector.mockReturnValue({
      isAuthenticated: false,
    });

    render(<NavBar />);

    const logoLink = screen.getByRole("link", { name: /colormap/i });
    expect(logoLink).toHaveAttribute("href", "/");
  });

  it("applies hover effects to logo", async () => {
    mockUseAppSelector.mockReturnValue({
      isAuthenticated: false,
    });

    render(<NavBar />);

    const logo = screen.getByAltText("Map Coloring Logo");
    expect(logo).toHaveClass(
      "transform",
      "group-hover:rotate-12",
      "transition-all",
      "duration-300"
    );
  });

  it("has responsive design for logo text", async () => {
    mockUseAppSelector.mockReturnValue({
      isAuthenticated: false,
    });

    render(<NavBar />);

    const logoText = screen.getByText("ColorMap");
    expect(logoText).toHaveClass("hidden", "sm:block");
  });

  it("has responsive title text", async () => {
    mockUseAppSelector.mockReturnValue({
      isAuthenticated: false,
    });

    render(<NavBar />);

    const title = screen.getByText(
      "🎨 The Best Map Coloring App in the World! 🗺️"
    );
    expect(title).toHaveClass("text-lg", "sm:text-xl", "lg:text-2xl");
  });

  it("has proper navigation styling", async () => {
    mockUseAppSelector.mockReturnValue({
      isAuthenticated: false,
    });

    render(<NavBar />);

    const nav = screen.getByRole("navigation");
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
