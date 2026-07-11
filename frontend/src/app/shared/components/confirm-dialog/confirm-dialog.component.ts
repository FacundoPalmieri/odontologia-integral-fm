import { Component, inject } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from "@angular/material/dialog";
import { IconsModule } from "../../../core/modules/tabler-icons.module";

export interface ConfirmDialogData {
  message: string;
  confirmText?: string;
  cancelText?: string;
}

@Component({
  selector: "app-confirm-dialog",
  templateUrl: "./confirm-dialog.component.html",
  standalone: true,
  imports: [
    MatDialogModule,
    MatButtonModule,
    IconsModule,
  ],
})
export class ConfirmDialogComponent {
  readonly dialogRef = inject(MatDialogRef<ConfirmDialogComponent>);
  readonly data: ConfirmDialogData = inject(MAT_DIALOG_DATA);

  get message(): string {
    return this.data.message;
  }

  get confirmText(): string {
    return this.data.confirmText ?? "Confirmar";
  }

  get cancelText(): string {
    return this.data.cancelText ?? "Cancelar";
  }
}
