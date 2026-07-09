import {
  Component,
  ChangeDetectionStrategy,
  input,
  computed,
} from "@angular/core";
import { MatCardModule } from "@angular/material/card";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { CardIconTitleComponent } from "../../../../../shared/components/card-icon-title/card-icon-title.component";
import { TreatmentRow } from "../prestation-table/prestation-table.component";

const IVA_RATE = 0.15;

@Component({
  selector: "app-consultation-summary-panel",
  templateUrl: "./consultation-summary-panel.component.html",
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatCardModule,
    IconsModule,
    CardIconTitleComponent,
  ],
})
export class ConsultationSummaryPanelComponent {
  treatments = input<TreatmentRow[]>([]);

  subtotal = computed(() =>
    this.treatments().reduce((acc, t) => acc + t.currentPrice, 0),
  );

  iva = computed(() => this.subtotal() * IVA_RATE);

  total = computed(() => this.subtotal() + this.iva());
}
