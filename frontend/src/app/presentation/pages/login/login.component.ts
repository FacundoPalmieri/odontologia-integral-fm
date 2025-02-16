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
import { IconsModule } from "../../../utils/tabler-icons.module";

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
    IconsModule,
  ],
})
export class LoginComponent {
  loginForm: FormGroup;
  hide = signal(true);

  constructor() {
    this.loginForm = new FormGroup({
      username: new FormControl<string>("", [
        Validators.required,
        Validators.email,
      ]),
      password: new FormControl<string>("", [Validators.required]),
    });
  }
}
