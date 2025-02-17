import { HttpErrorResponse, HttpInterceptorFn } from "@angular/common/http";
import { catchError, throwError } from "rxjs";

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 400) {
        console.error("Bad request"); // Cambiar console.log por snackbar o toasts
      } else if (error.status === 500) {
        console.error("Internal error");
      }

      return throwError(() => error.error.message);
    })
  );
};
