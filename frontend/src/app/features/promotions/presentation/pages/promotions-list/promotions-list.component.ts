import { Component, inject, signal, OnDestroy } from "@angular/core";
import { CommonModule } from "@angular/common";
import { MatCardModule } from "@angular/material/card";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatButtonModule } from "@angular/material/button";
import { MatChipsModule } from "@angular/material/chips";
import { MatTableModule } from "@angular/material/table";
import { Subject, takeUntil } from "rxjs";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { LoaderService } from "../../../../../core/services/loader.service";
import { ActionsEnum, PermissionsEnum } from "../../../../../shared/utils/enums/permissions.enum";
import { ApiResponseInterface } from "../../../../../shared/interfaces/api-response.interface";
import { SnackbarTypeEnum } from "../../../../../shared/utils/enums/snackbar-type.enum";
import { PromotionService } from "../../../services/promotion.service";
import { MatDialog } from "@angular/material/dialog";
import { AccessControlService } from "../../../../../core/services/access-control.service";
import { SnackbarService } from "../../../../../shared/services/snackbar.service";
import { PageToolbarComponent } from "../../../../../shared/components/page-toolbar/page-toolbar.component";
import { PromotionInterface } from "../../../data/interfaces/promotion.interface";
import { DiscountTypeEnum } from "../../../data/enums/discount-type.enum";
import { EmptyStateComponent } from "../../../../../shared/components/empty-state/empty-state.component";
import { PromotionCreateDialogComponent } from "../../components/promotion-create-dialog/promotion-create-dialog.component";
import { PromotionEditDialogComponent } from "../../components/promotion-edit-dialog/promotion-edit-dialog.component";
import { ConfirmDialogComponent } from "../../../../../shared/components/confirm-dialog/confirm-dialog.component";

@Component({
  selector: "app-promotions-list",
  templateUrl: "./promotions-list.component.html",
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    MatCardModule,
    MatTooltipModule,
    MatButtonModule,
    MatChipsModule,
    MatTableModule,
    PageToolbarComponent,
    EmptyStateComponent,
  ],
})
export class PromotionsListComponent implements OnDestroy {
  private readonly _destroy$ = new Subject<void>();
  private readonly loaderService = inject(LoaderService);
  private readonly accessControlService = inject(AccessControlService);
  private readonly promotionService = inject(PromotionService);
  private readonly snackbarService = inject(SnackbarService);
  private readonly dialog = inject(MatDialog);

  promotions = signal<PromotionInterface[]>([]);

  canCreate = signal<boolean>(false);
  canRead = signal<boolean>(false);
  canUpdate = signal<boolean>(false);

  displayedColumns: string[] = ['label', 'discountType', 'value', 'startDate', 'endDate', 'finishedAt'];

  constructor() {
    this._loadInitialData();
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  createPromotion() {
    const dialogRef = this.dialog.open(PromotionCreateDialogComponent, { width: '500px' });
    dialogRef
      .afterClosed()
      .subscribe((promotion: Omit<PromotionInterface, "id">) => {
        if (promotion) {
          this.loaderService.show();
          this.promotionService
            .create(promotion)
            .pipe(takeUntil(this._destroy$))
            .subscribe({
              next: (response: ApiResponseInterface<PromotionInterface>) => {
                this.snackbarService.openSnackbar(
                  response.message,
                  6000,
                  "center",
                  "top",
                  SnackbarTypeEnum.Success,
                );
                this._loadPromotions();
              },
              error: () => {
                this.loaderService.hide();
              }
            });
        }
      });
  }

  editPromotion(promotion: PromotionInterface) {
    const dialogRef = this.dialog.open(PromotionEditDialogComponent, {
      width: '500px',
      data: { promotion },
    });
    dialogRef.afterClosed().subscribe((updatedPromotion: Omit<PromotionInterface, "id">) => {
      if (updatedPromotion) {
        this.loaderService.show();
        this.promotionService
          .update(promotion.id, updatedPromotion)
          .pipe(takeUntil(this._destroy$))
          .subscribe({
            next: (response: ApiResponseInterface<PromotionInterface>) => {
              this.snackbarService.openSnackbar(
                response.message,
                6000,
                "center",
                "top",
                SnackbarTypeEnum.Success,
              );
              this._loadPromotions();
            },
            error: () => {
              this.loaderService.hide();
            }
          });
      }
    });
  }

  finishPromotion(promotion: PromotionInterface) {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        message: `¿Estás seguro de que deseas finalizar la promoción "${promotion.label}"? Esta acción no se puede deshacer.`,
        confirmText: 'Finalizar',
      },
    });

    dialogRef.afterClosed().subscribe((result: boolean) => {
      if (result) {
        this.loaderService.show();
        this.promotionService.finish(promotion.id)
          .pipe(takeUntil(this._destroy$))
          .subscribe({
            next: (response) => {
              this.snackbarService.openSnackbar(
                response.message,
                6000,
                "center",
                "top",
                SnackbarTypeEnum.Success,
              );
              this._loadPromotions();
            },
            error: (err) => {
              this.loaderService.hide();
              this.snackbarService.openSnackbar("Error al finalizar la promoción", 6000, "center", "top", SnackbarTypeEnum.Error);
            }
          });
      }
    });
  }

  getDiscountTypeLabel(type: DiscountTypeEnum): string {
    return type === DiscountTypeEnum.FIXED ? 'Fijo' : 'Porcentaje';
  }

  getDiscountTypeClass(type: DiscountTypeEnum): string {
    return type === DiscountTypeEnum.FIXED 
      ? "px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800"
      : "px-2 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800";
  }

  private _loadInitialData() {
    this._loadPermissionsFlags();
    if (this.canRead()) {
      this._loadPromotions();
    }
  }

  private _loadPermissionsFlags() {
    this.canCreate.set(
      this.accessControlService.can(PermissionsEnum.CONFIGURATION, ActionsEnum.CREATE)
    );
    this.canRead.set(
      this.accessControlService.can(PermissionsEnum.CONFIGURATION, ActionsEnum.READ)
    );
    this.canUpdate.set(
      this.accessControlService.can(PermissionsEnum.CONFIGURATION, ActionsEnum.UPDATE)
    );
  }

  private _loadPromotions() {
    this.loaderService.show();
    this.promotionService
      .getAll()
      .pipe(takeUntil(this._destroy$))
      .subscribe({
        next: (response: ApiResponseInterface<PromotionInterface[]>) => {
          const promotions = response.data ?? [];
          this.promotions.set(promotions);
          this.loaderService.hide();
        },
        error: (error) => {
          console.error("Error al cargar las promociones:", error);
          this.loaderService.hide();
          this.snackbarService.openSnackbar(
            "Error al cargar promociones.",
            6000,
            "center",
            "top",
            SnackbarTypeEnum.Error,
          );
        },
      });
  }
}
