import { HttpInterceptorFn } from "@angular/common/http";

export const tokenInterceptor: HttpInterceptorFn = (req, next) => {
  const token = ""; // Obtener del local o session storage

  const excludedUrls = [
    "/auth/login",
    "/auth/request/reset-password",
    "/auth/reset-password",
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
