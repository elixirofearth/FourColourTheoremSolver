import React from "react";

export interface ConfirmationModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  type?: "danger" | "warning" | "info";
}

const ConfirmationModal: React.FC<ConfirmationModalProps> = ({
  isOpen,
  onClose,
  onConfirm,
  title,
  message,
  confirmText = "Confirm",
  cancelText = "Cancel",
  type = "danger",
}) => {
  if (!isOpen) return null;

  const getIcon = () => {
    switch (type) {
      case "danger":
        return "🗑️";
      case "warning":
        return "⚠️";
      case "info":
        return "ℹ️";
      default:
        return "❓";
    }
  };

  const getConfirmButtonColors = () => {
    switch (type) {
      case "danger":
        return "from-red-500 to-rose-600 hover:from-red-600 hover:to-rose-700 focus:ring-red-200";
      case "warning":
        return "from-yellow-500 to-orange-600 hover:from-yellow-600 hover:to-orange-700 focus:ring-yellow-200";
      case "info":
        return "from-blue-500 to-indigo-600 hover:from-blue-600 hover:to-indigo-700 focus:ring-blue-200";
      default:
        return "from-red-500 to-rose-600 hover:from-red-600 hover:to-rose-700 focus:ring-red-200";
    }
  };

  return (
    <div
      className="fixed inset-0 z-50 overflow-y-auto"
      data-testid="confirmation-modal"
    >
      {/* Backdrop */}
      <div
        className="fixed inset-0 bg-black/50 backdrop-blur-sm transition-opacity duration-300"
        onClick={onClose}
      />

      {/* Modal */}
      <div className="flex min-h-full items-center justify-center p-3 sm:p-4">
        <div className="relative bg-white rounded-2xl sm:rounded-3xl shadow-2xl max-w-sm sm:max-w-md w-full mx-auto transform transition-all duration-300 scale-100 border border-gray-100">
          {/* Header */}
          <div className="px-4 sm:px-6 pt-4 sm:pt-6 pb-3 sm:pb-4">
            <div className="flex items-center space-x-2 sm:space-x-3">
              <div className="text-2xl sm:text-3xl">{getIcon()}</div>
              <h3 className="text-lg sm:text-xl font-bold text-gray-900">
                {title}
              </h3>
            </div>
          </div>

          {/* Content */}
          <div className="px-4 sm:px-6 pb-4 sm:pb-6">
            <p className="text-gray-600 leading-relaxed mb-4 sm:mb-6 text-sm sm:text-base">
              {message}
            </p>

            {/* Action Buttons */}
            <div className="flex flex-col sm:flex-row space-y-2 sm:space-y-0 sm:space-x-3">
              <button
                onClick={onClose}
                className="flex-1 bg-gray-100 text-gray-700 py-2.5 sm:py-3 px-3 sm:px-4 rounded-lg sm:rounded-xl hover:bg-gray-200 transition-all duration-300 font-semibold focus:outline-none focus:ring-4 focus:ring-gray-200 text-sm sm:text-base"
              >
                {cancelText}
              </button>
              <button
                onClick={() => {
                  onConfirm();
                  onClose();
                }}
                className={`flex-1 bg-gradient-to-r ${getConfirmButtonColors()} text-white py-2.5 sm:py-3 px-3 sm:px-4 rounded-lg sm:rounded-xl transition-all duration-300 font-semibold focus:outline-none focus:ring-4 transform hover:-translate-y-0.5 hover:shadow-lg text-sm sm:text-base`}
                data-testid="confirm-button"
              >
                {confirmText}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ConfirmationModal;
