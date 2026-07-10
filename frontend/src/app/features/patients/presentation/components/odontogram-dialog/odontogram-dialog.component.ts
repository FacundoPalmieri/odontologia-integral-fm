import { Component, inject } from "@angular/core";
import { MatDialogModule, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { MatButtonModule } from "@angular/material/button";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { OdontogramComponent } from "../../../../odontogram/presentation/pages/odontogram/odontogram.component";
import { OdontogramInterface } from "../../../../odontogram/data/interfaces/odontogram.interface";

@Component({
  selector: "app-odontogram-dialog",
  imports: [
    MatDialogModule,
    MatButtonModule,
    IconsModule,
    OdontogramComponent,
  ],
  template: `
    <div class="p-6 relative max-w-[100vw] overflow-hidden">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-xl font-bold">Odontograma</h2>
        <button mat-icon-button [mat-dialog-close]="true" aria-label="Cerrar">
          <i-tabler name="x" class="mb-1"></i-tabler>
        </button>
      </div>
      <div class="overflow-x-auto w-full">
        <app-odontogram
          [odontogram]="data.odontogram"
          [showToolbox]="true"
          [showTemporaries]="true"
          [showDateSelector]="false"
        />
      </div>
    </div>
  `,
})
export class OdontogramDialogComponent {
  readonly data = inject<{ odontogram: OdontogramInterface }>(MAT_DIALOG_DATA);
}
