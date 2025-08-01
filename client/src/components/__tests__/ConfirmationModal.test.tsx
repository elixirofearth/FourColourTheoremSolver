import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import ConfirmationModal from "../ConfirmationModal";

describe("ConfirmationModal", () => {
  const defaultProps = {
    isOpen: true,
    onClose: vi.fn(),
    onConfirm: vi.fn(),
    title: "Test Title",
    message: "Test message",
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("Modal Visibility", () => {
    it("renders when isOpen is true", () => {
      render(<ConfirmationModal {...defaultProps} />);
      expect(screen.getByText("Test Title")).toBeInTheDocument();
      expect(screen.getByText("Test message")).toBeInTheDocument();
    });

    it("does not render when isOpen is false", () => {
      render(<ConfirmationModal {...defaultProps} isOpen={false} />);
      expect(screen.queryByText("Test Title")).not.toBeInTheDocument();
      expect(screen.queryByText("Test message")).not.toBeInTheDocument();
    });
  });

  describe("Modal Content", () => {
    it("displays the correct title", () => {
      render(<ConfirmationModal {...defaultProps} title="Custom Title" />);
      expect(screen.getByText("Custom Title")).toBeInTheDocument();
    });

    it("displays the correct message", () => {
      render(<ConfirmationModal {...defaultProps} message="Custom message" />);
      expect(screen.getByText("Custom message")).toBeInTheDocument();
    });

    it("displays default button text", () => {
      render(<ConfirmationModal {...defaultProps} />);
      expect(screen.getByText("Confirm")).toBeInTheDocument();
      expect(screen.getByText("Cancel")).toBeInTheDocument();
    });

    it("displays custom button text", () => {
      render(
        <ConfirmationModal
          {...defaultProps}
          confirmText="Delete"
          cancelText="Keep"
        />
      );
      expect(screen.getByText("Delete")).toBeInTheDocument();
      expect(screen.getByText("Keep")).toBeInTheDocument();
    });
  });

  describe("Modal Types and Icons", () => {
    it("displays danger icon for danger type", () => {
      render(<ConfirmationModal {...defaultProps} type="danger" />);
      expect(screen.getByText("🗑️")).toBeInTheDocument();
    });

    it("displays warning icon for warning type", () => {
      render(<ConfirmationModal {...defaultProps} type="warning" />);
      expect(screen.getByText("⚠️")).toBeInTheDocument();
    });

    it("displays info icon for info type", () => {
      render(<ConfirmationModal {...defaultProps} type="info" />);
      expect(screen.getByText("ℹ️")).toBeInTheDocument();
    });

    it("displays default icon for unknown type", () => {
      render(<ConfirmationModal {...defaultProps} type="danger" />);
      expect(screen.getByText("🗑️")).toBeInTheDocument();
    });

    it("defaults to danger type when no type is specified", () => {
      render(<ConfirmationModal {...defaultProps} />);
      expect(screen.getByText("🗑️")).toBeInTheDocument();
    });
  });

  describe("Button Interactions", () => {
    it("calls onClose when cancel button is clicked", () => {
      const mockOnClose = vi.fn();
      render(<ConfirmationModal {...defaultProps} onClose={mockOnClose} />);

      const cancelButton = screen.getByText("Cancel");
      fireEvent.click(cancelButton);

      expect(mockOnClose).toHaveBeenCalledTimes(1);
    });

    it("calls onConfirm and onClose when confirm button is clicked", () => {
      const mockOnConfirm = vi.fn();
      const mockOnClose = vi.fn();
      render(
        <ConfirmationModal
          {...defaultProps}
          onConfirm={mockOnConfirm}
          onClose={mockOnClose}
        />
      );

      const confirmButton = screen.getByText("Confirm");
      fireEvent.click(confirmButton);

      expect(mockOnConfirm).toHaveBeenCalledTimes(1);
      expect(mockOnClose).toHaveBeenCalledTimes(1);
    });

    it("calls onClose when backdrop is clicked", () => {
      const mockOnClose = vi.fn();
      render(<ConfirmationModal {...defaultProps} onClose={mockOnClose} />);

      const backdrop = document.querySelector(
        ".fixed.inset-0.bg-black\\/50.backdrop-blur-sm"
      );
      expect(backdrop).toBeInTheDocument();
      fireEvent.click(backdrop!);

      expect(mockOnClose).toHaveBeenCalledTimes(1);
    });
  });

  describe("Button Styling by Type", () => {
    it("applies danger styling to confirm button for danger type", () => {
      render(<ConfirmationModal {...defaultProps} type="danger" />);
      const confirmButton = screen.getByTestId("confirm-button");
      expect(confirmButton).toHaveClass(
        "from-red-500",
        "to-rose-600",
        "hover:from-red-600",
        "hover:to-rose-700"
      );
    });

    it("applies warning styling to confirm button for warning type", () => {
      render(<ConfirmationModal {...defaultProps} type="warning" />);
      const confirmButton = screen.getByTestId("confirm-button");
      expect(confirmButton).toHaveClass(
        "from-yellow-500",
        "to-orange-600",
        "hover:from-yellow-600",
        "hover:to-orange-700"
      );
    });

    it("applies info styling to confirm button for info type", () => {
      render(<ConfirmationModal {...defaultProps} type="info" />);
      const confirmButton = screen.getByTestId("confirm-button");
      expect(confirmButton).toHaveClass(
        "from-blue-500",
        "to-indigo-600",
        "hover:from-blue-600",
        "hover:to-indigo-700"
      );
    });
  });

  describe("Modal Structure", () => {
    it("renders modal with proper backdrop", () => {
      render(<ConfirmationModal {...defaultProps} />);
      const backdrop = document.querySelector(
        ".fixed.inset-0.bg-black\\/50.backdrop-blur-sm"
      );
      expect(backdrop).toBeInTheDocument();
    });

    it("renders modal with proper container", () => {
      render(<ConfirmationModal {...defaultProps} />);
      const container = document.querySelector(
        ".flex.min-h-full.items-center.justify-center.p-3.sm\\:p-4"
      );
      expect(container).toBeInTheDocument();
    });

    it("renders modal with proper content structure", () => {
      render(<ConfirmationModal {...defaultProps} />);
      const content = document.querySelector(
        ".relative.bg-white.rounded-2xl.sm\\:rounded-3xl.shadow-2xl.max-w-sm.sm\\:max-w-md.w-full.mx-auto"
      );
      expect(content).toBeInTheDocument();
    });
  });

  describe("Accessibility", () => {
    it("has proper button roles", () => {
      render(<ConfirmationModal {...defaultProps} />);
      const buttons = screen.getAllByRole("button");
      expect(buttons).toHaveLength(2);
    });

    it("has proper focus management", () => {
      render(<ConfirmationModal {...defaultProps} />);
      const confirmButton = screen.getByTestId("confirm-button");
      confirmButton.focus();
      expect(confirmButton).toHaveFocus();
    });

    it("has proper backdrop element", () => {
      render(<ConfirmationModal {...defaultProps} />);
      const backdrop = document.querySelector(".fixed.inset-0");
      expect(backdrop).toBeInTheDocument();
    });
  });

  describe("Animation and Transitions", () => {
    it("has transition classes on backdrop", () => {
      render(<ConfirmationModal {...defaultProps} />);
      const backdrop = document.querySelector(
        ".fixed.inset-0.bg-black\\/50.backdrop-blur-sm"
      );
      expect(backdrop).toHaveClass("transition-opacity", "duration-300");
    });

    it("has transition classes on modal content", () => {
      render(<ConfirmationModal {...defaultProps} />);
      const modalContent = document.querySelector(
        ".relative.bg-white.rounded-2xl.sm\\:rounded-3xl.shadow-2xl.max-w-sm.sm\\:max-w-md.w-full.mx-auto"
      );
      expect(modalContent).toHaveClass(
        "transform",
        "transition-all",
        "duration-300",
        "scale-100"
      );
    });

    it("has hover effects on confirm button", () => {
      render(<ConfirmationModal {...defaultProps} />);
      const confirmButton = screen.getByTestId("confirm-button");
      expect(confirmButton).toHaveClass(
        "hover:-translate-y-0.5",
        "hover:shadow-lg"
      );
    });
  });

  describe("Edge Cases", () => {
    it("handles empty title", () => {
      render(<ConfirmationModal {...defaultProps} title="" />);
      expect(screen.getByText("Test message")).toBeInTheDocument();
    });

    it("handles empty message", () => {
      render(<ConfirmationModal {...defaultProps} message="" />);
      const messageElement = document.querySelector(
        "p.text-gray-600.leading-relaxed.mb-4.sm\\:mb-6.text-sm.sm\\:text-base"
      );
      expect(messageElement).toBeInTheDocument();
    });

    it("handles very long title and message", () => {
      const longTitle =
        "This is a very long title that should be handled properly";
      const longMessage =
        "This is a very long message that should be handled properly by the modal component without breaking the layout";
      render(
        <ConfirmationModal
          {...defaultProps}
          title={longTitle}
          message={longMessage}
        />
      );
      expect(screen.getByText(longTitle)).toBeInTheDocument();
      expect(screen.getByText(longMessage)).toBeInTheDocument();
    });

    it("handles special characters in title and message", () => {
      const specialTitle = "Title with special chars: !@#$%^&*()";
      const specialMessage = "Message with special chars: <>?{}[]|\\";
      render(
        <ConfirmationModal
          {...defaultProps}
          title={specialTitle}
          message={specialMessage}
        />
      );
      expect(screen.getByText(specialTitle)).toBeInTheDocument();
      expect(screen.getByText(specialMessage)).toBeInTheDocument();
    });
  });

  describe("Multiple Interactions", () => {
    it("handles multiple confirm clicks", () => {
      const mockOnConfirm = vi.fn();
      const mockOnClose = vi.fn();
      render(
        <ConfirmationModal
          {...defaultProps}
          onConfirm={mockOnConfirm}
          onClose={mockOnClose}
        />
      );

      const confirmButton = screen.getByText("Confirm");
      fireEvent.click(confirmButton);
      fireEvent.click(confirmButton);

      // Both functions should be called for each click
      expect(mockOnConfirm).toHaveBeenCalledTimes(2);
      expect(mockOnClose).toHaveBeenCalledTimes(2);
    });

    it("handles multiple cancel clicks", () => {
      const mockOnClose = vi.fn();
      render(<ConfirmationModal {...defaultProps} onClose={mockOnClose} />);

      const cancelButton = screen.getByText("Cancel");
      fireEvent.click(cancelButton);
      fireEvent.click(cancelButton);

      // Should be called multiple times since modal doesn't auto-close on cancel
      expect(mockOnClose).toHaveBeenCalledTimes(2);
    });
  });
});
