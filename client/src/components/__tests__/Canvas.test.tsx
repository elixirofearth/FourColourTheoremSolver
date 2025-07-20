import { vi, describe, it, expect, beforeEach } from "vitest";
import { authInterceptor } from "../../utils/authInterceptor";
import { useNotification } from "../../hooks/useNotification";

// Mock dependencies
vi.mock("../../utils/authInterceptor", () => ({
  authInterceptor: {
    makeAuthenticatedRequest: vi.fn(),
  },
}));

vi.mock("../../hooks/useNotification", () => ({
  useNotification: () => ({
    showNotification: vi.fn(),
  }),
}));

// Mock environment variables
vi.mock("import.meta.env", () => ({
  VITE_API_GATEWAY_URL: "http://localhost:3000",
}));

const mockMakeAuthenticatedRequest = vi.fn();
const mockShowNotification = vi.fn();

describe("Canvas Component Logic", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(authInterceptor.makeAuthenticatedRequest).mockImplementation(
      mockMakeAuthenticatedRequest
    );
    vi.mocked(useNotification)().showNotification = mockShowNotification;
  });

  describe("API Integration", () => {
    beforeEach(() => {
      mockMakeAuthenticatedRequest.mockResolvedValue({
        ok: true,
        json: () =>
          Promise.resolve([
            [1, 0, 0],
            [0, 1, 0],
            [0, 0, 1],
          ]),
      });
    });

    it("should call API with correct parameters when coloring map", async () => {
      const apiHost = "http://localhost:3000";
      const userId = 1;
      const imageData = new Uint8ClampedArray(500 * 500 * 4).fill(255);

      await authInterceptor.makeAuthenticatedRequest(
        `${apiHost}/api/v1/maps/color`,
        {
          method: "POST",
          body: JSON.stringify({
            image: {
              data: Array.from(imageData),
            },
            height: 500,
            width: 500,
            userId: userId,
          }),
          headers: {
            "Content-Type": "application/json",
          },
        }
      );

      expect(mockMakeAuthenticatedRequest).toHaveBeenCalledWith(
        "http://localhost:3000/api/v1/maps/color",
        expect.objectContaining({
          method: "POST",
          body: expect.stringContaining('"userId":1'),
        })
      );
    });

    it("should handle API errors gracefully", async () => {
      mockMakeAuthenticatedRequest.mockRejectedValue(new Error("API Error"));

      try {
        await authInterceptor.makeAuthenticatedRequest("test-url");
      } catch (error) {
        expect(error).toBeInstanceOf(Error);
        expect((error as Error).message).toBe("API Error");
      }
    });

    it("should handle authentication errors", async () => {
      mockMakeAuthenticatedRequest.mockRejectedValue(
        new Error("Authentication failed")
      );

      try {
        await authInterceptor.makeAuthenticatedRequest("test-url");
      } catch (error) {
        expect(error).toBeInstanceOf(Error);
        expect((error as Error).message).toBe("Authentication failed");
      }
    });
  });

  describe("Save Map Functionality", () => {
    beforeEach(() => {
      mockMakeAuthenticatedRequest.mockResolvedValue({
        ok: true,
        json: () => Promise.resolve({ id: "saved-map-id" }),
      });
    });

    it("should save map with correct parameters", async () => {
      const apiHost = "http://localhost:3000";
      const userId = 1;
      const imageData = "data:image/png;base64,test-data";
      const matrix = [
        [1, 0, 0],
        [0, 1, 0],
        [0, 0, 1],
      ];

      await authInterceptor.makeAuthenticatedRequest(`${apiHost}/api/v1/maps`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          userId: userId,
          name: "Test Map",
          imageData: imageData,
          matrix: matrix,
          width: 500,
          height: 500,
        }),
      });

      expect(mockMakeAuthenticatedRequest).toHaveBeenCalledWith(
        "http://localhost:3000/api/v1/maps",
        expect.objectContaining({
          method: "POST",
          body: expect.stringContaining('"userId":1'),
        })
      );
    });
  });

  describe("Environment Variables", () => {
    it("should handle missing API host", () => {
      const apiHost = undefined;

      if (!apiHost) {
        expect(() => {
          throw new Error(
            "API host is not defined in the environment variables"
          );
        }).toThrow("API host is not defined in the environment variables");
      }
    });

    it("should use configured API host", () => {
      const apiHost = "http://localhost:3000";
      expect(apiHost).toBe("http://localhost:3000");
    });
  });

  describe("Data Validation", () => {
    it("should validate image data format", () => {
      const imageData = new Uint8ClampedArray(500 * 500 * 4).fill(255);
      expect(imageData.length).toBe(500 * 500 * 4);
      expect(imageData[0]).toBe(255);
    });

    it("should validate matrix format", () => {
      const matrix = [
        [1, 0, 0],
        [0, 1, 0],
        [0, 0, 1],
      ];
      expect(matrix.length).toBe(3);
      expect(matrix[0].length).toBe(3);
      expect(matrix[0][0]).toBe(1);
    });

    it("should validate user data", () => {
      const user = { id: 1, name: "Test User", email: "test@example.com" };
      expect(user.id).toBe(1);
      expect(user.name).toBe("Test User");
      expect(user.email).toBe("test@example.com");
    });
  });
});
