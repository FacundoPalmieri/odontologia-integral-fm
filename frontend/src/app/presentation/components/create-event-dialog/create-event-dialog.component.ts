import { Component, inject } from "@angular/core";
import { MatDialogModule, MatDialogRef } from "@angular/material/dialog";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatNativeDateModule } from "@angular/material/core";
import { MatButtonModule } from "@angular/material/button";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { MatIconModule } from "@angular/material/icon";
import { MatChipsModule } from "@angular/material/chips";
import { MatAutocompleteModule } from "@angular/material/autocomplete";
import { FormsModule } from "@angular/forms";
import { IconsModule } from "../../../utils/tabler-icons.module";

@Component({
  selector: "app-create-event-dialog",
  templateUrl: "./create-event-dialog.component.html",
  standalone: true,
  imports: [
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatButtonModule,
    MatCheckboxModule,
    MatIconModule,
    MatChipsModule,
    MatAutocompleteModule,
    FormsModule,
    IconsModule,
  ],
})
export class CreateEventDialogComponent {
  dialogRef = inject(MatDialogRef<CreateEventDialogComponent>);

  // Mock data for demonstration
  isAllDay = false;
  selectedPatients: any[] = [];
  selectedProfessional: any = null;

  // Mock data
  patients = [
    { id: 1, name: "Juan Pérez", specialty: "Odontología General" },
    { id: 2, name: "María García", specialty: "Ortodoncia" },
    { id: 3, name: "Carlos López", specialty: "Endodoncia" },
  ];

  professionals = [
    { id: 1, name: "Dr. Ana Martínez", specialty: "Odontología General" },
    { id: 2, name: "Dr. Pedro Rodríguez", specialty: "Ortodoncia" },
    { id: 3, name: "Dr. Laura Sánchez", specialty: "Periodoncia" },
  ];

  onCancel() {
    this.dialogRef.close();
  }

  onSave() {
    console.log("Guardar cita");
    this.dialogRef.close();
  }
}
