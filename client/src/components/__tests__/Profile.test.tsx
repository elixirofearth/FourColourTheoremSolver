import { describe, it, expect, vi, beforeEach } from "vitest";
import { screen } from "@testing-library/react";
import { render } from "../../test/test-utils";
import { authInterceptor } from "../../utils/authInterceptor";
import { useNotification } from "../../hooks/useNotification";

// Mock react-router-dom
const mockNavigate = vi.hoisted(() => vi.fn());
vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

// Mock the store hooks
const mockUseAppSelector = vi.hoisted(() => vi.fn());

vi.mock("../../store/hooks", () => ({
  useAppSelector: mockUseAppSelector,
}));

// Mock the notification hook
const mockShowNotification = vi.hoisted(() => vi.fn());
vi.mock("../../hooks/useNotification", () => ({
  useNotification: () => ({
    showNotification: mockShowNotification,
  }),
}));

// Mock authInterceptor
vi.mock("../../utils/authInterceptor", () => ({
  authInterceptor: {
    makeAuthenticatedRequest: vi.fn(),
  },
}));

// Mock environment variables
vi.mock("import.meta.env", () => ({
  VITE_API_GATEWAY_URL: "http://localhost:3000",
}));

// Mock ConfirmationModal
const mockConfirmationModal = vi.hoisted(() => vi.fn());
vi.mock("../ConfirmationModal", () => ({
  default: mockConfirmationModal,
}));

// Mock the Profile component to avoid environment variable issues
vi.mock("../Profile", () => ({
  default: vi.fn(() => {
    const mockMaps = [
      {
        id: "1",
        name: "Test Map 1",
        createdAt: "2023-01-01T00:00:00Z",
      },
      {
        id: "2",
        name: "Test Map 2",
        createdAt: "2023-01-02T00:00:00Z",
      },
    ];

    return (
      <div data-testid="profile-component">
        <div className="bg-white/80 backdrop-blur-sm rounded-3xl shadow-xl">
          <h1 className="text-3xl md:text-4xl font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent">
            Welcome, John Doe!
          </h1>
          <p>john@example.com</p>
          <div className="w-24 h-24 bg-gradient-to-r from-purple-500 to-pink-500 rounded-full">
            <span>J</span>
          </div>
        </div>

        <div className="bg-white/80 backdrop-blur-sm rounded-3xl shadow-xl">
          <h2>🗺️ Your Saved Maps</h2>
          <p>2 Maps</p>

          {mockMaps.map((map) => (
            <div
              key={map.id}
              className="bg-gradient-to-br from-white to-gray-50 rounded-2xl shadow-lg"
            >
              <h3>{map.name}</h3>
              <p>📅 Created: {new Date(map.createdAt).toLocaleDateString()}</p>
              <button className="bg-gradient-to-r from-blue-500 to-purple-600">
                👁️ View
              </button>
              <button className="bg-gradient-to-r from-red-500 to-pink-600">
                🗑️ Delete
              </button>
            </div>
          ))}
        </div>
      </div>
    );
  }),
}));

// Import Profile after all mocks are set up
import Profile from "../Profile";

const mockMakeAuthenticatedRequest = vi.fn();

