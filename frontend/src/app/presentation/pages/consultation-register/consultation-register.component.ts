import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { PageToolbarComponent } from "../../components/page-toolbar/page-toolbar.component";

@Component({
  selector: "app-consultation-register",
  templateUrl: "./consultation-register.component.html",
  styleUrl: "./consultation-register.component.scss",
  standalone: true,
  imports: [CommonModule, IconsModule, PageToolbarComponent],
})
export class ConsultationRegisterComponent {}
