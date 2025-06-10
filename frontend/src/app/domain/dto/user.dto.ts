import { DentistCreateDtoInterface, DentistDtoInterface } from "./dentist.dto";
import { PersonCreateDtoInterface, PersonDtoInterface } from "./person.dto";
import { RoleDtoInterface } from "./role.dto";

export interface UserDtoInterface {
  id: number;
  username: string;
  rolesList: RoleDtoInterface[];
  enabled: boolean;
  personResponseDTO: PersonDtoInterface;
  desntisResponseDTO?: DentistDtoInterface[];
}

export interface UserCreateDtoInterface {
  username: string;
  password1: string;
  password2: string;
  rolesList: number[];
  personCreateRequestDTO: PersonCreateDtoInterface;
  dentistCreateRequestDTO?: DentistCreateDtoInterface;
}
