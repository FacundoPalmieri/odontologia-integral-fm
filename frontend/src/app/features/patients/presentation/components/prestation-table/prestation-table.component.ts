import {
  Component,
  ChangeDetectionStrategy,
  input,
  output,
} from "@angular/core";
import { MatTableModule } from "@angular/material/table";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { CardIconTitleComponent } from "../../../../../shared/components/card-icon-title/card-icon-title.component";

export interface TreatmentRow {
  tooth: string;
  procedure: string;
  status: "completed" | "in-progress" | "pending";
  cost: number;
}

@Component({
  selector: "app-prestation-table",
  templateUrl: "./prestation-table.component.html",
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [
    MatTableModule,
    MatButtonModule,
    MatCardModule,
    IconsModule,
    CardIconTitleComponent,
  ],
})
export class PrestationTableComponent {
  treatments = input<TreatmentRow[]>([]);
  addTreatment = output<void>();

  displayedColumns = ["tooth", "procedure", "status", "cost"];

  getStatusLabel(status: TreatmentRow["status"]): string {
    const labels: Record<TreatmentRow["status"], string> = {
      completed: "Completado",
      "in-progress": "En Curso",
      pending: "Pendiente",
    };
    return labels[status];
  }

  getStatusClass(status: TreatmentRow["status"]): string {
    return `status-badge status-${status}`;
  }
}
