import { Injectable } from "@angular/core";
import { MatSnackBar } from "@angular/material/snack-bar";
import { SnackbarTypeEnum } from "../utils/enums/snackbar-type.enum";
import { SnackbarComponent } from "../components/snackbar/snackbar.component";

/**
 * Service for displaying snackbar notifications.
 *
 * This service provides a centralized way to show toast-style notifications
 * to users. It supports different notification types (success, error, warning, info)
 * with customizable positioning and duration.
 */
@Injectable({
  providedIn: "root",
})
export class SnackbarService {
  constructor(private snackBar: MatSnackBar) {}

  /**
   * Displays a snackbar notification.
   *
   * Shows a toast-style notification with customizable appearance and behavior.
   * The snackbar automatically dismisses after the specified duration.
   *
   * @param message - The message to display in the snackbar
   * @param duration - Duration in milliseconds before auto-dismiss, defaults to 3000ms
   * @param horizontalPosition - Horizontal position on screen, defaults to 'center'
   * @param verticalPosition - Vertical position on screen, defaults to 'bottom'
   * @param type - Type of notification (success, error, warning, info), defaults to info
   * @returns void
   *
   * @example
   * ```typescript
   * // Show success message
   * snackbarService.openSnackbar(
   *   'Patient saved successfully',
   *   3000,
   *   'center',
   *   'bottom',
   *   SnackbarTypeEnum.Success
   * );
   *
   * // Show error message
   * snackbarService.openSnackbar(
   *   'Failed to save patient',
   *   5000,
   *   'center',
   *   'top',
   *   SnackbarTypeEnum.Error
   * );
   * ```
   */
  openSnackbar(
    message: string,
    duration: number = 3000,
    horizontalPosition: "start" | "center" | "end" = "center",
    verticalPosition: "top" | "bottom" = "bottom",
    type: SnackbarTypeEnum = SnackbarTypeEnum.Info,
  ): void {
    let panelClass = "snackbar-info";
    if (type === SnackbarTypeEnum.Success) panelClass = "snackbar-success";
    if (type === SnackbarTypeEnum.Error) panelClass = "snackbar-error";
    if (type === SnackbarTypeEnum.Warning) panelClass = "snackbar-warning";
    this.snackBar.openFromComponent(SnackbarComponent, {
      data: { message, type },
      duration: duration,
      horizontalPosition: horizontalPosition,
      verticalPosition: verticalPosition,
      panelClass: panelClass,
    });
  }

  /**
   * Manually closes the currently displayed snackbar.
   *
   * This can be used to dismiss a snackbar before its auto-dismiss duration expires.
   *
   * @returns void
   */
  closeSnackbar(): void {
    this.snackBar.dismiss();
  }
}
