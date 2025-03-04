import { Component, inject } from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MatToolbarModule } from "@angular/material/toolbar";
import { PageToolbarComponent } from "../../components/page-toolbar/page-toolbar.component";
import { MatFormFieldModule } from "@angular/material/form-field";
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { MatInputModule } from "@angular/material/input";
import { MatCardModule } from "@angular/material/card";
import { DevService } from "../../../services/dev.service";
import { ApiResponseInterface } from "../../../domain/interfaces/api-response.interface";
import { MatButtonModule } from "@angular/material/button";
import { SnackbarService } from "../../../services/snackbar.service";
import { SnackbarTypeEnum } from "../../../utils/enums/snackbar-type.enum";

@Component({
  selector: "app-system",
  templateUrl: "./system.component.html",
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    MatToolbarModule,
    PageToolbarComponent,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatInputModule,
    MatCardModule,
    MatButtonModule,
  ],
})
export class SystemComponent {
  devService = inject(DevService);
  snackbarService = inject(SnackbarService);
  tokenForm: FormGroup;

  constructor() {
    this.tokenForm = new FormGroup({
      expiration: new FormControl<number>(0, [Validators.required]),
      attempts: new FormControl<number>(0, [Validators.required]),
    });
    this.loadInitialData();
  }

  loadInitialData() {
    this.devService
      .getTokenExpirationTime()
      .subscribe((response: ApiResponseInterface<number>) => {
        if (response.success) {
          this.tokenForm.patchValue({
            expiration: response.data,
          });
        }
      });
    this.devService
      .getFailedAttempts()
      .subscribe((response: ApiResponseInterface<number>) => {
        if (response.success) {
          this.tokenForm.patchValue({
            attempts: response.data,
          });
        }
      });
  }

  setTokenExpirationTime() {
    const time = this.tokenForm.value.expiration;

    this.devService
      .setTokenExpirationTime(time)
      .subscribe((response: ApiResponseInterface<number>) => {
        this.snackbarService.openSnackbar(
          response.message,
          3000,
          "center",
          "top",
          SnackbarTypeEnum.Success
        );
      });
  }

  setFailedAttempts() {
    const attempts = this.tokenForm.value.attempts;

    this.devService
      .setFailedAttempts(attempts)
      .subscribe((response: ApiResponseInterface<number>) => {
        this.snackbarService.openSnackbar(
          response.message,
          3000,
          "center",
          "top",
          SnackbarTypeEnum.Success
        );
      });
  }
}
