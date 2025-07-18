import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
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
  const [userName, setUserName] = useState("");

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) {
      navigate("/login");
    } else {
      setIsLoading(false);
      const storedUserName = localStorage.getItem("name");
      setUserName(storedUserName || "User");
    }
  }, [navigate]);

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-blue-50 to-purple-100 flex items-center justify-center">
        <div className="bg-white/80 backdrop-blur-sm rounded-2xl p-8 shadow-xl">
          <div className="flex items-center space-x-3">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-purple-600"></div>
            <span className="text-lg font-semibold text-gray-700">
              Loading...
            </span>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 py-8 px-4">
      <div className="max-w-5xl mx-auto">
        {/* Welcome Header */}
        <div className="text-center mb-8">
          <h1 className="text-3xl md:text-5xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent mb-4">
            Welcome back, {userName}! 🎨
          </h1>
          <p className="text-gray-600 text-lg md:text-xl max-w-2xl mx-auto">
            Ready to create some amazing maps? Use your mouse to draw and let
            our AI solve the four-color theorem for you!
          </p>
        </div>

        {/* Main Content Card */}
        <div className="bg-white/80 backdrop-blur-sm rounded-3xl shadow-2xl p-8 border border-white/20">
          {/* Canvas Section */}
          <div className="mb-8">
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

          {/* Action Buttons */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <button
              onClick={handleColorMap}
              className="bg-gradient-to-r from-emerald-500 to-teal-600 text-white py-4 px-6 rounded-2xl hover:from-emerald-600 hover:to-teal-700 transform hover:-translate-y-2 hover:shadow-xl transition-all duration-300 font-bold text-center group"
            >
              <div className="text-2xl mb-2 group-hover:animate-bounce">🎨</div>
              <div>Color Map</div>
            </button>

            <button
              onClick={handleResetMap}
              className="bg-gradient-to-r from-red-500 to-pink-600 text-white py-4 px-6 rounded-2xl hover:from-red-600 hover:to-pink-700 transform hover:-translate-y-2 hover:shadow-xl transition-all duration-300 font-bold text-center group"
            >
              <div className="text-2xl mb-2 group-hover:animate-spin">🔄</div>
              <div>Reset Canvas</div>
            </button>

            <button
              onClick={handleDownloadMap}
              className="bg-gradient-to-r from-blue-500 to-indigo-600 text-white py-4 px-6 rounded-2xl hover:from-blue-600 hover:to-indigo-700 transform hover:-translate-y-2 hover:shadow-xl transition-all duration-300 font-bold text-center group"
            >
              <div className="text-2xl mb-2 group-hover:animate-pulse">⬇️</div>
              <div>Download</div>
            </button>

            <button
              onClick={handleSaveMap}
              className="bg-gradient-to-r from-purple-500 to-violet-600 text-white py-4 px-6 rounded-2xl hover:from-purple-600 hover:to-violet-700 transform hover:-translate-y-2 hover:shadow-xl transition-all duration-300 font-bold text-center group"
            >
              <div className="text-2xl mb-2 group-hover:animate-bounce">💾</div>
              <div>Save Map</div>
            </button>
          </div>

          {/* Instructions */}
          <div className="mt-8 bg-gradient-to-r from-blue-50 to-purple-50 rounded-2xl p-6 border border-blue-200">
            <h3 className="text-lg font-semibold text-gray-800 mb-4">
              📝 How to use:
            </h3>
            <div className="grid md:grid-cols-2 gap-4 text-sm text-gray-700">
              <div className="flex items-start space-x-2">
                <span className="text-blue-500 font-bold">1.</span>
                <span>Draw regions on the canvas using your mouse</span>
              </div>
              <div className="flex items-start space-x-2">
                <span className="text-purple-500 font-bold">2.</span>
                <span>Click "Color Map" to apply the four-color theorem</span>
              </div>
              <div className="flex items-start space-x-2">
                <span className="text-emerald-500 font-bold">3.</span>
                <span>Use "Reset" to clear and start over</span>
              </div>
              <div className="flex items-start space-x-2">
                <span className="text-pink-500 font-bold">4.</span>
                <span>"Save" your masterpiece or "Download" as image</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
