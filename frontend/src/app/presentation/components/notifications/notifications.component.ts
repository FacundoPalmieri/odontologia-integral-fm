import { Component, signal } from "@angular/core";
import { MatListModule } from "@angular/material/list";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { CommonModule } from "@angular/common";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatMenuModule } from "@angular/material/menu";
import { MatTooltipModule } from "@angular/material/tooltip";

@Component({
  selector: "app-notifications",
  imports: [
    CommonModule,
    MatListModule,
    IconsModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatTooltipModule,
  ],
  templateUrl: "./notifications.component.html",
})
export class NotificationsComponent {
  notifications = signal<any[]>([]);
}
