import { CommonModule } from "@angular/common";
import { Component } from "@angular/core";
import { IconsModule } from "../../../utils/tabler-icons.module";

@Component({
  selector: "app-person-form",
  templateUrl: "./snackbar.component.html",
  standalone: true,
  imports: [CommonModule, IconsModule],
})
export class PersonFormComponent {
  constructor() {}
}
