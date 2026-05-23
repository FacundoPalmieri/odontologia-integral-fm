import { Component, ChangeDetectionStrategy, input, output } from "@angular/core";
import { CommonModule } from "@angular/common";
import { MatCardModule } from "@angular/material/card";
import { MatButtonModule } from "@angular/material/button";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatDividerModule } from "@angular/material/divider";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { AppointmentActionsMenuComponent } from "../appointment-actions-menu/appointment-actions-menu.component";

@Component({
  selector: "app-appointment-patient-card",
  templateUrl: "./appointment-patient-card.component.html",
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatTooltipModule,
    MatDividerModule,
    IconsModule,
    AppointmentActionsMenuComponent,
  ],
})
export class AppointmentPatientCardComponent {
  readonly appointment = input.required<any>();
  readonly actionPerformed = output<void>();
}
