import {
  Component,
  effect,
  inject,
  OnDestroy,
  signal,
  ViewChild,
  OnInit,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatFormFieldModule } from "@angular/material/form-field";
import { FormControl, ReactiveFormsModule } from "@angular/forms";
import { MatInputModule } from "@angular/material/input";
import { MatCardModule } from "@angular/material/card";
import { MatButtonModule } from "@angular/material/button";
import { MatTableDataSource, MatTableModule } from "@angular/material/table";
import { MatSort, MatSortModule } from "@angular/material/sort";
import { MatPaginator, MatPaginatorModule } from "@angular/material/paginator";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatDialog, MatDialogModule } from "@angular/material/dialog";
import { Subject, takeUntil } from "rxjs";
import { IconsModule } from "../../../../../utils/tabler-icons.module";
import { PageToolbarComponent } from "../../../../components/page-toolbar/page-toolbar.component";
import { SnackbarService } from "../../../../../services/snackbar.service";
import { ApiResponseInterface } from "../../../../../domain/interfaces/api-response.interface";
import { SnackbarTypeEnum } from "../../../../../utils/enums/snackbar-type.enum";
import { RoleService } from "../../../../../services/role.service";
import { RoleInterface } from "../../../../../domain/interfaces/role.interface";
import { RoleEditDialogComponent } from "../role-edit-dialog/role-edit-dialog.component";
import { AccessControlService } from "../../../../../services/access-control.service";
import {
  ActionsEnum,
  PermissionsEnum,
} from "../../../../../utils/enums/permissions.enum";
import { MatChipsModule } from "@angular/material/chips";

@Component({
  selector: "app-roles-list",
  templateUrl: "./roles-list.component.html",
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    MatToolbarModule,
    PageToolbarComponent,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatInputModule,
    MatCardModule,
    MatButtonModule,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatTooltipModule,
    MatDialogModule,
    MatChipsModule,
  ],
})
export class RolesListComponent implements OnInit, OnDestroy {
  private readonly _destroy$ = new Subject<void>();
  private readonly dialog = inject(MatDialog);
  private readonly roleService = inject(RoleService);
  private readonly snackbarService = inject(SnackbarService);
  private readonly accessControlService = inject(AccessControlService);

  roles = signal<any[]>([]);
  rolesFilter = new FormControl("");
  rolesDataSource: MatTableDataSource<any> = new MatTableDataSource();
  canRead = false;
  canUpdate = false;
  rolesDisplayedColumns: string[] = [];

  @ViewChild("rolesPaginator")
  rolesPaginator!: MatPaginator;
  @ViewChild("rolesSort") rolesSort!: MatSort;

  constructor() {
    effect(() => {
      if (this.roles()) {
        this.rolesDataSource.data = this.roles();
        this.rolesDataSource.paginator = this.rolesPaginator;
        this.rolesDataSource.sort = this.rolesSort;
      }
    });
  }

  ngOnInit() {
    this.canRead = this.accessControlService.can(
      PermissionsEnum.CONFIGURATION,
      ActionsEnum.READ
    );
    this.canUpdate = this.accessControlService.can(
      PermissionsEnum.CONFIGURATION,
      ActionsEnum.UPDATE
    );
    this.rolesDisplayedColumns = ["id", "label", "name"];
    if (this.canUpdate) {
      this.rolesDisplayedColumns.push("action");
    }
    this._loadInitialData();
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  private _loadInitialData() {
    this._loadRoles();
    this._setupFilters();
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

  private _loadRoles() {
    this.roleService
      .getAll()
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<RoleInterface[]>) => {
        this.roles.set(response.data);
      });
  }

  private _setupFilters() {
    this.rolesFilter.valueChanges.subscribe((filterValue) => {
      this.rolesDataSource.filter = filterValue?.trim().toLowerCase()!;

      if (this.rolesDataSource.paginator) {
        this.rolesDataSource.paginator.firstPage();
      }
    });
  }
}
