import React, { useEffect, useState } from "react";

export interface NotificationProps {
  message: string;
  type: "success" | "error" | "warning" | "info";
  isVisible: boolean;
  onClose: () => void;
  duration?: number;
}

const Notification: React.FC<NotificationProps> = ({
  message,
  type,
  isVisible,
  onClose,
  duration = 4000,
}) => {
  const [isAnimating, setIsAnimating] = useState(false);

  useEffect(() => {
    if (isVisible) {
      setIsAnimating(true);
      const timer = setTimeout(() => {
        setIsAnimating(false);
        setTimeout(onClose, 300); // Wait for animation to complete
      }, duration);

      return () => clearTimeout(timer);
    }
  }, [isVisible, duration, onClose]);

  if (!isVisible && !isAnimating) return null;

  const getIcon = () => {
    switch (type) {
      case "success":
        return "✅";
      case "error":
        return "❌";
      case "warning":
        return "⚠️";
      case "info":
        return "ℹ️";
      default:
        return "ℹ️";
    }
  };

  const getColors = () => {
    switch (type) {
      case "success":
        return "from-green-500 to-emerald-600 border-green-200";
      case "error":
        return "from-red-500 to-rose-600 border-red-200";
      case "warning":
        return "from-yellow-500 to-orange-600 border-yellow-200";
      case "info":
        return "from-blue-500 to-indigo-600 border-blue-200";
      default:
        return "from-blue-500 to-indigo-600 border-blue-200";
    }
  };

  return (
    <div className="fixed top-4 right-4 z-50" data-testid="notification">
      <div
        className={`
          bg-gradient-to-r ${getColors()} text-white px-6 py-4 rounded-2xl shadow-2xl border-2
          transform transition-all duration-300 ease-in-out backdrop-blur-sm
          ${
            isAnimating
              ? "translate-x-0 opacity-100 scale-100"
              : "translate-x-full opacity-0 scale-95"
          }
          max-w-sm min-w-[300px]
        `}
      >
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3 flex-1">
            <span className="text-xl flex-shrink-0">{getIcon()}</span>
            <p className="font-semibold text-white leading-relaxed">
              {message}
            </p>
          </div>
          <button
            onClick={() => {
              setIsAnimating(false);
              setTimeout(onClose, 300);
            }}
            className="text-white/80 hover:text-white transition-colors duration-200 text-xl leading-none ml-3 flex-shrink-0"
          >
            ×
          </button>
        </div>
      </div>
    </div>
  );
};

export default Notification;
