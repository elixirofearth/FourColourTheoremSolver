import { ReactP5Wrapper } from "@p5-wrapper/react";
import sketch from "../utils/sketch.ts";

export default function Canvas() {
  return <ReactP5Wrapper sketch={sketch} />;
}
