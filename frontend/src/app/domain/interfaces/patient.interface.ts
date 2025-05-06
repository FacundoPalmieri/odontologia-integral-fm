import { DisseaseInterface } from "./clinical-history.interface";

export interface PatientInterface {
  firstName: string;
  lastName: string;
  dniType: DniTypeInterface;
  dni: string;
  birthDate: Date;
  gender: GenderInterface;
  nationality: NationalityInterface;
  locality: LocalityInterface;
  street: string;
  number: number;
  floor: string;
  apartment: string;
  healthPlan: HealthPlanInterface;
  affiliateNumber: string;
  email: string;
  phoneType: PhoneTypeInterface;
  phone: string;
  // medicalRisk: DisseaseInterface[];
  // medicalHistoryObservation: string;
}

export interface HealthPlanInterface {
  id: number;
  name: string;
}

export interface BaseGeoEntityInterface {
  id: number;
  name: string;
}

export interface ProvinceInterface extends BaseGeoEntityInterface {}
export interface NationalityInterface extends BaseGeoEntityInterface {}
export interface LocalityInterface extends BaseGeoEntityInterface {}
export interface CountryInterface extends BaseGeoEntityInterface {}
export interface PhoneTypeInterface extends BaseGeoEntityInterface {}

export interface GenderInterface {
  id: number;
  alias: string;
}

export interface DniTypeInterface {
  id: number;
  dni: string;
}
