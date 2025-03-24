import { Component, inject } from "@angular/core";
import { CommonModule } from "@angular/common";
import {
  MatDialogModule,
  MAT_DIALOG_DATA,
  MatDialogRef,
} from "@angular/material/dialog";
import { MatButtonModule } from "@angular/material/button";

@Component({
  selector: "app-confirm-dialog",
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>Confirmar</h2>
    <mat-dialog-content>{{ data.message }}</mat-dialog-content>
    <mat-dialog-actions>
      <button mat-button [mat-dialog-close]="false">No</button>
      <button mat-button [mat-dialog-close]="true" cdkFocusInitial>Sí</button>
    </mat-dialog-actions>
  `,
})
export class ConfirmDialogComponent {
  data: { message: string };
  constructor(public dialogRef: MatDialogRef<ConfirmDialogComponent>) {
    this.data = inject(MAT_DIALOG_DATA);
  }
}
