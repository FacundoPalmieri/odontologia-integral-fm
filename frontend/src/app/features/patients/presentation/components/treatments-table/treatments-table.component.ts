import { ChangeDetectionStrategy, Component, input } from "@angular/core";
import { MatCardModule } from "@angular/material/card";
import { MatTableModule } from "@angular/material/table";
import { CardIconTitleComponent } from "../../../../../shared/components/card-icon-title/card-icon-title.component";
import { EmptyStateComponent } from "../../../../../shared/components/empty-state/empty-state.component";
import { TreatmentInstanceContext } from "../../../data/interfaces/clinical-context.interface";
import { scopeLabel } from "../../../utils/scope-label";
import { conditionColor, conditionLabel } from "../../../../odontogram/utils/treatment-condition";

/** Treatments the patient has open, with their location and current condition. */
@Component({
  selector: "app-treatments-table",
  templateUrl: "./treatments-table.component.html",
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatCardModule, MatTableModule, CardIconTitleComponent, EmptyStateComponent],
})
export class TreatmentsTableComponent {
  readonly treatments = input<TreatmentInstanceContext[]>([]);
  readonly displayedColumns = ["label", "scope", "condition"];
  readonly scopeLabel = scopeLabel;
  readonly conditionLabel = conditionLabel;
  readonly conditionColor = conditionColor;
}
