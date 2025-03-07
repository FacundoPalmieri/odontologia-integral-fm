import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { Observable } from "rxjs";
import { ApiResponseInterface } from "../domain/interfaces/api-response.interface";
import { MessageInterface } from "../domain/interfaces/message.interface";
import { MessageDto } from "../domain/dto/message-update.dto";

@Injectable({ providedIn: "root" })
export class DevService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  getTokenExpirationTime(): Observable<ApiResponseInterface<number>> {
    return this.http.get<ApiResponseInterface<number>>(
      `${this.apiUrl}/dev/token/get`
    );
  }

  updateTokenExpirationTime(
    time: number
  ): Observable<ApiResponseInterface<number>> {
    return this.http.patch<ApiResponseInterface<number>>(
      `${this.apiUrl}/dev/token/update`,
      {
        expiration: time,
      }
    );
  }

  getFailedAttempts(): Observable<ApiResponseInterface<number>> {
    return this.http.get<ApiResponseInterface<number>>(
      `${this.apiUrl}/dev/session/get`
    );
  }

  updateFailedAttempts(
    attemtps: number
  ): Observable<ApiResponseInterface<number>> {
    return this.http.patch<ApiResponseInterface<number>>(
      `${this.apiUrl}/dev/session/update`,
      {
        value: attemtps,
      }
    );
  }

  getMessages(): Observable<ApiResponseInterface<MessageInterface[]>> {
    return this.http.get<ApiResponseInterface<MessageInterface[]>>(
      `${this.apiUrl}/dev/message/get`
    );
  }

  updateMessage(
    message: MessageDto
  ): Observable<ApiResponseInterface<MessageInterface>> {
    return this.http.patch<ApiResponseInterface<MessageInterface>>(
      `${this.apiUrl}/dev/message/update`,
      message
    );
  }
}
