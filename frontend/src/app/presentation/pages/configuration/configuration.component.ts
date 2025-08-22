import {
  Component,
  inject,
  QueryList,
  ViewChildren,
  signal,
  effect,
  OnDestroy,
  AfterViewInit,
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
import { RoleInterface } from "../../../domain/interfaces/role.interface";
import {
  ApiResponseInterface,
  PagedDataInterface,
} from "../../../domain/interfaces/api-response.interface";
import { MatPaginator, MatPaginatorModule } from "@angular/material/paginator";
import { MatSort, MatSortModule } from "@angular/material/sort";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatButtonModule } from "@angular/material/button";
import { MatChipsModule } from "@angular/material/chips";
import { MatDialog, MatDialogModule } from "@angular/material/dialog";
import { SnackbarService } from "../../../services/snackbar.service";
import { SnackbarTypeEnum } from "../../../utils/enums/snackbar-type.enum";
import { EditRoleDialogComponent } from "./edit-role-dialog/edit-role-dialog.component";
import { MatFormFieldModule } from "@angular/material/form-field";
import { FormControl, ReactiveFormsModule } from "@angular/forms";
import { MatInputModule } from "@angular/material/input";
import { Subject, takeUntil } from "rxjs";
import { Router } from "@angular/router";
import { UserDtoInterface } from "../../../domain/dto/user.dto";
import { PersonDataService } from "../../../services/person-data.service";
import { LoaderService } from "../../../services/loader.service";
import { AccessControlService } from "../../../services/access-control.service";
import {
  ActionsEnum,
  PermissionsEnum,
} from "../../../utils/enums/permissions.enum";

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
    MatFormFieldModule,
    ReactiveFormsModule,
    MatInputModule,
  ],
})
export class ConfigurationComponent implements OnDestroy, AfterViewInit {
  private readonly _destroy$ = new Subject<void>();
  private readonly loaderService = inject(LoaderService);
  private readonly router = inject(Router);
  private readonly accessControlService = inject(AccessControlService);
  readonly dialog = inject(MatDialog);

  userService = inject(UserService);
  personDataService = inject(PersonDataService);
  roleService = inject(RoleService);
  permissionService = inject(PermissionService);
  snackbarService = inject(SnackbarService);

  users = signal<UserDtoInterface[]>([]);
  roles = signal<RoleInterface[]>([]);

  usersPageSize = signal(5);
  usersPageIndex = signal(0);
  usersSortBy = signal("username");
  usersSortDirection = signal("asc");
  usersTotalElements = signal(0);

  userFilter = new FormControl("");
  usersDataSource = new MatTableDataSource<UserDtoInterface>([]);
  roleFilter = new FormControl("");
  rolesDataSource = new MatTableDataSource<RoleInterface>([]);

  @ViewChildren(MatPaginator) paginators!: QueryList<MatPaginator>;
  @ViewChildren(MatSort) sorts!: QueryList<MatSort>;

  userDisplayedColumns: string[] = [
    "avatar",
    "username",
    "rolesList",
    "enabled",
  ];
  roleDisplayedColumns: string[] = ["id", "name", "label"];

  canCreate = signal<boolean>(false);
  canRead = signal<boolean>(false);
  canUpdate = signal<boolean>(false);

