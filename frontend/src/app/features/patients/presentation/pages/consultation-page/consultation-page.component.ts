import { Component, inject, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { Router } from "@angular/router";
import { MatCardModule } from "@angular/material/card";
import { FormControl, FormGroup, ReactiveFormsModule } from "@angular/forms";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatButtonModule } from "@angular/material/button";
import { MatInputModule } from "@angular/material/input";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { OdontogramComponent } from "../../../../odontogram/presentation/pages/odontogram/odontogram.component";
import { PageToolbarComponent } from "../../../../../shared/components/page-toolbar/page-toolbar.component";
import { mockOdontogram1 } from "../../../../../shared/utils/mocks/odontogram.mock";

@Component({
  selector: "app-consultation-page",
  templateUrl: "./consultation-page.component.html",
  styleUrls: ["./consultation-page.component.scss"],
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    OdontogramComponent,
    PageToolbarComponent,
    MatCardModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatButtonModule,
    MatInputModule,
  ],
})
export class ConsultationPageComponent {
  private readonly router = inject(Router);
  odontogram = mockOdontogram1;

  odontogramForm = new FormGroup({
    observations: new FormControl(""),
  });

  saveOdontogram() {}

  goBack(): void {
    this.router.navigate(["/patients"]);
  }
}
