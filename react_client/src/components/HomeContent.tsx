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
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-lg">Loading...</div>
      </div>
    );
  }

  const buttonStyles =
    "px-2 py-1 md:px-4 md:py-2 text-xs md:text-sm font-bold uppercase rounded-full shadow-md transition-all duration-300 ease-in-out transform hover:-translate-y-1 hover:shadow-lg active:translate-y-0 active:shadow-md";

  return (
    <div className="min-h-screen flex flex-col items-center justify-center p-4">
      <div className="w-full max-w-2xl mx-auto">
        <h1 className="text-xl md:text-2xl font-bold text-center mb-4 md:mb-6 text-gray-800">
          <span>Welcome, </span> {userName}!
        </h1>
        <p className="text-center mb-4 md:mb-6 text-sm md:text-base">
          Please draw a map using your mouse 😊!
        </p>

        <div className="flex justify-center mb-4">
          <div className="w-full max-w-md">
            <Canvas />
          </div>
        </div>

        <div className="flex flex-wrap justify-center gap-2 md:gap-4">
          <button
            onClick={handleColorMap}
            className={`${buttonStyles} bg-green-500 text-white border-2 border-green-600 hover:bg-green-600`}
          >
            Color
          </button>
          <button
            onClick={handleResetMap}
            className={`${buttonStyles} bg-red-500 text-white border-2 border-red-600 hover:bg-red-600`}
          >
            Reset
          </button>
          <button
            onClick={handleDownloadMap}
            className={`${buttonStyles} bg-blue-500 text-white border-2 border-blue-600 hover:bg-blue-600`}
          >
            Download
          </button>
          <button
            onClick={handleSaveMap}
            className={`${buttonStyles} bg-purple-500 text-white border-2 border-purple-600 hover:bg-purple-600`}
          >
            Save
          </button>
        </div>
      </div>
    </div>
  );
}
