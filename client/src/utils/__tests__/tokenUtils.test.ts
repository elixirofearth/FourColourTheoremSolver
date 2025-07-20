import { describe, it, expect, vi, beforeEach } from "vitest";
import {
  decodeToken,
  isTokenExpired,
  isTokenExpiringSoon,
  getTimeUntilExpiry,
  getUserIdFromToken,
  type DecodedToken,
} from "../tokenUtils";

// Mock jwt-decode
vi.mock("jwt-decode", () => ({
  jwtDecode: vi.fn(),
}));

const mockJwtDecode = vi.mocked(await import("jwt-decode")).jwtDecode;

describe("tokenUtils", () => {
  const mockToken = "mock.jwt.token";
  const mockDecodedToken: DecodedToken = {
    sub: "user123",
    iat: 1640995200, // 2022-01-01 00:00:00 UTC
    exp: 1641081600, // 2022-01-02 00:00:00 UTC
  };

  beforeEach(() => {
    vi.clearAllMocks();
    // Mock Date.now() to return a fixed timestamp
    vi.spyOn(Date, "now").mockReturnValue(1640995200000); // 2022-01-01 00:00:00 UTC
  });

  describe("decodeToken", () => {
    it("should decode a valid token", () => {
      mockJwtDecode.mockReturnValue(mockDecodedToken);

      const result = decodeToken(mockToken);

      expect(mockJwtDecode).toHaveBeenCalledWith(mockToken);
      expect(result).toEqual(mockDecodedToken);
    });

    it("should return null for invalid token", () => {
      mockJwtDecode.mockImplementation(() => {
        throw new Error("Invalid token");
      });

      const result = decodeToken("invalid.token");

      expect(result).toBeNull();
    });
  });

  describe("isTokenExpired", () => {
    it("should return true for expired token", () => {
      const expiredToken = {
        ...mockDecodedToken,
        exp: 1640995200 - 3600, // 1 hour ago
      };
      mockJwtDecode.mockReturnValue(expiredToken);

      const result = isTokenExpired(mockToken);

      expect(result).toBe(true);
    });

    it("should return false for valid token", () => {
      const validToken = {
        ...mockDecodedToken,
        exp: 1640995200 + 3600, // 1 hour from now
      };
      mockJwtDecode.mockReturnValue(validToken);

      const result = isTokenExpired(mockToken);

      expect(result).toBe(false);
    });

    it("should return true for invalid token", () => {
      mockJwtDecode.mockImplementation(() => {
        throw new Error("Invalid token");
      });

      const result = isTokenExpired("invalid.token");

      expect(result).toBe(true);
    });
  });

  describe("isTokenExpiringSoon", () => {
    it("should return true when token expires within specified time", () => {
      const expiringToken = {
        ...mockDecodedToken,
        exp: 1640995200 + 1800, // 30 minutes from now
      };
      mockJwtDecode.mockReturnValue(expiringToken);

      const result = isTokenExpiringSoon(mockToken, 3600000); // 1 hour

      expect(result).toBe(true);
    });

    it("should return false when token expires after specified time", () => {
      const validToken = {
        ...mockDecodedToken,
        exp: 1640995200 + 7200, // 2 hours from now
      };
      mockJwtDecode.mockReturnValue(validToken);

      const result = isTokenExpiringSoon(mockToken, 3600000); // 1 hour

      expect(result).toBe(false);
    });

    it("should return true for invalid token", () => {
      mockJwtDecode.mockImplementation(() => {
        throw new Error("Invalid token");
      });

      const result = isTokenExpiringSoon("invalid.token");

      expect(result).toBe(true);
    });
  });

  describe("getTimeUntilExpiry", () => {
    it("should return correct time until expiry", () => {
      const tokenExpiringIn1Hour = {
        ...mockDecodedToken,
        exp: 1640995200 + 3600, // 1 hour from now
      };
      mockJwtDecode.mockReturnValue(tokenExpiringIn1Hour);

      const result = getTimeUntilExpiry(mockToken);

      expect(result).toBe(3600000); // 1 hour in milliseconds
    });

    it("should return 0 for expired token", () => {
      const expiredToken = {
        ...mockDecodedToken,
        exp: 1640995200 - 3600, // 1 hour ago
      };
      mockJwtDecode.mockReturnValue(expiredToken);

      const result = getTimeUntilExpiry(mockToken);

      expect(result).toBe(0);
    });

    it("should return 0 for invalid token", () => {
      mockJwtDecode.mockImplementation(() => {
        throw new Error("Invalid token");
      });

      const result = getTimeUntilExpiry("invalid.token");

      expect(result).toBe(0);
    });
  });

  describe("getUserIdFromToken", () => {
    it("should return user ID from valid token", () => {
      mockJwtDecode.mockReturnValue(mockDecodedToken);

      const result = getUserIdFromToken(mockToken);

      expect(result).toBe("user123");
    });

    it("should return null for invalid token", () => {
      mockJwtDecode.mockImplementation(() => {
        throw new Error("Invalid token");
      });

      const result = getUserIdFromToken("invalid.token");

      expect(result).toBeNull();
    });

    it("should return null for token without sub field", () => {
      const tokenWithoutSub = {
        iat: 1640995200,
        exp: 1641081600,
      };
      mockJwtDecode.mockReturnValue(tokenWithoutSub);

      const result = getUserIdFromToken(mockToken);

      expect(result).toBeNull();
    });
  });
});
