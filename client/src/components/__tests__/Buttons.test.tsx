import { describe, it, expect, vi } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
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

const renderWithRouter = (component: React.ReactElement) => {
  return render(<BrowserRouter>{component}</BrowserRouter>);
};

describe("Buttons", () => {
  describe("SignInButton", () => {
    it("renders sign in button with correct text", () => {
      renderWithRouter(<SignInButton />);

      const button = screen.getByText("Sign In");
      expect(button).toBeInTheDocument();
    });

    it("renders sign in button with correct styling classes", () => {
      renderWithRouter(<SignInButton />);

      const button = screen.getByText("Sign In");
      expect(button).toHaveClass(
        "px-3",
        "sm:px-6",
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
        "focus:outline-none",
        "text-sm",
        "sm:text-base"
      );
    });

    it("is wrapped in a Link component", () => {
      renderWithRouter(<SignInButton />);

      const link = document.querySelector('a[href="/login"]');
      expect(link).toBeInTheDocument();
    });

    it("has proper button role", () => {
      renderWithRouter(<SignInButton />);

      const button = screen.getByRole("button");
      expect(button).toBeInTheDocument();
    });
  });

  describe("ProfileButton", () => {
    it("renders profile button with correct text", () => {
      renderWithRouter(<ProfileButton />);

      const button = screen.getByText("Profile");
      expect(button).toBeInTheDocument();
    });

    it("renders profile button with correct styling classes", () => {
      renderWithRouter(<ProfileButton />);

      const button = screen.getByText("Profile");
      expect(button).toHaveClass(
        "px-3",
        "sm:px-6",
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
        "focus:outline-none",
        "text-sm",
        "sm:text-base"
      );
    });

    it("is wrapped in a Link component", () => {
      renderWithRouter(<ProfileButton />);

      const link = document.querySelector('a[href="/profile"]');
      expect(link).toBeInTheDocument();
    });

    it("has proper button role", () => {
      renderWithRouter(<ProfileButton />);

      const button = screen.getByRole("button");
      expect(button).toBeInTheDocument();
    });
  });

  describe("SignOutButton", () => {
    it("renders sign out button with correct text", () => {
      const mockOnSignOut = vi.fn();
      render(<SignOutButton onSignOut={mockOnSignOut} />);

      const button = screen.getByText("Sign Out");
      expect(button).toBeInTheDocument();
    });

    it("calls onSignOut when clicked", () => {
      const mockOnSignOut = vi.fn();
      render(<SignOutButton onSignOut={mockOnSignOut} />);

      const button = screen.getByText("Sign Out");
      fireEvent.click(button);

      expect(mockOnSignOut).toHaveBeenCalledTimes(1);
    });

    it("renders sign out button with correct styling classes", () => {
      const mockOnSignOut = vi.fn();
      render(<SignOutButton onSignOut={mockOnSignOut} />);

      const button = screen.getByText("Sign Out");
      expect(button).toHaveClass(
        "px-3",
        "sm:px-6",
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
        "focus:outline-none",
        "text-sm",
        "sm:text-base"
      );
    });

    it("has proper button role", () => {
      const mockOnSignOut = vi.fn();
      render(<SignOutButton onSignOut={mockOnSignOut} />);

      const button = screen.getByRole("button");
      expect(button).toBeInTheDocument();
    });

    it("is accessible with keyboard", () => {
      const mockOnSignOut = vi.fn();
      render(<SignOutButton onSignOut={mockOnSignOut} />);

      const button = screen.getByRole("button");
      button.focus();
      expect(button).toHaveFocus();
    });
  });

  describe("Button Styling Consistency", () => {
    it("all buttons have consistent base styling", () => {
      const mockOnSignOut = vi.fn();
      render(
        <div>
          <BrowserRouter>
            <SignInButton />
            <ProfileButton />
          </BrowserRouter>
          <SignOutButton onSignOut={mockOnSignOut} />
        </div>
      );

      const buttons = screen.getAllByRole("button");
      expect(buttons).toHaveLength(3);

      buttons.forEach((button) => {
        expect(button).toHaveClass(
          "px-3",
          "sm:px-6",
          "py-2",
          "text-white",
          "font-semibold",
          "rounded-full",
          "transform",
          "hover:-translate-y-0.5",
          "hover:shadow-lg",
          "transition-all",
          "duration-300",
          "focus:outline-none",
          "text-sm",
          "sm:text-base"
        );
      });
    });

    it("all buttons have gradient backgrounds", () => {
      const mockOnSignOut = vi.fn();
      render(
        <div>
          <BrowserRouter>
            <SignInButton />
            <ProfileButton />
          </BrowserRouter>
          <SignOutButton onSignOut={mockOnSignOut} />
        </div>
      );

      const buttons = screen.getAllByRole("button");
      buttons.forEach((button) => {
        expect(button).toHaveClass("bg-gradient-to-r");
      });
    });

    it("buttons have different color schemes", () => {
      const mockOnSignOut = vi.fn();
      render(
        <div>
          <BrowserRouter>
            <SignInButton />
            <ProfileButton />
          </BrowserRouter>
          <SignOutButton onSignOut={mockOnSignOut} />
        </div>
      );

      const signInButton = screen.getByText("Sign In");
      const profileButton = screen.getByText("Profile");
      const signOutButton = screen.getByText("Sign Out");

      expect(signInButton).toHaveClass("from-blue-500", "to-purple-600");
      expect(profileButton).toHaveClass("from-emerald-500", "to-teal-600");
      expect(signOutButton).toHaveClass("from-red-500", "to-pink-600");
    });
  });

  describe("Accessibility", () => {
    it("all buttons are keyboard accessible", () => {
      const mockOnSignOut = vi.fn();
      render(
        <div>
          <BrowserRouter>
            <SignInButton />
            <ProfileButton />
          </BrowserRouter>
          <SignOutButton onSignOut={mockOnSignOut} />
        </div>
      );

      const buttons = screen.getAllByRole("button");
      buttons.forEach((button) => {
        button.focus();
        expect(button).toHaveFocus();
      });
    });

    it("buttons have proper focus styles", () => {
      const mockOnSignOut = vi.fn();
      render(
        <div>
          <BrowserRouter>
            <SignInButton />
            <ProfileButton />
          </BrowserRouter>
          <SignOutButton onSignOut={mockOnSignOut} />
        </div>
      );

      const buttons = screen.getAllByRole("button");
      buttons.forEach((button) => {
        expect(button).toHaveClass("focus:outline-none");
      });
    });
  });
});
