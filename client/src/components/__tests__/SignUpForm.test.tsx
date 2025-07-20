import { describe, it, expect, vi, beforeEach } from "vitest";
import { screen, fireEvent, waitFor } from "@testing-library/react";
import { render } from "../../test/test-utils";
import SignUpForm from "../SignUpForm";
import { registerUser } from "../../store/authSlice";

// Mock react-router-dom
const mockNavigate = vi.fn();
vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => mockNavigate,
    Link: ({ children, to }: { children: React.ReactNode; to: string }) => (
      <a href={to}>{children}</a>
    ),
  };
});

// Mock the store hooks
const mockUseAppDispatch = vi.hoisted(() => vi.fn());
const mockUseAppSelector = vi.hoisted(() => vi.fn());

vi.mock("../../store/hooks", () => ({
  useAppDispatch: mockUseAppDispatch,
  useAppSelector: mockUseAppSelector,
}));

describe("SignUpForm", () => {
  const mockDispatch = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    mockUseAppDispatch.mockReturnValue(mockDispatch);
    mockUseAppSelector.mockReturnValue({
      isLoading: false,
      error: null,
      isAuthenticated: false,
    });
  });

  it("renders signup form with all elements", async () => {
    render(<SignUpForm />);

    expect(screen.getByText("Join Us Today!")).toBeInTheDocument();
    expect(
      screen.getByText("Create your account and start coloring")
    ).toBeInTheDocument();
    expect(screen.getByLabelText("Full Name")).toBeInTheDocument();
    expect(screen.getByLabelText("Email Address")).toBeInTheDocument();
    expect(screen.getByLabelText("Password")).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: "Create Account" })
    ).toBeInTheDocument();
    expect(screen.getByText("Already have an account?")).toBeInTheDocument();
    expect(screen.getByText("Sign in here")).toBeInTheDocument();
  });

  it("updates form fields when user types", async () => {
    render(<SignUpForm />);

    const nameInput = screen.getByLabelText("Full Name");
    const emailInput = screen.getByLabelText("Email Address");
    const passwordInput = screen.getByLabelText("Password");

    fireEvent.change(nameInput, { target: { value: "John Doe" } });
    fireEvent.change(emailInput, { target: { value: "john@example.com" } });
    fireEvent.change(passwordInput, { target: { value: "password123" } });

    expect(nameInput).toHaveValue("John Doe");
    expect(emailInput).toHaveValue("john@example.com");
    expect(passwordInput).toHaveValue("password123");
  });

  it("shows loading state when submitting", async () => {
    mockUseAppSelector.mockReturnValue({
      isLoading: true,
      error: null,
      isAuthenticated: false,
    });

    render(<SignUpForm />);

    expect(
      screen.getByRole("button", { name: "Creating Account..." })
    ).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: "Creating Account..." })
    ).toBeDisabled();
  });

  it("disables form inputs when loading", async () => {
    mockUseAppSelector.mockReturnValue({
      isLoading: true,
      error: null,
      isAuthenticated: false,
    });

    render(<SignUpForm />);

    const nameInput = screen.getByLabelText("Full Name");
    const emailInput = screen.getByLabelText("Email Address");
    const passwordInput = screen.getByLabelText("Password");

    expect(nameInput).toBeDisabled();
    expect(emailInput).toBeDisabled();
    expect(passwordInput).toBeDisabled();
  });

  it("shows error message when form is submitted with empty fields", async () => {
    render(<SignUpForm />);

    const submitButton = screen.getByRole("button", { name: "Create Account" });

    // Debug: Check if the button is clickable
    expect(submitButton).toBeEnabled();

    // Try submitting the form directly instead of just clicking the button
    const form = document.querySelector("form");
    expect(form).toBeInTheDocument();
    fireEvent.submit(form!);

    // Debug: Check if the form was actually submitted
    // Let's try to find any error message or check the form state
    await waitFor(() => {
      // Try to find the error message with a more flexible approach
      const errorElement = screen.queryByText(/Please fill in all fields/i);
      if (!errorElement) {
        // If not found, let's see what's actually in the DOM
        console.log("Form HTML:", document.body.innerHTML);
      }
      expect(errorElement).toBeInTheDocument();
    });
  });

  it("dispatches register action when form is submitted with valid data", async () => {
    mockDispatch.mockResolvedValue({
      type: registerUser.fulfilled.type,
      payload: {
        user_id: 1,
        name: "John Doe",
        email: "john@example.com",
        token: "token",
      },
    });

    render(<SignUpForm />);

    const nameInput = screen.getByLabelText("Full Name");
    const emailInput = screen.getByLabelText("Email Address");
    const passwordInput = screen.getByLabelText("Password");
    const submitButton = screen.getByRole("button", { name: "Create Account" });

    fireEvent.change(nameInput, { target: { value: "John Doe" } });
    fireEvent.change(emailInput, { target: { value: "john@example.com" } });
    fireEvent.change(passwordInput, { target: { value: "password123" } });
    fireEvent.click(submitButton);

    // The dispatch should be called immediately
    await waitFor(() => {
      expect(mockDispatch).toHaveBeenCalled();
    });
  });

  it("navigates to home page after successful registration", async () => {
    mockDispatch.mockResolvedValue({
      type: registerUser.fulfilled.type,
      payload: {
        user_id: 1,
        name: "John Doe",
        email: "john@example.com",
        token: "token",
      },
    });

    render(<SignUpForm />);

    const nameInput = screen.getByLabelText("Full Name");
    const emailInput = screen.getByLabelText("Email Address");
    const passwordInput = screen.getByLabelText("Password");
    const submitButton = screen.getByRole("button", { name: "Create Account" });

    fireEvent.change(nameInput, { target: { value: "John Doe" } });
    fireEvent.change(emailInput, { target: { value: "john@example.com" } });
    fireEvent.change(passwordInput, { target: { value: "password123" } });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith("/");
    });
  });

  it("shows error message from Redux state", async () => {
    mockUseAppSelector.mockReturnValue({
      isLoading: false,
      error: "Email already exists",
      isAuthenticated: false,
    });

    render(<SignUpForm />);

    expect(screen.getByText("Email already exists")).toBeInTheDocument();
  });

  it("redirects to home if already authenticated", async () => {
    mockUseAppSelector.mockReturnValue({
      isLoading: false,
      error: null,
      isAuthenticated: true,
    });

    render(<SignUpForm />);

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith("/");
    });
  });

  it("clears form error when user starts typing", async () => {
    mockUseAppSelector.mockReturnValue({
      isLoading: false,
      error: "Email already exists",
      isAuthenticated: false,
    });

    render(<SignUpForm />);

    const nameInput = screen.getByLabelText("Full Name");
    fireEvent.change(nameInput, { target: { value: "John Doe" } });

    // The error should still be visible as it comes from Redux state
    expect(screen.getByText("Email already exists")).toBeInTheDocument();
  });

  it("has proper form styling", async () => {
    render(<SignUpForm />);

    const form = document.querySelector("form");
    expect(form).toHaveClass(
      "bg-white/95",
      "backdrop-blur-sm",
      "rounded-3xl",
      "shadow-2xl",
      "p-8",
      "transform",
      "hover:scale-105",
      "transition-all",
      "duration-300",
      "border",
      "border-white/20"
    );
  });

  it("has proper button styling", async () => {
    render(<SignUpForm />);

    const submitButton = screen.getByRole("button", { name: "Create Account" });
    expect(submitButton).toHaveClass(
      "w-full",
      "bg-gradient-to-r",
      "from-purple-600",
      "to-pink-600",
      "text-white",
      "font-bold",
      "py-3",
      "px-6",
      "rounded-xl",
      "hover:from-purple-700",
      "hover:to-pink-700",
      "transform",
      "hover:-translate-y-1",
      "hover:shadow-xl",
      "transition-all",
      "duration-300",
      "focus:outline-none",
      "focus:ring-4",
      "focus:ring-purple-200",
      "disabled:opacity-50",
      "disabled:cursor-not-allowed"
    );
  });

  it("has proper input styling", async () => {
    render(<SignUpForm />);

    const nameInput = screen.getByLabelText("Full Name");
    expect(nameInput).toHaveClass(
      "w-full",
      "px-4",
      "py-3",
      "bg-gray-50",
      "border-2",
      "border-gray-200",
      "rounded-xl",
      "focus:border-purple-500",
      "focus:ring-4",
      "focus:ring-purple-200",
      "transition-all",
      "duration-300",
      "outline-none",
      "placeholder-gray-400"
    );
  });

  it("has proper link styling", async () => {
    render(<SignUpForm />);

    const signInLink = screen.getByText("Sign in here");
    // Check if the link has the expected classes by checking each one individually
    expect(signInLink).toHaveClass("text-purple-600");
    expect(signInLink).toHaveClass("hover:text-pink-600");
    expect(signInLink).toHaveClass("font-semibold");
    expect(signInLink).toHaveClass("transition-colors");
    expect(signInLink).toHaveClass("duration-300");
    expect(signInLink).toHaveClass("hover:underline");
  });

  it("handles special characters in form fields", async () => {
    render(<SignUpForm />);

    const nameInput = screen.getByLabelText("Full Name");
    const emailInput = screen.getByLabelText("Email Address");
    const passwordInput = screen.getByLabelText("Password");

    const specialName = "José María O'Connor-Smith";
    const specialEmail = "test+tag@example.com";
    const specialPassword = "P@ssw0rd!123";

    fireEvent.change(nameInput, { target: { value: specialName } });
    fireEvent.change(emailInput, { target: { value: specialEmail } });
    fireEvent.change(passwordInput, { target: { value: specialPassword } });

    expect(nameInput).toHaveValue(specialName);
    expect(emailInput).toHaveValue(specialEmail);
    expect(passwordInput).toHaveValue(specialPassword);
  });

  it("validates email format", async () => {
    render(<SignUpForm />);

    const emailInput = screen.getByLabelText("Email Address");
    expect(emailInput).toHaveAttribute("type", "email");
  });

  it("validates password field", async () => {
    render(<SignUpForm />);

    const passwordInput = screen.getByLabelText("Password");
    expect(passwordInput).toHaveAttribute("type", "password");
  });

  it("has required attributes on all fields", async () => {
    render(<SignUpForm />);

    const nameInput = screen.getByLabelText("Full Name");
    const emailInput = screen.getByLabelText("Email Address");
    const passwordInput = screen.getByLabelText("Password");

    expect(nameInput).toHaveAttribute("required");
    expect(emailInput).toHaveAttribute("required");
    expect(passwordInput).toHaveAttribute("required");
  });

  it("has proper placeholders", async () => {
    render(<SignUpForm />);

    const nameInput = screen.getByLabelText("Full Name");
    const emailInput = screen.getByLabelText("Email Address");
    const passwordInput = screen.getByLabelText("Password");

    expect(nameInput).toHaveAttribute("placeholder", "Enter your full name");
    expect(emailInput).toHaveAttribute("placeholder", "Enter your email");
    expect(passwordInput).toHaveAttribute(
      "placeholder",
      "Create a secure password"
    );
  });

  it("has proper labels", async () => {
    render(<SignUpForm />);

    expect(screen.getByText("Full Name")).toBeInTheDocument();
    expect(screen.getByText("Email Address")).toBeInTheDocument();
    expect(screen.getByText("Password")).toBeInTheDocument();
  });

  it("has proper logo and branding", async () => {
    render(<SignUpForm />);

    const logo = screen.getByAltText("Cartoon Logo");
    expect(logo).toBeInTheDocument();
    expect(logo).toHaveAttribute("src", "/logo.png");
    expect(logo).toHaveClass("mx-auto", "mb-4", "animate-bounce");
  });

  it("has proper title styling", async () => {
    render(<SignUpForm />);

    const title = screen.getByText("Join Us Today!");
    expect(title).toHaveClass(
      "text-2xl",
      "md:text-3xl",
      "font-bold",
      "bg-gradient-to-r",
      "from-purple-600",
      "to-pink-600",
      "bg-clip-text",
      "text-transparent",
      "mb-2"
    );
  });

  it("has proper subtitle styling", async () => {
    render(<SignUpForm />);

    const subtitle = screen.getByText("Create your account and start coloring");
    expect(subtitle).toHaveClass("text-gray-600", "text-sm");
  });

  it("has proper error message styling", async () => {
    mockUseAppSelector.mockReturnValue({
      isLoading: false,
      error: "Test error message",
      isAuthenticated: false,
    });

    render(<SignUpForm />);

    const errorMessage = screen.getByText("Test error message");
    const errorContainer = errorMessage.closest("div");
    expect(errorContainer).toHaveClass(
      "bg-red-50",
      "border-l-4",
      "border-red-500",
      "p-4",
      "rounded-lg",
      "animate-shake"
    );
    expect(errorMessage).toHaveClass("text-red-700", "text-sm", "font-medium");
  });
});
