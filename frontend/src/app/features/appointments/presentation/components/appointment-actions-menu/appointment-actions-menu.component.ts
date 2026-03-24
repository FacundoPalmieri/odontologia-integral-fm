import { Component, ChangeDetectionStrategy } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import { MatMenuModule } from "@angular/material/menu";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatDividerModule } from "@angular/material/divider";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";

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
export class AppointmentActionsMenuComponent {}
