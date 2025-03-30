import { Component, inject } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import { MatDialogModule, MatDialogRef } from "@angular/material/dialog";
import { MatFormFieldModule } from "@angular/material/form-field";
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import { ApiResponseInterface } from "../../../../domain/interfaces/api-response.interface";
import { MatIconModule } from "@angular/material/icon";
import { PermissionService } from "../../../../services/permission.service";
import { PermissionInterface } from "../../../../domain/interfaces/permission.interface";

@Component({
  selector: "app-create-role-dialog",
  templateUrl: "./create-role-dialog.component.html",
  standalone: true,
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
export class CreateRoleDialogComponent {
  dialogRef = inject(MatDialogRef<CreateRoleDialogComponent>);
  permissionService = inject(PermissionService);
  roleForm: FormGroup = new FormGroup({});
  permissions: PermissionInterface[] = [];

  constructor() {
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
      role: new FormControl<string>("", [Validators.required]),
      permissionsList: new FormControl<PermissionInterface[]>([]),
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
