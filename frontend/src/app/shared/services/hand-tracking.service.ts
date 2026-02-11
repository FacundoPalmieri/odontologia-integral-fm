import { Injectable } from "@angular/core";
import * as handTrack from "handtrackjs";

/**
 * Service for hand tracking and gesture recognition using HandTrack.js.
 *
 * This service provides real-time hand detection and tracking capabilities
 * using the device camera. It can detect hand positions and gestures,
 * useful for touchless interaction with the application.
 *
 * Features:
 * - Hand position tracking
 * - Gesture recognition (open, closed, point, pinch)
 * - Real-time video processing
 * - Configurable detection parameters
 */
@Injectable({
  providedIn: "root",
})
export class HandTrackingService {
  /** HandTrack.js model instance */
  private model: any = null;
  /** Flag indicating if tracking is currently active */
  private isTracking = false;
  /** Reference to the video element */
  private video: HTMLVideoElement | null = null;
  /** Reference to the canvas element for drawing */
  private canvas: HTMLCanvasElement | null = null;
  /** Canvas rendering context */
  private context: CanvasRenderingContext2D | null = null;
  /** Animation frame ID for tracking loop */
  private animationFrameId: number | null = null;

  /**
   * Configuration parameters for the hand tracking model.
   */
  private modelParams = {
    flipHorizontal: true, // Mirror the camera feed
    maxNumBoxes: 1, // Detect only one hand
    iouThreshold: 0.5, // Intersection over union threshold
    scoreThreshold: 0.7, // Confidence threshold (increased for better accuracy)
    modelType: "ssd320fpnlite" as const, // Optimized model for hand detection
  };

  /**
   * Callback function invoked when hand movement is detected.
   * @private
   */
  private onHandMoveCallback: ((x: number, y: number) => void) | null = null;

  /**
   * Callback function invoked when hand gestures are detected.
   * @private
   */
  private onHandGestureCallback: ((handClass: string) => void) | null = null;

  constructor() {}

  /**
   * Loads the HandTrack.js model.
   *
   * This method initializes the hand tracking model. It only loads once
   * and subsequent calls will return immediately if already loaded.
   *
   * @returns Promise that resolves when the model is loaded
   * @throws Error if model loading fails
   */
  async loadModel(): Promise<void> {
    if (this.model) {
      return; // Already loaded
    }

    try {
      this.model = await handTrack.load(this.modelParams);
      console.log("HandTrack.js model loaded successfully");
    } catch (error) {
      console.error("Error loading HandTrack.js model:", error);
      throw error;
    }
  }

  /**
   * Starts hand tracking.
   *
   * Initializes the video stream from the camera and begins detecting hands.
   * The model is automatically loaded if not already loaded.
   *
   * @param videoElement - HTML video element to display the camera feed
   * @param canvasElement - HTML canvas element for drawing detection boxes
   * @param onHandMove - Callback function called when hand movement is detected, receives normalized x,y coordinates (0-1)
   * @param onHandGesture - Optional callback function called when hand gestures are detected
   * @returns Promise that resolves when tracking starts
   * @throws Error if video stream cannot be started
   */
  async startTracking(
    videoElement: HTMLVideoElement,
    canvasElement: HTMLCanvasElement,
    onHandMove: (x: number, y: number) => void,
    onHandGesture?: (handClass: string) => void,
  ): Promise<void> {
    if (this.isTracking) {
      return;
    }

    this.video = videoElement;
    this.canvas = canvasElement;
    this.context = canvasElement.getContext("2d");
    this.onHandMoveCallback = onHandMove;
    this.onHandGestureCallback = onHandGesture || null;

    // Load model if not already loaded
    if (!this.model) {
      await this.loadModel();
    }

    // Start video stream
    try {
      const stream = await handTrack.startVideo(this.video);
      this.isTracking = true;
      this.runDetection();
    } catch (error) {
      console.error("Error starting video:", error);
      throw error;
    }
  }

