import { effect, Injectable, signal } from "@angular/core";

export interface Theme {
  id: string;
  displayName: string;
  category: "default" | "fm";
  previewColors: string[];
}

@Injectable({
  providedIn: "root",
})
export class ThemeService {
  private readonly DEFAULT_THEME_ID = "default-light";

  private readonly themes: Theme[] = [
    {
      id: "default-light",
      displayName: "Default Claro",
      category: "default",
      previewColors: ["#2f80ed", "#2dd4bf", "#f5f7fa"],
    },
    {
      id: "default-dark",
      displayName: "Default Oscuro",
      category: "default",
      previewColors: ["#38bdf8", "#2dd4bf", "#0f172a"],
    },
    {
      id: "light",
      displayName: "FM Claro",
      category: "fm",
      previewColors: ["#824e6a", "#ffb4a3", "#fff8f8"],
    },
    {
      id: "dark",
      displayName: "FM Oscuro",
      category: "fm",
      previewColors: ["#f5b4d4", "#ffb4a3", "#1e161c"],
    },
  ];

  currentTheme = signal<Theme>(this.getStoredTheme() || this.getDefaultTheme());

  constructor() {}

  private getDefaultTheme(): Theme {
    return (
      this.themes.find((t) => t.id === this.DEFAULT_THEME_ID) || this.themes[0]
    );
  }

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
