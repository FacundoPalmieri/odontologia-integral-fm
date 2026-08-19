import {
  Component,
  ChangeDetectionStrategy,
  input,
  output,
} from "@angular/core";

import { FormsModule } from "@angular/forms";
import { IconsModule } from "../../../core/modules/tabler-icons.module";
import { MatCardModule } from "@angular/material/card";
import { MatButtonModule } from "@angular/material/button";
import { MatTooltipModule } from "@angular/material/tooltip";
import { BackButtonComponent } from "../back-button/back-button.component";
import { MatDividerModule } from "@angular/material/divider";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatButtonToggleModule } from "@angular/material/button-toggle";
import { MatSelectModule } from "@angular/material/select";

export interface ToolbarSelectOption {
  id: number | string;
  label: string;
}

@Component({
  selector: "app-page-toolbar",
  templateUrl: "./page-toolbar.component.html",
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FormsModule,
    IconsModule,
    MatCardModule,
    MatButtonModule,
    MatTooltipModule,
    BackButtonComponent,
    MatDividerModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonToggleModule,
    MatSelectModule
],
})
export class PageToolbarComponent {
  readonly title = input<string>("");
  readonly showBackButton = input<boolean>(false);

  readonly showActionButton = input<boolean>(false);
  readonly actionButtonText = input<string>("Guardar");
  readonly actionButtonIcon = input<string>("device-floppy");
  readonly actionButtonDisabled = input<boolean>(false);

  readonly totalCount = input<number | null>(null);

  readonly showSearch = input<boolean>(false);
  readonly searchPlaceholder = input<string>("Buscar...");
  readonly searchValue = input<string>("");

  readonly showSelectFilter = input<boolean>(false);
  readonly selectFilterPlaceholder = input<string>("Filtrar...");
  readonly selectFilterIcon = input<string>("filter");
  readonly selectFilterOptions = input<ToolbarSelectOption[]>([]);
  readonly selectFilterValue = input<(number | string)[]>([]);

  readonly showViewMode = input<boolean>(false);
  readonly viewMode = input<"table" | "cards">("table");

  readonly back = output<void>();
  readonly action = output<void>();
  readonly searchChange = output<string>();
  readonly selectFilterChange = output<(number | string)[]>();
  readonly viewModeChange = output<"table" | "cards">();

  onSearchChange(value: string) {
    this.searchChange.emit(value);
  }

  onSelectFilterChange(value: (number | string)[]) {
    this.selectFilterChange.emit(value);
  }

  onViewModeChange(value: "table" | "cards") {
    this.viewModeChange.emit(value);
  }
}
