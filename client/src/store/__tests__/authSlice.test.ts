import { describe, it, expect, vi, beforeEach } from "vitest";
import { configureStore } from "@reduxjs/toolkit";
import authReducer, {
  loginUser,
  registerUser,
  logoutUser,
  verifyToken,
  refreshToken,
  clearError,
  setCredentials,
  logout,
  type AuthState,
  type User,
} from "../authSlice";

const createTestStore = (preloadedState?: Partial<AuthState>) => {
  return configureStore({
    reducer: { auth: authReducer },
    preloadedState: {
      auth: {
        user: null,
        token: null,
        isAuthenticated: false,
        isLoading: false,
        error: null,
        ...preloadedState,
      },
    },
  });
};

describe("authSlice", () => {
  let store: ReturnType<typeof createTestStore>;

  beforeEach(() => {
    store = createTestStore();
    vi.clearAllMocks();
  });

  describe("reducers", () => {
    it("should handle clearError", () => {
      const initialState = {
        user: null,
        token: null,
        isAuthenticated: false,
        isLoading: false,
        error: "Some error",
      };

      store = createTestStore(initialState);
      store.dispatch(clearError());

      const state = store.getState().auth;
      expect(state.error).toBeNull();
    });

    it("should handle setCredentials", () => {
      const user: User = {
        id: 1,
        name: "Test User",
        email: "test@example.com",
      };
      const token = "test-token";

      store.dispatch(setCredentials({ user, token }));

      const state = store.getState().auth;
      expect(state.user).toEqual(user);
      expect(state.token).toBe(token);
      expect(state.isAuthenticated).toBe(true);
      expect(state.error).toBeNull();
    });

    it("should handle logout", () => {
      const initialState = {
        user: { id: 1, name: "Test User", email: "test@example.com" },
        token: "test-token",
        isAuthenticated: true,
        isLoading: false,
        error: null,
      };

      store = createTestStore(initialState);
      store.dispatch(logout());

      const state = store.getState().auth;
      expect(state.user).toBeNull();
      expect(state.token).toBeNull();
      expect(state.isAuthenticated).toBe(false);
      expect(state.error).toBeNull();
    });
  });

  describe("async thunks", () => {
    describe("loginUser", () => {
      it("should handle successful login", async () => {
        const mockResponse = {
          user_id: 1,
          name: "Test User",
          email: "test@example.com",
          token: "test-token",
        };

        vi.mocked(fetch).mockResolvedValueOnce({
          ok: true,
          json: async () => mockResponse,
        } as Response);

        const result = await store.dispatch(
          loginUser({ email: "test@example.com", password: "password" })
        );

        expect(loginUser.fulfilled.match(result)).toBe(true);
        expect(result.payload).toEqual(mockResponse);

        const state = store.getState().auth;
        expect(state.user).toEqual({
          id: mockResponse.user_id,
          name: mockResponse.name,
          email: mockResponse.email,
        });
        expect(state.token).toBe(mockResponse.token);
        expect(state.isAuthenticated).toBe(true);
        expect(state.isLoading).toBe(false);
        expect(state.error).toBeNull();
      });

      it("should handle login failure with 401", async () => {
        vi.mocked(fetch).mockResolvedValueOnce({
          ok: false,
          status: 401,
          json: async () => ({ error: "Invalid credentials" }),
        } as Response);

        const result = await store.dispatch(
          loginUser({ email: "test@example.com", password: "wrong" })
        );

        expect(loginUser.rejected.match(result)).toBe(true);
        expect(result.payload).toBe("Invalid email or password");

        const state = store.getState().auth;
        expect(state.isLoading).toBe(false);
        expect(state.error).toBe("Invalid email or password");
      });

      it("should handle network error", async () => {
        vi.mocked(fetch).mockRejectedValueOnce(new Error("Network error"));

        const result = await store.dispatch(
          loginUser({ email: "test@example.com", password: "password" })
        );

        expect(loginUser.rejected.match(result)).toBe(true);
        expect(result.payload).toBe("Network error");

        const state = store.getState().auth;
        expect(state.isLoading).toBe(false);
        expect(state.error).toBe("Network error");
      });
    });

    describe("registerUser", () => {
      it("should handle successful registration", async () => {
        const mockResponse = {
          user_id: 1,
          name: "New User",
          email: "new@example.com",
          token: "new-token",
        };

        vi.mocked(fetch).mockResolvedValueOnce({
          ok: true,
          json: async () => mockResponse,
        } as Response);

        const result = await store.dispatch(
          registerUser({
            name: "New User",
            email: "new@example.com",
            password: "password",
          })
        );

        expect(registerUser.fulfilled.match(result)).toBe(true);
        expect(result.payload).toEqual(mockResponse);

        const state = store.getState().auth;
        expect(state.user).toEqual({
          id: mockResponse.user_id,
          name: mockResponse.name,
          email: mockResponse.email,
        });
        expect(state.token).toBe(mockResponse.token);
        expect(state.isAuthenticated).toBe(true);
        expect(state.isLoading).toBe(false);
        expect(state.error).toBeNull();
      });

      it("should handle registration failure with 409", async () => {
        vi.mocked(fetch).mockResolvedValueOnce({
          ok: false,
          status: 409,
          json: async () => ({ error: "User already exists" }),
        } as Response);

        const result = await store.dispatch(
          registerUser({
            name: "Existing User",
            email: "existing@example.com",
            password: "password",
          })
        );

        expect(registerUser.rejected.match(result)).toBe(true);
        expect(result.payload).toBe("User with this email already exists");

        const state = store.getState().auth;
        expect(state.isLoading).toBe(false);
        expect(state.error).toBe("User with this email already exists");
      });
    });

    describe("logoutUser", () => {
      it("should handle successful logout", async () => {
        const initialState = {
          user: { id: 1, name: "Test User", email: "test@example.com" },
          token: "test-token",
          isAuthenticated: true,
          isLoading: false,
          error: null,
        };

        store = createTestStore(initialState);

        vi.mocked(fetch).mockResolvedValueOnce({
          ok: true,
        } as Response);

        await store.dispatch(logoutUser());

        const state = store.getState().auth;
        expect(state.user).toBeNull();
        expect(state.token).toBeNull();
        expect(state.isAuthenticated).toBe(false);
        expect(state.error).toBeNull();
      });

      it("should handle logout with network error", async () => {
        const initialState = {
          user: { id: 1, name: "Test User", email: "test@example.com" },
          token: "test-token",
          isAuthenticated: true,
          isLoading: false,
          error: null,
        };

        store = createTestStore(initialState);

        vi.mocked(fetch).mockRejectedValueOnce(new Error("Network error"));

        await store.dispatch(logoutUser());

        // Should still clear local state even if server request fails
        const state = store.getState().auth;
        expect(state.user).toBeNull();
        expect(state.token).toBeNull();
        expect(state.isAuthenticated).toBe(false);
        expect(state.error).toBeNull();
      });
    });

    describe("verifyToken", () => {
      it("should handle successful token verification", async () => {
        const initialState = {
          user: { id: 1, name: "Test User", email: "test@example.com" },
          token: "test-token",
          isAuthenticated: true,
          isLoading: false,
          error: null,
        };

        store = createTestStore(initialState);

        vi.mocked(fetch).mockResolvedValueOnce({
          ok: true,
          json: async () => ({ valid: true }),
        } as Response);

        const result = await store.dispatch(verifyToken());

        expect(verifyToken.fulfilled.match(result)).toBe(true);

        const state = store.getState().auth;
        expect(state.error).toBeNull();
      });

      it("should handle token verification failure", async () => {
        const initialState = {
          user: { id: 1, name: "Test User", email: "test@example.com" },
          token: "invalid-token",
          isAuthenticated: true,
          isLoading: false,
          error: null,
        };

        store = createTestStore(initialState);

        vi.mocked(fetch).mockResolvedValueOnce({
          ok: false,
        } as Response);

        const result = await store.dispatch(verifyToken());

        expect(verifyToken.rejected.match(result)).toBe(true);
        expect(result.payload).toBe("Token invalid");

        const state = store.getState().auth;
        expect(state.user).toBeNull();
        expect(state.token).toBeNull();
        expect(state.isAuthenticated).toBe(false);
      });
    });

    describe("refreshToken", () => {
      it("should handle successful token refresh", async () => {
        const initialState = {
          user: { id: 1, name: "Test User", email: "test@example.com" },
          token: "old-token",
          isAuthenticated: true,
          isLoading: false,
          error: null,
        };

        store = createTestStore(initialState);

        const mockResponse = { token: "new-token" };

        vi.mocked(fetch).mockResolvedValueOnce({
          ok: true,
          json: async () => mockResponse,
        } as Response);

        const result = await store.dispatch(refreshToken());

        expect(refreshToken.fulfilled.match(result)).toBe(true);
        expect(result.payload).toEqual(mockResponse);

        const state = store.getState().auth;
        expect(state.token).toBe("new-token");
        expect(state.error).toBeNull();
      });

      it("should handle token refresh failure", async () => {
        const initialState = {
          user: { id: 1, name: "Test User", email: "test@example.com" },
          token: "old-token",
          isAuthenticated: true,
          isLoading: false,
          error: null,
        };

        store = createTestStore(initialState);

        vi.mocked(fetch).mockResolvedValueOnce({
          ok: false,
        } as Response);

        const result = await store.dispatch(refreshToken());

        expect(refreshToken.rejected.match(result)).toBe(true);
        expect(result.payload).toBe("Token refresh failed");

        const state = store.getState().auth;
        expect(state.user).toBeNull();
        expect(state.token).toBeNull();
        expect(state.isAuthenticated).toBe(false);
      });
    });
  });
});
