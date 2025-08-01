import { Link, useNavigate } from "react-router-dom";
import { ProfileButton, SignInButton, SignOutButton } from "./Buttons";
import { useAppDispatch, useAppSelector } from "../store/hooks";
import { logoutUser } from "../store/authSlice";
import { useNotification } from "../hooks/useNotification";
import { handleResetMap } from "../utils/sketchHandlers";

export default function NavBar() {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const { showNotification } = useNotification();
  const { isAuthenticated } = useAppSelector((state) => state.auth);

  const handleSignOut = async () => {
    try {
      await dispatch(logoutUser());
      handleResetMap();
      navigate("/login");
    } catch (error) {
      console.error("Error during sign-out:", error);
      showNotification("Error during logout", "error");
    }
  };

  return (
    <nav className="bg-gradient-to-r from-blue-600 via-purple-600 to-indigo-700 shadow-xl border-b-4 border-white/20">
      <div className="max-w-7xl mx-auto px-3 sm:px-4 lg:px-8">
        <div className="flex justify-between items-center h-14 sm:h-16">
          {/* Logo Section */}
          <div className="flex items-center flex-shrink-0">
            <Link
              to="/"
              className="flex items-center space-x-2 sm:space-x-3 group"
            >
              <img
                src="/logo.png"
                width={32}
                height={32}
                alt="Map Coloring Logo"
                className="sm:w-10 sm:h-10 transform group-hover:rotate-12 transition-all duration-300"
              />
              <span className="text-white font-bold text-sm sm:text-lg hidden xs:block">
                ColorMap
              </span>
            </Link>
          </div>

          {/* Title Section */}
          <div className="flex-1 text-center px-2 sm:px-4 min-w-0">
            <h1 className="text-white font-bold text-xs xs:text-sm sm:text-lg md:text-xl lg:text-2xl drop-shadow-lg truncate">
              <span className="hidden sm:inline">
                🎨 The Best Map Coloring App in the World! 🗺️
              </span>
              <span className="sm:hidden">🎨 ColorMap 🗺️</span>
            </h1>
          </div>

          {/* Auth Buttons Section */}
          <div className="flex items-center space-x-2 sm:space-x-3 flex-shrink-0">
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
