import {
  DisseaseConditionEnum,
  DisseaseEnum,
} from "../../utils/enums/dissease.enum";

export interface PatientInterface {
  id: number;
  firstName: string;
  lastName: string;
  dniType: DniTypeInterface;
  dni: string;
  birthDate: Date;
  gender: GenderInterface;
  nationality: NationalityInterface;
  country: CountryInterface;
  province: ProvinceInterface;
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

export interface DisseaseInterface {
  dissease: DisseaseTypeInterface;
  condition?: DisseaseConditionInterface[];
  type?: string;
  medicament?: string;
  description?: string;
}

export interface DisseaseTypeInterface {
  dissease: DisseaseEnum;
  name: string;
}

export interface DisseaseConditionInterface {
  condition: DisseaseConditionEnum;
  name: string;
}
