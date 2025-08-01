import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import Notification from "../Notification";

describe("Notification", () => {
  const defaultProps = {
    message: "Test notification",
    type: "info" as const,
    isVisible: true,
    onClose: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders notification with correct message", () => {
    render(<Notification {...defaultProps} />);
    expect(screen.getByText("Test notification")).toBeInTheDocument();
  });

  it("shows correct icon for info type", () => {
    render(<Notification {...defaultProps} type="info" />);
    expect(screen.getByText("ℹ️")).toBeInTheDocument();
  });

  it("shows correct icon for success type", () => {
    render(<Notification {...defaultProps} type="success" />);
    expect(screen.getByText("✅")).toBeInTheDocument();
  });

  it("shows correct icon for error type", () => {
    render(<Notification {...defaultProps} type="error" />);
    expect(screen.getByText("❌")).toBeInTheDocument();
  });

  it("shows correct icon for warning type", () => {
    render(<Notification {...defaultProps} type="warning" />);
    expect(screen.getByText("⚠️")).toBeInTheDocument();
  });

  it("applies correct colors for info type", () => {
    render(<Notification {...defaultProps} type="info" />);
    const notification = screen.getByText("Test notification").closest("div")
      ?.parentElement?.parentElement;
    expect(notification).toHaveClass("from-blue-500", "to-indigo-600");
  });

  it("applies correct colors for success type", () => {
    render(<Notification {...defaultProps} type="success" />);
    const notification = screen.getByText("Test notification").closest("div")
      ?.parentElement?.parentElement;
    expect(notification).toHaveClass("from-green-500", "to-emerald-600");
  });

  it("applies correct colors for error type", () => {
    render(<Notification {...defaultProps} type="error" />);
    const notification = screen.getByText("Test notification").closest("div")
      ?.parentElement?.parentElement;
    expect(notification).toHaveClass("from-red-500", "to-rose-600");
  });

  it("applies correct colors for warning type", () => {
    render(<Notification {...defaultProps} type="warning" />);
    const notification = screen.getByText("Test notification").closest("div")
      ?.parentElement?.parentElement;
    expect(notification).toHaveClass("from-yellow-500", "to-orange-600");
  });

  it("is positioned correctly with responsive classes", () => {
    render(<Notification {...defaultProps} />);
    const notification = screen.getByText("Test notification").closest("div")
      ?.parentElement?.parentElement?.parentElement;
    expect(notification).toHaveClass(
      "fixed",
      "top-3",
      "sm:top-4",
      "right-3",
      "sm:right-4",
      "left-3",
      "sm:left-auto",
      "z-50"
    );
  });

  it("has proper styling classes with responsive design", () => {
    render(<Notification {...defaultProps} />);
    const notification = screen.getByText("Test notification").closest("div")
      ?.parentElement?.parentElement;
    expect(notification).toHaveClass(
      "bg-gradient-to-r",
      "text-white",
      "px-4",
      "sm:px-6",
      "py-3",
      "sm:py-4",
      "rounded-xl",
      "sm:rounded-2xl",
      "shadow-2xl",
      "border-2",
      "transform",
      "transition-all",
      "duration-300",
      "ease-in-out",
      "backdrop-blur-sm",
      "w-full",
      "sm:max-w-sm",
      "sm:min-w-[300px]"
    );
  });

  it("shows close button", () => {
    render(<Notification {...defaultProps} />);
    expect(screen.getByText("×")).toBeInTheDocument();
  });

  it("calls onClose when close button is clicked", async () => {
    const mockOnClose = vi.fn();
    render(<Notification {...defaultProps} onClose={mockOnClose} />);

    const closeButton = screen.getByText("×");
    fireEvent.click(closeButton);

    // Wait for the 300ms delay
    await waitFor(
      () => {
        expect(mockOnClose).toHaveBeenCalledTimes(1);
      },
      { timeout: 500 }
    );
  });

  it("auto-closes after default duration", async () => {
    const mockOnClose = vi.fn();
    render(<Notification {...defaultProps} onClose={mockOnClose} />);

    await waitFor(
      () => {
        expect(mockOnClose).toHaveBeenCalledTimes(1);
      },
      { timeout: 5000 }
    );
  });

  it("auto-closes after custom duration", async () => {
    const mockOnClose = vi.fn();
    render(
      <Notification {...defaultProps} onClose={mockOnClose} duration={1000} />
    );

    await waitFor(
      () => {
        expect(mockOnClose).toHaveBeenCalledTimes(1);
      },
      { timeout: 2000 }
    );
  });

  it("does not render when not visible", () => {
    render(<Notification {...defaultProps} isVisible={false} />);
    expect(screen.queryByText("Test notification")).not.toBeInTheDocument();
  });

  it("handles animation states correctly", () => {
    const { rerender } = render(<Notification {...defaultProps} />);

    const notification = screen.getByText("Test notification").closest("div")
      ?.parentElement?.parentElement;
    expect(notification).toHaveClass(
      "translate-x-0",
      "opacity-100",
      "scale-100"
    );

    // When isVisible is false, the component remains visible during animation
    // The component manages its own internal animation state
    rerender(<Notification {...defaultProps} isVisible={false} />);

    // The component should still be in the document because it's animating
    expect(screen.getByText("Test notification")).toBeInTheDocument();

    // The notification should still have the animation classes
    const updatedNotification = screen
      .getByText("Test notification")
      .closest("div")?.parentElement?.parentElement;
    expect(updatedNotification).toHaveClass(
      "translate-x-0",
      "opacity-100",
      "scale-100"
    );
  });

  it("has proper flex layout", () => {
    render(<Notification {...defaultProps} />);
    const flexContainer = screen
      .getByText("Test notification")
      .closest("div")?.parentElement;
    expect(flexContainer).toHaveClass(
      "flex",
      "items-center",
      "justify-between"
    );
  });

  it("has proper text styling with responsive classes", () => {
    render(<Notification {...defaultProps} />);
    const messageText = screen.getByText("Test notification");
    expect(messageText).toHaveClass(
      "font-semibold",
      "text-white",
      "leading-relaxed",
      "text-sm",
      "sm:text-base",
      "truncate"
    );
  });

  it("has proper close button styling with responsive classes", () => {
    render(<Notification {...defaultProps} />);
    const closeButton = screen.getByText("×");
    expect(closeButton).toHaveClass(
      "text-white/80",
      "hover:text-white",
      "transition-colors",
      "duration-200",
      "text-lg",
      "sm:text-xl",
      "leading-none",
      "ml-2",
      "sm:ml-3",
      "flex-shrink-0",
      "p-1"
    );
  });

  it("handles long messages properly", () => {
    const longMessage =
      "This is a very long notification message that should be handled properly by the component";
    render(<Notification {...defaultProps} message={longMessage} />);

    const messageText = screen.getByText(longMessage);
    expect(messageText).toHaveClass("truncate");
    expect(messageText).toBeInTheDocument();
  });

  it("handles special characters in message", () => {
    const specialMessage = "Special chars: @#$%^&*()_+{}|:<>?[]\\;'\",./ 🎉";
    render(<Notification {...defaultProps} message={specialMessage} />);
    expect(screen.getByText(specialMessage)).toBeInTheDocument();
  });

  it("handles empty message gracefully", () => {
    render(<Notification {...defaultProps} message="" />);
    expect(screen.getByText("×")).toBeInTheDocument(); // Close button should still be there
  });
});
