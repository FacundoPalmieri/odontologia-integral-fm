import { HttpInterceptorFn } from "@angular/common/http";
import { inject } from "@angular/core";
import { AuthService } from "../../services/auth.service";

export const tokenInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getJwtToken();

  const excludedUrls = [
    "/auth/login",
    "/auth/password/reset-request",
    "/auth/password/reset",
  ];

  if (excludedUrls.some((url) => req.url.includes(url))) {
    return next(req);
  }

  const newRequest = token
    ? req.clone({
        headers: req.headers.append("Authorization", `Bearer ${token}`),
      })
    : req;

  return next(newRequest);
};
