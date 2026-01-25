import { Component, Input } from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";

@Component({
  selector: "app-card-icon-title",
  standalone: true,
  imports: [CommonModule, IconsModule],
  templateUrl: "./card-icon-title.component.html",
})
export class CardIconTitleComponent {
  @Input() title!: string;
  @Input() subtitle?: string;
  @Input() icon!: string;
  @Input() backgroundColor!: string;
}
