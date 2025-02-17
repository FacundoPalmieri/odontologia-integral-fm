import { HttpInterceptorFn } from "@angular/common/http";

export const tokenInterceptor: HttpInterceptorFn = (req, next) => {
  const token = ""; // Obtener del local o session storage

  const newRequest = req.clone({
    headers: req.headers.append("Authorization", `Bearer ${token}`),
  });

  return next(newRequest);
};
