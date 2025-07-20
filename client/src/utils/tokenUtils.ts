import { jwtDecode } from "jwt-decode";

export interface DecodedToken {
  sub: string; // user ID
  iat: number; // issued at
  exp: number; // expiration time
}

// For testing purposes - just change this variable to override token expiration
// Set to null to use real token expiration, or set to a Unix timestamp in seconds or ISO date string
// Examples:
// null = use real token expiration
// Math.floor(Date.now() / 1000) + 30 = expire in 30 seconds
// Math.floor(Date.now() / 1000) - 60 = expired 1 minute ago
// "2025-07-20T01:10:08.002Z" = expire at specific date/time
const testExpirationOverride: number | string | null = null;

/**
 * Decode a JWT token and return its payload
 */
export const decodeToken = (token: string): DecodedToken | null => {
  try {
    const decoded = jwtDecode<DecodedToken>(token);

    // Apply test override if set
    if (testExpirationOverride !== null) {
      let overrideExp: number;

      if (typeof testExpirationOverride === "string") {
        // Parse ISO date string to Unix timestamp
        const parsed = Date.parse(testExpirationOverride);
        if (isNaN(parsed)) {
          console.error(
            "Invalid date string for test expiration override:",
            testExpirationOverride
          );
          return decoded;
        }
        overrideExp = Math.floor(parsed / 1000);
      } else {
        overrideExp = testExpirationOverride;
      }

      return {
        ...decoded,
        exp: overrideExp,
      };
    }

    return decoded;
  } catch (error) {
    console.error("Failed to decode token:", error);
    return null;
  }
};

/**
 * Check if a token is expired
 */
export const isTokenExpired = (token: string): boolean => {
  const decoded = decodeToken(token);
  if (!decoded) return true;

  const currentTime = Date.now() / 1000; // Convert to seconds
  // Log the current time, decoded expiration time (all in readable format), and whether the token is expired
  console.log("currentTime", new Date(currentTime * 1000).toISOString());
  console.log("decoded.exp", new Date(decoded.exp * 1000).toISOString());
  console.log(
    "isTokenExpired",
    decoded.exp < currentTime,
    decoded.exp,
    currentTime
  );
  return decoded.exp < currentTime;
};

/**
 * Check if a token will expire within the specified time (in milliseconds)
 */
export const isTokenExpiringSoon = (
  token: string,
  timeBeforeExpiry: number = 3600000
): boolean => {
  const decoded = decodeToken(token);
  if (!decoded) return true;

  const currentTime = Date.now();
  const expiryTime = decoded.exp * 1000; // Convert to milliseconds
  const timeUntilExpiry = expiryTime - currentTime;

  return timeUntilExpiry < timeBeforeExpiry;
};

/**
 * Get the time until token expires in milliseconds
 */
export const getTimeUntilExpiry = (token: string): number => {
  const decoded = decodeToken(token);
  if (!decoded) return 0;

  const currentTime = Date.now();
  const expiryTime = decoded.exp * 1000; // Convert to milliseconds

  return Math.max(0, expiryTime - currentTime);
};

/**
 * Get user ID from token
 */
export const getUserIdFromToken = (token: string): string | null => {
  const decoded = decodeToken(token);
  return decoded?.sub || null;
};
