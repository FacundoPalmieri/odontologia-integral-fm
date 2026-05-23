import { Component, ChangeDetectionStrategy, inject, input, output } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import { MatMenuModule } from "@angular/material/menu";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatDividerModule } from "@angular/material/divider";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { ConsultationService } from "../../../services/consultation.service";
import { SnackbarService } from "../../../../../shared/services/snackbar.service";
import { SnackbarTypeEnum } from "../../../../../shared/utils/enums/snackbar-type.enum";

@Component({
  selector: "app-appointment-actions-menu",
  templateUrl: "./appointment-actions-menu.component.html",
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatButtonModule,
    MatMenuModule,
    MatTooltipModule,
    MatDividerModule,
    IconsModule,
  ],
})
export class AppointmentActionsMenuComponent {
  private readonly consultationService = inject(ConsultationService);
  private readonly snackbarService = inject(SnackbarService);

  appointment = input.required<any>();
  actionPerformed = output<void>();

  startConsultation() {
    this.consultationService.createAppointment(this.appointment().id).subscribe({
      next: () => {
        this.snackbarService.openSnackbar(
          "Consulta iniciada correctamente",
          4000,
          "center",
          "top",
          SnackbarTypeEnum.Success
        );
        this.actionPerformed.emit();
      },
      error: () => {
        this.snackbarService.openSnackbar(
          "Error al iniciar la consulta",
          4000,
          "center",
          "top",
          SnackbarTypeEnum.Error
        );
      }
    });
  }
}
