import {
  PersonCreateDtoInterface,
  PersonDtoInterface,
} from "../dto/person.dto";
import {
  DniTypeInterface,
  GenderInterface,
  NationalityInterface,
  PhoneTypeInterface,
} from "../interfaces/person-data.interface";
import { PersonInterface } from "../interfaces/person.interface";
import { PersonDataService } from "../../services/person-data.service";
import { inject } from "@angular/core";

export class PersonSerializer {
  private readonly personDataService = inject(PersonDataService);

  toCreateDto(person: PersonInterface): PersonCreateDtoInterface {
    return {
      id: person.id,
      firstName: person.firstName,
      lastName: person.lastName,
      dniTypeId: person.dniType.id,
      dni: person.dni,
      birthDate: person.birthDate,
      genderId: person.gender.id,
      nationalityId: person.nationality.id,
      contactEmails: person.contactEmails ? [person.contactEmails] : [],
      contactPhones: [
        {
          phoneType: person.phoneType.id,
          phone: person.phone,
        },
      ],
      address: {
        localityId: person.locality?.id,
        street: person.street,
        number: person.number,
        floor: person.floor,
        apartment: person.apartment,
      },
    };
  }

  toView(person: PersonDtoInterface): PersonInterface {
    return {
      id: person.id,
      firstName: person.firstName,
      lastName: person.lastName,
      dniType: this._getDniType(person.dniType),
      dni: person.dni,
      birthDate: person.birthDate,
      gender: this._getGender(person.gender),
      nationality: this._getNationality(person.nationality),
      contactEmails: person.contactEmails[0],
      phoneType: this._getPhoneType(person.contactPhone[0].typePhone),
      phone: person.contactPhone[0].phone,
      country: {
        id: person.address.countryId,
        name: person.address.country,
      },
      province: {
        id: person.address.provinceId,
        name: person.address.province,
      },
      locality: {
        id: person.address.localityId,
        name: person.address.locality,
      },
      street: person.address.street,
      number: person.address.number,
      floor: person.address.floor,
      apartment: person.address.apartment,
    };
  }

  private _getDniType(dniType: string): DniTypeInterface {
    return this.personDataService
      .dniTypes()
      .find((dt) => dt.dni == dniType) as DniTypeInterface;
  }

  private _getGender(gender: string): GenderInterface {
    return this.personDataService
      .genders()
      .find((g) => g.name == gender) as GenderInterface;
  }

  private _getNationality(nationality: string): NationalityInterface {
    return this.personDataService
      .nationalities()
      .find((n) => n.name == nationality) as NationalityInterface;
  }

  private _getPhoneType(phoneType: string): PhoneTypeInterface {
    return this.personDataService
      .phoneTypes()
      .find((pt) => pt.name == phoneType) as PhoneTypeInterface;
  }
}