  constructor() {
    this._loadInitialData();
    this._setupFilters();

    effect(() => {
      if (this.users()) {
        this.usersDataSource.data = this.users();
        if (this.paginators && this.paginators.length > 0) {
          this.paginators.toArray()[0].length = this.usersTotalElements();
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
      if (this.canUpdate()) {
        this.roleDisplayedColumns.push("action");
        this.userDisplayedColumns.push("action");
      }
    });
  }

  ngAfterViewInit(): void {
    if (this.paginators.length > 0 && this.sorts.length > 0) {
      const userPaginator = this.paginators.toArray()[0];
      const userSort = this.sorts.toArray()[0];

      userPaginator.page.pipe(takeUntil(this._destroy$)).subscribe((event) => {
        this.usersPageIndex.set(event.pageIndex);
        this.usersPageSize.set(event.pageSize);
        this._loadUsers(
          this.usersPageIndex(),
          this.usersPageSize(),
          this.usersSortBy(),
          this.usersSortDirection()
        );
      });

      userSort.sortChange.pipe(takeUntil(this._destroy$)).subscribe((sort) => {
        this.usersPageIndex.set(0);
        this.usersSortBy.set(sort.active);
        this.usersSortDirection.set(sort.direction);
        // Load users with new sort parameters
        this._loadUsers(
          this.usersPageIndex(),
          this.usersPageSize(),
          this.usersSortBy(),
          this.usersSortDirection()
        );
      });
    }
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  createUser() {
    this.router.navigate(["/configuration/users/create"]);
  }

  editUser(user: UserDtoInterface) {
    this.router.navigate(["/configuration/users/edit", user.id]);
  }

  editRole(role: RoleInterface) {
    if (role != null) {
      this.roleService
        .getById(role.id)
        .subscribe((response: ApiResponseInterface<RoleInterface>) => {
          const dialogRef = this.dialog.open(EditRoleDialogComponent, {
            data: { role: response.data },
          });
          dialogRef.afterClosed().subscribe((role: RoleInterface) => {
            if (role) {
              this.roleService
                .update(role)
                .pipe(takeUntil(this._destroy$))
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
    this._loadUsers(
      this.usersPageIndex(),
      this.usersPageSize(),
      this.usersSortBy(),
      this.usersSortDirection()
    );
    this._loadRoles();
    this._loadPermissionsFlags();
  }

  private _loadPermissionsFlags() {
    this.canCreate.set(
      this.accessControlService.can(
        PermissionsEnum.CONFIGURATION,
        ActionsEnum.CREATE
      )
    );
    this.canRead.set(
      this.accessControlService.can(
        PermissionsEnum.CONFIGURATION,
        ActionsEnum.READ
      )
    );
    this.canUpdate.set(
      this.accessControlService.can(
        PermissionsEnum.CONFIGURATION,
        ActionsEnum.UPDATE
      )
    );
  }

  private _loadUsers(
    page: number,
    size: number,
    sortBy: string,
    direction: string
  ) {
    this.userService
      .getAll(page, size, sortBy, direction)
      .pipe(takeUntil(this._destroy$))
      .subscribe(
        (
          response: ApiResponseInterface<PagedDataInterface<UserDtoInterface[]>>
        ) => {
          const users = response.data?.content;
          this.users.set(users);
          this.usersTotalElements.set(response.data?.totalElements);

          if (this.users()?.length > 0) {
            users.forEach((user) => {
              if (user.person?.id) {
                this.personDataService
                  .getAvatar(user.person.id)
                  .subscribe((avatar: any) => {
                    user.avatarUrl = avatar;
                    this.users.set([...this.users()]);
                  });
              }
            });
          }

          this.loaderService.hide();
        },
        (error) => {
          console.error("Error al cargar usuarios:", error);
          this.loaderService.hide();
          this.snackbarService.openSnackbar(
            "Error al cargar usuarios.",
            6000,
            "center",
            "top",
            SnackbarTypeEnum.Error
          );
        }
      );
  }

  private _loadRoles() {
    this.roleService
      .getAll()
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<RoleInterface[]>) => {
        this.roles.set(response.data);
      });
  }

  private _setupFilters() {
    this.userFilter.valueChanges.subscribe((filterValue) => {
      this.usersDataSource.filter = filterValue?.trim().toLowerCase()!;

      if (this.usersDataSource.paginator) {
        this.usersDataSource.paginator.firstPage();
      }
    });

    this.roleFilter.valueChanges.subscribe((filterValue) => {
      this.rolesDataSource.filter = filterValue?.trim().toLowerCase()!;

      if (this.usersDataSource.paginator) {
        this.usersDataSource.paginator.firstPage();
      }
    });
  }
}
