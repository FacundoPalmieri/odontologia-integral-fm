import { Component, inject, ChangeDetectionStrategy } from "@angular/core";
import { Location } from "@angular/common";
import { IconsModule } from "../../../core/modules/tabler-icons.module";
import { MatButtonModule } from "@angular/material/button";
import { MatTooltipModule } from "@angular/material/tooltip";

@Component({
  selector: "app-back-button",
  template: `
    <button
      matIconButton
      matTooltip="Volver"
      aria-label="Volver a la página anterior"
      (click)="goBack()"
    >
      <i-tabler name="arrow-narrow-left" class="mb-1" />
    </button>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IconsModule, MatButtonModule, MatTooltipModule],
})
export class BackButtonComponent {
  private readonly location = inject(Location);

  goBack(): void {
    this.location.back();
  }
}
