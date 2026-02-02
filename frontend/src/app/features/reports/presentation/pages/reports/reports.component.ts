import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import { MatToolbarModule } from "@angular/material/toolbar";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { PageToolbarComponent } from "../../../../../shared/components/page-toolbar/page-toolbar.component";

@Component({
  selector: "app-reports",
  templateUrl: "./reports.component.html",
  standalone: true,
  imports: [CommonModule, IconsModule, MatToolbarModule, PageToolbarComponent],
})
export class ReportsComponent {
  constructor() {}
}
