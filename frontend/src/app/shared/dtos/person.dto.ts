export interface PersonDto {
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
  contactPhone: ContactPhoneDto[];
  address: AddressDto;
}

export interface AddressDto {
  localityId: number;
  locality: string;
  provinceId: number;
  province: string;
  countryId: number;
  country: string;
  street: string;
  number: number;
  floor: string;
  apartment: string;
}

export interface ContactPhoneDto {
  typePhone: string;
  phone: string;
}

export interface PersonCreateDto {
  id?: number;
  firstName: string;
  lastName: string;
  dniTypeId: number;
  dni: string;
  birthDate: Date;
  genderId: number;
  nationalityId: number;
  contactEmails: string[];
  contactPhones: ContactPhoneCreateDto[];
  address: AddressCreateDto;
}

export interface AddressCreateDto {
  localityId: number;
  street: string;
  number: number;
  floor: string;
  apartment: string;
}

export interface ContactPhoneCreateDto {
  phoneType: number;
  phone: string;
}
