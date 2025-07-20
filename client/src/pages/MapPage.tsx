import { useParams, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { useAppSelector } from "../store/hooks";
import { authInterceptor } from "../utils/authInterceptor";

interface MapData {
  id: string;
  name: string;
  imageData?: string;
  createdAt: string;
  width: number;
  height: number;
}

export default function MapPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { token, isAuthenticated } = useAppSelector((state) => state.auth);
  const [mapData, setMapData] = useState<MapData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!isAuthenticated || !token) {
      navigate("/login");
      return;
    }

    const fetchMap = async () => {
      const apiHost = import.meta.env.VITE_API_GATEWAY_URL;
      if (!apiHost) {
        setError("API host not configured");
        setLoading(false);
        return;
      }

      try {
        const response = await authInterceptor.makeAuthenticatedRequest(
          `${apiHost}/api/v1/maps/${id}`
        );

        if (response.ok) {
          const data = await response.json();
          setMapData(data);
        } else {
          setError("Failed to load map");
        }
      } catch {
        setError("Error loading map");
      } finally {
        setLoading(false);
      }
    };

    if (id) {
      fetchMap();
    }
  }, [id, navigate, isAuthenticated, token]);

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-blue-50 to-purple-100 flex items-center justify-center">
        <div className="bg-white/80 backdrop-blur-sm rounded-2xl p-8 shadow-xl">
          <div className="flex items-center space-x-3">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-purple-600"></div>
            <span className="text-lg font-semibold text-gray-700">
              Loading map...
            </span>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-red-50 to-pink-100 flex items-center justify-center">
        <div className="bg-white/80 backdrop-blur-sm rounded-2xl p-8 shadow-xl text-center">
          <div className="text-6xl mb-4">😞</div>
          <h2 className="text-2xl font-bold text-red-600 mb-2">Oops!</h2>
          <p className="text-red-500 mb-6">{error}</p>
          <button
            onClick={() => navigate("/profile")}
            className="bg-gradient-to-r from-red-500 to-pink-600 text-white py-2 px-6 rounded-xl hover:from-red-600 hover:to-pink-700 transform hover:-translate-y-1 hover:shadow-lg transition-all duration-300 font-semibold"
          >
            ← Back to Profile
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-purple-50 to-pink-50 py-8 px-4">
      <div className="max-w-4xl mx-auto">
        <div className="bg-white/80 backdrop-blur-sm rounded-3xl shadow-xl p-8 border border-white/20">
          {/* Header */}
          <div className="text-center mb-8">
            <h1 className="text-3xl md:text-4xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent mb-4">
              🗺️ {mapData?.name || `Map ${id}`}
            </h1>
            <div className="flex flex-wrap justify-center gap-4 text-sm text-gray-600">
              <div className="bg-gradient-to-r from-blue-100 to-purple-100 px-4 py-2 rounded-full">
                📅 Created:{" "}
                {mapData?.createdAt
                  ? new Date(mapData.createdAt).toLocaleDateString()
                  : "Unknown"}
              </div>
              <div className="bg-gradient-to-r from-purple-100 to-pink-100 px-4 py-2 rounded-full">
                📐 Dimensions: {mapData?.width}×{mapData?.height}
              </div>
            </div>
          </div>

          {/* Map Image */}
          {mapData?.imageData && (
            <div className="mb-8">
              <div className="bg-gradient-to-r from-gray-100 to-gray-200 p-6 rounded-2xl">
                <img
                  src={mapData.imageData}
                  alt={mapData.name}
                  className="max-w-full h-auto rounded-xl border-4 border-white shadow-lg mx-auto"
                />
              </div>
            </div>
          )}

          {/* Actions */}
          <div className="flex flex-wrap justify-center gap-4">
            <button
              onClick={() => navigate("/profile")}
              className="bg-gradient-to-r from-blue-500 to-purple-600 text-white py-3 px-8 rounded-xl hover:from-blue-600 hover:to-purple-700 transform hover:-translate-y-1 hover:shadow-lg transition-all duration-300 font-semibold"
            >
              ← Back to Profile
            </button>
            <button
              onClick={() => navigate("/")}
              className="bg-gradient-to-r from-emerald-500 to-teal-600 text-white py-3 px-8 rounded-xl hover:from-emerald-600 hover:to-teal-700 transform hover:-translate-y-1 hover:shadow-lg transition-all duration-300 font-semibold"
            >
              🎨 Create New Map
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
