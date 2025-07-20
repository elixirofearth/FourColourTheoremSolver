import React, { useRef, useEffect, useState } from "react";
import { useNotification } from "../hooks/useNotification";
import { useAppSelector } from "../store/hooks";
import { authInterceptor } from "../utils/authInterceptor";
import { useNavigate } from "react-router-dom";

interface LinePoint {
  x: number;
  y: number;
}

// Extend the Window interface
declare global {
  interface Window {
    handleColorMap: () => void;
    handleResetMap: () => void;
    handleDownloadMap: () => void;
    handleSaveMap: () => Promise<void>;
  }
}

const Canvas: React.FC = () => {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const [isDrawing, setIsDrawing] = useState(false);
  const [startPoint, setStartPoint] = useState<LinePoint | null>(null);
  const [lines, setLines] = useState<LinePoint[][]>([]);
  const [captureImage, setCaptureImage] = useState(false);
  const [downloadImage, setDownloadImage] = useState(false);
  const [capturedImage, setCapturedImage] = useState(false);
  const [matrix, setMatrix] = useState<number[][][]>([]);
  const { showNotification } = useNotification();
  const { token, user } = useAppSelector((state) => state.auth);
  const navigate = useNavigate();

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    if (!capturedImage) {
      // Clear canvas
      ctx.clearRect(0, 0, 500, 500);
      ctx.fillStyle = "white";
      ctx.fillRect(0, 0, 500, 500);

      // Set line style
      ctx.strokeStyle = "black";
      ctx.lineWidth = 2;
      ctx.lineCap = "round";

      // Draw all completed lines
      lines.forEach((line) => {
        if (line.length === 2) {
          ctx.beginPath();
          ctx.moveTo(line[0].x, line[0].y);
          ctx.lineTo(line[1].x, line[1].y);
          ctx.stroke();
        }
      });

      // Draw current line being drawn
      if (isDrawing && startPoint) {
        ctx.beginPath();
        ctx.moveTo(startPoint.x, startPoint.y);
        ctx.lineTo(startPoint.x, startPoint.y); // Will be updated by mouse move
        ctx.stroke();
      }
    }

    if (capturedImage && matrix.length > 0) {
      displayColoredMap(ctx, matrix);
    }
  }, [lines, isDrawing, startPoint, capturedImage, matrix]);

  const getMousePos = (e: React.MouseEvent<HTMLCanvasElement>) => {
    const canvas = canvasRef.current;
    if (!canvas) return { x: 0, y: 0 };

    const rect = canvas.getBoundingClientRect();
    return {
      x: e.clientX - rect.left,
      y: e.clientY - rect.top,
    };
  };

  const handleMouseDown = (e: React.MouseEvent<HTMLCanvasElement>) => {
    // Don't allow drawing if a colored map is displayed
    if (capturedImage) return;

    const pos = getMousePos(e);
    setStartPoint(pos);
    setIsDrawing(true);
  };

  const handleMouseMove = (e: React.MouseEvent<HTMLCanvasElement>) => {
    // Don't allow drawing if a colored map is displayed
    if (capturedImage) return;

    if (!isDrawing || !startPoint) return;

    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    const currentPos = getMousePos(e);

    // Redraw everything
    ctx.clearRect(0, 0, 500, 500);
    ctx.fillStyle = "white";
    ctx.fillRect(0, 0, 500, 500);

    ctx.strokeStyle = "black";
    ctx.lineWidth = 2;
    ctx.lineCap = "round";

    // Draw all completed lines
    lines.forEach((line) => {
      if (line.length === 2) {
        ctx.beginPath();
        ctx.moveTo(line[0].x, line[0].y);
        ctx.lineTo(line[1].x, line[1].y);
        ctx.stroke();
      }
    });

    // Draw current line preview
    ctx.beginPath();
    ctx.moveTo(startPoint.x, startPoint.y);
    ctx.lineTo(currentPos.x, currentPos.y);
    ctx.stroke();
  };

  const handleMouseUp = (e: React.MouseEvent<HTMLCanvasElement>) => {
    // Don't allow drawing if a colored map is displayed
    if (capturedImage) return;

    if (!isDrawing || !startPoint) return;

    const endPoint = getMousePos(e);
    setLines((prev) => [...prev, [startPoint, endPoint]]);
    setStartPoint(null);
    setIsDrawing(false);
  };

  const clearCanvas = () => {
    setLines([]);
    setStartPoint(null);
    setIsDrawing(false);
    setCapturedImage(false);
  };

  // Handle image capture
  useEffect(() => {
    if (captureImage) {
      const canvas = canvasRef.current;
      if (!canvas) return;

      const ctx = canvas.getContext("2d");
      if (!ctx) return;

      // Get image data from the entire canvas
      const imageData = ctx.getImageData(0, 0, 500, 500);
      const array_pixels = Array.from(imageData.data);

      getData(array_pixels, 500, 500);
      setCaptureImage(false);
    }
  }, [captureImage]);

  // Handle image download
  useEffect(() => {
    if (downloadImage) {
      saveCanvasAsImage();
      setDownloadImage(false);
    }
  }, [downloadImage]);

  async function getData(array_pixels: number[], w: number, h: number) {
    const apiHost = import.meta.env.VITE_API_GATEWAY_URL;

    if (!apiHost) {
      throw new Error("API host is not defined in the environment variables");
    }

    try {
      console.log("Sending request with:", {
        width: w,
        height: h,
        imageLength: array_pixels.length,
        userId: user?.id,
      });

      const res = await authInterceptor.makeAuthenticatedRequest(
        `${apiHost}/api/v1/maps/color`,
        {
          method: "POST",
          body: JSON.stringify({
            image: {
              data: array_pixels,
            },
            height: h,
            width: w,
            userId: user?.id,
          }),
          headers: {
            "Content-Type": "application/json",
          },
        }
      );

      if (!res.ok) {
        const errorText = await res.text();
        console.error("Server response:", res.status, errorText);
        throw new Error(`HTTP error! status: ${res.status}`);
      }

      const data = await res.json();
      setMatrix(data);
      setCapturedImage(true);
    } catch (error) {
      console.error("Error in getData:", error);

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

      showNotification("Failed to color map. Please try again.", "error");
      throw error;
    }
  }

  function displayColoredMap(
    ctx: CanvasRenderingContext2D,
    matrix: number[][][]
  ) {
    const imageData = ctx.createImageData(matrix[0].length, matrix.length);
    const data = imageData.data;

    for (let i = 0; i < matrix.length; i++) {
      for (let j = 0; j < matrix[i].length; j++) {
        const index = (i * matrix[i].length + j) * 4;
        data[index] = matrix[i][j][0] * 256; // Red
        data[index + 1] = matrix[i][j][1] * 256; // Green
        data[index + 2] = matrix[i][j][2] * 256; // Blue
        data[index + 3] = 255; // Alpha
      }
    }

    ctx.putImageData(imageData, 0, 0);
  }

  function saveCanvasAsImage() {
    const canvas = canvasRef.current;
    if (!canvas) return;

    // Download the image
    const link = document.createElement("a");
    link.download = "map_image.png";
    link.href = canvas.toDataURL();
    link.click();
  }

  // Export functions for external use
  React.useEffect(() => {
    window.handleColorMap = () => {
      // Don't allow coloring if already colored
      if (capturedImage) {
        showNotification(
          "Map is already colored! Reset to draw a new map.",
          "warning"
        );
        return;
      }

      setCaptureImage(true);
      console.log("Coloring map");
    };

    window.handleResetMap = () => {
      clearCanvas();
      console.log("Resetting map");
    };

    window.handleDownloadMap = () => {
      // Check if canvas is blank (no lines drawn and no colored image)
      if (lines.length === 0 && !capturedImage) {
        showNotification(
          "Cannot download blank canvas. Please draw and color a map first.",
          "warning"
        );
        return;
      }

      setDownloadImage(true);
      console.log("Downloading map image");
    };

    window.handleSaveMap = async () => {
      if (!capturedImage || !matrix.length) {
        showNotification("Please color your map before saving!", "warning");
        return;
      }

      if (!token || !user) {
        showNotification("Please login to save your map", "error");
        return;
      }

      const apiHost = import.meta.env.VITE_API_GATEWAY_URL;
      if (!apiHost) {
        throw new Error("API host is not defined");
      }

      try {
        const canvas = canvasRef.current;
        if (!canvas) {
          throw new Error("Canvas not found");
        }

        const imageData = canvas.toDataURL("image/png");

        // Ensure matrix is properly formatted as number[][]
        const formattedMatrix = matrix.map((row) =>
          row.map((cell) => Number(cell))
        );

        console.log("Matrix structure:", {
          type: typeof formattedMatrix,
          isArray: Array.isArray(formattedMatrix),
          length: formattedMatrix.length,
          firstRow: formattedMatrix[0],
          firstElement: formattedMatrix[0][0],
          firstElementType: typeof formattedMatrix[0][0],
        });

        const requestBody = {
          userId: user?.id,
          name: `Map ${new Date().toLocaleString()}`,
          imageData: imageData,
          matrix: formattedMatrix,
          width: canvas.width,
          height: canvas.height,
        };

        console.log("Request body:", JSON.stringify(requestBody));

        const response = await authInterceptor.makeAuthenticatedRequest(
          `${apiHost}/api/v1/maps`,
          {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify(requestBody),
          }
        );

        if (!response.ok) {
          const errorText = await response.text();
          console.error("Server response:", errorText);
          throw new Error(
            `HTTP error! status: ${response.status}, message: ${errorText}`
          );
        }

        const savedMap = await response.json();
        showNotification("Map saved successfully!", "success");
        return savedMap;
      } catch (error) {
        console.error("Error saving map:", error);

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

        showNotification("Failed to save map. Please try again.", "error");
      }
    };
  }, [capturedImage, matrix]);

  return (
    <canvas
      ref={canvasRef}
      width={500}
      height={500}
      onMouseDown={handleMouseDown}
      onMouseMove={handleMouseMove}
      onMouseUp={handleMouseUp}
      onMouseLeave={handleMouseUp}
      style={{
        border: "1px solid #ccc",
        cursor: capturedImage ? "not-allowed" : "crosshair",
        display: "block",
      }}
    />
  );
};

export default Canvas;
