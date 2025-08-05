import { HttpClient } from "@angular/common/http";
import { inject, Injectable, signal } from "@angular/core";
import { environment } from "../environments/environment";
import { forkJoin, Observable, switchMap, tap } from "rxjs";
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
import { MedicalHistoryRiskInterface } from "../domain/interfaces/patient.interface";

@Injectable({ providedIn: "root" })
export class PersonDataService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  nationalities = signal<NationalityInterface[]>([]);
  healthPlans = signal<HealthPlanInterface[]>([]);
  countries = signal<CountryInterface[]>([]);
  provinces = signal<ProvinceInterface[]>([]);
  localities = signal<LocalityInterface[]>([]);
  genders = signal<GenderInterface[]>([]);
  dniTypes = signal<DniTypeInterface[]>([]);
  phoneTypes = signal<PhoneTypeInterface[]>([]);
  dentistSpecialties = signal<DentistSpecialtyInterface[]>([]);
  medicalHistoryRisks = signal<MedicalHistoryRiskInterface[]>([]);

  loadAllCatalogs() {
    return forkJoin({
      nationalities: this.getAllNationalities(),
      healthPlans: this.getAllHealthPlans(),
      countries: this.getAllCountries(),
      genders: this.getAllGenders(),
      dniTypes: this.getAllDNITypes(),
      phoneTypes: this.getAllPhoneTypes(),
      dentistSpecialties: this.getAllDentistSpecialties(),
      medicalHistoryRisks: this.getAllMedicalHistoryRisks(),
    }).pipe(
      tap((results) => {
        this.nationalities.set(results.nationalities.data);
        this.healthPlans.set(results.healthPlans.data);
        this.countries.set(results.countries.data);
        this.genders.set(results.genders.data);
        this.dniTypes.set(results.dniTypes.data);
        this.phoneTypes.set(results.phoneTypes.data);
        this.dentistSpecialties.set(results.dentistSpecialties.data);
        this.medicalHistoryRisks.set(results.medicalHistoryRisks.data);
      })
    );
  }

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

  getAllMedicalHistoryRisks(): Observable<
    ApiResponseInterface<MedicalHistoryRiskInterface[]>
  > {
    return this.http.get<ApiResponseInterface<MedicalHistoryRiskInterface[]>>(
      `${this.apiUrl}/medical-risk/all`
    );
  }

  getAvatar(id: number): Observable<string> {
    return this.http
      .get(`${this.apiUrl}/person/${id}/avatar`, { responseType: "blob" })
      .pipe(
        switchMap((blob) => {
          return new Observable<string>((observer) => {
            const reader = new FileReader();
            reader.onloadend = () => {
              observer.next(reader.result as string);
              observer.complete();
            };
            reader.readAsDataURL(blob);
          });
        })
      );
  }

  setAvatar(id: number, file: File): Observable<ApiResponseInterface<string>> {
    const formData = new FormData();
    formData.append("file", file);

    return this.http.post<ApiResponseInterface<string>>(
      `${this.apiUrl}/person/${id}/avatar`,
      formData
    );
  }

  removeAvatar(id: number): Observable<ApiResponseInterface<string>> {
    return this.http.delete<ApiResponseInterface<string>>(
      `${this.apiUrl}/person/${id}/avatar`
    );
  }

  uploadFile(
    personId: number,
    file: File
  ): Observable<ApiResponseInterface<string>> {
    const formData = new FormData();
    formData.append("file", file);
    return this.http.post<ApiResponseInterface<string>>(
      `${this.apiUrl}/files/${personId}`,
      formData
    );
  }
}
