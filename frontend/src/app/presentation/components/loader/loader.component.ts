import { Component, inject } from "@angular/core";
import { LoaderService } from "../../../services/loader.service";
import { MatProgressBarModule } from "@angular/material/progress-bar";
import { CommonModule } from "@angular/common";

@Component({
  selector: "app-loader",
  templateUrl: "./loader.component.html",
  styleUrls: ["./loader.component.scss"],
  standalone: true,
  imports: [CommonModule, MatProgressBarModule],
})
export class LoaderComponent {
  loaderService = inject(LoaderService);
  loading$ = this.loaderService.loading$;

  constructor() {}
}
