import {
  Component,
  OnInit,
  OnDestroy,
  ViewChild,
  ElementRef,
  inject,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatTooltipModule } from "@angular/material/tooltip";
import { Router, NavigationEnd } from "@angular/router";
import { filter } from "rxjs/operators";
import { HandTrackingService } from "../../../services/hand-tracking.service";
import { IconsModule } from "../../../utils/tabler-icons.module";

@Component({
  selector: "app-gesture-control",
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    IconsModule,
  ],
  templateUrl: "./gesture-control.component.html",
  styleUrl: "./gesture-control.component.scss",
})
export class GestureControlComponent implements OnInit, OnDestroy {
  private readonly handTrackingService = inject(HandTrackingService);
  private readonly router = inject(Router);

  @ViewChild("videoElement") videoElement!: ElementRef<HTMLVideoElement>;
  @ViewChild("canvasElement") canvasElement!: ElementRef<HTMLCanvasElement>;

  isActive = false;
  statusText = "Inactivo";
  isVideoMinimized = false; // Nueva propiedad para controlar minimización
  shouldShowGestureControl = true; // Controla si se muestra el componente

  videoWidth = 480; // Aumentado de 320 a 480
  videoHeight = 360; // Aumentado de 240 a 360

  cursorX = 0;
  cursorY = 0;
  cursorVisible = false;
  isClicking = false;

  // Factor de amplificación del movimiento (1.3 = 30% más de rango)
  private movementAmplification = 1.3;

  private previousHandSize = 0;
  private clickThreshold = 0.7; // 70% de reducción de tamaño para detectar pinch
  private lastClickTime = 0;
  private clickCooldown = 500; // ms entre clicks

  // Estado de la mano anterior para detectar cambios
  private previousHandClass = "";

  ngOnInit(): void {
    // Verificar la ruta actual
    this.checkRoute(this.router.url);

    // Escuchar cambios de ruta
    this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe((event: any) => {
        this.checkRoute(event.url);
      });

    this.handTrackingService.loadModel().catch((error) => {
      console.error("Failed to load hand tracking model:", error);
    });
  }

  ngOnDestroy(): void {
    this.stopTracking();
  }

  /**
   * Verifica si el componente debe mostrarse según la ruta actual
   */
  private checkRoute(url: string): void {
    // Lista de rutas donde NO se debe mostrar el control por gestos
    const hiddenRoutes = [
      "/login",
      "/auth/login",
      "/password-recovery",
      "/auth/password-recovery",
      "/reset-password",
      "/auth/reset-password",
      "/forgot-password",
      "/auth/forgot-password",
    ];

    // Verificar si la URL actual coincide con alguna ruta oculta
    this.shouldShowGestureControl = !hiddenRoutes.some((route) =>
      url.toLowerCase().includes(route.toLowerCase())
    );

    // Si el componente debe ocultarse y está activo, desactivarlo
    if (!this.shouldShowGestureControl && this.isActive) {
      this.stopTracking();
    }
  }

  async toggleGestureControl(): Promise<void> {
    if (this.isActive) {
      this.stopTracking();
    } else {
      await this.startTracking();
    }
  }

  toggleVideoSize(): void {
    this.isVideoMinimized = !this.isVideoMinimized;
  }

  private async startTracking(): Promise<void> {
    try {
      this.statusText = "Cargando...";

      await new Promise((resolve) => setTimeout(resolve, 100));

      await this.handTrackingService.startTracking(
        this.videoElement.nativeElement,
        this.canvasElement.nativeElement,
        (normalizedX: number, normalizedY: number) => {
          this.onHandMove(normalizedX, normalizedY);
        },
        (handClass: string) => {
          this.detectHandGesture(handClass);
        }
      );

      this.isActive = true;
      this.statusText = "Activo - Mueve tu mano";
    } catch (error) {
      console.error("Error starting gesture control:", error);
      this.statusText = "Error";
      this.isActive = false;
    }
  }

  private stopTracking(): void {
    this.handTrackingService.stopTracking();
    this.isActive = false;
    this.cursorVisible = false;
    this.statusText = "Inactivo";
  }

