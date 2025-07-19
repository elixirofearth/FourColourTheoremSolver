import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";

const SignUpForm: React.FC = () => {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
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
      const response = await fetch(`${apiHost}/api/v1/auth/register`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
        },
        body: JSON.stringify({ name, email, password }),
      });

      if (response.ok) {
        const data = await response.json();
        console.log("Sign-up successful:", data);
        localStorage.setItem("token", data.token);
        localStorage.setItem("name", data.name);
        localStorage.setItem("userId", data.user_id);
        localStorage.setItem("email", data.email);
        navigate("/");
      } else {
        const data = await response.json();
        setError(data.error);
      }
    } catch (error) {
      console.error("Error during sign-up:", error);
      setError("An error occurred during sign-up");
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
            <h2 className="text-2xl md:text-3xl font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent mb-2">
              Join Us Today!
            </h2>
            <p className="text-gray-600 text-sm">
              Create your account and start coloring
            </p>
          </div>

          <div className="space-y-6">
            <div className="relative">
              <label
                htmlFor="name"
                className="block text-sm font-semibold text-gray-700 mb-2"
              >
                Full Name
              </label>
              <input
                type="text"
                id="name"
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="w-full px-4 py-3 bg-gray-50 border-2 border-gray-200 rounded-xl focus:border-purple-500 focus:ring-4 focus:ring-purple-200 transition-all duration-300 outline-none placeholder-gray-400"
                placeholder="Enter your full name"
                required
              />
            </div>

            <div className="relative">
              <label
                htmlFor="email"
                className="block text-sm font-semibold text-gray-700 mb-2"
              >
                Email Address
              </label>
              <input
                type="email"
                id="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full px-4 py-3 bg-gray-50 border-2 border-gray-200 rounded-xl focus:border-purple-500 focus:ring-4 focus:ring-purple-200 transition-all duration-300 outline-none placeholder-gray-400"
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
                className="w-full px-4 py-3 bg-gray-50 border-2 border-gray-200 rounded-xl focus:border-purple-500 focus:ring-4 focus:ring-purple-200 transition-all duration-300 outline-none placeholder-gray-400"
                placeholder="Create a secure password"
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
              className="w-full bg-gradient-to-r from-purple-600 to-pink-600 text-white font-bold py-3 px-6 rounded-xl hover:from-purple-700 hover:to-pink-700 transform hover:-translate-y-1 hover:shadow-xl transition-all duration-300 focus:outline-none focus:ring-4 focus:ring-purple-200"
            >
              Create Account
            </button>

            <div className="text-center pt-4">
              <p className="text-gray-600 text-sm">
                Already have an account?{" "}
                <Link
                  to="/login"
                  className="text-purple-600 hover:text-pink-600 font-semibold transition-colors duration-300 hover:underline"
                >
                  Sign in here
                </Link>
              </p>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
};

export default SignUpForm;
