import React, { useState } from "react";
import type { ReactNode } from "react";
import Notification from "../components/Notification";
import { NotificationContext } from "./NotificationContextDef";

interface NotificationProviderProps {
  children: ReactNode;
}

const NotificationProvider: React.FC<NotificationProviderProps> = ({
  children,
}) => {
  const [notification, setNotification] = useState<{
    message: string;
    type: "success" | "error" | "warning" | "info";
    isVisible: boolean;
    duration?: number;
  } | null>(null);

  const showNotification = (
    message: string,
    type: "success" | "error" | "warning" | "info",
    duration?: number
  ) => {
    setNotification({ message, type, isVisible: true, duration });
  };

  const hideNotification = () => {
    setNotification((prev) => (prev ? { ...prev, isVisible: false } : null));
  };

  return (
    <NotificationContext.Provider value={{ showNotification }}>
      {children}
      {notification && (
        <Notification
          message={notification.message}
          type={notification.type}
          isVisible={notification.isVisible}
          onClose={hideNotification}
          duration={notification.duration}
        />
      )}
    </NotificationContext.Provider>
  );
};

export default NotificationProvider;
