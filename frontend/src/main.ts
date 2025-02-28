import { bootstrapApplication } from "@angular/platform-browser";
import { appConfig } from "./app/presentation/app.config";
import { AppComponent } from "./app/presentation/app.component";

bootstrapApplication(AppComponent, appConfig).catch((err) =>
  console.error(err)
);
