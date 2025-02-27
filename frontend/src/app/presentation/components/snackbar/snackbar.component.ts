import { CommonModule } from "@angular/common";
import { Component, Inject } from "@angular/core";
import { MAT_SNACK_BAR_DATA } from "@angular/material/snack-bar";
import { SnackbarTypeEnum } from "../../../utils/enums/snackbar-type.enum";
import { SnackbarDataInterface } from "../../../domain/interfaces/snackbar-data.interface";
import { IconsModule } from "../../../utils/tabler-icons.module";

@Component({
  selector: "app-snackbar",
  templateUrl: "./snackbar.component.html",
  standalone: true,
  imports: [CommonModule, IconsModule],
})
export class SnackbarComponent {
  SnackbarType = SnackbarTypeEnum;
  iconName: string = "";
  constructor(@Inject(MAT_SNACK_BAR_DATA) public data: SnackbarDataInterface) {
    this.setIcon();
  }

  private setIcon(): void {
    switch (this.data.type) {
      case SnackbarTypeEnum.Success:
        this.iconName = "circle-check";
        break;
      case SnackbarTypeEnum.Error:
        this.iconName = "xbox-x";
        break;
      case SnackbarTypeEnum.Warning:
        this.iconName = "alert-triangle";
        break;
      case SnackbarTypeEnum.Info:
        this.iconName = "exclamation-circle";
        break;
      default:
        this.iconName = "exclamation-circle";
        break;
    }
  }
}
