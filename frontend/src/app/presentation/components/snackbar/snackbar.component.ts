import { CommonModule } from "@angular/common";
import { Component, Inject } from "@angular/core";
import { MAT_SNACK_BAR_DATA } from "@angular/material/snack-bar";
import { SnackbarType } from "../../../utils/enums/snackbar-type.enum";

@Component({
  selector: "app-snackbar",
  templateUrl: "./snackbar.component.html",
  styleUrl: "./snackbar.component.scss",
  standalone: true,
  imports: [CommonModule],
})
export class SnackbarComponent {
  SnackbarType = SnackbarType;
  constructor(@Inject(MAT_SNACK_BAR_DATA) public data: any) {}
}
