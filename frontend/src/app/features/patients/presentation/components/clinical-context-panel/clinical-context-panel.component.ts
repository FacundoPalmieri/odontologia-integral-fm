import { ChangeDetectionStrategy, Component, computed, input } from "@angular/core";
import { MatCardModule } from "@angular/material/card";
import { MatChipsModule } from "@angular/material/chips";
import { CardIconTitleComponent } from "../../../../../shared/components/card-icon-title/card-icon-title.component";
import { EmptyStateComponent } from "../../../../../shared/components/empty-state/empty-state.component";
import { ClinicalContextResponse } from "../../../data/interfaces/clinical-context.interface";
import { StepStatusEnum } from "../../../utils/enums/consultation-instance.enum";
import { scopeLabel } from "../../../utils/scope-label";

const STEP_STATUS: Record<StepStatusEnum, string> = {
  [StepStatusEnum.IN_PROGRESS]: "En curso",
  [StepStatusEnum.FINISHED]: "Finalizado",
  [StepStatusEnum.OMITTED]: "Omitido",
  [StepStatusEnum.REPLACED]: "Reemplazado",
  [StepStatusEnum.CANCELED]: "Cancelado",
};

/** Lists what the patient has open: treatments and in-progress prestations with their steps. */
@Component({
  selector: "app-clinical-context-panel",
  templateUrl: "./clinical-context-panel.component.html",
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatCardModule, MatChipsModule, CardIconTitleComponent, EmptyStateComponent],
})
export class ClinicalContextPanelComponent {
  readonly context = input<ClinicalContextResponse | null>(null);
  readonly isEmpty = computed(() => {
    const c = this.context();
    return !c?.prestationInstances.length;
  });

  readonly scopeLabel = scopeLabel;
  readonly stepStatus = (s: StepStatusEnum | null) => (s ? STEP_STATUS[s] : "Pendiente");
}
