import { Injectable } from "@angular/core";
import * as handTrack from "handtrackjs";

@Injectable({
  providedIn: "root",
})
export class HandTrackingService {
  private model: any = null;
  private isTracking = false;
  private video: HTMLVideoElement | null = null;
  private canvas: HTMLCanvasElement | null = null;
  private context: CanvasRenderingContext2D | null = null;
  private animationFrameId: number | null = null;

  // Configuración del modelo
  private modelParams = {
    flipHorizontal: true, // Espejo de la cámara
    maxNumBoxes: 1, // Solo detectar una mano
    iouThreshold: 0.5, // Umbral de intersección
    scoreThreshold: 0.7, // Umbral de confianza (aumentado para mejor precisión)
    modelType: "ssd320fpnlite" as const, // Modelo optimizado para manos
  };

  // Callback para cuando se detecta movimiento
  private onHandMoveCallback: ((x: number, y: number) => void) | null = null;

  // Callback para gestos de la mano
  private onHandGestureCallback: ((handClass: string) => void) | null = null;

  constructor() {}

  /**
   * Carga el modelo de HandTrack.js
   */
  async loadModel(): Promise<void> {
    if (this.model) {
      return; // Ya está cargado
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
   * Inicia el tracking de la mano
   */
  async startTracking(
    videoElement: HTMLVideoElement,
    canvasElement: HTMLCanvasElement,
    onHandMove: (x: number, y: number) => void,
    onHandGesture?: (handClass: string) => void
  ): Promise<void> {
    if (this.isTracking) {
      return;
    }

    this.video = videoElement;
    this.canvas = canvasElement;
    this.context = canvasElement.getContext("2d");
    this.onHandMoveCallback = onHandMove;
    this.onHandGestureCallback = onHandGesture || null;

    // Cargar modelo si no está cargado
    if (!this.model) {
      await this.loadModel();
    }

    // Iniciar video
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
   * Detiene el tracking
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
   * Ejecuta la detección en cada frame
   */
  private runDetection(): void {
    if (!this.isTracking || !this.video || !this.canvas || !this.context) {
      return;
    }

    this.model.detect(this.video).then((predictions: any[]) => {
      // Limpiar canvas
      this.context!.clearRect(0, 0, this.canvas!.width, this.canvas!.height);

      // Log para debug (ver qué está detectando)
      if (predictions.length > 0) {
        console.log(
          "Detecciones:",
          predictions.map((p) => ({ class: p.class, score: p.score }))
        );
      }

      // Filtrar detecciones de manos (más permisivo)
      // HandTrack.js puede detectar: 'open', 'closed', 'point', 'pinch', 'face'
      let handPredictions = predictions.filter(
        (pred) => pred.class !== "face" // Excluir solo caras
      );

      // Si no hay detecciones de manos específicas, usar la primera predicción si no es cara
      if (handPredictions.length === 0 && predictions.length > 0) {
        handPredictions = predictions.filter((pred) => pred.class !== "face");
      }

      if (handPredictions.length > 0) {
        // Tomar la primera mano detectada
        const hand = handPredictions[0];

        // Dibujar el bounding box (opcional, para debug)
        this.drawBoundingBox(hand);

        // Calcular el centro de la mano (punto de control)
        const centerX = hand.bbox[0] + hand.bbox[2] / 2;
        const centerY = hand.bbox[1] + hand.bbox[3] / 2;

        // Normalizar coordenadas (0-1) basado en el tamaño del canvas
        const normalizedX = centerX / this.canvas!.width;
        const normalizedY = centerY / this.canvas!.height;

        // Llamar al callback de movimiento con las coordenadas normalizadas
        if (this.onHandMoveCallback) {
          this.onHandMoveCallback(normalizedX, normalizedY);
        }

        // Llamar al callback de gestos con la clase de la mano
        if (this.onHandGestureCallback) {
          this.onHandGestureCallback(hand.class);
        }
      }

      // Continuar con el siguiente frame
      if (this.isTracking) {
        this.animationFrameId = requestAnimationFrame(() =>
          this.runDetection()
        );
      }
    });
  }

  /**
   * Dibuja el bounding box de la mano detectada
   */
  private drawBoundingBox(prediction: any): void {
    if (!this.context) return;

    const [x, y, width, height] = prediction.bbox;

    this.context.strokeStyle = "#00ff00";
    this.context.lineWidth = 3;
    this.context.strokeRect(x, y, width, height);

    // Dibujar punto central
    const centerX = x + width / 2;
    const centerY = y + height / 2;

    this.context.fillStyle = "#ff0000";
    this.context.beginPath();
    this.context.arc(centerX, centerY, 5, 0, 2 * Math.PI);
    this.context.fill();
  }

  /**
   * Verifica si el tracking está activo
   */
  isActive(): boolean {
    return this.isTracking;
  }

  /**
   * Libera recursos del modelo
   */
  dispose(): void {
    this.stopTracking();
    if (this.model) {
      this.model.dispose();
      this.model = null;
    }
  }
}
