import { Component, inject } from "@angular/core";
import { ThemeService } from "../services/theme.service";
import { RouterModule } from "@angular/router";
import { LoaderComponent } from "./components/loader/loader.component";

@Component({
  selector: "app-root",
  standalone: true,
  imports: [RouterModule, LoaderComponent],
  template: `
    <app-loader></app-loader>
    <router-outlet></router-outlet>
  `,
})
export class AppComponent {
  themeService = inject(ThemeService);
  isDarkMode: boolean = false;

  toggleTheme() {
    this.isDarkMode = !this.isDarkMode;
    this.themeService.toggleTheme(this.isDarkMode);
  }
}
