import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";

const LoginForm: React.FC = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");

    const apiHost = import.meta.env.VITE_API_GATEWAY_URL;

    if (!apiHost) {
      throw new Error("API host is not defined in the environment variables");
    }

    try {
      const response = await fetch(`${apiHost}/api/v1/auth/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email: username, password: password }),
      });

      if (response.ok) {
        const data = await response.json();
        localStorage.setItem("token", data.token);
        localStorage.setItem("name", data.name);
        localStorage.setItem("userId", data.user_id);
        localStorage.setItem("email", data.email);
        console.log("Sign-in successful:", data);
        navigate("/");
      } else {
        setError("Invalid username or password");
      }
    } catch (error) {
      console.error("Error during sign-in:", error);
      setError("An error occurred during sign-in");
    }
  };

  return (
    <div className="flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <form
          onSubmit={handleSubmit}
          className="bg-white/95 backdrop-blur-sm rounded-3xl shadow-2xl p-8 transform hover:scale-105 transition-all duration-300 border border-white/20"
        >
          <div className="text-center mb-8">
            <img
              src="/logo.png"
              alt="Cartoon Logo"
              width={80}
              height={80}
              className="mx-auto mb-4 animate-bounce"
            />
            <h2 className="text-2xl md:text-3xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent mb-2">
              Welcome Back!
            </h2>
            <p className="text-gray-600 text-sm">
              Four-Color Map Theorem Solver
            </p>
          </div>

          <div className="space-y-6">
            <div className="relative">
              <label
                htmlFor="username"
                className="block text-sm font-semibold text-gray-700 mb-2"
              >
                Email Address
              </label>
              <input
                type="text"
                id="username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                className="w-full px-4 py-3 bg-gray-50 border-2 border-gray-200 rounded-xl focus:border-blue-500 focus:ring-4 focus:ring-blue-200 transition-all duration-300 outline-none placeholder-gray-400"
                placeholder="Enter your email"
                required
              />
            </div>

            <div className="relative">
              <label
                htmlFor="password"
                className="block text-sm font-semibold text-gray-700 mb-2"
              >
                Password
              </label>
              <input
                type="password"
                id="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full px-4 py-3 bg-gray-50 border-2 border-gray-200 rounded-xl focus:border-blue-500 focus:ring-4 focus:ring-blue-200 transition-all duration-300 outline-none placeholder-gray-400"
                placeholder="Enter your password"
                required
              />
            </div>

            {error && (
              <div className="bg-red-50 border-l-4 border-red-500 p-4 rounded-lg animate-shake">
                <p className="text-red-700 text-sm font-medium">{error}</p>
              </div>
            )}

            <button
              type="submit"
              className="w-full bg-gradient-to-r from-blue-600 to-purple-600 text-white font-bold py-3 px-6 rounded-xl hover:from-blue-700 hover:to-purple-700 transform hover:-translate-y-1 hover:shadow-xl transition-all duration-300 focus:outline-none focus:ring-4 focus:ring-blue-200"
            >
              Sign In
            </button>

            <div className="text-center pt-4">
              <p className="text-gray-600 text-sm">
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
