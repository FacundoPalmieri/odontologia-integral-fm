import { Component, inject, signal } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from "@angular/material/dialog";
import { MatFormFieldModule } from "@angular/material/form-field";
import {
  FormArray,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import {
  ActionInterface,
  PermissionInterface,
  RoleInterface,
} from "../../../../domain/interfaces/role.interface";
import { ApiResponseInterface } from "../../../../domain/interfaces/api-response.interface";
import { PermissionService } from "../../../../services/permission.service";
import { ActionService } from "../../../../services/action.service";
import { IconsModule } from "../../../../utils/tabler-icons.module";
import { MatIconModule } from "@angular/material/icon";
import { RoleService } from "../../../../services/role.service";
import { RoleCreateDtoInterface } from "../../../../domain/dto/role.dto";
import { Action } from "rxjs/internal/scheduler/Action";

@Component({
  selector: "app-edit-role-dialog",
  templateUrl: "./edit-role-dialog.component.html",
  standalone: true,
  imports: [
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatInputModule,
    MatSelectModule,
    IconsModule,
    MatIconModule,
  ],
})
export class EditRoleDialogComponent {
  dialogRef = inject(MatDialogRef<EditRoleDialogComponent>);
  permissionService = inject(PermissionService);
  actionService = inject(ActionService);
  roleService = inject(RoleService);

  roleForm: FormGroup = new FormGroup({});
  data: { role: RoleInterface };
  permissions = signal<PermissionInterface[]>([]);
  actions = signal<ActionInterface[]>([]);

  constructor() {
    this.data = inject(MAT_DIALOG_DATA);
    this._loadData();
    this._loadForm();
    this._populatePermissionsList();
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
      name: new FormControl<string>(this.data.role.name, [Validators.required]),
      label: new FormControl<string>(this.data.role.label, [
        Validators.required,
      ]),
      permissionsList: new FormArray([]),
    });
  }

  get permissionsList(): FormArray {
    return this.roleForm.get("permissionsList") as FormArray;
  }

  addPermission() {
    this.permissionsList.push(
      new FormGroup({
        permission: new FormControl<PermissionInterface | null>(null, [
          Validators.required,
        ]),
        actions: new FormControl<ActionInterface[]>([], [Validators.required]),
      })
    );
  }

  removePermission(index: number) {
    this.permissionsList.removeAt(index);
  }

  compare = (
    item1: ActionInterface | PermissionInterface | null,
    item2: ActionInterface | PermissionInterface | null
  ): boolean => {
    return item1 && item2 ? item1.id === item2.id : item1 === item2;
  };

  save() {
    const role: RoleInterface = {
      id: this.roleForm.value.id,
      name: this.roleForm.value.name,
      label: this.roleForm.value.label,
      permissionsList: this.permissionsList.value.map((perm: any) => ({
        id: perm.permission.id,
        name: perm.permission.name,
        label: perm.permission.label,
        actions: perm.actions.map((action: ActionInterface) => action),
      })),
    };

    this.dialogRef.close(role);
  }

  private _loadData() {
    this.permissionService
      .getAll()
      .subscribe((response: ApiResponseInterface<PermissionInterface[]>) => {
        this.permissions.set(response.data);
      });

    this.actionService
      .getAll()
      .subscribe((response: ApiResponseInterface<ActionInterface[]>) => {
        this.actions.set(response.data);
      });
  }

  private _populatePermissionsList() {
    this.permissionsList.clear();

    if (
      this.data.role.permissionsList &&
      Array.isArray(this.data.role.permissionsList)
    ) {
      this.data.role.permissionsList.forEach((perm: PermissionInterface) => {
        this.permissionsList.push(
          new FormGroup({
            permission: new FormControl<PermissionInterface | null>(perm, [
              Validators.required,
            ]),
            actions: new FormControl<ActionInterface[]>(
              perm.actions ? perm.actions.map((a: any) => a) : [],
              [Validators.required]
            ),
          })
        );
      });
    }
  }
}
