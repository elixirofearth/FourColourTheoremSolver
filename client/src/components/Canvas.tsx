import React, { useRef, useEffect, useState, useCallback } from "react";

interface Point {
  x: number;
  y: number;
}

interface Line {
  0: Point;
  1: Point;
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

const h = 500;
const w = 500;
const grid_h = 400;
const grid_w = 400;
const grid_margin = 50;

export default function Canvas() {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const [lines, setLines] = useState<Line[]>([]);
  const [start, setStart] = useState<Point | null>(null);
  const [end, setEnd] = useState<Point | null>(null);
  const [dragging, setDragging] = useState(false);
  const [gestureStartedInCanvas, setGestureStartedInCanvas] = useState(false);
  const [captureImage, setCaptureImage] = useState(false);
  const [downloadImage, setDownloadImage] = useState(false);
  const [capturedImage, setCapturedImage] = useState(false);
  const [matrix, setMatrix] = useState<number[][][]>([]);

  // Helper function to check if a point is within the canvas grid area
  const isWithinGridBounds = useCallback((x: number, y: number): boolean => {
    return (
      x >= grid_margin &&
      x <= grid_margin + grid_w &&
      y >= grid_margin &&
      y <= grid_margin + grid_h
    );
  }, []);

  // Helper function to constrain a point to the grid bounds
  const constrainToGrid = useCallback((x: number, y: number): Point => {
    return {
      x: Math.max(grid_margin, Math.min(grid_margin + grid_w, x)),
      y: Math.max(grid_margin, Math.min(grid_margin + grid_h, y)),
    };
  }, []);

  // Get mouse position relative to canvas
  const getMousePos = useCallback((e: React.MouseEvent<HTMLCanvasElement>) => {
    const canvas = canvasRef.current;
    if (!canvas) return { x: 0, y: 0 };

    const rect = canvas.getBoundingClientRect();
    return {
      x: e.clientX - rect.left,
      y: e.clientY - rect.top,
    };
  }, []);

  // Draw function
  const draw = useCallback(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    if (!capturedImage) {
      // Clear canvas
      ctx.clearRect(0, 0, w, h);

      // Draw background
      ctx.fillStyle = "#C8C8C8"; // rgb(200, 200, 200)
      ctx.fillRect(0, 0, w, h);

      // Draw grid background
      ctx.strokeStyle = "#000000";
      ctx.fillStyle = "#FFFFFF";
      ctx.fillRect(grid_margin, grid_margin, grid_w - 1, grid_h - 1);
      ctx.strokeRect(grid_margin, grid_margin, grid_w - 1, grid_h - 1);

      // Set up line drawing properties for smooth lines
      ctx.strokeStyle = "#000000";
      ctx.lineWidth = 2;
      ctx.lineCap = "round";
      ctx.lineJoin = "round";

      // Draw existing lines
      for (let i = 0; i < lines.length; i++) {
        const a = lines[i][0];
        const b = lines[i][1];
        ctx.beginPath();
        ctx.moveTo(a.x, a.y);
        ctx.lineTo(b.x, b.y);
        ctx.stroke();
      }

      // Draw temporary line while dragging with a slightly different style
      if (dragging && start && end) {
        ctx.strokeStyle = "#646464"; // rgb(100, 100, 100)
        ctx.lineWidth = 1.5;
        ctx.beginPath();
        ctx.moveTo(start.x, start.y);
        ctx.lineTo(end.x, end.y);
        ctx.stroke();
      }
    }

    if (capturedImage && matrix.length > 0) {
      displayColoredMap(ctx, matrix);
    }
  }, [lines, dragging, start, end, capturedImage, matrix]);

