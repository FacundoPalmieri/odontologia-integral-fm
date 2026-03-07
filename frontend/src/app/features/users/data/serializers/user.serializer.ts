import { inject } from "@angular/core";
import {
  DentistSpecialtyInterface,
  UserInterface,
} from "../interfaces/user.interface";
import { PersonSerializer } from "../../../../shared/serializers/person.serializer";
import { PersonDataService } from "../../../../shared/services/person-data.service";
import { UserCreateDto, UserDto } from "../dtos/user.dto";
import { RoleInterface } from "../../../roles/data/interfaces/role.interface";

export class UserSerializer {
  private readonly personSerializer = new PersonSerializer();
  private readonly personDataService = inject(PersonDataService);

  toCreateDto(user: UserInterface): UserCreateDto {
    const dto: Partial<UserCreateDto> = {};

    dto.enabled = user.enabled;
    if (user.id) {
      dto.id = user.id;
    }

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
      dto.person = this.personSerializer.toCreateDto(user.person);
    }

    if (user.dentist?.licenseNumber || user.dentist?.dentistSpecialty?.id) {
      dto.dentist = {
        licenseNumber: user.dentist.licenseNumber!,
        dentistSpecialtyId: user.dentist.dentistSpecialty?.id!,
      };
    }
    return dto as UserCreateDto;
  }

  toView(user: UserDto): UserInterface {
    return {
      id: user.id,
      username: user.username,
      rolesList: user.rolesList as RoleInterface[],
      enabled: user.enabled,
      person:
        user.person !== null ? this.personSerializer.toView(user.person) : null,
      dentist: {
        licenseNumber: user.dentist?.licenseNumber,
        dentistSpecialty: user.dentist?.dentistSpecialty,
      },
    };
  }

  _getDentistSpecialty(dentistSpecialty: string): DentistSpecialtyInterface {
    return this.personDataService
      .dentistSpecialties()
      .find((ds) => ds.name == dentistSpecialty) as DentistSpecialtyInterface;
  }
}
