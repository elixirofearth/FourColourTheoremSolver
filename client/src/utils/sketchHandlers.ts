// Extend the Window interface
declare global {
  interface Window {
    handleColorMap: () => void;
    handleResetMap: () => void;
    handleDownloadMap: () => void;
    handleSaveMap: () => Promise<void>;
  }
}

// Export functions that call the window functions set by the Canvas component
export function handleColorMap() {
  if (window.handleColorMap) {
    window.handleColorMap();
  }
}

export function handleResetMap() {
  if (window.handleResetMap) {
    window.handleResetMap();
  }
}

export function handleDownloadMap() {
  if (window.handleDownloadMap) {
    window.handleDownloadMap();
  }
}

export async function handleSaveMap() {
  if (window.handleSaveMap) {
    await window.handleSaveMap();
  }
}
