import { createContext } from "react";

interface NotificationContextType {
  showNotification: (
    message: string,
    type: "success" | "error" | "warning" | "info",
    duration?: number
  ) => void;
}

export const NotificationContext = createContext<
  NotificationContextType | undefined
>(undefined);
