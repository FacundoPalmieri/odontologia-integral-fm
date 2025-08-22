import { Component, EventEmitter, Input, Output } from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatButtonModule } from "@angular/material/button";
import { MatTooltipModule } from "@angular/material/tooltip";

@Component({
  selector: "app-page-toolbar",
  templateUrl: "./page-toolbar.component.html",
  styleUrl: "./page-toolbar.component.scss",
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    MatToolbarModule,
    MatButtonModule,
    MatTooltipModule,
  ],
})
export class PageToolbarComponent {
  @Input() title: string = "";
  @Input() showBackButton: boolean = false;
  @Output() back = new EventEmitter<void>();
}
