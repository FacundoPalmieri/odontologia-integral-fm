import { Component, OnInit, signal, inject, ChangeDetectionStrategy } from "@angular/core";
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { MatButtonModule } from "@angular/material/button";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatSelectModule } from "@angular/material/select";
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from "@angular/forms";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { PrestationService } from "../../../services/prestation.service";
import { PrestationDto } from "../../../data/interfaces/prestation.interface";

@Component({
  selector: "app-add-prestation-dialog",
  templateUrl: "./add-prestation-dialog.component.html",
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatSelectModule,
    ReactiveFormsModule,
    IconsModule,
  ],
})
export class AddPrestationDialogComponent implements OnInit {
  dialogRef = inject(MatDialogRef<AddPrestationDialogComponent>);
  prestationService = inject(PrestationService);

  prestationForm = new FormGroup({
    prestation: new FormControl<number | null>(null, Validators.required),
  });

  prestations = signal<PrestationDto[]>([]);

  ngOnInit() {
    this.prestationService.getAll().subscribe({
      next: (response) => {
        if (response.data) {
          this.prestations.set(response.data);
        }
      },
      error: (err) => {
        console.error("Error loading prestations:", err);
      },
    });
  }

  confirm() {
    if (this.prestationForm.invalid) return;
    const selectedId = this.prestationForm.value.prestation;
    const selected = this.prestations().find((p) => p.id === selectedId);
    this.dialogRef.close(selected);
  }
}
