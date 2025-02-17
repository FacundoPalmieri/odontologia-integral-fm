import { Component, signal } from "@angular/core";
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from "@angular/forms";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatIconModule } from "@angular/material/icon";

@Component({
  selector: "app-password-recovery",
  templateUrl: "./password-recovery.component.html",
  styleUrls: ["./password-recovery.component.scss"],
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
  ],
})
export class PasswordRecoveryComponent {
  passwordRecoveryForm: FormGroup;
  hidePassword = signal(true);

  constructor() {
    this.passwordRecoveryForm = new FormGroup(
      {
        password: new FormControl<string>("", [Validators.required]),
        confirmPassword: new FormControl<string>("", [Validators.required]),
      },
      { validators: this.passwordsMatchValidator }
    );
  }

  togglePasswordVisibility(): void {
    this.hidePassword.set(!this.hidePassword());
  }

  private passwordsMatchValidator: ValidatorFn = (
    control: AbstractControl
  ): ValidationErrors | null => {
    const formGroup = control as FormGroup;
    const password = formGroup.get("password")?.value;
    const confirmPassword = formGroup.get("confirmPassword")?.value;

    return password === confirmPassword ? null : { passwordsMismatch: true };
  };

  get isFormValid(): boolean {
    return this.passwordRecoveryForm.valid;
  }
}
