import { Injectable } from "@angular/core";
import { MatSnackBar } from "@angular/material/snack-bar";
import { SnackbarTypeEnum } from "../utils/enums/snackbar-type.enum";
import { SnackbarComponent } from "../presentation/components/snackbar/snackbar.component";

@Injectable({
  providedIn: "root",
})
export class SnackbarService {
  constructor(private snackBar: MatSnackBar) {}

  openSnackbar(
    message: string,
    duration: number = 3000,
    horizontalPosition: "start" | "center" | "end" = "center",
    verticalPosition: "top" | "bottom" = "bottom",
    type: SnackbarTypeEnum = SnackbarTypeEnum.Info
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

  closeSnackbar(): void {
    this.snackBar.dismiss();
  }
}
