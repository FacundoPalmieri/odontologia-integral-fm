import {
  ApplicationConfig,
  provideZonelessChangeDetection,
} from "@angular/core";
import { provideRouter } from "@angular/router";
import { routes } from "./app.routes";
import {
  provideClientHydration,
  withEventReplay,
} from "@angular/platform-browser";
import { provideAnimationsAsync } from "@angular/platform-browser/animations/async";
import {
  provideHttpClient,
  withFetch,
  withInterceptors,
} from "@angular/common/http";
import { MatPaginatorIntl } from "@angular/material/paginator";
import {
  MatNativeDateModule,
  provideNativeDateAdapter,
  MAT_DATE_FORMATS,
  MAT_DATE_LOCALE,
} from "@angular/material/core";
import { provideCharts, withDefaultRegisterables } from "ng2-charts";
import { errorInterceptor } from "../interceptors/error.interceptor";
import { tokenInterceptor } from "../interceptors/token.interceptor";
import { CustomPaginatorIntl } from "../initializers/custom-paginator.initializer";

export const CUSTOM_DATE_FORMATS = {
  parse: {
    dateInput: "DD/MM/YYYY",
  },
  display: {
    dateInput: "DD/MM/YYYY",
    monthYearLabel: "MMM YYYY",
    dateA11yLabel: "LL",
    monthYearA11yLabel: "MMMM YYYY",
  },
};

export const appConfig: ApplicationConfig = {
  providers: [
    provideZonelessChangeDetection(),
    provideRouter(routes),
    provideClientHydration(withEventReplay()),
    provideAnimationsAsync(),
    provideHttpClient(
      withInterceptors([errorInterceptor, tokenInterceptor]),
      withFetch(),
    ),
    provideNativeDateAdapter(),
    provideCharts(withDefaultRegisterables()),
    MatNativeDateModule,
    { provide: MatPaginatorIntl, useClass: CustomPaginatorIntl },
    { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS },
    { provide: MAT_DATE_LOCALE, useValue: "es-ES" },
  ],
};
