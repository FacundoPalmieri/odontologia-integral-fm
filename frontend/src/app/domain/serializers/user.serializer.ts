import { UserCreateDtoInterface } from "../dto/user.dto";
import { UserInterface } from "../interfaces/user.interface";
import { PersonSerializer } from "./person.serializer";

export class UserSerializer {
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
      dto.person = PersonSerializer.toCreateDto(user.person);
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
