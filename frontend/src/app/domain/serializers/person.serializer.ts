import { PersonDtoInterface } from "../dto/person.dto";
import { UserCreateDtoInterface, UserDtoInterface } from "../dto/user.dto";
import { PersonInterface } from "../interfaces/person.interface";
import { UserInterface } from "../interfaces/user.interface";

export class PersonSerializer {
  // static toView(person: PersonDtoInterface): PersonInterface {
  //   const personView: PersonInterface = {
  //     id: person.id,
  //     firstName: person.firstName,
  //     lastName: person.lastName,
  //     dniType: person.dniType,
  //     dni: person.dni,
  //     birthDate: person.birthDate,
  //   };
  //   return personView;
  // }
  // static toCreateDto(user: UserInterface): UserCreateDtoInterface | {} {
  //   return {};
  // }
}
