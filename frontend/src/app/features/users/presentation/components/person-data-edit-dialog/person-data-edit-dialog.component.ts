import { Component, inject } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from "@angular/material/dialog";
import { MatFormFieldModule } from "@angular/material/form-field";
import { ReactiveFormsModule } from "@angular/forms";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import { MatIconModule } from "@angular/material/icon";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { PersonFormComponent } from "../../../../../shared/components/person-form/person-form.component";
import { PersonInterface } from "../../../../../shared/interfaces/person.interface";

@Component({
  selector: "app-person-data-edit-dialog",
  templateUrl: "./person-data-edit-dialog.component.html",
  standalone: true,
  imports: [
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatInputModule,
    MatSelectModule,
    IconsModule,
    MatIconModule,
    PersonFormComponent,
  ],
})
export class PersonDataEditDialogComponent {
  dialogRef = inject(MatDialogRef<PersonDataEditDialogComponent>);
  data: { personData: PersonInterface };

  personData!: PersonInterface;
  isFormValid = false;
  formData!: PersonInterface;

  constructor() {
    this.data = inject(MAT_DIALOG_DATA);
    this.personData = this.data.personData;
  }

  onFormValidChange(isValid: boolean): void {
    this.isFormValid = isValid;
  }

  onFormValueChange(formValue: PersonInterface): void {
    this.formData = formValue;
  }

  save(): void {
    if (this.isFormValid && this.formData) {
      this.dialogRef.close(this.formData);
    }
  }
}
