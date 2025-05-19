import { CommonModule } from "@angular/common";
import { Component, inject } from "@angular/core";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { ShowTreatmentInterface } from "../../../domain/interfaces/treatment.interface";
import { TreatmentFactory } from "../../../utils/factories/treatment.factory";
import { TreatmentReferencesSidenavService } from "../../../services/treatment-references-sidenav.service";
import { MatSidenavModule } from "@angular/material/sidenav";
import { MatButtonModule } from "@angular/material/button";
import { MatTooltipModule } from "@angular/material/tooltip";

@Component({
  selector: "app-treatment-references",
  templateUrl: "./treatment-references.component.html",
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    MatSidenavModule,
    MatButtonModule,
    MatTooltipModule,
  ],
})
export class TreatmentReferencesComponent {
  treatmentReferencesSidenavService = inject(TreatmentReferencesSidenavService);
  treatments: ShowTreatmentInterface[] = TreatmentFactory.createTreatments();

  constructor() {}

  calculateMargin(index: number, totalIcons: number): string {
    if (totalIcons > 1 && index < totalIcons - 1) {
      return "mr-[-16px]";
    }
    return "";
  }
}
