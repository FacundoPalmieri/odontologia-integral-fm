import { Component, inject } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from "@angular/material/dialog";
import { MatFormFieldModule } from "@angular/material/form-field";
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { MatInputModule } from "@angular/material/input";
import { SystemParameterInterface } from "../../../data/interfaces/system-parameter.interface";
import { SystemParameterUpdateDto } from "../../../data/dtos/system-parameter.dto";

@Component({
  selector: "app-parameter-edit-dialog",
  templateUrl: "./parameter-edit-dialog.component.html",
  imports: [
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatInputModule,
  ],
})
export class ParametersEditDialogComponent {
  systemParameterForm: FormGroup;
  data: { systemParameter: SystemParameterInterface };
  dialogRef = inject(MatDialogRef<ParametersEditDialogComponent>);

  constructor() {
    this.data = inject(MAT_DIALOG_DATA);
    this.systemParameterForm = new FormGroup({
      id: new FormControl<number>(this.data.systemParameter.id, [
        Validators.required,
      ]),
      description: new FormControl<string>(
        this.data.systemParameter.description,
        [Validators.required],
      ),
      value: new FormControl<string>(this.data.systemParameter.value, [
        Validators.required,
      ]),
    });
  }

  systemParameterData(): SystemParameterUpdateDto {
    const systemParameterData: SystemParameterUpdateDto = {
      id: this.systemParameterForm.value.id,
      value: this.systemParameterForm.value.value,
    };

    return systemParameterData;
  }
}
