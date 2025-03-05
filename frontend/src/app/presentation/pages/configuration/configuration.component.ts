import { Component, inject, QueryList, ViewChildren } from "@angular/core";
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
import { MatPaginator } from "@angular/material/paginator";
import { MatSort } from "@angular/material/sort";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatButtonModule } from "@angular/material/button";

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
    MatPaginator,
    MatTooltipModule,
    MatButtonModule,
  ],
})
export class ConfigurationComponent {
  userService = inject(UserService);
  roleService = inject(RoleService);
  permissionService = inject(PermissionService);
  users: MatTableDataSource<UserInterface> = new MatTableDataSource();
  roles: MatTableDataSource<RoleInterface> = new MatTableDataSource();
  permissions: MatTableDataSource<PermissionInterface> =
    new MatTableDataSource();

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
  }

  loadInitialData() {
    this.userService
      .getAll()
      .subscribe((response: ApiResponseInterface<UserInterface[]>) => {
        this.users = new MatTableDataSource(response.data);
        this.users.paginator = this.paginators.toArray()[0];
        this.users.sort = this.sorts.toArray()[0];
      });
    this.roleService
      .getAll()
      .subscribe((response: ApiResponseInterface<RoleInterface[]>) => {
        this.roles = new MatTableDataSource(response.data);
        this.roles.paginator = this.paginators.toArray()[1];
        this.roles.sort = this.sorts.toArray()[1];
      });
    this.permissionService
      .getAll()
      .subscribe((response: ApiResponseInterface<PermissionInterface[]>) => {
        this.permissions = new MatTableDataSource(response.data);
        this.permissions.paginator = this.paginators.toArray()[2];
        this.permissions.sort = this.sorts.toArray()[2];
      });
  }
}
