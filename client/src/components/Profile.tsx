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

          if (data === null || data === undefined) {
            setMaps([]);
          } else if (Array.isArray(data)) {
            setMaps(data);
          } else {
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
  }, [navigate, isAuthenticated, user]);

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
      <div className="min-h-screen bg-gradient-to-br from-blue-50 to-purple-100 flex items-center justify-center px-4">
        <div className="bg-white/80 backdrop-blur-sm rounded-2xl p-6 sm:p-8 shadow-xl max-w-sm w-full">
          <div className="flex items-center space-x-3">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-purple-600"></div>
            <span className="text-base sm:text-lg font-semibold text-gray-700">
              Loading your profile...
            </span>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-purple-50 to-pink-50 py-4 sm:py-8 px-4">
      <div className="max-w-6xl mx-auto">
        {/* Profile Header */}
        <div className="bg-white/80 backdrop-blur-sm rounded-2xl sm:rounded-3xl shadow-xl p-6 sm:p-8 mb-6 sm:mb-8 border border-white/20">
          <div className="text-center">
            <div className="w-16 h-16 sm:w-20 sm:h-20 md:w-24 md:h-24 bg-gradient-to-r from-purple-500 to-pink-500 rounded-full mx-auto mb-3 sm:mb-4 flex items-center justify-center text-white text-xl sm:text-2xl md:text-3xl font-bold">
              {user?.name?.charAt(0).toUpperCase() || "U"}
            </div>
            <h1 className="text-2xl sm:text-3xl md:text-4xl font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent mb-2">
              Welcome, {user?.name || "User"}!
            </h1>
            <p className="text-gray-600 text-sm sm:text-base md:text-lg">
              {user?.email || "user@example.com"}
            </p>
          </div>
        </div>

        {/* Maps Section */}
        <div className="bg-white/80 backdrop-blur-sm rounded-2xl sm:rounded-3xl shadow-xl p-6 sm:p-8 border border-white/20">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-4 sm:mb-6">
            <h2 className="text-xl sm:text-2xl md:text-3xl font-bold text-gray-800 mb-3 sm:mb-0">
              🗺️ Your Saved Maps
            </h2>
            <div className="bg-gradient-to-r from-blue-500 to-purple-600 text-white px-3 sm:px-4 py-1.5 sm:py-2 rounded-full text-xs sm:text-sm font-semibold self-start sm:self-auto">
              {maps ? maps.length : 0} Maps
            </div>
          </div>

          {error && (
            <div className="bg-red-50 border-l-4 border-red-500 p-3 sm:p-4 rounded-lg mb-4 sm:mb-6">
              <p className="text-red-700 font-medium text-sm sm:text-base">
                {error}
              </p>
            </div>
          )}

          {maps && maps.length > 0 ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-6">
              {maps.map((map) => (
                <div
                  key={map.id}
                  className="bg-gradient-to-br from-white to-gray-50 rounded-xl sm:rounded-2xl p-4 sm:p-6 shadow-lg hover:shadow-xl transform hover:-translate-y-1 sm:hover:-translate-y-2 transition-all duration-300 border border-gray-100"
                >
                  <div className="mb-3 sm:mb-4">
                    <h3 className="text-lg sm:text-xl font-bold text-gray-800 mb-1 sm:mb-2 truncate">
                      {map.name}
                    </h3>
                    <p className="text-xs sm:text-sm text-gray-600">
                      📅 Created: {new Date(map.createdAt).toLocaleDateString()}
                    </p>
                  </div>

                  <div className="flex flex-col sm:flex-row sm:space-x-3 space-y-2 sm:space-y-0">
                    <button
                      onClick={() => handleViewMap(map.id)}
                      className="flex-1 bg-gradient-to-r from-blue-500 to-purple-600 text-white py-2 px-3 sm:px-4 rounded-lg sm:rounded-xl hover:from-blue-600 hover:to-purple-700 transform hover:-translate-y-0.5 transition-all duration-300 font-semibold text-xs sm:text-sm"
                    >
                      👁️ View
                    </button>
                    <button
                      onClick={() => handleDeleteClick(map.id)}
                      className="flex-1 bg-gradient-to-r from-red-500 to-pink-600 text-white py-2 px-3 sm:px-4 rounded-lg sm:rounded-xl hover:from-red-600 hover:to-pink-700 transform hover:-translate-y-0.5 transition-all duration-300 font-semibold text-xs sm:text-sm"
                    >
                      🗑️ Delete
                    </button>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-8 sm:py-12">
              <div className="text-4xl sm:text-6xl mb-3 sm:mb-4">🗺️</div>
              <h3 className="text-lg sm:text-xl font-semibold text-gray-700 mb-2">
                No maps yet!
              </h3>
              <p className="text-gray-500 mb-4 sm:mb-6 text-sm sm:text-base px-4">
                Start creating and coloring maps to see them here.
              </p>
              <button
                onClick={() => navigate("/")}
                className="bg-gradient-to-r from-blue-500 to-purple-600 text-white px-4 sm:px-6 py-2.5 sm:py-3 rounded-lg sm:rounded-xl hover:from-blue-600 hover:to-purple-700 transform hover:-translate-y-1 transition-all duration-300 font-semibold text-sm sm:text-base"
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