  /**
   * Stops hand tracking.
   *
   * Stops the video stream, cancels the animation loop, and cleans up resources.
   *
   * @returns void
   */
  stopTracking(): void {
    this.isTracking = false;

    if (this.animationFrameId) {
      cancelAnimationFrame(this.animationFrameId);
      this.animationFrameId = null;
    }

    if (this.video) {
      handTrack.stopVideo(this.video);
      this.video = null;
    }

    this.canvas = null;
    this.context = null;
    this.onHandMoveCallback = null;
  }

  /**
   * Executes hand detection on each video frame.
   *
   * This method runs in a loop, processing each frame to detect hands
   * and calling the appropriate callbacks when hands are found.
   *
   * @returns void
   * @private
   */
  private runDetection(): void {
    if (!this.isTracking || !this.video || !this.canvas || !this.context) {
      return;
    }

    this.model.detect(this.video).then((predictions: any[]) => {
      // Clear canvas
      this.context!.clearRect(0, 0, this.canvas!.width, this.canvas!.height);

      // Log for debugging (see what is being detected)
      if (predictions.length > 0) {
        console.log(
          "Detections:",
          predictions.map((p) => ({ class: p.class, score: p.score })),
        );
      }

      // Filter hand detections (more permissive)
      // HandTrack.js can detect: 'open', 'closed', 'point', 'pinch', 'face'
      let handPredictions = predictions.filter(
        (pred) => pred.class !== "face", // Exclude only faces
      );

      // If no specific hand detections, use the first prediction if it's not a face
      if (handPredictions.length === 0 && predictions.length > 0) {
        handPredictions = predictions.filter((pred) => pred.class !== "face");
      }

      if (handPredictions.length > 0) {
        // Take the first detected hand
        const hand = handPredictions[0];

        // Draw the bounding box (optional, for debugging)
        this.drawBoundingBox(hand);

        // Calculate the center of the hand (control point)
        const centerX = hand.bbox[0] + hand.bbox[2] / 2;
        const centerY = hand.bbox[1] + hand.bbox[3] / 2;

        // Normalize coordinates (0-1) based on canvas size
        const normalizedX = centerX / this.canvas!.width;
        const normalizedY = centerY / this.canvas!.height;

        // Call the movement callback with normalized coordinates
        if (this.onHandMoveCallback) {
          this.onHandMoveCallback(normalizedX, normalizedY);
        }

        // Call the gesture callback with the hand class
        if (this.onHandGestureCallback) {
          this.onHandGestureCallback(hand.class);
        }
      }

      // Continue with the next frame
      if (this.isTracking) {
        this.animationFrameId = requestAnimationFrame(() =>
          this.runDetection(),
        );
      }
    });
  }

  /**
   * Draws the bounding box of the detected hand on the canvas.
   *
   * This is useful for debugging and visualizing the detection.
   *
   * @param prediction - The hand prediction object containing bbox and other data
   * @returns void
   * @private
   */
  private drawBoundingBox(prediction: any): void {
    if (!this.context) return;

    const [x, y, width, height] = prediction.bbox;

    this.context.strokeStyle = "#00ff00";
    this.context.lineWidth = 3;
    this.context.strokeRect(x, y, width, height);

    // Draw center point
    const centerX = x + width / 2;
    const centerY = y + height / 2;

    this.context.fillStyle = "#ff0000";
    this.context.beginPath();
    this.context.arc(centerX, centerY, 5, 0, 2 * Math.PI);
    this.context.fill();
  }

  /**
   * Checks if tracking is currently active.
   *
   * @returns True if tracking is active, false otherwise
   */
  isActive(): boolean {
    return this.isTracking;
  }

  /**
   * Releases model resources and stops tracking.
   *
   * This should be called when the service is no longer needed
   * to free up memory and GPU resources.
   *
   * @returns void
   */
  dispose(): void {
    this.stopTracking();
    if (this.model) {
      this.model.dispose();
      this.model = null;
    }
  }
}
