import { describe, it, expect, vi, beforeEach } from "vitest";
import { screen, fireEvent } from "@testing-library/react";
import { render } from "../../test/test-utils";
import ConfirmationModal from "../ConfirmationModal";

describe("ConfirmationModal", () => {
  const defaultProps = {
    isOpen: true,
    onClose: vi.fn(),
    onConfirm: vi.fn(),
    title: "Test Modal",
    message: "This is a test message",
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("Modal Visibility", () => {
    it("renders when isOpen is true", () => {
      render(<ConfirmationModal {...defaultProps} />);

      expect(screen.getByText("Test Modal")).toBeInTheDocument();
      expect(screen.getByText("This is a test message")).toBeInTheDocument();
    });

    it("does not render when isOpen is false", () => {
      render(<ConfirmationModal {...defaultProps} isOpen={false} />);

      expect(screen.queryByText("Test Modal")).not.toBeInTheDocument();
      expect(
        screen.queryByText("This is a test message")
      ).not.toBeInTheDocument();
    });
  });

  describe("Modal Content", () => {
    it("displays the correct title", () => {
      render(<ConfirmationModal {...defaultProps} title="Custom Title" />);

      expect(screen.getByText("Custom Title")).toBeInTheDocument();
    });

    it("displays the correct message", () => {
      render(
        <ConfirmationModal {...defaultProps} message="Custom message content" />
      );

      expect(screen.getByText("Custom message content")).toBeInTheDocument();
    });

    it("displays default button text", () => {
      render(<ConfirmationModal {...defaultProps} />);

      expect(screen.getByText("Cancel")).toBeInTheDocument();
      expect(screen.getByText("Confirm")).toBeInTheDocument();
    });

    it("displays custom button text", () => {
      render(
        <ConfirmationModal
          {...defaultProps}
          confirmText="Delete"
          cancelText="Keep"
        />
      );

      expect(screen.getByText("Keep")).toBeInTheDocument();
      expect(screen.getByText("Delete")).toBeInTheDocument();
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
      render(
        <ConfirmationModal
          {...defaultProps}
          type={"unknown" as "danger" | "warning" | "info"}
        />
      );

      expect(screen.getByText("❓")).toBeInTheDocument();
    });

    it("defaults to danger type when no type is specified", () => {
      render(<ConfirmationModal {...defaultProps} />);

      expect(screen.getByText("🗑️")).toBeInTheDocument();
    });
  });

  describe("Button Interactions", () => {
    it("calls onClose when cancel button is clicked", () => {
      const onClose = vi.fn();
      render(<ConfirmationModal {...defaultProps} onClose={onClose} />);

      const cancelButton = screen.getByText("Cancel");
      fireEvent.click(cancelButton);

      expect(onClose).toHaveBeenCalledTimes(1);
    });

    it("calls onConfirm and onClose when confirm button is clicked", () => {
      const onConfirm = vi.fn();
      const onClose = vi.fn();
      render(
        <ConfirmationModal
          {...defaultProps}
          onConfirm={onConfirm}
          onClose={onClose}
        />
      );

      const confirmButton = screen.getByText("Confirm");
      fireEvent.click(confirmButton);

      expect(onConfirm).toHaveBeenCalledTimes(1);
      expect(onClose).toHaveBeenCalledTimes(1);
    });

    it("calls onClose when backdrop is clicked", () => {
      const onClose = vi.fn();
      render(<ConfirmationModal {...defaultProps} onClose={onClose} />);

      const backdrop = document.querySelector(
        ".fixed.inset-0.bg-black\\/50.backdrop-blur-sm"
      );
      fireEvent.click(backdrop!);

      expect(onClose).toHaveBeenCalledTimes(1);
    });
  });

  describe("Button Styling by Type", () => {
    it("applies danger styling to confirm button for danger type", () => {
      render(<ConfirmationModal {...defaultProps} type="danger" />);

      const confirmButton = screen.getByText("Confirm");
      expect(confirmButton).toHaveClass(
        "from-red-500",
        "to-rose-600",
        "hover:from-red-600",
        "hover:to-rose-700",
        "focus:ring-red-200"
      );
    });

    it("applies warning styling to confirm button for warning type", () => {
      render(<ConfirmationModal {...defaultProps} type="warning" />);

      const confirmButton = screen.getByText("Confirm");
      expect(confirmButton).toHaveClass(
        "from-yellow-500",
        "to-orange-600",
        "hover:from-yellow-600",
        "hover:to-orange-700",
        "focus:ring-yellow-200"
      );
    });

    it("applies info styling to confirm button for info type", () => {
      render(<ConfirmationModal {...defaultProps} type="info" />);

      const confirmButton = screen.getByText("Confirm");
      expect(confirmButton).toHaveClass(
        "from-blue-500",
        "to-indigo-600",
        "hover:from-blue-600",
        "hover:to-indigo-700",
        "focus:ring-blue-200"
      );
    });
  });

  describe("Modal Structure", () => {
    it("renders modal with proper backdrop", () => {
      render(<ConfirmationModal {...defaultProps} />);

      // Check that backdrop exists by looking for its classes
      const backdrop = document.querySelector(
        ".fixed.inset-0.bg-black\\/50.backdrop-blur-sm"
      );
      expect(backdrop).toBeInTheDocument();
    });

    it("renders modal with proper container", () => {
      render(<ConfirmationModal {...defaultProps} />);

      // Check that modal container exists by looking for its classes
      const container = document.querySelector(
        ".flex.min-h-full.items-center.justify-center.p-4"
      );
      expect(container).toBeInTheDocument();
    });

    it("renders modal with proper content structure", () => {
      render(<ConfirmationModal {...defaultProps} />);

      // Check that modal content exists by looking for its classes
      const content = document.querySelector(
        ".relative.bg-white.rounded-3xl.shadow-2xl.max-w-md.w-full.mx-auto"
      );
      expect(content).toBeInTheDocument();
    });
  });

  describe("Accessibility", () => {
    it("has proper button roles", () => {
      render(<ConfirmationModal {...defaultProps} />);

      const cancelButton = screen.getByRole("button", { name: "Cancel" });
      const confirmButton = screen.getByRole("button", { name: "Confirm" });

      expect(cancelButton).toBeInTheDocument();
      expect(confirmButton).toBeInTheDocument();
    });

    it("has proper focus management", () => {
      render(<ConfirmationModal {...defaultProps} />);

      const cancelButton = screen.getByRole("button", { name: "Cancel" });
      const confirmButton = screen.getByRole("button", { name: "Confirm" });

      expect(cancelButton).toHaveClass("focus:outline-none", "focus:ring-4");
      expect(confirmButton).toHaveClass("focus:outline-none", "focus:ring-4");
    });

    it("has proper backdrop element", () => {
      render(<ConfirmationModal {...defaultProps} />);

      const backdrop = document.querySelector(
        ".fixed.inset-0.bg-black\\/50.backdrop-blur-sm"
      );
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
        ".relative.bg-white.rounded-3xl.shadow-2xl.max-w-md.w-full.mx-auto"
      );
      expect(modalContent).toHaveClass(
        "transform",
        "transition-all",
        "duration-300"
      );
    });

    it("has hover effects on confirm button", () => {
      render(<ConfirmationModal {...defaultProps} />);

      const confirmButton = screen.getByText("Confirm");
      expect(confirmButton).toHaveClass(
        "transform",
        "hover:-translate-y-0.5",
        "hover:shadow-lg",
        "transition-all",
        "duration-300"
      );
    });
  });

  describe("Edge Cases", () => {
    it("handles empty title", () => {
      render(<ConfirmationModal {...defaultProps} title="" />);

      const titleElement = screen.getByRole("heading", { level: 3 });
      expect(titleElement).toHaveTextContent("");
    });

    it("handles empty message", () => {
      render(<ConfirmationModal {...defaultProps} message="" />);

      const messageElement = document.querySelector(
        "p.text-gray-600.leading-relaxed.mb-6"
      );
      expect(messageElement).toBeInTheDocument();
    });

    it("handles very long title and message", () => {
      const longTitle =
        "This is a very long title that might wrap to multiple lines and should still display correctly";
      const longMessage =
        "This is a very long message that contains a lot of text and should still be readable and properly formatted within the modal";

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
      const specialTitle = "Delete 🗑️ & Confirm ⚠️";
      const specialMessage =
        "Are you sure you want to delete this item? (This action cannot be undone)";

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
      const onConfirm = vi.fn();
      const onClose = vi.fn();
      render(
        <ConfirmationModal
          {...defaultProps}
          onConfirm={onConfirm}
          onClose={onClose}
        />
      );

      const confirmButton = screen.getByText("Confirm");
      fireEvent.click(confirmButton);
      fireEvent.click(confirmButton);
      fireEvent.click(confirmButton);

      expect(onConfirm).toHaveBeenCalledTimes(3);
      expect(onClose).toHaveBeenCalledTimes(3);
    });

    it("handles multiple cancel clicks", () => {
      const onClose = vi.fn();
      render(<ConfirmationModal {...defaultProps} onClose={onClose} />);

      const cancelButton = screen.getByText("Cancel");
      fireEvent.click(cancelButton);
      fireEvent.click(cancelButton);
      fireEvent.click(cancelButton);

      expect(onClose).toHaveBeenCalledTimes(3);
    });
  });
});
