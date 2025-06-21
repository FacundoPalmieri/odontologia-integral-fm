export interface GenderInterface {
  id: number;
  alias: string;
  name: string;
}

export interface NationalityInterface {
  id: number;
  name: string;
}

export interface PhoneTypeInterface {
  id: number;
  name: string;
}

export interface HealthPlanInterface {
  id: number;
  name: string;
}

export interface BaseInterface {
  id: number;
  name: string;
}

export interface DniTypeInterface {
  id: number;
  dni: string;
}

export interface ProvinceInterface extends BaseInterface {}
export interface NationalityInterface extends BaseInterface {}
export interface LocalityInterface extends BaseInterface {}
export interface CountryInterface extends BaseInterface {}
export interface PhoneTypeInterface extends BaseInterface {}
export interface DentistSpecialtyInterface extends BaseInterface {}
