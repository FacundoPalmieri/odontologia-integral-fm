import { Component, Input } from "@angular/core";

import { IconsModule } from "../../../core/modules/tabler-icons.module";

@Component({
  selector: "app-card-icon-title",
  standalone: true,
  imports: [IconsModule],
  templateUrl: "./card-icon-title.component.html",
})
export class CardIconTitleComponent {
  @Input() title!: string;
  @Input() subtitle?: string;
  @Input() icon!: string;
  @Input() backgroundColor!: string;
}
