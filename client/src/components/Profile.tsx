import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useNotification } from "../hooks/useNotification";
import { useAppSelector } from "../store/hooks";
import { authInterceptor } from "../utils/authInterceptor";
import ConfirmationModal from "./ConfirmationModal";

interface Map {
  id: string;
  name: string;
  createdAt: string;
}

export default function Profile() {
  const navigate = useNavigate();
  const { showNotification } = useNotification();
  const { token, user, isAuthenticated } = useAppSelector(
    (state) => state.auth
  );
  const [maps, setMaps] = useState<Map[] | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [mapToDelete, setMapToDelete] = useState<string | null>(null);

  useEffect(() => {
    if (!isAuthenticated || !token || !user) {
      navigate("/login");
      return;
    }

    const fetchUserData = async () => {
      const apiHost = import.meta.env.VITE_API_GATEWAY_URL;
      if (!apiHost) {
        throw new Error("API host is not defined");
      }

      try {
        const response = await authInterceptor.makeAuthenticatedRequest(
          `${apiHost}/api/v1/maps?userId=${user.id}`
        );

        if (response.ok) {
          const data = await response.json();
          console.log("Maps fetched:", data);

          if (data === null || data === undefined) {
            console.log("No data received, setting maps to empty array");
            setMaps([]);
          } else if (Array.isArray(data)) {
            console.log("Data is array, setting maps:", data);
            setMaps(data);
          } else {
            console.log("Data is not array, converting to array:", data);
            setMaps([data]);
          }
        } else {
          console.error("Error fetching maps:", response.statusText);
          setError("Error fetching maps");
          setMaps([]);
        }
      } catch (error) {
        console.error("Error fetching user data:", error);

        // Handle authentication errors
        if (error instanceof Error) {
          if (
            error.message.includes("Authentication failed") ||
            error.message.includes("Token expired") ||
            error.message.includes("No valid token available")
          ) {
            showNotification("Session expired. Please login again.", "error");
            navigate("/login");
            return;
          }
        }

        setError("Error fetching user data");
        setMaps([]);
      } finally {
        setIsLoading(false);
      }
    };

    fetchUserData();
  }, [navigate, isAuthenticated, user, showNotification, token]);

  const handleDeleteClick = (mapId: string) => {
    setMapToDelete(mapId);
    setShowDeleteModal(true);
  };

  const handleDeleteConfirm = async () => {
    if (!mapToDelete || !token) return;

    const apiHost = import.meta.env.VITE_API_GATEWAY_URL;
    if (!apiHost) {
      showNotification("Missing API configuration", "error");
      return;
    }

    try {
      const response = await authInterceptor.makeAuthenticatedRequest(
        `${apiHost}/api/v1/maps/${mapToDelete}`,
        {
          method: "DELETE",
        }
      );

      if (response.ok) {
        setMaps((prevMaps) =>
          prevMaps ? prevMaps.filter((map) => map.id !== mapToDelete) : []
        );
        showNotification("Map deleted successfully!", "success");
        console.log("Map deleted successfully");
      } else {
        console.error("Error deleting map:", response.statusText);
        showNotification("Error deleting map", "error");
      }
    } catch (error) {
      console.error("Error deleting map:", error);

      // Handle authentication errors
      if (error instanceof Error) {
        if (
          error.message.includes("Authentication failed") ||
          error.message.includes("Token expired") ||
          error.message.includes("No valid token available")
        ) {
          showNotification("Session expired. Please login again.", "error");
          navigate("/login");
          return;
        }
      }

      showNotification("Error deleting map", "error");
    } finally {
      setShowDeleteModal(false);
      setMapToDelete(null);
    }
  };

  const handleViewMap = (mapId: string) => {
    navigate(`/maps/${mapId}`);
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-blue-50 to-purple-100 flex items-center justify-center">
        <div className="bg-white/80 backdrop-blur-sm rounded-2xl p-8 shadow-xl">
          <div className="flex items-center space-x-3">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-purple-600"></div>
            <span className="text-lg font-semibold text-gray-700">
              Loading your profile...
            </span>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-purple-50 to-pink-50 py-8 px-4">
      <div className="max-w-6xl mx-auto">
        {/* Profile Header */}
        <div className="bg-white/80 backdrop-blur-sm rounded-3xl shadow-xl p-8 mb-8 border border-white/20">
          <div className="text-center">
            <div className="w-24 h-24 bg-gradient-to-r from-purple-500 to-pink-500 rounded-full mx-auto mb-4 flex items-center justify-center text-white text-3xl font-bold">
              {user?.name?.charAt(0).toUpperCase() || "U"}
            </div>
            <h1 className="text-3xl md:text-4xl font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent mb-2">
              Welcome, {user?.name || "User"}!
            </h1>
            <p className="text-gray-600 text-lg">
              {user?.email || "user@example.com"}
            </p>
          </div>
        </div>

        {/* Maps Section */}
        <div className="bg-white/80 backdrop-blur-sm rounded-3xl shadow-xl p-8 border border-white/20">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-2xl md:text-3xl font-bold text-gray-800">
              🗺️ Your Saved Maps
            </h2>
            <div className="bg-gradient-to-r from-blue-500 to-purple-600 text-white px-4 py-2 rounded-full text-sm font-semibold">
              {maps ? maps.length : 0} Maps
            </div>
          </div>

          {error && (
            <div className="bg-red-50 border-l-4 border-red-500 p-4 rounded-lg mb-6">
              <p className="text-red-700 font-medium">{error}</p>
            </div>
          )}

          {maps && maps.length > 0 ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {maps.map((map) => (
                <div
                  key={map.id}
                  className="bg-gradient-to-br from-white to-gray-50 rounded-2xl p-6 shadow-lg hover:shadow-xl transform hover:-translate-y-2 transition-all duration-300 border border-gray-100"
                >
                  <div className="mb-4">
                    <h3 className="text-xl font-bold text-gray-800 mb-2">
                      {map.name}
                    </h3>
                    <p className="text-sm text-gray-600">
                      📅 Created: {new Date(map.createdAt).toLocaleDateString()}
                    </p>
                  </div>

                  <div className="flex space-x-3">
                    <button
                      onClick={() => handleViewMap(map.id)}
                      className="flex-1 bg-gradient-to-r from-blue-500 to-purple-600 text-white py-2 px-4 rounded-xl hover:from-blue-600 hover:to-purple-700 transform hover:-translate-y-0.5 transition-all duration-300 font-semibold text-sm"
                    >
                      👁️ View
                    </button>
                    <button
                      onClick={() => handleDeleteClick(map.id)}
                      className="flex-1 bg-gradient-to-r from-red-500 to-pink-600 text-white py-2 px-4 rounded-xl hover:from-red-600 hover:to-pink-700 transform hover:-translate-y-0.5 transition-all duration-300 font-semibold text-sm"
                    >
                      🗑️ Delete
                    </button>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-12">
              <div className="text-6xl mb-4">🗺️</div>
              <h3 className="text-xl font-semibold text-gray-700 mb-2">
                No maps yet!
              </h3>
              <p className="text-gray-500 mb-6">
                Start creating and coloring maps to see them here.
              </p>
              <button
                onClick={() => navigate("/")}
                className="bg-gradient-to-r from-blue-500 to-purple-600 text-white px-6 py-3 rounded-xl hover:from-blue-600 hover:to-purple-700 transform hover:-translate-y-1 transition-all duration-300 font-semibold"
              >
                🎨 Create Your First Map
              </button>
            </div>
          )}
        </div>

        {/* Confirmation Modal */}
        <ConfirmationModal
          isOpen={showDeleteModal}
          onClose={() => setShowDeleteModal(false)}
          onConfirm={handleDeleteConfirm}
          title="Delete Map"
          message="Are you sure you want to delete this map? This action cannot be undone."
          confirmText="Delete"
          cancelText="Cancel"
          type="danger"
        />
      </div>
    </div>
  );
}
