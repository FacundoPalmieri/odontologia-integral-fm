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
import { UserInterface } from "../../../../domain/interfaces/user.interface";
import { MatSelectModule } from "@angular/material/select";
import { RoleInterface } from "../../../../domain/interfaces/rol.interface";
import { RoleService } from "../../../../services/role.service";
import { ApiResponseInterface } from "../../../../domain/interfaces/api-response.interface";

@Component({
  selector: "app-edit-user-dialog",
  templateUrl: "./edit-user-dialog.component.html",
  imports: [
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatInputModule,
    MatSelectModule,
  ],
})
export class EditUserDialogComponent {
  dialogRef = inject(MatDialogRef<EditUserDialogComponent>);
  roleService = inject(RoleService);
  userForm: FormGroup = new FormGroup({});
  data: { user: UserInterface };
  roles: RoleInterface[] = [];

  constructor() {
    this.data = inject(MAT_DIALOG_DATA);
    this._loadForm();
    this._loadData();
  }

  compareRoles(role1: RoleInterface, role2: RoleInterface): boolean {
    return role1 && role2 && role1.id === role2.id;
  }

  private _loadForm() {
    this.userForm = new FormGroup({
      id: new FormControl<number>(this.data.user.id, [Validators.required]),
      username: new FormControl<string>(this.data.user.username, [
        Validators.required,
        Validators.email,
      ]),
      rolesList: new FormControl<RoleInterface[]>(this.data.user.rolesList, [
        Validators.required,
      ]),
      enabled: new FormControl<boolean>(this.data.user.enabled, [
        Validators.required,
      ]),
    });
  }

  private _loadData() {
    this.roleService
      .getAll()
      .subscribe((response: ApiResponseInterface<RoleInterface[]>) => {
        this.roles = response.data;
      });
  }
}
