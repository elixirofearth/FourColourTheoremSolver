import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import type { PayloadAction } from "@reduxjs/toolkit";

// Types
export interface User {
  id: number;
  name: string;
  email: string;
}

export interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}

// Async thunks
export const loginUser = createAsyncThunk(
  "auth/login",
  async (
    credentials: { email: string; password: string },
    { rejectWithValue }
  ) => {
    try {
      const apiHost = import.meta.env.VITE_API_GATEWAY_URL;
      const response = await fetch(`${apiHost}/api/v1/auth/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(credentials),
      });

      if (!response.ok) {
        try {
          const errorData = await response.json();
          console.log("Login error response (JSON):", errorData);
          // Handle specific login errors
          if (response.status === 401) {
            return rejectWithValue("Invalid email or password");
          }
          return rejectWithValue(errorData.error || "Login failed");
        } catch (parseError) {
          console.error("Failed to parse error response as JSON:", parseError);
          // Try to get error from response text
          const errorText = await response.text();
          console.log("Login error response (text):", errorText);

          if (response.status === 401) {
            return rejectWithValue("Invalid email or password");
          }
          return rejectWithValue("Login failed");
        }
      }

      const data = await response.json();
      return data;
    } catch (_error) {
      console.error("Login failed:", _error);
      return rejectWithValue("Network error");
    }
  }
);

export const registerUser = createAsyncThunk(
  "auth/register",
  async (
    userData: { name: string; email: string; password: string },
    { rejectWithValue }
  ) => {
    try {
      const apiHost = import.meta.env.VITE_API_GATEWAY_URL;
      const response = await fetch(`${apiHost}/api/v1/auth/register`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(userData),
      });

      if (!response.ok) {
        try {
          const errorData = await response.json();
          console.log("Registration error response (JSON):", errorData);
          // Handle specific registration errors
          if (response.status === 409) {
            return rejectWithValue("User with this email already exists");
          }
          return rejectWithValue(errorData.error || "Registration failed");
        } catch (parseError) {
          console.error("Failed to parse error response as JSON:", parseError);
          // Try to get error from response text
          const errorText = await response.text();
          console.log("Registration error response (text):", errorText);

          if (response.status === 409) {
            return rejectWithValue("User with this email already exists");
          }
          return rejectWithValue("Registration failed");
        }
      }

      const data = await response.json();
      return data;
    } catch (_error) {
      console.error("Registration failed:", _error);
      return rejectWithValue("Network error");
    }
  }
);

// Flag to prevent multiple simultaneous logout requests
let isLoggingOut = false;

export const logoutUser = createAsyncThunk(
  "auth/logout",
  async (_, { getState }) => {
    // Prevent multiple simultaneous logout requests
    if (isLoggingOut) {
      console.log("Logout already in progress, skipping");
      return;
    }

    isLoggingOut = true;

    try {
      const state = getState() as { auth: AuthState };
      const token = state.auth.token;

      if (token) {
        const apiHost = import.meta.env.VITE_API_GATEWAY_URL;
        const response = await fetch(`${apiHost}/api/v1/auth/logout`, {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        // Log the response status for debugging
        if (!response.ok) {
          console.log(`Logout request returned status: ${response.status}`);
        }
      }
    } catch (_error) {
      // Even if logout fails on server, we should still clear local state
      console.error("Logout error:", _error);
    } finally {
      isLoggingOut = false;
    }
  }
);

export const verifyToken = createAsyncThunk(
  "auth/verifyToken",
  async (_, { getState, rejectWithValue }) => {
    try {
      const state = getState() as { auth: AuthState };
      const token = state.auth.token;

      if (!token) {
        return rejectWithValue("No token available");
      }

      const apiHost = import.meta.env.VITE_API_GATEWAY_URL;
      const response = await fetch(`${apiHost}/api/v1/auth/verify`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        return rejectWithValue("Token invalid");
      }

      const data = await response.json();
      return data;
    } catch (_error) {
      console.error("Token verification failed:", _error);
      return rejectWithValue("Token verification failed");
    }
  }
);

export const refreshToken = createAsyncThunk(
  "auth/refreshToken",
  async (_, { getState, rejectWithValue }) => {
    try {
      const state = getState() as { auth: AuthState };
      const token = state.auth.token;

      if (!token) {
        return rejectWithValue("No token available");
      }

      const apiHost = import.meta.env.VITE_API_GATEWAY_URL;
      const response = await fetch(`${apiHost}/api/v1/auth/refresh`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        return rejectWithValue("Token refresh failed");
      }

      const data = await response.json();
      return data;
    } catch (_error) {
      console.error("Token refresh failed:", _error);
      return rejectWithValue("Token refresh failed");
    }
  }
);

// Initial state
const initialState: AuthState = {
  user: null,
  token: null,
  isAuthenticated: false,
  isLoading: false,
  error: null,
};

// Slice
const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    setCredentials: (
      state,
      action: PayloadAction<{ user: User; token: string }>
    ) => {
      state.user = action.payload.user;
      state.token = action.payload.token;
      state.isAuthenticated = true;
      state.error = null;
    },
    logout: (state) => {
      state.user = null;
      state.token = null;
      state.isAuthenticated = false;
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    // Login
    builder
      .addCase(loginUser.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(loginUser.fulfilled, (state, action) => {
        state.isLoading = false;
        state.user = {
          id: action.payload.user_id,
          name: action.payload.name,
          email: action.payload.email,
        };
        state.token = action.payload.token;
        state.isAuthenticated = true;
        state.error = null;
      })
      .addCase(loginUser.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      });

    // Register
    builder
      .addCase(registerUser.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(registerUser.fulfilled, (state, action) => {
        state.isLoading = false;
        state.user = {
          id: action.payload.user_id,
          name: action.payload.name,
          email: action.payload.email,
        };
        state.token = action.payload.token;
        state.isAuthenticated = true;
        state.error = null;
      })
      .addCase(registerUser.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      });

    // Logout
    builder.addCase(logoutUser.fulfilled, (state) => {
      state.user = null;
      state.token = null;
      state.isAuthenticated = false;
      state.error = null;
    });

    // Verify Token
    builder
      .addCase(verifyToken.fulfilled, (state) => {
        // Token is valid, no need to update state
        state.error = null;
      })
      .addCase(verifyToken.rejected, (state) => {
        state.user = null;
        state.token = null;
        state.isAuthenticated = false;
      });

    // Refresh Token
    builder
      .addCase(refreshToken.fulfilled, (state, action) => {
        state.token = action.payload.token;
        state.error = null;
      })
      .addCase(refreshToken.rejected, (state) => {
        state.user = null;
        state.token = null;
        state.isAuthenticated = false;
      });
  },
});

export const { clearError, setCredentials, logout } = authSlice.actions;
export default authSlice.reducer;
