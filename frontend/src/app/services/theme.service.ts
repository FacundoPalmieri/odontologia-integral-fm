import { effect, Injectable, signal } from "@angular/core";

export interface Theme {
  id: string;
  displayName: string;
}

@Injectable({
  providedIn: "root",
})
export class ThemeService {
  private readonly themes: Theme[] = [
    {
      id: "light",
      displayName: "Light",
    },
    { id: "dark", displayName: "Dark" },
  ];

  currentTheme = signal<Theme>(this.themes[0]);

  getThemes(): Theme[] {
    return this.themes;
  }

  setTheme(themeId: string): void {
    const theme = this.themes.find((t) => t.id === themeId);
    if (theme) {
      this.currentTheme.set(theme);
    }
  }

  updateThemeClass = effect(() => {
    if (typeof document !== "undefined") {
      const theme = this.currentTheme();
      document.body.classList.remove(
        ...this.themes.map((t) => `${t.id}-theme`)
      );
      document.body.classList.add(`${theme.id}-theme`);
    }
  });

  toggleTheme(isDark: boolean) {
    if (isDark) {
      document.body.classList.add("dark-theme");
      document.body.classList.remove("light-theme");
    } else {
      document.body.classList.add("light-theme");
      document.body.classList.remove("dark-theme");
    }
  }
}
