import { Component, ChangeDetectionStrategy, inject, Inject } from "@angular/core";
import { CommonModule } from "@angular/common";
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from "@angular/forms";
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from "@angular/material/dialog";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatSelectModule } from "@angular/material/select";
import { MatButtonModule } from "@angular/material/button";

@Component({
  selector: "app-change-status-dialog",
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
    MatButtonModule,
  ],
  template: `
    <h2 mat-dialog-title class="!m-0 text-xl font-bold">Cambiar Estado de la Consulta</h2>
    <mat-dialog-content class="!pt-4">
      <form [formGroup]="form" class="flex flex-col gap-4 min-w-[280px]">
        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Nuevo Estado</mat-label>
          <mat-select formControlName="status">
            @for (option of statusOptions; track option.value) {
              <mat-option [value]="option.value">{{ option.label }}</mat-option>
            }
          </mat-select>
          @if (form.get('status')?.invalid && form.get('status')?.touched) {
            <mat-error>El estado es requerido</mat-error>
          }
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end" class="!pb-4 !px-6">
      <button mat-button (click)="onCancel()">Cancelar</button>
      <button
        mat-flat-button
        color="primary"
        [disabled]="form.invalid"
        (click)="onSave()"
      >
        Guardar
      </button>
    </mat-dialog-actions>
  `,
  styles: [
    `
      :host {
        display: block;
      }
    `,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ChangeStatusDialogComponent {
  private readonly dialogRef = inject(MatDialogRef<ChangeStatusDialogComponent>);

  readonly form = new FormGroup({
    status: new FormControl<string>("", {
      nonNullable: true,
      validators: [Validators.required],
    }),
  });

  readonly statusOptions = [
    { value: "PATIENT_RECEIVED", label: "En espera" },
    { value: "ATTENTION_STARTED", label: "En consulta" },
    { value: "ATTENTION_FINISHED", label: "Finalizada" },
  ];

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { currentStatus: string }
  ) {
    // Attempt to map current local status to its websocket enum value for initial state
    const currentMapped = this.mapLocalStatusToSocketEnum(data.currentStatus);
    if (currentMapped) {
      this.form.patchValue({ status: currentMapped });
    }
  }

  private mapLocalStatusToSocketEnum(localStatus: string): string | null {
    const map: { [key: string]: string } = {
      "En espera": "PATIENT_RECEIVED",
      "En consulta": "ATTENTION_STARTED",
      "Finalizada": "ATTENTION_FINISHED",
    };
    return map[localStatus] || null;
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    if (this.form.valid) {
      this.dialogRef.close(this.form.value.status);
    }
  }
}
