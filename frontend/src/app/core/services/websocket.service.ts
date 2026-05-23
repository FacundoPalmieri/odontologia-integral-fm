import {
  Injectable,
  computed,
  signal,
  PLATFORM_ID,
  inject,
} from "@angular/core";
import { isPlatformBrowser } from "@angular/common";
import { RxStomp, RxStompState } from "@stomp/rx-stomp";
import { Observable, tap, Subject } from "rxjs";
import { environment } from "../../environments/environment";
import { LocalStorageService } from "../../shared/services/local-storage.service";
import { SnackbarService } from "../../shared/services/snackbar.service";
import { SnackbarTypeEnum } from "../../shared/utils/enums/snackbar-type.enum";

export type ConnectionStatus =
  | "connected"
  | "disconnected"
  | "connecting"
  | "error";

@Injectable({
  providedIn: "root",
})
export class WebsocketService {
  private rxStomp: RxStomp;
  private platformId = inject(PLATFORM_ID);
  private localStorageService = inject(LocalStorageService);
  private snackbarService = inject(SnackbarService);

  // Expose connection status via Angular Signal
  private readonly _status = signal<ConnectionStatus>("disconnected");
  public readonly status = computed(() => this._status());

  // Expose consultation updates via RxJS Subject
  private readonly _consultationUpdates$ = new Subject<any>();
  public readonly consultationUpdates$ = this._consultationUpdates$.asObservable();

  constructor() {
    this.rxStomp = new RxStomp();

    // Listen to STOMP connection state changes
    this.rxStomp.connectionState$.subscribe((state: RxStompState) => {
      switch (state) {
        case RxStompState.CONNECTING:
          this._status.set("connecting");
          break;
        case RxStompState.OPEN:
          this._status.set("connected");
          console.log("[WebSocket STOMP] Connection established.");
          break;
        case RxStompState.CLOSED:
          this._status.set("disconnected");
          console.log("[WebSocket STOMP] Connection closed.");
          break;
      }
    });

    this.rxStomp.stompErrors$.subscribe((error) => {
      this._status.set("error");
      console.error("[WebSocket STOMP] Protocol Error:", error);
    });

    this.rxStomp.webSocketErrors$.subscribe((error) => {
      this._status.set("error");
      console.error("[WebSocket STOMP] WebSocket Error:", error);
    });

    // Automatically watch /topic/consultations
    this.watch("/topic/consultations").subscribe({
      next: (message) => {
        try {
          const payload = typeof message.body === "string" ? JSON.parse(message.body) : message.body;
          this._consultationUpdates$.next(payload);
        } catch (e) {
          // If message is just a string or not parseable JSON
          this._consultationUpdates$.next(message.body);
        }
      },
      error: (error) => {
        console.error("[WebSocket STOMP] Error in /topic/consultations subscription:", error);
      }
    });
  }

  /**
   * Initializes the WebSocket connection using STOMP.
   */
  public connect(url: string = environment.wsUrl): void {
    // WebSockets only work in the browser, prevent execution in SSR
    if (!isPlatformBrowser(this.platformId)) {
      console.warn("[WebSocket] Cannot connect on server side (SSR).");
      return;
    }

    console.log(`[WebSocket] Attempting to connect to: ${url}`);

    if (this._status() === "connected" || this._status() === "connecting") {
      console.log("[WebSocket] Already connected or connecting.");
      return;
    }

    // Retrieve JWT from local storage (or your Auth/Person Data service)
    const token = this.localStorageService.getJwtToken();

    // The backend's JwtHandshakeInterceptor expects logic like ws://localhost:8080/ws?token=EY...
    const finalUrl = token ? `${url}?token=${token}` : url;

    this.rxStomp.configure({
      brokerURL: finalUrl,

      // Optional: You could pass tokens via headers, depending on how strict the backend config is
      // connectHeaders: {
      //   Authorization: `Bearer ${token}`
      // },

      // How often to send/receive heartbeats to keep the connection alive (in ms).
      heartbeatIncoming: 0, // 0 = disable
      heartbeatOutgoing: 20000,

      // Reconnect automatically if dropped
      reconnectDelay: 5000,

      // Useful for testing to see what STOMP is talking under the hood:
      debug: (msg: string): void => console.log(`[WebSocket Debug] ${msg}`),
    });

    this.rxStomp.activate();
  }

  /**
   * Disconnects the WebSocket intentionally.
   */
  public disconnect(): void {
    this.rxStomp.deactivate();
    this._status.set("disconnected");
  }

  /**
   * Subscribes to a STOMP topic (e.g. backend websocket.broker-prefix: /topic/consultas)
   * Listen to messages pushed from backend.
   *
   * @param destination e.g '/topic/messages'
   * @returns Observable stream with message payload
   */
  public watch(destination: string): Observable<any> {
    console.log(`[WebSocket] Subscribing to destination: ${destination}`);
    return this.rxStomp.watch(destination).pipe(
      tap((message) => {
        console.log(`[WebSocket] Message received from ${destination}:`, message.body);
      })
    );
  }

  /**
   * Sends a payload to the WebSocket server if connected.
   * The destination must start with backend websocket.app-prefix (e.g. /app/send)
   *
   * @param destination e.g '/app/chat'
   * @param message Body of the message (will be JSON stringified automatically)
   */
  public publish(destination: string, message: any): void {
    if (this._status() !== "connected") {
      console.warn("[WebSocket] Cannot send message: not connected.");
      return;
    }

    console.log(`[WebSocket] Publishing to ${destination}:`, message);
    this.rxStomp.publish({ destination, body: JSON.stringify(message) } as any);
  }
}
