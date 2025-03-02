import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MatToolbarModule } from "@angular/material/toolbar";
import { PageToolbarComponent } from "../../components/page-toolbar/page-toolbar.component";

@Component({
  selector: "app-appointments",
  templateUrl: "./appointments.component.html",
  standalone: true,
  imports: [CommonModule, IconsModule, MatToolbarModule, PageToolbarComponent],
})
export class AppointmentsComponent {
  constructor() {}
}
