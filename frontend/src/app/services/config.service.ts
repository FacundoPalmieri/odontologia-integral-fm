import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { map, Observable } from "rxjs";
import { ApiResponseInterface } from "../domain/interfaces/api-response.interface";
import { MessageInterface } from "../domain/interfaces/message.interface";
import {
  MessageCreateDtoInterface,
  MessageDtoInterface,
} from "../domain/dto/message.dto";
import { MessageSerializer } from "../domain/serializers/message.serializer";

@Injectable({ providedIn: "root" })
export class ConfigService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  getTokenExpirationTime(): Observable<ApiResponseInterface<number>> {
    return this.http.get<ApiResponseInterface<number>>(
      `${this.apiUrl}/config/token`
    );
  }

  updateTokenExpirationTime(
    time: number
  ): Observable<ApiResponseInterface<number>> {
    return this.http.patch<ApiResponseInterface<number>>(
      `${this.apiUrl}/config/token`,
      {
        expiration: time,
      }
    );
  }

  getRefreshTokenExpirationTime(): Observable<ApiResponseInterface<number>> {
    return this.http.get<ApiResponseInterface<number>>(
      `${this.apiUrl}/config/token/refresh`
    );
  }

  updateRefreshTokenExpirationTime(
    time: number
  ): Observable<ApiResponseInterface<number>> {
    return this.http.patch<ApiResponseInterface<number>>(
      `${this.apiUrl}/config/token/refresh`,
      {
        expiration: time,
      }
    );
  }

  getFailedAttempts(): Observable<ApiResponseInterface<number>> {
    return this.http.get<ApiResponseInterface<number>>(
      `${this.apiUrl}/config/session`
    );
  }

  updateFailedAttempts(
    attemtps: number
  ): Observable<ApiResponseInterface<number>> {
    return this.http.patch<ApiResponseInterface<number>>(
      `${this.apiUrl}/config/session`,
      {
        value: attemtps,
      }
    );
  }

  getCronSchedule(): Observable<ApiResponseInterface<string>> {
    return this.http.get<ApiResponseInterface<string>>(
      `${this.apiUrl}/config/schedule`
    );
  }

  updateCronSchedule(
    cronExpression: string
  ): Observable<ApiResponseInterface<string>> {
    return this.http.patch<ApiResponseInterface<string>>(
      `${this.apiUrl}/config/schedule`,
      {
        id: 1, //El get tendría que retornarme el id para que lo pueda enviar en el patch
        cronExpression: cronExpression,
      }
    );
  }

  getMessages(): Observable<ApiResponseInterface<MessageInterface[]>> {
    return this.http
      .get<ApiResponseInterface<MessageDtoInterface[]>>(
        `${this.apiUrl}/config/message`
      )
      .pipe(
        map((response) => ({
          ...response,
          data: response.data.map((message) =>
            MessageSerializer.toView(message)
          ),
        }))
      );
  }

  updateMessage(
    message: MessageCreateDtoInterface
  ): Observable<ApiResponseInterface<MessageInterface>> {
    return this.http
      .patch<ApiResponseInterface<MessageDtoInterface>>(
        `${this.apiUrl}/config/message`,
        message
      )
      .pipe(
        map((response) => ({
          ...response,
          data: MessageSerializer.toView(response.data),
        }))
      );
  }
}
