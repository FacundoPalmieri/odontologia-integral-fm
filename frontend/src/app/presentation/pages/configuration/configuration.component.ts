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
import { RoleInterface } from "../../../domain/interfaces/rol.interface";
import { PermissionInterface } from "../../../domain/interfaces/permission.interface";
import { ApiResponseInterface } from "../../../domain/interfaces/api-response.interface";
import { MatPaginator, MatPaginatorModule } from "@angular/material/paginator";
import { MatSort, MatSortModule } from "@angular/material/sort";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatButtonModule } from "@angular/material/button";
import { MatChipsModule } from "@angular/material/chips";

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
  ],
})
export class ConfigurationComponent {
  userService = inject(UserService);
  roleService = inject(RoleService);
  permissionService = inject(PermissionService);

  users = signal<UserInterface[]>([]);
  roles = signal<RoleInterface[]>([]);
  permissions = signal<PermissionInterface[]>([]);

  usersDataSource = new MatTableDataSource<UserInterface>([]);
  rolesDataSource = new MatTableDataSource<RoleInterface>([]);
  permissionsDataSource = new MatTableDataSource<PermissionInterface>([]);

  @ViewChildren(MatPaginator) paginators!: QueryList<MatPaginator>;
  @ViewChildren(MatSort) sorts!: QueryList<MatSort>;

  userDisplayedColumns: string[] = [
    "id",
    "username",
    "rolesList",
    "enabled",
    "action",
  ];
  roleDisplayedColumns: string[] = ["id", "role", "permissionList", "action"];
  permissionDisplayedColumns: string[] = ["id", "permissionName", "action"];

  constructor() {
    this.loadInitialData();

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

    effect(() => {
      if (this.permissions()) {
        this.permissionsDataSource.data = this.permissions();
        if (this.paginators && this.sorts) {
          this.permissionsDataSource.paginator = this.paginators.toArray()[2];
          this.permissionsDataSource.sort = this.sorts.toArray()[2];
        }
      }
    });
  }

  loadInitialData() {
    this.userService
      .getAll()
      .subscribe((response: ApiResponseInterface<UserInterface[]>) => {
        this.users.set(response.data);
      });
    this.roleService
      .getAll()
      .subscribe((response: ApiResponseInterface<RoleInterface[]>) => {
        this.roles.set(response.data);
      });
    this.permissionService
      .getAll()
      .subscribe((response: ApiResponseInterface<PermissionInterface[]>) => {
        this.permissions.set(response.data);
      });
  }
}
