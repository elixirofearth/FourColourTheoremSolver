import { store } from "../store/store";
import { refreshToken, logout } from "../store/authSlice";
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
      store.dispatch(logout());
      return null;
    }

    // If token is expiring soon (within 1 hour), refresh it
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
        const result = await store.dispatch(refreshToken());
        if (refreshToken.fulfilled.match(result)) {
          this.processQueue(null, result.payload.token);
          this.isRefreshing = false;
          return result.payload.token;
        } else {
          this.processQueue(new Error("Token refresh failed"));
          this.isRefreshing = false;
          store.dispatch(logout());
          return null;
        }
      } catch (error) {
        this.processQueue(
          error instanceof Error ? error : new Error("Unknown error")
        );
        this.isRefreshing = false;
        store.dispatch(logout());
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
    let token = await this.checkAndRefreshToken();

    if (!token) {
      throw new Error("No valid token available");
    }

    const response = await fetch(url, {
      ...options,
      headers: {
        ...options.headers,
        Authorization: `Bearer ${token}`,
      },
    });

    // If we get a 401, try to refresh the token and retry once
    if (response.status === 401) {
      if (this.isRefreshing) {
        // Wait for the current refresh to complete
        token = await new Promise((resolve, reject) => {
          this.failedQueue.push({ resolve, reject });
        });
      } else {
        // Try to refresh the token
        this.isRefreshing = true;
        try {
          const result = await store.dispatch(refreshToken());
          if (refreshToken.fulfilled.match(result)) {
            token = result.payload.token;
            this.processQueue(null, token);
          } else {
            this.processQueue(new Error("Token refresh failed"));
            store.dispatch(logout());
            throw new Error("Authentication failed");
          }
        } catch (error) {
          this.processQueue(
            error instanceof Error ? error : new Error("Unknown error")
          );
          store.dispatch(logout());
          throw new Error("Authentication failed");
        } finally {
          this.isRefreshing = false;
        }
      }

      // Retry the original request with the new token
      if (token) {
        return fetch(url, {
          ...options,
          headers: {
            ...options.headers,
            Authorization: `Bearer ${token}`,
          },
        });
      }
    }

    return response;
  }
}

// Export singleton instance
export const authInterceptor = AuthInterceptor.getInstance();
