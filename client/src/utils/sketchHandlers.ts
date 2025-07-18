// Import the actual functions from sketch
import {
  handleColorMap as sketchColorMap,
  handleResetMap as sketchResetMap,
  handleDownloadMap as sketchDownloadMap,
  handleSaveMap as sketchSaveMap,
} from "./sketch.ts";

export function handleColorMap() {
  sketchColorMap();
}

export function handleResetMap() {
  sketchResetMap();
}

export function handleDownloadMap() {
  sketchDownloadMap();
}

export async function handleSaveMap() {
  await sketchSaveMap();
}
