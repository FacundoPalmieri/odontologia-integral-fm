import { Component, inject, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { Router } from "@angular/router";
import { OdontogramComponent } from "../../../components/odontogram/odontogram.component";
import { PageToolbarComponent } from "../../../components/page-toolbar/page-toolbar.component";
import { MatCardModule } from "@angular/material/card";
import { mockOdontogram1 } from "../../../../utils/mocks/odontogram.mock";
import { FormControl, FormGroup, ReactiveFormsModule } from "@angular/forms";
import { MatFormFieldModule } from "@angular/material/form-field";
import { IconsModule } from "../../../../utils/tabler-icons.module";
import { MatButtonModule } from "@angular/material/button";
import { MatInputModule } from "@angular/material/input";

@Component({
  selector: "app-odontogram-edit",
  templateUrl: "./odontogram-edit.component.html",
  styleUrls: ["./odontogram-edit.component.scss"],
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
export class OdontogramEditComponent implements OnInit {
  private readonly router = inject(Router);
  odontogram = mockOdontogram1;

  odontogramForm = new FormGroup({
    observations: new FormControl(""),
  });

  constructor() {}

  ngOnInit() {}

  saveOdontogram() {}

  goBack(): void {
    this.router.navigate(["/patients"]);
  }
}
