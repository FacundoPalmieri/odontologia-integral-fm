import { Component, Input } from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MatToolbarModule } from "@angular/material/toolbar";

@Component({
  selector: "app-page-toolbar",
  templateUrl: "./page-toolbar.component.html",
  styleUrl: "./page-toolbar.component.scss",
  standalone: true,
  imports: [CommonModule, IconsModule, MatToolbarModule],
})
export class PageToolbarComponent {
  @Input() title: string = "";
}
