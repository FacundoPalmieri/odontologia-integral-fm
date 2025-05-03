import { ApplicationConfig, provideZoneChangeDetection } from "@angular/core";
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
import { errorInterceptor } from "../utils/interceptors/error.interceptor";
import { tokenInterceptor } from "../utils/interceptors/token.interceptor";
import { CustomPaginatorIntl } from "../utils/custom-paginator.initializer";
import { MatPaginatorIntl } from "@angular/material/paginator";
import {
  MatNativeDateModule,
  provideNativeDateAdapter,
} from "@angular/material/core";
import { provideCharts, withDefaultRegisterables } from "ng2-charts";

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideClientHydration(withEventReplay()),
    provideAnimationsAsync(),
    provideHttpClient(
      withInterceptors([errorInterceptor, tokenInterceptor]),
      withFetch()
    ),
    provideNativeDateAdapter(),
    provideCharts(withDefaultRegisterables()),
    MatNativeDateModule,
    { provide: MatPaginatorIntl, useClass: CustomPaginatorIntl },
  ],
};
