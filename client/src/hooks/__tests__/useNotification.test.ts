import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, act } from "@testing-library/react";
import React from "react";
import { useNotification } from "../useNotification";
import { NotificationContext } from "../../contexts/NotificationContextDef";

describe("useNotification", () => {
  const mockShowNotification = vi.fn();

  const createWrapper = ({ children }: { children: React.ReactNode }) => {
    return React.createElement(NotificationContext.Provider, {
      value: { showNotification: mockShowNotification },
      children,
    });
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("returns showNotification function when used within provider", () => {
    const { result } = renderHook(() => useNotification(), {
      wrapper: createWrapper,
    });

    expect(result.current.showNotification).toBe(mockShowNotification);
  });

  it("throws error when used outside provider", () => {
    expect(() => {
      renderHook(() => useNotification());
    }).toThrow("useNotification must be used within a NotificationProvider");
  });

  it("calls showNotification with correct parameters", () => {
    const { result } = renderHook(() => useNotification(), {
      wrapper: createWrapper,
    });

    act(() => {
      result.current.showNotification("Test message", "success");
    });

    expect(mockShowNotification).toHaveBeenCalledWith(
      "Test message",
      "success"
    );
  });

  it("calls showNotification with info type", () => {
    const { result } = renderHook(() => useNotification(), {
      wrapper: createWrapper,
    });

    act(() => {
      result.current.showNotification("Info message", "info");
    });

    expect(mockShowNotification).toHaveBeenCalledWith("Info message", "info");
  });

  it("calls showNotification with error type", () => {
    const { result } = renderHook(() => useNotification(), {
      wrapper: createWrapper,
    });

    act(() => {
      result.current.showNotification("Error message", "error");
    });

    expect(mockShowNotification).toHaveBeenCalledWith("Error message", "error");
  });

  it("calls showNotification with warning type", () => {
    const { result } = renderHook(() => useNotification(), {
      wrapper: createWrapper,
    });

    act(() => {
      result.current.showNotification("Warning message", "warning");
    });

    expect(mockShowNotification).toHaveBeenCalledWith(
      "Warning message",
      "warning"
    );
  });

  it("calls showNotification with custom duration", () => {
    const { result } = renderHook(() => useNotification(), {
      wrapper: createWrapper,
    });

    act(() => {
      result.current.showNotification("Test message", "success", 5000);
    });

    expect(mockShowNotification).toHaveBeenCalledWith(
      "Test message",
      "success",
      5000
    );
  });

  it("can be called multiple times", () => {
    const { result } = renderHook(() => useNotification(), {
      wrapper: createWrapper,
    });

    act(() => {
      result.current.showNotification("First message", "info");
      result.current.showNotification("Second message", "success");
      result.current.showNotification("Third message", "error");
    });

    expect(mockShowNotification).toHaveBeenCalledTimes(3);
    expect(mockShowNotification).toHaveBeenNthCalledWith(
      1,
      "First message",
      "info"
    );
    expect(mockShowNotification).toHaveBeenNthCalledWith(
      2,
      "Second message",
      "success"
    );
    expect(mockShowNotification).toHaveBeenNthCalledWith(
      3,
      "Third message",
      "error"
    );
  });

  it("handles empty message", () => {
    const { result } = renderHook(() => useNotification(), {
      wrapper: createWrapper,
    });

    act(() => {
      result.current.showNotification("", "info");
    });

    expect(mockShowNotification).toHaveBeenCalledWith("", "info");
  });

  it("handles special characters in message", () => {
    const { result } = renderHook(() => useNotification(), {
      wrapper: createWrapper,
    });

    const specialMessage =
      "Message with special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?";

    act(() => {
      result.current.showNotification(specialMessage, "warning");
    });

    expect(mockShowNotification).toHaveBeenCalledWith(
      specialMessage,
      "warning"
    );
  });

  it("handles long messages", () => {
    const { result } = renderHook(() => useNotification(), {
      wrapper: createWrapper,
    });

    const longMessage =
      "This is a very long notification message that should be handled properly by the hook without causing any issues or errors in the application";

    act(() => {
      result.current.showNotification(longMessage, "success");
    });

    expect(mockShowNotification).toHaveBeenCalledWith(longMessage, "success");
  });

  it("maintains function reference between renders", () => {
    const { result, rerender } = renderHook(() => useNotification(), {
      wrapper: createWrapper,
    });

    const firstReference = result.current.showNotification;

    rerender();

    expect(result.current.showNotification).toBe(firstReference);
  });

  it("works with different context values", () => {
    const differentMockShowNotification = vi.fn();

    const DifferentWrapper = ({ children }: { children: React.ReactNode }) => {
      return React.createElement(NotificationContext.Provider, {
        value: { showNotification: differentMockShowNotification },
        children,
      });
    };

    const { result } = renderHook(() => useNotification(), {
      wrapper: DifferentWrapper,
    });

    act(() => {
      result.current.showNotification("Test message", "info");
    });

    expect(differentMockShowNotification).toHaveBeenCalledWith(
      "Test message",
      "info"
    );
    expect(mockShowNotification).not.toHaveBeenCalled();
  });
});
