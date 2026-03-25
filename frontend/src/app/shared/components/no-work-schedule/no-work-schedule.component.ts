import { Component, input } from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../core/modules/tabler-icons.module";

@Component({
  selector: "app-no-work-schedule",
  standalone: true,
  imports: [CommonModule, IconsModule],
  templateUrl: "./no-work-schedule.component.html",
})
export class NoWorkScheduleComponent {
  readonly message = input<string>(
    "No tienes una jornada laboral configurada. Configurá tu disponibilidad para poder utilizar el calendario y gestionar tus turnos.",
  );
}