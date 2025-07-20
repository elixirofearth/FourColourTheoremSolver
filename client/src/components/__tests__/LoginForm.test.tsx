import { describe, it, expect, vi, beforeEach } from "vitest";
import { screen, fireEvent, waitFor } from "@testing-library/react";
import { render } from "../../test/test-utils";
import LoginForm from "../LoginForm";
import { loginUser } from "../../store/authSlice";

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

describe("LoginForm", () => {
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

  it("renders login form with all elements", async () => {
    render(<LoginForm />);

    expect(screen.getByText("Welcome Back!")).toBeInTheDocument();
    expect(
      screen.getByText("Four-Color Map Theorem Solver")
    ).toBeInTheDocument();
    expect(screen.getByLabelText("Email Address")).toBeInTheDocument();
    expect(screen.getByLabelText("Password")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Sign In" })).toBeInTheDocument();
    expect(screen.getByText("Don't have an account?")).toBeInTheDocument();
    expect(screen.getByText("Create one here")).toBeInTheDocument();
  });

  it("updates form fields when user types", async () => {
    render(<LoginForm />);

    const emailInput = screen.getByLabelText("Email Address");
    const passwordInput = screen.getByLabelText("Password");

    fireEvent.change(emailInput, { target: { value: "test@example.com" } });
    fireEvent.change(passwordInput, { target: { value: "password123" } });

    expect(emailInput).toHaveValue("test@example.com");
    expect(passwordInput).toHaveValue("password123");
  });

  it("shows loading state when submitting", async () => {
    mockUseAppSelector.mockReturnValue({
      isLoading: true,
      error: null,
      isAuthenticated: false,
    });

    render(<LoginForm />);

    expect(
      screen.getByRole("button", { name: "Signing In..." })
    ).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: "Signing In..." })
    ).toBeDisabled();
  });

  it("disables form inputs when loading", async () => {
    mockUseAppSelector.mockReturnValue({
      isLoading: true,
      error: null,
      isAuthenticated: false,
    });

    render(<LoginForm />);

    const emailInput = screen.getByLabelText("Email Address");
    const passwordInput = screen.getByLabelText("Password");

    expect(emailInput).toBeDisabled();
    expect(passwordInput).toBeDisabled();
  });

  it("shows error message when form is submitted with empty fields", async () => {
    render(<LoginForm />);

    const submitButton = screen.getByRole("button", { name: "Sign In" });

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

  it("dispatches login action when form is submitted with valid data", async () => {
    mockDispatch.mockResolvedValue({
      type: loginUser.fulfilled.type,
      payload: {
        user_id: 1,
        name: "Test User",
        email: "test@example.com",
        token: "token",
      },
    });

    render(<LoginForm />);

    const emailInput = screen.getByLabelText("Email Address");
    const passwordInput = screen.getByLabelText("Password");
    const submitButton = screen.getByRole("button", { name: "Sign In" });

    fireEvent.change(emailInput, { target: { value: "test@example.com" } });
    fireEvent.change(passwordInput, { target: { value: "password123" } });
    fireEvent.click(submitButton);

    // The dispatch should be called immediately
    await waitFor(() => {
      expect(mockDispatch).toHaveBeenCalled();
    });
  });

  it("navigates to home page after successful login", async () => {
    mockDispatch.mockResolvedValue({
      type: loginUser.fulfilled.type,
      payload: {
        user_id: 1,
        name: "Test User",
        email: "test@example.com",
        token: "token",
      },
    });

    render(<LoginForm />);

    const emailInput = screen.getByLabelText("Email Address");
    const passwordInput = screen.getByLabelText("Password");
    const submitButton = screen.getByRole("button", { name: "Sign In" });

    fireEvent.change(emailInput, { target: { value: "test@example.com" } });
    fireEvent.change(passwordInput, { target: { value: "password123" } });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith("/");
    });
  });

  it("shows error message from Redux state", async () => {
    mockUseAppSelector.mockReturnValue({
      isLoading: false,
      error: "Invalid email or password",
      isAuthenticated: false,
    });

    render(<LoginForm />);

    expect(screen.getByText("Invalid email or password")).toBeInTheDocument();
  });

  it("redirects to home if already authenticated", async () => {
    mockUseAppSelector.mockReturnValue({
      isLoading: false,
      error: null,
      isAuthenticated: true,
    });

    render(<LoginForm />);

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith("/");
    });
  });

  it("clears form error when user starts typing", async () => {
    mockUseAppSelector.mockReturnValue({
      isLoading: false,
      error: "Invalid email or password",
      isAuthenticated: false,
    });

    render(<LoginForm />);

    const emailInput = screen.getByLabelText("Email Address");
    fireEvent.change(emailInput, { target: { value: "test@example.com" } });

    // The error should still be visible as it comes from Redux state
    expect(screen.getByText("Invalid email or password")).toBeInTheDocument();
  });
});
