import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import styles from "../styles/Profile.module.css";

interface Map {
  id: string;
  name: string;
  createdAt: string;
}

export default function Profile() {
  const navigate = useNavigate();
  const [userName, setUserName] = useState("");
  const [userEmail, setUserEmail] = useState("");
  const [maps, setMaps] = useState<Map[] | null>(null); // Initialize as null
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchUserData = async () => {
      const token = localStorage.getItem("token");
      const userId = localStorage.getItem("userId");

      if (!token || !userId) {
        navigate("/login");
        return;
      }

      const apiHost = import.meta.env.VITE_API_GATEWAY_URL;
      if (!apiHost) {
        throw new Error("API host is not defined");
      }

      const storedEmail = localStorage.getItem("email");
      setUserEmail(storedEmail || "user@gmail.com");

      const storedUserName = localStorage.getItem("name");
      setUserName(storedUserName || "User");

      try {
        const response = await fetch(
          `${apiHost}/api/v1/maps?userId=${userId}`,
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
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
          setMaps([]); // Set to empty array on error
        }
      } catch (error) {
        console.error("Error fetching user data:", error);
        setError("Error fetching user data");
        setMaps([]); // Set to empty array on error
      } finally {
        setIsLoading(false);
      }
    };

    fetchUserData();
  }, [navigate]);

  const handleDeleteMap = async (mapId: string) => {
    const token = localStorage.getItem("token");
    const apiHost = import.meta.env.VITE_API_GATEWAY_URL;

    if (!token || !apiHost) {
      setError("Missing authentication or API configuration");
      return;
    }

    try {
      const response = await fetch(`${apiHost}/api/v1/maps/${mapId}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (response.ok) {
        // Remove the deleted map from the state
        setMaps((prevMaps) =>
          prevMaps ? prevMaps.filter((map) => map.id !== mapId) : []
        );
        console.log("Map deleted successfully");
      } else {
        console.error("Error deleting map:", response.statusText);
        setError("Error deleting map");
      }
    } catch (error) {
      console.error("Error deleting map:", error);
      setError("Error deleting map");
    }
  };

  const handleViewMap = (mapId: string) => {
    navigate(`/maps/${mapId}`);
  };

  if (isLoading) {
    return (
      <div className={styles.loadingContainer}>
        <div className="text-lg">Loading...</div>
      </div>
    );
  }

  return (
    <div className={styles.profileContainer}>
      <div className={styles.profileHeader}>
        <h1 className="text-xl md:text-2xl lg:text-3xl">User Profile</h1>
        <div className={styles.userInfo}>
          <p className="text-sm md:text-base">
            <strong>Name:</strong> {userName}
          </p>
          <p className="text-sm md:text-base">
            <strong>Email:</strong> {userEmail}
          </p>
        </div>
      </div>

      <div className={styles.mapsSection}>
        <h2 className="text-lg md:text-xl lg:text-2xl">Your Saved Maps</h2>
        {error && <p className={`${styles.error} text-sm`}>{error}</p>}

        {maps && maps.length > 0 ? (
          <div className={styles.mapsList}>
            {maps.map((map) => (
              <div key={map.id} className={styles.mapCard}>
                <h3 className="text-sm md:text-base">{map.name}</h3>
                <p className="text-xs md:text-sm">
                  Created: {new Date(map.createdAt).toLocaleString()}
                </p>
                <div className={styles.mapActions}>
                  <button
                    onClick={() => handleViewMap(map.id)}
                    className={`${styles.viewButton} text-xs md:text-sm`}
                  >
                    View
                  </button>
                  <button
                    onClick={() => handleDeleteMap(map.id)}
                    className={`${styles.deleteButton} text-xs md:text-sm`}
                  >
                    Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <p className="text-sm md:text-base">No saved maps found.</p>
        )}
      </div>
    </div>
  );
}
