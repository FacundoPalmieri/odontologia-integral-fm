import {
  Component,
  inject,
  QueryList,
  ViewChildren,
  signal,
  effect,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MatToolbarModule } from "@angular/material/toolbar";
import { PageToolbarComponent } from "../../components/page-toolbar/page-toolbar.component";
import { MatCardModule } from "@angular/material/card";
import { UserService } from "../../../services/user.service";
import { RoleService } from "../../../services/role.service";
import { PermissionService } from "../../../services/permission.service";
import { MatTableDataSource, MatTableModule } from "@angular/material/table";
import { UserInterface } from "../../../domain/interfaces/user.interface";
import { RoleInterface } from "../../../domain/interfaces/role.interface";
import { ApiResponseInterface } from "../../../domain/interfaces/api-response.interface";
import { MatPaginator, MatPaginatorModule } from "@angular/material/paginator";
import { MatSort, MatSortModule } from "@angular/material/sort";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatButtonModule } from "@angular/material/button";
import { MatChipsModule } from "@angular/material/chips";
import { MatDialog, MatDialogModule } from "@angular/material/dialog";
import { EditUserDialogComponent } from "./edit-user-dialog/edit-user-dialog.component";
import { SnackbarService } from "../../../services/snackbar.service";
import { SnackbarTypeEnum } from "../../../utils/enums/snackbar-type.enum";
import { UserUpdateDto } from "../../../domain/dto/user-update.dto";
import { CreateUserDialogComponent } from "./create-user-dialog/create-user-dialog.component";
import { UserCreateDto } from "../../../domain/dto/user-create.dto";
import { EditRoleDialogComponent } from "./edit-role-dialog/edit-role-dialog.component";
import { RoleUpdateDto } from "../../../domain/dto/role-update.dto";
import { CreateRoleDialogComponent } from "./create-role-dialog/create-role-dialog.component";
import { RoleCreateDto } from "../../../domain/dto/role-create.dto";

@Component({
  selector: "app-configuration",
  templateUrl: "./configuration.component.html",
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    MatToolbarModule,
    PageToolbarComponent,
    MatCardModule,
    MatTableModule,
    MatTooltipModule,
    MatButtonModule,
    MatChipsModule,
    MatPaginatorModule,
    MatSortModule,
    MatDialogModule,
  ],
})
export class ConfigurationComponent {
  readonly dialog = inject(MatDialog);
  userService = inject(UserService);
  roleService = inject(RoleService);
  permissionService = inject(PermissionService);
  snackbarService = inject(SnackbarService);

  users = signal<UserInterface[]>([]);
  roles = signal<RoleInterface[]>([]);

  usersDataSource = new MatTableDataSource<UserInterface>([]);
  rolesDataSource = new MatTableDataSource<RoleInterface>([]);

  @ViewChildren(MatPaginator) paginators!: QueryList<MatPaginator>;
  @ViewChildren(MatSort) sorts!: QueryList<MatSort>;

  userDisplayedColumns: string[] = [
    "id",
    "username",
    "rolesList",
    "enabled",
    "action",
  ];
  roleDisplayedColumns: string[] = ["id", "role", "permissionsList", "action"];

  constructor() {
    this._loadInitialData();

    effect(() => {
      if (this.users()) {
        this.usersDataSource.data = this.users();
        if (this.paginators && this.sorts) {
          this.usersDataSource.paginator = this.paginators.toArray()[0];
          this.usersDataSource.sort = this.sorts.toArray()[0];
        }
      }
    });

    effect(() => {
      if (this.roles()) {
        this.rolesDataSource.data = this.roles();
        if (this.paginators && this.sorts) {
          this.rolesDataSource.paginator = this.paginators.toArray()[1];
          this.rolesDataSource.sort = this.sorts.toArray()[1];
        }
      }
    });
  }

  createUser() {
    const dialogRef = this.dialog.open(CreateUserDialogComponent);
    dialogRef.afterClosed().subscribe((user: UserCreateDto) => {
      if (user) {
        this.userService
          .create(user)
          .subscribe((response: ApiResponseInterface<UserInterface>) => {
            this.snackbarService.openSnackbar(
              response.message,
              3000,
              "center",
              "top",
              SnackbarTypeEnum.Success
            );
            this._loadUsers();
          });
      }
    });
  }

  editUser(user: UserInterface) {
    if (user != null) {
      const dialogRef = this.dialog.open(EditUserDialogComponent, {
        data: { user: user },
      });
      dialogRef.afterClosed().subscribe((user: UserInterface) => {
        if (user) {
          const userDto: UserUpdateDto = {
            id: user.id,
            enabled: user.enabled,
            rolesList: user.rolesList.map((role) => role.id),
          };
          this.userService
            .update(userDto)
            .subscribe((response: ApiResponseInterface<UserInterface>) => {
              this.snackbarService.openSnackbar(
                response.message,
                6000,
                "center",
                "top",
                SnackbarTypeEnum.Success
              );
              this._loadUsers();
            });
        }
      });
    } else {
      this.snackbarService.openSnackbar(
        "Ocurrió un error el editar el elemento",
        6000,
        "center",
        "bottom",
        SnackbarTypeEnum.Error
      );
    }
  }

  createRole() {
    const dialogRef = this.dialog.open(CreateRoleDialogComponent);
    dialogRef.afterClosed().subscribe((role: RoleCreateDto) => {
      if (role) {
        this.roleService
          .create(role)
          .subscribe((response: ApiResponseInterface<RoleInterface>) => {
            this.snackbarService.openSnackbar(
              response.message,
              3000,
              "center",
              "top",
              SnackbarTypeEnum.Success
            );
            this._loadRoles();
          });
      }
    });
  }

  editRole(role: RoleInterface) {
    if (role != null) {
      const dialogRef = this.dialog.open(EditRoleDialogComponent, {
        data: { role: role },
      });
      dialogRef.afterClosed().subscribe((role: RoleInterface) => {
        if (role) {
          const roleDto: RoleUpdateDto = {
            id: role.id,
            role: role.role,
            permissionsList: role.permissionsList,
          };
          this.roleService
            .update(roleDto)
            .subscribe((response: ApiResponseInterface<RoleInterface>) => {
              this.snackbarService.openSnackbar(
                response.message,
                6000,
                "center",
                "top",
                SnackbarTypeEnum.Success
              );
              this._loadRoles();
            });
        }
      });
    } else {
      this.snackbarService.openSnackbar(
        "Ocurrió un error el editar el elemento",
        6000,
        "center",
        "bottom",
        SnackbarTypeEnum.Error
      );
    }
  }

  private _loadInitialData() {
    this._loadUsers();
    this._loadRoles();
  }

  private _loadUsers() {
    this.userService
      .getAll()
      .subscribe((response: ApiResponseInterface<UserInterface[]>) => {
        this.users.set(response.data);
      });
  }

  private _loadRoles() {
    this.roleService
      .getAll()
      .subscribe((response: ApiResponseInterface<RoleInterface[]>) => {
        this.roles.set(response.data);
      });
  }
}
