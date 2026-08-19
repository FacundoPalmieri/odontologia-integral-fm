import { Component } from "@angular/core";

import { MatToolbarModule } from "@angular/material/toolbar";
import { PageToolbarComponent } from "../../../../../shared/components/page-toolbar/page-toolbar.component";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";

@Component({
  selector: "app-dashboard",
  templateUrl: "./dashboard.component.html",
  standalone: true,
  imports: [IconsModule, MatToolbarModule, PageToolbarComponent],
})
export class DashboardComponent {
  constructor() {}
}
