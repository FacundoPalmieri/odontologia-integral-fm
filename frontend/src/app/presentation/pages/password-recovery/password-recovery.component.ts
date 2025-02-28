import { Component, inject, OnInit, signal } from "@angular/core";
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
import { Subscription } from "rxjs";
import { ActivatedRoute } from "@angular/router";
import { AuthService } from "../../../services/auth.service";
import { ResetPasswordInterface } from "../../../domain/interfaces/reset-password.interface";
import { SnackbarTypeEnum } from "../../../utils/enums/snackbar-type.enum";
import { SnackbarService } from "../../../services/snackbar.service";
import { LoaderService } from "../../../services/loader.service";
import { ApiResponseInterface } from "../../../domain/interfaces/api-error.interface";

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
export class PasswordRecoveryComponent implements OnInit {
  private routeSub: Subscription | undefined;

  route = inject(ActivatedRoute);
  authService = inject(AuthService);
  snackbarService = inject(SnackbarService);
  loaderService = inject(LoaderService);
  hidePassword = signal(true);
  passwordRecoveryForm: FormGroup;
  token: string | null = null;
  hideResetPasswordForm = signal(false);

  constructor() {
    this.passwordRecoveryForm = new FormGroup(
      {
        password: new FormControl<string>("", [Validators.required]),
        confirmPassword: new FormControl<string>("", [Validators.required]),
      },
      { validators: this.passwordsMatchValidator }
    );
  }

  ngOnInit(): void {
    this.routeSub = this.route.queryParams.subscribe((params) => {
      this.token = params["token"];
    });
  }

  ngOnDestroy(): void {
    if (this.routeSub) {
      this.routeSub.unsubscribe();
    }
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

  resetPassword() {
    if (this.passwordRecoveryForm.invalid || !this.token) return;

    const { password, confirmPassword } = this.passwordRecoveryForm.value;
    const resetData: ResetPasswordInterface = {
      token: this.token,
      newPassword1: password,
      newPassword2: confirmPassword,
    };

    this.loaderService.show();
    this.authService.resetPassword(resetData).subscribe({
      next: (response: ApiResponseInterface) => {
        this.loaderService.hide();
        this.snackbarService.openSnackbar(
          response.message,
          3000,
          "center",
          "top",
          SnackbarTypeEnum.Success
        );
        this.hideResetPasswordForm.set(true);
      },
      error: () => {
        this.loaderService.hide();
      },
    });
  }

  get isFormValid(): boolean {
    return this.passwordRecoveryForm.valid;
  }
}
