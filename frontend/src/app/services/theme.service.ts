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

  currentTheme = signal<Theme>(this.getStoredTheme() || this.themes[0]);

  constructor() {}

  private getStoredTheme(): Theme | undefined {
    if (typeof localStorage !== "undefined") {
      const storedThemeId = localStorage.getItem("theme");
      return this.themes.find((t) => t.id === storedThemeId);
    }
    return undefined;
  }

  private storeTheme(themeId: string): void {
    if (typeof localStorage !== "undefined") {
      localStorage.setItem("theme", themeId);
    }
  }

  getThemes(): Theme[] {
    return this.themes;
  }

  setTheme(themeId: string): void {
    const theme = this.themes.find((t) => t.id === themeId);
    if (theme) {
      this.currentTheme.set(theme);
      this.storeTheme(themeId);
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
    const themeId = isDark ? "dark" : "light";
    this.setTheme(themeId);
  }
}
