import { Component, inject, signal } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from "@angular/material/dialog";
import { MatFormFieldModule } from "@angular/material/form-field";
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from "@angular/forms";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import { RoleInterface } from "../../../../domain/interfaces/rol.interface";
import { RoleService } from "../../../../services/role.service";
import { ApiResponseInterface } from "../../../../domain/interfaces/api-response.interface";
import { MatIconModule } from "@angular/material/icon";

@Component({
  selector: "app-create-user-dialog",
  templateUrl: "./create-user-dialog.component.html",
  imports: [
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatInputModule,
    MatSelectModule,
    MatIconModule,
  ],
})
export class CreateUserDialogComponent {
  dialogRef = inject(MatDialogRef<CreateUserDialogComponent>);
  roleService = inject(RoleService);
  hidePassword = signal(true);
  userForm: FormGroup = new FormGroup({});
  roles: RoleInterface[] = [];

  constructor() {
    this._loadForm();
    this._loadData();
  }

  compareRoles(role1: RoleInterface, role2: RoleInterface): boolean {
    return role1 && role2 && role1.id === role2.id;
  }

  togglePasswordVisibility(): void {
    this.hidePassword.set(!this.hidePassword());
  }

  private passwordsMatchValidator: ValidatorFn = (
    control: AbstractControl
  ): ValidationErrors | null => {
    const formGroup = control as FormGroup;
    const password = formGroup.get("password1")?.value;
    const confirmPassword = formGroup.get("password2")?.value;

    return password === confirmPassword ? null : { passwordsMismatch: true };
  };

  private _loadForm() {
    this.userForm = new FormGroup(
      {
        username: new FormControl<string>("", [
          Validators.required,
          Validators.email,
        ]),
        password1: new FormControl<string>("", [Validators.required]),
        password2: new FormControl<string>("", [Validators.required]),
        rolesList: new FormControl<RoleInterface[]>([], [Validators.required]),
      },
      { validators: this.passwordsMatchValidator }
    );
  }

  private _loadData() {
    this.roleService
      .getAll()
      .subscribe((response: ApiResponseInterface<RoleInterface[]>) => {
        this.roles = response.data;
      });
  }
}
