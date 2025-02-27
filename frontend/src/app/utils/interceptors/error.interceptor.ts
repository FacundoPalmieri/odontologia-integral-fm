import { HttpErrorResponse, HttpInterceptorFn } from "@angular/common/http";
import { inject } from "@angular/core";
import { catchError, throwError } from "rxjs";
import { SnackbarService } from "../../services/snackbar.service";

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const snackbarService = inject(SnackbarService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = "Ocurrió un error inesperado.";

      if (error.error?.message) {
        errorMessage = error.error.message;
      }

      snackbarService.openSnackbar(errorMessage, 3000, "center", "bottom");

      return throwError(() => errorMessage);
    })
  );
};
