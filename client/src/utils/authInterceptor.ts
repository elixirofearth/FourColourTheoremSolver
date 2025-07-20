import { store } from "../store/store";
import { refreshToken, logoutUser } from "../store/authSlice";
import { isTokenExpiringSoon, isTokenExpired } from "./tokenUtils";

/**
 * Authentication interceptor for handling token refresh and 401 errors
 */
export class AuthInterceptor {
  private static instance: AuthInterceptor;
  private isRefreshing = false;
  private failedQueue: Array<{
    resolve: (value: string | null) => void;
    reject: (error: Error) => void;
  }> = [];

  private constructor() {}

  static getInstance(): AuthInterceptor {
    if (!AuthInterceptor.instance) {
      AuthInterceptor.instance = new AuthInterceptor();
    }
    return AuthInterceptor.instance;
  }

  /**
   * Process the queue of failed requests
   */
  private processQueue(error: Error | null, token: string | null = null) {
    this.failedQueue.forEach(({ resolve, reject }) => {
      if (error) {
        reject(error);
      } else {
        resolve(token);
      }
    });
    this.failedQueue = [];
  }

  /**
   * Check and refresh token if needed
   */
  async checkAndRefreshToken(): Promise<string | null> {
    const state = store.getState();
    const token = state.auth.token;

    if (!token) {
      return null;
    }

    // If token is expired, logout immediately
    if (isTokenExpired(token)) {
      console.log("Token is expired, logging out user");
      store.dispatch(logoutUser());
      return null;
    }

    // If token is expiring soon (within 1 hour) but not expired, refresh it
    if (isTokenExpiringSoon(token, 3600000)) {
      // 1 hour
      if (this.isRefreshing) {
        // If already refreshing, wait for the current refresh to complete
        return new Promise((resolve, reject) => {
          this.failedQueue.push({ resolve, reject });
        });
      }

      this.isRefreshing = true;

      try {
        console.log("Token expiring soon, attempting refresh");
        const result = await store.dispatch(refreshToken());
        if (refreshToken.fulfilled.match(result)) {
          this.processQueue(null, result.payload.token);
          this.isRefreshing = false;
          return result.payload.token;
        } else {
          console.log("Token refresh failed, logging out user");
          this.processQueue(new Error("Token refresh failed"));
          this.isRefreshing = false;
          store.dispatch(logoutUser());
          return null;
        }
      } catch (error) {
        console.log("Token refresh error, logging out user:", error);
        this.processQueue(
          error instanceof Error ? error : new Error("Unknown error")
        );
        this.isRefreshing = false;
        store.dispatch(logoutUser());
        return null;
      }
    }

    return token;
  }

  /**
   * Make an authenticated request with automatic token refresh
   */
  async makeAuthenticatedRequest(
    url: string,
    options: RequestInit = {}
  ): Promise<Response> {
    const token = await this.checkAndRefreshToken();

    if (!token) {
      throw new Error("No valid token available - please login");
    }

    const response = await fetch(url, {
      ...options,
      headers: {
        ...options.headers,
        Authorization: `Bearer ${token}`,
      },
    });

    // If we get a 401, the server rejected our token - logout the user
    if (response.status === 401) {
      console.log("Server returned 401 - logging out user");
      store.dispatch(logoutUser());
      throw new Error("Authentication failed - please login again");
    } else {
      console.log("Server returned", response.status);
    }

    return response;
  }
}

// Export singleton instance
export const authInterceptor = AuthInterceptor.getInstance();
