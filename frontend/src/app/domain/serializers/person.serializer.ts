import { PersonCreateDtoInterface } from "../dto/person.dto";
import { PersonInterface } from "../interfaces/person.interface";

export class PersonSerializer {
  static toCreateDto(person: PersonInterface): PersonCreateDtoInterface {
    return {
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
        localityId: person.locality.id,
        street: person.street,
        number: person.number,
        floor: person.floor,
        apartment: person.apartment,
      },
    };
  }
}
