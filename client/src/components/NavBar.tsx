import { Link, useNavigate } from "react-router-dom";
import { ProfileButton, SignInButton, SignOutButton } from "./Buttons";
import { useEffect, useState } from "react";
import { handleResetMap } from "../utils/sketchHandlers";

export default function NavBar() {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const navigate = useNavigate();

  useEffect(() => {
    const checkAuthStatus = () => {
      const token = localStorage.getItem("token");
      setIsAuthenticated(!!token);
    };

    checkAuthStatus();
    // Add an event listener to check auth status when local storage changes
    window.addEventListener("storage", checkAuthStatus);

    return () => {
      window.removeEventListener("storage", checkAuthStatus);
    };
  }, []);

  const handleSignOut = async () => {
    const apiHost = import.meta.env.VITE_API_GATEWAY_URL;
    if (!apiHost) {
      throw new Error("API host is not defined in the environment variables");
    }

    console.log("Token:", localStorage.getItem("token"));

    try {
      const response = await fetch(`${apiHost}/api/v1/auth/logout`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });

      if (response.ok) {
        console.log("Sign-out successful", response);
        localStorage.removeItem("token");
        localStorage.removeItem("name");
        localStorage.removeItem("userId");
        localStorage.removeItem("email");
        setIsAuthenticated(false);
        handleResetMap();
        navigate("/login"); // Redirect to login page after sign out
      } else {
        console.error("Error during sign-out:", response.statusText);
      }
    } catch (error) {
      console.error("Error during sign-out:", error);
    }
  };

  return (
    <nav className="bg-gradient-to-r from-blue-600 via-purple-600 to-indigo-700 shadow-xl border-b-4 border-white/20">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Logo Section */}
          <div className="flex items-center">
            <Link to="/" className="flex items-center space-x-3 group">
              <img
                src="/logo.png"
                width={40}
                height={40}
                alt="Map Coloring Logo"
                className="rounded-full border-2 border-white/30 group-hover:border-white/60 transform group-hover:rotate-12 transition-all duration-300"
              />
              <span className="text-white font-bold text-lg hidden sm:block">
                ColorMap
              </span>
            </Link>
          </div>

          {/* Title Section */}
          <div className="flex-1 text-center px-4">
            <h1 className="text-white font-bold text-lg sm:text-xl lg:text-2xl drop-shadow-lg">
              🎨 The Best Map Coloring App in the World! 🗺️
            </h1>
          </div>

          {/* Auth Buttons Section */}
          <div className="flex items-center space-x-3">
            {isAuthenticated ? (
              <>
                <ProfileButton />
                <SignOutButton onSignOut={handleSignOut} />
              </>
            ) : (
              <SignInButton />
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}
