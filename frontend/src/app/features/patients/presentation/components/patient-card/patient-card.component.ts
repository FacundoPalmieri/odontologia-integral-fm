import {
  Component,
  ChangeDetectionStrategy,
  input,
  output,
} from "@angular/core";
import { MatCardModule } from "@angular/material/card";
import { MatButtonModule } from "@angular/material/button";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatDividerModule } from "@angular/material/divider";
import { MatMenuModule } from "@angular/material/menu";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { PatientDto } from "../../../data/dtos/patient.dto";

@Component({
  selector: "app-patient-card",
  templateUrl: "./patient-card.component.html",
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatCardModule,
    MatButtonModule,
    MatTooltipModule,
    MatDividerModule,
    MatMenuModule,
    IconsModule,
  ],
})
export class PatientCardComponent {
  readonly patient = input.required<PatientDto>();

  readonly viewProfile = output<PatientDto>();
  readonly openConsultation = output<PatientDto>();
}
