import { HttpClient } from "@angular/common/http";
import { inject, Injectable, signal } from "@angular/core";
import { environment } from "../../environments/environment";
import { forkJoin, Observable, switchMap, tap } from "rxjs";
import { ApiResponseInterface } from "../interfaces/api-response.interface";
import {
  CountryInterface,
  DniTypeInterface,
  GenderInterface,
  HealthPlanInterface,
  LocalityInterface,
  NationalityInterface,
  PhoneTypeInterface,
  ProvinceInterface,
} from "../interfaces/person-data.interface";
import { DentistSpecialtyInterface } from "../../features/users/data/interfaces/user.interface";
import { MedicalHistoryRiskInterface } from "../../features/patients/data/interfaces/medical-history.interface";

/**
 * Service for managing person-related catalog data and avatars.
 *
 * This service handles all catalog/reference data used in person forms,
 * including geographic data, medical information, and user profile images.
 * It provides:
 * - Catalog data loading and caching using signals
 * - Geographic hierarchy (countries, provinces, localities)
 * - Medical and dental reference data
 * - Avatar upload/download functionality
 */
@Injectable({ providedIn: "root" })
export class PersonDataService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  /** Signal containing all nationalities */
  nationalities = signal<NationalityInterface[]>([]);
  /** Signal containing all health plans */
  healthPlans = signal<HealthPlanInterface[]>([]);
  /** Signal containing all countries */
  countries = signal<CountryInterface[]>([]);
  /** Signal containing provinces (filtered by selected country) */
  provinces = signal<ProvinceInterface[]>([]);
  /** Signal containing localities (filtered by selected province) */
  localities = signal<LocalityInterface[]>([]);
  /** Signal containing all genders */
  genders = signal<GenderInterface[]>([]);
  /** Signal containing all DNI/ID document types */
  dniTypes = signal<DniTypeInterface[]>([]);
  /** Signal containing all phone types */
  phoneTypes = signal<PhoneTypeInterface[]>([]);
  /** Signal containing all dentist specialties */
  dentistSpecialties = signal<DentistSpecialtyInterface[]>([]);
  /** Signal containing all medical history risk factors */
  medicalHistoryRisks = signal<MedicalHistoryRiskInterface[]>([]);

  /**
   * Loads all catalog data in parallel and updates the signals.
   *
   * This method should be called once during application initialization
   * to pre-load all reference data. It uses forkJoin to load all catalogs
   * simultaneously for better performance.
   *
   * @returns Observable that completes when all catalogs are loaded
   */
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
      }),
    );
  }

  /**
   * Retrieves all nationalities.
   *
   * @returns Observable with array of nationality data
   */
  getAllNationalities(): Observable<
    ApiResponseInterface<NationalityInterface[]>
  > {
    return this.http.get<ApiResponseInterface<NationalityInterface[]>>(
      `${this.apiUrl}/nationality/all`,
    );
  }

  /**
   * Retrieves all health insurance plans.
   *
   * @returns Observable with array of health plan data
   */
  getAllHealthPlans(): Observable<ApiResponseInterface<HealthPlanInterface[]>> {
    return this.http.get<ApiResponseInterface<HealthPlanInterface[]>>(
      `${this.apiUrl}/health-plans/all`,
    );
  }

  /**
   * Retrieves all provinces for a specific country.
   *
   * @param id - The ID of the country
   * @returns Observable with array of province data
   */
  getProvinceByCountryId(
    id: number,
  ): Observable<ApiResponseInterface<ProvinceInterface[]>> {
    return this.http.get<ApiResponseInterface<ProvinceInterface[]>>(
      `${this.apiUrl}/geo/provinces/${id}`,
    );
  }

  /**
   * Retrieves all localities for a specific province.
   *
   * @param id - The ID of the province
   * @returns Observable with array of locality data
   */
  getLocalityByProvinceId(
    id: number,
  ): Observable<ApiResponseInterface<LocalityInterface[]>> {
    return this.http.get<ApiResponseInterface<LocalityInterface[]>>(
      `${this.apiUrl}/geo/localities/${id}`,
    );
  }

  /**
   * Retrieves all countries.
   *
   * @returns Observable with array of country data
   */
  getAllCountries(): Observable<ApiResponseInterface<CountryInterface[]>> {
    return this.http.get<ApiResponseInterface<CountryInterface[]>>(
      `${this.apiUrl}/geo/countries/all`,
    );
  }

  /**
   * Retrieves all gender options.
   *
   * @returns Observable with array of gender data
   */
  getAllGenders(): Observable<ApiResponseInterface<GenderInterface[]>> {
    return this.http.get<ApiResponseInterface<GenderInterface[]>>(
      `${this.apiUrl}/gender/all`,
    );
  }

  /**
   * Retrieves all DNI/ID document types.
   *
   * @returns Observable with array of DNI type data
   */
  getAllDNITypes(): Observable<ApiResponseInterface<DniTypeInterface[]>> {
    return this.http.get<ApiResponseInterface<DniTypeInterface[]>>(
      `${this.apiUrl}/dni-type/all`,
    );
  }

  /**
   * Retrieves all phone number types.
   *
   * @returns Observable with array of phone type data
   */
  getAllPhoneTypes(): Observable<ApiResponseInterface<PhoneTypeInterface[]>> {
    return this.http.get<ApiResponseInterface<PhoneTypeInterface[]>>(
      `${this.apiUrl}/phone-type/all`,
    );
  }

  /**
   * Retrieves all dentist specialties.
   *
   * @returns Observable with array of dentist specialty data
   */
  getAllDentistSpecialties(): Observable<
    ApiResponseInterface<DentistSpecialtyInterface[]>
  > {
    return this.http.get<ApiResponseInterface<DentistSpecialtyInterface[]>>(
      `${this.apiUrl}/dentist-Specialty/all`,
    );
  }

  /**
   * Retrieves all medical history risk factors.
   *
   * Risk factors include conditions like diabetes, hypertension, allergies, etc.
   *
   * @returns Observable with array of medical risk data
   */
  getAllMedicalHistoryRisks(): Observable<
    ApiResponseInterface<MedicalHistoryRiskInterface[]>
  > {
    return this.http.get<ApiResponseInterface<MedicalHistoryRiskInterface[]>>(
      `${this.apiUrl}/medical-risk/all`,
    );
  }

  /**
   * Retrieves a person's avatar image.
   *
   * Returns the avatar as a base64 data URL that can be used in img src attributes.
   * Returns null if no avatar is set.
   *
   * @param id - The ID of the person
   * @returns Observable with the avatar data URL, or null if no avatar exists
   */
  getAvatar(id: number): Observable<string | null> {
    return this.http
      .get(`${this.apiUrl}/person/${id}/avatar`, {
        responseType: "blob",
        observe: "response",
      })
      .pipe(
        switchMap((response) => {
          if (
            response.status === 204 ||
            !response.body ||
            response.body.size === 0
          ) {
            return new Observable<string | null>((observer) => {
              observer.next(null);
              observer.complete();
            });
          }

          return new Observable<string>((observer) => {
            const reader = new FileReader();
            reader.onloadend = () => {
              observer.next(reader.result as string);
              observer.complete();
            };
            reader.readAsDataURL(response.body!);
          });
        }),
      );
  }

  /**
   * Uploads or updates a person's avatar image.
   *
   * @param id - The ID of the person
   * @param file - The image file to upload
   * @returns Observable with upload confirmation message
   */
  setAvatar(id: number, file: File): Observable<ApiResponseInterface<string>> {
    const formData = new FormData();
    formData.append("file", file);

    return this.http.post<ApiResponseInterface<string>>(
      `${this.apiUrl}/person/${id}/avatar`,
      formData,
    );
  }

  /**
   * Removes a person's avatar image.
   *
   * @param id - The ID of the person
   * @returns Observable with deletion confirmation message
   */
  removeAvatar(id: number): Observable<ApiResponseInterface<string>> {
    return this.http.delete<ApiResponseInterface<string>>(
      `${this.apiUrl}/person/${id}/avatar`,
    );
  }
}
