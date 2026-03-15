import { Component, ChangeDetectionStrategy, computed, inject } from "@angular/core";
import { MatMenuModule } from "@angular/material/menu";
import { MatButtonModule } from "@angular/material/button";
import { MatDividerModule } from "@angular/material/divider";
import { MatTooltipModule } from "@angular/material/tooltip";
import { IconsModule } from "../../../core/modules/tabler-icons.module";
import { ThemeService } from "../../../core/services/theme.service";

@Component({
  selector: "app-toolbar-theme-toggle",
  templateUrl: "./toolbar-theme-toggle.component.html",
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatMenuModule, MatButtonModule, MatDividerModule, MatTooltipModule, IconsModule],
})
export class ToolbarThemeToggleComponent {
  private readonly themeService = inject(ThemeService);

  currentTheme = computed(() => this.themeService.currentTheme());

  getAvailableThemes() {
    return this.themeService.getThemes();
  }

  setTheme(themeId: string) {
    this.themeService.setTheme(themeId);
  }

  isCurrentTheme(themeId: string): boolean {
    return this.currentTheme().id === themeId;
  }
}
