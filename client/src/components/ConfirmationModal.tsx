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
    <div className="fixed inset-0 z-50 overflow-y-auto">
      {/* Backdrop */}
      <div
        className="fixed inset-0 bg-black/50 backdrop-blur-sm transition-opacity duration-300"
        onClick={onClose}
      />

      {/* Modal */}
      <div className="flex min-h-full items-center justify-center p-4">
        <div className="relative bg-white rounded-3xl shadow-2xl max-w-md w-full mx-auto transform transition-all duration-300 scale-100 border border-gray-100">
          {/* Header */}
          <div className="px-6 pt-6 pb-4">
            <div className="flex items-center space-x-3">
              <div className="text-3xl">{getIcon()}</div>
              <h3 className="text-xl font-bold text-gray-900">{title}</h3>
            </div>
          </div>

          {/* Content */}
          <div className="px-6 pb-6">
            <p className="text-gray-600 leading-relaxed mb-6">{message}</p>

            {/* Action Buttons */}
            <div className="flex space-x-3">
              <button
                onClick={onClose}
                className="flex-1 bg-gray-100 text-gray-700 py-3 px-4 rounded-xl hover:bg-gray-200 transition-all duration-300 font-semibold focus:outline-none focus:ring-4 focus:ring-gray-200"
              >
                {cancelText}
              </button>
              <button
                onClick={() => {
                  onConfirm();
                  onClose();
                }}
                className={`flex-1 bg-gradient-to-r ${getConfirmButtonColors()} text-white py-3 px-4 rounded-xl transition-all duration-300 font-semibold focus:outline-none focus:ring-4 transform hover:-translate-y-0.5 hover:shadow-lg`}
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
