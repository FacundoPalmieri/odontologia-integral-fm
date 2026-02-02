import {
  CountryInterface,
  DniTypeInterface,
  GenderInterface,
  LocalityInterface,
  NationalityInterface,
  PhoneTypeInterface,
  ProvinceInterface,
} from "./person-data.interface";

export interface PersonInterface {
  id?: number;
  firstName: string;
  lastName: string;
  dniType: DniTypeInterface;
  dni: string;
  birthDate: Date;
  gender: GenderInterface;
  nationality: NationalityInterface;
  contactEmails: string;
  phoneType: PhoneTypeInterface;
  phone: string;
  country: CountryInterface;
  province: ProvinceInterface;
  locality: LocalityInterface;
  street: string;
  number: number;
  floor: string;
  apartment: string;
}