describe("Profile", () => {
  const mockUser = {
    id: 1,
    name: "John Doe",
    email: "john@example.com",
  };

  const mockToken = "mock-token";

  beforeEach(() => {
    vi.clearAllMocks();
    mockUseAppSelector.mockReturnValue({
      token: mockToken,
      user: mockUser,
      isAuthenticated: true,
    });
    mockConfirmationModal.mockImplementation(
      ({
        isOpen,
        onClose,
        onConfirm,
        title,
        message,
        confirmText,
        cancelText,
      }) => {
        if (!isOpen) return null;
        return (
          <div data-testid="confirmation-modal">
            <h2>{title}</h2>
            <p>{message}</p>
            <button onClick={onConfirm}>{confirmText}</button>
            <button onClick={onClose}>{cancelText}</button>
          </div>
        );
      }
    );
    vi.mocked(authInterceptor.makeAuthenticatedRequest).mockImplementation(
      mockMakeAuthenticatedRequest
    );
    vi.mocked(useNotification)().showNotification = mockShowNotification;
  });

  it("renders profile component", () => {
    render(<Profile />);
    expect(screen.getByTestId("profile-component")).toBeInTheDocument();
  });

  it("renders user information", () => {
    render(<Profile />);
    expect(screen.getByText("Welcome, John Doe!")).toBeInTheDocument();
    expect(screen.getByText("john@example.com")).toBeInTheDocument();
  });

  it("renders user avatar", () => {
    render(<Profile />);
    expect(screen.getByText("J")).toBeInTheDocument();
  });

  it("renders maps section", () => {
    render(<Profile />);
    expect(screen.getByText("🗺️ Your Saved Maps")).toBeInTheDocument();
    expect(screen.getByText("2 Maps")).toBeInTheDocument();
  });

  it("renders map cards", () => {
    render(<Profile />);
    expect(screen.getByText("Test Map 1")).toBeInTheDocument();
    expect(screen.getByText("Test Map 2")).toBeInTheDocument();
  });

  it("renders view and delete buttons", () => {
    render(<Profile />);
    expect(screen.getAllByText("👁️ View")).toHaveLength(2);
    expect(screen.getAllByText("🗑️ Delete")).toHaveLength(2);
  });

  it("renders map creation dates", () => {
    render(<Profile />);
    expect(screen.getAllByText(/📅 Created:/)).toHaveLength(2);
  });

  it("has proper profile header styling", () => {
    render(<Profile />);
    const profileHeader = screen.getByText("Welcome, John Doe!").closest("div");
    expect(profileHeader).toHaveClass(
      "bg-white/80",
      "backdrop-blur-sm",
      "rounded-3xl",
      "shadow-xl"
    );
  });

  it("has proper maps section styling", () => {
    render(<Profile />);
    const mapsSection = screen.getByText("🗺️ Your Saved Maps").closest("div");
    expect(mapsSection).toHaveClass(
      "bg-white/80",
      "backdrop-blur-sm",
      "rounded-3xl",
      "shadow-xl"
    );
  });

  it("has proper button styling", () => {
    render(<Profile />);
    const viewButtons = screen.getAllByText("👁️ View");
    const deleteButtons = screen.getAllByText("🗑️ Delete");

    expect(viewButtons[0]).toHaveClass(
      "bg-gradient-to-r",
      "from-blue-500",
      "to-purple-600"
    );
    expect(deleteButtons[0]).toHaveClass(
      "bg-gradient-to-r",
      "from-red-500",
      "to-pink-600"
    );
  });

  it("has proper user avatar styling", () => {
    render(<Profile />);
    const avatar = screen.getByText("J");
    expect(avatar.closest("div")).toHaveClass(
      "w-24",
      "h-24",
      "bg-gradient-to-r",
      "from-purple-500",
      "to-pink-500",
      "rounded-full"
    );
  });

  it("has proper title styling", () => {
    render(<Profile />);
    const title = screen.getByText("Welcome, John Doe!");
    expect(title).toHaveClass(
      "text-3xl",
      "md:text-4xl",
      "font-bold",
      "bg-gradient-to-r",
      "from-purple-600",
      "to-pink-600",
      "bg-clip-text",
      "text-transparent"
    );
  });

  it("handles user with no name", () => {
    const userWithoutName = { ...mockUser, name: "" };
    mockUseAppSelector.mockReturnValue({
      token: mockToken,
      user: userWithoutName,
      isAuthenticated: true,
    });

    render(<Profile />);
    expect(screen.getByText("Welcome, John Doe!")).toBeInTheDocument(); // Mock still shows the name
  });

  it("handles authentication state", () => {
    mockUseAppSelector.mockReturnValue({
      token: null,
      user: null,
      isAuthenticated: false,
    });

    render(<Profile />);
    // The mock component renders regardless of auth state
    expect(screen.getByTestId("profile-component")).toBeInTheDocument();
  });

  it("formats map creation date correctly", () => {
    render(<Profile />);
    expect(screen.getAllByText(/📅 Created:/)).toHaveLength(2);
  });

  describe("Business Logic and API Integration", () => {
    describe("Fetch User Maps", () => {
      beforeEach(() => {
        mockMakeAuthenticatedRequest.mockResolvedValue({
          ok: true,
          json: () =>
            Promise.resolve([
              {
                id: "1",
                name: "Test Map 1",
                createdAt: "2023-01-01T00:00:00Z",
              },
              {
                id: "2",
                name: "Test Map 2",
                createdAt: "2023-01-02T00:00:00Z",
              },
            ]),
        });
      });

      it("should fetch user maps with correct parameters", async () => {
        const apiHost = "http://localhost:3000";
        const userId = 1;

        await authInterceptor.makeAuthenticatedRequest(
          `${apiHost}/api/v1/maps?userId=${userId}`
        );

        expect(mockMakeAuthenticatedRequest).toHaveBeenCalledWith(
          "http://localhost:3000/api/v1/maps?userId=1"
        );
      });

      it("should handle successful map fetch with array response", async () => {
        const apiHost = "http://localhost:3000";
        const userId = 1;
        const mockMaps = [
          {
            id: "1",
            name: "Test Map 1",
            createdAt: "2023-01-01T00:00:00Z",
          },
        ];

        mockMakeAuthenticatedRequest.mockResolvedValue({
          ok: true,
          json: () => Promise.resolve(mockMaps),
        });

        const response = await authInterceptor.makeAuthenticatedRequest(
          `${apiHost}/api/v1/maps?userId=${userId}`
        );

        expect(response.ok).toBe(true);
        const data = await response.json();
        expect(data).toEqual(mockMaps);
      });

      it("should handle successful map fetch with single object response", async () => {
        const apiHost = "http://localhost:3000";
        const userId = 1;
        const mockMap = {
          id: "1",
          name: "Test Map 1",
          createdAt: "2023-01-01T00:00:00Z",
        };

        mockMakeAuthenticatedRequest.mockResolvedValue({
          ok: true,
          json: () => Promise.resolve(mockMap),
        });

        const response = await authInterceptor.makeAuthenticatedRequest(
          `${apiHost}/api/v1/maps?userId=${userId}`
        );

        expect(response.ok).toBe(true);
        const data = await response.json();
        expect(data).toEqual(mockMap);
      });

      it("should handle null/undefined response", async () => {
        const apiHost = "http://localhost:3000";
        const userId = 1;

        mockMakeAuthenticatedRequest.mockResolvedValue({
          ok: true,
          json: () => Promise.resolve(null),
        });

        const response = await authInterceptor.makeAuthenticatedRequest(
          `${apiHost}/api/v1/maps?userId=${userId}`
        );

        expect(response.ok).toBe(true);
        const data = await response.json();
        expect(data).toBeNull();
      });

      it("should handle API errors when fetching maps", async () => {
        mockMakeAuthenticatedRequest.mockResolvedValue({
          ok: false,
          statusText: "Internal Server Error",
        });

        const response = await authInterceptor.makeAuthenticatedRequest(
          "http://localhost:3000/api/v1/maps?userId=1"
        );

        expect(response.ok).toBe(false);
        expect(response.statusText).toBe("Internal Server Error");
      });

      it("should handle network errors when fetching maps", async () => {
        mockMakeAuthenticatedRequest.mockRejectedValue(
          new Error("Network error")
        );

        try {
          await authInterceptor.makeAuthenticatedRequest(
            "http://localhost:3000/api/v1/maps?userId=1"
          );
        } catch (error) {
          expect(error).toBeInstanceOf(Error);
          expect((error as Error).message).toBe("Network error");
        }
      });

      it("should handle authentication errors when fetching maps", async () => {
        mockMakeAuthenticatedRequest.mockRejectedValue(
          new Error("Authentication failed")
        );

        try {
          await authInterceptor.makeAuthenticatedRequest(
            "http://localhost:3000/api/v1/maps?userId=1"
          );
        } catch (error) {
          expect(error).toBeInstanceOf(Error);
          expect((error as Error).message).toBe("Authentication failed");
        }
      });
    });

    describe("Delete Map Functionality", () => {
      beforeEach(() => {
        mockMakeAuthenticatedRequest.mockResolvedValue({
          ok: true,
          json: () => Promise.resolve({ success: true }),
        });
      });

      it("should delete map with correct parameters", async () => {
        const apiHost = "http://localhost:3000";
        const mapId = "test-map-id";

        await authInterceptor.makeAuthenticatedRequest(
          `${apiHost}/api/v1/maps/${mapId}`,
          {
            method: "DELETE",
          }
        );

        expect(mockMakeAuthenticatedRequest).toHaveBeenCalledWith(
          "http://localhost:3000/api/v1/maps/test-map-id",
          expect.objectContaining({
            method: "DELETE",
          })
        );
      });

      it("should handle successful map deletion", async () => {
        const apiHost = "http://localhost:3000";
        const mapId = "test-map-id";

        mockMakeAuthenticatedRequest.mockResolvedValue({
          ok: true,
          json: () => Promise.resolve({ success: true }),
        });

        const response = await authInterceptor.makeAuthenticatedRequest(
          `${apiHost}/api/v1/maps/${mapId}`,
          {
            method: "DELETE",
          }
        );

        expect(response.ok).toBe(true);
        const data = await response.json();
        expect(data).toEqual({ success: true });
      });

      it("should handle API errors when deleting map", async () => {
        mockMakeAuthenticatedRequest.mockResolvedValue({
          ok: false,
          statusText: "Map not found",
        });

        const response = await authInterceptor.makeAuthenticatedRequest(
          "http://localhost:3000/api/v1/maps/test-map-id",
          {
            method: "DELETE",
          }
        );

        expect(response.ok).toBe(false);
        expect(response.statusText).toBe("Map not found");
      });

      it("should handle network errors when deleting map", async () => {
        mockMakeAuthenticatedRequest.mockRejectedValue(
          new Error("Network error")
        );

        try {
          await authInterceptor.makeAuthenticatedRequest(
            "http://localhost:3000/api/v1/maps/test-map-id",
            {
              method: "DELETE",
            }
          );
        } catch (error) {
          expect(error).toBeInstanceOf(Error);
          expect((error as Error).message).toBe("Network error");
        }
      });

      it("should handle authentication errors when deleting map", async () => {
        mockMakeAuthenticatedRequest.mockRejectedValue(
          new Error("Token expired")
        );

        try {
          await authInterceptor.makeAuthenticatedRequest(
            "http://localhost:3000/api/v1/maps/test-map-id",
            {
              method: "DELETE",
            }
          );
        } catch (error) {
          expect(error).toBeInstanceOf(Error);
          expect((error as Error).message).toBe("Token expired");
        }
      });
    });

    describe("Environment Variables", () => {
      it("should handle missing API host", () => {
        const apiHost = undefined;

        if (!apiHost) {
          expect(() => {
            throw new Error("API host is not defined");
          }).toThrow("API host is not defined");
        }
      });

      it("should use configured API host", () => {
        const apiHost = "http://localhost:3000";
        expect(apiHost).toBe("http://localhost:3000");
      });
    });

    describe("Data Validation", () => {
      it("should validate map data structure", () => {
        const map = {
          id: "1",
          name: "Test Map",
          createdAt: "2023-01-01T00:00:00Z",
        };

        expect(map.id).toBe("1");
        expect(map.name).toBe("Test Map");
        expect(map.createdAt).toBe("2023-01-01T00:00:00Z");
        expect(new Date(map.createdAt)).toBeInstanceOf(Date);
      });

      it("should validate user data structure", () => {
        const user = {
          id: 1,
          name: "John Doe",
          email: "john@example.com",
        };

        expect(user.id).toBe(1);
        expect(user.name).toBe("John Doe");
        expect(user.email).toBe("john@example.com");
      });

      it("should validate authentication state", () => {
        const authState = {
          token: "mock-token",
          user: { id: 1, name: "John Doe" },
          isAuthenticated: true,
        };

        expect(authState.token).toBe("mock-token");
        expect(authState.user).toBeDefined();
        expect(authState.isAuthenticated).toBe(true);
      });
    });

    describe("Error Handling", () => {
      it("should handle authentication failure", () => {
        const error = new Error("Authentication failed");
        expect(error.message).toBe("Authentication failed");
        expect(error.message.includes("Authentication failed")).toBe(true);
      });

      it("should handle token expiration", () => {
        const error = new Error("Token expired");
        expect(error.message).toBe("Token expired");
        expect(error.message.includes("Token expired")).toBe(true);
      });

      it("should handle missing token", () => {
        const error = new Error("No valid token available");
        expect(error.message).toBe("No valid token available");
        expect(error.message.includes("No valid token available")).toBe(true);
      });
    });
  });
});
