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
import { SystemParameterUpdateDtoInterface } from "../../../../domain/dto/config.dto";
import { SystemParameterInterface } from "../../../../domain/interfaces/config.interface";

@Component({
  selector: "app-edit-system-parameter-dialog",
  templateUrl: "./edit-system-parameter-dialog.component.html",
  imports: [
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatInputModule,
  ],
})
export class EditSystemParameterDialogComponent {
  systemParameterForm: FormGroup;
  data: { systemParameter: SystemParameterInterface };
  dialogRef = inject(MatDialogRef<EditSystemParameterDialogComponent>);

  constructor() {
    this.data = inject(MAT_DIALOG_DATA);
    this.systemParameterForm = new FormGroup({
      id: new FormControl<number>(this.data.systemParameter.id, [
        Validators.required,
      ]),
      description: new FormControl<string>(
        this.data.systemParameter.description,
        [Validators.required]
      ),
      value: new FormControl<string>(this.data.systemParameter.value, [
        Validators.required,
      ]),
    });
  }

  systemParameterData(): SystemParameterUpdateDtoInterface {
    const systemParameterData: SystemParameterUpdateDtoInterface = {
      id: this.systemParameterForm.value.id,
      value: this.systemParameterForm.value.value,
    };

    return systemParameterData;
  }
}
