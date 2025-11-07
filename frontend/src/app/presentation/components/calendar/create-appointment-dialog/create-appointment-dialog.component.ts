import { Component, inject, ViewChild } from "@angular/core";
import { MatDialogModule, MatDialogRef } from "@angular/material/dialog";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import {
  MatDatepicker,
  MatDatepickerModule,
} from "@angular/material/datepicker";
import { MatNativeDateModule } from "@angular/material/core";
import { MatButtonModule } from "@angular/material/button";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { MatIconModule } from "@angular/material/icon";
import { MatChipsModule } from "@angular/material/chips";
import { MatAutocompleteModule } from "@angular/material/autocomplete";
import { FormsModule } from "@angular/forms";
import { IconsModule } from "../../../../utils/tabler-icons.module";

@Component({
  selector: "app-create-appointment-dialog",
  templateUrl: "./create-appointment-dialog.component.html",
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
export class CreateAppointmentDialogComponent {
  dialogRef = inject(MatDialogRef<CreateAppointmentDialogComponent>);

  @ViewChild("picker") picker!: MatDatepicker<Date>;

  // Mock data for demonstration
  isAllDay = false;
  selectedPatients: any[] = [];
  selectedProfessional: any = null;
  startTime: string = "";
  endTime: string = "";

  timeSlots: string[] = [];

  constructor() {
    this.generateTimeSlots();
  }

  openDatePicker(): void {
    this.picker.open();
  }

  private generateTimeSlots(): void {
    this.timeSlots = [];
    for (let hour = 0; hour < 24; hour++) {
      for (let minute = 0; minute < 60; minute += 30) {
        const timeString = `${hour.toString().padStart(2, "0")}:${minute
          .toString()
          .padStart(2, "0")}`;
        this.timeSlots.push(timeString);
      }
    }
  }

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
    this.dialogRef.close();
  }
}
