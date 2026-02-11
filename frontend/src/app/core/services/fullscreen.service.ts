import { Injectable, signal } from "@angular/core";

/**
 * Service for managing fullscreen mode in the application.
 *
 * This service provides methods to enter, exit, and toggle fullscreen mode,
 * along with a signal to track the current fullscreen state.
 */
@Injectable({
  providedIn: "root",
})
export class FullscreenService {
  /**
   * Signal indicating whether the application is currently in fullscreen mode.
   */
  isFullscreen = signal(false);

  /**
   * Toggles between fullscreen and normal mode.
   *
   * If currently in fullscreen mode, exits fullscreen.
   * If not in fullscreen mode, enters fullscreen.
   * Updates the isFullscreen signal accordingly.
   *
   * @returns void
   */
  toggleFullscreen(): void {
    if (this.isFullscreen()) {
      this.exitFullscreen();
    } else {
      this.enterFullscreen();
    }
    this.isFullscreen.set(!this.isFullscreen());
  }

  /**
   * Enters fullscreen mode by requesting fullscreen on the document element.
   *
   * @returns void
   * @private
   */
  private enterFullscreen(): void {
    const element = document.documentElement;
    if (element.requestFullscreen) {
      element.requestFullscreen();
    }
  }

  /**
   * Exits fullscreen mode.
   *
   * @returns void
   * @private
   */
  private exitFullscreen(): void {
    if (document.exitFullscreen) {
      document.exitFullscreen();
    }
  }
}
