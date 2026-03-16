import { Component, EventEmitter, Input, Output } from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../core/modules/tabler-icons.module";
import { MatCardModule } from "@angular/material/card";
import { MatButtonModule } from "@angular/material/button";
import { MatTooltipModule } from "@angular/material/tooltip";
import { BackButtonComponent } from "../back-button/back-button.component";

@Component({
  selector: "app-page-toolbar",
  templateUrl: "./page-toolbar.component.html",
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    MatCardModule,
    MatButtonModule,
    MatTooltipModule,
    BackButtonComponent,
  ],
})
export class PageToolbarComponent {
  @Input() title: string = "";
  @Input() showBackButton: boolean = false;
  @Input() showActionButton: boolean = false;
  @Input() actionButtonText: string = "Guardar";
  @Input() actionButtonIcon: string = "device-floppy";
  @Input() actionButtonDisabled: boolean = false;
  @Output() back = new EventEmitter<void>();
  @Output() action = new EventEmitter<void>();
}
