import { UserCreateDtoInterface } from "../dto/user.dto";
import { UserInterface } from "../interfaces/user.interface";

export class UserSerializer {
  // static toView(user: UserDtoInterface): UserInterface {
  //   const userView: UserInterface = {
  //     id: user.id,
  //     username: user.username,
  //     enabled: user.enabled,
  //     rolesList: user.rolesList,
  //     person: PersonSerializer.toView(user.person),
  //   };
  //   return userView;
  // }
  // static toCreateDto(user: UserInterface): UserCreateDtoInterface | {} {
  //   return {};
  // }

  static toCreateDto(user: UserInterface): UserCreateDtoInterface {
    const dto: Partial<UserCreateDtoInterface> = {};

    if (user.username) {
      dto.username = user.username;
    }

    if (user.password1) {
      dto.password1 = user.password1;
    }

    if (user.password2) {
      dto.password2 = user.password2;
    }

    if (user.rolesList?.length) {
      dto.rolesList = user.rolesList.map((role) => role.id);
    }

    if (user.person) {
      const person: any = {};

      if (user.person.firstName) person.firstName = user.person.firstName;
      if (user.person.lastName) person.lastName = user.person.lastName;
      if (user.person.dniType?.id) person.dniTypeId = user.person.dniType.id;
      if (user.person.dni) person.dni = user.person.dni;
      if (user.person.birthDate) person.birthDate = user.person.birthDate;
      if (user.person.gender?.id) person.genderId = user.person.gender.id;
      if (user.person.nationality?.id)
        person.nationalityId = user.person.nationality.id;
      if (user.person.contactEmails)
        person.contactEmails = [user.person.contactEmails];

      if (user.person.phoneType?.id && user.person.phone) {
        person.contactPhones = [
          {
            phoneType: user.person.phoneType.id,
            phone: user.person.phone,
          },
        ];
      }

      if (
        user.person.locality?.id ||
        user.person.street ||
        user.person.number ||
        user.person.floor ||
        user.person.apartment
      ) {
        person.address = {};
        if (user.person.locality?.id)
          person.address.localityId = user.person.locality.id;
        if (user.person.street) person.address.street = user.person.street;
        if (user.person.number) person.address.number = user.person.number;
        if (user.person.floor) person.address.floor = user.person.floor;
        if (user.person.apartment)
          person.address.apartment = user.person.apartment;
      }

      if (Object.keys(person).length > 0) {
        dto.person = person;
      }
    }

    if (user.licenseNumber || user.dentistSpecialty?.id) {
      dto.dentist = {
        licenseNumber: user.licenseNumber!,
        dentistSpecialtyId: user.dentistSpecialty?.id!,
      };
    }

    return dto as UserCreateDtoInterface;
  }
}