  // Handle image capture
  useEffect(() => {
    if (captureImage) {
      const canvas = canvasRef.current;
      if (!canvas) return;

      const ctx = canvas.getContext("2d");
      if (!ctx) return;

      // Get image data from the grid area
      const imageData = ctx.getImageData(
        grid_margin,
        grid_margin,
        grid_w,
        grid_h
      );
      const array_pixels = Array.from(imageData.data);

      getData(array_pixels, grid_w, grid_h);
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

  // Draw on canvas updates
  useEffect(() => {
    draw();
  }, [draw]);

  // Initialize canvas
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    canvas.width = w;
    canvas.height = h;
    draw();
  }, [draw]);

  const handleMouseDown = (e: React.MouseEvent<HTMLCanvasElement>) => {
    const mousePos = getMousePos(e);
    setDragging(false);

    if (isWithinGridBounds(mousePos.x, mousePos.y)) {
      setGestureStartedInCanvas(true);
      setStart(constrainToGrid(mousePos.x, mousePos.y));
    } else {
      setGestureStartedInCanvas(false);
      setStart(null);
    }
  };

  const handleMouseMove = (e: React.MouseEvent<HTMLCanvasElement>) => {
    if (!gestureStartedInCanvas) return;

    const mousePos = getMousePos(e);
    if (isWithinGridBounds(mousePos.x, mousePos.y)) {
      setEnd(constrainToGrid(mousePos.x, mousePos.y));
      setDragging(true);
    }
  };

  const handleMouseUp = (e: React.MouseEvent<HTMLCanvasElement>) => {
    if (dragging && start) {
      setDragging(false);
      const mousePos = getMousePos(e);
      const endPoint = constrainToGrid(mousePos.x, mousePos.y);

      if (
        start &&
        endPoint &&
        (start.x !== endPoint.x || start.y !== endPoint.y)
      ) {
        setLines((prevLines) => [...prevLines, [start, endPoint]]);
      }

      setStart(null);
      setEnd(null);
    } else {
      setDragging(false);
    }

    setGestureStartedInCanvas(false);
  };

  const handleMouseLeave = () => {
    setDragging(false);
    setGestureStartedInCanvas(false);
  };

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
      });

      const res = await fetch(`${apiHost}/api/v1/maps/color`, {
        method: "POST",
        body: JSON.stringify({
          image: {
            data: array_pixels,
          },
          height: h,
          width: w,
        }),
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
          "Content-Type": "application/json",
        },
      });

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

    ctx.putImageData(imageData, grid_margin, grid_margin);
  }

  function saveCanvasAsImage() {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    // Create a new canvas for the cropped image
    const croppedCanvas = document.createElement("canvas");
    croppedCanvas.width = grid_w;
    croppedCanvas.height = grid_h;
    const croppedCtx = croppedCanvas.getContext("2d");

    if (!croppedCtx) return;

    // Draw the cropped portion
    croppedCtx.drawImage(
      canvas,
      grid_margin,
      grid_margin,
      grid_w,
      grid_h,
      0,
      0,
      grid_w,
      grid_h
    );

    // Download the image
    const link = document.createElement("a");
    link.download = "map_image.png";
    link.href = croppedCanvas.toDataURL();
    link.click();
  }

  // Export functions for external use
  React.useEffect(() => {
    window.handleColorMap = () => {
      setCaptureImage(true);
      console.log("Coloring map");
    };

    window.handleResetMap = () => {
      setLines([]);
      setCapturedImage(false);
      console.log("Resetting map");
    };

    window.handleDownloadMap = () => {
      setDownloadImage(true);
      console.log("Downloading map image");
    };

    window.handleSaveMap = async () => {
      if (!capturedImage || !matrix.length) {
        alert("Please color your map before saving!");
        return;
      }

      const token = localStorage.getItem("token");
      const userId = localStorage.getItem("userId");

      if (!token || !userId) {
        alert("Please login to save your map");
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
          userId: userId,
          name: `Map ${new Date().toLocaleString()}`,
          imageData: imageData,
          matrix: formattedMatrix,
          width: canvas.width,
          height: canvas.height,
        };

        console.log("Request body:", JSON.stringify(requestBody));

        const response = await fetch(`${apiHost}/api/v1/maps`, {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify(requestBody),
        });

        if (!response.ok) {
          const errorText = await response.text();
          console.error("Server response:", errorText);
          throw new Error(
            `HTTP error! status: ${response.status}, message: ${errorText}`
          );
        }

        const savedMap = await response.json();
        alert("Map saved successfully!");
        return savedMap;
      } catch (error) {
        console.error("Error saving map:", error);
        alert("Failed to save map. Please try again.");
      }
    };
  }, [capturedImage, matrix]);

  return (
    <canvas
      ref={canvasRef}
      onMouseDown={handleMouseDown}
      onMouseMove={handleMouseMove}
      onMouseUp={handleMouseUp}
      onMouseLeave={handleMouseLeave}
      style={{
        border: "1px solid #ccc",
        cursor: "crosshair",
        display: "block",
      }}
    />
  );
}