  private onHandMove(normalizedX: number, normalizedY: number): void {
    // Centrar las coordenadas normalizadas (0-1) alrededor de 0.5
    const centeredX = (normalizedX - 0.5) * this.movementAmplification + 0.5;
    const centeredY = (normalizedY - 0.5) * this.movementAmplification + 0.5;

    // Asegurar que las coordenadas estén dentro del rango [0, 1]
    const clampedX = Math.max(0, Math.min(1, centeredX));
    const clampedY = Math.max(0, Math.min(1, centeredY));

    // Convertir coordenadas normalizadas a coordenadas de pantalla
    this.cursorX = clampedX * window.innerWidth;
    this.cursorY = clampedY * window.innerHeight;
    this.cursorVisible = true;
  }

  // Nueva función para detectar pinch basada en la clase de la mano
  detectHandGesture(handClass: string): void {
    console.log("Hand class detected:", handClass);

    // Solo hacer click cuando la mano cambia de 'open' a 'closed'
    // Esto evita clicks continuos mientras la mano está cerrada
    if (handClass === "closed" && this.previousHandClass === "open") {
      const now = Date.now();

      // Verificar cooldown para evitar clicks múltiples
      if (now - this.lastClickTime > this.clickCooldown) {
        console.log("Hand closed! Performing click...");
        this.performClick();
        this.lastClickTime = now;
      }
    }

    // Actualizar el estado anterior
    this.previousHandClass = handClass;
  }

  private performClick(): void {
    // Animación visual de click
    this.isClicking = true;
    setTimeout(() => {
      this.isClicking = false;
    }, 300);

    // Obtener el elemento en las coordenadas del cursor
    const element = document.elementFromPoint(
      this.cursorX,
      this.cursorY
    ) as HTMLElement;

    if (element) {
      console.log("Clicking on:", element.tagName, element.className);

      // Crear y disparar múltiples eventos para asegurar compatibilidad
      const events = [
        new MouseEvent("mousedown", {
          bubbles: true,
          cancelable: true,
          clientX: this.cursorX,
          clientY: this.cursorY,
          view: window,
        }),
        new MouseEvent("mouseup", {
          bubbles: true,
          cancelable: true,
          clientX: this.cursorX,
          clientY: this.cursorY,
          view: window,
        }),
        new MouseEvent("click", {
          bubbles: true,
          cancelable: true,
          clientX: this.cursorX,
          clientY: this.cursorY,
          view: window,
        }),
      ];

      // Disparar todos los eventos
      events.forEach((event) => {
        element.dispatchEvent(event);
      });

      // Si es un botón o link, hacer click directo
      if (element.tagName === "BUTTON" || element.tagName === "A") {
        (element as HTMLButtonElement | HTMLAnchorElement).click();
      }

      // Feedback visual adicional
      this.createClickRipple(this.cursorX, this.cursorY);

      console.log("Click performed successfully!");
    } else {
      console.log("No element found at cursor position");
    }
  }

  private createClickRipple(x: number, y: number): void {
    const ripple = document.createElement("div");
    ripple.style.position = "fixed";
    ripple.style.left = `${x}px`;
    ripple.style.top = `${y}px`;
    ripple.style.width = "10px";
    ripple.style.height = "10px";
    ripple.style.borderRadius = "50%";
    ripple.style.background = "rgba(0, 255, 0, 0.6)";
    ripple.style.transform = "translate(-50%, -50%)";
    ripple.style.pointerEvents = "none";
    ripple.style.zIndex = "9999";
    ripple.style.animation = "rippleEffect 0.6s ease-out";

    // Agregar animación CSS
    const style = document.createElement("style");
    style.textContent = `
      @keyframes rippleEffect {
        0% {
          transform: translate(-50%, -50%) scale(1);
          opacity: 1;
        }
        100% {
          transform: translate(-50%, -50%) scale(4);
          opacity: 0;
        }
      }
    `;
    document.head.appendChild(style);

    document.body.appendChild(ripple);

    setTimeout(() => {
      document.body.removeChild(ripple);
      document.head.removeChild(style);
    }, 600);
  }
}
