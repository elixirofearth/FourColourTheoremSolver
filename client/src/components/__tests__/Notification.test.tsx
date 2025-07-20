import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { screen, fireEvent, waitFor, act } from "@testing-library/react";
import { render } from "../../test/test-utils";
import Notification, { type NotificationProps } from "../Notification";

describe("Notification", () => {
  const defaultProps: NotificationProps = {
    message: "Test notification",
    type: "info",
    isVisible: true,
    onClose: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
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
    expect(notification).toHaveClass(
      "from-blue-500",
      "to-indigo-600",
      "border-blue-200"
    );
  });

  it("applies correct colors for success type", () => {
    render(<Notification {...defaultProps} type="success" />);

    const notification = screen.getByText("Test notification").closest("div")
      ?.parentElement?.parentElement;
    expect(notification).toHaveClass(
      "from-green-500",
      "to-emerald-600",
      "border-green-200"
    );
  });

  it("applies correct colors for error type", () => {
    render(<Notification {...defaultProps} type="error" />);

    const notification = screen.getByText("Test notification").closest("div")
      ?.parentElement?.parentElement;
    expect(notification).toHaveClass(
      "from-red-500",
      "to-rose-600",
      "border-red-200"
    );
  });

  it("applies correct colors for warning type", () => {
    render(<Notification {...defaultProps} type="warning" />);

    const notification = screen.getByText("Test notification").closest("div")
      ?.parentElement?.parentElement;
    expect(notification).toHaveClass(
      "from-yellow-500",
      "to-orange-600",
      "border-yellow-200"
    );
  });

  it("is positioned correctly", () => {
    render(<Notification {...defaultProps} />);

    const notification = screen.getByText("Test notification").closest("div")
      ?.parentElement?.parentElement?.parentElement;
    expect(notification).toHaveClass("fixed", "top-4", "right-4", "z-50");
  });

  it("has proper styling classes", () => {
    render(<Notification {...defaultProps} />);

    const notification = screen.getByText("Test notification").closest("div")
      ?.parentElement?.parentElement;
    expect(notification).toHaveClass(
      "bg-gradient-to-r",
      "text-white",
      "px-6",
      "py-4",
      "rounded-2xl",
      "shadow-2xl",
      "border-2",
      "transform",
      "transition-all",
      "duration-300",
      "ease-in-out",
      "backdrop-blur-sm",
      "max-w-sm",
      "min-w-[300px]"
    );
  });

  it("shows close button", () => {
    render(<Notification {...defaultProps} />);

    expect(screen.getByText("×")).toBeInTheDocument();
  });

  it("calls onClose when close button is clicked", () => {
    const onClose = vi.fn();
    render(<Notification {...defaultProps} onClose={onClose} />);

    const closeButton = screen.getByText("×");
    fireEvent.click(closeButton);

    // The close button should call onClose after a delay
    expect(onClose).toHaveBeenCalled();
  });

  it("auto-closes after default duration", async () => {
    const onClose = vi.fn();
    render(<Notification {...defaultProps} onClose={onClose} />);

    // Fast-forward time by default duration (4000ms) plus animation delay (300ms)
    act(() => {
      vi.advanceTimersByTime(4300);
    });

    await waitFor(
      () => {
        expect(onClose).toHaveBeenCalled();
      },
      { timeout: 10000 }
    );
  });

  it("auto-closes after custom duration", async () => {
    const onClose = vi.fn();
    render(
      <Notification {...defaultProps} onClose={onClose} duration={2000} />
    );

    // Fast-forward time by custom duration (2000ms) plus animation delay (300ms)
    act(() => {
      vi.advanceTimersByTime(2300);
    });

    await waitFor(
      () => {
        expect(onClose).toHaveBeenCalled();
      },
      { timeout: 10000 }
    );
  });

  it("does not render when not visible", () => {
    render(<Notification {...defaultProps} isVisible={false} />);

    expect(screen.queryByText("Test notification")).not.toBeInTheDocument();
  });

  it("handles animation states correctly", () => {
    render(<Notification {...defaultProps} />);

    const notification = screen.getByText("Test notification").closest("div")
      ?.parentElement?.parentElement;
    expect(notification).toHaveClass(
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

  it("has proper text styling", () => {
    render(<Notification {...defaultProps} />);

    const textElement = screen.getByText("Test notification");
    expect(textElement).toHaveClass(
      "font-semibold",
      "text-white",
      "leading-relaxed"
    );
  });

  it("has proper close button styling", () => {
    render(<Notification {...defaultProps} />);

    const closeButton = screen.getByText("×");
    expect(closeButton).toHaveClass(
      "text-white/80",
      "hover:text-white",
      "transition-colors",
      "duration-200",
      "text-xl",
      "leading-none",
      "ml-3",
      "flex-shrink-0"
    );
  });

  it("handles long messages properly", () => {
    const longMessage =
      "This is a very long notification message that should be handled properly by the component without breaking the layout or causing any issues with the display";
    render(<Notification {...defaultProps} message={longMessage} />);

    expect(screen.getByText(longMessage)).toBeInTheDocument();
  });

  it("handles special characters in message", () => {
    const specialMessage =
      "Test with special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?";
    render(<Notification {...defaultProps} message={specialMessage} />);

    expect(screen.getByText(specialMessage)).toBeInTheDocument();
  });

  it("handles empty message gracefully", () => {
    render(<Notification {...defaultProps} message="" />);

    // Should still render the notification container even with empty message
    expect(screen.getByText("ℹ️")).toBeInTheDocument();
  });
});
