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
import { MatSelectModule } from "@angular/material/select";
import { RoleInterface } from "../../../../domain/interfaces/role.interface";
import { ApiResponseInterface } from "../../../../domain/interfaces/api-response.interface";
import { PermissionService } from "../../../../services/permission.service";
import { PermissionInterface } from "../../../../domain/interfaces/permission.interface";

@Component({
  selector: "app-edit-role-dialog",
  templateUrl: "./edit-role-dialog.component.html",
  imports: [
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatInputModule,
    MatSelectModule,
  ],
})
export class EditRoleDialogComponent {
  dialogRef = inject(MatDialogRef<EditRoleDialogComponent>);
  permissionService = inject(PermissionService);
  roleForm: FormGroup = new FormGroup({});
  data: { role: RoleInterface };
  permissions: PermissionInterface[] = [];

  constructor() {
    this.data = inject(MAT_DIALOG_DATA);
    this._loadForm();
    this._loadData();
  }

  comparePermissions(
    permission1: PermissionInterface,
    permission2: PermissionInterface
  ): boolean {
    return permission1 && permission2 && permission1.id === permission2.id;
  }

  private _loadForm() {
    this.roleForm = new FormGroup({
      id: new FormControl<number>(this.data.role.id, [Validators.required]),
      role: new FormControl<string>(this.data.role.role, [Validators.required]),
      permissionsList: new FormControl<PermissionInterface[]>(
        this.data.role.permissionsList,
        [Validators.required]
      ),
    });
  }

  private _loadData() {
    this.permissionService
      .getAll()
      .subscribe((response: ApiResponseInterface<PermissionInterface[]>) => {
        this.permissions = response.data;
      });
  }
}
