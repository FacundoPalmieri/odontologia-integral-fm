import { Component, Input } from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MatMenuModule } from "@angular/material/menu";

@Component({
  selector: "app-tooth",
  templateUrl: "./tooth.component.html",
  standalone: true,
  imports: [CommonModule, IconsModule, MatMenuModule],
})
export class ToothComponent {
  @Input() toothNumber: number = 0;
  constructor() {}
}
