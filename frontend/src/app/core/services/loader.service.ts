import { Injectable } from "@angular/core";
import { BehaviorSubject } from "rxjs";

/**
 * Service for managing the global loading state of the application.
 *
 * This service provides a centralized way to show and hide loading indicators
 * throughout the application using RxJS observables.
 */
@Injectable({
  providedIn: "root",
})
export class LoaderService {
  /**
   * BehaviorSubject that holds the current loading state.
   * @private
   */
  private loadingSubject = new BehaviorSubject<boolean>(false);

  /**
   * Observable stream of the loading state that components can subscribe to.
   */
  loading$ = this.loadingSubject.asObservable();

  /**
   * Shows the global loader by emitting true to all subscribers.
   *
   * @returns void
   */
  show() {
    this.loadingSubject.next(true);
  }

  /**
   * Hides the global loader by emitting false to all subscribers.
   *
   * @returns void
   */
  hide() {
    this.loadingSubject.next(false);
  }
}
