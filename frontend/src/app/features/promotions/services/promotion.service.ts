import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Observable, map } from "rxjs";
import { PromotionInterface } from "../data/interfaces/promotion.interface";
import { PromotionSerializer } from "../data/serializers/promotion.serializer";
import { PromotionDtoInterface } from "../data/dtos/promotion.dto";
import { environment } from "../../../environments/environment";
import { ApiResponseInterface } from "../../../shared/interfaces/api-response.interface";

@Injectable({ providedIn: "root" })
export class PromotionService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  getAll(): Observable<ApiResponseInterface<PromotionInterface[]>> {
    return this.http
      .get<ApiResponseInterface<PromotionDtoInterface[]>>(`${this.apiUrl}/promotion/all`)
      .pipe(
        map((response) => ({
          ...response,
          data: response.data.map((dto) => PromotionSerializer.toView(dto)),
        }))
      );
  }

  create(promotion: Omit<PromotionInterface, "id">): Observable<ApiResponseInterface<PromotionInterface>> {
    const dto = PromotionSerializer.toCreateDto(promotion);

    return this.http
      .post<ApiResponseInterface<PromotionDtoInterface>>(`${this.apiUrl}/promotion`, dto)
      .pipe(
        map((response) => ({
          ...response,
          data: PromotionSerializer.toView(response.data),
        }))
      );
  }

  update(id: string, promotion: Omit<PromotionInterface, "id">): Observable<ApiResponseInterface<PromotionInterface>> {
    const dto = PromotionSerializer.toCreateDto(promotion);

    return this.http
      .put<ApiResponseInterface<PromotionDtoInterface>>(`${this.apiUrl}/promotion/${id}`, dto)
      .pipe(
        map((response) => ({
          ...response,
          data: PromotionSerializer.toView(response.data),
        }))
      );
  }

  finish(id: string): Observable<ApiResponseInterface<PromotionInterface>> {
    return this.http
      .patch<ApiResponseInterface<PromotionDtoInterface>>(`${this.apiUrl}/promotion/finish/${id}`, {})
      .pipe(
        map((response) => ({
          ...response,
          data: PromotionSerializer.toView(response.data),
        }))
      );
  }
}
