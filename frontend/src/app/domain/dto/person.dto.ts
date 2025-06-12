export interface PersonDtoInterface {
  id: number;
  firstName: string;
  lastName: string;
  dniType: string;
  dni: string;
  birthDate: Date;
  age: number;
  gender: string;
  nationality: string;
  contactEmails: string[];
  contactPhone: ContactPhoneDtoInterface[];
  address: AddressDtoInterface[];
}

export interface AddressDtoInterface {
  localityId: number;
  locality: string;
  provinceId: number;
  province: string;
  countryId: number;
  country: string;
  street: string;
  number: number;
  floor: string;
}

export interface ContactPhoneDtoInterface {
  typePhone: string;
  phone: string;
}

export interface PersonCreateDtoInterface {
  firstName: string;
  lastname: string;
  dniTypeId: number;
  dni: string;
  birthDate: Date;
  genderId: number;
  nationalityId: number;
  contactEmails: string[];
  contactPhones: ContactPhoneCreateDtoInterface[];
  address: AddressCreateDtoInterface[];
}

export interface AddressCreateDtoInterface {
  localityId: number;
  street: string;
  number: number;
  floor: string;
  apartment: string;
}

export interface ContactPhoneCreateDtoInterface {
  phoneType: number;
  phone: string;
}
