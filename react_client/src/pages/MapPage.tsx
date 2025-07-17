import { useParams, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";

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
  const [mapData, setMapData] = useState<MapData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchMap = async () => {
      const token = localStorage.getItem("token");
      if (!token) {
        navigate("/login");
        return;
      }

      const apiHost = import.meta.env.VITE_API_GATEWAY_URL;
      if (!apiHost) {
        setError("API host not configured");
        setLoading(false);
        return;
      }

      try {
        const response = await fetch(`${apiHost}/api/v1/maps/${id}`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

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
  }, [id, navigate]);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-lg">Loading map...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-red-500">{error}</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl mx-auto">
        <div className="bg-white rounded-lg shadow p-6">
          <h1 className="text-2xl font-bold mb-4">
            {mapData?.name || `Map ${id}`}
          </h1>

          {mapData?.imageData && (
            <div className="mb-4">
              <img
                src={mapData.imageData}
                alt={mapData.name}
                className="max-w-full h-auto rounded-lg border"
              />
            </div>
          )}

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
            <div>
              <strong>Created:</strong>{" "}
              {mapData?.createdAt
                ? new Date(mapData.createdAt).toLocaleString()
                : "Unknown"}
            </div>
            <div>
              <strong>Dimensions:</strong> {mapData?.width}x{mapData?.height}
            </div>
          </div>

          <div className="mt-6">
            <button
              onClick={() => navigate("/profile")}
              className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 transition-colors"
            >
              Back to Profile
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
