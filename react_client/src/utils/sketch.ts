import p5 from "p5";

interface Point {
  x: number;
  y: number;
}

interface Line {
  0: Point;
  1: Point;
}

const h = 500;
const w = 500;
const grid_h = 400;
const grid_w = 400;
const grid_margin = 50;

let lines: Line[] = [];
let start: Point, end: Point;
let dragging = false;
let captureImage = false;
let downloadImage = false;
let captured_image = false;
let matrix: number[][][];

// Helper function to check if a point is within the canvas grid area
function isWithinGridBounds(x: number, y: number): boolean {
  return (
    x >= grid_margin &&
    x <= grid_margin + grid_w &&
    y >= grid_margin &&
    y <= grid_margin + grid_h
  );
}

// Helper function to constrain a point to the grid bounds
function constrainToGrid(x: number, y: number): Point {
  return {
    x: Math.max(grid_margin, Math.min(grid_margin + grid_w, x)),
    y: Math.max(grid_margin, Math.min(grid_margin + grid_h, y)),
  };
}

function sketch(p: p5) {
  p.setup = function () {
    p.createCanvas(w, h);
    p.noSmooth();
    p.frameRate(30);
    start = { x: w / 2, y: h / 2 };
  };

  p.draw = function () {
    if (captured_image === false) {
      p.background(200);
      p.stroke(0);
      p.fill(255);
      p.rect(grid_margin, grid_margin, grid_w - 1, grid_h - 1);

      // draw lines
      for (let i = 0; i < lines.length; i++) {
        const a = lines[i][0];
        const b = lines[i][1];
        drawLine(p, a.x, a.y, b.x, b.y);
      }

      // draw temporary line while dragging
      if (dragging) drawLine(p, start.x, start.y, end.x, end.y);
    }

    if (captureImage) {
      const img = p.get(grid_margin, grid_margin, grid_w, grid_h);
      img.loadPixels();
      const array_pixels = img.pixels;
      getData(array_pixels, img.width, img.height);
      captureImage = false;
    }

    if (captured_image) {
      displayColoredMap(p, matrix);
    }

    if (downloadImage) {
      saveCanvasAsImage(p);
    }
  };

  p.mousePressed = function () {
    // Only start drawing if the mouse is within the grid bounds
    if (isWithinGridBounds(p.mouseX, p.mouseY)) {
      start = constrainToGrid(p.mouseX, p.mouseY);
      dragging = false; // Reset dragging state
      return false; // Prevent default behavior
    }
  };

  p.mouseDragged = function () {
    // Only continue dragging if we have a valid start point and current position is within bounds
    if (start && isWithinGridBounds(p.mouseX, p.mouseY)) {
      end = constrainToGrid(p.mouseX, p.mouseY);
      dragging = true;
      return false; // Prevent default behavior
    }
  };

  p.mouseReleased = function () {
    // Only complete the line if we were dragging
    if (dragging && start) {
      dragging = false;
      end = constrainToGrid(p.mouseX, p.mouseY);

      // Only add the line if both start and end points are valid and different
      if (start && end && (start.x !== end.x || start.y !== end.y)) {
        lines.push([start, end]);
      }

      // Reset state for next line
      start = { x: 0, y: 0 };
      end = { x: 0, y: 0 };
      return false; // Prevent default behavior
    } else {
      // Reset dragging state if released outside bounds or no drag occurred
      dragging = false;
    }
  };
}

function drawPoint(p: p5, x: number, y: number) {
  for (let i = 0; i < 3; i++) {
    for (let j = 0; j < 3; j++) {
      const px = x - 1 + i;
      const py = y - 1 + j;
      if (
        px >= grid_margin &&
        px < grid_w + grid_margin &&
        py >= grid_margin &&
        py < grid_h + grid_margin
      ) {
        p.point(px, py);
      }
    }
  }
}

function drawLine(p: p5, x0: number, y0: number, x1: number, y1: number) {
  const dx = Math.abs(x1 - x0);
  const dy = Math.abs(y1 - y0);
  const sx = x0 < x1 ? 1 : -1;
  const sy = y0 < y1 ? 1 : -1;
  let err = dx - dy;

  while (true) {
    drawPoint(p, x0, y0);
    if (x0 === x1 && y0 === y1) break;
    const e2 = 2 * err;
    if (e2 > -dy) {
      err -= dy;
      x0 += sx;
    }
    if (e2 < dx) {
      err += dx;
      y0 += sy;
    }
  }
}

async function getData(array_pixels: number[], w: number, h: number) {
  const apiHost = import.meta.env.VITE_API_GATEWAY_URL;

  if (!apiHost) {
    throw new Error("API host is not defined in the environment variables");
  }

  // Convert Uint8ClampedArray to regular array
  const pixelArray = Array.from(array_pixels);

  try {
    console.log("Sending request with:", {
      width: w,
      height: h,
      imageLength: pixelArray.length,
    });

    const res = await fetch(`${apiHost}/api/v1/maps/color`, {
      method: "POST",
      body: JSON.stringify({
        image: {
          data: pixelArray,
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
    matrix = data;
    captured_image = true;
  } catch (error) {
    console.error("Error in getData:", error);
    throw error;
  }
}

function displayColoredMap(p: p5, matrix: number[][][]) {
  const img = p.createImage(matrix[0].length, matrix.length);
  img.loadPixels();
  for (let i = 0; i < img.height; i++) {
    for (let j = 0; j < img.width; j++) {
      img.set(
        j,
        i,
        p.color(
          matrix[i][j][0] * 256,
          matrix[i][j][1] * 256,
          matrix[i][j][2] * 256
        )
      );
    }
  }
  img.updatePixels();
  p.image(img, grid_margin, grid_margin);
}

function saveCanvasAsImage(p: p5) {
  const img = p.get(grid_margin, grid_margin, grid_w, grid_h);
  p.save(img, "map_image.png");
  downloadImage = false;
}

export function handleColorMap() {
  captureImage = true;
  console.log("Coloring map");
}

export function handleResetMap() {
  lines = [];
  captured_image = false;
  console.log("Resetting map");
}

export function handleDownloadMap() {
  downloadImage = true;
  console.log("Downloading map image");
}

export async function handleSaveMap() {
  if (!captured_image || !matrix) {
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
    const canvas = document.querySelector("canvas");
    if (!canvas) {
      throw new Error("Canvas not found");
    }

    const imageData = canvas.toDataURL("image/png");

    // Ensure matrix is properly formatted as number[][]
    const formattedMatrix = matrix.map((row) =>
      row.map((cell) => Number(cell))
    );

    // Log the matrix structure
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

    // Log the request body
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
}

export default sketch;
