import { describe, it, expect, vi, beforeEach } from "vitest";
import { screen, fireEvent } from "@testing-library/react";
import { render } from "../../test/test-utils";
import { SignInButton, ProfileButton, SignOutButton } from "../Buttons";

// Mock react-router-dom
vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    Link: ({ children, to }: { children: React.ReactNode; to: string }) => (
      <a href={to}>{children}</a>
    ),
  };
});

describe("Buttons", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("SignInButton", () => {
    it("renders sign in button with correct text", () => {
      render(<SignInButton />);

      expect(screen.getByText("Sign In")).toBeInTheDocument();
    });

    it("renders sign in button with correct styling classes", () => {
      render(<SignInButton />);

      const button = screen.getByText("Sign In");
      expect(button).toHaveClass(
        "px-6",
        "py-2",
        "bg-gradient-to-r",
        "from-blue-500",
        "to-purple-600",
        "text-white",
        "font-semibold",
        "rounded-full",
        "hover:from-blue-600",
        "hover:to-purple-700",
        "transform",
        "hover:-translate-y-0.5",
        "hover:shadow-lg",
        "transition-all",
        "duration-300",
        "focus:outline-none"
      );
    });

    it("navigates to login page when clicked", () => {
      render(<SignInButton />);

      const link = screen.getByRole("link", { name: "Sign In" });
      expect(link).toHaveAttribute("href", "/login");
    });

    it("has proper button element structure", () => {
      render(<SignInButton />);

      const button = screen.getByRole("button", { name: "Sign In" });
      expect(button).toBeInTheDocument();
      expect(button.tagName).toBe("BUTTON");
    });
  });

  describe("ProfileButton", () => {
    it("renders profile button with correct text", () => {
      render(<ProfileButton />);

      expect(screen.getByText("Profile")).toBeInTheDocument();
    });

    it("renders profile button with correct styling classes", () => {
      render(<ProfileButton />);

      const button = screen.getByText("Profile");
      expect(button).toHaveClass(
        "px-6",
        "py-2",
        "bg-gradient-to-r",
        "from-emerald-500",
        "to-teal-600",
        "text-white",
        "font-semibold",
        "rounded-full",
        "hover:from-emerald-600",
        "hover:to-teal-700",
        "transform",
        "hover:-translate-y-0.5",
        "hover:shadow-lg",
        "transition-all",
        "duration-300",
        "focus:outline-none"
      );
    });

    it("navigates to profile page when clicked", () => {
      render(<ProfileButton />);

      const link = screen.getByRole("link", { name: "Profile" });
      expect(link).toHaveAttribute("href", "/profile");
    });

    it("has proper button element structure", () => {
      render(<ProfileButton />);

      const button = screen.getByRole("button", { name: "Profile" });
      expect(button).toBeInTheDocument();
      expect(button.tagName).toBe("BUTTON");
    });
  });

  describe("SignOutButton", () => {
    const mockOnSignOut = vi.fn();

    beforeEach(() => {
      mockOnSignOut.mockClear();
    });

    it("renders sign out button with correct text", () => {
      render(<SignOutButton onSignOut={mockOnSignOut} />);

      expect(screen.getByText("Sign Out")).toBeInTheDocument();
    });

    it("renders sign out button with correct styling classes", () => {
      render(<SignOutButton onSignOut={mockOnSignOut} />);

      const button = screen.getByText("Sign Out");
      expect(button).toHaveClass(
        "px-6",
        "py-2",
        "bg-gradient-to-r",
        "from-red-500",
        "to-pink-600",
        "text-white",
        "font-semibold",
        "rounded-full",
        "hover:from-red-600",
        "hover:to-pink-700",
        "transform",
        "hover:-translate-y-0.5",
        "hover:shadow-lg",
        "transition-all",
        "duration-300",
        "focus:outline-none"
      );
    });

    it("calls onSignOut callback when clicked", () => {
      render(<SignOutButton onSignOut={mockOnSignOut} />);

      const button = screen.getByRole("button", { name: "Sign Out" });
      fireEvent.click(button);

      expect(mockOnSignOut).toHaveBeenCalledTimes(1);
    });

    it("has proper button element structure", () => {
      render(<SignOutButton onSignOut={mockOnSignOut} />);

      const button = screen.getByRole("button", { name: "Sign Out" });
      expect(button).toBeInTheDocument();
      expect(button.tagName).toBe("BUTTON");
    });

    it("does not render as a link (unlike other buttons)", () => {
      render(<SignOutButton onSignOut={mockOnSignOut} />);

      const links = screen.queryAllByRole("link");
      expect(links).toHaveLength(0);
    });

    it("handles multiple clicks correctly", () => {
      render(<SignOutButton onSignOut={mockOnSignOut} />);

      const button = screen.getByRole("button", { name: "Sign Out" });
      fireEvent.click(button);
      fireEvent.click(button);
      fireEvent.click(button);

      expect(mockOnSignOut).toHaveBeenCalledTimes(3);
    });
  });

  describe("Button Accessibility", () => {
    it("all buttons are keyboard accessible", () => {
      const mockOnSignOut = vi.fn();

      render(
        <>
          <SignInButton />
          <ProfileButton />
          <SignOutButton onSignOut={mockOnSignOut} />
        </>
      );

      const signInButton = screen.getByRole("button", { name: "Sign In" });
      const profileButton = screen.getByRole("button", { name: "Profile" });
      const signOutButton = screen.getByRole("button", { name: "Sign Out" });

      // Buttons are naturally keyboard accessible
      expect(signInButton).toBeInTheDocument();
      expect(profileButton).toBeInTheDocument();
      expect(signOutButton).toBeInTheDocument();

      // Verify buttons can be found by their accessible name
      expect(
        screen.getByRole("button", { name: "Sign In" })
      ).toBeInTheDocument();
      expect(
        screen.getByRole("button", { name: "Profile" })
      ).toBeInTheDocument();
      expect(
        screen.getByRole("button", { name: "Sign Out" })
      ).toBeInTheDocument();
    });

    it("all buttons have focus outline styles", () => {
      const mockOnSignOut = vi.fn();

      render(
        <>
          <SignInButton />
          <ProfileButton />
          <SignOutButton onSignOut={mockOnSignOut} />
        </>
      );

      const signInButton = screen.getByRole("button", { name: "Sign In" });
      const profileButton = screen.getByRole("button", { name: "Profile" });
      const signOutButton = screen.getByRole("button", { name: "Sign Out" });

      expect(signInButton).toHaveClass("focus:outline-none");
      expect(profileButton).toHaveClass("focus:outline-none");
      expect(signOutButton).toHaveClass("focus:outline-none");
    });
  });

  describe("Button Styling Consistency", () => {
    it("all buttons have consistent base styling", () => {
      const mockOnSignOut = vi.fn();

      render(
        <>
          <SignInButton />
          <ProfileButton />
          <SignOutButton onSignOut={mockOnSignOut} />
        </>
      );

      const buttons = screen.getAllByRole("button");

      buttons.forEach((button) => {
        expect(button).toHaveClass(
          "px-6",
          "py-2",
          "text-white",
          "font-semibold",
          "rounded-full",
          "transform",
          "hover:-translate-y-0.5",
          "hover:shadow-lg",
          "transition-all",
          "duration-300",
          "focus:outline-none"
        );
      });
    });

    it("each button has unique gradient colors", () => {
      const mockOnSignOut = vi.fn();

      render(
        <>
          <SignInButton />
          <ProfileButton />
          <SignOutButton onSignOut={mockOnSignOut} />
        </>
      );

      const signInButton = screen.getByRole("button", { name: "Sign In" });
      const profileButton = screen.getByRole("button", { name: "Profile" });
      const signOutButton = screen.getByRole("button", { name: "Sign Out" });

      // SignInButton: blue to purple
      expect(signInButton).toHaveClass("from-blue-500", "to-purple-600");

      // ProfileButton: emerald to teal
      expect(profileButton).toHaveClass("from-emerald-500", "to-teal-600");

      // SignOutButton: red to pink
      expect(signOutButton).toHaveClass("from-red-500", "to-pink-600");
    });
  });
});
