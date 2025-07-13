import {
  Component,
  inject,
  QueryList,
  ViewChildren,
  signal,
  effect,
  OnDestroy,
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
export class ConfigurationComponent implements OnDestroy {
  private readonly _destroy$ = new Subject<void>();
  private readonly loaderService = inject(LoaderService);
  readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);
  userService = inject(UserService);
  personDataService = inject(PersonDataService);
  roleService = inject(RoleService);
  permissionService = inject(PermissionService);
  snackbarService = inject(SnackbarService);

  users = signal<UserDtoInterface[]>([]);
  roles = signal<RoleInterface[]>([]);

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
    "action",
  ];
  roleDisplayedColumns: string[] = ["id", "role", "permissionsList", "action"];

  constructor() {
    this._loadInitialData();
    this._setupFilters();

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
      const dialogRef = this.dialog.open(EditRoleDialogComponent, {
        data: { role: role },
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
      .pipe(takeUntil(this._destroy$))
      .subscribe(
        (
          response: ApiResponseInterface<PagedDataInterface<UserDtoInterface[]>>
        ) => {
          const users = response.data.content;
          this.users.set(users);

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
