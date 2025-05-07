import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { Observable, of } from "rxjs";
import { ApiResponseInterface } from "../domain/interfaces/api-response.interface";
import {
  CountryInterface,
  DniTypeInterface,
  GenderInterface,
  HealthPlanInterface,
  LocalityInterface,
  NationalityInterface,
  PatientInterface,
  ProvinceInterface,
} from "../domain/interfaces/patient.interface";
import { PatientCreateDto } from "../domain/dto/patient.dto";

@Injectable({ providedIn: "root" })
export class PatientService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  private _mockPatients: PatientInterface[] = [
    {
      firstName: "María",
      lastName: "Pérez",
      dniType: {
        id: 1,
        dni: "DNI",
      },
      dni: "40999888",
      birthDate: new Date(1998, 2, 11),
      gender: {
        id: 2,
        alias: "Mujer",
      },
      nationality: {
        id: 1,
        name: "Argentina",
      },
      locality: {
        id: 1,
        name: "San Justo",
      },
      street: "Av. Siempre Viva",
      number: 742,
      floor: "3",
      apartment: "B",
      healthPlan: {
        id: 2,
        name: "Swiss Medical",
      },
      affiliateNumber: "A123456789",
      email: "maria.perez@example.com",
      phoneType: {
        id: 1,
        name: "Celular",
      },
      phone: "1123456789",
    },
    {
      firstName: "Carlos",
      lastName: "Rodríguez",
      dniType: { id: 1, dni: "DNI" },
      dni: "38765432",
      birthDate: new Date(1985, 3, 8),
      gender: { id: 1, alias: "Hombre" },
      nationality: { id: 1, name: "Argentina" },
      locality: { id: 1, name: "San Telmo" },
      street: "Calle de la Luna",
      number: 789,
      floor: "4",
      apartment: "D",
      healthPlan: { id: 2, name: "Swiss Medical" },
      affiliateNumber: "S789123456",
      email: "carlos.rodriguez@example.com",
      phoneType: { id: 2, name: "Fijo" },
      phone: "1156789123",
    },
    {
      firstName: "Laura",
      lastName: "Martínez",
      dniType: { id: 1, dni: "DNI" },
      dni: "45678901",
      birthDate: new Date(2002, 7, 12),
      gender: { id: 2, alias: "Mujer" },
      nationality: { id: 1, name: "Argentina" },
      locality: { id: 4, name: "Villa Urquiza" },
      street: "Pasaje Estrella",
      number: 246,
      floor: "PB",
      apartment: "N/A",
      healthPlan: { id: 1, name: "OSDE" },
      affiliateNumber: "O246813579",
      email: "laura.martinez@example.com",
      phoneType: { id: 1, name: "Celular" },
      phone: "2611357924",
    },
    {
      firstName: "Ana",
      lastName: "Gómez",
      dniType: { id: 1, dni: "DNI" },
      dni: "25123456",
      birthDate: new Date(1978, 11, 20),
      gender: { id: 2, alias: "Mujer" },
      nationality: { id: 1, name: "Argentina" },
      locality: { id: 3, name: "Palermo" },
      street: "Avenida Siempreviva",
      number: 1234,
      floor: "PB",
      apartment: "A",
      healthPlan: { id: 1, name: "OSDE" },
      affiliateNumber: "O987654321",
      email: "ana.gomez@example.com",
      phoneType: { id: 1, name: "Celular" },
      phone: "1511223344",
    },
    {
      firstName: "Martín",
      lastName: "Pérez",
      dniType: { id: 2, dni: "LC" },
      dni: "1234567",
      birthDate: new Date(1965, 11, 3),
      gender: { id: 1, alias: "Hombre" },
      nationality: { id: 2, name: "Uruguay" },
      locality: { id: 5, name: "Belgrano" },
      street: "Calle Falsa",
      number: 567,
      floor: "1",
      apartment: "B",
      healthPlan: { id: 3, name: "Medifé" },
      affiliateNumber: "M654321987",
      email: "martin.perez@example.com",
      phoneType: { id: 2, name: "Fijo" },
      phone: "47778899",
    },
    {
      firstName: "Laura",
      lastName: "Fernández",
      dniType: { id: 1, dni: "DNI" },
      dni: "41987654",
      birthDate: new Date(1990, 9, 1),
      gender: { id: 2, alias: "Mujer" },
      nationality: { id: 1, name: "Argentina" },
      locality: { id: 2, name: "Recoleta" },
      street: "Pasaje Secreto",
      number: 234,
      floor: "2",
      apartment: "C",
      healthPlan: { id: 2, name: "Swiss Medical" },
      affiliateNumber: "S112233445",
      email: "laura.fernandez@example.com",
      phoneType: { id: 1, name: "Celular" },
      phone: "1599887766",
    },
    {
      firstName: "Javier",
      lastName: "López",
      dniType: { id: 1, dni: "DNI" },
      dni: "30543210",
      birthDate: new Date(1970, 12, 25),
      gender: { id: 1, alias: "Hombre" },
      nationality: { id: 3, name: "Argentina" },
      locality: { id: 4, name: "San Nicolás" },
      street: "Avenida Principal",
      number: 1010,
      floor: "5",
      apartment: "E",
      healthPlan: { id: 1, name: "OSDE" },
      affiliateNumber: "O556677889",
      email: "javier.lopez@example.com",
      phoneType: { id: 2, name: "Fijo" },
      phone: "43332211",
    },
    {
      firstName: "Sofía",
      lastName: "Martínez",
      dniType: { id: 1, dni: "DNI" },
      dni: "45678901",
      birthDate: new Date(1995, 7, 10),
      gender: { id: 2, alias: "Mujer" },
      nationality: { id: 1, name: "Argentina" },
      locality: { id: 1, name: "San Telmo" },
      street: "Calle Antigua",
      number: 321,
      floor: "3",
      apartment: "F",
      healthPlan: { id: 3, name: "Medifé" },
      affiliateNumber: "M998877665",
      email: "sofia.martinez@example.com",
      phoneType: { id: 1, name: "Celular" },
      phone: "1544556677",
    },
  ];

  getAll(): Observable<ApiResponseInterface<PatientInterface[]>> {
    const response: ApiResponseInterface<PatientInterface[]> = {
      success: true,
      message: "",
      data: this._mockPatients,
    };
    return of(response);
  }

  create(
    patient: PatientCreateDto
  ): Observable<ApiResponseInterface<PatientCreateDto>> {
    return this.http.post<ApiResponseInterface<PatientCreateDto>>(
      `${this.apiUrl}/patient`,
      patient
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
}
