import { Injectable, signal } from "@angular/core";

@Injectable({
  providedIn: "root",
})
export class FullscreenService {
  isFullscreen = signal(false);

  toggleFullscreen(): void {
    if (this.isFullscreen()) {
      this.exitFullscreen();
    } else {
      this.enterFullscreen();
    }
    this.isFullscreen.set(!this.isFullscreen());
  }

  private enterFullscreen(): void {
    const element = document.documentElement;
    if (element.requestFullscreen) {
      element.requestFullscreen();
    }
  }

  private exitFullscreen(): void {
    if (document.exitFullscreen) {
      document.exitFullscreen();
    }
  }
}
