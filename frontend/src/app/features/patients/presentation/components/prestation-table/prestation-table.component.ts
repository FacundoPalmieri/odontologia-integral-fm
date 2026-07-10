import {
  Component,
  ChangeDetectionStrategy,
  input,
  output,
} from "@angular/core";
import { MatTableModule } from "@angular/material/table";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { MatSelectModule } from "@angular/material/select";
import { MatFormFieldModule } from "@angular/material/form-field";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { CardIconTitleComponent } from "../../../../../shared/components/card-icon-title/card-icon-title.component";
import { EmptyStateComponent } from "../../../../../shared/components/empty-state/empty-state.component";
import { PrestationScopeEnum } from "../../../utils/enums/consultation-instance.enum";
import { PrestationDto } from "../../../data/interfaces/prestation.interface";
import { CurrencyFormatDirective } from "../../../../../shared/directives/currency-format/currency-format.directive";

export interface TreatmentRow extends PrestationDto {
  selectedScope?: PrestationScopeEnum;
}

@Component({
  selector: "app-prestation-table",
  templateUrl: "./prestation-table.component.html",
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatTableModule,
    MatButtonModule,
    MatCardModule,
    MatSelectModule,
    MatFormFieldModule,
    IconsModule,
    CardIconTitleComponent,
    EmptyStateComponent,
    CurrencyFormatDirective,
  ],
})
export class PrestationTableComponent {
  treatments = input<TreatmentRow[]>([]);
  addTreatment = output<void>();
  scopeChange = output<{ index: number; scope: PrestationScopeEnum }>();

  displayedColumns = [
    "name",
    "allowedScope",
    "hasSteps",
    "requiresLocation",
    "isUnique",
    "currentPrice",
  ];

  readonly scopeLabels: Record<PrestationScopeEnum, string> = {
    [PrestationScopeEnum.TOOTH]: "Diente",
    [PrestationScopeEnum.TOOTH_FACE]: "Cara Dental",
    [PrestationScopeEnum.QUADRANT]: "Cuadrante",
    [PrestationScopeEnum.MAXILLARY]: "Maxilar",
    [PrestationScopeEnum.FULL_MOUTH]: "Boca Completa",
  };

  getScopeLabel(scope: PrestationScopeEnum | string): string {
    return this.scopeLabels[scope as PrestationScopeEnum] ?? scope;
  }
}
