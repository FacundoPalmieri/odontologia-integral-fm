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
import { MessageInterface } from "../../../../domain/interfaces/config.interface";

@Component({
  selector: "app-edit-message-dialog",
  templateUrl: "./edit-message-dialog.component.html",
  imports: [
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatInputModule,
  ],
})
export class EditMessageDialogComponent {
  messageForm: FormGroup;
  data: { message: MessageInterface };
  dialogRef = inject(MatDialogRef<EditMessageDialogComponent>);

  constructor() {
    this.data = inject(MAT_DIALOG_DATA);
    this.messageForm = new FormGroup({
      id: new FormControl<number>(this.data.message.id, [Validators.required]),
      key: new FormControl<string>(this.data.message.key, [
        Validators.required,
      ]),
      value: new FormControl<string>(this.data.message.value, [
        Validators.required,
      ]),
      locale: new FormControl<string>(this.data.message.locale, [
        Validators.required,
      ]),
    });
  }
}
