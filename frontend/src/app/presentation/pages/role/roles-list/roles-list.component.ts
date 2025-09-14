import {
  Component,
  inject,
  ViewChildren,
  signal,
  effect,
  OnDestroy,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatCardModule } from "@angular/material/card";
import { MatTableDataSource, MatTableModule } from "@angular/material/table";
import { MatPaginator, MatPaginatorModule } from "@angular/material/paginator";
import { MatSort, MatSortModule } from "@angular/material/sort";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatButtonModule } from "@angular/material/button";
import { MatChipsModule } from "@angular/material/chips";
import { MatDialog, MatDialogModule } from "@angular/material/dialog";
import { MatFormFieldModule } from "@angular/material/form-field";
import { FormControl, ReactiveFormsModule } from "@angular/forms";
import { MatInputModule } from "@angular/material/input";
import { Subject, takeUntil } from "rxjs";
import { IconsModule } from "../../../../utils/tabler-icons.module";
import { PageToolbarComponent } from "../../../components/page-toolbar/page-toolbar.component";
import { AccessControlService } from "../../../../services/access-control.service";
import { RoleService } from "../../../../services/role.service";
import { SnackbarService } from "../../../../services/snackbar.service";
import { RoleInterface } from "../../../../domain/interfaces/role.interface";
import {
  ActionsEnum,
  PermissionsEnum,
} from "../../../../utils/enums/permissions.enum";
import { ApiResponseInterface } from "../../../../domain/interfaces/api-response.interface";
import { RoleEditDialogComponent } from "../role-edit-dialog/role-edit-dialog.component";
import { SnackbarTypeEnum } from "../../../../utils/enums/snackbar-type.enum";

@Component({
  selector: "app-roles-list",
  templateUrl: "./roles-list.component.html",
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
export class RolesListComponent implements OnDestroy {
  private readonly _destroy$ = new Subject<void>();
  private readonly accessControlService = inject(AccessControlService);
  private readonly dialog = inject(MatDialog);
  private readonly roleService = inject(RoleService);
  private readonly snackbarService = inject(SnackbarService);

  roles = signal<RoleInterface[]>([]);

  roleFilter = new FormControl("");
  rolesDataSource = new MatTableDataSource<RoleInterface>([]);

  @ViewChildren(MatPaginator) paginator!: MatPaginator;
  @ViewChildren(MatSort) sort!: MatSort;

  roleDisplayedColumns: string[] = ["id", "name", "label"];

  canCreate = signal<boolean>(false);
  canRead = signal<boolean>(false);
  canUpdate = signal<boolean>(false);

  constructor() {
    this._loadInitialData();
    this._setupFilters();

    effect(() => {
      if (this.roles()) {
        this.rolesDataSource.data = this.roles();
        if (this.paginator && this.sort) {
          this.rolesDataSource.paginator = this.paginator;
          this.rolesDataSource.sort = this.sort;
        }
      }
    });

    effect(() => {
      if (this.canUpdate()) {
        this.roleDisplayedColumns.push("action");
      }
    });
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  editRole(role: RoleInterface) {
    if (role != null) {
      this.roleService
        .getById(role.id)
        .subscribe((response: ApiResponseInterface<RoleInterface>) => {
          const dialogRef = this.dialog.open(RoleEditDialogComponent, {
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

  private _loadRoles() {
    this.roleService
      .getAll()
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<RoleInterface[]>) => {
        this.roles.set(response.data);
      });
  }

  private _setupFilters() {
    this.roleFilter.valueChanges.subscribe((filterValue) => {
      this.rolesDataSource.filter = filterValue?.trim().toLowerCase()!;

      if (this.rolesDataSource.paginator) {
        this.rolesDataSource.paginator.firstPage();
      }
    });
  }
}
