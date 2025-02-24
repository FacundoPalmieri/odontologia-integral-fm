import { Injectable } from "@angular/core";
import { MatSnackBar } from "@angular/material/snack-bar";
import { BehaviorSubject } from "rxjs";
import { SnackbarType } from "../utils/enums/snackbar-type.enum";
import { SnackbarComponent } from "../presentation/components/snackbar/snackbar.component";

@Injectable({
  providedIn: "root",
})
export class SnackbarService {
  private isSnackbarVisibleSubject = new BehaviorSubject<boolean>(false);
  isSnackbarVisible$ = this.isSnackbarVisibleSubject.asObservable();

  constructor(private snackBar: MatSnackBar) {}

  openSnackbar(
    message: string,
    duration: number = 3000,
    horizontalPosition: "start" | "center" | "end" = "center",
    verticalPosition: "top" | "bottom" = "bottom",
    type: SnackbarType = SnackbarType.Info
  ): void {
    if (!this.isSnackbarVisibleSubject.value) {
      this.isSnackbarVisibleSubject.next(true);
      this.snackBar
        .openFromComponent(SnackbarComponent, {
          data: { message, type },
          duration: duration,
          horizontalPosition: horizontalPosition,
          verticalPosition: verticalPosition,
        })
        .afterDismissed()
        .subscribe(() => {
          this.isSnackbarVisibleSubject.next(false);
        });
    }
  }

  closeSnackbar(): void {
    this.snackBar.dismiss();
    this.isSnackbarVisibleSubject.next(false);
  }
}
