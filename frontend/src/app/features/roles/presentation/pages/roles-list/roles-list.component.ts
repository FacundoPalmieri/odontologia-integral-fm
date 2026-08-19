import {
  Component,
  effect,
  inject,
  OnDestroy,
  signal,
  ViewChild,
} from "@angular/core";

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
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { PageToolbarComponent } from "../../../../../shared/components/page-toolbar/page-toolbar.component";
import { ApiResponseInterface } from "../../../../../shared/interfaces/api-response.interface";
import { SnackbarTypeEnum } from "../../../../../shared/utils/enums/snackbar-type.enum";
import { RoleService } from "../../../services/role.service";
import { SnackbarService } from "../../../../../shared/services/snackbar.service";
import { RoleEditDialogComponent } from "../../components/role-edit-dialog/role-edit-dialog.component";
import { RoleInterface } from "../../../data/interfaces/role.interface";
import { EmptyStateComponent } from "../../../../../shared/components/empty-state/empty-state.component";

@Component({
  selector: "app-roles-list",
  templateUrl: "./roles-list.component.html",
  standalone: true,
  imports: [
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
    EmptyStateComponent
],
})
export class RolesListComponent implements OnDestroy {
  private readonly _destroy$ = new Subject<void>();
  private readonly dialog = inject(MatDialog);
  private readonly roleService = inject(RoleService);
  private readonly snackbarService = inject(SnackbarService);

  roles = signal<any[]>([]);
  rolesFilter = new FormControl("");
  rolesDataSource: MatTableDataSource<any> = new MatTableDataSource();
  rolesDisplayedColumns: string[] = ["id", "label", "name", "action"];

  @ViewChild("rolesPaginator") set paginator(paginator: MatPaginator) {
    this.rolesDataSource.paginator = paginator;
  }
  @ViewChild("rolesSort") set sort(sort: MatSort) {
    this.rolesDataSource.sort = sort;
  }

  constructor() {
    this.loadInitialData();

    effect(() => {
      if (this.roles()) {
        this.rolesDataSource.data = this.roles();
      }
    });
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  loadInitialData() {
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
                    SnackbarTypeEnum.Success,
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
        SnackbarTypeEnum.Error,
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
