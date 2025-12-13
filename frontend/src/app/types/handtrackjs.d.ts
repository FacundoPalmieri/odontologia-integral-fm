declare module "handtrackjs" {
  export interface ModelParams {
    flipHorizontal?: boolean;
    maxNumBoxes?: number;
    iouThreshold?: number;
    scoreThreshold?: number;
    modelType?: "ssd320fpnlite" | "ssdlitemobilenetv2"; // Tipo de modelo
  }

  export interface Prediction {
    bbox: [number, number, number, number]; // [x, y, width, height]
    class: string;
    score: number;
  }

  export interface HandTrackModel {
    detect(
      input: HTMLVideoElement | HTMLImageElement | HTMLCanvasElement
    ): Promise<Prediction[]>;
    dispose(): void;
  }

  export function load(modelParams?: ModelParams): Promise<HandTrackModel>;
  export function startVideo(video: HTMLVideoElement): Promise<MediaStream>;
  export function stopVideo(video: HTMLVideoElement): void;
}
