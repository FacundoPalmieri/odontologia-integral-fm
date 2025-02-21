import { Component, inject, signal } from "@angular/core";
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatIconModule } from "@angular/material/icon";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { AuthService } from "../../../services/auth.service";
import { LoginInterface } from "../../../domain/interfaces/login.interface";
import { AuthUserInterface } from "../../../domain/interfaces/auth-user.interface";

@Component({
  selector: "app-login",
  templateUrl: "./login.component.html",
  styleUrls: ["./login.component.scss"],
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
    MatIconModule,
    IconsModule,
  ],
})
export class LoginComponent {
  authService = inject(AuthService);
  loginForm: FormGroup;
  forgotPasswordForm: FormGroup;
  hidePassword = signal(true);
  isForgotPassword = signal(false);

  constructor() {
    this.loginForm = new FormGroup({
      username: new FormControl<string>(
        "matiasnicolasiglesiasseliman@gmail.com",
        [Validators.required, Validators.email]
      ),
      password: new FormControl<string>("$MatiasIglesias12345678", [
        Validators.required,
      ]),
    });

    this.forgotPasswordForm = new FormGroup({
      email: new FormControl<string>("", [
        Validators.required,
        Validators.email,
      ]),
    });
  }

  clickEvent(event: MouseEvent) {
    this.hidePassword.set(!this.hidePassword());
    event.stopPropagation();
  }

  toggleForgotPassword() {
    this.isForgotPassword.set(!this.isForgotPassword());
  }

  login() {
    if (this.loginForm.invalid) return;

    const loginData: LoginInterface = this.loginForm.value;

    this.authService.logIn(loginData).subscribe({
      next: (response: AuthUserInterface) => {
        this.authService.doLogin(response);
      },
      error: (error) => {
        console.error("Error en el login:", error);
      },
    });
  }
}
