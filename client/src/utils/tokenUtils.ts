import { jwtDecode } from "jwt-decode";

export interface DecodedToken {
  sub: string; // user ID
  iat: number; // issued at
  exp: number; // expiration time
}

/**
 * Decode a JWT token and return its payload
 */
export const decodeToken = (token: string): DecodedToken | null => {
  try {
    return jwtDecode<DecodedToken>(token);
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
