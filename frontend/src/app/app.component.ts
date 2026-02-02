import { Component, inject } from "@angular/core";
import { RouterModule } from "@angular/router";
import { LoaderComponent } from "./shared/components/loader/loader.component";
import { ThemeService } from "./core/services/theme.service";

@Component({
  selector: "app-root",
  standalone: true,
  imports: [RouterModule, LoaderComponent],
  template: `
    <app-loader></app-loader>
    <!-- <app-gesture-control></app-gesture-control> -->
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
