import {
  Component,
  inject,
  signal,
  effect,
  OnDestroy,
  AfterViewInit,
  ViewChild,
  OnInit,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { MatCardModule } from "@angular/material/card";
import { MatTableDataSource, MatTableModule } from "@angular/material/table";
import { MatPaginator, MatPaginatorModule } from "@angular/material/paginator";
import { MatSort, MatSortModule } from "@angular/material/sort";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatButtonModule } from "@angular/material/button";
import { MatChipsModule } from "@angular/material/chips";
import { MatFormFieldModule } from "@angular/material/form-field";
import { FormControl, ReactiveFormsModule } from "@angular/forms";
import { MatInputModule } from "@angular/material/input";
import { Subject, takeUntil } from "rxjs";
import { Router } from "@angular/router";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MatToolbarModule } from "@angular/material/toolbar";
import { PageToolbarComponent } from "../../components/page-toolbar/page-toolbar.component";
import { LoaderService } from "../../../services/loader.service";
import { AccessControlService } from "../../../services/access-control.service";
import { InsumoService } from "../../../services/insumo.service";
import { SnackbarService } from "../../../services/snackbar.service";
import { InsumoDtoInterface } from "../../../domain/dto/insumo.dto";
import {
  ActionsEnum,
  PermissionsEnum,
} from "../../../utils/enums/permissions.enum";
import {
  ApiResponseInterface,
  PagedDataInterface,
} from "../../../domain/interfaces/api-response.interface";
import { SnackbarTypeEnum } from "../../../utils/enums/snackbar-type.enum";

@Component({
  selector: "app-inventory",
  templateUrl: "./inventory.component.html",
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
    MatFormFieldModule,
    ReactiveFormsModule,
    MatInputModule,
  ],
})
export class InventoryComponent implements OnInit, OnDestroy, AfterViewInit {
  private readonly _destroy$ = new Subject<void>();
  private readonly loaderService = inject(LoaderService);
  private readonly router = inject(Router);
  private readonly accessControlService = inject(AccessControlService);
  private readonly insumoService = inject(InsumoService);
  private readonly snackbarService = inject(SnackbarService);

  insumos = signal<InsumoDtoInterface[]>([]);

  insumosPageSize = signal(1000);
  insumosPageIndex = signal(0);
  insumosSortBy = signal("nombre");
  insumosSortDirection = signal("asc");
  insumosTotalElements = signal(0);

  insumoFilter = new FormControl("");
  insumosDataSource = new MatTableDataSource<InsumoDtoInterface>([]);

  permissionsReady = signal(false);

  private _insumosPaginator: MatPaginator | undefined;
  @ViewChild("insumosPaginator")
  set insumosPaginator(paginator: MatPaginator | undefined) {
    this._insumosPaginator = paginator;
    if (paginator) {
      this.insumosDataSource.paginator = paginator;
    }
  }
  get insumosPaginator() {
    return this._insumosPaginator;
  }
  @ViewChild("insumosSort") insumosSort!: MatSort;

  insumoDisplayedColumns: string[] = [
    "id",
    "nombre",
    "descripcion",
    "fechaIngreso",
    "fechaVencimiento",
    "cantidad",
  ];

  canCreate = signal<boolean>(false);
  canRead = signal<boolean>(false);
  canUpdate = signal<boolean>(false);

  constructor() {
    effect(() => {
      if (this.insumos()) {
        this.insumosDataSource.data = this.insumos();
        if (this.insumosPaginator) {
          this.insumosPaginator.length = this.insumosTotalElements();
        }
      }
    });

    effect(() => {
      if (this.canUpdate()) {
        this.insumoDisplayedColumns.push("action");
      }
    });
  }

  ngOnInit() {
    this._loadPermissionsFlags();
    this.permissionsReady.set(true);
    if (this.canRead()) {
      this._loadInitialData();
      this._setupFilters();
    }
  }

  ngAfterViewInit(): void {
    if (this.insumosSort) {
      const insumoSort = this.insumosSort;
      insumoSort.sortChange
        .pipe(takeUntil(this._destroy$))
        .subscribe((sort) => {
          this.insumosPageIndex.set(0);
          this.insumosSortBy.set(sort.active);
          this.insumosSortDirection.set(sort.direction);
          this._loadInsumos(
            this.insumosPageIndex(),
            this.insumosPageSize(),
            this.insumosSortBy(),
            this.insumosSortDirection()
          );
        });
    }
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  createInsumo() {
    this.router.navigate(["/inventory/create"]);
  }

  editInsumo(insumo: InsumoDtoInterface) {
    this.router.navigate(["/inventory/edit", insumo.id]);
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString("es-AR", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
    });
  }

  private _loadInitialData() {
    this._loadInsumos(
      this.insumosPageIndex(),
      this.insumosPageSize(),
      this.insumosSortBy(),
      this.insumosSortDirection()
    );
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

  private _loadInsumos(
    page: number,
    size: number,
    sortBy: string,
    direction: string
  ) {
    this.insumoService
      .getAll(page, size, sortBy, direction)
      .pipe(takeUntil(this._destroy$))
      .subscribe(
        (
          response: ApiResponseInterface<
            PagedDataInterface<InsumoDtoInterface[]>
          >
        ) => {
          const insumos = response.data?.content;
          this.insumos.set(insumos);
          this.loaderService.hide();
        },
        (error) => {
          console.error("Error al cargar insumos:", error);
          this.loaderService.hide();
          this.snackbarService.openSnackbar(
            "Error al cargar insumos.",
            6000,
            "center",
            "top",
            SnackbarTypeEnum.Error
          );
        }
      );
  }

  private _setupFilters() {
    this.insumoFilter.valueChanges.subscribe((filterValue) => {
      this.insumosDataSource.filter = filterValue?.trim().toLowerCase()!;

      if (this.insumosDataSource.paginator) {
        this.insumosDataSource.paginator.firstPage();
      }
    });
  }
}
