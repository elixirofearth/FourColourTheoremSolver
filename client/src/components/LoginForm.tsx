import React, { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAppDispatch, useAppSelector } from "../store/hooks";
import { loginUser, clearError } from "../store/authSlice";

const LoginForm: React.FC = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [formError, setFormError] = useState("");
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const { isLoading, error, isAuthenticated } = useAppSelector(
    (state) => state.auth
  );

  // Redirect if already authenticated
  useEffect(() => {
    if (isAuthenticated) {
      navigate("/");
    }
  }, [isAuthenticated, navigate]);

  // Handle error from Redux and show in form
  useEffect(() => {
    if (error) {
      setFormError(error);
      dispatch(clearError());
    }
  }, [error, dispatch]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(""); // Clear previous errors

    if (!username || !password) {
      setFormError("Please fill in all fields");
      return;
    }

    try {
      const result = await dispatch(loginUser({ email: username, password }));

      if (loginUser.fulfilled.match(result)) {
        navigate("/");
      }
    } catch (err) {
      console.error("Login error:", err);
    }
  };

  return (
    <div className="flex items-center justify-center p-3 sm:p-4 min-h-screen">
      <div className="w-full max-w-sm sm:max-w-md">
        <form
          onSubmit={handleSubmit}
          className="bg-white/95 backdrop-blur-sm rounded-2xl sm:rounded-3xl shadow-2xl p-6 sm:p-8 transform hover:scale-105 transition-all duration-300 border border-white/20"
        >
          <div className="text-center mb-6 sm:mb-8">
            <img
              src="/logo.png"
              alt="Cartoon Logo"
              width={64}
              height={64}
              className="sm:w-20 sm:h-20 mx-auto mb-3 sm:mb-4 animate-bounce"
            />
            <h2 className="text-xl sm:text-2xl md:text-3xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent mb-2">
              Welcome Back!
            </h2>
            <p className="text-gray-600 text-xs sm:text-sm">
              Four-Color Map Theorem Solver
            </p>
          </div>

          <div className="space-y-4 sm:space-y-6">
            <div className="relative">
              <label
                htmlFor="username"
                className="block text-xs sm:text-sm font-semibold text-gray-700 mb-2"
              >
                Email Address
              </label>
              <input
                type="text"
                id="username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                className="w-full px-3 sm:px-4 py-2.5 sm:py-3 bg-gray-50 border-2 border-gray-200 rounded-lg sm:rounded-xl focus:border-blue-500 focus:ring-4 focus:ring-blue-200 transition-all duration-300 outline-none placeholder-gray-400 text-sm sm:text-base"
                placeholder="Enter your email"
                disabled={isLoading}
              />
            </div>

            <div className="relative">
              <label
                htmlFor="password"
                className="block text-xs sm:text-sm font-semibold text-gray-700 mb-2"
              >
                Password
              </label>
              <input
                type="password"
                id="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full px-3 sm:px-4 py-2.5 sm:py-3 bg-gray-50 border-2 border-gray-200 rounded-lg sm:rounded-xl focus:border-blue-500 focus:ring-4 focus:ring-blue-200 transition-all duration-300 outline-none placeholder-gray-400 text-sm sm:text-base"
                placeholder="Enter your password"
                disabled={isLoading}
              />
            </div>

            {formError && (
              <div className="bg-red-50 border-l-4 border-red-500 p-3 sm:p-4 rounded-lg animate-shake">
                <p className="text-red-700 text-xs sm:text-sm font-medium">
                  {formError}
                </p>
              </div>
            )}

            <button
              type="submit"
              disabled={isLoading}
              className="w-full bg-gradient-to-r from-blue-600 to-purple-600 text-white font-bold py-2.5 sm:py-3 px-4 sm:px-6 rounded-lg sm:rounded-xl hover:from-blue-700 hover:to-purple-700 transform hover:-translate-y-1 hover:shadow-xl transition-all duration-300 focus:outline-none focus:ring-4 focus:ring-blue-200 disabled:opacity-50 disabled:cursor-not-allowed text-sm sm:text-base"
            >
              {isLoading ? "Signing In..." : "Sign In"}
            </button>

            <div className="text-center pt-3 sm:pt-4">
              <p className="text-gray-600 text-xs sm:text-sm">
                Don't have an account?{" "}
                <Link
                  to="/signup"
                  className="text-blue-600 hover:text-purple-600 font-semibold transition-colors duration-300 hover:underline"
                >
                  Create one here
                </Link>
              </p>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
};

export default LoginForm;
