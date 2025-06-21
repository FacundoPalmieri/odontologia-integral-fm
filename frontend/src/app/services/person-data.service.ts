import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { Observable } from "rxjs";
import { ApiResponseInterface } from "../domain/interfaces/api-response.interface";
import {
  CountryInterface,
  DentistSpecialtyInterface,
  DniTypeInterface,
  GenderInterface,
  HealthPlanInterface,
  LocalityInterface,
  NationalityInterface,
  PhoneTypeInterface,
  ProvinceInterface,
} from "../domain/interfaces/person-data.interface";

@Injectable({ providedIn: "root" })
export class PersonDataService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  getAllNationalities(): Observable<
    ApiResponseInterface<NationalityInterface[]>
  > {
    return this.http.get<ApiResponseInterface<NationalityInterface[]>>(
      `${this.apiUrl}/nationality/all`
    );
  }

  getAllHealthPlans(): Observable<ApiResponseInterface<HealthPlanInterface[]>> {
    return this.http.get<ApiResponseInterface<HealthPlanInterface[]>>(
      `${this.apiUrl}/health-plans/all`
    );
  }

  getProvinceByCountryId(
    id: number
  ): Observable<ApiResponseInterface<ProvinceInterface[]>> {
    return this.http.get<ApiResponseInterface<ProvinceInterface[]>>(
      `${this.apiUrl}/geo/provinces/${id}`
    );
  }

  getLocalityByProvinceId(
    id: number
  ): Observable<ApiResponseInterface<LocalityInterface[]>> {
    return this.http.get<ApiResponseInterface<LocalityInterface[]>>(
      `${this.apiUrl}/geo/localities/${id}`
    );
  }

  getAllCountries(): Observable<ApiResponseInterface<CountryInterface[]>> {
    return this.http.get<ApiResponseInterface<CountryInterface[]>>(
      `${this.apiUrl}/geo/countries/all`
    );
  }

  getAllGenders(): Observable<ApiResponseInterface<GenderInterface[]>> {
    return this.http.get<ApiResponseInterface<GenderInterface[]>>(
      `${this.apiUrl}/gender/all`
    );
  }

  getAllDNITypes(): Observable<ApiResponseInterface<DniTypeInterface[]>> {
    return this.http.get<ApiResponseInterface<DniTypeInterface[]>>(
      `${this.apiUrl}/dni-type/all`
    );
  }

  getAllPhoneTypes(): Observable<ApiResponseInterface<PhoneTypeInterface[]>> {
    return this.http.get<ApiResponseInterface<PhoneTypeInterface[]>>(
      `${this.apiUrl}/phone-type/all`
    );
  }

  getAllDentistSpecialties(): Observable<
    ApiResponseInterface<DentistSpecialtyInterface[]>
  > {
    return this.http.get<ApiResponseInterface<DentistSpecialtyInterface[]>>(
      `${this.apiUrl}/dentist-Specialty/all`
    );
  }
}
