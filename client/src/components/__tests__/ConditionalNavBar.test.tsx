import { describe, it, expect, vi, beforeEach } from "vitest";
import { screen } from "@testing-library/react";
import { render } from "../../test/test-utils";
import ConditionalNavBar from "../ConditionalNavBar";

// Mock react-router-dom
const mockUseLocation = vi.hoisted(() => vi.fn());
vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useLocation: () => mockUseLocation(),
  };
});

// Mock NavBar component
vi.mock("../NavBar", () => ({
  default: vi.fn(() => <div data-testid="navbar">Mock NavBar</div>),
}));

describe("ConditionalNavBar", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("when on auth pages", () => {
    it("returns null when on /login route", () => {
      mockUseLocation.mockReturnValue({
        pathname: "/login",
      });

      const { container } = render(<ConditionalNavBar />);

      expect(container.firstChild).toBeNull();
    });

    it("returns null when on /signup route", () => {
      mockUseLocation.mockReturnValue({
        pathname: "/signup",
      });

      const { container } = render(<ConditionalNavBar />);

      expect(container.firstChild).toBeNull();
    });

    it("returns null when on /login with query parameters", () => {
      mockUseLocation.mockReturnValue({
        pathname: "/login",
        search: "?redirect=/home",
      });

      const { container } = render(<ConditionalNavBar />);

      expect(container.firstChild).toBeNull();
    });

    it("returns null when on /signup with query parameters", () => {
      mockUseLocation.mockReturnValue({
        pathname: "/signup",
        search: "?source=email",
      });

      const { container } = render(<ConditionalNavBar />);

      expect(container.firstChild).toBeNull();
    });
  });

  describe("when on non-auth pages", () => {
    it("renders NavBar when on home page", () => {
      mockUseLocation.mockReturnValue({
        pathname: "/",
      });

      render(<ConditionalNavBar />);

      expect(screen.getByTestId("navbar")).toBeInTheDocument();
    });

    it("renders NavBar when on profile page", () => {
      mockUseLocation.mockReturnValue({
        pathname: "/profile",
      });

      render(<ConditionalNavBar />);

      expect(screen.getByTestId("navbar")).toBeInTheDocument();
    });

    it("renders NavBar when on map page", () => {
      mockUseLocation.mockReturnValue({
        pathname: "/map",
      });

      render(<ConditionalNavBar />);

      expect(screen.getByTestId("navbar")).toBeInTheDocument();
    });

    it("renders NavBar when on any other route", () => {
      mockUseLocation.mockReturnValue({
        pathname: "/some-other-page",
      });

      render(<ConditionalNavBar />);

      expect(screen.getByTestId("navbar")).toBeInTheDocument();
    });

    it("renders NavBar when on nested routes", () => {
      mockUseLocation.mockReturnValue({
        pathname: "/profile/settings",
      });

      render(<ConditionalNavBar />);

      expect(screen.getByTestId("navbar")).toBeInTheDocument();
    });

    it("renders NavBar when on routes with query parameters", () => {
      mockUseLocation.mockReturnValue({
        pathname: "/map",
        search: "?region=europe",
      });

      render(<ConditionalNavBar />);

      expect(screen.getByTestId("navbar")).toBeInTheDocument();
    });
  });

  describe("useLocation integration", () => {
    it("calls useLocation hook", () => {
      mockUseLocation.mockReturnValue({
        pathname: "/",
      });

      render(<ConditionalNavBar />);

      expect(mockUseLocation).toHaveBeenCalledTimes(1);
    });

    it("handles location object with only pathname", () => {
      mockUseLocation.mockReturnValue({
        pathname: "/home",
      });

      render(<ConditionalNavBar />);

      expect(screen.getByTestId("navbar")).toBeInTheDocument();
    });

    it("handles location object with pathname and search", () => {
      mockUseLocation.mockReturnValue({
        pathname: "/profile",
        search: "?tab=settings",
      });

      render(<ConditionalNavBar />);

      expect(screen.getByTestId("navbar")).toBeInTheDocument();
    });

    it("handles location object with pathname, search, and hash", () => {
      mockUseLocation.mockReturnValue({
        pathname: "/map",
        search: "?region=asia",
        hash: "#region-1",
      });

      render(<ConditionalNavBar />);

      expect(screen.getByTestId("navbar")).toBeInTheDocument();
    });
  });

  describe("route matching logic", () => {
    it("exactly matches /login route", () => {
      mockUseLocation.mockReturnValue({
        pathname: "/login",
      });

      const { container } = render(<ConditionalNavBar />);

      expect(container.firstChild).toBeNull();
    });

    it("exactly matches /signup route", () => {
      mockUseLocation.mockReturnValue({
        pathname: "/signup",
      });

      const { container } = render(<ConditionalNavBar />);

      expect(container.firstChild).toBeNull();
    });

    it("does not match partial routes", () => {
      mockUseLocation.mockReturnValue({
        pathname: "/login-page",
      });

      render(<ConditionalNavBar />);

      expect(screen.getByTestId("navbar")).toBeInTheDocument();
    });

    it("does not match routes that start with auth paths", () => {
      mockUseLocation.mockReturnValue({
        pathname: "/login-success",
      });

      render(<ConditionalNavBar />);

      expect(screen.getByTestId("navbar")).toBeInTheDocument();
    });

    it("does not match routes that end with auth paths", () => {
      mockUseLocation.mockReturnValue({
        pathname: "/user/login",
      });

      render(<ConditionalNavBar />);

      expect(screen.getByTestId("navbar")).toBeInTheDocument();
    });
  });

  describe("component behavior", () => {
    it("renders nothing when should hide navbar", () => {
      mockUseLocation.mockReturnValue({
        pathname: "/login",
      });

      const { container } = render(<ConditionalNavBar />);

      expect(container.firstChild).toBeNull();
      expect(container.innerHTML).toBe("");
    });

    it("renders NavBar component when should show navbar", () => {
      mockUseLocation.mockReturnValue({
        pathname: "/",
      });

      render(<ConditionalNavBar />);

      expect(screen.getByTestId("navbar")).toBeInTheDocument();
      expect(screen.getByText("Mock NavBar")).toBeInTheDocument();
    });
  });

  describe("edge cases", () => {
    it("handles empty pathname", () => {
      mockUseLocation.mockReturnValue({
        pathname: "",
      });

      render(<ConditionalNavBar />);

      expect(screen.getByTestId("navbar")).toBeInTheDocument();
    });

    it("handles root pathname", () => {
      mockUseLocation.mockReturnValue({
        pathname: "/",
      });

      render(<ConditionalNavBar />);

      expect(screen.getByTestId("navbar")).toBeInTheDocument();
    });

    it("handles undefined pathname", () => {
      mockUseLocation.mockReturnValue({
        pathname: undefined,
      });

      render(<ConditionalNavBar />);

      expect(screen.getByTestId("navbar")).toBeInTheDocument();
    });

    it("handles null pathname", () => {
      mockUseLocation.mockReturnValue({
        pathname: null,
      });

      render(<ConditionalNavBar />);

      expect(screen.getByTestId("navbar")).toBeInTheDocument();
    });
  });
});
