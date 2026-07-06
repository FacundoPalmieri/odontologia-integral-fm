import { Component, ChangeDetectionStrategy, input } from "@angular/core";
import { ReactiveFormsModule, FormControl } from "@angular/forms";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatCardModule } from "@angular/material/card";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { CardIconTitleComponent } from "../../../../../shared/components/card-icon-title/card-icon-title.component";

@Component({
  selector: "app-consultation-observations-panel",
  templateUrl: "./consultation-observations-panel.component.html",
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatCardModule,
    IconsModule,
    CardIconTitleComponent,
  ],
})
export class ConsultationObservationsPanelComponent {
  control = input.required<FormControl<string | null>>();
}
