import { effect, Injectable, signal } from "@angular/core";

/**
 * Interface representing a theme configuration.
 */
export interface Theme {
  /** Unique identifier for the theme */
  id: string;
  /** Display name shown to users */
  displayName: string;
  /** Theme category: default or FM (custom) */
  category: "default" | "fm";
  /** Array of color codes used for theme preview */
  previewColors: string[];
}

/**
 * Service for managing application themes.
 *
 * This service handles theme selection, storage, and application.
 * It provides both default and custom FM themes with light and dark variants.
 * Theme changes are persisted in localStorage and automatically applied to the document body.
 */
@Injectable({
  providedIn: "root",
})
export class ThemeService {
  /** Default theme identifier used when no theme is stored */
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

  /**
   * Signal holding the currently active theme.
   * Initialized with stored theme from localStorage or default theme.
   */
  currentTheme = signal<Theme>(this.getStoredTheme() || this.getDefaultTheme());

  constructor() {}

  /**
   * Retrieves the default theme configuration.
   *
   * @returns The default theme object
   * @private
   */
  private getDefaultTheme(): Theme {
    return (
      this.themes.find((t) => t.id === this.DEFAULT_THEME_ID) || this.themes[0]
    );
  }

  /**
   * Retrieves the theme stored in localStorage.
   *
   * @returns The stored theme if found, undefined otherwise
   * @private
   */
  private getStoredTheme(): Theme | undefined {
    if (typeof localStorage !== "undefined") {
      const storedThemeId = localStorage.getItem("theme");
      return this.themes.find((t) => t.id === storedThemeId);
    }
    return undefined;
  }

  /**
   * Stores the theme ID in localStorage.
   *
   * @param themeId - The ID of the theme to store
   * @returns void
   * @private
   */
  private storeTheme(themeId: string): void {
    if (typeof localStorage !== "undefined") {
      localStorage.setItem("theme", themeId);
    }
  }

  /**
   * Retrieves all available themes.
   *
   * @returns Array of all theme configurations
   */
  getThemes(): Theme[] {
    return this.themes;
  }

  /**
   * Sets the active theme by its ID.
   *
   * Updates the current theme signal and persists the selection to localStorage.
   *
   * @param themeId - The ID of the theme to activate
   * @returns void
   */
  setTheme(themeId: string): void {
    const theme = this.themes.find((t) => t.id === themeId);
    if (theme) {
      this.currentTheme.set(theme);
      this.storeTheme(themeId);
    }
  }

  /**
   * Effect that automatically updates the document body class when the theme changes.
   *
   * Removes all theme classes and applies the new theme class to the body element.
   */
  updateThemeClass = effect(() => {
    if (typeof document !== "undefined") {
      const theme = this.currentTheme();
      document.body.classList.remove(
        ...this.themes.map((t) => `${t.id}-theme`),
      );
      document.body.classList.add(`${theme.id}-theme`);
    }
  });

  /**
   * Toggles between light and dark theme variants.
   *
   * @param isDark - True to switch to dark theme, false for light theme
   * @returns void
   */
  toggleTheme(isDark: boolean) {
    const themeId = isDark ? "dark" : "light";
    this.setTheme(themeId);
  }
}
