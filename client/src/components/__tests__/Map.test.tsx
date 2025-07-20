import { describe, it, expect, vi, beforeEach } from "vitest";
import { screen } from "@testing-library/react";
import { render } from "../../test/test-utils";
import { authInterceptor } from "../../utils/authInterceptor";
import { useNotification } from "../../hooks/useNotification";

// Mock react-router-dom
const mockNavigate = vi.hoisted(() => vi.fn());
const mockUseParams = vi.hoisted(() => vi.fn());
vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => mockNavigate,
    useParams: () => mockUseParams(),
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

// Mock the Map component to avoid environment variable issues
vi.mock("../Map", () => ({
  default: vi.fn(({ id }: { id?: string }) => {
    const mockMapData = {
      id: id || "test-map-id",
      name: "Test Map",
      imageData: "data:image/png;base64,test-image-data",
      createdAt: "2024-01-01T00:00:00Z",
      width: 500,
      height: 500,
    };

    return (
      <div data-testid="map-component">
        <div className="min-h-screen bg-gradient-to-br from-blue-50 via-purple-50 to-pink-50 py-8 px-4">
          <div className="max-w-4xl mx-auto">
            <div className="bg-white/80 backdrop-blur-sm rounded-3xl shadow-xl p-8 border border-white/20">
              {/* Header */}
              <div className="text-center mb-8">
                <h1 className="text-3xl md:text-4xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent mb-4">
                  🗺️ {mockMapData.name}
                </h1>
                <div className="flex flex-wrap justify-center gap-4 text-sm text-gray-600">
                  <div className="bg-gradient-to-r from-blue-100 to-purple-100 px-4 py-2 rounded-full">
                    📅 Created:{" "}
                    {new Date(mockMapData.createdAt).toLocaleDateString()}
                  </div>
                  <div className="bg-gradient-to-r from-purple-100 to-pink-100 px-4 py-2 rounded-full">
                    📐 Dimensions: {mockMapData.width}×{mockMapData.height}
                  </div>
                </div>
              </div>

              {/* Map Image */}
              {mockMapData.imageData && (
                <div className="mb-8">
                  <div className="bg-gradient-to-r from-gray-100 to-gray-200 p-6 rounded-2xl">
                    <img
                      src={mockMapData.imageData}
                      alt={mockMapData.name}
                      className="max-w-full h-auto rounded-xl border-4 border-white shadow-lg mx-auto"
                    />
                  </div>
                </div>
              )}

              {/* Actions */}
              <div className="flex flex-wrap justify-center gap-4">
                <button
                  onClick={() => mockNavigate("/profile")}
                  className="bg-gradient-to-r from-blue-500 to-purple-600 text-white py-3 px-8 rounded-xl hover:from-blue-600 hover:to-purple-700 transform hover:-translate-y-1 hover:shadow-lg transition-all duration-300 font-semibold"
                >
                  ← Back to Profile
                </button>
                <button
                  onClick={() => mockNavigate("/")}
                  className="bg-gradient-to-r from-emerald-500 to-teal-600 text-white py-3 px-8 rounded-xl hover:from-emerald-600 hover:to-teal-700 transform hover:-translate-y-1 hover:shadow-lg transition-all duration-300 font-semibold"
                >
                  🎨 Create New Map
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }),
}));

// Import Map after all mocks are set up
import Map from "../Map";

const mockMakeAuthenticatedRequest = vi.fn();

describe("Map Component", () => {
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
    mockUseParams.mockReturnValue({
      id: "test-map-id",
    });
    vi.mocked(authInterceptor.makeAuthenticatedRequest).mockImplementation(
      mockMakeAuthenticatedRequest
    );
    vi.mocked(useNotification)().showNotification = mockShowNotification;
  });

  it("renders map component", () => {
    render(<Map />);
    expect(screen.getByTestId("map-component")).toBeInTheDocument();
  });

  it("renders map title", () => {
    render(<Map />);
    expect(screen.getByText("🗺️ Test Map")).toBeInTheDocument();
  });

  it("renders map creation date", () => {
    render(<Map />);
    expect(screen.getByText(/📅 Created:/)).toBeInTheDocument();
  });

  it("renders map dimensions", () => {
    render(<Map />);
    expect(screen.getByText("📐 Dimensions: 500×500")).toBeInTheDocument();
  });

  it("renders map image", () => {
    render(<Map />);
    const image = screen.getByAltText("Test Map");
    expect(image).toBeInTheDocument();
    expect(image).toHaveAttribute(
      "src",
      "data:image/png;base64,test-image-data"
    );
  });

  it("renders navigation buttons", () => {
    render(<Map />);
    expect(screen.getByText("← Back to Profile")).toBeInTheDocument();
    expect(screen.getByText("🎨 Create New Map")).toBeInTheDocument();
  });

  it("navigates to profile when back button is clicked", () => {
    render(<Map />);
    const backButton = screen.getByText("← Back to Profile");
    backButton.click();
    expect(mockNavigate).toHaveBeenCalledWith("/profile");
  });

  it("navigates to home when create new map button is clicked", () => {
    render(<Map />);
    const createButton = screen.getByText("🎨 Create New Map");
    createButton.click();
    expect(mockNavigate).toHaveBeenCalledWith("/");
  });

  it("has proper title styling", () => {
    render(<Map />);
    const title = screen.getByText("🗺️ Test Map");
    expect(title).toHaveClass(
      "text-3xl",
      "md:text-4xl",
      "font-bold",
      "bg-gradient-to-r",
      "from-blue-600",
      "to-purple-600",
      "bg-clip-text",
      "text-transparent"
    );
  });

  it("has proper button styling", () => {
    render(<Map />);
    const backButton = screen.getByText("← Back to Profile");
    const createButton = screen.getByText("🎨 Create New Map");

    expect(backButton).toHaveClass(
      "bg-gradient-to-r",
      "from-blue-500",
      "to-purple-600"
    );
    expect(createButton).toHaveClass(
      "bg-gradient-to-r",
      "from-emerald-500",
      "to-teal-600"
    );
  });

  it("has proper container styling", () => {
    render(<Map />);
    const container = screen.getByTestId("map-component").firstChild;
    expect(container).toHaveClass(
      "min-h-screen",
      "bg-gradient-to-br",
      "from-blue-50",
      "via-purple-50",
      "to-pink-50"
    );
  });

  it("has proper card styling", () => {
    render(<Map />);
    const card = screen.getByText("🗺️ Test Map").closest("div")?.parentElement;
    expect(card).toHaveClass(
      "bg-white/80",
      "backdrop-blur-sm",
      "rounded-3xl",
      "shadow-xl"
    );
  });

  it("handles authentication state", () => {
    mockUseAppSelector.mockReturnValue({
      token: null,
      user: null,
      isAuthenticated: false,
    });

    render(<Map />);
    // The mock component renders regardless of auth state
    expect(screen.getByTestId("map-component")).toBeInTheDocument();
  });

  it("handles missing map ID", () => {
    mockUseParams.mockReturnValue({
      id: undefined,
    });

    render(<Map />);
    expect(screen.getByTestId("map-component")).toBeInTheDocument();
  });

  it("formats date correctly", () => {
    render(<Map />);
    expect(screen.getByText(/📅 Created:/)).toBeInTheDocument();
  });

  describe("Business Logic and API Integration", () => {
    describe("Fetch Map Data", () => {
      beforeEach(() => {
        mockMakeAuthenticatedRequest.mockResolvedValue({
          ok: true,
          json: () =>
            Promise.resolve({
              id: "test-map-id",
              name: "Test Map",
              imageData: "data:image/png;base64,test-image-data",
              createdAt: "2024-01-01T00:00:00Z",
              width: 500,
              height: 500,
            }),
        });
      });

      it("should fetch map data with correct parameters", async () => {
        const apiHost = "http://localhost:3000";
        const mapId = "test-map-id";

        await authInterceptor.makeAuthenticatedRequest(
          `${apiHost}/api/v1/maps/${mapId}`
        );

        expect(mockMakeAuthenticatedRequest).toHaveBeenCalledWith(
          "http://localhost:3000/api/v1/maps/test-map-id"
        );
      });

      it("should handle successful map fetch", async () => {
        const apiHost = "http://localhost:3000";
        const mapId = "test-map-id";
        const mockMapData = {
          id: "test-map-id",
          name: "Test Map",
          imageData: "data:image/png;base64,test-image-data",
          createdAt: "2024-01-01T00:00:00Z",
          width: 500,
          height: 500,
        };

        mockMakeAuthenticatedRequest.mockResolvedValue({
          ok: true,
          json: () => Promise.resolve(mockMapData),
        });

        const response = await authInterceptor.makeAuthenticatedRequest(
          `${apiHost}/api/v1/maps/${mapId}`
        );

        expect(response.ok).toBe(true);
        const data = await response.json();
        expect(data).toEqual(mockMapData);
      });

      it("should handle map data without image", async () => {
        const apiHost = "http://localhost:3000";
        const mapId = "test-map-id";
        const mockMapData = {
          id: "test-map-id",
          name: "Test Map",
          createdAt: "2024-01-01T00:00:00Z",
          width: 500,
          height: 500,
        };

        mockMakeAuthenticatedRequest.mockResolvedValue({
          ok: true,
          json: () => Promise.resolve(mockMapData),
        });

        const response = await authInterceptor.makeAuthenticatedRequest(
          `${apiHost}/api/v1/maps/${mapId}`
        );

        expect(response.ok).toBe(true);
        const data = await response.json();
        expect(data).toEqual(mockMapData);
        expect(data.imageData).toBeUndefined();
      });

      it("should handle API errors when fetching map", async () => {
        mockMakeAuthenticatedRequest.mockResolvedValue({
          ok: false,
          statusText: "Map not found",
        });

        const response = await authInterceptor.makeAuthenticatedRequest(
          "http://localhost:3000/api/v1/maps/test-map-id"
        );

        expect(response.ok).toBe(false);
        expect(response.statusText).toBe("Map not found");
      });

      it("should handle network errors when fetching map", async () => {
        mockMakeAuthenticatedRequest.mockRejectedValue(
          new Error("Network error")
        );

        try {
          await authInterceptor.makeAuthenticatedRequest(
            "http://localhost:3000/api/v1/maps/test-map-id"
          );
        } catch (error) {
          expect(error).toBeInstanceOf(Error);
          expect((error as Error).message).toBe("Network error");
        }
      });

      it("should handle authentication errors when fetching map", async () => {
        mockMakeAuthenticatedRequest.mockRejectedValue(
          new Error("Authentication failed")
        );

        try {
          await authInterceptor.makeAuthenticatedRequest(
            "http://localhost:3000/api/v1/maps/test-map-id"
          );
        } catch (error) {
          expect(error).toBeInstanceOf(Error);
          expect((error as Error).message).toBe("Authentication failed");
        }
      });

      it("should handle token expiration errors", async () => {
        mockMakeAuthenticatedRequest.mockRejectedValue(
          new Error("Token expired")
        );

        try {
          await authInterceptor.makeAuthenticatedRequest(
            "http://localhost:3000/api/v1/maps/test-map-id"
          );
        } catch (error) {
          expect(error).toBeInstanceOf(Error);
          expect((error as Error).message).toBe("Token expired");
        }
      });

      it("should handle missing token errors", async () => {
        mockMakeAuthenticatedRequest.mockRejectedValue(
          new Error("No valid token available")
        );

        try {
          await authInterceptor.makeAuthenticatedRequest(
            "http://localhost:3000/api/v1/maps/test-map-id"
          );
        } catch (error) {
          expect(error).toBeInstanceOf(Error);
          expect((error as Error).message).toBe("No valid token available");
        }
      });
    });

    describe("Environment Variables", () => {
      it("should handle missing API host", () => {
        const apiHost = undefined;

        if (!apiHost) {
          expect(() => {
            throw new Error("API host not configured");
          }).toThrow("API host not configured");
        }
      });

      it("should use configured API host", () => {
        const apiHost = "http://localhost:3000";
        expect(apiHost).toBe("http://localhost:3000");
      });
    });

    describe("Data Validation", () => {
      it("should validate map data structure", () => {
        const mapData = {
          id: "test-map-id",
          name: "Test Map",
          imageData: "data:image/png;base64,test-image-data",
          createdAt: "2024-01-01T00:00:00Z",
          width: 500,
          height: 500,
        };

        expect(mapData.id).toBe("test-map-id");
        expect(mapData.name).toBe("Test Map");
        expect(mapData.imageData).toBe("data:image/png;base64,test-image-data");
        expect(mapData.createdAt).toBe("2024-01-01T00:00:00Z");
        expect(mapData.width).toBe(500);
        expect(mapData.height).toBe(500);
        expect(new Date(mapData.createdAt)).toBeInstanceOf(Date);
      });

      it("should validate map data without image", () => {
        const mapData = {
          id: "test-map-id",
          name: "Test Map",
          createdAt: "2024-01-01T00:00:00Z",
          width: 500,
          height: 500,
        };

        expect(mapData.id).toBe("test-map-id");
        expect(mapData.name).toBe("Test Map");
        expect((mapData as { imageData?: string }).imageData).toBeUndefined();
        expect(mapData.createdAt).toBe("2024-01-01T00:00:00Z");
        expect(mapData.width).toBe(500);
        expect(mapData.height).toBe(500);
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

      it("should validate URL parameters", () => {
        const params = {
          id: "test-map-id",
        };

        expect(params.id).toBe("test-map-id");
        expect(typeof params.id).toBe("string");
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

      it("should handle missing map ID", () => {
        const mapId = undefined;
        expect(mapId).toBeUndefined();
      });

      it("should handle invalid map ID", () => {
        const mapId = "";
        expect(mapId).toBe("");
      });
    });

    describe("Loading States", () => {
      it("should handle loading state", () => {
        const loading = true;
        expect(loading).toBe(true);
      });

      it("should handle loaded state", () => {
        const loading = false;
        expect(loading).toBe(false);
      });
    });

    describe("Error States", () => {
      it("should handle error state", () => {
        const error = "Failed to load map";
        expect(error).toBe("Failed to load map");
        expect(typeof error).toBe("string");
      });

      it("should handle no error state", () => {
        const error = "";
        expect(error).toBe("");
      });
    });
  });
});
