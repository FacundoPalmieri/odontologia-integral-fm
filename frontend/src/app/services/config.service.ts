import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { map, Observable } from "rxjs";
import { ApiResponseInterface } from "../domain/interfaces/api-response.interface";
import {
  MessageInterface,
  ScheduleInterface,
  SystemParameterInterface,
} from "../domain/interfaces/config.interface";
import { MessageSerializer } from "../domain/serializers/message.serializer";
import {
  MessageDtoInterface,
  MessageUpdateDtoInterface,
  ScheduleDtoInterface,
  ScheduleUpdateDtoInterface,
  SystemParameterDtoInterface,
  SystemParameterUpdateDtoInterface,
} from "../domain/dto/config.dto";

@Injectable({ providedIn: "root" })
export class ConfigService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  getSystemParameters(): Observable<
    ApiResponseInterface<SystemParameterInterface[]>
  > {
    return this.http.get<ApiResponseInterface<SystemParameterDtoInterface[]>>(
      `${this.apiUrl}/config/system-parameters`
    );
  }

  updateSystemParameter(
    systemParameter: SystemParameterUpdateDtoInterface
  ): Observable<ApiResponseInterface<string>> {
    return this.http.patch<ApiResponseInterface<string>>(
      `${this.apiUrl}/config/system-parameters`,
      systemParameter
    );
  }

  getSchedules(): Observable<ApiResponseInterface<ScheduleInterface[]>> {
    return this.http.get<ApiResponseInterface<ScheduleDtoInterface[]>>(
      `${this.apiUrl}/config/all/schedule`
    );
  }

  updateSchedule(
    schedule: ScheduleUpdateDtoInterface
  ): Observable<ApiResponseInterface<string>> {
    return this.http.patch<ApiResponseInterface<string>>(
      `${this.apiUrl}/config/schedule`,
      schedule
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
    message: MessageUpdateDtoInterface
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
