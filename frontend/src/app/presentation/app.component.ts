import { Component, inject } from "@angular/core";
import { ThemeService } from "../services/theme.service";
import { RouterModule } from "@angular/router";

@Component({
  selector: "app-root",
  standalone: true,
  imports: [RouterModule],
  template: ` <router-outlet></router-outlet> `,
  styles: `
    // @use '@angular/material' as mat;

    // mat-toolbar {
    //   justify-content: space-between;

    //   @include mat.toolbar-overrides((
    //     container-background-color: var(--mat-sys-primary),
    //     container-text-color: var(--mat-sys-on-primary),
    //   ));

    //   @include mat.icon-button-overrides((
    //     icon-color: var(--mat-sys-on-secondary),
    //   ));
    // }

    // .theme-menu-item {
    //   display: flex;
    //   align-items: center;
    //   gap: 12px;
    // }

    // .color-preview {
    //   width: 24px;
    //   height: 24px;
    //   border-radius: 50%;
    // }
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
