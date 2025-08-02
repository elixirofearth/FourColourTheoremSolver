import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAppSelector } from "../store/hooks";
import Canvas from "./Canvas";
import {
  handleColorMap,
  handleResetMap,
  handleDownloadMap,
  handleSaveMap,
} from "../utils/sketchHandlers";

export default function HomeContent() {
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(true);
  const [isDesktop, setIsDesktop] = useState(false);
  const { isAuthenticated, user } = useAppSelector((state) => state.auth);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate("/login");
    } else {
      setIsLoading(false);
    }
  }, [isAuthenticated, navigate]);

  useEffect(() => {
    const checkScreenSize = () => {
      setIsDesktop(window.innerWidth >= 1024); // lg breakpoint
    };

    checkScreenSize();
    window.addEventListener("resize", checkScreenSize);

    return () => window.removeEventListener("resize", checkScreenSize);
  }, []);

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-blue-50 to-purple-100 flex items-center justify-center px-4">
        <div className="bg-white/80 backdrop-blur-sm rounded-2xl p-6 sm:p-8 shadow-xl max-w-sm w-full">
          <div className="flex items-center space-x-3">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-purple-600"></div>
            <span className="text-base sm:text-lg font-semibold text-gray-700">
              Loading...
            </span>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 py-4 sm:py-8 px-4">
      <div className="max-w-5xl mx-auto">
        {/* Welcome Header */}
        <div className="text-center mb-6 sm:mb-8">
          <h1 className="text-2xl sm:text-3xl md:text-4xl lg:text-5xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent mb-3 sm:mb-4 px-2">
            Welcome back, {user?.name || "User"}! 🎨
          </h1>
          <p className="text-gray-600 text-base sm:text-lg md:text-xl max-w-2xl mx-auto px-4">
            Ready to create some amazing maps? Use your mouse to draw and let
            our algorithm solve the four-color theorem for you!
          </p>
        </div>

        {/* Main Content Card */}
        <div className="bg-white/80 backdrop-blur-sm rounded-2xl sm:rounded-3xl shadow-2xl p-4 sm:p-6 lg:p-8 border border-white/20">
          {/* Canvas Section - Only show on large screens */}
          <div className="hidden lg:block mb-8">
            <div className="bg-gradient-to-br from-gray-50 to-gray-100 rounded-2xl p-6 border-4 border-dashed border-gray-300">
              <div className="text-center mb-4">
                <h2 className="text-xl font-semibold text-gray-700 mb-2">
                  🖌️ Drawing Canvas
                </h2>
                <p className="text-gray-600 text-sm">
                  Click and drag to draw your map regions
                </p>
              </div>
              <div className="flex justify-center">
                <div className="bg-white rounded-xl shadow-lg p-4 border-2 border-gray-200">
                  <Canvas />
                </div>
              </div>
            </div>
          </div>

          {/* Mobile Message - Only show on smaller screens */}
          <div className="lg:hidden mb-6 sm:mb-8">
            <div className="bg-gradient-to-br from-orange-50 to-yellow-50 rounded-2xl p-6 border-4 border-dashed border-orange-200 text-center">
              <div className="text-4xl sm:text-6xl mb-4">🖥️</div>
              <h2 className="text-lg sm:text-xl font-semibold text-orange-700 mb-3">
                Desktop Required for Drawing
              </h2>
              <p className="text-orange-600 text-sm sm:text-base leading-relaxed">
                The drawing canvas is optimized for desktop use. Please use a
                computer with a mouse or trackpad to create and draw your maps.
              </p>
              <div className="mt-4 bg-orange-100 rounded-xl p-3">
                <p className="text-orange-800 text-xs sm:text-sm font-medium">
                  💡 You can still view your saved maps and manage your profile
                  on mobile!
                </p>
              </div>
            </div>
          </div>

          {/* Action Buttons - Disabled on mobile/tablet, enabled on desktop */}
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4">
            <button
              onClick={handleColorMap}
              disabled={!isDesktop}
              className={`py-3 sm:py-4 px-4 sm:px-6 rounded-xl sm:rounded-2xl transform transition-all duration-300 font-bold text-center group text-sm sm:text-base ${
                isDesktop
                  ? "bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-600 hover:to-teal-700 hover:-translate-y-1 hover:shadow-xl cursor-pointer text-white"
                  : "bg-gradient-to-r from-gray-400 to-gray-500 cursor-not-allowed opacity-60 text-white"
              }`}
            >
              <div className="text-lg sm:text-2xl mb-1 sm:mb-2 group-hover:animate-bounce">
                🎨
              </div>
              <div>Color Map</div>
            </button>

            <button
              onClick={handleResetMap}
              disabled={!isDesktop}
              className={`py-3 sm:py-4 px-4 sm:px-6 rounded-xl sm:rounded-2xl transform transition-all duration-300 font-bold text-center group text-sm sm:text-base ${
                isDesktop
                  ? "bg-gradient-to-r from-red-500 to-pink-600 hover:from-red-600 hover:to-pink-700 hover:-translate-y-1 hover:shadow-xl cursor-pointer text-white"
                  : "bg-gradient-to-r from-gray-400 to-gray-500 cursor-not-allowed opacity-60 text-white"
              }`}
            >
              <div className="text-lg sm:text-2xl mb-1 sm:mb-2 group-hover:animate-spin">
                🔄
              </div>
              <div>Reset Canvas</div>
            </button>

            <button
              onClick={handleDownloadMap}
              disabled={!isDesktop}
              className={`py-3 sm:py-4 px-4 sm:px-6 rounded-xl sm:rounded-2xl transform transition-all duration-300 font-bold text-center group text-sm sm:text-base ${
                isDesktop
                  ? "bg-gradient-to-r from-blue-500 to-indigo-600 hover:from-blue-600 hover:to-indigo-700 hover:-translate-y-1 hover:shadow-xl cursor-pointer text-white"
                  : "bg-gradient-to-r from-gray-400 to-gray-500 cursor-not-allowed opacity-60 text-white"
              }`}
            >
              <div className="text-lg sm:text-2xl mb-1 sm:mb-2 group-hover:animate-pulse">
                ⬇️
              </div>
              <div>Download</div>
            </button>

            <button
              onClick={handleSaveMap}
              disabled={!isDesktop}
              className={`py-3 sm:py-4 px-4 sm:px-6 rounded-xl sm:rounded-2xl transform transition-all duration-300 font-bold text-center group text-sm sm:text-base ${
                isDesktop
                  ? "bg-gradient-to-r from-purple-500 to-violet-600 hover:from-purple-600 hover:to-violet-700 hover:-translate-y-1 hover:shadow-xl cursor-pointer text-white"
                  : "bg-gradient-to-r from-gray-400 to-gray-500 cursor-not-allowed opacity-60 text-white"
              }`}
            >
              <div className="text-lg sm:text-2xl mb-1 sm:mb-2 group-hover:animate-bounce">
                💾
              </div>
              <div>Save Map</div>
            </button>
          </div>

          {/* Instructions */}
          <div className="mt-6 sm:mt-8 bg-gradient-to-r from-blue-50 to-purple-50 rounded-xl sm:rounded-2xl p-4 sm:p-6 border border-blue-200">
            <h3 className="text-base sm:text-lg font-semibold text-gray-800 mb-3 sm:mb-4">
              📝 How to use:
            </h3>
            <div className="grid sm:grid-cols-2 gap-3 sm:gap-4 text-xs sm:text-sm text-gray-700">
              <div className="flex items-start space-x-2">
                <span className="text-blue-500 font-bold text-sm sm:text-base">
                  1.
                </span>
                <span>Draw regions on the canvas using your mouse</span>
              </div>
              <div className="flex items-start space-x-2">
                <span className="text-purple-500 font-bold text-sm sm:text-base">
                  2.
                </span>
                <span>Click "Color Map" to apply the four-color theorem</span>
              </div>
              <div className="flex items-start space-x-2">
                <span className="text-emerald-500 font-bold text-sm sm:text-base">
                  3.
                </span>
                <span>Use "Reset" to clear and start over</span>
              </div>
              <div className="flex items-start space-x-2">
                <span className="text-pink-500 font-bold text-sm sm:text-base">
                  4.
                </span>
                <span>"Save" your masterpiece or "Download" as image</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
