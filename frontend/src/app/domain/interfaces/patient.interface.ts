import {
  DisseaseConditionEnum,
  DisseaseEnum,
} from "../../utils/enums/dissease.enum";
import {
  CountryInterface,
  DniTypeInterface,
  GenderInterface,
  HealthPlanInterface,
  LocalityInterface,
  NationalityInterface,
  PhoneTypeInterface,
  ProvinceInterface,
} from "./person-data.interface";

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
