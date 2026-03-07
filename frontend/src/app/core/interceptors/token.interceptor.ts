import {
  HttpInterceptorFn,
  HttpRequest,
  HttpHandlerFn,
  HttpErrorResponse,
} from "@angular/common/http";
import { inject } from "@angular/core";
import { catchError, switchMap, throwError, filter, take } from "rxjs";
import { Router } from "@angular/router";
import { RefreshTokenDataDto } from "../../features/auth/data/dtos/auth.dto";
import { AuthService } from "../../features/auth/services/auth.service";
import { UserDataInterface } from "../../features/auth/data/interfaces/auth.interface";
import { LocalStorageService } from "../../shared/services/local-storage.service";

export const tokenInterceptor: HttpInterceptorFn = (
  req: HttpRequest<any>,
  next: HttpHandlerFn,
) => {
  const authService = inject(AuthService);
  const localStorageService = inject(LocalStorageService);
  const router = inject(Router);
  const token = localStorageService.getJwtToken();
  const userData: UserDataInterface | null = localStorageService.getUserData();

  const excludedUrls = [
    "/auth/login",
    "/auth/password/reset-request",
    "/auth/password/reset",
    "/auth/token/refresh",
  ];

  if (excludedUrls.some((url) => req.url.includes(url))) {
    return next(req);
  }

  const newRequest = token
    ? req.clone({
        headers: req.headers.append("Authorization", `Bearer ${token}`),
      })
    : req;

  return next(newRequest).pipe(
    catchError((error: any) => {
      if (error instanceof HttpErrorResponse && error.status === 401) {
        if (authService.refreshTokenInProgress) {
          return authService.refreshTokenSubject.pipe(
            filter((result: string | null) => !!result),
            take(1),
            switchMap((newToken) => next(addToken(req, newToken!))),
          );
        } else {
          authService.refreshTokenInProgress = true;
          authService.refreshTokenSubject.next(null);

          const refreshTokenData: RefreshTokenDataDto = {
            jwt: userData?.jwt || "",
            refreshToken: userData?.refreshToken || "",
            idUser: userData?.idUser || 0,
            username: userData?.username || "",
          };

          return authService.refreshToken(refreshTokenData).pipe(
            switchMap((response) => {
              return authService.refreshTokenSubject.pipe(
                filter((newToken: string | null) => !!newToken),
                take(1),
                switchMap((newToken) => next(addToken(req, newToken!))),
              );
            }),
            catchError((refreshError) => {
              authService.refreshTokenInProgress = false;
              localStorageService.doLogout();
              router.navigateByUrl("/login");
              return throwError(() => refreshError);
            }),
          );
        }
      }
      return throwError(() => error);
    }),
  );
};

function addToken(req: HttpRequest<any>, token: string): HttpRequest<any> {
  return req.clone({
    headers: req.headers.append("Authorization", `Bearer ${token}`),
  });
}
